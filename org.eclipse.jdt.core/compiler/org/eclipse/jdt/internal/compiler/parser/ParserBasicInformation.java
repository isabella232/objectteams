/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {
    public final static int

      ERROR_SYMBOL      = 129,
      MAX_NAME_LENGTH   = 41,
      NUM_STATES        = 1197,

      NT_OFFSET         = 129,
      SCOPE_UBOUND      = 190,
      SCOPE_SIZE        = 191,
      LA_STATE_OFFSET   = 14484,
      MAX_LA            = 1,
      NUM_RULES         = 863,
      NUM_TERMINALS     = 129,
      NUM_NON_TERMINALS = 382,
      NUM_SYMBOLS       = 511,
      START_STATE       = 1634,
      EOFT_SYMBOL       = 73,
      EOLT_SYMBOL       = 73,
      ACCEPT_ACTION     = 14483,
      ERROR_ACTION      = 14484;
}
