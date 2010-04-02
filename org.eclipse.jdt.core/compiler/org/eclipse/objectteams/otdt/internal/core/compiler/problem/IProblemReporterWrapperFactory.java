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
 * $Id: IProblemReporterWrapperFactory.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.problem;

import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;


/**
 * Implement this interface to create a wrapper around ProblemReporter, to
 * wrap the error handling of special AST elements, e.g. CalloutMessageSend.
 * Pass an instance of this interface to a BlockScopeWrapper, which needs to be
 * used in your special AST-element's resolveType() method instead of the original
 * BlockScope.
 * @author gis
 */
public interface IProblemReporterWrapperFactory
{
	public abstract ProblemReporterWrapper create(ProblemReporter wrappee);
}
