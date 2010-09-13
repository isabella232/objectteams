/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.objectteams.otdt.core.ext.OTDTPlugin;

// replace cu creation as to use JLS3 instead of JLS2.
public class ASTRewritingModifyingTest 
	extends	org.eclipse.jdt.core.tests.rewrite.modifying.ASTRewritingModifyingTest 
{

	public ASTRewritingModifyingTest(String name) {
		super(name);
	}

	public CompilationUnit createCU(ICompilationUnit unit, boolean resolveBindings) {
	
		try {
			ASTParser c = ASTParser.newParser(AST.JLS3);
			c.setSource(unit);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException();
		}
	}

	public CompilationUnit createCU(char[] source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		ASTParser c = ASTParser.newParser(AST.JLS3);
		c.setSource(source);
		ASTNode result = c.createAST(null);
		return (CompilationUnit) result;
	}

	protected IJavaProject createJavaProject(String projectName, String[] sourceFolders, String[] libraries, String output, String compliance) 
			throws CoreException
	{
		IJavaProject javaProject = super.createJavaProject(projectName, sourceFolders, libraries, output, compliance);
		IProject project = javaProject.getProject();
		IProjectDescription description = project.getDescription();
		description.setNatureIds(OTDTPlugin.createProjectNatures(description));
		project.setDescription(description, null);
		return javaProject;
	}
}
