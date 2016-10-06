/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2014 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import static org.eclipse.objectteams.otredyn.bytecode.asm.AsmBoundClass.ASM_API;

import org.eclipse.objectteams.otredyn.transformer.names.ClassNames;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Add code into all direct implementors of Runnable.run() / Thread.run()
 * to inform the TeamThreadManager about new and ended threads.
 * (See org.eclipse.objectteams.otre.ThreadActivation in the old OTRE).
 */
public class AddThreadNotificationAdapter extends ClassVisitor {

	protected static final String THREAD_DESC = "L"+ClassNames.THREAD_SLASH+";";
	protected static final String VOID_DESC = "()V";

	// Runnable / Thread:
	protected static final String RUN 					= "run",				RUN_DESC					= VOID_DESC;

	// Thread:
	protected static final String CURRENT_THREAD 		= "currentThread", 		CURRENT_THREAD_DESC 		= "()"+THREAD_DESC;
	
	// TeamThreadManager:
	protected static final String NEW_THREAD_STARTED 	= "newThreadStarted",	NEW_THREAD_STARTED_DESC 	= "(Z"+THREAD_DESC+")Z";
	protected static final String THREAD_ENDED 			= "threadEnded", 		THREAD_ENDED_DESC 		= VOID_DESC;

	// any implementor:
	protected static final String INIT					= "<init>";
	
	// new field inserted by this adapter:
	protected static final String CREATION_THREAD 		= "_OT$creationThread";


	private AsmBoundClass clazz;

	public AddThreadNotificationAdapter(ClassVisitor cv, AsmBoundClass clazz) {
		super(ASM_API, cv);
		this.clazz = clazz;
	}
	
	@Override
	public void visitEnd() {
		cv.visitField(Opcodes.ACC_PRIVATE, CREATION_THREAD, THREAD_DESC, null, null);
		super.visitEnd();
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
		if (INIT.equals(methodName)) {
			// into each constructor ...
        	final MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, null, null);
            return new AdviceAdapter(this.api, methodVisitor, access, methodName, desc) {
            	@Override
            	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itfc) {
            		super.visitMethodInsn(opcode, owner, name, desc, itfc);
            		// ... that contains a super(..) call (rather than this(..)):
            		if (opcode == Opcodes.INVOKESPECIAL && INIT.equals(name) && owner.equals(clazz.getInternalSuperClassName())) {
            			// insert:
            			// this._OT$creationThread = Thread.currentThread();
            			methodVisitor.visitIntInsn(Opcodes.ALOAD, 0);
            			methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, ClassNames.THREAD_SLASH, CURRENT_THREAD, CURRENT_THREAD_DESC, false);
            			methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.getInternalName(), CREATION_THREAD, THREAD_DESC);
            		}
            	}
            };
		} else if (RUN.equals(methodName) && RUN_DESC.equals(desc)) {
        	final MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, null, null);
            return new AdviceAdapter(this.api, methodVisitor, access, methodName, desc) {

            	Label start = new Label(); 	// start of method (scope of new local)
            	Label end = new Label();	// end of method
				int isThreadStartIdx;		// new local: boolean _OT$isThreadStart

            	@Override
            	protected void onMethodEnter() {
            		methodVisitor.visitLabel(start);
            		isThreadStartIdx=newLocal(Type.BOOLEAN_TYPE);
            		methodVisitor.visitLocalVariable("_OT$isThreadStart", "Z", null, start, end, isThreadStartIdx);
            		// TeamThreadManager.newThreadStarted(false, this._OT$creationThread)
            		methodVisitor.visitInsn(Opcodes.ICONST_0);
            		methodVisitor.visitIntInsn(Opcodes.ALOAD, 0);
            		methodVisitor.visitFieldInsn(Opcodes.GETFIELD, clazz.getInternalName(), CREATION_THREAD, THREAD_DESC);
            		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, ClassNames.TEAM_THREAD_MANAGER_SLASH, 
            										NEW_THREAD_STARTED, NEW_THREAD_STARTED_DESC, false);
            		methodVisitor.visitIntInsn(Opcodes.ISTORE, isThreadStartIdx);
            		// this._OT$creationThread = null; // avoid leak
            		methodVisitor.visitIntInsn(Opcodes.ALOAD, 0);
            		methodVisitor.visitInsn(Opcodes.ACONST_NULL);
            		methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, clazz.getInternalName(), CREATION_THREAD, THREAD_DESC);
            	}
            	
            	@Override
            	protected void onMethodExit(int opcode) {
            		insertThreadEndedNotification();
            	}
            	
            	@Override
            	public void endMethod() {
            		methodVisitor.visitLabel(end);
            		
            		// insert another threadEnded notification as a handler for Throwable
            		Label handler = new Label();
            		methodVisitor.visitLabel(handler);
            		insertThreadEndedNotification();
            		methodVisitor.visitInsn(Opcodes.ATHROW); // rethrow caught exception
            		
            		methodVisitor.visitTryCatchBlock(start, end, handler, ClassNames.THROWABLE_SLASH);
            		methodVisitor.visitMaxs(0, 0);
            	}

				void insertThreadEndedNotification() {
					Label skip = new Label();
            		// insert:
            		// if (_OT$isThreadStart) TeamThreadManager.threadEnded();
            		methodVisitor.visitIntInsn(Opcodes.ILOAD, isThreadStartIdx);
            		methodVisitor.visitJumpInsn(Opcodes.IFEQ, skip);
            		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, ClassNames.TEAM_THREAD_MANAGER_SLASH, 
													THREAD_ENDED, THREAD_ENDED_DESC, false);
            		methodVisitor.visitLabel(skip);
				}
            };		
		}
		return null;
	}

	public static boolean shouldNotify(AsmWritableBoundClass clazz) {
		String[] interfaceNames = clazz.getSuperInterfaceNames();
		if (interfaceNames != null) {
			for (int i = 0; i < interfaceNames.length; i++) {
				if (ClassNames.RUNNABLE_SLASH.equals(interfaceNames[i]))
					return true;
			}
		}
		if (ClassNames.THREAD_SLASH.equals(clazz.getInternalSuperClassName()))
			return true;
		// not traversing super chains, currently. FIXME: Should indeed traverse super interfaces to find Runnable!!
		return false;
	}
}
