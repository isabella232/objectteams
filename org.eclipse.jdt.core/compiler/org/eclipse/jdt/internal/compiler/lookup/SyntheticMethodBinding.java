/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SyntheticMethodBinding.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;

/**
 * OTDT changes:
 *
 * What: Changed accessor methods in teams to public
 * Why:  Role methods may depend on those accessors, copy inheritance
 *       possibly moves those methods across packages.
 *
 * @version $Id: SyntheticMethodBinding.java 23405 2010-02-03 17:02:18Z stephan $
 */
public class SyntheticMethodBinding extends MethodBinding {

	public FieldBinding targetReadField;		// read access to a field
	public FieldBinding targetWriteField;		// write access to a field
	public MethodBinding targetMethod;			// method or constructor
	public TypeBinding targetEnumType; 			// enum type

	public int purpose;

	public final static int FieldReadAccess = 1; 		// field read
	public final static int FieldWriteAccess = 2; 		// field write
	public final static int SuperFieldReadAccess = 3; // super field read
	public final static int SuperFieldWriteAccess = 4; // super field write
	public final static int MethodAccess = 5; 		// normal method
	public final static int ConstructorAccess = 6; 	// constructor
	public final static int SuperMethodAccess = 7; // super method
	public final static int BridgeMethod = 8; // bridge method
	public final static int EnumValues = 9; // enum #values()
	public final static int EnumValueOf = 10; // enum #valueOf(String)
	public final static int SwitchTable = 11; // switch table method
//{ObjectTeams: other purposes:
	public final static int InferredCalloutToField = 12; // calling an inferred callout-to-field
	public final static int RoleMethodBridgeOuter = 13; // a team-level bridge method towards a private role method (for callout)
	public final static int RoleMethodBridgeInner = 14; // a role-level bridge method towards a private role method (for callout)
// SH}

	public int sourceStart = 0; // start position of the matching declaration
	public int index; // used for sorting access methods in the class file

//{ObjectTeams: for creation from binary binding:
	public SyntheticMethodBinding(MethodBinding fakedMethod, int purpose) {
		this(fakedMethod.declaringClass, fakedMethod.modifiers, fakedMethod.selector, fakedMethod.parameters, fakedMethod.returnType);
		this.targetMethod = fakedMethod;
		this.purpose = purpose;
	}
	protected SyntheticMethodBinding(ReferenceBinding declaringClass,
						   int modifiers,
						   char[] selector,
						   TypeBinding[] parameters,
						   TypeBinding returnType)
	{
		this.declaringClass = declaringClass;
		this.modifiers = modifiers;
		this.selector = selector;
		this.parameters = parameters;
		this.returnType = returnType;
		this.thrownExceptions = NO_EXCEPTIONS;
	}
	public static boolean isCalloutToStaticField(MethodBinding methodBinding) {
		if (!(methodBinding instanceof SyntheticMethodBinding))
			return false;
		SyntheticMethodBinding synthMeth = (SyntheticMethodBinding) methodBinding;
		return synthMeth.purpose == InferredCalloutToField && synthMeth.targetMethod.isStatic();
	}
	public void generateStaticCTFArgs(CodeStream codeStream, BlockScope scope, ASTNode node, int depth) {
		codeStream.iconst_0(); // dummy
		ReferenceBinding targetType = scope.enclosingSourceType().enclosingTypeAt(depth);
		Object[] emulationPath = scope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
		codeStream.generateOuterAccess(emulationPath, node, targetType, scope);
	}
// SH}

	public SyntheticMethodBinding(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess, ReferenceBinding declaringClass) {

		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic;
//{ObjectTeams: different visibility for team accessors:
		if (declaringClass.isTeam())
			this.modifiers |= ClassFileConstants.AccPublic;
// SH}
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
		SourceTypeBinding declaringSourceType = (SourceTypeBinding) declaringClass;
		SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
		this.selector = CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(methodId).toCharArray());

//{ObjectTeams: declaring class of the field need different parameter: role instead of team:
		TypeBinding receiverParameterType = getReceiverParameterType(targetField, declaringSourceType);
// SH}
		if (isReadAccess) {
			this.returnType = targetField.type;
			if (targetField.isStatic()) {
				this.parameters = Binding.NO_PARAMETERS;
			} else {
				this.parameters = new TypeBinding[1];
//{ObjectTeams: see above:
				this.parameters[0] = receiverParameterType;
/* orig:
				this.parameters[0] = declaringSourceType;
  :giro */
// SH}
			}
			this.targetReadField = targetField;
			this.purpose = isSuperAccess ? SyntheticMethodBinding.SuperFieldReadAccess : SyntheticMethodBinding.FieldReadAccess;
		} else {
			this.returnType = TypeBinding.VOID;
			if (targetField.isStatic()) {
				this.parameters = new TypeBinding[1];
				this.parameters[0] = targetField.type;
			} else {
				this.parameters = new TypeBinding[2];
//{ObjectTeams: see above:
				this.parameters[0] = receiverParameterType;
/* orig:
				this.parameters[0] = declaringSourceType;
  :giro */
// SH}
				this.parameters[1] = targetField.type;
			}
			this.targetWriteField = targetField;
			this.purpose = isSuperAccess ? SyntheticMethodBinding.SuperFieldWriteAccess : SyntheticMethodBinding.FieldWriteAccess;
		}
		this.thrownExceptions = Binding.NO_EXCEPTIONS;
		this.declaringClass = declaringSourceType;

		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				long range;
				MethodBinding[] methods = declaringSourceType.methods();
				if ((range = ReferenceBinding.binarySearch(this.selector, methods)) >= 0) {
					int paramCount = this.parameters.length;
					nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
						MethodBinding method = methods[imethod];
						if (method.parameters.length == paramCount) {
							TypeBinding[] toMatch = method.parameters;
							for (int i = 0; i < paramCount; i++) {
								if (toMatch[i] != this.parameters[i]) {
									continue nextMethod;
								}
							}
							needRename = true;
							break check;
						}
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null) continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector) && areParametersEqual(methods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a selector postfixed by a growing methodId
				setSelector(CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(++methodId).toCharArray()));
			}
		} while (needRename);

		// retrieve sourceStart position for the target field for line number attributes
		FieldDeclaration[] fieldDecls = declaringSourceType.scope.referenceContext.fields;
		if (fieldDecls != null) {
			for (int i = 0, max = fieldDecls.length; i < max; i++) {
				if (fieldDecls[i].binding == targetField) {
					this.sourceStart = fieldDecls[i].sourceStart;
					return;
				}
			}
		}

	/* did not find the target field declaration - it is a synthetic one
		public class A {
			public class B {
				public class C {
					void foo() {
						System.out.println("A.this = " + A.this);
					}
				}
			}
			public static void main(String args[]) {
				new A().new B().new C().foo();
			}
		}
	*/
		// We now at this point - per construction - it is for sure an enclosing instance, we are going to
		// show the target field type declaration location.
		this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart; // use the target declaring class name position instead
	}

//{ObjectTeams: hook for SyntheticRoleFieldAccess
	protected TypeBinding getReceiverParameterType(FieldBinding targetField, ReferenceBinding declaringSourceType) {
		return declaringSourceType;
	}
// SH}

	public SyntheticMethodBinding(FieldBinding targetField, ReferenceBinding declaringClass, TypeBinding enumBinding, char[] selector) {
		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic;
//{ObjectTeams: different visibility for team accessors:
		if (declaringClass.isTeam())
			this.modifiers |= ClassFileConstants.AccPublic;
// SH}
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
		SourceTypeBinding declaringSourceType = (SourceTypeBinding) declaringClass;
		SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
		this.selector = selector;
		this.returnType = declaringSourceType.scope.createArrayType(TypeBinding.INT, 1);
		this.parameters = Binding.NO_PARAMETERS;
		this.targetReadField = targetField;
		this.targetEnumType = enumBinding;
		this.purpose = SyntheticMethodBinding.SwitchTable;
		this.thrownExceptions = Binding.NO_EXCEPTIONS;
		this.declaringClass = declaringSourceType;

		if (declaringSourceType.isStrictfp()) {
			this.modifiers |= ClassFileConstants.AccStrictfp;
		}
		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				long range;
				MethodBinding[] methods = declaringSourceType.methods();
				if ((range = ReferenceBinding.binarySearch(this.selector, methods)) >= 0) {
					int paramCount = this.parameters.length;
					nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
						MethodBinding method = methods[imethod];
						if (method.parameters.length == paramCount) {
							TypeBinding[] toMatch = method.parameters;
							for (int i = 0; i < paramCount; i++) {
								if (toMatch[i] != this.parameters[i]) {
									continue nextMethod;
								}
							}
							needRename = true;
							break check;
						}
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null) continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector) && areParametersEqual(methods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a selector postfixed by a growing methodId
				setSelector(CharOperation.concat(selector, String.valueOf(++methodId).toCharArray()));
			}
		} while (needRename);

		// We now at this point - per construction - it is for sure an enclosing instance, we are going to
		// show the target field type declaration location.
		this.sourceStart = declaringSourceType.scope.referenceContext.sourceStart; // use the target declaring class name position instead
	}

	public SyntheticMethodBinding(MethodBinding targetMethod, boolean isSuperAccess, ReferenceBinding declaringClass) {

		if (targetMethod.isConstructor()) {
			initializeConstructorAccessor(targetMethod);
		} else {
			initializeMethodAccessor(targetMethod, isSuperAccess, declaringClass);
		}
	}

	/**
	 * Construct a bridge method
	 */
	public SyntheticMethodBinding(MethodBinding overridenMethodToBridge, MethodBinding targetMethod, SourceTypeBinding declaringClass) {

	    this.declaringClass = declaringClass;
	    this.selector = overridenMethodToBridge.selector;
	    // amongst other, clear the AccGenericSignature, so as to ensure no remains of original inherited persist (101794)
	    // also use the modifiers from the target method, as opposed to inherited one (147690)
	    this.modifiers = (targetMethod.modifiers | ClassFileConstants.AccBridge | ClassFileConstants.AccSynthetic) & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccNative  | ClassFileConstants.AccFinal | ExtraCompilerModifiers.AccGenericSignature);
//{ObjectTeams: role class method must be public in byte code:
	    if (targetMethod.declaringClass.isRole())
	    	this.modifiers = (this.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | ClassFileConstants.AccPublic;
// SH}
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
	    this.returnType = overridenMethodToBridge.returnType;
	    this.parameters = overridenMethodToBridge.parameters;
	    this.thrownExceptions = overridenMethodToBridge.thrownExceptions;
	    this.targetMethod = targetMethod;
	    this.purpose = SyntheticMethodBinding.BridgeMethod;
		SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
	}

	/**
	 * Construct enum special methods: values or valueOf methods
	 */
	public SyntheticMethodBinding(SourceTypeBinding declaringEnum, char[] selector) {
	    this.declaringClass = declaringEnum;
	    this.selector = selector;
	    this.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccStatic;
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
		LookupEnvironment environment = declaringEnum.scope.environment();
	    this.thrownExceptions = Binding.NO_EXCEPTIONS;
		if (selector == TypeConstants.VALUES) {
		    this.returnType = environment.createArrayType(environment.convertToParameterizedType(declaringEnum), 1);
		    this.parameters = Binding.NO_PARAMETERS;
		    this.purpose = SyntheticMethodBinding.EnumValues;
		} else if (selector == TypeConstants.VALUEOF) {
		    this.returnType = environment.convertToParameterizedType(declaringEnum);
		    this.parameters = new TypeBinding[]{ declaringEnum.scope.getJavaLangString() };
		    this.purpose = SyntheticMethodBinding.EnumValueOf;
		}
		SyntheticMethodBinding[] knownAccessMethods = ((SourceTypeBinding)this.declaringClass).syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
		if (declaringEnum.isStrictfp()) {
			this.modifiers |= ClassFileConstants.AccStrictfp;
		}
	}
	
	// Create a synthetic method that will simply call the super classes method.
	// Used when a public method is inherited from a non-public class into a public class.
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=288658
	public SyntheticMethodBinding(MethodBinding overridenMethodToBridge, SourceTypeBinding declaringClass) {

	    this.declaringClass = declaringClass;
	    this.selector = overridenMethodToBridge.selector;
	    // amongst other, clear the AccGenericSignature, so as to ensure no remains of original inherited persist (101794)
	    // also use the modifiers from the target method, as opposed to inherited one (147690)
	    this.modifiers = (overridenMethodToBridge.modifiers | ClassFileConstants.AccBridge | ClassFileConstants.AccSynthetic) & ~(ClassFileConstants.AccAbstract | ClassFileConstants.AccNative  | ClassFileConstants.AccFinal | ExtraCompilerModifiers.AccGenericSignature);
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
	    this.returnType = overridenMethodToBridge.returnType;
	    this.parameters = overridenMethodToBridge.parameters;
	    this.thrownExceptions = overridenMethodToBridge.thrownExceptions;
	    this.targetMethod = overridenMethodToBridge;
	    this.purpose = SyntheticMethodBinding.SuperMethodAccess;
		SyntheticMethodBinding[] knownAccessMethods = declaringClass.syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;
	}

	/**
	 * An constructor accessor is a constructor with an extra argument (declaringClass), in case of
	 * collision with an existing constructor, then add again an extra argument (declaringClass again).
	 */
	 public void initializeConstructorAccessor(MethodBinding accessedConstructor) {

		this.targetMethod = accessedConstructor;
		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccSynthetic;
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
		SourceTypeBinding sourceType = (SourceTypeBinding) accessedConstructor.declaringClass;
		SyntheticMethodBinding[] knownSyntheticMethods = sourceType.syntheticMethods();
		this.index = knownSyntheticMethods == null ? 0 : knownSyntheticMethods.length;

		this.selector = accessedConstructor.selector;
		this.returnType = accessedConstructor.returnType;
		this.purpose = SyntheticMethodBinding.ConstructorAccess;
		this.parameters = new TypeBinding[accessedConstructor.parameters.length + 1];
		System.arraycopy(
			accessedConstructor.parameters,
			0,
			this.parameters,
			0,
			accessedConstructor.parameters.length);
		this.parameters[accessedConstructor.parameters.length] =
			accessedConstructor.declaringClass;
		this.thrownExceptions = accessedConstructor.thrownExceptions;
		this.declaringClass = sourceType;

		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				MethodBinding[] methods = sourceType.methods();
				for (int i = 0, length = methods.length; i < length; i++) {
					if (CharOperation.equals(this.selector, methods[i].selector) && areParameterErasuresEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
				// check for collision with synthetic accessors
				if (knownSyntheticMethods != null) {
					for (int i = 0, length = knownSyntheticMethods.length; i < length; i++) {
						if (knownSyntheticMethods[i] == null)
							continue;
						if (CharOperation.equals(this.selector, knownSyntheticMethods[i].selector) && areParameterErasuresEqual(knownSyntheticMethods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a new extra argument
				int length = this.parameters.length;
				System.arraycopy(
					this.parameters,
					0,
					this.parameters = new TypeBinding[length + 1],
					0,
					length);
				this.parameters[length] = this.declaringClass;
			}
		} while (needRename);

		// retrieve sourceStart position for the target method for line number attributes
		AbstractMethodDeclaration[] methodDecls =
			sourceType.scope.referenceContext.methods;
		if (methodDecls != null) {
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				if (methodDecls[i].binding == accessedConstructor) {
					this.sourceStart = methodDecls[i].sourceStart;
					return;
				}
			}
		}
	}

	/**
	 * An method accessor is a method with an access$N selector, where N is incremented in case of collisions.
	 */
	public void initializeMethodAccessor(MethodBinding accessedMethod, boolean isSuperAccess, ReferenceBinding receiverType) {

		this.targetMethod = accessedMethod;
		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic;
//{ObjectTeams: different visibility for team accessors:
		if (receiverType.isTeam())
			this.modifiers |= ClassFileConstants.AccPublic;
// SH}
		this.tagBits |= (TagBits.AnnotationResolved | TagBits.DeprecatedAnnotationResolved);
		SourceTypeBinding declaringSourceType = (SourceTypeBinding) receiverType;
		SyntheticMethodBinding[] knownAccessMethods = declaringSourceType.syntheticMethods();
		int methodId = knownAccessMethods == null ? 0 : knownAccessMethods.length;
		this.index = methodId;

		this.selector = CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(methodId).toCharArray());
		this.returnType = accessedMethod.returnType;
		this.purpose = isSuperAccess ? SyntheticMethodBinding.SuperMethodAccess : SyntheticMethodBinding.MethodAccess;

//{ObjectTeams: is accessed role method also static?
		if (accessedMethod.needsSyntheticEnclosingTeamInstance()) { 
			this.parameters = new TypeBinding[accessedMethod.parameters.length + 2];
			this.parameters[0] = TypeBinding.INT;						// dummy
			this.parameters[1] = declaringSourceType.enclosingType();	// synth team arg
			System.arraycopy(accessedMethod.parameters, 0, this.parameters, 2, accessedMethod.parameters.length);
		} else 
// SH}
		if (accessedMethod.isStatic()) {
			this.parameters = accessedMethod.parameters;
		} else {
			this.parameters = new TypeBinding[accessedMethod.parameters.length + 1];
			this.parameters[0] = declaringSourceType;
			System.arraycopy(accessedMethod.parameters, 0, this.parameters, 1, accessedMethod.parameters.length);
		}
		this.thrownExceptions = accessedMethod.thrownExceptions;
		this.declaringClass = declaringSourceType;

		// check for method collision
		boolean needRename;
		do {
			check : {
				needRename = false;
				// check for collision with known methods
				MethodBinding[] methods = declaringSourceType.methods();
				for (int i = 0, length = methods.length; i < length; i++) {
					if (CharOperation.equals(this.selector, methods[i].selector) && areParameterErasuresEqual(methods[i])) {
						needRename = true;
						break check;
					}
				}
				// check for collision with synthetic accessors
				if (knownAccessMethods != null) {
					for (int i = 0, length = knownAccessMethods.length; i < length; i++) {
						if (knownAccessMethods[i] == null) continue;
						if (CharOperation.equals(this.selector, knownAccessMethods[i].selector) && areParameterErasuresEqual(knownAccessMethods[i])) {
							needRename = true;
							break check;
						}
					}
				}
			}
			if (needRename) { // retry with a selector & a growing methodId
				setSelector(CharOperation.concat(TypeConstants.SYNTHETIC_ACCESS_METHOD_PREFIX, String.valueOf(++methodId).toCharArray()));
			}
		} while (needRename);

		// retrieve sourceStart position for the target method for line number attributes
		AbstractMethodDeclaration[] methodDecls = declaringSourceType.scope.referenceContext.methods;
		if (methodDecls != null) {
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				if (methodDecls[i].binding == accessedMethod) {
					this.sourceStart = methodDecls[i].sourceStart;
					return;
				}
			}
		}
	}

	protected boolean isConstructorRelated() {
		return this.purpose == SyntheticMethodBinding.ConstructorAccess;
	}
}
