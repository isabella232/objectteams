/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseMethodTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import static org.eclipse.objectteams.otre.StaticSliceBaseTransformation._OT_ACTIVE_TEAMS;
import static org.eclipse.objectteams.otre.StaticSliceBaseTransformation._OT_ACTIVE_TEAM_IDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.eclipse.objectteams.otre.util.CallinBindingManager;
import org.eclipse.objectteams.otre.util.DebugUtil;
import org.eclipse.objectteams.otre.util.ListValueHashMap;
import org.eclipse.objectteams.otre.util.MethodBinding;
import org.eclipse.objectteams.otre.util.TeamIdDispenser;

import de.fub.bytecode.Constants;
import de.fub.bytecode.classfile.Field;
import de.fub.bytecode.classfile.LineNumber;
import de.fub.bytecode.classfile.LineNumberTable;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.generic.AASTORE;
import de.fub.bytecode.generic.ACONST_NULL;
import de.fub.bytecode.generic.ALOAD;
import de.fub.bytecode.generic.ANEWARRAY;
import de.fub.bytecode.generic.ARRAYLENGTH;
import de.fub.bytecode.generic.ATHROW;
import de.fub.bytecode.generic.ArrayType;
import de.fub.bytecode.generic.BasicType;
import de.fub.bytecode.generic.BranchInstruction;
import de.fub.bytecode.generic.ClassGen;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.DUP;
import de.fub.bytecode.generic.DUP_X1;
import de.fub.bytecode.generic.FieldGen;
import de.fub.bytecode.generic.GOTO;
import de.fub.bytecode.generic.IADD;
import de.fub.bytecode.generic.ICONST;

import de.fub.bytecode.generic.IFNE;
import de.fub.bytecode.generic.IFNONNULL;
import de.fub.bytecode.generic.IF_ICMPLT;
import de.fub.bytecode.generic.IINC;
import de.fub.bytecode.generic.INVOKESPECIAL;
import de.fub.bytecode.generic.Instruction;
import de.fub.bytecode.generic.InstructionConstants;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.InvokeInstruction;
import de.fub.bytecode.generic.LDC;
import de.fub.bytecode.generic.LocalVariableGen;
import de.fub.bytecode.generic.MONITOREXIT;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.NOP;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.POP;
import de.fub.bytecode.generic.PUSH;
import de.fub.bytecode.generic.TABLESWITCH;
import de.fub.bytecode.generic.Type;


/**
 * Insert dispatch code into base methods affected by callin bindings. <p>
 * If a loaded class contains binding declarations (transmitted by attributes)
 * these are stored for further determination of necessity for base-method-transforming.
 * Classes for which a callin binding exists will be transformed:
 * (base-class)methods <i>m()</i> which are changed by a callin will be copied to
 * <tt>_OT$<i>m</i>$orig()</tt>.
 * Hereafter the original method <i>m()</i> will be transformend depending on the
 * callin-modifier:
 * <ul>
 *  <li> replace: only the (role-)callin-method is called
 *  <li> after: first the original-method <tt>_OT$<i>m</i>$orig()</tt> is called and
 *       then the callin-method
 *  <li> before: first the the callin-method is called and then original-method
 *       <tt>_OT$<i>m</i>$orig()</tt>
 * </ul>
 *
 * @version $Id: BaseMethodTransformation.java 23408 2010-02-03 18:07:35Z stephan $
 * @author Christine Hundt
 * @author Stephan Herrmann
 */

public class BaseMethodTransformation
	extends ObjectTeamsTransformation
{
	// configurability for stepping behavior of the chaining wrapper:
	private static boolean SHOW_ORIG_CALL 		= true;
	private static boolean SHOW_RECURSIVE_CALL	= true;
	private static boolean SHOW_ROLE_CALL		= true;
	static {
		String callinStepping = System.getProperty("ot.debug.callin.stepping");
		if (callinStepping != null) {
			SHOW_ORIG_CALL = SHOW_RECURSIVE_CALL = SHOW_ROLE_CALL = false;
			StringTokenizer tokens = new StringTokenizer(callinStepping, ",");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if ("orig".equals(token))
					SHOW_ORIG_CALL = true;
				else if ("recurse".equals(token))
					SHOW_RECURSIVE_CALL = true;
				else if ("role".equals(token))
					SHOW_ROLE_CALL = true;
			}
		}
	}

	// method of o.o.Team:
    private static final String IS_ACTIVE = "isActive";  //$NON-NLS-1$

	private final static int NORESULT = -1;

	// FIXME(SH): once we remove JMangler support, these maps can be reduced to their RHS because then we'll consistently have one transformer per class:
    private HashMap /* class_name -> HashSet(method_name) */<String, HashSet<String>> transformableMethods = new HashMap<String, HashSet<String>>();
    private HashMap /* class_name -> HashSet(method_name) */<String, HashSet<String>> overridableMethods = new HashMap<String, HashSet<String>>();

	public boolean useReflection = false;
	
	public BaseMethodTransformation(SharedState state) {
		this(null, state);
	}

	public BaseMethodTransformation(ClassLoader loader, SharedState state) {
		super(loader, state);
	}
	/**
	 *  The code transformer only replaces the original code with
	 *  the initial wrapper.
	 */
    public void doTransformCode(ClassGen cg) {
    	factory = new InstructionFactory(cg);
    	ConstantPoolGen cpg        = cg.getConstantPool();
    	String          class_name = cg.getClassName();
			
    	Method[] methods = cg.getMethods();
    	for (int i=0; i<methods.length; i++) {
    		Method m           = methods[i];
    		//if (m.isNative())
    		//	continue;
    		if (m.isVolatile())
    			continue; // don't touch bridge methods
    		
    		String method_name = m.getName();
				
    		if (CallinBindingManager.isBoundBaseClass(class_name) 
    				&& !CallinBindingManager.hasBoundBaseParent(class_name) 
    				&& method_name.equals(Constants.CONSTRUCTOR_NAME)) 
    		{
    			addToConstructor(m, getInitializedRoleSet(cg.getClassName(), false), cg, cpg);
    			continue;
    		}
			                
    		String method_signature = m.getSignature();

    		if (state.interfaceTransformedClasses.contains(class_name)) {
    			HashSet<String> transformable = transformableMethods.get(class_name);
    			HashSet<String> overridable = overridableMethods.get(class_name);
    			if (transformable.contains(method_name + '.' + method_signature))
    				cg.replaceMethod(m, m = generateInitialWrapper(m, class_name, cg.getMajor(), cpg));
    			else if (overridable.contains(method_name + '.' + method_signature))
    				cg.replaceMethod(m, m = generateSuperCall(m, cg, cpg));
    			
    			Method replacement = checkReplaceWickedSuper(class_name, m, cpg);
    			if (replacement != null)
    				cg.replaceMethod(m, replacement);
    		}
    	}
    }

    /*
     * If a base method m1 has a super-call super.m2() and if that method m2 is callin-bound
     * we currently bypass aspect dispatch to avoid infinite recursions.
     */
	private Method checkReplaceWickedSuper(String className, Method m, ConstantPoolGen cpg)
	{
        if (m.isAbstract() || m.isNative())
            return null;
		MethodGen mg = new MethodGen(m, className, cpg);
		String method_name = m.getName();
		InstructionHandle[] ihs = mg.getInstructionList().getInstructionHandles();
		boolean found = false;
		for (InstructionHandle ih : ihs) {
			if (ih.getInstruction() instanceof INVOKESPECIAL) { 			
				Instruction actInstruction = ih.getInstruction();
				INVOKESPECIAL is = (INVOKESPECIAL)actInstruction;
				String is_name = is.getName(cpg);
				if (   !is_name.equals(method_name) 							// not same method
					&& !is_name.equals("<init>"))    							// not ctor call
				{ 
					String superClassName = is.getClassName(cpg);
					if (   !superClassName.equals(className)					// not private method of same class
						&& CallinBindingManager.isBoundBaseMethod( 				// target method is callin-affected
													superClassName,	
													is_name, 
													is.getSignature(cpg)))
					{					
						found = true;
	                    if(logging) printLogMessage("wicked super-call to " + is_name //$NON-NLS-1$
	                            + " has to be redirected to the orig-version!"); //$NON-NLS-1$
						ih.setInstruction(factory.createInvoke(superClassName, 
															   "_OT$"+is_name+"$orig",
															   is.getReturnType(cpg),
															   is.getArgumentTypes(cpg),
															   Constants.INVOKESPECIAL)); 
					}
				}
			}
		}
		if (found) 
			return mg.getMethod();
		return null;
 	}

	/**
	 * Main entry for this transformer.
	 */
    public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
    	String class_name = cg.getClassName();
    	//SourceMapGeneration sourceMapGen = new SourceMapGeneration(cg);

    	ConstantPoolGen cpg = cg.getConstantPool();
    	factory = new InstructionFactory(cg);
    	if (CallinBindingManager.isBoundBaseClass(class_name) && !cg.isInterface())  {
    		// TODO: where to add the role set infrastructure, if only an interface is bound? Implementing classes?
    		if (cg.containsField(OTConstants.ROLE_SET) == null && !CallinBindingManager.hasBoundBaseParent(class_name)) {
    			ce.addField(generateRoleSet(cpg, class_name), cg);
    			ce.addMethod(generateAddRole(cpg, class_name), cg);
    			ce.addMethod(generateRemoveRole(cpg, class_name), cg);
    			ce.addImplements(OTConstants.IBOUND_BASE, cg);
    		}
    	}

    	HashSet<String> transformedMethods = transformableMethods.get(class_name);
    	if (transformedMethods == null) {
    		transformedMethods = new HashSet<String>();
    		transformableMethods.put(class_name, transformedMethods);
    	}
    	HashSet<String> renamedMethods = overridableMethods.get(class_name);
    	if (renamedMethods == null) {
    		renamedMethods = new HashSet<String>();
    		overridableMethods.put(class_name, renamedMethods);
    	}

    	checkReadClassAttributes(ce, cg, class_name, cpg);
		
    	//JU: change the class modifier to 'public' if decapsulation required
    	if(CallinBindingManager.checkBaseClassModifierChange(class_name) && !cg.isPublic()) {
    		cg.setAccessFlags(makePublicFlags(cg.getAccessFlags()));
    	}
    	
    	Collection<MethodBinding> inheritedBindings = CallinBindingManager.getInheritedCallinBindings(class_name);
    	
//		if (inheritedSigns!=null && !inheritedSigns.isEmpty()) {
//			Iterator itx = inheritedSigns.iterator();
//			while (itx.hasNext()) {
//				System.err.print(class_name+" : ");
//				String[] next = (String[])itx.next();
//				System.err.println(next[0] +" " +next[1]) ;
//			}
//		}
    	/*
    	 String[] interfaceNames = cg.getInterfaceNames();
    	 Collection interfaceInheritedSigns  = new LinkedList();
    	 //System.err.println("searching inherited bindings for: "+class_name);
    	  for (int i=0; i<interfaceNames.length;i++) {
    	  		if (!interfaceNames[i].equals(class_name))
    	  			interfaceInheritedSigns.addAll(CallinBindingManager.getInterfaceInheritedCallinBindings(interfaceNames[i]));
          }*/
		/*
		 if (!interfaceInheritedSigns.isEmpty()) {
		 	System.err.println("BMT: searching inherited bindings for: "+class_name);
		 		for (Iterator iterator = interfaceInheritedSigns.iterator(); iterator.hasNext();) {
		 			String[] element = (String[]) iterator.next();
		 			System.err.println(element[0]+element[1]);
		        }
		 }*/
		//inheritedSigns.addAll(interfaceInheritedSigns);
			
    	boolean haveDirectCallin =
    		CallinBindingManager.isBoundBaseClass(class_name);
		// IMPLICIT_INHERITANCE
    	if (inheritedBindings.size() == 0 && !haveDirectCallin /*&& interfaceInheritedSigns.size()==0*/) {
            if(logging) printLogMessage("\nCallins: nothing to do for class " + class_name); //$NON-NLS-1$
    		return; // nothing to do
    	}
    	// if class is already transformed by this transformer
    	/*
    	 if (interfaceTransformedClasses.contains(class_name))
    	 	continue;
		*/
			
    	if(cg.isInterface()) {
    		//CallinBindingManager.addBoundBaseInterface(class_name); // <- this is to late, implementing class may be loaded before!!
    		return; // No transfomations neccessary for interfaces.
    	}
			
        if(logging) printLogMessage("\nCallin bindings may be changing class " //$NON-NLS-1$
    				+ class_name + ':');

        // A field to optimize class-literal in initial wrapper:
		if (cg.getMajor() < 49) {// pre 1.5?
			if (cg.containsField(OTConstants.SELF_CLASS) == null)
				ce.addField(new FieldGen(Constants.ACC_PROTECTED|Constants.ACC_STATIC, 
	                           			 classType, 
	                           			 OTConstants.SELF_CLASS,
	                           			 cpg)
								.getField(),
	                        cg);
		}
    	
    	Method[] methods = cg.getMethods();
    	for (int i=0; i<methods.length; i++) {
    		Method m           = methods[i];
    		if (m.isVolatile()) // bridge method!
    			continue;
    		String method_name = m.getName();
    		String method_signature = m.getSignature();
    		
    		Collection<MethodBinding> bindingsForMethod = null;
    		// IMPLICIT_INHERITANCE
    		if (haveDirectCallin)
    			bindingsForMethod = CallinBindingManager .
    			getBindingForBaseMethod(class_name, method_name, m.getSignature());
    		
    		//JU: added the following statement to determine overridden static base methods
    		//Collection inheritedCallinBindings = CallinBindingManager.getInheritedCallinBindings(class_name);
    		//CH: removed it again, because it is the same as 'inheritedSigns'!
    		
    		MethodGen mg = null;
    		int firstLine = STEP_OVER_LINENUMBER;
    		String original_signature = method_signature;
    		/*if (bindingsForMethod != null || containsSign(inheritedSigns, m)*/ /*|| containsSign(interfaceInheritedSigns, m)*/ //) {
    		MethodBinding match= matchingBinding(inheritedBindings, m, false);
    		if (bindingsForMethod != null || (match!= null && !m.isStatic() && !m.isPrivate())) {
    			
    			mg = new MethodGen(m, class_name, cpg);
    			Method orig_method;
					
    			String name_orig = genOrigMethName(method_name);
    			if (cg.containsMethod(name_orig, m.getSignature())!=null) {
    				continue;// method was already copied to orig-version!
    			}
				
    			if (debugging)
    				firstLine = findFirstLineNumber(m);
    			
    			mg.setName(name_orig);
					
				// TODO(SH): store this match, or keep previous?
    			if (matchingBinding(inheritedBindings, m, true) != null) // this method was adapted in a super class 
    				replaceSuperCalls(mg, method_name, cpg);
					
    			orig_method = mg.getMethod();
    			ce.addMethod(orig_method, cg);
                if(logging) printLogMessage("Method " + method_name + " was backuped as " //$NON-NLS-1$ //$NON-NLS-2$
    						+ name_orig + '.');

                if (match == null || method_signature.equals(match.getBaseMethodSignature()))
                	renamedMethods.add(method_name+'.'+method_signature);
                else // override with covariant return: at the VM-level this is a *new* method.
                	transformedMethods.add(method_name+'.'+method_signature);
    		}

    		/*if (bindingsForMethod != null || (containsSign(inheritedSigns,m) && m.isStatic())*/ /*|| containsSign(interfaceInheritedSigns, m)*/ //) {
    		//CH: changed 'inheritedCallinBindings' to 'inheritedSigns', because it was the same.
    		if (bindingsForMethod != null) { 
    			//add method '_OT$<method_name>$chain' :
    			Method chain;
    			mg = getConcretMethodGen(m, class_name, cpg);
    			chain = generateChainingWrapper(mg, method_name,
    									original_signature/*method_signature*/, class_name, cpg, cg, firstLine);
													
    			if (cg.containsMethod(chain.getName(), chain.getSignature()) == null)
    				ce.addMethod(chain, cg);

    			transformedMethods.add(method_name + '.' + method_signature);
    		}
    		if (mg == null)
                if (logging) printLogMessage("No method binding (direct or inherited) found for " //$NON-NLS-1$
                            + method_name);
    	}
    	state.interfaceTransformedClasses.add(class_name);
    }
    
    private int findFirstLineNumber(Method m) {
		LineNumberTable lnt = m.getLineNumberTable();
		if (lnt != null && lnt.getTableLength() > 0) {
			LineNumber[] lineNumberTable = lnt.getLineNumberTable();
			for (int i=0; i<lineNumberTable.length; i++) {
				int lineNumber = lineNumberTable[i].getLineNumber();
				if (lineNumber != OTConstants.STEP_OVER_LINENUMBER)
					return lineNumber;
			}
			return lineNumberTable[0].getLineNumber();
		}
		return STEP_OVER_LINENUMBER; // make it a valid line number
	}

	/**
	 * "super"-calls in callin bound base methods have to be redirected to the _OT$...$orig version, if the
	 * super-method is bound (and thus renamed to _OT$..$orig) too.
	 *
	 * @param orig_method	the copied method
	 * @param method_name	the prior name of the copied method
	 * @param cpg			the corresponding constang pool	
	 */
	private void replaceSuperCalls(MethodGen orig_method, String method_name, ConstantPoolGen cpg) {
		// search for super calls:
		InstructionList il = orig_method.getInstructionList();
		InstructionHandle[] ihs = il.getInstructionHandles();
		int actInstrIndex = 0;
		while (actInstrIndex < ihs.length) {
			if (ihs[actInstrIndex].getInstruction() instanceof INVOKESPECIAL) { 			
				Instruction actInstruction = ihs[actInstrIndex].getInstruction();
				INVOKESPECIAL is = (INVOKESPECIAL)actInstruction;
				String is_name = is.getName(cpg);
				if(is_name.equals(method_name)) {
					String superClassName = is.getClassName(cpg);
                    if(logging) printLogMessage("super-call to " + is_name //$NON-NLS-1$
                            + " has to be redirected to the orig-version!"); //$NON-NLS-1$
					// generate and set an instruction calling the orig-version of the super method:
					InvokeInstruction superOrigCall = factory.createInvoke(superClassName, 
																		   orig_method.getName(),
																		   orig_method.getReturnType(),
																		   orig_method.getArgumentTypes(),
																		   Constants.INVOKESPECIAL);
					ihs[actInstrIndex].setInstruction(superOrigCall); 
				}
			}
			actInstrIndex++;
		}
		// redirect them ( change called method name to  _OT$..$orig ) if super-method has been renamed:
		// --> 
	}

	/**
	 *  Is method `m' contained in `baseMethodBindings'?
	 *  @param nameSigns list of MethodBinding
	 *  @param m
     *  @param strict if true covariance must not be considered
     *  @return the matching binding from baseMethodBindings or null.
	 */
	static MethodBinding matchingBinding (Collection<MethodBinding> baseMethodBindings, Method m, boolean strict) 
	{
		for (MethodBinding binding: baseMethodBindings) 
			if (binding.matchesMethod(m.getName(), m.getSignature(), strict)) 
				return binding;

		return null;
	}

	/**
	 *  Get a MethodGen for `m'.
	 *  If `m' is abstract setup a new concrete method.
	 */
	static MethodGen getConcretMethodGen (Method m, String class_name, ConstantPoolGen cpg) {
		MethodGen mg;
		String signature   = m.getSignature();
		Type[] argTypes    = Type.getArgumentTypes(signature);
		if (m.isAbstract()) {
			Type   returnType  = Type.getReturnType(signature);
			InstructionList il = new InstructionList();
			il.append(new NOP());
			mg = new MethodGen(m.getAccessFlags()&~Constants.ACC_ABSTRACT,
							   returnType, argTypes,
							   null, // names are unknown
							   m.getName(), class_name,
							   il, cpg);
		} else {
            mg = wipeMethod(m, class_name, cpg);
		}
		if (debugging) {
			mg.removeLocalVariables();
			mg.removeLocalVariableTypes();
			int slot = 0;
			// create local variable table for "this" and arguments:
			if (!m.isAbstract())
				mg.addLocalVariable("this", new ObjectType(class_name),	slot++, null, null);
			for (int i=0; i<argTypes.length; i++)
				mg.addLocalVariable("arg"+i, argTypes[i], slot++, null, null);
			mg.setMaxLocals();
		}
		return mg;
	}

    /**
     * Generate the initial wrapper for the passed mehtod. 
     * It binds callin dispatch to Team-unaware client code.
     * @param m						the original method	
     * @param class_name	the name of the appropriate class
     * @param major 
     * @param cpg					the ConstantPoolGen of the class
     * @return							the generated method
     */
    private Method generateInitialWrapper(Method          m,
										  String          class_name,
										  int             major, 
										  ConstantPoolGen cpg)
	{
        MethodGen mg = getConcretMethodGen(m, class_name, cpg);

        String  method_name     = m.getName();
        Type    returnType      = mg.getReturnType();
        Type    chainReturnType = object;
        Type[]  argTypes        = mg.getArgumentTypes();

        String  name_chain      = genChainMethName(method_name);

        InstructionList il      = mg.getInstructionList();

        InstructionHandle startSynchronized;
        InstructionHandle chainCall; 
        int monitor;
		// start generating
		{
		    LocalVariableGen lg; // used for several local variables
		    
		    // Team[] _OT$teams;
		    int teams;
		    lg = mg.addLocalVariable(TEAMS, teamArray, null, null);
		    teams = lg.getIndex();
		    
		    // synchronized (TopMostBoundBaseClass.class) {
		    Pair<Integer,InstructionHandle> monitorResult = addClassMonitorEnter(mg, il, CallinBindingManager.getTopmostBoundBase(class_name), major, cpg);
		    monitor = monitorResult.first;
		    if (debugging)
		    	// no natural lines in this method: step-over until chain call, which has step-into: debugging => addLineNumber
		    	mg.addLineNumber(monitorResult.second, STEP_OVER_LINENUMBER);

		    // _OT$teams= new Teams[_OT$activeTeams.length];
		    startSynchronized= // begin area protected by exception handler
		    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		    il.append(InstructionConstants.ARRAYLENGTH);
		    il.append((Instruction)factory.createNewArray(teamType, (short) 1));
		    il.append(InstructionFactory.createStore(Type.OBJECT, teams));

		    // _OT$teamIDs=new int[_OT$activeTeamIDs.length];
		    int teamIDs;
		    lg = mg.addLocalVariable(TEAMIDS, intArray, null, null);
		    teamIDs = lg.getIndex();
		    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
		    il.append(InstructionConstants.ARRAYLENGTH);
		    il.append((Instruction)factory.createNewArray(Type.INT, (short) 1));
		    il.append(InstructionFactory.createStore(Type.OBJECT, teamIDs));
		    
		    // for (int i=0; i<_OT$activeTeams.length; ...
		    int for_index;
		    lg = mg.addLocalVariable("i", Type.INT, null, null); //$NON-NLS-1$
		    for_index = lg.getIndex();
		    il.append(new PUSH(cpg, 0));
		    il.append(InstructionFactory.createStore(Type.INT, for_index));
		  InstructionHandle for_start = 
			il.append(InstructionFactory.createLoad(Type.INT, for_index));
		    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
		    il.append(InstructionConstants.ARRAYLENGTH);
		  BranchInstruction if_loop_finished = 
			          InstructionFactory.createBranchInstruction(Constants.IF_ICMPGE, null);
		    il.append(if_loop_finished);
    
		    	// if (!_OT$activeTeams[i].isActive()) {
			    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
			    il.append(InstructionFactory.createLoad(Type.INT, for_index));
			    il.append(InstructionConstants.AALOAD);
			    il.append(factory.createInvoke(OTConstants.teamName, IS_ACTIVE, Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEINTERFACE));
			  BranchInstruction if_team_isactive = 
				  		  InstructionFactory.createBranchInstruction(Constants.IFNE, null);
			    il.append(if_team_isactive);
	
				    // invalidate activation of current team:
			    	// _OT$teams[i]= null;
				    il.append(InstructionFactory.createLoad(Type.OBJECT, teams));
				    il.append(InstructionFactory.createLoad(Type.INT, for_index));
				    il.append(InstructionConstants.ACONST_NULL);
				    il.append(InstructionConstants.AASTORE);
		
				    // _OT$teamIDs[i]= -1;
				    il.append(InstructionFactory.createLoad(Type.OBJECT, teamIDs));
				    il.append(InstructionFactory.createLoad(Type.INT, for_index));
				    il.append(new PUSH(cpg, -1));
				    il.append(InstructionConstants.IASTORE);
				  BranchInstruction goto_continue = 
					          InstructionFactory.createBranchInstruction(Constants.GOTO, null);
				    il.append(goto_continue);
				    
				// } else { adopt activation of current team:
				// _OT$teams[i]= _OT$activeTeams[i];
			  InstructionHandle adopt_activation = 
			    il.append(InstructionFactory.createLoad(Type.OBJECT, teams));
			    il.append(InstructionFactory.createLoad(Type.INT, for_index));
			    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAMS, teamArray, Constants.GETSTATIC));
			    il.append(InstructionFactory.createLoad(Type.INT, for_index));
			    il.append(InstructionConstants.AALOAD);
			    il.append(InstructionConstants.AASTORE);
	
				// _OT$teamIDs[i]= _OT$activeTeamIDs[i];
			    il.append(InstructionFactory.createLoad(Type.OBJECT, teamIDs));
			    il.append(InstructionFactory.createLoad(Type.INT, for_index));
			    il.append(factory.createFieldAccess(class_name, _OT_ACTIVE_TEAM_IDS, intArray, Constants.GETSTATIC));
			    il.append(InstructionFactory.createLoad(Type.INT, for_index));
			    il.append(InstructionConstants.IALOAD);
			    il.append(InstructionConstants.IASTORE);

		    // closing the above for: i++ and jump back:
		  InstructionHandle do_continue = 
			il.append(new IINC(for_index, 1));
		    il.append(InstructionFactory.createBranchInstruction(Constants.GOTO, for_start));

		    // link jump instructions to their targets:
		    InstructionHandle loop_finished = il.append(new NOP()); // synthetic jump target
		    if_loop_finished.setTarget(loop_finished);
		    if_team_isactive.setTarget(adopt_activation);
		    goto_continue.setTarget(do_continue);
		
		    // No more access to array fields, release monitor:
		    il.append(InstructionFactory.createLoad(Type.OBJECT, monitor));
		    il.append(new MONITOREXIT());
		    
			// load special arguments:
			if(!m.isStatic()) { // "this" cannot be accessed by static methods 
				chainCall= il.append(InstructionFactory.createThis());                  // this
			} else {
				chainCall= il.append(new NOP());
			}
			
			if (debugging)
				mg.addLineNumber(chainCall, STEP_INTO_LINENUMBER);
			// ih now points to end of area protected by exception handler 
			
			il.append(InstructionFactory.createLoad(teamArray, teams));  // _OT$teams
			il.append(InstructionFactory.createLoad(intArray, teamIDs)); // _OT$teamIDs
			il.append(new ICONST(0));                         // _OT$idx = 0
			il.append(new ICONST(0));							// _OT$bindIdx = 0
            il.append(new ICONST(-1));                        // _OT$baseMethTag is unused here
			il.append(new ACONST_NULL());                     // _OT$unusedArgs = null
		}
		
		
        // load regular arguments:
        int index = m.isStatic()?0:1;
        // chaining wrapper is always public, so never user INVOKESPECIAL:
        short invocationKind = m.isStatic()?Constants.INVOKESTATIC:Constants.INVOKEVIRTUAL;
		
        for (int i=0; i<argTypes.length; i++) {
            il.append(InstructionFactory.createLoad(argTypes[i],index));
            index += argTypes[i].getSize();
        }
		//
        il.append(factory.createInvoke(class_name, name_chain,
									   chainReturnType,
									   enhanceArgumentTypes(argTypes),
									   invocationKind));
		// generated invoke: _OT$<method_name>$chain (a1,.. aN)

        
		adjustValue(il, null, chainReturnType, returnType);
        il.append(InstructionFactory.createReturn(returnType));
        
        // handler for exception within synchronized:
        LocalVariableGen ex= mg.addLocalVariable("exceptionInSynchronized", Type.THROWABLE, il.getEnd(), null); //$NON-NLS-1$
        InstructionHandle handler=
        il.append(InstructionFactory.createStore(Type.THROWABLE, ex.getIndex()));
        il.append(InstructionFactory.createLoad(Type.OBJECT, monitor));
        il.append(new MONITOREXIT());
        il.append(InstructionFactory.createLoad(Type.THROWABLE, ex.getIndex()));
        il.append(new ATHROW());
        mg.addExceptionHandler(startSynchronized, chainCall, handler, Type.THROWABLE);
        
		// tidy:
        mg.setMaxStack();
        mg.setMaxLocals();
        Method generatedMethod = mg.getMethod();
        il.dispose();
        return generatedMethod;
    }

	/**
	 * Generate a version of the passed method which only calls its super method 
	 * (which will eventually call an initial-wrapper in a directly bound class).
	 * @param m		the original method
	 * @param cg	the ClassGen of the appropriate class
	 * @param cpg the ConstantPoolGen of the class
	 * @return			the generated method
	 */
	Method generateSuperCall (Method          m,
							  ClassGen        cg,
							  ConstantPoolGen cpg)
	{
		short invocationKind = m.isStatic() ? Constants.INVOKESTATIC : Constants.INVOKESPECIAL;
        MethodGen mg = getConcretMethodGen(m, cg.getClassName(), cpg);

        String  method_name     = m.getName();
        Type    returnType      = mg.getReturnType();
        Type[]  argTypes        = mg.getArgumentTypes();
        InstructionList il      = mg.getInstructionList();

        if(logging) printLogMessage("\nReplacing with call to super: " + method_name); //$NON-NLS-1$
		// start generating
		if(!m.isStatic()){ //JU
			il.append(InstructionFactory.createThis());
		} 
        int index = 1;
        for (int i=0; i<argTypes.length; i++) {
            il.append(InstructionFactory.createLoad(argTypes[i],index));
            index += argTypes[i].getSize();
        }
        il.append(factory.createInvoke(cg.getSuperclassName(), method_name,
									   returnType, argTypes,
									   invocationKind));
        il.append(InstructionFactory.createReturn(returnType));
		// generated invoke: return super.<method_name> (a1,.. aN)

		// tidy:
        mg.setMaxStack();
        mg.setMaxLocals();
        Method generatedMethod = mg.getMethod();
        il.dispose();
        return generatedMethod;
	}

	
	/**
	 * Generate a chaining wrapper for the original method described by the passed arguments. 
	 * This includes dispatch code and the termination condition for the recursion.
	 * @param mg								the MethodGen of the original method
	 * @param method_name			the name of the original method
	 * @param method_signature	the signature of the original method
	 * @param class_name				the name of the appropriate class
	 * @param cpg								the ConstantPoolGen of the class
	 * @param cg								the ClassGen of the class
	 * @param firstLine							the first real source line of this method
	 * @return
	 */
	Method generateChainingWrapper(MethodGen 		mg,
								   String 	 		method_name,
								   String 	 		method_signature,
								   String 	 		class_name, 
								   ConstantPoolGen 	cpg, 
								   ClassGen  		cg,
								   int 		 		firstLine)
    {
        Type     origReturnType        = mg.getReturnType();
        Type[]   argumentTypes         = mg.getArgumentTypes();
        String[] argumentNames         = mg.getArgumentNames();
        Type[]   enhancedArgumentTypes = enhanceArgumentTypes(argumentTypes);
        String[] enhancedArgumentNames = enhanceArgumentNames(argumentNames);
        Type enhancedReturnType = object;  // ALWAYS!

        InstructionList il = new InstructionList();

        // the chaining wrapper has to be 'public' because it will be called by base calls:
        int accessFlags = makePublicFlags(mg.getAccessFlags());
        
        MethodGen chainMethod = new MethodGen(accessFlags,
                                              enhancedReturnType,
                                              enhancedArgumentTypes,
                                              enhancedArgumentNames,
                                              genChainMethName(method_name),
                                              class_name,
                                              il, cpg);

		// All chaining calls return an Object.
		// Need to store this in a local variable to keep the stack
		// balanced, because each section (before, replace, after)
		// is guarded by its own exception handler, and they don't
		// like pending objects on the stack.
        InstructionHandle ih;
        int result, ot_team;
		{
			LocalVariableGen lg =
				chainMethod.addLocalVariable("_OT$result", enhancedReturnType, //$NON-NLS-1$
											 null, null);
			result = lg.getIndex();
			ih = il.append(InstructionFactory.createNull(enhancedReturnType));
			if (debugging)
				chainMethod.addLineNumber(ih, STEP_OVER_LINENUMBER);
			lg.setStart(il.append(InstructionFactory.createStore(enhancedReturnType, result)));
			// generated: RType _OT$result = null;
			
			lg = chainMethod.addLocalVariable("_OT$team", teamType, null, null); //$NON-NLS-1$
			ot_team = lg.getIndex();
			// generated: Team _OT$team;
		}
		
		int indexOffset = chainMethod.isStatic() ? -1 : 0; // argument indizes are decremented for static methods, 
                                                           // because of the missing 'this'
		short invocationKind = getInvocationType(mg);

        il.append(InstructionFactory.createLoad(Type.INT, IDX_ARG + indexOffset));
        il.append(InstructionFactory.createLoad(teamArray, TEAMS_ARG + indexOffset));
        il.append(new ARRAYLENGTH());
        IF_ICMPLT recursionNotYetTerminated = new IF_ICMPLT(null);
        il.append(recursionNotYetTerminated);
        // generated: if (_OT$teams.length < _OT$idx) {

        
		// load arguments:
        if (!chainMethod.isStatic()) {
        	ih = il.append(InstructionFactory.createThis());
        } else {
        	ih = il.append(new NOP());
        }
        if (debugging)
        	chainMethod.addLineNumber(ih, SHOW_ORIG_CALL ? firstLine : STEP_INTO_LINENUMBER); // show orig call at method header ("dispatching")

        int index = EXTRA_ARGS + 1;
        for (int i = 0; i < argumentTypes.length; i++) {
            il.append(InstructionFactory.createLoad(argumentTypes[i], index + indexOffset));
            index += argumentTypes[i].getSize();
        }
		//
        il.append(factory.createInvoke(class_name, genOrigMethName(method_name),
                                       origReturnType, argumentTypes,
									   invocationKind));
		// generated: this._OT$<method_name>$orig (a1,.., aN) 	for nonstatic case
        //                     _OT$<method_name>$orig (a1,.., aN) 			for static case
		
        if (debugging)
        	chainMethod.addLineNumber(il.append(new NOP()), STEP_OVER_LINENUMBER);
        
        adjustValue(il, null, origReturnType, enhancedReturnType);
        il.append(InstructionFactory.createReturn(enhancedReturnType));
        // generated: return _OT$result;

        ih = il.append(new NOP());
        recursionNotYetTerminated.setTarget(ih);
        // generated: ; (end of the if part)

        il.append(InstructionFactory.createLoad(teamArray, TEAMS_ARG + indexOffset));
        il.append(InstructionFactory.createLoad(Type.INT,  IDX_ARG + indexOffset));
        il.append(InstructionFactory.createArrayLoad(teamType));
        il.append(InstructionFactory.createStore(teamType, ot_team));
        // generated: _OT$team = _OT$teams[_OT$idx];

        // ---------------------------------------------
        createDispatchCode(chainMethod, il,
						   class_name, method_name,
						   method_signature, result, ot_team, cg, firstLine);
        // ---------------------------------------------

		ih = il.append(InstructionFactory.createLoad(enhancedReturnType, result));
        il.append(InstructionFactory.createReturn(enhancedReturnType));
        // generated: return _OT$result;
        if (debugging)
        	chainMethod.addLineNumber(ih, STEP_OVER_LINENUMBER);
        
		// tidy:
		chainMethod.removeNOPs();
		try { // [SH]: overcautious: I once saw this CCE with no clue, why it happened :(
			chainMethod.setMaxStack();
		} catch (ClassCastException cce) {
			System.err.println(chainMethod);
			cce.printStackTrace();
		}
        chainMethod.setMaxLocals();
		//chainMethod.removeNOPs();
		Method generated = chainMethod.getMethod();
		il.dispose();
        return generated;
    }

	private short getInvocationType(MethodGen chainMethod) {
		if (chainMethod.isStatic())
			return Constants.INVOKESTATIC;
		if (chainMethod.isPrivate())
			return Constants.INVOKESPECIAL;
		else
			return Constants.INVOKEVIRTUAL;
	}

	/**
	 * @param i
	 * @return
	 */
	private static int makePublicFlags(int flags) {
		if ((flags & Constants.ACC_PUBLIC) != 0) {
			return flags;
		}
		if ((flags & Constants.ACC_PRIVATE) != 0) {
			flags &= ~Constants.ACC_PRIVATE;
		} else if ((flags & Constants.ACC_PROTECTED) != 0) {
			flags &= ~Constants.ACC_PROTECTED;
		}
		flags |= Constants.ACC_PUBLIC;
		return flags;
	}

	/**
	 *  Generate the dispatch code by which a chaining wrapper invokes the
	 *  callin method(s).
	 *  This consists of three switch blocks: before, replace, after.
	 * @param chainMethod				the chaining wrapper method
	 * @param il									the InstructionList of the chaining wrapper			
	 * @param class_name				the name of the appropriate class
	 * @param method_name			the name of the original method
	 * @param method_signature	the signature of the original method
	 * @param result							the index of the '_OT$result' variable
	 * @param ot_team						the index of the variable containing the team currently processed by the chaining wrapper
	 * @param cg								the ClassGen of the appropriate class
	 * @param firstLine							the first real source line of this method
	 */
	void createDispatchCode(MethodGen chainMethod, InstructionList il,
							String class_name, String method_name,
							String method_signature, int result, int ot_team, ClassGen cg, int firstLine)
    {

        //    get bindings for actually modified methods and sort them by callin-modifier:
        //      modifier -> ArrayList<MethodBinding>
        HashMap<String, ArrayList<MethodBinding>> sortedMethodBindings = new HashMap<String, ArrayList<MethodBinding>>();
        Collection<MethodBinding> callinsForMethod;
        callinsForMethod = CallinBindingManager.getBindingForBaseMethod(class_name,
        																method_name, method_signature);
        //System.err.println(class_name +" : "+callinsForMethod);
        List<MethodBinding> inheritedMethodBindings = CallinBindingManager.getInheritedBaseMethodBindings(class_name, method_name, method_signature);
        //System.err.println(inheritedMethodBindings);
        
        //--------------------------------------------------------------------------------------------
        //JU: initialize callinsForMethod for overridden static base methods (begin)
//        if(chainMethod.isStatic()){
//        	if(callinsForMethod == null) {
//        		callinsForMethod = new LinkedList();
//        	}
//        	
//        	Collection classesDefBindingsToStaticMethods = CallinBindingManager.getInheritedCallinBindingsForStaticMethods(class_name, method_name, method_signature);
//        	Iterator classesIt = classesDefBindingsToStaticMethods.iterator();
//        	while(classesIt.hasNext()){
//        		String superClassName = (String) classesIt.next();
//        		Collection callinsFromSuperClasses = CallinBindingManager.getBindingForBaseMethod(superClassName, method_name, method_signature);
//        		callinsForMethod.addAll(callinsFromSuperClasses);
//        	}
//        }
        //JU: (end)
        //----------------------------------------------------------------------------------------------
        if (!chainMethod.isStatic())
        	callinsForMethod.addAll(inheritedMethodBindings);
        
        /*
        String[] interfaceNames = cg.getInterfaceNames();
		Collection interfaceInheritedMethodBindings  = new LinkedList();
		for (int i=0; i<interfaceNames.length;i++) {
			if (!interfaceNames[i].equals(class_name))
				interfaceInheritedMethodBindings.addAll(
						CallinBindingManager.getInterfaceInheritedMethodBindings(method_name,
																														  method_signature, 
																														  interfaceNames[i]));
		}

		if (callinsForMethod == null)
			callinsForMethod = new LinkedList();
		
		callinsForMethod.addAll(interfaceInheritedMethodBindings);
       */
        ListValueHashMap<MethodBinding> beforeBindings = new ListValueHashMap<MethodBinding>();
        ListValueHashMap<MethodBinding> replaceBindings = new ListValueHashMap<MethodBinding>();
        ListValueHashMap<MethodBinding> afterBindings = new ListValueHashMap<MethodBinding>();
        
        Iterator<MethodBinding> it = callinsForMethod.iterator();
        while (it.hasNext()) {
        	MethodBinding methodBinding = it.next();
        	//sourceMapGen.addSourceMapInfo(methodBinding);
        	String        modifier      = methodBinding.getModifier();
        	ArrayList<MethodBinding>     bindings      = sortedMethodBindings.get(modifier);
        	if (bindings == null) {
        		bindings = new ArrayList<MethodBinding>();
        		sortedMethodBindings.put(modifier, bindings);
        	}
        	bindings.add(methodBinding);
        	// ----> added for predecedence purpose:
        	String teamName = methodBinding.getTeamClassName();
        	if (modifier.equals("before")) {                    //$NON-NLS-1$
        		beforeBindings.put(teamName, methodBinding);
        	} else if (modifier.equals("replace")) {            //$NON-NLS-1$
        		replaceBindings.put(teamName, methodBinding);
        	} else if (modifier.equals("after")) {              //$NON-NLS-1$
        		afterBindings.put(teamName, methodBinding);
        	}
        	// <---
        }
        
        // if any team has multiple bindings, we need to insert additional checks to avoid duplicate
        // invocation of before/after causes during recursion.
        boolean useBindingIdx = false;
        for (LinkedList<MethodBinding> perTeamMethods : replaceBindings.valueSet())
        	if (perTeamMethods.size() > 1) {
        		useBindingIdx = true;
        		break;
        	}

        /****************************************************************************/
        // before callin :
        if (sortedMethodBindings.containsKey("before")) {          //$NON-NLS-1$
            if(logging) printLogMessage("before bindings will be applied..."); //$NON-NLS-1$
        	il.append(createSwitch(
        				  beforeBindings,
        				  chainMethod, ot_team,
        				  NORESULT, firstLine, cg.getMajor(), useBindingIdx));
            if(logging) printLogMessage("before bindings: "        //$NON-NLS-1$
        					+ sortedMethodBindings.get("before")); //$NON-NLS-1$
        }
        /****************************************************************************/
        // replacement callin or direct recursion :
        if (sortedMethodBindings.containsKey("replace")) { //$NON-NLS-1$
            if(logging) printLogMessage("recursive call and replace bindings will be applied..."); //$NON-NLS-1$
        	il.append(createSwitch(
        				  replaceBindings,
        				  chainMethod, ot_team,
        				  result, firstLine, cg.getMajor(), useBindingIdx));
            if(logging) printLogMessage("replace bindings: " //$NON-NLS-1$
        					+ sortedMethodBindings.get("replace")); //$NON-NLS-1$
        } else {
            if(logging) printLogMessage("recursive chain-method call will be done..."); //$NON-NLS-1$
        	// recursive call:

			createRecursiveCall(il, chainMethod, result, 1, 0, method_name, method_signature, firstLine);
        }
        /****************************************************************************/
        // after callin :
        if (sortedMethodBindings.containsKey("after")) { //$NON-NLS-1$
            if(logging) printLogMessage("after bindings will be applied..."); //$NON-NLS-1$
        	il.append(createSwitch(
        				  afterBindings,
        				  chainMethod, ot_team,
        				  /*NORESULT*/result, firstLine, cg.getMajor(), useBindingIdx));
            if(logging) printLogMessage("after bindings: " //$NON-NLS-1$
        					+ sortedMethodBindings.get("after")); //$NON-NLS-1$
        }
    }

	/**
	 *  Create a switch statement which contains one case for each MethodBinding
	 *  in a given list.
	 *  The switch block is furthermore wrapped in a try-catch block.
	 *  Herein all {@link org.objectteams.LiftingVetoException LiftingVetoException}
	 *  are caught, and possibly reported (if Dot.log.lift ist set).
	 * @param methodBindings hash map of team names to 'MethodBinding' lists
	 * @param mg method being generated.
	 * @param ot_team index of local variable <tt>_OT$team</tt>
	 * @param ot_result index of local variable <tt>_OT$result</tt>
	 */
	private InstructionList createSwitch(ListValueHashMap<MethodBinding> methodBindings, MethodGen mg,
										 int ot_team, int ot_result,
										 int firstLine,
										 int major,
										 boolean useBindingIdx)
	{
        InstructionList il = new InstructionList();

		boolean handlesReplacement = false;

		int indexOffset = mg.isStatic()?-1:0; // argument indizes are decremented for static methods, 
                                                                       // because of the missing 'this'
        
		// load value to be switched:
        il.append(InstructionFactory.createLoad(intArray, TEAMIDS_ARG+indexOffset));
        il.append(InstructionFactory.createLoad(Type.INT, IDX_ARG+indexOffset));
        InstructionHandle switchStart = il.append(InstructionFactory.createArrayLoad(Type.INT));
		// generated: _OT$teamIDs[_OT$idx]

        int numberOfCases = methodBindings.size();

        // one break for each case clause
        GOTO[] breaks = new GOTO[numberOfCases];
        for (int i = 0; i < numberOfCases; i++)
            breaks[i] = new GOTO(null);

        int[]               matches = new int[numberOfCases];
        InstructionHandle[] targets = new InstructionHandle[numberOfCases];

        int      caseCounter     = 0;
        
        List<MethodBinding> methodBindingsForTeam = null;
        MethodBinding mb = null;
        Iterator <Entry<String, LinkedList<MethodBinding>>> teamIterator = methodBindings.entrySet().iterator();
		while (teamIterator.hasNext()) {
			Entry<String, LinkedList<MethodBinding>> entry = teamIterator.next();
			String teamName = entry.getKey();
			methodBindingsForTeam = entry.getValue();
			//System.out.println(methodBindings.get(teamName));
			
			/*MethodBinding*/ mb = methodBindingsForTeam.get(0);
			
            matches[caseCounter] = TeamIdDispenser.getTeamId(teamName);
            InstructionHandle nextBranch = il.append(new NOP());

            // generate (_OT$teamID == teamId) branch here:

			// ========== create Cases: ==========
            if (mb.isReplace()) { // distinct treatment of replacement:
				handlesReplacement = true;
				createReplaceCase(mg, il,
								  teamName, methodBindingsForTeam,
								  ot_result, ot_team, major, firstLine);
			} else { // before or after callin:
				BranchInstruction ifBindingIdx = null;
				if (useBindingIdx) {
					// only if bindingIdx == 0
					il.append(InstructionFactory.createLoad(Type.INT, BIND_IDX_ARG+indexOffset));
					ifBindingIdx= new IFNE(null);
					il.append(ifBindingIdx);
				}
                createBeforeAfterCase(mg, il, teamName,
									  methodBindingsForTeam, ot_result, ot_team, major, firstLine);
				if (useBindingIdx) {
					ifBindingIdx.setTarget(il.append(new NOP()));
				}
			}

			// ===================================

            targets[caseCounter] = nextBranch;
            /*InstructionHandle break_instr =*/ il.append(breaks[caseCounter]);
			// generated: break;
  
            caseCounter++;
        } // end of while
        	
        // generate default branch here:
        InstructionHandle defaultBranch = il.append(new NOP());
        if (handlesReplacement)
			createRecursiveCall(il, mg, ot_result, 1, 0, mb.getBaseMethodName(), mb.getBaseMethodSignature(), firstLine);

        InstructionHandle afterSwitch = il.append(new NOP()); // all breaks point here.

		// ===== assemble the switch ====
		il.append(switchStart,
				  createLookupSwitch(matches, targets, breaks,
									 defaultBranch, afterSwitch)
				 );
		// ==============================

		// wrap everything in a try {} catch (LiftingVetoException e) {..}
		InstructionHandle endTry = il.getEnd();

		GOTO skipHdlr = null;
		skipHdlr = new GOTO(null);
		il.append(skipHdlr);
		// generated: goto normal exit

		InstructionHandle hdlr = il.append(new NOP());
		il.append(DebugUtil.createReportExc(factory));

		// catch: proceed with recursion if caught in a replace call
		if (handlesReplacement)
			createRecursiveCall(il, mg, ot_result, 1, 0, mb.getBaseMethodName(), mb.getBaseMethodSignature(), firstLine);

		mg.addExceptionHandler(il.getStart(), endTry, hdlr, liftingVeto);

		InstructionHandle nop = il.append(new NOP());
		skipHdlr.setTarget(nop);

        return il;
    }

	/** Create the recursice call of this chaining method.
	 * @param il insert the call into this list.
	 * @param mg this chaining method
	 * @param ot_result stack index of the _OT$result variable or NORESULT.
	 * @param idx_offset TODO
	 * @param bindIdx_offset TODO
	 * @param methodName TODO
	 * @param methodSignature TODO
	 */
	void createRecursiveCall (InstructionList il,
							  MethodGen mg,
							  int ot_result, int idx_offset, int bindIdx_offset, String methodName, String methodSignature, int firstLine)
	{
		InstructionHandle ih = !mg.isStatic()
				? il.append(InstructionFactory.createThis())
				: il.append(new NOP());
		if (debugging)
			mg.addLineNumber(ih, SHOW_RECURSIVE_CALL ? firstLine : STEP_INTO_LINENUMBER); // show recursive call at method header ("dispatching")
		
 		Type[] argTypes = mg.getArgumentTypes();
		Type returnType = mg.getReturnType();
		short invocationKind = getInvocationType(mg);
		
		// arguments: no adjustment except idx++.
		int index = 1;
		int indexOffset = mg.isStatic()?-1:0; // argument indizes are decremented for static methods, 
                                                                       // because of the missing 'this'
		for (int i=0; i<argTypes.length; i++) {
			il.append(InstructionFactory.createLoad(argTypes[i], index+indexOffset));
			if (index == OTConstants.IDX_ARG && idx_offset != 0) { // _OT$idx has to be incremented (adding idx_offset)
				il.append(new ICONST(idx_offset));
				il.append(new IADD());
			} else if (index == OTConstants.BIND_IDX_ARG) { // _OT$bindIdx has to be incremented (adding bindIdx_offset)
				if (bindIdx_offset == 0) { // bindArg argument has to be set to '0'
					il.append(new POP()); 			// remove loaded _OT$bindIdx
					il.append(new ICONST(0)); // replace it by '0' 
				} else {
					il.append(new ICONST(bindIdx_offset));
					il.append(new IADD());
				}
			}
			index += argTypes[i].getSize();
		}
		il.append(factory.createInvoke(mg.getClassName(), mg.getName(),
									   returnType, argTypes,
									   invocationKind));

		il.append(InstructionFactory.createStore(returnType, ot_result));

		// _OT$result = _OT$<method_name>$chain(_OT$teams, _OT$teamIDs, _OT$idx+1,
		//                                         a1, .., aN);
		if (debugging)
			mg.addLineNumber(il.append(new NOP()), STEP_OVER_LINENUMBER);
	}


	/**
	 * Create a block for a before or after callin as a case with a surrounding switch.
	 * @param mg TODO
	 * @param il InstructionList being assembled.
	 * @param connectorClassName name of a Team which has a callin to this method.
	 * @param mbList MethodBindings describing the callins.
	 * @param ot_result index of local variable <tt>_OT$result</tt>.
	 * @param ot_team  index of local variable <tt>_OT$team</tt>.l
	 * @param major
	 * @param firstLine							the first real source line of this method
	 */
	void createBeforeAfterCase(MethodGen mg,
							   InstructionList il, String connectorClassName,
							   List<MethodBinding> mbList, int ot_result, int ot_team,
							   int major, int firstLine)
    {	

		MethodBinding mb = mbList.get(0);
		ConstantPoolGen cpg = mg.getConstantPool();
		
		mbList = CallinBindingManager.sortMethodBindings(mbList, connectorClassName);
		
		Iterator<MethodBinding> it = mbList.iterator();
		while (it.hasNext()) {
			/*MethodBinding nextMethodBinding*/mb = it.next();
		
			String baseMethodSignature = mb.getBaseMethodSignature();
			Type[] baseArgTypes = Type.getArgumentTypes(baseMethodSignature);
			Type baseReturnType = Type.getReturnType(baseMethodSignature);
			
			Type[] wrapperArgTypes = Type.getArgumentTypes(mb.getWrapperSignature());
			int argsLen = wrapperArgTypes.length - 1; // don't count base arg.
			
			il.append(InstructionFactory.createLoad(teamType, ot_team));

			int packedArgPos = 0;
			InstructionHandle argArray = null;
			if (useReflection) {
				il.append(factory.createInvoke("java.lang.Object", "getClass", classType, new Type[0], Constants.INVOKEVIRTUAL));
				il.append(new LDC(cpg.addString(mb.getWrapperName())));
				pushTypeArray(il, wrapperArgTypes, major, cpg);
				il.append(factory.createInvoke("java.lang.Class", "getMethod", methodType, OTConstants.getMethodSignature, Constants.INVOKEVIRTUAL));
				
				il.append(InstructionFactory.createLoad(teamType, ot_team));
				// generated: 
				//   _OT$team.getClass().getMethod(<wrapperName>, <wrapper sign>)  
				//   _OT$team
				argArray = il.append(new ANEWARRAY(cpg.addClass(object)));
				
			} else {
				il.append(factory.createCast(teamType, new ObjectType(connectorClassName)));
				// generated: (<TeamClass>)_OT$team
			}
			
			// first argument: base object:
			packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
			{
				if (!mg.isStatic())
					il.append(InstructionFactory.createThis());
				else
					il.append(InstructionFactory.createNull(Type.OBJECT)); // no base object
			}
			checkPackValue1(il, useReflection, Type.OBJECT);
			
			// second argument: base result (if appropriate)
			if ( mb.isAfter() && !baseReturnType.equals(Type.VOID)) { // after callin wrapper get the base method result as second argument
				packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
				{
					il.append(InstructionFactory.createLoad(object, ot_result));
					adjustValue(il, null, object, baseReturnType);
					argsLen --; // the second (result) arg is also not part of the arg list
				}
				checkPackValue1(il, useReflection, baseReturnType);
			}
			
			// ------- load regular args: --------
			
			// stack positions for load instructions (one-based for non-statics, since 0 == this)
			int stackIndex = EXTRA_ARGS + (mg.isStatic() ? 0 : 1);
			// where within baseArgTypes do source arguments start?
			int firstArg = 0;
			
			if (mb.baseMethodIsCallin()) {
				// skip enhancement of this callin method (lower role).
				firstArg   += EXTRA_ARGS;
				stackIndex += EXTRA_ARGS;
				argsLen    += EXTRA_ARGS;
			}
			for (int i = firstArg; i < argsLen; i++) {
                if(logging) printLogMessage("loading " + baseArgTypes[i].toString()); //$NON-NLS-1$

                packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
                {
					il.append(InstructionFactory.createLoad(baseArgTypes[i],stackIndex));
                }
				checkPackValue1(il, useReflection, baseArgTypes[i]);
				
				stackIndex += baseArgTypes[i].getSize();
			}
			
			InstructionHandle callinCall;
			if (useReflection) {
				// this information was missing above:
				il.insert(argArray, createIntegerPush(cpg, packedArgPos));
				
				callinCall = il.append(factory.createInvoke("java.lang.reflect.Method", "invoke", object, new Type[]{object, objectArray}, Constants.INVOKEVIRTUAL));
				il.append(new POP()); // before/after wrappers return void
			} else {
				callinCall = il.append(factory.createInvoke(connectorClassName,
											   mb.getWrapperName(),
											   Type.VOID, wrapperArgTypes,
											   Constants.INVOKEVIRTUAL));
			}
			if (debugging) {
				mg.addLineNumber(callinCall, SHOW_ROLE_CALL ? firstLine : STEP_INTO_LINENUMBER); // show role call at method header ("dispatching")
				mg.addLineNumber(il.append(new NOP()), STEP_OVER_LINENUMBER);
			}
		}
    }

	/* this and the next method serve as a bracket for all pushes that may or may not require
	 * packing in an array, the array must already exist on the stack. */
	private int checkPackValue0(InstructionList il, boolean doPack, int argCount, ConstantPoolGen cpg) {
		if (doPack) {
			il.append(new DUP());
			il.append(createIntegerPush(cpg, argCount));
			return argCount+1;
		} else {
			return argCount;
		}
	}
	private void checkPackValue1(InstructionList il, boolean doPack, Type argType) {
		if (doPack) {
			if (argType instanceof BasicType)
				il.append(createBoxing((BasicType)argType));
			il.append(new AASTORE());
		}
	}
	/* Push an array of Class representing the signature given by argTypes. */
	private void pushTypeArray(InstructionList il, Type[] argTypes, int major, ConstantPoolGen cpg) {
		il.append(createIntegerPush(cpg, argTypes.length));
		il.append(new ANEWARRAY(cpg.addClass(classType)));
		for (int i=0; i<argTypes.length; i++) {
			Type type = argTypes[i];
			il.append(new DUP());
			il.append(createIntegerPush(cpg, i));
			if (type instanceof BasicType) {
				il.append(factory.createFieldAccess(toObjectTypeName((BasicType)type), "TYPE", classType, Constants.GETSTATIC));
			} else if (type instanceof ObjectType) {
				appendClassLiteral(il, ((ObjectType)type).getClassName(), major, cpg);
			} else if (type instanceof ArrayType) { 
				String prefix = "";
				while (type instanceof ArrayType) {
					prefix += '[';
					type = ((ArrayType)type).getElementType();
				}
				String elemTypeName = null;
				if (type instanceof ObjectType)
					elemTypeName = "L"+((ObjectType)type).getClassName()+';';
				else if (type instanceof BasicType)
					elemTypeName = ((BasicType)type).getSignature();
				appendClassLiteral(il, prefix+elemTypeName, major, cpg);
			} else {
				throw new OTREInternalError("unsupported type in signature "+type);
			}
			il.append(new AASTORE());
		}
	}

	/**
	 * Create a block for a replace callin as a case within a surrounding switch. If there are multiple bindings from the 
	 * same team to this base method, the bindings are sorted according to the precedence list of this team.
	 * @param mg base method being generated.
	 * @param il instruction list being assembled.
	 * @param connectorClassName name of a Team which has a callin to this method.
	 * @param mbList list of 'MethodBinding's
	 * @param ot_result index of a local variable <tt>_OT$result</tt>.
	 * @param ot_team  index of local variable <tt>_OT$team</tt>.
	 * @param major class file version
	 */
	void createReplaceCase(MethodGen mg, InstructionList il,
						   String connectorClassName, List<MethodBinding> mbList,
						   int ot_result, int ot_team, int major, int firstLine)
    {
		
		int indexOffset = mg.isStatic() ? -1 : 0; // argument indizes are decremented for static methods, 
												  // because of the missing 'this' 
		boolean multipleBindings = mbList.size() > 1;
		mbList = CallinBindingManager.sortMethodBindings(mbList, connectorClassName);
		
		MethodBinding mb = mbList.get(0); // default, if only one binding exists
		
		LocalVariableGen unused_args_lg = mg.addLocalVariable(UNUSED, objectArray, null, null);
		int unused_args = unused_args_lg.getIndex();
		unused_args_lg.setStart(il.append(new NOP()));
		if (multipleBindings) {
			InstructionList addition = new InstructionList();
			// load value to be switched:
			
			InstructionHandle switchStart = addition.append(InstructionFactory.createLoad(Type.INT, BIND_IDX_ARG+indexOffset)); 
			// loaded _OT$bindIdx
			
			int numberOfCases = mbList.size();
			
			// one break for each case clause
			GOTO[] breaks = new GOTO[numberOfCases];
			for (int i=0; i<numberOfCases; i++)
				breaks[i] = new GOTO(null);
			
			int[] matches = new int[numberOfCases];
			InstructionHandle[] targets = new InstructionHandle[numberOfCases];
			
			int      caseCounter     = 0;
			Iterator<MethodBinding> mbIterator = mbList.iterator();
			while (mbIterator.hasNext()) {
				mb = mbIterator.next();
				matches[caseCounter] = caseCounter;
				InstructionHandle nextBranch = addition.append(new NOP());
				// ========== create Cases: ===========
				addition.append(createSingleReplaceCallin(mg, connectorClassName, mb, ot_result, ot_team, unused_args, multipleBindings, mg.isStatic(), major, firstLine));
				// ==============================
				targets[caseCounter] = nextBranch;
				/*InstructionHandle break_instr =*/ addition.append(breaks[caseCounter]);
				// generated: break;
				caseCounter++;
			}
			// ========== create default: ===========
			InstructionHandle defaultBranch = addition.append(new NOP());
			createRecursiveCall(addition, mg, ot_result, 1, 0, mb.getBaseMethodName(), mb.getBaseMethodSignature(), firstLine);
			// ==============================
			
			InstructionHandle afterSwitch = addition.append(new NOP()); // all breaks point here.
			
			for (int i=0; i<numberOfCases; i++)
				breaks[i].setTarget(afterSwitch);
			
			addition.append(switchStart, new TABLESWITCH(matches, targets, defaultBranch));
			// wrap everything in a try {} catch (LiftingVetoException e) {..}
			InstructionHandle endTry = addition.getEnd();
			
			GOTO skipHdlr = null;
			skipHdlr = new GOTO(null);
			addition.append(skipHdlr);
			// generated: goto normal exit
			
			InstructionHandle hdlr = addition.append(new NOP());
			addition.append(DebugUtil.createReportExc(factory));
			
			// ========== create catch instructions: ===========
			createRecursiveCall(addition, mg, ot_result, 0, 1, mb.getBaseMethodName(), mb.getBaseMethodSignature(), firstLine);
			// =====================================
			mg.addExceptionHandler(addition.getStart(), endTry, hdlr, liftingVeto);
			
			InstructionHandle nop = addition.append(new NOP());
			skipHdlr.setTarget(nop);
			il.append(addition);
		} else { // only a single replace callin:
			il.append(createSingleReplaceCallin(mg, connectorClassName, mb, ot_result, ot_team, unused_args, multipleBindings, mg.isStatic(), major, firstLine));
		}
		unused_args_lg.setEnd(il.getEnd());
	}

	/**
	 * Creates a single replace callin call.
	 *
	 * @param mg						base method being generated.
	 * @param connectorClassName		name of a Team which has a callin to this method.
	 * @param mb						the 'MethodBinding' for this callin. 
	 * @param ot_result					index of a local variable <tt>_OT$result</tt>.
	 * @param ot_team					index of local variable <tt>_OT$team</tt>.
	 * @param unused_args				index of local variable <tt>_OT$unused_args</tt>.
	 * @param multipleBindings			flag indicating if there are multiple bindings in this case
	 * @param staticBaseMethod TODO
	 * @param major 					class file version
	 * @param firstLine					first real source line number of this method
	 * @return							instruction list for the callin call
	 */
	private InstructionList createSingleReplaceCallin(MethodGen mg, String connectorClassName, MethodBinding mb, int ot_result, int ot_team, int unused_args, boolean multipleBindings, boolean staticBaseMethod, int major, int firstLine) 
	{
		// Sequence of values to load is (letters refer to document parameter-passing.odg):
		// (f) _OT$team: 	call target for invoking the callin-wrapper
		// (g) baseObject:	this or null
		// (h) enhancement: [Team[IIII[Object; 
		// 					idxs are being manipulated here,
		//					unusedArgs[] is allocated and filled with 
		// (i)                - (int,Team) if current method is static role method.
		// (j)                - enhancement arguments, if current method is callin method
		// (k) regular arguments.
		
		// ------------------------------------------
    	//               prepare Types:
		// ------------------------------------------
		Type[] chainArgTypes = mg.getArgumentTypes();
    	Type[] baseArgTypes  = Type.getArgumentTypes(mb.getBaseMethodSignature());
    	Type[] roleArgTypes  = Type.getArgumentTypes(mb.getRoleMethodSignature());
    	roleArgTypes = enhanceArgumentTypes(roleArgTypes);
    	String wrapperName = mb.getWrapperName();
    	Type wrapperReturnType = Type.getReturnType(mb.getWrapperSignature());

    	Type[] wrapperArgTypes = Type.getArgumentTypes(mb.getWrapperSignature());
    	
    	// calculate return type:
        Type chainReturnType = mg.getReturnType();
        
        ConstantPoolGen cpg = mg.getConstantPool();
        InstructionList il = new InstructionList();
        // (f):
        il.append(InstructionFactory.createLoad(teamType, ot_team));

		int packedArgPos = 0;
		InstructionHandle argArray = null;
		if (useReflection) {
			il.append(factory.createInvoke("java.lang.Object", "getClass", classType, new Type[0], Constants.INVOKEVIRTUAL));
			il.append(new LDC(cpg.addString(mb.getWrapperName())));
			pushTypeArray(il, wrapperArgTypes, major, cpg);
			il.append(factory.createInvoke("java.lang.Class", "getMethod", methodType, OTConstants.getMethodSignature, Constants.INVOKEVIRTUAL));
			
			il.append(InstructionFactory.createLoad(teamType, ot_team));
			// generated: 
			//   _OT$team.getClass().getMethod(<wrapperName>, <wrapper sign>)  
			//   _OT$team
			argArray = il.append(new ANEWARRAY(cpg.addClass(object)));
		} else {
			il.append(factory.createCast(teamType, new ObjectType(connectorClassName)));
			// generated: (<TeamClass>)_OT$team
		}
       
        // (g):
		packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
		{
	        if(!staticBaseMethod)
				il.append(InstructionFactory.createThis());
			else
				il.append(InstructionFactory.createNull(Type.OBJECT)); // no base object
		}
		checkPackValue1(il, useReflection, Type.OBJECT);
        
    	// ----------------------------------------
    	// (h)   Load Extra Arguments:
    	// ----------------------------------------
        int staticOffset = staticBaseMethod?-1:0; // argument indizes are decremented for static methods, 
		          								  // because of the missing 'this' 
        // first 4 extra arguments: _OT$teams, _OT$teamIDs, _OT$idx, _OT$bindIdx
        for (int i=0; i<4; i++) { // If this is called only once _OT$idx has to be incremented, else _OT$bindIdx has to be incremented
        	packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
        	{
	        	il.append(InstructionFactory.createLoad(chainArgTypes[i], i+1+staticOffset));
	    		if (!multipleBindings && i+1+staticOffset == OTConstants.IDX_ARG+staticOffset) {// _OT$idx++:
	    			il.append(new ICONST(1));
	            	il.append(new IADD());
	    		} else if (multipleBindings && i+1+staticOffset == OTConstants.BIND_IDX_ARG+staticOffset) {// _OT$bindIdx:
	    			il.append(new ICONST(1));
	            	il.append(new IADD());
	    		}
        	}
        	checkPackValue1(il, useReflection, chainArgTypes[i]);
        }
		// _OT$baseMethTag:
        int base_meth_tag = CallinBindingManager.getBaseCallTag(mb.getBaseClassName(), 
												        		mb.getBaseMethodName(), 
												        		mb.getBaseMethodSignature());
//        //JU: added this if-statement (begin) ----------------------------------------------
//        if(staticBaseMethod && !mg.getClassName().equals(mb.getBaseClassName())){
//        	//the method binding is a dummy -> the callin wrapper is performed 
//        	//with an invalid base method tag value -> an exception will be thrown
//        	base_meth_tag = INVALID_BASE_METHOD_TAG;
//        }
//        //JU (end) --------------------------------------------------------------------------
        
        packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
        {
        	il.append(createIntegerPush(cpg, base_meth_tag));
        }
        checkPackValue1(il, useReflection, Type.INT);

        // collect regular args first, insert into il later:
        InstructionList regularArgs = new InstructionList();
        
        // put "new Object[]" on stack, later containing unused arguments:
        packedArgPos = checkPackValue0(il, useReflection, packedArgPos, cpg);
        {
	        il.append(createIntegerPush(cpg, baseArgTypes.length)); 
	        		// enough space to hold ALL base arguments
	        		// Note that the callin wrapper may add more elements to this array.
	        il.append((Instruction)factory.createNewArray(object, (short)1));
	        il.append(InstructionFactory.createStore(objectArray, unused_args));
	        // generated: _OT$unusedArgs = new Object[<n_base_args>];
	
	    	// ----------------------------------------
	        // handle more arguments: load regular ones, store unused arguments into the array:
	    	// ----------------------------------------
	        int unusedArgsIdx = 0; // index into Object[].
	
			int stackIdx = EXTRA_ARGS + 1 + staticOffset; // one-based, since 0 == this (unless static)
			int regularArgsStart = EXTRA_ARGS;
			boolean baseIsStaticCallin = mb.baseMethodIsCallin() && staticBaseMethod;
			if (baseIsStaticCallin) { 
				// (i) need these synthetic arguments up-front: "int dummy, Team enclosingTeam"
				storeUnusedArg(il, unused_args, 0,  // constant
						   	   new ICONST(0),
						   	   Type.INT,
						   	   cpg);
				storeUnusedArg(il, unused_args, 1,  // constant 
						   	   new ALOAD(regularArgsStart+1), 
						   	   OTConstants.teamType,
						   	   cpg);
	
				// consumed first two parameters into unusedArgs:
				regularArgsStart+=2;
				stackIdx += 2;
				unusedArgsIdx += 2;
			}
	        for (int i=regularArgsStart; i<chainArgTypes.length; i++) {
				Type argType = chainArgTypes[i];
				Instruction loadingInstruction = InstructionFactory.createLoad(argType, stackIdx);
				if (isRegularArg(i, mb.baseMethodIsCallin(), staticBaseMethod)) {
					// (k) collect loading instruction, for appending after _OT$unusedArgs.
					packedArgPos = checkPackValue0(regularArgs, useReflection, packedArgPos, cpg);
					{
						regularArgs.append(loadingInstruction);
					}
					checkPackValue0(regularArgs, useReflection, packedArgPos, cpg);
				} else {
					// (j) store unused arg 
					storeUnusedArg(il, unused_args, unusedArgsIdx++, loadingInstruction, argType, cpg);
					// generated: _OT$unusedArgs[<unusedArgsIdx>] = maybeBox(a<index>);
				}
				stackIdx += argType.getSize();
	        }
	        il.append(InstructionFactory.createLoad(objectArray, unused_args));
        }
        checkPackValue1(il, useReflection, objectArray);
        
		// (k) insert previously assembled load-sequence:
        il.append(regularArgs);
        
    	// ============= INVOKEVIRTUAL (wrapper) =============
        InstructionHandle callinCall;
        if (useReflection) {
    		// this information was missing above:
			il.insert(argArray, createIntegerPush(cpg, packedArgPos));
			
			callinCall = il.append(factory.createInvoke("java.lang.reflect.Method", "invoke", object, new Type[]{object, objectArray}, Constants.INVOKEVIRTUAL));
			wrapperReturnType = Type.OBJECT;
        } else {
        	callinCall = il.append(factory.createInvoke(connectorClassName, wrapperName,
    								   wrapperReturnType,
    								   wrapperArgTypes,
    								   Constants.INVOKEVIRTUAL));
        }
		if (debugging) {
			mg.addLineNumber(callinCall, SHOW_ROLE_CALL ? firstLine : STEP_INTO_LINENUMBER); // show role call at method header ("dispatching")
        	mg.addLineNumber(il.append(new NOP()), STEP_OVER_LINENUMBER);
		}

		adjustValue(il, null, wrapperReturnType, chainReturnType);
		il.append(InstructionFactory.createStore(chainReturnType, ot_result));
		
		return il;
	}

	/**
	 *  Store an unused value (loaded by pushInstruction) into _OT$unusedArgs 
	 */
	private void storeUnusedArg(InstructionList il, 
								int 		    unused_args, 
								int 			arrayIndex, 
								Instruction 	pushInstruction,
								Type        	argType,
								ConstantPoolGen cpg) 
	{
		il.append(InstructionFactory.createLoad(objectArray, unused_args));
		il.append(createIntegerPush(cpg, arrayIndex));
		il.append(pushInstruction);
		if (argType instanceof BasicType)
			il.append(createBoxing((BasicType)argType));
		il.append(InstructionFactory.createArrayStore(objectArray));
	}

	/**
	 * Is the parameter at position idx mapped (by paramPositions or implicitly)?
	 * Cut off head: 
	 * 		- (int,Team) if present (static role method)
	 * 		- enhancement (possible twice)
	 * @param idx parameter index of enhanced signature
	 */
	static boolean isRegularArg (int idx, boolean baseIsCallin, boolean baseIsStatic) {
		if (baseIsCallin && baseIsStatic) // FIXME(SH): should be baseIsRole instead of baseIsCallin!
			idx -= 2;
		int firstVisible = EXTRA_ARGS + (baseIsCallin?EXTRA_ARGS:0); // skip one or two enhancements
		return idx >= firstVisible;
	}

	/**
	 *  Given an argument of type <tt>actual</tt>, must
	 *  we use type <tt>formal</tt> in signatures,
	 *  because it is a supertype of <tt>actual</tt>?
	 */
	static Type checkWiden (Type actual, Type formal) {
		if (!actual.equals(formal)
			&& actual instanceof ObjectType
			&& formal instanceof ObjectType)
		{
			ObjectType actualObj = (ObjectType)actual;
			ObjectType formalObj = (ObjectType)formal;
			if (actualObj.subclassOf(formalObj))
				return formalObj;
		}
		return actual;
	}

	/**
	 * Create an instruction list for initializing the role set field on-demand.
	 * @param valueRequired should the role set be on the stack after this sequence?
	 * @param cg	the ClassGen of the appropriate class
	 */
	private InstructionList getInitializedRoleSet(String class_name, boolean valueRequired) {;
		InstructionList il = new InstructionList();

		// try to retrieve existing set:
		il.append(new ALOAD(0));
		il.append(factory.createGetField(class_name, OTConstants.ROLE_SET, OTConstants.roleSetType));
		if (valueRequired)
			il.append(new DUP()); // a spare value to keep on the stack if successful
		
		// if (roleSet == null) ..
		IFNONNULL branch = new IFNONNULL(null);
		il.append(branch);

		// conditionally create the set:
		if (valueRequired)
			il.append(new POP()); // remove useless "null", replace with DUP_X1 below
		il.append(new ALOAD(0));
		il.append(factory.createNew(OTConstants.roleSetType));
		il.append(new DUP());
		il.append(factory.createInvoke(OTConstants.roleSetType.getClassName(),
													 		Constants.CONSTRUCTOR_NAME,
													 		Type.VOID,
													 		Type.NO_ARGS, 
													 		Constants.INVOKESPECIAL));
		if (valueRequired)
			il.append(new DUP_X1()); // push below pending "this"
		
		// store in the field:
		il.append(factory.createPutField(class_name, OTConstants.ROLE_SET, OTConstants.roleSetType));
	
		// endif
		branch.setTarget(il.append(new NOP()));
		return il;
	}
	
	/**
	 * Generates the field '_OT$roleSet' which is used to store the added roles.
	 * @param cpg			the ClassGen of the appropriate class
	 * @param class_name	the name of the class
	 * @return				the generated field
	 */
	private Field generateRoleSet(ConstantPoolGen cpg, String class_name) {
		FieldGen fg = new FieldGen(Constants.ACC_PROTECTED, 
                                   OTConstants.roleSetType,
                                   OTConstants.ROLE_SET,
                                   cpg);
		return fg.getField();	
	}
	
	/**
	 * Generates the method 'public void _OT$addRole(Object role)' which adds the passed object 
	 * to the role set of this base class.
	 * @param cpg					the ClassGen of the appropriate class
	 * @param class_name	the name of the class
	 * @return							the generated method 
	 */
	private Method generateAddRole(ConstantPoolGen cpg, String class_name) {

		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC,
				 					 Type.VOID,
				 					 new Type[] { Type.OBJECT },
				 					 new String[] {"role"},
				 					 OTConstants.ADD_ROLE, class_name,
				 					 il, cpg);
		
		il.append(getInitializedRoleSet(class_name, /*valueRequired*/true)); 
		
		il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
		il.append(factory.createInvoke(OTConstants.roleSetType.getClassName(), 
									   "add", 
									   Type.BOOLEAN, 
									   new Type[] {Type.OBJECT},
									   Constants.INVOKEVIRTUAL));
		il.append(new POP());
		il.append(InstructionFactory.createReturn(Type.VOID));
		mg.removeNOPs();
		mg.setMaxStack();
		mg.setMaxLocals(2);
		return mg.getMethod();
	}
	
	/**
	 * Generates the method 'public void _OT$removeRole(Object role)' which removes the passed object 
	 * from the role set of this base class.
	 * @param cpg			the ClassGen of the appropriate class				
	 * @param class_name  	the name of the class
	 * @return				the generated method 
	 */
	private Method generateRemoveRole(ConstantPoolGen cpg, String class_name) {
		
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC,
						 			 Type.VOID,
						 			 new Type[] { Type.OBJECT },
						 			 new String[] {"role"},
						 			 OTConstants.REMOVE_ROLE, class_name,
						 			 il, cpg);
		il.append(new ALOAD(0)); 			  
		il.append(factory.createGetField(class_name, OTConstants.ROLE_SET, OTConstants.roleSetType));
		il.append(InstructionFactory.createLoad(Type.OBJECT, 1));
		il.append(factory.createInvoke(OTConstants.roleSetType.getClassName(), 
									   "remove", 
									   Type.BOOLEAN, 
									   new Type[] {Type.OBJECT},
									   Constants.INVOKEVIRTUAL));
		il.append(new POP());
		il.append(InstructionFactory.createReturn(Type.VOID));
		mg.removeNOPs();
		mg.setMaxStack(2);
		mg.setMaxLocals(2);
		return mg.getMethod();
	}
	
}
