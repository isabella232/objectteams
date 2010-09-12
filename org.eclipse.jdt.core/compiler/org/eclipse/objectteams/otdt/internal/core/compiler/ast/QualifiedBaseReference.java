/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009, Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/** 
 * This class represents a reference of the form <code>Type.base</code>
 * for use as a type anchor in a {@link TypeAnchorReference}.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class QualifiedBaseReference extends QualifiedThisReference {

	FieldBinding baseField;
	
	public QualifiedBaseReference(TypeReference name, int sourceStart, int sourceEnd) {
		super(name, sourceStart, sourceEnd);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		TypeBinding superResult = super.resolveType(scope);
		if (superResult == null || !superResult.isValidBinding())
			return null;
		if (this.currentCompatibleType != null && this.currentCompatibleType.isValidBinding()) {
			this.baseField = this.currentCompatibleType.getField(IOTConstants._OT_BASE, true);
			if (this.baseField != null) {
				if (this.baseField.isValidBinding())
					return this.resolvedType = this.baseField.type;
			} else {
				this.baseField = new ProblemFieldBinding((ReferenceBinding)this.resolvedType, IOTConstants.BASE, ProblemReasons.NotFound);
			}
		}		
		scope.problemReporter().unboundQualifiedBase((ReferenceBinding)this.qualification.resolvedType, this);
		return null; 
	}
	
	@Override
	public TypeBinding resolveType(ClassScope scope) {
		// for baseclass reference we indeed need to resolve from a class scope,
		// use the initializer scope as representing the class:
		return resolveType(scope.referenceContext.initializerScope);
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		// used only when enclosing TypeAnchorReference has isExpression==true.
		int pc = codeStream.position;
		super.generateCode(currentScope, codeStream, valueRequired);
		codeStream.fieldAccess(Opcodes.OPC_getfield, this.baseField, this.baseField.declaringClass);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.qualification.print(0, output).append(".base"); //$NON-NLS-1$
	}
}
