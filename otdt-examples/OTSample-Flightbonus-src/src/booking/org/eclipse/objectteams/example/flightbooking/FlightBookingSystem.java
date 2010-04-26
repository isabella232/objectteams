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
 * $Id: FlightBookingSystem.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking;

import org.eclipse.objectteams.example.flightbooking.model.BookingException;
import org.eclipse.objectteams.example.flightbooking.model.Flight;
import org.eclipse.objectteams.example.flightbooking.model.Passenger;
import org.eclipse.objectteams.example.flightbooking.model.PassengerDB;
import org.eclipse.objectteams.example.flightbooking.util.FlightIterator;
import org.eclipse.objectteams.example.flightbooking.util.FlightList;

/**
 * This class represents a flight booking system.
 */
public class FlightBookingSystem {
	/**
	 * The list of bookable <code>Flight</code>s.
	 */
	private FlightList _flights = new FlightList();

	/**
	 * The list of registered <code>Passenger</code>s.
	 */
	private PassengerDB _passengers = new PassengerDB();

	/**
	 * This is the <code>Passenger</code> who is currently active.
	 */
	private Passenger _currentPassenger;

	public FlightBookingSystem() {
		super();
	}

	/**
	 * This method registers a new <code>Passenger</code>.
	 * 
	 * @param name
	 *            The <code>Passenger</code>'s name.
	 * @param budget
	 *            The <code>Passenger</code>'s budget.
	 * @return <code>true</code> if the <code>Passenger</code> is already
	 *         known to the booking system, <code>false</code> otherwise.
	 */
	public boolean registerPassenger(String name, int budget) {
		if (_passengers.contains(name)) {
			return false;
		}
		_passengers.add(new Passenger(name, budget));

		return true;
	}

	/**
	 * This method adds a <code>Flight</code> to the booking system.
	 * 
	 * @param flight
	 *            The <code>Flight</code> to add.
	 */
	public void addFlight(Flight flight) {
		_flights.add(flight);
	}

	/**
	 * This method removes a <code>Flight</code> from the booking system.
	 * 
	 * @param flight
	 *            The <code>Flight</code> to remove.
	 */
	public void removeFlight(Flight flight) {
		_flights.remove(flight);
	}

	/**
	 * This method returns a <code>FlightIterator</code> of all offered
	 * <code>Flight</code>s.
	 * 
	 * @return The flights.
	 */
	public FlightIterator getOfferedFlights() {
		return _flights.getIterator();
	}

	/**
	 * This method books a certain <code>Flight</code> for the active
	 * <code>Passenger</code>.
	 * 
	 * @param idx
	 *            The number of the flight to book.
	 */
	public void bookFlight(int idx) throws BookingException {
		if (_currentPassenger == null) {
			throw new BookingException("No current passenger");
		}
		_currentPassenger.book(_flights.get(idx));
	}

	/**
	 * This method activates a <code>Passenger</code>.
	 * 
	 * @param name
	 *            The name of the <code>Passenger</code> to activate.
	 * @return <code>true</code> if the <code>Passenger</code> was found in
	 *         the booking system and there is currently no other active
	 *         <code>Passenger</code>,<code>false</code> otherwise.
	 */
	public boolean setCurrentPassenger(String name) {
		if (_passengers.contains(name) && (_currentPassenger == null)) {
			_currentPassenger = _passengers.get(name);
			return true;
		}
		return false;
	}

	/**
	 * This method deactivates a <code>Passenger</code>.
	 * 
	 * @param name
	 *            The name of the <code>Passenger</code> to deactivate.
	 * @return <code>true</code> if the <code>Passenger</code> was found in
	 *         the booking system and he really is the active
	 *         <code>Passenger</code>,<code>false</code> otherwise.
	 */
	public boolean disableCurrentPassenger(String name) {
		if (_passengers.contains(name)
				&& (_currentPassenger.getName().equals(name))) {
			_currentPassenger = null;
			return true;
		}
		return false;
	}

	/**
	 * This method returns the current <code>Passenger</code>.
	 * 
	 * @return The current <code>Passenger</code>.
	 */
	public Passenger getCurrentPassenger() {
		return _currentPassenger;
	}

	/**
	 * This method searches for a <code>Passenger</code> in the booking
	 * system.
	 * 
	 * @return <code>true</code> if the <code>Passenger</code> was found,
	 *         <code>false</code> otherwise.
	 */
	public boolean containsPassenger(String name) {
		return _passengers.contains(name);
	}

	/**
	 * Get the passengers list for iteration.
	 * 
	 * @return
	 */
	public PassengerDB getRegisteredPassengers() {
		_passengers.reset();
		return _passengers;
	}
	
	/**
	 * This method ends the program.
	 */
	public void exit() {
		System.exit(1);
	}

}