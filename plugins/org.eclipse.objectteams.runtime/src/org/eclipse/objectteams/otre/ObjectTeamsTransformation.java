/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ObjectTeamsTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.objectteams.otre.jplis.JPLISEnhancer;
import org.eclipse.objectteams.otre.util.AnnotationHelper;
import org.eclipse.objectteams.otre.util.AttributeReadingGuard;
import org.eclipse.objectteams.otre.util.CallinBindingManager;
import org.eclipse.objectteams.otre.util.RoleBaseBinding;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.*;

/**
 * Superclass for all transformations in this package.
 * This class and its subclasses does not directly depend on JPLIS.
 * 
 * Contains common fields and methods.
 *
 * @author  Christine Hundt
 * @author  Stephan Herrmann
 */
public abstract class ObjectTeamsTransformation
	implements OTConstants
{
	
	// ------------------------------------------
	// ---------- Flags: ------------------------
	// ------------------------------------------

	/** Check whether <tt>flags</tt> denote a generated callin wrapper. */
    static boolean isCallinWrapper(Method m, ClassGen cg) {
		return methodHasCallinFlags(m, cg, WRAPPER);
	}

	/** Check whether <tt>flags</tt> denote a callin method from the source code. */
    static boolean isCallin(Method m, ClassGen cg) {
    	return (methodHasCallinFlags(m, cg, 0) && !isCallinWrapper(m, cg));
    }


	// ------------------------------------------
	// ---------- Names: ------------------------
	// ------------------------------------------

	/** Generate the name for a backup of a method. */
    static String genOrigMethName(String methName) {
        return "_OT$" + methName + "$orig";
    }

	/** Generate the name for a chaining wrapper. */
    static String genChainMethName(String methName) {
        return "_OT$" + methName + "$chain";
    }
   
    /** Common factory for all tranformers.
	 *	To be initialized once we get a class for transformation. */
	InstructionFactory factory;
   
    /** Which class loader are we working for? */
    protected ClassLoader loader;
    protected boolean isLoadingSystemClass;

	public ObjectTeamsTransformation(ClassLoader loader) {
		if (loader == null) {
			isLoadingSystemClass = true;
			loader = getClass().getClassLoader();
		}
		this.loader = loader;
	}

	// ------------------------------------------
	// ---------- Logging: ----------------------
	// ------------------------------------------
	/** Initialized from property <tt>ot.log</tt>. */
    static boolean logging = false;

    static {
        if(System.getProperty("ot.log") != null)
            logging = true;
    }

	/** Print <tt>message</tt> only if <tt>logging</tt> is true. */
    public static void printLogMessage(String message) {
        System.out.println(message);
    }
	
	//	------------------------------------------
	// ---------- use the following file as config file for additional active teams: --------
	// ------------------------------------------
	 /** Initialized from property <tt>ot.teamconfig</tt>. */
	 
	 static String TEAM_CONFIG_FILE = null;

	 static {
		TEAM_CONFIG_FILE = System.getProperty("ot.teamconfig");
	 }
	 
	 //	------------------------------------------
	 // ---------- Compatibility with different compiler versions: --------
	 // ------------------------------------------
	 
	 // compiler 1.2.4 introduces isSuperAccess flag for basecall surrogate:
	 protected static boolean IS_COMPILER_GREATER_123 = false;
	 
	 protected static boolean IS_COMPILER_13X_PLUS = false;
	 
	 protected static boolean IS_COMPILER_14X_PLUS = false;

	 // ------------------------------------------
	 // ---------- This flag must currently be true for OT/Equinox: ----------------------
	 // ------------------------------------------
	 /** Initialized from property <tt>ot.equinox</tt>. */
	 public static boolean WORKAROUND_REPOSITORY = false;
	 
	 static {
	 	if(System.getProperty("ot.equinox") != null)
	 		WORKAROUND_REPOSITORY = true;
	 }
	 
	 // ------------------------------------------
	 // ---------- Debugging: ----------------------
	 // ------------------------------------------
	 /** Initialized from property <tt>ot.debug</tt>. */
	 public static boolean debugging = false;
	 
	 static {
	 	if(System.getProperty("ot.debug") != null)
	 		debugging = true;
	 }
	 
	// -------------------------------------------------------
	// ---------- Modes for implicit team activateion --------
	// -------------------------------------------------------
	enum ImplicitActivationMode { NEVER, ANNOTATED, ALWAYS }
	static ImplicitActivationMode implicitActivationMode = ImplicitActivationMode.ANNOTATED;
	static {
		String prop = System.getProperty("ot.implicit.team.activation");
		for (ImplicitActivationMode mode : ImplicitActivationMode.values()) {
			if (mode.name().equals(prop)) {
				implicitActivationMode = mode;
				break;
			}
		}
	}

	// -----------------------------------------
	// ---------- Signature enhancement --------
	// -----------------------------------------

	/**
	 *  Prepend hidden arguments to the signature.
	 *  This methods only treats argument names.
	 * @see #enhanceArgumentTypes
	 *  The arguments are:
	 *  <dl>
	 *  <dt><tt>Team[] _OT$teams</tt></dt>
	 *      <dd>array of active Teams affecting the current base method.
	 *  <dt><tt>int[] _OT$teamIDs</tt></dt>
	 *      <dd>array of IDs of the above Teams.
	 *  <dt><tt>int _OT$idx</tt></dt>
	 *      <dd>index into above arrays: the Team currently being processed.
	 *  <dt><tt>Object[] _OT$unusedArgs</tt></dt>
	 *      <dd>array of arguments which are unused by the current role method.
	 *  </dl>
	 *  @param argumentNames array of original argument names.
	 *  @return augmented array of argument names.
	 */
    static String[] enhanceArgumentNames(String[] argumentNames) {
		return enhanceArgumentNames(argumentNames, 0);
	}

	/**
	 *  Prepend hidden arguments to the signature.
	 *  This methods only treats argument names.
	 * @see #enhanceArgumentTypes
	 *  The arguments are:
	 *  <dl>
	 *  <dt><tt>Team[] _OT$teams</tt></dt>
	 *      <dd>array of active Teams affecting the current base method.
	 *  <dt><tt>int[] _OT$teamIDs</tt></dt>
	 *      <dd>array of IDs of the above Teams.
	 *  <dt><tt>int _OT$idx</tt></dt>
	 *      <dd>index into above arrays: the Team currently being processed.
	 *  <dt><tt>Object[] _OT$unusedArgs</tt></dt>
	 *      <dd>array of arguments which are unused by the current role method.
	 *  </dl>
	 *  @param argumentNames array of original argument names.
	 *  @param idx position where new arguments should be inserted into
	 *             <tt>originalArgumentTypes</tt>.
	 *  @return augmented array of argument names.
	 */
    static String[] enhanceArgumentNames(String[] argumentNames, int idx) {
  
    	String[] enhancedArgumentNames =
			new String[argumentNames.length + EXTRA_ARGS];

        for (int j=0; j<idx; j++)
            enhancedArgumentNames[j] = argumentNames[j];

        enhancedArgumentNames [idx + TEAMS_ARG     - 1] = TEAMS;
        enhancedArgumentNames [idx + TEAMIDS_ARG   - 1] = TEAMIDS;
        enhancedArgumentNames [idx + IDX_ARG       - 1] = IDX;
        enhancedArgumentNames [idx + BIND_IDX_ARG  - 1] = BIND_IDX;
        enhancedArgumentNames [idx + BASE_METH_ARG - 1] = BASE_METH_TAG;
        enhancedArgumentNames [idx + UNUSED_ARG    - 1] = UNUSED;

        for (int j = idx; j < argumentNames.length; j++)
            enhancedArgumentNames[j + EXTRA_ARGS] = argumentNames[j];
       
        return enhancedArgumentNames;
    }

	/**
	 * @see #enhanceArgumentTypes(Type[])
	 * @param signature String from which to extract the argument types.
	 * @return
	 */
	static Type[] enhanceArgumentTypes(String signature) {
		Type[] types = Type.getArgumentTypes(signature);
		return enhanceArgumentTypes(types);
	}

	/**
	 *  Prepend hidden arguments to the signature.
	 *  This methods only treats argument types.
	 *  @see #enhanceArgumentNames
	 *
	 *  @param originalArgumentTypes array of original argument types.
	 *  @return augmented array of argument names.
	 */
    static Type[] enhanceArgumentTypes(Type[] originalArgumentTypes) {
		return enhanceArgumentTypes(originalArgumentTypes, 0, true);
	}

	/**
	 *  Prepend hidden arguments to the signature.
	 *  This methods only treats argument types.
	 *  @see #enhanceArgumentNames
	 *
	 *  @param originalArgumentTypes array of original argument types.
	 *  @param idx position where new arguments should be inserted into
	 *             <tt>originalArgumentTypes</tt>.
	 *  @param createUnused should the <tt>unusedArgs</tt> argument be created?
	 *  @return augmented array of argument types.
	 */
    static Type[] enhanceArgumentTypes(Type[]  originalArgumentTypes,
								int     idx,
								boolean createUnused)
	{
    // creates enhanced argument type array:
    // ..(a1,.., aN) -> ..(Team[], int[], int, Object[], a1, .., aN)
		int offset = createUnused ? EXTRA_ARGS : EXTRA_ARGS-1;

        Type[] enhancedArgumentTypes =
			new Type[originalArgumentTypes.length+offset];

        for (int j=0; j<idx; j++)
            enhancedArgumentTypes[j] = originalArgumentTypes[j];

        enhancedArgumentTypes     [idx+TEAMS_ARG     - 1] = teamArray;
        enhancedArgumentTypes     [idx+TEAMIDS_ARG   - 1] = intArray;
        enhancedArgumentTypes     [idx+IDX_ARG       - 1] = Type.INT;
        enhancedArgumentTypes     [idx+BIND_IDX_ARG	 - 1] = Type.INT;
        enhancedArgumentTypes     [idx+BASE_METH_ARG - 1] = Type.INT;
		if (createUnused) {
			enhancedArgumentTypes [idx+UNUSED_ARG    - 1] = objectArray;
		}

        for (int j = idx; j < originalArgumentTypes.length; j++)
            enhancedArgumentTypes[j + EXTRA_ARGS] = originalArgumentTypes[j];

        return enhancedArgumentTypes;
    }

    /**
	 * Remove the arguments previously added by
	 * {@link #enhanceArgumentTypes enhanceArgumentTypes}.
	 * 
	 * @param enhancedArgumentTypes
	 * @param staticFlag
	 * @return
	 */
    // FIXME(SH): obsolete
    public static Type[] _retrenchArgumentTypes(Type[] enhancedArgumentTypes, boolean staticFlag) {
    // create retrenched argument type array:
    // ..(Team[], int[], int, Object[], a1, .., aN) -> ..(a1,.., aN)

        int offset = staticFlag? -1 : 0;
    	
        Type[] retrenchedArgumentTypes =
			new Type[enhancedArgumentTypes.length - EXTRA_ARGS + offset];

        for (int j = EXTRA_ARGS; j < enhancedArgumentTypes.length + offset; j++)
            retrenchedArgumentTypes[j - EXTRA_ARGS] = enhancedArgumentTypes[j];

        return retrenchedArgumentTypes;
    }

	// ---------------------------------------------------
	// ---------- further type and value conversions -----
	// ---------------------------------------------------

	/**
	 *  @see #generalizeReturnType(Type)
	 *  @param signature String from which to extract the return type.
	 */
	static Type generalizeReturnType (String signature) {
		Type type = Type.getReturnType(signature);
		return generalizeReturnType(type);
	}

	/**
	 * Given a return type, determine a reference type to which this type can be
	 * converted. "Object" if type is VOID.
	 * 
	 * @param type
	 * @return
	 */
	static Type generalizeReturnType (Type type) {
		if (type instanceof ReferenceType) return type;
		return object;
	}

	/**
	 * Get the generalized return type from <tt>sign1</tt>, unless
	 * <tt>sing2</tt> has void return type. In the latter case return
	 * <tt>Object</tt>.
	 * 
	 * @see #generalizeReturnType(Type)
	 * @param sign1
	 * @param sign2
	 * @return
	 */
	static Type generalizeReturnType (String sign1, String sign2) {
		Type type = Type.getReturnType(sign2);
		if (type == Type.VOID) return object;
		return generalizeReturnType(sign1);
	}

	/**
	 * Assuming a value of type <tt>oldType</tt> on the stack, convert it to a
	 * value of type <tt>newType</tt>. Changes instruction list <tt>il</tt>
	 * after position <tt>ih</tt> or at its end if <tt>ih</tt> is null.
	 * 
	 * @param il
	 * @param ih
	 * @param oldType
	 * @param newType
	 * @return the first inserted instruction or null if no adjustment needed
	 */
    InstructionHandle adjustValue (InstructionList il, InstructionHandle ih,
								   Type oldType, Type newType) {
		if (ih == null)
			ih = il.getEnd();
        if (oldType.equals(newType))
			return null;

        if (newType == Type.VOID)
            return il.append(ih, new POP());
        else if (oldType == Type.VOID)
            return il.append(ih, InstructionFactory.ACONST_NULL);
        else if (oldType instanceof BasicType)
            return il.append(ih, createBoxing((BasicType)oldType));
        else if (newType instanceof BasicType)
            return il.append(ih, createUnboxing((BasicType)newType));
        else
            return il.append(ih, factory.createCast(oldType, newType));
    }

	// ------------------------------------------
	// ---------- (Un-)Boxing: ------------------
	// ------------------------------------------

	/**
	 * Get the name of the class suitable for boxing <tt>basicType</tt>.
	 * 
	 * @param basicType
	 * @return
	 */
    static String toObjectTypeName(BasicType basicType) {
        String result = "";
        switch (basicType.getType()) {
            case Constants.T_BOOLEAN : result = "java.lang.Boolean";   break;
            case Constants.T_INT :     result = "java.lang.Integer";   break;
            case Constants.T_FLOAT :   result = "java.lang.Float";     break;
            case Constants.T_DOUBLE :  result = "java.lang.Double";    break;
            case Constants.T_SHORT :   result = "java.lang.Short";     break;
            case Constants.T_BYTE :    result = "java.lang.Byte";      break;
            case Constants.T_CHAR :    result = "java.lang.Character"; break;
            case Constants.T_LONG :    result = "java.lang.Long";      break;
            default: throw new Error("OTRE failure: Basic Type not supported!!"+basicType);
        }
        return result;
    }

	/**
	 * Create the instructions needed for boxing a basic type value. The value
	 * is expected on the stack an will be replaced by the boxed value.
	 * 
	 * @param basicType	type of the value on the stack.
	 * @return an InstructionList containing the conversion instructions.
	 */
    InstructionList createBoxing(BasicType basicType) {
        InstructionList il   = new InstructionList();
		String boxedTypeName = toObjectTypeName(basicType);
		                                              // .., result
        il.append(factory.createNew(boxedTypeName));  // .., result, box,

        if (basicType.equals(Type.DOUBLE) || basicType.equals(Type.LONG)) {
        // 'double' and 'long' are category 2 computational type:
            il.append(new DUP_X2());                  // .., box, result, box
            il.append(new DUP_X2());                  // .., box, box, result, box
        } else {
            il.append(new DUP_X1());                  // .., box, result, box
            il.append(new DUP_X1());                  // .., box, box, result, box
        }
        il.append(new POP());                         // .., box, box, result
        il.append(factory.createInvoke(boxedTypeName,
                                       Constants.CONSTRUCTOR_NAME,
                                       Type.VOID,
                                       new Type[] { basicType },
                                       Constants.INVOKESPECIAL));
        return il;
    }

	/**
	 * Create the instructions needed for unboxing a basic type value. The value
	 * is expected on the stack an will be replaced by the unboxed value.
	 * 
	 * @param basicType	expected type after unboxing.
	 * @return an InstructionList containing the conversion instructions.
	 */
    InstructionList createUnboxing(BasicType basicType) {
        InstructionList il   = new InstructionList();
		String boxedTypeName = toObjectTypeName(basicType);
        il.append(factory.createCast(object,
									 new ObjectType(boxedTypeName)));
        il.append(factory.createInvoke(boxedTypeName,
									   basicType.toString() + "Value",
									   basicType,
									   Type.NO_ARGS,
									   Constants.INVOKEVIRTUAL));
        return il;
    }

    /** Push an integer constant using the most appropriate/compact instruction. */
    Instruction createIntegerPush(ConstantPoolGen cpg, int val) {
    	if (val <= 5)
    		return new ICONST(val);
    	if (val <= Byte.MAX_VALUE)
    		return new BIPUSH((byte)val);
    	if (val <= Short.MAX_VALUE)
    		return new SIPUSH((short)val);
    	return new LDC(cpg.addInteger(val));
    }
    
    /** 
     * Create a throwing instruction for an OTREInternalError.
     * 
     * @param cpg
     * @param il		  instruction list to generate into
     * @param messagePush push sequence producing the exception message.
     * @return 			  handle to the first generated instruction
     */
	InstructionHandle createThrowInternalError(ConstantPoolGen cpg, InstructionList il, InstructionList messagePush) {
		InstructionHandle start = il.append(factory.createNew(OTConstants.internalError));
		il.append(new DUP());
		il.append(messagePush);
		il.append(factory.createInvoke(OTConstants.internalError.getClassName(),
				Constants.CONSTRUCTOR_NAME,
				Type.VOID,
				new Type[] { Type.STRING }, 
				Constants.INVOKESPECIAL));
		il.append(new ATHROW());
		return start;
	}

	/**
	 * Create a lookswitch from its constituents. Since JVM 1.4 this requires
	 * sorting of matches.
	 * 
	 * @param matches
	 * @param targets
	 * @param breaks an array of breaks (GOTOs) whose target will
	 *               be updated to point to <tt>afterSwitch</tt>
	 * @param defaultBranch
	 * @param afterSwitch
	 * @return the generated instruction.
	 */
	static BranchInstruction createLookupSwitch (int[] matches,
										  InstructionHandle[] targets,
										  GOTO[]              breaks,
										  InstructionHandle   defaultBranch,
										  InstructionHandle   afterSwitch) {

		int numberOfCases = matches.length;
        for (int i = 0; i < numberOfCases; i++)
            breaks[i].setTarget(afterSwitch);

        HashMap<Integer, InstructionHandle> match_target_mapping = new HashMap<Integer, InstructionHandle>();
        for (int i = 0; i < numberOfCases; i++)
            match_target_mapping.put(Integer.valueOf(matches[i]), targets[i]);

        Arrays.sort(matches);
        for (int i = 0; i < numberOfCases; i++)
            targets[i] = match_target_mapping.get(Integer.valueOf(matches[i]));

        BranchInstruction inst = new LOOKUPSWITCH(matches, targets, defaultBranch);
		return inst;
	}

	
    /**
	 * Read all byte code attributes for a given class. Side-Effect: depending
	 * on the Referenced-Team attribute, additional classes may be scheduled for
	 * loading.
	 * 
	 * @param ce
	 * @param cg
	 * @param class_name
	 * @param cpg
	 */
    public void checkReadClassAttributes(ClassEnhancer ce,
								         ClassGen cg,
								  		 String class_name,
								  		 ConstantPoolGen cpg)
    {
    	AttributeReadingGuard guard = AttributeReadingGuard.getInstanceForLoader(this.loader);
    	boolean addTeamInitializations = false;
    	List<String> classesToLoad;
    	synchronized (guard) {
    		if (!guard.iAmTheFirst(class_name))
    			return;
			if (AttributeReadingGuard.isFirstLoadedClass())
				addTeamInitializations = true;
			// scan for attributes here, because this transformer is applied first:
			Attribute[] attrsClass = cg.getAttributes();
			classesToLoad = scanClassOTAttributes(attrsClass, class_name, cpg, cg);
			
			guard.workDone(class_name);
		}
    	if (addTeamInitializations)
    		addTeamInitializations(cg, ce);
    	
		Iterator<String> it = classesToLoad.iterator();
		while (it.hasNext()) {
			String next = it.next();
            if(logging) printLogMessage("Loading of class " + next + " will be forced now!");
			ce.loadClass(next, this);
		}
			
		// scan for parameter bindings:
		Method[] possibleRoleMethods = cg.getMethods();
		for (int i=0; i<possibleRoleMethods.length; i++) {
			Method meth = possibleRoleMethods[i];
			Attribute[] attrsMethod = meth.getAttributes();
			scanMethodOTAttributes(attrsMethod, class_name, meth.getName(), cpg);
		}
        if(logging) printLogMessage(this.getClass().getName()
						+ " picked up the attributes for class " + class_name );
    }
        
    // --- helpers for adding line numbers at the front of a method  ---
    // unfortunately BCEL does not sort line numbers, but the debugger expects them sorted.
    class Pair<F,S> {
    	F first;
    	S second;
    	Pair(F f, S s) {
    		this.first = f;
    		this.second = s;
    	}
    }
    @SuppressWarnings("unchecked") // can't declare array of generics
	Pair<InstructionHandle, Integer>[] saveLineNumbers(MethodGen method, ConstantPoolGen cpg) {
    	LineNumberTable lnt = method.getLineNumberTable(cpg);
    	InstructionHandle[] ihs = method.getInstructionList().getInstructionHandles();
    	Pair<InstructionHandle, Integer>[] oldLines = new Pair[lnt.getTableLength()];
    	{
			int cur = -1;
			int n = 0;
			for (int i=0; i<ihs.length; i++) {
				int next = lnt.getSourceLine(ihs[i].getPosition());
				if (next > cur)  // reached a new source line
					oldLines[n++] = new Pair<InstructionHandle, Integer>(ihs[i], new Integer(next));
				cur = next;
			}
    	}
    	return oldLines;    	
    }
    /** Append the saved line numbers to the end of the method's line number table. */
	void restoreLineNumbers(MethodGen method, Pair<InstructionHandle, Integer>[] oldLines) {
		for (int i=0; i<oldLines.length; i++) {
			if (oldLines[i] == null)
				continue;
			InstructionHandle ih = oldLines[i].first;
			int line = oldLines[i].second.intValue();
			method.addLineNumber(ih, line);
		}
	}

    /**
	 * Adds team initialization for all teams in the config file.
	 * 
	 * @param cg
	 * @param ce
	 */
    private void addTeamInitializations(ClassGen cg, ClassEnhancer ce) {
    	if (isLoadingSystemClass) {
    		// JPLIS launching may intercept system classes before the custom main.
    		AttributeReadingGuard.reset(); // reset the guard in order to retry with subsequent classes.
    		return;
    	}
    	String main_class_name = cg.getClassName();
    	ConstantPoolGen cpg = cg.getConstantPool();
    	InstructionFactory factory = new InstructionFactory(cpg);
    	Method main = cg.containsMethod("main", "([Ljava/lang/String;)V");
    	if (main == null) {
    		// no main method in the first loaded class...
    		AttributeReadingGuard.reset(); // reset the guard in order to retry with subsequent classes.
    		return;
    	}
    	
    	MethodGen mainMethod = newMethodGen(main, main_class_name, cpg);
    	InstructionList il = mainMethod.getInstructionList();
    	
    	int startLine = -1;
    	Pair<InstructionHandle,Integer>[] oldLines = null;
    	if (debugging) {
    		LineNumberTable lnt = mainMethod.getLineNumberTable(cpg);
    		if (lnt != null) {
    			startLine = lnt.getSourceLine(0);
    			oldLines = saveLineNumbers(mainMethod, cpg);
    		}
    	}
    	
    	if (TEAM_CONFIG_FILE != null) {
    		
    		InstructionList teamInitializations = new InstructionList();
    		List<String> teamsToInitialize = getTeamsFromConfigFile();
    		Iterator<String> teamIt = teamsToInitialize.iterator();
    		while (teamIt.hasNext()) {
    			String nextTeam = teamIt.next();
    			JavaClass teamClass = null;
    			try {
    				teamClass = RepositoryAccess.lookupClassFully(nextTeam);
    			} catch (ClassNotFoundException cfne) {
    				System.err.println("Config error: Team class '"+nextTeam+ "' in config file '"+ TEAM_CONFIG_FILE+"' can not be found!");
    				continue;
    			}
    			ClassGen teamClassGen = new ClassGen(teamClass);
    			if (teamClassGen.containsMethod(Constants.CONSTRUCTOR_NAME, "()V") == null) {
    				System.err.println("Activation failed: Team class '"+nextTeam+ "' has no default constuctor!");
    				continue;
    			}
    			ce.loadClass(nextTeam, this);
    			if (logging)
					printLogMessage("Adding initialization of team " + nextTeam
							+ " to main method of class " + main_class_name);
    			teamInitializations.append(factory.createNew(nextTeam));
    			teamInitializations.append(new DUP());
    			teamInitializations.append(factory.createInvoke(nextTeam,
    					Constants.CONSTRUCTOR_NAME,
						Type.VOID,
						Type.NO_ARGS,
						Constants.INVOKESPECIAL));
    			teamInitializations.append(factory.createGetStatic(OTConstants.teamClassName,
						"ALL_THREADS",
						 OTConstants.threadType));
    			teamInitializations.append(factory.createInvoke(nextTeam,
    					"activate",
						Type.VOID,
						new Type[] {OTConstants.threadType},
						Constants.INVOKEVIRTUAL));
    		}
    		il.insert(teamInitializations);
    	}
    	// register main thread with TeamThreadManager:
        InstructionHandle cursor;
        cursor = il.insert(new ICONST(1)); // isMain=true
        cursor = il.append(cursor, new ACONST_NULL()); // parent=null
        cursor = il.append(cursor, factory.createInvoke("org.objectteams.TeamThreadManager", 
						                "newThreadStarted",
										 Type.BOOLEAN,
										 new Type[]{Type.BOOLEAN, OTConstants.threadType},
										 Constants.INVOKESTATIC));
        cursor = il.append(cursor, new POP()); // don't use boolean return

    	il.setPositions(); 
    	if (debugging && startLine > 0) {
    		mainMethod.removeLineNumbers(); 					  // fresh start, to ensure correct order
    		mainMethod.addLineNumber(il.getStart(), startLine-1); // new number to the front
    		restoreLineNumbers(mainMethod, oldLines);		      // append old numbers
    	}
    	mainMethod.setInstructionList(il);
    	mainMethod.setMaxStack();
    	mainMethod.setMaxLocals();
    	
    	cg.replaceMethod(main, mainMethod.getMethod());
    	JPLISEnhancer.requireClassFileVersionLessThan51(cg);
    }

	/**
	 * @return a list of teams in the team initialization config file
	 */
	private static List<String> getTeamsFromConfigFile() {
		List<String> result = new LinkedList<String>();
		try {
			FileInputStream fstream = new FileInputStream(TEAM_CONFIG_FILE);
			BufferedReader in = new BufferedReader(new InputStreamReader(fstream));
 			while (in.ready()) {
				String nextLine = in.readLine();
				String nextTeam = nextLine.trim();
				if (nextTeam.startsWith(COMMENT_MARKER))
					continue; // this is a comment line
				if (!nextTeam.equals("")) {
					result.add(nextTeam.trim());
				}
			}
			in.close();
		} catch (Exception e) {
			System.err.println("File input error: config file '" + TEAM_CONFIG_FILE + "' can not be found!");
		}
		return result;
	}

	 // -------------------------------------------------------------------------------------------------
	 // -------- store and return adapted bases for OT/Equinox --------------
	 // this data is collected by scanClassOTAttributes and must be collected by the caller
	 // before processing the next class.
	 // -------------------------------------------------------------------------------------------------
	 public HashSet<String> adaptedBases = new HashSet<String>();

	 /** Internal API for {@link org.eclipse.objectteams.otre.jplis.ObjectTeamsTransformer} */
	 public Collection<String> fetchAdaptedBases() {
		 HashSet<String> result;
		 result = new HashSet<String>(adaptedBases);
		 adaptedBases.clear();
		 return result;
	 }

	 // ------------------------------------------------------------------------------------------------- 
     /**
 	  * Container for base method properties
	  */
	 public static class BaseMethodInfo {
		 private String baseClassName;
		private String baseMethodName;
		private String baseMethodSignature;
		boolean isCallin;
		private boolean isRoleMethod;
		boolean isStatic;
		private int[] parameterPositions;
		int translationFlags;

		BaseMethodInfo(String base_class_name, String base_method_name,
					   String base_method_signature, boolean isCallin,
					   boolean isRoleMethod, boolean isStatic,
					   int[] parameter_positions, int translationFlags) 
		{
			this.baseClassName = base_class_name;
			this.baseMethodName = base_method_name;
			this.baseMethodSignature = base_method_signature;
			this.isCallin = isCallin;
			this.isRoleMethod = isRoleMethod;
			this.isStatic = isStatic;
			this.parameterPositions = parameter_positions;
			this.translationFlags = translationFlags;
		}
		/** Minimal version to pass some info into #translateLoads(): */
		BaseMethodInfo(boolean isCallin, boolean isStatic, int translationFlags) 
		{
			this.isCallin = isCallin;
			this.isStatic = isStatic;
			this.translationFlags = translationFlags;
		}

		public boolean isStaticRoleMethod() {
			return this.isRoleMethod && this.isStatic;
		}

		String getBaseClassName() {
			return baseClassName;
		}

		String getBaseMethodName() {
			return baseMethodName;
		}

		String getBaseMethodSignature() {
			return baseMethodSignature;
		}

		int[] getParameterPositions() {
			return parameterPositions;
		}
	}
	
	/**
	 * Scan the Attributes found in the class class_name for binding attributes
     * and registers them in the CallinBindingManager.
	 * 
	 * @param attributes    the Attributes to be examined
	 * @param class_name    the name of the class where the Attributes were found
     * @param cpg           the classes ConstantPoolGen
	 * @return              an ArrayList containing the names of classes which have
	*                       to be loaded immediately
    */
    ArrayList<String> scanClassOTAttributes(Attribute[] attributes,
								 String          class_name,
								 ConstantPoolGen cpg,
								 ClassGen cg)
	{
        if(logging) printLogMessage("Inspecting " + class_name);
        ArrayList<String> classesToLoad = new ArrayList<String>();
        String base_class_name = null;
        for (int k=0; k<attributes.length; k++) {
            Attribute actAttr = attributes[k];
            Unknown attr = isOTAttribute(actAttr);
	
            if (attr != null) { //this is a callin attribute
                String attrName = attr.getName();
                if(logging) printLogMessage("CallinBindingAttribute: " + attrName);
                byte[] indizes = attr.getBytes();
                int count = combineTwoBytes(indizes, 0);
                int numberOfEntries=0;
				String [] names;
				if (attrName.equals("OTClassFlags")) {
					int classFlags = combineTwoBytes(indizes, 0);
					String flagsString = "";
					if ((classFlags & 1) != 0) {
						flagsString = "team ";
						// TODO: use this instead of team modifier
					}
					if ((classFlags & 2) != 0) {
						flagsString += "role";
						CallinBindingManager.addRole(class_name);
					}
                    if (logging) {
						printLogMessage("OTClassFlags:");
						printLogMessage("\t" + flagsString);
					}
				} else if (attrName.equals("CallinRoleBaseBindings")) {
                    numberOfEntries = 2;
                    int i = 2;
                    int n = 2 * count * numberOfEntries; // n = count << 2; 
                    names = new String[numberOfEntries];
                    while (i <= n) {
						i = scanStrings(names, indizes, i, cpg);
						String role_name = names[0];
						String base_name = names[1];
						boolean baseIsInterface = false;
						if (base_name.charAt(0) == '^') {
							baseIsInterface = true;
							base_name = base_name.substring(1);
						}
                        if(logging) printLogMessage("**** Binding: " + role_name 
                                                    + " playedBy " + base_name);

                        //set binding:
                        CallinBindingManager.addRoleBaseBinding(role_name, base_name, baseIsInterface, class_name);
                        CallinBindingManager.addTeamBaseRelation(class_name, base_name);
                        
                		// [OT/Equinox] store adapted bases:
               			adaptedBases.add(base_name);

                        // super roles have to be loaded first for binding inheritance purpose:
                        // not necessary anymore?
                        //classesToLoad.addAll(getSuperRoles(role_name, attributes, cpg));
                        // roles themselve have to be loaded too:
                        classesToLoad.add(role_name);
                    }
				} else if (attrName.equals("BoundClassesHierarchy")) {
                    numberOfEntries = 2;
                    int i = 2;
                    int n = 2 * count * numberOfEntries; // n = count << 2; 
                    names = new String[numberOfEntries];
                    while (i <= n) {
						i = scanStrings(names, indizes, i, cpg);
						String sub_name = names[0];
						String super_name = names[1];
						
						CallinBindingManager.addBoundSuperclassLink(sub_name, super_name);
                        if(logging)printLogMessage("**** super-class link: "+sub_name
										+" -> "+super_name);
                    }
                } else if (attrName.equals("CallinMethodMappings")) {
                    //numberOfEntries = 6;
                    int i = 2;
                    for (int n=0; n<count;n++) {
                    	// JSR-045 support:
                    	names = new String[1];
                    	i = scanStrings(names, indizes, i, cpg);
                    	String binding_file_name = names[0];
                    	int binding_line_number = combineTwoBytes(indizes, i);
                    	i += 2;
                    	int binding_line_offset = combineTwoBytes(indizes, i);
                    	i += 2;
                    	boolean is_static_role_method = false;
                    	boolean covariant_base_return= false;
                    	// regular stuff:
                    	numberOfEntries = 3;
						names = new String[numberOfEntries];
						i = scanStrings(names, indizes, i, cpg);
						String wrapper_name = null;
						String wrapper_signature = null;
						// first 3 names:
						int index = 0;
						
						String binding_label         = names[index++];
						String role_method_name      = names[index++];
						String role_method_signature = names[index++];
						
						{	// a flag:
							int flags = combineTwoBytes(indizes, i);
							is_static_role_method = (flags & 1) != 0;
							// flag value 4 is "inherited"
							covariant_base_return = (flags & 8) != 0;
							i+=2;
						}
						// 3 more names
						numberOfEntries = 3;
						names = new String[numberOfEntries];
						i = scanStrings(names, indizes, i, cpg);
						index = 0;
						//if (NEW_COMPILER_VERSION)
						// static_role_method = see attribute
							
						String lift_method_name				= names[index++];
						String lift_method_signature		= names[index++];
						String binding_modifier				= names[index++];

                        int base_len = combineTwoBytes(indizes, i);
                        i += 2;

                        names = new String[4];
                        for (int n_base = 0; n_base < base_len; n_base++) {
                            i = scanStrings(names, indizes, i, cpg);
                            String base_method_name         = names[0];
						    String base_method_signature	= names[1];
							wrapper_name               		= names[2];
							wrapper_signature          		= names[3];

							byte baseFlags = indizes[i++];
							boolean baseIsCallin = (baseFlags & 1) != 0;
							boolean baseIsStatic = (baseFlags & 2) != 0;
	                        int translationFlags = (combineTwoBytes(indizes, i)<<16) + combineTwoBytes(indizes, i+2);
	                        i += 4;

                            if(logging) {
                                printLogMessage("**** Binding: " + binding_label + ":"
                                        + role_method_name + role_method_signature
                                        + " <- " + binding_modifier + " "
                                        + base_method_name + base_method_signature);
                                printLogMessage("**** Wrapper: " + wrapper_name
                                        + wrapper_signature);
                            }
                            //set binding:
                            CallinBindingManager.addMethodBinding(class_name,
                            									  base_class_name, // previously read from PlayedBy attribute
                            								  	  binding_file_name,
															  	  binding_line_number,
															  	  binding_line_offset,
															  	  binding_label,
															  	  role_method_name,
															  	  role_method_signature,
															  	  is_static_role_method,
															  	  wrapper_name,
															  	  wrapper_signature,
															  	  binding_modifier,
															  	  base_method_name,
															  	  base_method_signature,
															  	  baseIsStatic,
															  	  baseIsCallin,
															  	  covariant_base_return,
															  	  translationFlags,
															  	  lift_method_name, 
															  	  lift_method_signature); 

                        }
                    }
                } else if (attrName.equals("OTSpecialAccess")) {
                	numberOfEntries = 3;
                	int i = 2;
                	for (int j = 0; j < count; j++) {
                		short kind = indizes[i++];
                		switch (kind) {
                		case 1: // DecapsulatedMethodAccess
                			names = new String[numberOfEntries];
                			i = scanStrings(names, indizes, i, cpg);
                			if(logging) printLogMessage("**** Callout: " + names[0] + "." 
								     + names[1]+ " " + names[2]);
                			CallinBindingManager.addCalloutBinding(names[0], names[1], names[2]);
                			break;
                		case 2: // CalloutFieldAccess
                			short flags = indizes[i++];
                			String accessMode = (flags & 1) == 1 ? "set" : "get";
                			boolean isStaticField = (flags & 2) != 0;
                			names = new String[numberOfEntries];
                			i = scanStrings(names, indizes, i, cpg);
                			if (logging)
                				printLogMessage("**** Callout bound field: " + accessMode+(isStaticField?" static ":" ")
                							    + names[2]+ " " + names[1]);
                			CallinBindingManager.addCalloutBoundFileds(names[0], names[1], names[2], accessMode, isStaticField);
                			
                			break;
                		case 3: // SuperMethodAccess
                			numberOfEntries = 4; 
                			names = new String[numberOfEntries];
                			i = scanStrings(names, indizes, i, cpg);
                			if(logging) printLogMessage("**** SuperAccess: " + names[0] + "." 
								     + names[2]+ names[3]+" superclass "+names[1]);
                			CallinBindingManager.addSuperAccess(names[0], names[1], names[2], names[3]);
                			break;
                		}
                	}                	
                	// adapted baseclasses (w/ or w/o decapsulation):
                	count = combineTwoBytes(indizes, i);
                	i += 2;
                	names = new String[1];
                	for (int j=0; j<count; j++) {
	                	i = scanStrings(names, indizes, i, cpg);
	                	byte flag = indizes[i++];
	                	if (flag == 1) {
							CallinBindingManager.addBaseClassForModifierChange(names[0]);
						} else {
	                		// [OT/Equinox]: store adapted bases:
                			adaptedBases.add(names[0]);
						}
                	}
				} else if (attrName.equals("ReferencedTeams")) {
                    numberOfEntries = 1;
                    int i = 2;
                    int n = 2 * count * numberOfEntries; // n = count << 1;
                    names = new String[numberOfEntries];
                    while (i <= n) {
						i = scanStrings(names, indizes, i, cpg);
						String referenced_team = names[0];
                        if(logging) printLogMessage("**** found ReferencedTeams: " + referenced_team);
                        classesToLoad.add(referenced_team);

                    }
				} else if (attrName.equals("PlayedBy")) {
					names = new String[1];
					scanStrings(names, indizes, 0, cpg);
					base_class_name = names[0];
					int langle = base_class_name.indexOf('<');
					if (langle > -1) // it's an anchored type p.T$R<@C.o.f>, cut off everything after and including '<'.
						base_class_name = base_class_name.substring(0, langle-1);
                    if(logging) printLogMessage("**** found PlayedBy:  " + base_class_name);
                    // base_class_name is stored for later use, when method bindings are found.
					CallinBindingManager.addSuperRoleLink(cg.getClassName(), cg.getSuperclassName());
					// this information is NOT NEEDED at moment!		
				} else if (attrName.equals("OTCompilerVersion")) {
					int encodedVersion = combineTwoBytes(indizes, 0);
					int major = encodedVersion >>> 9;
					int minor = (encodedVersion >>> 5) & 0xF;
					int revision = encodedVersion & 0x1F;
                    if(logging) printLogMessage("**** class file was produced by compiler version "
                            + major + "." + minor + "." + revision + " ****");
                    IS_COMPILER_GREATER_123 = false; // reset, may be updated below
					// 1.6 stream:
					if (major == 1 && minor == 6) {
						// accept all revisions within this stream
						IS_COMPILER_GREATER_123 = true;
						IS_COMPILER_13X_PLUS = true;
						IS_COMPILER_14X_PLUS = true;
					// 1.5 stream:
					} else if (major == 1 && minor == 5) {
						if (revision < OT15_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
						IS_COMPILER_GREATER_123 = true;
						IS_COMPILER_13X_PLUS = true;
						IS_COMPILER_14X_PLUS = true;
					// 1.4 stream:
					} else if (major == 1 && minor == 4) {
						if (revision < OT14_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
						IS_COMPILER_GREATER_123 = true;
						IS_COMPILER_13X_PLUS = true;
						IS_COMPILER_14X_PLUS = true;
					// 1.3 stream:
					} else if (major == 1 && minor == 3) {
						if (revision < OT13_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
						IS_COMPILER_GREATER_123 = true;
						IS_COMPILER_13X_PLUS = true;
					// 1.2 stream:
					} else if (major == 1 && minor == 2) {
						if (revision < OT12_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
						if (revision > 3)
							IS_COMPILER_GREATER_123 = true;
						// 1.1 stream:
					} else if (major == 1 && minor == 1) {
						if (revision < OT11_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
						// 1.0 stream:
					} else if (major == 1 && minor == 0) {
						if (revision < OT10_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
					// 0.9 stream:
					} else if (major == 0 && minor == 9) {
						if (revision < OT09_REVISION) {
							if (class_name.startsWith(OTConstants.teamClassName))
								continue; // no specific byte codes in ooTeam and its inner classes.
							throw new InternalError("OTRE: Class " + class_name + " has unsupported revision " + revision);
						}
					// other major/minor version?
					} else {
						if (major != OT_VERSION_MAJOR)
							throw new InternalError("OTRE: Class " + class_name + " has unsupported major version " + major);
						if (minor != OT_VERSION_MINOR)
							throw new InternalError("OTRE: Class " + class_name + " has unsupported minor version " + minor);
						throw new InternalError("OTRE: Class " + class_name + " has unrecognized version " + major+"."+minor+"."+revision);
					}
				} else if (attrName.equals("CallinPrecedence")) {
                    List<String> precedenceList = new LinkedList<String>(); 
                    numberOfEntries = 1;
                    int i = 2;
                    int n = 2 * count * numberOfEntries; // n = count << 1;
                    names = new String[numberOfEntries];
                    while (i <= n) {
                        i = scanStrings(names, indizes, i, cpg);
                        String binding_label = names[0];
                        precedenceList.add(binding_label);
                    }
                    if(logging) printLogMessage("**** found precedence list for " + class_name + ": "
                            + precedenceList + " ****");
                    CallinBindingManager.addPrecedenceList(precedenceList, class_name);
                }
            }
        }
        return classesToLoad;
    }
    
	/**
	 * Read some strings from a byte array.
	 * @param entries Result array to be provided by caller.
	 * @param indizes buffer of read bytes to be provided by caller,
	 *                consists if indizes into the constant pool
	 * @param i       current index into indizes
	 * @param cpg     the pool.
	 * @result        updated value of <tt>i</tt>.
	 */
	public static int scanStrings(String[] entries,
					byte[]          indizes,
					int             i,
					ConstantPoolGen cpg)
	{
		for (int j = 0; j < entries.length; j++) {
			int nextIndex = combineTwoBytes(indizes, i);
			ConstantUtf8 cons      = (ConstantUtf8)cpg.getConstant(nextIndex);
			String       content   = cons.getBytes();
			entries[j] = content;
			i += 2;
		}
		return i;
	}

    /**
    *  Scan the Attributes found in the method <tt>method_name</tt> for binding attributes
    *  and registers them in the {@link CallinBindingManager CallinBindingManager}.
    *
    *  @param attributes    the Attributes to be examined
    *  @param class_name    the name of the class where the Attributes were found
    *  @param method_name   the name of the method where the Attributes were found
    *  @param cpg
    */
    static void scanMethodOTAttributes(Attribute[] attributes,
								String          class_name,
								String          method_name,
								ConstantPoolGen cpg)
	{
        for (int k = 0; k < attributes.length; k++) {
            Attribute actAttr = attributes[k];
            Unknown attr = isOTAttribute(actAttr);
            if (attr != null) { //this is an OT attribute
                String attrName = attr.getName();
                if(logging) printLogMessage("CallinBindingAttribute(" + method_name + ") :"
                        + attrName);
                if (attrName.equals("CallinParamMappings")) {
					byte[] indizes = attr.getBytes();
					int [] positions = null;
					if (indizes == null) throw new RuntimeException("Unexpected null attr");
					int count = combineTwoBytes(indizes, 0);
					positions = new int[count];
					int p = 2;
					for (int i = 0; i < count; i++, p += 2) {
						positions[i] = combineTwoBytes(indizes, p);
						// System.out.println(" "+i+"<-"+positions[i]);
					}
					CallinBindingManager.addParameterBinding(class_name,
															 method_name,
															 positions);
					// Here it is correct to use the (wrapper) method name without a signature, 
					// because for overloaded methods there are separate wrappers with unique (mangled) names. 
				}
            }
        }
    }


	/**
    *  Determines, if the given Attribute is a callin-attribute.
    *
    *  @param attr      the Attriute to be checked
    *  @return          the Attribute casted to Unknown, for later use, if true
    *                   null, if it is not a callin-attribute
    */
    static Unknown isOTAttribute(Attribute attr) {
        if (attr instanceof Unknown) {
            Unknown unknown = (Unknown)attr;
            String attrName = unknown.getName();
            if (attrName.equals("CallinRoleBaseBindings") 	||
                attrName.equals("BoundClassesHierarchy")    ||
                attrName.equals("CallinMethodMappings")   	||
                attrName.equals("CallinParamMappings")     	||
                attrName.equals("CallinFlags")             	||
				attrName.equals("OTSpecialAccess")     		||
                attrName.equals("WrappedRoleSignature")   	||
				attrName.equals("WrappedBaseSignature") 	||
                attrName.equals("ReferencedTeams")        	||
                attrName.equals("PlayedBy") 			  	||
                attrName.equals("Modifiers")				||
                attrName.equals("OTCompilerVersion") 		||
                attrName.equals("OTClassFlags")				||
                attrName.equals("OTJoinPoints")				||
				attrName.equals("CallinPrecedence")			||
				attrName.equals("StaticReplaceBindings"))
            {
                return unknown;
            }
        }
        return null;
    }

    /**
    *  Combines two int's representing the higher and the lower part
	*  of a two byte number.
    *
    *  @param first     the first (higer?) byte
    *  @param second    the second (lower?) byte
    *  @return          the combined number
    */
    public static int combineTwoBytes(byte [] indizes, int start) {
		int first = indizes[start];
		int second = indizes[start + 1];
		int twoBytes = 0;

		twoBytes = twoBytes | (first & 0xff);
		twoBytes = twoBytes << 8;
		twoBytes = twoBytes | (second & 0xff);
		return twoBytes;
    }
    
	/**
	 *  Returns a list of names of all super classes of class 'role_name' which are inner
	 *  classes of the enclosing team, what are all super roles of the 
	 *  role 'role_name'.
	 *
	 *  @param role_name     the name of the role class
	 *  @param attributes    the attributes of the enclosing team class
	 *  @param cpg	the constant pool of the enclosing team class
	 *  @return          the list of super role names
	 */
	/*
	private List getSuperRoles(String role_name, Attribute[] attributes, ConstantPoolGen cpg) {
		LinkedList superRoleNames = new LinkedList();
		JavaClass[] super_classes = Repository.getSuperClasses(role_name);
		 if (super_classes.length < 2) {// extends only Object, or none?
		 	return superRoleNames;
		 }
		LinkedList innerClassNames = new LinkedList();
		for (int i=0; i<attributes.length; i++) {
			Attribute actAttr = attributes[i];
			if (actAttr instanceof InnerClasses) {
				InnerClass[] inners = ((InnerClasses)actAttr).getInnerClasses();
				for (int j=0; j<inners.length; j++) {
					int name_index = inners[j].getInnerNameIndex();
					Constant name_c = cpg.getConstant(name_index);
					String name = ((ConstantUtf8)name_c).getBytes();
					int outer_class_index = inners[j].getOuterClassIndex();	
					Constant outer_c = cpg.getConstant(outer_class_index);
					String outerName = ((ConstantClass)outer_c).getBytes(cpg.getConstantPool());
					outerName = outerName.replace('/', '.');
					innerClassNames.add(outerName + "$" +name);
				}
			}
		}
		for (int i=0; i<super_classes.length; i++) {
			String superClassName = super_classes[i].getClassName();
			if (innerClassNames.contains(superClassName)) {
				superRoleNames.addFirst(superClassName);
			}
		}
		return superRoleNames;	
	}*/
    
    /** Create a MethodGen and remove some unwanted code attributes (which would need special translation which we don't have) */
    protected static MethodGen newMethodGen(Method m, String class_name, ConstantPoolGen cp) {
    	MethodGen mg = new MethodGen(m, class_name, cp);
    	List<Attribute> attributesToRemove = new ArrayList<Attribute>();
    	for (Attribute attr : mg.getCodeAttributes())
    		if (attr instanceof Unknown || attr instanceof StackMap)
    			attributesToRemove.add(attr);
		for (Attribute attr : attributesToRemove)
			mg.removeCodeAttribute(attr);
    	return mg;
    }
    
    /**
     * Remove all contents of a method as preparation for adding a new implementation
	 *
     * @param m 			The original method
     * @param class_name 	The class containing the method
     * @param cpg 			The class' constant pool
     * @return An empty method generator with the same declaration as m 
     *     	   and no implementation.
     */
    protected static MethodGen wipeMethod(Method m, String class_name, ConstantPoolGen cpg) {
        MethodGen mg;
        mg = new MethodGen(m, class_name, cpg);
        mg.getInstructionList().dispose(); //throw away the old implementation
        mg.removeLineNumbers();
        mg.removeLocalVariables();
        mg.removeExceptionHandlers();
        mg.removeAttributes();
        mg.removeCodeAttributes();
        return mg;
    }
    
	/**
	 * Add instructions of InstructionList il after the super constructor call of this constuctor.
	 *
	 * @param m 		the constructor method
	 * @param addedCode	the InstructionList containing the instructions to be added
	 * @param cg		the ClassGen
	 * @param cpg		the constant pool
	 */
	static void addToConstructor(Method m, InstructionList addedCode, ClassGen cg, ConstantPoolGen cpg) {
		String class_name = cg.getClassName();
		MethodGen mg = newMethodGen(m, class_name, cpg);
		InstructionList il = mg.getInstructionList().copy();
		InstructionHandle[] ihs = il.getInstructionHandles();

		MethodGen newConstructor = new MethodGen(mg.getAccessFlags(),
												 mg.getReturnType(),
												 mg.getArgumentTypes(),
												 mg.getArgumentNames(),
												 mg.getName(),
												 class_name,
												 il,
												 cpg );

//[SH:] 				
		updateCopiedMethod(mg, il, ihs, newConstructor);
//[:HS]

		int stackDepth = 0;
		int actInstrIndex = 0;
		
		boolean pauseStackCounting = false;
		InstructionHandle gotoTarget = null;
		
		// skip everything up to and including super() or this() call
		while (!((ihs[actInstrIndex].getInstruction() instanceof INVOKESPECIAL)
					&& (stackDepth - (ihs[actInstrIndex].getInstruction().consumeStack(cpg))) == 0)) {

			Instruction actInstruction = ihs[actInstrIndex].getInstruction();

			if (gotoTarget != null && actInstruction.equals(gotoTarget.getInstruction())) {
				pauseStackCounting = false;
				gotoTarget = null;
			}
			
			if (actInstruction instanceof GotoInstruction) {
				GotoInstruction gotoInsruction = (GotoInstruction)actInstruction;
				gotoTarget = gotoInsruction.getTarget();
				pauseStackCounting = true;
			}
			if (!pauseStackCounting) {
				stackDepth -= actInstruction.consumeStack(cpg);
				stackDepth += actInstruction.produceStack(cpg);
			}

			actInstrIndex++;
		}
		
		InstructionHandle ih = ihs[actInstrIndex];
		INVOKESPECIAL invoke = (INVOKESPECIAL)ih.getInstruction();
		String specialName = invoke.getName(cpg);
		if (!specialName.equals(Constants.CONSTRUCTOR_NAME)) {
			System.err.println("###ALERT: " + specialName);
			return;
		}

		if (logging) printLogMessage("Adding code to " + class_name + "." + m.getName());
		
		// calculate the length of the added code BEFORE it is inserted into the instruction list:
//[SH:] extracted for adding further manipulation of addedCode:
		int addedCodeLength = padCodeToAdd(addedCode);
//[:HS]
		
		InstructionHandle startOfAddedCode = il.append(ih, addedCode);

		il.setPositions();

		newConstructor.setInstructionList(il);
		newConstructor.setMaxStack();
		newConstructor.setMaxLocals();
//[SH:] if an unused local variable was copied in updateCopiedMethod() 
//      we have to adjust max_locals,
//      because unused locals are not found by setMaxLocals().
		newConstructor.setMaxLocals(Math.max(newConstructor.getMaxLocals(), mg.getMaxLocals()));
//[:HS]
		
		copyAndAdjustLineNumbers(mg, newConstructor, addedCodeLength, startOfAddedCode);
		
		cg.replaceMethod(m, newConstructor.getMethod());
    	JPLISEnhancer.requireClassFileVersionLessThan51(cg);
	}

	/** 
	 * Pads a given instruction list to multiples of 4 returning the padded length.
	 * Note that clients of this function must not call removeNOPs()!
	 * 
	 * Rationale: 
	 * switch instructions pad the lists of jmp targets to start at an offset % 4 == 0. 
	 * In order to ensure that this padding will not change when inserting new bytes
	 * the added bytes have to be a multiple of 4.
	 */
	static int padCodeToAdd(InstructionList addedCode) {
//[orig:]
		int addedCodeLength = 0;
		Instruction[] instr = addedCode.getInstructions();
		for (int i = 0; i < instr.length; i++) {
			addedCodeLength += instr[i].getLength();
		}
//[:giro]
		while (addedCodeLength % 4 > 0) {
			addedCode.append(new NOP());
			addedCodeLength++;
		}
		return addedCodeLength;
	}

//[SH:] helper methods for more complete method copying:
	
	/** After a method (incl. its instruction list) has been copied,
	 *  we need to manually copy some more properties:
	 *  + local variables
	 *  + declared exceptions
	 *  + exception handlers.
	 */
	static void updateCopiedMethod(MethodGen methodOrig, 
								   InstructionList ilCopy, 
								   InstructionHandle[] ihsCopy, 
								   MethodGen methodCopy) 
	{
		// local variables:
		LocalVariableGen[] oldLocals = methodOrig.getLocalVariables();
		int argLen = methodOrig.getArgumentTypes().length;
		if (!methodOrig.isStatic())
			argLen++; // this
		if (oldLocals.length > argLen) {
			InstructionList oldIL = methodOrig.getInstructionList();
			int maxLocals = methodOrig.getMaxLocals();
			for (int i = argLen; i<oldLocals.length; i++) {
				LocalVariableGen var = oldLocals[i];
				LocalVariableGen newVar =
					methodCopy.addLocalVariable(var.getName(), var.getType(), 
												mapIH(var.getStart(), oldIL, ihsCopy), 
												mapIH(var.getEnd(),   oldIL, ihsCopy));
				newVar.setIndex(var.getIndex());
				// reset, addLocalVariable might have changed this.
				methodCopy.setMaxLocals(maxLocals); 
			}
		}
		// declared exceptions:
		BaseCallRedirection.copyExceptionHandlers(methodOrig, methodCopy, ilCopy);
		// exception handlers:
		for(String excName : methodOrig.getExceptions())
			methodCopy.addException(excName);
	}
	
	static InstructionHandle mapIH(InstructionHandle alienIH, InstructionList oldIL, InstructionHandle[] newIHs) 
	{
		int position = alienIH.getPosition();
		int[] newPositions = oldIL.getInstructionPositions();
		for (int i=0; i<newPositions.length; i++) {
			if (newPositions[i] == position)
				return newIHs[i];
		}
		return null;
	}
//[:HS]
	
	/**
	 * Copy all line numbers from <tt>src</tt> to <tt>dest</tt>.
	 * For bytecode instructions after the added code area an offset has to be added to the 
	 * bytecode positions of the line numbers. 
	 * If in debug modus (flag 'debugging') add 'STEP_OVER_LINENUMBER' for the added code. 
	 * 
	 * @param src				The source method.
	 * @param dest				The destination method.
	 * @param offset			The offest to be added (the length of the added code list).
	 * @param startOfAddedCode	InstructionHandle to the beginning of the added code. 
	 */
	static void copyAndAdjustLineNumbers(MethodGen src, MethodGen dest, int offset, InstructionHandle startOfAddedCode) {
		InstructionList il_dest = dest.getInstructionList();
		LineNumberGen[] src_lng = src.getLineNumbers();
		boolean addedCodeHasLineNumber = false;

		for (int i = 0; i < src_lng.length; i++) {
			int position = src_lng[i].getInstruction().getPosition();
			if (position == startOfAddedCode.getPosition()) { // add the line number for added code here:
				dest.addLineNumber(startOfAddedCode, STEP_OVER_LINENUMBER);
				addedCodeHasLineNumber = true;
				continue;
			}
			if (position >= startOfAddedCode.getPosition())  // move line numbers, because of inserted code (add offset)
				position += offset;
			InstructionHandle ih = il_dest.findHandle(position);
			if (ih == null) {
				System.err.println("Handle not found!");
			} else {
				dest.addLineNumber(ih, src_lng[i].getSourceLine());
			}
		}
		if (!addedCodeHasLineNumber) { // no custom code after added code: add line number now:
			dest.addLineNumber(startOfAddedCode, STEP_OVER_LINENUMBER);
		}
	}
	
	/**
	* Test if a method has the given 'callin_flag'. 
	*
	* @param m				the method to search
	* @param cg				the ClassGen
	* @param callin_flags 	the flags to check for; 0 means any callin flags
	* @return true, if the method has the callin flag
	*/
	public  static boolean methodHasCallinFlags(Method m, ClassGen cg, int callin_flags) {
		boolean found = false;
		Attribute[] attributes = m.getAttributes();
		for (int i = 0; i < attributes.length; i++) {
			Attribute actAttr = attributes[i];
			if (!(actAttr instanceof Unknown)) 
				continue; 
			Unknown attr = (Unknown)actAttr;
			if (!(attr.getName().equals("CallinFlags")))
				continue;
			if (callin_flags == 0)
				return true;
			byte [] bytes = attr.getBytes();
			int flags = combineTwoBytes(bytes, 0);
			if ((flags & callin_flags) != 0) {
                if(logging) printLogMessage("Found CallinFlag " + callin_flags + " for "
                        + cg.getClassName() + "." + m.getName() + ".");
				found = true;
			}
		}
		return found;
	}

	/**
	 * A team needs to get added the team specific part, if it is a non-abstract team and
	 * not the class org.objectteams.Team itself.
	 * 
	 * @param cg 	the class to be tested
	 * @return true if this is a team which has to be extended
	 */
	protected static boolean classNeedsTeamExtensions(ClassGen cg) {
		return     ((cg.getAccessFlags() & OTConstants.TEAM) != 0)			// must be a team
				&& ((cg.getAccessFlags() & Constants.ACC_ABSTRACT) == 0)	// and non-abstract
				&& !(cg.getClassName().equals(OTConstants.teamClassName));	// and not o.o.Team itself.
	}

	/**
	 * Generates the name of the implementing role for the given role interface name.
	 * (Inserts the '__OT__' marker.)
	 *
	 * @param roleName the role interface name
	 * @return the implementing role class name
	 */
	protected static String genImplementingRoleName(String roleName) {
		int lastDollar = roleName.lastIndexOf('$');
		StringBuilder sb = new StringBuilder(roleName);
		sb.insert(lastDollar + 1, OTDT_PREFIX);
		return sb.toString();
	}

	/**
	 * Generates the name of the role interface the given role class is implementing. 
	 *(Removes the '__OT__' marker.) 
	 *
	 * @param roleName the role class name
	 * @return the implemented role interface
	 */
	public static String genRoleInterfaceName(String roleName) {
	      int lastDollar = roleName.lastIndexOf('$');
	      StringBuilder sb = new StringBuilder(roleName);
	      sb.delete(lastDollar + 1, lastDollar + 1 + OTDT_PREFIX.length());
	      return sb.toString();
	}
		
	protected static boolean isReflectiveOTMethod(String methodName, String methodSignature) {
		if ((methodName.equals("hasRole") && methodSignature.equals("(Ljava/lang/Object;)Z"))
				|| (methodName.equals("hasRole") && methodSignature.equals("(Ljava/lang/Object;Ljava/lang/Class;)Z"))
				|| (methodName.equals("getRole") && methodSignature.equals("(Ljava/lang/Object;)Ljava/lang/Object;"))
				|| (methodName.equals("getRole") && methodSignature.equals("(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;"))
				|| (methodName.equals("getAllRoles") && methodSignature.equals("()[Ljava/lang/Object;"))
				|| (methodName.equals("getAllRoles") && methodSignature.equals("(Ljava/lang/Class;)[Ljava/lang/Object;"))
				|| (methodName.equals("unregisterRole") && methodSignature.equals("(Ljava/lang/Object;)V"))
				|| (methodName.equals("unregisterRole") && methodSignature.equals("(Ljava/lang/Object;Ljava/lang/Class;)V"))
		   )
			return true;
		return false;
	}
	
	/**
	 * Calculates the name of the outer class.
	 *
	 * @param className	the full name of the inner class
	 * @return the name of the outer class
	 */
	public static String getOuterClassName(String className) {
		int    dollarIndex       = className.lastIndexOf('$');
		if (dollarIndex == -1) // no outer class exists!
			return null;
		String outerClassName    = className.substring(0, dollarIndex);
		return outerClassName;
	}
	
	/**
	 * @param m
	 * @param cg 
	 * @return
	 */
	static boolean candidateForImplicitActivation(Method m, ClassGen cg, ConstantPoolGen cpg) {
		if (!IS_COMPILER_14X_PLUS)
			implicitActivationMode = ImplicitActivationMode.ALWAYS;
		switch (implicitActivationMode) {
		case NEVER:	
			return false;
		case ANNOTATED :
			if (   AnnotationHelper.containsImplicitActivationAttribute(m.getAttributes(), cpg)
				|| AnnotationHelper.containsImplicitActivationAttribute(cg.getAttributes(), cpg))
				return canImplicitlyActivate(m);
			return false;
		case ALWAYS:
			if (!canImplicitlyActivate(m))
				return false;
			
			Attribute[] attributes = m.getAttributes();
			for(Attribute a : attributes) {
				if (a instanceof Unknown) {
					Unknown attr = (Unknown) a;
					String attrName = attr.getName();
					if ("RoleClassMethodModifiers".equals(attrName)) {
						byte[] bytes = attr.getBytes();
						int flags = combineTwoBytes(bytes, 0);
						if (flags == 0 ||/* flags == 2 || */flags == 4) {
							// 0: default, 2: private, 4: protected
							return false;
						}
					}
				}
			}
			if (!m.isPublic()) 
				return false; // m originally wasn't public
			
			String className = cg.getClassName();
			return !(CallinBindingManager.isRole(className) && cg.isProtected());
		default:
			return false; // cannot happen switch is exhaustive.
		}
    }

	private static boolean canImplicitlyActivate(Method m) {
		String methodName = m.getName();
		String methodSignature = m.getSignature();
		boolean isCandidate =
			(!m.isAbstract()) &&
			(!m.isStatic()) &&
			(!methodName.startsWith(OT_PREFIX)) &&
			(!methodName.equals(Constants.CONSTRUCTOR_NAME)) &&
			(!(methodName.equals("activate") && methodSignature.equals("()V"))) &&
			(!(methodName.equals("deactivate") && methodSignature.equals("()V"))) &&
			(!isReflectiveOTMethod(m.getName(), methodSignature));
		return isCandidate;
	}

	/**
	 * @param s
	 * @param c
	 * @return
	 */
	static int countOccurrences(String s, char c) {
		int count = 0;
        int idx = s.indexOf(c);
        while (idx != -1) {
            idx = s.indexOf(c, idx + 1);
            count++;
        }
        return count;
    }

	/**
	 * This method performs the changes for implicit Team activation.
	 * @param m					a method for which implicit activation will be enabled.
	 * @param className			the class name for 'm'.
	 * @param cpg				the constant pool of the class 'className'.
	 * @param activateOuter 	true, if the surrounding team has to be activated
	 * @return 
	 */
	Method genImplicitActivation(Method m, String className, ConstantPoolGen cpg, boolean activateOuter) {
		String targetName = className;
		int nestingDepth = 0;
		ObjectType outerClass = null;
		if (activateOuter) {
			outerClass = new ObjectType(getOuterClassName(className));
			nestingDepth = countOccurrences(className, '$') - 1;
			targetName = outerClass.getClassName();
		}
		MethodGen mg = newMethodGen(m, className, cpg);
		InstructionList il = mg.getInstructionList();
		InstructionList prefix = new InstructionList();
		InstructionHandle try_start = il.getStart();
		// ---> new implicit activation
		prefix.append(InstructionFactory.createThis());
		if (activateOuter) {// this is for a role method: activate the outer team
			// FIXME(SH): check replacing this$n with _OT$getTeam()
			prefix.append(factory.createGetField(className, "this$" + nestingDepth, outerClass));
		}
         prefix.append(factory.createInvoke(targetName, "_OT$implicitlyActivate",
				Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		// <-- new implicit activation
        if (debugging)
         	mg.addLineNumber(prefix.getStart() , STEP_OVER_LINENUMBER); 
		il.insert(prefix);

		InstructionList postfix = new InstructionList();
		// ---> new implicit deactivation
		postfix.append(InstructionFactory.createThis());
		if (activateOuter) {// this is for a role method: deactivate the outer team
			postfix.append(factory.createGetField(className, "this$"+nestingDepth, outerClass));
		}
		postfix.append(factory.createInvoke(targetName,
				"_OT$implicitlyDeactivate", Type.VOID, Type.NO_ARGS,
				Constants.INVOKEVIRTUAL));
		// <-- new implicit deactivation

		insertBeforeReturn(mg, il, postfix);

		/**
		 * **** add an exception handler which calls deactivate before throwing
		 * the exception (finaly-simulation): ***
		 */
		ObjectType throwable = new ObjectType("java.lang.Throwable");
		LocalVariableGen exception = mg.addLocalVariable(
				"_OT$thrown_exception", throwable, null, null);
		InstructionHandle try_end = il.getEnd();
		InstructionList postfix_ex = postfix.copy();
		if (debugging)
			mg.addLineNumber(postfix_ex.getStart(), STEP_OVER_LINENUMBER);
		postfix_ex.insert(InstructionFactory.createStore(throwable, exception
				.getIndex()));
		postfix_ex.append(InstructionFactory.createLoad(throwable, exception
				.getIndex()));
		postfix_ex.append(new ATHROW());
		InstructionHandle deactivation_handler = il.append(il.getEnd(),
				postfix_ex);
		mg.addExceptionHandler(try_start, try_end, deactivation_handler,
				throwable);
		/** ******************************************************************** */

		mg.setInstructionList(il);
		mg.setMaxLocals();
		mg.setMaxStack();
		
		if (!debugging)
			return mg.getMethod();
		
		/* sorting the line numbers per start pc: */
		Method newMethod = mg.getMethod();
		MethodGen newMethodGen = new MethodGen(newMethod, className, cpg);
		
		LineNumberGen[] lineNumbers = newMethodGen.getLineNumbers();
		
		newMethodGen.removeLineNumbers();
		
		Arrays.sort(lineNumbers, new Comparator<LineNumberGen>() {
		public int compare(LineNumberGen ln1, LineNumberGen ln2) {
			int firstLineNumberPC = ln1.getLineNumber().getStartPC();
			int secondLineNumberPC = ln2.getLineNumber().getStartPC();
			
			if (firstLineNumberPC < secondLineNumberPC)
				return -1;
			if (firstLineNumberPC > secondLineNumberPC)
				return 1;
			return 0;
		}
	}); 

		for (int i = 0; i<lineNumbers.length; i++) {
			newMethodGen.addLineNumber(lineNumbers[i].getInstruction(), lineNumbers[i].getSourceLine());
		}		

		return newMethodGen.getMethod();
	}
	
	/**
	 *  How many bytes does an instruction produce on the stack?
	 */
	static protected int stackDiff(Instruction instr, ConstantPoolGen cpg) {
		return instr.produceStack(cpg) - instr.consumeStack(cpg);
	}

	/**
	 *  Split an instruction list that loads method arguments into
	 *  one list for each argument.
	 *  @param cpg
	 *  @param src the original loading sequence (will be destroyed by this method).
	 *  @param argumentTypes source-level argument types of the callin method.
	 *  @return array of load sequences, same order as before but split into individual arguments.
	 */
	protected InstructionList[] splitLoading(ConstantPoolGen cpg, InstructionList src, Type[] argumentTypes) {
		int len = argumentTypes.length;
		InstructionList[] res = new InstructionList[len];
		// starting right _before_ an invoke instruction we loop backwards 
		// to find those values on the stack that would be passed to the method.
		for (int idx = len-1; idx>=0; idx--) {
			// new sub-list:
			res[idx] = new InstructionList();
			// how many bytes to expect on the stack:
			int expectedArgSize = argumentTypes[idx].getSize();
			// loop until we have a sequence that produces the expected number of bytes:
			while (expectedArgSize > 0) {
				InstructionHandle loadH = src.getEnd();
				Instruction       load  = loadH.getInstruction();
				expectedArgSize -= stackDiff(load, cpg);
				try {
					// transfer instruction from src to res[roleIdx]:
					res[idx].insert(load);
					src.delete(loadH);
				} catch (TargetLostException e) {
					throw new OTREInternalError(e);
				}
			}
		}
		return res;
	}

	/**
	 *  Translate the parameter loading of a base call to the correct sequence
	 *  expected by the base chaining wrapper.
	 *  Treats tunneled arguments but not current enhancement args.
	 *  Tasks performed:
	 *  <ul>
	 *     <li>pick load sequences from <tt>splitLoad</tt>
	 *     <li>extract unused args from Object[]
	 *     <li>insert casting or lowering if needed.
	 *  </ul>
	 *  More documentation is found in document parameter-passing.odg.
	 *  
	 * @param splitLoad one list of load instructions for each argument, ordered as found in the bytecode.
	 * @param enhancedRoleArgumentTypes enhanced role arguments
	 * @param enhancedBaseArgumentTypes arguments of the base chaining wrapper.
	 * @param parameterPosition encoded parameter mappings.
	 * @param teamName The name of the team containing the role class.
	 * @param enclosingRoleName enclosing role class or null for static replace/base-call.
	 * @param baseIsCallin is the base method a callin method?
	 * @param baseIsStaticRoleMethod is the base method a static role method?
	 * @param one byte for each parameter, signalling if lowering is required
	 * @return a complete loading sequence for all source-level and tunneled arguments
	 */
	protected InstructionList translateLoads(InstructionList[] splitLoad, 
											 Type[]  		   enhancedRoleArgumentTypes, 
											 Type[]  		   enhancedBaseArgumentTypes, 
											 int[]   		   parameterPositions,
											 String  		   teamName, 
											 String  		   enclosingRoleName,
											 BaseMethodInfo    baseMethod,
											 int    		   start,
											 ConstantPoolGen   cpg) 
	{
		boolean isStatic               = baseMethod.isStatic;
		boolean baseIsStaticRoleMethod = baseMethod.isStaticRoleMethod();
		boolean baseIsCallin 	       = baseMethod.isCallin;
		int     translationFlags	   = baseMethod.translationFlags;
		
		InstructionList il = new InstructionList();		

		// index into "Object[] _OT$unusedArgs" (points to first arg that has not yet been consumed):
		int nextUnusedArg = 0; 
		// index into enhancedBaseArgumentTypes:
		int baseIdx;
		// source-level version of baseIdx, i.e., ignoring any enhanced arguments:
		int baseSrcIdx;
		// index into roleArgumentTypes:
		int roleIdx;
		// source-level version of roleIdx, i.e., ignoring any enhanced arguments:
		int roleSrcIdx;		

		// ==  Note: letters (u-x) below refer to document parameter-passing.odg. ==
		// Skip enhancements that are already loaded (given in start).
		for (baseIdx = start; baseIdx < enhancedBaseArgumentTypes.length; baseIdx++) {
			baseSrcIdx = baseIdx-start;      // (u) skip first enhancement (already loaded)
			if (baseIsStaticRoleMethod) 
				baseSrcIdx -= 2;			 // (v) not mapped but extracted from unusedArgs
			if (baseIsCallin)
				baseSrcIdx -= EXTRA_ARGS;    // (w) not mapped but extracted from unusedArgs

			// value-dispatching: where to find this parameters? 
			if (baseSrcIdx < 0) {
				// (v) or (w)
				roleSrcIdx = -1;
			} else {
				// (x) merge arguments from unused and real:
				int numAvailableRoleArgs = enhancedRoleArgumentTypes.length-EXTRA_ARGS;
				if (!isStatic && IS_COMPILER_GREATER_123)
					numAvailableRoleArgs--; // don't count isSuperAccess
				roleSrcIdx = getMappedRolePosition(baseSrcIdx, parameterPositions, numAvailableRoleArgs);
			}
			
			// got all information, fetch it now:
			Type baseArgumentType = enhancedBaseArgumentTypes[baseIdx];
			if (roleSrcIdx == -1) {
				// not mapped, retrieve from _OT$unusedArgs:
				retrieveFromUnusedArg(il, nextUnusedArg++, baseArgumentType, cpg);
			} else {
				// mapped, fetch from splitLoad:
				roleIdx = roleSrcIdx+EXTRA_ARGS; // role always has an additional set of enhancements 
				if (IS_COMPILER_GREATER_123 && !isStatic) roleIdx++; // (+ isSuperAccess)
				Type roleArgumentType = enhancedRoleArgumentTypes[roleIdx];
				il.append(splitLoad[roleSrcIdx]);
				if (!roleArgumentType.equals(baseArgumentType)) {
					convertParamToBase(il, teamName, enclosingRoleName, roleArgumentType, baseArgumentType, 
										(translationFlags & (2<<baseSrcIdx)) != 0);
				}
			}
		}
		if (il.isEmpty())
			il.append(new NOP()); // ensure caller receives at least one instruction handle (for line number)
		return il;
	}
	
	/* Is the base arg identified by 'baseIdx' mapped to a role parameter?
	 * If so: which role position is it mapped to?
	 * If not: return -1
	 */
	private int getMappedRolePosition(int     baseSrcIdx, 
									  int[]   parameterPositions, 
									  int     numAvailableRoleArgs) 
	{
		if (parameterPositions == null) {
			if (baseSrcIdx < numAvailableRoleArgs) // as many as available ...
				return baseSrcIdx;        		   // in original order.
		} else {
			// search parameter mapping:
			for (int i = 0; i < parameterPositions.length; i++) {
				if (parameterPositions[i] == baseSrcIdx+1) // positions are one-based (base side)
					return i;
			}
		}
		return -1;
	}

	/* Retrieve an unused (tunneled) argument from position 'unusedIdx' of the _OT$unusedArgs array. */
	private void retrieveFromUnusedArg(InstructionList il, 
							   int             unusedIdx, 
							   Type            baseArgumentType,
							   ConstantPoolGen cpg) 
	{
		il.append(InstructionFactory.createLoad(objectArray, /*this*/1 + (UNUSED_ARG-1))); // 'UNUSED_ARG' is one-based
		il.append(createIntegerPush(cpg, unusedIdx));
		il.append(InstructionFactory.createArrayLoad(objectArray));
		if (baseArgumentType instanceof BasicType)
			il.append(createUnboxing((BasicType) baseArgumentType));
		else  // ObjectTypes just have to be re-casted
			il.append(factory.createCast(object, baseArgumentType));
	}

	/* May perform any of these conversions: casting, lowering, array-lowering.
	 * Conversion is generated into 'il'. 
	 */
	private void convertParamToBase(InstructionList il, 
									String          teamName, 
									String 			enclosingRoleName, 
									Type 			roleArgumentType, 
									Type 			baseArgumentType,
									boolean         loweringFlag) 
	{
		if (roleArgumentType instanceof ObjectType
		    && baseArgumentType instanceof ObjectType) 
		{
			if (loweringFlag) {
				lowerObject(il, teamName, roleArgumentType, baseArgumentType);
			} else {
				if(logging) printLogMessage("Try to cast " + roleArgumentType +
						" to " + baseArgumentType);
				il.append(factory.createCast(roleArgumentType,
						baseArgumentType));
			}
		} else 
		if (   roleArgumentType instanceof BasicType
			&& baseArgumentType instanceof ObjectType) 
		{
			il.append(createBoxing((BasicType)roleArgumentType));
		} else 
		if (   roleArgumentType instanceof ObjectType
			&& baseArgumentType instanceof BasicType) 
		{
			il.append(createUnboxing((BasicType)baseArgumentType));
		} else
		if (   roleArgumentType instanceof ArrayType
		    && baseArgumentType instanceof ArrayType)
		{
			lowerArray(il, teamName, enclosingRoleName, roleArgumentType, baseArgumentType);
		} else {
			throw new OTREInternalError("OTRE internal error:"+
							   "No way to make types conform\n"+
							   "role type "+roleArgumentType+
							   " -> "+baseArgumentType);
		}
	}

	/* Lower one object from roleArgumentType to baseArgumentType. */
	private void lowerObject(InstructionList il, 
							 String 		 teamName, 
							 Type 			 roleArgumentType, 
							 Type 			 baseArgumentType) 
	{
		String roleIfcName = ((ObjectType)roleArgumentType).getClassName();
		// use interface implementing role type for base-field access! 
		String roleClassName = ObjectTeamsTransformation.genImplementingRoleName(roleIfcName);
		
		int dollarIdx = roleClassName.lastIndexOf('$');
		if (dollarIdx == -1) {
			throw new OTREInternalError("OTRE internal error:" +
										"No way to make types conform\n" +
										"role type " + roleArgumentType +
										" -> " + baseArgumentType);
		}
		boolean isNested = dollarIdx != roleClassName.indexOf('$'); // is last == first?
		String strengthenedRoleName = teamName + roleClassName.substring(dollarIdx);
		
		// baseArgumentType may be imprecise due to cast in param mapping, 
		// try a RBB instead to find the exact base type
		RoleBaseBinding rbb = CallinBindingManager.getRoleBaseBinding(strengthenedRoleName);
		if (rbb != null) 
			baseArgumentType = new ObjectType(rbb.getBaseClassName());

		if (isNested) {
			// cannot use field access here, because role strengthening would require resolved information
			// so use the _OT$getBase() method instead:
			short kind = (roleIfcName.lastIndexOf('$') == roleIfcName.lastIndexOf("$__OT__")) // last segment is roleclass?
							? Constants.INVOKEVIRTUAL
							: Constants.INVOKEINTERFACE;
			il.append(factory.createInvoke(roleIfcName, GET_BASE, baseArgumentType, new Type[0], kind));
		} else {
			// access field via the role class:
			il.append(factory.createCast(roleArgumentType, new ObjectType(strengthenedRoleName)));
			
			// optimized version directly accessing the field:
			il.append(factory.createGetField(strengthenedRoleName,
											 BASE,
											 baseArgumentType));
		}
	}

	/* Lower from roleArgumentType[] to baseArgumentType[]. */
	private void lowerArray(InstructionList il, String teamName, String enclosingRoleName, Type roleArgumentType, Type baseArgumentType) {
		ArrayType array = (ArrayType)roleArgumentType;
		String roleName =((ObjectType)array.getElementType()).getClassName();
		int dollarIdx = roleName.lastIndexOf('$');
		String pureRoleName = roleName.substring(dollarIdx + 1);
		String transformMethodName = getArrayLoweringMethodName(pureRoleName, array.getDimensions());
		if (enclosingRoleName != null) {
			// fetch team from enclosing role:
			il.append(InstructionFactory.createThis());
			// FIXME(SH): is this$0 always correct (nesting!)??
			il.append(factory.createGetField(enclosingRoleName, "this$0", new ObjectType(teamName)));
		} else {
			// "this" is the team:
			il.append(InstructionFactory.createThis());
		}
		il.append(new SWAP()); // 'push' team instance below the role array
		il.append(factory.createInvoke(teamName, 
									   transformMethodName, 
									   baseArgumentType, 
									   new Type[]{roleArgumentType}, 
									   Constants.INVOKEVIRTUAL));
	}
	
	private String getArrayLoweringMethodName(String roleName, int dimensions) {
		return "_OT$transformArray" + roleName + "_OT$" + dimensions;
	}

	public List<String> getInnerClassNames(ClassGen cg, ConstantPoolGen cpg) {
		Attribute[] attributes = cg.getAttributes();		
		LinkedList<String> innerClassNames = new LinkedList<String>();
		for (int i = 0; i < attributes.length; i++) {
			Attribute actAttr = attributes[i];
			if (actAttr instanceof InnerClasses) {
				InnerClass[] inners = ((InnerClasses)actAttr).getInnerClasses();
				for (int j=0; j<inners.length; j++) {
					int name_index = inners[j].getInnerNameIndex();
					Constant name_c = cpg.getConstant(name_index);
					String name = ((ConstantUtf8)name_c).getBytes();
					innerClassNames.add(name);
				}
			}
		}
		return innerClassNames;
	}

	/**
	 * Create a monitor enter using a class literal
	 * @param mg 		 target method to which a local variable is added which holds the monitor object
	 * @param il		 instruction list to append to
	 * @param class_name name of the class literal to use as monitor
	 * @param major		 major class file version, used to determine how to translate class literals
	 * @param cpg		 constant pool gen
	 * @return the slot index of the monitor local variable and the handle of the first generated instruction.
	 */
	protected Pair<Integer,InstructionHandle> addClassMonitorEnter(MethodGen 		mg, 
																   InstructionList 	il,
																   String			class_name,
																   int 				major, 
																   ConstantPoolGen 	cpg)
	{
		return addClassMonitorEnter(mg, il, class_name, class_name, major, cpg);
	}
	protected Pair<Integer,InstructionHandle> addClassMonitorEnter(MethodGen 		mg, 
			   InstructionList 	il,
			   String			class_name_this,
			   String			class_name_target,
			   int 				major, 
			   ConstantPoolGen 	cpg)
	{
		int monitor;
		InstructionHandle ih= 
			appendClassLiteral(il, class_name_this, class_name_target, major, cpg);
		il.append(new DUP()); // for store and monitorenter
		LocalVariableGen lg2= 
			mg.addLocalVariable("monitor", Type.OBJECT, il.getStart(), null); //$NON-NLS-1$
		monitor= lg2.getIndex();
		il.append(InstructionFactory.createStore(Type.OBJECT, monitor)); // for use by monitorexit
		il.append(new MONITORENTER());
		return new Pair<Integer, InstructionHandle>(monitor, ih);
	}
	
	/** 
	 * Append an instruction sequence for loading the class literal for `class_name'.
	 * @param il	instruction list to append to
	 * @param class_name class name of the class literal
	 * @param major java version as stored in the byte code.
	 * @param cpg   for generating bytes
	 * @return the handle of the first instruction of the sequence.
	 */
	protected InstructionHandle appendClassLiteral(InstructionList il, 
												   String          class_name_this,
												   String          class_name_target,
												   int             major,
												   ConstantPoolGen cpg) 
	{
		if (major >= 49) // java 5
			return il.append(new LDC(cpg.addClass(new ObjectType(class_name_target))));
		// pre java 5, do it the hard way:
		// if (_OT$self_class$ != null)
		// if (ThisClass._OT$class$that$Class != null)
	    String receiverClass = class_name_this;
	    String fieldName = class_name_this.equals(class_name_this) ? OTConstants.SELF_CLASS : OTConstants.CLASS+class_name_target;
	  InstructionHandle start= 
		il.append(factory.createFieldAccess(receiverClass, fieldName, classType, Constants.GETSTATIC));
	    il.append(new DUP()); // keep a copy as a potential result
	  BranchInstruction checkLoaded=
		          InstructionFactory.createBranchInstruction(Constants.IFNONNULL, null);
	    il.append(checkLoaded);
	    il.append(new POP()); // discard null from above
	    // _OT$self_class$= Class.forName(<class_name>); // never fails, it is THIS class
		il.append(new LDC(cpg.addString(class_name_target)));
	  	il.append(factory.createInvoke("java.lang.Class", 
	  								   "forName", 
	  								   classType,
	  								   new Type[]{new ObjectType("java.lang.String")},
	  								   Constants.INVOKESTATIC));
	  	il.append(new DUP()); // keep a copy as the result
	  	il.append(factory.createFieldAccess(receiverClass, fieldName, classType, Constants.PUTSTATIC));
	
	  	checkLoaded.setTarget(il.append(new NOP()));
	  	return start;
	}

	@SuppressWarnings("unchecked")
	public static void insertBeforeReturn(MethodGen mg, InstructionList il, InstructionList insertion) {
		// InstructionFinder is broken see https://issues.apache.org/bugzilla/show_bug.cgi?id=40044
		// which is fixed in r516724 (2007-03-10) but latest release bcel 5.2 is 6. June 2006.
//		Iterator<InstructionHandle[]> ihIt = new InstructionFinder(il).search("ReturnInstruction");
//		while (ihIt.hasNext()) {
//			InstructionHandle[] ihAr = ihIt.next();
//			InstructionList insertionCopy = insertion.copy();
//			if (debugging)
//				mg.addLineNumber(insertionCopy.getStart(), STEP_OVER_LINENUMBER);
//			InstructionHandle inserted = il.insert(ihAr[0], insertionCopy); // instruction lists can not be reused
//			il.redirectBranches(ihAr[0], inserted);// SH: retarget all jumps that targeted at the return instruction
//		}
		Iterator<InstructionHandle>ihIt = il.iterator();
		while (ihIt.hasNext()) {
			InstructionHandle ihAr = ihIt.next();
			if (!(ihAr.getInstruction() instanceof ReturnInstruction))
				continue;
			InstructionList insertionCopy = insertion.copy();
			if (debugging)
				mg.addLineNumber(insertionCopy.getStart(), STEP_OVER_LINENUMBER);
			InstructionHandle inserted = il.insert(ihAr, insertionCopy); // instruction lists can not be reused
			il.redirectBranches(ihAr, inserted);// SH: retarget all jumps that targeted at the return instruction
		}
	}
}
