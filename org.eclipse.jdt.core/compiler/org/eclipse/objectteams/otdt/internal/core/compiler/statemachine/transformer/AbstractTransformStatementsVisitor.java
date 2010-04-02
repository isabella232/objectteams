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
 * $Id: AbstractTransformStatementsVisitor.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ParameterMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TsuperReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.WithinStatement;


/**
 * MIGRATION_STATE: 3.4
 *
 * A visitor which is able to replace expressions/statements within statements.
 * Subclasses just need to request replacement by calling <code>enterExpression</code>.
 * This class will then take care of performing the change in the parent of the
 * node to replace.
 * Also the infrastructure for recording pending replacements must be provided by
 * implementing the abstract methods. For a typical implementation of this infrastructure
 * see StackTransformStatementsVisitor.
 *
 * Note: all replacements take place in reverse order of traversal (due to stacking of replacements)
 *
 * @author stephan
 */
public abstract class AbstractTransformStatementsVisitor
    extends ASTVisitor
{
    /**
     * Request that statement or expression `oldExpr' be replaced by `newExpr'.
     * @param oldExpr
     * @param newExpr
     */
    protected abstract void enterExpression(Expression oldExpr, Expression newExpr, Statement node);

    /**
     * Verify that all replacements entered for and below node have been consumed by now.
     * @param node the statement/expression .. for which treatment is finished
     */
    protected abstract void assertAllConsumed(ASTNode node);

    /**
     * Check whether oldExpr is the one to be replaced. If so, retrieve the
     * and consume the current replacement expression.
     * @param oldExpr
     * @return expression previously recorded by enterExpression
     */
    protected abstract Expression checkConsumeExpression(Expression oldExpr, BlockScope scope);

    /**
     * Check whether oldStat is the one to be replaced. If so, retrieve the
     * and consume the current replacement statement.
     * @param oldStat
     * @return statement previously recorded by enterExpression
     */
    protected abstract Statement checkConsumeStatement(Statement oldStat, BlockScope scope);

    /** Private helper: replace an expression within an array of expressions.
     * @param stats The array
     */
    private void replaceStatsInArray(Statement[] stats, BlockScope scope)
    {
        if (stats != null)
            for (int i=stats.length-1; i>=0; i--)
           		stats[i] = checkConsumeStatement(stats[i], scope);
    }
    private void replaceExprsInArray(Expression[] exprs, BlockScope scope)
    {
        if (exprs != null)
            for (int i=exprs.length-1; i>=0; i--)
           		exprs[i] = checkConsumeExpression(exprs[i], scope);
    }


    // -------- below this line: endVisit methods for collecting entered replacements -------
	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
		assertStatement.assertExpression  = checkConsumeExpression(assertStatement.assertExpression, scope);
		assertAllConsumed(assertStatement);
	}
	public void endVisit(BaseCallMessageSend messageSend, BlockScope scope) {
		// Nop, messageSend._wrappee has already been traversed. Don't replace _wrappee itself
	}
	public void endVisit(MethodSpec methodSpec, BlockScope scope) {
		assertAllConsumed(methodSpec);
	}
	public void endVisit(StringLiteralConcatenation literal, BlockScope scope) {
		assertAllConsumed(literal);
	}
	public void endVisit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		assertAllConsumed(localTypeDeclaration);
	}
    protected void endVisit(AbstractMethodDeclaration decl) {
        replaceStatsInArray(decl.statements, decl.scope);
        assertAllConsumed(decl);
    }
    public void endVisit(ParameterMapping mapping, BlockScope scope) {
        mapping.expression = checkConsumeExpression(mapping.expression, scope);
        assertAllConsumed(mapping);
    }
    public void endVisit(AllocationExpression alloc, BlockScope scope) {
        replaceExprsInArray(alloc.arguments, scope);
        assertAllConsumed(alloc);
    }
    public void endVisit(AND_AND_Expression expr, BlockScope scope) {
        endVisit((BinaryExpression)expr, scope);
    }
    public void endVisit(Argument arg, BlockScope scope) {
        assertAllConsumed(arg);
    }
    public void endVisit(ArrayAllocationExpression alloc, BlockScope scope) {
        assertAllConsumed(alloc);
    }
    public void endVisit(ArrayInitializer init, BlockScope scope) {
        replaceExprsInArray(init.expressions, scope);
        assertAllConsumed(init);
    }
    public void endVisit(ArrayQualifiedTypeReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(ArrayQualifiedTypeReference ref, ClassScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(ArrayReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(ArrayTypeReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(ArrayTypeReference ref, ClassScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(Assignment ass, BlockScope scope) {
        ass.expression = checkConsumeExpression(ass.expression, scope);
        assertAllConsumed(ass);
    }
    public void endVisit(BinaryExpression expr, BlockScope scope) {
        expr.right = checkConsumeExpression(expr.right, scope);
        expr.left  = checkConsumeExpression(expr.left, scope);
        assertAllConsumed(expr);
    }
    public void endVisit(Block b, BlockScope scope) {
        replaceStatsInArray(b.statements, scope);
        assertAllConsumed(b);
    }
    public void endVisit(BreakStatement stat, BlockScope scope) {
        assertAllConsumed(stat);
    }
    public void endVisit(CaseStatement stat, BlockScope scope) {
        assertAllConsumed(stat);
    }
    public void endVisit(CastExpression cast, BlockScope scope) {
        cast.expression = checkConsumeExpression(cast.expression, scope);
        assertAllConsumed(cast);
    }
    public void endVisit(CharLiteral ch, BlockScope scope) {
        assertAllConsumed(ch);
    }
    public void endVisit(ClassLiteralAccess clazz, BlockScope scope) {
        assertAllConsumed(clazz);
    }
    public void endVisit(Clinit block, ClassScope scope) {
        endVisit(block);
    }
    public void endVisit(CompilationUnitDeclaration decl, CompilationUnitScope scope) {
        assertAllConsumed(decl);
    }
    public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
        endVisit((Assignment)compoundAssignment, scope);
    }
    public void endVisit(ConditionalExpression expr, BlockScope scope) {
    	expr.condition = checkConsumeExpression(expr.condition, scope);
        expr.valueIfFalse = checkConsumeExpression(expr.valueIfFalse, scope);
        expr.valueIfTrue = checkConsumeExpression(expr.valueIfTrue, scope);
        assertAllConsumed(expr);
    }
    public void endVisit(ConstructorDeclaration decl, ClassScope scope) {
        endVisit(decl);
    }
    public void endVisit(ContinueStatement continueStatement, BlockScope scope) {
        assertAllConsumed(continueStatement);
    }
    public void endVisit(DoStatement stat, BlockScope scope) {
        stat.condition = checkConsumeExpression(stat.condition, scope);
        stat.action = checkConsumeStatement(stat.action, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(DoubleLiteral doubleLiteral, BlockScope scope) {
        assertAllConsumed(doubleLiteral);
    }
    public void endVisit(EqualExpression expr, BlockScope scope) {
        endVisit((BinaryExpression)expr, scope);
    }
    public void endVisit(ExplicitConstructorCall call, BlockScope scope) {
        replaceExprsInArray(call.arguments, scope);
        assertAllConsumed(call);
    }
    public void endVisit(ExtendedStringLiteral lit, BlockScope scope) {
        assertAllConsumed(lit);
    }
    public void endVisit(FalseLiteral falseLiteral, BlockScope scope) {
        assertAllConsumed(falseLiteral);
    }
    public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
        fieldDeclaration.initialization = checkConsumeExpression(fieldDeclaration.initialization, scope);
        assertAllConsumed(fieldDeclaration);
    }
    public void endVisit(FieldReference fieldReference, BlockScope scope) {
        fieldReference.receiver = checkConsumeExpression(fieldReference.receiver, scope);
        assertAllConsumed(fieldReference);
    }
    public void endVisit(FloatLiteral floatLiteral, BlockScope scope) {
        assertAllConsumed(floatLiteral);
    }
    public void endVisit(EmptyStatement emptyStatement, BlockScope scope) {
        assertAllConsumed(emptyStatement);
    }
    public void endVisit(ForStatement forStatement, BlockScope scope) {
    	replaceStatsInArray(forStatement.initializations, scope);
    	forStatement.condition = checkConsumeExpression(forStatement.condition, scope);
    	replaceStatsInArray(forStatement.increments, scope);
        forStatement.action = checkConsumeStatement(forStatement.action, scope);
        assertAllConsumed(forStatement);
    }
    public void endVisit(IfStatement stat, BlockScope scope) {
    	stat.condition = checkConsumeExpression(stat.condition, scope);
        stat.elseStatement = checkConsumeStatement(stat.elseStatement, scope);
        stat.thenStatement = checkConsumeStatement(stat.thenStatement, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
        assertAllConsumed(importRef);
    }
    public void endVisit(Initializer initializer, MethodScope scope) {
        assertAllConsumed(initializer);
    }
    public void endVisit(InstanceOfExpression expr, BlockScope scope) {
        expr.expression = checkConsumeExpression(expr.expression, scope);
        assertAllConsumed(expr);
    }
    public void endVisit(IntLiteral intLiteral, BlockScope scope) {
        assertAllConsumed(intLiteral);
    }
    public void endVisit(LabeledStatement stat, BlockScope scope) {
        stat.statement = checkConsumeStatement(stat.statement, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(LocalDeclaration decl, BlockScope scope) {
        decl.initialization = checkConsumeExpression(decl.initialization, scope);
        assertAllConsumed(decl);
    }
    public void endVisit(LongLiteral longLiteral, BlockScope scope) {
        assertAllConsumed(longLiteral);
    }
    public void endVisit(TypeDeclaration decl, ClassScope scope) {
        assertAllConsumed(decl);
    }
    public void endVisit(MessageSend msg, BlockScope scope)
    {
        replaceExprsInArray(msg.arguments, scope);
        msg.receiver = checkConsumeExpression(msg.receiver, scope);
        assertAllConsumed(msg);
    }
    public void endVisit(MethodDeclaration decl, ClassScope scope) {
        endVisit(decl);
    }
    public void endVisit(NullLiteral nullLiteral, BlockScope scope) {
        assertAllConsumed(nullLiteral);
    }
    public void endVisit(OR_OR_Expression expr, BlockScope scope) {
        endVisit((BinaryExpression)expr, scope);
    }
    public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
		postfixExpression.expression = checkConsumeExpression(postfixExpression.expression, scope);
        assertAllConsumed(postfixExpression);
    }
    public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
    	prefixExpression.expression = checkConsumeExpression(prefixExpression.expression, scope);
        assertAllConsumed(prefixExpression);
    }
    public void endVisit(QualifiedAllocationExpression alloc, BlockScope scope) {
        replaceExprsInArray(alloc.arguments, scope);
        alloc.enclosingInstance = checkConsumeExpression(alloc.enclosingInstance, scope);
        assertAllConsumed(alloc);
    }
    public void endVisit(QualifiedNameReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(QualifiedSuperReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(QualifiedThisReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(QualifiedTypeReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(QualifiedTypeReference ref, ClassScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(ReturnStatement ret, BlockScope scope) {
        ret.expression = checkConsumeExpression(ret.expression, scope);
        assertAllConsumed(ret);
    }
    public void endVisit(SingleNameReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(SingleTypeReference ref, BlockScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(SingleTypeReference ref, ClassScope scope) {
        assertAllConsumed(ref);
    }
    public void endVisit(StringLiteral stringLiteral, BlockScope scope) {
        assertAllConsumed(stringLiteral);
    }
    public void endVisit(SuperReference superReference, BlockScope scope) {
        assertAllConsumed(superReference);
    }
    public void endVisit(SwitchStatement stat, BlockScope scope) {
        stat.expression = checkConsumeExpression(stat.expression, scope);
        replaceStatsInArray(stat.statements, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(SynchronizedStatement stat, BlockScope scope) {
        stat.expression = checkConsumeExpression(stat.expression, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(ThisReference thisReference, BlockScope scope) {
        assertAllConsumed(thisReference);
    }
    public void endVisit(ThrowStatement stat, BlockScope scope) {
        stat.exception = checkConsumeExpression(stat.exception, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(TrueLiteral trueLiteral, BlockScope scope) {
        assertAllConsumed(trueLiteral);
    }
    public void endVisit(TryStatement tryStatement, BlockScope scope) {
        assertAllConsumed(tryStatement);
    }
    public void endVisit(TypeDeclaration decl, CompilationUnitScope scope) {
        assertAllConsumed(decl);
    }
    public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
    	unaryExpression.expression = checkConsumeExpression(unaryExpression.expression, scope);
        assertAllConsumed(unaryExpression);
    }
    public void endVisit(WhileStatement stat, BlockScope scope) {
        stat.condition = checkConsumeExpression(stat.condition, scope);
        stat.action = checkConsumeStatement(stat.action, scope);
        assertAllConsumed(stat);
    }
    public void endVisit(LiftingTypeReference  liftingTypeReference, BlockScope scope) {
        assertAllConsumed(liftingTypeReference);
    }
    public void endVisit(LiftingTypeReference  liftingTypeReference, ClassScope scope) {
        assertAllConsumed(liftingTypeReference);
    }
    public void endVisit(WithinStatement stat, BlockScope scope) {
        endVisit((Block)stat, scope); // has already been translated to a flat block of statements
    }
    public void endVisit(TsuperReference tsuperReference, BlockScope scope) {
        assertAllConsumed(tsuperReference);
    }
    public void endVisit(BaseReference baseReference, BlockScope scope) {
        assertAllConsumed(baseReference);
    }
    public void endVisit(CallinMappingDeclaration decl, ClassScope scope) {
        assertAllConsumed(decl);

    }
    public void endVisit(CalloutMappingDeclaration decl, ClassScope scope) {
        assertAllConsumed(decl);
    }

}
