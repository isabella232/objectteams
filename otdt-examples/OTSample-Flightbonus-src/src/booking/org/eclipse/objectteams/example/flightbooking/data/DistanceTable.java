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
 * $Id: DistanceTable.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.data;

import java.util.Hashtable;

/**
 * This class represents a table of distances between different cities.
 * It is used by the <code>DistanceManager</code>
 * to store and retrieve those distances.
 */
public class DistanceTable {
	public final static int NO_DISTANCE = 0;

	private Hashtable<String, DistanceList> _data = new Hashtable<String, DistanceList>();

	/**
	 * A list of distances.
	 */
	public class DistanceList {
		private Hashtable<String, Integer> _distances = new Hashtable<String, Integer>();

		public void addDistance(String placeName, int distance) {
			_distances.put(placeName, new Integer(distance));
		}

		public int getDistance(String placeName) {
			return _distances.containsKey(placeName) ? _distances
					.get(placeName).intValue() : NO_DISTANCE;
		}
	}

	public DistanceTable() {
		super();
	}

	public void addPlace(String name) {
		_data.put(name, new DistanceList());
	}

	public DistanceList getPlaceDistances(String name) {
		return _data.get(name);
	}

	public boolean containsPlace(String name) {
		return _data.containsKey(name);
	}
}