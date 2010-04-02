/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ImportMatchLocatorParser.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.objectteams.otdt.internal.core.search.matching.ReferenceToTeamLocator;

class ImportMatchLocatorParser extends MatchLocatorParser {

	boolean reportImportMatch;

protected ImportMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
	super(problemReporter, locator);
	this.reportImportMatch = this.patternFineGrain == 0 || (this.patternFineGrain & IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE) != 0;
}
protected void consumeStaticImportOnDemandDeclarationName() {
	super.consumeStaticImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	super.consumeSingleStaticImportDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	super.consumeSingleTypeImportDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	super.consumeTypeImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
//{ObjectTeams: consider type references in team packages
protected void consumePackageDeclarationNameWithModifiers()
{
	super.consumePackageDeclarationNameWithModifiers();
	if (currentIsRole)
	{
	    if (   this.patternLocator instanceof TypeReferenceLocator
	    	|| this.patternLocator instanceof ReferenceToTeamLocator)
	    {
	        ImportReference importRef = this.compilationUnit.currentPackage;
	        int level = this.patternLocator.matchLevel(importRef);
			if (level != 0)
	            this.nodeSet.addMatch(importRef, level);
	    }
	}
}
// SH}
}
