/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractMethodMappingDeclaration.java 19873 2009-04-13 16:51:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Stephan Herrmann
 **********************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;

/** 
 * Protocol for rechecking problems that were detected early during compilation
 * but might later turn out to be false alarms.
 * 
 * @author stephan
 * @since 1.3.0
 */
public interface IProblemRechecker {

	/**
	 * Answer whether a given problem should actually be reported.
	 * Implementing classes are responsible for remembering enough context
	 * so that checking can be performed.
	 * 
	 * @param foundIrritants if reporting is suppressed by a suppress warnings annotation,
	 *    the fact of suppression should be recorded in this array of irritant sets
	 *    (cf. {@link CompilationUnitDeclaration#finalizeProblems()}).
	 */
	boolean shouldBeReported(IrritantSet[] foundIrritants);

}
