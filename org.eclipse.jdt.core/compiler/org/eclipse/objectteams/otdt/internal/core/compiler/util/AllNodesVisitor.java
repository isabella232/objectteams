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
 * $Id: AllNodesVisitor.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
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
 * A visitor that maps all visit methods to visitNode(ASTNode).
 * Note: yet unused class.
 */
public abstract class AllNodesVisitor extends ASTVisitor {
	protected abstract void visitNode(ASTNode node);

	public void endVisit(
		AllocationExpression node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(AND_AND_Expression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
			AnnotationMethodDeclaration node,
			ClassScope classScope) {
			visitNode(node);
	}
	public void endVisit(Argument node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Argument node,ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		ArrayAllocationExpression node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ArrayInitializer node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		ArrayQualifiedTypeReference node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		ArrayQualifiedTypeReference node,
		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ArrayReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ArrayTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ArrayTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(AssertStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Assignment node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(BinaryExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Block node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(BreakStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(CaseStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(CastExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(CharLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ClassLiteralAccess node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Clinit node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
		CompilationUnitDeclaration node,
		CompilationUnitScope scope) {
		visitNode(node);
	}
	public void endVisit(CompoundAssignment node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
			ConditionalExpression node,
			BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		ConstructorDeclaration node,
		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ContinueStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(DoStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(DoubleLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(EmptyStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(EqualExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		ExplicitConstructorCall node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		ExtendedStringLiteral node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(FalseLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(FieldDeclaration node, MethodScope scope) {
		visitNode(node);
	}
	public void endVisit(FieldReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(FieldReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(FloatLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ForeachStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ForStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(IfStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ImportReference node, CompilationUnitScope scope) {
		visitNode(node);
	}
	public void endVisit(Initializer node, MethodScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		InstanceOfExpression node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(IntLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Javadoc node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Javadoc node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocAllocationExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocAllocationExpression node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArgumentExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArgumentExpression node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArrayQualifiedTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArrayQualifiedTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArraySingleTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocArraySingleTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocFieldReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocFieldReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocImplicitTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocImplicitTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocMessageSend node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocMessageSend node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocQualifiedTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocQualifiedTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocReturnStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocReturnStatement node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocSingleNameReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocSingleNameReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocSingleTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(JavadocSingleTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(LabeledStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(LocalDeclaration node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(LongLiteral node, BlockScope scope) {
		visitNode(node);
	}
	/**
	 * @param annotation
	 * @param scope
	 * @since 3.1
	 */
	public void endVisit(MarkerAnnotation node, BlockScope scope) {
		visitNode(node);
	}
	/**
	 * @param pair
	 * @param scope
	 */
	public void endVisit(MemberValuePair node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(MessageSend node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(MethodDeclaration node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(StringLiteralConcatenation node, BlockScope scope) {
		visitNode(node);
	}
	/**
	 * @param annotation
	 * @param scope
	 * @since 3.1
	 */
	public void endVisit(NormalAnnotation node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(NullLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(OR_OR_Expression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ParameterizedQualifiedTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ParameterizedQualifiedTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ParameterizedSingleTypeReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ParameterizedSingleTypeReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(PostfixExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(PrefixExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedAllocationExpression node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		QualifiedNameReference node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
			QualifiedNameReference node,
			ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedSuperReference node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedSuperReference node,
    		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedThisReference node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedThisReference node,
    		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedTypeReference node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		QualifiedTypeReference node,
    		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ReturnStatement node, BlockScope scope) {
		visitNode(node);
	}
	/**
	 * @param annotation
	 * @param scope
	 * @since 3.1
	 */
	public void endVisit(SingleMemberAnnotation node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		SingleNameReference node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
			SingleNameReference node,
			ClassScope scope) {
			visitNode(node);
	}
	public void endVisit(
    		SingleTypeReference node,
    		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
    		SingleTypeReference node,
    		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(StringLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(SuperReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(SwitchStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		SynchronizedStatement node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ThisReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(ThisReference node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ThrowStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(TrueLiteral node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(TryStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		TypeDeclaration node,
		BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(
		TypeDeclaration node,
		ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(
		TypeDeclaration node,
		CompilationUnitScope scope) {
		visitNode(node);
	}
	public void endVisit(TypeParameter node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(TypeParameter node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(UnaryExpression node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(WhileStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Wildcard node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(Wildcard node, ClassScope scope) {
		visitNode(node);
	}

//{ObjectTeams:  visit new ast nodes:
	public void endVisit(LiftingTypeReference  node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(LiftingTypeReference  node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(WithinStatement node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(TsuperReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(BaseReference node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(MethodSpec node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(CallinMappingDeclaration node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(CalloutMappingDeclaration node, ClassScope scope) {
		visitNode(node);
	}
	public void endVisit(ParameterMapping node, BlockScope scope) {
		visitNode(node);
	}
	public void endVisit(BaseCallMessageSend node, BlockScope scope) {
		visitNode(node);
	}
// SH et al}
}
