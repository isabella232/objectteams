/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeValueParameter.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AnchorUsageRanksAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;

/**
 * This class models type parameters of the for MyClass<SomeType someName>.
 * Such parameters represent values instead of types.
 *
 * The effect of this syntax is as follows:
 * + The type 'MyClass' has a field 'public final SomeType someName'
 * + Each constructor of 'MyClass' has an implicit (invisible) additional argument
 *   of type 'SomeType'. The final field is automatically initialized from this argument.
 *
 * Life-cycle of a type with value parameters:
 * <ul>
 * <li>Parser creates the TypeValueParameter
 * <li>ClassScope.buildFields() calls TypeValueParameter.resolveValueParamters(..)
 *     in order to create FieldBindings for these parameters
 * <li>ClassScope.buildFieldsAndMethods() creates and connects a SyntheticArgumentBinding
 *     for each field created in the previous step.
 * <li>ClassScope.filterTypeValueParameters() filters all TypeValueParameters from typeParameters.
 * <li>TypeDeclaration.resolve() updates the field count (updateMaxFieldCount)
 * <li>TypeDeclaration.internalAnalyzeCode() triggers analysis of TypeValueParameters in order
 *     to mark these as definitely assigned.
 * <li>ConstructorDeclaration generates synthetic arguments and initializes fields from those args:
 *     see internalGenerateCode() and generateSyntheticFieldInitializationsIfNecessary()
 * </ul>
 *
 * For applications of classes with TypeValueParameters see TypeAnchorReference.
 *
 *
 * @author stephan
 * @version $Id: TypeValueParameter.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class TypeValueParameter extends TypeParameter {

	/** The field representing this parameter. */
	public FieldBinding fieldBinding;

	public TypeValueParameter(char[] name, long position)
	{
		this.name = name;
		this.sourceStart = (int)(position>>>32);
		this.sourceEnd = (int)position;
		this.declarationSourceEnd = this.sourceEnd;
		this.declarationEnd = this.sourceEnd;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return TYPE_VALUE_PARAMETER;
	}

	public static FlowInfo analyseCode(TypeParameter[] parameters, BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		for (int i = 0; i < parameters.length; i++) {
			flowInfo = parameters[i].analyseCode(currentScope, flowContext, flowInfo);
		}
		return flowInfo;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// type value parameters are assigned by construction.
		flowInfo.markAsDefinitelyAssigned(this.fieldBinding);
		return flowInfo;
	}

	@Override
	public void resolve(ClassScope scope) {
		// noop;
	}

	/** Resolve a list of type parameters, searching for type value parameters.
	 * @param typeParameters parameters to investigate
	 * @param scope
	 * @param bindings array of field bindings with enough space to hold all natural fields
	 *        plus all fields generated from type value parameters
	 * @param knownFieldNames used for storing generated fields, too.
	 */
	public static void resolveValueParameters(
				TypeParameter[] typeParameters,
				ClassScope scope,
				FieldBinding[] bindings,
				HashtableOfObject knownFieldNames)
	{
		int count = 0;
		for (int i = 0; i < typeParameters.length; i++) {
			if (typeParameters[i] instanceof TypeValueParameter) {
				FieldBinding resolvedField = ((TypeValueParameter) typeParameters[i]).resolveAsValueParameter(scope);
				knownFieldNames.put(typeParameters[i].name, resolvedField);
				bindings[count++] = resolvedField;
			}
		}
	}

	/** Resolve this parameter to a (resolved) field binding. */
	private FieldBinding resolveAsValueParameter(ClassScope scope) {
		this.type.resolveType(scope);
		this.fieldBinding = new FieldBinding(
				this.name,
				this.type.resolvedType,
				ClassFileConstants.AccFinal | ClassFileConstants.AccPublic | ClassFileConstants.AccValueParam,
				scope.enclosingSourceType(),
				Constant.NotAConstant);
		FieldModel.getModel(this.fieldBinding).addAttribute(new AnchorUsageRanksAttribute(this.fieldBinding));
		return this.fieldBinding;
	}

	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {
		return printAsExpression(indent, output);
	}

	/** Count all TypeValuaParameters of declaration into the number of its fields. */
	public static void updateMaxFieldCount(TypeDeclaration declaration) {
		TypeParameter[] typeParameters = declaration.typeParameters;
		for (int i = 0; i < typeParameters.length; i++) {
			if (typeParameters[i] instanceof TypeValueParameter)
				declaration.maxFieldCount++;
		}

	}
}
