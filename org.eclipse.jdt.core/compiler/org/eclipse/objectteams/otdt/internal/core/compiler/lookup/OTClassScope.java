/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2016 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemPackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * NEW for OTDT:
 *
 * A special type of scopes for role files and teams,
 * which is able to resolve from different sources:
 *   + parent is the team scope
 *   + roleUnitImportScope stores the imports of this role file
 *   + a team can locate types relative to its package
 *     (implemented in SourceTypeBinding.getMemberType()).
 *   + for types found in this scope perform the wrapping
 *
 * TODO (SH): ROFI: find out what is the latest point in time, when new roles can be accepted,
 *                  and what we should do, if a role file is detected too late!
 *
 * @author stephan
 * @version $Id: OTClassScope.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTClassScope extends ClassScope {

	private CompilationUnitScope roleUnitImportScope;

	private CompilationUnitScope baseImportScope;
	/**
	 * Create an OTClassScope (replaces public constructors).
	 *
	 * @param unitScope the physically enclosing scope
	 * @param otType    either a team or a role (or both ;-)
	 * @return a fresh OTClassScope
	 */
	public static OTClassScope createTopLevelOTClassScope(CompilationUnitScope unitScope, TypeDeclaration otType)
	{
		if (otType.isRole())
			return new OTClassScope(otType.enclosingType.scope, unitScope, otType);
		else
			return new OTClassScope(unitScope, null, otType);
	}

	/**
	 * This factory method is used for nested team types only.
	 *
	 * @param parent parent scope
	 * @param otType type for which to create the new scope
	 * @return a fresh OTClassScope
	 */
	public static OTClassScope createMemberOTClassScope(ClassScope parent, TypeDeclaration otType)
	{
		return new OTClassScope(parent, null, otType);
	}

	/**
	 * @param parent        the semantically enclosing scope
	 * @param roleUnitScope if it is a role file this scope stores the imports
	 * @param otType        the referenceContext of this scope
	 */
	private OTClassScope(Scope parent, CompilationUnitScope roleUnitScope, TypeDeclaration otType)
	{
		super(parent, otType);
		this.roleUnitImportScope = roleUnitScope;
		if (this.roleUnitImportScope != null)
			this.roleUnitImportScope.recordTypeReference(parent.enclosingReceiverType());
	}

	/**
	 * Override ClassScope.buildType():
	 * + Recover the enclosingType (role files call this with a null enclosingType)
	 * + Add the new type to the members of the enclosing type
	 */
	protected SourceTypeBinding buildType(SourceTypeBinding enclosingType, PackageBinding packageBinding, AccessRestriction accessRestriction)
	{
		if (!this.referenceContext.isRole())
			return super.buildType(enclosingType, packageBinding, accessRestriction);

		if (this.roleUnitImportScope != null) {
			// recover enclosing type of a role, which is determined from the semantically enclosing scope:
			assert enclosingType == null;
			enclosingType = this.parent.referenceType().binding;
		}
		SourceTypeBinding type = super.buildType(enclosingType, packageBinding, accessRestriction);
		int size = 0;
		ReferenceBinding[] newMembers;
		if (enclosingType.memberTypes == null) {
			newMembers = new ReferenceBinding[1];
		} else {
			size = enclosingType.memberTypes.length;
			//grow Array
			System.arraycopy(
					enclosingType.memberTypes, 0,
					newMembers = new ReferenceBinding[size+1], 0, size);
		}
		newMembers[size] = type;
		enclosingType.setMemberTypes(newMembers);
		return type;
	}

	/** Need to initialize the role file imports, too. */
	@Override
	protected void checkRoleFileImports() {
		super.checkRoleFileImports();
		CompilationUnitScope importScope= this.roleUnitImportScope;
		if (importScope != null && importScope.imports == null)
			importScope.checkAndSetImports();
	}

	/** override hook: consider parent AND roleUnitImportScope. */
	// TODO(SH): currently not active (super method is not in place).
	protected Binding getTypeOrPackageInParent(char[] compoundName, int flags, boolean needResolve) {
		Binding foundInParent = this.parent.getTypeOrPackage(compoundName, flags, needResolve);
		if (this.roleUnitImportScope != null) {
			Binding foundInImports = this.roleUnitImportScope.getTypeOrPackage(compoundName, flags, needResolve);
			if (foundInImports != null && foundInImports.isValidBinding())
			{
				if (   foundInParent != null
					&& foundInParent.isValidBinding()
					&& foundInImports != foundInParent)
				{
					switch (foundInParent.kind()) {
					case Binding.PACKAGE:
						return new ProblemPackageBinding(compoundName, ProblemReasons.Ambiguous);
					case Binding.TYPE:
						return problemTypeBinding(compoundName, (TypeBinding)foundInParent, ProblemReasons.Ambiguous);
					}
				}
				else
				return foundInImports;
			}
		}
		return foundInParent;
	}

	public TypeBinding getType(char[][] compoundName, int typeNameLength) {
		TypeBinding foundHere = super.getType(compoundName, typeNameLength);     // valid or Problem
		if (this.roleUnitImportScope != null) {
			TypeBinding foundInImports = this.roleUnitImportScope.getType(compoundName, typeNameLength);
			if (foundInImports.isValidBinding()) {
				if (foundHere.isValidBinding() && TypeBinding.notEquals(foundInImports, foundHere)) {
					return problemTypeBinding(compoundName, foundHere, ProblemReasons.Ambiguous);
				} else
					return foundInImports;
			}
		}
		return maybeWrap(foundHere);
	}

	public TypeBinding getType(char[] token) {
		return getType(token, null);
	}
	public TypeBinding getType(char[] token, PackageBinding packageBinding) {
		TypeBinding foundHere = packageBinding != null ?
				super.getType(token, packageBinding) :
				super.getType(token); // don't pass null packageBinding, would cause infinite recursion.
		TypeBinding foundInImports = findImportedType(token, packageBinding, foundHere);
		if (foundInImports != null)
			return foundInImports;
		return maybeWrap(foundHere);
	}

	private TypeBinding problemTypeBinding(char[][] compoundName, TypeBinding problem, int reason)
	{
		if (problem instanceof ReferenceBinding)
			return new ProblemReferenceBinding(compoundName,
					(ReferenceBinding)problem, ProblemReasons.Ambiguous);
		else
			return null; // no ProblemBinding can be constructed (cf. Scope.TODO should improve).
	}
	private TypeBinding problemTypeBinding(char[] compoundName, TypeBinding problem, int reason)
	{
		if (problem instanceof ReferenceBinding)
			return new ProblemReferenceBinding(compoundName,
					(ReferenceBinding)problem, ProblemReasons.Ambiguous, true);
		else
			return null; // no ProblemBinding can be constructed (cf. Scope.TODO should improve).
	}
	/**
	 * After looking for a member type check whether imports also yield a candidate.
	 *
	 * @param token     name of type to find
	 * @param foundHere type already found directly or null
	 * @return <ul>
	 *   <li>valid type binding means: type found in imports only
	 *   <li>null means: not found in imports
	 *   <li>ProblemReferenceBinding means: ambiguous, found both directly and in imports.
	 *   </ul>
	 */
	public TypeBinding findImportedType(char[] token, PackageBinding packageBinding, TypeBinding foundHere) {
		if (this.roleUnitImportScope != null) {
			TypeBinding foundInImports = this.roleUnitImportScope.getType(token, packageBinding);
			if (foundInImports.isValidBinding()) {
				if (   foundHere != null
					&& foundHere.isValidBinding()
					&& TypeBinding.notEquals(foundInImports, foundHere))
					return problemTypeBinding(token, foundHere, ProblemReasons.Ambiguous);
				else
					return foundInImports;
			}
		}
		return null;
	}

	/* TODO(SH): if this really works it might replace TeamModel.findEnclosingTeamContainingRole() */
	private TypeBinding maybeWrap(TypeBinding foundHere) {
		if (   foundHere.isValidBinding()						// don't wrap problem
			&& foundHere instanceof MemberTypeBinding   		// only unwrapped member types
			&& !TSuperHelper.isMarkerInterface(foundHere)) // never wrap marker ifc
		{
			ReferenceBinding foundHereRef = (ReferenceBinding)foundHere;

			// outer loop sub-super, inner loop in-out
			ReferenceBinding site = enclosingSourceType();
			TypeBinding foundEnclosing = foundHereRef.enclosingType();
			while (   site != null
				   && site.isTeam())
			{
				ReferenceBinding currentClass = site;
				while (currentClass != null) {
					// OK to compare references: containment only managed in class parts.
					if (TypeBinding.equalsEquals(site, foundEnclosing)) {
						VariableBinding tthis = site.getTeamModel().getTThis();
						return tthis.getRoleTypeBinding(foundHereRef, /*dimensions*/0);
					}
					currentClass = currentClass.enclosingType();
				}
				site = site.superclass();
			}
		}
		return foundHere;
	}

	public CompilationUnitDeclaration referenceCompilationUnit() {
		if (this.referenceContext.isRole() && !this.referenceContext.isPurelyCopied) {
			CompilationUnitDeclaration result = this.referenceContext.compilationUnit;
			if (result != null)
				return result;
		}
		if (this.parent != null)
			return this.parent.referenceCompilationUnit();
		return super.referenceCompilationUnit();
	}

	public void checkAndSetBaseImports(LookupEnvironment env,
									   ImportReference[] references,
									   ImportBinding[] resolvedBaseImports)
	{
		// TODO what checks need to be performed??
		// TODO: support base class decapsulation for base imports!
		// TODO: perhaps we should not report name class between a role's name and its base-imported
		//       baseclass??
		ImportReference currentPackage = referenceCompilationUnit().currentPackage;
		if (currentPackage != null && currentPackage.isTeam()) {
			for (ImportReference reference : references)
				if (reference != null)
					problemReporter().baseImportInRoleFile(reference);
			return;
		}
		CompilationUnitDeclaration cud = new CompilationUnitDeclaration(problemReporter(), this.referenceContext.compilationResult, 0);
		cud.currentPackage = currentPackage;
		this.baseImportScope = new CompilationUnitScope(cud, env);
		this.baseImportScope.imports = resolvedBaseImports;
		this.baseImportScope.fPackage = compilationUnitScope().fPackage;
		this.baseImportScope.topLevelTypes = new SourceTypeBinding[0];
	}

	public CompilationUnitScope getBaseImportScope(Scope originalScope) {
		if (this.baseImportScope != null)
			this.baseImportScope.originalScope = originalScope;
		return this.baseImportScope;
	}

	public void faultInRoleFileImports() {
		if (this.roleUnitImportScope != null)
			this.roleUnitImportScope.faultInImports();
	}

	public ImportBinding[] getRoleUnitImports() {
		if (this.roleUnitImportScope == null)
			return null;
		return this.roleUnitImportScope.imports;
	}

	public void checkUnusedImports() {
		// cf. CompilationUnitDeclaration.checkUnusedImports()
		CompilationUnitScope scope = this.baseImportScope;
		if (scope != null && scope.imports != null)
		{
			for (int i = 0, max = scope.imports.length; i < max; i++){
				ImportBinding importBinding = scope.imports[i];
				ImportReference importReference = importBinding.reference;
				if (importReference != null && ((importReference.bits & ASTNode.Used) == 0)){
					scope.problemReporter().unusedImport(importReference);
				}
			}
		}
	}

	/** Potentially look in role file CU and team CU. */
	@Override
	public boolean cuIgnoreFurtherInvestigation() {
		if (this.referenceContext.isRole()) {
			CompilationUnitDeclaration result = this.referenceContext.compilationUnit;
			if (result != null && result.ignoreFurtherInvestigation)
				return true;
		}
		if (this.parent != null) {
			 if (this.parent.kind == CLASS_SCOPE)
				 return ((ClassScope)this.parent).cuIgnoreFurtherInvestigation();
			 else
				 return ((CompilationUnitScope)this.parent).referenceContext.ignoreFurtherInvestigation;
		}
		return false;
	}

	/** Mark base import as used when a role file is found. */
	public void recordBaseClassUse(ReferenceBinding baseclass) {
		if (this.baseImportScope != null && this.baseImportScope.imports != null)
			for (ImportBinding importBinding : this.baseImportScope.imports)
				if (importBinding.isBase && importBinding.resolvedImport == baseclass)
					importBinding.reference.bits |= ASTNode.Used;
	}
}
