/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InsertTypeAdjustmentsVisitor.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lowering;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * MIGRATION_STATE: Eclipse 3.2.
 *
 * This visitor descends a block scope and checks where type compatibility
 * for role types can only be achieved by explicit adjustment.
 * Supports:
 * + explicit cast from role interface part to class part
 * + lowering.
 *
 * Invoked during resolve.
 *
 * @author stephan
 * @version $Id: InsertTypeAdjustmentsVisitor.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class InsertTypeAdjustmentsVisitor extends ASTVisitor {

	/** while visiting a specific generated local variable, lowering is not allowed. */
    private boolean disallowLower;

	public void endVisit(Assignment assignment, BlockScope scope) {
        assignment.expression = maybeWrap(
                scope,
                assignment.expression,
                assignment.lhs.resolvedType);
    }

    public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
        endVisit((AbstractVariableDeclaration)fieldDeclaration, scope);
    }

    public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
        endVisit((AbstractVariableDeclaration)localDeclaration, scope);
    }

    private void endVisit(AbstractVariableDeclaration varDecl, BlockScope scope) {
    	boolean oldDisallowLower = this.disallowLower;
    	if (CharOperation.prefixEquals(CallinImplementor.OT_LOCAL.toCharArray(), varDecl.name))
    		// this is a parameter mapped from base to role, cannot *lower*
    		this.disallowLower = true;
    	try {
    		// Note(SH): varDecl.type == null has actually been observed.
    		//           FieldDeclaration.resolve comments that this may occur for
    		//           enum-constants, however, this visitor is only invoked if
    		//           Config.requireTypeAdjustment() is set, which I found no way to produce.
    		if (varDecl.type == null) {
    			new InternalCompilerError("varDecl.type is null for "+new String(varDecl.name)) //$NON-NLS-1$
    					.printStackTrace();
    			System.err.println(scope.referenceContext());
    			return;
    		}
    		// no lowering for cache-read in liftTo method:
    		// (could be attempted because Confined role is not conform to Object)
    		if (scope.isGeneratedScope()) {
    			if (varDecl.initialization instanceof MessageSend) {
    				if (CharOperation.equals(((MessageSend)varDecl.initialization).selector, IOTConstants.GET))
    					this.disallowLower = true;
    			}
    		}
    		varDecl.initialization = maybeWrap(
					    					scope,
					    					varDecl.initialization,
					    					varDecl.type.resolvedType);
    	} finally {
    		this.disallowLower = oldDisallowLower;
    	}
    }

    public void endVisit(MessageSend messageSend, BlockScope scope) {
        if (messageSend.arguments != null) {
        	boolean saveLower = this.disallowLower;
        	// no lowering for arg of _OT$addRole() (invoked in lifting constructor).
        	// (could be attempted because Confined role is not conform to Object)
        	if (CharOperation.equals(messageSend.selector, IOTConstants.ADD_ROLE))
        		this.disallowLower = true;
            TypeBinding[] params = messageSend.binding.parameters;
            for (int i=0; i<messageSend.arguments.length; i++) {
                messageSend.arguments[i] = maybeWrap(
                        scope,
                        messageSend.arguments[i],
                        params[i]);
            }
            this.disallowLower = saveLower;
        }
    }

    @Override
    public void endVisit(FieldReference fieldReference, BlockScope scope) {
    	// e.g., someArray.length has NO declaring class, so be careful
    	if (fieldReference.binding.declaringClass != null)
    		fieldReference.receiver = maybeWrap(
    					scope,
    					fieldReference.receiver,
    					fieldReference.binding.declaringClass);
    }

    public void endVisit(AllocationExpression alloc, BlockScope scope) {
        if (alloc.arguments != null) {
            TypeBinding[] params = alloc.binding.parameters;
            for (int i=0; i<alloc.arguments.length; i++) {
                alloc.arguments[i] = maybeWrap(
                        scope,
                        alloc.arguments[i],
                        params[i]);
            }
        }
    }

    public void endVisit(QualifiedAllocationExpression alloc, BlockScope scope) {
        if (alloc.arguments != null) {
            TypeBinding[] params = alloc.binding.parameters;
            for (int i=0; i<alloc.arguments.length; i++) {
                alloc.arguments[i] = maybeWrap(
                        scope,
                        alloc.arguments[i],
                        params[i]);
            }
        }
    }

    public void endVisit(ReturnStatement returnStatement, BlockScope scope) {
        MethodScope methodScope = scope.methodScope();
        MethodBinding methodBinding;
        // this nasty statement stolen from ReturnStatement.resolve(..)
        TypeBinding methodType =
            (methodScope.referenceContext instanceof AbstractMethodDeclaration)
                ? ((methodBinding = ((AbstractMethodDeclaration) methodScope.referenceContext).binding) == null
                    ? null
                    : methodBinding.returnType)
                : TypeBinding.VOID;

        boolean oldDisallow = this.disallowLower;
        if (scope.isGeneratedScope()) {
        	AbstractMethodDeclaration mDecl = scope.methodScope().referenceMethod();
        	if (mDecl.isMappingWrapper.callout())
        		this.disallowLower = true; // don't lower callout return!
        }
        try {
	        returnStatement.expression = maybeWrap(
	                scope,
	                returnStatement.expression,
	                methodType);
        } finally {
        	this.disallowLower = oldDisallow;
        }
    }

    public void endVisit(EqualExpression eqExpr, BlockScope scope) {
        TypeBinding leftType = eqExpr.left.resolvedType;
        TypeBinding rightType = eqExpr.right.resolvedType;

        Config oldConfig = Config.createOrResetConfig(this);
        try {
	        if ((   eqExpr.checkCastTypesCompatibility(scope, leftType, rightType, null)
	             || eqExpr.checkCastTypesCompatibility(scope, rightType, leftType, null)) // just recheck.
	            && Config.getLoweringRequired())
	            scope.problemReporter().illegalImplicitLower(
	                    eqExpr, leftType, rightType);
        } finally {
        	Config.removeOrRestore(oldConfig, this);
        }
    }

    public void endVisit(InstanceOfExpression ioExpr, BlockScope scope) {
        TypeBinding leftType = ioExpr.type.resolvedType;
        TypeBinding rightType = ioExpr.expression.resolvedType;

        Config oldConfig = Config.createOrResetConfig(this);
        try {
	        ioExpr.checkCastTypesCompatibility(scope, leftType, rightType, ioExpr.expression);
	        if (Config.getLoweringRequired())
	            scope.problemReporter().illegalImplicitLower(
	                    ioExpr, leftType, rightType);
        } finally {
        	Config.removeOrRestore(oldConfig, this);
        }
    }

    public void endVisit(CastExpression castExpr, BlockScope scope) {
        if (castExpr.isGenerated)
        	return; // don't adjust again!

        TypeBinding leftType = castExpr.type.resolvedType;
        TypeBinding rightType = castExpr.expression.resolvedType;

        Config oldConfig = Config.createOrResetConfig(this);
        try {
	        castExpr.checkCastTypesCompatibility(scope, leftType, rightType, castExpr.expression); // just recheck.
	        if (Config.getLoweringRequired())
	            scope.problemReporter().illegalImplicitLower(
	                    castExpr, leftType, rightType);
        } finally {
        	Config.removeOrRestore(oldConfig, this);
        }
    }

    public void endVisit(ThrowStatement throwStat, BlockScope scope) {
    	ReferenceBinding excType = (ReferenceBinding)throwStat.exceptionType;
    	ReferenceBinding expectedType = null;
    	if (excType.isSynthInterface())
    		expectedType = excType.roleModel.getClassPartBinding();
    	else if (excType instanceof RoleTypeBinding)
    		expectedType = excType.getRealClass();

    	if (expectedType != null)
    		throwStat.exception = maybeWrap(
    				scope,
					throwStat.exception,
					expectedType);
    }
    /*
     * This is what we do, if compatibility requires:
     * (a) Wrap 'expr' with a cast to 'expectedType', or:
     * (b) Lower 'expr' to 'expectedType'.
     */
    private Expression maybeWrap(
            BlockScope  scope,
            Expression  expr,
            TypeBinding expectedType)
    {
        Expression newExpr=null;
        if (expr != null) {
        	Config oldConfig = Config.createOrResetConfig(this);
            try {
	            expr.resolvedType.isCompatibleWith(expectedType); // just recheck.
	            ReferenceBinding requiredClass = Config.getCastRequired();
				if (requiredClass != null) {
					// FIXME(SH): this is still needed for casting role(ifc) to non-role super
					// (cf.  1.4.4-otjld-cast-to-superclass-1/3)
					Config.setCastRequired(null);
					if (   requiredClass == SourceTypeBinding.MultipleCasts
						|| !expectedType.isCompatibleWith(requiredClass)
						|| Config.getCastRequired() != null) // checking request by previous condition
					{
						throw new InternalCompilerError("incompatible cast requirements?"); //$NON-NLS-1$
					}
	                AstGenerator gen = new AstGenerator(expr.sourceStart, expr.sourceEnd);
					newExpr = gen.resolvedCastExpression(
				                        expr,
				                        expectedType,
										CastExpression.NEED_CLASS);
	            }
	            else if (Config.getLoweringRequired()) {
	            	if (this.disallowLower) {
	            		if (!isGeneratedCodeLoweringConfined(scope, expr.resolvedType, expectedType))
	            			scope.problemReporter().illegalImplicitLower(expr, expectedType, expr.resolvedType);
	            	} else {
	            		newExpr = new Lowering().lowerExpression(scope, expr, expr.resolvedType, expectedType, null/*teamExpression*/, true); // not enough context to provide teamExpr
	            	}
	            }
            } finally {
            	Config.removeOrRestore(oldConfig, this);
            }
        }
        if (newExpr != null) return newExpr;
        return expr;
    }

    /* Generated code may treat Confined as compatible to Object,
     * which looks like lowering if the role is bound.
 	 */
    private boolean isGeneratedCodeLoweringConfined(BlockScope scope, TypeBinding type, TypeBinding expected)
	{
		if (!scope.isGeneratedScope())
			return false;
		if (expected.id != TypeIds.T_JavaLangObject)
			return false;
		return  TypeAnalyzer.isConfined(type);
	}
}
