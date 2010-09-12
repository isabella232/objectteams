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
package org.eclipse.objectteams.example.atm;

/**
 * Object Teams features demonstrated by this example:
 * ---------------------------------------------------
 * 
 * Guard Predicates:
 * Guard predicates are used to restrict the activation of callins.
 * 
 * Reflection:
 * Reflection is used to check whether a base object already has a role in 
 * the context of a certain team.
 *  
 * 
 * Domain description: 
 * -------------------
 *
 * This is a simple example of an automatic teller machine (ATM).
 * Using a certain account it is possible to pay cash and to check the balance.
 * Accounts of foreign banks are handled differently from home bank accounts. Additionally, 
 * it is possible to participate in special conditions to gain bonus for certain withdrawals.

 * Launching the application:
 * --------------------------
 * 
 *   - Just run this class as you would run any regular Java program.
 *     (to check enablement of OT/J you may visit the JRE tab of the corres-
 *     ponding launch configuration and ensure that "Enable OTRE" is checked).
 * 
 */
public class Main {
	
	public static void main(String[] args) {
		Bank bb = new Bank("Bust-Bank");
		
		Account acc1 = new Account(bb);

		
		Bank cb = new Bank("Crash-Bank");
		Account acc2 = new Account(cb);
	
		ATM cbATM = new ATM(cb);
		System.out.println("Both accounts get 1000 Euro seed capital.");
		acc1.credit(1000);
		acc2.credit(1000);
		
		System.out.println("Withdrawing 200 Euro from both accounts:");
		cbATM.payCash(acc1, 200);
		System.out.println("Balance of foreign account: "+ acc1.getBalance()+ " Euro");
		cbATM.payCash(acc2, 200);
		System.out.println("Balance of home account: "+ acc2.getBalance()+ " Euro");
		System.out.println("ATMs fee account balance: " + cbATM.getFeeAccountBalance()+ " Euro");
		
		System.out.println("---------------------------------------------------");
		try {
			System.out.println("Get balance of foreign account via atm: ");
			System.out.println(cbATM.getBalance(acc1)+ " Euro");
		} catch (AccessDeniedException ade) {
			System.out.println("Sorry: Can not read the balance of a foreign account!");
		}
		try {
			System.out.println("Get balance of home account via atm: ");
			System.out.println(cbATM.getBalance(acc2)+ " Euro");
		} catch (AccessDeniedException ade) {
			System.out.println("Sorry: Can not read the balance of a foreign account!");
		}
		System.out.println("---------------------------------------------------");
		SpecialConditions sc = new SpecialConditions();
		sc.activate();
		sc.participate(acc2);
		
		System.out.println("Crediting 2000 Euro to both accounts:");
		int acc1_before = acc1.getBalance();
		int acc2_before = acc2.getBalance();
		acc1.credit(2000); // -> balance += 2020 
		
		System.out.println("Not participating account gets: " + (acc1.getBalance() - acc1_before)+" Euro.");
		acc2.credit(2000); // -> balance += 2000
		System.out.println("Special condition participating account gets: " + (acc2.getBalance() - acc2_before)+" Euro.");
		
	}
}
