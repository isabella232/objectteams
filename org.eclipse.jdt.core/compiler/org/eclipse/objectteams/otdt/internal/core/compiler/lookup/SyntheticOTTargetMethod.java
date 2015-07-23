/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2014 GK Software AG
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * A synthetic method binding, which used as an invocation target needs to
 * influence code generation.
 * 
 * @since 2.3.1
 */
public abstract class SyntheticOTTargetMethod extends SyntheticMethodBinding {

	protected SyntheticOTTargetMethod(MethodBinding targetMethod, int purpose) {
		super(targetMethod, purpose);
	}
	
	protected SyntheticOTTargetMethod(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess, SourceTypeBinding declaringClass) {
		super(targetField, isReadAccess, isSuperAccess, declaringClass);
	}

	protected SyntheticOTTargetMethod(BinaryTypeBinding declaringClass, int modifiers, char[] selector,
			TypeBinding[] parameters, TypeBinding returnType) {
		super(declaringClass, modifiers, selector, parameters, returnType);
	}

	/**
	 * We assume: all arguments of an invocation have already been pushed.
	 * At this point we may insert additional byte codes, or generate the actual invocation.
	 * @return 0 means: no further action needed, else we return the opcode for which the actual
	 * 						invoke still needs to be generated.
	 */
	public abstract byte prepareOrGenerateInvocation(CodeStream codeStream, byte opcode);
	
	/**
	 * Represents an inferred callout-to-field.
	 * May need to tweak invokestatic (for regular access$n methods) into invokevirtual (for the generated c-t-f method).
	 */
	public static class CalloutToField extends SyntheticOTTargetMethod {
		public CalloutToField(MethodBinding targetMethod) {
			super(targetMethod, InferredCalloutToField);
		}

		@Override
		public byte prepareOrGenerateInvocation(CodeStream codeStream, byte opcode) {
			if (!isStatic() && this.purpose == SyntheticMethodBinding.InferredCalloutToField)
				return Opcodes.OPC_invokevirtual;
			return opcode;
		}
	}
	
	/**
	 * Represents a decapsulating field access while targeting OTDRE.
	 * We need to generate a special sequence to call _OT$access(int,int,Object[],ITeam)
	 * Currently only supports read access.
	 * Typical use is when a base predicate refers to a private base field.
	 */
	public static class OTDREFieldDecapsulation extends SyntheticOTTargetMethod {

		private int accessId;
		private int opKind;
		private ReferenceBinding enclosingTeam;
		private TypeBinding originalType;
		private ASTNode site;
		private BlockScope scope;
		
		/**
		 * Create a binding for a field access using decapsulation.
		 * @param fakedMethod this method is member of the base class, representing the otdre-generated access method.
		 * @param originalType return type of the original feature (field)
		 * @param accessId ID by which the base field is identified inside the access method
		 * @param opKind 0 = get, 1 = set
		 * @param scope where this access has been seen
		 * @param site the exact node where decapsulation happened
		 */
		public OTDREFieldDecapsulation(MethodBinding fakedMethod, TypeBinding originalType, int accessId, int opKind, BlockScope scope, ASTNode site) {
			super(fakedMethod, SyntheticMethodBinding.InferredCalloutToField);
			this.accessId = accessId;
			this.opKind = opKind;
			ReferenceBinding enclosingRole = scope.enclosingReceiverType();
			this.enclosingTeam = enclosingRole.enclosingType();
			this.originalType = originalType;
			this.site = site;
			this.scope = scope;
		}

		@Override
		public byte prepareOrGenerateInvocation(CodeStream codeStream, byte opcode) {
			// accessId
			codeStream.bipush((byte) this.accessId);
			// 0 = get, 1 = set
			if (this.opKind == 0)
				codeStream.iconst_0();
			else
				codeStream.iconst_1();
			// no args to pack for read access:
			codeStream.aconst_null(); 
			// enclosing team instance:
			Object[] emulationPath = this.scope.getEmulationPath(this.enclosingTeam, true /*only exact match*/, false/*consider enclosing arg*/);
			codeStream.generateOuterAccess(emulationPath, this.site, this.enclosingTeam, this.scope);
			// invoke it:
			byte invoke = this.targetMethod.isStatic() ? Opcodes.OPC_invokestatic : Opcodes.OPC_invokevirtual;
			codeStream.invoke(invoke, this.targetMethod, this.targetMethod.declaringClass);
			// convert result?:
			if (this.originalType != TypeBinding.VOID) {
				if (this.originalType.isBaseType()) {
					codeStream.checkcast(this.scope.environment().computeBoxingType(this.originalType));
					codeStream.generateUnboxingConversion(this.originalType.id);
				} else {
					codeStream.checkcast(this.originalType);
				}
			} else {
				// what? - not hit for field read :)
			}
			return 0; // signal we're done
		}
	}

	/**
	 * Represents a decapsulating method access while targeting OTDRE (not an explicit callout).
	 * We need to generate a special sequence to call _OT$access(int,int,Object[],ITeam)
	 */
	public static class OTDREMethodDecapsulation extends SyntheticOTTargetMethod implements TeamModel.UpdatableAccessId {

		private int accessId;
		private ReferenceBinding enclosingTeam;
		private TypeBinding[] originalParameters;
		private TypeBinding originalReturnType;
		private ASTNode site;
		private BlockScope scope;
		
		/**
		 * Create a binding for a method access using decapsulation.
		 * @param targetMethod this method is member of the base class, representing the otdre-generated access method.
		 * @param originalParameters parameters of the original target method (before replacing with _OT$access)
		 * @param originalReturn return type of the original target method (before replacing with _OT$access)
		 * @param accessId ID by which the base field is identified inside the access method
		 * @param scope where this access has been seen
		 * @param site the exact node where decapsulation happened
		 */
		public OTDREMethodDecapsulation(MethodBinding targetMethod, TypeBinding[] originalParameters, TypeBinding originalReturn, int accessId, BlockScope scope, ASTNode site) {
			super(targetMethod, SyntheticMethodBinding.MethodDecapsulation);
			this.accessId = accessId;
			ReferenceBinding enclosingRole = scope.enclosingReceiverType();
			this.enclosingTeam = enclosingRole.enclosingType();
			this.originalParameters = originalParameters;
			this.originalReturnType = originalReturn;
			this.site = site;
			this.scope = scope;
		}

		@Override
		public byte prepareOrGenerateInvocation(CodeStream codeStream, byte opcode) {
			TypeBinding[] tgtParams = this.originalParameters;
			byte len = (byte) tgtParams.length;
			// argument array:
			codeStream.bipush(len);
			codeStream.anewarray(this.scope.getJavaLangObject());
			// fold array store into arguments on stack:
			for (byte i = (byte) (len-1); i >= 0; i--) {
				// argi, array
				if (size(tgtParams[i]) == 1) {
					codeStream.dup_x1();
					// array, argi, array
					codeStream.swap();
				} else {
					codeStream.dup_x2();
					// array, argi, array
					codeStream.dup_x2();
					// array, array, argi, array
					codeStream.pop();
				}
				// array, array, argi
				if (tgtParams[i].isPrimitiveType())
					codeStream.generateBoxingConversion(tgtParams[i].id); // no longer need to handle 2-byte values
				codeStream.bipush(i);
				// array, array, argi, i
				codeStream.swap();
				// array, array, i, argi
				codeStream.aastore();
				// array
			}
			// array (containing all arguments)

			// accessId:
			codeStream.bipush((byte) this.accessId);
			codeStream.swap();
			// accessId, array
			
			// opKind (ignored):
			codeStream.iconst_0();
			codeStream.swap();
			// accessId, opKind, array

			// enclosing team instance:
			Object[] emulationPath = this.scope.getEmulationPath(this.enclosingTeam, true /*only exact match*/, false/*consider enclosing arg*/);
			codeStream.generateOuterAccess(emulationPath, this.site, this.enclosingTeam, this.scope);
			// invoke it:
			byte invoke = this.targetMethod.isStatic() ? Opcodes.OPC_invokestatic : Opcodes.OPC_invokevirtual;
			codeStream.invoke(invoke, this, this.targetMethod.declaringClass);
			// convert result?:
			if (this.originalReturnType != TypeBinding.VOID) {
				if (this.originalReturnType.isBaseType()) {
					codeStream.checkcast(this.scope.environment().computeBoxingType(this.originalReturnType));
					codeStream.generateUnboxingConversion(this.originalReturnType.id);
				} else {
					codeStream.checkcast(this.originalReturnType);
				}
			} else {
				codeStream.pop();
			}
			return 0; // signal we're done
		}

		@Override
		public void update(int offset) {
			this.accessId += offset;
		}
	}
	static int size(TypeBinding type) {
		switch (type.id) {
			case TypeIds.T_double :
			case TypeIds.T_long :
				return 2;
			case TypeIds.T_void :
				return 0;
			default :
				return 1;
		}
	}
}
