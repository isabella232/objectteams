/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OpenTeamWizardAction.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.jdt.ui.actions.AbstractOpenWizardAction;
import org.eclipse.ui.INewWizard;

/**
 * Action for the team wizard. This action is only used in cheatsheets.
 *
 * @author brcan
 * @version $Id: OpenTeamWizardAction.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class OpenTeamWizardAction extends AbstractOpenWizardAction
{
	public OpenTeamWizardAction() {
//		WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}
/*	km: not needed anymore?	
	public OpenTeamWizardAction(String label, Class[] acceptedTypes) {
		super(label, acceptedTypes, false);
//		WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_CLASS_WIZARD_ACTION);
	}
*/
	protected INewWizard createWizard() { 
		return new NewTeamCreationWizard(); 
	}
	
//	 FIXME(KM): method isnt override anymore - what to use instead? outcommented to get compiled
/*	protected boolean shouldAcceptElement(Object obj) { 
		return isOnBuildPath(obj) && !isInArchive(obj);
	}
*/
}
