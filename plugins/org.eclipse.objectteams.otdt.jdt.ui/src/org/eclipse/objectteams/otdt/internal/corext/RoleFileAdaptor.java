/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleFileAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.corext;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

import base org.eclipse.jdt.internal.corext.dom.Bindings;

/**
 * This team tells the jdt.ui how to handle role files.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class RoleFileAdaptor 
{
	/** Adapt class Bindings. */
	protected class BindingsAdaptor playedBy Bindings 
	{
		/** Trigger: repair lookup of enclosing type for role files. */
		getBindingOfParentType <- replace getBindingOfParentType;
		
		/** 
		 * Original method assumes parent of a nestedType is the enclosing type.
		 * Unfortunately the dom does not give direct access from a role file
		 * to its enclosing team. 
		 */
		@SuppressWarnings({"rawtypes","basecall"}) // base call not issued if replacing behaviour executes
		static callin ITypeBinding getBindingOfParentType(ASTNode node) 
		{
			if (node.getNodeType() == ASTNode.COMPILATION_UNIT) {
				List types = ((CompilationUnit)node).types();
				for (int i=0; i<types.size(); i++) {
					ASTNode type = ((ASTNode)types.get(i));
					if (type.getNodeType() == ASTNode.ROLE_TYPE_DECLARATION)
						return getTeamOfRoleFile((RoleTypeDeclaration)type);
				}
			}
			return base.getBindingOfParentType(node);
		}
		/** Retrieve the enclosing team of a role file. */
		private static ITypeBinding getTeamOfRoleFile(RoleTypeDeclaration roleType) {
			return roleType.resolveBinding().getDeclaringClass();
		}
	}
	
	/**
	 * Fetch all names of base classes referenced from the given CU.
	 * @param astRoot start searching packages from here.
	 * @return list of simple base class names.
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getRoFiBaseClassNames(CompilationUnit astRoot) {
		ArrayList<String> result = new ArrayList<String>();
		List<AbstractTypeDeclaration> types = astRoot.types();
		for (AbstractTypeDeclaration type : types) {
			if (type.isTeam()) {
				ITypeBinding typeBinding = type.resolveBinding();
				String teamName = ""; //$NON-NLS-1$
				if (typeBinding != null) {
					teamName = typeBinding.getQualifiedName();
				} else {
					PackageDeclaration currentPackage = astRoot.getPackage();
					if (currentPackage != null)
						teamName = currentPackage.getName().getFullyQualifiedName()+'.';
					teamName += type.getName().getIdentifier();
				}
				IJavaProject prj = astRoot.getJavaElement().getJavaProject();
				try {
					for (IPackageFragmentRoot roots : prj.getPackageFragmentRoots()) {
						IPackageFragment pkg = roots.getPackageFragment(teamName);
						if (pkg.exists())
							for (IJavaElement cu : pkg.getChildren())
								if (cu.getElementType() == IJavaElement.COMPILATION_UNIT)
									for(IType roleType : ((org.eclipse.jdt.internal.core.CompilationUnit)cu).getTypes()) 
									{
										IOTType ottype = OTModelManager.getOTElement(roleType);
										if (ottype != null && ottype.isRole()) {
											String baseClass = ((IRoleType)ottype).getBaseclassName();
											if (baseClass != null) {
												// always remember as simple name:
												int lastDot = baseClass.lastIndexOf('.');
												if (lastDot > -1)
													baseClass = baseClass.substring(lastDot+1);
												result.add(baseClass);
											}
										}
									}
					}
				} catch (JavaModelException e) {
					// couldn't read team package, skip.
				}
			}
		}
		return result;
	}
}
