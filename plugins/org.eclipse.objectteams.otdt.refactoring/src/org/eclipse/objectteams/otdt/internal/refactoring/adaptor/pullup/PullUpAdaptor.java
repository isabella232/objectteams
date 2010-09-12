package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.pullup;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.internal.core.RoleType;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

import base org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;

/**
 * @author Johannes Gebauer
 * 
 */
@SuppressWarnings({ "restriction", "decapsulation" }) // private base classes
public team class PullUpAdaptor {

	public class PullUpRefactoringProcessorRole playedBy PullUpRefactoringProcessor {
		
		@SuppressWarnings("rawtypes")
		void setFCachedSkippedSuperTypes(Set fCachedSkippedSuperTypes) -> set Set fCachedSkippedSuperTypes;
		@SuppressWarnings("rawtypes")
		Set getFCachedSkippedSuperTypes() -> get Set fCachedSkippedSuperTypes;
		IMember[] getMembersToDelete(IProgressMonitor monitor) -> IMember[] getMembersToDelete(IProgressMonitor monitor);
		IMethod[] getFDeletedMethods() -> get IMethod[] fDeletedMethods;
		IMember[] getFMembersToMove() -> get IMember[] fMembersToMove;

		// callouts
		IType getDestinationType() -> IType getDestinationType();
		IMember[] getMembersToMove() -> IMember[] getMembersToMove();
		IType getDeclaringType() -> IType getDeclaringType();
		ITypeHierarchy getDestinationTypeHierarchy(IProgressMonitor pm) -> ITypeHierarchy getDestinationTypeHierarchy(IProgressMonitor pm);
		
		private void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) throws CoreException {
			
			status.merge(checkForAspectBindings(pm));
			status.merge(checkOverloadingAndAmbiguity(pm));
			status.merge(checkOverriding(pm));
			if(TypeHelper.isRole(getDeclaringType().getFlags())){
				status.merge(checkDestinationForOTElements());
				status.merge(checkShadowingFieldInImplicitHierarchy(pm));
			}
		}
		
		void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) <- after RefactoringStatus checkFinalConditions(IProgressMonitor pm,
				CheckConditionsContext context) with {
			pm <- pm,
			context <- context,
			status <- result
		}

		private RefactoringStatus checkShadowingFieldInImplicitHierarchy(IProgressMonitor pm) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			if(!TypeHelper.isRole(getDestinationType().getFlags())){
				return status;
			}
			
			ITypeHierarchy hier = getDestinationTypeHierarchy(pm);
			ArrayList<IType> implicitSubRoles = new ArrayList<IType>();
			implicitSubRoles.addAll(Arrays.asList(OTTypeHierarchies.getInstance().getAllTSubTypes(hier, getDestinationType())));
			
			// remove the subtypes of the declaring type
			implicitSubRoles.removeAll(Arrays.asList(OTTypeHierarchies.getInstance().getAllTSubTypes(hier, getDeclaringType())));
			
			pm.beginTask("Checking Shadowing", implicitSubRoles.size());
			pm.subTask(""); //$NON-NLS-1$
			
			for (int i = 0; i < getFMembersToMove().length; i++) {
				IMember element = getFMembersToMove()[i];
				if(element instanceof IField){
					IField field = (IField) element;
					for (IType type : implicitSubRoles) {
						IField shadowingField = RefactoringUtil.fieldIsShadowedInType(field.getElementName(), field.getTypeSignature(), type);
						if(shadowingField != null){
							
							ArrayList<IMember> membersToDelete = new ArrayList<IMember>();
							membersToDelete.addAll(Arrays.asList(getMembersToDelete(pm)));
							// do not indicate shadowing by deleted fields as an error
							if(!membersToDelete.contains(shadowingField)) {
								String msg = Messages.format("The pulled up field ''{0}'' would be shadowed in ''{1}''.", new String[] { field.getElementName(),
										type.getFullyQualifiedName('.') });
								status.addFatalError(msg, JavaStatusContext.create(shadowingField));
							}
							
						}
						pm.worked(1);
						// do not repeat errors in hierarchy
						if(status.hasFatalError()){
							pm.done();
							return status;
						}
					}
				}
			}
			pm.done();
			return status;
			
		}
		
		private RefactoringStatus checkDestinationForOTElements() throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			for (int i = 0; i < getFMembersToMove().length; i++) {
				IMember element = getFMembersToMove()[i];
				if (element instanceof IMethod){
					IMethod method = (IMethod)element;
					// callin methods can only be moved to roles
					if(Flags.isCallin(method.getFlags()) && !TypeHelper.isRole(getDestinationType().getFlags())){
						String msg = Messages.format("The callin method ''{0}'' can only be moved to a role (OTJLD �4.2.(d)).", new String[] { method.getElementName() });
						status.addFatalError(msg, JavaStatusContext.create(method));
					}
				}
			}
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguityInType(IProgressMonitor pm, IType type) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			ITypeHierarchy hier = getDestinationTypeHierarchy(pm);
			for (int i = 0; i < getFMembersToMove().length; i++) {
				IMember element = getFMembersToMove()[i];
				if (element instanceof IMethod){
					final IMethod method = (IMethod)element;
					String[] paramTypes = method.getParameterTypes();
					status.merge(RefactoringUtil.checkOverloadingAndAmbiguity(type, hier, method.getElementName(), paramTypes,
							new IAmbuguityMessageCreator() {

						public String createAmbiguousMethodSpecifierMsg() {
							return "Refactoring cannot be performed! There would be an ambiguous method specifier in a method binding after moving!";
						}

					}, new IOverloadingMessageCreator() {

						public String createOverloadingMessage() {
							String msg = Messages.format("The pulled up method ''{0}'' would be overloaded after refactoring.", new String[] { method.getElementName()});
							return msg;
						}

					}, pm));
				}
			}
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm) throws JavaModelException {
			
			ITypeHierarchy destinationTypeHierarchy = getDestinationTypeHierarchy(pm);
			IType[] subtypes = destinationTypeHierarchy.getAllSubtypes(getDestinationType());
			
			pm.beginTask("Checking Overloading", subtypes.length + 1);
			pm.subTask(""); //$NON-NLS-1$
			
			RefactoringStatus status = new RefactoringStatus();
			
			// check overloading in destination type
			status.merge(checkOverloadingAndAmbiguityInType(pm, getDestinationType()));
			
			pm.worked(1);
			
			// do not repeat errors in hierarchy
			if(status.hasFatalError()){
				pm.done();
				return status;
			}
			
			// check overloading in subtypes of the destination type
			for (int i = 0; i < subtypes.length; i++) {
				status.merge(checkOverloadingAndAmbiguityInType(pm, subtypes[i]));
				
				pm.worked(1);
				
				// do not repeat errors in hierarchy
				if(status.hasFatalError()){
					pm.done();
					return status;
				}
			}
			pm.done();
			return status;
		}
		
		private RefactoringStatus checkOverriding(IProgressMonitor pm) throws JavaModelException{
			RefactoringStatus status = new RefactoringStatus();
			
			ITypeHierarchy hier = getDestinationTypeHierarchy(pm);
			ArrayList<IType> allSubTypes = new ArrayList<IType>();
			allSubTypes.addAll(Arrays.asList(hier.getAllSubtypes(getDestinationType())));
			
			// remove the subtypes of the declaring type
			allSubTypes.removeAll(Arrays.asList(hier.getAllSubtypes(getDeclaringType())));
			
			pm.beginTask("Checking Overriding", allSubTypes.size());
			pm.subTask(""); //$NON-NLS-1$
			
			for (int i = 0; i < getFMembersToMove().length; i++) {
				IMember element = getFMembersToMove()[i];
				if(element instanceof IMethod){
					IMethod method = (IMethod) element;
					for (IType type : allSubTypes) {
						IMethod overridingMethod = methodIsOverriddenInType(method.getElementName(), method.getParameterTypes(), type);
						if(overridingMethod != null){
							
							ArrayList<IMember> membersToMove = new ArrayList<IMember>();
							membersToMove.addAll(Arrays.asList(getMembersToMove()));
							ArrayList<IMember> membersToDelete = new ArrayList<IMember>();
							membersToDelete.addAll(Arrays.asList(getMembersToDelete(pm)));
							// do not indicate overriding for deleted/moved methods as an error
							if(!membersToMove.contains(overridingMethod) && !membersToDelete.contains(overridingMethod)) {
								String msg = Messages.format("The pulled up method ''{0}'' would be overridden in ''{1}''.", new String[] { method.getElementName(),
										type.getFullyQualifiedName('.') });
								status.addError(msg, JavaStatusContext.create(overridingMethod));
							}
							
						}
						pm.worked(1);
						// do not repeat errors in hierarchy
						if(status.hasError()){
							pm.done();
							return status;
						}
					}
				}
			}
			pm.done();
			return status;
		}
		
		private IMethod methodIsOverriddenInType(String methodName, String[] paramTypes, IType type){
			IMethod method = type.getMethod(methodName, paramTypes);
			if(method.exists()){
				return method;
			}
			return null;
		}
		
		
		/**
		 * Searches for aspect bindings that reference members to be moved.
		 * @return The refactoring status contains warnings if any referencing aspect bindings exist.
		 */
		private RefactoringStatus checkForAspectBindings(IProgressMonitor monitor) throws CoreException {
			// search all references for the members to be moved
			IMember[] membersToMove = getMembersToMove();
			final HashMap<IMember,Set<SearchMatch>> references= new HashMap<IMember,Set<SearchMatch>>();
			IJavaSearchScope scope= SearchEngine.createWorkspaceScope();
			for (int i = 0; i < membersToMove.length; i++) {
				final IMember member = membersToMove[i];
				// may not be able to access return type, but IGNORE_RETURN_TYPE is only effective with ALL_OCCURRENCES:
				int limitTo = IJavaSearchConstants.ALL_OCCURRENCES|IJavaSearchConstants.IGNORE_RETURN_TYPE;
				SearchPattern pattern= SearchPattern.createPattern(member, limitTo, SearchPattern.R_EXACT_MATCH);
				SearchEngine engine= new SearchEngine();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, scope, new SearchRequestor() {
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						if (   match.getAccuracy() == SearchMatch.A_ACCURATE
							&& !match.isInsideDocComment()
							&& !(match instanceof MethodDeclarationMatch)) // limit to references despite ALL_OCCURRENCES above:
						{
							if(references.get(member) == null){
								Set<SearchMatch> refSet = new HashSet<SearchMatch>();
								refSet.add(match);
								references.put(member, refSet);
							}else{
								references.get(member).add(match);
							}
						}
					}
				}, monitor);
			}
			
			// search the matches for aspect bindings
			RefactoringStatus status = new RefactoringStatus();
			for (int i = 0; i < membersToMove.length; i++) {
				IMember member = membersToMove[i];
				Set<SearchMatch> refSet = references.get(member);
				if(refSet == null){
					continue;
				}
				for (SearchMatch match : refSet) {
					Object element= match.getElement();
					if (element instanceof ICalloutMapping) {
						ICalloutMapping mapping = (ICalloutMapping) element;
						if(mapping.getBoundBaseMethod().equals(member)){
							addAspectBindingWarning(member, status, mapping);
						}
					}
					if (element instanceof ICalloutToFieldMapping) {
						ICalloutToFieldMapping mapping = (ICalloutToFieldMapping) element;
						if(mapping.getBoundBaseField().equals(member)){
							addAspectBindingWarning(member, status, mapping);
						}
					}
					if (element instanceof ICallinMapping) {
						ICallinMapping mapping = (ICallinMapping) element;
						for (int j = 0; j < mapping.getBoundBaseMethods().length; j++) {
							if(mapping.getBoundBaseMethods()[i].equals(member)){
								addAspectBindingWarning(member, status, mapping);
								break;
							}
						}
					}	
				}
			}
			return status;
		}
		
		/**
		 * Adds a warning to the given refactoring status that notifies the user about existing aspect bingings.
		 * 
		 * @param member the member to be pulled up.
		 * @param status the refactoring status, where the warning should be added.
		 * @param mapping the method mapping that causes the warning.
		 */
		private void addAspectBindingWarning(IMember member, RefactoringStatus status,IMethodMapping mapping){
			status.addEntry(new RefactoringStatusEntry(RefactoringStatus.WARNING,
					MessageFormat.format("Pulled up member ''{0}'' is referenced in an aspect binding by ''{1}''",
							new Object[]{member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName()})));
		
		}
		
		/**
		 * Adds implicit role super types for roles. Candidate types are the possible super types,
		 * that are available as the destination type.
		 */
		callin IType[] getCandidateTypes(final RefactoringStatus status, final IProgressMonitor monitor) throws JavaModelException{
			RefactoringStatus jdtStatus = new RefactoringStatus();
			IType[] jdtCandidates = base.getCandidateTypes(jdtStatus, monitor);
			final IType declaring= getDeclaringType();
			// try to get the corresponding OTType that is represented in the declaring type
			IOTType otType = OTModelManager.getOTElement(declaring);
			if(otType != null && otType instanceof RoleType){
				RoleType role = (RoleType) otType;
				IType[] otSuperTypes = role.newTypeHierarchy(monitor).getAllSupertypes(declaring);
				List<IType> list = new ArrayList<IType>();
				int binary = 0;
				for (int i = 0; i < otSuperTypes.length; i++) {
					IType type = otSuperTypes[i];
					if(OTModelManager.hasOTElementFor(otSuperTypes[i])){
						IOTType otSuperType = OTModelManager.getOTElement(type);
						if(otSuperType instanceof RoleType && type != null && type.exists() && !type.isReadOnly() && !type.isBinary()){
							list.add(type);
						}else if(type.isBinary()){
							binary += 1;
						}
					}
				}
				if(!list.isEmpty()){
					if(jdtStatus.hasFatalError()){
						if (otSuperTypes.length == binary)
							status.addFatalError(RefactoringCoreMessages.PullUPRefactoring_no_all_binary);
					}else{
						status.merge(jdtStatus);
					}
					Collections.reverse(list);
					list.addAll(0,Arrays.asList(jdtCandidates));
					return list.toArray(new IType[list.size()]);
				}
			}
			status.merge(jdtStatus);
			return jdtCandidates;
		}

		getCandidateTypes <- replace getCandidateTypes;
		
		
		
		/**
		 * Prevents callin methods from visibility adjustments.
		 */
		callin boolean needsVisibilityAdjustment(final IMember member, final boolean references, final IProgressMonitor monitor, final RefactoringStatus status) throws JavaModelException {
			boolean result = base.needsVisibilityAdjustment(member, references, monitor, status);
			if(Flags.isCallin(member.getFlags())){
				return false;
			}else{
				return result;
			}
		}
		
		needsVisibilityAdjustment <- replace needsVisibilityAdjustment;
			
	}
	
}


