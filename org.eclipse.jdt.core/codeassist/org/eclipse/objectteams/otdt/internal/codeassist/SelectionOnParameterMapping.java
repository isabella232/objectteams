/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2008 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SelectionOnTSuperReference.java 14411 2006-09-23 03:20:06Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;


import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ParameterMapping;

/** Selection on the ident of a parameter mapping. */
public class SelectionOnParameterMapping extends ParameterMapping
{
	public SelectionOnParameterMapping(ParameterMapping orig) {
		super(orig.direction, orig.expression, orig.ident);
	}
	@Override
	protected void argumentsResolved(MethodSpec spec) {
		super.argumentsResolved(spec);
		if (this.ident.binding != null)
			throw new SelectionNodeFound(this.ident.binding);
	}
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("<SelectOnParameterMapping:"); //$NON-NLS-1$
		return super.printExpression(0, output).append('>');
	}
}
