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
 * $Id: NewRoleCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.RoleCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.RoleTypeInfo;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeInfo;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;

/**
 * A wizard for creating a new role.
 *
 * @author brcan
 * @version $Id: NewRoleCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class NewRoleCreationWizard extends NewTypeCreationWizard
{
	
	private NewRoleWizardPage _rolePage;


	public NewRoleCreationWizard()
	{
        super();
  
		setDefaultPageImageDescriptor(
			OTDTUIPlugin.getDefault().getImageRegistry().getDescriptor(
			ImageConstants.NEW_ROLE));
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());        
		setWindowTitle(OTNewWizardMessages.NewRoleCreationWizard_title);
	}

	/*
	 * @see Wizard#createPages
	 */
	public void addPages()
	{
		super.addPages();
		_rolePage = new NewRoleWizardPage();
		addPage(_rolePage);
		_rolePage.init(getSelection());
	}
	
	/**
	 * @return The page referenced by this wizard. This is an object of type 
	 *         org.eclipse.objectteams.otdt.internal.ui.wizards.NewRoleWizardPage
	 */
    public NewTypeWizardPage getPage()
    {
    	return _rolePage;
    }
    
	/**
	 * @return A new object of type org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.RoleCreator
	 */
	protected TypeCreator createTypeCreator()
	{
		return new RoleCreator();	
	}
	
	protected TypeInfo setTypeData(TypeCreator creator)
	{
	    TypeInfo typeInfo = super.setTypeData(creator);

	    if ( (typeInfo instanceof RoleTypeInfo) && (getPage() instanceof NewRoleWizardPage) )
	    {
	        setRoleSpecificTypeData( (RoleTypeInfo)typeInfo, (NewRoleWizardPage)getPage() );
	    }
	    return typeInfo;	    
	}

	private void setRoleSpecificTypeData(RoleTypeInfo roleInfo, NewRoleWizardPage rolePage)
	{
        roleInfo.setBaseTypeName( rolePage.getBaseClassName() );	    
	}
	
	protected TypeInfo createTypeInfo()
	{
	    NewTypeWizardPage page = getPage();
	    
	    return new RoleTypeInfo(page.getTypeName(),
	            page.getPackageFragmentRoot(),
	            page.getPackageFragment());
	}	
}

