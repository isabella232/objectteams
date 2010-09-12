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
package org.eclipse.objectteams.example.observer.pattern;

import java.util.LinkedList;

/**
 * This team gives a reusable implementation of the Observer Pattern.
 * Only application-specific parts are left abstract.
 * 
 * Two applications of this pattern and a simple Main can be found in package org.eclipse.objectteams.example.observer.application.
 * 
 * To run the application, select org.eclipse.objectteams.example.observer.application/Main.java in the package explorer
 * and select "Run as" -> "Java Application".
 * (to check enablement of OT/J you may visit the JRE tab of the corres-
 * ponding launch configuration and ensure that "Enable OTRE" is checked).
 */
public abstract team class ObserverPattern {

	/**
	 * The Subject role of the observer pattern.
	 * Abstractness is not strictly needed, but it signals that applications
	 * of the pattern should refine this role.
	 */
	protected abstract class Subject {
		
		// ========== Registry of known Observers: ==========
		private LinkedList<Observer> observers = new LinkedList<Observer>();
	    
		protected void addObserver (Observer o) {
			observers.add(o);
		}

		protected void removeObserver (Observer o) {
			observers.remove(o);
		}

		/**
		 * All edit operations of the concrete Subject should call into this method. 
		 */
		public void changeOp() {
			for (Observer observer : observers)
				observer.update(this);
		}

		/**
		 *  Variant for multiple changes in one method call.
		 *  Because we suspect reentrance at the base side, we temporarily deactivate this Team.
		 *    (This solves the problem known as "jumping aspects" 
		 *     where more notifications would be sent than needed).
		 *  By declaring the method as "callin" it is designed to be bound using "replace".
		 */
		callin void changeOpMany () {
			boolean wasActive = isActive();
			deactivate();        // no notification during the base call.
			base.changeOpMany(); // call original version (requires "callin" modifier).
			if (wasActive)
				activate(); // restore state

			changeOp();
		}
	}

	/**
	 * Observer role of the design pattern.
	 */
	protected abstract class Observer {

		/** 
		 *  This method needs to be realized to do something usefull 
		 *  on the actual observer instance.
		 */
		protected abstract void update(Subject s);

		/**
		 * To be called, when a concrete observer starts to participate in the pattern.
		 * @param s the subject to connect to.
		 */
		public void start (Subject s) {
			s.addObserver(this);
		}

		/**
		 * To be called, when a concrete observer stops to participate in the pattern.
		 * @param s the subject to disconnect from.
		 */
		public void stop (Subject s) {
			s.removeObserver(this);
		}
	}
}

