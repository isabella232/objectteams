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
 * $Id: ProposalAdaptor.java 23495 2010-02-05 23:15:16Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.core;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import base org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;

public team class ProposalAdaptor {

	/** Just decapsulate one private field ;-) */
	protected class GuessingProposal playedBy ParameterGuessingProposal {

		@SuppressWarnings("decapsulation")
		protected 
		ICompletionProposal[][] getFChoices() -> get ICompletionProposal[][] fChoices;
		
	}
	/** API */
	public ICompletionProposal[][] getChoices(ParameterGuessingProposal as GuessingProposal proposal) {
		return proposal.getFChoices();
	}
	
}
