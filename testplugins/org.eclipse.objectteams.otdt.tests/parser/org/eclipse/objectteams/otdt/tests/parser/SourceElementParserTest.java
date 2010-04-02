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
 * $Id: SourceElementParserTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.parser;

import java.util.Locale;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author brcan
 * @version $Id: SourceElementParserTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class SourceElementParserTest extends FileBasedModelTest implements ISourceElementRequestor
{
	private char[] source;
    
    public SourceElementParserTest(String testName)
    {
        super(testName);
    }
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(SourceElementParserTest.class);
        }
        junit.framework.TestSuite suite = new Suite(SourceElementParserTest.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("ParserTest");        
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void fullParse(String src, String fileName)
    {
	    	this.source = src.toCharArray();
	    	
	    	// km: two new parameter in 32m4: "reportLocalDeclarations","optimizeStringLiterals" 
	    	//     assuming both as true
	    	SourceElementParser parser = 
	    		new SourceElementParser(this, new DefaultProblemFactory(Locale.getDefault()), new CompilerOptions(), true, true); 
	
	    	ICompilationUnit sourceUnit = new CompilationUnit(source, fileName, null);
	
	    	parser.parseCompilationUnit(sourceUnit, true, null);
    }

// test-methods following
    
    public void testDeclarationSourceStart() throws JavaModelException
    {
        org.eclipse.jdt.core.ICompilationUnit unit = getCompilationUnit(
                "ParserTest",
                "src",
                "sourcelocations",
                "Disposition.java"); // FIXME(SH): need a new test file
        
        String    src = unit.getSource();
        IResource res = unit.getCorrespondingResource();        
                    
	    	String fileName = res.toString();
	    	fullParse(src, fileName);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptConstructorReference(char[], int, int)
     */
    public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptFieldReference(char[], int)
     */
    public void acceptFieldReference(char[] fieldName, int sourcePosition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptLineSeparatorPositions(int[])
     */
    public void acceptLineSeparatorPositions(int[] positions)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptMethodReference(char[], int, int)
     */
    public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptTypeReference(char[][], int, int)
     */
    public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptTypeReference(char[], int)
     */
    public void acceptTypeReference(char[] typeName, int sourcePosition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptUnknownReference(char[][], int, int)
     */
    public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#acceptUnknownReference(char[], int)
     */
    public void acceptUnknownReference(char[] name, int sourcePosition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#enterCompilationUnit()
     */
    public void enterCompilationUnit()
    {
        // TODO Auto-generated method stub
        
    }


    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#enterInitializer(int, int)
     */
    public void enterInitializer(int declarationStart, int modifiers)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitCompilationUnit(int)
     */
    public void exitCompilationUnit(int declarationEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitConstructor(int)
     */
    public void exitConstructor(int declarationEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitField(int, int, int)
     */
    public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitInitializer(int)
     */
    public void exitInitializer(int declarationEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitCalloutMapping(int)
     */
    public void exitCalloutMapping(int sourceEnd, int declarationSourceEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitCalloutToFieldMapping(int)
     */
    public void exitCalloutToFieldMapping(int sourceEnd, int declarationSourceEnd)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ISourceElementRequestor#exitCallinMapping(int)
     */
    public void exitCallinMapping(int sourceEnd, int declarationSourceEnd)
    {
        // TODO Auto-generated method stub
        
    }


//  {ObjectTeams: 
	public void enterCalloutMapping(CalloutInfo calloutInfo) {}
    public void enterCalloutToFieldMapping(CalloutToFieldInfo calloutInfo) {}
    public void enterCallinMapping(CallinInfo callinInfo) {}
    
    public void acceptBaseReference(char[][] typeName, int sourceStart, int sourceEnd) {}
//    haebor}

	public void enterConstructor(MethodInfo methodInfo) {
		// TODO Auto-generated method stub
		
	}

	public void enterField(FieldInfo fieldInfo) {
		// TODO Auto-generated method stub
		
	}

	public void enterMethod(MethodInfo methodInfo) {
		// TODO Auto-generated method stub
		
	}

	public void enterType(TypeInfo typeInfo) {
		// TODO Auto-generated method stub
		
	}

	public void exitType(int declarationEnd) {
		// TODO Auto-generated method stub
		
	}

	public void acceptProblem(CategorizedProblem problem) {
		// TODO Auto-generated method stub
		
	}

	public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand, int modifiers) {
		// TODO Auto-generated method stub
		
	}

	public void acceptAnnotationTypeReference(char[][] annotation, int sourceStart, int sourceEnd) {
		// TODO Auto-generated method stub
	}

	public void acceptAnnotationTypeReference(char[] annotation, int sourcePosition) {
		// TODO Auto-generated method stub
	}

	public void acceptPackage(ImportReference importReference) {
		// TODO Auto-generated method stub
	}

	public void exitMethod(int declarationEnd, Expression defaultValue) {
		// TODO Auto-generated method stub
	}

}
