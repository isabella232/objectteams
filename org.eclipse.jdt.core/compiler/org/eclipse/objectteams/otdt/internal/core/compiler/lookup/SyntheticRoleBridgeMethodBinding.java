/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

public class SyntheticRoleBridgeMethodBinding extends SyntheticOTMethodBinding {

	public static final char[] PRIVATE = "$private$".toCharArray(); //$NON-NLS-1$

	public SyntheticRoleBridgeMethodBinding(SourceTypeBinding declaringRole, ReferenceBinding originalRole, MethodBinding targetMethod, int bridgeKind) {
		super(declaringRole, AccPublic|AccSynthetic, targetMethod.selector, targetMethod.parameters, targetMethod.returnType);
		this.purpose = bridgeKind;
		switch (bridgeKind) {
			case RoleMethodBridgeOuter:
				// correction: this method sits in the team not the role:
				this.declaringClass = declaringRole.enclosingType();
				// perform two adjustments of the first parameter passing the role instance:
				// - weakening (using originalRole)
				// - use ifc-part: inner field accessor uses role class, don't expose it at this level
				int len = this.parameters.length;
				if (len > 0) { // accessor to static field has no argument
					System.arraycopy(this.parameters, 0, this.parameters = new TypeBinding[len], 0, len);
					this.parameters[0] = originalRole.getRealType(); // may also be weakened
				}
				break;
			case RoleMethodBridgeInner:
				// correction: add role as first parameter:
				len = targetMethod.parameters.length;
				int offset = targetMethod.isStatic()?2:0;
				this.parameters = new TypeBinding[len+1+offset];
				this.parameters[0] = declaringRole.getRealType();
				if (offset > 0) {
					this.parameters[1] = TypeBinding.INT;				// dummy int
					this.parameters[2] = declaringRole.enclosingType(); // team arg
				}
				System.arraycopy(targetMethod.parameters, 0, this.parameters, 1+offset, len);
				// correction: this bridge is static:
				this.modifiers |= AccStatic;
				// correction: generate the bridge method name:
				this.selector = SyntheticRoleBridgeMethodBinding.getPrivateBridgeSelector(targetMethod.selector, declaringRole.sourceName());
				break;
		}
		this.targetMethod = targetMethod;
		this.thrownExceptions = targetMethod.thrownExceptions;
		this.typeVariables = targetMethod.typeVariables;
		SyntheticMethodBinding[] knownAccessMethods = ((SourceTypeBinding) this.declaringClass).syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
	}

	@Override
	public void generateInstructions(CodeStream codeStream) {
		TypeBinding[] arguments = this.parameters;
		int argLen = arguments.length;
		TypeBinding[] targetParameters = this.targetMethod.parameters;
		int resolvedPosition = 0;
		int argIdx = 0;
		int targetIdx = 0;
		switch (this.purpose) {
			case RoleMethodBridgeInner:
				codeStream.aload_0(); // synthetic first arg is the receiver role
				codeStream.checkcast(this.targetMethod.declaringClass);
				resolvedPosition = 1; // first arg is processed
				argIdx = 1;
				if (this.targetMethod.isStatic()) {
					codeStream.iconst_0(); // dummy int
					codeStream.aload_2(); // pass synth. team arg
					argIdx += 2;
					resolvedPosition += 2;
				}
				break;
			case RoleMethodBridgeOuter:
				resolvedPosition = 1; // ignore team instance at 0
				argIdx = 0; 		  // pass all args unchanged
				break;
		}
		while (argIdx < argLen) {
		    TypeBinding parameter = targetParameters[targetIdx++];
		    TypeBinding argument = arguments[argIdx++];
			codeStream.load(argument, resolvedPosition);
			if (argument != parameter)
				codeStream.checkcast(parameter);
			switch(parameter.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					resolvedPosition += 2;
					break;
				default :
					resolvedPosition++;
					break;
			}
		}
		switch (this.purpose) {
			case RoleMethodBridgeInner :
				// call the private role method
				codeStream.invoke(Opcodes.OPC_invokespecial, this.targetMethod, null);
				break;
			case RoleMethodBridgeOuter :
				// call the static role method bridge:
				codeStream.invoke(Opcodes.OPC_invokestatic, this.targetMethod, null);
				break;
		}
		switch (this.targetMethod.returnType.id) {
			case TypeIds.T_void :
				codeStream.return_();
				break;
			case TypeIds.T_boolean :
			case TypeIds.T_byte :
			case TypeIds.T_char :
			case TypeIds.T_short :
			case TypeIds.T_int :
				codeStream.ireturn();
				break;
			case TypeIds.T_long :
				codeStream.lreturn();
				break;
			case TypeIds.T_float :
				codeStream.freturn();
				break;
			case TypeIds.T_double :
				codeStream.dreturn();
				break;
			default :
				codeStream.areturn();
		}
	}

	public static char[] getPrivateBridgeSelector(char[] selector, char[] roleName) {
		return CharOperation.concat(
				CharOperation.concat(IOTConstants.OT_DOLLAR_NAME, roleName),
				CharOperation.concat(PRIVATE, selector));
	}

	public static boolean isPrivateBridgeSelector(char[] selector) {
		if (!CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, selector))
			return false;
		return CharOperation.indexOf(PRIVATE, selector, true, IOTConstants.OT_DOLLAR_LEN) > -1;
	}

	public static MethodBinding findOuterAccessor(Scope scope, ReferenceBinding roleType, MethodBinding targetMethod) {
		ReferenceBinding roleClass = roleType.getRealClass();
		if (roleClass instanceof SourceTypeBinding)
			return ((SourceTypeBinding)roleClass).findOuterRoleMethodSyntheticAccessor(targetMethod);
		// for binary type find it in the team's regular methods:
		int len = targetMethod.parameters.length;
		TypeBinding[] extendedParamters = new TypeBinding[len+1];
		extendedParamters[0] = roleType;
		System.arraycopy(targetMethod.parameters, 0, extendedParamters, 1, len);
		char[] selector = getPrivateBridgeSelector(targetMethod.selector, roleType.sourceName());
		return TypeAnalyzer.findMethod(scope, roleType.enclosingType(), selector, extendedParamters);
	}

}
