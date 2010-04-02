/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2005-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: otreAgent.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.jplis;

import java.lang.instrument.Instrumentation;

/**
*
* @version $Id: otreAgent.java 23408 2010-02-03 18:07:35Z stephan $
* @author Christine Hundt
*/
public class otreAgent {

	private static Instrumentation instCopy;
//	private static String optionsCopy;
	
	private static ObjectTeamsTransformer otTransformer;

	public static void premain(String options, Instrumentation inst) {
		instCopy = inst;
//		optionsCopy = options;
		
		// add all necessary transformers:
		otTransformer = new ObjectTeamsTransformer();
		instCopy.addTransformer(otTransformer);
		
		/* All future class definitions will be seen by the transformer, 
		 except definitions of classes upon which any registered transformer is dependent. */
	}
	
	public static Instrumentation getInstrumentation() {
		return instCopy;
	}
}
