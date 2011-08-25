/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult.CheckPoint;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleFileCache;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeValueParameter;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticBaseCallSurrogate;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleBridgeMethodBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticRoleFieldAccess;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.FakeKind;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.PredicateGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleMigrationImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleSplitter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * OTDT changes:
 *
 * What: Team packages
 * How:  Set in constructor, use in getMemberType() to lookup role files.
 *
 * What: Creation of byte code attribute FieldTypeAnchor:
 * How:  Right after resolving the type of a field, if it is an externalized role,
 *       create a byte code attribute that stores the anchor path.
 *
 * What: Respect STATE_FINAL
 * Why:  In that state the scope is lost => some lookup cannot be performed any more.
 *
 * What: Let Dependencies control faultInTypesForFieldsAndMethods()
 * How:  Make it public, respect side effect (on demand role loading)
 *
 * What: Create type of base arg in base-guard.
 * Why:  During parsing the base type is not yet known. Fill in this
 *       information resolveTypesFor(MethodBinding)
 *
 * What: Resolve anchored types in arguments
 * Why:  Special treatment because arguments can be anchored to each other.
 *
 * What: resolveGeneratedMethod() lets a generated method catch up.
 *
 * What: Some more small changes...
 *
 */
@SuppressWarnings("unchecked")
public class SourceTypeBinding extends ReferenceBinding {
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;
	private FieldBinding[] fields;
	private MethodBinding[] methods;
	public ReferenceBinding[] memberTypes;
//{ObjectTeams: initialization added
	// readableName() will otherwise not work before Scope.buildTypeVariables()!
    public TypeVariableBinding[] typeVariables = Binding.NO_TYPE_VARIABLES;
// SH}

	public ClassScope scope;

	// Synthetics are separated into 4 categories: methods, super methods, fields, class literals and bridge methods
	// if a new category is added, also increment MAX_SYNTHETICS
	private final static int METHOD_EMUL = 0;
	private final static int FIELD_EMUL = 1;
	private final static int CLASS_LITERAL_EMUL = 2;
//{ObjectTeams: these bridges are not to be found by addSyntheticMethod(MethodBinding,boolean), use only from callout:
	private final static int ROLE_BRIDGE = 3;
	
	private final static int MAX_SYNTHETICS = 4;
/* orig:
	private final static int MAX_SYNTHETICS = 3;
  :giro */
// SH}

	HashMap[] synthetics;
	char[] genericReferenceTypeSignature;

//{ObjectTeams: managing membertypes
    public final static ReferenceBinding MultipleCasts = new SourceTypeBinding();
    static {
    	MultipleCasts.compoundName = new char[][] { "multiple".toCharArray(), "casts".toCharArray(), "required".toCharArray() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

	protected SourceTypeBinding() { super(null); } // default ctor for Singleton membertypes NoBaseclass, ProblemBaseclass

//Markus Witte}

	private SimpleLookupTable storedAnnotations = null; // keys are this ReferenceBinding & its fields and methods, value is an AnnotationHolder

public SourceTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
//{ObjectTeams:	// share model from TypeDeclaration:
	super(scope.referenceContext.getModel());
// SH}
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.referenceCompilationUnit().getFileName();
	this.modifiers = scope.referenceContext.modifiers;
//{ObjectTeams: when compiling org.objectteams.Team: set flag 'team'
	if (CharOperation.equals(compoundName, IOTConstants.ORG_OBJECTTEAMS_TEAM)) {
		this.modifiers |= ClassFileConstants.AccTeam;
		scope.referenceContext.modifiers |= ClassFileConstants.AccTeam;
	}
// SH}
	this.sourceName = scope.referenceContext.name;
	this.scope = scope;
//{ObjectTeams: ROFI create a package binding for our role files (if any):
	maybeSetTeamPackage(compoundName, fPackage, scope.environment());
// SH}

	// expect the fields & methods to be initialized correctly later
	this.fields = Binding.UNINITIALIZED_FIELDS;
	this.methods = Binding.UNINITIALIZED_METHODS;

	computeId();
}

private void addDefaultAbstractMethods() {
	if ((this.tagBits & TagBits.KnowsDefaultAbstractMethods) != 0) return;

	this.tagBits |= TagBits.KnowsDefaultAbstractMethods;
	if (isClass() && isAbstract()) {
		if (this.scope.compilerOptions().targetJDK >= ClassFileConstants.JDK1_2)
			return; // no longer added for post 1.2 targets

		ReferenceBinding[] itsInterfaces = superInterfaces();
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			MethodBinding[] defaultAbstracts = null;
			int defaultAbstractsCount = 0;
			ReferenceBinding[] interfacesToVisit = itsInterfaces;
			int nextPosition = interfacesToVisit.length;
			for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding superType = interfacesToVisit[i];
				if (superType.isValidBinding()) {
					MethodBinding[] superMethods = superType.methods();
					nextAbstractMethod: for (int m = superMethods.length; --m >= 0;) {
						MethodBinding method = superMethods[m];
						// explicitly implemented ?
						if (implementsMethod(method))
							continue nextAbstractMethod;
						if (defaultAbstractsCount == 0) {
							defaultAbstracts = new MethodBinding[5];
						} else {
							// already added as default abstract ?
							for (int k = 0; k < defaultAbstractsCount; k++) {
								MethodBinding alreadyAdded = defaultAbstracts[k];
								if (CharOperation.equals(alreadyAdded.selector, method.selector) && alreadyAdded.areParametersEqual(method))
									continue nextAbstractMethod;
							}
						}
						MethodBinding defaultAbstract = new MethodBinding(
								method.modifiers | ExtraCompilerModifiers.AccDefaultAbstract | ClassFileConstants.AccSynthetic,
								method.selector,
								method.returnType,
								method.parameters,
								method.thrownExceptions,
								this);
						if (defaultAbstractsCount == defaultAbstracts.length)
							System.arraycopy(defaultAbstracts, 0, defaultAbstracts = new MethodBinding[2 * defaultAbstractsCount], 0, defaultAbstractsCount);
						defaultAbstracts[defaultAbstractsCount++] = defaultAbstract;
					}

					if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
						int itsLength = itsInterfaces.length;
						if (nextPosition + itsLength >= interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
						nextInterface : for (int a = 0; a < itsLength; a++) {
							ReferenceBinding next = itsInterfaces[a];
							for (int b = 0; b < nextPosition; b++)
								if (next == interfacesToVisit[b]) continue nextInterface;
							interfacesToVisit[nextPosition++] = next;
						}
					}
				}
			}
			if (defaultAbstractsCount > 0) {
				int length = this.methods.length;
				System.arraycopy(this.methods, 0, this.methods = new MethodBinding[length + defaultAbstractsCount], 0, length);
				System.arraycopy(defaultAbstracts, 0, this.methods, length, defaultAbstractsCount);
				// re-sort methods
				length = length + defaultAbstractsCount;
				if (length > 1)
					ReferenceBinding.sortMethods(this.methods, 0, length);
				// this.tagBits |= TagBits.AreMethodsSorted; -- already set in #methods()
			}
		}
	}
}
/* Add a new synthetic field for <actualOuterLocalVariable>.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForInnerclass(LocalVariableBinding actualOuterLocalVariable) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(actualOuterLocalVariable);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, actualOuterLocalVariable.name),
			actualOuterLocalVariable.type,
			ClassFileConstants.AccPrivate | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.FIELD_EMUL].size());
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(actualOuterLocalVariable, synthField);
	}

	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 1;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX,
						actualOuterLocalVariable.name,
						("$" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic field for <enclosingType>.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForInnerclass(ReferenceBinding enclosingType) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(enclosingType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType,
			ClassFileConstants.AccDefault | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.FIELD_EMUL].size());

//{ObjectTeams: adjust modifiers?
		// may need to access enclosing team of role across packages, changed Default to Public
		if (enclosingType.isTeam())
			synthField.modifiers |= ClassFileConstants.AccPublic;
		// for migratable roles this$n is not final!
		if (isRole() && this.isCompatibleWith(this.scope.getOrgObjectteamsITeamMigratable())) {
			synthField.modifiers &= ~ClassFileConstants.AccFinal;
			// also, add the very method that leverages this non-finalness:
			RoleMigrationImplementor.addMigrateToTeamMethod(this.scope.referenceContext);
		}
// SH}

		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(enclosingType, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					if (this.scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5) {
						synthField.name = CharOperation.concat(
							synthField.name,
							"$".toCharArray()); //$NON-NLS-1$
						needRecheck = true;
					} else {
						this.scope.problemReporter().duplicateFieldInType(this, fieldDecl);
					}
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

//{ObjectTeams: synthetic arguments for value parameters:
private SyntheticArgumentBinding[] valueParameters = NO_SYNTH_ARGUMENTS;
public SyntheticArgumentBinding[] valueParamSynthArgs() {
	return this.valueParameters;
}
/* Add a new synthetic argument for <parameterType> (type of a value parameter).
*/
public void addSyntheticArgForValParam(TypeValueParameter param) {
	ReferenceBinding parameterType = (ReferenceBinding)param.type.resolvedType;
	SyntheticArgumentBinding synthLocal = null;
	if (this.valueParameters == null) {
		synthLocal = new SyntheticArgumentBinding(param.name, parameterType);
		this.valueParameters = new SyntheticArgumentBinding[] {synthLocal};
	} else {
		int size = this.valueParameters.length;
		int newArgIndex = size;
		for (int i = size; --i >= 0;) {
			if (this.valueParameters[i].type == parameterType)
				return; // already exists
			if (enclosingType() == parameterType)
				newArgIndex = 0;
		}
		SyntheticArgumentBinding[] newInstances = new SyntheticArgumentBinding[size + 1];
		System.arraycopy(this.valueParameters, 0, newInstances, newArgIndex == 0 ? 1 : 0, size);
		newInstances[newArgIndex] = synthLocal = new SyntheticArgumentBinding(param.name, parameterType);
		this.valueParameters = newInstances;
	}
	synthLocal.matchingField = param.fieldBinding;
}

/**
 * Compute the resolved positions for all the value parameter arguments
 */
final public void computeValueParameterSlotSizes() {

	int slotSize = 0;
	// insert enclosing instances first, followed by the outerLocals
	int enclosingInstancesCount = this.valueParameters == null ? 0 : this.valueParameters.length;
	for (int i = 0; i < enclosingInstancesCount; i++){
		SyntheticArgumentBinding argument = this.valueParameters[i];
		// position the enclosing instance synthetic arg
		argument.resolvedPosition = slotSize + 1; // shift by 1 to leave room for aload0==this
		if (slotSize + 1 > 0xFF) { // no more than 255 words of arguments
			this.scope.problemReporter().noMoreAvailableSpaceForArgument(argument, this.scope.referenceType());
		}
		if ((argument.type == TypeBinding.LONG) || (argument.type == TypeBinding.DOUBLE)){
			slotSize += 2;
		} else {
			slotSize ++;
		}
	}
}
// SH}

/* Add a new synthetic field for a class literal access.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForClassLiteral(TypeBinding targetType, BlockScope blockScope) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL] == null)
		this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL] = new HashMap(5);

	// use a different table than FIELDS, given there might be a collision between emulation of X.this$0 and X.class.
	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].get(targetType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_CLASS,
				String.valueOf(this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].size()).toCharArray()),
			blockScope.getJavaLangClass(),
			ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].size());
		this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].put(targetType, synthField);
	}
	// ensure there is not already such a field defined by the user
	FieldBinding existingField;
	if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
		TypeDeclaration typeDecl = blockScope.referenceType();
		FieldDeclaration[] typeDeclarationFields = typeDecl.fields;
		int max = typeDeclarationFields == null ? 0 : typeDeclarationFields.length;
		for (int i = 0; i < max; i++) {
			FieldDeclaration fieldDecl = typeDeclarationFields[i];
			if (fieldDecl.binding == existingField) {
				blockScope.problemReporter().duplicateFieldInType(this, fieldDecl);
				break;
			}
		}
	}
	return synthField;
}
/* Add a new synthetic field for the emulation of the assert statement.
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForAssert(BlockScope blockScope) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get("assertionEmulation"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ASSERT_DISABLED,
			TypeBinding.BOOLEAN,
			ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.FIELD_EMUL].size());
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put("assertionEmulation", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			int max = (typeDecl.fields == null) ? 0 : typeDecl.fields.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ASSERT_DISABLED,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic field for recording all enum constant values
*	Answer the new field or the existing field if one already existed.
*/
public FieldBinding addSyntheticFieldForEnumValues() {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new HashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get("enumConstantValues"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ENUM_VALUES,
			this.scope.createArrayType(this,1),
			ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.FIELD_EMUL].size());
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put("enumConstantValues", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ENUM_VALUES,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
//{ObjectTeams: set copied synthetic field:
public void addCopiedSyntheticFied(FieldBinding field) {
	if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, field.name))
	{
		int d = field.declaringClass.depth() - ((ReferenceBinding)field.type).depth();
		ReferenceBinding outer = enclosingType();
		while (d-- > 1) {
			outer = outer.enclosingType();
		}
		addSyntheticFieldForInnerclass(outer);
	} else if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_CLASS, field.name))
	{
		MethodScope dummyScope = new MethodScope(this.scope, null, false);
		ReferenceBinding dummyType = new SourceTypeBinding(new char[0][0], null, this.scope);
		FieldBinding newField = addSyntheticFieldForClassLiteral(dummyType, dummyScope);
		this.roleModel.addSyntheticFieldMapping(field, newField);
	} else if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, field.name))
	{
		char[] localName = CharOperation.subarray(field.name, TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX.length, -1);
		LocalVariableBinding local = new LocalVariableBinding(localName, field.type, field.modifiers, false/*isArgument*/);
		SyntheticArgumentBinding synthArg = ((NestedTypeBinding)this).addSyntheticArgumentAndField(local);
		FieldBinding newField = synthArg.matchingField;
		this.roleModel.addSyntheticFieldMapping(field, newField);
	}
}
//SH}
/* Add a new synthetic access method for read/write access to <targetField>.
	Answer the new method or the existing method if one already existed.
*/
//{ObjectTeams: last parameter added:
/*orig:
public SyntheticMethodBinding addSyntheticMethod(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess) {
 :giro*/
public SyntheticMethodBinding addSyntheticMethod(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess, boolean externalizedReceiver) {
// SH}
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

//{ObjectTeams: role field?
	ReferenceBinding enclosingTeam = null;
	if (   externalizedReceiver                 // must use role field accessor!
		|| targetField.declaringClass != this)  // if declaringClass==this: use regular accessor
	{
		enclosingTeam = SyntheticRoleFieldAccess.getTeamOfRoleField(targetField);
	}
	if (enclosingTeam != null) {
		if (externalizedReceiver
			? (this != enclosingTeam)                // be strict for externalized access
			: !this.isCompatibleWith(enclosingTeam)) // within instance scope compatibility suffices
		{
			// found a team but its not the current team.
			// indirection: synth method to be fetched from another team:
			SyntheticMethodBinding accessor = SyntheticRoleFieldAccess.getAccessorFor(enclosingTeam, targetField, isReadAccess, externalizedReceiver);
			if (accessor != null)
				return accessor;
			else if (this.scope != null)
				this.scope.problemReporter().missingAccessorInBinary(this, targetField);
			else
				throw new InternalCompilerError("Missing synthetic accessor for "+new String(targetField.name)); //$NON-NLS-1$
		}
	}
// SH}
	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(targetField);
	if (accessors == null) {
//{ObjectTeams: role field?
	  if (enclosingTeam != null) // this team is responsible
	  	  accessMethod = new SyntheticRoleFieldAccess(targetField, isReadAccess, isSuperAccess, this);
	  else
// orig:
		accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, isSuperAccess, this);
// SH}
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(targetField, accessors = new SyntheticMethodBinding[2]);
		accessors[isReadAccess ? 0 : 1] = accessMethod;
	} else {
		if ((accessMethod = accessors[isReadAccess ? 0 : 1]) == null) {
//{ObjectTeams: role field?
		  if (enclosingTeam != null) // this team is responsible
			  accessMethod = new SyntheticRoleFieldAccess(targetField, isReadAccess, isSuperAccess, this);
		  else
// orig:
			accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, isSuperAccess, this);
// SH}
			accessors[isReadAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}
/* Add a new synthetic method the enum type. Selector can either be 'values' or 'valueOf'.
 * char[] constants from TypeConstants must be used: TypeConstants.VALUES/VALUEOF
*/
public SyntheticMethodBinding addSyntheticEnumMethod(char[] selector) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(selector);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(this, selector);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(selector, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			accessMethod = new SyntheticMethodBinding(this, selector);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
/*
 * Add a synthetic field to handle the cache of the switch translation table for the corresponding enum type
 */
public SyntheticFieldBinding addSyntheticFieldForSwitchEnum(char[] fieldName, String key) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new HashMap(5);

	SyntheticFieldBinding synthField = (SyntheticFieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(key);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			fieldName,
			this.scope.createArrayType(TypeBinding.INT,1),
			ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant,
			this.synthetics[SourceTypeBinding.FIELD_EMUL].size());
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(key, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						fieldName,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}
/* Add a new synthetic method the enum type. Selector can either be 'values' or 'valueOf'.
 * char[] constants from TypeConstants must be used: TypeConstants.VALUES/VALUEOF
*/
public SyntheticMethodBinding addSyntheticMethodForSwitchEnum(TypeBinding enumBinding) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	char[] selector = CharOperation.concat(TypeConstants.SYNTHETIC_SWITCH_ENUM_TABLE, enumBinding.constantPoolName());
	CharOperation.replace(selector, '/', '$');
	final String key = new String(selector);
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(key);
	// first add the corresponding synthetic field
	if (accessors == null) {
		// then create the synthetic method
		final SyntheticFieldBinding fieldBinding = addSyntheticFieldForSwitchEnum(selector, key);
		accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(key, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			final SyntheticFieldBinding fieldBinding = addSyntheticFieldForSwitchEnum(selector, key);
			accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
public SyntheticMethodBinding addSyntheticMethodForEnumInitialization(int begin, int end) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = new SyntheticMethodBinding(this, begin, end);
	SyntheticMethodBinding[] accessors = new SyntheticMethodBinding[2]; 
	this.synthetics[SourceTypeBinding.METHOD_EMUL].put(accessMethod.selector, accessors);
	accessors[0] = accessMethod;
	return accessMethod;
}
/* Add a new synthetic access method for access to <targetMethod>.
 * Must distinguish access method used for super access from others (need to use invokespecial bytecode)
	Answer the new method or the existing method if one already existed.
*/
public SyntheticMethodBinding addSyntheticMethod(MethodBinding targetMethod, boolean isSuperAccess) {
//{ObjectTeams: role interfaces: use role class instead
	if (isSynthInterface()) {
		SourceTypeBinding classPart = (SourceTypeBinding)this.roleModel.getClassPartBinding();
		return classPart.addSyntheticMethod(
								new MethodBinding(targetMethod, classPart),
								isSuperAccess);
	}
// SH}
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(targetMethod);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(targetMethod, accessors = new SyntheticMethodBinding[2]);
		accessors[isSuperAccess ? 0 : 1] = accessMethod;
	} else {
		if ((accessMethod = accessors[isSuperAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
			accessors[isSuperAccess ? 0 : 1] = accessMethod;
		}
	}
	if (targetMethod.declaringClass.isStatic()) {
		if ((targetMethod.isConstructor() && targetMethod.parameters.length >= 0xFE)
				|| targetMethod.parameters.length >= 0xFF) {
			this.scope.problemReporter().tooManyParametersForSyntheticMethod(targetMethod.sourceMethod());
		}
	} else if ((targetMethod.isConstructor() && targetMethod.parameters.length >= 0xFD)
			|| targetMethod.parameters.length >= 0xFE) {
		this.scope.problemReporter().tooManyParametersForSyntheticMethod(targetMethod.sourceMethod());
	}
	return accessMethod;
}
//{ObjectTeams: add OT-specific synthetic bridges:
// (mostly empty) basecall surrogate
public SyntheticMethodBinding addSyntheticBaseCallSurrogate(MethodBinding callinMethod) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(callinMethod);
	if (accessors == null) {
		accessMethod = new SyntheticBaseCallSurrogate(callinMethod, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(callinMethod, accessors = new SyntheticMethodBinding[2]);
		accessors[1/*not super*/] = accessMethod;
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticBaseCallSurrogate(callinMethod, this);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;	
}
// bridges towards a private role method
public SyntheticMethodBinding addSyntheticRoleMethodBridge(SourceTypeBinding declaringRole, ReferenceBinding originalRole, MethodBinding targetMethod, int bridgeKind) {
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.ROLE_BRIDGE] == null)
		this.synthetics[SourceTypeBinding.ROLE_BRIDGE] = new HashMap(5);

	SyntheticMethodBinding accessMethod = (SyntheticMethodBinding) this.synthetics[SourceTypeBinding.ROLE_BRIDGE].get(targetMethod);
	if (accessMethod == null) {
		accessMethod = new SyntheticRoleBridgeMethodBinding(declaringRole, originalRole, targetMethod, bridgeKind);
		this.synthetics[SourceTypeBinding.ROLE_BRIDGE].put(targetMethod, accessMethod);
	}
	return accessMethod;	
}
// and retrieve an existing accessor:
public SyntheticMethodBinding findOuterRoleMethodSyntheticAccessor(MethodBinding targetMethod) {
	SyntheticMethodBinding accessor;
	if (this.synthetics != null && this.synthetics[ROLE_BRIDGE] != null) {
		accessor = (SyntheticMethodBinding) this.synthetics[ROLE_BRIDGE].get(targetMethod);
		if (accessor != null) {
			if (accessor.isStatic() && isRole())
				return ((SourceTypeBinding)enclosingType()).findOuterRoleMethodSyntheticAccessor(accessor);
			else 
				return accessor;
		}
	}
	if (MethodModel.isFakedMethod(targetMethod, FakeKind.BASE_FIELD_ACCESSOR))
		return ((SourceTypeBinding)enclosingType()).findOuterRoleMethodSyntheticAccessor(targetMethod);
	return null;
}
// SH}
/*
 * Record the fact that bridge methods need to be generated to override certain inherited methods
 */
public SyntheticMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge, MethodBinding targetMethod) {
	if (isInterface()) return null; // only classes & enums get bridge methods
	// targetMethod may be inherited
//{ObjectTeams: retrieve callin method's real return type:
	TypeBinding inheritedReturn= MethodModel.getReturnType(inheritedMethodToBridge);
	TypeBinding targetReturn= MethodModel.getReturnType(targetMethod);
	if (inheritedReturn.erasure() == targetReturn.erasure()
/* orig:
	if (inheritedMethodToBridge.returnType.erasure() == targetMethod.returnType.erasure()
  :giro */
// SH}
		&& inheritedMethodToBridge.areParameterErasuresEqual(targetMethod)) {
			return null; // do not need bridge method
	}
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) {
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);
	} else {
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = this.synthetics[SourceTypeBinding.METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object synthetic = synthMethods.next();
			if (synthetic instanceof MethodBinding) {
				MethodBinding method = (MethodBinding) synthetic;
				if (CharOperation.equals(inheritedMethodToBridge.selector, method.selector)
					&& inheritedMethodToBridge.returnType.erasure() == method.returnType.erasure()
					&& inheritedMethodToBridge.areParameterErasuresEqual(method)) {
						return null;
				}
			}
		}
	}

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticMethodBinding[2]);
		accessors[1] = accessMethod;
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658. Generate a bridge method if a public method is inherited
 * from a non-public class into a public class (only in 1.6 or greater)
 */
public SyntheticMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
	if (this.scope.compilerOptions().complianceLevel <= ClassFileConstants.JDK1_5) {
		return null;
	}
	if (isInterface()) return null;
	if (inheritedMethodToBridge.isAbstract() || inheritedMethodToBridge.isFinal() || inheritedMethodToBridge.isStatic()) {
		return null;
	}
	if (this.synthetics == null)
		this.synthetics = new HashMap[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) {
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new HashMap(5);
	} else {
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = this.synthetics[SourceTypeBinding.METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object synthetic = synthMethods.next();
			if (synthetic instanceof MethodBinding) {
				MethodBinding method = (MethodBinding) synthetic;
				if (CharOperation.equals(inheritedMethodToBridge.selector, method.selector)
					&& inheritedMethodToBridge.returnType.erasure() == method.returnType.erasure()
					&& inheritedMethodToBridge.areParameterErasuresEqual(method)) {
						return null;
				}
			}
		}
	}

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, this);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
boolean areFieldsInitialized() {
	return this.fields != Binding.UNINITIALIZED_FIELDS;
}
boolean areMethodsInitialized() {
	return this.methods != Binding.UNINITIALIZED_METHODS;
}
public int kind() {
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) return Binding.GENERIC_TYPE;
	return Binding.TYPE;
}

public char[] computeUniqueKey(boolean isLeaf) {
//{ObjectTeams: don't use __OT__R names for keys (TODO(SH): nested roles?)
	if (isRole()) {
		if (isClass()) {
			ReferenceBinding realType = getRealType();
			if (realType != null && realType != this)
				return realType.computeUniqueKey(isLeaf);
		}
		// role file is also a main type
		if (this.roleModel.isRoleFile())
			return super.computeUniqueKey(isLeaf);
	}
// SH}
	char[] uniqueKey = super.computeUniqueKey(isLeaf);
	if (uniqueKey.length == 2) return uniqueKey; // problem type's unique key is "L;"
	if (Util.isClassFileName(this.fileName)) return uniqueKey; // no need to insert compilation unit name for a .class file

	// insert compilation unit name if the type name is not the main type name
	int end = CharOperation.lastIndexOf('.', this.fileName);
	if (end != -1) {
		int start = CharOperation.lastIndexOf('/', this.fileName) + 1;
		char[] mainTypeName = CharOperation.subarray(this.fileName, start, end);
		start = CharOperation.lastIndexOf('/', uniqueKey) + 1;
		if (start == 0)
			start = 1; // start after L
		if (this.isMemberType()) {
			end = CharOperation.indexOf('$', uniqueKey, start);
		} else {
			// '$' is part of the type name
			end = -1;
		}
		if (end == -1)
			end = CharOperation.indexOf('<', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf(';', uniqueKey, start);
		char[] topLevelType = CharOperation.subarray(uniqueKey, start, end);
		if (!CharOperation.equals(topLevelType, mainTypeName)) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(uniqueKey, 0, start);
			buffer.append(mainTypeName);
			buffer.append('~');
			buffer.append(topLevelType);
			buffer.append(uniqueKey, end, uniqueKey.length - end);
			int length = buffer.length();
			uniqueKey = new char[length];
			buffer.getChars(0, length, uniqueKey, 0);
			return uniqueKey;
		}
	}
	return uniqueKey;
}

//{ObjectTeams: allow access from Dependencies:
public
// SH}
void faultInTypesForFieldsAndMethods() {
	// check @Deprecated annotation
	getAnnotationTagBits(); // marks as deprecated by side effect
	ReferenceBinding enclosingType = enclosingType();
	if (enclosingType != null && enclosingType.isViewedAsDeprecated() && !isDeprecated())
		this.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	fields();
	methods();

//{ObjectTeams: do not cache memberTypes.length!
// During faultInTypesForFieldsAndMethods(), memberTypes may be added (role files, on demand)
// process those new members as well.
/* orig:
	for (int i = 0, length = this.memberTypes.length; i < length; i++)
  :giro */
	for (int i = 0; i < this.memberTypes.length; i++)
		if (!this.memberTypes[i].isBinaryBinding()) // roles could be binary contained in source
//carp}
		((SourceTypeBinding) this.memberTypes[i]).faultInTypesForFieldsAndMethods();
}
// NOTE: the type of each field of a source type is resolved when needed
public FieldBinding[] fields() {
	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return this.fields;

	int failed = 0;
	FieldBinding[] resolvedFields = this.fields;
	try {
		// lazily sort fields
		if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
			int length = this.fields.length;
			if (length > 1)
				ReferenceBinding.sortFields(this.fields, 0, length);
			this.tagBits |= TagBits.AreFieldsSorted;
		}
//{ObjectTeams: don't cache length, may shrink in re-entrant executions
/*orig
		for (int i = 0, length = this.fields.length; i < length; i++) {
 */
		for (int i = 0; i < this.fields.length; i++) {
// check discouraged field in @Instantation(ALWAYS) roles:
		  if (   this.scope != null 
			  && (this.tagBits & TagBits.AnnotationInstantiation) != 0 
			  && !this.fields[i].isStatic())
			  this.scope.problemReporter().fieldInRoleWithInstantiationPolicy(this, this.fields[i]);
			  
// after compilation is finished we have no scope, can't resolve any better
//   		    resolveTypeFor would NPE!
		  int length = this.fields.length;
		  if (   this.model!=null
			  && this.model.getState() == ITranslationStates.STATE_FINAL
			  && this.fields[i].type == null)
		  {
			  this.fields[i] = null;
			  failed++;
		  } else
//SH}
			if (resolveTypeFor(this.fields[i]) == null) {
				// do not alter original field array until resolution is over, due to reentrance (143259)
				if (resolvedFields == this.fields) {
					System.arraycopy(this.fields, 0, resolvedFields = new FieldBinding[length], 0, length);
				}
				resolvedFields[i] = null;
				failed++;
			}
		}
	} finally {
		if (failed > 0) {
			// ensure fields are consistent reqardless of the error
			int newSize = resolvedFields.length - failed;
			if (newSize == 0)
				return this.fields = Binding.NO_FIELDS;

			FieldBinding[] newFields = new FieldBinding[newSize];
			for (int i = 0, j = 0, length = resolvedFields.length; i < length; i++) {
				if (resolvedFields[i] != null)
					newFields[j++] = resolvedFields[i];
			}
			this.fields = newFields;
		}
	}
	this.tagBits |= TagBits.AreFieldsComplete;
	return this.fields;
}

public void tagIndirectlyAccessibleMembers() {
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
	for (int i = 0; i < this.fields.length; i++) {
		if (!this.fields[i].isPrivate())
			this.fields[i].modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
	}
	for (int i = 0; i < this.memberTypes.length; i++) {
		if (!this.memberTypes[i].isPrivate())
			this.memberTypes[i].modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
	}
	if (this.superclass.isPrivate()) 
		if (this.superclass instanceof SourceTypeBinding)  // should always be true because private super type can only be accessed in same CU
			((SourceTypeBinding) this.superclass).tagIndirectlyAccessibleMembers();
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
public char[] genericTypeSignature() {
    if (this.genericReferenceTypeSignature == null)
    	this.genericReferenceTypeSignature = computeGenericTypeSignature(this.typeVariables);
    return this.genericReferenceTypeSignature;
}
/**
 * <param1 ... paramN>superclass superinterface1 ... superinterfaceN
 * <T:LY<TT;>;U:Ljava/lang/Object;V::Ljava/lang/Runnable;:Ljava/lang/Cloneable;:Ljava/util/Map;>Ljava/lang/Exception;Ljava/lang/Runnable;
 */
public char[] genericSignature() {
    StringBuffer sig = null;
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
	    sig = new StringBuffer(10);
	    sig.append('<');
	    for (int i = 0, length = this.typeVariables.length; i < length; i++)
	        sig.append(this.typeVariables[i].genericSignature());
	    sig.append('>');
	} else {
	    // could still need a signature if any of supertypes is parameterized
	    noSignature: if (this.superclass == null || !this.superclass.isParameterizedType()) {
		    for (int i = 0, length = this.superInterfaces.length; i < length; i++)
		        if (this.superInterfaces[i].isParameterizedType())
					break noSignature;
	        return null;
	    }
	    sig = new StringBuffer(10);
	}
	if (this.superclass != null)
		sig.append(this.superclass.genericTypeSignature());
	else // interface scenario only (as Object cannot be generic) - 65953
		sig.append(this.scope.getJavaLangObject().genericTypeSignature());
    for (int i = 0, length = this.superInterfaces.length; i < length; i++)
        sig.append(this.superInterfaces[i].genericTypeSignature());
	return sig.toString().toCharArray();
}

/**
 * Compute the tagbits for standard annotations. For source types, these could require
 * lazily resolving corresponding annotation nodes, in case of forward references.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	if ((this.tagBits & TagBits.AnnotationResolved) == 0 && this.scope != null) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0)
			this.modifiers |= ClassFileConstants.AccDeprecated;
//{ObjectTeams: @Instantation can only be applied to role classes		
		if (   (this.tagBits & TagBits.AnnotationInstantiation) != 0
			&& (!isRole() || isInterface()))
			this.scope.problemReporter().instantiationAnnotationInNonRole(typeDecl);
// SH}
	}
	return this.tagBits;
}
public MethodBinding[] getDefaultAbstractMethods() {
	int count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			count++;
	if (count == 0) return Binding.NO_METHODS;

	MethodBinding[] result = new MethodBinding[count];
	count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			result[count++] = this.methods[i];
	return result;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
//{ObjectTeams: weaker form of equality:
	/* orig:
						if (toMatch[iarg] != argumentTypes[iarg])
	  :giro*/
						if (!AnchorMapping.areTypesEqual(toMatch[iarg], argumentTypes[iarg], method))
// SH}
							continue nextMethod;
					return method;
				}
			}
		}
	} else {
		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactConstructor(argumentTypes);  // try again since the problem methods have been removed
				}
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
						if (toMatch[iarg] != argumentTypes[iarg])
							continue nextMethod;
					return method;
				}
			}
		}
	}
	return null;
}

//NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
//searches up the hierarchy as long as no potential (but not exact) match was found.
public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	// sender from refScope calls recordTypeReference(this)
	int argCount = argumentTypes.length;
	boolean foundNothing = true;

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				foundNothing = false; // inner type lookups must know that a method with this name exists
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
//{ObjectTeams: weaker form of equality:
/* orig:
						if (toMatch[iarg] != argumentTypes[iarg])
  :giro*/
						if (!AnchorMapping.areTypesEqual(toMatch[iarg], argumentTypes[iarg], method))
// SH}
							continue nextMethod;
					return method;
				}
			}
		}
	} else {
		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}

		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			// check unresolved method
			int start = (int) range, end = (int) (range >> 32);
			for (int imethod = start; imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
				}
			}
			// check dup collisions
			boolean isSource15 = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
			for (int i = start; i <= end; i++) {
				MethodBinding method1 = this.methods[i];
				for (int j = end; j > i; j--) {
					MethodBinding method2 = this.methods[j];
					boolean paramsMatch = isSource15
						? method1.areParameterErasuresEqual(method2)
						: method1.areParametersEqual(method2);
					if (paramsMatch) {
						methods();
						return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
					}
				}
			}
			nextMethod: for (int imethod = start; imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				TypeBinding[] toMatch = method.parameters;
				if (toMatch.length == argCount) {
					for (int iarg = 0; iarg < argCount; iarg++)
//{ObjectTeams: weaker form of equality:
/* orig:
						if (toMatch[iarg] != argumentTypes[iarg])
  :giro*/
						if (!AnchorMapping.areTypesEqual(toMatch[iarg], argumentTypes[iarg], method))
// SH}
							continue nextMethod;
					return method;
				}
			}
		}
	}

	if (foundNothing) {
		if (isInterface()) {
			 if (this.superInterfaces.length == 1) {
				if (refScope != null)
					refScope.recordTypeReference(this.superInterfaces[0]);
				return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
			 }
		} else if (this.superclass != null) {
			if (refScope != null)
				refScope.recordTypeReference(this.superclass);
			return this.superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}

//NOTE: the type of a field of a source type is resolved when needed
public FieldBinding getField(char[] fieldName, boolean needResolve) {
//{ObjectTeams: could be called before fields are present
//              (from ClassScope.findSupertype if supertyperef is a QualifiedTypeReference),
//              can't find any fields, but mustn't prematurely set NO_FIELDS.
	if (this.fields == Binding.UNINITIALIZED_FIELDS)
		return null;
// SH}

	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return ReferenceBinding.binarySearch(fieldName, this.fields);

	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = this.fields.length;
		if (length > 1)
			ReferenceBinding.sortFields(this.fields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
//{ObjectTeams: special case introduced by re-entrance of fields() et al:
	// be especially careful while fields are sorted but not yet complete:
	// may have nulls in this.fields, therefor cannot use binarySearch
	else {
		FieldBinding field = null;
		FieldBinding result = null;
		try {
			for (FieldBinding f : this.fields) {
				if (f != null && CharOperation.equals(f.name, fieldName)) {
					field = f;
					result = resolveTypeFor(f);
					return result;
				}
			}
		} finally {
			// copied from below with adaptations:
			// SH: for generated fields we must not assume that the field actually exists in fields.
			if (result == null) {
				// ensure fields are consistent reqardless of the error
				int newSize = this.fields.length - 1;
				if (newSize < 0) { // don't delete before finding
					this.fields = Binding.NO_FIELDS;
				} else {
					FieldBinding[] newFields = new FieldBinding[newSize];
					int index = 0;
					for (int i = 0, length = this.fields.length; i < length; i++) {
						FieldBinding f = this.fields[i];
						if (f == field) continue;
						if (index == newSize) return result; // already full => none deleted
						newFields[index++] = f;
					}
					this.fields = newFields;
				}
			}
		}
		return null;
	}
// SH}
	// always resolve anyway on source types
	FieldBinding field = ReferenceBinding.binarySearch(fieldName, this.fields);
	if (field != null) {
		FieldBinding result = null;
		try {
			result = resolveTypeFor(field);
			return result;
		} finally {
			if (result == null) {
				// ensure fields are consistent reqardless of the error
				int newSize = this.fields.length - 1;
				if (newSize == 0) {
					this.fields = Binding.NO_FIELDS;
				} else {
					FieldBinding[] newFields = new FieldBinding[newSize];
					int index = 0;
					for (int i = 0, length = this.fields.length; i < length; i++) {
						FieldBinding f = this.fields[i];
						if (f == field) continue;
						newFields[index++] = f;
					}
					this.fields = newFields;
				}
			}
		}
	}
	return null;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] getMethods(char[] selector) {
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			int start = (int) range, end = (int) (range >> 32);
			int length = end - start + 1;
			MethodBinding[] result;
			System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
			return result;
		} else {
			return Binding.NO_METHODS;
		}
	}
	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	MethodBinding[] result;
	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		int start = (int) range, end = (int) (range >> 32);
		for (int i = start; i <= end; i++) {
			MethodBinding method = this.methods[i];
			if (resolveTypesFor(method) == null || method.returnType == null) {
				methods();
				return getMethods(selector); // try again since the problem methods have been removed
			}
		}
		int length = end - start + 1;
		System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
	} else {
		return Binding.NO_METHODS;
	}
	boolean isSource15 = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
	for (int i = 0, length = result.length - 1; i < length; i++) {
		MethodBinding method = result[i];
		for (int j = length; j > i; j--) {
			boolean paramsMatch = isSource15
				? method.areParameterErasuresEqual(result[j])
				: method.areParametersEqual(result[j]);
			if (paramsMatch) {
				methods();
				return getMethods(selector); // try again since the duplicate methods have been removed
			}
		}
	}
	return result;
}
//{ObjectTeams: ROFI if it is a team, the member might be a role file still to be found:
public ReferenceBinding getMemberType(char[] name) {
	ReferenceBinding result = super.getMemberType(name);
	if (result != null) {
		// is it a member in a different file?
		if (   result.enclosingType() == this
			&& isTeam()
			&& !CharOperation.equals(result.getFileName(), this.fileName)
			&& !RoleFileCache.isRoFiCache(result)) // don't record the cache itself
		{
			// record this role file:
			getTeamModel().addKnownRoleFile(name, result);
		}
		return result;
	}
// FIXME(SH): needed for 1.7.7-otjld-5f, but causes more problems.
// Note: since State-Management has been restructured, things *might*
// be different by now.
	if (    isTeam()
		&& !StateHelper.hasState(this, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY)
		&& StateHelper.isReadyToProcess(this, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY))
	{
		if (   Dependencies.ensureBindingState(this, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY)
			&& StateHelper.hasState(this, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY))
		{
			ReferenceBinding member = getMemberType(name); // try again for copied roles.
			if (member != null && (member.tagBits & TagBits.HasMissingType) == 0)
				return member;
		}
	}
	return findTypeInTeamPackage(name);
}

ReferenceBinding findTypeInTeamPackage(char[] name) {

	ReferenceBinding result = null;
	if (this.teamPackage != null) {
	    // we might be called without setting up Dependencies, i.e. when resolving the DOM AST
	    // TODO (carp): what else can we do instead of giving up? See TPX171
	    if (!Dependencies.isSetup())
	        return null;

		TeamModel teamModel = getTeamModel();

		// need to set Parser.currentTeam, for enclosingType to be set!
		TypeDeclaration previousTeam = null;
		Parser parser = Config.getParser();
		if (parser != null) {
			previousTeam = parser.currentTeam;
			parser.currentTeam = this._teamModel.getAst();
		}

		// indirect recursive calls should not translated newly loaded roles:
		boolean oldFlag = teamModel._blockCatchup;
		teamModel._blockCatchup = true;
		try {
			// if name contains __OT__ trigger loading using the source name:
			if (RoleSplitter.isClassPartName(name)) {
				char[] srcName = RoleSplitter.getInterfacePartName(name);
				if (super.getMemberType(srcName) == null)
					this.teamPackage.getRoleType(srcName); // discard result, triggering was all we wanted.
				result = ((PackageBinding)this.teamPackage).getType0(name);
				// If role is source, the above getRoleType should have triggered
				// loading of class part, so that getType0() return a valid type.
				// If role is binary, class part must be requested separately below.
			}
			if (result == null || result == LookupEnvironment.TheNotFoundType) {
				// this might trigger Compiler.accept -> parse and start processing!
				result = this.teamPackage.getRoleType(name);
			}
		} finally {
			// end critical section which might load new role file.
			teamModel._blockCatchup = oldFlag;
		}

		if (parser != null)
			parser.currentTeam = previousTeam;
		if (   result != null
			&& result.isValidBinding())
		{
			if (result.isRole()) {
				// record the role file:
				teamModel.addKnownRoleFile(name, result);

				if (   result instanceof BinaryTypeBinding  // already processed
					|| teamModel._blockCatchup)             // process later
					return result;


				// newly accepted role must catch up!
				Dependencies.lateRolesCatchup(teamModel);

// TODO(SH): enable when needed ;-) [ tests currently pass without ]
//				if (teamModel.liftingEnv != null)
//					teamModel.liftingEnv.init(teamModel.getAst());
			}
		} else if (teamModel.getState() >= ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY) {
			return checkCopyLateRoleFile(teamModel, name);
		}
	}
	return result;
}
private ReferenceBinding checkCopyLateRoleFile(TeamModel teamModel, char[] name) {
	ReferenceBinding superTeam = superclass(); // FIXME(SH): tsuper teams
	if (   superTeam != null
		&& !TypeAnalyzer.isOrgObjectteamsTeam(superTeam)
		&& !teamModel._isCopyingLateRole
		&& !OTNameUtils.isTSuperMarkerInterface(name))
	{
		ReferenceBinding tsuperRole = superTeam.getMemberType(name);
		if (   tsuperRole != null && tsuperRole.isRole() && tsuperRole.isValidBinding()
			&& !tsuperRole.isLocalType())
		{
			return CopyInheritance.copyLateRole(teamModel.getAst(), tsuperRole);
		}
	}
	return null;
}
//SH}
/* Answer the synthetic field for <actualOuterLocalVariable>
*	or null if one does not exist.
*/
public FieldBinding getSyntheticField(LocalVariableBinding actualOuterLocalVariable) {
	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null) return null;
	return (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(actualOuterLocalVariable);
}
//{ObjectTeams: retrieve synthetic field by its name
/** synthetic field for outer local */
public FieldBinding getSyntheticOuterLocal(char[] fieldName) {
	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null) return null;
	HashMap outerLocalMap = this.synthetics[SourceTypeBinding.FIELD_EMUL];
	for(Object element : outerLocalMap.values()) {
		FieldBinding field = (FieldBinding)element;
		if (CharOperation.equals(field.name, fieldName))
			return field;
	}
	return null;
}
/** synthetic field for class literal */
public FieldBinding getSyntheticClassLiteral(char[] fieldName) {
	if (this.synthetics == null || this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL] == null) return null;
	HashMap classLiteralMap = this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL];
	for(Object element : classLiteralMap.values()) {
		FieldBinding field = (FieldBinding)element;
		if (CharOperation.equals(field.name, fieldName))
			return field;
	}
	return null;
}
// SH}
/* Answer the synthetic field for <targetEnclosingType>
*	or null if one does not exist.
*/
public FieldBinding getSyntheticField(ReferenceBinding targetEnclosingType, boolean onlyExactMatch) {

	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null) return null;
	FieldBinding field = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(targetEnclosingType);
	if (field != null) return field;

	// type compatibility : to handle cases such as
	// class T { class M{}}
	// class S extends T { class N extends M {}} --> need to use S as a default enclosing instance for the super constructor call in N().
	if (!onlyExactMatch){
		Iterator accessFields = this.synthetics[SourceTypeBinding.FIELD_EMUL].values().iterator();
		while (accessFields.hasNext()) {
			field = (FieldBinding) accessFields.next();
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, field.name)
				&& field.type.findSuperTypeOriginatingFrom(targetEnclosingType) != null)
					return field;
		}
	}
	return null;
}
/*
 * Answer the bridge method associated for an  inherited methods or null if one does not exist
 */
public SyntheticMethodBinding getSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
	if (this.synthetics == null) return null;
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) return null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) return null;
	return accessors[1];
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#initializeDeprecatedAnnotationTagBits()
 */
public void initializeDeprecatedAnnotationTagBits() {
	if ((this.tagBits & TagBits.DeprecatedAnnotationResolved) == 0) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveDeprecatedAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
			this.tagBits |= TagBits.DeprecatedAnnotationResolved;
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0) {
			this.modifiers |= ClassFileConstants.AccDeprecated;
		}
	}
}

// ensure the receiver knows its hierarchy & fields/methods so static imports can be resolved correctly
// see bug 230026
//{ObjectTeams: accessible across AbstractOTReferenceBinding (sitting in a different package):
protected
// SH}
void initializeForStaticImports() {
	if (this.scope == null) return; // already initialized

	if (this.superInterfaces == null)
		this.scope.connectTypeHierarchy();
	this.scope.buildFields();
	this.scope.buildMethods();
}

/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {

	if (this == otherType) return true;
	if (otherType == null) return false;
	switch(otherType.kind()) {

		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE:
			return ((WildcardBinding) otherType).boundCheck(this);

		case Binding.PARAMETERIZED_TYPE :
			if ((otherType.tagBits & TagBits.HasDirectWildcard) == 0 && (!isMemberType() || !otherType.isMemberType()))
				return false; // should have been identical
			ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
			if (this != otherParamType.genericType())
				return false;
			if (!isStatic()) { // static member types do not compare their enclosing
            	ReferenceBinding enclosing = enclosingType();
            	if (enclosing != null) {
            		ReferenceBinding otherEnclosing = otherParamType.enclosingType();
            		if (otherEnclosing == null) return false;
            		if ((otherEnclosing.tagBits & TagBits.HasDirectWildcard) == 0) {
						if (enclosing != otherEnclosing) return false;
            		} else {
            			if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) return false;
            		}
            	}
			}
			int length = this.typeVariables == null ? 0 : this.typeVariables.length;
			TypeBinding[] otherArguments = otherParamType.arguments;
			int otherLength = otherArguments == null ? 0 : otherArguments.length;
			if (otherLength != length)
				return false;
			for (int i = 0; i < length; i++)
				if (!this.typeVariables[i].isTypeArgumentContainedBy(otherArguments[i]))
					return false;
			return true;

		case Binding.RAW_TYPE :
	        return otherType.erasure() == this;
	}
	return false;
}
public boolean isGenericType() {
    return this.typeVariables != Binding.NO_TYPE_VARIABLES;
}
public boolean isHierarchyConnected() {
	return (this.tagBits & TagBits.EndHierarchyCheck) != 0;
}
public ReferenceBinding[] memberTypes() {
	return this.memberTypes;
}

public boolean hasMemberTypes() {
    return this.memberTypes.length > 0;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] methods() {
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return this.methods;

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}

	int failed = 0;
	MethodBinding[] resolvedMethods = this.methods;
	try {
//{ObjectTeams: don't cache length, resolving callin creates base call surrogate
/* orig:
		for (int i = 0, length = this.methods.length; i < length; i++) {
  :giro */
		for (int i = 0; i < this.methods.length; i++) {
			int length = this.methods.length;
// SH}
			if (resolveTypesFor(this.methods[i]) == null) {
				// do not alter original method array until resolution is over, due to reentrance (143259)
				if (resolvedMethods == this.methods) {
					System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
				}
//{ObjectTeams: may need to grow resolvedMethods:
				if (i >= resolvedMethods.length) {
					int l = resolvedMethods.length;
					System.arraycopy(resolvedMethods, 0, resolvedMethods = new MethodBinding[length], 0, l);
				}
// SH}
				resolvedMethods[i] = null; // unable to resolve parameters
				failed++;
			}
		}
//{ObjectTeams:  in state final we lost the scope, cannot use code below any more.
		if (isStateFinal())
			return this.methods; // relies on finally block below.
// SH}

		// find & report collision cases
		boolean complyTo15OrAbove = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		boolean compliance16 = this.scope.compilerOptions().complianceLevel == ClassFileConstants.JDK1_6;

//{ObjectTeams: this.methods may have changed size, use the appropriate length:
/* orig:
		for (int i = 0, length = this.methods.length; i < length; i++) {
  :giro */
		for (int i = 0, length = resolvedMethods.length; i < length; i++) {
// SH}
			int severity = ProblemSeverities.Error;
			MethodBinding method = resolvedMethods[i];
			if (method == null)
				continue;
			char[] selector = method.selector;
			AbstractMethodDeclaration methodDecl = null;
			nextSibling: for (int j = i + 1; j < length; j++) {
				MethodBinding method2 = resolvedMethods[j];
				if (method2 == null)
					continue nextSibling;
				if (!CharOperation.equals(selector, method2.selector))
					break nextSibling; // methods with same selector are contiguous

				if (complyTo15OrAbove) {
					if (method.areParameterErasuresEqual(method2)) {
						// we now ignore return types in 1.7 when detecting duplicates, just as we did before 1.5 
						// Only in 1.6, we have to make sure even return types are different
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
						if (compliance16 && method.returnType != null && method2.returnType != null) {
							if (method.returnType.erasure() != method2.returnType.erasure()) {
								// check to see if the erasure of either method is equal to the other
								// if not, then change severity to WARNING
								TypeBinding[] params1 = method.parameters;
								TypeBinding[] params2 = method2.parameters;
								int pLength = params1.length;
								TypeVariableBinding[] vars = method.typeVariables;
								TypeVariableBinding[] vars2 = method2.typeVariables;
								boolean equalTypeVars = vars == vars2;
								MethodBinding subMethod = method2;
								if (!equalTypeVars) {
									MethodBinding temp = method.computeSubstitutedMethod(method2, this.scope.environment());
									if (temp != null) {
										equalTypeVars = true;
										subMethod = temp;
									}
								}
								boolean equalParams = method.areParametersEqual(subMethod);
								if (equalParams && equalTypeVars) {
									// duplicates regardless of return types
								} else if (vars != Binding.NO_TYPE_VARIABLES && vars2 != Binding.NO_TYPE_VARIABLES) {
									// both have type arguments. Erasure of signature of one cannot be equal to signature of other
									severity = ProblemSeverities.Warning;
								} else if (pLength > 0) {
									int index = pLength;
									// is erasure of signature of m2 same as signature of m1?
									for (; --index >= 0;) {
										if (params1[index] != params2[index].erasure())
											break;
										if (params1[index] == params2[index]) {
											TypeBinding type = params1[index].leafComponentType();
											if (type instanceof SourceTypeBinding && type.typeVariables() != Binding.NO_TYPE_VARIABLES) {
												index = pLength; // handle comparing identical source types like X<T>... its erasure is itself BUT we need to answer false
												break;
											}
										}
									}
									if (index >= 0 && index < pLength) {
										// is erasure of signature of m1 same as signature of m2?
										for (index = pLength; --index >= 0;)
											if (params1[index].erasure() != params2[index])
												break;
										
									}
									if (index >= 0) {
										// erasure of neither is equal to signature of other
										severity = ProblemSeverities.Warning;
									}
								} else if (pLength != 0){
									severity = ProblemSeverities.Warning;
								} // pLength = 0 automatically makes erasure of arguments one equal to arguments of other.
							}
							// else return types also equal. All conditions satisfied
							// to give error in 1.6 compliance as well.
						}
					} else {
						continue nextSibling;
					}
				} else if (!method.areParametersEqual(method2)) {
					// prior to 1.5, parameters identical meant a collision case
					continue nextSibling;
				}
				// otherwise duplicates / name clash
				boolean isEnumSpecialMethod = isEnum() && (CharOperation.equals(selector,TypeConstants.VALUEOF) || CharOperation.equals(selector,TypeConstants.VALUES));
				// report duplicate
				boolean removeMethod2 = (severity == ProblemSeverities.Error) ? true : false; // do not remove if in 1.6 and just a warning given
				if (methodDecl == null) {
					methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost & may still be null if method is special
					if (methodDecl != null && methodDecl.binding != null) { // ensure its a valid user defined method
						boolean removeMethod = method.returnType == null && method2.returnType != null;
						if (isEnumSpecialMethod) {
							this.scope.problemReporter().duplicateEnumSpecialMethod(this, methodDecl);
							// remove user defined methods & keep the synthetic
							removeMethod = true;
						} else {
							this.scope.problemReporter().duplicateMethodInType(this, methodDecl, method.areParametersEqual(method2), severity);
						}
						if (removeMethod) {
							removeMethod2 = false;
							methodDecl.binding = null;
							// do not alter original method array until resolution is over, due to reentrance (143259)
							if (resolvedMethods == this.methods)
								System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
							resolvedMethods[i] = null;
							failed++;
						}
					}
				}
				AbstractMethodDeclaration method2Decl = method2.sourceMethod();
				if (method2Decl != null && method2Decl.binding != null) { // ensure its a valid user defined method
					if (isEnumSpecialMethod) {
						this.scope.problemReporter().duplicateEnumSpecialMethod(this, method2Decl);
						removeMethod2 = true;
					} else {
						this.scope.problemReporter().duplicateMethodInType(this, method2Decl, method.areParametersEqual(method2), severity);
					}
					if (removeMethod2) {
						method2Decl.binding = null;
						// do not alter original method array until resolution is over, due to reentrance (143259)
						if (resolvedMethods == this.methods)
							System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
						resolvedMethods[j] = null;
						failed++;
					}
				}
			}
			if (method.returnType == null && resolvedMethods[i] != null) { // forget method with invalid return type... was kept to detect possible collisions
				methodDecl = method.sourceMethod();
				if (methodDecl != null)
					methodDecl.binding = null;
				// do not alter original method array until resolution is over, due to reentrance (143259)
				if (resolvedMethods == this.methods)
					System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
				resolvedMethods[i] = null;
				failed++;
			}
		}
	} finally {
		if (failed > 0) {
			int newSize = resolvedMethods.length - failed;
			if (newSize == 0) {
				this.methods = Binding.NO_METHODS;
			} else {
				MethodBinding[] newMethods = new MethodBinding[newSize];
				for (int i = 0, j = 0, length = resolvedMethods.length; i < length; i++)
					if (resolvedMethods[i] != null)
						newMethods[j++] = resolvedMethods[i];
				this.methods = newMethods;
			}
		}

		// handle forward references to potential default abstract methods
		addDefaultAbstractMethods();
		this.tagBits |= TagBits.AreMethodsComplete;
	}
	return this.methods;
}
public FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return field;

	if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
		if ((field.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
			field.modifiers |= ClassFileConstants.AccDeprecated;
	}
	if (isViewedAsDeprecated() && !field.isDeprecated())
		field.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	if (hasRestrictedAccess())
		field.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
	FieldDeclaration[] fieldDecls = this.scope.referenceContext.fields;
	int length = fieldDecls == null ? 0 : fieldDecls.length;
	for (int f = 0; f < length; f++) {
		if (fieldDecls[f].binding != field)
			continue;

			MethodScope initializationScope = field.isStatic()
				? this.scope.referenceContext.staticInitializerScope
				: this.scope.referenceContext.initializerScope;
			FieldBinding previousField = initializationScope.initializedField;
			try {
				initializationScope.initializedField = field;
				FieldDeclaration fieldDecl = fieldDecls[f];
				TypeBinding fieldType =
					fieldDecl.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT
						? initializationScope.environment().convertToRawType(this, false /*do not force conversion of enclosing types*/) // enum constant is implicitly of declaring enum type
						: fieldDecl.type.resolveType(initializationScope, true /* check bounds*/);
				field.type = fieldType;
				field.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
				if (fieldType == null) {
					fieldDecl.binding = null;
					return null;
				}
				if (fieldType == TypeBinding.VOID) {
					this.scope.problemReporter().variableTypeCannotBeVoid(fieldDecl);
					fieldDecl.binding = null;
					return null;
				}
				if (fieldType.isArrayType() && ((ArrayBinding) fieldType).leafComponentType == TypeBinding.VOID) {
					this.scope.problemReporter().variableTypeCannotBeVoidArray(fieldDecl);
					fieldDecl.binding = null;
					return null;
				}
				if ((fieldType.tagBits & TagBits.HasMissingType) != 0) {
					field.tagBits |= TagBits.HasMissingType;
				}
				TypeBinding leafType = fieldType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding)leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
					field.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				}
			} finally {
			    initializationScope.initializedField = previousField;
			}
//{ObjectTeams: copy-inherited fields and anchored types:
			if (fieldDecls[f].getKind() != AbstractVariableDeclaration.ENUM_CONSTANT) {
				if (fieldDecls[f].type == null)  // should not happen for non-enum types
					throw new InternalCompilerError("Field "+fieldDecls[f]+" has no type in "+this);
	
				field.copyInheritanceSrc = fieldDecls[f].copyInheritanceSrc;
				field.maybeSetFieldTypeAnchorAttribute();
				// anchored to tthis?
				field.type = RoleTypeCreator.maybeWrapUnqualifiedRoleType(this.scope, field.type, fieldDecls[f].type);
				if (field.couldBeTeamAnchor()) {
					// link decl and binding via model
					// for early resolving from TeamAnchor.hasSameBestNameAs()
					FieldModel.getModel(fieldDecls[f]).setBinding(field);
				}
			}
			// need role field bridges?
			if (   isRole() 
				&& ((field.modifiers & ClassFileConstants.AccPrivate) != 0) 
				&& !CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, field.name)) 
			{
				MethodBinding inner;
				ReferenceBinding originalRole = field.declaringClass;
				if (field.copyInheritanceSrc != null)
					originalRole = field.copyInheritanceSrc.declaringClass;
				inner = FieldModel.getDecapsulatingFieldAccessor(this, field, true/*isGetter*/);
				((SourceTypeBinding) enclosingType()).addSyntheticRoleMethodBridge(this, originalRole, inner, SyntheticMethodBinding.RoleMethodBridgeOuter);
				if (!field.isFinal()) { // no setter for final (includes all static role fields)
									    // otherwise we would have to handle different signatures (w/ w/o role arg), which we currently don't
					inner = FieldModel.getDecapsulatingFieldAccessor(this, field, false/*isGetter*/);
					((SourceTypeBinding) enclosingType()).addSyntheticRoleMethodBridge(this, originalRole, inner, SyntheticMethodBinding.RoleMethodBridgeOuter);
				}
			}
// SH}
		return field;
	}
	return null; // should never reach this point
}
public MethodBinding resolveTypesFor(MethodBinding method) {
//{ObjectTeams: overload with one more parameter:
	return resolveTypesFor(method, false);
}
public MethodBinding resolveTypesFor(MethodBinding method, boolean fromSynthetic) {
// SH}
	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return method;
//{ObjectTeams:  in state final we lost the scope, cannot use code below any more.
    if (isStateFinal())
    	return method;
//SH}

	if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
		if ((method.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
			method.modifiers |= ClassFileConstants.AccDeprecated;
	}
	if (isViewedAsDeprecated() && !method.isDeprecated())
		method.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	if (hasRestrictedAccess())
		method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;

	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	if (methodDecl == null) return null; // method could not be resolved in previous iteration

//{ObjectTeams: pre-fetch (resolve) potential type anchors:
	boolean[] anchorFlags = null; // keep track so we don't doubly resolve
	if (methodDecl.arguments != null) {
		anchorFlags = TypeAnchorReference.fetchAnchorFlags(methodDecl.arguments, methodDecl.typeParameters());
		for (int i = 0; i < anchorFlags.length; i++)
			if (anchorFlags[i])
				methodDecl.arguments[i].type.resolveType(methodDecl.scope, true /* check bounds*/);
	}
// SH}

	TypeParameter[] typeParameters = methodDecl.typeParameters();
	if (typeParameters != null) {
		methodDecl.scope.connectTypeVariables(typeParameters, true);
		// Perform deferred bound checks for type variables (only done after type variable hierarchy is connected)
		for (int i = 0, paramLength = typeParameters.length; i < paramLength; i++)
//{ObjectTeams: more to connect:
		{
// orig:
			typeParameters[i].checkBounds(methodDecl.scope);
// :giro
			typeParameters[i].connectTypeAnchors(methodDecl.scope);
		}
// SH}
	}
	TypeReference[] exceptionTypes = methodDecl.thrownExceptions;
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		method.thrownExceptions = new ReferenceBinding[size];
		int count = 0;
		ReferenceBinding resolvedExceptionType;
		for (int i = 0; i < size; i++) {
			resolvedExceptionType = (ReferenceBinding) exceptionTypes[i].resolveType(methodDecl.scope, true /* check bounds*/);
			if (resolvedExceptionType == null)
				continue;
			if (resolvedExceptionType.isBoundParameterizedType()) {
				methodDecl.scope.problemReporter().invalidParameterizedExceptionType(resolvedExceptionType, exceptionTypes[i]);
				continue;
			}
			if (resolvedExceptionType.findSuperTypeOriginatingFrom(TypeIds.T_JavaLangThrowable, true) == null) {
				if (resolvedExceptionType.isValidBinding()) {
					methodDecl.scope.problemReporter().cannotThrowType(exceptionTypes[i], resolvedExceptionType);
					continue;
				}
			}
			if ((resolvedExceptionType.tagBits & TagBits.HasMissingType) != 0) {
				method.tagBits |= TagBits.HasMissingType;
			}
			method.modifiers |= (resolvedExceptionType.modifiers & ExtraCompilerModifiers.AccGenericSignature);
			method.thrownExceptions[count++] = resolvedExceptionType;
		}
		if (count < size)
			System.arraycopy(method.thrownExceptions, 0, method.thrownExceptions = new ReferenceBinding[count], 0, count);
	}
	final boolean reportUnavoidableGenericTypeProblems = this.scope.compilerOptions().reportUnavoidableGenericTypeProblems;
	boolean foundArgProblem = false;
	Argument[] arguments = methodDecl.arguments;
	if (arguments != null) {
		int size = arguments.length;
		method.parameters = Binding.NO_PARAMETERS;
		TypeBinding[] newParameters = new TypeBinding[size];
		for (int i = 0; i < size; i++) {
			Argument arg = arguments[i];
			if (arg.annotations != null) {
				method.tagBits |= TagBits.HasParameterAnnotations;
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
			boolean deferRawTypeCheck = !reportUnavoidableGenericTypeProblems && !method.isConstructor() && (arg.type.bits & ASTNode.IgnoreRawTypeCheck) == 0;
			TypeBinding parameterType;
			if (deferRawTypeCheck) {
				arg.type.bits |= ASTNode.IgnoreRawTypeCheck;
			}
//{ObjectTeams: update special argument:
			// first arg (base) in base predicates:
			final boolean isBaseGuard = CharOperation.prefixEquals(IOTConstants.BASE_PREDICATE_PREFIX, method.selector);
			if (   i == 0
				&& isBaseGuard)
			{
				if (!PredicateGenerator.createBaseArgType(methodDecl, arg)) {
					foundArgProblem = true;
					continue;
				}
			}
// SH}
//{ObjectTeams: option to roll back some problems
			CheckPoint cp = this.scope.referenceContext.compilationResult.getCheckPoint(methodDecl);
			// prepare baseclass decapsulation if its
			// - the first arg in a role constructor
			if (   isFirstRoleCtorArg(methodDecl, method, i)
				&& !arg.type.getBaseclassDecapsulation().isAllowed())
			{
				arg.type.setBaseclassDecapsulation(DecapsulationState.ALLOWED);
			}
			DecapsulationState previousDecapsulation = arg.type.getBaseclassDecapsulation();
			// check if this argument was checked early as a type anchor for another argument:
			try {
				parameterType = 
					 (anchorFlags[i] && arg.type.resolvedType.isValidBinding()) ? 
				 				arg.type.resolvedType :
					 			arg.type.resolveType(methodDecl.scope, true /* check bounds*/);
// orig:
//				parameterType = arg.type.resolveType(methodDecl.scope, true /* check bounds*/); 
			} finally {
				if (deferRawTypeCheck) { 
					arg.type.bits &= ~ASTNode.IgnoreRawTypeCheck;
				}
			}
// :giro
			// try anchored types (var.Type arg):
            if (   (parameterType == null || parameterType instanceof MissingTypeBinding)
            	&& !(arg.type instanceof LiftingTypeReference))
            {
                TypeBinding anchoredType = RoleTypeCreator.getTypeAnchoredToParameter(
                		arg.type,
                        arguments, i,
                        methodDecl.scope,
                        cp);
                if (anchoredType != null && anchoredType.isValidBinding())
                	parameterType = anchoredType; // only use if no error
                	// in this case getTypeAnchoredToParameter() has performed a roleBack.
            }
            // if decapsulation was actually used, try if that was legal:
            if (arg.type.getBaseclassDecapsulation().isAllowed() != previousDecapsulation.isAllowed())
            {
            	ReferenceBinding declaredBaseclass = method.declaringClass.baseclass();
            	if (   declaredBaseclass == null
            		|| !parameterType.isCompatibleWith(declaredBaseclass))
            	{
            		ProblemReferenceBinding problemBinding = new ProblemReferenceBinding(
            				parameterType.readableName(), (ReferenceBinding)parameterType, ProblemReasons.NotVisible);
            		this.scope.problemReporter().invalidType(arg.type, problemBinding);
            	}
            }
// SH}
			if (parameterType == null) {
				foundArgProblem = true;
			} else if (parameterType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoid(this, methodDecl, arg);
				foundArgProblem = true;
			} else {
				if ((parameterType.tagBits & TagBits.HasMissingType) != 0) {
					method.tagBits |= TagBits.HasMissingType;
				}
				TypeBinding leafType = parameterType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				newParameters[i] = parameterType;
//{ObjectTeams: don't overwrite existing binding (only fill in detail) 
// Note: possibly a binding has been created along this call chain:
// TypeParameter.connectTypeAnchors() -> TypeAnchorReference.[resolveType()->findVariable()] ->  RoleTypeCreator.resolveTypeAnchoredToArgument() -> Argument.bind()
			  if (arg.binding != null)
				arg.binding.type = parameterType; // only add this missing info
			  else
// SH}
				arg.binding = new LocalVariableBinding(arg, parameterType, arg.modifiers, true);
			}
		}
		// only assign parameters if no problems are found
		if (!foundArgProblem) {
			method.parameters = newParameters;
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	if (this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_7) {
		if ((method.tagBits & TagBits.AnnotationSafeVarargs) != 0) {
			if (!method.isVarargs()) {
				methodDecl.scope.problemReporter().safeVarargsOnFixedArityMethod(method);
			} else if (!method.isStatic() && !method.isFinal() && !method.isConstructor()) {
				methodDecl.scope.problemReporter().safeVarargsOnNonFinalInstanceMethod(method);
			}
		} else if (method.parameters != null && method.parameters.length > 0 && method.isVarargs()) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
			if (!method.parameters[method.parameters.length - 1].isReifiable()) {
				methodDecl.scope.problemReporter().possibleHeapPollutionFromVararg(methodDecl.arguments[methodDecl.arguments.length - 1]);
			}
		}
	}

	boolean foundReturnTypeProblem = false;
	if (!method.isConstructor()) {
		TypeReference returnType = methodDecl instanceof MethodDeclaration
			? ((MethodDeclaration) methodDecl).returnType
			: null;
		if (returnType == null) {
			methodDecl.scope.problemReporter().missingReturnType(methodDecl);
			method.returnType = null;
			foundReturnTypeProblem = true;
		} else {
//{ObjectTeams: option to roll back problems:
			CheckPoint cp = this.scope.referenceContext.compilationResult.getCheckPoint(methodDecl);
//	 SH}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
			boolean deferRawTypeCheck = !reportUnavoidableGenericTypeProblems && (returnType.bits & ASTNode.IgnoreRawTypeCheck) == 0;
			TypeBinding methodType;
			if (deferRawTypeCheck) {
				returnType.bits |= ASTNode.IgnoreRawTypeCheck;
			}
			try {
				methodType = returnType.resolveType(methodDecl.scope, true /* check bounds*/);
			} finally {
				if (deferRawTypeCheck) { 
					returnType.bits &= ~ASTNode.IgnoreRawTypeCheck;
				}
			}
//{ObjectTeams: might need a method parameter as type anchor:
			if (   arguments != null
				&& (   methodType == null
					|| methodType.problemId() == ProblemReasons.NotFound)) // FIXME(SH): shouldn't occur (only null or valid).
			{
				TypeBinding adjustedMethodType = RoleTypeCreator.getTypeAnchoredToParameter(
							returnType,
	                        arguments, arguments.length,
	                        methodDecl.scope,
	                        cp);
                if (adjustedMethodType != null && adjustedMethodType.isValidBinding())
                	methodType = adjustedMethodType;  // adopt alternative proposal
                    // in this case getTypeAnchoredToParameter() has performed a roleBack.
			}
// SH}
			if (methodType == null) {
				foundReturnTypeProblem = true;
			} else if (methodType.isArrayType() && ((ArrayBinding) methodType).leafComponentType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().returnTypeCannotBeVoidArray((MethodDeclaration) methodDecl);
				foundReturnTypeProblem = true;
			} else {
//{ObjectTeams: generalize return of callin method?
				if (method.isCallin() && methodType.isBaseType())
					methodType = MethodSignatureEnhancer.generalizeReturnType((MethodDeclaration)methodDecl, methodType);
// SH}
				if ((methodType.tagBits & TagBits.HasMissingType) != 0) {
					method.tagBits |= TagBits.HasMissingType;
				}
				method.returnType = methodType;
				TypeBinding leafType = methodType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
			}
		}
	}
	if (foundArgProblem) {
		methodDecl.binding = null;
		method.parameters = Binding.NO_PARAMETERS; // see 107004
		// nullify type parameter bindings as well as they have a backpointer to the method binding
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=81134)
		if (typeParameters != null)
			for (int i = 0, length = typeParameters.length; i < length; i++)
				typeParameters[i].binding = null;
		return null;
	}
//{ObjectTeams: all types in resolved signatures of non-synthetic methods may require wrapping:
	if (fromSynthetic) {
		 method.modifiers |= ClassFileConstants.AccSynthetic;
	} else {
		if (this.isRole() || this.isTeam())
			RoleTypeCreator.wrapTypesInMethodDeclSignature(method, methodDecl);
	}
// SH}
	if (foundReturnTypeProblem)
		return method; // but its still unresolved with a null return type & is still connected to its method declaration

	method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
//{ObjectTeams: need role method bridges?
	int abstractStatic = ClassFileConstants.AccAbstract | ClassFileConstants.AccStatic;
	if (   isRole() 
		&& (   (method.modifiers & ClassFileConstants.AccPrivate) != 0
			|| ((method.modifiers & abstractStatic) == abstractStatic) && !method.declaringClass.isInterface())
		&& !CharOperation.prefixEquals(IOTConstants.OT_DOLLAR_NAME, method.selector)
		&& !methodDecl.isConstructor())
	{
		ReferenceBinding originalRole = this;
		if (method.copyInheritanceSrc != null)
			originalRole = method.copyInheritanceSrc.declaringClass;
		else if (method.overriddenTSupers != null)
			originalRole = method.overriddenTSupers[0].declaringClass;
		MethodBinding inner = addSyntheticRoleMethodBridge(this, originalRole, method, SyntheticMethodBinding.RoleMethodBridgeInner);
		((SourceTypeBinding) enclosingType()).addSyntheticRoleMethodBridge(this, originalRole, inner, SyntheticMethodBinding.RoleMethodBridgeOuter);
	}
// SH}
	return method;
}
//{ObjectTeams: helper to find args allowing baseclass decapsulation:
private boolean isFirstRoleCtorArg(AbstractMethodDeclaration methodDecl, MethodBinding method, int i)
{
	return methodDecl.isConstructor()
	    && i==0
	    && method.declaringClass.isRole();
}
// SH}
public AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
	if (forceInitialization)
		binding.getAnnotationTagBits(); // ensure annotations are up to date
	return super.retrieveAnnotationHolder(binding, false);
}
public void setFields(FieldBinding[] fields) {
	this.fields = fields;
//{ObjectTeams: // integrate pendingField
	if (this.pendingField != null) {
		int l= fields.length;
		System.arraycopy(fields, 0, this.fields= new FieldBinding[l+1], 1, l);
		this.fields[0]= this.pendingField;
		this.pendingField= null;
	}
	// in case getField() (or similar func) has already been called reset flag:
	this.tagBits &= ~(TagBits.AreFieldsSorted | TagBits.AreFieldsComplete);
// SH}
}
public void setMethods(MethodBinding[] methods) {
//{ObjectTeams: baseCallSurrogate might be stored before regular methods are created
    // happens when binary role with static callin method is loaded before STATE_BINDINGS_COMPLETE
	if (this.pendingMethods != null)	{
		assert (this.tagBits & TagBits.AreMethodsSorted) == 0 : "setMethods after sorting"; //$NON-NLS-1$
		int len1 = this.pendingMethods.size();
		int len2 = methods.length;
		this.methods = new MethodBinding[len1+len2];
		this.pendingMethods.toArray(this.methods);
		System.arraycopy(methods, 0, this.methods, len1, len2);
		this.pendingMethods = null;
	} else
// SH}
	this.methods = methods;
}
//{ObjectTeams: adding generated elements
FieldBinding pendingField= null;
public void addField(FieldBinding fieldBinding) {
	if (this.fields == Binding.UNINITIALIZED_FIELDS) {
		assert this.pendingField == null: "Can only have one pending field";  //$NON-NLS-1$
		this.pendingField= fieldBinding;
		return;
	}
	int size = this.fields.length;
	if ((this.tagBits & TagBits.AreFieldsSorted) != 0) {
		this.fields = ReferenceBinding.sortedInsert(this.fields, fieldBinding);
		if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
			resolveTypeFor(fieldBinding);
	} else {
		//grow array
		System.arraycopy(this.fields, 0, this.fields = new FieldBinding[size + 1], 0, size);

		this.fields[size] = fieldBinding;
	}
	fieldBinding.id = size;
}
// base call surrogate could possibly be added very early, store it here first:
private List<MethodBinding> pendingMethods = null;
public void addMethod(MethodBinding methodBinding) {
	// adding before methods are set?
	if (this.methods == Binding.UNINITIALIZED_METHODS) {
		if (this.pendingMethods == null)
			this.pendingMethods = new ArrayList<MethodBinding>();
		this.pendingMethods.add(methodBinding);
		return;
	}
	// differentiate between sorted and unsorted state:
	int size = this.methods.length;
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
		if (resolveTypesFor(methodBinding) == null)
			return; // don't add erroenous method
	}
	if ((this.tagBits & TagBits.AreMethodsSorted) != 0) {
		this.methods= ReferenceBinding.sortedInsert(this.methods, methodBinding);
	} else {
		//grow array
		System.arraycopy(this.methods, 0, this.methods= new MethodBinding[size + 1], 0, size);

		this.methods[size] = methodBinding;
	}
}
public void reduceMethods(HashSet<MethodBinding> toRemove, int len) {
	if (len < 0)
		len = 0;
	MethodBinding[] newMethods = new MethodBinding[len];
	if (len > 0)
		CopyInheritance.reduceArray(this.methods, newMethods, toRemove);
	this.methods = newMethods;
}
public void removeMethod(MethodBinding method) {
	int length = this.methods.length;
	MethodBinding[] methods = new MethodBinding[length - 1];
	int pos = -1;
	if (this.methods != null) {
	    for (int i = 0; i < this.methods.length; i++) {
	        if (method == this.methods[i]) {
	            pos = i;
	            break;
	        }
		}
	}
	if (pos >= 0) {
	    System.arraycopy(this.methods, 0, methods, 0, pos);
	    System.arraycopy(
	        this.methods, pos + 1,
	        methods,      pos,
	        length - (pos + 1));
	    this.methods = methods;
	}
}

public MethodBinding resolveGeneratedMethod(MethodBinding mb) {
    return resolveTypesFor(mb);
}
/**
 * Create and link binding for a generated method.
 * @param methodDeclaration
 * @param wasSynthetic
 * @param copyInheritanceSrc
 */
public void resolveGeneratedMethod(AbstractMethodDeclaration methodDeclaration, boolean wasSynthetic, MethodBinding copyInheritanceSrc) {
    if (this.scope != null) {
		MethodBinding binding = this.scope.createMethod(methodDeclaration);
		binding.setCopyInheritanceSrc(copyInheritanceSrc);
	} else {
    	// in STATE_FINAL we can only fake this method:
    	// (this way, dependent classes can already be fully translated).
    	// TODO (SH): should this be reported as an error -> trigger recompilation?
		MethodBinding binding = new MethodBinding(
					methodDeclaration.modifiers
						| ExtraCompilerModifiers.AccUnresolved
						| ExtraCompilerModifiers.AccLocallyUsed, // suppress all "unused" warnings for generated methods
					methodDeclaration.selector,
					null, null, null,
					this);
		binding.setCopyInheritanceSrc(copyInheritanceSrc);
    	methodDeclaration.binding = binding;
    	resolveTypesFor(binding);
    	return;
    }

    CheckPoint cp = null;
    if (this.scope.referenceContext.ignoreFurtherInvestigation)
    	// for types with errors be more forgiving wrt to generated methods
    	cp = this.scope.referenceContext.compilationResult.getCheckPoint(methodDeclaration);

    try {
	    MethodBinding resolvedMethod = methodDeclaration.binding;
	    if (!methodDeclaration.isCopied)
	    	resolvedMethod.modifiers |= ExtraCompilerModifiers.AccLocallyUsed; // suppress all "unused" warnings for generated methods
// FIXME(SH): does this improve robustness?
//		if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
//		{
			if (resolveTypesFor(resolvedMethod, wasSynthetic) == null) {
		    	// other parts may rely on finding generated methods, give back a binding:
		    	if (methodDeclaration.binding == null) {
		    		methodDeclaration.binding = new ProblemMethodBinding(
		    				resolvedMethod,
							resolvedMethod.selector,
							resolvedMethod.parameters,
							ProblemReasons.NoError);
		    		if (!methodDeclaration.compilationResult.hasErrors()) // accept all cases where an error has already been reported.
		    			throw new InternalCompilerError("Cannot resolve types for generated method: "+methodDeclaration); //$NON-NLS-1$
		    		// avoid deletion of this binding during resolveTypesFor():
		    		methodDeclaration.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
		    		methodDeclaration.binding.returnType = resolvedMethod.returnType;
		    		methodDeclaration.binding.thrownExceptions = Binding.NO_EXCEPTIONS;
		    	}
		   		// try to keep going with the code below!
		    }
//		}
		// don't add more than one binding for a method
		// (CopyInheritance)
	    MethodBinding methodBinding = getExactMethod(
	    	methodDeclaration.selector, resolvedMethod.parameters,
	    	this.scope.compilationUnitScope());
	    if (   (methodBinding == null)
	        || (methodBinding.declaringClass != this))
	    {
			// create it (if successful!)
	    	if (!methodDeclaration.ignoreFurtherInvestigation)
	    		this.scope.addGeneratedMethod(methodDeclaration.binding);
	    }
	    else
	    {
	    	TypeBinding declarationReturn= methodDeclaration.binding.returnType;
			// replace any existing binding
			methodDeclaration.binding = methodBinding;
			// but use the new return type
			// (might be more specific, e.g., when a callout refines an inherited).
			methodDeclaration.binding.returnType= declarationReturn;
	    }
//FIXME(SH): is something like the following needed?
//	    scope.connectTypeVariables(methodDeclaration.typeParameters(), true);
	    if (   StateMemento.hasMethodResolveStarted(this)
	    	&& methodDeclaration.binding.isValidBinding())
	    {
	    	// manually detect overriding, if we're past the MethodVerifier:
	    	if (StateHelper.hasState(this, ITranslationStates.STATE_METHODS_VERIFIED))
	    		if (methodBinding != null && methodBinding.isValidBinding() && methodBinding.declaringClass != this)
	    			methodDeclaration.binding.modifiers |= methodBinding.declaringClass.isInterface() 
									    					? ExtraCompilerModifiers.AccImplementing 
									    					: ExtraCompilerModifiers.AccOverriding;
	        methodDeclaration.resolve(this.scope);
	    }
    } finally {
    	if (cp != null)
    		// type already had errors, roll back errors in generated methods
    		this.scope.referenceContext.compilationResult.rollBack(cp);
    }
}

//Markus Witte}
public final int sourceEnd() {
	return this.scope.referenceContext.sourceEnd;
}
public final int sourceStart() {
	return this.scope.referenceContext.sourceStart;
}
SimpleLookupTable storedAnnotations(boolean forceInitialize) {
	if (forceInitialize && this.storedAnnotations == null && this.scope != null) { // scope null when no annotation cached, and type got processed fully (159631)
		this.scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
//{ObjectTeams: do support annotations for roles for the sake of copying:
	  if (!this.isRole())
// SH}
		if (!this.scope.environment().globalOptions.storeAnnotations)
			return null; // not supported during this compile
		this.storedAnnotations = new SimpleLookupTable(3);
	}
	return this.storedAnnotations;
}
public ReferenceBinding superclass() {
	return this.superclass;
}
public ReferenceBinding[] superInterfaces() {
	return this.superInterfaces;
}
public SyntheticMethodBinding[] syntheticMethods() {
//{ObjectTeams: two different kinds of synthetics:
/* orig:
	if (this.synthetics == null 
			|| this.synthetics[SourceTypeBinding.METHOD_EMUL] == null 
			|| this.synthetics[SourceTypeBinding.METHOD_EMUL].size() == 0) {
		return null;
	}
	// difficult to compute size up front because of the embedded arrays so assume there is only 1
	int index = 0;
	SyntheticMethodBinding[] bindings = new SyntheticMethodBinding[1];
 :giro */
  if (this.synthetics == null) return null;
  int index = 0;
  SyntheticMethodBinding[] bindings = new SyntheticMethodBinding[1];
  if (this.synthetics[SourceTypeBinding.METHOD_EMUL] != null && this.synthetics[METHOD_EMUL].size() > 0) 
  {
//orig:
	Iterator methodArrayIterator = this.synthetics[SourceTypeBinding.METHOD_EMUL].values().iterator();
	while (methodArrayIterator.hasNext()) {
		SyntheticMethodBinding[] methodAccessors = (SyntheticMethodBinding[]) methodArrayIterator.next();
		for (int i = 0, max = methodAccessors.length; i < max; i++) {
			if (methodAccessors[i] != null) {
				if (index+1 > bindings.length) {
					System.arraycopy(bindings, 0, (bindings = new SyntheticMethodBinding[index + 1]), 0, index);
				}
				bindings[index++] = methodAccessors[i]; 
			}
		}
	}
  }
// :giro
// more synthetics to generate:
  	if (this.synthetics[SourceTypeBinding.ROLE_BRIDGE] != null && this.synthetics[SourceTypeBinding.ROLE_BRIDGE].size() > 0) 
  	{
		Iterator methodArrayIterator = this.synthetics[SourceTypeBinding.ROLE_BRIDGE].values().iterator();
		while (methodArrayIterator.hasNext()) {
			SyntheticMethodBinding methodAccessor = (SyntheticMethodBinding) methodArrayIterator.next();
			if (methodAccessor != null) {
				if (index+1 > bindings.length) {
					System.arraycopy(bindings, 0, (bindings = new SyntheticMethodBinding[index + 1]), 0, index);
				}
				bindings[index++] = methodAccessor; 
			}
		}
	}
  	if (index == 0)
  		return null; // nothing found
// SH}
	// sort them in according to their own indexes
	int length;
	SyntheticMethodBinding[] sortedBindings = new SyntheticMethodBinding[length = bindings.length];
	for (int i = 0; i < length; i++){
		SyntheticMethodBinding binding = bindings[i];
		sortedBindings[binding.index] = binding;
	}
	return sortedBindings;
}
/**
 * Answer the collection of synthetic fields to append into the classfile
 */
public FieldBinding[] syntheticFields() {
	if (this.synthetics == null) return null;
	int fieldSize = this.synthetics[SourceTypeBinding.FIELD_EMUL] == null ? 0 : this.synthetics[SourceTypeBinding.FIELD_EMUL].size();
	int literalSize = this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL] == null ? 0 :this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].size();
	int totalSize = fieldSize + literalSize;
	if (totalSize == 0) return null;
	FieldBinding[] bindings = new FieldBinding[totalSize];

	// add innerclass synthetics
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] != null){
		Iterator elements = this.synthetics[SourceTypeBinding.FIELD_EMUL].values().iterator();
		for (int i = 0; i < fieldSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.next();
			bindings[synthBinding.index] = synthBinding;
		}
	}
	// add class literal synthetics
	if (this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL] != null){
		Iterator elements = this.synthetics[SourceTypeBinding.CLASS_LITERAL_EMUL].values().iterator();
		for (int i = 0; i < literalSize; i++) {
			SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.next();
			bindings[fieldSize+synthBinding.index] = synthBinding;
		}
	}
	return bindings;
}
public String toString() {
    StringBuffer buffer = new StringBuffer(30);
    buffer.append("(id="); //$NON-NLS-1$
    if (this.id == TypeIds.NoId)
        buffer.append("NoId"); //$NON-NLS-1$
    else
        buffer.append(this.id);
    buffer.append(")\n"); //$NON-NLS-1$
	if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
	if (isPublic()) buffer.append("public "); //$NON-NLS-1$
	if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
	if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
	if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
	if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
	if (isFinal()) buffer.append("final "); //$NON-NLS-1$
//{ObjectTeams
	if (isTeam()) buffer.append("team "); //$NON-NLS-1$
//	Markus Witte}

	if (isEnum()) buffer.append("enum "); //$NON-NLS-1$
	else if (isAnnotationType()) buffer.append("@interface "); //$NON-NLS-1$
	else if (isClass()) buffer.append("class "); //$NON-NLS-1$
	else buffer.append("interface "); //$NON-NLS-1$
	buffer.append((this.compoundName != null) ? CharOperation.toString(this.compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	if (this.typeVariables == null) {
		buffer.append("<NULL TYPE VARIABLES>"); //$NON-NLS-1$
	} else if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		buffer.append("<"); //$NON-NLS-1$
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			if (i  > 0) buffer.append(", "); //$NON-NLS-1$
			if (this.typeVariables[i] == null) {
				buffer.append("NULL TYPE VARIABLE"); //$NON-NLS-1$
				continue;
			}
			char[] varChars = this.typeVariables[i].toString().toCharArray();
			buffer.append(varChars, 1, varChars.length - 2);
		}
		buffer.append(">"); //$NON-NLS-1$
	}
	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((this.superclass != null) ? this.superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (this.superInterfaces != null) {
		if (this.superInterfaces != Binding.NO_SUPERINTERFACES) {
			buffer.append("\n\timplements : "); //$NON-NLS-1$
			for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
				if (i  > 0)
					buffer.append(", "); //$NON-NLS-1$
				buffer.append((this.superInterfaces[i] != null) ? this.superInterfaces[i].debugName() : "NULL TYPE"); //$NON-NLS-1$
			}
		}
	} else {
		buffer.append("NULL SUPERINTERFACES"); //$NON-NLS-1$
	}

//	{ObjectTeams
	if(isDirectRole())
	{
		buffer.append("\n\tplayedBy "); //$NON-NLS-1$
		buffer.append((this.baseclass != null) ? this.baseclass.debugName() : "NULL TYPE"); //$NON-NLS-1$
	}
//	Markus Witte}
	if (enclosingType() != null) {
		buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
		buffer.append(enclosingType().debugName());
	}

	if (this.fields != null) {
		if (this.fields != Binding.NO_FIELDS) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (int i = 0, length = this.fields.length; i < length; i++)
			    buffer.append('\n').append((this.fields[i] != null) ? this.fields[i].toString() : "NULL FIELD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (this.methods != null) {
		if (this.methods != Binding.NO_METHODS) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (int i = 0, length = this.methods.length; i < length; i++)
				buffer.append('\n').append((this.methods[i] != null) ? this.methods[i].toString() : "NULL METHOD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (this.memberTypes != null) {
		if (this.memberTypes != Binding.NO_MEMBER_TYPES) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (int i = 0, length = this.memberTypes.length; i < length; i++)
				buffer.append('\n').append((this.memberTypes[i] != null) ? this.memberTypes[i].toString() : "NULL TYPE"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
	}

	buffer.append("\n\n"); //$NON-NLS-1$
	return buffer.toString();
}
public TypeVariableBinding[] typeVariables() {
	return this.typeVariables != null ? this.typeVariables : Binding.NO_TYPE_VARIABLES;
}
void verifyMethods(MethodVerifier verifier) {
//{ObjectTeams: shortcut for predefined confined types (override final methods???)
	if (TypeAnalyzer.isTopConfined(this))
		return;
// SH}
	verifier.verify(this);

	for (int i = this.memberTypes.length; --i >= 0;)
//{ObjectTeams: roles can be binary contained in source type:
	  if (!this.memberTypes[i].isBinaryBinding())
// SH}
		 ((SourceTypeBinding) this.memberTypes[i]).verifyMethods(verifier);
//{ObjectTeams: one more structure to connect:
	// (requires inherited callin bindings (from STATE_LATE_ATTRIBUTES_EVALUATED), but must happend
	//  before resolve() so resolving BaseCallMessageSends will find inherited surrogates)
	if (this.isDirectRole() && !this.isInterface())
		SyntheticBaseCallSurrogate.addFakedBaseCallSurrogates(this);
// SH}
}

public FieldBinding[] unResolvedFields() {
	return this.fields;
}
//{ObjectTeams:  in state final we lost the scope, cannot use some code.
private boolean isStateFinal() {
    if (this.roleModel != null)
    	return this.roleModel.getState() == ITranslationStates.STATE_FINAL;
    if (this._teamModel != null)
    	return this._teamModel.getState() == ITranslationStates.STATE_FINAL;
    return (this.scope == null);
}
//SH}
}
