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
 * $Id: BonusGUI.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.fbapplication;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.eclipse.objectteams.example.flightbooking.model.Passenger;

/**
 * This team gives an unspecific, partially abstract implementation of a GUI
 * for a bonus programme. 
 * Please also see its nested team FlightBonusDialog
 * (since FlightBonusDialog is a role file, you may try pressing ctrl-o twice for navigation).
 * FlightBonusDialog already connects to FlightBonus.
 * However, no bindings to the flight booking system are given at this level.
 */
public team class BonusGUI {

	/** This is a reference to the active GUI. */
	View view = null;

	/**
	 * Make the GUI accessible and extend it by a registration question.
	 */    
    abstract protected class View  {

    	void registerView() {
    		BonusGUI.this.view = this;
    	}

    	protected boolean queryRegisterDialog() {
    		int choice = JOptionPane.showConfirmDialog(
			    				getComponent(), 
								"Register for Flight Bonus?", 
			    				"OT Bonus System", 
								JOptionPane.YES_NO_OPTION);
    		
    		return choice == JOptionPane.YES_OPTION;
    	}   

    	/**
    	 * Expected method: get an AWT component as parent for new windows.
    	 */
    	protected abstract Component getComponent();
    }
	
			
	/**
	 * This role is used as a hook into PassengerList.
	 */
    protected class Controller 
		when (BonusGUI.this.view != null) // don't receive callin triggers before proper initialization
	{
    	/**
		 * Given a passenger, ask if he/she should participate in the
		 * bonus programme.
		 */
		void queryRegisterForBonus (Passenger p) {
			if (BonusGUI.this.view.queryRegisterDialog()) 
				new FlightBonusDialog(new FlightBonus(p));
		};
    }    
}

