/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTModelReconcileListener.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.internal.core.OTType;


/**
 * The OTModelListener performs the necessary updating of the OTM in order
 * to sync with the JavaModel.
 * 
 * @author kaiser
 * @version $Id: OTModelReconcileListener.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTModelReconcileListener implements IElementChangedListener
{	
	/**
	 * Invoked when some or more JavaModel elements have changed, which may
	 * require additional changes within the OTM 
	 * @param event - event describing changes
	 */
    public void elementChanged(ElementChangedEvent event)
    {    	
    	removeAffectedModelElements( new IJavaElementDelta[] { event.getDelta() } );
    }

	/**
	 * Removes from JavaModel changes affected OTM elements. Actually it removes
	 * ITypes only which have been removed in the JavaModel.
	 * NOTE: recursive method
	 * PRE: deltas != null
	 */
    private void removeAffectedModelElements(IJavaElementDelta[] deltas)
    {
    	assert (deltas != null);
    	
        for (int idx = 0; idx < deltas.length; idx++)
        {
			IJavaElementDelta delta = deltas[idx];
			IJavaElement      elem  = delta.getElement();

			// check for nested deltas
			if (elem instanceof IParent)
			{
				// visit child deltas				
				removeAffectedModelElements(delta.getAffectedChildren());
			}

			// remove corresponding OTM elements if JavaModel IType has been changed/removed
			if (elem instanceof IType)
			{
				// check for changed modifiers on element-changed deltas because
				// this means that the JavaModel IType element has been recreated
				// and old instances need to be removed from the OTM 
				if ((delta.getKind() == IJavaElementDelta.CHANGED)
					&& ((delta.getFlags() & IJavaElementDelta.F_MODIFIERS) != 0))
				{
					OTModelManager.removeOTElement((IType)elem, true);
				}
				else if (delta.getKind() == IJavaElementDelta.REMOVED)
				{
					OTModelManager.removeOTElement((IType)elem);
				}
			}
		    // TODO (carp): do we need special support for WorkingCopies, e.g. when creating
		    // or discarding them?
//			checkWorkingCopies(delta, elem);
        }
    }

    @SuppressWarnings("unused") // see above
	private void checkWorkingCopies(IJavaElementDelta delta, IJavaElement elem)
    {
		if (elem instanceof ICompilationUnit)
		{
			ICompilationUnit unit = (ICompilationUnit) elem;
			if ((delta.getFlags() & IJavaElementDelta.REMOVED) != 0)
			{
			    if (unit.exists())
			    {
			        try
                    {
                        IType[] types = unit.getTypes();
                        System.out.println(types);
                    }
                    catch (JavaModelException ex)
                    {
                        ex.printStackTrace();
                    }
			    }
			}
			
			if ((delta.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0)
			{
				if (!unit.isWorkingCopy() && unit.exists())
				{
					try {
				        ICompilationUnit wc = unit.getWorkingCopy(new NullProgressMonitor());
				        IType[] wcTypes = wc.getTypes();
						IType[] types = unit.getAllTypes();
						for (int i = 0; i < types.length; i++)
						{
                            IType wcType = wcTypes[i];
						    IType currentType = types[i];
							OTType otType = (OTType) OTModelManager.getOTElement(currentType);
							IOTType wcOTType = OTModelManager.getOTElement(wcType);
							if (otType == wcOTType)
							{
								// FIXME(SH): fetching into unused locals? WHY? Incomplete implementation??
								IOTType t1 = OTModelManager.getOTElement(currentType);
								IOTType t2 = OTModelManager.getOTElement(wcType);
								System.out
                                        .println("OTModelReconcileListener.removeAffectedModelElements()");
							}
							
							if (otType != null) // need to update the workingcopy
							{
							    ICompilationUnit otUnit = otType.getCompilationUnit();
							    if (otUnit.isWorkingCopy())
							    {
							        otType.setCorrespondingJavaElement(currentType);
							        System.out.println("Updated: " + otType.toString());
							    }
							}
						}
					}
					catch (JavaModelException ignored) {
					}
				}
			}
		}
    }
}
