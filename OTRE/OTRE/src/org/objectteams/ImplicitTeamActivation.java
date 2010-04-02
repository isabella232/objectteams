/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 			Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.objectteams;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marker annotation enables implicit team activation for the annotated element:
 * <ul>
 * <li>If attached to a method the effect is that each call to this method implicitly
 *     activates the enclosing team.</li>
 * <li>If attached to a class it has the same effect as annotating all contained methods.</li>
 * </ul>
 * See <a href="http://www.objectteams.org/def/1.3/s5.html#s5.3">OTJLD § 5.3</a>.
 * <p>
 * This annotation is only evaluated if the property <code>ot.implicit.team.activation</code>
 * is set to the string <code>ANNOTATED</code>.
 * </p>
 * @author stephan
 * @since 1.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ImplicitTeamActivation {
	/* no members, pure marker annotation. */
}
