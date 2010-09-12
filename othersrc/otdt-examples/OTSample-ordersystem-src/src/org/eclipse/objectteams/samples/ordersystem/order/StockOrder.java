/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StockOrder.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.order;

import org.eclipse.objectteams.samples.ordersystem.reservation.StockReservations;

import base org.eclipse.objectteams.samples.ordersystem.data.Customer;
import base org.eclipse.objectteams.samples.ordersystem.data.Address;
import base org.eclipse.objectteams.samples.ordersystem.store.StockItem;

/**
 * The team <code>StockOrder</code> is a specialization of the <code>Order</code>. 
 * It inherits the <code>Item</code> role from its super-team.
 */
public team class StockOrder extends Order {

	/**
	 * The role class <code>Address</code> is implemented in the super-team 
	 * <code>Order</code>. 
	 * Here the role class is just bound to its base class.
	 */
	public class Address playedBy Address {

		abstract public String toString();
		toString -> toString;
		
	}
	
	/**
	 * The role class <code>Customer</code> is implemented in the super-team 
	 * <code>Order</code>. 
	 * Here the role class is just bound to its base class.
	 */
	public class Customer playedBy Customer {
		
		abstract public String toString();
		toString -> toString;
		
		public String getRepres() {
			boolean PRINT_ADDRESS = false;
			return toString() + (PRINT_ADDRESS ? "\n" + orderAddress.toString() : "");
		}
		
	}
	
	/**
	 * The role class <code>Item</code> is implemented in the super-team 
	 * <code>Order</code> and realizes the ordered stock item. 
	 * Here the role class is just bound to its base class.
	 */
    protected class Item playedBy StockItem {


		// callout method bindings: forwarding to the base methods
		getStockCount  -> getCount;
		getSinglePrice -> getPrice;
		getDescription -> getName;

		// callin method bindings:
		check <- after put;
		alarm <- replace invalidate;

		/**
		 * Reserves the missing pieces of the current stock item.
		 */
		public int doReserve(int numMissing) {
			return res.reserve(this, numMissing);
		}

		/**
		 * Delivers the given number of pieces.
		 */
		public void doDeliver(int count) {
			res.deliver(this, count);
		}
	}

	/** Reference to another team used for the reservation functionality. */
	StockReservations res; 
	
	// ================ Facade interface: ================
	// (see the corresponding comment in StockReservations)
	
	/**
	 * Orders a given number of pieces of an item.
	 */
	public void order (StockItem as Item item, int count) {
		super.order(item, count);
	}

	/**
	 * Sets the given discounting rate for an item.
	 */
	public void setDiscount (StockItem as Item item, double discount) {
		super.setDiscount(item, discount);
	}

	/**
	 * Team constructor, uses the constructor of the super-team. 
	 */
	public StockOrder (Customer as Customer customer) {
		super(customer);
		init();
	}

	/**
	 * Team constructor, uses the constructor of the super-team. 
	 */ 
	public StockOrder (Customer as Customer customer, 
	        		   Address  as Address address) 
	{
		super(customer, address);
		init();
	}

    /**
     * Team constructor, uses the constructor of the super-team. 
     */ 
    public StockOrder (Customer as Customer customer, 
                       Address as Address address,
                       boolean isUsingPrivateAddress) 
    {
        super(customer, address, isUsingPrivateAddress);
        init();
    }

	StockOrder() {}

	public void init() {
		res = (StockReservations)StockReservations.theInstance();
	}
}
