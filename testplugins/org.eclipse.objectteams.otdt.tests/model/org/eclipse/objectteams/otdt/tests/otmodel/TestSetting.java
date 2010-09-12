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
 * $Id: TestSetting.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;

/**
 * @author kaschja
 * @version $Id: TestSetting.java 23494 2010-02-05 23:06:44Z stephan $
 */
// FIXME: why does this inherit FileBasedModelTest??? See also setUp() and tearDown() comments!
public class TestSetting extends FileBasedModelTest
{
    private static final String SAMPLE_ROLE_NAME = "SampleRole";

    private List<String> _roleNames;
    private String _testProject;
    private String _srcDir;
    private String _teamPkg;
    private String _teamClass;
    
    private IType _teamJavaElem;
    
    public TestSetting(String testPrj, String srcDir, String teamPkg)
    {
        super(createName(testPrj, srcDir, teamPkg));
        
        _testProject = testPrj;
        _srcDir = srcDir;
        _teamPkg = teamPkg;
        
        setUsePerformanceMeter(false);
    }
    
    public static String createName(String testPrj, String srcDir, String teamPkg)
    {
    	StringBuffer buffer = new StringBuffer("TestSetting");
    	buffer.append("/").append(testPrj);
    	buffer.append("/").append(srcDir);
    	buffer.append("/").append(teamPkg);
    	
    	return buffer.toString();
    }
    
    public void resetProjectDirectory(String directory) {
    	this._testProject = directory;
    }

    public void setUp() throws Exception
    {
		super.setUp();
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                    _testProject,
                    _srcDir,
                    _teamPkg, 
                    _teamClass + ".java");

            _teamJavaElem = teamUnit.getType(_teamClass);
        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    }
    
    protected void tearDown() throws Exception 
    {
		super.tearDown();
    }
    
    public IType getTeamJavaElement()
    {
        return _teamJavaElem;
    }

    public IType getRoleJavaElement()
    {
        //TODO(jwl): Fix to parse the Team file for an external role!
        _teamJavaElem.exists();
        
        IOTType teamOTElem = OTModelManager.getOTElement(_teamJavaElem);
        assertNotNull("The team type you're trying to test, seems not to exist for the OTModel!", teamOTElem);
        
        return teamOTElem.getRoleType(getRoleName());
    }
    
    public IType[] getRoleJavaElements()
    {       
        if (_roleNames == null)
        {
            return new IType[] { getRoleJavaElement() };
        }
        
        //TODO(jwl): Fix to parse the Team file for an external role!
        _teamJavaElem.exists();
        
        IOTType teamOTElem = OTModelManager.getOTElement(_teamJavaElem);
        assertNotNull("The team type you're trying to test, seems not to exist for the OTModel!", teamOTElem);
        
        List<IType> roles = new LinkedList<IType>();
        for (Iterator<String> roleIter = _roleNames.iterator(); roleIter.hasNext();)
        {
            String roleName = roleIter.next();
            IType role = teamOTElem.getRoleType(roleName);
            assertNotNull("The role type you're trying to test seems not to exist!", role);
            if (role != null)
            {
                roles.add(role);
            }
        }
        
        return roles.toArray(new IType[roles.size()]);
    }
    
    public String getTeamClass()
    {
        return _teamClass;
    }
    
    public String getRoleName()
    {
        return SAMPLE_ROLE_NAME;
    } 
    
    public String getTestProject()
    {
        return _testProject;
    }

    public void setTeamClass(String teamName)
    {
        _teamClass = teamName;
    }
    
    public void setRoleNames(String[] names)
    {
        _roleNames = new ArrayList<String>(names.length);
        for (int idx = 0; idx < names.length; idx++)
        {
        	_roleNames.add(names[idx]);
		}
    }
}
