/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PretendAllRoleFilesArePublic.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.viewsupport;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccProtected;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccVisibilityMASK;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccRole;


import base org.eclipse.jdt.internal.core.search.matching.QualifiedTypeDeclarationPattern;

/**
 * This team helps ViewAdaptor.OTClassLabelDecorator to handle all role files,
 * despite that fact that {@link org.eclipse.jdt.internal.ui.InterfaceIndicatorLabelDecorator#getOverlayWithSearchEngine}
 * only considers public types.
 * 
 * @author stephan
 * @since 1.2.2
 */
@SuppressWarnings("restriction")
public team class PretendAllRoleFilesArePublic 
{
	final static int AccProtectedRole = AccRole | AccProtected;
	
	/**
	 * When decoding a type declaration pattern (as used by InterfaceIndicatorLabelDecorator),
	 * tweak protected roles to public.
	 */
	protected class PatternAdaptor playedBy QualifiedTypeDeclarationPattern {

		void setModifiers(int modifiers) -> set int modifiers;
		int getModifiers() -> get int modifiers;

		adjustModifiersForRole <- after decodeIndexKey
			base when ((base.modifiers & AccProtectedRole) == AccProtectedRole);

		private void adjustModifiersForRole() {
			int modifiers = getModifiers();
			modifiers &= ~AccVisibilityMASK;
			modifiers |= AccPublic;
			setModifiers(modifiers);
		}
	}
}
