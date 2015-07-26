/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2015 Stephan Herrmann.
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

import static org.eclipse.objectteams.otredyn.transformer.names.ClassNames.OBJECT_SLASH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.objectteams.otredyn.bytecode.AbstractBoundClass;
import org.eclipse.objectteams.otredyn.bytecode.Method;
import org.eclipse.objectteams.otredyn.bytecode.asm.AbstractTransformableClassNode.IBoundMethodIdInsnProvider;
import org.eclipse.objectteams.otredyn.runtime.IMethod;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A "wicked super call" is a super call, whose target method is not the one
 * that is overridden by the enclosing method.
 * 
 * If the targeted super method is callin-bound, a "wicked super call" may
 * cause StackOverflowError at runtime. To prevent this, these super calls
 * are transformed into proper invocations {@code super._OT$callOrig(...)}.
 * 
 * @see <a href="https://wiki.eclipse.org/OTCaveats#Wicked_super_calls">Wicked super calls</a>
 */
public class ReplaceWickedSuperCallsAdapter extends AbstractTransformableClassNode implements IBoundMethodIdInsnProvider {

	private AbstractBoundClass superclass;
	private List<Method> targetMethods = new ArrayList<Method>();
	private Map<MethodNode,List<MethodInsnNode>> instructionsToWeave = new HashMap<MethodNode, List<MethodInsnNode>>();

	/**
	 * Register the given targetMethod from the given superclass to the end that
	 * all wicked super calls towards this method will be replaced.
	 * @param nodes list of transformations of the current class, if it already contains a
	 * 		{@link ReplaceWickedSuperCallsAdapter}, it will be re-used by only registering
	 * 		one more targetMethod. 
	 * @param superclass the current class's super class, target of any super calls.
	 * @param targetMethod super class's method which is callin-bound and requires treatment by this adapter.
	 */
	public static void register(List<AbstractTransformableClassNode> nodes, AbstractBoundClass superclass, Method targetMethod) {
		// try to add to existing adapter:
		for (AbstractTransformableClassNode node: nodes) {
			if (node instanceof ReplaceWickedSuperCallsAdapter) {
				((ReplaceWickedSuperCallsAdapter) node).addTargetMethod(targetMethod);
				return;
			}
		}
		// no existing, create new adapter:
		nodes.add(new ReplaceWickedSuperCallsAdapter(superclass, targetMethod));
	}

	private ReplaceWickedSuperCallsAdapter(AbstractBoundClass superclass, Method targetMethod) {
		this.superclass = superclass;
		this.targetMethods.add(targetMethod);
	}

	private void addTargetMethod(Method targetMethod) {
		this.targetMethods.add(targetMethod);
	}

	/**
	 * Collect all wicked super calls requiring replacement (per enclosing method).
	 * This method is called while the ClassReader reads the current class.
	 */
	public MethodVisitor visitMethod(int access, final String enclosingMethodName, final String enclosingMethodDesc, String signature, String[] exceptions) {
        final MethodNode mn = new MethodNode(access, enclosingMethodName, enclosingMethodDesc, signature, exceptions);
        methods.add(mn);
		return new MethodVisitor(this.api, mn) {
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				super.visitMethodInsn(opcode, owner, name, desc, itf);
				if (opcode == Opcodes.INVOKESPECIAL && !enclosingMethodName.equals(name) && owner.equals(superclass.getInternalName())) {
					// we have a wicked super call ...
					for (Method tgt: targetMethods) {
						if (tgt.getName().equals(name) && tgt.getSignature().equals(desc)) {
							// ... and it targets a registered method
							// => remember this instruction:
							List<MethodInsnNode> insns = instructionsToWeave.get(mn);
							if (insns == null) {
								insns = new ArrayList<MethodInsnNode>();
								instructionsToWeave.put(mn, insns);
							}
							insns.add((MethodInsnNode) mn.instructions.getLast()); // appended by super.visisMethodIsns(..) above
							break;
						}
					}
				}
			}
		};
	}

	@Override
	protected boolean transform() {
		if (instructionsToWeave.isEmpty())
			return false; // nothing to do
		
		// after a visitor was fine for scanning the byte code for instructions to be replaced,
		// we now need the tree API for manipulating the instruction list, because we need to
		// morph an arg-load sequence into its packing variant.

		for (Map.Entry<MethodNode,List<MethodInsnNode>> toWeave : instructionsToWeave.entrySet()) {
			MethodNode enclosingMethod = toWeave.getKey();
			InsnList instructions = enclosingMethod.instructions;
			List<MethodInsnNode> superCallsToReplace = toWeave.getValue();
			Type returnType = Type.getReturnType(enclosingMethod.desc);
			boolean returnsJLObject = returnType.getSort() == Type.OBJECT ? returnType.getInternalName().equals(OBJECT_SLASH) : false;
			replaceSuperCallsWithCallToCallOrig(instructions, superCallsToReplace, returnsJLObject, superclass, this);
		}
		return true;
	}

	/** Callback. */
	public AbstractInsnNode getLoadBoundMethodIdInsn(MethodInsnNode methodInsn) {
		// boundMethodId can be statically determined based on the target method.
		IMethod otdreMethod = superclass.getMethod(methodInsn.name, methodInsn.desc, false, false);
		int methodID = otdreMethod.getGlobalId(superclass);
		return createLoadIntConstant(methodID);
	}
}
