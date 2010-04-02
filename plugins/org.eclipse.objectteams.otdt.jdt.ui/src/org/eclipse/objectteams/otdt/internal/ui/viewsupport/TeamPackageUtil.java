/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamPackageUtil.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.viewsupport;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * Helper class to deal with team packages and role files.
 * @author stephan
 */
@SuppressWarnings("restriction")
public class TeamPackageUtil 
{
	/**
	 * If aPackage is a team package answer the corresponding team compilation unit
	 * 
	 * @param aPackage
	 * @return a team unit or null.
	 * @throws JavaModelException
	 */
	public static ICompilationUnit getTeamUnit(IPackageFragment aPackage) 
		throws JavaModelException 
	{
		ICompilationUnit[] units = aPackage.getCompilationUnits();
		if (units != null && units.length > 0) {
			IType firstType = JavaElementUtil.getMainType(units[0]);
			IOTType otType = OTModelManager.getOTElement(firstType);
			if (otType != null && otType.isRole())
			{
				String unitName = aPackage.getPath().lastSegment()+".java"; //$NON-NLS-1$
				IPackageFragment enclosingPackage = JavaElementUtil.getParentSubpackage(aPackage);
				if (enclosingPackage != null) {
					return enclosingPackage.getCompilationUnit(unitName);
				} else {
					// if parent is already the root, then we have a team in the default package, yacks.
					// need to travel one up, two down:
					IPackageFragmentRoot root = (IPackageFragmentRoot)aPackage.getParent();
					for (IJavaElement fragment : root.getChildren()) {
						if (fragment.getElementName() == IPackageFragment.DEFAULT_PACKAGE_NAME)
							for (IJavaElement element : ((IPackageFragment)fragment).getChildren()) 
								if (element.getElementType() == IJavaElement.COMPILATION_UNIT)
									if (element.getElementName().equals(unitName))
										return (ICompilationUnit)element;
					}
				}
			}
		}
		return null;
	}
}
