/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StockReservations.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.reservation;

import base org.eclipse.objectteams.samples.ordersystem.store.StockItem;

/**
 * Team class <code>StockReservations</code> extends the abstract team class <code>Reservations</code>
 * and realizes the role bindings. This example shows, that the role bindings may appear in a subteam.
 */
public team class StockReservations extends Reservations {
	
	/**
	 * The role class <code>Reservable</code> is implemented in the unbound super-class and realizes the
	 * reservable item. Here the role class is simply bound to its base class store.StockItem.
	 */
	protected class Reservable playedBy StockItem {
		
		// callout method bindings:
		remove     -> take;
		getCount   -> getCount;
		invalidate -> invalidate;
		toString   => toString;             //overriding of inherited method
	
		/* --------------------------------------------------------------- */
		
		// callin method binding:
		toStr <- replace toString;
		check <- after take;
	}
	
	/**
	 * Team constructor is private because Stockreservations is a Singleton.
	 */
	private StockReservations() {
		// callins are effective only after activation
		activate(ALL_THREADS);
	}
	
	/**
	 * Singleton: Create the instance and store reference to it.
	 */
	static{
		instance = new StockReservations(); 
	}
	
	/**
	 * Returns the singleton instance
	 */
	public static StockReservations theInstance() { return (StockReservations) instance; }

	
	// ================ Facade interface: ================
	// The following methods export the main functions of this team.
	// The inherited methods require a role type argument, but for better
	// encapsulation, clients should not refer to role types. 
	// To this end, the "as" signatures allow to invoke these methods 
	// with an argument of type StockItem.
	// Before entering the method body, the argument is lifted to its Reservable role,
	// which is then passed to the super-method.
	
	/**
	 * Reserves the given number of pieces.
	 */
	public int reserve(StockItem as Reservable item, int count) {
		return super.reserve(item, count);
	}
	
	/**
	 * Releases the given number of pieces.
	 */
	public int release(StockItem as Reservable item, int count) {
		return super.release(item, count);
	}
	
	/**
	 * Delivers the given number of pieces.
	 */
	public void deliver(StockItem as Reservable item, int count) {
		super.deliver(item, count);
	}
	
	/**
	 * Returns the number of available pieces in the storage.
	 */
	public int numAvail(StockItem as Reservable item) {
		return super.numAvail(item);
	}
}
