/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamInterfaceImplementation.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.*;

import java.util.*;

import org.eclipse.objectteams.otre.util.*;

/**
 * Adds the general Team infrastructure to team classes.
 * 
 * Fields: public (static) final_OT$ID <- static if 'ot.no_static' ist set
 * Methods: public int activate(int level) public int deactivate(int level)
 * public int _OT$getID() Static initialization (adds clinit method, if not yet
 * presented) OR to every constructor: <- if 'ot.no_static' is set _OT$ID =
 * TeamIdDispenser.getTeamId(class_name)
 * 
 * Enables implicit team activation for normal (user defined) public team-level
 * methods. A call to 'activate' is woven at the beginning of the method. A call
 * to 'deactivate' is woven before every return and at every thrown exception.
 * 
 * @version $Id: TeamInterfaceImplementation.java 23408 2010-02-03 18:07:35Z stephan $
 * @author Christine Hundt
 * @author Stephan Herrmann
 */
public class TeamInterfaceImplementation 
	extends ObjectTeamsTransformation {


	public TeamInterfaceImplementation(ClassLoader loader, SharedState state) {
		super(loader, state);
	}

	/**
	 * @param cg
	 */
	public void doTransformCode(ClassGen cg) {
		factory = new InstructionFactory(cg);

		if (!classNeedsTeamExtensions(cg)) {
			return;
		}

        ConstantPoolGen cpg = cg.getConstantPool();
        String class_name = cg.getClassName();
		genImplicitActivation(cg, cpg);
		
		InstructionList implicitSuperRoleRegistrations = genImplicitSuperRegistration(cg, cpg);
		if (!implicitSuperRoleRegistrations.isEmpty()) {
			// add implicitSuperRoleRegistrations to the static initializer:
			Method clinitMethod = cg.containsMethod(Constants.STATIC_INITIALIZER_NAME, "()V");
			addToMethodStart(implicitSuperRoleRegistrations, clinitMethod, cg, cpg);
		}

		if (CallinBindingManager.getBasesPerTeam(class_name) == null) {
			return; // this team does not adapt any base class
		}

		/**
		 * ******************* add initialization of the field '_OT$ID' to the static initializer : ******
		 */
		int nextTeamId = TeamIdDispenser.getTeamId(class_name);
		
		/*
		 * The clinit method always exists at this moment, because it has
		 * been added by the interface transformer part of this transformer
		 * if necessary.
		 */
		addStaticInitializations(nextTeamId, cg, cpg);
	}

	/**
	 * Add initialization of "_OT$ID" with 'nextTeamID' to the static
	 * initializer of this team class.
	 * 
	 * @param nextTeamId
	 *            the id which will be associated with the currently transformed
	 *            team
	 * @param cg
	 *            the ClassGen for the given team class
	 * @param cpg
	 *            the constant pool ot the team
	 */
	private void addStaticInitializations(int nextTeamId, ClassGen cg,
			ConstantPoolGen cpg) {
		Method clinitMethod = cg.containsMethod(
				Constants.STATIC_INITIALIZER_NAME, "()V");
		MethodGen mg = newMethodGen(clinitMethod, cg.getClassName(), cpg);
		InstructionList il = mg.getInstructionList();

		InstructionList addedInitialization = new InstructionList();
		addedInitialization.append(createIntegerPush(cpg, nextTeamId));
		addedInitialization.append(factory.createFieldAccess(cg.getClassName(),
				"_OT$ID", Type.INT, Constants.PUTSTATIC));
		// generated: _OT$ID = <id dispensed by TeamIdDispenser>;
		
		// add static initialization for added static field at the beginning of
		// the <clinit> method:
		il.insert(addedInitialization);
		mg.setMaxStack();
		mg.setMaxLocals();

		Method newClinit = mg.getMethod();
		cg.replaceMethod(clinitMethod, newClinit);

		il.dispose(); // Reuse instruction handles
	}
	
	/**
	 * For all roles in the current team which have tsuper versions: 
	 * Generate instruction list containig calls to registration methods 
	 * the implicit super roles. 
	 *
	 * @param cg	The ClassGen of the current team.
	 * @param cpg	The constant pool of the current team.
	 * @return		The instruction list containing the registration calls.
	 */
	private InstructionList genImplicitSuperRegistration(ClassGen cg, ConstantPoolGen cpg) {
		InstructionList il = new InstructionList();
		List<String> inheritedRoleNames = getInheritedRoleNames(cg, cpg);
			if (inheritedRoleNames.isEmpty())
				return il; // nothing to do
			Iterator<String> iter = inheritedRoleNames.iterator();
			while (iter.hasNext()) {
				String roleName = iter.next();
				String unqualifiedRoleName = roleName.substring(roleName.lastIndexOf('$') + 1);
				if (CallinBindingManager.isBoundBaseAndRoleClass(roleName)) {
					String potientialImplicitSuperRoleName = cg.getSuperclassName() + '$' + unqualifiedRoleName;
					
 					if (!CallinBindingManager.isBoundBaseAndRoleClass(potientialImplicitSuperRoleName)) {
						// Only bound base classes have the infrastructure for notifying 
						// implicit subclasses about team activation!
						continue;
					}
 					il.append(new PUSH(cpg, roleName));
					il.append(factory.createInvoke("java.lang.Class", "forName", 
												   classType, new Type[] { Type.STRING },
												   Constants.INVOKESTATIC));
					il.append(factory.createInvoke(potientialImplicitSuperRoleName, "_OT$registerObserver", 
												   Type.VOID, new Type [] { classType },
												   Constants.INVOKESTATIC));
				}
			}
		return il;
	}

	public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
		String class_name = cg.getClassName();
		ConstantPoolGen cpg = cg.getConstantPool();

		checkReadClassAttributes(ce, cg, class_name, cpg);

		if (state.interfaceTransformedClasses.contains(class_name)) {
			return; // class has already been transformed by this transformer
		}

		if (!classNeedsTeamExtensions(cg)) {
			return;
		}
		
		factory = new InstructionFactory(cg);
		
		/**
		 * ********** empty implementation of the static class initialization method '<clinit>' ***
		 * NOTE: this is unnecessary in some cases, but checking is too complicated
		 */
		addStaticInitializer(cg, cpg, class_name, ce);

		List<String> handledBases = CallinBindingManager.getBasesPerTeam(class_name);
		// TeamInterfaceImplementer only registers teams at bases, wich are part
		// of a 'CallinRoleBaseBinding'-attribute of the team.
		if (handledBases == null) {
			return; // this team does not adapt any base class
		}
		
		if(logging) printLogMessage("Adding the general Team infrastructure to "
				+ class_name);

		/**
		 * ******************* addition of the field '_OT$ID'
		 * **********************************
		 */

		int accessFlags = Constants.ACC_PUBLIC | Constants.ACC_FINAL | Constants.ACC_STATIC;

		FieldGen IDField = new FieldGen(accessFlags, Type.INT, "_OT$ID", cpg);
		ce.addField(IDField.getField(), cg);
		// generated global variable: public final static int _OT$ID;

		factory = new InstructionFactory(cpg);
		InstructionList il;

		/**
		 * ******************* implementation of method '_OT$getID'
		 * ************************
		 */

		il = new InstructionList();
		MethodGen getIDMethod = new MethodGen(Constants.ACC_PUBLIC, Type.INT,
				Type.NO_ARGS, new String[] {}, "_OT$getID", class_name, il, cpg);

		il.append(factory.createFieldAccess(class_name, "_OT$ID", Type.INT,
				Constants.GETSTATIC));
		il.append(InstructionFactory.createReturn(Type.INT));

		getIDMethod.setMaxStack();
		getIDMethod.setMaxLocals();

		ce.addMethod(getIDMethod.getMethod(), cg);

		/**
		 * ***************** implementation of team (un)registration methods
		 * **********************
		 */

		ce.addMethod(generateTeamRegistrationMethod(cpg, class_name,
				handledBases), cg);

		ce.addMethod(generateTeamUnregistrationMethod(cpg, class_name,
				handledBases), cg);

		/**
		 * ***************** implementation of base call surrogtes for static methods
		 * **********************
		 */
		Method [] base_call_surrogates = generateStaticBaseCallSurrogates(class_name, cpg, cg);
		for(int i=0; i<base_call_surrogates.length;i++) {
			// perhaps the compiler already generated an empty surrogate for an unbound super-role?
			// (see X.1.5-otjld-callin-from-static-base-method-12a)
			ce.addOrReplaceMethod(base_call_surrogates[i], cg);
		}
		
		/** *************************************************************************** */

		il.dispose();
		state.interfaceTransformedClasses.add(class_name);
	}
	
	/**
	 * Add the given instruction list to the start of the givern method.
	 * @param additionalInstructions
	 * @param method
	 * @param cg
	 * @param cpg
	 */
	private void addToMethodStart(InstructionList additionalInstructions, Method method, ClassGen cg, ConstantPoolGen cpg) {
		MethodGen mg = newMethodGen(method, cg.getClassName(), cpg);
		InstructionList il = mg.getInstructionList();
		il.insert(additionalInstructions);
		mg.setMaxStack();
		mg.setMaxLocals();

		Method newMethod = mg.getMethod();
		cg.replaceMethod(method, newMethod);
		il.dispose(); // Reuse instruction handles	
	}

	/**
	 * Generate the static initializer method 'clinit'.
	 * @param cpg			The constant pool
	 * @param class_name	The name of the class
	 * @param cg			The ClassGen for the class
	 * @return				The static initialier for this class
	 */
	void addStaticInitializer(ClassGen cg, ConstantPoolGen cpg, String class_name, ClassEnhancer ce) {
		/*
		 * Adding static initializations requires the addition of the clinit
		 * method, if not yet presented. This requires synchronization with
		 * other transformers (TeamInterfaceImplementer) which may do the
		 * same this. This is done via 'TeamIdDispenser.clinitAdded(class_name)'.
		 */
		Method existingClinit = cg.containsMethod(Constants.STATIC_INITIALIZER_NAME, "()V");
		if (existingClinit == null &&  !TeamIdDispenser.clinitAdded(class_name, loader)) {
			// otherwise the clinit-Method already exists and only has to be extended
			// by the code transformation of this transformer
			InstructionList il = new InstructionList();
			MethodGen clinitMethodGen = new MethodGen(Constants.ACC_STATIC,
					Type.VOID, Type.NO_ARGS, new String[] {},
					Constants.STATIC_INITIALIZER_NAME, class_name, il, cpg);

			il.append(InstructionFactory.createReturn(Type.VOID));

			clinitMethodGen.setMaxStack();
			clinitMethodGen.setMaxLocals();
			Method clinitMethod = clinitMethodGen.getMethod();
			ce.addMethod(clinitMethod, cg);
		}
	}

	/**
	 * Generates the ' _OT$registerAtBases()' method. This method registers the
	 * team at every adapted base class by calling the respective 'addTeam'
	 * method.
	 * 
	 * @param cpg
	 *            The ConstantPoolGen of the team class.
	 * @param class_name
	 *            The name of the team class.
	 * @param handledBases
	 *            The list of teams adapted by (roles of) this team.
	 * @return The generated 'activate' method.
	 */
	Method generateTeamRegistrationMethod(ConstantPoolGen cpg,
			String class_name, List<String> handledBases)
	{
		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
				Type.NO_ARGS, null, "_OT$registerAtBases", class_name, il, cpg);

		Iterator<String> it = handledBases.iterator();
		while (it.hasNext()) {
			String actBase = it.next();
			//if (CallinBindingManager.hasBoundBaseParent(actBase))
			//	continue; // team was already added to a super base class
			//	problem: bound base parent could be bound to another team class!!!
			
			//String boundBase = CallinBindingManager.getBoundBaseParent(actBase);
			//if (boundBase != null && handledBases.contains(boundBase))
			//	continue;
			if (CallinBindingManager.teamAdaptsSuperBaseClass(class_name, actBase)) 
				continue;
			
			InstructionHandle startTry = il.append(new ALOAD(0));
			
			il.append(factory.createFieldAccess(class_name, "_OT$ID", Type.INT,
					Constants.GETSTATIC));
			// Note: the method _OT$addTeam may be found in a super classes (topmostBoundBase),
			//       but we'll simply leave this lookup to the VM 
			il.append(factory.createInvoke(actBase, "_OT$addTeam", Type.VOID,
					new Type[] { teamType, Type.INT }, Constants.INVOKESTATIC));
			// generated: <actBase>._OT$addTeam(this, _OT$ID);
			
			addNoSuchMethodErrorHandling(startTry, il.getEnd(), getErrorMessage(class_name, actBase, "Activation"), il, mg, cpg);
		}
		il.append(new RETURN());

		mg.setMaxStack();
		mg.setMaxLocals();
		return mg.getMethod();
	}

	/**
	 * Direct inverse of generateTeamRegistrationMethod(..). Generates the '
	 * _OT$unregisterFromBases' method. This method deregisters the team at
	 * every adapted base class by calling the respective 'removeTeam' method.
	 * 
	 * @param cpg
	 *            The ConstantPoolGen of the team class.
	 * @param class_name
	 *            The name of the team class.
	 * @param handledBases
	 *            The list of teams adapted by (roles of) this team.
	 * @return The generated 'deactivate' method.
	 */
	Method generateTeamUnregistrationMethod(ConstantPoolGen cpg,
			String class_name, List<String> handledBases) {

		InstructionList il = new InstructionList();
		MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
				Type.NO_ARGS, null, "_OT$unregisterFromBases", class_name, il,
				cpg);

		Iterator<String> it = handledBases.iterator();
		while (it.hasNext()) {
			String actBase = it.next();
			//if (CallinBindingManager.hasBoundBaseParent(actBase))
			//	continue; // team was only added to a super base class
			// problem: bound base parent could be bound to another team class!!!
			
			// String boundBase = CallinBindingManager.getBoundBaseParent(actBase);
			//if (boundBase != null && handledBases.contains(boundBase))
			//	continue;
			if (CallinBindingManager.teamAdaptsSuperBaseClass(class_name, actBase)) 
				continue;
			
			InstructionHandle startTry = il.append(new ALOAD(0));

			il.append(factory.createInvoke(actBase, "_OT$removeTeam",
					Type.VOID, new Type[] { teamType }, Constants.INVOKESTATIC));
			// generated: <actBase>._OT$removeTeam(this, _OT$ID);
			
			addNoSuchMethodErrorHandling(startTry, il.getEnd(), getErrorMessage(class_name, actBase, "Deactivation"), il, mg, cpg);
		}
		il.append(new RETURN());

		mg.setMaxStack();
		mg.setMaxLocals();
		return mg.getMethod();
	}

	private String getErrorMessage(String teamName, String baseName, String action) {
		String errorMessage = action+" of team '" + teamName + "' failed! Callins of this team have NOT been WOVEN into base class '" + baseName + "'!\n" 
		  + "This is probably caused by a loading order problem.";
		return errorMessage;
	}
	
	/**
	 * Adds an exception handler to the given method which catches 'java.lang.NoSuchMethodError's 
	 * caused by missing team registration methods in base classes. Causes the throwing of an 
	 * 'org.objectteams.UnsupportedFeatureException'.
	 * 
	 * @param startTry		handle to the start of the try block
	 * @param endTry		handle to the end of the try block
	 * @param errorMessage	the error message to be printed when throwing the exception
	 * @param il			instruction list of the method
	 * @param mg			MethodGen of the method
	 * @param cpg			corresponding ConstantPoolGen
	 */
	private void addNoSuchMethodErrorHandling(InstructionHandle startTry, InstructionHandle endTry, 
											  String errorMessage, InstructionList il, MethodGen mg, 
											  ConstantPoolGen cpg) {
		GOTO skipHdlr = null;
		skipHdlr = new GOTO(null);
		il.append(skipHdlr);
		// generated: goto normal exit
		
		// throw away the expection reference:
		InstructionHandle hdlr = il.append(new POP()); 
		
		il.append(factory.createNew(OTConstants.unsupportedFeature));
		il.append(new DUP());
		il.append(new PUSH(cpg, errorMessage));
		il.append(factory.createInvoke(OTConstants.unsupportedFeature.getClassName(),
													 Constants.CONSTRUCTOR_NAME,
													 Type.VOID,
													 new Type[] { Type.STRING }, 
													 Constants.INVOKESPECIAL));
		il.append(new ATHROW());
		
		InstructionHandle nop = il.append(new NOP());
		skipHdlr.setTarget(nop);
		mg.addExceptionHandler(startTry, endTry, hdlr, new ObjectType("java.lang.NoSuchMethodError"));
	}

	/**
	 * Generate implicit Team activation for each "normal" public method.
	 * 
	 * @param cg
	 *            class for which methods are to be augmented.
	 * @param cpg
	 *            the constant pool of the class 'cg'.
	 */
	void genImplicitActivation(ClassGen cg, ConstantPoolGen cpg) {
		Method[] methods = cg.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (candidateForImplicitActivation(m, cg, cpg)) {
                if(logging) printLogMessage("Adding implicit activation to " + m.getName());
				cg.replaceMethod(m, genImplicitActivation(m, cg.getClassName(), cpg, false)); 
			}
		}
	}
	
	/**
	 * Scans the team attribute StaticReplaceBinding and generates an array of base call surrogates
	 * 
	 * @param class_name
	 * @param cpg
	 * @param cg
	 * @return
	 */
	private Method [] generateStaticBaseCallSurrogates(String class_name, ConstantPoolGen cpg, ClassGen cg){
		
		Set<String> roleMethodKeys = new HashSet<String>();
		
		//------------------------------------------------------------------------------------------
		// scan static replace bindings attributes
		//------------------------------------------------------------------------------------------
		Attribute [] attributes = cg.getAttributes();
		for(int k=0; k<attributes.length; k++){
			
			Unknown attr = isOTAttribute(attributes[k]);
			if(attr == null) continue;
		
			if(attr.getName().equals("StaticReplaceBindings")) {
				
				byte[] indizes = attr.getBytes();
				int count = combineTwoBytes(indizes, 0);
	            int numberOfEntries=0;
				String [] names;
				numberOfEntries = 5;
				int i = 2;
				
				for (int n=0; n<count;n++) {
					names = new String[numberOfEntries];
					i = scanStrings(names, indizes, i, cpg);
					int index = 0;
					String role_name             = names[index++];
					String role_method_name      = names[index++];
					String role_method_signature = names[index++];
					String lift_method_name      = names[index++];
					String lift_method_signature = names[index++];
										
					String roleMethodKey = genRoleMethodKey(class_name, role_name, role_method_name, role_method_signature, lift_method_name, lift_method_signature);
					roleMethodKeys.add(roleMethodKey);
						
					int base_len = combineTwoBytes(indizes, i);
					BaseMethodInfo baseMethod;
					i += 2;
					names = new String[3];
					for (int n_base = 0; n_base < base_len; n_base++) {
						int [] positions = null;
						
						i = scanStrings(names, indizes, i, cpg);
						
						int flags = indizes[i++];
						boolean baseIsCallin     = (flags & 1) != 0;
						boolean baseIsRoleMethod = (flags & 2) != 0;
						boolean baseIsStatic     = (flags & 4) != 0;
						//parameter positions scanning
						int pos_len = combineTwoBytes(indizes, i);
                        i+=2;
                        
                        if(pos_len > 0) {
                        	positions = new int[pos_len];
                        }
                        
                        for(int pos = 0; pos < pos_len; pos++){
                        	positions[pos] = combineTwoBytes(indizes,i);
                        	i += 2;
                        }
                        int translationFlags = (combineTwoBytes(indizes, i)<<16) + combineTwoBytes(indizes, i+2);
                        i+=4;
						baseMethod = new BaseMethodInfo(names[0], names[1], names[2], 
														baseIsCallin, baseIsRoleMethod, baseIsStatic, 
														positions, translationFlags);
						
						CallinBindingManager.assignBaseCallTag(names[0],names[1],names[2]);
						CallinBindingManager.addStaticReplaceBindingForRoleMethod(roleMethodKey, baseMethod);
					}					
				}
				break; 
			}
		}
		
		//------------------------------------------------------------------------------------------
		// generate base call surrogates
		//------------------------------------------------------------------------------------------
		Iterator<String> roleMethodIter = roleMethodKeys.iterator();
		int count = roleMethodKeys.size();
		Method [] generatedSurrogates = new Method[count];
		
		int j = 0;
		
		while(roleMethodIter.hasNext()) {
			String roleMethodKey = roleMethodIter.next();
			LinkedList<BaseMethodInfo> baseMethods = CallinBindingManager.getStaticReplaceBindingsForRoleMethod(roleMethodKey);
			
			//split the key into team class name, role class name, role method name and role method signature
			int firstPointIndex = roleMethodKey.indexOf(STATIC_REPLACE_BINDING_SEPARATOR);
			int secondPointIndex = roleMethodKey.indexOf(STATIC_REPLACE_BINDING_SEPARATOR, firstPointIndex+1);
			int thirdPointIndex = roleMethodKey.indexOf(STATIC_REPLACE_BINDING_SEPARATOR, secondPointIndex+1);
			int fourthPointIndex = roleMethodKey.indexOf(STATIC_REPLACE_BINDING_SEPARATOR, thirdPointIndex+1);
			int fifthPointIndex = roleMethodKey.indexOf(STATIC_REPLACE_BINDING_SEPARATOR, fourthPointIndex+1);
			
			String role_name = roleMethodKey.substring(firstPointIndex+2, secondPointIndex);
			String role_method_name = roleMethodKey.substring(secondPointIndex+2, thirdPointIndex);
			String role_method_signature = roleMethodKey.substring(thirdPointIndex+2, fourthPointIndex);
			String lift_method_name = null;
			String lift_method_signature = null;
			// SH: without this check we get 
			//        StringIndexOutOfBoundsException: String index out of range: -1
			//     I hope this patch is correct..
			if (  fourthPointIndex + 2 <= fifthPointIndex
				&& fifthPointIndex + 2 < roleMethodKey.length()) 
			{
				lift_method_name = roleMethodKey.substring(fourthPointIndex+2, fifthPointIndex);
				lift_method_signature = roleMethodKey.substring(fifthPointIndex+2, roleMethodKey.length());
			}
			
			generatedSurrogates[j] = genBaseCallSurrogate(cg, role_name, role_method_name, 
														  role_method_signature, 
														  lift_method_name, lift_method_signature, baseMethods);
			j++;
		}
		
		return generatedSurrogates;
	}
	
	/**
	 * Generates a base call surrogate for a given static role method
	 * @param cg
	 * @param role_name		
	 * @param role_method_name
	 * @param role_method_signature
	 * @param lift_method_name
	 * @param lift_method_signature
	 * @param base_methods
	 * @return
	 */
	private Method genBaseCallSurrogate(ClassGen cg,
										String role_name, 
										String role_method_name,
										String role_method_signature,
										String lift_method_name,
										String lift_method_signature, 
										LinkedList<BaseMethodInfo> base_methods)
	{	
		ConstantPoolGen cpg = cg.getConstantPool();
		String class_name = cg.getClassName();

		if (base_methods.isEmpty()) { 
			return null;
		}
		Type[] enhancedArgumentTypes = enhanceArgumentTypes(Type.getArgumentTypes(role_method_signature));
		Type enhancedReturnType  = generalizeReturnType(Type.getReturnType(role_method_signature));
		String[] enhancedArgumentNames = null;
		InstructionList il = new InstructionList();
		int accessFlags = Constants.ACC_PROTECTED;
		
		MethodGen baseCallSurrogate = new MethodGen(accessFlags,
																enhancedReturnType,
																enhancedArgumentTypes,
																enhancedArgumentNames,
																getBaseCallSurrogateName(role_name, role_method_name), 
																class_name,
																il, cpg);
			
		
		LocalVariableGen otResult = null;
		
		/*
		 int slot = enhancedArgumentTypes.length+1;
		 otResult = baseCallSurrogate.addLocalVariable("_OT$result",
		 enhancedReturnType,
		 slot, null, null);
		 */
		otResult = baseCallSurrogate.addLocalVariable("_OT$result",
				enhancedReturnType, null, null);
		
		il.insert(InstructionFactory.createStore(enhancedReturnType,
				otResult.getIndex()));
		il.insert(new ACONST_NULL());
		il.setPositions(); // about to retrieve instruction handles.
		
		if(logging) printLogMessage("base-call switch has to be inserted!");
		InstructionList loading = new InstructionList();
		loading.append(InstructionFactory.createThis());
		int index = 1;
		for (int i = 0; i < enhancedArgumentTypes.length; i++) {
			loading.append(InstructionFactory.createLoad(enhancedArgumentTypes[i],index));
			index += enhancedArgumentTypes[i].getSize();
		}
		
		Type[] argumentTypes = Type.getArgumentTypes(role_method_signature);
		Type returnType = Type.getReturnType(role_method_signature);
		
		if (debugging) {
			baseCallSurrogate.addLineNumber(il.getStart(), STEP_OVER_LINENUMBER);
		}
		
		il.append(genBaseCallSwitch(cpg, base_methods, baseCallSurrogate,
				argumentTypes,
				returnType,
				lift_method_name,
				lift_method_signature,
				otResult, loading, cg.getClassName()));
		
		il.append(InstructionFactory.createLoad(enhancedReturnType, otResult.getIndex()));
		il.append(InstructionFactory.createReturn(enhancedReturnType));							
		
		il.setPositions();
		baseCallSurrogate.removeNOPs();
		baseCallSurrogate.setMaxStack();
		baseCallSurrogate.setMaxLocals();
		return baseCallSurrogate.getMethod();
	}
	
	private static String getBaseCallSurrogateName(String class_name, String method_name){
		// base call surrogate for static callin methods:
		// name contains role class name and role method name, 
		// because its generated into the team.
		return OT_PREFIX + class_name + "$" + method_name + "$base";		
	}
	
	/** 
     * Generate a dispatching switch statement which calls the proper base method.
	 * @param cpg
	 * @param base_methods list of BaseMethodInfo that applies to this callin method
	 * @param enhancedMethod the enhanced callin method
	 * @param argumentTypes arg types of the callin method
	 * @param returnType the return type of the original callin method
	 * @param liftMethodSignature
	 * @param otResult the local variable storing the base call result
	 * @param loading an instruction list holding the original instructions for
	 *        loading parameters
	 * @param teamName
	 * @param lift_method_name
	 * @return InstructionList the complete replacement implementing the base call.
     */
	InstructionList genBaseCallSwitch (ConstantPoolGen cpg,
									   LinkedList<BaseMethodInfo> base_methods, MethodGen enhancedMethod,
									   Type[] argumentTypes,
                                       Type returnType, String liftMethodName, String liftMethodSignature,
                                       LocalVariableGen otResult, InstructionList loading, String teamName)
    {
    	
		short invocationKind = Constants.INVOKESTATIC;
		
        String  className                = enhancedMethod.getClassName();
        Type    enhancedMethodReturnType = enhancedMethod.getReturnType();
		boolean callinHasReturnValue = returnType != Type.VOID;

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
        
        //base_methods may contain duplicates!
        //baseMethodTags is used to determine the number of cases without duplicates
        HashSet<Integer> baseMethodTags = new HashSet<Integer>();
        Iterator<BaseMethodInfo> iter = base_methods.iterator();
        while(iter.hasNext()) {
        	BaseMethodInfo baseMethod = iter.next();
        	//baseMethodTags.add(CallinBindingManager.getBaseCallTag( baseMethod.getBaseClassName(),
        	//														baseMethod.getBaseMethodName(),
        	//														baseMethod.getBaseMethodSignature()));
        	int baseMethodTag = CallinBindingManager.getBaseCallTag(baseMethod.getBaseClassName(),
        															baseMethod.getBaseMethodName(),
																	baseMethod.getBaseMethodSignature());
        	baseMethodTags.add(Integer.valueOf(baseMethodTag));
        }       
        
        // one break for each case clause
        int numberOfCases = baseMethodTags.size();
        
        GOTO[] breaks = new GOTO[numberOfCases];
        for (int i=0; i<numberOfCases; i++)
            breaks[i] = new GOTO(null);
        
        int[]               matches = new int[numberOfCases];
        InstructionHandle[] targets = new InstructionHandle[numberOfCases];
		int caseCounter = 0;

		//now baseMethodTags is used to store the handled tags
		baseMethodTags.clear();
		
		//JU:
		Type[] enhancedMethodArguments = enhancedMethod.getArgumentTypes();
		Type[] enhancedArgumentsForBaseCall = new Type[enhancedMethodArguments.length - 1];
		System.arraycopy(enhancedMethodArguments, 0,
                enhancedArgumentsForBaseCall, 0,
                enhancedArgumentsForBaseCall.length);
		
		Iterator<BaseMethodInfo> it = base_methods.iterator();
		while (it.hasNext()) {
			
			BaseMethodInfo baseMethod = it.next();
			String baseClassName = baseMethod.getBaseClassName();
			String baseMethodName = baseMethod.getBaseMethodName();
			String baseMethodSignature = baseMethod.getBaseMethodSignature();
			int base_method_tag = CallinBindingManager.getBaseCallTag(baseClassName, baseMethodName, baseMethodSignature);
			
			//if the current baseMethod is a dulpicate:
			// workaround for jdk 1.4:
			Integer bmt = Integer.valueOf(base_method_tag);
			//if(baseMethodTags.contains(base_method_tag)){
			if (baseMethodTags.contains(bmt)) {
				continue;
			}
			
			//baseMethodTags.add(base_method_tag);
			baseMethodTags.add(bmt);
		
			int [] parameterPositions = baseMethod.getParameterPositions();
			int len = Type.getArgumentTypes(baseMethodSignature).length;

			// if the base method is a callin method as well, further enhance the signature:
			if (baseMethod.isCallin) 
				len += EXTRA_ARGS;			


			matches[caseCounter] = CallinBindingManager.getBaseCallTag(baseClassName, baseMethodName, baseMethodSignature);
            InstructionHandle nextBranch = il.append(new NOP());

			Type[] baseMethodArgumentTypes   = Type.getArgumentTypes(baseMethodSignature);
			Type   baseMethodReturnType      = Type.getReturnType   (baseMethodSignature);
			String baseChainMethodName       = genChainMethName(baseMethodName);
			Type   baseChainReturnType       = object; // ALWAYS
			Type[] enhancedBaseArgumentTypes = enhanceArgumentTypes(baseMethodArgumentTypes);
			
			boolean resultLiftingNecessary   = false;

            // TODO (SH): if both types are ObjectType we should probably use subclassOf() ??
            // (don't lift if simple polymorphism suffices!)
            //TODO: if the base method return type is a subtype of the role method return type no lifting has to take place!! is this allowed??
			//if (!returnTypeCompatible(baseMethodReturnType, returnType) && callinHasReturnValue)
			if (/*!baseMethodReturnType.equals(object) &&*/ 
				   !baseMethodReturnType.equals(returnType) 
				&& !(baseMethodReturnType instanceof BasicType) // requires boxing not lifting
				&& !(returnType instanceof BasicType)			// requires unboxing not lifting
				&& callinHasReturnValue)
			{
				resultLiftingNecessary = true;
			}

			// --- load arguments of the new method: ---
			//     (letters refer to document parameter-passing.odg)
		
			// (u) generate extra arguments (indices are equal at role and base):
			for (int idx = 0; idx < EXTRA_ARGS; idx++) 
				il.append(InstructionFactory.createLoad(enhancedMethodArguments[idx], 
														idx+1)); // first arg is "this" (enclosing team)			
			
			// (v)(w)(x) split loading sequence and transfer source-level arguments
			// (includes reverse-application of parameter mappings):
			InstructionHandle baseCallLine = il.append(translateLoads(splitLoading(cpg,
																				   loading.copy(),
																				   argumentTypes), 
																	  enhancedMethodArguments, 
																	  enhancedBaseArgumentTypes, 
																	  parameterPositions, 
																	  teamName, 
																	  null,
																	  baseMethod,
																	  EXTRA_ARGS/*start*/,
																	  cpg));
			// --- done loading ---			

			// invoke the chaining method of the base class (base-call!):
			il.append(factory.createInvoke(baseClassName, 
										   baseChainMethodName,
										   baseChainReturnType,
										   enhancedBaseArgumentTypes,
										   invocationKind));

			// FIXME(SH): if this assert holds, remove computing of resultLiftingNecessary above.
			assert resultLiftingNecessary == ((baseMethod.translationFlags & 1) != 0);
			
			if (resultLiftingNecessary) { // call the lift-method: 
				Type[] liftMethodArgs = Type.getArgumentTypes(liftMethodSignature);
				Type liftMethodReturnType = Type.getReturnType(liftMethodSignature);

				// cast result of base call:
				il.append(factory.createCast(baseChainReturnType, baseMethodReturnType));

				// load the team instance at which to call the lift method:
				il.append(InstructionFactory.createThis());
				il.append(factory.createCast(object, new ObjectType(teamName)));
				
				// put them in correct order:
                il.append(new SWAP()); // -> .., this$0, (BaseType)result
				
				il.append(factory.createInvoke(teamName,
											   liftMethodName,
											   liftMethodReturnType,
											   liftMethodArgs,
											   Constants.INVOKEVIRTUAL));
			}

			// adjust the return value to the type expected by the WRAPPER:
			il.append(new DUP()); // keep for adjustment below
			if (!resultLiftingNecessary)
				adjustValue(il, null, baseChainReturnType, enhancedMethodReturnType);
			il.append(InstructionFactory.createStore(enhancedMethodReturnType,
					otResult.getIndex())); // store "globally"
			// this store is needed to tunnel unused results through the callin.
			
			InstructionHandle afterBaseCallLine = il.append(new NOP());
			
			// adjust the return value to the type expected by the ORIGINAL CALLIN:
			adjustValue(il, null,  baseChainReturnType, returnType);
			if (callinHasReturnValue) {
				il.append(InstructionFactory.createStore(returnType, localResult)); // store "locally"
			}	
 			    // this store is useful for callins which make use of the result.

            targets[caseCounter] = nextBranch;
            il.append(breaks[caseCounter]);
            // generated: break;

            caseCounter++;
            
            if (debugging) {
            	enhancedMethod.addLineNumber(baseCallLine, STEP_INTO_LINENUMBER);
            	enhancedMethod.addLineNumber(afterBaseCallLine, STEP_OVER_LINENUMBER);
            }
		}
        
        //JU: added the follwing part (begin) -----------------------------------------------------	
        InstructionHandle defaultBranch = il.append(new NOP());
        
        if (logging)
			printLogMessage("Exeption has to be thrown! Base-Call is impossible.");
		
		il.append(factory.createNew(OTConstants.unsupportedFeature));
		il.append(new DUP());
		// ## FIXME: fix otld$
		il.append(new PUSH(cpg, "Binding-Error: base-call from " + className + "." + enhancedMethod.getName()
								+ "impossible! This problem is documented in OTLD $XY."));
		il.append(factory.createInvoke(OTConstants.unsupportedFeature.getClassName(),
													 Constants.CONSTRUCTOR_NAME,
													 Type.VOID,
													 new Type[] { Type.STRING }, 
													 Constants.INVOKESPECIAL));
		il.append(new ATHROW());
		//JU: (end) --------------------------------------------------------------------------------
		
		InstructionHandle afterSwitch = il.append(new NOP()); // all breaks point here.

		il.append(switchStart, createLookupSwitch(matches, targets, breaks,
												  defaultBranch, afterSwitch));

		// retrieve locally stored result:
		if (callinHasReturnValue) {
			il.append(InstructionFactory.createLoad(returnType, localResult));
			lg.setStart(il.getStart()); // restrict local variable to this segment.
			lg.setEnd(il.getEnd());
		}
        return il;
	}
	
	/**
	 * Generates a key for the given role method parameters
	 * 
	 * @param teamClassName
	 * @param roleClassName
	 * @param roleMethodName
	 * @param roleMethodSignature
	 * @param liftMethodSignature
	 * @return
	 */
	private static String genRoleMethodKey(String teamClassName,
			String roleClassName, String roleMethodName,
			String roleMethodSignature, String liftMethodName,
			String liftMethodSignature)
	{
		StringBuilder roleMethodKey = new StringBuilder(64);
		roleMethodKey.append(teamClassName);
		roleMethodKey.append(STATIC_REPLACE_BINDING_SEPARATOR);
		roleMethodKey.append(roleClassName);
		roleMethodKey.append(STATIC_REPLACE_BINDING_SEPARATOR);
		roleMethodKey.append(roleMethodName);
		roleMethodKey.append(STATIC_REPLACE_BINDING_SEPARATOR);
		roleMethodKey.append(roleMethodSignature);
		roleMethodKey.append(STATIC_REPLACE_BINDING_SEPARATOR);
		roleMethodKey.append(liftMethodName);
		roleMethodKey.append(STATIC_REPLACE_BINDING_SEPARATOR);
		roleMethodKey.append(liftMethodSignature);
		return roleMethodKey.toString();
	}
	
	/**
	  * Read the InheritedRoles attribute and return the list of inherited roles.
	  * @param cg	The ClassGen of the inspected class.
	  * @param cpg	The constant pool of the instpected class.
	  * @return		A list of inherited role names.
	  */
	 private List<String> getInheritedRoleNames(ClassGen cg, ConstantPoolGen cpg) {
		 Attribute[] attributes = cg.getAttributes();		
		 LinkedList<String> inheritedRoleNames = new LinkedList<String>();
		 for (int i = 0; i < attributes.length; i++) {
			 Attribute actAttr = attributes[i];
			 if (actAttr instanceof Unknown) {
				 Unknown attr = (Unknown)actAttr;
				 byte[] indizes = attr.getBytes();
				 int count = combineTwoBytes(indizes, 0);
				 if (attr.getName().equals("InheritedRoles")) {
					 int j = 2;
					 while (j<=2*count) {
						 String[] names = new String[1];
						 j = scanStrings(names, indizes, j, cpg);
						 String inherited_role = names[0];
						 inheritedRoleNames.add(inherited_role);
					 }
				 }
			 }
		 }
		 return inheritedRoleNames;
	 }
}
