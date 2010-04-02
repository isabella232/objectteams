package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.pullup;

import org.eclipse.jdt.core.IType;

import base org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;

/**
 * @author Johannes Gebauer
 * 
 *         This team is by default deactivated. It can be activated to suppress
 *         the <code>UnsupportedOperationException</code> on
 *         {@link OTTypeHierarchy#getSuperclass(IType)}. The adaptor is useful
 *         to reuse jdt functionality that is not aware of multiple
 *         superclasses.
 */
@SuppressWarnings("restriction")
public team class OTTypeHierarchyAdaptor {

	/**
	 * Prevents {@link OTTypeHierarchy#getSuperclass(IType)} calls on
	 * OTTypeHierarchies to avoid an <code>UnsupportedOperationException</code>.
	 */
	public class OTTypeHierarchyRole playedBy OTTypeHierarchy {
		IType getExplicitSuperclass(IType type) -> IType getExplicitSuperclass(IType type);

		@SuppressWarnings("basecall")
		callin IType getSuperclass(IType type) {
			return this.getExplicitSuperclass(type);
		}

		getSuperclass <- replace getSuperclass;

	}

}
