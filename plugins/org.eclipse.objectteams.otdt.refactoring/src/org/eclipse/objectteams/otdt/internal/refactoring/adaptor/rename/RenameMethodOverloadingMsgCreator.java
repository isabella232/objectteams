package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.rename;

import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;

public class RenameMethodOverloadingMsgCreator implements IOverloadingMessageCreator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.objectteams.otdt.refactoring.util.IOverloadingMessageCreator#createOverloadingMessage()
	 */
	public String createOverloadingMessage() {
		return "Renamed method will be overloaded after refactoring!";
	}

}
