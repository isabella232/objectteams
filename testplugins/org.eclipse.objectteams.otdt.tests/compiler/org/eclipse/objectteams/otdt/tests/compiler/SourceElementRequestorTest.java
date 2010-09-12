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
 * $Id: SourceElementRequestorTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler;
import java.util.Locale;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
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
 * @author haebor/mkr
 * @version $Id: SourceElementRequestorTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class SourceElementRequestorTest extends FileBasedModelTest implements ISourceElementRequestor
{
	private char[] source;
	protected String PROJECT_DIR;
    
    public SourceElementRequestorTest(String testName)
    {
        super(testName);
    }
    
    public static Test suite()
    {
        if (true)
        {
            return new Suite(SourceElementRequestorTest.class);
        }
        junit.framework.TestSuite suite = new Suite(SourceElementRequestorTest.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir(PROJECT_DIR);
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void fullParse(String src, String fileName)
    {
	    	this.source = src.toCharArray();
	    	
	    	// km: ctor with new parameter "reportLocalDeclarations", "optimizeStringLiterals" assuming both to be true
	    	SourceElementParser parser = 
	    		new SourceElementParser(this, new DefaultProblemFactory(Locale.getDefault()), new CompilerOptions(), true, true); 
	
	    	ICompilationUnit sourceUnit = new CompilationUnit(source, fileName, null);
	
	    	parser.parseCompilationUnit(sourceUnit, true, null);
    }
    
    public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition)
    {
    }

    public void acceptFieldReference(char[] fieldName, int sourcePosition)
    {
    }

    public void acceptLineSeparatorPositions(int[] positions)
    {
    }

    public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition)
    {
    }

    public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd)
    {
    }

    public void acceptTypeReference(char[] typeName, int sourcePosition)
    {
    }

    public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd)
    {
    }

    public void acceptUnknownReference(char[] name, int sourcePosition)
    {
    }

    public void enterCompilationUnit()
    {
    }

    public void enterInitializer(int declarationStart, int modifiers)
    {
    }

    public void exitCompilationUnit(int declarationEnd)
    {
    }

    public void exitConstructor(int declarationEnd)
    {
    }

    public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd)
    {
    }

    public void exitInitializer(int declarationEnd)
    {
    }

    public void enterCalloutMapping(CalloutInfo calloutInfo)
    {
    }

    public void enterCalloutToFieldMapping(CalloutToFieldInfo calloutInfo)
    {
    }

    public void enterCallinMapping(CallinInfo callinInfo)
    {
    }
    
    public void exitCallinMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

    public void exitCalloutMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

    public void exitCalloutToFieldMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

    public void acceptBaseReference(char[][] typeName, int sourceStart, int sourceEnd){}

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
