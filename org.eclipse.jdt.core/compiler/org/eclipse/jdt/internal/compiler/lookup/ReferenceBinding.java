/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 358903 - Filter practically unimportant resource leak warnings
 *								bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
 *								bug 400421 - [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
 *								bug 382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
 *								bug 392384 - [1.8][compiler][null] Restore nullness info from type annotations in class files
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 415291 - [1.8][null] differentiate type incompatibilities due to null annotations
 *								Bug 415043 - [1.8][null] Follow-up re null type annotations after bug 392099
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *								Bug 426792 - [1.8][inference][impl] generify new type inference engine
 *								Bug 428019 - [1.8][compiler] Type inference failure with nested generic invocation.
 *								Bug 427199 - [1.8][resource] avoid resource leak warnings on Streams that have no resource
 *								Bug 418743 - [1.8][null] contradictory annotations on invocation of generic method not reported
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 431581 - Eclipse compiles what it should not
 *								Bug 440759 - [1.8][null] @NonNullByDefault should never affect wildcards and uses of a type variable
 *								Bug 452788 - [1.8][compiler] Type not correctly inferred in lambda expression
 *								Bug 446442 - [1.8] merge null annotations from super methods
 *								Bug 456532 - [1.8][null] ReferenceBinding.appendNullAnnotation() includes phantom annotations in error messages
 *								Bug 410218 - Optional warning for arguments of "unexpected" types to Map#get(Object), Collection#remove(Object) et al.
 *      Jesper S Moller - Contributions for
 *								bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *								bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *								bug 527554 - [18.3] Compiler support for JEP 286 Local-Variable Type
 *     Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AbstractOTReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleSplitter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/*
Not all fields defined by this type (& its subclasses) are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/

/**
 * OTDT changes:
 *
 * Note: New supertype AbstractOTReferenceBinding holds most of what we changed here.
 *
 *
 * What: Slightly changed strategy for enclosingType
 * Why:  Binary local types don't set enclosingType, but role local types need it.
 * Where BinaryTypeBinding.setEnclosingOfRoleLocal()
 *
 * What: new function attributeName()
 * Why:  in byte code attributes type names are encoded differently
 *
 * What: new functions getMemberTypeRecurse(name), getMethod(Scope scope, char[] selector)
 * Why:  need deep lookup.
 *
 * What: isStrictlyCompatibleWith()
 * Why:  confined types are not compatible to Object (which other methods assume)
 *
 * What: variants of printable names: filter out __OT__,
 * Note: now sourceName and sourceName() have different semantics.
 *
 */
//{ObjectTeams: changed superclass in order to separate out OT features:
abstract public class ReferenceBinding extends AbstractOTReferenceBinding {
	protected ReferenceBinding _this() { return this; } // used in AbstractOTReferenceBinding to refer to 'this'
// SH}

	public char[][] compoundName;
	public char[] sourceName;
//{ObjectTeams: now in AbstractOTReferenceBinding:
/* orig:
	public int modifiers;
  :giro */
// SH}
	public PackageBinding fPackage;
//{ObjectTeams: accessible for sub-classes:
/* orig:
    char[] fileName;
    char[] constantPoolName;
    char[] signature;
  :giro */
    public char[] fileName;
    public char[] constantPoolName;
    protected char[] signature;
// SH}


	private SimpleLookupTable compatibleCache;

	int typeBits; // additional bits characterizing this type
	protected MethodBinding [] singleAbstractMethod;

	public static final ReferenceBinding LUB_GENERIC = new ReferenceBinding() { /* used for lub computation */
		{ this.id = TypeIds.T_undefined; }
		public boolean hasTypeBit(int bit) { return false; }
	};

	private static final Comparator<FieldBinding> FIELD_COMPARATOR = new Comparator<FieldBinding>() {
		public int compare(FieldBinding o1, FieldBinding o2) {
			char[] n1 = o1.name;
			char[] n2 = o2.name;
			return ReferenceBinding.compare(n1, n2, n1.length, n2.length);
		}
	};
	private static final Comparator<MethodBinding> METHOD_COMPARATOR = new Comparator<MethodBinding>() {
		public int compare(MethodBinding o1, MethodBinding o2) {
			MethodBinding m1 = o1;
			MethodBinding m2 = o2;
			char[] s1 = m1.selector;
			char[] s2 = m2.selector;
			int c = ReferenceBinding.compare(s1, s2, s1.length, s2.length);
			return c == 0 ? m1.parameters.length - m2.parameters.length : c;
		}
	};
	static protected ProblemMethodBinding samProblemBinding = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, ProblemReasons.NoSuchSingleAbstractMethod);


	public ReferenceBinding(ReferenceBinding prototype) {
	super(prototype);

	this.compoundName = prototype.compoundName;
	this.sourceName = prototype.sourceName;
	this.modifiers = prototype.modifiers;
	this.fPackage = prototype.fPackage;
	this.fileName = prototype.fileName;
	this.constantPoolName = prototype.constantPoolName;
	this.signature = prototype.signature;
	this.compatibleCache = prototype.compatibleCache;
	this.typeBits = prototype.typeBits;
	this.singleAbstractMethod = prototype.singleAbstractMethod;
//{ObjectTeams: most subclasses need model to be set:
	this.model = new TypeModel(prototype.model, this);
// SH}
}

public ReferenceBinding() {
	super();
//{ObjectTeams: most subclasses need model to be set:
	this.model = new TypeModel(this);
// SH}
}

public static FieldBinding binarySearch(char[] name, FieldBinding[] sortedFields) {
	if (sortedFields == null)
		return null;
	int max = sortedFields.length;
	if (max == 0)
		return null;
	int left = 0, right = max - 1, nameLength = name.length;
	int mid = 0;
	char[] midName;
	while (left <= right) {
		mid = left + (right - left) /2;
		int compare = compare(name, midName = sortedFields[mid].name, nameLength, midName.length);
		if (compare < 0) {
			right = mid-1;
		} else if (compare > 0) {
			left = mid+1;
		} else {
			return sortedFields[mid];
		}
	}
	return null;
}

//{ObjectTeams:
public static FieldBinding[] sortedInsert(FieldBinding[] sortedFields, FieldBinding newField) {
	if (sortedFields != null) {
		int max = sortedFields.length;
		if (max > 0) {
			int insertBefore = 0;
			char[] name = newField.name;
			int left = 0, right = max - 1, nameLength = name.length;
			int mid = 0;
			char[] midName;
			while (left <= right) {
				mid = (left + right) /2;
				int compare = compare(name, midName = sortedFields[mid].name, nameLength, midName.length);
				if (compare < 0) {
					right = mid-1;
					insertBefore = mid;
				} else if (compare > 0) {
					left = mid+1;
					insertBefore = mid+1;
				} else {
					insertBefore = mid;
					break; // mid's name is equal to `name'
				}
			}
			FieldBinding[] result = new FieldBinding[max+1];
			System.arraycopy(sortedFields, 0, result, 0, insertBefore);
			System.arraycopy(sortedFields, insertBefore, result, insertBefore+1, max-insertBefore);
			result[insertBefore] = newField;
			return result;
		}
	}
	return new FieldBinding[] {newField};
}
public static MethodBinding[] sortedInsert(MethodBinding[] sortedMethods, MethodBinding newMethod) {
	if (sortedMethods != null) {
		int max = sortedMethods.length;
		if (max > 0) {
			int insertBefore = 0;
			int left = 0, right = max - 1;
			int mid = 0;
			binarySearch:
			while (left <= right) {
				mid = (left + right) /2;
				MethodBinding midM = sortedMethods[mid];
				if (midM == null) {
					// revert to linear search:
					while (left <= right) {
						midM = sortedMethods[right];
						while (midM == null && left <= right)
							midM = sortedMethods[--right];
						int compare = METHOD_COMPARATOR.compare(newMethod, midM);
						if (compare < 0) {
							right--;
						} else if (compare > 0) {
							insertBefore = right+1;
							break binarySearch;
						} else {
							insertBefore = right;
							break binarySearch;
						}
					}
				}
				int compare = METHOD_COMPARATOR.compare(newMethod, midM);
				if (compare < 0) {
					right = mid-1;
					insertBefore = mid;
				} else if (compare > 0) {
					left = mid+1;
					insertBefore = mid+1;
				} else {
					insertBefore = mid;
					break; // mid's name is equal to `name'
				}
			}
			MethodBinding[] result = new MethodBinding[max+1];
			System.arraycopy(sortedMethods, 0, result, 0, insertBefore);
			System.arraycopy(sortedMethods, insertBefore, result, insertBefore+1, max-insertBefore);
			result[insertBefore] = newMethod;
			return result;
		}
	}
	return new MethodBinding[] {newMethod};
}
// SH}
/**
 * Returns a combined range value representing: (start + (end<<32)), where start is the index of the first matching method
 * (remember methods are sorted alphabetically on selectors), and end is the index of last contiguous methods with same
 * selector.
 * -1 means no method got found
 * @param selector
 * @param sortedMethods
 * @return (start + (end<<32)) or -1 if no method found
 */
public static long binarySearch(char[] selector, MethodBinding[] sortedMethods) {
	if (sortedMethods == null)
		return -1;
	int max = sortedMethods.length;
	if (max == 0)
		return -1;
	int left = 0, right = max - 1, selectorLength = selector.length;
	int mid = 0;
	char[] midSelector;
	while (left <= right) {
		mid = left + (right - left) /2;
		int compare = compare(selector, midSelector = sortedMethods[mid].selector, selectorLength, midSelector.length);
		if (compare < 0) {
			right = mid-1;
		} else if (compare > 0) {
			left = mid+1;
		} else {
			int start = mid, end = mid;
			// find first method with same selector
			while (start > left && CharOperation.equals(sortedMethods[start-1].selector, selector)){ start--; }
			// find last method with same selector
			while (end < right && CharOperation.equals(sortedMethods[end+1].selector, selector)){ end++; }
			return start + ((long)end<< 32);
		}
	}
	return -1;
}

/**
 * Compares two strings lexicographically.
 * The comparison is based on the Unicode value of each character in
 * the strings.
 *
 * @return  the value <code>0</code> if the str1 is equal to str2;
 *          a value less than <code>0</code> if str1
 *          is lexicographically less than str2;
 *          and a value greater than <code>0</code> if str1 is
 *          lexicographically greater than str2.
 */
static int compare(char[] str1, char[] str2, int len1, int len2) {
	int n= Math.min(len1, len2);
	int i= 0;
	while (n-- != 0) {
		char c1= str1[i];
		char c2= str2[i++];
		if (c1 != c2) {
			return c1 - c2;
		}
	}
	return len1 - len2;
}

/**
 * Sort the field array using a quicksort
 */
public static void sortFields(FieldBinding[] sortedFields, int left, int right) {
	Arrays.sort(sortedFields, left, right, FIELD_COMPARATOR);
}

/**
 * Sort the field array using a quicksort
 */
public static void sortMethods(MethodBinding[] sortedMethods, int left, int right) {
	Arrays.sort(sortedMethods, left, right, METHOD_COMPARATOR);
}

//{ObjectTeams: some subclasses share an existing model
	protected ReferenceBinding(TypeModel model) {
		this.model = model;
		if (model != null && model.getBinding() == null)
			model.setBinding(this);
	}
// SH}

// {ObjectTeams: new API for adding elements:
    /**
     * add a generated method
     * @param method
     */
    public void addMethod(MethodBinding method) {
    	throw new InternalCompilerError("Method not applicable on this type"); //$NON-NLS-1$
    }
// SH}

/**
 * Return the array of resolvable fields (resilience)
 */
public FieldBinding[] availableFields() {
	return fields();
}

/**
 * Return the array of resolvable methods (resilience)
 */
public MethodBinding[] availableMethods() {
//{ObjectTeams: clients should see some generated methods, too:
	try {
		if (isRole())
			Dependencies.ensureBindingState(this, ITranslationStates.STATE_METHODS_CREATED);
	} catch (Throwable t) { /* don't care about exceptions in compiler */ }
// SH}
	return methods();
}

/**
 * Answer true if the receiver can be instantiated
 */
public boolean canBeInstantiated() {
	return (this.modifiers & (ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface | ClassFileConstants.AccEnum | ClassFileConstants.AccAnnotation)) == 0;
}

/**
 * Answer true if the receiver is visible to the invocationPackage.
 */
public boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == this.fPackage;
}

/**
 * Answer true if the receiver is visible to the receiverType and the invocationType.
 */
public boolean canBeSeenBy(ReferenceBinding receiverType, ReferenceBinding invocationType) {
	if (isPublic()) return true;

	if (isStatic() && (receiverType.isRawType() || receiverType.isParameterizedType()))
		receiverType = receiverType.actualType(); // outer generics are irrelevant

	if (TypeBinding.equalsEquals(invocationType, this) && TypeBinding.equalsEquals(invocationType, receiverType)) return true;

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (TypeBinding.equalsEquals(invocationType, this)) return true;
		if (invocationType.fPackage == this.fPackage) return true;

		TypeBinding currentType = invocationType.erasure();
		TypeBinding declaringClass = enclosingType().erasure(); // protected types always have an enclosing one
		if (TypeBinding.equalsEquals(declaringClass, invocationType)) return true;
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		//int depth = 0;
		do {
			if (currentType.findSuperTypeOriginatingFrom(declaringClass) != null) return true;
			//depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
		// answer true if the receiverType is the receiver or its enclosingType
		// AND the invocationType and the receiver have a common enclosingType
		receiverCheck: {
			if (!(TypeBinding.equalsEquals(receiverType, this) || TypeBinding.equalsEquals(receiverType, enclosingType()))) {
				// special tolerance for type variable direct bounds, but only if compliance <= 1.6, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=334622
				if (receiverType.isTypeVariable()) {
					TypeVariableBinding typeVariable = (TypeVariableBinding) receiverType;
					if (typeVariable.environment.globalOptions.complianceLevel <= ClassFileConstants.JDK1_6 && (typeVariable.isErasureBoundTo(erasure()) || typeVariable.isErasureBoundTo(enclosingType().erasure())))
						break receiverCheck;
				}
				return false;
			}
		}

		if (TypeBinding.notEquals(invocationType, this)) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = (ReferenceBinding)erasure();
			temp = outerDeclaringClass.enclosingType();
			while (temp != null) {
				outerDeclaringClass = temp;
				temp = temp.enclosingType();
			}
			if (TypeBinding.notEquals(outerInvocationType, outerDeclaringClass)) return false;
		}
		return true;
	}

	// isDefault()
	if (invocationType.fPackage != this.fPackage) return false;

	ReferenceBinding currentType = receiverType;
	TypeBinding originalDeclaringClass = (enclosingType() == null ? this : enclosingType()).original();
	do {
		if (currentType.isCapture()) {  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=285002
			if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.erasure().original())) return true;
		} else { 
			if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.original())) return true;
		}
		PackageBinding currentPackage = currentType.fPackage;
		// package could be null for wildcards/intersection types, ignore and recurse in superclass
		if (currentPackage != null && currentPackage != this.fPackage) return false;
	} while ((currentType = currentType.superclass()) != null);
	return false;
}

/**
 * Answer true if the receiver is visible to the type provided by the scope.
 */
public boolean canBeSeenBy(Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (TypeBinding.equalsEquals(invocationType, this)) return true;

	if (invocationType == null) // static import call
		return !isPrivate() && scope.getCurrentPackage() == this.fPackage;

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType.fPackage == this.fPackage) return true;

		TypeBinding declaringClass = enclosingType(); // protected types always have an enclosing one
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		declaringClass = declaringClass.erasure();// erasure cannot be null
		TypeBinding currentType = invocationType.erasure();
		// int depth = 0;
		do {
			if (TypeBinding.equalsEquals(declaringClass, invocationType)) return true;
			if (currentType.findSuperTypeOriginatingFrom(declaringClass) != null) return true;
			// depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}
	if (isPrivate()) {
		// answer true if the receiver and the invocationType have a common enclosingType
		// already know they are not the identical type
		ReferenceBinding outerInvocationType = invocationType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = (ReferenceBinding)erasure();
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return TypeBinding.equalsEquals(outerInvocationType, outerDeclaringClass);
	}

	// isDefault()
	return invocationType.fPackage == this.fPackage;
}

public char[] computeGenericTypeSignature(TypeVariableBinding[] typeVariables) {

	boolean isMemberOfGeneric = isMemberType() && !isStatic() && (enclosingType().modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0;
	if (typeVariables == Binding.NO_TYPE_VARIABLES && !isMemberOfGeneric) {
		return signature();
	}
	StringBuffer sig = new StringBuffer(10);
	if (isMemberOfGeneric) {
	    char[] typeSig = enclosingType().genericTypeSignature();
	    sig.append(typeSig, 0, typeSig.length-1); // copy all but trailing semicolon
	    sig.append('.'); // NOTE: cannot override trailing ';' with '.' in enclosing signature, since shared char[]
	    sig.append(this.sourceName);
	}	else {
	    char[] typeSig = signature();
	    sig.append(typeSig, 0, typeSig.length-1); // copy all but trailing semicolon
	}
	if (typeVariables == Binding.NO_TYPE_VARIABLES) {
	    sig.append(';');
	} else {
	    sig.append('<');
	    for (int i = 0, length = typeVariables.length; i < length; i++) {
	        sig.append(typeVariables[i].genericTypeSignature());
	    }
	    sig.append(">;"); //$NON-NLS-1$
	}
	int sigLength = sig.length();
	char[] result = new char[sigLength];
	sig.getChars(0, sigLength, result, 0);
	return result;
}

public void computeId() {
	// note that more (configurable) ids are assigned from PackageBinding#checkIfNullAnnotationType() 

	// try to avoid multiple checks against a package/type name
	switch (this.compoundName.length) {

		case 3 :
//{ObjectTeams: org.objectteams...
			if (   CharOperation.equals(IOTConstants.ORG, this.compoundName[0])
				&& CharOperation.equals(IOTConstants.OBJECTTEAMS, this.compoundName[1]))
			{
				if(CharOperation.equals(IOTConstants.ITEAM, this.compoundName[2]))
					this.id = IOTConstants.T_OrgObjectTeamsITeam;
				else if(CharOperation.equals(IOTConstants.TEAM, this.compoundName[2]))
					this.id = IOTConstants.T_OrgObjectTeamsTeam;
				else if(CharOperation.equals(IOTConstants.IBOUNDBASE, this.compoundName[2]))
					this.id = IOTConstants.T_OrgObjectTeamsIBoundBase;
				else if(CharOperation.equals(IOTConstants.IBOUNDBASE2, this.compoundName[2]))
					this.id = IOTConstants.T_OrgObjectTeamsIBoundBase2;
				else if(CharOperation.equals(IOTConstants.INSTANTIATION, this.compoundName[2]))
					this.id = IOTConstants.T_OrgObjectTeamsInstantiation;
				return;
			}
// SH}
			char[] packageName = this.compoundName[0];
			// expect only java.*.* and javax.*.* and junit.*.* and org.junit.*
			switch (packageName.length) {
				case 3: // only one type in this group, yet:
					if (CharOperation.equals(TypeConstants.ORG_JUNIT_ASSERT, this.compoundName))
						this.id = TypeIds.T_OrgJunitAssert;
					return;						
				case 4:
					if (!CharOperation.equals(TypeConstants.JAVA, packageName))
						return;
					break; // continue below ...
				case 5:
					switch (packageName[1]) {
						case 'a':
							if (CharOperation.equals(TypeConstants.JAVAX_ANNOTATION_INJECT_INJECT, this.compoundName))
								this.id = TypeIds.T_JavaxInjectInject;
							return;
						case 'u':
							if (CharOperation.equals(TypeConstants.JUNIT_FRAMEWORK_ASSERT, this.compoundName))
								this.id = TypeIds.T_JunitFrameworkAssert;
							return;
					}
				return;
				default: return;
			}
			// ... at this point we know it's java.*.*
			
			packageName = this.compoundName[1];
			if (packageName.length == 0) return; // just to be safe
			char[] typeName = this.compoundName[2];
			if (typeName.length == 0) return; // just to be safe
			// remaining types MUST be in java.*.*
			if (!CharOperation.equals(TypeConstants.LANG, this.compoundName[1])) {
				switch (packageName[0]) {
					case 'i' :
						if (CharOperation.equals(packageName, TypeConstants.IO)) {
							switch (typeName[0]) {
								case 'C' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_CLOSEABLE[2]))
										this.typeBits |= TypeIds.BitCloseable; // don't assign id, only typeBit (for analysis of resource leaks) 
									return;
								case 'E' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_EXTERNALIZABLE[2]))
										this.id = TypeIds.T_JavaIoExternalizable;
									return;
								case 'I' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_IOEXCEPTION[2]))
										this.id = TypeIds.T_JavaIoException;
									return;
								case 'O' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_OBJECTSTREAMEXCEPTION[2]))
										this.id = TypeIds.T_JavaIoObjectStreamException;
									return;
								case 'P' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_PRINTSTREAM[2]))
										this.id = TypeIds.T_JavaIoPrintStream;
									return;
								case 'S' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_SERIALIZABLE[2]))
										this.id = TypeIds.T_JavaIoSerializable;
									return;
							}
						}
						return;
					case 'u' :
						if (CharOperation.equals(packageName, TypeConstants.UTIL)) {
							switch (typeName[0]) {
								case 'C' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_COLLECTION[2])) {
										this.id = TypeIds.T_JavaUtilCollection;
										this.typeBits |= TypeIds.BitCollection;
									}										
									return;
								case 'I' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_ITERATOR[2]))
										this.id = TypeIds.T_JavaUtilIterator;
									return;
								case 'L' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_LIST[2])) {
										this.id = TypeIds.T_JavaUtilList;
										this.typeBits |= TypeIds.BitList;
									}										
									return;
								case 'M' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_MAP[2])) {
										this.id = TypeIds.T_JavaUtilMap;
										this.typeBits |= TypeIds.BitMap;
									}
									return;
								case 'O' :
									if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_OBJECTS[2]))
										this.id = TypeIds.T_JavaUtilObjects;
									return;
							}
						}
						return;
				}
				return;
			}

			// remaining types MUST be in java.lang.*
			switch (typeName[0]) {
				case 'A' :
					switch(typeName.length) {
						case 13 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_AUTOCLOSEABLE[2])) {
								this.id = TypeIds.T_JavaLangAutoCloseable;
								this.typeBits |= TypeIds.BitAutoCloseable; 
							}
							return;
						case 14:
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ASSERTIONERROR[2]))
								this.id = TypeIds.T_JavaLangAssertionError;
							return;
					}
					return;
				case 'B' :
					switch (typeName.length) {
						case 4 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_BYTE[2]))
								this.id = TypeIds.T_JavaLangByte;
							return;
						case 7 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_BOOLEAN[2]))
								this.id = TypeIds.T_JavaLangBoolean;
							return;
					}
					return;
				case 'C' :
					switch (typeName.length) {
						case 5 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLASS[2]))
								this.id = TypeIds.T_JavaLangClass;
							return;
						case 9 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CHARACTER[2]))
								this.id = TypeIds.T_JavaLangCharacter;
							else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLONEABLE[2]))
							    this.id = TypeIds.T_JavaLangCloneable;
							return;
						case 22 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLASSNOTFOUNDEXCEPTION[2]))
								this.id = TypeIds.T_JavaLangClassNotFoundException;
							return;
					}
					return;
				case 'D' :
					switch (typeName.length) {
						case 6 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_DOUBLE[2]))
								this.id = TypeIds.T_JavaLangDouble;
							return;
						case 10 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_DEPRECATED[2]))
								this.id = TypeIds.T_JavaLangDeprecated;
							return;
					}
					return;
				case 'E' :
					switch (typeName.length) {
						case 4 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ENUM[2]))
								this.id = TypeIds.T_JavaLangEnum;
							return;
						case 5 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ERROR[2]))
								this.id = TypeIds.T_JavaLangError;
							return;
						case 9 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_EXCEPTION[2]))
								this.id = TypeIds.T_JavaLangException;
							return;
					}
					return;
				case 'F' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_FLOAT[2]))
						this.id = TypeIds.T_JavaLangFloat;
					else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_FUNCTIONAL_INTERFACE[2]))
						this.id = TypeIds.T_JavaLangFunctionalInterface;
					return;
				case 'I' :
					switch (typeName.length) {
						case 7 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INTEGER[2]))
								this.id = TypeIds.T_JavaLangInteger;
							return;
						case 8 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ITERABLE[2]))
								this.id = TypeIds.T_JavaLangIterable;
							return;
						case 24 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION[2]))
								this.id = TypeIds.T_JavaLangIllegalArgumentException;
							return;
					}
					return;
				case 'L' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_LONG[2]))
						this.id = TypeIds.T_JavaLangLong;
					return;
				case 'N' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_NOCLASSDEFERROR[2]))
						this.id = TypeIds.T_JavaLangNoClassDefError;
					return;
				case 'O' :
					switch (typeName.length) {
						case 6 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_OBJECT[2]))
								this.id = TypeIds.T_JavaLangObject;
							return;
						case 8 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_OVERRIDE[2]))
								this.id = TypeIds.T_JavaLangOverride;
							return;
					}
					return;
				case 'R' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION[2]))
						this.id = 	TypeIds.T_JavaLangRuntimeException;
					break;
				case 'S' :
					switch (typeName.length) {
						case 5 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SHORT[2]))
								this.id = TypeIds.T_JavaLangShort;
							return;
						case 6 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRING[2]))
								this.id = TypeIds.T_JavaLangString;
							else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SYSTEM[2]))
								this.id = TypeIds.T_JavaLangSystem;
							return;
						case 11 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SAFEVARARGS[2]))
								this.id = TypeIds.T_JavaLangSafeVarargs;
							return;
						case 12 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRINGBUFFER[2]))
								this.id = TypeIds.T_JavaLangStringBuffer;
							return;
						case 13 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRINGBUILDER[2]))
								this.id = TypeIds.T_JavaLangStringBuilder;
							return;
						case 16 :
							if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SUPPRESSWARNINGS[2]))
								this.id = TypeIds.T_JavaLangSuppressWarnings;
							return;
					}
					return;
				case 'T' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_THROWABLE[2]))
						this.id = TypeIds.T_JavaLangThrowable;
					return;
				case 'V' :
					if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_VOID[2]))
						this.id = TypeIds.T_JavaLangVoid;
					return;
			}
		break;

		case 4:
			// expect one type from com.*.*.*:
			if (CharOperation.equals(TypeConstants.COM_GOOGLE_INJECT_INJECT, this.compoundName)) {
				this.id = TypeIds.T_ComGoogleInjectInject;
				return;
			}
			// otherwise only expect java.*.*.*
			if (!CharOperation.equals(TypeConstants.JAVA, this.compoundName[0]))
				return;
			packageName = this.compoundName[1];
			if (packageName.length == 0) return; // just to be safe

			packageName = this.compoundName[2];
			if (packageName.length == 0) return; // just to be safe
			typeName = this.compoundName[3];
			if (typeName.length == 0) return; // just to be safe
			switch (packageName[0]) {
				case 'a' :
					if (CharOperation.equals(packageName, TypeConstants.ANNOTATION)) {
						switch (typeName[0]) {
							case 'A' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION[3]))
									this.id = TypeIds.T_JavaLangAnnotationAnnotation;
								return;
							case 'D' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_DOCUMENTED[3]))
									this.id = TypeIds.T_JavaLangAnnotationDocumented;
								return;
							case 'E' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_ELEMENTTYPE[3]))
									this.id = TypeIds.T_JavaLangAnnotationElementType;
								return;
							case 'I' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_INHERITED[3]))
									this.id = TypeIds.T_JavaLangAnnotationInherited;
								return;
							case 'R' :
								switch (typeName.length) {
									case 9 :
										if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_RETENTION[3]))
											this.id = TypeIds.T_JavaLangAnnotationRetention;
										return;
									case 10 :
										if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_REPEATABLE[3]))
											this.id = TypeIds.T_JavaLangAnnotationRepeatable;
										return;
									case 15 :
										if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_RETENTIONPOLICY[3]))
											this.id = TypeIds.T_JavaLangAnnotationRetentionPolicy;
										return;
								}
								return;
							case 'T' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_TARGET[3]))
									this.id = TypeIds.T_JavaLangAnnotationTarget;
								return;
						}
					}
					return;
				case 'i':
					if (CharOperation.equals(packageName, TypeConstants.INVOKE)) {
						if (typeName.length == 0) return; // just to be safe
						switch (typeName[0]) {
							case 'M' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_$_POLYMORPHICSIGNATURE[3]))
									this.id = TypeIds.T_JavaLangInvokeMethodHandlePolymorphicSignature;
								return;
						}
					}
					return;
				case 'r' :
					if (CharOperation.equals(packageName, TypeConstants.REFLECT)) {
						switch (typeName[0]) {
							case 'C' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_CONSTRUCTOR[2]))
									this.id = TypeIds.T_JavaLangReflectConstructor;
								return;
							case 'F' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_FIELD[2]))
									this.id = TypeIds.T_JavaLangReflectField;
								return;
							case 'M' :
								if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_METHOD[2]))
									this.id = TypeIds.T_JavaLangReflectMethod;
								return;
						}
					}
					return;
			}
			break;
		case 5 :
			packageName = this.compoundName[0];
			switch (packageName[0]) {
				case 'j' :
					if (!CharOperation.equals(TypeConstants.JAVA, this.compoundName[0]))
						return;
					packageName = this.compoundName[1];
					if (packageName.length == 0) return; // just to be safe
					
					if (CharOperation.equals(TypeConstants.LANG, packageName)) {
						packageName = this.compoundName[2];
						if (packageName.length == 0) return; // just to be safe
						switch (packageName[0]) {
							case 'i' :
								if (CharOperation.equals(packageName, TypeConstants.INVOKE)) { 
									typeName = this.compoundName[3];
									if (typeName.length == 0) return; // just to be safe
									switch (typeName[0]) {
										case 'M' :
											char[] memberTypeName = this.compoundName[4];
											if (memberTypeName.length == 0) return; // just to be safe
											if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_POLYMORPHICSIGNATURE[3])
													&& CharOperation.equals(memberTypeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_POLYMORPHICSIGNATURE[4]))
												this.id = TypeIds.T_JavaLangInvokeMethodHandlePolymorphicSignature;
											return;
									}
								}
								return;
						}
						return;
					}
					return;
				case 'o':
					if (!CharOperation.equals(TypeConstants.ORG, this.compoundName[0]))
						return;
					packageName = this.compoundName[1];
					if (packageName.length == 0) return; // just to be safe

					switch (packageName[0]) {
						case 'e':
					if (CharOperation.equals(TypeConstants.ECLIPSE, packageName)) {
						packageName = this.compoundName[2];
						if (packageName.length == 0) return; // just to be safe
						switch (packageName[0]) {
							case 'c' :
								if (CharOperation.equals(packageName, TypeConstants.CORE)) { 
									typeName = this.compoundName[3];
									if (typeName.length == 0) return; // just to be safe
									switch (typeName[0]) {
										case 'r' :
											char[] memberTypeName = this.compoundName[4];
											if (memberTypeName.length == 0) return; // just to be safe
											if (CharOperation.equals(typeName, TypeConstants.ORG_ECLIPSE_CORE_RUNTIME_ASSERT[3])
													&& CharOperation.equals(memberTypeName, TypeConstants.ORG_ECLIPSE_CORE_RUNTIME_ASSERT[4]))
												this.id = TypeIds.T_OrgEclipseCoreRuntimeAssert;
											return;
									}
								}
								return;
						}
						return;
					}
					return;
						case 'a':
							if (CharOperation.equals(TypeConstants.APACHE, packageName)) {
								if (CharOperation.equals(TypeConstants.COMMONS, this.compoundName[2])) {
									if (CharOperation.equals(TypeConstants.ORG_APACHE_COMMONS_LANG_VALIDATE, this.compoundName))
										this.id = TypeIds.T_OrgApacheCommonsLangValidate;
									else if (CharOperation.equals(TypeConstants.ORG_APACHE_COMMONS_LANG3_VALIDATE, this.compoundName))
										this.id = TypeIds.T_OrgApacheCommonsLang3Validate;
			}
							}
							return;
					}
					return;
				case 'c':
					if (!CharOperation.equals(TypeConstants.COM, this.compoundName[0]))
						return;
					if (CharOperation.equals(TypeConstants.COM_GOOGLE_COMMON_BASE_PRECONDITIONS, this.compoundName))
						this.id = TypeIds.T_ComGoogleCommonBasePreconditions;
					return;
			}
			break;
		case 6:
			if (CharOperation.equals(TypeConstants.ORG, this.compoundName[0])) {
				if (CharOperation.equals(TypeConstants.SPRING, this.compoundName[1])) {
					if (CharOperation.equals(TypeConstants.AUTOWIRED, this.compoundName[5])) {
						if (CharOperation.equals(TypeConstants.ORG_SPRING_AUTOWIRED, this.compoundName)) {
							this.id = TypeIds.T_OrgSpringframeworkBeansFactoryAnnotationAutowired;
						}
					}
					return;
				}
				if (!CharOperation.equals(TypeConstants.JDT, this.compoundName[2]) || !CharOperation.equals(TypeConstants.ITYPEBINDING, this.compoundName[5]))
					return;
				if (CharOperation.equals(TypeConstants.ORG_ECLIPSE_JDT_CORE_DOM_ITYPEBINDING, this.compoundName))
					this.typeBits |= TypeIds.BitUninternedType;
			}
			break;
		case 7 :
			if (!CharOperation.equals(TypeConstants.JDT, this.compoundName[2]) || !CharOperation.equals(TypeConstants.TYPEBINDING, this.compoundName[6]))
				return;
			if (CharOperation.equals(TypeConstants.ORG_ECLIPSE_JDT_INTERNAL_COMPILER_LOOKUP_TYPEBINDING, this.compoundName))
				this.typeBits |= TypeIds.BitUninternedType;
			break;
	}
}

public void computeId(LookupEnvironment environment) {
	environment.getUnannotatedType(this);
}

/**
 * p.X<T extends Y & I, U extends Y> {} -> Lp/X<TT;TU;>;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	if (!isLeaf) return signature();
	return genericTypeSignature();
}

/**
 * Answer the receiver's constant pool name.
 *
 * NOTE: This method should only be used during/after code gen.
 */
public char[] constantPoolName() /* java/lang/Object */ {
	if (this.constantPoolName != null) return this.constantPoolName;
	return this.constantPoolName = CharOperation.concatWith(this.compoundName, '/');
}

//{ObjectTeams: this is how type names are encoded in OT byte code attributes
public char[] attributeName() /* p1.p2.COuter$CInner */ {
    return CharOperation.concatWith(this.compoundName, '.');
}
// SH}
public String debugName() {
	return (this.compoundName != null) ? this.hasTypeAnnotations() ? annotatedDebugName() : new String(readableName()) : "UNNAMED TYPE"; //$NON-NLS-1$
}

public int depth() {
	int depth = 0;
	ReferenceBinding current = this;
	while ((current = current.enclosingType()) != null)
		depth++;
	return depth;
}

public boolean detectAnnotationCycle() {
	if ((this.tagBits & TagBits.EndAnnotationCheck) != 0) return false; // already checked
	if ((this.tagBits & TagBits.BeginAnnotationCheck) != 0) return true; // in the middle of checking its methods

	this.tagBits |= TagBits.BeginAnnotationCheck;
	MethodBinding[] currentMethods = methods();
	boolean inCycle = false; // check each method before failing
	for (int i = 0, l = currentMethods.length; i < l; i++) {
		TypeBinding returnType = currentMethods[i].returnType.leafComponentType().erasure();
		if (TypeBinding.equalsEquals(this, returnType)) {
			if (this instanceof SourceTypeBinding) {
				MethodDeclaration decl = (MethodDeclaration) currentMethods[i].sourceMethod();
				((SourceTypeBinding) this).scope.problemReporter().annotationCircularity(this, this, decl != null ? decl.returnType : null);
			}
		} else if (returnType.isAnnotationType() && ((ReferenceBinding) returnType).detectAnnotationCycle()) {
			if (this instanceof SourceTypeBinding) {
				MethodDeclaration decl = (MethodDeclaration) currentMethods[i].sourceMethod();
				((SourceTypeBinding) this).scope.problemReporter().annotationCircularity(this, returnType, decl != null ? decl.returnType : null);
			}
			inCycle = true;
		}
	}
	if (inCycle)
		return true;
	this.tagBits |= TagBits.EndAnnotationCheck;
	return false;
}

public final ReferenceBinding enclosingTypeAt(int relativeDepth) {
	ReferenceBinding current = this;
	while (relativeDepth-- > 0 && current != null)
		current = current.enclosingType();
	return current;
}

public int enumConstantCount() {
	int count = 0;
	FieldBinding[] fields = fields();
	for (int i = 0, length = fields.length; i < length; i++) {
		if ((fields[i].modifiers & ClassFileConstants.AccEnum) != 0) count++;
	}
	return count;
}

public int fieldCount() {
//{ObjectTeams: don't count faked base field:
	if (isRole()) {
		int count = 0;
		for (FieldBinding field : fields()) {
			if ((field.otBits & IOTConstants.IsFakedField) ==0)
				count++;
		}
		return count;
	}
// SH}
	return fields().length;
}

public FieldBinding[] fields() {
	return Binding.NO_FIELDS;
}

public final int getAccessFlags() {
//{ObjectTeams: use extended mask (was AccJustFlag):
	return this.modifiers & ExtraCompilerModifiers.AccOTTypeJustFlag;
// SH}
}

/**
 * @return the JSR 175 annotations for this type.
 */
public AnnotationBinding[] getAnnotations() {
	return retrieveAnnotations(this);
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	return this.tagBits;
}

/**
 * @return the enclosingInstancesSlotSize
 */
public int getEnclosingInstancesSlotSize() {
	if (isStatic()) return 0;
	return enclosingType() == null ? 0 : 1;
}

public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	return null;
}

public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	return null;
}
public FieldBinding getField(char[] fieldName, boolean needResolve) {
	return null;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return this.fileName;
}

public ReferenceBinding getMemberType(char[] typeName) {
	ReferenceBinding[] memberTypes = memberTypes();
	for (int i = memberTypes.length; --i >= 0;)
		if (CharOperation.equals(memberTypes[i].sourceName, typeName))
			return memberTypes[i];
	return null;
}
//{ObjectTeams: new utility function
public ReferenceBinding getMemberTypeRecurse(char[] typeName) {
	ReferenceBinding[] memberTypes = memberTypes();
	for (int i = memberTypes.length; --i >= 0;) {
		if (CharOperation.equals(memberTypes[i].sourceName, typeName))
			return memberTypes[i];
		ReferenceBinding result = memberTypes[i].getMemberTypeRecurse(typeName);
		if (result != null)
			return result;
	}
	return null;
}
// SH}

public MethodBinding[] getMethods(char[] selector) {
	return Binding.NO_METHODS;
}

//Answer methods named selector, which take no more than the suggestedParameterLength.
//The suggested parameter length is optional and may not be guaranteed by every type.
public MethodBinding[] getMethods(char[] selector, int suggestedParameterLength) {
	return getMethods(selector);
}

//{ObjectTeams:
	/**
	 * get method by its selector, search includes superclasses, superinterfaces
	 * (except when searching a constructor).
	 * If more than one method (significantly, ie., not overriding each other)
	 * is found, return a ProblemMethodBinding, if none is found return null.
	 * Ignore tsuper versions of methods.
	 */
	public MethodBinding getMethod(Scope scope, char[] selector) {
		MethodBinding[] currentMethods = getMethods(selector);
		MethodBinding foundMethod = null;
		if (currentMethods != null)
		{
			int numFound = currentMethods.length;
			int suitableIdx = 0;
			for (int i=0; i<currentMethods.length; i++) {
				if (TSuperHelper.isTSuper(currentMethods[i]))
					numFound--;
				else
					suitableIdx = i;
			}
			if (numFound == 1)
				foundMethod = currentMethods[suitableIdx];
			else if (numFound > 1)
				return new ProblemMethodBinding(selector, Binding.NO_PARAMETERS, ProblemReasons.Ambiguous);
		}
		if (CharOperation.equals(TypeConstants.INIT, selector))
			return foundMethod; // don't search supers for constructor
		MethodVerifier verifier = scope.environment().methodVerifier(); // respect source level.
		if (superclass() != null) {
			MethodBinding superclassMethod = superclass().getMethod(scope, selector);
			foundMethod = combineFoundMethods(foundMethod, superclassMethod, verifier);
		}
		if (foundMethod != null && !foundMethod.isValidBinding())
			return foundMethod; // one problem is enough
		ReferenceBinding[] superInterfaces = superInterfaces();
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfaces.length; i++) {
				MethodBinding superIfcMethod = superInterfaces[i].getMethod(scope, selector);
				foundMethod = combineFoundMethods(foundMethod, superIfcMethod, verifier);
				if (foundMethod != null && !foundMethod.isValidBinding())
					return foundMethod; // one problem is enough
			}
		}
		return foundMethod;
	}
	/**
	 * helper for the above
	 * @param match1
	 * @param superMethod
	 * @param verifier
	 * @return either match1 or superMethod or a ProblemMethodBinding (Ambiguous)
	 */
	private MethodBinding combineFoundMethods(
			MethodBinding match1,
			MethodBinding superMethod,
			MethodVerifier verifier)
	{

		MethodBinding foundMethod = match1;
		if (superMethod != null)
		{
			if (superMethod.isPrivate())
				return match1;  // ignore private method
			if (!superMethod.isValidBinding()) {
				foundMethod = superMethod; // propagate problem
			} else {
				if (match1 == null) {
					foundMethod = superMethod;
				} else {
					MethodBinding superSubstitute= verifier.computeSubstituteMethod(superMethod, match1);
					if (superSubstitute != null)
						superMethod= superSubstitute;
					if (!ImplicitNullAnnotationVerifier.areParametersEqual(match1, superMethod, verifier.environment))
						foundMethod = new ProblemMethodBinding(match1.selector, Binding.NO_PARAMETERS, ProblemReasons.Ambiguous);
				}
			}
		}
		return foundMethod;
	}
// SH}

/**
 * @return the outerLocalVariablesSlotSize
 */
public int getOuterLocalVariablesSlotSize() {
	return 0;
}

public PackageBinding getPackage() {
	return this.fPackage;
}

public TypeVariableBinding getTypeVariable(char[] variableName) {
	TypeVariableBinding[] typeVariables = typeVariables();
	for (int i = typeVariables.length; --i >= 0;)
		if (CharOperation.equals(typeVariables[i].sourceName, variableName))
			return typeVariables[i];
	return null;
}

public int hashCode() {
	// ensure ReferenceBindings hash to the same position as UnresolvedReferenceBindings so they can be replaced without rehashing
	// ALL ReferenceBindings are unique when created so equals() is the same as ==
	return (this.compoundName == null || this.compoundName.length == 0)
		? super.hashCode()
		: CharOperation.hashCode(this.compoundName[this.compoundName.length - 1]);
}

/**
 * Returns true if the two types have an incompatible common supertype,
 * e.g. List<String> and List<Integer>
 */
public boolean hasIncompatibleSuperType(ReferenceBinding otherType) {

    if (TypeBinding.equalsEquals(this, otherType)) return false;

	ReferenceBinding[] interfacesToVisit = null;
	int nextPosition = 0;
    ReferenceBinding currentType = this;
	TypeBinding match;
	do {
		match = otherType.findSuperTypeOriginatingFrom(currentType);
		if (match != null && match.isProvablyDistinct(currentType))
			return true;

		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
			if (interfacesToVisit == null) {
				interfacesToVisit = itsInterfaces;
				nextPosition = interfacesToVisit.length;
			} else {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
	} while ((currentType = currentType.superclass()) != null);

	for (int i = 0; i < nextPosition; i++) {
		currentType = interfacesToVisit[i];
		if (TypeBinding.equalsEquals(currentType, otherType)) return false;
		match = otherType.findSuperTypeOriginatingFrom(currentType);
		if (match != null && match.isProvablyDistinct(currentType))
			return true;

		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
			int itsLength = itsInterfaces.length;
			if (nextPosition + itsLength >= interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
			nextInterface : for (int a = 0; a < itsLength; a++) {
				ReferenceBinding next = itsInterfaces[a];
				for (int b = 0; b < nextPosition; b++)
					if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
				interfacesToVisit[nextPosition++] = next;
			}
		}
	}
	return false;
}

public boolean hasMemberTypes() {
    return false;
}

/**
 * Answer whether a @NonNullByDefault is applicable at the reference binding,
 * for 1.8 check if the default is applicable to the given kind of location.
 */
// pre: null annotation analysis is enabled
boolean hasNonNullDefaultFor(int location, boolean useTypeAnnotations, int sourceStart) {
	// Note, STB overrides for correctly handling local types
	ReferenceBinding currentType = this;
	while (currentType != null) {
		if (useTypeAnnotations) {
			int nullDefault = ((ReferenceBinding)currentType.original()).getNullDefault();
			if (nullDefault != 0)
				return (nullDefault & location) != 0;
		} else {
			if ((currentType.tagBits & TagBits.AnnotationNonNullByDefault) != 0)
				return true;
			if ((currentType.tagBits & TagBits.AnnotationNullUnspecifiedByDefault) != 0)
				return false;
		}
		currentType = currentType.enclosingType();
	}
	// package
	if (useTypeAnnotations)
		return (this.getPackage().defaultNullness & location) != 0;
	else
		return this.getPackage().defaultNullness == NONNULL_BY_DEFAULT;
}

int getNullDefault() {
	return 0;
}

public boolean acceptsNonNullDefault() {
	return true;
}

public final boolean hasRestrictedAccess() {
	return (this.modifiers & ExtraCompilerModifiers.AccRestrictedAccess) != 0;
}

/** Query typeBits without triggering supertype lookup. */
public boolean hasNullBit(int mask) {
	return (this.typeBits & mask) != 0;
}

//{ObjectTeams: support asymmetric comparison. // FIXME(SH): is this needed or is super-impl smart enough??
@Override
public boolean isProvablyDistinct(TypeBinding otherType) {
	if (otherType.isRoleType())
		return otherType.isProvablyDistinct(this);
	else
		return super.isProvablyDistinct(otherType);
}
// SH}

/** Answer true if the receiver implements anInterface or is identical to anInterface.
* If searchHierarchy is true, then also search the receiver's superclasses.
*
* NOTE: Assume that anInterface is an interface.
*/
public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
	if (TypeBinding.equalsEquals(this, anInterface))
		return true;

	ReferenceBinding[] interfacesToVisit = null;
	int nextPosition = 0;
	ReferenceBinding currentType = this;
	do {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) { // in code assist cases when source types are added late, may not be finished connecting hierarchy
			if (interfacesToVisit == null) {
				interfacesToVisit = itsInterfaces;
				nextPosition = interfacesToVisit.length;
			} else {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
	} while (searchHierarchy && (currentType = currentType.superclass()) != null);

	for (int i = 0; i < nextPosition; i++) {
		currentType = interfacesToVisit[i];
		if (currentType.isEquivalentTo(anInterface))
			return true;

		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) { // in code assist cases when source types are added late, may not be finished connecting hierarchy
			int itsLength = itsInterfaces.length;
			if (nextPosition + itsLength >= interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
			nextInterface : for (int a = 0; a < itsLength; a++) {
				ReferenceBinding next = itsInterfaces[a];
				for (int b = 0; b < nextPosition; b++)
					if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
				interfacesToVisit[nextPosition++] = next;
			}
		}
	}
//{ObjectTeams: special case: bound base classes are conform to o.o.IBoundBase:
	if (isBoundBase())
		return (anInterface.id == IOTConstants.T_OrgObjectTeamsIBoundBase
				|| anInterface.id == IOTConstants.T_OrgObjectTeamsIBoundBase2); // assume only one of these can be used during one compile
//SH}
	return false;
}

// Internal method... assume its only sent to classes NOT interfaces
//{ObjectTeams: accessible to sub-class:
protected
// SH}
boolean implementsMethod(MethodBinding method) {
	char[] selector = method.selector;
	ReferenceBinding type = this;
	while (type != null) {
		MethodBinding[] methods = type.methods();
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, methods)) >= 0) {
			int start = (int) range, end = (int) (range >> 32);
			for (int i = start; i <= end; i++) {
				if (methods[i].areParametersEqual(method))
					return true;
			}
		}
		type = type.superclass();
	}
	return false;
}

/**
 * Answer true if the receiver is an abstract type
*/
public final boolean isAbstract() {
	return (this.modifiers & ClassFileConstants.AccAbstract) != 0;
}

public boolean isAnnotationType() {
	return (this.modifiers & ClassFileConstants.AccAnnotation) != 0;
}

public final boolean isBinaryBinding() {
	return (this.tagBits & TagBits.IsBinaryBinding) != 0;
}

public boolean isClass() {
	return (this.modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) == 0;
}

public boolean isProperType(boolean admitCapture18) {
	ReferenceBinding outer = enclosingType();
	if (outer != null && !outer.isProperType(admitCapture18))
		return false;
	return super.isProperType(admitCapture18);
}

//{ObjectTeams: more queries in preparition of RoleTypeBinding:
/**
 * Overridden in RoleTypeBinding.
 * @return the source type (even if we had a RoleTypeBinding).
 */
public ReferenceBinding getRealType() {
	if (this.roleModel != null)
	{
		ReferenceBinding ifcPart = this.roleModel.getInterfacePartBinding();
		if (ifcPart != null) {
			if (isRawType() && !ifcPart.isRawType())
				return (ReferenceBinding) ((ParameterizedTypeBinding)this).environment.convertToRawType(ifcPart, false);
			return ifcPart;
		}
	}
    return this;
}

/**
 * Overridden in RoleTypeBinding.
 * @return the class (even if we had a RoleTypeBinding).
 */
public ReferenceBinding getRealClass() {
	if (this.roleModel != null)
	{
		ReferenceBinding classPart = this.roleModel.getClassPartBinding();
		if (classPart != null)
			return classPart;
	}
    return this;
}
/** 
 * Return a version of other that has the same type arguments as this.
 */
public ReferenceBinding transferTypeArguments(ReferenceBinding other) {
	// only subclasses do real work
	return other;
}
// SH}


/**
 * Answer true if the receiver type can be assigned to the argument type (right)
 * In addition to improving performance, caching also ensures there is no infinite regression
 * since per nature, the compatibility check is recursive through parameterized type arguments (122775)
 */
public boolean isCompatibleWith(TypeBinding otherType, /*@Nullable*/ Scope captureScope) {
//{ObjectTeams: behind the facade introduce new parameter useObjectShortcut.
	return isCompatibleWith(otherType, true, captureScope);
}
// version which does not consider everything conform to Object:
public boolean isStrictlyCompatibleWith(TypeBinding otherType, /*@Nullable*/ Scope captureScope) {
	return isCompatibleWith(otherType, false, captureScope);
}
public boolean isCompatibleWith(TypeBinding otherType, boolean useObjectShortcut, /*@Nullable*/ Scope captureScope) {
// SH}
	if (equalsEquals(otherType, this))
		return true;

//{ObjectTeams: respect new argument useObjectShortcut:
/* orig:
	if (otherType.id == TypeIds.T_JavaLangObject)
  :giro */
	if (otherType.id == TypeIds.T_JavaLangObject && useObjectShortcut)
// SH}
		return true;
	Object result;
	if (this.compatibleCache == null) {
		this.compatibleCache = new SimpleLookupTable(3);
		result = null;
	} else {
		result = this.compatibleCache.get(otherType); // [dbg reset] this.compatibleCache.put(otherType,null)
		if (result != null) {
			return result == Boolean.TRUE;
		}
	}
	this.compatibleCache.put(otherType, Boolean.FALSE); // protect from recursive call
//{ObjectTeams: propagate:
/* orig:
	if (isCompatibleWith0(otherType, captureScope)) {
  :giro */
	if (isCompatibleWith0(otherType, useObjectShortcut, captureScope)) {
// SH}
		this.compatibleCache.put(otherType, Boolean.TRUE);
		return true;
	}
	if (captureScope == null 
			&& this instanceof TypeVariableBinding 
			&& ((TypeVariableBinding)this).firstBound instanceof ParameterizedTypeBinding) {
		// see https://bugs.eclipse.org/395002#c9
		// in this case a subsequent check with captureScope != null may actually get
		// a better result, reset this info to ensure we're not blocking that re-check.
		this.compatibleCache.put(otherType, null);
	}
	return false;
}

//{ObjectTeams: after re-wiring superclass/ifc reset all false values in the compatibleCache
public void resetIncompatibleTypes() {
	if (this.compatibleCache == null) return;
	for (int i=0; i< this.compatibleCache.valueTable.length; i++) {
		if (this.compatibleCache.valueTable[i] == Boolean.FALSE) {
			this.compatibleCache.keyTable[i] = null;
			this.compatibleCache.elementSize--;
		}
	}
}
// SH}
/**
 * Answer true if the receiver type can be assigned to the argument type (right)
 */
//{ObjectTeams: new parameter useObjectShortcut
private boolean isCompatibleWith0(TypeBinding otherType, boolean useObjectShortcut, /*@Nullable*/ Scope captureScope) {
// SH}
	if (TypeBinding.equalsEquals(otherType, this))
		return true;
//{ObjectTeams: respect new param:
/*orig:
	if (otherType.id == TypeIds.T_JavaLangObject)
 :giro*/
	if (otherType.id == TypeIds.T_JavaLangObject && useObjectShortcut)
// SH}
		return true;
	// equivalence may allow compatibility with array type through wildcard
	// bound
	if (isEquivalentTo(otherType))
		return true;
	switch (otherType.kind()) {
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE:
			return false; // should have passed equivalence check above if
							// wildcard
		case Binding.TYPE_PARAMETER :
			// check compatibility with capture of ? super X
			if (otherType.isCapture()) {
				CaptureBinding otherCapture = (CaptureBinding) otherType;
				TypeBinding otherLowerBound;
				if ((otherLowerBound = otherCapture.lowerBound) != null) {
					if (otherLowerBound.isArrayType()) return false;
					return isCompatibleWith(otherLowerBound);
				}
			}
			if (otherType instanceof InferenceVariable) {
				// may interpret InferenceVariable as a joker, but only when within an outer lambda inference:
				if (captureScope != null) {
					MethodScope methodScope = captureScope.methodScope();
					if (methodScope != null) {
						ReferenceContext referenceContext = methodScope.referenceContext;
						if (referenceContext instanceof LambdaExpression
								&& ((LambdaExpression)referenceContext).inferenceContext != null)
							return true;
					}
				}
			}
			//$FALL-THROUGH$
		case Binding.GENERIC_TYPE :
		case Binding.TYPE :
		case Binding.PARAMETERIZED_TYPE :
		case Binding.RAW_TYPE :
		case Binding.INTERSECTION_TYPE18 :
			switch (kind()) {
				case Binding.GENERIC_TYPE :
				case Binding.PARAMETERIZED_TYPE :
				case Binding.RAW_TYPE :
					if (TypeBinding.equalsEquals(erasure(), otherType.erasure())) 
						return false; // should have passed equivalence check
										// above if same erasure
			}
			ReferenceBinding otherReferenceType = (ReferenceBinding) otherType;
			if (otherReferenceType.isIntersectionType18()) {
				ReferenceBinding[] intersectingTypes = ((IntersectionTypeBinding18)otherReferenceType).intersectingTypes;
				for (ReferenceBinding binding : intersectingTypes) {
					if (!isCompatibleWith(binding))
						return false;
				}
				return true;
			}
			if (otherReferenceType.isInterface()) { // could be annotation type
				if (implementsInterface(otherReferenceType, true))
					return true;
				if (this instanceof TypeVariableBinding && captureScope != null) {
					TypeVariableBinding typeVariable = (TypeVariableBinding) this;
					if (typeVariable.firstBound instanceof ParameterizedTypeBinding) {
						TypeBinding bound = typeVariable.firstBound.capture(captureScope, -1, -1); // no position needed as this capture will never escape this context
						return bound.isCompatibleWith(otherReferenceType);
					}
				}
			}
//{ObjectTeams: only leave if we have checked for java.lang.Object above:
/* orig:
			if (isInterface())  // Explicit conversion from an interface
										// to a class is not allowed
  :giro */
			if (isInterface() && useObjectShortcut)
// SH}
				return false;
			return otherReferenceType.isSuperclassOf(this);
		default :
			return false;
	}
}
//{ObjectTeams: type weakening and lowering
public ReferenceBinding weakenFrom(ReferenceBinding other) {
	return other;
}
public boolean isCompatibleViaLowering(ReferenceBinding other) {
	return false;
}
// SH}
public boolean isSubtypeOf(TypeBinding other) {
	if (isSubTypeOfRTL(other))
		return true;
	// TODO: if this has wildcards, perform capture before the next call:
	TypeBinding candidate = findSuperTypeOriginatingFrom(other);
	if (candidate == null)
		return false;
	if (TypeBinding.equalsEquals(candidate, other))
		return true;
	
	// T<Ai...> <: T#RAW:
	if (other.isRawType() && TypeBinding.equalsEquals(candidate.erasure(), other.erasure()))
		return true;
	
	TypeBinding[] sis = other.typeArguments();
	TypeBinding[] tis = candidate.typeArguments();
	if (tis == null || sis == null)
		return false;
	if (sis.length != tis.length)
		return false;
	for (int i = 0; i < sis.length; i++) {
		if (!tis[i].isTypeArgumentContainedBy(sis[i]))
			return false;
	}
	return true;
}

protected boolean isSubTypeOfRTL(TypeBinding other) {
	if (TypeBinding.equalsEquals(this, other))
		return true;
	if (other instanceof CaptureBinding) {
		// for this one kind we must first unwrap the rhs:
		TypeBinding lower = ((CaptureBinding) other).lowerBound;
		return (lower != null && isSubtypeOf(lower));
	}
	if (other instanceof ReferenceBinding) {
		TypeBinding[] intersecting = ((ReferenceBinding) other).getIntersectingTypes();
		if (intersecting != null) {
			for (int i = 0; i < intersecting.length; i++) {
				if (!isSubtypeOf(intersecting[i]))
					return false;
			}
			return true;
		}
	}
	return false;
}

/**
 * Answer true if the receiver has default visibility
 */
public final boolean isDefault() {
	return (this.modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate)) == 0;
}

/**
 * Answer true if the receiver is a deprecated type
 */
public final boolean isDeprecated() {
	return (this.modifiers & ClassFileConstants.AccDeprecated) != 0;
}

public boolean isEnum() {
	return (this.modifiers & ClassFileConstants.AccEnum) != 0;
}

/**
 * Answer true if the receiver is final and cannot be subclassed
 */
public final boolean isFinal() {
	return (this.modifiers & ClassFileConstants.AccFinal) != 0;
}

/**
 * Returns true if the type hierarchy is being connected
 */
public boolean isHierarchyBeingConnected() {
	return (this.tagBits & TagBits.EndHierarchyCheck) == 0 && (this.tagBits & TagBits.BeginHierarchyCheck) != 0;
}
/**
 * Returns true if the type hierarchy is being connected "actively" i.e not paused momentatrily, 
 * while resolving type arguments. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=294057
 */
public boolean isHierarchyBeingActivelyConnected() {
	return (this.tagBits & TagBits.EndHierarchyCheck) == 0 && (this.tagBits & TagBits.BeginHierarchyCheck) != 0 && (this.tagBits & TagBits.PauseHierarchyCheck) == 0;
}

/**
 * Returns true if the type hierarchy is connected
 */
public boolean isHierarchyConnected() {
	return true;
}

public boolean isInterface() {
	// consider strict interfaces and annotation types
	return (this.modifiers & ClassFileConstants.AccInterface) != 0;
}

public boolean isFunctionalInterface(Scope scope) {
	MethodBinding method;
	return isInterface() && (method = getSingleAbstractMethod(scope, true)) != null && method.isValidBinding();
}

/**
 * Answer true if the receiver has private visibility
 */
public final boolean isPrivate() {
	return (this.modifiers & ClassFileConstants.AccPrivate) != 0;
}

/**
 * Answer true if the receiver or any of its enclosing types have private visibility
 */
public final boolean isOrEnclosedByPrivateType() {
	if (isLocalType()) return true; // catch all local types
	ReferenceBinding type = this;
	while (type != null) {
		if ((type.modifiers & ClassFileConstants.AccPrivate) != 0)
			return true;
		type = type.enclosingType();
	}
	return false;
}

/**
 * Answer true if the receiver has protected visibility
 */
public final boolean isProtected() {
	return (this.modifiers & ClassFileConstants.AccProtected) != 0;
}

/**
 * Answer true if the receiver has public visibility
 */
public final boolean isPublic() {
	return (this.modifiers & ClassFileConstants.AccPublic) != 0;
}

/**
 * Answer true if the receiver is a static member type (or toplevel)
 */
public final boolean isStatic() {
//{ObjectTeams: roles, even their interface part, are not static:
	if ((this.modifiers & ExtraCompilerModifiers.AccRole) != 0) return false;
// SH}
	return (this.modifiers & (ClassFileConstants.AccStatic | ClassFileConstants.AccInterface)) != 0 || (this.tagBits & TagBits.IsNestedType) == 0;
}

/**
 * Answer true if all float operations must adher to IEEE 754 float/double rules
 */
public final boolean isStrictfp() {
	return (this.modifiers & ClassFileConstants.AccStrictfp) != 0;
}

/**
 * Answer true if the receiver is in the superclass hierarchy of aType
 * NOTE: Object.isSuperclassOf(Object) -> false
 */
public boolean isSuperclassOf(ReferenceBinding otherType) {
	while ((otherType = otherType.superclass()) != null) {
		if (otherType.isEquivalentTo(this)) return true;
	}
	return false;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isThrowable()
 */
public boolean isThrowable() {
	ReferenceBinding current = this;
	do {
		switch (current.id) {
			case TypeIds.T_JavaLangThrowable :
			case TypeIds.T_JavaLangError :
			case TypeIds.T_JavaLangRuntimeException :
			case TypeIds.T_JavaLangException :
				return true;
		}
	} while ((current = current.superclass()) != null);
	return false;
}

/**
 * JLS 11.5 ensures that Throwable, Exception, RuntimeException and Error are directly connected.
 * (Throwable<- Exception <- RumtimeException, Throwable <- Error). Thus no need to check #isCompatibleWith
 * but rather check in type IDs so as to avoid some eager class loading for JCL writers.
 * When 'includeSupertype' is true, answers true if the given type can be a supertype of some unchecked exception
 * type (i.e. Throwable or Exception).
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isUncheckedException(boolean)
 */
public boolean isUncheckedException(boolean includeSupertype) {
	switch (this.id) {
			case TypeIds.T_JavaLangError :
			case TypeIds.T_JavaLangRuntimeException :
				return true;
			case TypeIds.T_JavaLangThrowable :
			case TypeIds.T_JavaLangException :
				return includeSupertype;
	}
	ReferenceBinding current = this;
	while ((current = current.superclass()) != null) {
		switch (current.id) {
			case TypeIds.T_JavaLangError :
			case TypeIds.T_JavaLangRuntimeException :
				return true;
			case TypeIds.T_JavaLangThrowable :
			case TypeIds.T_JavaLangException :
				return false;
		}
	}
	return false;
}

/**
 * Answer true if the receiver has private visibility and is used locally
 */
public final boolean isUsed() {
	return (this.modifiers & ExtraCompilerModifiers.AccLocallyUsed) != 0;
}

/**
 * Answer true if the receiver is deprecated (or any of its enclosing types)
 */
public final boolean isViewedAsDeprecated() {
	if ((this.modifiers & (ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDeprecatedImplicitly)) != 0)
		return true;
	if (getPackage().isViewedAsDeprecated()) {
		this.tagBits |= (getPackage().tagBits & TagBits.AnnotationTerminallyDeprecated);
		return true;
	}
	return false;
}

public ReferenceBinding[] memberTypes() {
	return Binding.NO_MEMBER_TYPES;
}

public MethodBinding[] methods() {
	return Binding.NO_METHODS;
}

public final ReferenceBinding outermostEnclosingType() {
	ReferenceBinding current = this;
	while (true) {
		ReferenceBinding last = current;
		if ((current = current.enclosingType()) == null)
			return last;
	}
}
//{ObjectTeams: the following output-oriented methods should
//              never use field sourceName directly, but only sourceName()!
/**
 * Answer the source name for the type.
 * In the case of member types, as the qualified name from its top level type.
 * For example, for a member type N defined inside M & A: "A.M.N".
 */
// orig (if internal names are desired, add a method qualifiedInternalName(),
//       here and in sub-classes.):
public char[] qualifiedSourceName() {
	if (isMemberType())
		return CharOperation.concat(enclosingType().qualifiedSourceName(), sourceName(), '.');
	return sourceName();
}

/**
 * Answer the receiver's signature.
 *
 * NOTE: This method should only be used during/after code gen.
 */
public char[] readableName() /*java.lang.Object,  p.X<T> */ {
	return readableName(true);
}
public char[] readableName(boolean showGenerics) /*java.lang.Object,  p.X<T> */ {
    char[] readableName;
	if (isMemberType()) {
		readableName = CharOperation.concat(enclosingType().readableName(showGenerics && !isStatic()), sourceName(), '.'); // OT: was sourceName
	} else {
		readableName = CharOperation.concatWith(this.compoundName, '.');
	}
	if (showGenerics) {
		TypeVariableBinding[] typeVars;
//{ObjectTeams: include value parameters:
		SyntheticArgumentBinding[] valueParams = valueParamSynthArgs();
/* orig:
		if ((typeVars = typeVariables()) != Binding.NO_TYPE_VARIABLES) {
  :giro */
		if ((typeVars = typeVariables()) != Binding.NO_TYPE_VARIABLES || valueParams != Binding.NO_SYNTH_ARGUMENTS) {
// orig:
	    	StringBuffer nameBuffer = new StringBuffer(10);
	    	nameBuffer.append(readableName).append('<');
// :giro
		    for (int i = 0; i < valueParams.length; i++) {
		    	nameBuffer.append('@');
				nameBuffer.append(valueParams[i].readableName());
				if (typeVars.length > 0)
					nameBuffer.append(',');
			}
// SH}
		    for (int i = 0, length = typeVars.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
	    	    nameBuffer.append(typeVars[i].readableName());
		    }
	    	nameBuffer.append('>');
			int nameLength = nameBuffer.length();
			readableName = new char[nameLength];
			nameBuffer.getChars(0, nameLength, readableName, 0);
		}
	}
	return readableName;
}

protected void appendNullAnnotation(StringBuffer nameBuffer, CompilerOptions options) {
	if (options.isAnnotationBasedNullAnalysisEnabled) {
		if (options.usesNullTypeAnnotations()) {
			for (AnnotationBinding annotation : this.typeAnnotations) {
				ReferenceBinding annotationType = annotation.getAnnotationType();
				if (annotationType.hasNullBit(TypeIds.BitNonNullAnnotation|TypeIds.BitNullableAnnotation)) {
					nameBuffer.append('@').append(annotationType.shortReadableName()).append(' ');
				}
			}
		} else {
			// restore applied null annotation from tagBits:
		    if ((this.tagBits & TagBits.AnnotationNonNull) != 0) {
		    	char[][] nonNullAnnotationName = options.nonNullAnnotationName;
				nameBuffer.append('@').append(nonNullAnnotationName[nonNullAnnotationName.length-1]).append(' ');
		    }
		    if ((this.tagBits & TagBits.AnnotationNullable) != 0) {
		    	char[][] nullableAnnotationName = options.nullableAnnotationName;
				nameBuffer.append('@').append(nullableAnnotationName[nullableAnnotationName.length-1]).append(' ');
		    }
		}
	}
}

public AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
	SimpleLookupTable store = storedAnnotations(forceInitialization, false);
	return store == null ? null : (AnnotationHolder) store.get(binding);
}

//{ObjectTeams: accessible to classes in org.eclipse.objectteams...:
public
// SH}
AnnotationBinding[] retrieveAnnotations(Binding binding) {
	AnnotationHolder holder = retrieveAnnotationHolder(binding, true);
	return holder == null ? Binding.NO_ANNOTATIONS : holder.getAnnotations();
}

public void setAnnotations(AnnotationBinding[] annotations, boolean forceStore) {
	storeAnnotations(this, annotations, forceStore);
}
public void setContainerAnnotationType(ReferenceBinding value) {
	// Leave this to subclasses
}
public void tagAsHavingDefectiveContainerType() {
	// Leave this to subclasses
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#nullAnnotatedReadableName(CompilerOptions,boolean)
 */
public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
	if (shortNames)
		return nullAnnotatedShortReadableName(options);
	return nullAnnotatedReadableName(options);
}

char[] nullAnnotatedReadableName(CompilerOptions options) {
    StringBuffer nameBuffer = new StringBuffer(10);
	if (isMemberType()) {
		nameBuffer.append(enclosingType().nullAnnotatedReadableName(options, false));
		nameBuffer.append('.');
		appendNullAnnotation(nameBuffer, options);
		nameBuffer.append(this.sourceName);
	} else if (this.compoundName != null) {
		int i;
		int l=this.compoundName.length;
		for (i=0; i<l-1; i++) {
			nameBuffer.append(this.compoundName[i]);
			nameBuffer.append('.');
		}
	    appendNullAnnotation(nameBuffer, options);
		nameBuffer.append(this.compoundName[i]);
	} else {
		// case of TypeVariableBinding with nullAnnotationTagBits:
		appendNullAnnotation(nameBuffer, options);
		if (this.sourceName != null)
			nameBuffer.append(this.sourceName);
		else // WildcardBinding, CaptureBinding have no sourceName
			nameBuffer.append(this.readableName());
	}
	TypeBinding [] arguments = typeArguments();
	if (arguments != null && arguments.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
		nameBuffer.append('<');
	    for (int i = 0, length = arguments.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(arguments[i].nullAnnotatedReadableName(options, false));
	    }
	    nameBuffer.append('>');
	}
	int nameLength = nameBuffer.length();
	char[] readableName = new char[nameLength];
	nameBuffer.getChars(0, nameLength, readableName, 0);
    return readableName;
}

char[] nullAnnotatedShortReadableName(CompilerOptions options) {
    StringBuffer nameBuffer = new StringBuffer(10);
	if (isMemberType()) {
		nameBuffer.append(enclosingType().nullAnnotatedReadableName(options, true));
		nameBuffer.append('.');
		appendNullAnnotation(nameBuffer, options);
		nameBuffer.append(this.sourceName);
	} else {
		appendNullAnnotation(nameBuffer, options);
		if (this.sourceName != null)
			nameBuffer.append(this.sourceName);
		else // WildcardBinding, CaptureBinding have no sourceName
			nameBuffer.append(this.shortReadableName());
	}
	TypeBinding [] arguments = typeArguments();
	if (arguments != null && arguments.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
		nameBuffer.append('<');
	    for (int i = 0, length = arguments.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(arguments[i].nullAnnotatedReadableName(options, true));
	    }
	    nameBuffer.append('>');
	}
	int nameLength = nameBuffer.length();
	char[] shortReadableName = new char[nameLength];
	nameBuffer.getChars(0, nameLength, shortReadableName, 0);
    return shortReadableName;
}

public char[] shortReadableName() /*Object*/ {
	return shortReadableName(true);
}
public char[] shortReadableName(boolean showGenerics) /*Object*/ {
	char[] shortReadableName;
	if (isMemberType()) {
		shortReadableName = CharOperation.concat(enclosingType().shortReadableName(showGenerics && !isStatic()), sourceName(), '.'); // OT: was sourceName
	} else {
		shortReadableName = sourceName(); // OT: was sourceName
	}
	if (showGenerics) {
		TypeVariableBinding[] typeVars;
//{ObjectTeams: include value parameters:
		SyntheticArgumentBinding[] valueParams = valueParamSynthArgs();
/* orig:
		if ((typeVars = typeVariables()) != Binding.NO_TYPE_VARIABLES) {
  :giro */
		if ((typeVars = typeVariables()) != Binding.NO_TYPE_VARIABLES || valueParams != Binding.NO_SYNTH_ARGUMENTS) {
// orig:		
		    StringBuffer nameBuffer = new StringBuffer(10);
		    nameBuffer.append(shortReadableName).append('<');
// :giro
		    for (int i = 0; i < valueParams.length; i++) {
		    	nameBuffer.append('@');
				nameBuffer.append(valueParams[i].shortReadableName());
				if (typeVars.length > 0)
					nameBuffer.append(',');
			}
// SH}
		    for (int i = 0, length = typeVars.length; i < length; i++) {
	    	    if (i > 0) nameBuffer.append(',');
	        	nameBuffer.append(typeVars[i].shortReadableName());
		    }
		    nameBuffer.append('>');
			int nameLength = nameBuffer.length();
			shortReadableName = new char[nameLength];
			nameBuffer.getChars(0, nameLength, shortReadableName, 0);
		}
	}
	return shortReadableName;
}
// SH} // end sourceName - edited section

//{ObjectTeams: to be overridden in RoleTypeBinding
public char[] optimalName() {
	return readableName();
}
// SH}
//{ObjectTeams: beautify for completion:
@Override
public char[] genericTypeSignature(boolean retrenchCallin) {
	char[] result = genericTypeSignature();
	if (!retrenchCallin)
		return result;
	return CharOperation.replace(result, IOTConstants.OT_DELIM_NAME, new char[0]);
}
// SH}
public char[] signature() /* Ljava/lang/Object; */ {
	if (this.signature != null)
		return this.signature;

	return this.signature = CharOperation.concat('L', constantPoolName(), ';');
}

public char[] sourceName() {
//{ObjectTeams: human readable source name, stripping __OT__ prefix if present
    if (isSourceRole() && RoleSplitter.isClassPartName(this.sourceName))
        return RoleSplitter.getInterfacePartName(this.sourceName);
	return this.sourceName;
}
// original sourceName():
public char[] internalName() {
	return this.sourceName;
}
// SH}

/**
 * Perform an upwards type projection as per JLS 4.10.5
 * @param scope Relevant scope for evaluating type projection
 * @param mentionedTypeVariables Filter for mentioned type variabled
 * @returns Upwards type projection of 'this', or null if downwards projection is undefined 
*/
public ReferenceBinding upwardsProjection(Scope scope, TypeBinding[] mentionedTypeVariables) {
	return this;
}

/**
 * Perform a downwards type projection as per JLS 4.10.5
 * @param scope Relevant scope for evaluating type projection
 * @param mentionedTypeVariables Filter for mentioned type variabled
 * @returns Downwards type projection of 'this', or null if downwards projection is undefined 
*/
public ReferenceBinding downwardsProjection(Scope scope, TypeBinding[] mentionedTypeVariables) {
	return this;
}

void storeAnnotationHolder(Binding binding, AnnotationHolder holder) {
	if (holder == null) {
		SimpleLookupTable store = storedAnnotations(false, false);
		if (store != null)
			store.removeKey(binding);
	} else {
		SimpleLookupTable store = storedAnnotations(true, false);
		if (store != null)
			store.put(binding, holder);
	}
}

//{ObjectTeams: accessible to classes in org.eclipse.objectteams...:
public
// SH}
void storeAnnotations(Binding binding, AnnotationBinding[] annotations, boolean forceStore) {
	AnnotationHolder holder = null;
	if (annotations == null || annotations.length == 0) {
		SimpleLookupTable store = storedAnnotations(false, forceStore);
		if (store != null)
			holder = (AnnotationHolder) store.get(binding);
		if (holder == null) return; // nothing to delete
	} else {
		SimpleLookupTable store = storedAnnotations(true, forceStore);
		if (store == null) return; // not supported
		holder = (AnnotationHolder) store.get(binding);
		if (holder == null)
			holder = new AnnotationHolder();
	}
	storeAnnotationHolder(binding, holder.setAnnotations(annotations));
}

SimpleLookupTable storedAnnotations(boolean forceInitialize, boolean forceStore) {
	return null; // overrride if interested in storing annotations for the receiver, its fields and methods
}

public ReferenceBinding superclass() {
	return null;
}

public ReferenceBinding[] superInterfaces() {
	return Binding.NO_SUPERINTERFACES;
}

public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
	if (isStatic()) return null;
	ReferenceBinding enclosingType = enclosingType();
	if (enclosingType == null)
		return null;
	return new ReferenceBinding[] {enclosingType};
}

MethodBinding[] unResolvedMethods() { // for the MethodVerifier so it doesn't resolve types
	return methods();
}

public FieldBinding[] unResolvedFields() {
	return Binding.NO_FIELDS;
}

/*
 * If a type - known to be a Closeable - is mentioned in one of our white lists
 * answer the typeBit for the white list (BitWrapperCloseable or BitResourceFreeCloseable).
 */
protected int applyCloseableClassWhitelists() {
	switch (this.compoundName.length) {
		case 3:
			if (CharOperation.equals(TypeConstants.JAVA, this.compoundName[0])) {
				if (CharOperation.equals(TypeConstants.IO, this.compoundName[1])) {
					char[] simpleName = this.compoundName[2];
					int l = TypeConstants.JAVA_IO_WRAPPER_CLOSEABLES.length;
					for (int i = 0; i < l; i++) {
						if (CharOperation.equals(simpleName, TypeConstants.JAVA_IO_WRAPPER_CLOSEABLES[i]))
							return TypeIds.BitWrapperCloseable;
					}
					l = TypeConstants.JAVA_IO_RESOURCE_FREE_CLOSEABLES.length;
					for (int i = 0; i < l; i++) {
						if (CharOperation.equals(simpleName, TypeConstants.JAVA_IO_RESOURCE_FREE_CLOSEABLES[i]))
							return TypeIds.BitResourceFreeCloseable;
					}
				}
			}
			break;
		case 4:
			if (CharOperation.equals(TypeConstants.JAVA, this.compoundName[0])) {
				if (CharOperation.equals(TypeConstants.UTIL, this.compoundName[1])) {
					if (CharOperation.equals(TypeConstants.ZIP, this.compoundName[2])) {
						char[] simpleName = this.compoundName[3];
						int l = TypeConstants.JAVA_UTIL_ZIP_WRAPPER_CLOSEABLES.length;
						for (int i = 0; i < l; i++) {
							if (CharOperation.equals(simpleName, TypeConstants.JAVA_UTIL_ZIP_WRAPPER_CLOSEABLES[i]))
								return TypeIds.BitWrapperCloseable;
						}
					}
				}
			}
			break;
	}
	int l = TypeConstants.OTHER_WRAPPER_CLOSEABLES.length;
	for (int i = 0; i < l; i++) {
		if (CharOperation.equals(this.compoundName, TypeConstants.OTHER_WRAPPER_CLOSEABLES[i]))
			return TypeIds.BitWrapperCloseable;
	}	
	return 0;
}

/*
 * If a type - known to be a Closeable - is mentioned in one of our white lists
 * answer the typeBit for the white list (BitWrapperCloseable or BitResourceFreeCloseable).
 */
protected int applyCloseableInterfaceWhitelists() {
	switch (this.compoundName.length) {
		case 4:
			if (CharOperation.equals(this.compoundName, TypeConstants.RESOURCE_FREE_CLOSEABLE_STREAM))
				return TypeIds.BitResourceFreeCloseable;
			break;
	}
	return 0;
}

//{ObjectTeams: support for checking substitution of a value parameter 
public VariableBinding valueParamSynthArgAt(int typeParamPosition) {
	SyntheticArgumentBinding[] args = valueParamSynthArgs();
	if (args.length > typeParamPosition)
		return args[typeParamPosition];
	return null;
}
// SH}

protected MethodBinding [] getInterfaceAbstractContracts(Scope scope, boolean replaceWildcards, boolean filterDefaultMethods) throws InvalidInputException {
	
	if (!isInterface() || !isValidBinding()) {
		throw new InvalidInputException("Not a functional interface"); //$NON-NLS-1$
	}
	
	MethodBinding [] methods = methods();
	MethodBinding [] contracts = new MethodBinding[0];
	int contractsCount = 0;
	int contractsLength = 0;
	
	ReferenceBinding [] superInterfaces = superInterfaces();
	for (int i = 0, length = superInterfaces.length; i < length; i++) {
		// filterDefaultMethods=false => keep default methods needed to filter out any abstract methods they may override:
		MethodBinding [] superInterfaceContracts = superInterfaces[i].getInterfaceAbstractContracts(scope, replaceWildcards, false);
		final int superInterfaceContractsLength = superInterfaceContracts == null  ? 0 : superInterfaceContracts.length;
		if (superInterfaceContractsLength == 0) continue;
		if (contractsLength < contractsCount + superInterfaceContractsLength) {
			System.arraycopy(contracts, 0, contracts = new MethodBinding[contractsLength = contractsCount + superInterfaceContractsLength], 0, contractsCount);
		}
		System.arraycopy(superInterfaceContracts, 0, contracts, contractsCount,	superInterfaceContractsLength);
		contractsCount += superInterfaceContractsLength;
	}

	LookupEnvironment environment = scope.environment();
	for (int i = 0, length = methods == null ? 0 : methods.length; i < length; i++) {
		final MethodBinding method = methods[i];
		if (method == null || method.isStatic() || method.redeclaresPublicObjectMethod(scope) || method.isPrivate()) 
			continue;
		if (!method.isValidBinding()) 
			throw new InvalidInputException("Not a functional interface"); //$NON-NLS-1$
		for (int j = 0; j < contractsCount;) {
			if ( contracts[j] != null && MethodVerifier.doesMethodOverride(method, contracts[j], environment)) {
				contractsCount--;
				// abstract method from super type overridden by present interface ==> contracts[j] = null;
				if (j < contractsCount) {
					System.arraycopy(contracts, j+1, contracts, j, contractsCount - j);
					continue;
				}
			}
			j++;
		}
		if (filterDefaultMethods && method.isDefaultMethod())
			continue; // skip default method itself
		if (contractsCount == contractsLength) {
			System.arraycopy(contracts, 0, contracts = new MethodBinding[contractsLength += 16], 0, contractsCount);
		}
		if(environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
			ImplicitNullAnnotationVerifier.ensureNullnessIsKnown(method, scope);
		}
		contracts[contractsCount++] = method;
	}
	// check mutual overriding of inherited methods (i.e., not from current type):
	for (int i = 0; i < contractsCount; i++) {
		MethodBinding contractI = contracts[i];
		if (TypeBinding.equalsEquals(contractI.declaringClass, this))
			continue;
		for (int j = 0; j < contractsCount; j++) {
			MethodBinding contractJ = contracts[j];
			if (i == j || TypeBinding.equalsEquals(contractJ.declaringClass, this))
				continue;
			if (contractI == contractJ || MethodVerifier.doesMethodOverride(contractI, contractJ, environment)) {
				contractsCount--;
				// abstract method from one super type overridden by other super interface ==> contracts[j] = null;
				if (j < contractsCount) {
					System.arraycopy(contracts, j+1, contracts, j, contractsCount - j);
				}				
				j--;
				if (j < i)
					i--;
				continue;
			}
		}
		if (filterDefaultMethods && contractI.isDefaultMethod()) {
			contractsCount--;
			// remove default method after it has eliminated any matching abstract methods from contracts
			if (i < contractsCount) {
				System.arraycopy(contracts, i+1, contracts, i, contractsCount - i);
			}				
			i--;				
		}
	}
	if (contractsCount < contractsLength) {
		System.arraycopy(contracts, 0, contracts = new MethodBinding[contractsCount], 0, contractsCount);
	}
	return contracts;
}
public MethodBinding getSingleAbstractMethod(Scope scope, boolean replaceWildcards) {
	
	int index = replaceWildcards ? 0 : 1;
	if (this.singleAbstractMethod != null) {
		if (this.singleAbstractMethod[index] != null)
		return this.singleAbstractMethod[index];
	} else {
		this.singleAbstractMethod = new MethodBinding[2];
	}

	if (this.compoundName != null)
		scope.compilationUnitScope().recordQualifiedReference(this.compoundName);
	MethodBinding[] methods = null;
	try {
		methods = getInterfaceAbstractContracts(scope, replaceWildcards, true);
		if (methods == null || methods.length == 0)
			return this.singleAbstractMethod[index] = samProblemBinding;
		int contractParameterLength = 0;
		char [] contractSelector = null;
		for (int i = 0, length = methods.length; i < length; i++) {
			MethodBinding method = methods[i];
			if (method == null) continue;
			if (contractSelector == null) {
				contractSelector = method.selector;
				contractParameterLength = method.parameters == null ? 0 : method.parameters.length;
			} else {
				int methodParameterLength = method.parameters == null ? 0 : method.parameters.length;
				if (methodParameterLength != contractParameterLength || !CharOperation.equals(method.selector, contractSelector))
					return this.singleAbstractMethod[index] = samProblemBinding;
			}
		}
	} catch (InvalidInputException e) {
		return this.singleAbstractMethod[index] = samProblemBinding;
	}
	if (methods.length == 1)
		return this.singleAbstractMethod[index] = methods[0];
	
	final LookupEnvironment environment = scope.environment();
	boolean genericMethodSeen = false;
	int length = methods.length;
	boolean analyseNullAnnotations = environment.globalOptions.isAnnotationBasedNullAnalysisEnabled;
	
	next:for (int i = length - 1; i >= 0; --i) {
		MethodBinding method = methods[i], otherMethod = null;
		if (method.typeVariables != Binding.NO_TYPE_VARIABLES)
			genericMethodSeen = true;
		TypeBinding returnType = method.returnType;
		TypeBinding[] parameters = method.parameters;
		for (int j = 0; j < length; j++) {
			if (i == j) continue;
			otherMethod = methods[j];
			if (otherMethod.typeVariables != Binding.NO_TYPE_VARIABLES)
				genericMethodSeen = true;
			
			if (genericMethodSeen) { // adapt type parameters.
				otherMethod = MethodVerifier.computeSubstituteMethod(otherMethod, method, environment);
				if (otherMethod == null)
					continue next;
			}
			if (!MethodVerifier.isSubstituteParameterSubsignature(method, otherMethod, environment) || !MethodVerifier.areReturnTypesCompatible(method, otherMethod, environment)) 
				continue next;
			if (analyseNullAnnotations) {
				returnType = NullAnnotationMatching.strongerType(returnType, otherMethod.returnType, environment);
				parameters = NullAnnotationMatching.weakerTypes(parameters, otherMethod.parameters, environment);
			}
		}
		// If we reach here, we found a method that is override equivalent with every other method and is also return type substitutable. Compute kosher exceptions now ...
		ReferenceBinding [] exceptions = new ReferenceBinding[0];
		int exceptionsCount = 0, exceptionsLength = 0;
		final MethodBinding theAbstractMethod = method;
		boolean shouldEraseThrows = theAbstractMethod.typeVariables == Binding.NO_TYPE_VARIABLES && genericMethodSeen;
		boolean shouldAdaptThrows = theAbstractMethod.typeVariables != Binding.NO_TYPE_VARIABLES;
		final int typeVariableLength = theAbstractMethod.typeVariables.length;
		
		none:for (i = 0; i < length; i++) {
			method = methods[i];
			ReferenceBinding[] methodThrownExceptions = method.thrownExceptions;
			int methodExceptionsLength = methodThrownExceptions == null ? 0: methodThrownExceptions.length;
			if (methodExceptionsLength == 0) break none;
			if (shouldAdaptThrows && method != theAbstractMethod) {
				System.arraycopy(methodThrownExceptions, 0, methodThrownExceptions = new ReferenceBinding[methodExceptionsLength], 0, methodExceptionsLength);
				for (int tv = 0; tv < typeVariableLength; tv++) {
					if (methodThrownExceptions[tv] instanceof TypeVariableBinding) {
						methodThrownExceptions[tv] = theAbstractMethod.typeVariables[tv];
					}
				}
			}
			nextException: for (int j = 0; j < methodExceptionsLength; j++) {
				ReferenceBinding methodException = methodThrownExceptions[j];
				if (shouldEraseThrows)
					methodException = (ReferenceBinding) methodException.erasure();
				nextMethod: for (int k = 0; k < length; k++) {
					if (i == k) continue;
					otherMethod = methods[k];
					ReferenceBinding[] otherMethodThrownExceptions = otherMethod.thrownExceptions;
					int otherMethodExceptionsLength =  otherMethodThrownExceptions == null ? 0 : otherMethodThrownExceptions.length;
					if (otherMethodExceptionsLength == 0) break none;
					if (shouldAdaptThrows && otherMethod != theAbstractMethod) {
						System.arraycopy(otherMethodThrownExceptions, 
								0, 
								otherMethodThrownExceptions = new ReferenceBinding[otherMethodExceptionsLength], 
								0, 
								otherMethodExceptionsLength);
						for (int tv = 0; tv < typeVariableLength; tv++) {
							if (otherMethodThrownExceptions[tv] instanceof TypeVariableBinding) {
								otherMethodThrownExceptions[tv] = theAbstractMethod.typeVariables[tv];
							}
						}
					}
					for (int l = 0; l < otherMethodExceptionsLength; l++) {
						ReferenceBinding otherException = otherMethodThrownExceptions[l];
						if (shouldEraseThrows)
							otherException = (ReferenceBinding) otherException.erasure();
						if (methodException.isCompatibleWith(otherException))
							continue nextMethod;
					}
					continue nextException;
				}
				// If we reach here, method exception or its super type is covered by every throws clause.
				if (exceptionsCount == exceptionsLength) {
					System.arraycopy(exceptions, 0, exceptions = new ReferenceBinding[exceptionsLength += 16], 0, exceptionsCount);
				}
				exceptions[exceptionsCount++] = methodException;
			}
		}
		if (exceptionsCount != exceptionsLength) {
			System.arraycopy(exceptions, 0, exceptions = new ReferenceBinding[exceptionsCount], 0, exceptionsCount);
		}
		this.singleAbstractMethod[index] = new MethodBinding(theAbstractMethod.modifiers | ClassFileConstants.AccSynthetic, 
				theAbstractMethod.selector, 
				returnType, 
				parameters, 
				exceptions, 
				theAbstractMethod.declaringClass);
	    this.singleAbstractMethod[index].typeVariables = theAbstractMethod.typeVariables;
		return this.singleAbstractMethod[index];
	}
	return this.singleAbstractMethod[index] = samProblemBinding;
}

// See JLS 4.9 bullet 1
public static boolean isConsistentIntersection(TypeBinding[] intersectingTypes) {
	TypeBinding[] ci = new TypeBinding[intersectingTypes.length];
	for (int i = 0; i < ci.length; i++) {
		TypeBinding current = intersectingTypes[i];
		ci[i] = (current.isClass() || current.isArrayType())
					? current : current.superclass();
	}
	TypeBinding mostSpecific = ci[0];
	for (int i = 1; i < ci.length; i++) {
		TypeBinding current = ci[i];
		// when invoked during type inference we only want to check inconsistency among real types:
		if (current.isTypeVariable() || current.isWildcard() || !current.isProperType(true))
			continue;
		if (mostSpecific.isSubtypeOf(current))
			continue;
		else if (current.isSubtypeOf(mostSpecific))
			mostSpecific = current;
		else
			return false;
	}
	return true;
}
public ModuleBinding module() {
	if (this.fPackage != null)
		return this.fPackage.enclosingModule;
	return null;
}
}
