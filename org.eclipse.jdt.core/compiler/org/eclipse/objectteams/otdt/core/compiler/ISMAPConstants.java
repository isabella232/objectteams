/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.core.compiler;

/** This interface defines constants for use by types in JSR-045 context.
 *
 * @author ike
 */
@SuppressWarnings("nls")
public interface ISMAPConstants
{
    public static final String OTJ_STRATUM_NAME = "OTJ";
    public static final String OTJ_CLASS_ENDING = ".class";
    public static final String OTJ_JAVA_ENDING = ".java";
    public static final String OTJ_PATH_DELIMITER = "/";
    public static final char OTJ_PATH_DELIMITER_CHAR = '/';
    public static final int OTJ_START_LINENUMBER = 1;

    public final static int STEP_OVER_LINENUMBER = Short.MAX_VALUE *2;                       // = 65534
    public final static int STEP_INTO_LINENUMBER = STEP_OVER_LINENUMBER - 1;                 // = 65533

    public static final int STEP_OVER_SOURCEPOSITION_START = Integer.MAX_VALUE - 2;          // = 2147483645
    public static final int STEP_OVER_SOURCEPOSITION_END = 0;
    public static final long STEP_OVER_SOURCEPOSITION = (((long)STEP_OVER_SOURCEPOSITION_START)<<32) + STEP_OVER_SOURCEPOSITION_END;

	public static final int STEP_INTO_SOURCEPOSITION_START = STEP_OVER_SOURCEPOSITION_START - 1; // = 2147483644
	public static final int STEP_INTO_SOURCEPOSITION_END = 0;
    public static final long STEP_INTO_SOURCEPOSITION = (((long)STEP_INTO_SOURCEPOSITION_START)<<32) + STEP_INTO_SOURCEPOSITION_END;

}
