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
import org.objectteams.ImplicitTeamActivation;

/**
 * This team realizes the ATM.
 * An ATM belongs to a certain bank and only for withdrawal from a foreign account 
 * an additional fee is debited. Furthermore it is not possible to query the balance 
 * of a foreign account. 
 */
@ImplicitTeamActivation
public team class ATM {
	private Bank myBank;
	private Account feeAccount;
	
	public ATM(Bank bank) {
		myBank = bank;
		feeAccount = new Account(bank);
	}
	
	/**
	 * Returns the balance of the fee account.
	 */
	int getFeeAccountBalance() {
		return feeAccount.getBalance();
	}
	
	/**
	 * Pays the given amount of cash from the given account, if it contains enough money. 
	 */
	public int payCash(Account account, int amount) {
		boolean ok = account.debit(amount);
		if (ok) 
			return amount;
		else 
			return 0;
	}
	
	/**
	 * Returns the balance of the given Account object. If an exception is thrown while accessing the
	 * balance of the account this method throws an AccessDeniedException.
	 */
	public int getBalance(Account account) throws AccessDeniedException {
		try {
			return account.getBalance();
		} catch (Exception e) {
			throw new AccessDeniedException();
		}
	}
	
	/**
	 * This role is responsible for the different handling of accounts belonging to a different bank
	 * than the ATM does (foreign accounts).
	 * 
	 * The guard predicate attached to the ForeignAccount role ensures that this role and therefore 
	 * all its callin bindings are only effective for foreign accounts. It checks, if the bank of the base 
	 * object to be lifted is different from the bank of the ATM. 
	 */
	public class ForeignAccount playedBy Account
	base when (!(ATM.this.myBank.equals(base.getBank())))
	{
		/**
		 * This callin method calls its base method with the given amount plus an additional fee.
		 */
		callin boolean debitWithFee(int amount) {
			int fee = calculateFee(amount);
			if (base.debitWithFee(fee+amount)) {
				System.out.println("Debiting from a foreign account: Additional fee of "+fee+" Euro will be debited!");
				feeAccount.credit(fee);
				return true;
			}
			return false;
		}
		/**
		 * Binds the role method debitWithFee to the base method debit of the base class Account.
		 */
		debitWithFee <- replace debit;
		
		/**
		 * Restricting the query of balance, is realized as another callin method denying the call to
		 * getBalance for foreign accounts. Because of the role predicate, this callin method is not called for own accounts.
		 */
		callin int checkedGetBalance() { 
			throw new RuntimeException("Access to balance of foreign account not allowed!");
		}
		
		/**
		 * Binds the role method checkedGetBalance to the base method getBalance of the base class Account.
		 */
		checkedGetBalance <- replace getBalance;
		
		/**
		 * Returns the fee for debiting the given amount from a foreign account. 
		 * Here this is a fixed fee of 5%.
		 */
		public int calculateFee(int amount) {
			int feePercent = 5;
			return (amount/100)*feePercent;
		}
	}
}
