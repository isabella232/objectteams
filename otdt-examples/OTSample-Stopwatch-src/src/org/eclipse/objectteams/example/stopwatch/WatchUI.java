/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: WatchUI.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.stopwatch;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This team implements the UI for a StopWatch and contains the role class
 * WatchDisplay.
 */
public team class WatchUI {
	static int ypos = 150;

	/**
	 * Role class WatchDisplay is played by the base class StopWatch. The role
	 * class WatchDisplay is bound to the base class StopWatch with the keyowrd
	 * 'playedBy'.
	 */
	protected class WatchDisplay extends JFrame playedBy StopWatch 
	{
		private JTextField display;
		private JButton startButton;
		private JButton stopButton;
		private JButton clearButton;

		/**
		 * This constructor is used for automatic role creation. E.g. via
		 * declared lifting. Role class constructor takes an object of the type
		 * of the declared base class. Setup the window, create a textfield for
		 * time display and three buttons "start", "stop", and "clear".
		 */
		public WatchDisplay(StopWatch w) {
			setTitle("Digital Stop Watch");
			setSize(new Dimension(300, 100));
			setLocation(410, ypos+=110);
			Container pane = getContentPane();
			pane.setLayout(new GridLayout(2,3));

			pane.add(new JLabel(""));
			display = new JTextField("0", 8);
			display.setHorizontalAlignment(JTextField.RIGHT);
			pane.add(display);
			pane.add(new JLabel(""));

			startButton = new JButton("start");
			startButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						start();
					}});
			pane.add(startButton);

			stopButton = new JButton("stop");
			stopButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						stop();
					}});
			pane.add(stopButton);

			clearButton = new JButton("clear");
			clearButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						clear();
					}});
			pane.add(clearButton);
			setVisible(true);
		}		

		/**
		 * Shows the new time value on the watch display.
		 */
		void update() {
			String val = getStringValue();
			display.setText(val);
		}

		// Abstract methods for mapping to the concrete base methods:
		abstract void   start();
		abstract void   stop();
		abstract void   clear();
		abstract String getStringValue();
			
			   // callout method bindings: any call of the abstract WatchDisplay
			   // method will be forwarded to the concrete StopWatch method
			   start            ->       start;
			   stop             ->       stop;
			   clear            ->       reset;
		String getStringValue()	-> int   getValue() 
			   with {
					// result is a predefined name.
			   		result      <-   Integer.toString(result)
		       }

		  	   /* -------------------------------------------------------------- */
		
			   // Callin method bindings: WatchDisplay (role object) is updated
			   // after the StopWatch (base object) advanced or was reset.
			   void update()	<- after void advance();
			   void update()    <- after void reset();
	}

	/**
	 * The team constructor uses declared lifting. A WatchDisplay role is
	 * created for the given StopWatch object.
	 */
	public WatchUI (StopWatch as WatchDisplay w) {
		activate(ALL_THREADS); // Without this, the callin bindings have no effect.
	}
}
