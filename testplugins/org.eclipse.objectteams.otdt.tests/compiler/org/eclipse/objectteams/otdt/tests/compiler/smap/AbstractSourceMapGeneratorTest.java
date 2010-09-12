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
 * $Id: AbstractSourceMapGeneratorTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.smap;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.compiler.regression.InMemoryNameEnvironment;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.objectteams.otdt.debug.core.breakpoints.IOOTBreakPoints;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.SmapStratum;
import org.eclipse.objectteams.otdt.tests.ClasspathUtil;
import org.eclipse.objectteams.otdt.tests.compiler.CustomizedCompiler;
import org.eclipse.objectteams.otdt.tests.compiler.ICallbackClient;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * @author ike
 */
public abstract class AbstractSourceMapGeneratorTest extends FileBasedModelTest implements ICallbackClient, ISourceElementRequestor
{
    
	protected String PROJECT_DIR;
    
	public static final String COMPLIANCE_1_3 = "1.3";
	public static final String COMPLIANCE_1_4 = "1.4";
	public static final String COMPLIANCE_1_5 = "1.5";
	
	private String complianceLevel = COMPLIANCE_1_5;
	protected String[] classpaths;
	protected Hashtable <String, List<SmapStratum>>expectedStrata;
    
	protected String TYPENAME;
	
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_5; //$NON-NLS-1$

	public static String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "smaptest";
	
	// the source line within method Team.java:__OT__Confined._OT$getTeam().
	public static int OT_CONFINED_GET_TEAM_LINE = IOOTBreakPoints.LINE_ConfinedGetTeam;
    
    public AbstractSourceMapGeneratorTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(AbstractSourceMapGeneratorTest.class);
        }
        junit.framework.TestSuite suite = new Suite(AbstractSourceMapGeneratorTest.class
            .getName());
        return suite;
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        expectedStrata = new Hashtable<String, List<SmapStratum>>();
    }

    protected INameEnvironment getNameEnvironment(final String[] testFiles,
            String[] classPaths)
    {
        this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
        return new InMemoryNameEnvironment(testFiles, getClassLibs());
    }

    protected INameEnvironment[] getClassLibs()
    {
        String encoding = (String)getCompilerOptions().getMap().get(CompilerOptions.OPTION_Encoding);
        if ("".equals(encoding))
            encoding = null;

        INameEnvironment[] classLibs = new INameEnvironment[1];
        classLibs[0] = new FileSystem(this.classpaths, new String[] {}, // ignore initial file names
                encoding // default encoding
        );
        return classLibs;
    }

    @SuppressWarnings("unchecked")
	protected CompilerOptions getCompilerOptions()
    {
    	CompilerOptions cOptions = new CompilerOptions();
        Map<String, String> options = cOptions.getMap();
        if (COMPLIANCE_1_3.equals(this.complianceLevel))
        {
            options.put(CompilerOptions.OPTION_Compliance,  CompilerOptions.VERSION_1_3);
            options.put(CompilerOptions.OPTION_Source,  CompilerOptions.VERSION_1_3);
            options.put(CompilerOptions.OPTION_TargetPlatform,  CompilerOptions.VERSION_1_3);
        }
        else if (COMPLIANCE_1_4.equals(this.complianceLevel))
        {
            options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
            options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
            options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
        }
        else if (COMPLIANCE_1_5.equals(this.complianceLevel))
        {
            options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
            options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
            options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
        }

        options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
        options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
        options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
        options.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.WARNING);
               
        return new CompilerOptions(options);
    }

    protected String[] getDefaultClassPaths()
    {
        return Util.concatWithClassLibs(ClasspathUtil.OTRE_PATH, false);
    }

    protected IErrorHandlingPolicy getErrorHandlingPolicy()
    {
        return new IErrorHandlingPolicy()
        {
            public boolean stopOnFirstError()
            {
                return false;
            }

            public boolean proceedOnErrors()
            {
                return true;
            }
        };
    }

    protected IProblemFactory getProblemFactory()
    {
        return new DefaultProblemFactory(Locale.getDefault());
    }
    
    /**
     * @param team
     * @param role
     * @throws JavaModelException
     */
    public boolean parseAndCompile(org.eclipse.jdt.core.ICompilationUnit[] units ) throws JavaModelException
    {
    	return parseAndCompile(units, null);
    }
    public boolean parseAndCompile(org.eclipse.jdt.core.ICompilationUnit[] units, HashMap<String, int[]> methodLineNumbers ) throws JavaModelException
    {
    	return parseAndCompile(units, methodLineNumbers, null, null);
    }
    public boolean parseAndCompile(org.eclipse.jdt.core.ICompilationUnit[] units, 
    							   HashMap<String, int[]> methodLineNumbers,
    							   String[] classPaths,
    							   String outputPath) 
    		throws JavaModelException
    {
        SourceElementParser parser = new SourceElementParser(this,
                new DefaultProblemFactory(Locale.getDefault()),
                new CompilerOptions(), false, false);
        ICompilationUnit [] cUnits = new ICompilationUnit[units.length];
        
        for (int idx = 0; idx < units.length; idx++)
        {
            org.eclipse.jdt.core.ICompilationUnit unit = units[idx];
            
            String unit_src = unit.getSource();
            IResource unit_res = unit.getCorrespondingResource();
            String unit_fileName = unit_res.toString();
            char[] unit_source = unit_src.toCharArray();
            ICompilationUnit unit_sourceUnit = new CompilationUnit(unit_source,
                    unit_fileName, null);
            
            CompilationUnitDeclaration cuDecl = parser.parseCompilationUnit(
                    unit_sourceUnit, true, null);
            
            if (cuDecl.hasErrors())
               return false;
            
            cUnits[idx] = unit_sourceUnit;
        } 
   
        Requestor requestor = new Requestor(
							false,
							null, /*no custom requestor */
							false, /* show category */
							false /* show warning token*/,
							methodLineNumbers);
        if (outputPath != null) {
        	requestor.outputPath = outputPath;
        	File outDir = new File(outputPath);
        	if (!outDir.exists())
        		outDir.mkdir();
        }
        
		CompilerOptions options =  getCompilerOptions();

		CustomizedCompiler batchCompiler = new CustomizedCompiler(
        		getNameEnvironment( new String[] {}, classPaths), 
        		getErrorHandlingPolicy(),
        		options,
                requestor,
                getProblemFactory()
                );
        
        batchCompiler.addCallBack(this);
        
        batchCompiler.compile(cUnits); // compile all files together
        
        boolean hasErrors = requestor.hasErrors;
        
        //errorlog contains errore and warnings, skip warnings
        if (hasErrors)
        {
            String expectedErrorlog = "";
            String actualErrorlog = requestor.problemLog;
            assertEquals("COMPILATION FAILED. Errorlog should be empty.", expectedErrorlog, actualErrorlog);
        }        

        if (methodLineNumbers != null)
        	requestor.checkAllLineNumbersSeen();
        
        return !hasErrors;
    }
    
    public abstract void callback(CompilationUnitDeclaration cuDecl);
    
    public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition)
    {
    }

    public void acceptFieldReference(char[] fieldName, int sourcePosition)
    {
    }

    public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand, int modifiers)
    {
    }

    public void acceptLineSeparatorPositions(int[] positions)
    {
    }

    public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition)
    {
    }

    public void acceptPackage(ImportReference importReference) 
    {
    }
    
    public void acceptProblem(IProblem problem)
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

    public void enterClass(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[] superclass, char[][] superinterfaces)
    {
    }

    public void enterCompilationUnit()
    {
    }

    public void enterConstructor(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes)
    {
    }

    public void enterField(int declarationStart, int modifiers, char[] type, char[] name, int nameSourceStart, int nameSourceEnd)
    {
    }

    public void enterInitializer(int declarationStart, int modifiers)
    {
    }

    public void enterInterface(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] superinterfaces)
    {
    }

    public void enterMethod(int declarationStart, int modifiers, char[] returnType, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes)
    {
    }

    public void exitClass(int declarationEnd)
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

    public void exitMethod(int declarationEnd, Expression defaultValue) 
    {
    }
    
    public void acceptBaseReference(char[][] typeName, int sourceStart, int sourceEnd)
    {
    }

    public void enterInterface(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] superinterfaces, char[] baseclassName, boolean isRoleFile)
    {
    }

    public void enterClass(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[] superclass, char[][] superinterfaces, char[] baseclassName, boolean isRoleFile)
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

    public void exitCalloutMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

    public void exitCalloutToFieldMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

    public void exitCallinMapping(int sourceEnd, int declarationSourceEnd)
    {
    }

	public void acceptProblem(CategorizedProblem problem) {
		// TODO Auto-generated method stub
		
	}

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

	public void exitMethod(int declarationEnd, int defaultValueStart, int defaultValueEnd) {
		// TODO Auto-generated method stub
		
	}

	public void exitType(int declarationEnd) {
		// TODO Auto-generated method stub
		
	}

	public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand, int modifiers) {
		// TODO Auto-generated method stub
		
	}
	
	public void acceptAnnotationTypeReference(char[][] annotation,
			int sourceStart, int sourceEnd) {
		// TODO Auto-generated method stub
		
	}

	public void acceptAnnotationTypeReference(char[] annotation,
			int sourcePosition) {
		// TODO Auto-generated method stub
		
	}
}
