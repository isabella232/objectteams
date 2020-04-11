/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Erling Ellingsen -  patch for bug 125570
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *     Sebastian Zarnekow - Contribution for
 *								Bug 544921 - [performance] Poor performance with large source files
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.IProblemRechecker;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.OTClassScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;


/**
 * OTDT changes:
 *
 * Role files and team packages:
 * =============================
 * What: Don't create PackageBinding for team package
 * Why:  Although syntactically alike, team package declartations are handled differently:
 * 	     Create the binding in SourceTypeBinding.<init> for a team type.
 *
 * What: Handle, what to Java looks like a clash between package and type.
 * Why:  If the type is a team, equal names are intended.
 *       While doing the check also compare names of the current team and the team package.
 *
 * What: Decide whether to create ClassScopes or OTClassScopes for contained types.
 * Why:  Teams and role files need OTClassScope for specific lookup.
 *
 * Other:
 * ======
 * What: Avoid doubly processing faultInImports()
 * Why:  faultInTypes is now controlled by Dependencies
 *
 */
public class CompilationUnitScope extends Scope {

	public LookupEnvironment environment;
	public CompilationUnitDeclaration referenceContext;
	public char[][] currentPackageName;
	public PlainPackageBinding fPackage;
	public ImportBinding[] imports;
	public int importPtr;
	public HashtableOfObject typeOrPackageCache; // used in Scope.getTypeOrPackage()

	public SourceTypeBinding[] topLevelTypes;

	private SortedCompoundNameVector qualifiedReferences;
	private SortedSimpleNameVector simpleNameReferences;
	private SortedSimpleNameVector rootReferences;
	private LinkedHashSet<ReferenceBindingSetWrapper> referencedTypes;
	private Set<ReferenceBindingSetWrapper> referencedSuperTypesSet;
	private ObjectVector referencedSuperTypes;

	HashtableOfType constantPoolNameUsage;
	private int captureID = 1;

	private ImportBinding[] tempImports;	// to keep a record of resolved imports while traversing all in faultInImports()

	/**
	 * Skips import caching if unresolved imports were
	 * found last time.
	 */
	private boolean skipCachingImports;

	boolean connectingHierarchy;
	private ArrayList<Invocation> inferredInvocations;
	/** Cache of interned inference variables. Access only via {@link InferenceVariable#get(TypeBinding, int, InvocationSite, Scope, ReferenceBinding, boolean)}. */
	Map<InferenceVariable.InferenceVarKey, InferenceVariable> uniqueInferenceVariables = new HashMap<>();
//{ObjectTeams: when used as a baseimport scope, remember the original scope during this current lookup
	public Scope originalScope;
	// store parser for on-demand Dependencies.setup() lateron:
	public Parser parser;
// SH}

public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
	this(unit, environment.globalOptions);
	this.environment = environment;
}

public CompilationUnitScope(CompilationUnitDeclaration unit, CompilerOptions compilerOptions) {
	super(COMPILATION_UNIT_SCOPE, null);
	this.referenceContext = unit;
	unit.scope = this;
	this.currentPackageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;

	if (compilerOptions.produceReferenceInfo) {
		this.qualifiedReferences = new SortedCompoundNameVector();
		this.simpleNameReferences = new SortedSimpleNameVector();
		this.rootReferences = new SortedSimpleNameVector();
		this.referencedTypes = new LinkedHashSet<>();
		this.referencedSuperTypesSet = new HashSet<>();
		this.referencedSuperTypes = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
		this.rootReferences = null;
		this.referencedTypes = null;
		this.referencedSuperTypesSet = null;
		this.referencedSuperTypes = null;
	}
	// client still needs to assign #environment
}
//{ObjectTeams: accessible from Dependencies:
public
// SH}
void buildFieldsAndMethods() {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].scope.buildFieldsAndMethods();
}
//{ObjectTeams: extract from the above:
public void connectBaseclass() {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].scope.connectBaseclassRecurse();
}
// SH}
void buildTypeBindings(AccessRestriction accessRestriction) {
	this.topLevelTypes = new SourceTypeBinding[0]; // want it initialized if the package cannot be resolved
	boolean firstIsSynthetic = false;
	if (this.referenceContext.compilationResult.compilationUnit != null) {
		char[][] expectedPackageName = this.referenceContext.compilationResult.compilationUnit.getPackageName();
		if (expectedPackageName != null && !this.referenceContext.isModuleInfo()
				&& !CharOperation.equals(this.currentPackageName, expectedPackageName)) {

			// only report if the unit isn't structurally empty
			if (this.referenceContext.currentPackage != null
					|| this.referenceContext.types != null
					|| this.referenceContext.imports != null) {
				problemReporter().packageIsNotExpectedPackage(this.referenceContext);
			}
			this.currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR : expectedPackageName;
		}
	}
//{ObjectTeams: severe problems have to set ignoreFurtherInvestigation:
  boolean hasPackageError = false;
  try {
// SH}
	if (this.currentPackageName == CharOperation.NO_CHAR_CHAR) {
		// environment default package is never null
		this.fPackage = this.environment.defaultPackage;
		if (this.referenceContext.isModuleInfo()) { // cannot have a package as per grammar
			ModuleDeclaration moduleDecl = this.referenceContext.moduleDeclaration;
			if (moduleDecl != null) {
				moduleDecl.createScope(this);
				moduleDecl.checkAndSetModifiers();
			}
		} else if (module() != this.environment.UnNamedModule) {
			problemReporter().unnamedPackageInNamedModule(module());
		}
	} else {
//{ObjectTeams: ROFI consider team packages, different creation time, different conflict rule
	  if (   this.referenceContext.currentPackage != null
		  && this.referenceContext.currentPackage.isTeam())
	  {
		// ROFI: for a role file get the real package from the enclosing team.
		if (   this.referenceContext.types != null
			&& this.referenceContext.types.length > 0)
		{
			this.fPackage = this.referenceContext.types[0].getPackageOfTeam(this.referenceContext.currentPackage, this);
			if (this.fPackage == null) {
				hasPackageError = true;
				return; // cannot recover
			}
		}
	  }
	  else {
        // ROFI: only create non-team packages here
        // team packages are created in SourceTypeBinding.<init>
// orig:
		if ((this.fPackage = this.environment.createPlainPackage(this.currentPackageName)) == null) {
// :giro
			// ROFI: check whether seeming conflict is due to a missing "team" modifier of the package:
			final Parser parser = Config.getParser();
			if (parser != null && parser.currentTeam != null) {
				if (   this.referenceContext.currentPackage != null
				    && CharOperation.equals(
						  this.referenceContext.currentPackage.tokens,
						  parser.currentTeam.binding.compoundName))
				{
					problemReporter().nonTeamPackageForRole(
							this.referenceContext.currentPackage.tokens,
							this.referenceContext.getFileName(),
							this.referenceContext.currentPackage.sourceStart,
							this.referenceContext.currentPackage.sourceEnd);
					// ensure fPackage is not null
					this.fPackage = this.environment.defaultPackage;
					hasPackageError = true;
					return;
				}
			}
			// only report if the unit isn't structurally empty
			if (   this.referenceContext.currentPackage == null
				&& this.referenceContext.types == null
				&&  this.referenceContext.imports == null)
				return;
// orig:
			if (this.referenceContext.currentPackage != null) {
				problemReporter().packageCollidesWithType(this.referenceContext); // only report when the unit has a package statement
			}
			// ensure fPackage is not null
			this.fPackage = this.environment.defaultPackage;
/* OT:  */	hasPackageError = true;
			return;
		} else if (this.referenceContext.isPackageInfo()) {
			// resolve package annotations now if this is "package-info.java".
			if (this.referenceContext.types == null || this.referenceContext.types.length == 0) {
				this.referenceContext.types = new TypeDeclaration[1];
				this.referenceContext.createPackageInfoType();
				firstIsSynthetic = true;
			}
			// ensure the package annotations are copied over before resolution
			if (this.referenceContext.currentPackage != null && this.referenceContext.currentPackage.annotations != null) {
				this.referenceContext.types[0].annotations = this.referenceContext.currentPackage.annotations;
			}
		}
		recordQualifiedReference(this.currentPackageName); // always dependent on your own package
// :giro
	  }
// SH}
	}
// {ObjectTeams: maybe push down severe problem to types:
  } finally {
	if (hasPackageError)
		for (int i = 0; i < this.referenceContext.types.length; i++)
			this.referenceContext.types[i].tagAsHavingErrors();
  }
// SH}

	// Skip typeDeclarations which know of previously reported errors
	TypeDeclaration[] types = this.referenceContext.types;
	int typeLength = (types == null) ? 0 : types.length;
	this.topLevelTypes = new SourceTypeBinding[typeLength];
	int count = 0;
	nextType: for (int i = 0; i < typeLength; i++) {
		TypeDeclaration typeDecl = types[i];
		if (this.environment.root.isProcessingAnnotations && this.environment.isMissingType(typeDecl.name))
			throw new SourceTypeCollisionException(); // resolved a type ref before APT generated the type
		recordSimpleReference(typeDecl.name); // needed to detect collision cases
		if (this.fPackage.hasType0Any(typeDecl.name)) {
//{ObjectTeams:
		  if (!typeDecl.isRole() || !(this.fPackage.getType0(typeDecl.name) instanceof MissingTypeBinding)) {
// orig:
			// if its an unresolved binding - its fixed up whenever its needed, see UnresolvedReferenceBinding.resolve()
			if (this.environment.root.isProcessingAnnotations)
				throw new SourceTypeCollisionException(); // resolved a type ref before APT generated the type
			// if a type exists, check that its a valid type
			// it can be a NotFound problem type if its a secondary type referenced before its primary type found in additional units
			// and it can be an unresolved type which is now being defined
			problemReporter().duplicateTypes(this.referenceContext, typeDecl);
			continue nextType;
// :giro
		  }
// SH}
		}

//{ObjectTeams: role files: (a) classes never match the name of the unit: prepended __OT__!
//                          (b) always check for matching file name (also protected), except for purely copied role files
		boolean isRole = typeDecl.isRole() ;
		if (   (typeDecl.modifiers & ClassFileConstants.AccPublic) != 0
			|| typeDecl.isRole()) // (b) check independently of publicness
		{
			char[] mainTypeName;
/* orig:
		if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
			char[] mainTypeName;
			if ((mainTypeName = this.referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
					&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
  :giro */
			if ((mainTypeName = this.referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of ICompilationUnit decided to return null
				&& !CharOperation.equals(mainTypeName, typeDecl.name)
				&& !(isRole && !typeDecl.isInterface()) // (a): ignore role classes
				&& !typeDecl.isPurelyCopied)            //      (has no file)
			{
			  if (typeDecl.isRole()) // (b) : more specific message
				problemReporter().roleFileMismatchingName(this.referenceContext, typeDecl);
			  else
// SH}
				problemReporter().publicClassMustMatchFileName(this.referenceContext, typeDecl);
				// tolerate faulty main type name (91091), allow to proceed into type construction
			}
		}

//{ObjectTeams: ROFI: new type OTClassScope:
		ClassScope child;
		if (typeDecl.isRole() || typeDecl.isTeam())
			child = OTClassScope.createTopLevelOTClassScope(this, typeDecl);
		else
//orig:
			child = new ClassScope(this, typeDecl);
// SH}
		SourceTypeBinding type = child.buildType(null, this.fPackage, accessRestriction);
		if (firstIsSynthetic && i == 0)
			type.modifiers |= ClassFileConstants.AccSynthetic;
		if (type != null)
			this.topLevelTypes[count++] = type;
	}

	// shrink topLevelTypes... only happens if an error was reported
	if (count != this.topLevelTypes.length)
		System.arraycopy(this.topLevelTypes, 0, this.topLevelTypes = new SourceTypeBinding[count], 0, count);

	if (this.referenceContext.moduleDeclaration != null) {
		this.module().completeIfNeeded(IUpdatableModule.UpdateKind.MODULE);
		this.referenceContext.moduleDeclaration.resolvePackageDirectives(this);
		this.module().completeIfNeeded(IUpdatableModule.UpdateKind.PACKAGE);
	}
}
//{ObjectTeams: open for Dependencies:
public
// SH}
void checkAndSetImports() {
	// TODO(SHMOD): verify: this block moved here from buildTypeBindings.
	// package resolving may require all modules to be known
	TypeDeclaration[] types = this.referenceContext.types;
	if (types != null) {
		for (int i = 0; i < types.length; i++) {
			TypeDeclaration typeDecl = types[i];
			if (this.fPackage != this.environment.defaultPackage && this.fPackage.getPackage(typeDecl.name, module()) != null) {
//{ObjectTeams: handle team-names and team-packages names of roles: is not a clash.
			  if(!TeamModel.isAnyTeam(typeDecl))
//Markus Witte}
				// if a package exists, it must be a valid package - cannot be a NotFound problem package
				// this is now a warning since a package does not really 'exist' until it contains a type, see JLS v2, 7.4.3
				problemReporter().typeCollidesWithPackage(this.referenceContext, typeDecl);
			}
		}
	}

	if (this.referenceContext.moduleDeclaration != null) {
		this.referenceContext.moduleDeclaration.resolveModuleDirectives(this);
	}

	if (this.referenceContext.imports == null) {
		this.imports = getDefaultImports();
//{ObjectTeams: may need an empty base import scope:
		setBaseImports(new ImportBinding[0], 0, new ImportReference[0]);
// SH}
		return;
	}

	// allocate the import array, add java.lang.* by default
	int numberOfStatements = this.referenceContext.imports.length;
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	resolvedImports[0] = getDefaultImports()[0];
	int index = 1;
//{ObjectTeams: collect base imports seperately:
	ImportBinding[] resolvedBaseImports = new ImportBinding[numberOfImports];
	ImportReference[] baseImports = new ImportReference[numberOfImports];
	int baseCount = 0;
// SH}

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic())
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
					continue nextImport;
		}

		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, this.currentPackageName))
				continue nextImport;

			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding() || (importReference.isStatic() && importBinding instanceof PackageBinding))
				continue nextImport;	// we report all problems in faultInImports()
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
		} else {
//{ObjectTeams: base imports are collected seperately
	  	  if (importReference.isBase()) {
 			baseImports[baseCount] = importReference;
			Binding importBinding = findImport(compoundName, compoundName.length, true/*allowDecapsulation*/);
			if (importBinding.problemId() == ProblemReasons.NotVisible) {
				final ReferenceBinding importedType = (ReferenceBinding) ((ProblemReferenceBinding)importBinding).closestMatch();
				problemReporter().setRechecker(new IProblemRechecker() {
													@Override
													public boolean shouldBeReported(IrritantSet[] foundIrritants) {
														return !CompilationUnitScope.this.isDecapsulationSuppressedAtRole(importedType, foundIrritants);
													}
												}).decapsulation(importReference);
				importBinding = importedType;
			}
			resolvedBaseImports[baseCount++] = new ImportBinding(compoundName, false, /*isBase:*/true, importBinding, importReference);
  		  } else
// SH}
			// resolve single imports only when the last name matches
			resolvedImports[index++] = new ImportBinding(compoundName, false, null, importReference);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	this.imports = resolvedImports;
//{ObjectTeams: base imports:
	setBaseImports(resolvedBaseImports, baseCount, baseImports);
// SH}
}

//{ObjectTeams: base imports
// helper: let a role suppress warnings for its base import:
protected boolean isDecapsulationSuppressedAtRole(ReferenceBinding importedType, IrritantSet[] foundIrritants) {
	if (this.referenceContext.types != null)
		for (TypeDeclaration type : this.referenceContext.types)
			if (isDecapsulationSuppressedAtRole(type.binding, importedType, foundIrritants))
				return true;

	return false;
}
protected boolean isDecapsulationSuppressedAtRole(ReferenceBinding currentType, ReferenceBinding importedType, IrritantSet[] foundIrritants) {
	if (currentType == null) return false;
	for (ReferenceBinding member : currentType.memberTypes()) {
		if (TypeBinding.equalsEquals(member.baseclass(), importedType)) {
			TypeDeclaration roleAst = member.roleModel.getAst();
			if (roleAst != null)
				return this.referenceContext.isWarningSuppressedAt(IProblem.Decapsulation, roleAst.sourceStart, roleAst.sourceEnd, foundIrritants);
		}
		if (isDecapsulationSuppressedAtRole(member, currentType, foundIrritants))
			return true;
	}
	return false;
}
private void setBaseImports(ImportBinding[] resolvedBaseImports, int baseCount, ImportReference[] refs)
{
	if (this.referenceContext == null || this.referenceContext.types == null || this.referenceContext.types.length == 0)
		return; // nothing we can do without a type
	OTClassScope otScope = null;
	TypeDeclaration firstType = this.referenceContext.types[0];
	if (this.referenceContext.types != null && this.referenceContext.types.length > 0)
	{
		if (firstType.scope != null && (firstType.scope instanceof OTClassScope))
			otScope = (OTClassScope)firstType.scope;
	}
	if (otScope != null) {
		// shrink resolvedBaseImports...
		if (resolvedBaseImports.length > baseCount)
			System.arraycopy(resolvedBaseImports, 0, resolvedBaseImports = new ImportBinding[baseCount], 0, baseCount);
		otScope.checkAndSetBaseImports(this.environment, refs, resolvedBaseImports);
	} else {
		for (ImportReference reference : refs) {
			if (reference != null)
				problemReporter().baseImportInRegularClass(firstType, reference);
		}
	}
}
// SH}
/**
 * Perform deferred check specific to parameterized types: bound checks, supertype collisions
 */
//{ObjectTeams: accessible for Dependencies:
public
// SH}
void checkParameterizedTypes() {
	if (compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) return;

	for (int i = 0, length = this.topLevelTypes.length; i < length; i++) {
		ClassScope scope = this.topLevelTypes[i].scope;
		scope.checkParameterizedTypeBounds();
		scope.checkParameterizedSuperTypeCollisions();
	}
}
/*
 * INTERNAL USE-ONLY
 * Innerclasses get their name computed as they are generated, since some may not
 * be actually outputed if sitting inside unreachable code.
 */
public char[] computeConstantPoolName(LocalTypeBinding localType) {
	if (localType.constantPoolName != null) {
		return localType.constantPoolName;
	}
	// delegates to the outermost enclosing classfile, since it is the only one with a global vision of its innertypes.

	if (this.constantPoolNameUsage == null)
		this.constantPoolNameUsage = new HashtableOfType();

	ReferenceBinding outerMostEnclosingType = localType.scope.outerMostClassScope().enclosingSourceType();

	// ensure there is not already such a local type name defined by the user
	int index = 0;
	char[] candidateName;
	boolean isCompliant15 = compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5;
	while(true) {
		if (localType.isMemberType()){
			if (index == 0){
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					localType.sourceName,
					'$');
			} else {
				// in case of collision, then member name gets extra $1 inserted
				// e.g. class X { { class L{} new X(){ class L{} } } }
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					'$',
					String.valueOf(index).toCharArray(),
					'$',
					localType.sourceName);
			}
		} else if (localType.isAnonymousType()){
			if (isCompliant15) {
				// from 1.5 on, use immediately enclosing type name
				candidateName = CharOperation.concat(
					localType.enclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			}
		} else {
			// local type
			if (isCompliant15) {
				candidateName = CharOperation.concat(
					CharOperation.concat(
						localType.enclosingType().constantPoolName(),
						String.valueOf(index+1).toCharArray(),
						'$'),
					localType.sourceName);
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					'$',
					String.valueOf(index+1).toCharArray(),
					'$',
					localType.sourceName);
			}
		}
		if (this.constantPoolNameUsage.get(candidateName) != null) {
			index ++;
		} else {
			this.constantPoolNameUsage.put(candidateName, localType);
			break;
		}
	}
	return candidateName;
}

//{ObjectTeams: ensure inherited local types may keep their names:
public void registerLocalType(char[] tsuperConstantPoolName, TypeDeclaration localType) {
	if (this.constantPoolNameUsage == null)
		this.constantPoolNameUsage = new HashtableOfType();
	ReferenceBinding outerMostEnclosingType = localType.scope.outerMostClassScope().enclosingSourceType();

	// replace outer-class prefix (strengthening): TODO(SH): multi-level?
	int dollarPos = CharOperation.indexOf('$', tsuperConstantPoolName);
	if (dollarPos > -1)
		tsuperConstantPoolName = CharOperation.concat(
				outerMostEnclosingType.constantPoolName,
				CharOperation.subarray(tsuperConstantPoolName, dollarPos+1, -1),
				'$');

	ReferenceBinding foundType = this.constantPoolNameUsage.get(tsuperConstantPoolName);
	if (foundType != null)
		assert TypeBinding.equalsEquals(foundType, localType.binding);
	else
		this.constantPoolNameUsage.put(tsuperConstantPoolName, localType.binding);
}
//if its a binary local type reserve its binary name:
public void registerBinaryNested(ReferenceBinding nestedBinding) {
	if (nestedBinding.isLocalType()) {
		if (this.constantPoolNameUsage == null)
			this.constantPoolNameUsage = new HashtableOfType();
		assert this.constantPoolNameUsage.get(nestedBinding.constantPoolName) == null;
		this.constantPoolNameUsage.put(nestedBinding.constantPoolName, nestedBinding);
	}
}
// SH}
//{ObjectTeams: open for Dependencies:
public
// SH}
void connectTypeHierarchy() {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].scope.connectTypeHierarchy();
}
//{ObjectTeams: support faultInTypes being controlled by Dependencies
// record when imports have been built:
private boolean faultInImportsDone = false;
// make accessible from Dependencies:
public
// SH}
void faultInImports() {
	if (this.tempImports != null)
		return; // faultInImports already in progress
	boolean unresolvedFound = false;
	// should report unresolved only if we are not suppressing caching of failed resolutions
	boolean reportUnresolved = !this.environment.suppressImportErrors;

	if (this.typeOrPackageCache != null && !this.skipCachingImports)
		return; // can be called when a field constant is resolved before static imports
	if (this.referenceContext.imports == null) {
		this.typeOrPackageCache = new HashtableOfObject(1);
		return;
	}
//{ObjectTeams: don't process twice
	if (this.faultInImportsDone)
		return;
	this.faultInImportsDone = true;
// SH}

	// collect the top level type names if a single type import exists
	int numberOfStatements = this.referenceContext.imports.length;
	HashtableOfType typesBySimpleNames = null;
	for (int i = 0; i < numberOfStatements; i++) {
		if ((this.referenceContext.imports[i].bits & ASTNode.OnDemand) == 0) {
			typesBySimpleNames = new HashtableOfType(this.topLevelTypes.length + numberOfStatements);
			for (int j = 0, length = this.topLevelTypes.length; j < length; j++)
				typesBySimpleNames.put(this.topLevelTypes[j].sourceName, this.topLevelTypes[j]);
			break;
		}
	}

	// allocate the import array, add java.lang.* by default
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(TypeConstants.JAVA_LANG, importReference.tokens) && !importReference.isStatic()) {
			numberOfImports--;
			break;
		}
	}
	this.tempImports = new ImportBinding[numberOfImports];
	this.tempImports[0] = getDefaultImports()[0];
	this.importPtr = 1;

	CompilerOptions compilerOptions = compilerOptions();
	boolean inJdtDebugCompileMode = compilerOptions.enableJdtDebugCompileMode;

	// keep static imports with normal imports until there is a reason to split them up
	// on demand imports continue to be packages & types. need to check on demand type imports for fields/methods
	// single imports change from being just types to types or fields
	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = this.referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < this.importPtr; j++) {
			ImportBinding resolved = this.tempImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0) && resolved.isStatic() == importReference.isStatic()) {
				if (CharOperation.equals(compoundName, resolved.compoundName)) {
					problemReporter().unusedImport(importReference); // since skipped, must be reported now
					continue nextImport;
				}
			}
		}
		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding()) {
				problemReporter().importProblem(importReference, importBinding);
				continue nextImport;
			}
			if (importBinding instanceof PackageBinding) {
				PackageBinding uniquePackage = ((PackageBinding)importBinding).getVisibleFor(module(), false);
				if (uniquePackage instanceof SplitPackageBinding && !inJdtDebugCompileMode) {
					SplitPackageBinding splitPackage = (SplitPackageBinding) uniquePackage;
					problemReporter().conflictingPackagesFromModules(splitPackage, module(), importReference.sourceStart, importReference.sourceEnd);
					continue nextImport;
				}
			}
			if (importReference.isStatic() && importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			}
			recordImportBinding(new ImportBinding(compoundName, true, importBinding, importReference));
		} else {
			Binding importBinding = findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, importReference.isStatic());
			if (importBinding instanceof SplitPackageBinding && !inJdtDebugCompileMode) {
				SplitPackageBinding splitPackage = (SplitPackageBinding) importBinding;
				int sourceEnd = (int)(importReference.sourcePositions[splitPackage.compoundName.length-1] & 0xFFFF);
				problemReporter().conflictingPackagesFromModules((SplitPackageBinding) importBinding, module(), importReference.sourceStart, sourceEnd);
				continue nextImport;
			}
			if (!importBinding.isValidBinding()) {
//{ObjectTeams: base imports may decapsulate:
			  if (   importBinding.problemId() == ProblemReasons.NotVisible
				  && importReference.isBase())
			  {
				// decapsulation is already checked in checkAndSetImports, proceed with the closest match
				importBinding = ((ProblemReferenceBinding)importBinding).closestMatch();
			  } else {
// orig:
				if (importBinding.problemId() == ProblemReasons.Ambiguous) {
					// keep it unless a duplicate can be found below
				} else {
					unresolvedFound = true;
					if (reportUnresolved) {
						problemReporter().importProblem(importReference, importBinding);
					}
					continue nextImport;
				}
// :giro
			  }
// SH}
			}
//{ObjectTeams: cannot import roles:
			if (   importBinding instanceof ReferenceBinding
				&& ((ReferenceBinding)importBinding).isRole()) {
				problemReporter().cannotImportRole(importReference, importBinding);
				continue nextImport;
			}
// SH}
			if (importBinding instanceof PackageBinding) {
				problemReporter().cannotImportPackage(importReference);
				continue nextImport;
			} else if (this.environment.useModuleSystem && importBinding instanceof ReferenceBinding) {
				PackageBinding importedPackage = ((ReferenceBinding) importBinding).fPackage;
				if (importedPackage != null) {
					if (!importedPackage.isValidBinding()) {
						problemReporter().importProblem(importReference, importedPackage);
						continue nextImport;
					}
					// re-get to find a possible split package:
					importedPackage = (PackageBinding) findImport(importedPackage.compoundName, false, true);
					if (importedPackage != null)
						importedPackage = importedPackage.getVisibleFor(module(), true);
					if (importedPackage instanceof SplitPackageBinding && !inJdtDebugCompileMode) {
						SplitPackageBinding splitPackage = (SplitPackageBinding) importedPackage;
						int sourceEnd = (int) importReference.sourcePositions[splitPackage.compoundName.length-1];
						problemReporter().conflictingPackagesFromModules(splitPackage, module(), importReference.sourceStart, sourceEnd);
						continue nextImport;
					}
				}
			}
			// all the code here which checks for valid bindings have been moved to the method
			// checkAndRecordImportBinding() since bug 361327
			if(checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName) == -1)
				continue nextImport;
			if (importReference.isStatic()) {
				// look for more static bindings being imported by single static import(bug 361327).
				// findSingleImport() finds fields first, followed by method and then type
				// So if a type is found, no fields and methods are available anyway
				// similarly when method is found, type may be available but no field available for sure
				if (importBinding.kind() == Binding.FIELD) {
					checkMoreStaticBindings(compoundName, typesBySimpleNames, Binding.TYPE | Binding.METHOD, importReference);
				} else if (importBinding.kind() == Binding.METHOD) {
					checkMoreStaticBindings(compoundName, typesBySimpleNames, Binding.TYPE, importReference);
				}
			}
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (this.tempImports.length > this.importPtr)
		System.arraycopy(this.tempImports, 0, this.tempImports = new ImportBinding[this.importPtr], 0, this.importPtr);
	this.imports = this.tempImports;
	this.tempImports = null;
	int length = this.imports.length;
	this.typeOrPackageCache = new HashtableOfObject(length);
	for (int i = 0; i < length; i++) {
		ImportBinding binding = this.imports[i];
		if (!binding.onDemand && binding.resolvedImport instanceof ReferenceBinding || binding instanceof ImportConflictBinding)
			this.typeOrPackageCache.put(binding.compoundName[binding.compoundName.length - 1], binding);
	}
	this.skipCachingImports = this.environment.suppressImportErrors && unresolvedFound;
}
public void faultInTypes() {
	faultInImports();
	if (this.referenceContext.moduleDeclaration != null) {
		this.referenceContext.moduleDeclaration.resolveTypeDirectives(this);
	} else if (this.referenceContext.currentPackage != null) {
		this.referenceContext.currentPackage.checkPackageConflict(this);
	}

	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].faultInTypesForFieldsAndMethods();
}
// this API is for code assist purpose
public Binding findImport(char[][] compoundName, boolean findStaticImports, boolean onDemand) {
	if(onDemand) {
		return findImport(compoundName, compoundName.length);
	} else {
		return findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, findStaticImports);
	}
}
private Binding findImport(char[][] compoundName, int length) {
//{ObjectTeams: optional third argument:
	return findImport(compoundName, length, false);
}
private Binding findImport(char[][] compoundName, int length, boolean allowDecapsulation) {
// SH}
	recordQualifiedReference(compoundName);
	ModuleBinding module = module();
	Binding binding = this.environment.getTopLevelPackage(compoundName[0]);
	int i = 1;
	foundNothingOrType: if (binding != null) {
		PackageBinding packageBinding = (PackageBinding) binding;
		while (i < length) {
//{ObjectTeams: For a team with team package we need to give precedence to the type:
			if (i == length -1 && module.canAccess(packageBinding)) {
				boolean save = packageBinding.dontRememberNotFoundType;
				packageBinding.dontRememberNotFoundType = true;
				try {
					ReferenceBinding type = packageBinding.getType(compoundName[i], module);
					if (type != null && type.isValidBinding() && type.isTeam()) {
						if (!type.canBeSeenBy(this.fPackage))
							return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
						return type;
					}
				} finally {
					packageBinding.dontRememberNotFoundType = save;
				}
			}
// SH}
			binding = packageBinding.getTypeOrPackage(compoundName[i++], module, i<length);
			if (binding instanceof ReferenceBinding && binding.problemId() == ProblemReasons.NotAccessible) {
				return this.environment.convertToRawType((TypeBinding) binding, false /*do not force conversion of enclosing types*/);
			}
			if (binding == null) {
				break foundNothingOrType;
			} else if (!binding.isValidBinding()) {
				if (binding.problemId() == ProblemReasons.Ambiguous && packageBinding instanceof SplitPackageBinding)
					return packageBinding; // pass the split package to the caller so they can report conflictingPackagesFromModules()
				binding = null;
				break foundNothingOrType;
			}
			if (!(binding instanceof PackageBinding)) {
				PackageBinding visibleFor = packageBinding.getVisibleFor(module, false); // filter out empty parent-packages
				if (visibleFor instanceof SplitPackageBinding)
					return visibleFor;
				break foundNothingOrType;
			}

			packageBinding = (PackageBinding) binding;
		}
		if (packageBinding.isValidBinding() && !module.canAccess(packageBinding))
			return new ProblemPackageBinding(compoundName, ProblemReasons.NotAccessible, this.environment);
		return packageBinding;
	}

	ReferenceBinding type;
	if (binding == null) {
		if (!module.isUnnamed()) {
			Binding inaccessible = this.environment.getInaccessibleBinding(compoundName, module);
			if (inaccessible != null)
				return inaccessible;
		}
		if (compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return problemType(compoundName, i, null);
		type = findType(compoundName[0], this.environment.defaultPackage, this.environment.defaultPackage);
		if (type == null || !type.isValidBinding())
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		i = 1; // reset to look for member types inside the default package type
	} else {
		type = (ReferenceBinding) binding;
	}

	while (i < length) {
		type = (ReferenceBinding)this.environment.convertToRawType(type, false /*do not force conversion of enclosing types*/); // type imports are necessarily raw for all except last
//{ObjectTeams:
	  if (!allowDecapsulation)
// SH}
		if (!type.canBeSeenBy(this.fPackage))
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), type, ProblemReasons.NotVisible);

		char[] name = compoundName[i++];
		// does not look for inherited member types on purpose, only immediate members
		type = type.getMemberType(name);
		if (type == null)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
	}
	if (!type.canBeSeenBy(this.fPackage))
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	return type;
}
private Binding findSingleImport(char[][] compoundName, int mask, boolean findStaticImports) {
	if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		if (compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4 && !this.referenceContext.isModuleInfo())
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0], this.environment.defaultPackage, this.fPackage);
		if (typeBinding == null)
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		return typeBinding;
	}

	if (findStaticImports)
		return findSingleStaticImport(compoundName, mask);
	return findImport(compoundName, compoundName.length);
}
private Binding findSingleStaticImport(char[][] compoundName, int mask) {
	Binding binding = findImport(compoundName, compoundName.length - 1);
	if (!binding.isValidBinding()) return binding;

	char[] name = compoundName[compoundName.length - 1];
	if (binding instanceof PackageBinding) {
		Binding temp = ((PackageBinding) binding).getTypeOrPackage(name, module(), false);
		if (temp != null && temp instanceof ReferenceBinding) // must resolve to a member type or field, not a top level type
			return new ProblemReferenceBinding(compoundName, (ReferenceBinding) temp, ProblemReasons.InvalidTypeForStaticImport);
		return binding; // cannot be a package, error is caught in sender
	}

	// look to see if its a static field first
	ReferenceBinding type = (ReferenceBinding) binding;
	FieldBinding field = (mask & Binding.FIELD) != 0 ? findField(type, name, null, true) : null;
	if (field != null) {
		if (field.problemId() == ProblemReasons.Ambiguous && ((ProblemFieldBinding) field).closestMatch.isStatic())
			return field; // keep the ambiguous field instead of a possible method match
		if (field.isValidBinding() && field.isStatic() && field.canBeSeenBy(type, null, this))
			return field;
	}

	// look to see if there is a static method with the same selector
	MethodBinding method = (mask & Binding.METHOD) != 0 ? findStaticMethod(type, name) : null;
	if (method != null) return method;

	type = findMemberType(name, type);
	if (type == null || !type.isStatic()) {
		if (field != null && !field.isValidBinding() && field.problemId() != ProblemReasons.NotFound)
			return field;
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotFound);
	}
	if (type.isValidBinding() && !type.canBeSeenBy(this.fPackage))
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	if (type.problemId() == ProblemReasons.NotVisible) // ensure compoundName is correct
		return new ProblemReferenceBinding(compoundName, ((ProblemReferenceBinding) type).closestMatch, ProblemReasons.NotVisible);
	return type;
}
// helper method for findSingleStaticImport()
private MethodBinding findStaticMethod(ReferenceBinding currentType, char[] selector) {
	if (!currentType.canBeSeenBy(this))
		return null;

	do {
		currentType.initializeForStaticImports();
		MethodBinding[] methods = currentType.getMethods(selector);
		if (methods != Binding.NO_METHODS) {
			for (int i = methods.length; --i >= 0;) {
				MethodBinding method = methods[i];
				if (method.isStatic() && method.canBeSeenBy(this.fPackage))
					return method;
			}
		}
	} while ((currentType = currentType.superclass()) != null);
	return null;
}
ImportBinding[] getDefaultImports() {
	// initialize the default imports if necessary... share the default java.lang.* import
	if (this.environment.root.defaultImports != null) return this.environment.root.defaultImports;

	Binding importBinding = this.environment.getTopLevelPackage(TypeConstants.JAVA);
	if (importBinding != null)
		importBinding = ((PackageBinding) importBinding).getTypeOrPackage(TypeConstants.JAVA_LANG[1], module(), false);

	if (importBinding == null || !importBinding.isValidBinding()) {
		// create a proxy for the missing BinaryType
		problemReporter().isClassPathCorrect(
				TypeConstants.JAVA_LANG_OBJECT,
			this.referenceContext,
			this.environment.missingClassFileLocation, false);
		BinaryTypeBinding missingObject = this.environment.createMissingType(null, TypeConstants.JAVA_LANG_OBJECT);
		importBinding = missingObject.fPackage;
	}

	return this.environment.root.defaultImports = new ImportBinding[] {new ImportBinding(TypeConstants.JAVA_LANG, true, importBinding, null)};
}
// NOT Public API
public final Binding getImport(char[][] compoundName, boolean onDemand, boolean isStaticImport) {
//{ObjectTeams: this method may be called in a Config-unaware context:
  try (Config config = Dependencies.setup(this, this.parser, environment(), false, true))
  {
// orig:
	if (onDemand)
		return findImport(compoundName, compoundName.length);
	return findSingleImport(compoundName, Binding.TYPE | Binding.FIELD | Binding.METHOD, isStaticImport);
// :giro
  }
//SH}
}

public int nextCaptureID() {
	return this.captureID++;
}

@Override
public ModuleBinding module() {
	if (!this.referenceContext.isModuleInfo() &&
			this.referenceContext.types == null &&
			this.referenceContext.currentPackage == null &&
			this.referenceContext.imports == null) {
		this.environment = this.environment.UnNamedModule.environment;
		return this.environment.UnNamedModule;
	}
	return super.module();
}

/* Answer the problem reporter to use for raising new problems.
*
* Note that as a side-effect, this updates the current reference context
* (unit, type or method) in case the problem handler decides it is necessary
* to abort.
*/
@Override
public ProblemReporter problemReporter() {
	ProblemReporter problemReporter = this.referenceContext.problemReporter;
	problemReporter.referenceContext = this.referenceContext;
	return problemReporter;
}

/*
What do we hold onto:

1. when we resolve 'a.b.c', say we keep only 'a.b.c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b.c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b.c'
-> This approach fails because every type is resolved in every onDemand import to
 detect collision cases... so the references could be 10 times bigger than necessary.

2. when we resolve 'a.b.c', lets keep 'a.b' & 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
-> This approach does not have a space problem but fails to handle collision cases.
 What happens if a type is added named 'a.b'? We would search for 'a' & 'b' but
 would not find a match.

3. when we resolve 'a.b.c', lets keep 'a', 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a', 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b'
OR 'a' -> '' & 'a'
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.

4. when we resolve 'a.b.c', lets keep 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b' in the simple name collection
OR 'a' -> 'a' in the simple name collection
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.
*/
void recordQualifiedReference(char[][] qualifiedName) {
	if (this.qualifiedReferences == null) return; // not recording dependencies

	int length = qualifiedName.length;
	if (length > 1) {
		recordRootReference(qualifiedName[0]);
		while (this.qualifiedReferences.add(qualifiedName)) {
			if (length == 2) {
				recordSimpleReference(qualifiedName[0]);
				recordSimpleReference(qualifiedName[1]);
				return;
			}
			length--;
			recordSimpleReference(qualifiedName[length]);
			System.arraycopy(qualifiedName, 0, qualifiedName = new char[length][], 0, length);
		}
	} else if (length == 1) {
		recordRootReference(qualifiedName[0]);
		recordSimpleReference(qualifiedName[0]);
	}
}
void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
	recordQualifiedReference(qualifiedEnclosingName);
	if (qualifiedEnclosingName.length == 0)
		recordRootReference(simpleName);
	recordSimpleReference(simpleName);
}
void recordReference(ReferenceBinding type, char[] simpleName) {
	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null)
		recordReference(actualType.compoundName, simpleName);
}
void recordRootReference(char[] simpleName) {
	if (this.rootReferences == null) return; // not recording dependencies

	this.rootReferences.add(simpleName);
}
void recordSimpleReference(char[] simpleName) {
	if (this.simpleNameReferences == null) return; // not recording dependencies

	this.simpleNameReferences.add(simpleName);
}
//{ObjectTeams: public for use by CopyInheritance:
public
// SH}
void recordSuperTypeReference(TypeBinding type) {
	if (this.referencedSuperTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && this.referencedSuperTypesSet.add(new ReferenceBindingSetWrapper(actualType)))
		this.referencedSuperTypes.add(actualType);
}
public void recordTypeConversion(TypeBinding superType, TypeBinding subType) {
	recordSuperTypeReference(subType); // must record the hierarchy of the subType that is converted to the superType
}
//{ObjectTeams: accessible for classes in org.eclipse.objectteams..:
public
// SH}
void recordTypeReference(TypeBinding type) {
	if (this.referencedTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null)
		this.referencedTypes.add(new ReferenceBindingSetWrapper(actualType));
}
void recordTypeReferences(TypeBinding[] types) {
	if (this.referencedTypes == null) return; // not recording dependencies
	if (types == null || types.length == 0) return;

	for (int i = 0, max = types.length; i < max; i++) {
		// No need to record supertypes of method arguments & thrown exceptions, just the compoundName
		// If a field/method is retrieved from such a type then a separate call does the job
		ReferenceBinding actualType = typeToRecord(types[i]);
		if (actualType != null)
			this.referencedTypes.add(new ReferenceBindingSetWrapper(actualType));
	}
}
Binding resolveSingleImport(ImportBinding importBinding, int mask) {
	if (importBinding.resolvedImport == null) {
		importBinding.resolvedImport = findSingleImport(importBinding.compoundName, mask, importBinding.isStatic());
//{ObjectTeams: base imports may decapsulate:
		if (   importBinding.resolvedImport.problemId() == ProblemReasons.NotVisible
			&& importBinding.isBase)
		{
			Binding result = ((ProblemReferenceBinding)importBinding.resolvedImport).closestMatch();
			importBinding.resolvedImport = null; // treat and report in faultInImports()
			return result; // work with the closest match for now.
		}
//SH}
		if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
			if (importBinding.resolvedImport.problemId() == ProblemReasons.Ambiguous)
				return importBinding.resolvedImport;
			if (this.imports != null) {
				ImportBinding[] newImports = new ImportBinding[this.imports.length - 1];
				for (int i = 0, n = 0, max = this.imports.length; i < max; i++)
					if (this.imports[i] != importBinding)
						newImports[n++] = this.imports[i];
				this.imports = newImports;
			}
			return null;
		}
	}
	return importBinding.resolvedImport;
}
public void storeDependencyInfo() {
	// add the type hierarchy of each referenced supertype
	// cannot do early since the hierarchy may not be fully resolved
	for (int i = 0; i < this.referencedSuperTypes.size; i++) { // grows as more types are added
		ReferenceBinding type = (ReferenceBinding) this.referencedSuperTypes.elementAt(i);
		this.referencedTypes.add(new ReferenceBindingSetWrapper(type));

		if (!type.isLocalType()) {
			ReferenceBinding enclosing = type.enclosingType();
			if (enclosing != null)
				recordSuperTypeReference(enclosing);
		}
		ReferenceBinding superclass = type.superclass();
		if (superclass != null)
			recordSuperTypeReference(superclass);
		ReferenceBinding[] interfaces = type.superInterfaces();
		if (interfaces != null)
			for (int j = 0, length = interfaces.length; j < length; j++)
				recordSuperTypeReference(interfaces[j]);
	}

	for (ReferenceBindingSetWrapper wrapper : this.referencedTypes) {
		ReferenceBinding type = wrapper.referenceBinding;
		if (!type.isLocalType())
			recordQualifiedReference(type.isMemberType()
				? CharOperation.splitOn('.', type.readableName())
				: type.compoundName);
	}

	int size = this.qualifiedReferences.size;
	char[][][] qualifiedRefs = new char[size][][];
	for (int i = 0; i < size; i++)
		qualifiedRefs[i] = this.qualifiedReferences.elementAt(i);
	this.referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

	size = this.simpleNameReferences.size;
	char[][] simpleRefs = new char[size][];
	for (int i = 0; i < size; i++)
		simpleRefs[i] = this.simpleNameReferences.elementAt(i);
	this.referenceContext.compilationResult.simpleNameReferences = simpleRefs;

	size = this.rootReferences.size;
	char[][] rootRefs = new char[size][];
	for (int i = 0; i < size; i++)
		rootRefs[i] = this.rootReferences.elementAt(i);
	this.referenceContext.compilationResult.rootReferences = rootRefs;
}
@Override
public String toString() {
	return "--- CompilationUnit Scope : " + new String(this.referenceContext.getFileName()); //$NON-NLS-1$
}
private ReferenceBinding typeToRecord(TypeBinding type) {
	if (type == null)
		return null;
	while (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType();

	switch (type.kind()) {
		case Binding.BASE_TYPE :
		case Binding.TYPE_PARAMETER :
		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE :
		case Binding.INTERSECTION_TYPE18: // constituents would have been recorded.
		case Binding.POLY_TYPE: // not a real type, will mutate into one, hopefully soon.
			return null;
		case Binding.PARAMETERIZED_TYPE :
		case Binding.RAW_TYPE :
			type = type.erasure();
	}
	ReferenceBinding refType = (ReferenceBinding) type;
	if (refType.isLocalType()) return null;
	return refType;
}
public void verifyMethods(MethodVerifier verifier) {
	for (int i = 0, length = this.topLevelTypes.length; i < length; i++)
		this.topLevelTypes[i].verifyMethods(verifier);
}
private void recordImportBinding(ImportBinding bindingToAdd) {
	if (this.tempImports.length == this.importPtr) {
		System.arraycopy(this.tempImports, 0, (this.tempImports = new ImportBinding[this.importPtr + 1]), 0, this.importPtr);
	}
	this.tempImports[this.importPtr++] = bindingToAdd;
}
/**
 * Checks additional bindings (methods or types) imported from a single static import.
 * Method is tried first, followed by type. If found, records them.
 * If in the process, import is flagged as duplicate, -1 is returned.
 * @param compoundName
 * @param typesBySimpleNames
 * @param mask
 * @param importReference
 */
private void checkMoreStaticBindings(
		char[][] compoundName,
		HashtableOfType typesBySimpleNames,
		int mask,
		ImportReference importReference) {
	Binding importBinding = findSingleStaticImport(compoundName, mask);
	if (!importBinding.isValidBinding()) {
		// only continue if the same kind's ambiguous binding is returned
		// may have found an ambiguous type when looking for field or method. Don't continue in that case
		if (importBinding.problemId() == ProblemReasons.Ambiguous) {
			// keep it unless a duplicate can be found below
			checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
		}
	} else {
		checkAndRecordImportBinding(importBinding, typesBySimpleNames, importReference, compoundName);
	}
	if (((mask & Binding.METHOD) != 0) && (importBinding.kind() == Binding.METHOD)) {
		// found method
		// type is left to be looked for
		// reset METHOD bit to enable lookup for only type
		mask &= ~Binding.METHOD;
		// now search for a type binding
		checkMoreStaticBindings(compoundName, typesBySimpleNames, mask, importReference);
	}
}
/**
 * Checks for duplicates. If all ok, records the importBinding
 * returns -1 when this import is flagged as duplicate.
 * @param importBinding
 * @param typesBySimpleNames
 * @param importReference
 * @param compoundName
 * @return -1 when this import is flagged as duplicate, importPtr otherwise.
 */
private int checkAndRecordImportBinding(
		Binding importBinding,
		HashtableOfType typesBySimpleNames,
		ImportReference importReference,
		char[][] compoundName) {
	ReferenceBinding conflictingType = null;
	if (importBinding instanceof MethodBinding) {
		conflictingType = (ReferenceBinding) getType(compoundName, compoundName.length);
		if (!conflictingType.isValidBinding() || (importReference.isStatic() && !conflictingType.isStatic()))
			conflictingType = null;
	}
	// collisions between an imported static field & a type should be checked according to spec... but currently not by javac
	final char[] name = compoundName[compoundName.length - 1];
	if (importBinding instanceof ReferenceBinding || conflictingType != null) {
		ReferenceBinding referenceBinding = conflictingType == null ? (ReferenceBinding) importBinding : conflictingType;
		ReferenceBinding typeToCheck = referenceBinding.problemId() == ProblemReasons.Ambiguous
			? ((ProblemReferenceBinding) referenceBinding).closestMatch
			: referenceBinding;
		if (importReference.isTypeUseDeprecated(typeToCheck, this))
			problemReporter().deprecatedType(typeToCheck, importReference);

		ReferenceBinding existingType = typesBySimpleNames.get(name);
		if (existingType != null) {
			// duplicate test above should have caught this case, but make sure
			if (TypeBinding.equalsEquals(existingType, referenceBinding)) {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302865
				// Check all resolved imports to see if this import qualifies as a duplicate
				for (int j = 0; j < this.importPtr; j++) {
					ImportBinding resolved = this.tempImports[j];
					if (resolved instanceof ImportConflictBinding) {
						ImportConflictBinding importConflictBinding = (ImportConflictBinding) resolved;
						if (TypeBinding.equalsEquals(importConflictBinding.conflictingTypeBinding, referenceBinding)) {
							if (!importReference.isStatic()) {
								// resolved is implicitly static
								problemReporter().duplicateImport(importReference);
								recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
							}
						}
					} else if (resolved.resolvedImport == referenceBinding) {
						if (importReference.isStatic() != resolved.isStatic()) {
							recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
						}
					}
				}
				return -1;
			}
			// either the type collides with a top level type or another imported type
			for (int j = 0, length = this.topLevelTypes.length; j < length; j++) {
				if (CharOperation.equals(this.topLevelTypes[j].sourceName, existingType.sourceName)) {
					problemReporter().conflictingImport(importReference);
					return -1;
				}
			}
			if (importReference.isStatic() && importBinding instanceof ReferenceBinding && compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8) {
				// 7.5.3 says nothing about collision of single static imports and JDK8 tolerates them, though use is flagged.
				for (int j = 0; j < this.importPtr; j++) {
					ImportBinding resolved = this.tempImports[j];
					if (resolved.isStatic() && resolved.resolvedImport instanceof ReferenceBinding && importBinding != resolved.resolvedImport) {
						if (CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
							ReferenceBinding type = (ReferenceBinding) resolved.resolvedImport;
							resolved.resolvedImport = new ProblemReferenceBinding(new char[][] { name }, type, ProblemReasons.Ambiguous);
							return -1;
						}
					}
				}
			}
			problemReporter().duplicateImport(importReference);
			return -1;
		}
		typesBySimpleNames.put(name, referenceBinding);
	} else if (importBinding instanceof FieldBinding) {
		for (int j = 0; j < this.importPtr; j++) {
			ImportBinding resolved = this.tempImports[j];
			// find other static fields with the same name
			if (resolved.isStatic() && resolved.resolvedImport instanceof FieldBinding && importBinding != resolved.resolvedImport) {
				if (CharOperation.equals(name, resolved.compoundName[resolved.compoundName.length - 1])) {
					if (compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8) {
						// 7.5.3 says nothing about collision of single static imports and JDK8 tolerates them, though use is flagged.
						FieldBinding field = (FieldBinding) resolved.resolvedImport;
						resolved.resolvedImport = new ProblemFieldBinding(field, field.declaringClass, name, ProblemReasons.Ambiguous);
						return -1;
					} else {
						problemReporter().duplicateImport(importReference);
						return -1;
					}
				}
			}
		}
	}
//{ObjectTeams: base imports are collected seperately
  if (!importReference.isBase())
//SH}

	if (conflictingType == null) {
		recordImportBinding(new ImportBinding(compoundName, false, importBinding, importReference));
	} else {
		recordImportBinding(new ImportConflictBinding(compoundName, importBinding, conflictingType, importReference));
	}
	return this.importPtr;
}
@Override
public boolean hasDefaultNullnessFor(int location, int sourceStart) {
	int nonNullByDefaultValue = localNonNullByDefaultValue(sourceStart);
	if (nonNullByDefaultValue != 0) {
		return (nonNullByDefaultValue & location) != 0;
	}
	if (this.fPackage != null)
		return (this.fPackage.getDefaultNullness() & location) != 0;
	return false;
}

@Override
public /* @Nullable */ Binding checkRedundantDefaultNullness(int nullBits, int sourceStart) {
	Binding target = localCheckRedundantDefaultNullness(nullBits, sourceStart);
	if (target != null) {
		return target;
	}
	if (this.fPackage != null) {
		return this.fPackage.findDefaultNullnessTarget(n -> n == nullBits);
	}

	return null;
}

public void registerInferredInvocation(Invocation invocation) {
	if (this.inferredInvocations == null)
		this.inferredInvocations = new ArrayList<>();
	this.inferredInvocations.add(invocation);
}
public void cleanUpInferenceContexts() {
	if (this.inferredInvocations == null)
		return;
	for (Invocation invocation : this.inferredInvocations)
		invocation.cleanUpInferenceContexts();
	this.inferredInvocations = null;
}
}
