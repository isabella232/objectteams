package org.eclipse.objectteams.otdt.internal.refactoring.util;

/**
 * 
 * This Interface is used for call back objects to provide refactoring specific
 * error messages concerning overloading.
 * 
 * @author Johannes Gebauer
 */
public interface IOverloadingMessageCreator {

	/**
	 * Creates a refactoring specific message for overloading warnings.
	 * 
	 * @return a refactoring specific message for overloading.
	 */
	public String createOverloadingMessage();

}
