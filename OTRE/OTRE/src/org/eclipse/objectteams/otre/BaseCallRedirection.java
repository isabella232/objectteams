/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseCallRedirection.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import de.fub.bytecode.classfile.*;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.*;

import java.util.*;

import org.eclipse.objectteams.otre.util.*;

/**
 * for callin-role-methods with recursive method-calls (base calls) we add a
 * method with extendend signature.
 * Within the extendend method recursive calls are replaced by the corresponding
 * (chaining)base-call.<p>
 * For example:
 * <pre>
 *      callin m1() { m1(); } -->
 *      callin m1(Team _OT$teams[], int _OT$teamIDs[],
 *                int _OT$idx, int _OT$baseMethTag){
 *          liftToRole(b1._OT$m1$chain(Team _OT$teams[],
 *                                     int _OT$teamIDs[],
 *                                     int _OT$idx,
 *                                     int _OT$baseMethTag));
 *      }
 * </pre>
 *
 * @version $Id: BaseCallRedirection.java 23408 2010-02-03 18:07:35Z stephan $
 * @author  Christine Hundt
 * @author Stephan Herrmann
 */
public class BaseCallRedirection extends ObjectTeamsTransformation {

    static class IHPair {
		private InstructionHandle _ih1, _ih2;
		public IHPair (InstructionHandle ih1, InstructionHandle ih2) {
			_ih1 = ih1;
			_ih2 = ih2;
		}
		public InstructionHandle fst() {return _ih1; }
		public InstructionHandle snd() {return _ih2; }
	}

	public BaseCallRedirection(SharedState state) { this(null, state); }
    public BaseCallRedirection(ClassLoader loader, SharedState state) {
    	super(loader, state);
    }
    
    /**
     * @param ce
     * @param cg
     */
    public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
    	factory = new InstructionFactory(cg);
    	String class_name = cg.getClassName();

    	if (state.interfaceTransformedClasses.contains(class_name)) {
    		return; //already transformed!
    	}

        ConstantPoolGen cpg = cg.getConstantPool();
    	checkReadClassAttributes(ce, cg, class_name, cpg);
            
    	if (!CallinBindingManager.isRole(class_name)) {
    		return;
    	}
						
    	if (!cg.isInterface()) {
	    	Set<String> boundBaseMethods = CallinBindingManager.getBoundRoleMethods(class_name);    	
	    	addBaseCallSurrogatesForReplaceBindings(ce, boundBaseMethods, cg);
    	}

    	Method[] methods = cg.getMethods();
    	for (int i=0; i<methods.length; i++) {
    		Method m = methods[i];
    		String method_name  = m.getName();
    		String method_signature = m.getSignature();
            
    		if (candidateForImplicitActivation(m, cg, cpg)) { // TODO: check the other preconditions, like not abstact etc. 
    			cg.replaceMethod(m, genImplicitActivation(m, class_name, cpg, true));
    		}
    		
    		if (!isCallin(m, cg))
    			continue;
            if (logging) printLogMessage(method_name + " in " + class_name
                        + " IS A CALLIN-METHOD!");
    		if (method_name.startsWith(OT_PREFIX)) {
    			method_name = revertToOriginalName(method_name);
    			if (logging) printLogMessage("Reverted tsuper name to " + method_name);
    		}
    		// code SHOULD contain at least one base call.
            if(logging) printLogMessage("----->will add another method " + method_name
    						+ " with enhanced signature");
             
//{SH: retrench signature because otherwise the binding will not be found,
//     as a result an empty basecall surrogate will be generated leading to repetetive methods...
//     TODO(SH) is this the proper way to retrench??
			String enhancedPrefix = "([Lorg/objectteams/Team;[IIII[Ljava/lang/Object;";
			if (method_signature.startsWith(enhancedPrefix)) 
    			method_signature = "("+method_signature.substring(enhancedPrefix.length());
// SH}    		
    		
    		boolean roleMethodIsBound = CallinBindingManager.roleMethodHasBinding(class_name, 
    																	   method_name,
																		   method_signature);

    		//if (mbs.isEmpty()) {
    		if (!roleMethodIsBound) {
    			if (logging) printLogMessage("callin method " + method_name
    						+ " was not bound in this class!!!");
    		}
    		MethodGen baseCallSurrogate = null;
    		if (!IS_COMPILER_13X_PLUS) { // since 1.3.0 this part is legacy:
	    		if (!roleMethodIsBound && !methodHasCallinFlags(m, cg, OVERRIDING) && !m.isStatic()) {
	    			// method not bound in current class and doesn't inherit a base call surrogate
	    			baseCallSurrogate = generateEmptyBaseCallSurrogate(cg, m);
	    		}
	    		if (baseCallSurrogate != null)
	    			ce.addMethod(baseCallSurrogate.getMethod(), cg);
    		}
    		
    	}
    	state.interfaceTransformedClasses.add(class_name);
    }

	/**
	 *  Generates an "empty" base call surrogate method, which just throws an 'Error'.
	 *  This method should normaly never be called, but overwritten in a subclass. 
	 *  It has to be generated just for the wellformedness of the class file.
	 * @param cg
	 * @param m
	 * @param mbs
	 * @param baseClassName
	 * @return
	 */
	private MethodGen generateEmptyBaseCallSurrogate(ClassGen cg, Method m/*, List mbs*/) {
		if (m.getName().startsWith(OT_PREFIX))
			return null; // ot-internal methods don't need this?

		ConstantPoolGen cpg = cg.getConstantPool();
		String class_name = cg.getClassName();
		MethodGen mg = new MethodGen(m, class_name, cpg);
		
		if (isTSuperWrapper(mg)) {
			// tsuper wrapper do not need base call surrogates
			return null;
		}
		
		// role method already was enhanced by compiler
		Type[] enhancedArgumentTypes = mg.getArgumentTypes();
		if (IS_COMPILER_GREATER_123) {
			// add super flag between enhancement and real arguments:
			int len = enhancedArgumentTypes.length;
			Type[] newArgumentTypes = new Type[len+1];
			System.arraycopy(enhancedArgumentTypes, 0, newArgumentTypes, 0, EXTRA_ARGS);
			newArgumentTypes[EXTRA_ARGS] = Type.BOOLEAN;
			System.arraycopy(enhancedArgumentTypes, EXTRA_ARGS, newArgumentTypes, EXTRA_ARGS+1, len-EXTRA_ARGS);
			enhancedArgumentTypes = newArgumentTypes;
		}
		Type   enhancedReturnType    = mg.getReturnType();
		
// {SH: interface method has no argument names? generate dummy names: 
		String[] argumentNames = new String[enhancedArgumentTypes.length];
		for (int i = 0; i < argumentNames.length; i++) {
			argumentNames[i] = "arg"+i;
		}
			
		// role method already was enhanced by compiler:
		String[] enhancedArgumentNames = argumentNames;
// }	
		InstructionList il = new InstructionList();
		//int accessFlags = m.getAccessFlags();
		int accessFlags = Constants.ACC_PROTECTED; // no unanticipated calls possible

// {SH: interface methods must be public abstract:
		if (cg.isInterface()) 
			accessFlags = Constants.ACC_ABSTRACT|Constants.ACC_PUBLIC;
// }
		
		MethodGen baseCallSurrogate = new MethodGen(accessFlags,
													enhancedReturnType,
													enhancedArgumentTypes,
													enhancedArgumentNames,
													getBaseCallSurrogateName(m.getName(), m.isStatic(), class_name /*genRoleInterfaceName(class_name)*/), 
													class_name,
													il, cpg);
// {SH: no code for interface method:
		if (!cg.isInterface()) {
			// orig:
			if (logging)
				printLogMessage("Exception has to be thrown!");

			createThrowInternalError(cpg, il, new InstructionList(new PUSH(cpg, "Binding-Error: base-call impossible!")));

			il.append(InstructionFactory.createNull(enhancedReturnType));
			il.append(InstructionFactory.createReturn(enhancedReturnType));

			if (debugging)
				baseCallSurrogate.addLineNumber(il.getStart(), STEP_OVER_LINENUMBER);
		}
// }

		il.setPositions();
		baseCallSurrogate.removeNOPs();
		baseCallSurrogate.setMaxStack();
		baseCallSurrogate.setMaxLocals();
		return baseCallSurrogate;		
	}

	/**
	 * @param mg
	 * @return
	 */
	private static boolean isTSuperWrapper(MethodGen mg) {
		Type[] argTypes = mg.getArgumentTypes();
		if (argTypes.length == 0) {
			return false; // no tsuper marker interface argument existing 
		}
		String lastArgument = (argTypes[argTypes.length - 1]).toString();
		return lastArgument.contains(OTDT_PREFIX);
	}

	/**
	 * Adds base call surrogate method for all role method bindings in the current role class.
	 * Thereby method bindings which are defined in super roles are accumulated and 
	 * considered as well. 
	 * @param ce				the ClassEnhancer to which the new method has to be added
	 * @param boundRoleMethods	the bound methods of the role class
	 * @param cg 				the ClassGen for the role class
	 */
	private void addBaseCallSurrogatesForReplaceBindings(ClassEnhancer ce, Set<String> boundRoleMethods, ClassGen cg)
	{
		Iterator<String> it = boundRoleMethods.iterator();
		while (it.hasNext()) {
			String nameAndSignature = it.next();
			int dotIndex = nameAndSignature.indexOf('.');
			String methodName = nameAndSignature.substring(0, dotIndex);
			String methodSignature = nameAndSignature.substring(dotIndex + 1);
			List<MethodBinding> mbs = CallinBindingManager.getBindingsForRoleMethod(cg.getClassName(), 
																	 methodName, 
																	 methodSignature);
			MethodBinding anyMethodBinding = mbs.get(0);
			if (!anyMethodBinding.isReplace()) {
				continue; 
			}
			mbs.addAll(CallinBindingManager.getInheritedRoleMethodBindings(cg.getClassName(),
																		   methodName,
																		   methodSignature));
			
			if (anyMethodBinding.hasStaticRoleMethod())
				continue; // base call surrogates for static methods are generated within the enclosing team class
			// TODO: remove this check as soon as static replace method bindings are no longer in 'CallinMethodMappings'
			MethodGen baseCallSurrogate = genBaseCallSurrogate(cg, mbs);
			ce.addOrReplaceMethod(baseCallSurrogate.getMethod(), cg);
		}
	}

	/**
	 * Generates base call surrogate method for the role method for which the method bindings 'mbs' are.
	 * Thereby a switch-case for each bound base method is generated.
	 * This method is only for nonstatic role methods. Base call surrogates for static methods 
	 * are generated within the enclosing team class
	 * @param cg			the ClassGen for the role class	
	 * @param mbs			the method bindings for one role method
	 * @param baseClassName	the name of the base class
	 */
	MethodGen genBaseCallSurrogate(ClassGen cg, List<MethodBinding> mbs) {
		
		//baseClassName would not be needed here, if I could find out the root-base-class-type...
		ConstantPoolGen cpg = cg.getConstantPool();
		String class_name = cg.getClassName();

		MethodBinding anyBindingForRoleMethod = mbs.get(0);
		String baseClassName = anyBindingForRoleMethod.getRootBoundBase();
		String roleMethodSignature = anyBindingForRoleMethod.getRoleMethodSignature();

		Type[] enhancedArgumentTypes;
		{
			Type[] argTypesTail = Type.getArgumentTypes(roleMethodSignature);
			if (IS_COMPILER_GREATER_123) {
				// add super flag between enhancement and real arguments:
				int len = argTypesTail.length;
				System.arraycopy(argTypesTail, 0, argTypesTail=new Type[len+1], 1, len);
				argTypesTail[0] = Type.BOOLEAN;
			}
			enhancedArgumentTypes = enhanceArgumentTypes(argTypesTail);
		}
		Type enhancedReturnType = generalizeReturnType(Type.getReturnType(roleMethodSignature));
		String methodName = anyBindingForRoleMethod.getRoleMethodName();
		InstructionList il = new InstructionList();
		int accessFlags = Constants.ACC_PROTECTED;

		MethodGen baseCallSurrogate = new MethodGen(accessFlags,
													enhancedReturnType,
													enhancedArgumentTypes,
													null, // no explicit names
													getBaseCallSurrogateName(methodName, false, 
																			 genRoleInterfaceName(class_name)), 
													class_name,
													il, cpg);

		ObjectType baseClass = new ObjectType(baseClassName);
		ObjectType outerClass;
		{
			String outerClassName = getOuterClassName(class_name);
			outerClass = new ObjectType(outerClassName);
		}
			
		LocalVariableGen otResult = null;
		
		otResult = baseCallSurrogate.addLocalVariable("_OT$result",
				enhancedReturnType, null, null);

		il.insert(InstructionFactory.createStore(enhancedReturnType,
				otResult.getIndex()));
		il.insert(new ACONST_NULL());
		il.setPositions(); // about to retrieve instruction handles.
		
		if (logging) printLogMessage("base-call switch has to be inserted!");	
		InstructionList loading = new InstructionList();
		loading.append(InstructionFactory.createThis());
		int index = 1;
		for (int i = 0; i < enhancedArgumentTypes.length; i++) {
			loading.append(InstructionFactory.createLoad(enhancedArgumentTypes[i],index));
			index += enhancedArgumentTypes[i].getSize();
		}
		
		Type[] argumentTypes = Type.getArgumentTypes(roleMethodSignature);
		Type returnType = Type.getReturnType(roleMethodSignature);
		
		if (debugging) {
			baseCallSurrogate.addLineNumber(il.getStart(), STEP_OVER_LINENUMBER);
		}
		
		boolean generateSuperAccess = false;
		List<SuperMethodDescriptor> superAccesses = IS_COMPILER_GREATER_123 ? CallinBindingManager.getSuperAccesses(baseClassName) : null;
		if (superAccesses != null) {
			outer: for (SuperMethodDescriptor superMethod : superAccesses) {
				for (MethodBinding methodBinding : mbs) {
					if (   superMethod.methodName.equals(methodBinding.getBaseMethodName())
						&& superMethod.signature.equals(methodBinding.getBaseMethodSignature())) 
					{
						generateSuperAccess = true; 
						break outer;
					}
				}
			}
		}

		BranchInstruction ifSuper = new IFEQ(null);
		GotoInstruction skipElse  = new GOTO(null);
		if (generateSuperAccess) {
			// gen: if (isSuperAccess) { _OT$base._OT$m$super(args); } else ...
			il.append(InstructionFactory.createLoad(Type.BOOLEAN, EXTRA_ARGS+1)); // last synthetic arg is super-flag
			il.append(ifSuper);
			il.append(genBaseCallSwitch(cpg, mbs, baseCallSurrogate,
										 argumentTypes,
										 outerClass, baseClass,
										 returnType, 
										 otResult, loading, true));
			il.append(skipElse);
		}
		InstructionList 
		basecall = genBaseCallSwitch(cpg, mbs, baseCallSurrogate,
									 argumentTypes,
									 outerClass, baseClass,
									 returnType,
									 otResult, loading, false);
		InstructionHandle callStart = basecall.getStart(); // store handle before append eats the list
		il.append(basecall);
		if (generateSuperAccess) {
			ifSuper.setTarget(callStart);
			skipElse.setTarget(il.append(new NOP()));
		}
		
		il.append(InstructionFactory.createLoad(enhancedReturnType, otResult.getIndex()));
		il.append(InstructionFactory.createReturn(enhancedReturnType));							
		
		il.setPositions();
		baseCallSurrogate.removeNOPs();
		baseCallSurrogate.setMaxStack();
		baseCallSurrogate.setMaxLocals();
		return baseCallSurrogate;		
	}

/*
    Type findBaseFieldType(JavaClass c, ConstantPoolGen cpg) {
           Field[] fields = c.getFields();
            for (int l=0; l<fields.length; l++) {
                Field f = fields[l];
                if (f.getName().equals(BASE)) {
                    FieldGen fg = new FieldGen(f, cpg);
                    return fg.getType();
                }
            }
			JavaClass superClass = Repository.lookupClass(c.getSuperclassName());
			// BCEL bug: super class of "Object" is "Object":
			//System.err.println("Superclass of "+ c.getClassName()+" is " +c.getSuperclassName());
			if (!superClass.getClassName().equals("java.lang.Object")) {
					return findBaseFieldType(superClass, cpg);
			}
            return null;
    }*/

    /**
	 * Iterate through the instructions of a callin method.
	 * <ul>
	 *   <li>Adjust local variable instructions due to inserted extra arguments.
	 *   <li>Replace base-calls and calls to activate.
	 * </ul>
	 * @param cg
	 * @param m
	 * @param mbs List<MethodBinding>
	 * @return MethodGen
	 */
    MethodGen replaceBaseCalls(ClassGen cg, Method m, List<MethodBinding> mbs) {

    	int indexOffset = m.isStatic() ? -1 : 0; // argument indices are decremented for static methods, 
												 // because of the missing 'this'
        
    	ConstantPoolGen cpg = cg.getConstantPool();
        String class_name   = cg.getClassName();
        String method_name  = m.getName();

        MethodGen mg = new MethodGen(m, class_name, cpg);

		Type[]   argumentTypes         = mg.getArgumentTypes();
        Type     returnType            = mg.getReturnType();
       
        Type[] enhancedArgumentTypes = enhanceArgumentTypes(mg.getArgumentTypes());
// {SH abstract methods may not have argument names??        
        String[] argumentNames;
        if (m.isAbstract()) {
        	argumentNames = new String[argumentTypes.length];
			int index = 0;
        	for (int i = 0; i < argumentNames.length; i++) {
				argumentNames[i] = "arg" + index/* i */;
				index += argumentTypes[i].getSize();
			}
			
/*			
//			load regular arguments:
				 int index = 1;
				 for (int i=0; i<argTypes.length; i++) {
					 il.append(InstructionFactory.createLoad(argTypes[i],index));
					 index += argTypes[i].getSize();
				 }
//*/
			
        } else {
        	argumentNames = mg.getArgumentNames();
        }
        String[] enhancedArgumentNames = enhanceArgumentNames(argumentNames);
// orig:        String[] enhancedArgumentNames = enhanceArgumentNames(mg.getArgumentNames());
// SH}       

        Type enhancedMethodReturnType  = generalizeReturnType(m.getSignature());

// {SH instruction list may be null for abstract method		
// orig:   InstructionList il = mg.getInstructionList().copy();
		InstructionList il = mg.getInstructionList();
		if (il != null)
			il = il.copy();
		else
			il = new InstructionList();
// SH}		

        MethodGen enhancedMethod = new MethodGen(m.getAccessFlags(),
                                                 enhancedMethodReturnType,
                                                 enhancedArgumentTypes,
                                                 enhancedArgumentNames,
                                                 method_name, class_name,
                                                 il, cpg);
		//not needed??:
		// or not??????
		copyLocalVariables(mg, enhancedMethod);
		copyLineNumbers(mg, enhancedMethod);
		
        boolean returnValueAdded   =    (returnType               == Type.VOID)
                                     && (enhancedMethodReturnType != Type.VOID);

		// all exception handlers of this method, which have to be updated later.
		CodeExceptionGen [] handlers = copyExceptionHandlers(mg, enhancedMethod, il);
		// list of instruction handles (old and new) that are replaced in the sequel
		ArrayList<IHPair> replacedInstructions = new ArrayList<IHPair>(); // of IHPair;
		// set of instruction handles which signal TargetLostException during delete().
		HashSet<InstructionHandle> targetLost = new HashSet<InstructionHandle>(); // of InstructionHandle

        // create  LocalVariable Object _OT$result:
        LocalVariableGen otResult = null;
        
        int slot = mg.getMaxLocals() + EXTRA_ARGS-indexOffset;
        otResult = enhancedMethod.addLocalVariable("_OT$result", enhancedMethodReturnType,
												   slot, null, null);
        
        // subtract EXTRA_ARGS since this offset will be added again below.
        il.insert(InstructionFactory.createStore(enhancedMethodReturnType,
        							    		 otResult.getIndex() - EXTRA_ARGS));

        il.insert(new ACONST_NULL());
        il.setPositions(); // about to retrieve instruction handles.

        InstructionHandle[] ihs = il.getInstructionHandles();
        //printLogMessage("every call of base." + method_name + "(...) will be replaced by "
        //            + liftMethodName + BASE + "."+baseChainMethodName+"(...))");

        int actInstruction = 0;
        int offset = EXTRA_ARGS;
        while (actInstruction < ihs.length) {
			Instruction act_instruction = ihs[actInstruction].getInstruction();
/****************************** variable index adaption: **********************************/
   			if(act_instruction instanceof LocalVariableInstruction) {
            // add offset to the index of every variable load or store instruction,
			// because of the inserted EXTRA_ARGS  arguments:
                LocalVariableInstruction localVariableInstruction = (LocalVariableInstruction) act_instruction;
                if (localVariableInstruction.getIndex() != 0 || (enhancedMethod.isStatic())) { // 'this' stays at index 0
                    if (localVariableInstruction instanceof StoreInstruction) {
                        localVariableInstruction =
							InstructionFactory.createStore(localVariableInstruction.getType(cpg),
														   offset+localVariableInstruction.getIndex());
                    } else if (localVariableInstruction instanceof LoadInstruction) {
                        localVariableInstruction =
							InstructionFactory.createLoad(localVariableInstruction.getType(cpg),
											   			  offset+localVariableInstruction.getIndex());
                    } else if (localVariableInstruction instanceof IINC) {
                    	localVariableInstruction.setIndex(offset+localVariableInstruction.getIndex());
                    	// TODO: check, if this is enough for all kinds of LocalVariableInstructions 
                    	//               and if there are more instructions which use variable indizes!!
                    }
                    ihs[actInstruction].setInstruction(localVariableInstruction);
                }
            } else if (act_instruction instanceof RET) {
                RET ret = (RET)act_instruction;
                if (ret.getIndex() != 0)
                    ihs[actInstruction].setInstruction(new RET(offset + ret.getIndex()));

/*************************** "super"- & "tsuper"-call enhancement: **********************************/
   			} else if (super_or_tsuper_instruction(act_instruction, method_name, cpg) ) {

				InvokeInstruction ii = (InvokeInstruction)act_instruction;
				InstructionHandle next = ihs[actInstruction+1];
				InstructionList changedArea;
				InstructionHandle[] delim = new InstructionHandle[2];
				int stackDepth = computeArgumentStackDepth(cpg, ii);
				InstructionList loading = pruneLoading(il, ihs, actInstruction,
													   stackDepth, cpg,
													   targetLost, delim, true);
				if (loading == null) {
					actInstruction++;
					continue;
				}
				 
				changedArea = genEnhancedSuperCall(cpg, ii, enhancedMethod, loading);
				if (returnValueAdded) {
                    changedArea.append(InstructionFactory.createStore(
                            enhancedMethodReturnType, otResult.getIndex()));
                } else {
					InstructionHandle ih = adjustValue(changedArea, changedArea.getEnd(), enhancedMethodReturnType, returnType);
					if (debugging && ih != null)
						mg.addLineNumber(ih, STEP_OVER_LINENUMBER);
				}
				replacedInstructions.add(new IHPair(delim[0], changedArea.getStart()));
				replacedInstructions.add(new IHPair(delim[1], changedArea.getEnd()));

				il.insert(next, changedArea);
				actInstruction++;
				continue;
				
/*************************** "activate" substitution and base-call generation: ************/
            //} else if (act_instruction instanceof INVOKEVIRTUAL) {
            //    INVOKEVIRTUAL iv = (INVOKEVIRTUAL)act_instruction;
			} else if (act_instruction instanceof InvokeInstruction) {
				InvokeInstruction iv = (InvokeInstruction)act_instruction;
                String iv_name = iv.getName(cpg);

                // FIXME(SH): is this still needed? 
                //            - activate is commented-out,
                //            - base call is now generated by the compiler.
                if(!(iv_name.equals(method_name) ||
                     iv_name.equals("activate")))
				{
                    actInstruction++;
                    continue;
                }
                InstructionHandle next = ihs[actInstruction+1];
                InstructionList changedArea;
				InstructionHandle[] delim = new InstructionHandle[2];
               /*
                if (iv_name.equals("activate")) {
					// blank original invokevirtual:
					ihs[actInstruction].setInstruction(new NOP());
					Type [] ivArgTypes = iv.getArgumentTypes(cpg);
					int activateArgCount = ivArgTypes.length;
					InstructionList loading = null;
					if (activateArgCount == 1)
						loading = pruneLoading (il, ihs, actInstruction,
												ivArgTypes[0].getSize(), cpg,
												targetLost, delim, false);
					changedArea = enhanceActivateCall(factory, cpg, loading, iv);
					if (activateArgCount == 1) {
						replacedInstructions.add(new IHPair(delim[0],
															changedArea.getStart()));
						replacedInstructions.add(new IHPair(delim[1],
															changedArea.getEnd()));
					}
                } else*/ { // base call:
                    int            stackDepth = computeArgumentStackDepth(cpg, iv);
                    boolean deleteThis = true;
                   
                    if(enhancedMethod.isStatic())
                    	deleteThis = false;
                    
                    /*
                    if (iv.getOpcode()==Constants.INVOKESTATIC) 
                    	deleteThis = false; 
                    */
                    InstructionList   loading = pruneLoading(il, ihs, actInstruction,
															 stackDepth, cpg,
															 targetLost, delim, /*true*/deleteThis);
					//System.err.println(loading);
					if (loading == null) {
						actInstruction++;
						continue;
					}

					// insert call of base-call surrogate method:
					String roleInterfaceName = genRoleInterfaceName(cg.getClassName());
					
					String calleeClassName = null;
					if(m.isStatic()) {
						calleeClassName = extractTeamName(roleInterfaceName);
					}
					changedArea = genBaseCallSurrogateCall(cpg, iv, enhancedMethod, loading, extractRoleName(roleInterfaceName), calleeClassName);

					if (returnValueAdded) {
						changedArea.append(InstructionFactory.createStore(enhancedMethodReturnType,
																		 otResult.getIndex()));				
                	} else {
						InstructionHandle ih = adjustValue(changedArea, changedArea.getEnd(), enhancedMethodReturnType, returnType);
						if (debugging && ih != null)
							mg.addLineNumber(ih, STEP_OVER_LINENUMBER);
                	}
                    replacedInstructions.add(new IHPair(delim[0], changedArea.getStart()));
                    replacedInstructions.add(new IHPair(delim[1], changedArea.getEnd()));

				} // if (activate or base call)

                il.insert(next, changedArea);


/*************************** "return" enhancements: ************/
			} else if (act_instruction instanceof ReturnInstruction) {
				// replace return statement by result preparation and a new return statement
				// construct back to front, to keep the insertion position!
				InstructionHandle oldReturn    = ihs[actInstruction];
				InstructionHandle replacedPos  = oldReturn;
				il.append(oldReturn, InstructionFactory.createReturn(enhancedMethodReturnType));
				if (returnValueAdded) {
					// load ot_result:
					oldReturn.setInstruction(InstructionFactory.createLoad(enhancedMethodReturnType,
																otResult.getIndex()));
				} else {
					oldReturn.setInstruction(new NOP());
					replacedPos =
						adjustValue(il, oldReturn, returnType, enhancedMethodReturnType);
					if (debugging && replacedPos != null)
						mg.addLineNumber(replacedPos, STEP_OVER_LINENUMBER);
				}
				if (replacedPos != null)
					replacedInstructions.add(new IHPair(oldReturn, replacedPos));
            } // conditional over instruction types
			actInstruction++;
        } //end while

		// tidy:
		checkUpdate(handlers, replacedInstructions, targetLost);
        enhancedMethod.removeNOPs();
		il.setPositions();
        enhancedMethod.setMaxStack();
        enhancedMethod.setMaxLocals();

        return enhancedMethod;
    }

    /**
     * Given an invokevirtual compute the space its arguments use on the stack.
     * @param cpg
     * @param iv
     * @return int stack size.
     */
    static int computeArgumentStackDepth(ConstantPoolGen cpg, InvokeInstruction ii) {
        Type [] iiargs = ii.getArgumentTypes(cpg);
        int depth=0;
        for (int i=0; i<iiargs.length; i++)
            depth += iiargs[i].getSize();
        return depth;
    }

	/**
	 *  Copy all local variables from <tt>src</tt> to <tt>dest</tt>.
	 *  While doing so, increment their index by EXTRA_ARGS.
	 */
	static void copyLocalVariables(MethodGen src, MethodGen dest) {
		Type[] argumentTypes = src.getArgumentTypes();
		LocalVariableGen[] lvgs = src.getLocalVariables();
		for (int l=argumentTypes.length; l<lvgs.length; l++) {
			LocalVariableGen lvg = lvgs[l];
			if (lvg.getIndex() > 0) {
				dest.addLocalVariable(lvg.getName(),
									  			   lvg.getType(),
									  			   lvg.getIndex()+(EXTRA_ARGS+1), // +1?????
									  			   null, null);
				//System.err.println("adding:" +src.getClassName() +" "+src.getName()+" "+lvg.getName() +" " + lvg.getType() +" "+ (lvg.getIndex()+(EXTRA_ARGS+1)));
			}	
		}
	}
	/** Copy all line numbers from <tt>src</tt> to <tt>dest</tt>. */
	static void copyLineNumbers(MethodGen src, MethodGen dest) {
		InstructionList il_dest = dest.getInstructionList();
		il_dest.setPositions();
		LineNumberGen[] src_lng = src.getLineNumbers();
		for (int i=0; i<src_lng.length; i++) {
			int position = src_lng[i].getInstruction().getPosition();
			InstructionHandle ih = il_dest.findHandle(position);
			dest.addLineNumber(ih, src_lng[i].getSourceLine());
		}
	}
   /**
     * Prune a invokevirtual portion from a given instruction list.
     * Note, that arguments don't include 'this', which is not pruned but blanked
     * (need to keep as possible jump target).
     * @param il the source list
     * @param ihs array of handles of this list
     * @param idx points to a invokevirtual that shall be removed
     * @param stackDepth size of the called method's arguments on the stack.
	 *                   This is how deep we need to cut into the stack.
     * @param cpg
     * @param targetLost set of lost InstructionHandles to be filled
     * @param delim array of two handles, which should be filled with start and end of
	 *              the pruned region.
	 * @param blankThis should the 'this' call target be overwritten?
     * @return InstructionList a copy of the original value loading.
     */
    static InstructionList pruneLoading (InstructionList il, InstructionHandle[] ihs, int idx,
								  int stackDepth, ConstantPoolGen cpg,
								  HashSet<InstructionHandle> targetLost, InstructionHandle[] delim,
								  boolean blankThis)
    {
        InstructionList nlist = new InstructionList();
        InstructionHandle start = ihs[idx];
        InstructionHandle end = ihs[idx--];
        while (stackDepth > 0) {
            start = ihs[idx--];
            Instruction instr = start.getInstruction();
            stackDepth -= stackDiff(instr, cpg);
            nlist.insert(instr);
        }
		if (blankThis) {
			if (!isALoad0(ihs[idx].getInstruction()))
				return null;
			ihs[idx].setInstruction(new NOP()); // keep as jump target but delete 'this'
		}
        delim[0] = start;
        delim[1] = end;
        safeDelete(il, start, end, targetLost);
		return nlist;
    }

	static boolean isALoad0(Instruction i) {
		if (!(i instanceof ALOAD)) return false;
		return ((ALOAD)i).getIndex() == 0;
	}

	/** Get the lenght of the longest base method signature in mbs.
	 *  @param mbs List of {@link MethodBinding MethodBinding}
	 */
//	static int getMaxBaseArgLen (List mbs) {
//		int max=0;
//		Iterator it = mbs.iterator();
//		while (it.hasNext()) {
//			MethodBinding mb = (MethodBinding)it.next();
//			String sign = mb.getBaseMethodSignature();
//			int len = Type.getArgumentTypes(sign).length;
//			if (len>max) max = len;
//		}
//		return max;
//	}

	/**
     * Generate a dispatching switch statement which calls the proper base method.
	 * @param cpg
     * @param mbs list of MethodBinding that applies to this callin method
     * @param enhancedMethod the enhanced callin method
	 * @param roleArgumentTypes arg types of the callin method
     * @param outerClass the Team
     * @param baseClass the base bound to this role
     * @param returnType the return type of the original callin method
     * @param otResult the local variable storing the base call result
     * @param loading an instruction list holding the original instructions for
	 *        loading parameters
     * @return InstructionList the complete replacement implementing the base call.
     */
	InstructionList genBaseCallSwitch (ConstantPoolGen cpg,
									   List<MethodBinding> mbs, MethodGen enhancedMethod,
									   Type[] roleArgumentTypes,
									   ObjectType outerClass, ObjectType baseClass,
                                       Type returnType, LocalVariableGen otResult,
                                       InstructionList loading,
                                       boolean isSuperAccess)
    {
    	
        String  className                = enhancedMethod.getClassName();
        Type    enhancedMethodReturnType = enhancedMethod.getReturnType();
		boolean callinHasReturnValue     = returnType != Type.VOID;

		InstructionList il = new InstructionList();

		// Setup a variable which holds the result of this base call.
		// This variabel is local to this segment of code and used only
		// to transport this result out off the switch statement.
		int     localResult = -1;
		LocalVariableGen lg = null;
		if (callinHasReturnValue) {
			lg = enhancedMethod.addLocalVariable("_OT$tmpResult", returnType,
												 null, null);
			localResult = lg.getIndex();
			il.append(InstructionFactory.createNull (returnType));
			il.append(InstructionFactory.createStore(returnType, localResult));
		}
		
				// ---- Prepare the switch: ----
        InstructionHandle switchStart = il.append
			(InstructionFactory.createLoad(Type.INT, BASE_METH_ARG));
		// generated: _OT$baseMethTag
		
        removeDuplicatedBaseMethodTags(mbs);
        
        // one break for each case clause
        int numberOfCases = mbs.size();
        GOTO[] breaks = new GOTO[numberOfCases];
        for (int i=0; i<numberOfCases; i++)
            breaks[i] = new GOTO(null);

        int[]               matches = new int[numberOfCases];
        InstructionHandle[] targets = new InstructionHandle[numberOfCases];
		int caseCounter = 0;

		Iterator<MethodBinding> it = mbs.iterator();
		while (it.hasNext()) {

			MethodBinding mb = it.next();

			String wrapperName  = mb.getWrapperName();
			int[] paramPositions = CallinBindingManager.getParamPositions(outerClass.getClassName(),
																	wrapperName);
            if (logging) printLogMessage("param pos(" + wrapperName + ")=" + paramPositions);
			
            matches[caseCounter] = CallinBindingManager.getBaseCallTag(mb.getBaseClassName(),
            														   mb.getBaseMethodName(),
            														   mb.getBaseMethodSignature());
            InstructionHandle nextBranch = il.append(new NOP());

			String baseMethodName            = mb.getBaseMethodName();
			String baseMethodSignature       = mb.getBaseMethodSignature();
			Type[] baseMethodArgumentTypes   = Type.getArgumentTypes(baseMethodSignature);
			Type   baseMethodReturnType      = Type.getReturnType   (baseMethodSignature);
			
			String baseChainMethodName;
			Type   baseChainReturnType;
			Type[] enhancedBaseArgumentTypes;
			if (isSuperAccess) {
				baseChainMethodName 		 = OT_PREFIX+baseMethodName+"$super";
				baseChainReturnType          = returnType;
				// base arguments are un-enhanced but have a leading base instance:
				int len = baseMethodArgumentTypes.length;
				System.arraycopy(baseMethodArgumentTypes, 0, enhancedBaseArgumentTypes=new Type[len+1], 1, len);  
				enhancedBaseArgumentTypes[0] = baseClass; 
			} else {
				baseChainMethodName          = genChainMethName(baseMethodName);
				baseChainReturnType          = object; // ALWAYS
				enhancedBaseArgumentTypes    = enhanceArgumentTypes(baseMethodArgumentTypes);
			}
			
			// --- call target: ---

			// if base class type is a role type _OT$base field has role interface type:
			{
				String baseClassName = baseClass.toString();
				if (baseClassName.indexOf(OTDT_PREFIX) != -1) {
					baseClass = new ObjectType(ObjectTeamsTransformation.genRoleInterfaceName(baseClassName));
					if(logging) printLogMessage(baseClassName + " --> " + ObjectTeamsTransformation.genRoleInterfaceName(baseClassName));
				}
			}

			// load '_OT$base' field:
			InstructionHandle baseCallLine = il.append(InstructionFactory.createThis());
			il.append(factory.createFieldAccess(className, BASE, baseClass, Constants.GETFIELD));
			
			if (!baseClass.getClassName().equals(mb.getBaseClassName())) {
				// playedBy has been refined in the sub role;
				// create a cast to the sub base class:
				il.append(factory.createCast(baseClass, new ObjectType(mb.getBaseClassName())));
			}
			
			// --- load arguments of the new method: ---
			//     (letters refer to document parameter-passing.odg)
			
			// (u) generate extra arguments (indices are equal at role and base):
			if (!isSuperAccess)
				for (int idx = 0; idx < EXTRA_ARGS; idx++) 
					il.append(InstructionFactory.createLoad(enhancedMethod.getArgumentTypes()[idx], 
													        idx+1/*translating non-static*/));		
			
			// (v)(w)(x) split loading sequence and transfer source-level arguments
			// (includes reverse-application of parameter mappings):

			// Start at EXTRA_ARGS, because one set of enhancement has already been loaded,
			// except when doing super access which only has one extra arg: base instance.
			int start = isSuperAccess ? 1 : EXTRA_ARGS; 
			il.append(translateLoads(splitLoading(cpg,
											      loading.copy(),
												  roleArgumentTypes), 
									 enhancedMethod.getArgumentTypes(), 
									 enhancedBaseArgumentTypes, 
									 paramPositions,
									 extractTeamName(enhancedMethod.getClassName()), 
									 className,
									 new BaseMethodInfo(mb.baseMethodIsCallin(), false/*static*/, mb.getTranslationFlags()),
									 start,
									 cpg));
			// --- done loading ---									 

			// invoke the chaining method of the base class (base-call!):
			il.append(factory.createInvoke(mb.getBaseClassName(),
										   baseChainMethodName,
										   baseChainReturnType,
										   enhancedBaseArgumentTypes,
										   isSuperAccess 
											   ? Constants.INVOKESTATIC
											   : Constants.INVOKEVIRTUAL
										   ));
										   
			boolean resultLiftingNecessary = ((mb.getTranslationFlags()&1)!=0);
			
			if (resultLiftingNecessary) { // call the static lift-method:
				// STATIC_PARTS_OK: in role: lift method call
//TODO: lift method args!
				String liftMethodName = mb.getLiftMethodName();
				Type liftMethodReturnType = Type.getReturnType(mb.getLiftMethodSignature());
				Type[] liftMethodArgs = Type.getArgumentTypes(mb.getLiftMethodSignature());
				
				il.append(factory.createCast(baseChainReturnType, baseMethodReturnType));
                il.append(InstructionFactory.createThis());
                
                int nestingDepth = countOccurrences(className, '$') -1;
                il.append(factory.createGetField(className, "this$" + nestingDepth, outerClass ));

                il.append(new SWAP()); // -> .., this$0, (BaseType)result
				//il.append(new ICONST(LIFT_ARG_RES));
				
				il.append(factory.createInvoke(outerClass.getClassName(),
											   liftMethodName,
											   liftMethodReturnType,
											   liftMethodArgs,
											   Constants.INVOKEVIRTUAL));
			}
			
			InstructionHandle afterBaseCallLine = il.append(new NOP());
			
			if (baseChainReturnType != Type.VOID) {
				// adjust the return value to the type expected by the WRAPPER:
				il.append(new DUP()); // keep for adjustment below
				if (!resultLiftingNecessary)
					adjustValue(il, null, baseChainReturnType, enhancedMethodReturnType);
				il.append(InstructionFactory.createStore(enhancedMethodReturnType,
						otResult.getIndex())); // store "globally"
				// this store is needed to tunnel unused results through the callin.

				// adjust the return value to the type expected by the ORIGINAL CALLIN:
				adjustValue(il, null,  baseChainReturnType, returnType);
				if (callinHasReturnValue)
					il.append(InstructionFactory.createStore(returnType, localResult)); // store "locally"
	 			    // this store is useful for callins which make use of the result.
			}  
		
            targets[caseCounter] = nextBranch;
            il.append(breaks[caseCounter]);
            // generated: break;

            caseCounter++;
            
            if (debugging) {
            	enhancedMethod.addLineNumber(baseCallLine, STEP_INTO_LINENUMBER);
            	enhancedMethod.addLineNumber(afterBaseCallLine, STEP_OVER_LINENUMBER);
            }
		}
		// Default case: throw an exception reporting the situation:
		// create: String msg = ("Unhandled base-call case!"+base_method_tag)
		InstructionList messagePush = new InstructionList();
		messagePush.append(factory.createNew(OTConstants.STRING_BUFFER_NAME));
		messagePush.append(new DUP());
		messagePush.append(factory.createInvoke(OTConstants.STRING_BUFFER_NAME, Constants.CONSTRUCTOR_NAME, Type.VOID, new Type[0], Constants.INVOKESPECIAL));
		messagePush.append(new PUSH(cpg, "Unhandled base-call case: "));
		messagePush.append(factory.createInvoke(OTConstants.STRING_BUFFER_NAME, "append", Type.STRINGBUFFER, new Type[]{Type.STRING}, Constants.INVOKEVIRTUAL));
		messagePush.append(InstructionFactory.createLoad(Type.INT, BASE_METH_ARG));
		messagePush.append(factory.createInvoke(OTConstants.STRING_BUFFER_NAME, "append", Type.STRINGBUFFER, new Type[]{Type.INT}, Constants.INVOKEVIRTUAL));
		messagePush.append(factory.createInvoke(OTConstants.STRING_BUFFER_NAME, "toString", Type.STRING, new Type[0], Constants.INVOKEVIRTUAL));
		// create: throw new OTREInternalError(msg)
		InstructionHandle defaultCase = createThrowInternalError(cpg, il, messagePush);
		
        InstructionHandle afterSwitch = il.append(new NOP()); // all breaks point here.

		il.append(switchStart, createLookupSwitch(matches, targets, breaks,
												  defaultCase, afterSwitch));

		// retrieve locally stored result:
		if (callinHasReturnValue) {
			il.append(InstructionFactory.createLoad(returnType, localResult));
			lg.setStart(il.getStart()); // restrict local variable to this segment.
			lg.setEnd(il.getEnd());
		}

        return il;
	}

	/**
	 * Removes duplicated method bindings with the same base call tag from the list, to avoid duplicated 
	 * cases in the base call surrogate-switch.
	 * @param mbs
	 */
	private static void removeDuplicatedBaseMethodTags(List<MethodBinding> mbs) {
		if (mbs.size() < 2) // nothing to remove
			return;
		
		MethodBinding[] mbArray = mbs.toArray(new MethodBinding[mbs.size()]);
		
		Comparator<MethodBinding> baseCallTagComparator = new Comparator<MethodBinding>() {
			public int compare(MethodBinding firstMB, MethodBinding secondMB) {
				int firstBaseTag = CallinBindingManager.getBaseCallTag(firstMB.getBaseClassName(), 
																	   firstMB.getBaseMethodName(), 
																	   firstMB.getBaseMethodSignature());
				int secondBaseTag = CallinBindingManager.getBaseCallTag(secondMB.getBaseClassName(), 
						   											    secondMB.getBaseMethodName(), 
						   											    secondMB.getBaseMethodSignature());
				
				if (firstBaseTag < secondBaseTag)
					return -1;
				if (firstBaseTag > secondBaseTag)
					return 1;
				return 0;
			}
		};
		Arrays.sort(mbArray, baseCallTagComparator);
		for (int i = 0; i + 1 < mbArray.length; i++) {
			if (baseCallTagComparator.compare(mbArray[i], mbArray[i + 1]) == 0) {
				mbs.remove(mbArray[i + 1]);
			}
		}
	}


	/**
	 * @param className
	 * @return
	 */
	private static String extractTeamName(String roleClassName) {
		int lastDollarIndex = roleClassName.lastIndexOf('$');
		return roleClassName.substring(0, lastDollarIndex);
	}
	
	/** 
	 * @param className
	 * @return
	 */
	private static String extractRoleName(String roleClassName) {
		int lastDollarIndex = roleClassName.lastIndexOf('$');
		return roleClassName.substring(lastDollarIndex+1, roleClassName.length());
	}

	/**
	 * FIXME(SH): obsolete!
	 * @param baseMethodReturnType
	 * @param returnType
	 * @return
	 */
//	private static boolean returnTypeCompatible(Type from, Type to) {
//		System.out.println("test for " + from + "->" + to);
//		if (from.equals(to))
//			return true;
//		if (from instanceof ObjectType && to instanceof ObjectType) {// how to handle compatible basic types??
//			ObjectType otFrom = (ObjectType) from;
//			ObjectType otTo = (ObjectType) to;
//			if (otFrom.subclassOf(otTo))
//				return true;	
//		}
//		return false;
//	}

	/**
	 *	Copy all exception handlers of a method.
	 *  @param source the method from where to copy
	 *  @param dest the method where to copy to
	 *  @param il instructions of `dest' which must still have the same positions
	 *            as the instructions in `source'.
	 *  @return an array of handler generators, which still has to be maintained,
	 *     whenever instructions are replaced in the methods instruction list.
	 */
	static CodeExceptionGen[] copyExceptionHandlers(MethodGen source,
											 MethodGen dest,
											 InstructionList il) {
		il.setPositions(); // needed to retrieve handles by position.
		CodeExceptionGen[] excGens = source.getExceptionHandlers();
		CodeExceptionGen[] newGens = new CodeExceptionGen[excGens.length];
		if ((excGens != null) && excGens.length > 0) {
			for (int hcount=0; hcount<excGens.length; hcount++) {
				CodeExceptionGen excGen = excGens[hcount];
				InstructionHandle excStart   = il.findHandle(excGen.getStartPC().getPosition());
				InstructionHandle excEnd     = il.findHandle(excGen.getEndPC().getPosition());
				InstructionHandle excHandler = il.findHandle(excGen.getHandlerPC().getPosition());
				ObjectType catchType =  excGen.getCatchType();
				newGens[hcount] =
					dest.addExceptionHandler(excStart, excEnd, excHandler, catchType);
			}
		}
		return newGens;
	}

	/**
	 *  Update the positions of all exception handlers.
	 *  @param handlers the handlers of this method.
	 *  @param replaced a pair of InstructionHandles, the first is replaced by the second.
	 */
	static void updateHandlers (CodeExceptionGen[] handlers, IHPair replaced) {
		InstructionHandle old = replaced.fst();
		InstructionHandle neu = replaced.snd();
		for (int i=0; i<handlers.length; i++) {
            // System.out.println("handler "+handlers[i]);
			if (handlers[i].containsTarget(old) && (old != neu)) {
                // System.out.println("update "+old+"->"+neu);
				handlers[i].updateTarget(old, neu);
			}
		}
	}

	/**
	 * Delete a range of instructions, whithout throwing TargetLostException.
	 * @param il the list to delete from
	 * @param start handle to first instruction to delete.
	 * @param end handle to last instruction to delete.
	 * @param collect a set of InstructionHandle which are still targeted.
	 */
	static void safeDelete(InstructionList il,
					InstructionHandle start, InstructionHandle end,
					HashSet<InstructionHandle> collect)
	{
		try {
			il.delete(start, end);
		} catch(TargetLostException e) {
			//	System.out.print("Loosing:"+e+" ");
			InstructionHandle [] targets = e.getTargets();
			for (int tcount = 0; tcount < targets.length; tcount++) {
				collect.add(targets[tcount]);
				// System.out.println(targets[tcount]+"!!");
			}
		}
	}

	/**
	 *  Update all exceptions handlers with respect to all instructions that were replaced.
	 *  Check that this covers all instruction handles in 'lost'.
	 *  @param handles the exception handlers of this method.
	 *  @param replacedList list of IHPairs describing what has been modified.
	 *  @param lost set of instruction handles that were still referred to when
	 *    they were deleted. All these handles should be updated.
	 */
	static void checkUpdate(CodeExceptionGen[] handlers, ArrayList<IHPair> replacedList, HashSet<InstructionHandle> lost) {
        // System.out.println("Update "+replacedList+"/"+lost);
		Iterator<IHPair> iter = replacedList.iterator();
		while (iter.hasNext()) {
			IHPair replaced = iter.next();
			updateHandlers(handlers, replaced);
			lost.remove(replaced.fst());
		}
		if (!lost.isEmpty()) {
			System.err.println("Warning: "+lost.size()+" target(s) lost: ");
			Iterator<InstructionHandle> it = lost.iterator();
			while (it.hasNext())
				System.out.println(it.next());
		}
	}

	/**
	 * While adding extra args to an activate call:
	 * <ul>
	 *   <li>Check whether an activationLevel was passed (single argument)
	 *   <li>if so, insert the loading sequence for that expression.
	 *   <li>also the return differs (void or int), should however be
	 *      consistent between plain and enhanced version.
	 *   <li>decrement "idx" so this team is the currently active in the chain.
	 * </ul>
	 * @param factory
	 * @param cpg
	 * @param loading sequence, which loads "level" argument, else null.
	 * @param iv the invokevirtual for "activate", used to inspect the
	 *   original signature.
	 * @return the full sequence for loading the arguments, but not the
	 *   call target (because that's the enclosing team and is kept
	 *   unmodified in the original instruction list).
	 */
//    static InstructionList enhanceActivateCall (final InstructionFactory factory,
//										 ConstantPoolGen cpg,
//										 InstructionList loading,
//										 INVOKEVIRTUAL iv) {
//        InstructionList changedArea = new InstructionList();
//
//        // load arguments of the new method:
//        int index = 1;
//        Type[] enhancedArgumentTypes = enhanceArgumentTypes(iv.getArgumentTypes(cpg),
//															0, false, false);
//		Type returnType = Type.VOID;
//		int kount = enhancedArgumentTypes.length;
//		if (iv.getArgumentTypes(cpg).length == 1) {
//			kount--; // "level" loaded separately via 'loading'
//			returnType = Type.INT;
//		}
//
//        for (int k=0; k<kount; k++) {
//            changedArea.append(InstructionFactory.createLoad(enhancedArgumentTypes[k], index));
//            index += enhancedArgumentTypes[k].getSize();
//        }
//		if (loading != null)
//			changedArea.append(loading);
//
//        // invoke the overloaded activate method:
//        changedArea.append(factory.createInvoke("org.objectteams.Team",
//                                                "activate",
//                                                returnType,
//                                                enhancedArgumentTypes,
//                                                Constants.INVOKEVIRTUAL));
//
//        // generate: idx--;
//        changedArea.append(InstructionFactory.createLoad(Type.INT, IDX_ARG));
//        changedArea.append(new ICONST(1));
//        changedArea.append(new ISUB());
//        changedArea.append(InstructionFactory.createStore(Type.INT, IDX_ARG));
//        
//        return changedArea;
//    }

	/**
	 * Generates the instructions to call the enhanced version of the 'super' respectively 'tsuper'
	 * call in a role method.
	 * @param cpg						the constant pool
	 * @param ii							the original invoke instruction
	 * @param enhancedMethod	the enhanced role method
	 * @param loading					the originally loaded arguments 
	 * @return	the instruction list containing the method call ingredients							
	 */
	InstructionList genEnhancedSuperCall(ConstantPoolGen cpg, InvokeInstruction ii,
										   MethodGen enhancedMethod, InstructionList loading)
	{
		Type returnType = enhancedMethod.getReturnType();
		//Type[] argTypes = enhancedMethod.getArgumentTypes();
		Type[] argTypes = enhanceArgumentTypes(ii.getArgumentTypes(cpg));
		InstructionList il = new InstructionList();

		il.append(InstructionFactory.createThis());
		// load all additional arguments of the enhanced method (first EXTRA_ARGS):
		int index = 1;
		for (int i=0; i<EXTRA_ARGS; i++) {
			il.append(InstructionFactory.createLoad(argTypes[i],index));
			index += argTypes[i].getSize();
		}
		// load arguments of the originally call:
		il.append(loading);

		// call super.<enhancedMethod>:
		short kind=0;
		if (ii instanceof INVOKESPECIAL)
			kind = Constants.INVOKESPECIAL;
		else
			kind = Constants.INVOKEVIRTUAL;

		il.append(factory.createInvoke(ii.getClassName(cpg),
													 	ii.getMethodName(cpg),
												  	 	returnType,
														argTypes,
													 	kind));
			return il;
	}

	/**
	 * Generates the instructions to call the base call surrogate method.
	 * @param cpg						the constant pool
	 * @param iv							the original invoke instruction
	 * @param enhancedMethod	the enhanced role method
	 * @param loading					the originally loaded arguments 
	 * @return	the instruction list containing the method call ingredients		
	 */
	InstructionList genBaseCallSurrogateCall(ConstantPoolGen cpg, InvokeInstruction/*INVOKEVIRTUAL*/ iv,
											   MethodGen enhancedMethod, InstructionList loading, 
											   String roleClassName, String calleeClassName) //JU: added String roleClassName and teamClassName to the method signature
	{
		int indexOffset = enhancedMethod.isStatic()?-1:0; // argument indexes are decremented for static methods, 
																								// because of the missing 'this' 
		// for static methods callee is 'null' -> substitute by current class (JU)
		if(calleeClassName == null) {
			calleeClassName = iv.getClassName(cpg);
		}
		
		Type returnType = enhancedMethod.getReturnType();
		//Type[] argTypes = enhancedMethod.getArgumentTypes();
		Type[] argTypes = enhanceArgumentTypes(iv.getArgumentTypes(cpg));
		InstructionList il = new InstructionList();

		String methodName = getBaseCallSurrogateName(enhancedMethod.getName(), 
													 enhancedMethod.isStatic(),
													 roleClassName);
		short invokeKind;
				
		if (!enhancedMethod.isStatic()) {
			il.append(InstructionFactory.createThis());
			invokeKind = Constants.INVOKEVIRTUAL;
		
		} else { // role method is static: 
			invokeKind = Constants.INVOKESTATIC;
		}
			
		// load all additional arguments of the enhanced method (first EXTRA_ARGS):
		int index = 1;
		for (int i = 0; i < argTypes.length; i++) {
			if(i < EXTRA_ARGS){ // skip original arguments
				il.append(InstructionFactory.createLoad(argTypes[i], index+indexOffset));
			}
			//calculate the next index
			index += argTypes[i].getSize();
		}
		// load arguments of the originally call:
		il.append(loading);
			
		// call _OT$<enhancedMethod>$base():
		il.append(factory.createInvoke(calleeClassName,
													 methodName,
													 returnType,
													 argTypes,
													 invokeKind/*Constants.INVOKEVIRTUAL*/));
		return il;
	}
 
 		
	/**
	   * Checks, if a given instuction is a super or a tsuper call 
	   * @param instr					the instruction to check
	   * @param method_name	the name of the method from wich the 'inst' came
	   * @param cpg					the constant pool
	   * @return							true, if 'inst' is a super or tsuper call
	   */
	private static boolean super_or_tsuper_instruction(Instruction instr, String method_name, ConstantPoolGen cpg) {
		if (isTSuperCall(instr, method_name, cpg))
			return true;
		if (isSuperCall(instr, method_name, cpg))
			return true;
		return false;
	}   

	private static boolean isTSuperCall(Instruction instr, String method_name, ConstantPoolGen cpg) {
		if (instr instanceof INVOKEVIRTUAL) {
			INVOKEVIRTUAL iv = (INVOKEVIRTUAL)instr;
			String iv_name = iv.getName(cpg);
			Type[] argTypes = iv.getArgumentTypes(cpg);
			if (argTypes.length<1) // no tsuper marker interface parameter!
				return false;
			String lastArgument = (argTypes[argTypes.length-1]).toString();
			if (iv_name.equals(method_name) && (lastArgument.indexOf(TSUPER_PREFIX)!=-1)) {
				// if iv == <method_name>(..., TSuper__OT__<SuperTeamName>) -->
                if(logging) printLogMessage("tsuper-call to " + iv_name + " has to be enhanced!");
				return true;
			}
		}
		return false;
	}

	private static boolean isSuperCall(Instruction instr, String method_name, ConstantPoolGen cpg) {
		if (instr instanceof INVOKESPECIAL) { 			
			INVOKESPECIAL is = (INVOKESPECIAL)instr;
			String is_name = is.getName(cpg);
			
			if(is_name.equals(method_name)) {
               if(logging) printLogMessage("super-call to " + is_name + " has to be enhanced!");
			   return true;
			}
		}
		return false;
	}
	  
   /**
	* Generates the base call surrogate name for a given method name.
	* @param	method_name the name of the role method
	* @param	staticFlag 
	* @param	roleClassName the name of the role method
	* @return	the base call surrogate name
	*/
	private static String getBaseCallSurrogateName(String method_name, boolean staticFlag, String roleClassName) {
		//JU: for static methods the role class name should be inserted 
		if(staticFlag) {
			return OT_PREFIX+roleClassName+"$"+method_name+"$base";
		}
		return OT_PREFIX+method_name+"$base";
	}
		
   /**
	* Reverts a method name to its original by returning the substring after the last '$'
	* until the end.  
	* @param	method_name method name to be adjusted
	* @return	the original method name
	*/
	private static String revertToOriginalName(String method_name) {
		int p = method_name.lastIndexOf('$');
		return method_name.substring(p + 1);
	}

/*
 * (non-Javadoc)
 * 
 * @see org.eclipse.objectteams.otre.common.ObjectTeamsTransformation#doTransformCode(de.fub.bytecode.generic.ClassGen)
 */
    public void doTransformCode(ClassGen cg) {
        // nothing to do
    }
}
