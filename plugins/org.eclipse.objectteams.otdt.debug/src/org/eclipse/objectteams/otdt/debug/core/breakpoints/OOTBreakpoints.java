/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OOTBreakpoints.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.objectteams.otdt.debug.OTDebugPlugin;

import static org.eclipse.objectteams.otdt.debug.core.breakpoints.IOOTBreakPoints.*;

/**
 * @author ike
 *
 * This class provides methods to create OOT-specific (org.objectteams.Team) breakpoints
 *
 * $Id: OOTBreakpoints.java 23427 2010-02-03 22:23:59Z stephan $
 */
@SuppressWarnings("nls")
public class OOTBreakpoints
{
	public static final String FIELD_THIS = "this";
	public static final String LOCAL_THREAD_ACT_DEACT = "thread";
	public static final String LOCAL_THREAD_IMPLICIT_ACT_DEACT = "currentThread";
	public static final String FIELD_ALL_THREADS = "ALL_THREADS";
	
	public static final String ATTR_OT_BREAKPOINT = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint";
	public static final String ATTR_OT_BREAKPOINT_CTOR     = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.Constructor";
	public static final String ATTR_OT_BREAKPOINT_FINALIZE = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.FinalizeMethod";
	public static final String ATTR_OT_BREAKPOINT_ACT   = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.ActivateMethod";
	public static final String ATTR_OT_BREAKPOINT_DEACT = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.DeactivateMethod";
	public static final String ATTR_OT_BREAKPOINT_IMPLICIT_ACT   = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.ImplicitActivateMethod";
	public static final String ATTR_OT_BREAKPOINT_IMPLICIT_DEACT = OTDebugPlugin.PLUGIN_ID + ".TeamBreakpoint.ImplicitDeactivateMethod";

	
	//associated with "public Team() {}"
    public static int getTeamConstructorLineNumber()
    {
        return LINE_TeamConstructor;
    }

    //associated with "doRegistration();"
    public static int getActivateMethodLineNumber()
    {
        return LINE_ActivateMethod;
    }
    
    //associated with "_OT$lazyGlobalActiveFlag = false;"
    public static int getDeactivateMethodLineNumber()
    {
        return LINE_DeactivateMethod;
    }
    
    //associated with "implicitActivationsPerThread.set(Integer.valueOf(implActCount + 1 ));"
    public static int getImplicitActivateMethodLineNumber()
    {
        return LINE_ImplicitActivateMethod;
    }

    //implicitActivationsPerThread.set(Integer.valueOf(implActCount - 1));
    public static int getImplicitDeactivateMethodLineNumber()
    {
        return LINE_ImplicitDeactivateMethod;
    }
    
    // implicit "return;"
    public static int getFinalizeMethodLineNumber() {
		return LINE_FinalizeMethod;
	}

    public static Map<String, Boolean> getBreakpointAttributes()
    {
        Map<String, Boolean> attrs = new HashMap<String, Boolean>();
        attrs.put(OOTBreakpoints.ATTR_OT_BREAKPOINT, Boolean.TRUE);
        return attrs;
    }

    public static IBreakpoint createOOTConstructorBreakpoint(IType oot) 
    		throws CoreException
    {
		Map<String, Boolean> constructorAttributes = getBreakpointAttributes();
		constructorAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_CTOR, Boolean.TRUE);
         return createOOTBreakpoint(oot, getTeamConstructorLineNumber(), constructorAttributes);
    }
    
    public static IBreakpoint createOOTFinalizeBreakpoint(IType oot)
    		throws CoreException
    {
    	Map<String, Boolean> finalizeMethodAttributes = getBreakpointAttributes();
    	finalizeMethodAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_FINALIZE, Boolean.TRUE);
    	return createOOTMethodBreakpoint(oot, getFinalizeMethodLineNumber(), finalizeMethodAttributes);    	
    }
        
    public static IBreakpoint createOOTActivateBreakpoint(IType oot)throws CoreException
    {
		Map<String, Boolean> activateMethodAttributes = getBreakpointAttributes();
		activateMethodAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_ACT, Boolean.TRUE);
    	return createOOTBreakpoint(oot, getActivateMethodLineNumber(), activateMethodAttributes);
    }

    public static IBreakpoint createOOTDeactivateBreakpoint(IType oot) throws CoreException
	{
		Map<String, Boolean> deactivateMethodAttributes = getBreakpointAttributes();
		deactivateMethodAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_DEACT, Boolean.TRUE);
		return createOOTBreakpoint(oot, getDeactivateMethodLineNumber(), deactivateMethodAttributes);
	}
    
    public static IBreakpoint createOOTImplicitActivateBreakpoint(IType oot)throws CoreException
    {
		Map<String, Boolean> implicitActivateMethodAttributes = getBreakpointAttributes();
		implicitActivateMethodAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_IMPLICIT_ACT, Boolean.TRUE);
    	return createOOTBreakpoint(oot, getImplicitActivateMethodLineNumber(), implicitActivateMethodAttributes);
    }

    public static IBreakpoint createOOTImplicitDeactivateBreakpoint(IType oot)throws CoreException
    {
		Map<String, Boolean> implicitDeactivateMethodAttributes = getBreakpointAttributes();
		implicitDeactivateMethodAttributes.put(OOTBreakpoints.ATTR_OT_BREAKPOINT_IMPLICIT_DEACT, Boolean.TRUE);
    	return createOOTBreakpoint(oot, getImplicitDeactivateMethodLineNumber(), implicitDeactivateMethodAttributes);
    }
	public static IBreakpoint createOOTBreakpoint(IType oot, int linenumber, Map attributes) 
			throws CoreException
	{
		IResource teamResource = oot.getJavaProject().getResource();
		IJavaBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(
				teamResource,
				oot.getFullyQualifiedName(),
				linenumber,
				-1, -1, 0,
				false /*register*/,
				attributes);
		breakpoint.setPersisted(false);
		
		return breakpoint;
	}
    
	public static IBreakpoint createOOTMethodBreakpoint(IType oot, int linenumber, Map attributes)
			throws CoreException
	{
		IResource teamResource = oot.getJavaProject().getResource();
		IJavaBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(
				teamResource, 
				oot.getFullyQualifiedName(), 
				"finalize", 
				"()V",
				true /*entry*/, false /*exit*/, false /*native*/,
				linenumber, 
				-1, -1, 0, 
				false /*register*/, 
				attributes);
		breakpoint.setPersisted(false);
		return breakpoint;
	}
	
	public static final boolean isOOTBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT) != null;
    }

    public static final boolean isOOTConstructorBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_CTOR) != null;
    }
    
    public static final boolean isOOTFinalizeBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
    	return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_FINALIZE) != null;
    }
    
    public static final boolean isOOTActiveMethodBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_ACT) != null;
    }
    
    public static final boolean isOOTDeactiveMethodBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_DEACT) != null;
    }
    
    public static final boolean isOOTImplicitActiveMethodBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_IMPLICIT_ACT) != null;
    }
    
    public static final boolean isOOTImplicitDeactiveMethodBreakpoint(IBreakpoint breakpoint) throws CoreException
    {
        return breakpoint.getMarker().getAttribute(OOTBreakpoints.ATTR_OT_BREAKPOINT_IMPLICIT_DEACT) != null;
    }
}