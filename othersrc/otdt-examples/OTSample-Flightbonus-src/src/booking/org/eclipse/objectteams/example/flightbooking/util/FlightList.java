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
 * $Id: FlightList.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.util;

import java.util.Vector;
import java.util.Enumeration;

import org.eclipse.objectteams.example.flightbooking.model.Flight;
import org.eclipse.objectteams.example.flightbooking.util.FlightIterator;


/**
 * This class implements a list of <code>Flight</code>s.
 */
public class FlightList {
	private Vector<Flight> _data;

	public FlightList() {
		_data = new Vector<Flight>();
	}

	public void add(Flight flight) {
		if (!_data.contains(flight)) {
			_data.add(flight);
		}
	}

	public void remove(Flight flight) {
		_data.remove(flight);
	}

	public Flight get(int idx) {
		return _data.get(idx);
	}

	public FlightIterator getIterator() {
		final Enumeration<Flight> rawIter = _data.elements();

		return new FlightIterator() {

			public boolean hasNext() {
				return rawIter.hasMoreElements();
			}

			public Flight getNext() {
				return rawIter.nextElement();
			}

			public int getLength() {
				return _data.size();
			}
		};
	}
}