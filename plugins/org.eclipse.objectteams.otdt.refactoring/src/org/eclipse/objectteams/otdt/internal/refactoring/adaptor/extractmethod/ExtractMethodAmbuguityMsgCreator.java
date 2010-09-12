package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod;

import org.eclipse.objectteams.otdt.internal.refactoring.corext.OTRefactoringCoreMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;

public class ExtractMethodAmbuguityMsgCreator implements IAmbuguityMessageCreator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator#createAmbiguousMethodSpecifierMsg()
	 */
	public String createAmbiguousMethodSpecifierMsg() {
		return OTRefactoringCoreMessages.getString("OTExtractMethodRefactoring.ambiguous_method_specifier"); //$NON-NLS-1$
	}

}
