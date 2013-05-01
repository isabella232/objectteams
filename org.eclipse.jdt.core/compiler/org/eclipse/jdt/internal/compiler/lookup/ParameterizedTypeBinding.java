/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;

/**
 * OTDT changes:
 * What: support playedBy generic (isBoundBase())
 *
 * What: share type models.
 *
 * A parameterized type encapsulates a type with type arguments,
 */
public class ParameterizedTypeBinding extends ReferenceBinding implements Substitution {

//{ObjectTeams: make visible to objectteams package
/* orig:
	private ReferenceBinding type; // must ensure the type is resolved
  :giro */
	public ReferenceBinding type; // must ensure the type is resolved
// SH}
	public TypeBinding[] arguments;
	public LookupEnvironment environment;
	public char[] genericTypeSignature;
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;
	public FieldBinding[] fields;
	public ReferenceBinding[] memberTypes;
	public MethodBinding[] methods;
	private ReferenceBinding enclosingType;

	public ParameterizedTypeBinding(ReferenceBinding type, TypeBinding[] arguments,  ReferenceBinding enclosingType, LookupEnvironment environment){
		this.environment = environment;
		this.enclosingType = enclosingType; // never unresolved, never lazy per construction
//		if (enclosingType != null && enclosingType.isGenericType()) {
//			RuntimeException e = new RuntimeException("PARAM TYPE with GENERIC ENCLOSING");
//			e.printStackTrace();
//			throw e;
//		}
//		if (!(type instanceof UnresolvedReferenceBinding) && type.typeVariables() == Binding.NO_TYPE_VARIABLES) {
//			System.out.println();
//		}
		initialize(type, arguments);
		if (type instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) type).addWrapper(this, environment);
		if (arguments != null) {
			for (int i = 0, l = arguments.length; i < l; i++)
				if (arguments[i] instanceof UnresolvedReferenceBinding)
					((UnresolvedReferenceBinding) arguments[i]).addWrapper(this, environment);
		}
		this.tagBits |=  TagBits.HasUnresolvedTypeVariables; // cleared in resolve()
	}

	/**
	 * May return an UnresolvedReferenceBinding.
	 * @see ParameterizedTypeBinding#genericType()
	 */
//{ObjectTeams: made public (was protected):
	public ReferenceBinding actualType() {
// SH}
		return this.type;
	}

//{ObjectTeams: make other inherited methods aware of type parameters:
	@Override
	public ReferenceBinding getRealClass() {
		ReferenceBinding realClass = super.getRealClass();
		if (realClass == this)
			return this; // no further wrapping if this _is_ the class part
		return this.environment.createParameterizedType(realClass, this.arguments, realClass.enclosingType()); 
	}
// FIXME(SH): this method causes regressions (still looks consistent, though):
//	@Override
//	public ReferenceBinding getRealType() {
//		ReferenceBinding realType = super.getRealType();
//		if (realType == this || isRawType())
//			return this; // no further wrapping if this _is_ the ifc part
//		return this.environment.createParameterizedType(realType, this.arguments, realType.enclosingType()); 
//	}
	/** {@inheritDoc} */
	@Override
	public ReferenceBinding transferTypeArguments(ReferenceBinding other) {
		return this.environment.createParameterizedType((ReferenceBinding)other.original(), this.arguments, other.enclosingType());
	}
// SH}
	/**
	 * Iterate type arguments, and validate them according to corresponding variable bounds.
	 */
	public void boundCheck(Scope scope, TypeReference[] argumentReferences) {
		if ((this.tagBits & TagBits.PassedBoundCheck) == 0) {
			boolean hasErrors = false;
			TypeVariableBinding[] typeVariables = this.type.typeVariables();
			if (this.arguments != null && typeVariables != null) { // arguments may be null in error cases
				for (int i = 0, length = typeVariables.length; i < length; i++) {
				    if (typeVariables[i].boundCheck(this, this.arguments[i], scope)  != TypeConstants.OK) {
				    	hasErrors = true;
				    	if ((this.arguments[i].tagBits & TagBits.HasMissingType) == 0) {
				    		// do not report secondary error, if type reference already got complained against
							scope.problemReporter().typeMismatchError(this.arguments[i], typeVariables[i], this.type, argumentReferences[i]);
				    	}
				    }
				}
			}
			if (!hasErrors) this.tagBits |= TagBits.PassedBoundCheck; // no need to recheck it in the future
		}
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#canBeInstantiated()
	 */
	public boolean canBeInstantiated() {
		return ((this.tagBits & TagBits.HasDirectWildcard) == 0) && super.canBeInstantiated(); // cannot instantiate param type with wildcard arguments
	}
	/**
	 * Perform capture conversion for a parameterized type with wildcard arguments
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#capture(Scope,int)
	 */
	public TypeBinding capture(Scope scope, int position) {
		if ((this.tagBits & TagBits.HasDirectWildcard) == 0)
			return this;

		TypeBinding[] originalArguments = this.arguments;
		int length = originalArguments.length;
		TypeBinding[] capturedArguments = new TypeBinding[length];

		// Retrieve the type context for capture bindingKey
		ReferenceBinding contextType = scope.enclosingSourceType();
		if (contextType != null) contextType = contextType.outermostEnclosingType(); // maybe null when used programmatically by DOM

		for (int i = 0; i < length; i++) {
			TypeBinding argument = originalArguments[i];
			if (argument.kind() == Binding.WILDCARD_TYPE) { // no capture for intersection types
				capturedArguments[i] = new CaptureBinding((WildcardBinding) argument, contextType, position, scope.compilationUnitScope().nextCaptureID());
			} else {
				capturedArguments[i] = argument;
			}
		}
		ParameterizedTypeBinding capturedParameterizedType = this.environment.createParameterizedType(this.type, capturedArguments, enclosingType());
		for (int i = 0; i < length; i++) {
			TypeBinding argument = capturedArguments[i];
			if (argument.isCapture()) {
				((CaptureBinding)argument).initializeBounds(scope, capturedParameterizedType);
			}
		}
		return capturedParameterizedType;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#collectMissingTypes(java.util.List)
	 */
	public List collectMissingTypes(List missingTypes) {
		if ((this.tagBits & TagBits.HasMissingType) != 0) {
			if (this.enclosingType != null) {
				missingTypes = this.enclosingType.collectMissingTypes(missingTypes);
			}
			missingTypes = genericType().collectMissingTypes(missingTypes);
			if (this.arguments != null) {
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					missingTypes = this.arguments[i].collectMissingTypes(missingTypes);
				}
			}
		}
		return missingTypes;
	}

	/**
	 * Collect the substitutes into a map for certain type variables inside the receiver type
	 * e.g.   Collection<T>.collectSubstitutes(Collection<List<X>>, Map), will populate Map with: T --> List<X>
	 * Constraints:
	 *   A << F   corresponds to:   F.collectSubstitutes(..., A, ..., CONSTRAINT_EXTENDS (1))
	 *   A = F   corresponds to:      F.collectSubstitutes(..., A, ..., CONSTRAINT_EQUAL (0))
	 *   A >> F   corresponds to:   F.collectSubstitutes(..., A, ..., CONSTRAINT_SUPER (2))
	 */
	public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
		if ((this.tagBits & TagBits.HasTypeVariable) == 0) {
			TypeBinding actualEquivalent = actualType.findSuperTypeOriginatingFrom(this.type);
			if (actualEquivalent != null && actualEquivalent.isRawType()) {
				inferenceContext.isUnchecked = true;
			}
			return;
		}
		if (actualType == TypeBinding.NULL) return;

		if (!(actualType instanceof ReferenceBinding)) return;
		TypeBinding formalEquivalent, actualEquivalent;
		switch (constraint) {
			case TypeConstants.CONSTRAINT_EQUAL :
			case TypeConstants.CONSTRAINT_EXTENDS :
				formalEquivalent = this;
		        actualEquivalent = actualType.findSuperTypeOriginatingFrom(this.type);
		        if (actualEquivalent == null) return;
		        break;
			case TypeConstants.CONSTRAINT_SUPER :
	        default:
		        formalEquivalent = this.findSuperTypeOriginatingFrom(actualType);
		        if (formalEquivalent == null) return;
		        actualEquivalent = actualType;
		        break;
		}
		// collect through enclosing type
		ReferenceBinding formalEnclosingType = formalEquivalent.enclosingType();
		if (formalEnclosingType != null) {
			formalEnclosingType.collectSubstitutes(scope, actualEquivalent.enclosingType(), inferenceContext, constraint);
		}
		// collect through type arguments
		if (this.arguments == null) return;
        TypeBinding[] formalArguments;
        switch (formalEquivalent.kind()) {
        	case Binding.GENERIC_TYPE :
        		formalArguments = formalEquivalent.typeVariables();
        		break;
        	case Binding.PARAMETERIZED_TYPE :
        		formalArguments = ((ParameterizedTypeBinding)formalEquivalent).arguments;
        		break;
        	case Binding.RAW_TYPE :
        		if (inferenceContext.depth > 0) {
	           		inferenceContext.status = InferenceContext.FAILED; // marker for impossible inference
        		}
        		return;
        	default :
        		return;
        }
        TypeBinding[] actualArguments;
        switch (actualEquivalent.kind()) {
        	case Binding.GENERIC_TYPE :
        		actualArguments = actualEquivalent.typeVariables();
        		break;
        	case Binding.PARAMETERIZED_TYPE :
        		actualArguments = ((ParameterizedTypeBinding)actualEquivalent).arguments;
        		break;
        	case Binding.RAW_TYPE :
        		if (inferenceContext.depth > 0) {
	           		inferenceContext.status = InferenceContext.FAILED; // marker for impossible inference
        		} else {
	        		inferenceContext.isUnchecked = true;
        		}
        		return;
        	default :
        		return;
        }
        inferenceContext.depth++;
        for (int i = 0, length = formalArguments.length; i < length; i++) {
        	TypeBinding formalArgument = formalArguments[i];
        	TypeBinding actualArgument = actualArguments[i];
        	if (formalArgument.isWildcard()) {
                formalArgument.collectSubstitutes(scope, actualArgument, inferenceContext, constraint);
                continue;
        	} else if (actualArgument.isWildcard()){
    			WildcardBinding actualWildcardArgument = (WildcardBinding) actualArgument;
    			if (actualWildcardArgument.otherBounds == null) {
    				if (constraint == TypeConstants.CONSTRAINT_SUPER) { // JLS 15.12.7, p.459
						switch(actualWildcardArgument.boundKind) {
		    				case Wildcard.EXTENDS :
		    					formalArgument.collectSubstitutes(scope, actualWildcardArgument.bound, inferenceContext, TypeConstants.CONSTRAINT_SUPER);
		    					continue;
		    				case Wildcard.SUPER :
		    					formalArgument.collectSubstitutes(scope, actualWildcardArgument.bound, inferenceContext, TypeConstants.CONSTRAINT_EXTENDS);
		    					continue;
		    				default :
		    					continue; // cannot infer anything further from unbound wildcard
		    			}
    				} else {
    					continue; // cannot infer anything further from wildcard
    				}
    			}
        	}
        	// by default, use EQUAL constraint
            formalArgument.collectSubstitutes(scope, actualArgument, inferenceContext, TypeConstants.CONSTRAINT_EQUAL);
        }
        inferenceContext.depth--;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#computeId()
	 */
	public void computeId() {
		this.id = TypeIds.NoId;
	}

	public char[] computeUniqueKey(boolean isLeaf) {
	    StringBuffer sig = new StringBuffer(10);
	    ReferenceBinding enclosing;
		if (isMemberType() && ((enclosing = enclosingType()).isParameterizedType() || enclosing.isRawType())) {
		    char[] typeSig = enclosing.computeUniqueKey(false/*not a leaf*/);
		    sig.append(typeSig, 0, typeSig.length-1); // copy all but trailing semicolon
		    sig.append('.').append(sourceName());
		} else if(this.type.isLocalType()){
			LocalTypeBinding localTypeBinding = (LocalTypeBinding) this.type;
			enclosing = localTypeBinding.enclosingType();
			ReferenceBinding temp;
			while ((temp = enclosing.enclosingType()) != null)
				enclosing = temp;
			char[] typeSig = enclosing.computeUniqueKey(false/*not a leaf*/);
		    sig.append(typeSig, 0, typeSig.length-1); // copy all but trailing semicolon
			sig.append('$');
			sig.append(localTypeBinding.sourceStart);
		} else {
		    char[] typeSig = this.type.computeUniqueKey(false/*not a leaf*/);
		    sig.append(typeSig, 0, typeSig.length-1); // copy all but trailing semicolon
		}
		ReferenceBinding captureSourceType = null;
		if (this.arguments != null) {
		    sig.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		    	TypeBinding typeBinding = this.arguments[i];
		        sig.append(typeBinding.computeUniqueKey(false/*not a leaf*/));
		        if (typeBinding instanceof CaptureBinding)
		        	captureSourceType = ((CaptureBinding) typeBinding).sourceType;
		    }
		    sig.append('>');
		}
		sig.append(';');
		if (captureSourceType != null && captureSourceType != this.type) {
			// contains a capture binding
			sig.insert(0, "&"); //$NON-NLS-1$
			sig.insert(0, captureSourceType.computeUniqueKey(false/*not a leaf*/));
		}

		int sigLength = sig.length();
		char[] uniqueKey = new char[sigLength];
		sig.getChars(0, sigLength, uniqueKey, 0);
		return uniqueKey;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#constantPoolName()
	 */
	public char[] constantPoolName() {
		return this.type.constantPoolName(); // erasure
	}

	public ParameterizedMethodBinding createParameterizedMethod(MethodBinding originalMethod) {
		return new ParameterizedMethodBinding(this, originalMethod);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	public String debugName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
	    if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
	    	// restore applied null annotation from tagBits:
		    if ((this.tagBits & TagBits.AnnotationNonNull) != 0) {
		    	char[][] nonNullAnnotationName = environment().getNonNullAnnotationName();
				nameBuffer.append('@').append(nonNullAnnotationName[nonNullAnnotationName.length-1]).append(' ');
		    } else if ((this.tagBits & TagBits.AnnotationNullable) != 0) {
		    	char[][] nullableAnnotationName = environment().getNullableAnnotationName();
				nameBuffer.append('@').append(nullableAnnotationName[nullableAnnotationName.length-1]).append(' ');
		    }
	    }
	    if (this.type instanceof UnresolvedReferenceBinding) {
	    	nameBuffer.append(this.type);
	    } else {
			nameBuffer.append(this.type.sourceName());
	    }
		if (this.arguments != null && this.arguments.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].debugName());
		    }
		    nameBuffer.append('>');
		}
	    return nameBuffer.toString();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#enclosingType()
	 */
	public ReferenceBinding enclosingType() {
	    return this.enclosingType;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#environment()
	 */
	public LookupEnvironment environment() {
		return this.environment;
	}

	/**
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
     */
    public TypeBinding erasure() {
        return this.type.erasure(); // erasure
    }
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fieldCount()
	 */
	public int fieldCount() {
		return this.type.fieldCount(); // same as erasure (lazy)
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fields()
	 */
	public FieldBinding[] fields() {
		if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
			return this.fields;

		try {
			FieldBinding[] originalFields = this.type.fields();
			int length = originalFields.length;
			FieldBinding[] parameterizedFields = new FieldBinding[length];
			for (int i = 0; i < length; i++)
				// substitute all fields, so as to get updated declaring class at least
				parameterizedFields[i] = new ParameterizedFieldBinding(this, originalFields[i]);
			this.fields = parameterizedFields;
		} finally {
			// if the original fields cannot be retrieved (ex. AbortCompilation), then assume we do not have any fields
			if (this.fields == null)
				this.fields = Binding.NO_FIELDS;
			this.tagBits |= TagBits.AreFieldsComplete;
		}
		return this.fields;
	}

	/**
	 * Return the original generic type from which the parameterized type got instantiated from.
	 * This will perform lazy resolution automatically if needed.
	 * @see ParameterizedTypeBinding#actualType() if no resolution is required (unlikely)
	 */
	public ReferenceBinding genericType() {
		if (this.type instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) this.type).resolve(this.environment, false);
		return this.type;
	}

	/**
	 * Ltype<param1 ... paramN>;
	 * LY<TT;>;
	 */
	public char[] genericTypeSignature() {
		if (this.genericTypeSignature == null) {
			if ((this.modifiers & ExtraCompilerModifiers.AccGenericSignature) == 0) {
		    	this.genericTypeSignature = this.type.signature();
			} else {
			    StringBuffer sig = new StringBuffer(10);
			    if (isMemberType()) {
			    	ReferenceBinding enclosing = enclosingType();
					char[] typeSig = enclosing.genericTypeSignature();
					sig.append(typeSig, 0, typeSig.length-1);// copy all but trailing semicolon
			    	if ((enclosing.modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
			    		sig.append('.');
			    	} else {
			    		sig.append('$');
			    	}
			    	sig.append(sourceName());
			    } else {
			    	char[] typeSig = this.type.signature();
//{ObjectTeams: for nested generic <R<@t..>> (type variable wrapped in a dependent type binding)
			    	if (this.type.isTypeVariable())
			    		typeSig = this.type.genericTypeSignature();
// SH}
					sig.append(typeSig, 0, typeSig.length-1);// copy all but trailing semicolon
		    	}
				if (this.arguments != null) {
				    sig.append('<');
				    for (int i = 0, length = this.arguments.length; i < length; i++) {
				        sig.append(this.arguments[i].genericTypeSignature());
				    }
				    sig.append('>');
				}
				sig.append(';');
				int sigLength = sig.length();
				this.genericTypeSignature = new char[sigLength];
				sig.getChars(0, sigLength, this.genericTypeSignature, 0);
			}
		}
		return this.genericTypeSignature;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getAnnotationTagBits()
	 */
	public long getAnnotationTagBits() {
		return this.type.getAnnotationTagBits();
	}

	public int getEnclosingInstancesSlotSize() {
		return genericType().getEnclosingInstancesSlotSize();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactConstructor(TypeBinding[])
	 */
	public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
		int argCount = argumentTypes.length;
		MethodBinding match = null;

		if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
			long range;
			if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
				nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
					MethodBinding method = this.methods[imethod];
					if (method.parameters.length == argCount) {
						TypeBinding[] toMatch = method.parameters;
						for (int iarg = 0; iarg < argCount; iarg++)
							if (toMatch[iarg] != argumentTypes[iarg])
								continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
					}
				}
			}
		} else {
			MethodBinding[] matchingMethods = getMethods(TypeConstants.INIT); // takes care of duplicates & default abstract methods
			nextMethod : for (int m = matchingMethods.length; --m >= 0;) {
				MethodBinding method = matchingMethods[m];
				TypeBinding[] toMatch = method.parameters;
				if (toMatch.length == argCount) {
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
				}
			}
		}
		return match;
	}
	
	 /**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactMethod(char[], TypeBinding[],CompilationUnitScope)
	 */
	public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
		// sender from refScope calls recordTypeReference(this)
		int argCount = argumentTypes.length;
		boolean foundNothing = true;
		MethodBinding match = null;

		if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
			long range;
			if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
				nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
					MethodBinding method = this.methods[imethod];
					foundNothing = false; // inner type lookups must know that a method with this name exists
					if (method.parameters.length == argCount) {
						TypeBinding[] toMatch = method.parameters;
						for (int iarg = 0; iarg < argCount; iarg++)
							if (toMatch[iarg] != argumentTypes[iarg])
								continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
					}
				}
			}
		} else {
			MethodBinding[] matchingMethods = getMethods(selector); // takes care of duplicates & default abstract methods
			foundNothing = matchingMethods == Binding.NO_METHODS;
			nextMethod : for (int m = matchingMethods.length; --m >= 0;) {
				MethodBinding method = matchingMethods[m];
				TypeBinding[] toMatch = method.parameters;
				if (toMatch.length == argCount) {
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
				}
			}
		}
		if (match != null) {
			// cannot be picked up as an exact match if its a possible anonymous case, such as:
			// class A<T extends Number> { public void id(T t) {} }
			// class B<TT> extends A<Integer> { public <ZZ> void id(Integer i) {} }
			if (match.hasSubstitutedParameters()) return null;
			return match;
		}

		if (foundNothing && (this.arguments == null || this.arguments.length <= 1)) {
			if (isInterface()) {
				 if (superInterfaces().length == 1) {
					if (refScope != null)
						refScope.recordTypeReference(this.superInterfaces[0]);
					return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
				 }
			} else if (superclass() != null) {
				if (refScope != null)
					refScope.recordTypeReference(this.superclass);
				return this.superclass.getExactMethod(selector, argumentTypes, refScope);
			}
		}
		return null;
	}

	 /**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getField(char[], boolean)
	 */
	public FieldBinding getField(char[] fieldName, boolean needResolve) {
		fields(); // ensure fields have been initialized... must create all at once unlike methods
		return ReferenceBinding.binarySearch(fieldName, this.fields);
	}
	 
 	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMemberType(char[])
	 */
	public ReferenceBinding getMemberType(char[] typeName) {
		memberTypes(); // ensure memberTypes have been initialized... must create all at once unlike methods
		int typeLength = typeName.length;
		for (int i = this.memberTypes.length; --i >= 0;) {
			ReferenceBinding memberType = this.memberTypes[i];
			if (memberType.sourceName.length == typeLength && CharOperation.equals(memberType.sourceName, typeName))
				return memberType;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMethods(char[])
	 */
	public MethodBinding[] getMethods(char[] selector) {
		if (this.methods != null) {
			long range;
			if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
				int start = (int) range;
				int length = (int) (range >> 32) - start + 1;
				// cannot optimize since some clients rely on clone array
				// if (start == 0 && length == this.methods.length)
				//	return this.methods; // current set is already interesting subset
				MethodBinding[] result;
				System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
				return result;
			}
		}
		if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
			return Binding.NO_METHODS; // have created all the methods and there are no matches

		MethodBinding[] parameterizedMethods = null;
		try {
		    MethodBinding[] originalMethods = this.type.getMethods(selector);
		    int length = originalMethods.length;
		    if (length == 0) return Binding.NO_METHODS;

		    parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = createParameterizedMethod(originalMethods[i]);
		    if (this.methods == null) {
				MethodBinding[] temp = new MethodBinding[length];
				System.arraycopy(parameterizedMethods, 0, temp, 0, length);
				this.methods = temp; // must be a copy of parameterizedMethods since it will be returned below
		    } else {
				int total = length + this.methods.length;
				MethodBinding[] temp = new MethodBinding[total];
				System.arraycopy(parameterizedMethods, 0, temp, 0, length);
				System.arraycopy(this.methods, 0, temp, length, this.methods.length);
				if (total > 1)
					ReferenceBinding.sortMethods(temp, 0, total); // resort to ensure order is good
				this.methods = temp;
			}
		    return parameterizedMethods;
		} finally {
			// if the original methods cannot be retrieved (ex. AbortCompilation), then assume we do not have any methods
		    if (parameterizedMethods == null)
		        this.methods = parameterizedMethods = Binding.NO_METHODS;
		}
	}

	public int getOuterLocalVariablesSlotSize() {
		return genericType().getOuterLocalVariablesSlotSize();
	}

	public boolean hasMemberTypes() {
	    return this.type.hasMemberTypes();
	}

	public boolean hasTypeBit(int bit) {
		TypeBinding erasure = erasure();
		if (erasure instanceof ReferenceBinding)
			return ((ReferenceBinding) erasure).hasTypeBit(bit);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#implementsMethod(MethodBinding)
	 */
	public boolean implementsMethod(MethodBinding method) {
		return this.type.implementsMethod(method); // erasure
	}

	void initialize(ReferenceBinding someType, TypeBinding[] someArguments) {
		this.type = someType;
		this.sourceName = someType.sourceName;
		this.compoundName = someType.compoundName;
		this.fPackage = someType.fPackage;
		this.fileName = someType.fileName;
		// should not be set yet
		// this.superclass = null;
		// this.superInterfaces = null;
		// this.fields = null;
		// this.methods = null;
		this.modifiers = someType.modifiers & ~ExtraCompilerModifiers.AccGenericSignature; // discard generic signature, will compute later
		// only set AccGenericSignature if parameterized or have enclosing type required signature
		if (someArguments != null) {
			this.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
		} else if (this.enclosingType != null) {
			this.modifiers |= (this.enclosingType.modifiers & ExtraCompilerModifiers.AccGenericSignature);
			this.tagBits |= this.enclosingType.tagBits & (TagBits.HasTypeVariable | TagBits.HasMissingType);
		}
		if (someArguments != null) {
			this.arguments = someArguments;
			for (int i = 0, length = someArguments.length; i < length; i++) {
				TypeBinding someArgument = someArguments[i];
				switch (someArgument.kind()) {
					case Binding.WILDCARD_TYPE :
						this.tagBits |= TagBits.HasDirectWildcard;
						if (((WildcardBinding) someArgument).boundKind != Wildcard.UNBOUND) {
							this.tagBits |= TagBits.IsBoundParameterizedType;
						}
						break;
					case Binding.INTERSECTION_TYPE :
						this.tagBits |= TagBits.HasDirectWildcard | TagBits.IsBoundParameterizedType; // Surely NOT X<?,?>, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=366131
						break;
					default :
						this.tagBits |= TagBits.IsBoundParameterizedType;
						break;
				}
				this.tagBits |= someArgument.tagBits & (TagBits.HasTypeVariable | TagBits.HasMissingType | TagBits.ContainsNestedTypeReferences);
			}
		}
//{ObjectTeams: share models
		this.model = someType.model;
		this._teamModel = someType._teamModel;
		this.roleModel = someType.roleModel;
// SH}
		this.tagBits |= someType.tagBits & (TagBits.IsLocalType| TagBits.IsMemberType | TagBits.IsNestedType | TagBits.HasMissingType | TagBits.ContainsNestedTypeReferences | TagBits.AnnotationNullMASK);
		this.tagBits &= ~(TagBits.AreFieldsComplete|TagBits.AreMethodsComplete);
	}

	protected void initializeArguments() {
	    // do nothing for true parameterized types (only for raw types)
	}

//{ObjectTeams: accessible across AbstractOTReferenceBinding (sitting in a different package):
	protected
// SH}
	void initializeForStaticImports() {
		this.type.initializeForStaticImports();
	}

//{ObjectTeams: for playedBy generic:
	@Override
	public boolean isBoundBase() {
		return super.isBoundBase() || (this.type != null && this.type.isBoundBase());
	}
	@Override
	public void setIsBoundBase(ReferenceBinding roleType) {
		super.setIsBoundBase(roleType);
		this.type.setIsBoundBase(roleType);
	}
	/** mimicked after superclass(): */
	@Override
	public ReferenceBinding baseclass() {
	    if (this.baseclass == null) {
	        this.baseclass = this.type.baseclass();
	        if (this.baseclass == null) return null;
	        if (this.arguments != null && this.arguments.length >= this.baseclass.typeVariables().length)
	        	this.baseclass = (ReferenceBinding) Scope.substitute(this, this.baseclass);

	    }
		return this.baseclass;
	}
	@Override
	public ReferenceBinding rawBaseclass() {
	    ReferenceBinding rawBaseclass = this.type.rawBaseclass();
	    if (rawBaseclass != null) {
	    	this.baseclass = rawBaseclass; // we already have it -> cache it.
	    	return (ReferenceBinding) Scope.substitute(this, rawBaseclass);
	    }
	    return null;
	}
	// for mixed type and value parameters:
	@Override
	public DependentTypeBinding asPlainDependentType() {
		if (DependentTypeBinding.isPlainDependentType(this.type))
			return (DependentTypeBinding) this.type;
		return null;
	}
// SH}

	public boolean isEquivalentTo(TypeBinding otherType) {
		if (this == otherType)
		    return true;
	    if (otherType == null)
	        return false;
	    switch(otherType.kind()) {

	    	case Binding.WILDCARD_TYPE :
			case Binding.INTERSECTION_TYPE:
	        	return ((WildcardBinding) otherType).boundCheck(this);

	    	case Binding.PARAMETERIZED_TYPE :
	            ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
	            if (this.type != otherParamType.type)
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
	            if (this.arguments == null) {
	            	return otherParamType.arguments == null;
	            }
	            int length = this.arguments.length;
	            TypeBinding[] otherArguments = otherParamType.arguments;
	            if (otherArguments == null || otherArguments.length != length) return false;
	            for (int i = 0; i < length; i++) {
	            	if (!this.arguments[i].isTypeArgumentContainedBy(otherArguments[i]))
	            		return false;
	            }
	            return true;

	    	case Binding.RAW_TYPE :
	            return erasure() == otherType.erasure();
	    }
	    /* With the hybrid 1.4/1.5+ projects modes, while establishing type equivalence, we need to
	       be prepared for a type such as Map appearing in one of three forms: As (a) a ParameterizedTypeBinding 
	       e.g Map<String, String>, (b) as RawTypeBinding Map#RAW and finally (c) as a BinaryTypeBinding 
	       When the usage of a type lacks type parameters, whether we land up with the raw form or not depends
	       on whether the underlying type was "seen to be" a generic type in the particular build environment or
	       not. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=328827 
	     */
	    if (erasure() == otherType) {
	    	return true;
	    }
	    return false;
	}

	public boolean isHierarchyConnected() {
		return this.superclass != null && this.superInterfaces != null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#isRawSubstitution()
	 */
	public boolean isRawSubstitution() {
		return isRawType();
	}

	public int kind() {
		return PARAMETERIZED_TYPE;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#memberTypes()
	 */
	public ReferenceBinding[] memberTypes() {
		if (this.memberTypes == null) {
			try {
				ReferenceBinding[] originalMemberTypes = this.type.memberTypes();
				int length = originalMemberTypes.length;
				ReferenceBinding[] parameterizedMemberTypes = new ReferenceBinding[length];
				// boolean isRaw = this.isRawType();
				for (int i = 0; i < length; i++)
					// substitute all member types, so as to get updated enclosing types
					parameterizedMemberTypes[i] = /*isRaw && originalMemberTypes[i].isGenericType()
						? this.environment.createRawType(originalMemberTypes[i], this)
						: */ this.environment.createParameterizedType(originalMemberTypes[i], null, this);
				this.memberTypes = parameterizedMemberTypes;
			} finally {
				// if the original fields cannot be retrieved (ex. AbortCompilation), then assume we do not have any fields
				if (this.memberTypes == null)
					this.memberTypes = Binding.NO_MEMBER_TYPES;
			}
		}
		return this.memberTypes;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#methods()
	 */
	public MethodBinding[] methods() {
//{ObjectTeams: need to check whether 'type' got any methods added since last call:
/* orig:
		if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
			return this.methods;
  :giro */
		if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
			MethodBinding[] originalMethods = this.type.methods();
			if (originalMethods.length == this.methods.length)
				return this.methods;
			MethodBinding[] newMethods = new MethodBinding[originalMethods.length];
			for (int i=0, j=0; i < originalMethods.length; i++) {
				if (j < this.methods.length && this.methods[j].original() == originalMethods[i])
					newMethods[i] = this.methods[j++];
				else
					newMethods[i] = createParameterizedMethod(originalMethods[i]);				
			}
			return this.methods = newMethods;
		}
// SH}

		try {
		    MethodBinding[] originalMethods = this.type.methods();
		    int length = originalMethods.length;
		    MethodBinding[] parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute all methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = createParameterizedMethod(originalMethods[i]);
		    this.methods = parameterizedMethods;
		} finally {
			// if the original methods cannot be retrieved (ex. AbortCompilation), then assume we do not have any methods
		    if (this.methods == null)
		        this.methods = Binding.NO_METHODS;

			this.tagBits |=  TagBits.AreMethodsComplete;
		}
		return this.methods;
	}
//{ObjectTeams: adding methods to generic roles (see A.1.14-otjld-generic-role-1)
	public void addMethod(MethodBinding methodBinding)
	{
		this.type.addMethod(methodBinding);

		if ((this.tagBits & TagBits.AreMethodsComplete) == 0)
			return;

		methodBinding = createParameterizedMethod(methodBinding);

		this.methods= ReferenceBinding.sortedInsert(this.methods, methodBinding);
	}
// SH}

	/**
	 * Define to be able to get the computeId() for the inner type binding.
	 *
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#problemId()
	 */
	public int problemId() {
		return this.type.problemId();
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedPackageName()
	 */
	public char[] qualifiedPackageName() {
		return this.type.qualifiedPackageName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedSourceName()
	 */
	public char[] qualifiedSourceName() {
		return this.type.qualifiedSourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		if (isMemberType()) {
			nameBuffer.append(CharOperation.concat(enclosingType().readableName(), this.sourceName, '.'));
		} else {
			nameBuffer.append(CharOperation.concatWith(this.type.compoundName, '.'));
		}
//{ObjectTeams
/* orig:
		if (this.arguments != null && this.arguments.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].readableName());
		    }
		    nameBuffer.append('>');
		}
  :giro */
		nameBuffer = appendValueAndTypeParameters(nameBuffer, false/*short*/);
// SH}
		int nameLength = nameBuffer.length();
		char[] readableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, readableName, 0);
	    return readableName;
	}

//{ObjectTeams: pretty printing:
	// extracted from readableName/shortReadableName:
	private StringBuffer appendValueAndTypeParameters(StringBuffer nameBuffer, boolean makeShort) {
		StringBuffer buf2 = new StringBuffer();
		buf2.append('<');
		boolean haveValueParameters = appendReadableValueParameterNames(buf2);
		if (haveValueParameters)
			nameBuffer = new StringBuffer().append(this.type.sourceName()); // reset to single name
		TypeBinding[] args = this.arguments;
		// avoid printing incomplete parameters: if there are value parameters also print declared type params:
		if (haveValueParameters && args == null && this.type.isGenericType())
			args = this.type.typeVariables();

		if (args != null && args.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
		    for (int i = 0, length = args.length; i < length; i++) {
		        if (haveValueParameters || i > 0) buf2.append(',');
		        if (makeShort)
		        	buf2.append(args[i].shortReadableName());
		        else
		        	buf2.append(args[i].readableName());
			}
		}
		buf2.append('>');
		if (buf2.length() > 2)
			nameBuffer.append(buf2);
		return nameBuffer;
	}
	// hook for dependent type printing:
	public boolean appendReadableValueParameterNames(StringBuffer buf) {
		if (DependentTypeBinding.isDependentType(this.type))
			return ((DependentTypeBinding)this.type).appendReadableValueParameterNames(buf);
		return false;
	}
// SH}
	
	ReferenceBinding resolve() {
		if ((this.tagBits & TagBits.HasUnresolvedTypeVariables) == 0)
			return this;

		this.tagBits &= ~TagBits.HasUnresolvedTypeVariables; // can be recursive so only want to call once
		ReferenceBinding resolvedType = (ReferenceBinding) BinaryTypeBinding.resolveType(this.type, this.environment, false /* no raw conversion */); // still part of parameterized type ref
		this.tagBits |= resolvedType.tagBits & TagBits.ContainsNestedTypeReferences;
		if (this.arguments != null) {
			int argLength = this.arguments.length;
			for (int i = 0; i < argLength; i++) {
				TypeBinding resolveType = BinaryTypeBinding.resolveType(this.arguments[i], this.environment, true /* raw conversion */);
				this.arguments[i] = resolveType;
				this.tagBits |= resolvedType.tagBits & TagBits.ContainsNestedTypeReferences;
			}
			/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=186565, Removed generic check
			   and arity check since we are dealing with binary types here and the fact that
			   the compiler produced class files for these types at all is proof positive that
			   the generic check and the arity check passed in the build environment that produced
			   these class files. Otherwise we don't handle mixed 1.5 and 1.4 projects correctly.
			   Just as with bounds check below, incremental build will propagate the change and
			   detect problems in source.
			 */
			
//			// arity check
//			TypeVariableBinding[] refTypeVariables = resolvedType.typeVariables();
//			if (refTypeVariables == Binding.NO_TYPE_VARIABLES) { // check generic
//				// Below 1.5, we should have already complained about the use of type parameters.
//				boolean isCompliant15 = this.environment.globalOptions.originalSourceLevel >= ClassFileConstants.JDK1_5;
//				if (isCompliant15 && (resolvedType.tagBits & TagBits.HasMissingType) == 0) {
//					this.environment.problemReporter.nonGenericTypeCannotBeParameterized(0, null, resolvedType, this.arguments);
//				}
//				return this;
//			} else if (argLength != refTypeVariables.length) { // check arity
//				this.environment.problemReporter.incorrectArityForParameterizedType(null, resolvedType, this.arguments);
//				return this; // cannot reach here as AbortCompilation is thrown
//			}
			// check argument type compatibility... REMOVED for now since incremental build will propagate change & detect in source
//			for (int i = 0; i < argLength; i++) {
//			    TypeBinding resolvedArgument = this.arguments[i];
//				if (refTypeVariables[i].boundCheck(this, resolvedArgument) != TypeConstants.OK) {
//					this.environment.problemReporter.typeMismatchError(resolvedArgument, refTypeVariables[i], resolvedType, null);
//			    }
//			}
		}
		return this;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		if (isMemberType()) {
			nameBuffer.append(CharOperation.concat(enclosingType().shortReadableName(), this.sourceName, '.'));
		} else {
			nameBuffer.append(this.type.sourceName);
		}
//{ObjectTeams:
/* orig:
		if (this.arguments != null && this.arguments.length > 0) { // empty arguments array happens when PTB has been created just to capture type annotations
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].shortReadableName());
		    }
		    nameBuffer.append('>');
		}
  :giro */
		nameBuffer = appendValueAndTypeParameters(nameBuffer, true/*short*/);
// SH}
		int nameLength = nameBuffer.length();
		char[] shortReadableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, shortReadableName, 0);
	    return shortReadableName;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
	 */
	public char[] signature() {
	    if (this.signature == null) {
	        this.signature = this.type.signature();  // erasure
	    }
		return this.signature;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#sourceName()
	 */
	public char[] sourceName() {
		return this.type.sourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#substitute(org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding)
	 */
	public TypeBinding substitute(TypeVariableBinding originalVariable) {

		ParameterizedTypeBinding currentType = this;
		while (true) {
			TypeVariableBinding[] typeVariables = currentType.type.typeVariables();
			int length = typeVariables.length;
			// check this variable can be substituted given parameterized type
			if (originalVariable.rank < length && typeVariables[originalVariable.rank] == originalVariable) {
			    // lazy init, since cannot do so during binding creation if during supertype connection
			    if (currentType.arguments == null)
					currentType.initializeArguments(); // only for raw types
			    if (currentType.arguments != null) {
			    	 if (currentType.arguments.length == 0) { // diamond type
					    	return originalVariable;
					    }
			    	 return currentType.arguments[originalVariable.rank];
			    }	
			}
			// recurse on enclosing type, as it may hold more substitutions to perform
			if (currentType.isStatic()) break;
			ReferenceBinding enclosing = currentType.enclosingType();
			if (!(enclosing instanceof ParameterizedTypeBinding))
				break;
			currentType = (ParameterizedTypeBinding) enclosing;
		}
		return originalVariable;
	}

//{ObjectTeams: implement new method from Substitution
	public ITeamAnchor substituteAnchor(ITeamAnchor anchor, int rank) {
		return null;
	}
//SH}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superclass()
	 */
	public ReferenceBinding superclass() {
	    if (this.superclass == null) {
	        // note: Object cannot be generic
	        ReferenceBinding genericSuperclass = this.type.superclass();
	        if (genericSuperclass == null) return null; // e.g. interfaces
		    this.superclass = (ReferenceBinding) Scope.substitute(this, genericSuperclass);
	    }
		return this.superclass;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
	 */
	public ReferenceBinding[] superInterfaces() {
	    if (this.superInterfaces == null) {
	    		if (this.type.isHierarchyBeingConnected())
	    			return Binding.NO_SUPERINTERFACES; // prevent superinterfaces from being assigned before they are connected
	    		this.superInterfaces = Scope.substitute(this, this.type.superInterfaces());
	    }
		return this.superInterfaces;
	}

	public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
		boolean update = false;
		if (this.type == unresolvedType) {
			this.type = resolvedType; // cannot be raw since being parameterized below
			update = true;
			ReferenceBinding enclosing = resolvedType.enclosingType();
			if (enclosing != null) {
				this.enclosingType = (ReferenceBinding) env.convertUnresolvedBinaryToRawType(enclosing); // needed when binding unresolved member type
			}
		}
		if (this.arguments != null) {
			for (int i = 0, l = this.arguments.length; i < l; i++) {
				if (this.arguments[i] == unresolvedType) {
					this.arguments[i] = env.convertUnresolvedBinaryToRawType(resolvedType);
					update = true;
				}
			}
		}
		if (update)
			initialize(this.type, this.arguments);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticEnclosingInstanceTypes()
	 */
	public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
		return genericType().syntheticEnclosingInstanceTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticOuterLocalVariables()
	 */
	public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
		return genericType().syntheticOuterLocalVariables();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    StringBuffer buffer = new StringBuffer(30);
	    if (this.type instanceof UnresolvedReferenceBinding) {
	    	buffer.append(debugName());
	    } else {
			if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
			if (isPublic()) buffer.append("public "); //$NON-NLS-1$
			if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
			if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
			if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
			if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
			if (isFinal()) buffer.append("final "); //$NON-NLS-1$

			if (isEnum()) buffer.append("enum "); //$NON-NLS-1$
			else if (isAnnotationType()) buffer.append("@interface "); //$NON-NLS-1$
			else if (isClass()) buffer.append("class "); //$NON-NLS-1$
			else buffer.append("interface "); //$NON-NLS-1$
			buffer.append(debugName());

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

	//		if (memberTypes != null) {
	//			if (memberTypes != NoMemberTypes) {
	//				buffer.append("\n/*   members   */");
	//				for (int i = 0, length = memberTypes.length; i < length; i++)
	//					buffer.append('\n').append((memberTypes[i] != null) ? memberTypes[i].toString() : "NULL TYPE");
	//			}
	//		} else {
	//			buffer.append("NULL MEMBER TYPES");
	//		}

			buffer.append("\n\n"); //$NON-NLS-1$
	    }
		return buffer.toString();

	}

	public TypeVariableBinding[] typeVariables() {
		if (this.arguments == null) {
			// retain original type variables if not substituted (member type of parameterized type)
			return this.type.typeVariables();
		}
		return Binding.NO_TYPE_VARIABLES;
	}
	
	public FieldBinding[] unResolvedFields() {
		return this.fields;
	}
	public MethodBinding getSingleAbstractMethod(final Scope scope) {
		MethodBinding theAbstractMethod = genericType().getSingleAbstractMethod(scope);
		if (theAbstractMethod == null || !theAbstractMethod.isValidBinding())
			return theAbstractMethod;
		
		TypeBinding [] typeArguments = this.arguments; // A1 ... An 
		TypeVariableBinding [] typeParameters = genericType().typeVariables(); // P1 ... Pn
		TypeBinding [] types = new TypeBinding[typeArguments.length];  // T1 ... Tn
		for (int i = 0, length = typeArguments.length; i < length; i++) {
			TypeBinding typeArgument = typeArguments[i];
			switch (typeArgument.kind()) {
				case Binding.WILDCARD_TYPE :
					WildcardBinding wildcard = (WildcardBinding) typeArgument;
					switch(wildcard.boundKind) {
	    				case Wildcard.EXTENDS :
	    				case Wildcard.SUPER :
	    					types[i] = wildcard.bound;
	    					break;
	    				case Wildcard.UNBOUND :
	    					// if Pi has upper bound Bi that mentions none of P1...Pn, then Ti = Bi; otherwise, Ti = Object
	    					final TypeBinding upperBound = typeParameters[i].firstBound;
							if (upperBound == null || typeParametersMentioned(upperBound)) {
	    						types[i] = scope.getJavaLangObject();
	    					} else {
	    						types[i] = upperBound;
	    					}
	    					break;
					}
					break;
				default :
					types[i] = typeArgument;
					break;
			}
			if (typeParameters[i].boundCheck(null, types[i]) != TypeConstants.OK)
				return this.singleAbstractMethod = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, ProblemReasons.NoSuchSingleAbstractMethod);
		}
		ParameterizedTypeBinding parameterizedType = scope.environment().createParameterizedType(genericType(), types, this.enclosingType);
		return this.singleAbstractMethod = new ParameterizedMethodBinding(parameterizedType, theAbstractMethod);
	}

	private boolean typeParametersMentioned(TypeBinding upperBound) {
		class MentionListener implements Substitution {
			private boolean typeParametersMentioned = false;
			public TypeBinding substitute(TypeVariableBinding typeVariable) {
				this.typeParametersMentioned = true;
				return typeVariable;
			}
			public boolean isRawSubstitution() {
				return false;
			}
			public LookupEnvironment environment() {
				return null;
			}
			public boolean typeParametersMentioned() {
				return this.typeParametersMentioned;
			}
//{ObjectTeams: new method:
			public ITeamAnchor substituteAnchor(ITeamAnchor anchor, int rank) {
				return anchor;
			}
// SH}
		}
		MentionListener mentionListener = new MentionListener();
		Scope.substitute(mentionListener, upperBound);
		return mentionListener.typeParametersMentioned();
	}
}
