/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.observer.library;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Central class of a simplistic library-system.
 * The book manager keeps a set of books.
 */
public class BookManager {

	private HashMap<String, String> view = new HashMap<String, String>(); // ID -> status

	/**
	 * A new copy of a book is bought and stored in the book manager.
	 * @param bc the new book copy.
	 */
	public void buy (BookCopy bc) {
		updateView (bc);
	}

	/**
	 * Remove a book copy, e.g., after it was lost.
	 * @param bc 
	 */
	public void drop (BookCopy bc) {
		view.remove(bc.getID());
	}

	/**
	 * Print some information on each book managed by us.
	 */
	public void printView () {
		System.out.println("Copy\t\t\tStatus");
		System.out.println("--------------------------------");
		String[] keys = view.keySet().toArray(new String[view.size()]);
		Arrays.sort(keys); // sorting is for testability.
		for (int i=0; i<keys.length; i++) {
			String key = keys[i];
			String status = view.get(key);
			System.out.println(key + "\t" + status);
		}
	}

	/**
	 * Update the status (available/out) of a given book copy.
	 * @param bc
	 */
	public void updateView(BookCopy bc) {
		String status = bc.isAvail() ? "available" : "out";
		view.put(bc.getID(), status);
	}
}
