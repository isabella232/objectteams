/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISearchFilter.java 23434 2010-02-03 23:52:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.dialogs;

import org.eclipse.objectteams.otdt.core.IOTType;

/**
 * @author gis
 */
public interface ISearchFilter
{
    /**
     * Gets an array of OT types as input and is supposed
     * to return a new filtered array of OT Types that should
     * be used instead of the given one.
     * 
     * May return the (modified or not) original array.
     */
    public IOTType[] filterTypes(IOTType[] types);

}
