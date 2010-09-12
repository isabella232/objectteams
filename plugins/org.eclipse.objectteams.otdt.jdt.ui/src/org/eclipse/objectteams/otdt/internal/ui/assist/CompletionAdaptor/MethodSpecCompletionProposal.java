/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodSpecCompletionProposal.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.objectteams.otdt.internal.ui.util.OTStubUtility;
import org.eclipse.swt.graphics.Image;

/**
 * Rewrite based completion proposal for inserting a RHS method spec.
 * Adjusts style of method spec (short/long) to the existing LHS method spec.
 * (No linked mode in this case).
 * 
 * @author stephan
 * @since 1.1.6
 */
protected class MethodSpecCompletionProposal extends MethodMappingCompletionProposal
{
	
	public MethodSpecCompletionProposal(IJavaProject 	   jProject,
								        ICompilationUnit   cu,
								        CompletionProposal proposal,
								        String[]		   paramTypes,
									    int                length,
									    String             displayName,
									    Image              image) 
	{
		super(jProject, cu, proposal, paramTypes, length, displayName, image);
	}
	
	boolean setupRewrite(ICompilationUnit                 iCU,
			             ASTRewrite                       rewrite,
			             ImportRewrite                    importRewrite,
			             ITypeBinding                     roleBinding,
			             ITypeBinding                     baseBinding,
			             ASTNode                          type,
			             AbstractMethodMappingDeclaration partialMapping,
			             ChildListPropertyDescriptor      bodyProperty) 
			throws CoreException
	{
		if (partialMapping == null)
			return false;
		// find base method:
		IMethodBinding method= findMethod(baseBinding, fMethodName, fParamTypes);
		if (method == null)
			return false;
		// create and insert:
		MethodSpec spec= OTStubUtility
					.createMethodSpec(iCU, rewrite, importRewrite, method, partialMapping.hasSignature());
		if (partialMapping instanceof CalloutMappingDeclaration)
			rewrite.set(partialMapping, CalloutMappingDeclaration.BASE_MAPPING_ELEMENT_PROPERTY, spec, null);
		else 
			rewrite.getListRewrite(partialMapping, CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY)
					.insertFirst(spec, null);
		return true;	
	}

}
