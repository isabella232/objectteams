/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2010 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleReuseGuard.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileStruct;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

import base org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import base org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import base org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import base org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleFileCache;
import base org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute;
import base org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;

/**
 * This team avoids the situation that a source team when loading its RoFi cache
 * loads a binary role which is neither purely copied nor a role file.
 * Such roles are either stale (no longer present in the team) or in some other
 * way conflict with a role currently being translated.
 * 
 * By intervening in classfile lookup we prevent conflicting binaries to be 
 * stored by the LookupEnvironment.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class RoleReuseGuard 
{
	public static int DEBUG = 2; // levels: 0 nothing, 1 some, 2 more.

	// ==== the following roles communicate a cflow-dependency via this field:  ====
	static ThreadLocal<Object> isLoadingRolesOfSourceType = new ThreadLocal<Object>();

	/** Reflect base super-class by a corresponding role super-class. */
	protected class AbstractAttribute playedBy AbstractAttribute {
		protected boolean nameEquals(char[] name) -> boolean nameEquals(char[] name);
	}
	
	/** This role is a trigger in UseCase A. */
	protected class WordValueAttribute 
			extends AbstractAttribute 
			playedBy WordValueAttribute 
	{
		void checkClassFlags(WordValueAttribute attr) 
		<- after WordValueAttribute readClassFlags(ClassFileStruct reader, 
												   int             readOffset,
												   int[]           constantPoolOffsets)
		with { attr <- result }
		
		static void checkClassFlags(WordValueAttribute attr) {
			if (RoleReuseGuard.isLoadingRolesOfSourceType.get() != null)
				checkNonReusableRole(attr.getValue());
		}
		@SuppressWarnings("decapsulation")
		protected
		int getValue() -> get int _value;
		toString => toString;
	}
	
	/** A pure cflow-guard. */
	protected class RoFiTracker
			playedBy RoleFileCache 
	{
		@SuppressWarnings("decapsulation")
		cflow <- replace readBinary;
		@SuppressWarnings("basecall")
		callin void cflow() {
			Object save = isLoadingRolesOfSourceType.get();
			isLoadingRolesOfSourceType.set(new Object());
			try {
				base.cflow();
			} catch (IllegalReusedBinaryRoleException irbre) {
				// do nothing. role could simply not be re-used.
			} finally {
				isLoadingRolesOfSourceType.set(save);
			}
		}		
	}
	
	protected class SourceTypeBinding playedBy SourceTypeBinding 
	{
		getMemberType <- replace getMemberType;
		@SuppressWarnings("basecall")
		callin ReferenceBinding getMemberType(char[] name) {
			Object save = isLoadingRolesOfSourceType.get();
			isLoadingRolesOfSourceType.set(new Object());
			try {
				return base.getMemberType(name);
			} catch (IllegalReusedBinaryRoleException ex) {
				if (DEBUG>0)
					System.out.println("refused reusing type "+new String(name)); //$NON-NLS-1$
				return null; // pretend type was not found.
			} finally {
				isLoadingRolesOfSourceType.set(save);
			}
		}		
		ReferenceBinding[] getSuperInterfaces() 			  -> get ReferenceBinding[] superInterfaces;
		void setSuperInterfaces(ReferenceBinding[] superIfcs) -> set ReferenceBinding[] superInterfaces; 
	}
	
	protected class SafeEnvironment playedBy LookupEnvironment {
		ReferenceBinding askForType(char[][] typeName) 
			<- replace ReferenceBinding askForType(char[][] typeName);
		@SuppressWarnings("basecall")
		callin ReferenceBinding askForType(char[][] typeName) {
			try {
				return base.askForType(typeName);
			} catch (IllegalReusedBinaryRoleException irbre) {
				if (DEBUG>0) {
					String rn = new String(CharOperation.concatWith(typeName, '.'));
					System.out.println("rejected binary role "+rn); //$NON-NLS-1$
				}
				return null;
			}
		}

		void checkEnclosing(ReferenceBinding created) 
			<- after BinaryTypeBinding createBinaryTypeFrom(IBinaryType       binaryType, 
															PackageBinding    packageBinding, 
															boolean 		  needFieldsAndMethods, 
															AccessRestriction accessRestriction)
			with { created <- result }
		
		/**
		 * When reading a binary type as a member of a source type,
		 * check whether this is OK or whether the binary member
		 * should be discarded.
		 */		
		void checkEnclosing(ReferenceBinding type) {
			ReferenceBinding enclosing = type.enclosingType();
			if (enclosing != null && !enclosing.isBinaryBinding() && type.isRole()) {
				int flags = type.roleModel.getExtraRoleFlags();
				checkNonReusableRole(flags);
			}
		}
	}
	
	
	protected class BinaryType playedBy BinaryTypeBinding 
	{	
		
		// reverse the effect from SourceTypeBinding in case of nested lookup.
		getMemberType <- replace getMemberType;
		callin ReferenceBinding getMemberType(char[] name) {
			Object save = isLoadingRolesOfSourceType.get();
			isLoadingRolesOfSourceType.remove();
			try {
				return base.getMemberType(name);
			} finally {
				isLoadingRolesOfSourceType.set(save);
			}
		}		
		
		// ==== Callouts: ==== 
		
		// DEBUGGING:
		String internalName() -> char[] internalName() 
			with { result <- new String(result) }
	}

	
	/** Trigger of UseCase A. */
	static void checkNonReusableRole(int otClassFlags)
		throws IllegalReusedBinaryRoleException
	{
		if ((otClassFlags & IOTConstants.OT_CLASS_ROLE) == 0) 
			return;
		int specialRoleFlags = IOTConstants.OT_CLASS_PURELY_COPIED|IOTConstants.OT_CLASS_ROLE_FILE;
		if ((otClassFlags & specialRoleFlags) == 0)
		{ 
			if (DEBUG>0)
				System.out.println("throwing!!! "+otClassFlags); //$NON-NLS-1$
			RuntimeException t = new IllegalReusedBinaryRoleException("don't reuse explicit inline role"); //$NON-NLS-1$
			//t.printStackTrace();
			throw t;
		}
	}
}
