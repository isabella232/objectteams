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
 * $Id: BlockScopeWrapper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.problem;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Special Scope for special AST elements, e.g. wrapped MessageSends,
 * wrapping a normal scope.
 * Necessary for catching and redirectoring error-reports to a special ProblemReporter
 * taking care that the error-reports do not point to a generated method, but
 * e.g. to a calling binding.
 *
 * @author carp
 */
public class BlockScopeWrapper extends BlockScope
{
	private ProblemReporterWrapper _problemWrapper = null;
	private IProblemReporterWrapperFactory _problemReporterFactory = null;

	public BlockScopeWrapper(BlockScope parentScope, IProblemReporterWrapperFactory factory)
	{
		super(parentScope);
		this._problemReporterFactory = factory;
	}

	/*
	 * create a special problem reporter
	 * @return a problem-reporter wrapper
	 */
	public ProblemReporter problemReporter()
	{
		// a new instance for each problem for setting referenceContext.
		this._problemWrapper = this._problemReporterFactory.create(super.problemReporter());
		return this._problemWrapper;
	}
}
