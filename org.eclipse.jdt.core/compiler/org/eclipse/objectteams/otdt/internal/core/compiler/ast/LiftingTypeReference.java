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
 * $Id: LiftingTypeReference.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
/**
 * ObjectTeams Eclipse source extensions
 * More information available at www.ObjectTeams.org
 *
 * @author Markus Witte
 *
 * @date 04.07.2003
 */
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import java.util.HashSet;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * NEW for OTDT
 *
 * Represent types with declared lifting "MyBase as MyRole"
 *
 * @author macwitte
 * @version $Id: LiftingTypeReference.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class LiftingTypeReference extends TypeReference {

	public char[] roleToken;
	public char[][] baseTokens;
	public TypeReference baseReference, roleReference;

    public LocalDeclaration fakedArgument = null;
	public boolean hasIncompatibleArrayDimensions = false;

	public boolean isDeclaredLifting() {
	  return true;
	}

	public void setReferences(TypeReference baseReference, TypeReference roleReference) {
		this.baseReference=baseReference;
		this.roleReference=roleReference;
		this.baseReference.setBaseclassDecapsulation(DecapsulationState.ALLOWED);

		this.baseTokens = baseReference.getTypeName();
		this.roleToken = roleReference.getTypeName()[0];

		this.sourceStart=baseReference.sourceStart;
		this.sourceEnd=roleReference.sourceEnd;
	}

	public TypeReference copyDims(int dim){
		//return a type reference copy of me with some dimensions
		//warning : the new type ref has a null binding

		return new ArrayTypeReference(this.roleToken,dim,(((long)this.sourceStart)<<32)+this.sourceEnd) ;
	}

	// The binding is basically the baseReference's binding.
	public TypeBinding getTypeBinding(Scope scope) {
	    if (this.resolvedType != null)
	        return this.resolvedType;
    	throw new InternalCompilerError("Unexpected control flow, method not intended to do work."); //$NON-NLS-1$
	}

	/* in addition to resolving implement some checks here
	 */
	@Override
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
		TypeBinding baseType = this.resolvedType = this.baseReference.resolveType(scope);
		if (this.roleReference.getTypeName().length > 1) {
			scope.problemReporter().qualifiedLiftingType(this.roleReference, scope.enclosingSourceType());
			return invalidate(baseType);
		}
	    TypeBinding roleType = this.roleReference.resolveType(scope);
	    if (scope.kind != Scope.BLOCK_SCOPE) { // not a catch block?
			if (!TeamModel.isAnyTeam(scope.enclosingSourceType()))
			{
				scope.problemReporter().liftingTypeNotAllowedHere(scope.methodScope().referenceContext, this);
				return invalidate(roleType);
			}
		}
	    if (baseType == null || baseType instanceof MissingTypeBinding)
	        return invalidate(roleType);
	    if (roleType == null || roleType instanceof MissingTypeBinding)
	        return invalidate(baseType);
	    if (roleType.isArrayType()){
	        baseType = baseType.leafComponentType();
	        roleType = roleType.leafComponentType();
	    }
	    if (roleType.isBaseType()) {
	        scope.problemReporter().primitiveTypeNotAllowedForLifting(
	                scope.referenceType(), this.roleReference, roleType);
	        return invalidate(roleType);
	    }
	    if (baseType.isBaseType()) {
	        scope.problemReporter().primitiveTypeNotAllowedForLifting(
	                scope.referenceType(), this.baseReference, baseType);
	        return invalidate(roleType);
	    }

	    ReferenceBinding roleRefType = (ReferenceBinding)roleType;
	    if (!roleRefType.isValidBinding()) // already reported.
	        return invalidate(roleType);

	    if (!roleRefType.isDirectRole()) {
	        scope.problemReporter().needRoleInLiftingType(
	                scope.referenceType(), this.roleReference, roleType);
	        return invalidate(roleType);
	    }
	    if (roleRefType.isSynthInterface())
	    	roleRefType = roleRefType.getRealClass();

	    if (roleRefType.roleModel.hasBaseclassProblem()) {// already reported for the role class.
	    	scope.referenceContext().tagAsHavingErrors(); // -> mark method as erroneous, too.
	        return invalidate(roleType);
	    }

	    // TODO (SH): maybe look for bound sub-type?
	    // Note (SH): calling baseclass() requires STATE_LENV_DONE_FIELDS_AND_METHODS:
	    Dependencies.ensureBindingState(roleRefType, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);

	    if (baseType.isTypeVariable() && ((TypeVariableBinding)baseType).roletype != null) {
	    	// resolving "<B base R> as R":
	    	roleRefType = ((TypeVariableBinding)baseType).roletype;
	    	// check ambiguity:
	    	HashSet<ReferenceBinding> mappedBases = new HashSet<ReferenceBinding>();
	    	for(ReferenceBinding boundRole : roleRefType.roleModel.getBoundDescendants())
	    		if (mappedBases.contains(boundRole.baseclass()))
	    			scope.problemReporter().definiteLiftingAmbiguity(baseType, roleRefType, this);
	    		else
	    			mappedBases.add(boundRole.baseclass());
	    } else if ((baseType.tagBits & TagBits.HierarchyHasProblems) != 0) {
	    	// already reported (?)
	    } else {
	    	// static adjustment (OTJLD 2.3.2(a)):
	    	roleRefType = (ReferenceBinding)TeamModel.getRoleToLiftTo(scope, baseType, roleRefType, true, this);
		    if (roleRefType == null)
		    	roleRefType = (ReferenceBinding)roleType; // revert unsuccessful adjustment
		    if (    roleRefType.baseclass() == null
		    	|| (roleRefType.tagBits & TagBits.BaseclassHasProblems) != 0)
		    {
		        scope.problemReporter().roleNotBoundCantLift(
		                scope.referenceType(), this.roleReference, roleType);
		        return invalidate(roleType);
		    }
	    	if (baseType.isRole()) // see http://trac.objectteams.org/ot/ticket/73
	    		baseType= RoleTypeCreator.maybeWrapUnqualifiedRoleType(baseType, scope.enclosingReceiverType());
	    	if (baseType == null)
	    		return invalidate(roleType);
	    	Config oldConfig = Config.createOrResetConfig(this);
	    	try {
	    		// fetch role's base class and perform substitutions:
	    		ReferenceBinding roleBase = roleRefType.baseclass();
	    		if (roleType.isParameterizedType()) {
	    			ParameterizedTypeBinding parameterizedRole = (ParameterizedTypeBinding)roleType;
	    			TypeBinding[] typeArgs = parameterizedRole.arguments;
	    			ITeamAnchor anchor = null;
	    			if (roleRefType.baseclass() instanceof RoleTypeBinding)
	    				anchor = ((RoleTypeBinding)roleRefType.baseclass())._teamAnchor;
	    			roleBase = parameterizedRole.environment.createParameterizedType((ReferenceBinding)roleBase.original(), typeArgs, anchor, -1, roleBase.enclosingType());
	    		}
	    		// THE compatibility check:
		    	if (   !baseType.isCompatibleWith(roleBase)
		    		|| Config.getLoweringRequired())
		    	{
			        scope.problemReporter().incompatibleBaseForRole(
			        		scope.referenceType(), this, roleType, baseType);
			        return invalidate(roleType);
		    	}
	    	} finally {
	    		Config.removeOrRestore(oldConfig, this);
	    	}
	    }
		return this.resolvedType;
	}

	@Override
	public TypeBinding resolveType(ClassScope scope) {
		throw new InternalCompilerError("LiftingTypeReference cannot be used in a ClassScope!"); //$NON-NLS-1$
	}

	// when resolve detects an error delete the lifting call in the
	// faked argument, to avoid subsequent errors when resolving
	// an invalid lifting call.
	private TypeBinding invalidate(TypeBinding variableType) {
	    if (this.fakedArgument != null) {
	        int start = this.roleReference.sourceStart;
	        int end   = this.roleReference.sourceEnd;

	        Expression nullValue = null;
	        if (variableType.isBaseType()) {
	            char[] tok = new char[]{'0'};
	            switch (variableType.id) {
	            case TypeIds.T_boolean: nullValue = new FalseLiteral (start, end); break;
	            case TypeIds.T_char   : nullValue = new CharLiteral  (tok,start, end); break;
	            case TypeIds.T_double : nullValue = new DoubleLiteral(tok,start, end); break;
	            case TypeIds.T_float  : nullValue = new FloatLiteral (tok,start, end); break;
	            case TypeIds.T_int    : nullValue = new IntLiteral   (tok,start, end); break;
	            case TypeIds.T_long   : nullValue = new LongLiteral  (tok,start, end); break;
	            }
	        } else {
	            nullValue = new NullLiteral(start, end);
	        }

	        this.fakedArgument.initialization = nullValue;

	        if (variableType.isValidBinding())
	        	this.fakedArgument.type = new AstGenerator(this).typeReference(variableType);

	    }
	    return null;
	}

	@Override
	public char [][] getTypeName() {
		return this.baseTokens;
	}

	@Override
	public char[] getLastToken() {
		return this.baseReference.getLastToken();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output = this.baseReference.printExpression(indent, output);
		output.append(" as "); //$NON-NLS-1$
		output = this.roleReference.printExpression(indent, output);
		return output;
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

}
// Markus Witte+SH}