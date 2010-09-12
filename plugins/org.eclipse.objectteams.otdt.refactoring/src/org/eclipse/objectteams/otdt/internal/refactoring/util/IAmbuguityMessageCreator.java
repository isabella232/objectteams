package org.eclipse.objectteams.otdt.internal.refactoring.util;

/**
 * 
 * This Interface is used for call back objects to provide refactoring specific
 * error messages concerning ambiguity.
 * 
 * @author Johannes Gebauer
 */
public interface IAmbuguityMessageCreator {

	/**
	 * Creates an ambiguous method specifier error message.
	 * 
	 * @return a refactoring specific message for an abmigious method specifier.
	 */
	public String createAmbiguousMethodSpecifierMsg();

}
