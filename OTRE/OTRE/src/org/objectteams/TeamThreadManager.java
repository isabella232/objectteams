/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2006-2008 Berlin Institute of Technology, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamThreadManager.java 23408 2010-02-03 18:07:35Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.objectteams;

import java.util.HashSet;
import java.util.WeakHashMap;


/**
 * This class is for internal use, only.
 * 
 * Maintain information about existing threads as to manage
 * team activation per thread vs. globally.
 * 
 * @author Chistine Hundt
 * @author Stephan Herrmann
 */
public class TeamThreadManager {

	private static Object token = new Object();
	
	private static HashSet<ITeam> globalActiveTeams = new HashSet<ITeam>();
	private static WeakHashMap<ITeam,Object> teamsWithActivationInheritance = new WeakHashMap<ITeam,Object>();
	private static HashSet<Thread> existingThreads = new HashSet<Thread>();
	
	public static boolean newThreadStarted(boolean isMain, Thread parent) {
		if (!isMain && (new Exception().getStackTrace().length > 3))
			return false;
		// workaround for application hang on Mac OS with Apple JVM:
		Thread currentThread = Thread.currentThread();
		if (System.getProperty("os.name").startsWith("Mac"))
			if (currentThread.getName().equals("AWT-Shutdown"))
				return false;

		ITeam[] globalTeams;
		ITeam[] inheritableTeams;
		synchronized (TeamThreadManager.class) {
			existingThreads.add(currentThread);
			
			globalTeams = globalActiveTeams.toArray(new ITeam[globalActiveTeams.size()]);
			inheritableTeams = teamsWithActivationInheritance.keySet().toArray(new ITeam[teamsWithActivationInheritance.size()]);
		}
		// activate teams outside synchronized block:
		for (ITeam t : globalTeams)			
			t.activate(currentThread); // small version? global -> already registered...!
		if (parent != null)
			for (ITeam t : inheritableTeams)
				if (t.internalIsActiveSpecificallyFor(parent))
					t.activate(currentThread); // pass activation from parent to child thread
		return true;
	}
	public static void threadEnded() {
		ITeam[] teamsToDeactivate = internalThreadEnded();
		// + remove per thread activation:
		for (ITeam t : teamsToDeactivate) 
			//t.deactivate(Thread.currentThread()); // small version?
			t.deactivateForEndedThread(Thread.currentThread());			
	}
	private synchronized static ITeam[] internalThreadEnded() {
		existingThreads.remove(Thread.currentThread());
		// fetch all global active teams for deactivation:
		return globalActiveTeams.toArray(new ITeam[globalActiveTeams.size()]);
	}
	
	public synchronized static void addGlobalActiveTeam(ITeam t) {
		globalActiveTeams.add(t);
	}
	
	public synchronized static void removeGlobalActiveTeam(ITeam t) {
		globalActiveTeams.remove(t);
	}
	
	public static HashSet<Thread> getExistingThreads() {
		return existingThreads;
	}
	public static void registerTeamForActivationInheritance(ITeam aTeam) {
		teamsWithActivationInheritance.put(aTeam,token);
	}
	public static void unRegisterTeamForActivationInheritance(ITeam aTeam) {
		teamsWithActivationInheritance.remove(aTeam);
	}

}
