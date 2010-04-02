/**
 * 
 */
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor.pushdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.MemberActionInfo;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IAmbuguityMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.IOverloadingMessageCreator;
import org.eclipse.objectteams.otdt.internal.refactoring.util.RefactoringUtil;

import base org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor;

/**
 * @author Johannes Gebauer
 * 
 */
@SuppressWarnings("restriction")
public team class PushDownAdaptor {
	
	@SuppressWarnings("decapsulation") // base class is final
	protected class PushDownRefactoringProcessor playedBy PushDownRefactoringProcessor {
		
		MemberActionInfo[] getAbstractDeclarationInfos() -> MemberActionInfo[] getAbstractDeclarationInfos();
		// callouts
		IType[] getAbstractDestinations(IProgressMonitor arg0) -> IType[] getAbstractDestinations(IProgressMonitor arg0);
		IMember[] getMembersToMove() -> IMember[] getMembersToMove();
		IType getDeclaringType() -> IType getDeclaringType();
		ITypeHierarchy getHierarchyOfDeclaringClass(IProgressMonitor pm) -> ITypeHierarchy getHierarchyOfDeclaringClass(IProgressMonitor pm);

		private ITypeHierarchy fDeclaringTypeHierachy;
		
		private void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) throws CoreException {
			
			status.merge(checkForDirectPhantomSubRoles(pm));
			status.merge(checkForAspectBindings(pm));
			status.merge(checkShadowingFieldInImplicitHierarchy(pm));
			IType[] subclasses = getAbstractDestinations(pm);
			for (int i = 0; i < subclasses.length; i++) {
				status.merge(checkOverriding(subclasses[i],pm));
			}
			status.merge(checkOverloadingAndAmbiguity(pm));
		}
		

		void checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context, RefactoringStatus status) <- after RefactoringStatus checkFinalConditions(IProgressMonitor pm,
				CheckConditionsContext context) with {
			pm <- pm,
			context <- context,
			status <- result
		}
		
		private RefactoringStatus checkShadowingFieldInImplicitHierarchy(IProgressMonitor pm) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			ArrayList<IType> subTypes = new ArrayList<IType>();
			subTypes.addAll(Arrays.asList(hier.getSubtypes(getDeclaringType())));
			
			pm.beginTask("Checking Shadowing", subTypes.size());
			pm.subTask(""); //$NON-NLS-1$
			
			for (int i = 0; i < getMembersToMove().length; i++) {
				IMember element = getMembersToMove()[i];
				if(element instanceof IField){
					IField field = (IField) element;
					for (IType type : subTypes) {
						
						// shadowing fields is just forbidden in implicit hierarchies
						if(TypeHelper.isRole(type.getFlags())){
							OTTypeHierarchy implicitHierarchy = new OTTypeHierarchy(type, type.getJavaProject(), false);
							implicitHierarchy.refresh(pm);
							IType[] implicitSuperTypes = implicitHierarchy.getAllTSuperTypes(type);
							
							for (int j = 0; j < implicitSuperTypes.length; j++) {
								IType implicitSuperType = implicitSuperTypes[i];
								IField shadowingField = RefactoringUtil.fieldIsShadowedInType(field.getElementName(), field.getTypeSignature(), implicitSuperType);
								if(shadowingField != null){
									
									String msg = Messages.format("The pushed down field ''{0}'' would be shadowed in ''{1}''.", new String[] { field.getElementName(),
											implicitSuperType.getFullyQualifiedName('.') });
									status.addError(msg, JavaStatusContext.create(shadowingField));
									
								}
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
		
		/**
		 * Searches for aspect bindings that reference members to be moved.
		 * @return The refactoring status contains errors if the pushed down members are not visible in existing bidnings after refactoring.
		 */
		@SuppressWarnings("restriction")
		private RefactoringStatus checkForAspectBindings(IProgressMonitor monitor) throws CoreException {
			// search all references for the members to be moved
			IMember[] membersToMove = getMembersToMove();
			final HashMap<IMember,Set<SearchMatch>> references= new HashMap<IMember,Set<SearchMatch>>();
			IJavaSearchScope scope= SearchEngine.createWorkspaceScope();
			for (int i = 0; i < membersToMove.length; i++) {
				final IMember member = membersToMove[i];
				SearchPattern pattern= SearchPattern.createPattern(member, IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
				SearchEngine engine= new SearchEngine();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, scope, new SearchRequestor() {
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						if (match.getAccuracy() == SearchMatch.A_ACCURATE && !match.isInsideDocComment()){
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
				
				// do not search for aspect bindings if an abstract declaration remains
				if(leavesAbstractMethod(member)){
					continue;
				}
				
				Set<SearchMatch> refSet = references.get(member);
				if(refSet == null){
					continue;
				}
				for (SearchMatch match : refSet) {
					Object element= match.getElement();
					if (element instanceof ICalloutMapping) {
						ICalloutMapping mapping = (ICalloutMapping) element;
						if(mapping.getBoundBaseMethod().equals(member)){
							String msg = Messages.format("The pushed down method ''{0}'' is referenced in a callout method binding in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else if(mapping.getRoleMethod() != null && mapping.getRoleMethod().equals(member)){
							String msg = Messages.format("The pushed down method ''{0}'' is bound in a callout method binding in ''{1}''.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}else{
							// TODO find a better way to analyze references in parameter mappings
							String msg = Messages.format("The pushed down member ''{0}'' is referenced in a callout parameter mapping in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					if (element instanceof ICalloutToFieldMapping) {
						ICalloutToFieldMapping mapping = (ICalloutToFieldMapping) element;
						if(mapping.getBoundBaseField().equals(member)){
							String msg = Messages.format("The pushed down field ''{0}'' is referenced in a callout to field binding in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else if(mapping.getRoleMethod() != null && mapping.getRoleMethod().equals(member)){
							String msg = Messages.format("The pushed down method ''{0}'' is bound in a callout to field binding in ''{1}''.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}else{
							// TODO find a better way to analyze references in parameter mappings
							String msg = Messages.format("The pushed down member ''{0}'' is referenced in a callout to field value mapping in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					if (element instanceof ICallinMapping) {
						ICallinMapping mapping = (ICallinMapping) element;
						boolean baseMethodFound = false;
						for (int j = 0; j < mapping.getBoundBaseMethods().length; j++) {
							if(mapping.getBoundBaseMethods()[i].equals(member)){
								String msg = Messages.format("The pushed down method ''{0}'' is referenced in a callin method binding in ''{1}'' and will not be visible after refactoring.",
										new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
								status.addError(msg, JavaStatusContext.create(mapping));
								baseMethodFound = true;
								break;
							}
						}
						
						if(baseMethodFound){
							continue;
						}
						
						if(mapping.getRoleMethod().equals(member)){
							String msg = Messages.format("The pushed down method ''{0}'' is bound in a callin method binding in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						} else {
							// TODO find a better way to analyze references in parameter mappings
							String msg = Messages.format("The pushed down member ''{0}'' is referenced in a callin parameter mapping in ''{1}'' and will not be visible after refactoring.",
									new String[] { member.getElementName(), mapping.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(mapping));
						}
					}
					
					if (element instanceof ResolvedSourceMethod) {
						ResolvedSourceMethod method = (ResolvedSourceMethod) element;
						// References in the declaring type are checked by the base
						if(!method.getDeclaringType().equals(getDeclaringType())){
							String msg = Messages.format("Pushed down member ''{0}'' is referenced by ''{1}''.",
									new String[] { member.getElementName(), method.getDeclaringType().getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(method));
						}
					}
				}
			}
			return status;
		}

		@SuppressWarnings("restriction")
		private boolean leavesAbstractMethod(IMember member) throws JavaModelException {
			MemberActionInfo[] methodsToBeDeclaredAbstract = getAbstractDeclarationInfos();
			for (int j = 0; j < methodsToBeDeclaredAbstract.length; j++) {
				if(methodsToBeDeclaredAbstract[j].getMember() == member){
					return true;
				}
			}
			return false;
		}
		
		private RefactoringStatus checkForDirectPhantomSubRoles(IProgressMonitor pm) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			
			if(hier instanceof OTTypeHierarchy){
				OTTypeHierarchy otHier = (OTTypeHierarchy)hier;
				otHier.setPhantomMode(true);
				IType[] subTypes = otHier.getSubtypes(getDeclaringType());
				for (int i = 0; i < subTypes.length; i++) {
					IType subType = subTypes[i];
					if(subType instanceof PhantomType){
						String msg = Messages.format("An implicit sub role of ''{0}'' is a phantom role, therefore the pushed down members cannot be moved to ''{1}''.", new String[] { getDeclaringType().getFullyQualifiedName('.'), subType.getFullyQualifiedName('.') });
						status.addError(msg, JavaStatusContext.create(subType));
					}
				}
				otHier.setPhantomMode(false);
			}
			return status;
		}
		
		/**
		 * Checks if the pushed down method overrides an implicitly inherited method.
		 * 
		 * @param type the type to check overriding in
		 * @param pm the progress monitor
		 * @return the <code>RefactoringStatus</code> indicating overriding
		 * @throws JavaModelException 
		 */
		private RefactoringStatus checkOverriding(IType type ,IProgressMonitor pm) throws JavaModelException{
			RefactoringStatus status = new RefactoringStatus();
			
			// only roles inherit implicitly
			if(TypeHelper.isRole(type.getFlags())){
				
				IMember[] membersToPushDown = getMembersToMove();
				
				// create the ot hierarchy to check implicit super types
				OTTypeHierarchy hierarchy = new OTTypeHierarchy(type, type.getJavaProject(), false);
				hierarchy.refresh(pm);
				IType[] superRoles = hierarchy.getTSuperTypes(type);
				
				pm.beginTask("Checking Overriding", superRoles.length);
				pm.subTask(""); //$NON-NLS-1$
				
				for (int i = 0; i < superRoles.length; i++) {
					IType superRole = superRoles[i];
					// do not search in the declaring type to avoid finding the pushed down method itself
					if(!superRole.equals(getDeclaringType())){
						for (int j = 0; j < membersToPushDown.length; j++) {
							// check only the pushed down methods
							if(membersToPushDown[j] instanceof IMethod){
								IMethod pushedDownMethod = (IMethod) membersToPushDown[j];
								IMethod overriddenMethod = superRole.getMethod(pushedDownMethod.getElementName(), pushedDownMethod.getParameterTypes());
								if(overriddenMethod.exists()){
									String msg = Messages.format("The pushed down method ''{0}'' would override the implicitly inherited method ''{1}''.", new String[] { pushedDownMethod.getElementName(), overriddenMethod.getDeclaringType().getFullyQualifiedName('.') + "." + overriddenMethod.getElementName()});
									status.addError(msg, JavaStatusContext.create(overriddenMethod));
								}
							}
						}
					}
					pm.worked(1);
				}
			}
			pm.done();
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguityInType(IProgressMonitor pm, IType type) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			ITypeHierarchy hier = getHierarchyOfDeclaringClass(pm);
			for (int i = 0; i < getMembersToMove().length; i++) {
				IMember element = getMembersToMove()[i];
				
				// overloading can only be caused by private methods
				if (Flags.isPrivate(element.getFlags()) && element instanceof IMethod){
					final IMethod method = (IMethod)element;
					String[] paramTypes = method.getParameterTypes();
					status.merge(RefactoringUtil.checkOverloadingAndAmbiguity(type, hier, method.getElementName(), paramTypes,
							new IAmbuguityMessageCreator() {

						public String createAmbiguousMethodSpecifierMsg() {
							return "Refactoring cannot be performed! There would be an ambiguous method specifier in a method binding after moving!";
						}

					}, new IOverloadingMessageCreator() {

						public String createOverloadingMessage() {
							String msg = Messages.format("The pushed down method ''{0}'' would be overloaded after refactoring.", new String[] { method.getElementName()});
							return msg;
						}

					}, pm));
				}
			}
			return status;
		}
		
		private RefactoringStatus checkOverloadingAndAmbiguity(IProgressMonitor pm) throws JavaModelException {
			
			IType[] subtypes = getAbstractDestinations(pm);
			
			pm.beginTask("Checking Overloading", subtypes.length);
			pm.subTask(""); //$NON-NLS-1$
			
			RefactoringStatus status = new RefactoringStatus();
			
			// check overloading in subtypes of the destination type
			for (int i = 0; i < subtypes.length; i++) {
				status.merge(checkOverloadingAndAmbiguityInType(pm, subtypes[i]));
				pm.worked(1);
				
			}
			pm.done();
			return status;
		}
		
		
		/**
		 * Replaces the jdt hierarchy with the ot hierarchy. 
		 * @throws JavaModelException 
		 */
		@SuppressWarnings("basecall")
		callin ITypeHierarchy getOTHierarchyOfDeclaringClass(IProgressMonitor monitor) throws JavaModelException  {
			try {
				if (fDeclaringTypeHierachy != null)
					return fDeclaringTypeHierachy;
				if(OTModelManager.hasOTElementFor(getDeclaringType())){
					fDeclaringTypeHierachy = OTModelManager.getOTElement(getDeclaringType()).newOTTypeHierarchy(monitor);
				}else{
					fDeclaringTypeHierachy = base.getOTHierarchyOfDeclaringClass(monitor);
				}
				return fDeclaringTypeHierachy;
			} finally {
				monitor.done();
			}
		}
		
		getOTHierarchyOfDeclaringClass <- replace getHierarchyOfDeclaringClass;
		
	}
 
}
