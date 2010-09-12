/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 GK Software AG
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
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal;

import java.util.HashSet;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.objectteams.TeamThreadManager;

/**
 * This listener ensures that each worker threads is made known to the OTRE 
 * as soon as is starts running Jobs.
 * 
 * @since 0.7.1 (incubation)
 */
public class JobAndThreadListener implements IJobChangeListener {

	private HashSet<Thread> knownThreads = new HashSet<Thread>();
	
	public void aboutToRun(IJobChangeEvent event) {
		Thread current = Thread.currentThread();
		synchronized (this.knownThreads) {
			if (this.knownThreads.contains(current))
				return;
			this.knownThreads.add(current);
		}
		TeamThreadManager.newThreadStarted(false, null);
	}

	public void awake(IJobChangeEvent event) {
	}

	public void done(IJobChangeEvent event) {
		// FIXME(SH): implement thread ended strategy
	}

	public void running(IJobChangeEvent event) {
	}

	public void scheduled(IJobChangeEvent event) {
	}

	public void sleeping(IJobChangeEvent event) {
	}

}
