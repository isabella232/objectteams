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
 * $Id: BaseCallTrackingVariable.java 19873 2009-04-13 16:51:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

/**
 * NEW for OTDT:
 *
 * Special kind of dummy variable that shall only track the occurrences of
 * base calls within a callin method.
 * We use the mechanisms of definite assignment to find out whether a base call
 * happens
 * - definitely/potentially
 * - definitely/potentially not
 * - definitely/potentially more than once
 *
 * @author stephan
 * @version $Id: BaseCallTrackingVariable.java 19873 2009-04-13 16:51:05Z stephan $
 */
public class BaseCallTrackingVariable extends LocalDeclaration {

	private static final char[] NAME = "<baseCallTracker>".toCharArray(); //$NON-NLS-1$

	/**
	 * @param the callin method to analyze
	 */
	public BaseCallTrackingVariable(MethodDeclaration method) {
		super(NAME, method.modifiersSourceStart, method.sourceStart);
		this.type = new SingleTypeReference(
				"<no type>".toCharArray(),  //$NON-NLS-1$
				((long)this.sourceStart <<32)+this.sourceEnd);
		this.isGenerated = true;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream)
	{ /* NOP - this variable is completely dummy, ie. for analysis only. */ }

	/**
	 * Resolving a BaseCallTrackingVariable must happen after everything else of
	 * the callin method has been resolved, because we need to know the number
	 * of (real) allocated local variables.
	 */
	public void resolve (BlockScope scope) {
		// only need the binding, which is used as reference in FlowInfo methods.
		this.binding = new LocalVariableBinding(
				this.name,
				Scope.getBaseType("boolean".toCharArray()),  // arbitrary.. //$NON-NLS-1$
				ClassFileConstants.AccFinal,
				false);
		this.binding.setConstant(Constant.NotAConstant);
		// use a free slot without assigning it:
		this.binding.id = scope.outerMostMethodScope().analysisIndex+1;
	}
}
