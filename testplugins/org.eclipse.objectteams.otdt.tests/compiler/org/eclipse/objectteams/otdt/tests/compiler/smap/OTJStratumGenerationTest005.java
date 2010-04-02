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
 * $Id: OTJStratumGenerationTest005.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.smap;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.FileInfo;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.LineInfo;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.RoleSmapGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.SmapStratum;

/**
 * @author ike
 *
 */
public class OTJStratumGenerationTest005 extends AbstractSourceMapGeneratorTest
{
	private org.eclipse.jdt.core.ICompilationUnit _superTeam; 
	private org.eclipse.jdt.core.ICompilationUnit _subTeam;
	private org.eclipse.jdt.core.ICompilationUnit _baseClass;
	private String _enclosingTypename;
    
	
    public OTJStratumGenerationTest005(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(OTJStratumGenerationTest005.class);
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("JSR-045");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _superTeam = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "callout",
                "SuperTeam.java");

        _subTeam = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "callout",
                "SubTeam.java");
        
        _baseClass = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "callout",
                "BaseClass.java");
    }
    
    public void testSmapGeneration1() throws JavaModelException
    {
        SmapStratum stratum_role1 = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo_role1 = stratum_role1.getOrCreateFileInfo("SuperTeam.java", "callout/SuperTeam.java");
        LineInfo lineInfo1_role1 = new LineInfo(1,1);
        lineInfo1_role1.setRepeatCount(10);
        LineInfo lineInfo2 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo3 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);

        fileInfo_role1.addLineInfo(lineInfo1_role1);
        fileInfo_role1.addLineInfo(lineInfo2);
        fileInfo_role1.addLineInfo(lineInfo3);
        
        stratum_role1.optimize();
        
        TYPENAME = "__OT__RoleA";
        _enclosingTypename = "SuperTeam";
        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);
        
        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_baseClass, _superTeam, _subTeam});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSmapGeneration2()
    {
        SmapStratum stratum_role2 = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);;
        
        FileInfo fileinfo = stratum_role2.getOrCreateFileInfo("Team.java", "org/objectteams/Team.java");
        LineInfo lineInfo1 = new LineInfo(OT_CONFINED_GET_TEAM_LINE,11);
        fileinfo.addLineInfo(lineInfo1);
        
        FileInfo fileInfo_role2 = stratum_role2.getOrCreateFileInfo("SuperTeam.java", "callout/SuperTeam.java");
        LineInfo lineInfo2 = new LineInfo(1,1);
        lineInfo2.setRepeatCount(10);
        LineInfo lineInfo3 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo4 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);
        fileInfo_role2.addLineInfo(lineInfo2);
        fileInfo_role2.addLineInfo(lineInfo3);
        fileInfo_role2.addLineInfo(lineInfo4);
        
        stratum_role2.optimize();
        
        TYPENAME = "__OT__Confined";
        _enclosingTypename = "SuperTeam";
        List<SmapStratum> strata_role2 = new ArrayList<SmapStratum>();
        strata_role2.add(stratum_role2);
        
        expectedStrata.put(TYPENAME, strata_role2);
        
        try
        {
        	 parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_baseClass, _superTeam, _subTeam});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSmapGeneration3() throws JavaModelException
    {
        SmapStratum stratum_role1 = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);
        
        FileInfo fileInfo1_role1 = stratum_role1.getOrCreateFileInfo("SuperTeam.java", "callout/SuperTeam.java");
        LineInfo fileInfo1_lineInfo1 = new LineInfo(4,10);
        LineInfo fileInfo1_lineInfo2 = new LineInfo(8,11);
        fileInfo1_role1.addLineInfo(fileInfo1_lineInfo1);
        fileInfo1_role1.addLineInfo(fileInfo1_lineInfo2);
        FileInfo fileInfo2_role1 = stratum_role1.getOrCreateFileInfo("SubTeam.java", "callout/SubTeam.java");
        LineInfo fileInfo2_lineInfo1 = new LineInfo(1,1);
        fileInfo2_lineInfo1.setRepeatCount(9);
        LineInfo fileInfo2_lineInfo4 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo fileInfo2_lineInfo5 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);

        fileInfo2_role1.addLineInfo(fileInfo2_lineInfo1);
        fileInfo2_role1.addLineInfo(fileInfo2_lineInfo4);
        fileInfo2_role1.addLineInfo(fileInfo2_lineInfo5);

        stratum_role1.optimize();
        
        TYPENAME = "__OT__RoleA";
        _enclosingTypename = "SubTeam";
        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);
        
        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_baseClass, _superTeam, _subTeam});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSmapGeneration4()
    {
    	SmapStratum stratum_role2 = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);
    
    	FileInfo fileinfo = stratum_role2.getOrCreateFileInfo("Team.java", "org/objectteams/Team.java");
    	LineInfo lineInfo1 = new LineInfo(OT_CONFINED_GET_TEAM_LINE,11);
    	fileinfo.addLineInfo(lineInfo1);
    
        FileInfo fileInfo1 = stratum_role2.getOrCreateFileInfo("SuperTeam.java", "callout/SuperTeam.java");
        LineInfo lineInfo2 = new LineInfo(2,10);
        fileInfo1.addLineInfo(lineInfo2);
        
        FileInfo fileInfo2 = stratum_role2.getOrCreateFileInfo("SubTeam.java", "callout/SubTeam.java");
        LineInfo lineInfo3 = new LineInfo(1,1);
        lineInfo3.setRepeatCount(9);
        LineInfo lineInfo5 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo6 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);
        fileInfo2.addLineInfo(lineInfo3);
        fileInfo2.addLineInfo(lineInfo5);
        fileInfo2.addLineInfo(lineInfo6);        
        
        stratum_role2.optimize();
        
        TYPENAME = "__OT__Confined";
        _enclosingTypename = "SubTeam";
        List<SmapStratum> strata_role2 = new ArrayList<SmapStratum>();
        strata_role2.add(stratum_role2);
        
        expectedStrata.put(TYPENAME, strata_role2);
        
        try
        {
        	 parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_baseClass, _superTeam, _subTeam});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void callback(CompilationUnitDeclaration cuDecl)
    {
        String cuDeclName = String.valueOf(cuDecl.getMainTypeName());
        if (!_enclosingTypename.equals(cuDeclName))
            return;
        
        
        TypeDeclaration typeDecl = cuDecl.types[0];
        
        assertNotNull("TypeDeclaration should not be null.", typeDecl);
        
        assertTrue("Membertypes of TypeDeclaration should be greater than 0.", typeDecl.memberTypes.length > 0);
        
        TypeDeclaration [] members = typeDecl.memberTypes;
        for (int idx = 0; idx < members.length; idx++)
        {
            TypeDeclaration decl = members[idx];
            String typeName = String.valueOf(decl.name);
            
            if (decl.isRole() && !decl.isInterface() && typeName.equals(TYPENAME))
            {
                RoleSmapGenerator rolefileSmapGenerator = new RoleSmapGenerator(decl);
                rolefileSmapGenerator.addStratum("OTJ");
                rolefileSmapGenerator.generate();
                List actualStrata = rolefileSmapGenerator.getStrata();
                
                assertEquals("Strata of type \"" + typeName + "\" should be equal.\n", expectedStrata.get(typeName).toString(), actualStrata.toString());
            }
        }
    }

}
