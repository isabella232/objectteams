/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * Copyright 2010 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SelectionOnCallinMapping.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * 
 * @author stephan
 */
public class SelectionOnCallinMapping extends CallinMappingDeclaration {
	public SelectionOnCallinMapping(CallinMappingDeclaration callinMapping) {
		super(callinMapping.compilationResult);
		// transfer information as available after consumeCallinBindingLeft()
		this.declarationSourceStart = callinMapping.declarationSourceStart;
		this.sourceStart = callinMapping.sourceStart;
		this.sourceEnd = callinMapping.sourceEnd;
		this.name = callinMapping.name;
		this.roleMethodSpec = callinMapping.roleMethodSpec;
		this.callinModifier = callinMapping.callinModifier;
		this.hasSignature = callinMapping.hasSignature;
		this.javadoc = callinMapping.javadoc;
		this.annotations = callinMapping.annotations;
	}

	@Override
	public void resolveMethodSpecs(RoleModel role, ReferenceBinding baseType, boolean resolveBaseMethods) {
		super.resolveMethodSpecs(role, baseType, resolveBaseMethods); // FIXME(SH): checking
		throw new SelectionNodeFound(this.binding);
	}
}
