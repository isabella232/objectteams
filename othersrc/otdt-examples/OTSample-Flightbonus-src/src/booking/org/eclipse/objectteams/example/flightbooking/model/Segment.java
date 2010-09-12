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
 * $Id: Segment.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.model;

import org.eclipse.objectteams.example.flightbooking.data.DistanceManager;

/**
 * A <code>Segment</code> is part of a <code>Flight</code>. Different
 * <code>Flight</code>s may share common <code>Segment</code>s.
 */
public class Segment {
	/**
	 * This <code>Segment</code>'s start.
	 */
	private String _start;

	/**
	 * This <code>Segment</code>'s destination.
	 */
	private String _destination;

	/**
	 * This <code>Segment</code>'s date.
	 */
	private int _date;

	/**
	 * The number of available seats for this <code>Segment</code>.
	 */
	private int _seatsAvail;

	/**
	 * This <code>Segment</code>'s price.
	 */
	private int _price;

	/**
	 * Constructs a new <code>Segment</code>.
	 */
	public Segment(String start, String dest, int date, int seats, int price) {
		_start = start;
		_destination = dest;
		_date = date;
		_seatsAvail = seats;
		_price = price;
	}

	/**
	 * This method returns this <code>Segment</code>'s start.
	 */
	public String getStart() {
		return _start;
	}

	/**
	 * This method returns this <code>Segment</code>'s destination.
	 */
	public String getDestination() {
		return _destination;
	}

	/**
	 * This method returns this <code>Segment</code>'s date.
	 */
	public int getDate() {
		return _date;
	}

	/**
	 * This method returns the number of this <code>Segment</code>'s
	 * available seats.
	 */
	public int getAvailableSeats() {
		return _seatsAvail;
	}

	/**
	 * This method returns this <code>Segment</code>'s price.
	 */
	public int getPrice() {
		return _price;
	}

	public void book(Passenger pass) throws BookingException {
		if (_seatsAvail <= 0) {
			throw new BookingException("Segment overbooked!");
		}
		reserveSeat(pass);
	}

	private void reserveSeat(Passenger pass) {
		// reserve a seat for the passenger
		_seatsAvail--;
	}

	/**
	 * This method returns the distance between this <code>Segment</code>'s
	 * start and its destination.
	 * 
	 * @return The distance.
	 */
	public int getDistance() {
		return DistanceManager.getSingleton().getDistance(_start, _destination);
	}
}