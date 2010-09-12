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
 * $Id: GeneralTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.rolelevel.internal;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * $Id: GeneralTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * superclass for all testcases
 * in which an anonymous class is instantiated insight a role class
 */
public abstract class GeneralTest extends FileBasedModelTest
{
    
    protected final String PROJECT =  "AnonymousInnerclass";
    protected final String SRC_FOLDER = "rolelevelinternal";
    protected final String TEAM_PKG = "rolelevelinternal.teampkg";
    
    protected IType _roleJavaElem = null;
    
    
    public GeneralTest(String name)
    {
        super(name);
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(PROJECT);
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
    		super.setUp();
    	
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                PROJECT,
                SRC_FOLDER,
                TEAM_PKG,
                getTeamName() +".java");
            IType teamJavaElem = teamUnit.getType(getTeamName());
            
            System.out.println(getClass().getName());
            System.out.println("Teamklasse: \n" +teamJavaElem);
            
            _roleJavaElem = teamJavaElem.getType(getRoleName());
            
            System.out.println("Rollenklasse\n" +_roleJavaElem);
        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    }
    
    protected abstract String getTeamName();
    
    protected abstract String getRoleName();
}
