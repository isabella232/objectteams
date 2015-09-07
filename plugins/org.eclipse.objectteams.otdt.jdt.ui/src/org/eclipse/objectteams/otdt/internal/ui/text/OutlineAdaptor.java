/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OutlineAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.text;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.Messages;
import org.eclipse.objectteams.otdt.ui.Util;

import base org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl;
import base org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl.OutlineContentProvider;
/**
 * This team adapts the quick outline (Ctrl-O and Ctrl-O+Ctrl-O)
 * - add method mappings
 * - add role files (only when also showing inherited members)
 * - hide generated methods/fields.
 * 
 * @author stephan
 * @since 0.9.18
 */
@SuppressWarnings({"restriction","decapsulation"})
public team class OutlineAdaptor 
{
	private static boolean activated = false;
	public static void doActivate() {
		if (!activated)
			new OutlineAdaptor().activate(ALL_THREADS);
		activated = true;
	}
	
	protected class OutlineContentProvider playedBy OutlineContentProvider
	{
		callin Object[] getChildren(Object element) 
		{
			boolean processingTeam = false;
			IOTType otElement = null;

			// start with result from base call:
			Object[] children = base.getChildren(element);
			
			// inspect element
			if (element instanceof IType) {
				IType type = (IType)element;
				processingTeam = OTModelManager.isTeam(type);
				otElement = OTModelManager.getOTElement(type);
				// note: base.getChildren(otElement) can not answer inherited members!
			}
			
			if (otElement == null)
				return children;
			
			// filter out generated members:
			children = Util.filterOTGenerated(children);
			
			if (processingTeam && showRoleFiles()) {
				// add role files:
				try {
					IType[] roleFiles = otElement.getRoleTypes(IOTType.ROLEFILE);
					if (roleFiles != null)
						return concatArrays(roleFiles, children);
				} catch (JavaModelException e) {
					// nop, did not improve
				}
			}					
			return children;
		}
		Object[] getChildren(Object element) <- replace Object[] getChildren(Object element);
		
		boolean showRoleFiles() -> boolean isShowingInheritedMembers();  
	
		private Object[] concatArrays(Object[] array1, Object[] array2) {
			int length1 = array1.length;
			int length2 = array2.length;
			if (length1 == 0)
				return array2;
			if (length2 == 0)
				return array1;
			Object[] result = new Object[length1+length2];
			System.arraycopy(array1, 0, result, 0, length1);
			System.arraycopy(array2, 0, result, length1, length2);
			return result;
		}
	}	
	
	/** Adapt the control as to display an adapted help text if needed. */
	protected class JavaOutlineInformationControl playedBy JavaOutlineInformationControl 
	{
		/** Compute the info text possibly appending a note on role files. */
		callin String getStatusFieldText() {
			String msg = base.getStatusFieldText();
			IJavaElement element = getInput();
			if (element != null) {
				if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
					// find main type:
					try {
						IType[] types = ((CompilationUnit)element).getTypes();
						if (types != null && types.length > 0)
							element = types[0];
					} catch (JavaModelException e) {
						return msg;// simply won't update text upon exception
					}
				}
				// update the info text only for teams:
				if (element.getElementType() == IJavaElement.TYPE && OTModelManager.isTeam((IType)element))
					msg += Messages.QuickOutline__and_role_files;
			}
			return msg;
		}
		getStatusFieldText <- replace getStatusFieldText;
		
		/** Trigger re-computing the info text once the input element is set. */
		void updateInfoText() {
			updateStatusFieldText();
		}
		updateInfoText <- after setInput;

		// Callouts:
		IJavaElement getInput()      -> get IJavaElement fInput;
		void updateStatusFieldText() -> void updateStatusFieldText();
	}
}
