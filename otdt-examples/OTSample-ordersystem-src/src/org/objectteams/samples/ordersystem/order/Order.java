/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Order.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.objectteams.samples.ordersystem.order;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

/**
* This is a generic team definition of an order. An <code>Order</code> consists of a list of
* <code>Item</code>s, implemented as an abstract role, and several team features.
*/
public team class Order {

	/**
	 * The abstract role of an address. This address is used by Customer
	 */
	public abstract class Address {
		@SuppressWarnings("unused") private int id;
	}
		
	/**
	 * The abstract role of a customer.
	 */
	public abstract class Customer {
		
		@SuppressWarnings("unused") private int id;

		private boolean usePrivateAddress = true;
		private Address orderAddress;
		
		/**
		 * @return Returns the usePrivateAddress.
		 */
		public boolean isUsePrivateAddress() {
			return usePrivateAddress;
		}
		
		/**
		 * @param usePrivateAddress The usePrivateAddress to set.
		 */
		public void setUsePrivateAddress(boolean usePrivateAddress) {
			this.usePrivateAddress = usePrivateAddress;
//			this.orderAddress = null;
		}
		
		/**
		 * @return Returns the orderAddress.
		 */
		public Address getOrderAddress() {
			return orderAddress;
		}
		
		/**
		 * @param orderAddress The orderAddress to set.
		 */
		public void setOrderAddress(Address orderAddress) {
			this.orderAddress = orderAddress;
			this.usePrivateAddress = false;
		}
		
		abstract public String getRepres();
		
	}
	
	/**
	 * The abstract role of an order entry. An <code>Item</code> is a part of an order list.
	 */
    protected abstract class Item {
		
		/**
		 * Returns the number of available items. This abstract method is implemented in a sub-role. 
		 */
		abstract int getStockCount();
		
		/**
		 * Returns the current price of a single piece. This abstract method is implemented in a sub-role.
		 */
		abstract int getSinglePrice ();
		
		/**
		 * Returns the description of the item. This abstract method is implemented in a sub-role. 
		 */
		abstract String getDescription();

		@SuppressWarnings("unused") private int id;

		/**
		 * Number of ordered pieces.
		 */
		private int count = 0;
		
		/**
		 * Number of missing pieces.
		 */
		private int numMissing = 0;
		
		/**
		 * Discounting rate for the current item.
		 */
		private double discount = 0.0;

		/**
		 * Returns the number of ordered pieces.
		 */
		protected int  getCount() {
			return count ;  	    }

		/**
		 * Sets the number of ordered pieces.
		 * @param count
		 */
		protected void setCount(int count) {
			this.count = count;	}

		/**
		 * Returns the number of missing pieces.
		 */
		protected int  getMissing  () {
			return numMissing;    }

		/**
		 * Sets the number of missing pieces. 
		 * @param count number of missing pieces
		 */
		protected void setMissing  (int count) {
			numMissing = count;  }

		/**
		 * Sets the discounting rate for the item.
		 * @param discount Discounting rate to be set
		 */
		protected void setDiscount (double discount) {
			this.discount = discount; }

		/**
		 * Returns the total price of the item considering the discounting rate.
		 */
		protected int getTotalPrice() {
			return (int) ((getSinglePrice() * getCount()) * (1.0 - discount));
		}

		/**
		 * Reserves the missing pices again and returns the number of actually reserved pieces.
		 * Checks whether the complete order list can be delivered, if all missing pieces have been reserved.
		 */
		void check () {
			int stockCount = getStockCount();
			if ((numMissing > 0) && (stockCount > 0)) {
				int reserved = doReserve(numMissing);
				numMissing -= reserved;
				if (numMissing == 0)
					Order.this.check(); // delegate up to the enclosing team instance
			}
		}

		/**
		 * Give the alarm, if the reservation became invalid.
		 * Completely replaces the original behavior, thus the warning about
		 * a missing base call.
		 */
		@SuppressWarnings("basecall")
		callin void alarm () {
			System.err.println("\nOrder of "+Order.this.customer.getRepres()+":");
			System.err.println("ATTENTION, Reservation became invalid!");
			System.err.println("Order item: "+getDescription());
			// TODO: [begin] set missing pieces to zero, try to newly reserve items 
			setMissing(getCount());
			check();
		    // TODO: [end]
		}

		/**
		 * Returns a report about the ordered item.
		 */
		public String toString() {
			String comment = "";
			if (numMissing > 0)
				comment = "\n !! "+numMissing+" pieces not on stock!!";
			return getDescription()+": \t"
				+ getCount()+"*\t= "+getEuroString(getTotalPrice())
				+ comment;
		}

		/**
		 * Reserves the missing items. Returns the number of actually reserved pieces.
		 * @param numMissing Number of missing pieces
		 * @return Number of actually reserved pieces
		 */
		abstract public int doReserve(int numMissing);
		
		/**
		 * Deliver the given number of pieces.
		 * @param count
		 */
		abstract public void doDeliver(int count);

	}

	@SuppressWarnings("unused") private int id;

	/**
	 * An Order is always associated with a customer.
	 */
	Customer customer;
	
	/**
	 * List of ordered items.
	 */
	List<Item> itemList;
	
	/**
	 * This <code>Order</code>'s status.
	 */
	int status;
	
	/**
	 * Status value of the order list. All ordered items are available and can be delivered.
	 */
	public static final int NORMAL = 0;
	
	/**
	 * Status value of the order list. There is at least one of the ordered items which is 
	 * not available so that the complete order list cannot be delivered and has to wait for stock.
	 */
	public static final int WAIT   = 1;
	
	/**
	 * Status value of the order list. All ordered items have been delivered.
	 */
	public static final int DELIVERED = 2;
	
	/**
	 * Status values of the order list.
	 */
	public static final String[] STATES = {
		"in progress",
		"wait for stock",
		"delivered"
	};
	
	/**
	 * Creates a new team instance.
	 */
	public Order (Customer customer) {
		this.customer = customer;
		itemList = new LinkedList<Item>();
		status = NORMAL;
		activate(ALL_THREADS);
	}

	/**
	 * Creates a new team instance with a different order address.
	 */
	public Order (Customer customer, Address orderAddress) {
		// this(customer);
		this.customer = customer;
		this.customer.setOrderAddress(orderAddress);
		itemList = new LinkedList<Item>();
		status = NORMAL;
		activate(ALL_THREADS);
	}

    /**
     * Creates a new team instance with a different order address.
     */
    public Order (Customer customer, Address orderAddress, boolean isUsingPrivateAddress) {
        // this(customer);
        this.customer = customer;
        this.customer.setOrderAddress(orderAddress);
        this.customer.setUsePrivateAddress(true);
        itemList = new LinkedList<Item>();
        status = NORMAL;
        activate(ALL_THREADS);
    }

	Order() { }

	/**
	 * Checks the order list and notes the missing items which cannot be delevered.  The state of 
	 * the missing items is set on <code>WAIT</code>. If all items in the order list are available, 
	 * they will be delivered. 
	 */
	void check () {
		status = NORMAL;
		Iterator<Item> it = itemList.iterator();
		while (it.hasNext()) {
			Item p = it.next();
			if (p.getMissing() > 0) {
				status = WAIT;
				return;
			}
		}
		System.out.println("Now deliverable!");
		print();
		deliver();
		print();
	}

	/**
	 * Orders a given number (count) of pieces. The local variable <code>reserved</code> holds the 
	 * number of actually reserved pieces. 
	 */
	public void order (Item item, int count) {
		int reserved = item.doReserve(count);
		item.setCount(count);
		if (reserved < count) {
			item.setMissing(count - reserved);
			status = WAIT;
		}
		itemList.add(item);
	}

	/**
	 * Checks the order status and returns true, if the order can be delivered.
	 */
	public boolean isDeliverable () {
		return status == NORMAL;
	}

	/**
	 * Delivers all items in the order list. After delivering the team instance should be deaktivated.
	 */
	public void deliver () {
		Iterator<Item> it = itemList.iterator();
		while (it.hasNext()) {
			Item p = it.next();
			p.doDeliver(p.getCount());
		}
		status = DELIVERED;
		deactivate();
	}

	/**
	 * Sets the discounting rate for the given item.
	 * @param item item which should be discounted
	 * @param discount discounting rate
	 */
	public void setDiscount (Item item, double discount) {
		item.setDiscount(discount);
	}

	/**
	 * This team method prints a string representation of this order.
	 */
	public void print() {
        System.out.println("----------------------------------------");
		System.out.println(toString());
        System.out.println("----------------------------------------");
	}

    /**
     * 
     */
    public String toString() {
        String tempString = "";
        tempString += "Order for "+customer.getRepres()+"\n";
        if (customer.getOrderAddress() != null) {
            tempString += "order address  : " + customer.getOrderAddress() + "\n";
            tempString += "-> using " + 
                          ((customer.isUsePrivateAddress()) ? "private address" : "order address") 
                          + "\n";
        }
        tempString += "("+STATES[status]+")\n";
        tempString += "========================================\n";
		Iterator<Item> it = itemList.iterator();
		int i = 1;
		int total = 0;
		while (it.hasNext()) {
			Item p = it.next();
            tempString += ((i++)+".\t"+p) + "\n";
			total += p.getTotalPrice();
		}
        tempString += "Total: "+getEuroString(total) +"\n";
        return tempString;
    }

	/**
	 * Returns an euro string for the given price. 
	 * @param price
	 * @return euro string 
	 */
	public String getEuroString(int price) {
		int euro = price / 100;
		int cent = price % 100;
		return euro + "," + cent + " Euro";
	}

}
