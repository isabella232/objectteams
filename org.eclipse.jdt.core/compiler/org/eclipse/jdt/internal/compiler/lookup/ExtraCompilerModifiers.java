/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ExtraCompilerModifiers.java 19874 2009-04-13 17:59:05Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

// TODO (philippe) these should be moved to tagbits
/**
 * OTDT changes:
 * What: new modifiers AccRole, AccCallin, AccCallsBaseCtor
 *
 * @version $Id: ExtraCompilerModifiers.java 19874 2009-04-13 17:59:05Z stephan $
 */
public interface ExtraCompilerModifiers { // modifier constant
	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)
	final int AccJustFlag = 0xFFFF;// 16 lower bits

	// bit17 - free
	// bit18 - use by ClassFileConstants.AccAnnotationDefault
	final int AccRestrictedAccess = ASTNode.Bit19;
	final int AccFromClassFile = ASTNode.Bit20;
	final int AccDefaultAbstract = ASTNode.Bit20;
	// bit21 - use by ClassFileConstants.AccDeprecated
	final int AccDeprecatedImplicitly = ASTNode.Bit22; // record whether deprecated itself or contained by a deprecated type
	final int AccAlternateModifierProblem = ASTNode.Bit23;
	final int AccModifierProblem = ASTNode.Bit24;
	final int AccSemicolonBody = ASTNode.Bit25;
	final int AccUnresolved = ASTNode.Bit26;
	final int AccBlankFinal = ASTNode.Bit27; // for blank final variables
	final int AccIsDefaultConstructor = ASTNode.Bit27; // for default constructor
	final int AccLocallyUsed = ASTNode.Bit28; // used to diagnose unused private/local members
	final int AccVisibilityMASK = ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate;

	final int AccOverriding = ASTNode.Bit29; // record fact a method overrides another one
	final int AccImplementing = ASTNode.Bit30; // record fact a method implements another one (it is concrete and overrides an abstract one)
	final int AccGenericSignature = ASTNode.Bit31; // record fact a type/method/field involves generics in its signature (and need special signature attr)

//{ObjectTeams: modifiers/flags not written to class files:
	/**
 	 * Purely internal flag, never seen in source code nor byte code.
     * also: AccSemicolonBody (no conflict, class flag vs. method flag).
     */
 	int AccRole = 0x1000000; // bit 25

 	/** Flag for base imports. */
 	int AccBase = 0x1000000; // bit 25

	/**
	 * Encoding for the "callin" method modifier.
     * Never seen in class files, but restored using the CallinFlags attribute
	 */
	int AccCallin = 0x80000000; // bit 32

	// future:
	int AccReadonly = ASTNode.Bit27; // shared with blank final / default ctor

 	// purely internal, only passes CallsBaseConstructor from MethodInfo to MethodBinding
 	int AccCallsBaseCtor = 0x10000; // bit 17

	final int AccOTTypeJustFlag = AccJustFlag | AccRole | AccOverriding;
// SH}

}
