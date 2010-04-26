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
 * $Id: Passenger.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking.model;

/**
 * This class represents a passenger who has a name, a budget and who is able to
 * book flights.
 */
public class Passenger {
	private String _name;

	private int _budget;

	/**
	 * Constructs a new <code>Passenger</code>.
	 * 
	 * @param name
	 *            The <code>Passenger</code>'s name.
	 */
	public Passenger(String name) {
		_name = name;
		_budget = 0;
	}

	/**
	 * Constructs a new <code>Passenger</code>.
	 * 
	 * @param name
	 *            The <code>Passenger</code>'s name.
	 * @param budget
	 *            The <code>Passenger</code>'s budget.
	 */
	public Passenger(String name, int budget) {
		_name = name;
		_budget = budget;
	}

	/**
	 * This method implements the booking of a <code>Flight</code>.
	 * 
	 * @param flight
	 *            The <code>Flight</code> to book.
	 * @throws BookingException
	 *             Thrown if the <code>Passenger</code> has not enough money
	 *             to book the given <code>Flight</code>.
	 */
	public void book(Flight flight) throws BookingException {
		int price = flight.getPrice();

		if (_budget < price) {
			throw new BookingException("Passenger " + getName()
					+ " hasn't enough money!");
		}

		_budget -= price;

		flight.book(this);
	}

	/**
	 * This method returns the <code>Passenger</code>'s budget.
	 * 
	 * @return The budget.
	 */
	public int getBudget() {
		return _budget;
	}

	/**
	 * This method increases the <code>Passenger</code>'s budget by a given
	 * amount.
	 * 
	 * @param amount
	 *            The amount to increase the budget by.
	 */
	public void earn(int amount) {
		_budget += amount;
	}

	/**
	 * This is a setter for the <code>Passenger</code>'s budget.
	 * 
	 * @param bud
	 *            The budget.
	 */
	public void setBudget(int bud) {
		_budget = bud;
	}

	/**
	 * This method returns a String representation of this
	 * <code>Passenger</code>.
	 */
	public String toString() {
		return _name;
	}

	/**
	 * This method returns the <code>Passenger</code>'s name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return _name;
	}
}