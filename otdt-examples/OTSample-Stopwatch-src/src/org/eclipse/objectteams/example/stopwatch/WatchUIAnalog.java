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
 * $Id: WatchUIAnalog.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.stopwatch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This team implements the UI for a StopWatch and contains the role class
 * WatchDisplay.
 */
public team class WatchUIAnalog {
	public class WatchDisplay extends JFrame playedBy StopWatch {

	    AnalogClock clockFace;
		private JButton startButton;
		private JButton stopButton;
		private JButton clearButton;
		
	    //========================================================== constructor
	    public WatchDisplay(StopWatch sw) {
	        Container content = this.getContentPane();
			content.setLayout(new BorderLayout());
	        clockFace = new AnalogClock();
	        this.setLocation(90, 110);
	        this.setResizable(false);

	        content.add(clockFace, BorderLayout.CENTER);
			JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout());
			
			startButton = new JButton("start");
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					start();
				}});
			buttons.add(startButton);
			
			stopButton = new JButton("stop");
			stopButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
				}});
			buttons.add(stopButton);
			
			clearButton = new JButton("clear");
			clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clear();
				}});
			buttons.add(clearButton);

	        content.add(buttons, BorderLayout.SOUTH);

			this.setTitle("Analog Stop Watch");
			this.setSize(new Dimension(298, 440));

	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        setVisible(true);
	    }//end constructor
	    
	    void update() {
	    	clockFace.update(getValue());
		}
//	  Abstract methods for mapping to the concrete base methods:
		abstract void    stop();
		abstract void   clear();
		abstract int getValue();
			
			   // callout method bindings: any call of the abstract WatchDisplay
			   // method will be forwarded to the concrete StopWatch method
		void   start()          ->       void start();
			   stop             ->       stop;
			   clear            ->       reset;
			   getValue			->       getValue; 
			   
//			 Callin method bindings: WatchDisplay (role object) is updated
			   // after the StopWatch (base object) advanced or was reset.
			   void update()	<- after void advance();
			   void update()    <- after void reset();
	}//end class ClockAnalogBuf
	

	/**
	 * The team constructor uses declared lifting. A WatchDisplay role is
	 * created for the given StopWatch object.
	 */
	public WatchUIAnalog (StopWatch as WatchDisplay w) {
		activate(ALL_THREADS); // Without this, the callin bindings have no effect.
	}
}
