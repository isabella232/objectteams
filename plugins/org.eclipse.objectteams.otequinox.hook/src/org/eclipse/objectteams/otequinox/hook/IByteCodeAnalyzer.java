/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IByteCodeAnalyzer.java 23461 2010-02-04 22:10:39Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.hook;

import java.io.InputStream;

/**
 * Interface for byte code analyzers that peek specific information
 * from a class' byte code without parsing the whole structure.
 * 
 * @author stephan
 * @since 1.2.3
 */
public interface IByteCodeAnalyzer {

	String getSuperclass(InputStream is, String className);
	String getSuperclass(byte[] bytes, String className);
}
