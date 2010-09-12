/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IIndexConstants.java 19915 2009-04-19 17:23:21Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.core.search.IJavaSearchConstants;

public interface IIndexConstants {

	/* index encoding */
	char[] REF= "ref".toCharArray(); //$NON-NLS-1$
	char[] ANNOTATION_REF= "annotationRef".toCharArray(); //$NON-NLS-1$
	char[] METHOD_REF= "methodRef".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_REF= "constructorRef".toCharArray(); //$NON-NLS-1$
	char[] SUPER_REF = "superRef".toCharArray(); //$NON-NLS-1$
	char[] TYPE_DECL = "typeDecl".toCharArray(); //$NON-NLS-1$
	char[] METHOD_DECL= "methodDecl".toCharArray(); //$NON-NLS-1$
	char[] CONSTRUCTOR_DECL= "constructorDecl".toCharArray(); //$NON-NLS-1$
	char[] FIELD_DECL= "fieldDecl".toCharArray(); //$NON-NLS-1$
	char[] OBJECT = "Object".toCharArray(); //$NON-NLS-1$
//{ObjectTeams: needed for indexing Team and Role types
	// OT index categories
	char [] TEAM_DECL = "teamDecl".toCharArray(); //$NON-NLS-1$
	char [] ROLE_DECL = "roleDecl".toCharArray(); //$NON-NLS-1$
	char[] BASE_REF = "baseRef/".toCharArray(); //$NON-NLS-1$
	char[] REF_TO_TEAMPACKAGE = "refToTeam".toCharArray(); //$NON-NLS-1$

	// supertypes of all teams
	char[] TEAM  = "org.objectteams.Team".toCharArray(); //$NON-NLS-1$
	char[] ITEAM = "org.objectteams.ITeam".toCharArray(); //$NON-NLS-1$
//Andreas Kaiser}

	char[][] COUNTS= 
		new char[][] { new char[] {'/', '0'}, new char[] {'/', '1'}, new char[] {'/', '2'}, new char[] {'/', '3'}, new char[] {'/', '4'},
			new char[] {'/', '5'}, new char[] {'/', '6'}, new char[] {'/', '7'}, new char[] {'/', '8'}, new char[] {'/', '9'}
	};
	char[] DEFAULT_CONSTRUCTOR = new char[]{'/', '#'};
	char CLASS_SUFFIX = 'C';
	char INTERFACE_SUFFIX = 'I';
//{ObjectTeams: team and roles classes have a special index
	// maybe we don't need this at all -- the compiler flags (modifiers) are stored in the index as well.
	// If we need that, we need to change MANY places (search for CLASS_SUFFIX).
	//char ROLE_SUFFIX = 'R';
	//char TEAM_SUFFIX = 'T';
//   carp}
	char BASECLASS_SUFFIX = 'B'; // signals that a type is the baseclass of another: superClassOrInterface ::= C|I|B
// SH}
	char ENUM_SUFFIX = 'E';
	char ANNOTATION_TYPE_SUFFIX = 'A';
	char TYPE_SUFFIX = 0;
	char CLASS_AND_ENUM_SUFFIX = IJavaSearchConstants.CLASS_AND_ENUM;
	char CLASS_AND_INTERFACE_SUFFIX = IJavaSearchConstants.CLASS_AND_INTERFACE;
	char INTERFACE_AND_ANNOTATION_SUFFIX = IJavaSearchConstants.INTERFACE_AND_ANNOTATION;
	char SEPARATOR= '/';
	char PARAMETER_SEPARATOR= ',';
	char SECONDARY_SUFFIX = 'S';

	char[] ONE_STAR = new char[] {'*'};
	char[][] ONE_STAR_CHAR = new char[][] {ONE_STAR};

	// used as special marker for enclosing type name of local and anonymous classes
	char ZERO_CHAR = '0'; 
	char[] ONE_ZERO = new char[] { ZERO_CHAR }; 
	char[][] ONE_ZERO_CHAR = new char[][] {ONE_ZERO};

	int PKG_REF_PATTERN = 0x0001;
	int PKG_DECL_PATTERN = 0x0002;
	int TYPE_REF_PATTERN = 0x0004;
	int TYPE_DECL_PATTERN = 0x0008;
	int SUPER_REF_PATTERN = 0x0010;
	int CONSTRUCTOR_PATTERN = 0x0020;
	int FIELD_PATTERN = 0x0040;
	int METHOD_PATTERN = 0x0080;
	int OR_PATTERN = 0x0100;
	int LOCAL_VAR_PATTERN = 0x0200;
	int TYPE_PARAM_PATTERN = 0x0400;
	int AND_PATTERN = 0x0800;
	int ANNOT_REF_PATTERN = 0x1000;
//{ObjectTeams
	// leave some room to Eclipse so we don't clash upon the next release
	int TEAM_DECL_PATTERN          = 0x10000;
	int ROLE_DECL_PATTERN          = 0x20000;
	int CALLIN_DECL_PATTERN        = 0x40000;
	int REF_TO_TEAMPACKAGE_PATTERN = 0x80000;
//carp}
}
