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
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * OTDT changes:
 *
 * What: Support the new modifier "team" for package declarations.
 *
 * What: Support "import base".
 *
 * @version $Id: ImportReference.java 19881 2009-04-13 23:35:46Z stephan $
 */
public class ImportReference extends ASTNode {

	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers; // 1.5 addition for static imports
	public Annotation[] annotations;
	// star end position
	public int trailingStarPosition;

//{ObjectTeams: new queries:
	public boolean isTeam() {
		return (this.modifiers & ExtraCompilerModifiers.AccTeam) != 0;
	}
	public boolean isBase() {
		return (this.modifiers & ExtraCompilerModifiers.AccBase) != 0;
	}
// SH}


	public ImportReference(
			char[][] tokens,
			long[] sourcePositions,
			boolean onDemand,
			int modifiers) {

		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		if (onDemand) {
			this.bits |= ASTNode.OnDemand;
		}
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length-1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
		this.modifiers = modifiers;
	}

	public boolean isStatic() {
		return (this.modifiers & ClassFileConstants.AccStatic) != 0;
	}

	public char[][] getImportName() {
		return this.tokens;
	}

	public char[] getSimpleName() {
		return this.tokens[this.tokens.length - 1];
	}

	public void checkPackageConflict(CompilationUnitScope scope) {
		ModuleBinding module = scope.module();
		PackageBinding visiblePackage = module.getVisiblePackage(this.tokens);
		if (visiblePackage instanceof SplitPackageBinding) {
			Set<ModuleBinding> declaringMods = new HashSet<>();
			for (PackageBinding incarnation : ((SplitPackageBinding) visiblePackage).incarnations) {
				if (incarnation.enclosingModule != module && module.canAccess(incarnation))
					declaringMods.add(incarnation.enclosingModule);
			}
			if (!declaringMods.isEmpty()) {
				CompilerOptions compilerOptions = scope.compilerOptions();
				boolean inJdtDebugCompileMode = compilerOptions.enableJdtDebugCompileMode;
				if (!inJdtDebugCompileMode) {
					scope.problemReporter().conflictingPackagesFromOtherModules(this, declaringMods);
				}
			}
		}
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {

		return print(indent, output, true);
	}

	public StringBuffer print(int tab, StringBuffer output, boolean withOnDemand) {
		/* when withOnDemand is false, only the name is printed */
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		if (withOnDemand && ((this.bits & ASTNode.OnDemand) != 0)) {
			output.append(".*"); //$NON-NLS-1$
		}
		return output;
	}

	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {
		// annotations are traversed during the compilation unit traversal using a class scope
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
