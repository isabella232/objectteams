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
 * $Id: StopWatch.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.stopwatch;

/**
 * This class acts as base class for this example. It is bound to the
 * WatchDisplay role of the WatchUI team. StopWatch implemets a simple watch as
 * a thread. The watch can be controlled via three methods: start(), stop() and
 * clear(). getValue() returns the current time value. Note, that the StopWatch
 * is not aware of the GUI.
 */
public class StopWatch implements Runnable {

	/**
	 * Current time value
	 */
	private int time = 0;

	/**
	 * Flag that indicates if the watch is running
	 */
	private boolean running;

	/**
	 * Flag that indicates if there is a stop request
	 */
	private boolean stopRequest;

	/**
	 * Increases the time value.
	 */
	private synchronized void advance() {
		time++;
	}

	/**
	 * Starts the watch thread.
	 */
	public synchronized void start() {
		if (!running) {
			setRunning(true);
			setStopped(false);
			(new Thread(this)).start();
		}
	}

	/**
	 * Stops the watch thread.
	 *  
	 */
	public synchronized void stop() {
		setStopped(true);
	}

	/**
	 * Resets the time value.
	 */
	public synchronized void reset() {
		time = 0;
	}

	/**
	 * Returns the current time value.
	 */
	public synchronized int getValue() {
		return time;
	}

	/**
	 * Sets the running flag value.
	 */
	private synchronized void setRunning(boolean value) {
		running = value;
	}

	/**
	 * Returns the stop flag value
	 */
	private synchronized boolean isStopped() {
		return stopRequest;
	}

	/**
	 * Sets the stop flag value
	 */
	private synchronized void setStopped(boolean value) {
		stopRequest = value;
	}

	/**
	 * This method contains the main loop that is executed while the watch is
	 * running.
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				setRunning(false);
				return;
			}
			if (isStopped()) {
				setRunning(false);
				return;
			}
			advance();
		}
	}
}