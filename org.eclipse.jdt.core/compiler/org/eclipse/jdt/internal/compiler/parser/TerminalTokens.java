/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
      TokenNameIdentifier = 16,
      TokenNameabstract = 55,
      TokenNameassert = 84,
      TokenNameboolean = 109,
      TokenNamebreak = 85,
      TokenNamebyte = 110,
      TokenNamecase = 111,
      TokenNamecatch = 112,
      TokenNamechar = 113,
      TokenNameclass = 71,
      TokenNamecontinue = 86,
      TokenNameconst = 146,
      TokenNamedefault = 80,
      TokenNamedo = 87,
      TokenNamedouble = 114,
      TokenNameelse = 126,
      TokenNameenum = 76,
      TokenNameextends = 94,
      TokenNamefalse = 39,
      TokenNamefinal = 56,
      TokenNamefinally = 122,
      TokenNamefloat = 115,
      TokenNamefor = 88,
      TokenNamegoto = 147,
      TokenNameif = 89,
      TokenNameimplements = 141,
      TokenNameimport = 116,
      TokenNameinstanceof = 18,
      TokenNameint = 117,
      TokenNameinterface = 75,
      TokenNamelong = 118,
      TokenNamenative = 57,
      TokenNamenew = 37,
      TokenNamenull = 40,
      TokenNamepackage = 95,
      TokenNameprivate = 58,
      TokenNameprotected = 59,
      TokenNamepublic = 60,
      TokenNamereturn = 90,
      TokenNameshort = 119,
      TokenNamestatic = 50,
      TokenNamestrictfp = 61,
      TokenNamesuper = 35,
      TokenNameswitch = 51,
      TokenNamesynchronized = 53,
      TokenNamethis = 36,
      TokenNamethrow = 82,
      TokenNamethrows = 127,
      TokenNametransient = 62,
      TokenNametrue = 41,
      TokenNametry = 91,
      TokenNamevoid = 120,
      TokenNamevolatile = 63,
      TokenNamewhile = 83,
      TokenNamemodule = 123,
      TokenNameopen = 124,
      TokenNamerequires = 128,
      TokenNametransitive = 134,
      TokenNameexports = 129,
      TokenNameopens = 130,
      TokenNameto = 142,
      TokenNameuses = 131,
      TokenNameprovides = 132,
      TokenNamewith = 96,
      TokenNameas = 135,
      TokenNamebase = 32,
      TokenNamecallin = 64,
      TokenNameplayedBy = 143,
      TokenNameprecedence = 125,
      TokenNameteam = 54,
      TokenNametsuper = 38,
      TokenNamewhen = 97,
      TokenNamewithin = 92,
      TokenNamereplace = 136,
      TokenNameafter = 133,
      TokenNamebefore = 137,
      TokenNameget = 138,
      TokenNameset = 139,
      TokenNameIntegerLiteral = 42,
      TokenNameLongLiteral = 43,
      TokenNameFloatingPointLiteral = 44,
      TokenNameDoubleLiteral = 45,
      TokenNameCharacterLiteral = 46,
      TokenNameStringLiteral = 47,
      TokenNamePLUS_PLUS = 2,
      TokenNameMINUS_MINUS = 3,
      TokenNameEQUAL_EQUAL = 21,
      TokenNameLESS_EQUAL = 14,
      TokenNameGREATER_EQUAL = 15,
      TokenNameNOT_EQUAL = 22,
      TokenNameLEFT_SHIFT = 19,
      TokenNameRIGHT_SHIFT = 13,
      TokenNameUNSIGNED_RIGHT_SHIFT = 17,
      TokenNamePLUS_EQUAL = 98,
      TokenNameMINUS_EQUAL = 99,
      TokenNameMULTIPLY_EQUAL = 100,
      TokenNameDIVIDE_EQUAL = 101,
      TokenNameAND_EQUAL = 102,
      TokenNameOR_EQUAL = 103,
      TokenNameXOR_EQUAL = 104,
      TokenNameREMAINDER_EQUAL = 105,
      TokenNameLEFT_SHIFT_EQUAL = 106,
      TokenNameRIGHT_SHIFT_EQUAL = 107,
      TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 108,
      TokenNameOR_OR = 31,
      TokenNameAND_AND = 30,
      TokenNamePLUS = 4,
      TokenNameMINUS = 5,
      TokenNameNOT = 67,
      TokenNameREMAINDER = 10,
      TokenNameXOR = 26,
      TokenNameAND = 23,
      TokenNameMULTIPLY = 9,
      TokenNameOR = 28,
      TokenNameTWIDDLE = 68,
      TokenNameDIVIDE = 11,
      TokenNameGREATER = 12,
      TokenNameLESS = 8,
      TokenNameLPAREN = 20,
      TokenNameRPAREN = 24,
      TokenNameLBRACE = 52,
      TokenNameRBRACE = 34,
      TokenNameLBRACKET = 6,
      TokenNameRBRACKET = 70,
      TokenNameSEMICOLON = 25,
      TokenNameQUESTION = 29,
      TokenNameCOLON = 65,
      TokenNameCOMMA = 33,
      TokenNameDOT = 1,
      TokenNameEQUAL = 79,
      TokenNameAT = 49,
      TokenNameELLIPSIS = 140,
      TokenNameARROW = 77,
      TokenNameCOLON_COLON = 7,
      TokenNameBeginLambda = 48,
      TokenNameBeginIntersectionCast = 69,
      TokenNameBeginTypeArguments = 81,
      TokenNameElidedSemicolonAndRightBrace = 72,
      TokenNameAT308 = 27,
      TokenNameAT308DOTDOTDOT = 144,
      TokenNameBeginCaseExpr = 73,
      TokenNameBreakPreviewMarker = 145,
      TokenNameATOT = 121,
      TokenNameBINDIN = 78,
      TokenNameCALLOUT_OVERRIDE = 93,
      TokenNameSYNTHBINDOUT = 74,
      TokenNameEOF = 66,
      TokenNameERROR = 148;


	// This alias is statically inserted by generateOTParser.sh:
	int TokenNameBINDOUT = TokenNameARROW;
}

