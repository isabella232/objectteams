/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DistanceManager.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.data;


/**
 * The distance manager is used to manage the distances between
 * different cities. It implements the Singleton pattern.
 */
public class DistanceManager 
{
    private DistanceTable _distanceTable = new DistanceTable();

	/**
	 * Some exemplary distances are stored in the distance table.
	 */
	private DistanceManager() 
	{
		_distanceTable.addPlace("Berlin");
		_distanceTable.getPlaceDistances("Berlin").addDistance("Frankfurt", 424);
		_distanceTable.getPlaceDistances("Berlin").addDistance("Hamburg", 252);
        _distanceTable.getPlaceDistances("Berlin").addDistance("Brussels", 653);
        _distanceTable.getPlaceDistances("Berlin").addDistance("London", 934);
        _distanceTable.getPlaceDistances("Berlin").addDistance("Amsterdam", 576);

		_distanceTable.addPlace("Frankfurt");
		_distanceTable.getPlaceDistances("Frankfurt").addDistance("New York", 6194);
		_distanceTable.getPlaceDistances("Frankfurt").addDistance("Atlanta", 7396);
		_distanceTable.getPlaceDistances("Frankfurt").addDistance("Chicago", 6960);
        
        _distanceTable.addPlace("Brussels");
        _distanceTable.getPlaceDistances("Brussels").addDistance("Chicago", 6657);
        _distanceTable.addPlace("London");
        _distanceTable.getPlaceDistances("London").addDistance("Chicago", 6347);
        _distanceTable.getPlaceDistances("London").addDistance("Berlin", 934);
        _distanceTable.addPlace("Amsterdam");
        _distanceTable.getPlaceDistances("Amsterdam").addDistance("Chicago", 6605);
	}

//	 TODO(mkr): add javadoc
	public int getDistance(String place1, String place2) 
	{
		if (_distanceTable.containsPlace(place1)) 
		{
			return _distanceTable.getPlaceDistances(place1).getDistance(place2);
		}
		if (_distanceTable.containsPlace(place2)) 
		{
			return _distanceTable.getPlaceDistances(place2).getDistance(place1);
		}
		return DistanceTable.NO_DISTANCE;
	}

	/**
	 * This method returns the singleton object.
	 * @return The <code>DistanceManager</code>.
	 */
	public static DistanceManager getSingleton() {
		return new DistanceManager();
	}
}