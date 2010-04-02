/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FileBasedHierarchyTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.hierarchy;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

public class FileBasedHierarchyTest extends FileBasedModelTest
{
	protected OTTypeHierarchy _testObj;
	protected IType _focusType;

    public FileBasedHierarchyTest(String name)
    {
        super(name);
    }
    
    private OTTypeHierarchy createOTTypeHierarchy(IType focusType, boolean computeSubtypes) throws JavaModelException
    {
    	OTTypeHierarchy hierarchy = new OTTypeHierarchy(focusType, focusType.getJavaProject(), computeSubtypes);
		hierarchy.refresh(new NullProgressMonitor());
        return hierarchy;
    }

    public OTTypeHierarchy createOTTypeHierarchy(IType focusType) throws JavaModelException
    {
    	return createOTTypeHierarchy(focusType, true);
    }

    public OTTypeHierarchy createOTSuperTypeHierarchy(IType focusType) throws JavaModelException
    {
        return createOTTypeHierarchy(focusType, false);
    }



}
