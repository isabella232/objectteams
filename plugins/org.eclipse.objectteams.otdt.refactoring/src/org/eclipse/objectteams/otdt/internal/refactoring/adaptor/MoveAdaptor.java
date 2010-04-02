/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MoveAdaptor.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

import base org.eclipse.jdt.internal.corext.refactoring.structure.MoveStaticMembersProcessor;

/**
 * Adjust rules for move static member with respect to OT/J.
 * 
 * @author stephan
 * @since 1.1.7
 */
@SuppressWarnings("restriction")
public team class MoveAdaptor 
{
	@SuppressWarnings("decapsulation")
	protected class MoveStaticMembersProcessor playedBy MoveStaticMembersProcessor {

		IType getFDestinationType() -> get IType fDestinationType;

		RefactoringStatus checkDestinationType() <- replace RefactoringStatus checkDestinationType();
		callin RefactoringStatus checkDestinationType() 
				throws JavaModelException 
		{
			RefactoringStatus result= base.checkDestinationType();
			if (result.isOK())
				return result;
			// check for error that is actually tolerable for roles:
			IOTType otType= OTModelManager.getOTElement(getFDestinationType());
			if (otType != null) {
				if (otType.isRole()) 
				{
					boolean filtered= false;
					RefactoringStatus newStatus= new RefactoringStatus();

					for (RefactoringStatusEntry entry : result.getEntries())
						if (isTolerableError(entry))							
							filtered= true;// filter out: roles may indeed hold static members (in contrast to nested types in Java)
						else
							newStatus.addEntry(entry);

					if (filtered)
						return newStatus;
				}
			}
			return result;
		}
		boolean isTolerableError(RefactoringStatusEntry entry) {
			if (entry.isError()) 
				return entry.getMessage().equals(RefactoringCoreMessages.MoveMembersRefactoring_static_declaration);
			return false;
		}
	}
}
