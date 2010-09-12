/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IStatementsGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;

/**
 * Interface for deferred creation of statements for generated methods.
 * Transformers usually create an anonymous instance of this type and
 * store it in the method's MethodModel.
 * Dependencies.establishLateElementsCopied(..) will find the statements
 * generator and invoke it.
 *
 * @author stephan
 */
public interface IStatementsGenerator {

	/** Generate including a registered prefix if any. */
	public boolean generateAllStatements(AbstractMethodDeclaration methodDecl);

	/** Register statements for the front of this method. */
	public void prepend(List<Statement> newStatements);
}
