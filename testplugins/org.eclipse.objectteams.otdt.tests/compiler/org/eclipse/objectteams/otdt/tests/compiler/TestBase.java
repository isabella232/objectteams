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
 * $Id: TestBase.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.ClasspathUtil;

/**
 * This class represents a base for testing the compiler with several files.
 *
 * @author Jan Wloka
 * @version $Id: TestBase.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class TestBase extends TestCase
{
	
	public static final String NL = "\r\n";
	
	public static final String CLASS_FILE_EXTENSION = ".class";
    public static final String JAVA_FILE_EXTENSION 	= ".java";
    public static final String LOG_FILE_EXTENSION 	= ".log";
    
    public static final String WORKSPACE_NAME 		= "testing-workspace";
    public static final String PROJECT_NAME 		= "TestProject";
	
	public static final String JAVA_HOME = System.getProperty("java.home");
	public static final String USER_HOME = System.getProperty("user.home");
	
	public static final String JRE_JAR_PATH;
	static {
		String path = JAVA_HOME+File.separator+"lib"+File.separator+"rt.jar";
		if ((new File(path).exists())) {
			JRE_JAR_PATH = path;
		} else {
			JRE_JAR_PATH = JAVA_HOME+File.separator+"lib"+File.separator+"vm.jar";
			System.err.println("TestBase: using alternate jre "+JRE_JAR_PATH);
		}
	}

    public static final String PROJECT_PATH = USER_HOME 
													+ File.separator 
													+ WORKSPACE_NAME 
													+ File.separator
													+ PROJECT_NAME;

    
	private Main   _compiler;
    private String _logFileName;
    private File   _workingDir;
    
    public TestBase(String testName)
    {
    		super(testName);
    }
    
    private void cleanWorkingDirectory()
    {
       if (!_workingDir.isDirectory())
       {
       		return;
       }
       cleanRecursively(_workingDir);
//       File[] containedFiles = _workingDir.listFiles();
//       
//       for (int idx = 0; idx < containedFiles.length; idx++)
//	   {
//	       	containedFiles[idx].delete();    
//	   }
//	   
//	   _workingDir.delete();
    }
    private void cleanRecursively(File file) {
    	if (file.isDirectory()) {
    		for (File child : file.listFiles()) {
				cleanRecursively(child);
			}
    	}
    	file.delete();
    }

    // -- use default options: --
    public void compileFile(String fname)
    {
    	compileFile(fname, null);
    }
    public void compileFile(String fname, String[] classpath)
    {
		Map<String,String> options= new HashMap<String,String>();
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ERROR);
		compileFile(fname, classpath, options);
    }
    // -- use custom options: --
    public void compileFile(String fname, String[] classpath, Map options)
    {
        System.out.println("*************************** "
        		+ this.getClass().getName()
				+" "
                + this.getName()
                + " ***************************");
        _logFileName = fname;
        
        try
        {
        	String missing;
	        if ((missing = missingClasspathEntry()) != null)
	        {
	            throw new FileNotFoundException("Missing library "+missing);
	        }
	        
	        String[] args = (classpath == null)  
	        					? createClassPath(fname)
	        					: classpath;
		
			File javaFile = new File(_workingDir.getAbsolutePath() 
												+ File.separator 
												+ fname 
												+ JAVA_FILE_EXTENSION);
												
			if(!javaFile.exists())
			{
				throw new FileNotFoundException("File to compile was not found!: " + _logFileName);
			}
			
            _compiler =
                new Main(
                    new PrintWriter(
                    	new FileOutputStream(PROJECT_PATH 
                    							+ File.separator
                    							+ _logFileName 
                    							+ LOG_FILE_EXTENSION)),
                    new PrintWriter(
                    	new FileOutputStream(PROJECT_PATH 
                    							+ File.separator
                    							+ _logFileName
                    							+ LOG_FILE_EXTENSION)),
                    false,
                    options,
                    null);
                    
            _compiler.compile(args);
        }
        catch (FileNotFoundException ex)
        {
            handleException(ex);
        }
    }

    private String missingClasspathEntry()
    {
        File otreJar = new File(ClasspathUtil.OTRE_PATH);
        File jreJar = new File(JRE_JAR_PATH);
        
        if (!otreJar.exists())
        	return ClasspathUtil.OTRE_PATH;
        if (!jreJar.exists())
        	return JRE_JAR_PATH;
        return null;
    }

    public void createFile(String fname, String content)
    {
		try
		{
	    	File pkgDir = createPackageDirectory(content);
	        File file   = new File(pkgDir.getAbsolutePath()
        						+ File.separator 
        						+ fname 
        						+ JAVA_FILE_EXTENSION);
        file.deleteOnExit();
        
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        }
        catch (IOException ex)
        {
            handleException(ex);
        }
    }

	/**
	 * Creates a file at given relative position and all necessary directories 
	 * The content of package is not evaluatet for saving location
	 * @param fname the name of the new file to create
	 * @param relPath the relative path where to store the file
	 * @param content the content
	 */
	public void createFileAt(String fname, String relPath, String content)
	{
		try
		{
			String curPath = _workingDir.getAbsolutePath();
			
			if(relPath.trim().length() > 0)
			{
				curPath+= File.separator + relPath;
			}

			File   pkgDir  = new File(curPath);
			pkgDir.mkdirs();
			
			File file   = new File(pkgDir.getAbsolutePath()
								+ File.separator 
								+ fname 
								+ JAVA_FILE_EXTENSION);
		file.deleteOnExit();
        
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		}
		catch (IOException ex)
		{
			handleException(ex);
		}
	}

    private File createPackageDirectory(String fileContent) throws IOException
    {
		String qualPkgName = getQualifiedPackageName(fileContent);
		String dirPath     = getDirectoryPath(qualPkgName);
    	
    	String curPath = _workingDir.getAbsolutePath() + File.separator + dirPath;
		File   result  = new File(curPath);
		result.mkdirs();

        return result;
    }

    private String getDirectoryPath(String qualPkgName)
    {
        if ((qualPkgName == null) || (qualPkgName.trim().length() == 0))
        {
			return "";
        }
        
        if (qualPkgName.indexOf('.') == -1)
        {
        	return qualPkgName;
        }
        else
        {
			return qualPkgName.replaceAll("[.]", File.separator);
        }
    }

    private String getQualifiedPackageName(String fileContent)
    {
        String packageKeyword = "package ";
        
        int pos1 = fileContent.indexOf(packageKeyword);
		if (pos1 == -1)
		{
			return "";
		}
        
        int pos2 = fileContent.indexOf(';', pos1);
		if (pos2 == -1)
		{
			return "";
		}       
        
        
        return fileContent.substring(pos1 + packageKeyword.length(), pos2);
    }

    private void handleException(Exception ex)
    {
        System.out.println("UNCAUGHT EXCEPTION: " + ex);
        ex.printStackTrace(System.out);
    }


    /**
     * checks whether the compiler has proceeded without errors or warnings 
     */
	public boolean isCompilationSuccessful()
	{
		if (_compiler.globalErrorsCount != 0)
		{
			printAllProblems();
			return false;
		}
		else
		{
			File file = new File(_workingDir.getAbsolutePath() 
									+ File.separator 
									+ _logFileName 
									+ LOG_FILE_EXTENSION);
			file.delete();          
			return true;
		}
	}

	/**
	 * checks whether the compiler has generated the expected errors and warnings,
	 * if it created more problems than specified this is OK for this method.
	 * @param problemIDs IDs of the expected errors and warnings as specified in 
	 *                   org.eclipse.jdt.core.compiler.IProblem
	 */
	public boolean hasAtLeastExpectedProblems(int[] problemIDs)
	{
		expected: for (int i = 0; i < problemIDs.length; i++) {
			for (int j = 0; j < _compiler.logger._globalProblems.size(); j++) {
				if (problemIDs[i] == ((IProblem)_compiler.logger._globalProblems.get(j)).getID())
					continue expected;
			}
			printAllProblems();
			return false;
		}
		File file = new File(_workingDir.getAbsolutePath() 
								+ File.separator 
								+ _logFileName 
								+ LOG_FILE_EXTENSION);
		file.delete();
		return true;
	}

	/**
	 * checks whether the compiler has generated the expected errors and warnings
	 * @param problemIDs IDs of the expected errors and warnings as specified in 
	 *                   org.eclipse.jdt.core.compiler.IProblem
	 */
	public boolean hasExpectedProblems(int[] problemIDs)
	{
		if ( areProblemsEqual(_compiler.logger._globalProblems, problemIDs) )
		{       	
			File file = new File(_workingDir.getAbsolutePath() 
									+ File.separator 
									+ _logFileName 
									+ LOG_FILE_EXTENSION);
			file.delete();
            
			return true;
		}
		else
		{
			printAllProblems();
			return false;
		}
	}
	
    private void printAllProblems()
    {
        for (Iterator iter = _compiler.logger._globalProblems.iterator(); iter.hasNext();)
        {
            IProblem prob = (IProblem)iter.next();
        	System.err.println(prob.toString());
            if (prob.getID() == IProblem.Unclassified) // it was an exception.
                throw new InternalError(prob.toString()); 
        }
    }
	
	/**
	 * checks whether the compiler has generated the expected errors and warnings
	 * @param errorIDs IDs of the expected errors as specified in 
	 *                 org.eclipse.jdt.core.compiler.IProblem
	 * @param warningIDs analogous 
	 */
    public boolean hasExpectedProblems(int[] errorIDs, int[] warningIDs)
    {
 		if (   areProblemsEqual(_compiler.logger._globalErrors,   errorIDs)
 		    && areProblemsEqual(_compiler.logger._globalWarnings, warningIDs) )
        {
        	
            File file = new File(_workingDir.getAbsolutePath() 
            						+ File.separator 
            						+ _logFileName 
            						+ LOG_FILE_EXTENSION);
            file.delete();
            
            return true;
        }
        else
        {
        	return false;
        }
    }
    
    
    /**
     * @param problems	Vector elements of type IProblem
     */
    private boolean areProblemsEqual(List problems, int[] problemIDs)
    {
		if ( problemIDs == null)
		{
			return problems.isEmpty();
		}

        boolean result = true;

        if (problems.size() != problemIDs.length)
        {
            result = false;
        }
        for (Iterator iter = problems.iterator(); result && iter.hasNext();)
        {
            IProblem curProblem = (IProblem) iter.next();
            int curProblemID = curProblem.getID();

            boolean found = false;
            int idx = 0;

            while (!found && (idx < problemIDs.length))
            {
                if (curProblemID == problemIDs[idx])
                {
                    found = true;
                }
                idx++;
            }
            if (!found)
            {
                result = false;
            }
        }
        return result;
    }
    
//	public boolean isCompilationSuccessful(int errors, int warnings)
//	{
//		if (_compiler.globalErrorsCount != errors
//			|| _compiler.globalProblemsCount > (errors + warnings)
//			|| _compiler.globalWarningsCount != warnings)
//		{
//			return false;
//		}
//		else
//		{
//			// TODO (SH): check whether we have the expected problems.
//			File file = new File(_workingDir.getAbsolutePath() 
//									+ File.separator 
//									+ _logFileName 
//									+ LOG_FILE_EXTENSION);
//			file.delete();
//            
//			return true;
//		}
//	}    
    
    
    private String[] createClassPath(String fname)
    {
        File javaFile = new File(_workingDir.getAbsolutePath() 
        							+ File.separator 
        							+ fname 
        							+ JAVA_FILE_EXTENSION);
        
        String[] args =
        {
            "-classpath",
            new Path(ClasspathUtil.OTRE_PATH).toString()
                		+ File.pathSeparator
                		+ new Path(JRE_JAR_PATH).toString()
                		+ File.pathSeparator
                		+ new Path(_workingDir.getAbsolutePath() 
                		+ File.separator).toString(),
            		 javaFile.getAbsolutePath()
        };
                
        return args;
    }
    
    protected String[] createClassPathNoOTRE(String fname)
    {
        File javaFile = new File(_workingDir.getAbsolutePath() 
        							+ File.separator 
        							+ fname 
        							+ JAVA_FILE_EXTENSION);
        
        String[] args =
        {
            "-classpath",
            new Path(JRE_JAR_PATH).toString()
                		+ File.pathSeparator
                		+ new Path(_workingDir.getAbsolutePath() 
                		+ File.separator).toString(),
            javaFile.getAbsolutePath()
        };
                
        return args;
    }
       
       
	protected void setUp() throws Exception
	{
		_workingDir = new File(PROJECT_PATH);
		cleanWorkingDirectory();
		_workingDir.mkdirs();
	}
	   
    protected void tearDown() throws Exception
    {

    }
    
    /**
     * This method was added for convenient testing of single testmethods in testclasses.
     * The first commandline argument is expected to be the class where the 
     * testmethod can be found. The following arguments are the testmethodnames
     * that should run. 
     * example: 
     * java Testbase org.eclipse.objectteams.otdt.tests.compiler.errorreporting.compiler.CalloutBindingTest testMultipleCalloutBinding1 
     * @param args
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		TestSuite selected = null;
		Constructor clsConst =null;
		
		switch (args.length)
		{
			case 0:
			{
				System.err.println("You must specify the class containing the testcases as argument.");
				System.exit(1);
				break; // duh
			}
			
			case 1: // take all methods
			{
				Class testClass = Class.forName(args[0]);
				selected = new TestSuite(testClass);
				break;
			}
		
			default: // single methods to execute given
			{
				Class<?> testClass = Class.forName(args[0]);
				clsConst = testClass.getConstructor( new Class<?>[] { String.class } );
				selected = new TestSuite();
		
				for (int idx = 1; idx < args.length; idx++)
				{
					selected.addTest((Test)clsConst.newInstance( new Object[] { args[idx] } ));
				}
			}
		}
				
		TestRunner.run(selected);		
	}
}
