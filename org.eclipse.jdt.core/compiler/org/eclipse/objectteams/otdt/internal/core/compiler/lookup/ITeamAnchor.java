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
 * $Id: ITeamAnchor.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * NEW for OTDT.
 *
 * This interface encapsulates everything you do with variables that
 * are used as team anchors for externalized roles.
 *
 * See class TeamAnchor for more documentation.
 *
 * @author stephan
 * @version $Id: ITeamAnchor.java 23417 2010-02-03 20:13:55Z stephan $
 */
public interface ITeamAnchor {
	ITeamAnchor[] getBestNamePath();
	ITeamAnchor[] getBestNamePath(boolean needResolve);

	void setBestNameFromStat(Statement rhs);

	void shareBestName(ITeamAnchor other);
	/**
	 * Do two variables provably denote the same instance?
	 * Shown by shallow-equal bestNamePaths.
	 * Note that two tthis bindings will be considered equal,
	 * because in no way could they lead to different roles
	 * of the same name (thanks to OTJLD 1.4(c)).
	 */
	boolean hasSameBestNameAs(ITeamAnchor other);

	/**
	 * Get this anchor's "best name", ie., the 'minimal' path.
	 * @return a flat ('.'-seperated) representation of this variable's best name.
	 */
	char[] getBestName();

	/**
	 * Is this anchor "_OT$base" (translated from "base")?
	 */
	boolean isBaseAnchor();

	/**
	 * Get the tokens representing this anchor.
	 */
	char[][] tokens();

	/** Create a new anchor in which the first element of this' best name is replaced. */
	ITeamAnchor replaceFirst(ITeamAnchor anchor);

	/**
	 * Create a VariableBinding with a bestNamePath constructed from
	 * the bestNamePath of `prefix' plus this as last element.
	 */
	ITeamAnchor setPathPrefix (ITeamAnchor prefix);

	/**
	 *  Is prefix legal, ie., is this path a legal and necessary continuation of prefix?
	 *  (Concerning necessity see TeamAnchor.maySkipAnchor())
	 */
	boolean isPrefixLegal(ReferenceBinding site, ITeamAnchor prefix);

	ITeamAnchor asAnchorFor (ReferenceBinding roleType);
	ITeamAnchor retrieveAnchorFromAnchorRoleTypeFor(ReferenceBinding roleType);

	boolean isValidBinding();
	boolean isValidAnchor();


	TypeBinding getRoleTypeBinding(ReferenceBinding roleType, int dimensions);
	TypeBinding getRoleTypeBinding(ReferenceBinding roleType, TypeBinding[] arguments, int dimensions);
	TypeBinding getDependentTypeBinding(ReferenceBinding refBinding, int typeParamPosition, TypeBinding[] arguments, int dimensions, LookupEnvironment env);
	TypeBinding getDependentTypeBinding(ReferenceBinding refBinding, int typeParamPosition, TypeBinding[] arguments, int dimensions) throws NotConfiguredException;
	TypeBinding resolveRoleType(char[] roleName, int dimensions);

	boolean hasValidReferenceType();
	boolean hasSameTypeAs(ITeamAnchor other);
	boolean isFinal();

	char[] internalName();
	char[] readableName();

	boolean isTeam();
	TeamModel getTeamModelOfType();
	boolean isTypeCompatibleWith(ReferenceBinding other);
	boolean isTypeCompatibleWithTypeOf(ITeamAnchor other);
	void setStaticallyKnownTeam(RoleTypeBinding rtb);

	FieldBinding getFieldOfType(char[] token, boolean isStatic, boolean resolve);
	ReferenceBinding getMemberTypeOfType(char[] name);
	RoleModel getStrengthenedRole (ReferenceBinding role);
	boolean isTeamContainingRole(ReferenceBinding type);

	ReferenceBinding getFirstDeclaringClass();

	TypeBinding getResolvedType();
	int problemId();

}
