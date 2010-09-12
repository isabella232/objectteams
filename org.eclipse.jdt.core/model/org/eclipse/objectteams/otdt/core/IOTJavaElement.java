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
 * $Id: IOTJavaElement.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Generic ObjectTeams JavaModel extension element
 *
 * @author jwloka
 * @version $Id: IOTJavaElement.java 23416 2010-02-03 19:59:31Z stephan $
 */
public interface IOTJavaElement extends IJavaElement
{
	/** OTElement type identifier for a Team */
//{OTModelUpdate : types had been defined from 0x01 to 0x04, this doesn't 
// work for the updated model since the result of getType() is in some  
// places interpreted as a JavaElement type  
//orig:	public int TEAM            = 0x01;
	public int TEAM            = 100;

	/** OTElement type identifier for a Role */
//orig: public int ROLE            = 0x02;
	public int ROLE            = 101;

	/** OTElement type identifier for a Callin Mapping */
//orig:	public int CALLIN_MAPPING  = 0x03;
	public int CALLIN_MAPPING  = 102;
	
	/** OTElement type identifier for a Callout Mapping */
//orig:	public int CALLOUT_MAPPING = 0x04;	
	public int CALLOUT_MAPPING = 103;
    
    /** OTElement type identifier for a Callout To Field Mapping */
    public int CALLOUT_TO_FIELD_MAPPING = 104;
//haebor, gbr}

	/**
	 * Get elements simple name e.g. getElementName() for "my.namespace.MyTeam.MyRole"
	 * returns "MyRole" 
	 * @return simple name (unqualified by package or enclosing type)
	 */
	public String getElementName();

	/**
	 * Get elements type identifier. Convenience method to reduce instanceof
	 * checking.
	 * @return possible values defined above
	 */
	public int getElementType();
	
	/**
	 * Returns the corresponding element from the JavaModel.
	 * 
	 * @return linked java element
	 */
	public IJavaElement getCorrespondingJavaElement();
	
	public void toString(int tab, StringBuffer buffer);
}
