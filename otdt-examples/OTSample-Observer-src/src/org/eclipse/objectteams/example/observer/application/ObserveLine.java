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

import base org.eclipse.objectteams.example.observer.point_n_line.Polyline;
import base org.eclipse.objectteams.example.observer.point_n_line.Area;

/**
 * This class applies the observer pattern to a set of drawing elements.
 * One could easily think of a canvas that should react to changes of
 * its figures.
 */
public team class ObserveLine extends ObserverPattern {
	
	/**
	 * Elements to be observed are simple poly-lines.
	 */
	protected class Subject playedBy Polyline {
		// bindings only:
		changeOp     <- after   addPoint;
		changeOpMany <- replace addPoints;
		toString     => toString; // callout overrides existing method
	}
	protected class Observer playedBy Area {
		/**
		 * Only print out that a change happend and some action should be taken.
		 * (this method implements the inherited abstract method, instead of 
		 * binding it to a base method).
		 *  
		 * @param s the element that has changed.
		 */
		protected void update(Subject s) {
			System.out.println("Observing: "+s);
		}
		/**
		 * Bind the trigger to start the protocol:
		 * only elements that are drawn need to participate in the observer pattern.
		 */
		start <- after draw; 
	}
}
