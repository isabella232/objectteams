/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RenameAdaptor.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.rename.RenameMethodAmbuguityMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.rename.RenameMethodOverloadingMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.OTRefactoringCoreMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.rename.BaseCallFinder;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

import base org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import base org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;
import base org.eclipse.jdt.internal.corext.refactoring.rename.RippleMethodFinder2;

/**
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
public team class RenameAdaptor 
{
	@SuppressWarnings("basecall")
	protected class MethodChecks playedBy MethodChecks 
	{
		// dispatching some methods. Might consider collecting the dispatch and the implementation..		
		
		static callin IMethod getTopmostMethod(IMethod method, ITypeHierarchy typeHierarchy, IProgressMonitor monitor) 
				throws JavaModelException 
		{
			return RefactoringUtil.getTopmostMethod(method, typeHierarchy, monitor);
		}
		getTopmostMethod <- replace getTopmostMethod;
		
		static callin boolean isVirtual(IMethod method) 
				throws JavaModelException 
		{
			return RefactoringUtil.isVirtual(method);
		}
		boolean isVirtual(IMethod method) <- replace boolean isVirtual(IMethod method);
	}
	
	@SuppressWarnings("decapsulation") // multiple
	protected class RippleMethodFinder2 playedBy RippleMethodFinder2 
	{
		@SuppressWarnings("basecall")
		callin void createHierarchyOfDeclarations(IProgressMonitor pm, WorkingCopyOwner owner) 
				throws JavaModelException 
		{
// UNUSED orig:
//			IRegion region= JavaCore.newRegion();
//			for (Iterator<IMethod> iter= getFDeclarations().iterator(); iter.hasNext();) {
//				IType declaringType= iter.next().getDeclaringType();
//				region.add(declaringType);
//			}
//			fHierarchy= JavaCore.newTypeHierarchy(region, owner, pm);
	//{ObjectTeams: use OTTypeHierarchy
			IType declaringType= getFMethod().getDeclaringType();
			// FIXME(SH): OTTypeHierarchy cannot be computed for regions!
			OTTypeHierarchy hier = new OTTypeHierarchy(declaringType, true);
			hier.refresh(pm);
			setFHierarchy(hier);
	//jsv}		
		}
		
		static callin IMethod[] getRelatedMethods(IMethod method, IProgressMonitor monitor) 
			throws CoreException 
		{
			// for combining two result arrays:
			HashSet<IMethod> methods = new HashSet<IMethod>();
		
			// OT-Strategy (knows about implicit inheritance):
			// TODO(SH): passing null-TypeHierarchy probably causes a performance penalty.
			IMethod topMethod= RefactoringUtil.getTopmostMethod(method, null, monitor);
			if (topMethod != null)
				method= topMethod;
			IMethod[] methodArray = RefactoringUtil.getOverriddenMethods(method, monitor);
			if (methodArray != null)
				for (IMethod m : methodArray)
					methods.add(m);
			
			// Java-Strategy:
			methodArray = base.getRelatedMethods(method, monitor);
			if (methodArray != null)
				for (IMethod m : methodArray)
					methods.add(m);
			
			// merge results:
			methodArray = new IMethod[methods.size()];
			return methods.toArray(methodArray);
		}
		IMethod[] getRelatedMethods(IMethod method, IProgressMonitor pm)
			<- replace IMethod[] getRelatedMethods(IMethod method, ReferencesInBinaryContext binaryRefs,
												   IProgressMonitor pm, WorkingCopyOwner owner)
			with { method <- method, pm <- pm, result -> result }
		

		createHierarchyOfDeclarations <- replace createHierarchyOfDeclarations;
		void setFHierarchy(ITypeHierarchy fHierarchy) -> set ITypeHierarchy fHierarchy;
		IMethod getFMethod() -> get IMethod fMethod;
	}
	/**
	 * This class implements the adaptation of the rename virtual method
	 * refactoring for OT/J 
	 * (both from otdt..RenameVirtualMethodProcessor and OTRenameVirtualMethodProcessor).
	 * 
	 * <p>The adaptation is implemented as follows:</p>
	 * <p>
	 * Preconditions:
	 * <ol>
	 *   <li><code>checkFinalConditions(IProgressMonitor, CheckConditionsContext)
	 *       </code>:</li>
	 *   <ul>
	 *     <li>An additional check has been added for cases, where a role class
	 *         implements an interface. See the comment of this method for a more
	 *         detailed description of the added check.</li>
	 *     <li>For collecting all references to the target method, the <code>
	 *         RefactoringSearchEngine</code> is used <code>(RenameMethodProcessor.
	 *         getOccurrences(IProgressMonitor, RefactoringStatus))</code>. In
	 *         particular, the classes <code>MatchLocatorParser</code>, <code>
	 *         MatchLocator</code>, <code>PatternLocator</code>, <code>OrLocator
	 *         </code>, and <code>MethodLocator</code> have been modified, in order
	 *         to find references in callin and callout method bindings.</li>
	 *   </ul>
	 * </ol>
	 * </p>
	 * 
	 * @author brcan
	 */
	@SuppressWarnings("decapsulation") // multiple!
	protected class RenameVirtualMethodProcessor
//			extends JavaRenameProcessor
			playedBy RenameVirtualMethodProcessor
	{	
		// copied from base method
		@SuppressWarnings("basecall")
		callin ITypeHierarchy getCachedHierarchy(IType declaring, IProgressMonitor monitor) 
				throws JavaModelException 
		{
			ITypeHierarchy cachedHierarchy = getFCachedHierarchy();
			if (cachedHierarchy != null && declaring.equals(cachedHierarchy.getType()))
				return cachedHierarchy;
	//{ObjectTeams: use OTTypeHierarchy
			//orig: fCachedHierarchy= declaring.newTypeHierarchy(new SubProgressMonitor(monitor, 1));
			cachedHierarchy = new OTTypeHierarchy(declaring, true);
			cachedHierarchy.refresh(monitor);
			setFCachedHierarchy(cachedHierarchy);
	//jsv}
			return cachedHierarchy;
		}
		getCachedHierarchy <- replace getCachedHierarchy;
		
		ITypeHierarchy getFCachedHierarchy() -> get ITypeHierarchy fCachedHierarchy;
		void setFCachedHierarchy(ITypeHierarchy fCachedHierarchy) -> set ITypeHierarchy fCachedHierarchy;

		// communication vars for enclosing doCheckFinalConditions():
		IProgressMonitor tmpMonitor = null;
		String tmpErrorMsg = null;
		/*
		 * TODO(SH): isSpecialCase() is only invoked for interface methods. Is this the correct hook?
		 */
		void isOTSpecialCase () throws CoreException 
		{
			if (RefactoringUtil.isOTSpecialCase(getMethod(),
					getNewElementName(),
					true,
					tmpMonitor))
			{
				tmpErrorMsg = OTRefactoringCoreMessages.getString("RenameVirtualMethodRefactoring.special_team_method"); //$NON-NLS-1$
			}
		}
		 isOTSpecialCase <- after isSpecialCase;
		/**
	     * Provides OT-specific final condition checking and performs the
	     * final condition checking provided by the standard Eclipse
	     * implementation.
	     * <p>
	     * What:
	     * <ol>
	     *   <li>OT-specific final condition checking:</li>
	     *   <ul>
		 *     <li>The following additional checks/changes have been added:
		 *         (1) check for overloading and ambiguous method specs.
		 *         (2) Update base calls when renaming callin methods.
		 *     </li>
	     *   </ul>
	     * </ol>
	     * </p>
	     */
		callin RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext) 
				throws CoreException 
		{
			tmpMonitor = pm;
			tmpErrorMsg = null;
			RefactoringStatus result = null;
			try {
				result = base.doCheckFinalConditions(pm, checkContext);
				result.merge(checkOverloadingAndAmbiguity(pm));
			} finally {
				if (tmpErrorMsg != null && result != null)
					result.addError(tmpErrorMsg);
				tmpErrorMsg = null;
			}
			return result;
		}
		doCheckFinalConditions <- replace doCheckFinalConditions;
	
		//	also check for overloading and ambiguity in OT-subclassed elements
		@SuppressWarnings("nls")
		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm)
				throws JavaModelException 
		{
	    	IMethod newMethod = getOriginalMethod();
	    	IType focusType = getOriginalMethod().getDeclaringType();
	    	if (focusType.isAnnotation() || focusType.isEnum())
	    		return new RefactoringStatus();
	    	ICompilationUnit cu = focusType.getCompilationUnit();
	    	String newMethodName = getNewElementName();
			
			try
			{
				return RefactoringUtil.checkOverloadingAndAmbiguity(cu,
						focusType,
						newMethodName,
						getOriginalMethod().getElementName(),
						newMethod.getParameterTypes(),
						new RenameMethodAmbuguityMsgCreator(),
						new RenameMethodOverloadingMsgCreator(), pm);
			} 
	    	catch (JavaModelException e)
			{
	    		//TODO(jsv): Use meaningful message and store it in the message file
	    		RefactoringStatus result = new RefactoringStatus();
	    		result.addError("Error while checking overloading and ambiguity");
	    		return result;
			}
		}
		
		void addOccurrences(TextChangeManager manager, IProgressMonitor pm, RefactoringStatus status) 
	    		throws CoreException 
	    {
	    	createChangeForPotentialBaseCalls(manager);
	    }
	    addOccurrences <- after addOccurrences;

	    /**
	     * Creates changes for base call in a callin method.
	     * Appearance of warning or error is not possible, this method does not
	     * return a refactoring status.
	     */
	    private void createChangeForPotentialBaseCalls(TextChangeManager changeManager)
	    {
	        Set<IMethod> methodsToRename = getMethodsToRename();
	        
	        for (Iterator<IMethod> methods = methodsToRename.iterator(); methods.hasNext();)
	        {
	            IMethod currentMethod =  methods.next();
	            MethodDeclaration methodDeclaration = null;
	    		try
	    		{
	    		    if (!Flags.isCallin(currentMethod.getFlags()))
	    		    {
	    		        continue;
	    		    }
	    			methodDeclaration = RefactoringUtil.getMethodDeclaration(currentMethod);
	    		} 
	        	catch (JavaModelException ex)
	        	{
	        	    JavaPlugin.logErrorMessage("Problems while searching DOM AST representation of a method"); //$NON-NLS-1$
	        	    continue;
	    		}
	        	
	        	BaseCallFinder baseCallFinder = new BaseCallFinder();
	        	methodDeclaration.accept(baseCallFinder);
	        	BaseCallMessageSend[] baseCalls = baseCallFinder.getResult();
	        	
	        	for (int idx = 0;idx < baseCalls.length; idx++)
	        	{
	        	    ICompilationUnit cu = currentMethod.getCompilationUnit();
	        	    TextChange textChange = changeManager.get(cu);
	        	    
	        	    String editName = OTRefactoringCoreMessages.getString(
	        	            "OTRenameVirtualMethodProcessor.update_base_call_occurrence"); //$NON-NLS-1$
	        	    TextChangeCompatibility.addTextEdit(textChange, editName,
	        	            new ReplaceEdit(baseCalls[idx].getName().getStartPosition(),
	        	                    baseCalls[idx].getName().getLength(),
	        	                    getNewElementName()));
	        	}
	        }
	    }
	  
		IMethod getOriginalMethod()  	  -> IMethod getOriginalMethod();
		String  getNewElementName()       -> String  getNewElementName();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<IMethod> getMethodsToRename() -> Set     getMethodsToRename();
		IMethod getMethod() 		      -> IMethod getMethod();
	}
}
