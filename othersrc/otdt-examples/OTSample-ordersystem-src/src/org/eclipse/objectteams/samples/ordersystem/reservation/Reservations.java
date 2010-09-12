/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Reservations.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem.reservation;

/**
 * This is a team implementation for a simple reservation system.
 * <code>Reservations</code> contains an abstract role <code>Reservable</code>
 * which is bound in the sub-team <code>StockReservations</code>.
 */
public abstract team class Reservations {

	/**
	 * The role class <code>Reservable</code> represents a reservable item.
	 */
    protected abstract class Reservable {

		
		abstract protected void remove(int count);
		
		/**
		 * Returns the number of pieces, which are located in the storage.
		 * Should be bound to the base class methods via callout in the subteam.
		 */
		abstract int getCount();
		
		/**
		 * Makes the item invalid.
		 */
		abstract void invalidate();

		@SuppressWarnings("unused") private int id;

		/**
		 * Contains the quantity of reserved pieces.
		 */
		private int numReserved = 0;

		/**
		 * Reserves the given number of pieces for the item and returns the number
		 * of pieces which were actually reserved.
		 * @param count Number of pieces that should be reserved
		 * @return Number of actually reserved pieces
		 */
		protected int reserve (int count) {
			//check, how much pieces are available:
			int avail = numAvail();
			//if not enough pieces are available, reserve the remaining pieces:
			if (count > avail) 
				count = avail;
			//add the newly reserved items to the reserved ones:
			numReserved += count;
			return count;
		}

		/**
		 * Releases the given number of pieces by moving them to the available ones and
		 * returns the number of actually released pieces.
		 * @param count Number of pieces that should be released
		 * @return Number of actually released items
		 */
		protected int release(int count) {
			//check, whether the given number of pieces can be released:
			if (count > numReserved)
				count = numReserved;
			//put the released pieces to the available ones:
			numReserved -= count;
			return count;
		}
		
		/**
		 * Returns the number of available pieces. This is the difference between the pieces
		 * which are located in the storage and the reserved ones.
		 * @return Number of available pieces
		 */
		protected int numAvail() {
			return getCount() - numReserved;
		}

		/**
		 * Checks the availability of the item.
		 */
		void check() {
			System.out.println(">>> Checking: "+this+"("+numAvail()+")");
			//invalidate the item if some pieces are missing
			if (numAvail() < 0) {
			    // TODO: [begin] before invalidation, remove all reservations
			    numReserved = 0;
			    // TODO: [end]
				invalidate();
			}
		}
		
		callin String toStr() {
					String result = "";
					result = base.toStr();
					return result+"\t reserved="+numReserved;
				}		

	}

	/**
	 * Singleton: reference to the team instance.
	 */
	static Reservations instance;
	
	@SuppressWarnings("unused") private int id;

	/**
	 * Returns the singleton instance 
	 */
	public static Reservations theInstance() { return instance; }

	/**
	 * Reserves the given number of pieces. Returns the number actually reserved pieces.
	 */	
	public int reserve(Reservable item, int count) {
		return item.reserve(count);
	}
	
	/**
	 * Releases the given number of pieces. Returns the number actually released pieces.
	 */
	public int release(Reservable item, int count) {
		return item.release(count);
	}
	
	/**
	 * Delivers the given number of pieces and returns the number of actualy delivered pieces.
	 */
	public void deliver(Reservable item, int count) {
		item.remove(item.release(count));
	}
	
	/**
	 * Checks whether the given item is available.
	 */
	public int numAvail(Reservable item) {
		return item.numAvail();
	}
}

