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
 * $Id: AbstractOTReferenceBinding.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding.TeamPackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * Implementation only class, which separates some additions out of ReferenceBinding.
 *
 * What: new structure (fields):
 *       baseclass, callinCallouts, precedences, teamPackage
 * 	     model, roleModel, _teamModel
 *       + access methods for all these
 *       flag isRolish (any type within a team)
 *
 * What: setupOrgObjectteamsTeamModel()
 * Why:  org.objectteams.Team needs special treatment: it's a team without the team modifier!
 *
 * @author stephan
 * @version $Id: AbstractOTReferenceBinding.java 23417 2010-02-03 20:13:55Z stephan $
 */
public abstract class AbstractOTReferenceBinding extends TypeBinding
{
	// Field pulled up from original ReferenceBinding:
	public int modifiers;
	// "forward declarations":
	public abstract ReferenceBinding[] memberTypes();
	public abstract ReferenceBinding superclass();
	public abstract boolean isBinaryBinding();
	public abstract int depth();

	// allow to see this as a ReferenceBinding:
	protected abstract ReferenceBinding _this();

	// === Start OT-additions: ===
	/** If set to true, assume that this class implements o.o.IBoundBase (will be added by the OTRE) */
	private boolean isBoundBase = false;
	public boolean isBoundBase() {
		if (this.isBoundBase) return true;
		if ((this.tagBits & TagBits.HierarchyHasProblems) != 0)
			return false;
		if (isInterface()) {
			for (ReferenceBinding superInterface: _this().superInterfaces())
				if (superInterface != null && superInterface.isBoundBase())
					return true;
		} else if (superclass() != null) {
			return superclass().isBoundBase();
		}
		return false;
	}
	public void setIsBoundBase(ReferenceBinding roleType) {
		this.isBoundBase = true;
	}

	/** Either direct role or role local type. (see isRole()). */
	private boolean isRolish = false;
	public void setIsRoleLocal() {
		this.isRolish = true;
		if (this.roleModel == null)
			this.roleModel = new RoleModel((ReferenceBinding)this);
	}
	public ReferenceBinding baseclass;
	public CallinCalloutBinding[] callinCallouts;
	public PrecedenceBinding[] precedences = PrecedenceBinding.NoPrecedences;
	public TypeModel model;
	public RoleModel roleModel;
	public TeamModel _teamModel; // FIXME(SH): check direct access vs. getTeamModel()
	public TeamModel getTeamModel() {
		if (this._teamModel == null)
			if (isRole())
				// select the class part, which has the structure:
				this._teamModel = this.roleModel.getTeamModelOfThis();
		return this._teamModel;
	}
	public void setTeamModel(TeamModel teamModel) {
		this._teamModel = teamModel;
	}
	/**
	 * Get direct and inherited callins.
	 * @return non-null array
	 */
	public CallinCalloutBinding[] allCallins() {
		HashSet<CallinCalloutBinding> cpInhSrcs = new HashSet<CallinCalloutBinding>();
		HashMap<String, CallinCalloutBinding> callins = internalGetCallins(cpInhSrcs);
		CallinCalloutBinding[] result = new CallinCalloutBinding[callins.size()];
		callins.values().toArray(result);
		return result;
	}
	/**
	 * @param cpInhSrcs use this set to filter out duplicates due to diamond copy inheritance.
	 * @return
	 */
	HashMap<String, CallinCalloutBinding> internalGetCallins(HashSet<CallinCalloutBinding> cpInhSrcs)
	{
		// use a hashmap to filter out duplicates by their names (overriding!)
		HashMap<String, CallinCalloutBinding> callins = new HashMap<String, CallinCalloutBinding>();

		// super first, let others overwrite:
		ReferenceBinding superRole = superclass();
		if (superRole != null && superRole.isRole())
			callins.putAll(((AbstractOTReferenceBinding)superRole).internalGetCallins(cpInhSrcs));

		if (this.callinCallouts != null) {
	    	for (int i = 0; i < this.callinCallouts.length; i++) {
				CallinCalloutBinding callinBinding = this.callinCallouts[i];
				if (callinBinding.type == CallinCalloutBinding.CALLIN)
				{
					// filter bindings with the same copy-inheritance root:
					if (cpInhSrcs.contains(callinBinding.getOrigin()))
						continue;
					// remember original:
					cpInhSrcs.add(callinBinding.getOrigin());

					callins.put(new String(callinBinding.getQualifiedName()), callinBinding);

				}
	    	}
		}

		ReferenceBinding[] memberTypes = memberTypes();
		for (int i = 0; i < memberTypes.length; i++) {
			callins.putAll(((AbstractOTReferenceBinding)memberTypes[i]).internalGetCallins(cpInhSrcs));
		}

		return callins;
	}
	/** If this is a team store here the package corresponding to the team (for role files).
	 *  Do not use a specific sub class because several subclasses of ReferenceBinding need this feature!
	 */
	public TeamPackageBinding teamPackage;
	protected void maybeSetTeamPackage(char[][] compoundName, PackageBinding parentPackage, LookupEnvironment environment) {
		if (isTeam())
			this.teamPackage = new TeamPackageBinding(compoundName, parentPackage, environment);
	}
	/**
	 * add callinCallouts read from attribute
	 * @param _mappings
	 */
	public void addCallinCallouts(CallinCalloutBinding[] mappings) {
		if (this.callinCallouts == null) {
			this.callinCallouts = mappings;
		} else {
			int oldLen = this.callinCallouts.length;
			int addLen = mappings.length;
			CallinCalloutBinding[] newMappings = new CallinCalloutBinding[oldLen+addLen];
			System.arraycopy(this.callinCallouts, 0, newMappings, 0, oldLen);
			System.arraycopy(mappings, 0, newMappings, oldLen, addLen);
			this.callinCallouts = newMappings;
		}
	}
	public final boolean isTeam() {

		if ((this.modifiers & ClassFileConstants.AccTeam) != 0)
			return true;
		if(TypeAnalyzer.isOrgObjectteamsTeam(_this())) {
			// only now detect that type is a team?
			if (this._teamModel == null)
				setupOrgObjectteamsTeamModel();
			return true;
		}
		return false;
	}
	/**
	 * When compiling org.objectteams.Team from source code
	 * the team model is not created earlier, because we didn't
	 * reckognize this class. This method catches up on this
	 * and links the new team model to binding and ast.
	 */
	protected void setupOrgObjectteamsTeamModel() {
		assert !isBinaryBinding(); // binary type org.objectteams.Team is detected by the ClassFileReader.

		TypeDeclaration teamDecl = ((SourceTypeBinding)this).scope.referenceContext;
		teamDecl.modifiers |= ClassFileConstants.AccTeam;
		this._teamModel = teamDecl.getTeamModel();
		this._teamModel.setBinding(_this());
		if (this.model != null)
			this._teamModel.setState(this.model.getState()); // transfer state from TypeModel to TeamModel

		TypeDeclaration[] memberTypes = teamDecl.memberTypes;
		TypeDeclaration confinedClass = null;
		TypeDeclaration confinedIfc   = null;
		for (int i = 0; i < memberTypes.length; i++) {
			memberTypes[i].modifiers |= ExtraCompilerModifiers.AccRole;
			RoleModel memberRole = memberTypes[i].getRoleModel(this._teamModel);
			if (memberTypes[i].getModel() != null)
				memberRole.setState(memberTypes[i].getModel().getState());
			if (CharOperation.equals(memberTypes[i].name, IOTConstants.OTCONFINED))
				confinedClass = memberTypes[i];
			else if (CharOperation.equals(memberTypes[i].name, IOTConstants.CONFINED))
				confinedIfc   = memberTypes[i];
		}
		confinedClass.getRoleModel()._interfacePart = confinedIfc;
		confinedIfc.getRoleModel()._classPart = confinedClass;
	}
	/** Any form: (1) source/generated, (2) class/interface, (3) direct/nested */
	public boolean isRole() {
		if (isEnum())
			return false;
		if (this.isRolish)
			return true;
		ReferenceBinding enclosingTeam = TeamModel.getEnclosingTeam(_this());
		if (enclosingTeam == null)
			return false;
		if (   isLocalType()
			&& (depth() - enclosingTeam.depth()) == 1 // direct local of team is not a role!
			&& !enclosingTeam.isRole())               // .. unless enclosing team is also a role
				return false;
		this.isRolish = true;
		return true;
	}
	/** only (1) source (3) direct roles */
	public boolean isSourceRole() {
		return    (this.modifiers & ExtraCompilerModifiers.AccRole) != 0
			   && !isSynthInterface();
	}
	/** only (3) direct roles */
	public boolean isDirectRole() {
		return    (this.modifiers & ExtraCompilerModifiers.AccRole) != 0;
	}
	public boolean isRegularInterface() {
	    return (this.modifiers & (ClassFileConstants.AccInterface|ClassFileConstants.AccSynthetic)) == ClassFileConstants.AccInterface;
	}
	public boolean isSynthInterface() {
	    return (this.modifiers & (ClassFileConstants.AccInterface|ClassFileConstants.AccSynthetic))
	    			== (ClassFileConstants.AccInterface|ClassFileConstants.AccSynthetic);
	}
	/** reset role status upon severe error */
	public void unrolify(TypeDeclaration ast) {
		// cleanup locally:
		this.isRolish = false;
		this.modifiers &= ~ExtraCompilerModifiers.AccRole;
		if (ast != null)
			ast.modifiers &= ~ExtraCompilerModifiers.AccRole;

		// transfer state:
		int state = this.roleModel.getState();
		this.model.setState(state);

		// also handle ifc part if given
		ReferenceBinding interfaceBinding = this.roleModel.getInterfacePartBinding();
		if (isClass() && interfaceBinding != null)
			interfaceBinding.unrolify(this.roleModel.getInterfaceAst());
	}
	/** (overridden in MemberTypeBinding, BinaryTypeBinding and RoleTypeBinding) */
	public ReferenceBinding baseclass() {
	    return this.baseclass;
	}
	public ReferenceBinding rawBaseclass() {
	    return baseclass();
	}
	protected abstract boolean implementsMethod(MethodBinding method);
}
