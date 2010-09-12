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
 * $Id: Main.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.flightbooking;

import org.eclipse.objectteams.example.flightbooking.gui.FlightBookingGUI;
import org.eclipse.objectteams.example.flightbooking.model.Flight;
import org.eclipse.objectteams.example.flightbooking.model.Segment;

// ## Attention: the current version has a few publics which should
// not be needed, but callout still requires regular visibility.
/**
 * This class contains the main method.
 */
public class Main {
	final private FlightBookingSystem _system;

	// ## added to give access to _system
	/**
	 * This method returns the associated <code>FlightBookingSystem</code>.
	 * 
	 * @return The code>FlightBookingSystem</code>.
	 */
	public FlightBookingSystem getSystem() {
		return _system;
	}

	/**
	 * Contructs a new <code>Main</code> object.
	 */
	public Main() {
		_system = new FlightBookingSystem();
	}
	
	public void start() {
		initFlights();
		providePassengers();
						

		java.awt.EventQueue.invokeLater(new Runnable() {
	           	public void run() {
	           		FlightBookingGUI fbgui = new FlightBookingGUI(_system);
	           		fbgui.setVisible(true);
	           }
	    	});
	}

	/**
	 * This is the main method that is used to start the flight booking example.
	 * 
	 * @param args
	 *            No args.
	 */
	public static void main(String[] args) {
		final Main main = new Main();
		main.start();
	}

	private void initFlights() {
		Flight berlinNY = new Flight();
		berlinNY.addSegment(new Segment("Berlin", "Frankfurt", 903, 150, 400));
		berlinNY.addSegment(new Segment("Frankfurt", "New York", 903, 350, 1400));

		Flight berlinHH = new Flight();
		berlinHH.addSegment(new Segment("Berlin", "Hamburg", 1105, 100, 99));
		
		Flight berlinLDN = new Flight();
		berlinLDN.addSegment(new Segment("Berlin", "London", 937, 120, 125));
		berlinLDN.addSegment(new Segment("London", "Berlin", 938, 120, 135));
		
		Flight chicago1 = new Flight();
		chicago1.addSegment(new Segment("Berlin", "Amsterdam", 342, 60, 213));
		chicago1.addSegment(new Segment("Amsterdam", "Chicago", 342, 260, 313));

		Flight chicago2 = new Flight();
		chicago2.addSegment(new Segment("Berlin", "Frankfurt", 342, 60, 113));
		chicago2.addSegment(new Segment("Frankfurt", "Chicago", 342, 260, 316));

		_system.addFlight(berlinNY);
		_system.addFlight(berlinHH);
		_system.addFlight(berlinLDN);
		_system.addFlight(chicago1);
		_system.addFlight(chicago2);
	}

	private void providePassengers() {
		_system.registerPassenger("Carsten", 16000);
		_system.registerPassenger("Jan", 100);
		_system.registerPassenger("Stephan", 16000);
	}
}