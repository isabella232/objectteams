/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTTypeSelectionLabelProvider.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.dialogs;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 * A LabelProvider for the lower pane of a TeamSelectionDialog.
 * 
 * @author kaschja
 * @version $Id: OTTypeSelectionLabelProvider.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class OTTypeSelectionLabelProvider extends LabelProvider
{
	private static final Image PKG_ICON = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKAGE);

    private WorkbenchLabelProvider _wblp = new WorkbenchLabelProvider();


    /**
     * @param  element may be of any type in particular of type {@link org.eclipse.objectteams.otdt.core.IOTType}
     * @return package name and source location (source folder) of the given element. 
     */
    public String getText(Object element)
    {
    	String result;
    	
    	if (element instanceof IOTType)
    	{
//{OTModelUpdate    	    
    		IType type = (IType) ((IOTType)element).getCorrespondingJavaElement();
//haebor}    		
			result = JavaElementLabels.getElementLabel(type.getPackageFragment(),
													   JavaElementLabels.P_POST_QUALIFIED);
    	}
    	else
    	{
    		result = _wblp.getText(element);
    	}
    	
    	return result;
    }

	/**
	 * @return the package icon
	 */    
    public Image getImage(Object element)
	{
		return PKG_ICON;
	}	
}
