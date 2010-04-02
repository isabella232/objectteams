/**
 * 
 */
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;

import base org.eclipse.objectteams.otdt.core.PhantomType;

/**
 * 
 * This team provides the implementation for some unimplemented methods in
 * {@link PhantomType}, that would otherwise throw an
 * <code>UnsupportedOperationException</code>. The methods are used in
 * {@link RenameTypeAdaptor} to search for <code>PhantomType</code> references
 * with the java search. This team should be removed if the necessary methods
 * are implemented in the original {@link PhantomType}.
 * 
 * @author Johannes Gebauer
 */
public team class PhantomTypeAdaptor{
	protected class PhantomType playedBy PhantomType {

		IType getRealType() -> IType getRealType();

		@SuppressWarnings("basecall")
		callin ITypeParameter[] getTypeParameters() throws JavaModelException {
			return getRealType().getTypeParameters();
		}
		getTypeParameters <- replace getTypeParameters;
		
		@SuppressWarnings("basecall")
		callin boolean isResolved() {
			return false;
		}
		isResolved <- replace isResolved;
		
	}
}
