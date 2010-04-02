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
 * $Id: Requestor.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.smap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ILineNumberAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.core.util.ClassFileReader;

// from org.eclipse.jdt.core.tests.compiler.regression.Requestor
// added capability to check generated line numbers.
public class Requestor extends Assert implements ICompilerRequestor {
	public boolean hasErrors = false;
	public String outputPath;
	private boolean forceOutputGeneration;
	public Hashtable expectedProblems = new Hashtable();
	public String problemLog = "";
	public ICompilerRequestor clientRequestor;
	public boolean showCategory = false;
	public boolean showWarningToken = false;
	HashMap<String, int[]> lineNumbers = new HashMap<String, int[]>();

	
public Requestor(boolean forceOutputGeneration, ICompilerRequestor clientRequestor, boolean showCategory, boolean showWarningToken, HashMap<String, int[]> lineNumbers) {
	this.forceOutputGeneration = forceOutputGeneration;
	this.clientRequestor = clientRequestor;
	this.showCategory = showCategory;
	this.showWarningToken = showWarningToken;
	this.lineNumbers = lineNumbers;
}
public void acceptResult(CompilationResult compilationResult) {
	this.hasErrors |= compilationResult.hasErrors();
	this.problemLog += Util.getProblemLog(compilationResult, this.showCategory, this.showWarningToken);
	outputClassFiles(compilationResult);
	if (this.clientRequestor != null) {
		this.clientRequestor.acceptResult(compilationResult);
	}
}
protected void outputClassFiles(CompilationResult unitResult) {
	if ((unitResult != null) && (!unitResult.hasErrors() || forceOutputGeneration)) {
		ClassFile[]classFiles = unitResult.getClassFiles();
		for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
			// retrieve the key and the corresponding classfile
			ClassFile classFile = classFiles[i];
			if (outputPath != null) {
				String relativeName = 
					new String(classFile.fileName()).replace('/', File.separatorChar) + ".class";
				try {
					org.eclipse.jdt.internal.compiler.util.Util.writeToDisk(true, outputPath, relativeName, classFile);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			if (this.lineNumbers != null) {
				ClassFileReader cfr;
				try {
					cfr = new ClassFileReader(classFile.getBytes(), IClassFileReader.METHOD_INFOS|IClassFileReader.METHOD_BODIES);
				} catch (ClassFormatException e) {
					throw new AssertionFailedError("Can't read class file: "+e.getMessage());
				}
				for (IMethodInfo method : cfr.getMethodInfos()) {
					String fullMethodDesignator = String.valueOf(
															CharOperation.concatWith(
																classFile.getCompoundName(),
																CharOperation.concat(method.getName(), method.getDescriptor()),
																'.'));
					int[] expectedNumbers = this.lineNumbers.get(fullMethodDesignator);
					if (expectedNumbers != null) {
						this.lineNumbers.remove(fullMethodDesignator);
						ILineNumberAttribute lineNumberAttribute = method.getCodeAttribute().getLineNumberAttribute();
						int[][] table = lineNumberAttribute.getLineNumberTable();
						Assert.assertEquals("wrong number of line numbers", expectedNumbers.length, table.length);
						for (int n=0; n<expectedNumbers.length; n++)
							Assert.assertEquals("wrong line numeber", expectedNumbers[n], table[n][1]);
					}					
				}
			}
		}
	}
}
public void checkAllLineNumbersSeen() {
	if (this.lineNumbers != null) {
		if (!this.lineNumbers.isEmpty()) {
			String methods = "";
			for (Map.Entry<String, int[]> entry : this.lineNumbers.entrySet()) {
				System.out.print("Unmatched line numbers for method "+entry.getKey());
				for(int l : entry.getValue())
					System.out.print(" "+l);
				System.out.println();
				methods += " "+entry.getKey();
			}
			Assert.fail("Unmatched line numbers"+methods);
		}
	}
}
}
