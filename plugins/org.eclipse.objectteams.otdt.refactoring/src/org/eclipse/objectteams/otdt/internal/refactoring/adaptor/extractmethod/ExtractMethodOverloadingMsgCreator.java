package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod;

import org.eclipse.objectteams.otdt.internal.refactoring.corext.OTRefactoringCoreMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;

public class ExtractMethodOverloadingMsgCreator implements IOverloadingMessageCreator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.objectteams.otdt.refactoring.util.IOverloadingMessageCreator#createOverloadingMessage()
	 */
	public String createOverloadingMessage() {
		return OTRefactoringCoreMessages.getString("OTExtractMethodRefactoring.overloading"); //$NON-NLS-1$
	}

}
