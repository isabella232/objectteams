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

/**
 * Instances represent physical books in the shelf.
 */
public class BookCopy {

	private Book theBook; // the title of which this is a copy
	private Person borrower = null;
	private int nr; 

	public BookCopy (Book ofBook) {
		theBook = ofBook;
		nr = ofBook.registerCopy(this);
	}

	/**
	 * When a person borrows a book, record who is the borrower.
	 * @param who
	 * @return the borrower 
	 */
	public Person borrow (Person who) {
		borrower = who;
		return borrower;
	}

	/**
	 * (Unfortunately cannot name a method "return" ;-)
	 * A book copy that was out is returned.
	 */
	public void returnIt () {
		borrower = null;
	}

	/**
	 * Is it available for borrowing?
	 */
	public boolean isAvail () {
		return borrower == null;
	}

	/**
	 * The ID of a book copy gives the title and a serial number.
	 */
	public String getID () {
		return "'"+theBook.getName() + "'#" + nr;
	}

	public String toString () {
		return getID();
	}
}