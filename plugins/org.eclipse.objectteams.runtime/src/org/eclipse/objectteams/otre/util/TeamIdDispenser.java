/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamIdDispenser.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;
import java.util.*;

public class TeamIdDispenser {

	private static Map<ClassLoader, TeamIdDispenser> instances = new HashMap<ClassLoader, TeamIdDispenser>();
    private static TeamIdDispenser defaultInstance = new TeamIdDispenser();
    
    static int lastDispensedId = 0;
    private static HashMap<String, Integer> teamIDs = new HashMap<String, Integer>();

//    @SuppressWarnings("unchecked")
	private static int produceNextTeamId(String team_name) {
        lastDispensedId++;
        Integer teamId = Integer.valueOf(lastDispensedId);
        teamIDs.put(team_name, teamId);
        return lastDispensedId;
    }

    public static int getTeamId(String class_name) {
        Integer teamId = teamIDs.get(class_name);
        if (teamId != null)
        	// the team <class_name> already has a team-id assigned
        	return teamId.intValue();
		else return produceNextTeamId(class_name);
    }
    
    // Data shared among different transformers of the same class loader:
    // REFACTOR: move the following to a better place:
    private ArrayList<String> clinitAddedClasses = new ArrayList<String>();
    public static boolean clinitAdded(String class_name, ClassLoader loader) {
    	TeamIdDispenser instance = getInstanceForLoader(loader);
    	if (instance.clinitAddedClasses.contains(class_name))
    		return true;
		
    	instance.clinitAddedClasses.add(class_name);
		return false;	
    }
    
	/**
	 * Since actual data are stored in an instance, static methods need to retrieve the appropriate
     * instance regarding the given class loader.
	 */
	private static TeamIdDispenser getInstanceForLoader(ClassLoader loader) {
		if (loader == null)
			return defaultInstance;
		
		TeamIdDispenser instance = instances.get(loader);
		if (instance == null)
			instances.put(loader, instance = new TeamIdDispenser());
		return instance;
	}
}
