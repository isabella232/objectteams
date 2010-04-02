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
 * $Id: BuildManager.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

import base org.eclipse.jdt.core.compiler.CategorizedProblem;
import base org.eclipse.jdt.internal.compiler.CompilationResult;
import base org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader; // base-class of a role file
import base org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import base org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import base org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import base org.eclipse.jdt.internal.compiler.problem.AbortType;
import base org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import base org.eclipse.objectteams.otdt.internal.core.compiler.lifting.LiftingEnvironment;
import base org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder; // base-class of a role file

/**
 * This Team observes the build/compile process and advises the IncrementalImageBuilder, 
 * when and what to recompile due to OT-specific dependencies.
 * 
 * @author stephan
 * @version $Id: BuildManager.java 23451 2010-02-04 20:33:32Z stephan $
 */
@SuppressWarnings("restriction")
public team class BuildManager extends CompilationThreadWatcher
{
	public static int DEBUG = 0; // levels: 0 nothing, 1 some, 2 more.

	/** Remember the (one) ImageBuilder role. */
	ImageBuilder builder = null;
	
	// ====== Data common for all roles (incl. accessors and life-cycle). ======
	
	
	/** Manage cross-ref table for copied roles:
	 *  PRODUCER: RoleCopyTracker.trackCopyRole()
	 *  CONSUMER: ClassFileChangeTracker.nonStructuralChange()
	 *  
	 *  ENCODING: RoleTypeName -> Set<TeamSourceFileName> */
	HashMap<String, Set<String>> roleToSubTeams = new HashMap<String, Set<String>>();
	
	void recordCopiedRole(char[] roleName, char[] subTeamSourceFileName) {
		String roleString = new String(roleName);
		Set<String> teams = roleToSubTeams.get(roleString);
		if (teams == null) 
			roleToSubTeams.put(roleString, teams = new HashSet<String>());
		teams.add(new String(subTeamSourceFileName));
		if (DEBUG >= 2)
			System.out.println("role "+roleString+" is COPIED to "+new String(subTeamSourceFileName));  //$NON-NLS-1$//$NON-NLS-2$
	}
//	// debug helper
//	void printCopyTable() {
//		System.out.print("-------------------------");
//		for (Map.Entry<String, Set<String>> entry : roleToSubTeams.entrySet()) {
//			System.out.print("\nRole "+entry.getKey());
//			for (String value : entry.getValue()) {
//				System.out.print("\n\t"+value);
//			}
//		}
//		System.out.println("\n-------------------------");
//	}
	
	/** If a tsuper role has non-structural changes, some sub-teams need to be recompiled.
	 *  This set holds the teams to be recompiled.
	 *
	 *  PRODUCERS: ClassFileChangeTracker.nonStructuralChange()
	 *  		   ImageBuilder.shouldPreserveBinary() (during compile->finishedWith)
	 *  CONSUMER:  ImageBuilder.addAffectedTeamFiles()
	 *  
	 *  ENCODING: source file name
	 */
	HashSet<String> teamsToRecompile = new HashSet<String>();
	
	/** Get and clear the set of teams waiting for recompilation. */
	HashSet<String> fetchTeamsToRecompile() {
		HashSet<String> result = teamsToRecompile;
		teamsToRecompile = new HashSet<String>();
		return result;
	}

	/** If a tsuper role has non-structural changes, some sub-teams need to be recompiled.
	 *  This set holds the roles that should NOT be preserved.
	 *  PRODUCER: ClassFileChangeTracker.nonStructuralChange()
	 *  CONSUMER: shouldPreserveBinary() (via shouldCandidateBePreserved())
	 *  
	 *  ENCODING: Qualified Type Name. p.T$I, canonical form, i.e., __OT__ prefixes removed.
	 */
	HashSet<String> staleRoles = new HashSet<String>();

	synchronized void initializeDependencyStorage() {
		this.roleToSubTeams = new HashMap<String, Set<String>>();
		this.teamsToRecompile = new HashSet<String>();
		this.staleRoles = new HashSet<String>();
	}
	
	protected class BinaryType playedBy BinaryTypeBinding 
	{
		/** Record if a given type depends on an unresolvable type.
		 *  More specifically, we look for roles depending on unresolvable tsuper roles. 
		 */
		superInterfaces         <- replace superInterfaces;
		callin ReferenceBinding[] superInterfaces() {
			try { 
				return base.superInterfaces();
			} catch (AbortCompilation as Abort abort) {
				abort.referencedBinaries.add(this);
				throw abort;
			}
		}
		
		// ==== Callouts: ==== 
				
		char[][] compoundName() -> get char[][] compoundName;

		// DEBUGGING:
		String internalName() -> char[] internalName() 
			with { result <- new String(result) }
	}
	
	protected class ClassScope playedBy ClassScope
	{
		@SuppressWarnings("decapsulation")
		void connectTypeHierarchy() <- replace void connectTypeHierarchy();
		@SuppressWarnings("basecall")
		callin void connectTypeHierarchy() {
			try {
				base.connectTypeHierarchy();
			} catch (org.eclipse.jdt.internal.compiler.problem.AbortType at) {
				TypeDeclaration referenceContext = referenceContext();
				SourceTypeBinding sourceType = referenceContext.binding;
				at.updateContext(referenceContext, referenceCompilationUnit().compilationResult);
				referenceContext.ignoreFurtherInvestigation = true;
				if (sourceType.superInterfaces == null)
					sourceType.superInterfaces = TypeBinding.NO_SUPERINTERFACES; // TODO(SH): recurse?
				// don't rethrow, marking ignoreFurtherInvestigation is enough abortion
			}
		}
		TypeDeclaration referenceContext()                    -> get TypeDeclaration referenceContext;
		CompilationUnitDeclaration referenceCompilationUnit() -> CompilationUnitDeclaration referenceCompilationUnit();
	}

	protected class LiftingEnv playedBy LiftingEnvironment 
	{
		callin void init(TypeDeclaration teamType) {
			try {
				base.init(teamType);
			} catch (org.eclipse.jdt.internal.compiler.problem.AbortType at) {
				teamType.ignoreFurtherInvestigation = true;
				BuildManager.this.teamsToRecompile.add(String.valueOf(teamType.compilationResult.fileName));
				throw at; // TODO(SH): might want to mark certain AT-instances as recoverable?
			}
		}
		init <- replace init;
	}
	
	/** This role helps to link Problem->Abort->BinaryType,
	 *  in order to determine whether a problem was caused by an unresolvable tsuper role.  
	 */
	protected class Abort playedBy AbortCompilation 
	{
		public List<BinaryType> referencedBinaries = new ArrayList<BinaryType>();

		/** Trigger: this role has more to be updated from the context. */
		void updateContext(ASTNode astNode, CompileResult unitResult) 
			<- after void updateContext(ASTNode astNode, CompilationResult unitResult);

		/** 
		 * If the current abort exception could possibly be fixed by a recompile,
		 * + convert the exception onto a less drastic AbortType 
		 * + record the link Problem->Abort.
		 */
		public void updateContext(ASTNode astNode, CompileResult unitResult) {
			Problem problem = getProblem();
			if (   problem != null
				&& problem.couldBeFixedByRecompile()
				&& unitResult.isReusingBinaryMember()) 
			{ 
				// convert AbortCompilation into AbortType
				Abort abort = new AbortType(
								new org.eclipse.jdt.internal.compiler.problem.AbortType(
									unitResult, problem));
				abort.referencedBinaries = this.referencedBinaries;
				problem.abortException = abort;				
				RuntimeException ex = abort;
				throw ex;
			}
		}
	
		protected Problem getProblem() -> get CategorizedProblem problem;
	}
	
	protected class AbortType extends Abort playedBy AbortType 
	{
//		// FIXME : this ctor gives a VerifyError:
//		AbortType (CompileResult result, Problem problem) {
//			base(result, problem);
//			problem.abortException = this;
//		}
		@Override
		public void updateContext(ASTNode astNode, CompileResult unitResult) {
			// no super call: would cause recursive creation of roles
			Problem problem = getProblem();
			if (problem != null) {
				problem.abortException = this;
			}
		}
	}
	
	/** Intermediate purely callout role. */
	protected class CompileResult playedBy CompilationResult
	{
	  protected // don't publically export protected role Problem
		Problem[] problems()                     -> get CategorizedProblem[] problems;
		
		// TODO(SH): workaround for lowering problem concerning base call in ImageBuilder.shouldPreserveBinary() 
		boolean hasBinaryMember(char[] typeName) ->     boolean hasBinaryMember(char[] typeName);
		
		@SuppressWarnings({"decapsulation", "unchecked"})
	  protected
		boolean isReusingBinaryMember() 		 -> get ArrayList binaryMemberNames
			with { result <- !(result == null || result.isEmpty()) }
		
		char[] getFileName() -> char[] getFileName();
		// debugging:
		toString => toString;
	}
	
	/** Let a problem know about the associated exception. */
	protected class Problem playedBy CategorizedProblem 
	{
		public Abort abortException = null;
		public char[] typeToRemove= null;
	
		/** Certain problem reasons might be fixed by recompilation. */
		protected boolean couldBeFixedByRecompile() {
			switch (getID()) {
			case IProblem.IsClassPathCorrect:
			case IProblem.StaleTSuperRole:
			case IProblem.StaleSubRole:
			case IProblem.MissingRoleInBinaryTeam:
			case IProblem.RoleFileInBinaryTeam:
			case IProblem.CorruptBytecode:
			case IProblem.MissingAccessorInBinary:
			case IProblem.MismatchingRoleParts:
			case IProblem.InconsistentlyResolvedRole:
			case IProblem.NotGeneratingCallinBinding:
				return true;
			default:
				return false;
			}
		}
		
		int getID() -> int getID();
		// debugging:
		toString => getMessage;
	}

	/** Watch specific error reports. */
	protected class ProblemReporter playedBy ProblemReporter {

		// a missing role in a binary team means the binary team should probably be deleted.
		void missingRoleInBinaryTeam(ReferenceBinding type)
		<- replace void missingRoleInBinaryTeam(char[] roleName, ReferenceBinding enclosingTeam)
			with { type <- enclosingTeam }

		callin void missingRoleInBinaryTeam(ReferenceBinding type) {
			ReferenceContext context= getReferenceContext();
			try {
				base.missingRoleInBinaryTeam(type);
			} finally {
				if (context != null && context.compilationResult() != null) {			
					int count= context.compilationResult().problemCount;
					recordTypeToRemove(context.compilationResult().problems[count-1], 
										  type.constantPoolName());
				}
			}
		}

		ReferenceContext getReferenceContext() -> get ReferenceContext referenceContext;
	}

	// ---- ImageBuilder is a role file ---- 

	
	// ---- Team level features: ----
	void recordTypeToRemove(CategorizedProblem as Problem prob, char[] roleName) {
		prob.typeToRemove= roleName;
	}

	public static boolean isPredefinedRole(char[] roleName) {
		int dollarPos = CharOperation.lastIndexOf('$', roleName);
		if (dollarPos != -1) 
			roleName = CharOperation.subarray(roleName, dollarPos+1, -1);
		return CharOperation.equals(roleName, IOTConstants.CONFINED)
			|| CharOperation.equals(roleName, IOTConstants.OTCONFINED)
			|| CharOperation.equals(roleName, IOTConstants.ICONFINED)
			|| CharOperation.equals(roleName, IOTConstants.IBOUNDBASE)
			|| CharOperation.equals(roleName, IOTConstants.ILOWERABLE);					
	}
	
	/** Answer whether the given role type should be re-used (ask the ImageBuilder). */
	public boolean shouldPreserveBinaryRole(ReferenceBinding role, CompilationResult as CompileResult cResult) {
		if (builder == null)
			return true;
		return builder.shouldPreserveBinaryRole(role, cResult);
	}
	
	public String canonicalName(String roleName) {
		return roleName.replace(IOTConstants.OT_DELIM, ""); //$NON-NLS-1$
	}
}
