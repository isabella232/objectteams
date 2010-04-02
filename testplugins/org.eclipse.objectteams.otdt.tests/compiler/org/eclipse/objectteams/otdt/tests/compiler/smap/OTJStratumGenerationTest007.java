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
 * $Id: OTJStratumGenerationTest007.java 23494 2010-02-05 23:06:44Z stephan $
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
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.LineInfoReminder;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.RoleSmapGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.SmapStratum;

/**
 * @author ike
 *  
 */
public class OTJStratumGenerationTest007 extends AbstractSourceMapGeneratorTest
{
    private org.eclipse.jdt.core.ICompilationUnit _teamA;

    @SuppressWarnings("unused")
	private org.eclipse.jdt.core.ICompilationUnit _tea1m2;

    private org.eclipse.jdt.core.ICompilationUnit _teamB;

    private org.eclipse.jdt.core.ICompilationUnit _baseClass;

    private String _enclosingTypename;

    private String _packagePath;

    private boolean _buildPartially;

    public OTJStratumGenerationTest007(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(OTJStratumGenerationTest007.class);
    }

    public void setUpSuite() throws Exception
    {
        setTestProjectDir("JSR-045");
        super.setUpSuite();
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        _packagePath = "callin_after_before";

        _teamA = getCompilationUnit(
                getTestProjectDir(), 
                "src",
                _packagePath,
                "TeamA.java");

        _teamB = getCompilationUnit(
                getTestProjectDir(), 
                "src", 
                _packagePath,
                "TeamB.java");

        _baseClass = getCompilationUnit(
                getTestProjectDir(),
                "src",
                _packagePath,
                "BaseClass.java");
    }

    public void testSmapGeneration1() throws JavaModelException
    {
        TYPENAME = "__OT__RoleA";
        _enclosingTypename = "TeamA";
        String enclTypeSourceName = _enclosingTypename + ".java";
        _buildPartially = false;

        SmapStratum stratum_role1 = new SmapStratum(
                ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo_role1 = stratum_role1.getOrCreateFileInfo(
                enclTypeSourceName, _packagePath + "/" + enclTypeSourceName);
        LineInfo lineInfo1_role1 = new LineInfo(1, 1);
        lineInfo1_role1.setRepeatCount(15);
        LineInfo lineInfo1_role2 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo1_role3 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);

        
        fileInfo_role1.addLineInfo(lineInfo1_role1);
        fileInfo_role1.addLineInfo(lineInfo1_role2);
        fileInfo_role1.addLineInfo(lineInfo1_role3);

        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);

        stratum_role1.optimize();
        
        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[] {
                    _baseClass, _teamA, _teamB });
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }

    public void testSmapGeneration2() throws JavaModelException
    {
        TYPENAME = "__OT__RoleA";
        _enclosingTypename = "TeamA";
        String enclTypeSourceName = _enclosingTypename + ".java";
        _buildPartially = true;

        SmapStratum stratum_role1 = new SmapStratum(
                ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo_role1 = stratum_role1.getOrCreateFileInfo(
                enclTypeSourceName, _packagePath + "/" + enclTypeSourceName);
        fileInfo_role1.addLineInfo(new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER));

        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);

        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[] {
                    _baseClass, _teamA, _teamB });
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testSmapGeneration3() throws JavaModelException
    {
        TYPENAME = "__OT__RoleB";
        _enclosingTypename = "TeamB";
        String enclTypeSourceName = _enclosingTypename + ".java";
        _buildPartially = false;
        
        SmapStratum stratum_role1 = new SmapStratum(
                ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo_role1 = stratum_role1.getOrCreateFileInfo(
                enclTypeSourceName, _packagePath + "/" + enclTypeSourceName);
        LineInfo lineInfo1_role1 = new LineInfo(1, 1);
        lineInfo1_role1.setRepeatCount(13);
        LineInfo lineInfo1_role2 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo1_role3 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);

        fileInfo_role1.addLineInfo(lineInfo1_role1);
        fileInfo_role1.addLineInfo(lineInfo1_role2);
        fileInfo_role1.addLineInfo(lineInfo1_role3);

        stratum_role1.optimize();
        
        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);

        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[] {
                    _baseClass, _teamA, _teamB });
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }

    
    public void testSmapGeneration4() throws JavaModelException
    {
        TYPENAME = "__OT__RoleB";
        _enclosingTypename = "TeamB";
        String enclTypeSourceName = _enclosingTypename + ".java";
        _buildPartially = true;
        
        SmapStratum stratum_role1 = new SmapStratum(
                ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo_role1 = stratum_role1.getOrCreateFileInfo(
                enclTypeSourceName, _packagePath + "/" + enclTypeSourceName);
        fileInfo_role1.addLineInfo(new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER));

        List<SmapStratum> strata_role1 = new ArrayList<SmapStratum>();
        strata_role1.add(stratum_role1);

        expectedStrata.put(TYPENAME, strata_role1);

        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[] {
                    _baseClass, _teamA, _teamB });
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

        assertTrue("Membertypes of TypeDeclaration should be greater than 0.",
                typeDecl.memberTypes.length > 0);

        TypeDeclaration[] members = typeDecl.memberTypes;
        for (int idx = 0; idx < members.length; idx++)
        {
            TypeDeclaration decl = members[idx];
            String typeName = String.valueOf(decl.name);

            if (decl.isRole() && !decl.isInterface()
                    && typeName.equals(TYPENAME))
            {
                RoleSmapGenerator rolefileSmapGenerator = new RoleSmapGenerator(decl);
                if (_buildPartially)
                {
                    rolefileSmapGenerator.addStratum("OTJ");
                    SmapStratum stratum = (SmapStratum)rolefileSmapGenerator.getStrata().get(0);
                    rolefileSmapGenerator.generatePartialOTJSmap(stratum, new LineInfoReminder());
                }
                else
                {
                    rolefileSmapGenerator.addStratum("OTJ");
                    rolefileSmapGenerator.generate();
                }
                
                List actualStrata = rolefileSmapGenerator.getStrata();

                assertEquals("Strata of type \"" + typeName
                        + "\" should be equal.\n",
                        expectedStrata.get(typeName).toString(), actualStrata.toString());
            }
        }
    }

}
