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
 * $Id: Flight.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.model;

import org.eclipse.objectteams.example.flightbooking.util.SegmentIterator;
import org.eclipse.objectteams.example.flightbooking.util.SegmentList;

/**
 * A flight which consists of a number of segments.
 */
public class Flight {
	/**
	 * The list of <code>Segment</code>s, this Flight consists of.
	 */
	private SegmentList _segments;

	/**
	 * Constructs a new <code>Flight</code>.
	 */
	public Flight() {
		_segments = new SegmentList();
	}

	/**
	 * This method adds a <code>Segment</code> to this <code>Flight</code>.
	 * 
	 * @param seg
	 *            The <code>Segment</code> to add.
	 */
	public void addSegment(Segment seg) {
		_segments.add(seg);
	}

	/**
	 * This method returns the price of this <code>Flight</code>, which
	 * equals the sum of all its <code>Segment</code>s' prices.
	 * 
	 * @return The price.
	 */
	public int getPrice() {
		int price = 0;
		for (SegmentIterator iter = _segments.getIterator(); iter.hasNext();) {
			price += iter.getNext().getPrice();
		}

		return price;
	}

	/**
	 * This method books a <code>Flight</code>.
	 * 
	 * @param pass
	 *            The <code>Passenger</code> who is booking.
	 */
	public void book(Passenger pass) throws BookingException {
		for (SegmentIterator iter = _segments.getIterator(); iter.hasNext();) {
			iter.getNext().book(pass);
		}
	}

	/**
	 * This method returns a String representation of this <code>Flight</code>.
	 * 
	 * @return The String. 
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(_segments.get(0).getStart());

		for (SegmentIterator iter = _segments.getIterator(); iter.hasNext();) {
			result.append(" --> ");
			result.append(iter.getNext().getDestination());
		}

		return result.toString();
	}
}