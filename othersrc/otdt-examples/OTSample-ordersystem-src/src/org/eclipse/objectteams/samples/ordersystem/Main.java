/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Main.java 23502 2010-02-08 18:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.samples.ordersystem;

import org.eclipse.objectteams.samples.ordersystem.data.*;
import org.eclipse.objectteams.samples.ordersystem.order.StockOrder;
import org.eclipse.objectteams.samples.ordersystem.reservation.StockReservations;
import org.eclipse.objectteams.samples.ordersystem.store.*;

/**
 * Object Teams features demonstrated by this example:
 * ---------------------------------------------------
 * Team class as design pattern Facade: The team class reservations.Reservations 
 * provides a simple interface which hides the system complexity and minimizes 
 * the communication between reservation system and main class.
 * 
 * Role classes as Decorator: The role class Reservable decorates its base class 
 * StockItem by adding of reservation feature, thereby allowing to control the 
 * availability of items in the storage. The role class Item encloses its base class 
 * StockItem, forwards requests to its provided methods (callouts) and performs 
 * additional actions for its base class (callins), thereby allowing to regard 
 * the storage items as a part of an order list. So we can manage the items in 
 * the storage, discount them and give the alarm, if the item has been invalidated.
 * 
 * Team and role inheritance: The team class reservation.StockReservations inherits 
 * the role class Reservable from its super-team reservation.Reservations, the team 
 * class order.StockOrder inherits the role class Item from its super-team order.Order 
 * 
 * Declared lifting: The base object StockItem is explicitly lifted to its role object
 * in the team class methods (teams order.StockOrder and reservation.StockReservations).
 * 
 * Translation polymorphism: The base class StockItem plays two different roles 
 * in two different teams: Item in order.StockOrder and Reservable in 
 * reservation.StockReservations. The both role classes are not conform to each
 * other. By transfer of an object of type Item between two methods of the different 
 * team classes (order.StockOrder and reservation.StockReservations) the Item object
 * is implicitly lowered to its base object StockOrder following by declared lifting 
 * to the object of type Reservable. (see the data flow between Item.doReserve(int) and 
 * StockReservations.reserve(StockItem as Reservable,int))
 * 
 * Separation of team implementaion and role binding: The role classes Item and Reservable
 * are implemented in their enclosing teams order.Order and reservation.Reservations 
 * and bound to their base classes in the sub-teams order.StockOrder and
 * reservation.StockReservations. 
 *  
 * Callin and callout method binding: The essential adaptations are integrated using 
 * callin bindings. Note, that the roles also employ the style of callout bindings 
 * in order to access features of their base class.
 * 
 * 
 * Domain description:
 * -------------------
 * 
 * A generic storage functionality is implemented in the classes Storage and StockItem.
 * The order system is introduced in the team class order.Order, where the role Item is 
 * unbound. The team class reservation.Reservatoins realizes the storage management in
 * connection with the reservation of contained items. The base class storage.StockItem
 * plays a role of an order entry in the team order.StockOrder (Item) and the role of 
 * a reservable item in the team reservation.StockReservations (Reservable).  
 */
public class Main {
	static StockItem[] items = new StockItem[] {
		new StockItem("screw driver", 1500),
		new StockItem("tool box pro", 5000),
		new StockItem("stepladder", 7999),
		new StockItem("philips screw", 155),
		new StockItem("wall paint", 1680),
		new StockItem("paint brush", 800)
	};
	
	static Address[] addresses = new Address[] {
		new Address("22, october avenue", "2345", "Chicago", "Il,USA"),
		new Address("Blaumeisenweg 5", "12345", "Berlin", "Germany"),
		new Address("10, Lundy Avenue", "513648", "Paris", "France"),
		new Address("Venus/Mars", "135754", "Io", "Jupiter"),
	};
	
	static Customer[] customers = new Customer[] {
		new Customer("Joe", "Jojo", addresses[0]),		
		new Customer("Jim", "Beann", addresses[1]),
		new Customer("Jan", "Tatam", addresses[2])
	};
	
	static StockReservations res = (StockReservations)StockReservations.theInstance();

	public static void main (String[] args) {
		
		
		print("================StoreTest================\n");

//		print("----------------Init Tests---------------\n");
//		InitTests.initTests();
		
		print("----------------Fill storage-------------");
		fillStorage();
		Storage storage = Storage.theInstance();
		storage.print(System.out);

		print("----------------Remove item--------------");
		storage.delete(items[2]);
		storage.print(System.out);


		print("----------------Change item counts-------");
		storage.changeCount(items[4], 40);
		storage.changeCount(items[0], 25);
		storage.changeCount(items[5], 99);
		storage.print(System.out);
		
		reserve(items[0], 24);
		reserve(items[3], 100);
		Storage.theInstance().print(System.out);
		printStore();
		print("--- Deliver 20 of "+items[0]);
		res.deliver(items[0], 20); 
		printStore();
		
		print("--- Record that we lost 5 screw driver by theft.");
		items[0].take(5); // they were stolen!!
		printStore();

		//create a new stock order and try to order some items and deliver these
		StockOrder o1 = new StockOrder(customers[0]);
		o1.order(items[4], 100);
		o1.order(items[5], 2);
		o1.print();
		o1.deliver();
		o1.print();

		//create a new stock order and try to order an item
		StockOrder o2 = new StockOrder(customers[1]);
		o2.order(items[4], 200);
		o2.print();

		items[4].put(400);
		
		//discounting
		StockOrder o3 = new StockOrder(customers[2], addresses[3]);
		o3.setDiscount(items[4], 0.10);
		o3.order(items[4], 200);
		o3.order(items[5], 2);
		o3.print();
		items[4].take(200);		
	}

	public static void fillStorage() {
		Storage storage = Storage.theInstance();
		storage .add(items[0]);
		storage .add(items[1]);
		items[1].put(20);
		storage .add(items[2]);
		storage .add(items[3]);
		items[3].put(1);
		storage .add(items[4]);
		items[4].put(200);
		storage .add(items[5]);
	}

	public static void print(String str) {
		System.out.println(str);
	}
	
	static void reserve(StockItem item, int count) {
		int actual = res.reserve(item, count);
		print("--- Reserved "+actual+"("+count+") of "+item);
	}
	
	/**
	 * Printing of inventory list
	 */
	static void printStore() {
		for (int i=0;i<items.length; i++)
			print("available: "+res.numAvail(items[i])+"\t of "+items[i]);	
	}
}
