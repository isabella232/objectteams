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
 * $Id: CompletionOnFieldAccessSpec.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.complete.CompletionNodeFound;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;

/**
 * A FieldAccessSpec at the right hand side of a method mapping is a good candidate
 * for completion: we already know the receiver type (baseclass).
 *
 * @author stephan
 * @version $Id: CompletionOnFieldAccessSpec.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CompletionOnFieldAccessSpec extends FieldAccessSpec {

	public CompletionOnFieldAccessSpec(char[] ident, TypeReference type, long pos, int modifiers) {
		super(ident, type, pos, modifiers);
	}


	public CompletionOnFieldAccessSpec(char[] ident, long pos, boolean isSetter) {
		super(ident, pos, isSetter);
	}


	@Override
	public void resolveFeature(ReferenceBinding receiverType, BlockScope scope, boolean callinExpected, boolean isBaseSide, boolean allowEnclosing)
	{
		throw new CompletionNodeFound(this, receiverType, scope);
	}

	@Override
	public void resolveTypes(CallinCalloutScope scope, boolean isBaseSide) {
		// need to resolve return type at least:
		super.resolveTypes(scope, isBaseSide);

		ReferenceBinding enclosing = scope.enclosingReceiverType();
		ReferenceBinding baseclass = null;
		if (enclosing != null && enclosing.isRole())
			baseclass = enclosing.baseclass();
		throw new CompletionNodeFound(this, baseclass, scope);
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		output.append("<CompleteOnFieldAccessSpec:"); //$NON-NLS-1$
		super.print(indent, output);
		output.append('>');
		return output;
	}
}
