/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {
    public final static int

      ERROR_SYMBOL      = 136,
      MAX_NAME_LENGTH   = 41,
      NUM_STATES        = 1311,

      NT_OFFSET         = 136,
      SCOPE_UBOUND      = 357,
      SCOPE_SIZE        = 358,
      LA_STATE_OFFSET   = 17581,
      MAX_LA            = 1,
      NUM_RULES         = 936,
      NUM_TERMINALS     = 136,
      NUM_NON_TERMINALS = 417,
      NUM_SYMBOLS       = 553,
      START_STATE       = 1332,
      EOFT_SYMBOL       = 65,
      EOLT_SYMBOL       = 65,
      ACCEPT_ACTION     = 17580,
      ERROR_ACTION      = 17581;
}
