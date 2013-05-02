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
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation.
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API.
 * The mirror implementation is using the backward compatible ITerminalSymbols constant
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer.
 * This integer is used to represent the terminal when computing a parsing action.
 *
 * Disclaimer : These constant values are generated automatically using a Java
 * grammar, therefore their actual values are subject to change if new keywords
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int 
		TokenNameNotAToken = 0,
	    TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;

	int
      TokenNameIdentifier = 19,
      TokenNameabstract = 54,
      TokenNameassert = 82,
      TokenNameboolean = 108,
      TokenNamebreak = 83,
      TokenNamebyte = 109,
      TokenNamecase = 118,
      TokenNamecatch = 119,
      TokenNamechar = 110,
      TokenNameclass = 73,
      TokenNamecontinue = 84,
      TokenNameconst = 136,
      TokenNamedefault = 80,
      TokenNamedo = 85,
      TokenNamedouble = 111,
      TokenNameelse = 124,
      TokenNameenum = 76,
      TokenNameextends = 105,
      TokenNamefalse = 40,
      TokenNamefinal = 55,
      TokenNamefinally = 122,
      TokenNamefloat = 112,
      TokenNamefor = 86,
      TokenNamegoto = 137,
      TokenNameif = 87,
      TokenNameimplements = 133,
      TokenNameimport = 120,
      TokenNameinstanceof = 16,
      TokenNameint = 113,
      TokenNameinterface = 74,
      TokenNamelong = 114,
      TokenNamenative = 56,
      TokenNamenew = 37,
      TokenNamenull = 41,
      TokenNamepackage = 106,
      TokenNameprivate = 57,
      TokenNameprotected = 58,
      TokenNamepublic = 59,
      TokenNamereturn = 88,
      TokenNameshort = 115,
      TokenNamestatic = 50,
      TokenNamestrictfp = 60,
      TokenNamesuper = 35,
      TokenNameswitch = 89,
      TokenNamesynchronized = 51,
      TokenNamethis = 36,
      TokenNamethrow = 90,
      TokenNamethrows = 125,
      TokenNametransient = 61,
      TokenNametrue = 42,
      TokenNametry = 91,
      TokenNamevoid = 116,
      TokenNamevolatile = 62,
      TokenNamewhile = 81,
      TokenNameas = 127,
      TokenNamebase = 33,
      TokenNamecallin = 63,
      TokenNameplayedBy = 134,
      TokenNameprecedence = 123,
      TokenNamereadonly = 64,
      TokenNameteam = 52,
      TokenNametsuper = 38,
      TokenNamewhen = 107,
      TokenNamewith = 117,
      TokenNamewithin = 92,
      TokenNamereplace = 128,
      TokenNameafter = 126,
      TokenNamebefore = 129,
      TokenNameget = 130,
      TokenNameset = 131,
      TokenNameIntegerLiteral = 43,
      TokenNameLongLiteral = 44,
      TokenNameFloatingPointLiteral = 45,
      TokenNameDoubleLiteral = 46,
      TokenNameCharacterLiteral = 47,
      TokenNameStringLiteral = 48,
      TokenNamePLUS_PLUS = 2,
      TokenNameMINUS_MINUS = 3,
      TokenNameEQUAL_EQUAL = 20,
      TokenNameLESS_EQUAL = 14,
      TokenNameGREATER_EQUAL = 15,
      TokenNameNOT_EQUAL = 21,
      TokenNameLEFT_SHIFT = 18,
      TokenNameRIGHT_SHIFT = 13,
      TokenNameUNSIGNED_RIGHT_SHIFT = 17,
      TokenNamePLUS_EQUAL = 93,
      TokenNameMINUS_EQUAL = 94,
      TokenNameMULTIPLY_EQUAL = 95,
      TokenNameDIVIDE_EQUAL = 96,
      TokenNameAND_EQUAL = 97,
      TokenNameOR_EQUAL = 98,
      TokenNameXOR_EQUAL = 99,
      TokenNameREMAINDER_EQUAL = 100,
      TokenNameLEFT_SHIFT_EQUAL = 101,
      TokenNameRIGHT_SHIFT_EQUAL = 102,
      TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 103,
      TokenNameOR_OR = 31,
      TokenNameAND_AND = 30,
      TokenNamePLUS = 4,
      TokenNameMINUS = 5,
      TokenNameNOT = 67,
      TokenNameREMAINDER = 9,
      TokenNameXOR = 25,
      TokenNameAND = 22,
      TokenNameMULTIPLY = 8,
      TokenNameOR = 28,
      TokenNameTWIDDLE = 68,
      TokenNameDIVIDE = 10,
      TokenNameGREATER = 12,
      TokenNameLESS = 11,
      TokenNameLPAREN = 23,
      TokenNameRPAREN = 24,
      TokenNameLBRACE = 53,
      TokenNameRBRACE = 34,
      TokenNameLBRACKET = 7,
      TokenNameRBRACKET = 70,
      TokenNameSEMICOLON = 27,
      TokenNameQUESTION = 29,
      TokenNameCOLON = 66,
      TokenNameCOMMA = 32,
      TokenNameDOT = 1,
      TokenNameEQUAL = 75,
      TokenNameAT = 39,
      TokenNameELLIPSIS = 132,
      TokenNameARROW = 77,
      TokenNameCOLON_COLON = 6,
      TokenNameBeginLambda = 49,
      TokenNameBeginIntersectionCast = 69,
      TokenNameBeginTypeArguments = 79,
      TokenNameElidedSemicolonAndRightBrace = 71,
      TokenNameAT308 = 26,
      TokenNameAT308DOTDOTDOT = 135,
      TokenNameATOT = 121,
      TokenNameBINDIN = 78,
      TokenNameCALLOUT_OVERRIDE = 104,
      TokenNameSYNTHBINDOUT = 72,
      TokenNameEOF = 65,
      TokenNameERROR = 138;


	// This alias is statically inserted by generateOTParser.sh:
	int TokenNameBINDOUT = TokenNameARROW;
}

