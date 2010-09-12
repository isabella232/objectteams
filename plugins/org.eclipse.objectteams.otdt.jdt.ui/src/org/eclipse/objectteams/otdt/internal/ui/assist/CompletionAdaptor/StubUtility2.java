/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: StubUtility2.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	   Technical University Berlin - Initial API and implementation
 *     IBM Corporation - implementation of individual method bodies
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.assist.CompletionAdaptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;

/**
 * Add new functions to StubUtility2, accessing otherwise invisible helper functions.
 * @author stephan
 * @since 1.1.2
 */
@SuppressWarnings({ "unchecked", "rawtypes"/*parameter-less lists from DOM*/, "restriction", "decapsulation"/*final baseclass + callout-decapsulation*/ })
protected class StubUtility2 playedBy StubUtility2 
{

	// CALLOUT INTERFACE:
	List createParameters(IJavaProject project, ImportRewrite imports, ImportRewriteContext context, AST ast, IMethodBinding binding, MethodDeclaration decl) 
		-> List createParameters(IJavaProject project, ImportRewrite imports, ImportRewriteContext context, AST ast, IMethodBinding binding, MethodDeclaration decl);

	void findUnimplementedInterfaceMethods(ITypeBinding typeBinding, HashSet visited, ArrayList allMethods, IPackageBinding currPack, ArrayList toImplement) 
		-> void findUnimplementedInterfaceMethods(ITypeBinding typeBinding, HashSet visited, ArrayList allMethods, IPackageBinding currPack, ArrayList toImplement);
	IMethodBinding findMethodBinding(IMethodBinding method, List allMethods) 
		-> IMethodBinding findMethodBinding(IMethodBinding method, List allMethods);
	
	/** 
	 * This callin adds treatment of implicit inheritance including phantom roles
	 * to it's base version.
	 */
	IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput) 
		<- replace IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput);
	@SuppressWarnings("basecall")
	callin static IMethodBinding[] getUnimplementedMethods(ITypeBinding typeBinding, boolean implementAbstractsOfInput) {
		// COPY&PASTE from base version:
		ArrayList allMethods= new ArrayList();
		ArrayList toImplement= new ArrayList();

		IMethodBinding[] typeMethods= typeBinding.getDeclaredMethods();
		for (int i= 0; i < typeMethods.length; i++) {
			IMethodBinding curr= typeMethods[i];
			int modifiers= curr.getModifiers();
			if (!curr.isConstructor() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
//{ObjectTeams: never enter copied methods (have no java element)
			  if (!curr.isCopied())
// SH}
				allMethods.add(curr);
			}
		}
//{ObjectTeams: direct tsuper roles:
		ITypeBinding[] tsupers= typeBinding.getSuperRoles();
		  if (tsupers != null)
			  for (ITypeBinding tsuperRole : tsupers) 
				  findVisibleVirtualMethods(tsuperRole, allMethods);
// SH}
		
		ITypeBinding superClass= typeBinding.getSuperclass();
		while (superClass != null) {
//{ObjectTeams: watch for phantom roles (which don't exist):
		  if (superClass.getJavaElement().exists()) {
			  // extracted orig:
			  findVisibleVirtualMethods(superClass, allMethods);
		  } else {
			  // proceed to the existing super of a phantom role:
			  tsupers= superClass.getSuperRoles();
			  if (tsupers != null)
				  for (ITypeBinding tsuperRole : tsupers) 
					  findVisibleVirtualMethods(tsuperRole, allMethods);
		  }
// SH}
		  superClass= superClass.getSuperclass();
		}

		for (int i= 0; i < allMethods.size(); i++) {
			IMethodBinding curr= (IMethodBinding) allMethods.get(i);
			int modifiers= curr.getModifiers();
			if ((Modifier.isAbstract(modifiers) || curr.getDeclaringClass().isInterface()) && (implementAbstractsOfInput || typeBinding != curr.getDeclaringClass())) {
				// implement all abstract methods
				toImplement.add(curr);
			}
		}

		HashSet visited= new HashSet();
		ITypeBinding curr= typeBinding;
		while (curr != null) {
			ITypeBinding[] superInterfaces= curr.getInterfaces();
			for (int i= 0; i < superInterfaces.length; i++) {
//{ObjectTeams: consider that a role class part finds its super interfaces in the interface part:
				if (curr.isClassPartOf(superInterfaces[i])) {
					// do consider transitively inherited methods:
					ITypeBinding[] superSuperInterfaces = superInterfaces[i].getInterfaces();
					for (int j = 0; j < superSuperInterfaces.length; j++)
						findUnimplementedInterfaceMethods(superSuperInterfaces[j], visited, allMethods, typeBinding.getPackage(), toImplement);
					// but don't add methods from our own ifc-part (occurs when role extends non-role).
					continue; 
				}
// SH}
				findUnimplementedInterfaceMethods(superInterfaces[i], visited, allMethods, typeBinding.getPackage(), toImplement);
			}
			curr= curr.getSuperclass();
		}

		return (IMethodBinding[]) toImplement.toArray(new IMethodBinding[toImplement.size()]);
	}
	// COPY&PASTE: orig extracted as helper from base of above:
	private static void findVisibleVirtualMethods(ITypeBinding typeBinding, ArrayList allMethods) {
		IMethodBinding[] typeMethods= typeBinding.getDeclaredMethods();
		for (int i= 0; i < typeMethods.length; i++) {
			IMethodBinding curr= typeMethods[i];
			int modifiers= curr.getModifiers();
			if (!curr.isConstructor() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
			  if (!curr.isCopied()) // new check for OT
				if (findMethodBinding(curr, allMethods) == null) {
					allMethods.add(curr);
				}
			}
		}		
	}

}

