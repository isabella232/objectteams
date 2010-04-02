/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SubBoundBaseMethodRedefinition.java 23408 2010-02-03 18:07:35Z stephan $
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
 * Redefines inherited (and not redefined) base methods in subclasses with a
 * call to the super method if they are bound for the subclass only. This is
 * done in order to provide a place for the BaseMethodTransformer to weave in
 * the callin code.
 * 
 * @version $Id: SubBoundBaseMethodRedefinition.java 23408 2010-02-03 18:07:35Z stephan $
 * @author  Christine Hundt
 * @author  Stephan Herrmann
 */

public class SubBoundBaseMethodRedefinition 
	extends ObjectTeamsTransformation {

	public SubBoundBaseMethodRedefinition(SharedState state) { this(null, state); }
	public SubBoundBaseMethodRedefinition(ClassLoader loader, SharedState state) { super(loader, state); }

	/**
	 * Main entry for this transformer.
	 */
	public void doTransformInterface(ClassEnhancer ce, ClassGen cg) {
		String class_name = cg.getClassName();
		ConstantPoolGen cpg = cg.getConstantPool();

		factory = new InstructionFactory(cg);

		checkReadClassAttributes(ce, cg, class_name, cpg);

		// if class is already transformed by this transformer
		if (state.interfaceTransformedClasses.contains(class_name)) {
			return;
		}

		List<MethodBinding> mbsForClass = CallinBindingManager
				.getMethodBindingsForBaseClass(class_name);
		if (mbsForClass.isEmpty()) {
			return; // no bindings for this base class
		}

		List<MethodBinding> inheritedBoundMethods = getInheritedBoundMethods(mbsForClass, cg);
		
		addSubBoundMethodRedefinitions(inheritedBoundMethods, ce, cg);
		state.interfaceTransformedClasses.add(class_name);
	}

	/**
	 * Adds redefinitions with calls to the super method to all methods
	 * contained in the 'inheritedBoundMethods' list.
	 * 
	 * @param inheritedBoundMethods
	 *            The list of method bindings for inherited methods.
	 * @param ce
	 *            ClassEnhancer with the extension set of this class.
	 * @param cg
	 *            The ClassGen object for the transformed class.
	 */
	private void addSubBoundMethodRedefinitions(List<MethodBinding> inheritedBoundMethods,
			ClassEnhancer ce, ClassGen cg) {
		List<String> alreadyAddedRedefinitions = new LinkedList<String>();

		Iterator<MethodBinding> it = inheritedBoundMethods.iterator();
		while (it.hasNext()) {
			MethodBinding mb = it.next();
			String baseMethodKey = mb.getBaseMethodName() + "."
					+ mb.getBaseMethodSignature();

			if (alreadyAddedRedefinitions.contains(baseMethodKey)) {
				continue;
			}
			Method m = genMethodRedefinition(mb, cg);
			
			ce.addMethod(m, cg);
            if(logging) printLogMessage("Added " + baseMethodKey + " to " + cg.getClassName());
			alreadyAddedRedefinitions.add(baseMethodKey);
		}
	}

	/**
	 * Generates a (redefining) method, which just calls its super version.
	 * 
	 * @param mb
	 *            A method binding containing the method to be redefined.
	 * @param cg
	 *            The ClassGen object for the transformed class.
	 * @return The generated method.
	 */
	private Method genMethodRedefinition(MethodBinding mb, ClassGen cg) {
		boolean staticMethod = mb.hasStaticBaseMethod();
		short invocationKind = staticMethod ? Constants.INVOKESTATIC : Constants.INVOKESPECIAL;
		
		String methodName = mb.getBaseMethodName();
		String methodSignature = mb.getBaseMethodSignature();
		String className = mb.getBaseClassName();
		Type returnType = Type.getReturnType(methodSignature);
		Type[] argTypes = Type.getArgumentTypes(methodSignature);
		InstructionList il = new InstructionList();

		int accFlags = Constants.ACC_PUBLIC;
		if (staticMethod) {
			accFlags = accFlags | Constants.ACC_STATIC;
		}
		MethodGen redefinition = new MethodGen(accFlags, returnType, argTypes, 
											   null, methodName, className, il, 
											   cg.getConstantPool());

		if(!staticMethod){
			il.append(InstructionFactory.createThis());
		}
		// load all arguments:
		int index = 1;
		for (int i = 0; i < argTypes.length; i++) {
			il.append(InstructionFactory.createLoad(argTypes[i], index));
			index += argTypes[i].getSize();
		}
		il.append(factory.createInvoke(cg.getSuperclassName(), methodName,
				returnType, argTypes, invocationKind));
		il.append(InstructionFactory.createReturn(returnType));

		redefinition.removeNOPs();
		il.setPositions();
		redefinition.setMaxStack();
		redefinition.setMaxLocals();
		return redefinition.getMethod();
	}

	/**
	 * Selects all method bindings from the 'mbsForClass' list which methods are
	 * inherited only (not defined in the class itself).
	 * 
	 * @param mbsForClass
	 *            The method bindings of the transformed class.
	 * @param cg
	 *            The ClassGen object for the transformed class.
	 * @return A sublist of 'mbsForClass' containing all bindings for methods
	 *         which are inherited only.
	 * 
	 */
	private static List<MethodBinding> getInheritedBoundMethods(List<MethodBinding> mbsForClass, ClassGen cg) {
		List<MethodBinding> inheritedBoundMethod = new LinkedList<MethodBinding>();
		Iterator<MethodBinding> it = mbsForClass.iterator();
		while (it.hasNext()) {
			MethodBinding mb = it.next();
			if ((cg.containsMethod(mb.getBaseMethodName(), mb
					.getBaseMethodSignature())) == null)
				inheritedBoundMethod.add(mb);
		}
		return inheritedBoundMethod;
	}
}
