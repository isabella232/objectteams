/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2011 Fraunhofer Gesellschaft, Munich, Germany,
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.compiler.CompilerVersion;
import org.eclipse.objectteams.otequinox.TransformerPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 * 
 * @author jwloka
 * @noinstantiate clients are not supposed to instantiate this class.
 * @noextend clients are not supposed to extend this class.
 */
public class OTDTPlugin extends Plugin
{
	public static final String PLUGIN_ID = JavaCore.OTDT_PLUGIN_ID;

	public static final String OTDT_INSTALLDIR = "OTDT_INSTALLDIR"; //$NON-NLS-1$
	
	// === IDs for configurable options for the compiler : ===
	/** 
	 * Compiler option ID: Reporting missing or duplicate basecalls in a callin method.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_BASE_CALL = PLUGIN_ID + ".compiler.problem.basecall"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting cycles in playedBy and containment relationships.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_BASECLASS_CYCLE = PLUGIN_ID + ".compiler.problem.baseclass_cycle"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting role instantiations that might conflict 
	 * with an existing role for the same base object.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_UNSAFE_ROLE_INSTANTIATION = PLUGIN_ID + ".compiler.problem.unsafe_role_instantiation"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting a callin binding that requires a base call to provide the required return value.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_FRAGILE_CALLIN = PLUGIN_ID + ".compiler.problem.fragile_callin"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting if playedBy bindings of multiple roles may create an ambiguity.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_POTENTIAL_AMBIGUOUS_PLAYEDBY = PLUGIN_ID + ".compiler.problem.potential_ambiguous_playedby"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting an abstract role class that might be relevant for instantiation (e.g., lifting).
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_ABSTRACT_POTENTIAL_RELEVANT_ROLE = PLUGIN_ID + ".compiler.problem.abstract_potential_relevant_role"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting decapsulation.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_DECAPSULATION = PLUGIN_ID + ".compiler.problem.decapsulation"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting field write decapsulation.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_DECAPSULATION_WRITE = PLUGIN_ID + ".compiler.problem.decapsulation_write"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting violations of binding conventions, notably the rule that 
	 * types after the playedBy keyword should be imported with the base modifier.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code> (except for OT-Plugin Projects which set this to <code>"error"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_BINDING_CONVENTIONS = PLUGIN_ID + ".compiler.problem.binding_conventions"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting the use of inferred callout bindings.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"error"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_INFERRED_CALLOUT = PLUGIN_ID + ".compiler.problem.inferred_callout"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting attempts to weave into a system class.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_WEAVE_INTO_SYSTEM_CLASS = PLUGIN_ID + ".compiler.problem.weave_into_system_class"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting when overriding a final roles.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"error"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_OVERRIDE_FINAL_ROLE = PLUGIN_ID + ".compiler.problem.override_final_role"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting when a guard predicate may throw a checked exception.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"error"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_EXCEPTION_IN_GUARD = PLUGIN_ID + ".compiler.problem.exception_in_guard"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting when lowering and upcast to Object are ambiguous.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_AMBIGUOUS_LOWERING = PLUGIN_ID + ".compiler.problem.ambiguous_lowering"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting when binding to a deprecated class (playedBy) or method (callin binding).
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"error"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_ADAPTING_DEPRECATED = PLUGIN_ID + ".compiler.problem.adapting_deprecated"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting when a callin after method will ignore the return value of the role method.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_IGNORING_ROLE_RETURN = PLUGIN_ID + ".compiler.problem.ignoring_role_return"; //$NON-NLS-1$
	/** 
	 * Compiler option ID: Reporting use of the old path syntax for dependent types.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "error", "warning", "ignore" }</code></dd>
	 * <dt>Default:</dt><dd><code>"warning"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_DEPRECATED_PATH_SYNTAX = PLUGIN_ID + ".compiler.problem.deprecated_path_syntax"; //$NON-NLS-1$;
	/** 
	 * Compiler option ID: Reporting callout to field without any effect nor result.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
	 * <dt>Default:</dt><dd><code>"enabled"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_EFFECTLESS_FIELD_ACCESS = PLUGIN_ID + ".compiler.problem.effectless_fieldaccess";
	/** 
	 * Compiler option ID: Reporting unused parameter mappings.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
	 * <dt>Default:</dt><dd><code>"enabled"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */	
	public static final String OT_COMPILER_UNUSED_PARAMMAP =  PLUGIN_ID + ".compiler.problem.unused_parammap";

	/**
	 * Compiler option ID: Parse pure Java (disabling OT/J keywords).
	 * This is not normally set by clients but initialized from the project nature.
	 * <dl>
	 * <dt>Possible values:</dt><dd><code>{ "enabled", "disabled" }</code></dd>
	 * <dt>Default:</dt><dd><code>"enabled"</code></dd>
	 * </dl>
	 * @category CompilerOptionID
	 */
	public static final String OT_COMPILER_PURE_JAVA = PLUGIN_ID + ".compiler.option.pure_java";
	
    private static OTDTPlugin _singleton = null;


    public OTDTPlugin()
    {
        super();
        _singleton = this;
    }

    /** Get the singleton activator instance. */
    public static OTDTPlugin getDefault()
    {
        return _singleton;
    }

    @Override
    public void start(BundleContext context) throws Exception {    
    	super.start(context);
    	try {
    		String weavingProperty = System.getProperty("ot.weaving"); //$NON-NLS-1$
    		boolean useDynamicWeaving;
    		if (weavingProperty != null) {
				useDynamicWeaving = "dynamic".equals(weavingProperty);
			} else
    			useDynamicWeaving = TransformerPlugin.useDynamicWeaving();
    		CompilerVersion.setDynamicWeaving(useDynamicWeaving);
    		OTREContainer.findBytecodeLib(context, useDynamicWeaving);
    		if (useDynamicWeaving)
    			OTREContainer.OT_RUNTIME_PLUGIN = "org.eclipse.objectteams.otredyn"; //$NON-NLS-1$
    	} catch (RuntimeException re) {
    		this.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Cannot initialize BCEL location", re)); //$NON-NLS-1$
    	}
    }
    
    /** 
     * Add the OT/J nature to the natures of a given project.
     * @param prjDesc start from the natures of this project.
     * @return a fresh array including the natures of prjDesc plus the OT/J nature. 
     */
	public static String[] createProjectNatures(IProjectDescription prjDesc)
	{
	    String[] natures = prjDesc.getNatureIds();
	    String[] result = new String[natures.length + 1];
	    
	    System.arraycopy(natures, 0, result, 0, natures.length); 	
	    
	    result[natures.length] = JavaCore.OTJ_NATURE_ID;
	      		
	    return result;
	}

	/**
	 * Add the OT/J builder to the build commands of a given project.
	 * @param project start from the builders of this project.
	 * @return a fresh array including the build commands of project plus the OT/J builder. 
	 */
	public static ICommand[] createProjectBuildCommands(IProjectDescription project)
	{
	    return new ICommand[] { createProjectBuildCommand(project) };
	}

	/**
	 * Create a build command for the OT/J builder.
	 * @param project the project to which the command shall be applied.
	 * @return a fresh build command
	 */
	public static ICommand createProjectBuildCommand(IProjectDescription project) 
	{
		ICommand otBuildCmd = project.newCommand();
	    otBuildCmd.setBuilderName(JavaCore.OTJ_BUILDER_ID);
		return otBuildCmd;
	}
    
	/**
	 * Log an exception.
	 * @param message    detail message
	 * @param exception  the exception
	 */
	public static void logException(String message, Throwable exception) {
		_singleton.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, exception));
	}
	
	
	/** 
     * Take relativeFileName relative to a given classpath variable and make an absolute path from that.
     * @param variableName		a classpath variable
     * @param relativeFilename  a filename relative to the classpath variable
     * @return the resolved combined path.
     */
	public static IPath getResolvedVariablePath(String variableName, String relativeFilename)
	{
		Path path = new Path(variableName + '/'+ relativeFilename);
		return JavaCore.getResolvedVariablePath(path);
	}
}
