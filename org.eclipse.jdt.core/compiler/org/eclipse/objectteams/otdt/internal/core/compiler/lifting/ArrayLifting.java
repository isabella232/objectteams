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
 * $Id: ArrayLifting.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * MIGRATION_STATE: complete.
 * moved here from org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.
 *
 * @author stephan
 * @version $Id: ArrayLifting.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ArrayLifting extends ArrayTranslations {

	/** API for StandardElementGenerator. */
	public MessageSend liftArray(
			BlockScope  scope,
			Expression  teamExpr,
			Expression  expression,
			TypeBinding requiredType)
	{
		this._teamExpr = teamExpr;
		return (MessageSend)translateArray(scope, expression, expression.resolvedType, requiredType, /*isLifting*/true);
	}

	/* implement hook. */
	Expression translation(
			Expression rhs,
			TypeBinding providedType,
			TypeBinding requiredType)
	{
		return Lifting.liftCall(this._scope, this._teamExpr, rhs, providedType, requiredType, false);
	}

}
