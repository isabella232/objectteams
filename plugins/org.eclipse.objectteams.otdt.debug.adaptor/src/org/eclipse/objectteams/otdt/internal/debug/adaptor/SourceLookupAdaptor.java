/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoFiBreakpointTests.java 18812 2008-07-27 18:01:43Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

import base org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
//import base org.eclipse.pde.internal.ui.launcher.PDESourceLookupQuery;

/**
 * This team makes the PDE source lookup aware of role files.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class SourceLookupAdaptor {

	// field stored in the team, because intercepted base method generateSourceName() has static context.
	Object currentElement;
	
//	/** 
//	 * This role is needed because its base class over-eagerly discards
//	 * the names of inner classes when looking for the source file. 
//	 */
//	protected class PDESourceLookupQuery playedBy PDESourceLookupQuery {
//		
//		@SuppressWarnings("decapsulation")
//		Object getFElement() -> get Object fElement;
//		
//		keepElement <- before run;
//		void keepElement() {
//			currentElement = getFElement();			
//		}
//		
//		@SuppressWarnings("decapsulation")
//		String generateSourceName(String qualifiedTypeName) <- replace String generateSourceName(String qualifiedTypeName);
//
//		@SuppressWarnings("basecall")
//		static callin String generateSourceName(String qualifiedTypeName) {
//			try {
//				return JavaDebugUtils.getSourceName(currentElement);
//			} catch (CoreException e) {
//				OTDebugAdaptorPlugin.getDefault().getLog().log(new Status(Status.ERROR, OTDebugAdaptorPlugin.PLUGIN_ID, "Source lookup failed", e));
//				return base.generateSourceName(qualifiedTypeName);
//			}
//		}
//	}
	
	protected class JavaDebugUtils playedBy JavaDebugUtils 
	{
		String getSourceName(Object object) -> String getSourceName(Object object);		
		
		// FIXME(SH): workaround, problem with overload between callin and inferred callout
		IType resolveType(String qualifiedName, IJavaElement javaElement) 
		-> IType resolveType(String qualifiedName, IJavaElement javaElement);
	
		IType resolveType(IJavaType type) <- replace IType resolveType(IJavaType type);

		@SuppressWarnings({ "basecall", "inferredcallout" })
		static callin IType resolveType(IJavaType type) throws CoreException {
			// copied from base method:
	    	IJavaElement element = resolveJavaElement(type, type.getLaunch());
	    	if (element != null ) {
	    		IType result = resolveType(type.getName(), element);
	    		// start OT-adaptation: check result:
	    		if (result != null && result.exists())
	    			return result;
	    		// the given compilation unit doesn't have a type `type.getName()`
	    		try {
		    		ICompilationUnit resolvedCU = (ICompilationUnit) element;
		    		String typeName = type.getName();
		    		int lastDollar = typeName.lastIndexOf('$');
		    		if (lastDollar != -1) {
		    			// find a type that is (a) superclass of `type` and (b) contained in `resolvedCU`
		    			String enclosingName = typeName.substring(0, lastDollar);
		    			IJavaProject javaProject = resolvedCU.getJavaProject();
						IType enclosingType = javaProject.findType(enclosingName);
		    			while (enclosingType != null) {
		    				if (resolvedCU.equals(enclosingType.getAncestor(IJavaElement.COMPILATION_UNIT))) {
		    					// got the enclosing team, now find the corresponding role:
		    					String roleName = typeName.substring(lastDollar+1);
		    					if (roleName.startsWith(IOTConstants.OT_DELIM))
		    						roleName = roleName.substring(IOTConstants.OT_DELIM_LEN);
								return enclosingType.getType(roleName);
		    				}
		    				String[][] superclassName = enclosingType.resolveType((enclosingType.getSuperclassName()));
							enclosingType = javaProject.findType(superclassName[0][0], superclassName[0][1]);
		    			}
		    		}
	    		} catch (Exception e) {
	    			return null;
	    		}
	    	}
	    	return null;
		}
	}
}
