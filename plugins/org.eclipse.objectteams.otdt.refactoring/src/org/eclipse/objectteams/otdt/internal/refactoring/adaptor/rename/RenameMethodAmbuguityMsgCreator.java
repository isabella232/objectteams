package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.rename;

import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;

public class RenameMethodAmbuguityMsgCreator implements IAmbuguityMessageCreator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.objectteams.otdt.refactoring.util.IAmbuguityMessageCreator#createAmbiguousMethodSpecifierMsg()
	 */
	public String createAmbiguousMethodSpecifierMsg() {
		return "Refactoring cannot be performed! There would be an ambiguous method specifier in a method binding after renaming!";
	}

}
