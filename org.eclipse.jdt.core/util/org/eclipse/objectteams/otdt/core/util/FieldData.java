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
 * $Id: FieldData.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.util;

import org.eclipse.jdt.core.Signature;

/**
 * @author brcan
 *
 */
public class FieldData
{
    private String _selector;
    //attention: this field seems to use the constant pool encoding of types
    private String _fieldType;
    
    private boolean _isSetter;

    public FieldData(String selector, String fieldType, boolean isSetter)
    {
        _selector   = selector;
        _fieldType = fieldType;
        _isSetter   = isSetter;
    }
    
    public String getSelector()
    {
        return _selector;
    }
    
    public String getFieldType()
    {
        return _fieldType;
    }
    
    public boolean isSetter() 
    {
    	return _isSetter;
    }
    
    public String toString()
    {
        return Signature.getSimpleName(Signature.toString(_fieldType)) + " " + _selector; //$NON-NLS-1$
    }
}

