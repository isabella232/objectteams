/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PackageExplorerAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.packageview;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleFileType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.Messages;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;

import base org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerActionGroup;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerLabelProvider;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import base org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart.PackageExplorerProblemTreeViewer;

/**
 * @author stephan
 * 
 * @role ContentProvider
 */
@SuppressWarnings("restriction")
public team class PackageExplorerAdaptor {
	
	@SuppressWarnings("decapsulation")
	public class PartAdaptor playedBy PackageExplorerPart
	{
		private boolean fIsShowingCallinLabel = true;
		
		void createPartControl(Composite parent) {
			HideExternalRolePackagesAction hideRolesAction = new HideExternalRolePackagesAction(this);
			//TODO: this should be saved and restored somewhere!
			ContentProvider contentProvider = getContentProvider();
			if (contentProvider != null) // is null when base is MockPluginView
				hideRolesAction.setChecked(contentProvider.isHideRolePackages());
										
			updateToolBar(hideRolesAction);
		}
		createPartControl <- after createPartControl;
		
		@SuppressWarnings("basecall")
		callin LabelProvider createLabelProvider() {
			long textFlags = AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED;
			int imageFlags = AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS;
			int otTextFlags = LabelProvider.CALLIN_DEFAULT;
			return new LabelProvider(textFlags, imageFlags, otTextFlags, getContentProvider());
		}
		createLabelProvider <- replace createLabelProvider;

		/**
		 * Add button for toggling view of external defined roles 
		 */    
		protected void updateToolBar(HideExternalRolePackagesAction hideRolesAction)
		{
			IActionBars     actionBar = getViewSite().getActionBars();
			IToolBarManager tbManager = actionBar.getToolBarManager();

			// save original toolbar items and insert in new order
			IContributionItem[] items = tbManager.getItems();
			tbManager.removeAll();				

			for (int idx = 0; idx < items.length; idx++)
			{
				tbManager.add(items[idx]);
				// insert new action after group marker
				if (items[idx] instanceof GroupMarker)
					if (((GroupMarker)items[idx]).getId().equals(PackageExplorerActionGroup.getFRAME_ACTION_GROUP_ID()))
						tbManager.add(hideRolesAction);
				 
			}
		}
		public void setCallinFormatting(boolean showLabel) {
			if (showLabel == fIsShowingCallinLabel)
				return;
			fIsShowingCallinLabel = showLabel;
			getLabelProvider().setCallinFormatting(fIsShowingCallinLabel);
			
			TreeViewer viewer = getTreeViewer();
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}

		boolean adjustInput(Object input) <- replace boolean showInput(Object input);
		callin boolean adjustInput(Object input) {
			if (getContentProvider().isHideRolePackages()) {
				if (input instanceof ICompilationUnit) {
					try {
						IType[] types = ((ICompilationUnit)input).getTypes();
						for(int typeIdx = 0; typeIdx < types.length; typeIdx++)
						{
							IOTType otType = OTModelManager.getOTElement(types[typeIdx]);
							if (otType != null) {
								input = otType;
								break;
							}
						}
					} catch (JavaModelException e) {
						// fall through to base behavior
					}
				}
			}
			return base.adjustInput(input);
		}
		
		
		
		protected
		ContentProvider getContentProvider() -> get PackageExplorerContentProvider fContentProvider;
		LabelProvider   getLabelProvider()   -> get PackageExplorerLabelProvider fLabelProvider;
		TreeViewer      getTreeViewer()      -> TreeViewer getTreeViewer();
		IViewSite       getViewSite()        -> IViewSite getViewSite();
		
	}
	
	protected class HideExternalRolePackagesAction extends Action {
	
	    private PartAdaptor _part;
		
	    public HideExternalRolePackagesAction(PartAdaptor part) {
			super(Messages.PackageExplorer_DisplayRoleFilesAction);
			
			_part = part;
	                
			ImageManager.getSharedInstance().setActionImageDescriptors(
												this, ImageConstants.HIDE_TEAMPACKAGE);        
	        
			setDescription(Messages.PackageExplorer_DisplayRoleFilesDescription);
			setToolTipText(Messages.PackageExplorer_DisplayRoleFilesTooltip);

			setChecked(this._part.getContentProvider().isHideRolePackages());
	    }

		/**
		 * Toggle visibility of packages containing external roles and
		 * refresh Package Explorer TreeViewer
		 */
		public void run() {
		    ContentProvider provider = _part.getContentProvider();
		    provider.setHideRolePackages(!provider.isHideRolePackages()); // toggle
			        
			TreeViewer viewer  = _part.getTreeViewer();

			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}
	}
	
	@SuppressWarnings("decapsulation")
	protected class PackageTreeViewer playedBy PackageExplorerProblemTreeViewer {

		ContentProvider getContentProvider() -> IContentProvider getContentProvider()
			with { result <- (PackageExplorerContentProvider)result }

		void add(Object parent, Object child) <- replace void add(Object parentElementOrTreePath, Object childElement);

		@SuppressWarnings("basecall") // suppressing base call if child should in fact be hidden
		callin void add(Object parent, Object child) {
			try {
				if (   child instanceof IPackageFragment
					&& getContentProvider().isHideRolePackages())
				{
					if (containsExternalRoles((IPackageFragment) child)) 
						return; // don't add!
				}
			} catch (Exception e) { // also catch potential CCE in callout getContentProvider()
				// ignore
			}
			base.add(parent, child);
		}
		
	}
	
	protected class LabelProvider playedBy PackageExplorerLabelProvider {
		// Technical note: class org.eclipse.objectteams.otdt.internal.ui.WorkbenchAdapter already provides
		// default labels for all OT-elements including method mappings.
		// However, the implicit singleton WorkbenchAdapter is not aware of who is calling,
		// so it cannot be configured to select between different possible representations.
	    /**
	     * Whether the name of a callin (aka 'callin label' shall be included in the text label)
	     */
	    public static final int CALLIN_NAME = 1;
	    /**
	     * Whether the callin declaration shall be included in the text label)
	     */
	    public static final int CALLIN_DECL = 2;
	    /**
	     * The user-configured representation of callins.
	     */
	    public static final int CALLIN_DEFAULT = 4;
	    
	    protected int _otTextFlags = CALLIN_NAME | CALLIN_DECL;

	    /** 
	     * Create an OT-aware label provider wrapping a PackageExplorerLabelProvider.
	     * 
	     * @param otTextFlags how should callins be printed (see constants above)?
	     */
	    protected LabelProvider(long textFlags, int imageFlags, int otTextFlags, ContentProvider contentProvider) {
	    	base(contentProvider);
	    	_otTextFlags = checkOTTextFlags(otTextFlags);
	    }
	
	    protected void setCallinFormatting(boolean isShowingCallinLabel) {
	    	if (isShowingCallinLabel)
	    		_otTextFlags |= CALLIN_NAME;
	    	else
	    		_otTextFlags &= ~CALLIN_NAME;
	    }
	
		@SuppressWarnings("basecall")
		callin String getText(Object element) {
		    if (element instanceof IMethodMapping)
		    	return getMethodMappingText((IMethodMapping)element);
		    return base.getText(element);
		}
		getText <- replace getText;
		
		protected int checkOTTextFlags(int newFlags)
	    {
		    if ((newFlags & CALLIN_DEFAULT) != 0)
		        return getDefaultCallinFlags();
		    
		    if ((newFlags & (CALLIN_DECL | CALLIN_NAME)) == 0) 
		        throw new IllegalArgumentException("Must set at least CALLIN_DECL or CALLIN_NAME"); //$NON-NLS-1$
		    
		    return newFlags;
	    }
		
	    protected int getDefaultCallinFlags()
	    {
	        // FIXME: ask preference store
	        return CALLIN_DECL | CALLIN_NAME;
	    }
	    
		public String getMethodMappingText(IMethodMapping mapping)
		{
		    if (mapping instanceof ICallinMapping)
		    {
		        ICallinMapping callinMapping = (ICallinMapping) mapping;
	            StringBuffer buf = new StringBuffer();

		        if ((_otTextFlags & CALLIN_NAME) != 0)
			    {
			        if (callinMapping.hasName())
			        {
			            buf.append(callinMapping.getName());
			            if ((_otTextFlags & CALLIN_DECL) != 0)
			                buf.append(": "); //$NON-NLS-1$
			        }
			    }

		        if ((_otTextFlags & CALLIN_DECL) != 0)
			        buf.append(callinMapping.getElementName());

		        return buf.toString();
		    }
		    
		    return mapping.getElementName();
		}
	}
	/** gateway only. */
	protected class ClassPathContainer playedBy ClassPathContainer 
	{
		protected ClassPathContainer(IJavaProject project, IClasspathEntry entry) {
			base(project, entry);
		}
		@SuppressWarnings("decapsulation")
		protected
		boolean contains(IJavaProject project, IClasspathEntry entry, IPackageFragmentRoot root)
			-> boolean contains(IJavaProject project, IClasspathEntry entry, IPackageFragmentRoot root);
	}
	
	@SuppressWarnings("decapsulation")
	protected class PackageExplorerActionGroup playedBy PackageExplorerActionGroup {
		callin void setGroups(ActionGroup[] groups) {
			int len = groups.length;
			System.arraycopy(groups, 0, groups=new ActionGroup[len+1], 0, len);
			groups[len] = new OTLayoutActionGroup(PackageExplorerAdaptor.this, this.getPart());
			base.setGroups(groups);
		}
		setGroups <- replace setGroups;
		
		PartAdaptor getPart() -> get PackageExplorerPart fPart;
		
		protected
		String getFRAME_ACTION_GROUP_ID() -> get String FRAME_ACTION_GROUP_ID;
	}
	
	// === Common lookup functions: ===
	/** Is the element a package fragment representing a team package? */
	boolean isExternalRolePackage(Object element)
		throws JavaModelException
	{
	    //TODO (haebor) what about caching already found results
	    if((element instanceof IPackageFragment)
            && (containsExternalRoles((IPackageFragment)element)))
	    {
	        return true;
	    }
	    return false;
	}
	/** Does the package fragment represent a team package (=contain role files)? */
	boolean containsExternalRoles(IPackageFragment packageFragment)
		throws JavaModelException
	{
	    IJavaElement[] children = packageFragment.getChildren();
	    for(int idx = 0; idx < children.length; idx++)
	    {
	        IJavaElement current = children[idx]; 
	        if((current instanceof ICompilationUnit))
	        {
	            IType[] types = ((ICompilationUnit)current).getTypes();
	            for(int typeIdx = 0; typeIdx < types.length; typeIdx++)
	            {
	                IType currentType = types[typeIdx];
	                IOTType otType = 
	                    OTModelManager.getOTElement(currentType);
		            if (otType != null && otType instanceof IRoleFileType)
			        {
			            return true;
			        }
	            }
	        }
	    }
	    return false;
	}
	
	// === Test Infrastructure ===
	private static PackageExplorerAdaptor instance;
	
	public PackageExplorerAdaptor() {
		instance = this;
	}
	public static PackageExplorerAdaptor getInstance() {
		return instance;
	}
	public void setShowTeamPackages(boolean show, PackageExplorerContentProvider as ContentProvider contentProvider) {
		contentProvider.setHideRolePackages(!show);
	}
}
