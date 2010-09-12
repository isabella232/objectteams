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
 * This team realizes special conditions for selected accounts.
 * If an account is registered to collect a special bonus, every time 
 * an amount of more than 1000 is deposited, additionaly, 1% of the amount 
 * is credited. 
 */
public team class SpecialConditions {
	
	/**
	 * This team provides a registration method participate which
	 * is used to register an Account for the special conditions. 
	 * Here, the explicit role creation mechanism is used.
	 */
	public void participate(Account as BonusAccount ba) {}

	
	/**
	 * The base guard predicate at the BonusAccount role checks, if the base
	 * object already has a role in this team. If this is not the case it prevents 
	 * lifting (and thus role creation). In combination with the registration method 
	 * this means that BonusAccountroles are never created automatically via lifting but 
	 * have to be explicitly registered first.
	 */
	public class BonusAccount playedBy Account
		base when(SpecialConditions.this.hasRole(base, BonusAccount.class))
	{ 	  
		/**
		 * This callin method implements the collection of the bonus. 
		 * It replaces the original Account.credit method and performs a base call with the 
		 * increased amount of money. 
		 */
		callin void creditBonus(int amount)
		{
			int bonus = amount/100;
			base.creditBonus(amount+bonus);
			System.out.println("You will gain a bonus of "+ bonus + " Euro!");
		}

		/**
		 * In the method binding we use an additional predicate to ensure that 
		 * bonus is only credited for amounts greater than 1000.
		 */
		void creditBonus(int amount) <- replace void credit(int i)
			base when (i > 1000); 
	}    
}
