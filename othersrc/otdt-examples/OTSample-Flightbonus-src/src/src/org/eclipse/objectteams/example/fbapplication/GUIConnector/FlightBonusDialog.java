/*
 * Created on Mar 6, 2005
 */
team package org.eclipse.objectteams.example.fbapplication.GUIConnector;


/**
 * This team is nested within GUIConnector (see the package statement above).
 * It is responsible for recording acquired credits and displaying this
 * information after a passenger has booked his or her flight.
 *
 * Note, that this class implicitly inherits from GUIConnector.FlightBonusDialog
 * and so do its roles recursively.
 */
protected team class FlightBonusDialog playedBy FlightBonus {

	protected class Collector playedBy Item<@FlightBonusDialog.base> {
   		
		//-------------------------------------------------------
		//Callin bindings
		recordCredits            <- replace calculateCredit;
		//-------------------------------------------------------
		//Callout bindings
		getDestination           ->         getDestination;
		getStart                 ->         getStart;
		//-------------------------------------------------------
   	}

	protected class Message playedBy Subscriber<@FlightBonusDialog.base> {
		
		//-------------------------------------------------------
		// Callout bindings 
		getTotalCollectedCredits ->        getCollectedCredits;
		getName                  ->        getName;
		//-------------------------------------------------------
		// Callin binding
		showBonusDialog          <- after  buy;
		//-------------------------------------------------------
	}
}