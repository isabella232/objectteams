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
 * This class represents an account. 
 * In this application it is the base class of the ATM.ForeignAccount role 
 * and the SpecialConditions.BonusAccount role. 
 */
public class Account {
	
    private int balance;
    private Bank bank;

    /**
     * Constructor of an account object. Gets the owning bank as parameter.
     */
    public Account(Bank _bank) {
        bank = _bank;
    }

    /**
     * Get the balance of the account.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Get the bank of the account.
     */
    public Bank getBank() {
        return bank;
    }

    /**
     * Debit an amount from the account.
     */
    public boolean debit(int amount) {

        if (!(amount>balance)) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Credit an amount to the account.
     */
    public void credit(int amount) {
        balance += amount;
    }

}
