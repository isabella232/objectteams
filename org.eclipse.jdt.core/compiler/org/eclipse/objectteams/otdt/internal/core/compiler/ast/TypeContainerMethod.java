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
 * $Id: TypeContainerMethod.java 19873 2009-04-13 16:51:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.Parser;

/**
 * NEW for OTDT.
 *
 * This class is used to wrap local types of roles in a method during copy inheritance.
 *
 * Why: During copy inheritance we need an AST-node contained in the role class,
 * 		which triggers code generation (ie., copyAdjustBytecode()).
 * 	    Without the TypeContainerMethod we would simply not find local types,
 * 		since the method originally defining this local type is available only
 * 	    as a MethodBinding, not its AST.
 *
 * @version $Id: TypeContainerMethod.java 19873 2009-04-13 16:51:05Z stephan $
 * @author stephan
 */
public class TypeContainerMethod extends MethodDeclaration {

	private final static char[] SELECTOR = "<type wrapper>".toCharArray(); //$NON-NLS-1$
	/**
	 * @param compilationResult
	 * @param localType the type to wrap
	 */
	public TypeContainerMethod(CompilationResult compilationResult, TypeDeclaration localType)
	{
		super(compilationResult);
		setStatements(new Statement[] {localType});
		this.selector = SELECTOR;
		this.isGenerated = true;
		this.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// NO-OP. (there is no source at all)
	}

	/**
	 * Override method from AbstractMethodDeclaration: only generate code
	 * for the wrapped type, not for the method itself.
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {
		CodeStream codeStream = classFile.codeStream;
		codeStream.reset(this, classFile);
		this.statements[0].generateCode(this.scope, codeStream);
	}

	public void traverse(
			ASTVisitor visitor,
			ClassScope classScope)
	{
		// NO-OP
		// do nothing because otherwise TransformStatementsTransformer would
		// again add the type to RoleModel._localTypes, where it is already contained.
	}

	public void resolve(ClassScope upperScope) {
		// NO-OP
	}
	/**
	 * Is method a type container method?
	 */
	public static boolean isTypeContainer(MethodBinding method) {
		return CharOperation.equals(method.selector, SELECTOR);
	}
}
