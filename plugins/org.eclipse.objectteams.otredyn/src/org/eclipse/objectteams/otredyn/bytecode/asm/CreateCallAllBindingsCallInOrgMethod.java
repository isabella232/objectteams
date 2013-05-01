/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2012 Oliver Frank and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Oliver Frank - Initial API and implementation
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.bytecode.asm;

import org.eclipse.objectteams.otredyn.bytecode.Method;
import org.eclipse.objectteams.otredyn.transformer.names.ClassNames;
import org.eclipse.objectteams.otredyn.transformer.names.ConstantMembers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * This class creates and adds the instructions, that are needed 
 * to call the method callAllBindings to a method.<br/> <br/>
 * The instructions looks as follows:<br/>
 * <code>
 * Object[] args = {args1, ..., argsn};<br/>
 * this.callAllBindings(boundMethodId, args);
 * </code>
 * @author Oliver Frank
 */
public class CreateCallAllBindingsCallInOrgMethod extends
		AbstractTransformableClassNode {

	private Method orgMethod;
	private int boundMethodId;

	public CreateCallAllBindingsCallInOrgMethod(Method orgMethod,
			int boundMethodId) {
		this.orgMethod = orgMethod;
		this.boundMethodId = boundMethodId;
	}

	@Override
	public void transform() {
		MethodNode method = getMethod(orgMethod);
		method.instructions.clear();

		// start of try-block:
		LabelNode start = new LabelNode();
		method.instructions.add(start);

		// put this on the stack
		method.instructions.add(new IntInsnNode(Opcodes.ALOAD, 0));
		// put boundMethodId on the stack
		method.instructions.add(createLoadIntConstant(boundMethodId));
		Type[] args = Type.getArgumentTypes(method.desc);
		// box the arguments
		method.instructions.add(getBoxingInstructions(args, false));

		// this.callAllBindings(boundMethodId, args);
		method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
				this.name, ConstantMembers.callAllBindingsClient.getName(),
				ConstantMembers.callAllBindingsClient.getSignature()));
		Type returnType = Type.getReturnType(method.desc);
		method.instructions
				.add(getUnboxingInstructionsForReturnValue(returnType));

		// catch and unwrap SneakyException:
		addCatchSneakyException(method, start);

		int localSlots = 0;
		int maxArgSize = 1;
		for (Type type : args) {
			int size = type.getSize();
			localSlots += size;
			if (size == 2)
				maxArgSize = 2;
		}
		method.maxStack = args.length > 0 ? 5+maxArgSize : 3;
		method.maxLocals = localSlots+1;
	}

	@SuppressWarnings("unchecked") // access to raw type List in ASM
	void addCatchSneakyException(MethodNode method, LabelNode start) {
		method.tryCatchBlocks.add(getCatchBlock(method.instructions, start, orgMethod));
	}

	TryCatchBlockNode getCatchBlock(InsnList instructions, LabelNode start, Method method) {
		// end (exclusive) of try-block
		LabelNode end = new LabelNode();
		instructions.add(end);

		// catch (SneakyException e) { e.rethrow(); }
		LabelNode catchSneaky = new LabelNode();
		instructions.add(catchSneaky);
		instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ClassNames.SNEAKY_EXCEPTION_SLASH, ClassNames.RETHROW_SELECTOR, ClassNames.RETHROW_SIGNATURE));
		
		// never reached, just to please the verifier:
		Type returnType = Type.getReturnType(method.getSignature());
		instructions.add(getReturnInsn(returnType));
		return new TryCatchBlockNode(start, end, catchSneaky, ClassNames.SNEAKY_EXCEPTION_SLASH);
	}

	protected InsnList getReturnInsn(Type returnType) {
		InsnList instructions = new InsnList();
		switch (returnType.getSort()) {
		case Type.VOID:
			instructions.add(new InsnNode(Opcodes.RETURN));
			break;
		case Type.ARRAY:
		case Type.OBJECT:
			instructions.add(new InsnNode(Opcodes.ACONST_NULL));
			instructions.add(new InsnNode(Opcodes.ARETURN));
			break;
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.INT:
		case Type.SHORT:
		case Type.LONG:
			instructions.add(new InsnNode(Opcodes.ICONST_0));
			instructions.add(new InsnNode(Opcodes.IRETURN));
			break;
		case Type.DOUBLE:
			instructions.add(new InsnNode(Opcodes.DCONST_0));
			instructions.add(new InsnNode(Opcodes.DRETURN));
			break;
		case Type.FLOAT:
			instructions.add(new InsnNode(Opcodes.FCONST_0));
			instructions.add(new InsnNode(Opcodes.FRETURN));
			break;
		}
		return instructions;
	}
}
