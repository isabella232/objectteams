/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CopyInheritanceBreakpointManager.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;

/**
 * This class deals with the problem that a breakpoint in role-method won't have an effect
 * in tsub-classes. This is due to the fact, that methods are inherited through CopyInheritance,
 * that is, their byte-code is copied to the tsub-class.
 * 
 * This class sort of performs copy-inheritance for breakpoints. I.e. whenever a breakpoint
 * is installed for a role-method, it will be installed in the copied methods of all tsub-classes
 * as well.
 * 
 * When uninstalling a breakpoint for a target, the copies are removed from that target, 
 * and completely removed if no longer installed in any targets.
 * 
 * Mapping of line numbers is handled by a JavaStratumLineBreakpoint for stratum "OTJ".
 * 
 * @author gis, stephan
 */
public class CopyInheritanceBreakpointManager implements IJavaBreakpointListener, IResourceChangeListener
{
    private static final Object OT_BREAKPOINT_COPY = "OT_BREAKPOINT_COPY"; //$NON-NLS-1$
    
    private static final String ROLE_CLASS_SEPARATOR = "$__OT__"; //$NON-NLS-1$
    
    /** keep our own mapping from original breakpoints to copies (the breakpoint manager only knows installed breakpoints). */
    private Map<IMarker, List<IJavaBreakpoint>> copiedBreakpoints = new HashMap<IMarker, List<IJavaBreakpoint>>();

    public CopyInheritanceBreakpointManager()
    {
        super();
    }

    public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
    public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {}
    public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {}
    public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {}
    public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint)
    {
        return IJavaBreakpointListener.DONT_CARE;
    }
    
    /**
     * The debugger signals that a breakpoint is being installed into the VM.
     * Check if we need to add copies into tsub roles.
     */
    public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type)
    {
        try {
        	if (isBreakpointCopy(breakpoint))
        		return IJavaBreakpointListener.INSTALL; // yes please! (no further copying needed)
        	
        	if (!(type instanceof IJavaInterfaceType)) 
        		return IJavaBreakpointListener.DONT_CARE; // only use ifc part, (tsuper-) class may not be loaded

	        if (breakpoint instanceof IJavaLineBreakpoint)
                addTSubBreakpointsFor(type, (IJavaLineBreakpoint) breakpoint, target);
        }
        catch (CoreException ex)
        {
            OTDebugUIPlugin.getExceptionHandler().logCoreException("Problem with breakpoint handling", ex); //$NON-NLS-1$
        }
        
        return IJavaBreakpointListener.DONT_CARE;
    }

    private boolean isBreakpointCopy(IJavaBreakpoint breakpoint) throws CoreException
    {
        IMarker marker = breakpoint.getMarker();
        if (marker == null)
            return false;
        
        Map properties = marker.getAttributes();
        return properties.containsKey(OT_BREAKPOINT_COPY);
    }

    private void addTSubBreakpointsFor(IJavaType triggerType, IJavaLineBreakpoint breakpoint, IJavaDebugTarget target) 
    		throws CoreException
    {
    	IMarker marker = breakpoint.getMarker();
        IType type = BreakpointUtils.getType(breakpoint);

        // only act when triggered by the class of the breakpoint (which must be a role):
        IOTType otType = OTModelManager.getOTElement(type);
        if (otType == null)
        	return;
        if (!otType.getFullyQualifiedName('$').equals(triggerType.getName()))
        	return;
        
    	// check whether copies have already been created:
    	List<IJavaBreakpoint> existingCopies = this.copiedBreakpoints.get(marker);
    	if (existingCopies != null) {
    		// tsub breakpoints are already created, only add them to this target:
    		for (IJavaBreakpoint existingCopy : existingCopies)
    			target.breakpointAdded(existingCopy);
    		return;
    	}
    	
    	// find tsub roles to install into:
        IType[] tsubClasses = new TSubClassComputer((IRoleType) otType).getSubClasses();
        if (tsubClasses == null || tsubClasses.length == 0)
            return;

        // perform:
        String fileName = type.getCompilationUnit().getElementName();
        List<IJavaBreakpoint> newBreakpoints = new ArrayList<IJavaBreakpoint>(tsubClasses.length);
        for (IType tsubClass : tsubClasses) {
			IJavaLineBreakpoint newBreakpoint = propagateBreakpoint(breakpoint, fileName, tsubClass, target);
			if (newBreakpoint != null)
				newBreakpoints.add(newBreakpoint);
		}

		this.copiedBreakpoints.put(marker, newBreakpoints);

    }

    /**
     * Propagate the given breakpoint to one tsub role.
     * 
     * @param breakpoint  breakpoint to copy
     * @param fileName    name of the source file that implements the given line
     * @param destType	  the tsub role into which to install
     * @param target	  the debug target into which the breakpoint should be installed
     * @return a new breakpoint or null
     * @throws CoreException when accessing the existing breakpoint fails or the marker for the new breakpoint could not be created
     */
    private IJavaLineBreakpoint propagateBreakpoint(IJavaLineBreakpoint breakpoint, String fileName, IType destType, IJavaDebugTarget target)
    		throws CoreException
    {
        if (destType == null)
        {
            OTDebugUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
            									OTDebugUIPlugin.PLUGIN_ID,
            									"CopyInheritanceBreakpointManager.propagateBreakpoint(): tsub type is null")); //$NON-NLS-1$
            return null;
        }

        Exception ex = null;
        int sourceLineNumber = -1;
        try {
	        sourceLineNumber = breakpoint.getLineNumber();
        } catch (CoreException ce) {
        	ex = ce;
        }
        if (sourceLineNumber == -1 || ex != null)
        {
        	OTDebugUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
							        			OTDebugUIPlugin.PLUGIN_ID,
							        			"CopyInheritanceBreakpointManager.propagateBreakpoint(): source line number not found", //$NON-NLS-1$
							        			ex));
        	return null;
        }
        
        return duplicateBreakpoint(breakpoint, fileName, destType, sourceLineNumber, target);
    }

    /**
     * Duplicate the given breakpoint for the given tsub-role.
     * 
     * @param breakpoint	 	breakpoint to copy
     * @param fileName 		  	name of the source file that implements the given line
     * @param destType 		  	the tsub role into which to install
     * @param sourceLineNumber	line number within fileName
     * @param target		 	the debug target into which the breakpoint should be installed
     * @return a new breakpoint, never null;
     * @throws CoreException when accessing the existing breakpoint fails or the marker for the new breakpoint could not be created
     */
    private IJavaLineBreakpoint duplicateBreakpoint(IJavaLineBreakpoint breakpoint, String fileName, IType destType, int sourceLineNumber, IJavaDebugTarget target) 
    		throws CoreException
    {
        // FIXME: other breakpoint types, exception, properties (null)
        Map properties = getBreakpointProperties(breakpoint);
        Boolean origEnabled = (Boolean) properties.get(IBreakpoint.ENABLED);
        String destName = getClassPartName(destType);
        // Note: by marking the breakpoint as unregistered, we prevent it from showing up in the breakpoints view.
        // Conversely this means that we can not rely on the breakpoint manager but must maintain our own registry (copiedBreakpoints)
		IJavaLineBreakpoint newBreakpoint = JDIDebugModel.createStratumBreakpoint(
															breakpoint.getMarker().getResource(),
															ISMAPConstants.OTJ_STRATUM_NAME,
															fileName,
															null, //sourcePath,
															destName, // classNamePattern
															sourceLineNumber,
															-1, -1, // charStart, charEnd
															breakpoint.getHitCount(),
															false,
															properties);
		// restore one attribute that is hardcoded in JavaStratumLineBreakpoint.<init>:
		if (!origEnabled)
			try {
				newBreakpoint.getMarker().setAttribute(IBreakpoint.ENABLED, Boolean.FALSE);
			} catch (CoreException ex) {
				OTDebugUIPlugin.getExceptionHandler().logCoreException("Unable to disable breakpoint", ex); //$NON-NLS-1$
			}
        target.getDebugTarget().breakpointAdded(newBreakpoint);
        
        return newBreakpoint;
    }
    
    /**
 	 * If type is a role return the name of its class-part,
     * and ensure all enclosing role-teams are given by their class-part, too.
     */
    String getClassPartName(IType type) {
    	IType enclosing = type.getDeclaringType();
    	try {
			if (   enclosing != null 
				&& Flags.isTeam(enclosing.getFlags()))
				return getClassPartName(enclosing)+ROLE_CLASS_SEPARATOR+type.getElementName();
		} catch (JavaModelException e) {
			// fall through
		}
		return type.getFullyQualifiedName();
    }

    /** Initialize breakpoint properties from `breakpoint' and add a few values specific to copies. */
    private Map getBreakpointProperties(IJavaLineBreakpoint breakpoint)
    {
        Map properties = new HashMap(13);
        try {
			properties.putAll(breakpoint.getMarker().getAttributes());
		} catch (CoreException e) {
			// couldn't read marker attributes
		}
		properties.put(IBreakpoint.PERSISTED, Boolean.FALSE);
        properties.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_WARNING)); // hide from the ruler
        properties.put(OT_BREAKPOINT_COPY, Boolean.TRUE);
        
        return properties;
    }

    /**
     * A breakpoint has been removed from a target. Do the same for copies of this breakpoint.
     * 
     * @param target	 the debug target from which the breakpoint has been removed
     * @param breakpoint the breakpoint (potential tsuper). 
     */
    public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) 
    {
        IMarker marker = breakpoint.getMarker();
        if (marker == null || !marker.exists())
            return;

      	// retrieve breakpoint and disable it:
    	List<IJavaBreakpoint> copies = this.copiedBreakpoints.get(marker);
    	if (copies == null) 
    		return;
    	
    	List<IJavaBreakpoint> remainingCopies = new ArrayList<IJavaBreakpoint>(copies.size());
		for (IJavaBreakpoint copy : copies)  {
			target.getDebugTarget().breakpointRemoved(copy, null);
			try {
				if (copy.isInstalled())
					remainingCopies.add(copy);
				else
					copy.delete();
			} catch (CoreException ex) {
				OTDebugUIPlugin.getExceptionHandler().logCoreException("Unable to query copied breakpoint", ex); //$NON-NLS-1$
			}
        }
		// cleanup if no more copies are active:
		if (remainingCopies.size() == 0)
			this.copiedBreakpoints.remove(marker);
		// cleanup if fewer copies are active:
		else if (remainingCopies.size() != copies.size())
			this.copiedBreakpoints.put(marker, remainingCopies);
    }

    private class TSubClassComputer implements IRunnableWithProgress
    {
        private ITypeHierarchy _hierarchy;
        private IRoleType _roleType;
        private IType[] _subClasses;
        
        public TSubClassComputer(IRoleType otType)
        {
            _roleType = otType;
        }
        
        public void run(IProgressMonitor monitor)
        	throws InvocationTargetException, InterruptedException
		{
		    monitor.beginTask(BreakpointMessages.CopyInheritanceBreakpointManager_find_tsub_types_task, 1);
		    IProgressMonitor mon = new SubProgressMonitor(monitor, 1);
		    try
		    {
		        _hierarchy = _roleType.newTypeHierarchy(mon);
		        OTTypeHierarchies.getInstance().setPhantomMode(_hierarchy, true);
		        _subClasses = OTTypeHierarchies.getInstance().getAllTSubTypes(_hierarchy, (IType)_roleType);

		    }
		    catch (JavaModelException ex)
		    {
		        throw new InvocationTargetException(ex);
		    }
		    finally
		    {
		        mon.done();
		    }
		}

        /**
         * @return null or the computed tsub classes
         */
        public IType[] getSubClasses()
        {
            try 
            {
//                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(this);
                run(new NullProgressMonitor());
            }
            catch (InvocationTargetException ex)
            {
                if (ex.getCause() instanceof CoreException)
                    OTDebugUIPlugin.getExceptionHandler().logCoreException("Error creating type hiearchy", (CoreException) ex.getCause()); //$NON-NLS-1$
                else
                    OTDebugUIPlugin.getExceptionHandler().logException("Error creating type hiearchy", ex); //$NON-NLS-1$
            }
            catch (InterruptedException ex)
            {
                OTDebugUIPlugin.getExceptionHandler().logException("Error creating type hiearchy", ex); //$NON-NLS-1$
            }
            
            return _subClasses;
        }
    }

    /** 
     * Watch for changes of the ENABLED attribute of breakpoint markers.
     * 
     * @param event the change event.
     */
	public void resourceChanged(IResourceChangeEvent event) 
	{
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(IBreakpoint.BREAKPOINT_MARKER, true);
		if (markerDeltas == null) 
			return;
		for (IMarkerDelta markerDelta : markerDeltas) {
			if (markerDelta.getKind() == IResourceDelta.CHANGED) {
				IMarker marker = markerDelta.getMarker();
				final Boolean oldEnabled = markerDelta.getAttribute(IBreakpoint.ENABLED, Boolean.FALSE);
				final Boolean newEnabled = marker.getAttribute(IBreakpoint.ENABLED, Boolean.FALSE);
				if (!oldEnabled.equals(newEnabled)) {
					// we have a change wrt enablement
					List<IJavaBreakpoint> breakpoints = this.copiedBreakpoints.get(marker);
			    	if (breakpoints != null) {
						for (final IJavaBreakpoint copy : breakpoints) {
							Job job = new Job(BreakpointMessages.CopyInheritanceBreakpointManager_toggle_enablement_job) {
								protected IStatus run(IProgressMonitor monitor) {
									try {
										copy.setEnabled(newEnabled);
										return Status.OK_STATUS;
									} catch (CoreException e) {
										return new Status(IStatus.ERROR, OTDebugUIPlugin.PLUGIN_ID, "Error toggling copied breakpoint enablement", e); //$NON-NLS-1$
									}
								}
							};
							job.setRule(event.getResource());
							job.schedule();
						}
					}
				}
			}
		}
	}
}
