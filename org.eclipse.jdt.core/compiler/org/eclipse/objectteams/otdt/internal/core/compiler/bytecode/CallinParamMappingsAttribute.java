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
 * $Id: CallinParamMappingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;

/**
 * MIGRATION_STATE: complete.
 *
 * An integer list encoding the parameter postitions of a callin replace binding.
 * See AbstractMethodMappingDeclaration.recordPosition for explanation and an example.
 *
 * Represents the "CallinParamMappings" attribute.
 *
 * Location:
 * A callin wrapper method.
 *
 * Content:
 * A list of pairs: role method parameter index + base method parameter index.
 *
 * Purpose:
 * The OTRE uses this attribute while generating the base call and while weaving a replace callin.
 *
 * @author stephan
 * @version $Id: CallinParamMappingsAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class CallinParamMappingsAttribute extends ListValueAttribute {

	int[] positions;

	public CallinParamMappingsAttribute(CallinMappingDeclaration mappingDecl) {
		super(IOTConstants.CALLIN_PARAM_MAPPINGS, mappingDecl.positions.length, 2);
		this.positions = mappingDecl.positions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
	 */
	void writeElementValue(int i) {
		writeUnsignedShort(this.positions[i]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
	 */
	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
	 */
	@SuppressWarnings("nls")
	String toString(int i) {
		String result = new String(this._name)+"(";
		for (int j = 0; j < this.positions.length; j++) {
			result += this.positions[j]+",";
		}
		return result+")";
	}

}
