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
 * $Id: OTJStratumGenerationTest003.java 23494 2010-02-05 23:06:44Z stephan $
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
public class OTJStratumGenerationTest003 extends AbstractSourceMapGeneratorTest
{
	private org.eclipse.jdt.core.ICompilationUnit _team1; 
	private org.eclipse.jdt.core.ICompilationUnit _team2;
	private org.eclipse.jdt.core.ICompilationUnit _role;
    private String _enclosingTypename;
	private String _packagePath;
	
    public OTJStratumGenerationTest003(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(OTJStratumGenerationTest003.class);
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("JSR-045");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _packagePath = "roleFileAndCopyInh";
        	  
        _team1 = getCompilationUnit(
                getTestProjectDir(),
                "src",
                _packagePath,
                "SubTeam.java");

        _team2 = getCompilationUnit(
                getTestProjectDir(),
                "src",
                _packagePath,
                "SuperTeam.java");
        
        _role = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleFileAndCopyInh.SuperTeam",
                "RoleA.java");
    }
    
    public void testSmapGeneration1() throws JavaModelException
    {
    	
        TYPENAME = "__OT__RoleA";
        _enclosingTypename = "SubTeam";
        
        String roleFileSourceName = "RoleA.java";
        
        SmapStratum stratum_role = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo1 = stratum_role.getOrCreateFileInfo(
        		roleFileSourceName, _packagePath + "/" + "SuperTeam" + "/" + roleFileSourceName);
        
        LineInfo lineInfo1 = new LineInfo(5,1);  // role (5..) mapped to synthetic line 1  (no own lines in SubTeam.RoleA).
        LineInfo lineInfo2 = new LineInfo(9,2); // method roleMethod (9..) mapped to synthetic line 2..  
        lineInfo2.setRepeatCount(2);
        LineInfo lineInfo3 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo4 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);
        
        fileInfo1.addLineInfo(lineInfo1);
        fileInfo1.addLineInfo(lineInfo2);
        fileInfo1.addLineInfo(lineInfo3);
        fileInfo1.addLineInfo(lineInfo4);

        stratum_role.optimize();
        
        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role);
        
        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_team2, _team1, _role});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSmapGeneration2()
    {
        SmapStratum stratum_role = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);

        FileInfo fileInfo = stratum_role.getOrCreateFileInfo("Team.java", "org/objectteams/Team.java");
        LineInfo lineInfo1 = new LineInfo(OT_CONFINED_GET_TEAM_LINE,22); // getTeam mapped to synthetic line 22
        fileInfo.addLineInfo(lineInfo1);
    	
        FileInfo fileInfo1 = stratum_role.getOrCreateFileInfo("SuperTeam.java", "roleFileAndCopyInh/SuperTeam.java");
        LineInfo lineInfo2 = new LineInfo(3,21); // class position to first synthetic line 21
        fileInfo1.addLineInfo(lineInfo2);
        
        FileInfo fileInfo2 = stratum_role.getOrCreateFileInfo("SubTeam.java", "roleFileAndCopyInh/SubTeam.java");
        LineInfo lineInfo3 = new LineInfo(1,1); // all lines of SubTeam unmapped
        lineInfo3.setRepeatCount(20);
        LineInfo lineInfo5 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo6 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);
        fileInfo2.addLineInfo(lineInfo3);
        fileInfo2.addLineInfo(lineInfo5);
        fileInfo2.addLineInfo(lineInfo6);
        
        stratum_role.optimize();
        
        TYPENAME = "__OT__Confined";
        _enclosingTypename = "SubTeam";
        List<SmapStratum> strata_role2 = new ArrayList<SmapStratum>();
        strata_role2.add(stratum_role);
        
        expectedStrata.put(TYPENAME, strata_role2);
        
        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_team2, _team1, _role});
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
                RoleSmapGenerator roleSmapGenerator = new RoleSmapGenerator(decl);
                roleSmapGenerator.addStratum("OTJ");
                roleSmapGenerator.generate();
                List actualStrata = roleSmapGenerator.getStrata();
                
                assertEquals("Strata of type \"" + typeName + "\" should be equal.\n", expectedStrata.get(typeName).toString(), actualStrata.toString());
            }
        }
    }
}
