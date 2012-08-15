/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractOTJLDTest.java 23534 2010-02-19 18:48:08Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractComparableTest;
import org.eclipse.jdt.core.tests.compiler.regression.InMemoryNameEnvironment;
import org.eclipse.jdt.core.tests.compiler.regression.RegressionTestSetup;
import org.eclipse.jdt.core.tests.compiler.regression.Requestor;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.ClasspathUtil;

/**
 * Common super class for tests that were formerly implemented using jacks.
 * @author stephan
 */
public class AbstractOTJLDTest extends AbstractComparableTest {

	/** a test verifier that allows reusing a running vm even in the presence of (constant) vm arguments. */
	protected static class OTTestVerifier extends TestVerifier {
		protected OTTestVerifier(boolean reuseVM) {
			super(reuseVM);
			this.fVMArguments = OT_VM_ARGS;
		}
		@Override
		public boolean verifyClassFiles(String sourceFilePath, String className, String expectedOutputString, String expectedErrorStringStart, 
										String[] classpaths, String[] programArguments, String[] vmArguments) {
			vmArguments = mergeArgs(vmArguments);
			return super.verifyClassFiles(sourceFilePath, className, expectedOutputString, expectedErrorStringStart, classpaths, programArguments, vmArguments);
		}
		@Override
		public boolean vmArgsEqual(String[] newArgs) {
			return super.vmArgsEqual(mergeArgs(newArgs));
		}
		protected String[] mergeArgs(String[] newArgs) {
			if (newArgs == null)
				return OT_VM_ARGS;
			else {
				int l1 = OT_VM_ARGS.length;
				int l2 = newArgs.length;
				String[] result = new String[l1+l2];
				System.arraycopy(OT_VM_ARGS, 0, result, 0, l1);
				System.arraycopy(newArgs, 0, result, l1, l2);
				return result;
			}
		}
	}
	
	public static final String[] OT_VM_ARGS = new String[] {
			"-javaagent:"+ClasspathUtil.OTAGENT_JAR_PATH,
			"-Xbootclasspath/a:"+ClasspathUtil.OTRE_MIN_JAR_PATH,
			"-Dot.dump=1"
		};
	// ===
	
	protected static final JavacTestOptions DEFAULT_TEST_OPTIONS = new JavacTestOptions();

	// shall compiler output be matched exactly or using some matching?
	boolean errorMatching = false;
	
	// each subarray defines a set of classes to be compiled together:
	protected String[][] compileOrder;
	
	public AbstractOTJLDTest(String name) {
		super(name);
	}

	/** Add otre and bcel to the class path. */
	@Override
	protected String[] getDefaultClassPaths() {
		String[] defaults = super.getDefaultClassPaths();
		int len = defaults.length;
		System.arraycopy(defaults, 0, defaults=new String[len+2], 0, len);
		defaults[len] = new Path(ClasspathUtil.OTRE_PATH).toString();
		defaults[len+1] = ClasspathUtil.BCEL_JAR_PATH.toString();
		return defaults;
	}

	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		if (setUp instanceof RegressionTestSetup) {
			RegressionTestSetup regressionSetTup = (RegressionTestSetup) setUp;
			if (!(regressionSetTup.verifier instanceof OTTestVerifier)) {
				// overwrite plain TestVerifier:
				regressionSetTup.verifier = this.verifier = new OTTestVerifier(true/*reuseVM*/);
			}
		}
	}

	@Override
	protected TestVerifier getTestVerifier(boolean reuseVM) {
		return new OTTestVerifier(reuseVM);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.compileOrder = null;
		super.tearDown();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
		return options;
	}
	
	protected void runNegativeTestMatching(String[] testFiles, String expectedCompilerLog) {
		this.errorMatching = true;
		// ensure expectingCompilerError is set, which runNegativeTest(String[],String) not always does
		super.runNegativeTest(testFiles, expectedCompilerLog, DEFAULT_TEST_OPTIONS);
		this.errorMatching = false;
	}

	/** Relaxed comparison using contains rather than equals. TODO(SH): pattern matching. */
	protected void checkCompilerLog(String[] testFiles, Requestor requestor,
			String platformIndependantExpectedLog, Throwable exception) {
		String computedProblemLog = Util.convertToIndependantLineDelimiter(requestor.problemLog.toString());
		if (!computedProblemLog.contains(platformIndependantExpectedLog)) {
			logTestTitle();
			System.out.println(Util.displayString(computedProblemLog, INDENT, SHIFT));
			logTestFiles(false, testFiles);
			if (errorMatching && exception == null)
				assertTrue("Invalid problem log\n"+computedProblemLog, false);
		}
		if (!errorMatching && exception == null) {
			assertEquals("Invalid problem log ", platformIndependantExpectedLog, computedProblemLog);
		}
    }
	// inaccessible helper from super
	void logTestFiles(boolean logTitle, String[] testFiles) {
		if (logTitle) {
			logTestTitle();
		}
		for (int i = 0; i < testFiles.length; i += 2) {
			System.out.print(testFiles[i]);
			System.out.println(" ["); //$NON-NLS-1$
			System.out.println(testFiles[i + 1]);
			System.out.println("]"); //$NON-NLS-1$
		}
	}
	// inaccessible helper from super
	void logTestTitle() {
		System.out.println(getClass().getName() + '#' + getName());
	}
	
	// support explicit compile order in several steps:
	@Override
	protected void compileTestFiles(Compiler batchCompiler, String[] testFiles) {
		if (this.compileOrder == null) {
			super.compileTestFiles(batchCompiler, testFiles);
		} else {
			for (String[] bunch : this.compileOrder) {
				String[] bunchFiles = new String[bunch.length * 2];
				int b = 0;
				for (int i = 0; i < bunch.length; i++) {
					int b0 = b;
					for (int j = 0; j < testFiles.length; j+=2) {
						if (bunch[i].equals(testFiles[j])) {
							bunchFiles[b++] = testFiles[j];
							bunchFiles[b++] = testFiles[j+1];
						}
					}
					assertTrue("Unmatched filename: "+bunch[i], b == b0+2);
				}
				super.compileTestFiles(batchCompiler, bunchFiles);
				batchCompiler.lookupEnvironment.nameEnvironment.cleanup(); // don't use chached info from previous runs
			}
		}
	}
	
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		// make cleanup weaker:
		return new InMemoryNameEnvironment(testFiles, getClassLibs(false)) {
			@Override
			public void cleanup() {
				for (int i = 0, max = this.classLibs.length; i < max; i++)
					if (this.classLibs[i] instanceof FileSystem)
						((FileSystem) this.classLibs[i]).softReset();
			}
		};
	}
	
	/** Additional entry for tests expecting a compiler warning and don't run. */
	protected void runTestExpectingWarnings(String[] files, String expectedWarnings) {
    	Map options = getCompilerOptions();
		runConformTest(
	 		// test directory preparation
			true /* flush output directory */,
			files,
			// compiler options
			null /* no class libraries */,
			options /* custom options - happen to be the default not changed by the test suite */,
			// compiler results
			expectedWarnings,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings /* javac test options */);	
	}
	
	/** Additional entry for tests expecting a compiler warning and don't run. */
	protected void runTestExpectingWarnings(String[] files, String expectedWarnings, Map options) {
		runConformTest(
	 		// test directory preparation
			true /* flush output directory */,
			files,
			// compiler options
			null /* no class libraries */,
			options /* custom options - happen to be the default not changed by the test suite */,
			// compiler results
			expectedWarnings,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings /* javac test options */);	
	}
	
	/** Additional entry for tests expecting a compiler warning and don't run. */
	protected void runTestExpectingWarnings(String[] files, String expectedWarnings, boolean flushOutputDirectory) {
    	Map options = getCompilerOptions();
		runConformTest(
	 		// test directory preparation
			flushOutputDirectory,
			files,
			// compiler options
			null /* no class libraries */,
			options /* custom options - happen to be the default not changed by the test suite */,
			// compiler results
			expectedWarnings,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings /* javac test options */);	
	}
	/** Additional entry for tests expecting a compiler warning and run. */
	protected void runTestExpectingWarnings(String[] files, String expectedWarnings, String expectedOutput) {
    	Map options = getCompilerOptions();
		runConformTest(
	 		// test directory preparation
			true/*flushOutputDirectory*/,
			files,
			// compiler options
			null /* no class libraries */,
			options /* custom options - happen to be the default not changed by the test suite */,
			// compiler results
			expectedWarnings,
			// runtime results
			expectedOutput,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings /* javac test options */);	
	}

	protected void myWriteFiles(String[] testFiles) {
		// force the directory to comply with the infrastructure from AbstractRegressionTest:
		String testName = null;
		try {
			testName = getName();
			setName("regression");
			// Write files in dir
			writeFiles(testFiles);
		} finally {
			setName(testName);
		}
	}
	
	protected String getTestResourcePath(String filename) {
        try
        {
            URL platformURL = Platform
				                .getBundle("org.eclipse.objectteams.otdt.tests")
				                .getEntry("/testresources/"+filename);
            return new File(FileLocator.toFileURL(platformURL).getFile())
                .getAbsolutePath();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
	}
	/** Create a vm arg for team activation and reset the verifier (avoid reuse of a running VM). */
	protected String[] getTeamActivationVMArgs(String relativeFilePath) {
		if (this.verifier != null)
			this.verifier.shutDown();
        this.verifier = getTestVerifier(false);
        this.createdVerifier = true;
		return new String[] {
				"-Dot.teamconfig="+OUTPUT_DIR+'/'+relativeFilePath
		};
	}
}
