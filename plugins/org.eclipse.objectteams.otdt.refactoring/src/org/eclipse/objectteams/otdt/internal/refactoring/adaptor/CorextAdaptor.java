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
 * $Id: CorextAdaptor.java 23473 2010-02-05 19:46:08Z stephan $
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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BaseCallMessageSend;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod.ExtractMethodAmbuguityMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.adaptor.extractmethod.ExtractMethodOverloadingMsgCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.OTRefactoringCoreMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

import base org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import base org.eclipse.jdt.internal.corext.refactoring.TypedSource;
import base org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import base org.eclipse.jdt.internal.corext.refactoring.surround.ExceptionAnalyzer;

/**
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
public team class CorextAdaptor 
{
	protected class TypedSource playedBy TypedSource
	{
		static callin boolean canCreateForType(int type){
			return     base.canCreateForType(type) 	
					// consider OT-specific elements
					|| type == IOTJavaElement.TEAM
					|| type == IOTJavaElement.ROLE
					|| type == IOTJavaElement.CALLIN_MAPPING
					|| type == IOTJavaElement.CALLOUT_MAPPING
					|| type == IOTJavaElement.CALLOUT_TO_FIELD_MAPPING;
		}
		@SuppressWarnings("decapsulation")
		canCreateForType <- replace canCreateForType;
	}
	
	protected class ExtractMethodRefactoring playedBy ExtractMethodRefactoring 
	{	
		// previous version patched inline (better integration with progress monitor?
//		public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException {
//			pm.beginTask(RefactoringCoreMessages.ExtractMethodRefactoring_checking_new_name, 2); 
//			pm.subTask(EMPTY);
//			
//			RefactoringStatus result= checkMethodName();
//			result.merge(checkParameterNames());
//			result.merge(checkVarargOrder());
//			pm.worked(1);
//			if (pm.isCanceled())
//				throw new OperationCanceledException();
//
//			BodyDeclaration node= fAnalyzer.getEnclosingBodyDeclaration();
//			if (node != null) {
//				fAnalyzer.checkInput(result, fMethodName, fAST);
//				pm.worked(1);
//			}
////			{ObjectTeams: also check for overloading and ambiguity in OT-subclassed elements
//			pm.subTask("OTExtractMethodRefactoring.checking_overloading"); 
//			result.merge(checkOverloadingAndAmbiguity(pm));
//	//sko}		
//			pm.done();
//			return result;
//		}
		
		
		// let's just append this behavior:
		void checkFinalConditions(IProgressMonitor pm, RefactoringStatus result) 
				throws CoreException 
		{
			pm.beginTask(OTRefactoringCoreMessages.getString("OTExtractMethodRefactoring.checking_overloading"), 1);  //$NON-NLS-1$
			pm.subTask(""); //$NON-NLS-1$
			result.merge(checkOverloadingAndAmbiguity(pm));
			pm.worked(1);
			pm.done();
		}
		void checkFinalConditions(IProgressMonitor pm, RefactoringStatus status) 
			<- after RefactoringStatus checkFinalConditions(IProgressMonitor pm) 
		with {
			pm <- pm, status <- result
		}
	
		//	also check for overloading and ambiguity in OT-subclassed elements
		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm)
				throws JavaModelException 
		{
			return RefactoringUtil.checkOverloadingAndAmbiguity(getCompilationUnit(),
					getDestination(), getMethodName(), getParamTypes(),
					new ExtractMethodAmbuguityMsgCreator(),
					new ExtractMethodOverloadingMsgCreator(), pm);
		}
		
		private String[] getParamTypes() {
			List<ParameterInfo> infos = getParameterInfos();
			if (infos.size() == 0)
				return new String[0];
			String[] result = new String[infos.size()];
			Iterator<ParameterInfo> iterator = infos.iterator();
			for (int i=0; iterator.hasNext();i++) 
				result[i] = iterator.next().getNewTypeName();
			
			return result;
		}

		ICompilationUnit getCompilationUnit() -> ICompilationUnit getCompilationUnit();
		@SuppressWarnings("decapsulation")
		ASTNode getDestination() -> get ASTNode fDestination;
		String getMethodName() -> String getMethodName();
		
		@SuppressWarnings("unchecked")
		List<ParameterInfo> getParameterInfos() -> List getParameterInfos();
	}
	
	protected class RefactoringScopeFactory playedBy RefactoringScopeFactory {
		static void create(IJavaElement javaElement, boolean considerVisibility, IJavaSearchScope scope) 
			throws JavaModelException 
		{
			if (considerVisibility & javaElement instanceof IMember) {
				IMember member= (IMember) javaElement;
				if (JdtFlags.isPrivate(member)) {
					int includeMask = IJavaSearchScope.REFERENCED_PROJECTS | IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES;					
					((JavaSearchScope)scope).add((JavaProject)javaElement.getJavaProject(), includeMask, new HashSet<IProject>());
				}
			}
		}
		void create(IJavaElement javaElement, boolean considerVisibility, IJavaSearchScope scope)
			<- after IJavaSearchScope create(IJavaElement javaElement, boolean considerVisibility, boolean sourceReferencesOnly)
			with {
				javaElement        <- javaElement,
				considerVisibility <- considerVisibility,
				scope              <- result
			}

	}
	
	/** Need to analyze one more node type: BaseCallMessageSend. */
	protected class ExceptionAnalyzer playedBy ExceptionAnalyzer
	{
		@SuppressWarnings("decapsulation")
		boolean handleExceptions(IMethodBinding binding) -> boolean handleExceptions(IMethodBinding binding);
		@SuppressWarnings("decapsulation")
		boolean isSelected(ASTNode node) -> boolean isSelected(ASTNode node);
				
		boolean visit(BaseCallMessageSend node) <- replace boolean visit(BaseCallMessageSend node);
	    @SuppressWarnings({ "basecall", "unchecked" }) // unchecked: List getBaseMappingElements()
		callin boolean visit(BaseCallMessageSend node) 
	    {
			if (!isSelected(node))
				return false;
			
			// find enclosing method
			ASTNode parent= node.getParent();
			while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION)
				parent= parent.getParent();
			if (parent == null)
				return false;
			IMethodBinding method= ((MethodDeclaration)parent).resolveBinding();
			if (method == null)
				return false;
			
			// find enclosing role type
			while (parent != null && parent.getNodeType() != ASTNode.ROLE_TYPE_DECLARATION)
				parent= parent.getParent();
			if (parent == null)
				return false;
			RoleTypeDeclaration role= (RoleTypeDeclaration)parent;
			
			// find all bound base methods and collect the exceptions they declare
			boolean result= false;
			for (CallinMappingDeclaration callinDecl: role.getCallIns()) {
				MethodSpec roleMethod = (MethodSpec)callinDecl.getRoleMappingElement();
				if (roleMethod.resolveBinding() == method) {
					List<ASTNode> baseMethods= callinDecl.getBaseMappingElements(); // raw conversion
					for (ASTNode elem : baseMethods) {
						if (elem.getNodeType() == ASTNode.METHOD_SPEC) {
							MethodSpec baseMethod= (MethodSpec)elem;
							if (handleExceptions(baseMethod.resolveBinding()))
								result= true;
						}
					}
				}
			}
			return result;
	    }
	}
}
