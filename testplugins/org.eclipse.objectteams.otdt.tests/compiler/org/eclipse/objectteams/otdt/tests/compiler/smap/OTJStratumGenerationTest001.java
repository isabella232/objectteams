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
 * $Id: OTJStratumGenerationTest001.java 23494 2010-02-05 23:06:44Z stephan $
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
public class OTJStratumGenerationTest001 extends AbstractSourceMapGeneratorTest
{
	private org.eclipse.jdt.core.ICompilationUnit _role; 
	private org.eclipse.jdt.core.ICompilationUnit _team;
	
    public OTJStratumGenerationTest001(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new Suite(OTJStratumGenerationTest001.class);
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("JSR-045");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        _role = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleFile.TeamA",
                "RoleA.java");

        _team = getCompilationUnit(
                getTestProjectDir(),
                "src",
                "roleFile",
                "TeamA.java");
    }
    
    public void testSimpleRoleSmapGeneration() throws JavaModelException
    {
        SmapStratum stratum = new SmapStratum(ISMAPConstants.OTJ_STRATUM_NAME);
        FileInfo fileInfo = stratum.getOrCreateFileInfo("RoleA.java","roleFile/TeamA/RoleA.java");
        LineInfo lineInfo = new LineInfo(1,1);
        lineInfo.setRepeatCount(11);
        LineInfo lineInfo1 = new LineInfo(ISMAPConstants.STEP_INTO_LINENUMBER,ISMAPConstants.STEP_INTO_LINENUMBER);
        LineInfo lineInfo2 = new LineInfo(ISMAPConstants.STEP_OVER_LINENUMBER,ISMAPConstants.STEP_OVER_LINENUMBER);
        fileInfo.addLineInfo(lineInfo);
        fileInfo.addLineInfo(lineInfo1);
        fileInfo.addLineInfo(lineInfo2);
        
        stratum.optimize();
        
        TYPENAME = "__OT__RoleA";
        List <SmapStratum>strata = new ArrayList<SmapStratum>();
        strata.add(stratum);
        
        expectedStrata.put(TYPENAME, strata);
        
        try
        {
            parseAndCompile(new org.eclipse.jdt.core.ICompilationUnit[]{_team, _role});
        }
        catch (JavaModelException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void callback(CompilationUnitDeclaration cuDecl)
    {
        TypeDeclaration typeDecl = cuDecl.types[0];
        
        assertNotNull("TypeDeclaration should not be null.", typeDecl);
        
        if (typeDecl.memberTypes == null)
        {
            return;
        }
        
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
                
                assertEquals("Strata of type \"" + typeName + "\" should be equal.\n", expectedStrata.get(typeName), actualStrata);
            }
        }
    }
}
