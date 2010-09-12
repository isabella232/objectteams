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
 * $Id: FlightBonus.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.fbapplication;

import org.eclipse.objectteams.example.bonussystem.Bonus;

import base org.eclipse.objectteams.example.flightbooking.model.Passenger;
import base org.eclipse.objectteams.example.flightbooking.model.Segment;

/**
 * This team is a specialization of the generic Bonus programme. 
 * It inherits all roles from its super-team, which are adapted
 * to the flightbooking domain in this team. 
 */
public team class FlightBonus extends Bonus {

	/**
	 * Register a passenger as subscriber of this bonus programme. This creates
	 * a Subscriber role for the given Passenger object and makes the declared
	 * callin method binding effective for it.
	 */
    public FlightBonus(Passenger as Subscriber s) {
        activate();
    }

	/**
	 * In flight booking our subscribers are passengers.
	 */ 
	public class Subscriber playedBy Passenger 
		// adapt only registered passengers (guard predicate):
		base when (FlightBonus.this.hasRole(base)) 
	{
		// Callin method binding:
		buy     <- replace book;

		// Callout method binding
		String getName() -> String getName();
	};

	/**
	 * Credits are associated with each segment of a flight.
	 */
	public class Item playedBy Segment {
		
		/**
		 * Implement the air line's strategy: For each started 1000 miles give
		 * 1000 points.
		 */
		public int calculateCredit () {
			int miles = getDistance();
			int credit = ((miles+999)/1000) * 1000; // round up
			return credit;
		};

		
		// Callin method binding:
		earnCredit     <- after book;

		// Callout method binding: forwarding to base method
		String getDestination() -> String getDestination();

		int getDistance() -> int getDistance();

		String getStart() -> String getStart();
	}
}
