/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
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
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * New for OTDT:
 *
 * This class manages the access to role fields.
 * Mainly two issues are relevant here:
 * 1. Role fields are copied and references must use an indirection via the team.
 * 2. Role fields are in the class part, wheras usually the interface part is used.
 *
 * All issues are resolved by synthetic method bindings.
 * They are similar to normal synthetic field accessors, but
 * - they are not static to allow overriding to adjust to the suitable role type (impl.inh.)
 * - they generate different code (including a cast to the class part)
 *
 * Synthetic role field accessors always reside in the enclosing team,
 *
 * This class does NOT handle the case of private fields since this class ONLY
 * handles field access from outside the role object, and private fields are not
 * visible for that kind of access.
 *
 * @author stephan
 */
public class SyntheticRoleFieldAccess extends SyntheticMethodBinding {

	static final char[] FIELD_GET_NAME = "_fieldget_".toCharArray(); //$NON-NLS-1$
	static final char[] FIELD_GET_PREFIX = CharOperation.concat(
												IOTConstants.OT_DOLLAR_NAME,
												FIELD_GET_NAME);
	static final char[] FIELD_SET_NAME = "_fieldset_".toCharArray(); //$NON-NLS-1$
	static final char[] FIELD_SET_PREFIX = CharOperation.concat(
												IOTConstants.OT_DOLLAR_NAME,
												FIELD_SET_NAME);

	char[] encodedFieldName;

	public SyntheticRoleFieldAccess(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess, ReferenceBinding declaringClass) {
		super(targetField, isReadAccess, isSuperAccess, declaringClass);
		FieldDeclaration sourceField = targetField.sourceField();
		if (sourceField != null)
			this.sourceStart = sourceField.sourceStart;
		this.modifiers &= ~ClassFileConstants.AccStatic;
		this.selector = CharOperation.concatWith( isReadAccess ? FIELD_GET_PREFIX : FIELD_SET_PREFIX,
									new char[][] {targetField.declaringClass.sourceName(),
									  targetField.name},
									 '$');
	}
	/** Recover an accessor from binary. */
	public SyntheticRoleFieldAccess(BinaryTypeBinding declaringClass,
									int               modifiers,
									char[] 			  selector,
									TypeBinding[] 	  parameters,
									TypeBinding 	  returnType)
	{
		super(declaringClass, modifiers, selector, parameters, returnType);
		if (CharOperation.prefixEquals(FIELD_GET_PREFIX, selector))
			this.purpose = FieldReadAccess;
		else
			this.purpose = FieldWriteAccess;
	}

	// hook for first ctor:
	@Override
	protected TypeBinding getReceiverParameterType(FieldBinding targetField, ReferenceBinding declaringSourceType)
	{
		if (!targetField.isStatic()) {
			ReferenceBinding targetDeclaringClass = targetField.declaringClass;
			if (targetDeclaringClass.isRole()) {
				if (TeamModel.isTeamContainingRole(declaringSourceType, targetDeclaringClass))
				{
					targetDeclaringClass = (ReferenceBinding)TeamModel.strengthenRoleType(declaringSourceType, targetDeclaringClass);
				} else {
					throw new InternalCompilerError("synth accessor created in wrong scope"); //$NON-NLS-1$
				}
				return targetDeclaringClass.getRealType();
			}
		}
		return declaringSourceType.getRealType();
	}

	/** For binary type: recover target field information from the encoded selector. */
	public FieldBinding resolvedField() {
		if (this.targetReadField != null)
			return this.targetReadField;
		if (this.targetWriteField != null)
			return this.targetWriteField;

		char[][] splitName = CharOperation.splitOn('$', this.selector); // "_OT fieldget/set Role field"
		ReferenceBinding roleType = this.declaringClass.getMemberType(splitName[2]);
		FieldBinding field = roleType.getRealClass().getField(splitName[3], true);
		if (field == null) {
			field = roleType.getRealType().getField(splitName[3], true);
			if (field == null || !field.isStatic())
				field = new ProblemFieldBinding(roleType, splitName[3], ProblemReasons.NotFound);
		}
		if (CharOperation.equals(FIELD_GET_NAME, splitName[1])) {
			this.targetReadField = field;
			this.purpose = FieldReadAccess;
		} else {
			this.targetWriteField = field;
			this.purpose = FieldWriteAccess;
		}
		return field;
	}
	public void resolveTypes() {
		LookupEnvironment env;
		try {
			env = Config.getLookupEnvironment();
		} catch (NotConfiguredException e) {
			e.logWarning("Cannot resolve types"); //$NON-NLS-1$
			return;
		}
		for (int i=0; i<this.parameters.length; i++) {
			TypeBinding param = this.parameters[i];
			if (param instanceof UnresolvedReferenceBinding)
				this.parameters[i] = ((UnresolvedReferenceBinding)param).resolve(env, false);
		}
		if (this.returnType instanceof UnresolvedReferenceBinding)
			this.returnType = ((UnresolvedReferenceBinding)this.returnType).resolve(env, false);
	}


	public void generateBodyForRead(FieldBinding fieldBinding, CodeStream codeStream) {
		if (fieldBinding.isStatic()) {
			fieldBinding = checkAdjustRoleFieldAccess(fieldBinding, codeStream); // may insert cast, too.
			codeStream.fieldAccess(Opcodes.OPC_getstatic, fieldBinding, fieldBinding.declaringClass);
			// FIXME(SH): throw new InternalCompilerError("accessor for static field not applicable.");
		} else {
			// prepare "this" and role args:
			LocalVariableBinding thisArg = createArgumentBinding(codeStream, "this".toCharArray(), fieldBinding.declaringClass.enclosingType(), 0);
			char[] argName = typeNameToLower(this.parameters[0].sourceName());
			LocalVariableBinding arg1 = createArgumentBinding(codeStream, argName, this.parameters[0], 1);

			// generate code:
			codeStream.aload_1(); // not a static accessor, positions shifted by 1.
			fieldBinding = checkAdjustRoleFieldAccess(fieldBinding, codeStream); // may insert cast, too.
			codeStream.fieldAccess(Opcodes.OPC_getfield, fieldBinding, fieldBinding.declaringClass);

			// finish args:
			if ((codeStream.generateAttributes & (ClassFileConstants.ATTR_VARS
					| ClassFileConstants.ATTR_STACK_MAP_TABLE
					| ClassFileConstants.ATTR_STACK_MAP)) == 0)
				return; // avoid NPE below
			thisArg.recordInitializationEndPC(codeStream.position);
			arg1.recordInitializationEndPC(codeStream.position);
		}
	}
	private char[] typeNameToLower(char[] typeName) {
		int len = typeName.length;
		char[] newName = new char[len];
		System.arraycopy(typeName, 0, newName, 0, len);
		newName[0] = Character.toLowerCase(typeName[0]);
		return newName;
	}
	private LocalVariableBinding createArgumentBinding(CodeStream codeStream, char[] argName, TypeBinding argType, int pos)
	{
		LocalVariableBinding argBinding = new LocalVariableBinding(argName, argType, 0, true);
		argBinding.resolvedPosition = pos;
		// declaration needed because otherwise completeCodeAttributeForSyntheticMethod
		// would refuse to generate the local variable entry
		argBinding.declaration = new Argument(argName, 0, null, 0);
		codeStream.addVisibleLocalVariable(argBinding);
		codeStream.record(argBinding);
		argBinding.recordInitializationStartPC(0);
		return argBinding;
	}

	public void generateBodyForWrite(FieldBinding fieldBinding, CodeStream codeStream) {
		if (fieldBinding.isStatic()) {
			fieldBinding = checkAdjustRoleFieldAccess(fieldBinding, codeStream);
			codeStream.load(fieldBinding.type, 1);
			codeStream.fieldAccess(Opcodes.OPC_putstatic, fieldBinding, fieldBinding.declaringClass);
		} else {
			// prepare "this" and role args:
			LocalVariableBinding thisArg = createArgumentBinding(codeStream, "this".toCharArray(), fieldBinding.declaringClass.enclosingType(), 0);
			char[] argName = typeNameToLower(this.parameters[0].sourceName());
			LocalVariableBinding arg1 = createArgumentBinding(codeStream, argName, this.parameters[0], 1);
			LocalVariableBinding arg2 = createArgumentBinding(codeStream, "value".toCharArray(), this.parameters[1], 2);

			codeStream.aload_1(); // not a static accessor, positions shifted by 1.
			fieldBinding = checkAdjustRoleFieldAccess(fieldBinding, codeStream);
			codeStream.load(fieldBinding.type, 2);
			codeStream.fieldAccess(Opcodes.OPC_putfield, fieldBinding, fieldBinding.declaringClass);

			// finish args:
			if ((codeStream.generateAttributes & (ClassFileConstants.ATTR_VARS
					| ClassFileConstants.ATTR_STACK_MAP_TABLE
					| ClassFileConstants.ATTR_STACK_MAP)) == 0)
				return; // avoid NPE below
			thisArg.recordInitializationEndPC(codeStream.position);
			arg1.recordInitializationEndPC(codeStream.position);
			arg2.recordInitializationEndPC(codeStream.position);
		}
	}

	private FieldBinding checkAdjustRoleFieldAccess(FieldBinding fieldBinding,
													CodeStream codeStream)
	{
		if (fieldBinding.declaringClass.isRole() && !fieldBinding.declaringClass.isInterface())
		{
			ReferenceBinding roleType = fieldBinding.declaringClass;
			roleType = (ReferenceBinding)TeamModel.strengthenRoleType(this.declaringClass, roleType);
			roleType = roleType.getRealClass();
			if (!fieldBinding.isStatic())
				codeStream.checkcast(roleType); // receiver cast

			fieldBinding = new FieldBinding(fieldBinding, roleType);
		}
		return fieldBinding;
	}

	/**
	 * Generate the sequence for invoking this accessor.
	 *
	 * PRE: the role instance is on the stack, for write access also the new value
	 * POST: values from PRE have been consumed, for read access the value is on the stack
	 *
	 * @param codeStream
	 */
	public void generateInvoke(CodeStream codeStream) {
		ReferenceBinding roleType = (ReferenceBinding)this.parameters[0];
		if (roleType instanceof UnresolvedReferenceBinding) {
			try {
				roleType = ((UnresolvedReferenceBinding)roleType)
									.resolve(Config.getLookupEnvironment(), false);
			} catch (NotConfiguredException e) {
				e.logWarning("Failed to generate accessor"); //$NON-NLS-1$
				return;
			}
			this.parameters[0] = roleType;
		}
		if (this.purpose == FieldReadAccess) {
			insertOuterAccess(codeStream, roleType); // role -> role,team
			codeStream.swap();                       // role,team -> team,role
			codeStream.invoke(Opcodes.OPC_invokevirtual,
							  this,					 // team,role -> result
							  this.declaringClass);
		} else {
			TypeBinding targetType = this.targetWriteField.type;
			LocalVariableBinding arg = new LocalVariableBinding("<tmp>".toCharArray(),  //$NON-NLS-1$
																targetType,
																0, false);
			arg.resolvedPosition = codeStream.maxLocals;
			arg.useFlag= LocalVariableBinding.USED;
			codeStream.record(arg);
			arg.recordInitializationStartPC(codeStream.position);
			codeStream.store(arg, false/*valueRequired*/); // role, arg -> role
			insertOuterAccess(codeStream, roleType);	   // role -> role,team
			codeStream.swap();              		 	   // role,team -> team,role
			codeStream.load(arg); 					       //     	    -> team,role,arg
			codeStream.invoke(Opcodes.OPC_invokevirtual,
					  this,					 			   //           -> <empty>
					  this.declaringClass);
			if (arg.initializationPCs != null) // null checking is asymmetric in LocalVariableBinding.
				arg.recordInitializationEndPC(codeStream.position);
		}
	}

	private void insertOuterAccess(CodeStream codeStream, ReferenceBinding roleType)
	{
		// external: use _OT$getTeam() method:
		codeStream.dup();                               // role,role
		codeStream.invokeGetTeam(roleType);             // role,team
		codeStream.checkcast(this.declaringClass);      // role,team
	}

	// =============== Static interface ===================

	/**
	 * When reading a method from byte code: do modifiers and selector denote a role field accessor?
	 */
	public static boolean isRoleFieldAccess(int modifiers, char[] selector) {
		if ((modifiers & ClassFileConstants.AccSynthetic) == 0)
			return false;
		if (CharOperation.prefixEquals(FIELD_GET_PREFIX, selector))
			return true;
		if (CharOperation.prefixEquals(FIELD_SET_PREFIX, selector))
			return true;
		return false;
	}

	/**
	 * Is given field a role field requiring a synthetic accessor?
	 * If so answer the enclosing team class.
	 *
	 * @param targetField the field
	 *
	 * @return the enclosing team type where the accessor should reside, or null.
	 */
	public static ReferenceBinding getTeamOfRoleField(FieldBinding targetField)
	{
		if (!targetField.declaringClass.isRole())
			return null;  // not a role
		if (targetField.isSynthetic())
			return null;  // synthetics shouldn't be accessed across scopes
		return targetField.declaringClass.enclosingType();
	}

	/**
	 * Get the access method for a given team and field when accessing from outside.
	 *
	 * @param enclosingTeam
	 * @param targetField
	 * @param isReadAccess
	 * @return
	 */
	public static SyntheticMethodBinding getAccessorFor(ReferenceBinding enclosingTeam,
													    FieldBinding targetField,
													    boolean isReadAccess,
													    boolean externalizedReceiver)
	{
		if (enclosingTeam.isBinaryBinding()) {
			for (MethodBinding method : enclosingTeam.methods()) {
				if (SyntheticRoleFieldAccess.isAccessFor(method, targetField, isReadAccess))
					return (SyntheticMethodBinding)method;
  			}
			return null;
  		} else {
  			SourceTypeBinding outerSrc = (SourceTypeBinding)enclosingTeam;
  			return outerSrc.addSyntheticMethod(targetField, isReadAccess, false/*isSuperAccess*/, externalizedReceiver);
  		}
	}
	/* when scanning binary access methods: check if method is indeed the required accessor for targetField. */
	private static boolean isAccessFor(MethodBinding method, FieldBinding targetField, boolean isReadAccess)
	{
		if (!(method instanceof SyntheticRoleFieldAccess))
			return false;
		SyntheticRoleFieldAccess accessor = (SyntheticRoleFieldAccess)method;
		if (accessor.resolvedField() != targetField)
			return false;
		return isReadAccess ? accessor.purpose == FieldReadAccess : accessor.purpose == FieldWriteAccess;
	}
}
