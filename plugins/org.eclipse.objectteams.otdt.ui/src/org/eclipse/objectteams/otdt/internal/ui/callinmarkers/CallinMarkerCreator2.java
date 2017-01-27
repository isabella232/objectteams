/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2015 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.ext.IMarkableJavaElement;
import org.eclipse.objectteams.otdt.core.ext.MarkableFactory;
import org.eclipse.objectteams.otdt.core.search.OTSearchHelper;
import org.eclipse.objectteams.otdt.internal.ui.preferences.GeneralPreferences;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * This class creates Object Teams binding markers of two kinds:
 * <ul>
 * <li>playedBy markers marking base classes bound by playedBy
 * <li>callin markers marking base methods bound by callin.
 * </ul>
 * It also monitors editor and resource changes and automatically creates/removes callin
 * markers for all methods.
 * 
 * TODO(SH): validate updating for methods and implement something similar for types, too.
 * 
 * @author carp
 */
public class CallinMarkerCreator2 extends JavaEditorActivationListener
{
	
	protected CallinMarkerJob _currentJob;
	private Map<IJavaElement,IType> m_cachedBaseForRole = new HashMap<IJavaElement,IType>();
    private Set<IResource> m_cachedMarkersForResources = new HashSet<IResource>();
    private Set<IJavaElement> m_cachedMarkersForJavaElements = new HashSet<IJavaElement>();
    private boolean m_enabled = false;
    protected AnnotationHelper annotationHelper;
    private boolean isInitialized = false;

	/**
	 * Typical usage:
	 * <code>
	 * CallinMarkerCreator creator = OTDTUIPlugin.getCallinMarkerCreator();
	 * creator.updateAllMarkers(myMethod, someProgressMonitor);
	 * </code>
	 */
	public CallinMarkerCreator2()
	{
	    IPreferenceStore store = OTDTUIPlugin.getDefault().getPreferenceStore();
	    store.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (GeneralPreferences.CALLIN_MARKER_ENABLED_BOOL.equals(event.getProperty()))
                {
                    boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
                    setEnabled(newValue);
                    // FIXME: upon enablement, run callin marker creator for current editor, if any
                }
            }
        });
	    
	    setEnabled(store.getBoolean(GeneralPreferences.CALLIN_MARKER_ENABLED_BOOL));
	}
	
	public void setEnabled(boolean enable)
	{
	    if (m_enabled == enable)
	        return;
	    
	    if (enable)
	        installListener();
	    else
	        uninstallListener();

	    m_enabled = enable;
	}
	
	public boolean isEnabled()
	{
	    return m_enabled;
	}
	
	/** API for {@link RoleBindingChangedListener}. */
    public void updateCallinMarker(final IMember member, IStatusLineManager statusLine)
    {
        if (!isEnabled())
            return;
        
        if (!isCached(member.getResource()) && !isCreatingMarkersFor(member.getResource()))
            return;
        
        final IMarkableJavaElement target = MarkableFactory.createMarkable(member.getResource());
        CallinMarkerJob job = new CallinMarkerJob(target) {
            protected void updateMarkers(IProgressMonitor monitor) throws Exception {
                updateCallinMarker(target, member, monitor);
            }
        };
        
        // updating just one single method will not set current _currentJob
        schedule(job, statusLine);
    }
    
    
    /**
     * API for {@link RoleBindingChangedListener}.
     * When some role bindings have changed remove cached information regarding its base. 
	 */
	public void invalidateRole(IType roleType, IType baseClass) {
		if (baseClass != null)
			invalidateBase(baseClass);
		IType cachedBaseClass = this.m_cachedBaseForRole.get(roleType);
		if (   cachedBaseClass != null
			&& !cachedBaseClass.equals(baseClass)) 
		{
			// playedBy mapping itself has changed
			this.m_cachedBaseForRole.remove(roleType);
			invalidateBase(cachedBaseClass);
		}
	}
	public void invalidateBase(IJavaElement baseClass) {
		IMarkableJavaElement baseMarkable = MarkableFactory.createMarkable(baseClass);
		IEditorPart editor = (IEditorPart) this.fActiveEditor;
		if (baseMarkable.isBinary()) {
			IJavaElement baseJavaElement = baseMarkable.getJavaElement();
			if (baseJavaElement != null) {
				this.m_cachedMarkersForJavaElements.remove(baseJavaElement);
				if (this.fActiveEditor != null && this.fActiveEditor instanceof IEditorPart) {
					if (editor.getEditorInput() instanceof IClassFileEditorInput) {
						IClassFile editorClassFile = ((IClassFileEditorInput)editor.getEditorInput()).getClassFile();
						if (editorClassFile != null && !isCreatingMarkersFor(editorClassFile) && editorClassFile.equals(baseJavaElement))
							updateForBaseMarkable(baseMarkable, editor);
					}
				}
			}
		} else {
			IResource baseResource = baseMarkable.getResource();
			this.m_cachedMarkersForResources.remove(baseResource);
			if (this.fActiveEditor != null && this.fActiveEditor instanceof IEditorPart) {
				if (editor.getEditorInput() instanceof IFileEditorInput) {
					IResource editorResource = ((IFileEditorInput)editor.getEditorInput()).getFile();
					if (editorResource != null && !isCreatingMarkersFor(editorResource) && editorResource.equals(baseResource))
						updateForBaseMarkable(baseMarkable, editor);
				}
			}
		}
	}

	private void updateForBaseMarkable(IMarkableJavaElement baseMarkable,
			IEditorPart editor) {
		IStatusLineManager statusLine = editor.getEditorSite().getActionBars().getStatusLineManager();
		updateCallinMarkers(baseMarkable, statusLine);
	}
	
    /**
     * When the editor input changed find the markable target and update markers.
     */
    protected void activeJavaEditorChanged(IWorkbenchPart editor)
    {
        if (!(editor instanceof IEditorPart))
        {
        	if (editor == null) {
        		this.annotationHelper = null;
        		this.fActiveEditor = null;
        	}
            return;
        }
        
        IEditorPart targetEditor = (IEditorPart) editor;
		final IEditorInput editorInput = targetEditor.getEditorInput();
		final IStatusLineManager statusLine = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		
		this.fActiveEditor = editor;
		this.annotationHelper = new AnnotationHelper(targetEditor, editorInput);
		
		IMarkableJavaElement target= null;
		if ((editorInput instanceof IFileEditorInput)) { 			// source file
			IResource resource = ((IFileEditorInput)editorInput).getFile();
			if (resource == null || isCached(resource) || isCreatingMarkersFor(resource))
				return; // already has markers -- skip it
			target = MarkableFactory.createMarkable(resource);
		} else if (editorInput instanceof IClassFileEditorInput) {	// binary java element
			IClassFile element = ((IClassFileEditorInput) editorInput).getClassFile();
			if (element == null || isCached(element) || isCreatingMarkersFor(element))
				return; // already has markers -- skip it
			target = MarkableFactory.createMarkable(element);
		} else {
			return; // unexpected editor input
		}
		
		if (target.exists())
			updateCallinMarkers(target, statusLine);
    }

    // FIXME: listen to classpath changes as well
    // hint from the news group:
    // See:
    // - org.eclipse.jdt.core.JavaCore.addElementChangedListener(IElementChangedListener, int)
    // - org.eclipse.jdt.core.IJavaElementDelta.F_CLASSPATH_CHANGED
    
    /**
     * @param resource the resource in where to look for bound base methods
     * @param statusLine a status line where errors can be displayed or null if you don't want error messages
     */
    public void updateCallinMarkers(final IMarkableJavaElement target, IStatusLineManager statusLine)
    {
    	IJavaElement javaElement = target.getJavaElement();
		IJavaElement pack = javaElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    	if (pack != null) {
			if (pack.getElementName().startsWith("java.lang")) //$NON-NLS-1$
				return;
			if (pack.getElementName().equals("java.io") && javaElement.getElementName().startsWith("Serializable")) //$NON-NLS-1$ //$NON-NLS-2$
				return;
		}
		CallinMarkerJob job = new CallinMarkerJob(target) {
			protected void updateMarkers(IProgressMonitor monitor) throws Exception {
				updateCallinMarkers(target, monitor);
			}
		};
		
		setCurrentJob(job);
		schedule(job, statusLine);
    }

    /**
     * For a given markable target remove all old markers and create new ones.
     * 
     * @param target  the markable item to create markers for
     * @param monitor
     * @throws CoreException thrown when one of the many searches and or lookups failed.
     */
    private void updateCallinMarkers(final IMarkableJavaElement target, IProgressMonitor monitor) throws CoreException
	{
        try 
        {
	        if (target == null || !target.exists())
	            return;
            
	        monitor.beginTask(
	                NLS.bind(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.updating_for_resource_message"),  //$NON-NLS-1$
	                         new Object[] { target.getName() }), 62);
	        
		    target.removeMarkers(CallinMarker.CALLIN_MARKER_IDS);
		    monitor.worked(2);
	    
		    // ==== role bindings: ====
		    IJavaProject[] projects = target.getProjects();
		    Set<IType> allTypes = target.getAllTypes(projects, monitor); // 10 ticks
		    Map<IMember, Set<IType>> playedByMap = OTSearchHelper.searchPlayedByBindings(allTypes, projects, new MySubProgressMonitor(monitor, 20));
			if (playedByMap == null || playedByMap.size() == 0)
				return; // no base types or cancelled

			createMarkersFor(target, playedByMap, CallinMarker.PLAYEDBY_ID, new MySubProgressMonitor(monitor, 5));

		    // collect all roles for use as search scope:
		    IMember[] allRoleTypes = collectRolesAndSubroles(playedByMap, new MySubProgressMonitor(monitor, 10));
		    if (monitor.isCanceled())
		    	return;
		    
		    // ==== callin/callout bindings: ====
		    Set<IMember> allMembers = getAllMethodsAndFields(target.getJavaElement());
		    monitor.worked(5);
			updateMethodMarkers(target, allRoleTypes, allMembers, new SubProgressMonitor(monitor, 10));
        }
        finally {
            monitor.done();
        }
	}

	private IMember[] collectRolesAndSubroles(Map<IMember, Set<IType>> playedByMap, IProgressMonitor submon)
			throws JavaModelException 
	{
		IMember[] allRoleTypes;
		Set<IMember> collectedRoleTypes = new HashSet<IMember>();
		submon.beginTask("Finding sub roles", playedByMap.size()); //$NON-NLS-1$
		for (Set<IType> value : playedByMap.values()) {
			collectedRoleTypes.addAll(value);
			for (IType member : value) {
				if (submon.isCanceled()) return null;
				if (member.exists() && OTModelManager.isRole(member)) {
					IType[] subtypes = member.newTypeHierarchy(submon).getAllSubtypes(member);
					for (IType subtype : subtypes)
						collectedRoleTypes.add(subtype);
				}
				submon.worked(1);
			}		    		
		}
		allRoleTypes = collectedRoleTypes.toArray(new IMember[collectedRoleTypes.size()]);
		submon.done();
		return allRoleTypes;
	}
	
    private void updateCallinMarker(IMarkableJavaElement target, IMember member, IProgressMonitor monitor) throws CoreException
	{
    	// TODO(SH): in this scenario a search for all callins mentioning method should be faster.
	    try 
	    {
	        monitor.beginTask(
	                NLS.bind(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.updating_for_method_message"),  //$NON-NLS-1$
	                         new Object[] { member.getElementName() }), 45);
	        
	        IResource resource = member.getResource();
			if (resource != null) // only called when base class changed -> source only.
			{
			    CallinMarkerRemover.removeCallinMarker( member, resource );
			    monitor.worked(5);
			    
			    // find direct roles:
			    ArrayList<IType> allTypes = new ArrayList<IType>(1);
			    allTypes.add((IType)member.getAncestor(IJavaElement.TYPE)); // TODO(SH): could be IOTType?
			    Map<IMember, Set<IType>> playedByMap = OTSearchHelper.searchPlayedByBindings(allTypes,
			    															  new IJavaProject[]{member.getJavaProject()}, 
			    															  new MySubProgressMonitor(monitor, 20));
			    if (playedByMap == null || playedByMap.isEmpty())
			    	return; // no base types or cancelled

			    // collect all roles w/ subroles for use as search scope:
			    IMember[] allRoleTypes = collectRolesAndSubroles(playedByMap, new MySubProgressMonitor(monitor, 10));
			    if (monitor.isCanceled())
			    	return;
			    
			    ArrayList<IMember> memberSet = new ArrayList<IMember>(1);
			    memberSet.add(member);
			    updateMethodMarkers(target, allRoleTypes, memberSet, new SubProgressMonitor(monitor, 10));
			}
	    }
	    finally {
	        monitor.done();
	    }
	}

	private void updateMethodMarkers(IMarkableJavaElement target, IMember[] allRoleTypes, Collection<IMember> memberSet, IProgressMonitor monitor) 
	{
		try {
			monitor.beginTask(null, 40);
			Map<IMember, Set<IMember>> callinMap = new HashMap<IMember, Set<IMember>>();
			Map<IMember, Set<IMember>> calloutMap = new HashMap<IMember, Set<IMember>>();
			searchMemberBindings(memberSet, allRoleTypes, callinMap, calloutMap, new MySubProgressMonitor(monitor, 20));
			if (monitor.isCanceled()) return;
			createMarkersFor(target, callinMap, CallinMarker.CALLIN_ID, new MySubProgressMonitor(monitor, 10));
			createMarkersFor(target, calloutMap, CallinMarker.CALLOUT_ID, new MySubProgressMonitor(monitor, 10));
		} finally {
			monitor.done();
		}
	}
	
    private void setCurrentJob(CallinMarkerJob job)
    {
	    synchronized (this) {
	        if (_currentJob != null) {
	            _currentJob.cancel();
	        }
	        
	        _currentJob = job;
	    }
    }
    
    // ------ Some of these methods are duplicated to account for the source/binary duality: ------
    public boolean isCreatingMarkersFor(IResource resource)
    {
        synchronized(this) {
            if (_currentJob == null) return false;
            IResource jobResource = _currentJob.getResource();
			return (jobResource != null) && jobResource.equals(resource);
        }
    }
    
    private boolean isCreatingMarkersFor(IJavaElement element)
    {
        synchronized(this) {
            if (_currentJob == null) return false;
            IJavaElement javaElement = _currentJob.getJavaElement();
			return (javaElement != null) && javaElement.equals(element);
        }
    }

    private boolean isCached(IResource resource)
    {
        return m_cachedMarkersForResources.contains(resource);
    }
    private boolean isCached(IJavaElement element)
    {
    	return m_cachedMarkersForJavaElements.contains(element);
    }

    private void setCached(final IResource resource)
    {
        m_cachedMarkersForResources.add(resource);
    }
    private void setCached(final IJavaElement element)
    {
    	m_cachedMarkersForJavaElements.add(element);
    }
    
    private void removeFromCache(final IResource resource)
    {
        m_cachedMarkersForResources.remove(resource);
    }
    private void removeFromCache(final IJavaElement element)
    {
        m_cachedMarkersForJavaElements.remove(element);
    }

    private void removeFromCache(IWorkbenchPartReference ref)
    {
        IWorkbenchPart part = ref.getPart(false);
        if (part instanceof IEditorPart)
        {
            IEditorInput input = ((IEditorPart) part).getEditorInput();
            if (input instanceof IFileEditorInput)
                removeFromCache(((IFileEditorInput) input).getFile());
        }
    }
    
    public void partClosed(IWorkbenchPartReference ref)
    {
        removeFromCache(ref);
        super.partClosed(ref);
    }
    
    /**
     * Fetch all methods and fields contained in the input.
	 * @param javaElement the corresponding ICompilationUnit, IClassFile or IType
	 */
    private Set<IMember> getAllMethodsAndFields(IJavaElement javaElement) {
		if (javaElement == null)
			return new HashSet<IMember>(0);

		Set<IMember> members = new HashSet<IMember>(13);
		
		switch (javaElement.getElementType())
		{
			case IJavaElement.COMPILATION_UNIT:
			{
				ICompilationUnit unit  = (ICompilationUnit)javaElement;
				IType[] types;
				try {

					types = unit.getTypes();
					for (int idx = 0; idx < types.length; idx++)
						members.addAll(getAllMethodsAndFields(types[idx]));

				} catch (JavaModelException e) {
					// ignore, without types we simply find no methods
				}				
                break;
			}
			case IJavaElement.CLASS_FILE:
			{
				IClassFile classFile = (IClassFile)javaElement;
				members.addAll(getAllMethodsAndFields(classFile.getType()));
				break;
			}
			case IJavaElement.TYPE:
			{
				IType type = (IType) javaElement;
				try {

					members.addAll(Arrays.asList(type.getMethods()));
					members.addAll(Arrays.asList(type.getFields()));
					IOTType otType = OTModelManager.getOTElement(type);
					if (otType != null && otType.isRole())
						for (IMethodMapping mapping : ((IRoleType)otType).getMethodMappings(IRoleType.CALLOUTS))
							members.add(mapping);
					// handle all inner types
					IType[] memberTypes = type.getTypes();
					for (int idx = 0; idx < memberTypes.length; idx++)
						members.addAll(getAllMethodsAndFields(memberTypes[idx]));

				} catch (JavaModelException e) {
					// ignore, finding methods bailed out but keep those we already found
				}
				break;
			}
			default:
				break;
		}
		
		return members;
    }
    
    /**
     * Search all callin bindings within allRoleTypes mentioning one of baseMethods as a base method.
     * 
     * @param baseMembers   base methods and fields of interest
     * @param allRoleTypes  roles where to search
	 * @param callinMap     store found callin bindings here (one set per base method)
	 * @param calloutMap    store found callout bindings here (one set per base method/field)
     * @param monitor
     */
    private void searchMemberBindings(Collection<IMember> baseMembers,
    								  IMember[] allRoleTypes,
    								  Map<IMember, Set<IMember>> callinMap,
    								  Map<IMember, Set<IMember>> calloutMap,
    								  MySubProgressMonitor monitor)
    {
        if (baseMembers == null || baseMembers.size() == 0) {
            monitor.doneNothing();
            return;
        }
        
        // given all potential role types, just directly traverse to the callin bindings:
        for (IMember roleMember : allRoleTypes) 
        {
        	if (monitor.isCanceled()) return;
        	
			IOTType otType = OTModelManager.getOTElement((IType)roleMember);
			if (otType == null || !otType.isRole()) continue;
			IRoleType roleType = (IRoleType)otType;
		
			for (IMethodMapping mapping : roleType.getMethodMappings()) 
			{
				try {
					if (mapping.getElementType() == IOTJavaElement.CALLIN_MAPPING) 
					{
						ICallinMapping callinMapping = (ICallinMapping) mapping;
							for (IMethod baseMethod : callinMapping.getBoundBaseMethods())
								if (baseMembers.contains(baseMethod)) // TODO(SH): would comparison of resources suffice??
							    	OTSearchHelper.addToMapOfSets(callinMap, baseMethod, mapping);
					}
					else if (mapping.getElementType() == IOTJavaElement.CALLOUT_MAPPING) {
						ICalloutMapping calloutMapping = (ICalloutMapping) mapping;
						IMethod baseMethod = calloutMapping.getBoundBaseMethod();
						if (baseMembers.contains(baseMethod) && !isVisibleFor(baseMethod, roleType))
					    	OTSearchHelper.addToMapOfSets(calloutMap, baseMethod, mapping);
					}
					else if (mapping.getElementType() == IOTJavaElement.CALLOUT_TO_FIELD_MAPPING) {
						ICalloutToFieldMapping calloutMapping = (ICalloutToFieldMapping) mapping;
						IField baseField = calloutMapping.getBoundBaseField();
						if (baseMembers.contains(baseField) && !isVisibleFor(baseField, roleType))
					    	OTSearchHelper.addToMapOfSets(calloutMap, baseField, mapping);
					}
				} catch (JavaModelException ex) {
					OTDTUIPlugin.getDefault().getLog().log(new Status(Status.ERROR, OTDTUIPlugin.UIPLUGIN_ID, "Error checking callin/callout binding", ex)); //$NON-NLS-1$
				}
			}
		}
    }

    boolean isVisibleFor(IMember baseMember, IType roleType) throws JavaModelException {
    	int flags = baseMember.getFlags();
    	if (Flags.isPrivate(flags))
    		return false;
    	if (Flags.isPublic(flags))
    		return true;
    	String rolePackage = roleType.getPackageFragment().getElementName();
    	return baseMember.getDeclaringType().getPackageFragment().getElementName().equals(rolePackage);
    }
    
    /**
	 * Create actual markers wrapped by CallinMarkers from the data given as bindingMap.
	 * 
	 * @param target      where to attach the markers
	 * @param bindingMap  data for creating markers: a map from base elements (classes|methods) to role elements (roles|callins) binding to the former.
	 * @param markerKind  what kind of marker should be created?
	 * @param monitor
	 */
    private <M extends IMember> void createMarkersFor(final IMarkableJavaElement target, final Map<IMember, Set<M>> bindingMap, final String markerKind, MySubProgressMonitor monitor)
    {
        if (bindingMap == null) {
            monitor.doneNothing();
            return;
        }
        
		final Set<IMember> baseElements = bindingMap.keySet();
		if (baseElements == null || baseElements.size() == 0) {
            monitor.doneNothing();
		    return;
    	}
		// freeze this value to avoid shared access to the field:
		final AnnotationHelper myAnnotationHelper = annotationHelper;
		if (myAnnotationHelper == null) { // has the active editor been reset to null?
			if (monitor != null)
				monitor.setCanceled(true);
			return;
		}
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException
			{
				monitor.beginTask(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.creating_markers_message"), baseElements.size()); //$NON-NLS-1$
				
				try {
					ILineTracker lineTracker = null;
					final ICompilationUnit compilationUnit = baseElements.iterator().next().getCompilationUnit();
					if (compilationUnit != null) {
						lineTracker  = new DefaultLineTracker();
						lineTracker.set(compilationUnit.getSource());
					}
					CallinMarker marker = new CallinMarker(markerKind);

					for (IMember baseElement : baseElements) 
					{
						// ensure type is actually contained in the target, not super/inherited
			        	if (baseElement instanceof IType && !target.containsElement(baseElement))
			        		continue;
			        	
						Set<M> bindings = bindingMap.get(baseElement);
								
						if (bindings != null && bindings.size() != 0)
						{
							if (markerKind == CallinMarker.PLAYEDBY_ID)
								for (M binding : bindings)
									CallinMarkerCreator2.this.m_cachedBaseForRole.put(binding, (IType)baseElement);

						    try {
						    	ISourceRange nameRange = baseElement.getNameRange();
						    	if (nameRange.getOffset() >= 0 && nameRange.getLength() >= 0) {
							    	if (lineTracker != null) {
										int line = lineTracker.getLineNumberOfOffset(nameRange.getOffset()) + 1; // one-based
		                                marker.setLineNumber(line);
							    	} else {
							    		marker.setNameRange(nameRange);
							    	}
							    	if (markerKind.equals(CallinMarker.CALLOUT_ID)) {
										myAnnotationHelper.removeSomeWarnings(target.getResource(), nameRange);
									}
						    	} else if (!baseElement.isBinary()) {
							    	if (nameRange.getOffset() < 0)
							    		throw new BadLocationException("Offset must be >= 0, is "+nameRange.getOffset()); //$NON-NLS-1$
							    	if (nameRange.getLength() < 0)
							    		throw new BadLocationException("Length must be >= 0, is "+nameRange.getLength()); //$NON-NLS-1$
						    	}
                            }
                            catch (BadLocationException ex) {
                    			OTDTUIPlugin.logException(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.line_number_error_message"), ex); //$NON-NLS-1$
                            }
						    marker.setBaseElement(baseElement);
						    marker.setRoleElement(bindings);

						    marker.create(target);
						}
						monitor.worked(1);
			        }
				}
				finally {
				    monitor.done();
				}
			}
		};
		
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException ex) {
			OTDTUIPlugin.logException("Error creating markers", ex); //$NON-NLS-1$
		}
    }

    /** chained scheduling:<ul>
     *  <li> first wait until the indexes are ready (only on first invocation),
     *  <li> only then schedule our job.
     */
    protected synchronized void schedule(final CallinMarkerJob job, final IStatusLineManager statusLine)
    {
    	if (this.isInitialized) {
    		doSchedule(job, statusLine);
    		return;
    	} else {
    		this.isInitialized = true;
    		Job waitForIndex = new Job(OTDTUIPlugin.getResourceString("CallinMarkerCreator2.wait_for_index_message")) { //$NON-NLS-1$
    			@Override
    			protected IStatus run(IProgressMonitor monitor) {
    				try {
    					// dummy query for waiting until the indexes are ready (see AbstractJavaModelTest)
						new SearchEngine().searchAllTypeNames(
								null,
								SearchPattern.R_EXACT_MATCH,
								"!@$#!@".toCharArray(), //$NON-NLS-1$
								SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE,
								IJavaSearchConstants.CLASS,
								SearchEngine.createWorkspaceScope(),
								new TypeNameRequestor() {
									public void acceptType(int modifiers,char[] packageName,char[] simpleTypeName,char[][] enclosingTypeNames,String path) {}
								},
								IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
								null);
					} catch (CoreException e) {
					} finally {
						// chaining:
						doSchedule(job, statusLine);
					}
					return Status.OK_STATUS;
    			}
    		};
    		waitForIndex.setPriority(Job.DECORATE);
    		waitForIndex.schedule(100);
    	}    	
    }
    protected void doSchedule(final CallinMarkerJob job, IStatusLineManager statusLine)
    {
        job.addJobChangeListener(new JobListener(statusLine) {
            protected void jobFinished(int status) {
                synchronized(CallinMarkerCreator2.this) {
                    if (_currentJob == job)
                        _currentJob = null;
	                
                    IResource resource = job.getResource();
                    if (resource != null) 
                    	if (status == IStatus.OK)
                    		setCached(resource);
                    	else
                    		removeFromCache(resource);
                    else {
                    	IJavaElement element = job.getJavaElement();
                    	if (status == IStatus.OK)
                    		setCached(element);
                    	else
                    		removeFromCache(element);
                    }
                }
            }
        });
        
	    job.setPriority(Job.DECORATE);
	    job.schedule(100); // delay 100ms
    }
}
