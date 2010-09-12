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
 * $Id: IOTType.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * OTM Type with Team/Role support and link to a corresponding JavaModel element
 *
 * @author jwloka
 * @version $Id: IOTType.java 23416 2010-02-03 19:59:31Z stephan $
 */
public interface IOTType extends IOTJavaElement, IType
{
    /**
     * Used for getRoleType(int) to indicate which role classes should be returned.
     */
	public static final int INLINED = 1;
    /**
     * Used for getRoleType(int) to indicate which role classes should be returned.
     */
	public static final int ROLEFILE = 2;
	/**
	 * If the given team is also role, traverse its implicit super hierarchy and 
	 * include the roles of all implicit super teams
	 */
	public static final int IMPLICTLY_INHERITED = 4;
	/**
	 * Traverse the explicit super hierarchy and include the roles of all explicit super teams
	 */
	public static final int EXPLICITLY_INHERITED = 8;
	/**
	 * Only search the roles in the super hierarchy. Must be combined with 
	 * EXPLICITLY_INHERITED or IMPLICITLY_INHERITED
	 */
	public static final int EXCLUDE_SELF = 16;
	/**
	 * Search in the given team and all implicit and explicit super teams.
	 * Equivalent to
	 * INLINED | ROLEFILE | IMPLICTLY_INHERITED | EXPLICITLY_INHERITED;
	 */
	public static final int ALL = INLINED | ROLEFILE | IMPLICTLY_INHERITED | EXPLICITLY_INHERITED;
	
	/**
	 * Indicates whether this type is a role or not 
	 */
	public boolean isRole();

	/**
	 * Indicates whether this type is a team or not 
	 */
	public boolean isTeam();

	/**
	 * Returns the flags from parsing. These flags provide additional
	 * information about the type's modifiers e.g. IConstants.AccTeam
	 * @see Flags
	 */	
	public int getFlags();

	/**
	 * Returns all types defined in this type.
	 * @return array of IType's or an empty array   
	 */
	public IType[] getInnerTypes();
	
	/**
	 * Returns the role type associated conceptually contained by this team type
	 * or null.
	 * @param simpleName - the name of the desired role
	 */
	public IType getRoleType(String simpleName);
	
	/**
	 * Similar to getRoleType(String), but directly use the search engine
	 * thus avoiding exists() test on a non-existent ROFI type.
	 */
	public IType searchRoleType(String simpleName);

    /**
	 * Returns all role types (inlined and role files) contained by this team.
	 */
	public IType[] getRoleTypes() throws JavaModelException;
	
	/**
	 * Returns all roles of this team. Either the inlined, the role files or both are 
	 * returned.
	 * See {@link IOTType#getRoleTypes()} for gathering all role types.
	 * 
	 * @param which an ORed combination of IOTType.INLINED, IOTType.ROLEFILE, IOTType.IMPLICITLY_INHERITED,
	 * IOTType.EXPLICITLY_INHERITED and IOTType.EXCLUDE_SELF
	 */
	public IType[] getRoleTypes(int which) throws JavaModelException;
	
	/**
	 * Returns roles named roleName of this team. Either the inlined, the role files or both are 
	 * returned.
	 * See {@link IOTType#getRoleTypes()} for gathering all role types.
	 * 
	 * @param which an ORed combination of IOTType.INLINED, IOTType.ROLEFILE, IOTType.IMPLICITLY_INHERITED,
	 * IOTType.EXPLICITLY_INHERITED and IOTType.EXCLUDE_SELF
	 */
	public IType[] getRoleTypes(int which, String roleName) throws JavaModelException;

}
