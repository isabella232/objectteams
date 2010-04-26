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
 * $Id: Bonus.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.bonussystem;

/**
 * Abstract team defining the collaboration between a subscriber and some bonus
 * items by which the subscriber earns some credits.
 */
public abstract team class Bonus {

	/**
	 * By this attribute a bonus item can access the currently active subscriber
	 * (relative to the team instance).
	 */
	protected Subscriber subscriber = null;
	
	/**
	 * While a subscriber is buying something, all acquired
	 * bonus items will contribute to his/her credits.
	 */ 
	public abstract class Subscriber {
	    
	    int collectedCredits = 0;
	    
	    protected int getCollectedCredits() {
	    	return collectedCredits;
	    }
	    
	    /** Add new credit points to our balance. */
	    protected void collectCredit (int credit) {
			collectedCredits += credit;
	    }
	    
		/**
		 * When buying something, tell the enclosing team
		 * who we are, so that we will get the credits.
		 */
	    callin void buy ()
	    {
			Bonus.this.subscriber = this;
			base.buy();
			Bonus.this.subscriber = null;

			log("Subscriber has collected a total of "+getCollectedCredits()+" credit points.");
	    }
	}	

	/**
	 * An item that is associated with some bonus points (credits).
	 */
	public abstract class Item {
	    
	    /** Expected method: calculate the number of points to be earned. */
	    protected abstract int calculateCredit ();
	    
	    /** 
		 *  The item is about to be acquired: give the associated
		 *  credit points to the subscriber.
		 */
	    protected void earnCredit () {
			Subscriber subscriber = Bonus.this.subscriber;
			if (subscriber == null) 
				return;
			
			int tmpCredits = calculateCredit();
			log("buying an item that yields "+tmpCredits+" credit points.");
			subscriber.collectCredit(tmpCredits);
	    }
	}
	
	protected void log (String msg) {
		System.out.println(">>Bonus>> "+msg);
	}
}