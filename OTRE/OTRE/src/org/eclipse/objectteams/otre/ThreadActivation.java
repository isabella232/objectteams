/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ThreadActivation.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre;

import java.util.HashSet;

import de.fub.bytecode.classfile.*;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.*;


/**
 * This transformer inserts a notification call to the TeamThreadManager at the 
 * beginning of every run()-Method in Subtypes of java.lang.Thread or java.lang.Runnable. 
 * Thre TeamThreadManager then ensures the thread activation of the current thread 
 * for every global active team instance.
 * 
 * @author  Christine Hundt
 * @author  Stephan Herrmann
 */
public class ThreadActivation 
{
	// name of a generated field that stores the thread which created a given runnable:
	private static final String CREATION_THREAD = "_OT$creationThread";
	HashSet<String> transformableClasses = new HashSet<String>();
	
	private boolean shouldTransform(ClassGen cg) {
		Method runMethode = cg.containsMethod("run", "()V");
		if (runMethode == null || runMethode.isAbstract()) {
			// this class contains no concrete run() method
			return false;
		}
		String class_name = cg.getClassName();
		// check if this class is a subtype of Thread or Runnable:
		try {
			return Repository.implementationOf(class_name, "java.lang.Runnable") || Repository.instanceOf(class_name, "java.lang.Thread");
		} catch (NullPointerException npe) {
			if (ObjectTeamsTransformation.WORKAROUND_REPOSITORY) {
				return false;
			}
			else
				throw npe; // rethrow
		}
	}
	public void doTransformInterface(ClassEnhancer enhancer, ClassGen cg) {
		if (!shouldTransform(cg))
			return;
		
		// if class is already transformed by this transformer
		if (this.transformableClasses.contains(cg.getClassName()))
			return;
		
		this.transformableClasses.add(cg.getClassName());
		FieldGen field = new FieldGen(Constants.ACC_PRIVATE, OTConstants.threadType, CREATION_THREAD, cg.getConstantPool());
		enhancer.addField(field.getField(), cg);
	}
	/**
     *  
     */	
	public void doTransformCode(ClassGen cg) {
		if (!this.transformableClasses.contains(cg.getClassName()))
			return;
		
		this.transformableClasses.remove(cg.getClassName());

		InstructionFactory factory = new InstructionFactory(cg);

		for (Method method : cg.getMethods()) {
			MethodGen mg = isRootCtor(method, cg);
			if (mg != null)
				enhanceConstructor(cg, factory, method, mg);
		}
		
		enhanceRunMethod(cg, factory);
    }
	private void enhanceRunMethod(ClassGen cg, InstructionFactory factory) {
		String class_name = cg.getClassName();
		ConstantPoolGen cpg = cg.getConstantPool();

		Method runMethode = cg.containsMethod("run", "()V"); // existence checked in transformInterface

		MethodGen mg = new MethodGen(runMethode, class_name, cpg);
		InstructionList il = mg.getInstructionList();
		InstructionHandle try_start = il.getStart();
		
		/** *** Insert a call to TeamThreadManager.newThreadStarted() at the beginning of the run() method: ****** */	
		InstructionList threadActivation = new InstructionList();
		threadActivation.append(new ICONST(0)); // isMain = false
		threadActivation.append(InstructionConstants.ALOAD_0); // parent=this._OT$creationThread;
		threadActivation.append(factory.createFieldAccess(class_name, CREATION_THREAD, OTConstants.threadType, Constants.GETFIELD));
		threadActivation.append(factory.createInvoke("org.objectteams.TeamThreadManager", 
				                                             "newThreadStarted",
															 Type.BOOLEAN,
															 new Type[]{Type.BOOLEAN, OTConstants.threadType},
															 Constants.INVOKESTATIC));
		LocalVariableGen flag = mg.addLocalVariable("_OT$isThreadStart", Type.BOOLEAN, il.getStart(), il.getEnd());
		threadActivation.append(new ISTORE(flag.getIndex()));
		il.insert(threadActivation);
		
		/** *** Insert a call to TeamThreadManager.threadEnded() before every return of the run() method: ******** */	
		InstructionList threadDeactivation = new InstructionList();
		threadDeactivation.append(new ILOAD(flag.getIndex()));
		BranchInstruction ifIsThreadStarted = new IFEQ(null);
		threadDeactivation.append(ifIsThreadStarted);
		threadDeactivation.append(factory.createInvoke("org.objectteams.TeamThreadManager", 
                "threadEnded",
				 Type.VOID,
				 Type.NO_ARGS,
				 Constants.INVOKESTATIC));
		ifIsThreadStarted.setTarget(threadDeactivation.append(new NOP()));

		FindPattern findPattern = new FindPattern(il);
		String pat = "`ReturnInstruction'";
		InstructionHandle ih = findPattern.search(pat);
		while (ih != null) {
			// insert deactivate-call before return instruction in ih:
			InstructionList deactivationCopy = threadDeactivation.copy();
			InstructionHandle inserted = il.insert(ih, deactivationCopy); // instruction lists can not be reused
			il.redirectBranches(ih, inserted);// SH: retarget all jumps that targeted at the return instruction
			if (ih.getNext() == null)
				break; // end of instruction list reached
			ih = findPattern.search(pat, ih.getNext());
		}

		/** **** Add an exception handler which calls TeamThreadManager.threadEnded() *****
		 * ***** before throwing the exception (finaly-simulation): 											        */
		ObjectType throwable = new ObjectType("java.lang.Throwable");
		LocalVariableGen exception = mg.addLocalVariable(
				"_OT$thrown_exception", throwable, null, null);
		InstructionHandle try_end = il.getEnd();
		InstructionList deactivation_ex = threadDeactivation.copy();
		deactivation_ex.insert(InstructionFactory.createStore(throwable, exception.getIndex()));
		deactivation_ex.append(InstructionFactory.createLoad(throwable, exception.getIndex()));
		deactivation_ex.append(new ATHROW());
		InstructionHandle deactivation_handler = il.append(il.getEnd(), deactivation_ex);
		mg.addExceptionHandler(try_start, try_end, deactivation_handler, throwable);
		/** ******************************************************************** */		
		
		mg.setMaxStack();
        mg.setMaxLocals();
        Method generatedMethod = mg.getMethod();
        cg.replaceMethod(runMethode, generatedMethod);
        threadActivation.dispose();
        il.dispose();
	}
	// is method a constructor that does not invoke another this()-ctor?
	private MethodGen isRootCtor(Method method, ClassGen cg) {
		if (!method.getName().equals("<init>"))
			return null;
		String className = cg.getClassName();
		ConstantPoolGen cpg = cg.getConstantPool();
		MethodGen mg = new MethodGen(method, className, cpg);
		InstructionList il = mg.getInstructionList();
		InstructionHandle ih = il.getStart();
		while (ih != null && ih.getInstruction().getOpcode() != Constants.INVOKESPECIAL) {
			ih = ih.getNext();
		}
		if (ih == null)
			return null;
		if (((InvokeInstruction)ih.getInstruction()).getClassName(cpg).equals(className))
			return null; // this-call
		return mg;
	}
	// add statements to store the thread that created this runnable
	private void enhanceConstructor(ClassGen cg, InstructionFactory factory, Method initMethod, MethodGen mg) {
		String class_name = cg.getClassName();
		InstructionList il = mg.getInstructionList();
		il.insert(il.getEnd(), InstructionConstants.ALOAD_0);
		il.insert(il.getEnd(), factory.createInvoke("java.lang.Thread", "currentThread", OTConstants.threadType, new Type[0], Constants.INVOKESTATIC));
		il.insert(il.getEnd(), factory.createFieldAccess(class_name, CREATION_THREAD, OTConstants.threadType, Constants.PUTFIELD));
		mg.setMaxStack();
	    mg.setMaxLocals();
	    cg.replaceMethod(initMethod, mg.getMethod());
	    il.dispose();
	}
}
