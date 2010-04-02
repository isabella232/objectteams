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
 * $Id: ModelElement.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.CallinMethodMappingsAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementor;

/**
 * MIGRATION_STATE: complete.
 *
 * @author stephan
 * @version $Id: ModelElement.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class ModelElement {
    /** OT-specific bytecode attributes for this model element. */
    public AbstractAttribute[] _attributes;


    /** Add an OT-specific bytecode attribute. */
    public void addAttribute (AbstractAttribute attr)
    {
        if (this._attributes == null)
        {
            this._attributes = new AbstractAttribute[]{attr};
        }
        else
        {
            int len = this._attributes.length;
            AbstractAttribute[] newAttrs = new AbstractAttribute[len+1];
            System.arraycopy(this._attributes, 0, newAttrs, 0, len);
            newAttrs[len] = attr;
            this._attributes = newAttrs;
        }
    }

    public void addOrMergeAttribute (AbstractAttribute attr) {
    	AbstractAttribute existingAttr = null;
    	if (this._attributes != null) {
	    	for (int i = 0; i < this._attributes.length; i++) {
	    		if (this._attributes[i].nameEquals(attr)) {
	    			existingAttr = this._attributes[i];
	    			break;
	    		}
	    	}
		}
    	if (existingAttr == null) {
    		if (!attr.nameEquals(IOTConstants.CALLIN_METHOD_MAPPINGS))
    		{
    			addAttribute(attr);
    			return;
    		}
    		addAttribute(existingAttr = new CallinMethodMappingsAttribute(new CallinMappingDeclaration[0]));
    	}
		// this call also creates bindings from the attribute:
    	existingAttr.merge(this, attr);
    	if (attr.nameEquals(IOTConstants.CALLIN_METHOD_MAPPINGS))
    		CallinImplementor.checkCopyCallinBinding((CallinMethodMappingsAttribute)attr, this);
    }

    /** Find an attribute by its name, return null if not present. */
    public AbstractAttribute getAttribute(char[] name) {
    	if (this._attributes == null)
    		return null;
    	for (AbstractAttribute attr : this._attributes) {
			if (attr.nameEquals(name))
				return attr;
		}
    	return null;
    }
    /**
     * Write all OT-specific attributes of a given type binding to the given class file
     * @param type the element whose attributes should be written
     * @param file the destination class file
     * @return number of attributes written
     */
    public static int writeAttributes(ReferenceBinding type, ClassFile file)
    {
        int count = 0;
        if (type.isTeam())
            count += type.getTeamModel().writeAttributes(file);
        if (type.isRole() && (type.roleModel != null))
            count += type.roleModel.writeAttributes(file);
        if (type.model != null)
            count += type.model.writeAttributes(file);
        return count;
    }

    /**
     * Write all OT-specific attributes of this model element to the given class file.
     * @param file
     * @return number of attributes written
     */
    public int writeAttributes(ClassFile file)
    {
        if (this._attributes == null)
            return 0;
        int count = 0;
        for (int i=0; i<this._attributes.length; i++)
        	if (this._attributes[i].setupForWriting()) {
        		this._attributes[i].write(file);
        		count++;
        	}
        return count;
    }

    public static void evaluateLateAttributes(ReferenceBinding type, int state)
    {
        if (type.isTeam())
            type.getTeamModel().evaluateLateAttributes(state);
        if (type.isRole() && (type.roleModel != null)) {
            type.roleModel.evaluateLateAttributes(state);
            // method mappings in role interfaces need to be implemented them in sub-classes:
            type.roleModel.implementMethodBindingsFromSuperinterfaces();
        }
        /*
        if (type.model != null)
            count += type.model.evaluateLateAttributes();
        */
    }
}
