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
 * $Id: BaseAllocationExpression.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CREATOR_PREFIX_NAME;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants._OT_BASE;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * A base constructor invocation "base(args)";
 * Translated to "_OT$base = new BaseClass(args);"
 *
 * @author stephan
 * @version $Id: BaseAllocationExpression.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class BaseAllocationExpression extends Assignment {

    // intermediate store, before transfering this to the AllocationExpression.
    public Expression[] arguments;
    public Expression enclosingInstance;

    public boolean isExpression = false;
    private boolean isAstCreated = false;
	private Boolean checkResult = null; // three-valued logic (incl null)

    /**
     *
     */
    public BaseAllocationExpression(int start, int end) {
        // only set the lhs yet, expression is constructed later.
        super(new SingleNameReference(_OT_BASE, 0), null, end);
        this.sourceStart = start;
    }

	public FlowInfo analyseCode(
			BlockScope currentScope,
			FlowContext flowContext,
			FlowInfo flowInfo)
	{
		if (this.isExpression) // don't treat as assignment
			return this.expression.analyseCode(currentScope, flowContext, flowInfo);
		// in case of error assume it might relate to duplicate base() calls, don't report again.
		if (((AbstractMethodDeclaration)currentScope.methodScope().referenceContext).ignoreFurtherInvestigation)
			return flowInfo;
		return super.analyseCode(currentScope, flowContext, flowInfo);
	}

    /**
     * Initialize the type to create from the "playedBy" clause.
     * @param scope non-null
     */
    private void createAst(BlockScope scope) {
        if (this.isAstCreated) return; // already done.

        this.isAstCreated = true; // even if creation fails, don't try again.

        ReferenceBinding          enclType;
        AbstractMethodDeclaration enclMethodDecl;
        ReferenceBinding          baseclass = null;

        enclType = scope.enclosingSourceType();
        enclMethodDecl = (AbstractMethodDeclaration)scope.methodScope().referenceContext;

        if (enclType.isDirectRole())
            baseclass = ((MemberTypeBinding)enclType).baseclass();
        if (   baseclass == null
            || !enclMethodDecl.isConstructor())
        {
            scope.problemReporter().baseConstructorCallInWrongMethod(
                    this, scope.methodScope().referenceContext);
            return;
        }
        ConstructorDeclaration enclCtor = (ConstructorDeclaration)enclMethodDecl;
        if (this.isExpression) {
        	if (!isArgOfOtherCtor(enclCtor, scope))
        		scope.problemReporter().baseConstructorExpressionOutsideCtorCall(this);
        } else {
        	if (enclCtor.statements[0] != this) 
        		scope.problemReporter().baseConstructorCallIsNotFirst(this);
        }

        AstGenerator gen = new AstGenerator(this.sourceStart, this.sourceEnd);
        Expression allocation;
        if (this.enclosingInstance != null) {
        	this.enclosingInstance= new PotentialLowerExpression(this.enclosingInstance, baseclass.enclosingType());
        // FIXME(SH): check baseclass.enclosingType();
        }

        if (baseclass.isDirectRole()) {
        	// instead of new B() create:
        	// 		receiver._OT$createB():
        	Expression receiver;
        	if (RoleTypeBinding.isRoleWithExplicitAnchor(baseclass)) {
	        	RoleTypeBinding baseRole = (RoleTypeBinding)baseclass;
	        	ITeamAnchor anchor = baseRole._teamAnchor;
	        	ReferenceBinding startClass = anchor.getFirstDeclaringClass();
	        	char[][] tokens = anchor.tokens();
	        	if (startClass != null) {
	        		// relevant start class, create as receiver:
	        		//     EnclType.this.field1.
	        		TypeReference startReference = gen.typeReference(startClass);
	        		startReference.setBaseclassDecapsulation(DecapsulationState.ALLOWED);
	        		receiver = gen.qualifiedThisReference(startReference);
	        		for (int i = 0; i < tokens.length; i++) {
						receiver = gen.fieldReference(receiver, tokens[i]);
					}
	        	} else {
	        		// the best name path defines the receiver:
		        	receiver = gen.qualifiedNameReference(tokens);
	        	}
        	} else {
        		if (this.enclosingInstance != null)
        			receiver= this.enclosingInstance;
        		else
        			receiver = gen.thisReference(); // creating a role of the same team as base instance??
        	}
            char[] selector = CharOperation.concat(CREATOR_PREFIX_NAME, baseclass.sourceName());

        	MessageSend allocSend = new MessageSend() {
        		@Override
				public boolean isDecapsulationAllowed(Scope scope2) {
        			// this message send can decapsulate independent of scope
        			return true;
        		}
        		@Override
        		public DecapsulationState getBaseclassDecapsulation() {
        			return DecapsulationState.ALLOWED;
        		}
        	};
        	gen.setPositions(allocSend);
        	allocSend.receiver = receiver;
        	allocSend.selector = selector;
        	allocSend.arguments = this.arguments;
        	allocation = allocSend;
        } else {
            AllocationExpression alloc = newAllocation(baseclass, gen);
            alloc.type.setBaseclassDecapsulation(DecapsulationState.ALLOWED); // report individually
            alloc.arguments    = this.arguments;
            alloc.sourceStart  = this.sourceStart;
            alloc.sourceEnd    = this.sourceEnd;
            alloc.statementEnd = this.statementEnd;
            allocation = alloc;
        }
        this.arguments = null; // don't use any more.

        ExplicitConstructorCall selfcall = enclCtor.constructorCall;
        if (   selfcall.isImplicitSuper()
        	&& enclType.superclass().isDirectRole()
			&& enclType.superclass().baseclass() != null)
        {
        	// implement 2.4.2(c):
        	// transform "super(); base(args);" => "super(new MyBase(args)); nop;"
        	enclCtor.constructorCall = genLiftCtorCall(allocation);
        	enclCtor.statements[0] = new AstGenerator(this.sourceStart, this.sourceEnd).emptyStatement();
        	// pretend we are not calling base() because we already call the lifting-ctor.
        } else if (this.isExpression) {
        	// similar to above:
        	// translate "super(base(args), ...);" as "super(new MyBase(args), ...);"
        	this.expression = allocation; // and ignore the assignment flavor of this node.
        } else {
        	if (   !enclType.roleModel.hasBaseclassProblem()
        		&& !scope.referenceType().ignoreFurtherInvestigation)
        	{
	        	// needed by ASTConverter:
	        	this.expression = allocation;

	    		MethodModel.setCallsBaseCtor(enclCtor);

	    		// really creating base here, need to register this base object
	    		RoleModel boundRootRoleModel = enclType.roleModel.getBoundRootRole();
	    		if (boundRootRoleModel == null)
    				throw new InternalCompilerError("Unexpected: role has neither baseclassProblem nor boundRootRole"); //$NON-NLS-1$
	    		Statement[] regStats = Lifting.genRoleRegistrationStatements(scope,
	    																	 boundRootRoleModel,
	    																	 baseclass,
	    																	 enclCtor,
	    																	 gen);
	    		int len = enclCtor.statements.length;
	    		Statement[] newStats = new Statement[len+regStats.length];
	    		newStats[0] = this;
	    		System.arraycopy(regStats, 0, newStats, 1, regStats.length);
	    		System.arraycopy(enclCtor.statements, 1, newStats, regStats.length+1, len-1);
	    		enclCtor.setStatements(newStats);
    		}
        }
    }
    
    private boolean isArgOfOtherCtor(ConstructorDeclaration constructorDecl, BlockScope scope) {
    	class FoundException extends RuntimeException {}
    	class NotFoundException extends RuntimeException {}
    	try {
    		constructorDecl.traverse(new ASTVisitor() {
    			int inCtorCall=0;
    			@Override
    			public boolean visit(ExplicitConstructorCall ctorCall, BlockScope scope) {
    				this.inCtorCall++;
   					return super.visit(ctorCall, scope);
    			}
    			@Override
    			public void endVisit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
    				super.endVisit(explicitConstructor, scope);
    				this.inCtorCall--;
    			}
    			@Override
    			public boolean visit(Assignment assig, BlockScope scope) {
    				if (assig == BaseAllocationExpression.this) {
    					if (this.inCtorCall>0)
    						throw new FoundException();
    					else
    						throw new NotFoundException();
    				}
    				return super.visit(assig, scope);
    			}
			},
			scope.classScope());
    	} catch (FoundException fe) {
    		return true;
    	} catch (NotFoundException nfe) {
    		return false;
    	}
    	return false;
    }

	private AllocationExpression newAllocation(ReferenceBinding baseclass, AstGenerator gen)
	{
		if (this.enclosingInstance == null) {
			AllocationExpression alloc= new AllocationExpression();
			alloc.type= gen.typeReference(baseclass);
			return alloc;
		} else {
			QualifiedAllocationExpression alloc= new QualifiedAllocationExpression();
			alloc.enclosingInstance= this.enclosingInstance;
			alloc.type= gen.singleTypeReference(baseclass.sourceName);
			return alloc;
		}
	}

    /**
	 * @param baseExpr
	 */
	private ExplicitConstructorCall genLiftCtorCall(Expression baseExpr) {
		ExplicitConstructorCall constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.Super);
		constructorCall.arguments = new Expression[] { baseExpr };
		constructorCall.sourceStart = this.sourceStart;
		constructorCall.sourceEnd   = this.sourceEnd;
		return constructorCall;
	}

	public boolean checkGenerate(BlockScope scope) {
		if (this.checkResult != null)
			return this.checkResult;
		this.checkResult = Boolean.TRUE; // preliminary, prevent re-entrance from isArgOfOtherCtor
		return this.checkResult = Boolean.valueOf(internalCheckGenerate(scope));
	}
	private boolean internalCheckGenerate(BlockScope scope) {
    	if (scope == null)
    		return false;
    	ReferenceContext referenceContext = scope.methodScope().referenceContext;
    	if (!(referenceContext instanceof AbstractMethodDeclaration)) {
    		scope.problemReporter().baseConstructorCallInWrongMethod(this, referenceContext);
    		return false;
    	}
    	AbstractMethodDeclaration enclosingMethodDeclaration = (AbstractMethodDeclaration)referenceContext;
		if (!enclosingMethodDeclaration.ignoreFurtherInvestigation)
		{
    		createAst(scope);
    		return this.expression != null;
		}
		return false;
    }

    public void traverse(ASTVisitor visitor, BlockScope scope) {
        // we might be the first to analyse this expression:
        // (Actually triggered by TransformStatementsVisitor
    	//  - but don't do it before STATE_LENV_DONE_FIELDS_AND_METHODS,
    	//    which is needed to lookup baseclass()!)
    	TypeDeclaration enclType = (scope != null) ? scope.referenceType() : null;
    	if (   enclType != null
    		&& enclType.isDirectRole()
			&& StateHelper.hasState(enclType.binding, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS))
    	{
	    	if (checkGenerate(scope)) { // only if successful:
	    		// when called from createAst->isArgOfOtherCtor we don't yet have the expression generated
	    		if (this.isExpression && this.expression != null)
	    			this.expression.traverse(visitor, scope);
	    		else
	    			super.traverse(visitor, scope);
	    	}
    	} else {
    		if (this.expression != null)
    			super.traverse(visitor, scope);
    	}
    }

    public TypeBinding resolveType(BlockScope scope) {
    	TypeDeclaration roleDecl = scope.referenceType();
    	if (roleDecl != null && roleDecl.isRole() && roleDecl.getRoleModel()._playedByEnclosing) {
    		scope.problemReporter().baseAllocationDespiteBaseclassCycle(this, roleDecl);
    		return null;
    	}
        if (!checkGenerate(scope)) { // createAst failed.
            return null;
        }
		if (this.isExpression) // don't treat as assignment
			return this.resolvedType = this.expression.resolveType(scope);
        if (!scope.methodScope().referenceContext.hasErrors())
            return super.resolveType(scope);
        return null;
    }
    
    @Override
    public void computeConversion(Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType) {
    	if (this.isExpression)
    		this.expression.computeConversion(scope, runtimeType, compileTimeType);
    	else
    		super.computeConversion(scope, runtimeType, compileTimeType);
    }

    @Override
    public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.isExpression) // don't treat as assignment
			this.expression.generateCode(currentScope, codeStream, valueRequired);
		else
			super.generateCode(currentScope, codeStream, valueRequired);
    }
    
    public String toString() {
        if (this.expression == null)
            return "unresolved base() call"; //$NON-NLS-1$
        return this.expression.toString();
    }

    public StringBuffer printExpression (int indent, StringBuffer output) {
    	if (this.expression != null)
    		return super.printExpression(indent, output);
    	return output.append("<no expression yet>"); //$NON-NLS-1$
    }

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {
    	if (this.expression != null)
    		return super.printExpressionNoParenthesis(indent, output);
    	return output.append("<no expression yet>"); //$NON-NLS-1$
	}

}
