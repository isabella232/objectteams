/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleFileHelper.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;

/**
 * MIGRATION_STATE: 3.3
 *
 * Some functions for treating role files.
 * Linking role files with their enclosing team involves the following:
 * <ul>
 * <li>for STATE_ROLE_FILES_LINKED {@link #getTeamOfRoleFile} is called if current
 *     CU holds a role as toplevel type.
 *     In that case the enclosing team is searched and connected.
 *     <ul>
 *     <li>Loading a team via {@link LookupEnvironment#getTeamForRoFi} may include a call
 *         to one of the requestor's accept methods, which in turn call completeTypeBindings.
 *         (see, e.g., Engine#accept).
 *         However, before the teams type hierarchy can be connected the role file
 *         has to be linked in (see {@link LookupEnvironment#checkConnectTeamToRoFi}).
 *     </ul>
 * <li>if a team is loaded before its role files, the following locations try to find relevant role files:
 *     <ul>
 *     <li>{@link CopyInheritance#copyRolesFromTeam} using loadRoleFiles: using fields, methods, tsuper
 *          and tsub roles as hints.
 *     <li>{@link Dependencies#checkReadKnownRoles} called directly before completeTypeBindings
 *         (using the binary role file cache).
 *     </ul>
 * <li>If eager loading misses a role file that is needed later,
 *     {@link SourceTypeBinding#findTypeInTeamPackage} may retrieve a role file via the team package.
 *     Roles found this way are remembered using {@link TeamModel#addKnownRoleFile} and try to
 *     catch up using {@link Dependencies.lateRolesCatchup} and {@link CopyInheritance#copyLateRole}.
 * </ul>
 *
 * @author stephan
 * @version $Id: RoleFileHelper.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class RoleFileHelper {

	/**
	 *  Given a role file find and link the enclosing team type.
	 *
	 * @param roleUnit the compilation unit containing the role
	 * @param roleType   the role type
	 * @param environment for lookup of the team type.
	 */
	public static void getTeamOfRoleFile(
			CompilationUnitDeclaration roleUnit,
			TypeDeclaration roleType,
			LookupEnvironment environment)
	{
		// restore what might have been forgotton e.g., by SourceTypeConverter
		if (roleType.compilationUnit == null)
			roleType.compilationUnit = roleUnit;

		if (roleUnit.currentPackage == null) {
			if (roleUnit.ignoreFurtherInvestigation)
				return; // silently, error (assumably severe) has been reported
		}

		boolean sourceTypeReqSave = Config.getSourceTypeRequired();
		Config.setSourceTypeRequired(true);
		char[][] tokens = roleUnit.currentPackage.tokens;
		ReferenceBinding teamBinding = myGetType(environment, roleType, tokens, null);
		if (teamBinding != null && teamBinding.isRole())
			teamBinding = teamBinding.roleModel.getClassPartBinding();
		Config.setSourceTypeRequired(sourceTypeReqSave);
		// TODO (SH): ROFI: still need to deal with team for which a
		//            binary type already exists, which needs to be cancelled/converted.
		if (teamBinding != null) {
			if (!teamBinding.isTeam()) {
				environment.problemReporter.missingTeamForRoleWithMembers((SourceTypeBinding) teamBinding, roleType);
				// remove the role mark from the type and transfer the state to the TypeModel:
				if (roleType.binding != null)
					roleType.binding.unrolify(roleType);
			} else if (teamBinding instanceof SourceTypeBinding)
			{
				SourceTypeBinding teamSource = (SourceTypeBinding)teamBinding;
				TypeDeclaration teamDecl = (teamSource.scope != null) ?
						teamSource.scope.referenceContext :
						teamSource.getTeamModel().getAst();
				char[][] packageName = roleUnit.currentPackage.tokens;
				if (!CharOperation.equals(packageName[packageName.length-1], teamBinding.sourceName()))
					environment.problemReporter.mismatchingPackageForRole(
							packageName,
							teamDecl.name,
							roleUnit.getFileName(),
							roleUnit.currentPackage.sourceStart,
							roleUnit.currentPackage.sourceEnd);
				if (roleType.enclosingType == null) // otherwise already connected in LookupEnvironment#checkConnectTeamToRoFi
					AstEdit.addMemberTypeDeclaration(teamDecl, roleType);
				int teamState = teamBinding.getTeamModel().getState();
				if (teamState > ITranslationStates.STATE_ROLE_FILES_LINKED)
					roleType.getRoleModel()._state.requestState(roleType, teamState);
			} else {
				environment.problemReporter.roleFileInBinaryTeam(roleType, teamBinding);
				// TODO (SH): Several problems with recompiling a role file whose team is already compiled:
				if (teamBinding.getMemberType(roleType.name) != null)
					throw new InternalCompilerError("Binary team "+new String(teamBinding.readableName())+" already contains this role, yet compilation can't proceed"); //$NON-NLS-1$ //$NON-NLS-2$
				else
					throw new InternalCompilerError("Source Role in Binary Team not yet implemented, please delete the classfile of "+new String(teamBinding.readableName())); //$NON-NLS-1$
			}
		} else {
			// FIXME(SH): team not found _could_ mean that the team is something like a phantom role
			// (see 1.5.16-otjld-1). Therefor, we might want to postpone the roleUnit and hope
			// that a new type has been entered via env.getType() and processing will proceed that way.
			// Should this be done by the compiler.adaptor? How then would we determine fatal error?
			environment.problemReporter.noEnclosingTeamForRoleFile(roleUnit, roleType);
			// reset flags because we can't process it as a role without a team:
			roleUnit.currentPackage.modifiers &= ~ClassFileConstants.AccTeam;
			roleType.modifiers &= ~ExtraCompilerModifiers.AccRole;
			roleType.tagAsHavingErrors(); // CUD is already marked, nor can the type be translated successfully
		}
	}

	private static ReferenceBinding myGetType(LookupEnvironment env, TypeDeclaration roleType, char[][] firstTypeName, char[] nestedTypeName)
	{
		ReferenceBinding firstType;
		if (nestedTypeName == null)
			firstType= env.getTeamForRoFi(firstTypeName, roleType);
		else
			firstType= env.getType(firstTypeName);
		if (firstType == null && firstTypeName.length > 1) {
			char[][] parentTypeName = new char[firstTypeName.length-1][];
			System.arraycopy(firstTypeName, 0, parentTypeName, 0, parentTypeName.length);
			firstType = myGetType(env, roleType, parentTypeName, firstTypeName[firstTypeName.length-1]);
		}
		if (firstType == null)
			return null;
		// the case of deeply nested role: env didn't like to fetch it, but by unwrapping we are actually fine:
		if (firstType.problemId() == ProblemReasons.InternalNameProvided)
			firstType = ((ProblemReferenceBinding)firstType).closestReferenceMatch();
		if (nestedTypeName == null)
			return firstType;
		if (firstType.isSynthInterface())
			firstType = firstType.getRealClass();
		return firstType.getMemberType(nestedTypeName);
	}
	/** Do compound names match even if one uses inner class syntax and the other package syntax? */
	public static boolean compoundNameMatch(char[][] name1, char[][] name2) {
		if (CharOperation.equals(name1, name2))
			return true;
		int common, remainder;
		char[][] longerName;
		char[][] splitName;
		if (name1.length < name2.length) {
			common = name1.length-1;
			remainder = name2.length-common;
			longerName = name2;
			splitName = CharOperation.splitOn('$', name1[common]);
		} else if (name1.length > name2.length) {
			common = name2.length-1;
			remainder = name1.length-common;
			longerName = name1;
			splitName = CharOperation.splitOn('$', name2[common]);
		} else {
			return false;
		}
		if (splitName.length != remainder)
			return false;
		for (int i = 0; i < common; i++) {
			if (!CharOperation.equals(name1[i], name2[i]))
				return false;
		}
		for (int i = 0; i < remainder; i++) {
			if (!CharOperation.equals(splitName[i], longerName[common+i])) {
				if (CharOperation.prefixEquals(IOTConstants.OT_DELIM_NAME, splitName[i]))
					if (CharOperation.equals(longerName[common+i], splitName[i], IOTConstants.OT_DELIM_LEN, splitName[i].length))
						continue;
				return false;
			}
		}
		return true;
	}
}
