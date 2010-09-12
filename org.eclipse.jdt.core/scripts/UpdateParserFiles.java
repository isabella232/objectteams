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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
import java.io.IOException;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class UpdateParserFiles {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			printUsage();
			return;
		}
	Parser.buildFilesFromLPG(args[0], args[1]);
	}
			
	public static void printUsage() {
		System.out.println("Usage: UpdateParserFiles <path to javadcl.java> <path to javahdr.java>");
		System.out.println("e.g. UpdateParserFiles c:/javadcl.java c:/javahdr.java");
	}
}

