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
 * $Id: Main.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.stopwatch;

/**
 * Object Teams features demonstrated by this example:
 * ---------------------------------------------------
 * 
 * Callin and callout method binding: Callin bindings are used to inform the GUI
 * about changes in the base class. The callout method bindings allow us to
 * propagate commands from the GUI to the base class.
 * 
 * Role class binding: The role WatchDisplay is bound to the base class StopWatch.
 *  
 * 
 * Domain description: 
 * -------------------
 *
 * This is a simple example for an implementation of the Model-View-Controller
 * pattern. A StopWatch with the basic functions start, stop, and clear is
 * created and passed to two WatchDisplays. Any changes in the model (StopWatch)
 * are made visible in both views (WatchDisplay).
 * 
 * Launching the application:
 * --------------------------
 * Just run this main class as a Java Application, e.g., like this:
 *  - Choose "Run", "Run..." in the Eclipse menu bar
 *  - Select "Java Application"
 *  - Create a new run configuration by clicking "New"
 *  - Click "Run"
 * (to check enablement of OT/J you may visit the JRE tab of the corres-
 * ponding launch configuration and ensure that "Enable OTRE" is checked).
 */
public class Main {

	public static void main(String[] args) {
		StopWatch w = new StopWatch();
		new WatchUIAnalog(w);
		new WatchUI(w);
	}
}