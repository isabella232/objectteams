/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StockItem.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.store;

/**
 * This is a simple implementation of a stock item. It contains the name of the item,
 * its id, price and quantity.
 */
public class StockItem {
	
	/**
	 * Id that was assigned at last. 
	 */
	private static int lastId = 0;
	
	/**
	 * The id of the stock item.
	 */
	private Integer id;
	
	/**
	 * The name of the stock item.
	 */
	private String name;
	
	/**
	 * The currently quantity of pieces.
	 */
	private int count;
	
	/**
	 * The curently price for a piece (in cents). 
	 */
	private int price;
	
	/**
	 * Creates a new stock item initialized with the specified name and price. 
	 */ 
	public StockItem (String name, int price) {
		this.name = name;
		this.price = price;
		this.id = new Integer(++ lastId);
		this.count = 0;
	}
	
	StockItem() {}

	/**
	 * Returns the id of the item.
	 */
	public Integer getId() {return id; }
	
	/**
	 * Returns the name of the item.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the quantity of the item. 
	 */
	public int getCount() { return count; }
	
	/**
	 * Returns the price of the item.
	 */
	public int getPrice() { return price; }

	/**
	 * Generates the price string
	 */
	public String getPriceString() {
		int euro = price / 100;
		int cent = price % 100;
		return euro + "," + cent + " Euro";
	}
	
	/**
	 * Generates an entry string which describes the item.
	 */
	public String toString() {
		return "StockItem "+id+" '"+name+"'\t #="+count+",\t a "+getPriceString();
	}

	/**
	 * Increases the number of pieces located in the storage.
	 */
	public void put (int count) {
		this.count += count;
	}

	/**
	 * Decreases the number of pieces located in the storage.
	 */
	public void take (int count) {
		if (this.count > count)
			this.count -= count;
		else
			this.count = 0;
	}
	
	/**
	 * Invalidates the current item, meaning that something is wrong with
	 * this item's records.
	 * 
	 * This method only prints to System.err since the Storage has no 
	 * means for handling invalid items.
	 */
	public void invalidate () {
		System.err.println("*****");
		System.err.println("*** Item "+this+"\n*** has been invalidated.");
		System.err.println("*****");
	}
}
