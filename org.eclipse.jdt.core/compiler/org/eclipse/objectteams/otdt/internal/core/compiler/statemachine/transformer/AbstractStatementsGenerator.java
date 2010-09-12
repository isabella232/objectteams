/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2008 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractStatementsGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;

/**
 * Deferred generation of method statements, with the option to
 * prepend additional statements.
 *
 * @author stephan
 * @since 1.2.1
 */
public abstract class AbstractStatementsGenerator implements IStatementsGenerator
{
	private List<Statement> prefixStats = null;

	/** Register statements for the front of this method. */
	public void prepend(List<Statement> newStatements) {
		this.prefixStats = newStatements;
	}

	/** Generate the statements for generated method methodDecl. */
	protected abstract boolean generateStatements(AbstractMethodDeclaration methodDecl);


	/** Generate statements and merge them with registered prefixStats, if any. */
	public boolean generateAllStatements(AbstractMethodDeclaration methodDecl) {
		if (!generateStatements(methodDecl))
			return false;
		if (this.prefixStats == null)
			return true;
		int len1 = this.prefixStats.size();
		int len2 = methodDecl.statements.length;
		Statement[] newStats = new Statement[len1+len2];
		this.prefixStats.toArray(newStats);
		System.arraycopy(methodDecl.statements, 0, newStats, len1, len2);
		methodDecl.statements = newStats;
		return true;
	}
}
