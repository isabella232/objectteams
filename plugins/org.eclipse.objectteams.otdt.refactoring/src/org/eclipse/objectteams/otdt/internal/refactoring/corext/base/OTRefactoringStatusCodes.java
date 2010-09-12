/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTRefactoringStatusCodes.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
// renamed to "basis" due to incompatibilities with OT/J keyword "base"
package org.eclipse.objectteams.otdt.internal.refactoring.corext.base;

/**
 * This utility class is a part of the OT/J refactoring adaptation.
 * It contains OT refactoring related status/error codes.
 * 
 * @author brcan
 */
public class OTRefactoringStatusCodes
{
	private OTRefactoringStatusCodes()
	{
	    //no instance
	}

    public static final int PRIVATE_FIELD_ACCESS = 1; // FIXME(SH): unused
    public static final int PROTECTED_FIELD_ACCESS = 2; // FIXME(SH): unused
    public static final int PACKAGE_VISIBLE_FIELD_ACCESS = 3; // FIXME(SH): unused
    public static final int AMBIGUOUS_METHOD_SPECIFIER = 4;
    public static final int ROLE_METHOD_OVERRIDES_MOVED_METHOD = 5; // FIXME(SH): unused
    public static final int BASE_METHOD_OVERRIDES_MOVED_METHOD = 6; // FIXME(SH): unused
    public static final int MOVED_METHOD_IS_OVERRIDDEN_IN_REGULAR_SUBCLASS = 7; // FIXME(SH): unused
    public static final int DUPLICATE_METHOD_IN_NEW_RECEIVER = 8;
    public static final int CANNOT_MOVE_PRIVATE = 9;
    public static final int OVERLOADING = 10;
    
}
