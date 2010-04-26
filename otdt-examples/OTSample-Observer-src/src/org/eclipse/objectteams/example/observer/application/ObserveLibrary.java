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
package org.eclipse.objectteams.example.observer.application;

import org.eclipse.objectteams.example.observer.pattern.ObserverPattern;

import base org.eclipse.objectteams.example.observer.library.BookManager;
import base org.eclipse.objectteams.example.observer.library.BookCopy;

/**
 * This is a team class which is used as a connector, meaning, it contains
 * just bindings, no implementation.
 * The abstract modifier of superclass is dropped, 
 * because abstract methods are defined by deployment (using callout-bindings).
 */
public team class ObserveLibrary extends ObserverPattern {

    public class Observer playedBy BookManager {
    	
    	// Callout method binding: bind an action to the update event.
        update   -> updateView; 
        
        /* -------------------------------------------------------------- */
        
        // Callin method bindings: bind events to trigger the start/stop operations.
        start    <- after  buy;
        stop     <- before drop;  
    }

    public class Subject playedBy BookCopy {

    	// Callin method binding: bind events to trigger the notification mechanism. 
        changeOp <- after returnIt, borrow; 
    }
}
