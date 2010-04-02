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
 * $Id: ArrayLowering.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;


/**
 * MIGRATION_STATE: complete.
 * moved here from org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.
 *
 * @author stephan
 * @version $Id: ArrayLowering.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ArrayLowering extends ArrayTranslations {

	/** API for Lowering. */
	Expression lowerArray(
			BlockScope  scope,
			Expression  expression,
			TypeBinding providedType,
			TypeBinding requiredType)
	{
		// TODO (SH): check if we need to use the team anchor of a RoleTypeBinding
		//            as receiver for the translation call.
		ReferenceBinding teamBinding = ((ReferenceBinding)providedType.leafComponentType()).enclosingType();
		this._teamExpr = new AstGenerator(expression).qualifiedThisReference(teamBinding);
		this._teamExpr.resolveType(scope);
		return translateArray(scope, expression, providedType, requiredType, /*isLifting*/false);
	}

	/* implement hook. */
	Expression translation(Expression rhs, TypeBinding providedType, TypeBinding requiredType) {
		return new Lowering().lowerExpression(this._scope, rhs, providedType, requiredType, false);
	}
}
