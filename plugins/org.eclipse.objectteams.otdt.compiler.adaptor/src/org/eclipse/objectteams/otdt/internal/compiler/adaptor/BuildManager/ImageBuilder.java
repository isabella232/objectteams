/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ImageBuilder.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.compiler.adaptor.BuildManager;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.builder.SourceFile;
import org.eclipse.jdt.internal.core.builder.WorkQueue;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;

/**
 * @author stephan
 */
@SuppressWarnings("restriction")
protected class ImageBuilder playedBy IncrementalImageBuilder 
{
	ImageBuilder (IncrementalImageBuilder builder) {
		BuildManager.this.builder = this;
	}
	
	// Set of teams being compiled due to our special request.
	// don't preserve binary roles within such a team.
	private HashSet<String> teamsForcedRecompilation = new HashSet<String>(); 
	resetQueue:
	void resetQueue() <- after void compile(SourceFile[] files);
	void resetQueue() {
		teamsForcedRecompilation.clear();
	}

	/**
	 * Trigger B1: During processing a type with errors ... 
	 */
	storeProblemsFor <- replace storeProblemsFor;
	
	/** Before storing problems for a given source file, check whether the problem
	 *  could be due to a binary role referring to a stale or missing tsuper-copy.
	 *  In that case trigger recompilation. 
	 *  The binary file will be deleted later ...
	 */
	callin void storeProblemsFor(SourceFile sourceFile, Problem[] problems) 
		throws org.eclipse.core.runtime.CoreException 
	{
		if (sourceFile != null && problems != null && problems.length > 0) {
			Problem[] remainingProblems = new Problem[problems.length];
			int count = 0;
			for (int i = 0; i < problems.length; i++) {
				if (problems[i].couldBeFixedByRecompile()) {
					// record the source file for recompilation:
					ArrayList<SourceFile> sourceFiles = sourceFiles();
					if (!sourceFiles.contains(sourceFile)) {
						sourceFiles.add(sourceFile);
						if (DEBUG>0)
							System.out.println("Abort causes recompile of "+sourceFile); //$NON-NLS-1$
					}
					// don't add the problem to remainingProblems, because any IProblem.IsClassPathCorrect
					// will abort this compilation! (we still think, we can fix this problem..)
					char[] typePath= problems[i].typeToRemove;
					if (typePath != null)
						this.scheduleForRemoval(sourceFile, String.valueOf(typePath));
				} else {
					remainingProblems[count++] = problems[i];
				}
			}
			if (count < problems.length)
				System.arraycopy(remainingProblems, 0, problems = new Problem[count], 0, count);
		}
		base.storeProblemsFor(sourceFile, problems);
	}
	
	void scheduleForRemoval(SourceFile sourceFile, String typePath) 
		-> void scheduleForRemoval(SourceFile sourceFile, String typePath);
	
	/**
	 * Trigger B2: During finishedWith() generated binary files are deleted, but some 
	 *             should perhaps be preserved...
	 */
	shouldPreserveBinary <- replace shouldPreserveBinary;

	/**
	 * If the given binary type is a re-used member (role) preserve the binary file, 
	 * HOWEVER, if it has dependency problems do NOT preserve it.
	 * 
	 * @param cResult
	 * @param sourceFolder
	 * @param packagePath
	 * @param binaryTypeName
	 * @return
	 */
	callin boolean shouldPreserveBinary(CompileResult cResult,
									    IPath sourceFolder,
										IPath packagePath,
										char[] binaryTypeName) 
	{
		// don't preserve re-used binary types with problems. They might be stale.
		if (!base.shouldPreserveBinary(cResult, sourceFolder, packagePath, binaryTypeName))
			return false;
		return shouldPreserveBinaryRole(cResult, sourceFolder, packagePath, binaryTypeName);
	}
	/** Entry from AdaptorActivator.CopyInheritanceObserver (via BuildManager): */
	protected boolean shouldPreserveBinaryRole(ReferenceBinding role, CompileResult cResult) {
		String fileName    = new String(cResult.getFileName());
		IPath packagePath  = new Path(fileName).removeLastSegments(1);
		int packageDepth   = role.getPackage().compoundName.length;
		IPath sourceFolder = packagePath.removeLastSegments(packageDepth);	
		packagePath        = packagePath.removeFirstSegments(sourceFolder.segmentCount());
		char[][] roleName  = CharOperation.splitOn('.', role.attributeName());
		boolean result = shouldPreserveBinaryRole(cResult, sourceFolder, packagePath, roleName[roleName.length-1]); // Team$Role
		if (!result)
			scheduleForRemoval(findTeamSourceFile(fileName), new String(role.constantPoolName()));
		return result;
	}
	/** Common implementation for the two entries above.
	 *  Both clients when receiving a 'false' answer will remove the stale binary class.
	 *  This will ensure that during the next cycle this type will be compiled from source.
	 */
	boolean shouldPreserveBinaryRole(CompileResult cResult, IPath sourceFolder, IPath packagePath, char[] binaryTypeName) 
	{
		// binary type with problem -> NO 		
		if (binaryHasProblem(cResult.problems(), binaryTypeName))
			return false;
		
		String binaryTypeString = packagePath.toString()+'/'+new String(binaryTypeName);
		if (DEBUG >= 2)
			System.out.print("candidate for preserving: "+binaryTypeString); //$NON-NLS-1$
		
		// predefined type -> YES
		if (isPredefinedRole(binaryTypeName)) {
			if (DEBUG >= 2)
				System.out.println(" YES(predefined)."); //$NON-NLS-1$
			return true;
		}
		// others: further investigate.
		boolean shouldPreserve = shouldCandidateBePreserved(sourceFolder, binaryTypeString);
		if (!shouldPreserve)
			// propagate changes down to sub-teams:
			ClassFileChangeTracker.nonStructuralChange(binaryTypeString);
		return shouldPreserve;
	}
	/** Checks the following reasons against preserving:<ul>
	 *  <li> the enclosing team is forced for recompilation (meaning: full recompilation)
	 *  <li> the given type is a role known to be stale                                   */
	boolean shouldCandidateBePreserved(IPath sourceFolder, String binaryTypeString) 
	{
		int dollarPos = binaryTypeString.lastIndexOf('$');
		String enclosingRelativeFileString = binaryTypeString.substring(0, dollarPos)+".java"; //$NON-NLS-1$
		String enclosingAbsoluteFileString = sourceFolder.append(enclosingRelativeFileString).toString();
		for (String teamFileName : teamsForcedRecompilation) {
			if (teamFileName.equals(enclosingAbsoluteFileString)) {
				if (DEBUG >= 2)
					System.out.println(" NO(forced)."); //$NON-NLS-1$
				BuildManager.this.teamsToRecompile.add(teamFileName); // let binary roles be removed and try again.
				return false;
			}
		}		
		binaryTypeString = sourceFolder.append(binaryTypeString).toString();
		if (BuildManager.this.staleRoles.contains(binaryTypeString)) {
			BuildManager.this.staleRoles.remove(binaryTypeString);
			if (DEBUG >= 2)
				System.out.println(" NO(changed)."); //$NON-NLS-1$
			return false;
		}
		
		if (DEBUG >= 2)
			System.out.println(" YES"); //$NON-NLS-1$
		return true;		
	}
	
	/** Is the given binary type known to have an unresolved dependency? */
	boolean binaryHasProblem(Problem[] problems, char[] binaryTypeName) {
		if (problems != null) 
			for (Problem problem : problems) 
				if (problem != null && problem.abortException != null) {
					Abort abort = problem.abortException;
					for (BinaryType binding : abort.referencedBinaries) {
						char[][] compoundName = binding.compoundName();
						if (CharOperation.equals(compoundName[compoundName.length-1], binaryTypeName))
							return true;
					}
				}
		// remove "__OT__" from binaryTypeName
		char[] strippedName = OTNameUtils.removeOTDelim(binaryTypeName);
		if (strippedName != binaryTypeName)
			return binaryHasProblem(problems, strippedName);
		return false;
	}
	
	// Trigger: after adding source files to compile,
	//          consider any roles which have been copied:
	//          if the source changed, all sub teams must be recompiled.
	void addAffectedTeamFiles() <- after void addAffectedSourceFiles();
	
	void addAffectedTeamFiles() {
		if (++BuildManager.this.retries > MAX_RETRIES)
			BuildManager.this.deactivate();

		// fetch sets of teams:
		Set<String> teamFiles = fetchTeamsToRecompile();
		
		ArrayList<SourceFile> sourceFiles = sourceFiles();
		
		// add all relevant teams to sourceFiles:
		for (String teamName : teamFiles) {
			SourceFile teamFile = findTeamSourceFile(teamName);
			if (   teamFile != null 
				&& !sourceFiles.contains(teamFile)) 
			{
				if (DEBUG>0)
					System.out.println("Scheduling for recompilation: teamFile "+teamFile+" for "+teamName); //$NON-NLS-1$ //$NON-NLS-2$
				sourceFiles.add(teamFile);
				teamsForcedRecompilation.add(teamName);
			}
		}
	}
	SourceFile findTeamSourceFile(String teamName) {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IFile file = ws.getRoot().getFile(new Path(teamName));
		return findSourceFile(file);
	}

	// ==== GENERAL ACCESS TO BASE ELEMENTS: ====
	
	@SuppressWarnings("decapsulation")
	SourceFile findSourceFile(IFile file) -> SourceFile findSourceFile(IFile file, boolean mustExist)
		with { file -> file, true -> mustExist, result <- result }

	@SuppressWarnings({"decapsulation", "unchecked"})
	ArrayList<SourceFile> sourceFiles() -> get ArrayList<SourceFile> sourceFiles;
	
	@SuppressWarnings("decapsulation")
	WorkQueue   getWorkQueue()          -> get WorkQueue workQueue;
		
	// ==== LOGGING ====
	void logCompile(String msg) <- before void compile(SourceFile[] units)
		with { msg <- "Starting" } //$NON-NLS-1$

	logDone:
	void logCompile(String msg) <- after void compile(SourceFile[] units)
		with { msg <- "Done" } //$NON-NLS-1$

	void logCompile(String msg)
		when (DEBUG > 0)
	{
		System.out.println("Incremental compilation: "+msg+" for "+getWorkQueue()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	precedence after logDone, resetQueue;
}