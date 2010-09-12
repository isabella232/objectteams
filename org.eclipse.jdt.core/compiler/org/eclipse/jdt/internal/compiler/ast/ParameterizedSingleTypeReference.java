/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ParameterizedSingleTypeReference.java 23405 2010-02-03 17:02:18Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.OTClassScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ProblemAnchorBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * OTDT changes:
 * What: support baseclass decapsulation
 *
 * What: support value parameters
 *
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class ParameterizedSingleTypeReference extends ArrayTypeReference {

	public TypeReference[] typeArguments;

//{ObjectTeams: anchor(s):
	public TypeAnchorReference[] typeAnchors;
	// decapsulation:
	@Override
	public void setBaseclassDecapsulation(DecapsulationState state) {
		super.setBaseclassDecapsulation(state);
		if (this.typeArguments != null)
			for (TypeReference argument : this.typeArguments)
				if (argument != null)
					argument.setBaseclassDecapsulation(state);
	}
// SH}

	public ParameterizedSingleTypeReference(char[] name, TypeReference[] typeArguments, int dim, long pos){
		super(name, dim, pos);
		this.originalSourceEnd = this.sourceEnd;
		this.typeArguments = typeArguments;
	}
	public void checkBounds(Scope scope) {
		if (this.resolvedType == null) return;

		if (this.resolvedType.leafComponentType() instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) this.resolvedType.leafComponentType();
			ReferenceBinding currentType = parameterizedType.genericType();
			TypeVariableBinding[] typeVariables = currentType.typeVariables();
			TypeBinding[] argTypes = parameterizedType.arguments;
			if (argTypes != null && typeVariables != null) { // may be null in error cases
				parameterizedType.boundCheck(scope, this.typeArguments);
			}
		}
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		return new ParameterizedSingleTypeReference(this.token, this.typeArguments, dim, (((long)this.sourceStart)<<32)+this.sourceEnd);
	}

	/**
	 * @return char[][]
	 */
	public char [][] getParameterizedTypeName(){
//{ObjectTeams: were all parameters value parameters?
		if (this.typeArguments.length == 0) {
			return super.getParameterizedTypeName();
		}
// SH}
		StringBuffer buffer = new StringBuffer(5);
		buffer.append(this.token).append('<');
		for (int i = 0, length = this.typeArguments.length; i < length; i++) {
			if (i > 0) buffer.append(',');
			buffer.append(CharOperation.concatWith(this.typeArguments[i].getParameterizedTypeName(), '.'));
		}
		buffer.append('>');
		int nameLength = buffer.length();
		char[] name = new char[nameLength];
		buffer.getChars(0, nameLength, name, 0);
		int dim = this.dimensions;
		if (dim > 0) {
			char[] dimChars = new char[dim*2];
			for (int i = 0; i < dim; i++) {
				int index = i*2;
				dimChars[index] = '[';
				dimChars[index+1] = ']';
			}
			name = CharOperation.concat(name, dimChars);
		}
		return new char[][]{ name };
	}
	/**
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    protected TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }

    /*
     * No need to check for reference to raw type per construction
     */
	private TypeBinding internalResolveType(Scope scope, ReferenceBinding enclosingType, boolean checkBounds) {
//{ObjectTeams: separate scopes
		return internalResolveType(null, scope, enclosingType, checkBounds);
	}
	private TypeBinding internalResolveType(Scope importScope, Scope scope, ReferenceBinding enclosingType, boolean checkBounds) {
		// no double resolve due to expression replacement: FIXME(SH): obsolete?
		if (this.resolvedType != null)
			return this.resolvedType;
		// find a base import scope if that's allowed:
		if (importScope == null && getBaseclassDecapsulation().isAllowed()) {
			Scope currentScope = scope;
			while (currentScope != null) {
				if (currentScope instanceof OTClassScope) {
					importScope = ((OTClassScope)currentScope).getBaseImportScope();
					if (importScope != null)
						break;
				}
				currentScope = currentScope.parent;
			}
		}
// SH}
		// handle the error here
		this.constant = Constant.NotAConstant;
		if ((this.bits & ASTNode.DidResolve) != 0) { // is a shared type reference which was already resolved
			if (this.resolvedType != null) { // is a shared type reference which was already resolved
				if (this.resolvedType.isValidBinding()) {
					return this.resolvedType;
				} else {
					switch (this.resolvedType.problemId()) {
						case ProblemReasons.NotFound :
						case ProblemReasons.NotVisible :
						case ProblemReasons.InheritedNameHidesEnclosingName :
							TypeBinding type = this.resolvedType.closestMatch();
							return type;
						default :
							return null;
					}
				}
			}
		}
		boolean hasGenericError = false;
		ReferenceBinding currentType;
		this.bits |= ASTNode.DidResolve;
//{ObjectTeams: check team anchor first:
	    for (int typeParamPos=0; typeParamPos<this.typeArguments.length; typeParamPos++) {
		    if (this.typeArguments[typeParamPos] instanceof TypeAnchorReference)
		    {
		    	TypeAnchorReference typeAnchorReference = (TypeAnchorReference)this.typeArguments[typeParamPos];
				ITeamAnchor anchor = typeAnchorReference.resolveAnchor(scope);
				if (!ProblemAnchorBinding.checkAnchor(scope, typeAnchorReference, anchor, this.token))
					return null;
		    	for (ITeamAnchor seg : anchor.getBestNamePath())
		    		if (!seg.isFinal())
		    			scope.problemReporter().anchorPathNotFinal(typeAnchorReference, seg, this.token);
		    		// proceed to possibly report more errors
	
		    	// try member type (role) of anchor's type (team):
				this.resolvedType = anchor.resolveRoleType(this.token, this.dimensions); // FIXME(SH): although contained in a loop only one anchor can win
				if (this.resolvedType == null) {
					// not a role type try type with explicit value anchor:
					this.resolvedType = scope.getType(this.token); // resolve raw type (premature, see below).
					if (this.resolvedType.isTypeVariable()) {
						ITeamAnchor[] declaredAnchors = ((TypeVariableBinding)this.resolvedType).anchors;
						if (declaredAnchors != null && typeParamPos < declaredAnchors.length && declaredAnchors[typeParamPos].isValidBinding())
							if (!declaredAnchors[typeParamPos].hasSameBestNameAs(anchor))
								scope.problemReporter().rebindingTypeVariableAnchor(this, typeAnchorReference, declaredAnchors[typeParamPos]);
					}
					this.resolvedType = typeAnchorReference.createDependentTypeBinding(scope, this, typeParamPos);
					if (this.resolvedType == null) {
						this.resolvedType = new ProblemReferenceBinding(anchor, this.token, null, ProblemReasons.NotFound);
						return null;
					}
				}
				if (this.resolvedType != null && this.resolvedType.isValidBinding() && this.resolvedType instanceof ReferenceBinding) {
					if (!checkParameterizedRoleVisibility(scope, anchor, (ReferenceBinding) this.resolvedType))
						return null;
				}
	
				if (   shouldAnalyzeRoleReference()
					&& isIllegalQualifiedUseOfProtectedRole(scope))
					return null; // problem binding may be in this.resolvedType
				// consume first arg:
				int len = 0;
				if (this.typeAnchors != null) {
					len = this.typeAnchors.length;
					System.arraycopy(this.typeAnchors, 0,
								     this.typeAnchors = new TypeAnchorReference[len+1], 0, len);
				} else {
					this.typeAnchors = new TypeAnchorReference[1];
				}
				this.typeAnchors[len] = typeAnchorReference;
				len = this.typeArguments.length-1;
				System.arraycopy(this.typeArguments, 1,
								 this.typeArguments = new TypeReference[len], 0, len);
	
				// note: handling of arrays differs for role and regular types
				if (len == 0)
					return this.resolvedType; // we're done

				// proceed with a word of warning:
				scope.problemReporter().experimentalFeature(this, "Implementation for mixed type and value parameters is experimental.");
		    }
		}
// SH}
		if (enclosingType == null) {
//{ObjectTeams: try both scopes (base, regular):
		  if (importScope != null) 
			this.resolvedType = importScope.getType(this.token);
		  if (this.resolvedType == null || this.resolvedType.problemId() == ProblemReasons.NotFound)
// SH}
			this.resolvedType = scope.getType(this.token);
			if (this.resolvedType.isValidBinding()) {
				currentType = (ReferenceBinding) this.resolvedType;
			} else {
				hasGenericError = true;
				reportInvalidType(scope);
				switch (this.resolvedType.problemId()) {
					case ProblemReasons.NotFound :
					case ProblemReasons.NotVisible :
					case ProblemReasons.InheritedNameHidesEnclosingName :
						TypeBinding type = this.resolvedType.closestMatch();
						if (type instanceof ReferenceBinding) {
							currentType = (ReferenceBinding) type;
							break;
						}
						//$FALL-THROUGH$ - unable to complete type binding, but still resolve type arguments
					default :
						boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
					int argLength = this.typeArguments.length;
					for (int i = 0; i < argLength; i++) {
						TypeReference typeArgument = this.typeArguments[i];
						if (isClassScope) {
							typeArgument.resolveType((ClassScope) scope);
						} else {
							typeArgument.resolveType((BlockScope) scope, checkBounds);
						}
					}
					return null;
				}
				// be resilient, still attempt resolving arguments
			}
			enclosingType = currentType.enclosingType(); // if member type
			if (enclosingType != null) {
				enclosingType = currentType.isStatic()
					? (ReferenceBinding) scope.environment().convertToRawType(enclosingType, false /*do not force conversion of enclosing types*/)
					: scope.environment().convertToParameterizedType(enclosingType);
				currentType = scope.environment().createParameterizedType((ReferenceBinding) currentType.erasure(), null /* no arg */, enclosingType);
			}
		} else { // resolving member type (relatively to enclosingType)
			this.resolvedType = currentType = scope.getMemberType(this.token, enclosingType);
			if (!this.resolvedType.isValidBinding()) {
				hasGenericError = true;
				scope.problemReporter().invalidEnclosingType(this, currentType, enclosingType);
				return null;
			}
			if (isTypeUseDeprecated(currentType, scope))
				scope.problemReporter().deprecatedType(currentType, this);
			ReferenceBinding currentEnclosing = currentType.enclosingType();
			if (currentEnclosing != null && currentEnclosing.erasure() != enclosingType.erasure()) {
				enclosingType = currentEnclosing; // inherited member type, leave it associated with its enclosing rather than subtype
			}
		}

		// check generic and arity
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
	    TypeReference keep = null;
	    if (isClassScope) {
	    	keep = ((ClassScope) scope).superTypeReference;
	    	((ClassScope) scope).superTypeReference = null;
	    }
		int argLength = this.typeArguments.length;
		TypeBinding[] argTypes = new TypeBinding[argLength];
		boolean argHasError = false;
		ReferenceBinding currentOriginal = (ReferenceBinding)currentType.original();
		for (int i = 0; i < argLength; i++) {
		    TypeReference typeArgument = this.typeArguments[i];
//{ObjectTeams: limitation (value parameter other than 1st position).
		    if (typeArgument instanceof TypeAnchorReference) {
		    	scope.problemReporter().incompleteDependentTypesImplementation(typeArgument,
		    									"type value parameter must be in 1st position"); //$NON-NLS-1$
		    	argHasError = true;
		    	continue;
		    }
// SH}
		    TypeBinding argType = isClassScope
				? typeArgument.resolveTypeArgument((ClassScope) scope, currentOriginal, i)
				: typeArgument.resolveTypeArgument((BlockScope) scope, currentOriginal, i);
		     if (argType == null) {
		         argHasError = true;
		     } else {
//{ObjectTeams:
		    	argType = RoleTypeCreator.maybeWrapUnqualifiedRoleType(scope, argType, typeArgument);
// SH}
			    argTypes[i] = argType;
		     }
		}
		if (argHasError) {
			return null;
		}
		if (isClassScope) {
	    	((ClassScope) scope).superTypeReference = keep;
			if (((ClassScope) scope).detectHierarchyCycle(currentOriginal, this))
				return null;
		}
//{ObjectTeams: already done?
		if (argLength == 0)
			return this.resolvedType;
// SH}

		TypeVariableBinding[] typeVariables = currentOriginal.typeVariables();
		if (typeVariables == Binding.NO_TYPE_VARIABLES) { // non generic invoked with arguments
			boolean isCompliant15 = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
			if ((currentOriginal.tagBits & TagBits.HasMissingType) == 0) {
				if (isCompliant15) { // below 1.5, already reported as syntax error
					this.resolvedType = currentType;
					scope.problemReporter().nonGenericTypeCannotBeParameterized(0, this, currentType, argTypes);
					return null;
				}
			}
			// resilience do not rebuild a parameterized type unless compliance is allowing it
			if (!isCompliant15) {
				// array type ?
				TypeBinding type = currentType;
				if (this.dimensions > 0) {
					if (this.dimensions > 255)
						scope.problemReporter().tooManyDimensions(this);
					type = scope.createArrayType(type, this.dimensions);
				}
				if (hasGenericError)
					return type;
				return this.resolvedType = type;
			}
			// if missing generic type, and compliance >= 1.5, then will rebuild a parameterized binding
		} else if (argLength != typeVariables.length) { // check arity
			scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
			return null;
		} else if (!currentType.isStatic()) {
			ReferenceBinding actualEnclosing = currentType.enclosingType();
			if (actualEnclosing != null && actualEnclosing.isRawType()){
				scope.problemReporter().rawMemberTypeCannotBeParameterized(
						this, scope.environment().createRawType(currentOriginal, actualEnclosing), argTypes);
				return null;
			}
		}

    	ParameterizedTypeBinding parameterizedType = scope.environment().createParameterizedType(currentOriginal, argTypes, enclosingType);
		// check argument type compatibility
		if (checkBounds) // otherwise will do it in Scope.connectTypeVariables() or generic method resolution
			parameterizedType.boundCheck(scope, this.typeArguments);
		else
			scope.deferBoundCheck(this);
		if (isTypeUseDeprecated(parameterizedType, scope))
			reportDeprecatedType(parameterizedType, scope);

		TypeBinding type = parameterizedType;
		// array type ?
		if (this.dimensions > 0) {
			if (this.dimensions > 255)
				scope.problemReporter().tooManyDimensions(this);
			type = scope.createArrayType(type, this.dimensions);
		}
		if (hasGenericError) {
			return type;
		}
		return this.resolvedType = type;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append(this.token);
		output.append("<"); //$NON-NLS-1$
		int max = this.typeArguments.length - 1;
//{ObjectTeams: value parameters:
		if (this.typeAnchors != null) {
			int anchorLen = this.typeAnchors.length;
			for (int i = 0; i < anchorLen; i++) {
				this.typeAnchors[i].print(0, output);
				if (i+1 < anchorLen || max >= 0)
					output.append(", "); //$NON-NLS-1$
			}
		}
	  // after filtering out value parameters, an empty array may remain:
	  if (max >= 0) {
// orig:
		for (int i= 0; i < max; i++) {
			this.typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		this.typeArguments[max].print(0, output);
// :giro
	  }
// SH}
		output.append(">"); //$NON-NLS-1$
		if ((this.bits & IsVarArgs) != 0) {
			for (int i= 0 ; i < this.dimensions - 1; i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i= 0 ; i < this.dimensions; i++) {
				output.append("[]"); //$NON-NLS-1$
			}
		}
		return output;
	}

	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
	    return internalResolveType(scope, null, checkBounds);
	}

	public TypeBinding resolveType(ClassScope scope) {
	    return internalResolveType(scope, null, false /*no bounds check in classScope*/);
	}

	public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	    return internalResolveType(scope, enclosingType, true/*check bounds*/);
	}

//{ObjectTeams: resolve helpers
	// for base-imported types (only single is supported):
	@Override
	public TypeBinding checkResolveUsingBaseImportScope(Scope scope) {
		// same as in SingleTypeReference:
		if (   this.getBaseclassDecapsulation().isAllowed()
			|| scope.isBaseGuard())
		{
			TypeBinding problem = this.resolvedType;
			this.resolvedType = null; // force re-computation in getTypeBinding()
			Scope currentScope = scope;
			while (currentScope != null) {
				if (currentScope instanceof OTClassScope) {
					Scope baseImportScope = ((OTClassScope)currentScope).getBaseImportScope();
					if (baseImportScope != null) {
						this.resolvedType = getTypeBinding(baseImportScope);
						if (this.resolvedType != null && this.resolvedType.isValidBinding())
							return this.resolvedType = checkResolvedType(this.resolvedType, baseImportScope, false);
					}
				}
				currentScope = currentScope.parent;
			}
			this.resolvedType = problem;
		}
		return null;
	}
	// check for externalized role's visibility
	boolean checkParameterizedRoleVisibility(Scope scope, ITeamAnchor anchor, ReferenceBinding type) {
		if (!type.isPublic()) {
			Dependencies.ensureBindingState(type, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);
			if (TypeAnalyzer.isConfined(type)) {
				// confined has highest priority for reporting / doesn't allow decapsulation
				scope.problemReporter().decapsulatingConfined(this, (ReferenceBinding)this.resolvedType);
				setBaseclassDecapsulation(DecapsulationState.CONFINED);
				this.resolvedType = new ProblemReferenceBinding(anchor, this.token, type, ProblemReasons.NotVisible);
				return false;
			} else if (RoleTypeBinding.isRoleWithExplicitAnchor(type)) {
				// externalized non-public role
				switch (getBaseclassDecapsulation()) {
				case ALLOWED:
					scope.problemReporter().decapsulation(this, type);
					setBaseclassDecapsulation(DecapsulationState.REPORTED);
					//$FALL-THROUGH$
				case REPORTED:
					return true;
				case NONE:
					this.resolvedType = new ProblemReferenceBinding(anchor, this.token, type, ProblemReasons.NotVisible);
					scope.problemReporter().qualifiedProtectedRole(this, type);
					return false;
				}
			} else 	if (!type.canBeSeenBy(scope)) {
				// other invisible types
				this.resolvedType = new ProblemReferenceBinding(anchor, this.token, type, ProblemReasons.NotVisible);
				reportInvalidType(scope);
				return false;
			}
		}
		return true;
	}
// SH}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
//{ObjectTeams: API for OT-Model:
	/**
     * API for OT-Model:
     * If typeReference is an anchored type, return the qualified name of the type anchor.
     */
	public static char[][] getTypeAnchor(TypeReference typeReference) {
		if (typeReference instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference type= (ParameterizedSingleTypeReference)typeReference;
			if (type.resolvedType != null) {
				// after resolve use typeAnchors:
				TypeAnchorReference[] anchors= type.typeAnchors;
				if (anchors != null && anchors.length == 1)
					return beautifyTypeAnchor(anchors[0].getTypeName());
			} else {
				if (type.typeArguments != null && type.typeArguments.length > 0) {
					TypeReference first= type.typeArguments[0];
					if (first instanceof TypeAnchorReference) {
						char[][] anchorName= first.getTypeName();
						anchorName[0] = CharOperation.subarray(anchorName[0], 1, -1); // chop off '@'
						return beautifyTypeAnchor(anchorName);
					}
				}
			}
		}
		return null;
	}
	private static char[][] beautifyTypeAnchor(char[][] orig) {
		int len= orig.length;
		if (!CharOperation.equals(orig[len-1], IOTConstants._OT_BASE))
			return orig;
		char[][] result= new char[len][];
		System.arraycopy(orig, 0, result, 0, len-1);
		result[len-1]= IOTConstants.BASE;
		return result;
	}
// SH}
}
