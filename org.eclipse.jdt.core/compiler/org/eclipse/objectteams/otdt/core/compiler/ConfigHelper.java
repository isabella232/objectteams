/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
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
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.compiler;

import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;

/**
 * API class for protecting (indirect) calls into the compiler with some minimal setup.
 * @author stephan
 * @since 1.3.2
 */
public class ConfigHelper {

	/**
	 * When no config has been prepared, create a stub config, which can
	 * handle all boolean queries but throws InternalCompilerError when
	 * attempting to access the lookupEnvironment or the parser.
	 */
	public static boolean checkCreateStubConfig(Object client) {
		if (Config.hasConfig())
			return false; // no need to create a stub.
		Config newConfig = new Config() {
			@Override
			protected LookupEnvironment lookupEnvironment() {
				throw new InternalCompilerError("Lookup Environment not configured"); //$NON-NLS-1$
			}
			@Override
			protected Parser parser() {
				throw new InternalCompilerError("Parser not configured"); //$NON-NLS-1$
			}
		};
		Config.addConfig(newConfig);
		return true;
	}

	/**
	 * Remove the config that has been registered for 'client'
	 * @param client
	 */
	public static void removeConfig(Object client) {
		Config.removeConfig(client);
	}
}
