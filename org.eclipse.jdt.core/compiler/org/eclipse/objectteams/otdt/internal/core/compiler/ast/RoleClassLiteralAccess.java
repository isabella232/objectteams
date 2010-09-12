/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright  2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleClassLiteralAccess.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import static org.eclipse.jdt.internal.compiler.lookup.Binding.NO_METHODS;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccVisibilityMASK;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.GET_CLASS_PREFIX;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;


/**
 * NEW for OTDT.
 *
 * This class specializes MyType.class expressions for role types.
 * Such expressions must be delegated to a dynamically bound team method,
 * in order to implement late binding of the role type.
 *
 * @author stephan
 * @version $Id: RoleClassLiteralAccess.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class RoleClassLiteralAccess extends ClassLiteralAccess {

	MessageSend      send;        // transformed version of this access
	ReferenceBinding teamBinding; // the team to call

	/**
	 * Construct a role class literal from a given class literal.
	 * @param
	 */
	public RoleClassLiteralAccess(ClassLiteralAccess orig, TypeBinding methodType) {
		super(orig.sourceEnd, orig.type);
		this.constant = orig.constant;
		generateMessageSend(methodType);
	}

	/** 
	 * Constructor for use by the Parser.
     */
	public RoleClassLiteralAccess(SingleTypeReference typeRef) {
		super(typeRef.sourceEnd, typeRef);
	}
	
	private void generateMessageSend(TypeBinding methodType) {
		AstGenerator gen = new AstGenerator(this.type.sourceStart, this.sourceEnd);
		ReferenceBinding roleBinding = (ReferenceBinding)this.type.resolvedType;
		this.teamBinding = roleBinding.enclosingType();
		if (!roleBinding.isValidBinding()) {
			this.resolvedType= methodType; // pre-computed type of getclass method
			return;                        // don't generate illegal message send
		}
		Expression receiver;
		if (RoleTypeBinding.isRoleWithExplicitAnchor(roleBinding)) {
			// create some.anchor._OT$getClass$R()
			char[][] tokens = ((RoleTypeBinding)roleBinding)._teamAnchor.tokens();
			if (tokens.length == 1)
				receiver= gen.singleNameReference(tokens[0]);
			else
				receiver= gen.qualifiedNameReference(tokens);
		} else {
			// create MyTeam.this._OT$getClass$R()
			receiver= gen.qualifiedThisReference(this.teamBinding);
		}
		this.send = gen.messageSend(
					receiver,
					CharOperation.concat(GET_CLASS_PREFIX, roleBinding.sourceName()),
					new Expression[] {}
				);
	}

	public FlowInfo analyseCode(
			BlockScope currentScope,
			FlowContext flowContext,
			FlowInfo flowInfo)
	{
		// simply delegate:
		if (this.send != null)
			return this.send.analyseCode(currentScope, flowContext, flowInfo);
		return flowInfo;
	}

	public void generateCode(
			BlockScope currentScope,
			CodeStream codeStream,
			boolean valueRequired)
	{
		// simply delegate:
		if (this.send != null)
			this.send.generateCode(currentScope, codeStream, valueRequired);
		else if (valueRequired)
			codeStream.aconst_null();
	}

	public TypeBinding resolveType(BlockScope scope) {
		if (this.type instanceof ParameterizedSingleTypeReference) {
			// directly created from parsing R<@t>.class
			this.constant = Constant.NotAConstant;
			
			ParameterizedSingleTypeReference typeRef = (ParameterizedSingleTypeReference)this.type;
			if (typeRef.token == null) {
				// syntax error, however, we can still check the type anchor:
				if (typeRef.typeArguments != null)
					for (TypeReference anchors : typeRef.typeArguments)
						((TypeAnchorReference)anchors).resolveAnchor(scope);
				return null;
			}
			TypeBinding roleType = this.type.resolveType(scope);
			if (roleType != null && roleType.isValidBinding()) 
				generateMessageSend(roleType);
		}
		// clients might still use this (obsolete) field?
		this.targetType = this.type.resolvedType; // is already resolved by the original ClassLiteralAccess
		// and delegate:
		if (this.send != null)
			this.resolvedType = this.send.resolveType(scope);
		return this.resolvedType;
	}

	/**
	 * API for Dependencies:
	 *
	 * Generate for each role (bound or unbound):
	 *		Class _OT$getClass$<roleName>()
	 * and add it to the enclosing team.
	 * @param teamModel team to add the method to
	 * @param roleModel the role whose class should be accessed
	 */
	public static TypeBinding ensureGetClassMethod(
			TeamModel teamModel,
			RoleModel roleModel)
	{
		TypeDeclaration  teamDecl    = teamModel.getAst();
		ReferenceBinding teamBinding = teamModel.getBinding();
		TypeDeclaration  roleDecl    = roleModel.getAst();
		ReferenceBinding roleBinding = roleModel.getBinding();
		char[] selector = CharOperation.concat(GET_CLASS_PREFIX, roleBinding.sourceName());
		TypeBinding result = ensureGetClassMethodPart(teamDecl, teamBinding, roleDecl, roleBinding, selector);
		if (teamBinding.isRole()) {
			TypeDeclaration teamIfc = teamBinding.roleModel.getInterfaceAst();
			if (teamIfc != null)
				ensureGetClassMethodPart(teamIfc, teamIfc.binding, roleDecl, roleBinding, selector);
		}
		return result;
	}

	private static TypeBinding ensureGetClassMethodPart(TypeDeclaration teamDecl, ReferenceBinding teamBinding,
													    TypeDeclaration roleDecl, ReferenceBinding roleBinding, char[] selector) 
	{
		MethodBinding[] existingMethods = teamBinding.getMethods(selector);
		if (existingMethods != NO_METHODS)
			return existingMethods[0].returnType; // already generated
		if (teamDecl == null)
			throw new InternalCompilerError("Requesting to generate a method for binary type "+String.valueOf(teamBinding.readableName()));		 //$NON-NLS-1$
		AstGenerator gen;
		if (roleDecl != null)
			gen = new AstGenerator(roleDecl.scope.compilerOptions().sourceLevel, roleDecl.sourceStart, roleDecl.sourceEnd);
		else
			gen = new AstGenerator(teamDecl.scope.compilerOptions().sourceLevel, teamDecl.sourceStart, teamDecl.sourceEnd);
		MethodDeclaration method = gen.method(teamDecl.compilationResult,
				(teamBinding.isRole())
					? ClassFileConstants.AccPublic // advertized via ifc, must be public
					: roleBinding.modifiers & AccVisibilityMASK,
				gen.parameterizedQualifiedTypeReference( // java.lang.Class<R>
							TypeConstants.JAVA_LANG_CLASS,
							new TypeBinding[] { roleBinding.getRealType() },
							true/*deeply generic*/),
				selector,
				null);
		if (teamBinding.isInterface())
			method.modifiers |= ClassFileConstants.AccAbstract|ExtraCompilerModifiers.AccSemicolonBody;
		else
			method.setStatements(new Statement[] {
				gen.returnStatement(new ClassLiteralAccess(gen.sourceEnd, gen.typeReference(roleBinding), true))
			});
		AstEdit.addMethod(teamDecl, method);
		return method.returnType.resolvedType;
	}
}
