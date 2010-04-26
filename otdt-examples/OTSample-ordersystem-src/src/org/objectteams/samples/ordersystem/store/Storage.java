/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Storage.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.store;

import java.util.HashMap;
import java.util.Iterator;
import java.io.PrintStream;

/**
 * This class is a simple model of a storage. A Storage comprises a number of
 * <code>StockItem</code>s and has methods to add, delete and print these items.
 */
public class Storage {
	
	/**
	 * List of available items
	 */
	private HashMap<Integer, StockItem> items;

	/**
	 * Reference to the storage instance
	 */
	private static Storage instance = new Storage();
	
	/**
	 * Storage constrctor
	 */
	private Storage () {
		items = new HashMap<Integer, StockItem>();
	}
	
	/**
	 * Returns the storage instance. This method is used for the object creation (Singleton). 
	 */
	public static Storage theInstance() { return instance; }

	/**
	 * Adds a new item to the storage.
	 */ 
	public void add(StockItem item) { 
		if (items.containsKey(item.getId()))
			throw new RuntimeException("Already contained item "+item);
		items.put(item.getId(), item);
	}
	
	/**
	 * Deletes the given item from the storage.
	 */
	public void delete(StockItem item) {
		if (!items.containsKey(item.getId()))
			throw new RuntimeException("Not contained item "+item);
		items.remove(item.getId());
	}
	
	/** 
	 * Changes the number of pieces for the given item.
	 */
	public void changeCount(StockItem item, int difference) {
		if (difference >= 0)
			item.put(difference);
		else
			item.take(-difference);
	}
	
	/**
	 * Prints a list of available items.
	 */
	public void print(PrintStream out) {
		Iterator<StockItem> it=items.values().iterator(); 
		while (it.hasNext()) {
			out.println(it.next().toString());
		}
		out.println();
	}	
}

