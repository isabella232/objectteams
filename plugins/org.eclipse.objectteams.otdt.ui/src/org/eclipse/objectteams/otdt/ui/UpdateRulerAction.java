/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: UpdateRulerAction.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.callinmarkers.CallinMarker;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that is executed when the user right clicks on a binding marker at
 * the vertical ruler and chooses a mapping (bound role class or callin mapping)
 * from the ObjectTeams submenu.
 *
 * @author brcan
 * @version $Id: UpdateRulerAction.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class UpdateRulerAction extends AbstractRulerActionDelegate
{
	public final static String OT_PLAYEDBY_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_playedby_title"); //$NON-NLS-1$
	public final static String OT_CALLIN_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_callin_title"); //$NON-NLS-1$
	public final static String OT_CALLOUT_MENU_LABEL = OTDTUIPlugin.getResourceString("CallinMarker.menu_callout_title"); //$NON-NLS-1$
	
	private IEditorPart        _editor = null;
	private IVerticalRulerInfo _rulerInfo = null;

	public UpdateRulerAction()
	{
	}

	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor)
	{
		_editor = targetEditor;

		super.setActiveEditor(callerAction, targetEditor);
	}
	
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo)
	{
		_rulerInfo = rulerInfo;
    	
		return null;
	}
	
	public void menuAboutToShow(IMenuManager contextMenu)
	{
		try
		{
			IMarker[] markers = findMarkers();

			if (markers != null && markers.length != 0)
			{				
				Integer      clickedLine = new Integer(_rulerInfo.getLineOfLastMouseButtonActivity() + 1);
				IDocument    document    = null;
				if (this._editor instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) this._editor;
					document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
				}
				for (int idx = 0; idx < markers.length; idx++)
				{
					IMarker curMarker = markers[idx];

					Object lineAttribute = curMarker.getAttribute(IMarker.LINE_NUMBER);
					if (lineAttribute != null)
					{
						if (lineAttribute.equals(clickedLine))
							insertTeamMenus(contextMenu, curMarker);
					} else if (document != null) {
						// markers in ClassFileEditor have no line number, must go via position:
						Integer startAttribute = (Integer) curMarker.getAttribute(IMarker.CHAR_START);
						try {
							if (clickedLine.equals(document.getLineOfOffset(startAttribute)+1))
								insertTeamMenus(contextMenu, curMarker);
						}
						catch (BadLocationException e) { /* nop */ }
					} 
				}
			}
		}
		catch (CoreException ex)
		{
			OTDTUIPlugin.getExceptionHandler().logCoreException("Problems extending ruler context menu", ex); //$NON-NLS-1$
		}
	}

	private IMarker[] findMarkers() throws CoreException
	{
		
		final IEditorInput editorInput = _editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) 
		{
			IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
			IFile            file            = fileEditorInput.getFile();			
	
			IMarker[] result = getAllBindingMarkers(file);
			return result;
		} 
		else if (editorInput instanceof IClassFileEditorInput) 
		{			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IMarker[] allMarkers = getAllBindingMarkers(root);
			
			// now we have all CallinMarkers for all class files in the workspace, need to filter now: 
			IClassFile classFile = ((IClassFileEditorInput) editorInput).getClassFile();
			List<IMarker> filteredMarkers = new ArrayList<IMarker>(13);
			for (IMarker marker : allMarkers)
				if (JavaCore.isReferencedBy(classFile, marker))
					filteredMarkers.add(marker);
			return filteredMarkers.toArray(new IMarker[filteredMarkers.size()]);
		}
		return null;
	}

	/** get all playedBy and callin markers for the given resource. */
	private IMarker[] getAllBindingMarkers(IResource resource) throws CoreException {
		IMarker[] markers1 = resource.findMarkers(CallinMarker.PLAYEDBY_ID, true, IResource.DEPTH_INFINITE);
		IMarker[] markers2 = resource.findMarkers(CallinMarker.CALLIN_ID, true, IResource.DEPTH_INFINITE);
		IMarker[] markers3 = resource.findMarkers(CallinMarker.CALLOUT_ID, true, IResource.DEPTH_INFINITE);
		int len1 = markers1.length, len2 = markers2.length, len3 = markers3.length;
		IMarker[] result = new IMarker[len1+len2+len3];
		System.arraycopy(markers1, 0, result, 0, len1);
		System.arraycopy(markers2, 0, result, len1, len2);
		System.arraycopy(markers3, 0, result, len1+len2, len3);
		return result;
	}

	/** Get the context menu of kind 'markerKind', creating it if ondemand. */
    private IMenuManager getObjectTeamsMenu(IMenuManager contextMenu, String markerKind)
    {
    	String label;
    	if (CallinMarker.CALLIN_ID.equals(markerKind))
    		label = OT_CALLIN_MENU_LABEL;
    	else if (CallinMarker.CALLOUT_ID.equals(markerKind))
    		label = OT_CALLOUT_MENU_LABEL;
    	else
    		label = OT_PLAYEDBY_MENU_LABEL;
    	
    	IMenuManager subMenu = getSubMenu(contextMenu, label);
    	if (subMenu != null)
    		return subMenu;
    	
		MenuManager otMenu = new MenuManager(label, markerKind); // id cannot be null, re-use the markerKind
		if (contextMenu.isEmpty())
		    contextMenu.add(otMenu);
		else // insert on top
		    contextMenu.insertBefore(contextMenu.getItems()[0].getId(), otMenu);
		return otMenu;
    }

    private IAction createOpenEditorAction(String label, final IJavaElement target)
    {
		Action result = new Action(label)
		{
			public void run()
			{
				try
				{
					IEditorPart part = EditorUtility.openInEditor(target);
					if (target.exists()) // also initializes source positions if necessary
						EditorUtility.revealInEditor(part, target);
				}
				catch (PartInitException ex)
				{
					OTDTUIPlugin.getExceptionHandler().logException("Problems initializing editor", ex); //$NON-NLS-1$
				}
			}
		};
    	
        return result;
    }

	List<IMember> getMappings (IMarker marker) throws CoreException 
	{
    	Object attr = marker.getAttribute(CallinMarker.ATTR_ROLE_ELEMENTS);
    	if (attr == null || !(attr instanceof String))
    		return null;
    	String str = (String)attr;
    	List<IMember> result = new ArrayList<IMember>();
    	int start = 0;
    	int pos;
    	while ((pos = str.indexOf('\n', start)) != -1) {
    		result.add((IMember)JavaCore.create(str.substring(start, pos)));
    		start = pos+1;
    	}
    	return result;
    }
    
    private void insertTeamMenus(IMenuManager contextMenu, IMarker marker) throws CoreException
    {
    	List<IMember> mappings = getMappings(marker);
    	if (mappings == null) return;
    	
    	IMenuManager otMenu = getObjectTeamsMenu(contextMenu, marker.getType());
    	
        for (IMember curMapping : mappings) 
        {
        	IType type = (IType)(curMapping.getAncestor(IJavaElement.TYPE));
        	IOTType otType = OTModelManager.getOTElement(type); // FIXME(SH): doesn't find role files??? (try StubUtility2)
        	if (otType == null || !otType.isRole())
        		continue;
			
        	IOTType teamType = ((IRoleType) otType).getTeam();
 			
			IMenuManager curTeamMenu = null;
					 			
 			if (!isSubMenuContained(otMenu, teamType.getElementName()))
 			{
	            curTeamMenu = new MenuManager(teamType.getElementName());
				otMenu.add(curTeamMenu);
 			}
 			else
 			{
 				curTeamMenu = getSubMenu(otMenu, teamType.getElementName());
 			}
			String actLabel = getMappingLabel(type, curMapping);
			curTeamMenu.add(createOpenEditorAction(actLabel, curMapping));
        }
    }

    private String getMappingLabel(IType type, IMember mapping)
    {
    	if (type.equals(mapping)) return type.getElementName();
    	
        return type.getElementName() + ": " + mapping.getElementName(); //$NON-NLS-1$
    }

    private IMenuManager getSubMenu(IMenuManager otMenu, String subMenuName)
    {
    	if (otMenu == null)
    		return null;

    	IContributionItem[] items = otMenu.getItems();
    	
    	for (int idx = 0; idx < items.length; idx++)
        {
            if (items[idx] instanceof IMenuManager)
            {
            	MenuManager cur = (MenuManager)items[idx];
            	if (cur.getMenuText().equals(subMenuName))
            	{
            		return cur; 
            	}
            }
        }
        
        return null;
    }

    private boolean isSubMenuContained(IMenuManager menu, String subMenuName)
    {
        return getSubMenu(menu, subMenuName) != null;
    }
}
