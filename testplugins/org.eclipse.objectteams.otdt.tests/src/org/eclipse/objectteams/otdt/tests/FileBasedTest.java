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
 * $Id: FileBasedTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.tests.otmodel.TestDataHandler;
import org.eclipse.objectteams.otdt.tests.otmodel.TestSetting;

public abstract class FileBasedTest extends AbstractJavaModelTests
{
    private static final String LINE_SEPARATOR = System
                                                   .getProperty("line.separator");
    private static final String WORKSPACE_DIR  = "workspace";
    
    private String              _projectDir;
	protected IJavaProject      javaProject;

    public FileBasedTest(String name)
    {
        super(name);
        
        if (getTestSetting() != null)
        	setUsePerformanceMeter(false);
    }

    public TestSetting getTestSetting()
    {
        return TestDataHandler.getTestSetting(getClass());
    }
    
    public String getSourceWorkspacePath()
    {
        return getPluginDirectoryPath()
                + java.io.File.separator
                + WORKSPACE_DIR;
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        
        // ensure autobuilding is turned off
        IWorkspaceDescription description = getWorkspace().getDescription();
        if (description.isAutoBuilding())
        {
            description.setAutoBuilding(false);
            getWorkspace().setDescription(description);
        }
        if (_projectDir != null)
        	this.javaProject = setUpJavaProject(_projectDir); 
    }

    public void tearDownSuite() throws Exception
    {
        this.deleteProject(_projectDir);
        super.tearDownSuite();
    }

    /**
     * Returns the OS path to the directory that contains this plugin.
     */
    protected String getPluginDirectoryPath()
    {
        try
        {
            URL platformURL = Platform
				                .getBundle(getPluginID())
				                .getEntry("/");
            return new File(FileLocator.toFileURL(platformURL).getFile())
                .getAbsolutePath();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    protected abstract String getPluginID();

    protected String getResource(String packageName, String resourceName)
    {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

        IResource resource = workspaceRoot.findMember(new Path("/"
                + _projectDir
                + "/"
                + packageName
                + "/"
                + resourceName));
        assertNotNull("No resource found", resource);
        return resource.getLocation().toOSString();
    }

    protected String getSource(ASTNode astNode, char[] source)
    {
        String result = new String(CharOperation.subarray(source, astNode
            .getStartPosition() + 1, astNode.getStartPosition()
                + astNode.getLength()
                - 1));
        if (result.endsWith("\\n"))
        {
            return result.substring(0, result.length() - 2) + LINE_SEPARATOR;
        }
        return result;
    }
    
    protected void setTestProjectDir(String dirName)
    {
        _projectDir = dirName;
    }
    
    protected String getTestProjectDir()
    {
        return _projectDir;
    }
    
    public boolean isRole(IType type)
    {
        IOTType otType =
            OTModelManager.getOTElement(type);
        return (otType != null)
        	&& (otType.exists())
        	&& (otType instanceof IRoleType);
    }
    
    public IRoleType getRole(String project, String srcFolder, String pkg, String aTeam, String role)
    	throws JavaModelException
	{
	    ICompilationUnit teamUnit = 
	        getCompilationUnit(project, srcFolder, pkg, aTeam + ".java"); 
	    assertNotNull(teamUnit);
	    assertTrue(teamUnit.exists());
	    
	    IType teamType = teamUnit.getType(aTeam);
	    assertNotNull(teamType);
	    assertTrue(teamType.exists());
	    
	    IType javaModelRole = teamType.getType(role);
	    assertNotNull(javaModelRole);
	    assertTrue(javaModelRole.exists());
	    
        IOTJavaElement roleType = 
            OTModelManager.getOTElement(javaModelRole);
        return (IRoleType)roleType;
	
	}
    
    public IType getRole(IType aTeam, String role)
    {
        return TypeHelper.findNestedRoleType(aTeam, role);
    }
    
    public IType getMemberType(String project, String srcFolder, String pkg, String enclosingTypeName, String member)
		throws JavaModelException
	{
	    ICompilationUnit declaringUnit = 
	        getCompilationUnit(project, srcFolder, pkg, enclosingTypeName + ".java"); 
	    assertTrue(declaringUnit.exists());
	    
	    IType enclosingType = declaringUnit.getType(enclosingTypeName);
	    assertTrue(enclosingType.exists());
	    
	    IType memberType = enclosingType.getType(member);
	    assertTrue(memberType.exists());
	
	    return memberType;
	}
    
    public IType getType(String projectName, String srcFolder, String pkg, String typeName)
    	throws JavaModelException
	{
	    ICompilationUnit declaringUnit = 
	        getCompilationUnit(projectName, srcFolder, pkg, typeName + ".java"); 
	    
	    IType type = declaringUnit != null ? declaringUnit.getType(typeName) : null;
	    if(type == null)
	    {
		    type = getJavaProject(projectName).findType(pkg + "." + typeName);
	    }
	    assertNotNull(type);
	    assertTrue(type.exists());
	    
	    return type;
	}

    
    public IType getRoleJavaElem(
            String project, 
            String srcFolder, 
            String pkg, 
            String aTeam, 
            String role)
    	throws JavaModelException
    {
        return getMemberType(
                project, 
                srcFolder,
                pkg,
                aTeam,
                role);
    }
    
    public boolean containsAllRequiredTypes(IType[] actual, List expected)
    {
        if(actual == null 
            || expected == null
            || actual.length != expected.size())
        {
            return false;
        }
        boolean result = true;
        Iterator iterator = expected.iterator();
        while(iterator.hasNext())
        {
            String currentExpected = (String)iterator.next();
            //Is currentExpected contained in actual
            boolean loopResult = false;
	        for(int actualIdx = 0; actualIdx < actual.length; actualIdx++)
	        {
	            loopResult |= currentExpected.compareTo(actual[actualIdx].getFullyQualifiedName()) == 0;
	            if(loopResult)
	            {
	                break;
	            }
	        }
	        result &= loopResult;
        }
        return result;
    }
    
    
    /**
     * Fully qualified name-based comparison of typelists, without respect to the order
     * @param expected types that have been expected
     * @param actual types that are actually there
     */
    public boolean compareTypes(IType[] expected, IType[] actual)
    {
        if(actual == null 
                || expected == null
                || actual.length != expected.length)
        {
            return false;
        }
        
        boolean result = true;
        for (int expIdx = 0; expIdx < expected.length; expIdx++)
        {
            String curExpName = expected[expIdx].getFullyQualifiedName();
            //Is currentExpected contained in actual
            boolean loopResult = false;
	        for (int actIdx = 0; actIdx < actual.length; actIdx++)
	        {
	            loopResult |= curExpName.compareTo(actual[actIdx].getFullyQualifiedName()) == 0;
	            if (loopResult)
	            {
	                break;
	            }
	        }
	        if (!loopResult)
	        	System.out.println("Not found: "+curExpName);
	        result &= loopResult;
        }
        return result;
    }
    
    /**
     * Name-based comparison of methodlists, without respect to the order
     * @param expected methods that have been expected
     * @param actual methods that are actually there
     */
    public boolean compareMethods(IMethod[] expected, IMethod[] actual)
    {
        if(actual == null 
                || expected == null
                || actual.length != expected.length)
        {
            return false;
        }
        
        boolean result = true;
        for (int expIdx = 0; expIdx < expected.length; expIdx++)
        {
            String curExpName = expected[expIdx].toString();
            //Is currentExpected contained in actual
            boolean loopResult = false;
            for (int actIdx = 0; actIdx < actual.length; actIdx++)
            {
                loopResult |= curExpName.compareTo(actual[actIdx].toString()) == 0;
                if (loopResult)
                {
                    break;
                }
            }
            result &= loopResult;
        }
        return result;
    }
    
    /**
     * Fully qualified name-based comparison of two types
     */
    public boolean compareTypes(IType type1, IType type2)
    {
        if (type1 == null || type2 == null)
        {
            return false;
        }
        
        String name1 = type1.getFullyQualifiedName();
        String name2 = type2.getFullyQualifiedName();
        
        return name1.equals(name2);
    }

    public IType getJavaLangObject(IJavaProject project) throws JavaModelException
    {
        IType jlo = project.findType("java.lang.Object");
        assertTrue(jlo != null);
        assertTrue(jlo.exists());
        return jlo;
    }
    
// Println output disabled
//	/**
//	 * enriches the super-method with print-statements
//	 */
//	public void runBare() throws Throwable 
//	{
//	    System.out.println("\nsetUp");
//		setUp();
//		try {
//		    System.out.println("\n" +getName());
//			runTest();
//		}
//		finally {
//			tearDown();
//		}
//	}    
}