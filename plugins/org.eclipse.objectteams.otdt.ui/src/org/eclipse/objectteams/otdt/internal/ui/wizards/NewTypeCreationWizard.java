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
 * $Id: NewTypeCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeCreator;
import org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation.TypeInfo;


/**
 * @author kaschja
 * @version $Id: NewTypeCreationWizard.java 23435 2010-02-04 00:14:38Z stephan $
 */
public abstract class NewTypeCreationWizard  extends NewElementWizard
{
    private IType _createdType;


	public NewTypeCreationWizard()
    {
    }


	public boolean performFinish()
	{
		warnAboutTypeCommentDeprecation();
		return super.performFinish();
	}

	// finishPage() is run as a WorkspaceRunnable. We need access to the SWT-thread though,
	// so we need to stay in our thread.
	protected boolean canRunForked() 
	{
		return false;
	}

	protected void finishPage(IProgressMonitor monitor)
		throws InterruptedException, CoreException
	{	
	    TypeCreator creator = createTypeCreator();
	    setTypeData(creator);
	    
        IType createdType = creator.createType(monitor);
		ICompilationUnit createdCompUnit = createdType.getCompilationUnit();
		ICompilationUnit cu = createdCompUnit == null ? null : createdCompUnit.getPrimary();
		
		
		if (cu != null)
		{
			IResource resource = cu.getResource();
			selectAndReveal(resource);
			openResource((IFile) resource);

			_createdType = cu.getType(createdType.getElementName());
		}
	}

	public IType getCreatedType()
	{
		return _createdType;
	}
	
	/*
	 * Hook methods.
	 */
	protected abstract NewTypeWizardPage getPage();
	protected abstract TypeCreator createTypeCreator();

	protected TypeInfo createTypeInfo()
	{
	    NewTypeWizardPage page = getPage();
	    
	    return new TypeInfo(page.getTypeName(),
	            page.getPackageFragmentRoot(),
	            page.getPackageFragment());
	}
	
	
	@SuppressWarnings("unchecked") // raw list page.getSuperInterfaces()
	protected TypeInfo setTypeData(TypeCreator creator)
	{
	    NewTypeWizardPage page = getPage();	    
	    TypeInfo typeInfo = createTypeInfo();
	    
	    typeInfo.setEnclosingTypeName(page.getEnclosingTypeName());
	    typeInfo.setSuperClassName(page.getSuperTypeName());
	    typeInfo.setSuperInterfacesNames(page.getSuperInterfaces());
	    typeInfo.setModifier(page.getModifiers());
	    typeInfo.setCurrentType(page.getCurrentType());
	    
	    if (page instanceof NewTeamWizardPage)
	    {
	        IType enclosingType = page.getEnclosingType();
	        if ( (enclosingType != null) && enclosingType.exists() )
	        {
	            IOTType enclosingOTType = OTModelManager.getOTElement(enclosingType);
	            if (enclosingOTType != null)
	            {
	                typeInfo.setIsRole(true);
	            }
	        }
	    }
	    else // if (page instanceof NewRoleWizardPage)
	    {
	        typeInfo.setIsRole(true);
	    }

	    typeInfo.setInline(page.isInlineTypeSelected());
	    
        typeInfo.setCreateMainMethod(page.isCreateMainSelected());
        typeInfo.setCreateConstructor(page.isCreateConstructorsSelected());
        typeInfo.setCreateAbstractInheritedMethods(page.isCreateInheritedSelected());
	    
	    creator.setTypeInfo(typeInfo);
	    return typeInfo;
	}
	
	public IJavaElement getCreatedElement() {
		return _createdType;
	}	

}
