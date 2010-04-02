/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTDTPlugin.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;


/**
 * The main plugin class to be used in the desktop.
 * 
 * @author jwloka
 */
public class OTDTPlugin extends Plugin
{
	public static final String PLUGIN_ID = JavaCore.OTDT_PLUGIN_ID;

	public static final String OTDT_INSTALLDIR = "OTDT_INSTALLDIR"; //$NON-NLS-1$
	
	// === IDs for configurable options for the compiler : ===
	public static final String OT_COMPILER_BASE_CALL =
		PLUGIN_ID + ".compiler.problem.basecall"; //$NON-NLS-1$
	public static final String OT_COMPILER_UNSAFE_ROLE_INSTANTIATION =
		PLUGIN_ID + ".compiler.problem.unsafe_role_instantiation"; //$NON-NLS-1$

	public static final String OT_COMPILER_EFFECTLESS_FIELDACCESS =
		PLUGIN_ID + ".compiler.problem.effectless_fieldaccess"; //$NON-NLS-1$
	public static final String OT_COMPILER_FRAGILE_CALLIN = 
		PLUGIN_ID + ".compiler.problem.fragile_callin"; //$NON-NLS-1$
	public static final String OT_COMPILER_UNUSED_PARAMMAP =
		PLUGIN_ID + ".compiler.problem.unused_parammap"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_POTENTIAL_AMBIGUOUS_PLAYEDBY =
		PLUGIN_ID + ".compiler.problem.potential_ambiguous_playedby"; //$NON-NLS-1$
	public static final String OT_COMPILER_ABSTRACT_POTENTIAL_RELEVANT_ROLE =
		PLUGIN_ID + ".compiler.problem.abstract_potential_relevant_role"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_DECAPSULATION =
		PLUGIN_ID + ".compiler.problem.decapsulation"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_BINDING_CONVENTIONS = 
		PLUGIN_ID + ".compiler.problem.binding_conventions"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_INFERRED_CALLOUT =
		PLUGIN_ID + ".compiler.problem.inferred_callout"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_INCOMPLETE_BUILD =
		PLUGIN_ID + ".compiler.problem.incomplete_build"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_WEAVE_INTO_SYSTEM_CLASS =
		PLUGIN_ID + ".compiler.problem.weave_into_system_class"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_OVERRIDE_FINAL_ROLE =
		PLUGIN_ID + ".compiler.problem.override_final_role"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_EXCEPTION_IN_GUARD =
		PLUGIN_ID + ".compiler.problem.exception_in_guard"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_AMBIGUOUS_LOWERING =
		PLUGIN_ID + ".compiler.problem.ambiguous_lowering"; //$NON-NLS-1$
	
	public static final String OT_COMPILER_ADAPTING_DEPRECATED =
		PLUGIN_ID + ".compiler.problem.adapting_deprecated"; //$NON-NLS-1$

	public static final String OT_COMPILER_SCOPED_KEYWORDS = 
		PLUGIN_ID + ".compiler.option.scoped_keywords"; //$NON-NLS-1$

	public static final String OT_COMPILER_DEPRECATED_PATH_SYNTAX = 
		PLUGIN_ID + ".compiler.problem.deprecated_path_syntax"; //$NON-NLS-1$;


	public static String OTRUNTIME_INSTALLDIR = "OTRUNTIME_INSTALLDIR"; //$NON-NLS-1$

    private static OTDTPlugin _singleton = null;


    public OTDTPlugin()
    {
        super();
        _singleton = this;
    }

    public static OTDTPlugin getDefault()
    {
        return _singleton;
    }

	public static String[] createProjectNatures(IProjectDescription prjDesc)
	{
	    String[] natures = prjDesc.getNatureIds();
	    String[] result = new String[natures.length + 1];
	    
	    System.arraycopy(natures, 0, result, 0, natures.length); 	
	    
	    result[natures.length] = JavaCore.OTJ_NATURE_ID;
	      		
	    return result;
	}

	public static ICommand[] createProjectBuildCommands(IProjectDescription project)
	{
	    return new ICommand[] { createProjectBuildCommand(project) };
	}

	public static ICommand createProjectBuildCommand(IProjectDescription project) 
	{
		ICommand otBuildCmd = project.newCommand();
	    otBuildCmd.setBuilderName(JavaCore.OTJ_BUILDER_ID);
		return otBuildCmd;
	}
    
	public static ExceptionHandler getExceptionHandler()
	{
		return new ExceptionHandler(PLUGIN_ID);
	}
	
	public static Status createErrorStatus(String message, Throwable exception)
	{
	    return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, exception);
	}
	
    /** 
     * Take relativeFileName relative to OTRUNTIME_INSTALLDIR and make an absolute path from that.
     * The returned string is enclosed in double-quotes so it is safe to have blanks within.
     */
	public static String calculateAbsoluteRuntimePath(String relativeFileName)
	{
		Path path = new Path(OTDTPlugin.OTRUNTIME_INSTALLDIR + relativeFileName);
		return "\""+JavaCore.getResolvedVariablePath( path ).toOSString()+'"'; //$NON-NLS-1$
	}
}
