/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JdtTestAdaptor.java 23529 2010-02-18 23:06:04Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann  - Initial API and implementation
 **********************************************************************/
package sourcelocations;

import base org.eclipse.jdt.core.tests.runtime.StandardVMLauncher;

/**
 * This team contains just one adaptation that would be too difficult to achieve using plain Java.
 * @author stephan
 */
public team class JdtTestAdaptor {

	/** Adapt command line computation of a vm launcher. */
	protected class VMLauncher playedBy StandardVMLauncher 
	{
		void getCommandLine(String[] args) <- after String[] getCommandLine()
			with { args <- result }

		/* 
		 * load-time transformed code currently has no StackMap, thus the verifier must
		 * be able to revert to the old behavior without a StackMap.
		 */
		private void getCommandLine(String[] args) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-XX:-FailOverToOldVerifier")) {
					args[i] = "-XX:+FailOverToOldVerifier";
					break;
				}
			}
		}		
	}
}
