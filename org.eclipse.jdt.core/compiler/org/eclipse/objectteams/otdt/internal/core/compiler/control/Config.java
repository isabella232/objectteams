/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Config.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.control;

import java.util.Stack;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;


/**
 * MIGRATION_STATE: complete.
 *
 * Instances of this class are used to store some global data while
 * the compiler is invoked from arbitrary clients.
 * Threadsafety is obtained by storing one Config for each client
 * identified by Thread.currentThread() and the field 'client'.
 *
 * We don't use ThreadLocal, since we do cleanup manually using release().
 */
public class Config {
	
	@SuppressWarnings("serial")
	public static class NotConfiguredException extends RuntimeException {
		public NotConfiguredException(String string) {
			super(string);
		}
		public void logWarning(String msg) {
			try {
				JavaCore.getJavaCore().getLog().log(new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, msg, this)); 
			} catch (NoClassDefFoundError ncdfe) {
				System.err.println("Warning: "+msg); //$NON-NLS-1$
				this.printStackTrace(System.err);
			}
		}
	}

    Object            client;
    Parser            parser;
    Parser            plainParser; // alternate parser when client is a MatchLocator
    LookupEnvironment lookupEnvironment;
    public boolean verifyMethods;
    boolean analyzeCode;
    boolean generateCode;
    boolean buildFieldsAndMethods;
    boolean bundledCompleteTypeBindings = false;
    /** are statements ever parsed or are we on a strict diet? */
    boolean strictDiet;
    /** is it sound to ignore missing byte code during copy-inheritance? */
    public boolean ignoreMissingBytecode = false;
//	    Exception ex;

    // the following two are set from RoleTypeBinding.isCompatibleWith():
    /** Here we signal the need to insert casts at the end of resolve(). */
    ReferenceBinding castRequired = null;
    /** Here we signal the need to insert lowering at the end of resolve(). */
    boolean loweringRequired = false;
    /** Here we signal that type checking could not decide whether or not to insert a lowering translation. */
    boolean loweringPossible = false;
    /** Does current type lookup require a source type? */
    boolean sourceTypeRequired = false;

    // end data

    /**
     * Compares this element with other, taking into account everything but the client object.
     * Note: the identities are compared, not the equality!
     */
    /* uncomment if needed for debugging:
    private boolean isAlmostIdentical(Config other)
    {
        boolean identical =
        	parser 			  == other.parser &&
        	lookupEnvironment == other.lookupEnvironment &&
        	verifyMethods     == other.verifyMethods &&
        	analyzeCode       == other.analyzeCode &&
        	generateCode      == other.generateCode &&
			buildFieldsAndMethods        == other.buildFieldsAndMethods &&
			bundledCompleteTypeBindings == other.bundledCompleteTypeBindings &&
        	castRequired      == other.castRequired &&
        	loweringRequired  == other.loweringRequired &&
			sourceTypeRequired== other.sourceTypeRequired;

        return identical;
    }
    */

	// Thread -> Stack<Config>
	// (entries are removed explicitly using release)
	private static final ThreadLocal<Stack<Config>> _configs = new ThreadLocal<Stack<Config>>();

	public static void addConfig(Config config)
	{
	    synchronized (_configs) {
		    Stack<Config> configStack = _configs.get();
		    if (configStack == null)
		    {
		        configStack = new Stack<Config>();
		        _configs.set(configStack);
		    }
		    else
		    {
		        assert(!configStack.empty());
		        // apparently, this is not really the case
		        // assert(config.isAlmostIdentical((Config) configStack.peek()));
		    }

	        configStack.push(config);
	    }
	}

	/**
	 * Check whether a config for the current thread exists.
	 * If not, create a light-weight-Config as owned by 'client'
	 * (this instance is not really configured, only the flags for cast and lower are used).
	 *
	 * If a config already existed, a clone is return, and after cloning the
	 * castRequired and loweringRequired fields are reset.
	 * These values should later be restored using the config-clone as an
	 * argument to removeOrRestore().
     *
	 * @param client
	 * @return a clone of the old config if existent.
	 */
	public static Config createOrResetConfig(Object client) {
	    synchronized (_configs) {
		    Stack<Config> configStack = _configs.get();
		    if (configStack == null) {
		        configStack = new Stack<Config>();
		        _configs.set(configStack);

		        Config config = new Config();
		    	config.client = client;
		    	configStack.push(config);
		    	return null; // no old config
		    } else {
		        assert(!configStack.empty());
		        Config existing = configStack.peek();
		    	Config clone = new Config();
		    	clone.castRequired = existing.castRequired;
		    	clone.loweringRequired = existing.loweringRequired;
		    	clone.loweringPossible = existing.loweringPossible;
		    	existing.castRequired = null;
		    	existing.loweringRequired = false;
		    	existing.loweringPossible = false;
		    	return clone;
		    }
	    }
	}

	/**
	 * Restore the fields castRequired and loweringRequired from a config clone
	 * which was created by checkCreateConfig.
	 *
	 * @param storedConfig
	 */
	private static void restoreConfig(Config storedConfig) {
	    synchronized (_configs) {
		    Stack<Config> configStack = _configs.get();
		    if (configStack != null) {
		    	Config config = configStack.peek();
		    	config.castRequired = storedConfig.castRequired;
		    	config.loweringRequired = storedConfig.loweringRequired;
		    	config.loweringPossible = storedConfig.loweringPossible;
		    }
	    }
	}

	public static void removeOrRestore(Config storedConfig, Object client) {
		if (storedConfig == null)
			removeConfig(client);
		else
			restoreConfig(storedConfig);
	}

	public static void removeConfig(Object client)
	{
    	synchronized (_configs) {
    	    Stack<Config> configStack = _configs.get();
    	    assert(configStack != null);
    	    if (configStack != null)
    	    {
    	        Config config = configStack.pop(); // remove Config
    		    assert(config != null);
    	        if (config.client != client && config.client != null) // bad balance of addConfig and removeConfig calls
    	        {
    	            assert(false);
    	            configStack.push(config); // be defensive, put it back
    	        }
    	        if (configStack.empty())
    	        	_configs.set(null); // remove entire Stack //TODO (carp): optimization: don't remove
    	    }
    	}
	}

	public static Config getConfig()
	{
		if (_configs == null) {
			InternalCompilerError.log("Dependencies has no _configs"); //$NON-NLS-1$
			return null;
		}
    	synchronized (_configs) {
    	    Stack<Config> configStack = _configs.get();
			if (configStack == null) {
				InternalCompilerError.log("Dependencies not configured"); //$NON-NLS-1$
				return null;
			}
    	    return configStack.peek();
    	}
	}
	/** get the current config or null if not configured. */
	public static Config safeGetConfig() {
		if (_configs == null)
			return null;
    	synchronized (_configs) {
    	    Stack<Config> configStack = _configs.get();
			if (configStack == null)
				return null;
    	    return configStack.peek();
    	}
	}

	public static boolean hasConfig()
	{
		if (_configs == null)
			return false;
	    synchronized(_configs) {
	        return _configs.get() != null;
	    }
	}

	static boolean getVerifyMethods() {
		return getConfig().verifyMethods;
	}

	static boolean getAnalyzeCode() {
		return getConfig().analyzeCode;
	}

	static boolean getGenerateCode() {
		return getConfig().generateCode;
	}

	/** Request that a cast to 'castType' be inserted. */
	public static void setCastRequired(ReferenceBinding castType) {
		Config config =	getConfig();
		if (   config.castRequired != null
			&& castType != null)
		{
			config.castRequired = SourceTypeBinding.MultipleCasts;
		} else {
			config.castRequired = castType;
		}
	}
	/** Retrieve the type to which casting was requested.
	 *  Null if no request, SourceTypeBinding.multipleCasts, if more than one
	 *  cast was requested since last reset.
	 */
	public static ReferenceBinding getCastRequired() {
		return getConfig().castRequired;
	}

	public static void setLoweringRequired(boolean val) {
		getConfig().loweringRequired = val;
	}

	public static boolean getLoweringRequired() {
		return getConfig().loweringRequired;
	}

	public static void setLoweringPossible(boolean val) {
		getConfig().loweringPossible = val;		
	}

	/** 
	 * Has type checking detected that lowering is possible but not required due to expected type java.lang.Object?
	 * (see OTJLD 2.2(f))
	 */
	public static boolean getLoweringPossible() {
		return getConfig().loweringPossible;
	}

	/**
	 * Has compatibility check detected the need to add a type adjustment
	 * (lowering or casting)?
	 * The flags are destructivly read, i.e., reset during reading.
	 */
	public static boolean requireTypeAdjustment() {
	    boolean result = (getCastRequired() != null) || getLoweringRequired();
	    setCastRequired(null);
	    setLoweringRequired(false);
	    return result;
	}

	public static void setSourceTypeRequired(boolean val) {
		getConfig().sourceTypeRequired = val;
	}

	public static boolean getSourceTypeRequired() {
		final Config config = getConfig();
		return config != null && config.sourceTypeRequired;
	}

	public static LookupEnvironment getLookupEnvironment() throws NotConfiguredException {
		Config current = getConfig();
		if (current == null)
			throw new NotConfiguredException("LookupEnvironment not configured"); //$NON-NLS-1$
		return current.lookupEnvironment();
	}

	protected LookupEnvironment lookupEnvironment() {
		return this.lookupEnvironment;
	}

	public static boolean hasLookupEnvironment() {
		if (_configs == null)
			return false;
		synchronized(_configs) {
			if (!hasConfig())
				return false;
			Config config = getConfig();
			return (config != null) && (config.lookupEnvironment != null);
		}
	}
	static boolean getBuildFieldsAndMethods() {
		return getConfig().buildFieldsAndMethods;
	}

	public static void assertBuildFieldsAndMethods(boolean flag) {
		assert flag == getBuildFieldsAndMethods();
	}

    /** are statements ever parsed or are we on a strict diet? */
    public static boolean getStrictDiet() {
		return getConfig().strictDiet;
	}

	// the following internal lookup functions return pieces
	// from a Config instance, which is identified by the current thread.
	public static Parser getParser() {
		return getConfig().parser();
	}
	protected Parser parser() {
		return this.parser;
	}

	/**
	 * Find the proper object to fetch method bodies and delegate to that object.
	 * (if MatchLocator is our client don't use it or its MatchLocatorParser,
	 * but a plain Parser!)
	 * @param unit
	 */
	public static void delegateGetMethodBodies(CompilationUnitDeclaration unit) {
		Config config = getConfig();
		Parser parser = config.parser();
		if (config.client instanceof ITypeRequestor) {
			// MatchLocator.getMethodBodies and MatchLocatorParser.getMethodBodies
			// both contribute to locating matches. Unit parser on behalf of
			// Dependencies should however be parsed using a plain Parser:
			if (config.plainParser == null)
				config.plainParser = ((ITypeRequestor)config.client).getPlainParser();
			if (config.plainParser != null)
				parser = config.plainParser;
		}
		parser.getMethodBodies(unit);
	}

	/**
	 * For (lifting-)constructors source statements are merged with
	 * generated statements. At the time of parsing the body, the
	 * generated statements may already be present. This method checks
	 * whether it is legal to have statements before parsing, which
	 * should then be merged.
	 */
	public static boolean areStatementsAcceptable (ConstructorDeclaration cd,
												   boolean hasExplicitConstructorCall,
												   ProblemReporter problemReporter)
	{
    	if (!cd.isGenerated)
    		throw new InternalCompilerError("generated statements in non-generated constructor "+ cd.toString()); //$NON-NLS-1$
    	if (hasExplicitConstructorCall) {
       		if (cd.scope != null)
       			problemReporter.explicitSuperInLiftConstructor(
        					cd.scope.referenceType(), cd);
			return false;
    	}
		return true;
	}
	/**
	 * @return Is the client invoking Dependencies a Compiler? (currently unused).
	 */
	public static boolean clientIsCompiler() {
		Config config = getConfig();
		return (config != null && config.client instanceof Compiler);
	}

	public static boolean clientIsBatchCompiler() {
		Config config = safeGetConfig();
		return (   config != null
				&& config.client instanceof Compiler
				&& ((Compiler)config.client).isBatchCompiler);
	}

	/**
	 * @param mode
	 * @return previous mode
	 */
	public boolean setBundledCompleteTypeBindingsMode(boolean mode) {
		boolean save = this.bundledCompleteTypeBindings;
		this.bundledCompleteTypeBindings = mode;
		return save;
	}

	/**
	 * should type bindings be completed as a bundle?
	 */
	public static boolean getBundledCompleteTypeBindingsMode() {
		Config config = getConfig();
		return config.bundledCompleteTypeBindings;
	}

	// Logging, common API for both batch and IDE modes:
	private static ILogger logger = null;
	public static synchronized void setLogger(ILogger aLogger) {
		logger = aLogger;
	}
	public static synchronized void logException(String message, Throwable exception) {
		if (logger != null)
			logger.logException(message, exception);
		else
			System.err.println("OT/J: "+message); //$NON-NLS-1$
			exception.printStackTrace(System.err);
	}

}