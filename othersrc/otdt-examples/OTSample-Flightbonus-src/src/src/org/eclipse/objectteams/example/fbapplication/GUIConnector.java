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
 * $Id: GUIConnector.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.fbapplication;

import java.awt.Component;

import base org.eclipse.objectteams.example.flightbooking.gui.FlightBookingGUI;
import base org.eclipse.objectteams.example.flightbooking.model.PassengerDB;

/**
 * This team class (connector) connects the 
 */
public team class GUIConnector extends BonusGUI {
	
	
    protected class View playedBy FlightBookingGUI {
    	protected Component getComponent () {
    		return this; // implicit lowering, the base object conforms to Component.
    	}    
    	
    	registerView                 <- after  initComponents;
    }
    
    protected class Controller playedBy PassengerDB {
		//------------------------------------------------------
		// Bootstrap callin method binding:
		queryRegisterForBonus        <- after  add;
		//-------------------------------------------------------
    }
}
