/**
 * 
 */
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenamePackageChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;

import base org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;



/**
 * @author Johannes Gebauer
 * 
 */
@SuppressWarnings({"restriction","rawtypes","decapsulation"})
public team class RenameTypeAdaptor {

	protected class RenameTypeProcessor playedBy RenameTypeProcessor {
		
		private static final String ICONFINED = "IConfined"; //$NON-NLS-1$
		private static final String CONFINED = "Confined"; //$NON-NLS-1$
		private static final String ILOWERABLE = "ILowerable"; //$NON-NLS-1$

		private IPackageFragment fRoleDirectory;
		private IType[] fImplicitRelatedTypes;
		private IType[] fImplicitRelatedPhantomTypes;
		private OTTypeHierarchy fOTTypeHierarchy;
		private boolean fTypeToRenameIsRole;
		private String fCachedNewElementName;
		private JavaModelException fCachedException;
		
		// ======= callouts =======
		LinkedHashMap getFPreloadedElementToName() -> get LinkedHashMap fPreloadedElementToName;
		void setFPreloadedElementToName(LinkedHashMap preloadedElementToName) -> set LinkedHashMap fPreloadedElementToName;
		IType getFType() -> get IType fType;
		void setFType(IType type) -> set IType fType;
		String getNewElementLabel() -> String getNewElementLabel();
		TextChangeManager getFChangeManager() -> get TextChangeManager fChangeManager;
		SearchResultGroup[]  getFReferences() -> get SearchResultGroup[]  fReferences;
		void setFReferences(SearchResultGroup[] fReferences) -> set SearchResultGroup[]  fReferences;
		String getNewElementName() -> String getNewElementName();	
		boolean getUpdateSimilarDeclarations() -> boolean getUpdateSimilarDeclarations();
		int getFRenamingStrategy() -> get int fRenamingStrategy;
		int getFCachedRenamingStrategy() -> get int fCachedRenamingStrategy;
		boolean getFCachedRenameSimilarElements() -> get boolean fCachedRenameSimilarElements;
		RefactoringStatus getFCachedRefactoringStatus() -> get RefactoringStatus fCachedRefactoringStatus;
		String getFCachedNewName() -> get String fCachedNewName;
		void setFPreloadedElementToNameDefault(LinkedHashMap fPreloadedElementToNameDefault) -> set LinkedHashMap fPreloadedElementToNameDefault;
		LinkedHashMap getFPreloadedElementToNameDefault() -> get LinkedHashMap fPreloadedElementToNameDefault;
		void setFPreloadedElementToSelection(Map fPreloadedElementToSelection) -> set Map fPreloadedElementToSelection;
		Map getFPreloadedElementToSelection() -> get Map fPreloadedElementToSelection;
		void setFFinalSimilarElementToName(Map fFinalSimilarElementToName) -> set Map fFinalSimilarElementToName;
		Map getFFinalSimilarElementToName() -> get Map fFinalSimilarElementToName;
		
		
		callin RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException{
			initializeOTInformations(pm);
			
			// Java conditions:
			RefactoringStatus jdtStatus = base.doCheckFinalConditions(pm, context);
			if(jdtStatus.hasFatalError()){
				return jdtStatus;
			}
			
			// throw a cached exception that could not be thrown earlier
			if(fCachedException != null){
				throw fCachedException;
			}
			
			// OT conditions:
			if(Flags.isTeam(getFType().getFlags())){
				if(fRoleDirectory != null){
					jdtStatus.merge(checkForConflictingRoleDirectoryName(fRoleDirectory));
				}
			}
			
			// Special Conditions for Roles
			if(fTypeToRenameIsRole){
				jdtStatus.merge(checkForRoleOverriding(pm));
			    jdtStatus.merge(checkRenameForImplicitRelatedTypes(getFType(), fOTTypeHierarchy, pm));
			}
			
			pm.done();
			return jdtStatus;
			
		}

		doCheckFinalConditions <- replace doCheckFinalConditions;

		private void initializeOTInformations(IProgressMonitor pm) throws JavaModelException {
			// Do not initialize again if the preconditions have not changed.
			// OT informations depend on the type and the new element name.
			if(fOTTypeHierarchy != null && fOTTypeHierarchy.getFocusType() == getFType() 
			   &&  getNewElementName().equals(fCachedNewElementName)){
				return;
			}

			fCachedNewElementName = getNewElementName();
			fOTTypeHierarchy = new OTTypeHierarchy(getFType(), getFType().getJavaProject(), true);
			fOTTypeHierarchy.refresh(pm);
			findImplicitRelatedTypes(getFType(), fOTTypeHierarchy, null);
			fTypeToRenameIsRole = TypeHelper.isRole(getFType().getFlags());
			if(Flags.isTeam(getFType().getFlags())){
				fRoleDirectory = getRoleDirectoryForTeam(getFType());
			}
			fCachedException = null;
		}
		
		/**
		 * Adds implicit related Types references to the jdt references, that contain only the focus type references. 
		 * 
		 */
		@SuppressWarnings({ "basecall", "unchecked", "rawtypes" })
		callin  RefactoringStatus initializeReferences(IProgressMonitor monitor) throws JavaModelException, OperationCanceledException{

			// Do not search again if the preconditions have not changed.
			// This check is a copy of RenameTypeProcessor.initializeReferences(),
			// to prevent multiple processing of the implicit related types.
			if (getFReferences() != null && (getNewElementName().equals(getFCachedNewName())) && (getFCachedRenameSimilarElements() == getUpdateSimilarDeclarations())
					&& (getFCachedRenamingStrategy() == getFRenamingStrategy()))
				return getFCachedRefactoringStatus();

			// The ot informations may have not been initialized if the wizard computes the references for a preview.
			initializeOTInformations(monitor);

			//jdt strategy
			RefactoringStatus jdtStatus = base.initializeReferences(monitor);

			// cache the original focus type and found jdt references
			IType originalFocusType = getFType();
			SearchResultGroup[] jdtReferences = getFReferences();

			// cache the resources for the renaming of similar named elements
			LinkedHashMap originalPreloadedElementToName = getFPreloadedElementToName();
			Map originalPreloadedElementToSelection = getFPreloadedElementToSelection();
			LinkedHashMap originalPreloadedElementToNameDefault = getFPreloadedElementToNameDefault();

			// initialize data structures for the ot references and similar named elements
			ArrayList<SearchResultGroup> otReferences = new ArrayList<SearchResultGroup>();
			LinkedHashMap otPreloadedElementToName = new LinkedHashMap();
			Map otPreloadedElementToSelection = new HashMap();
			LinkedHashMap otPreloadedElementToNameDefault = new LinkedHashMap();

			try{
				// find references for implicit related types
				for (int i = 0; i < fImplicitRelatedTypes.length; i++) {
					setFType(fImplicitRelatedTypes[i]);
					setFReferences(null);
					
					jdtStatus.merge(base.initializeReferences(monitor));
					
					// add all references of the implicit related type
					otReferences.addAll(Arrays.asList(getFReferences()));
					
					// add all similar name references of the implicit related type to the maps
					otPreloadedElementToName.putAll(getFPreloadedElementToName());
					otPreloadedElementToSelection.putAll(getFPreloadedElementToSelection());
					otPreloadedElementToNameDefault.putAll(getFPreloadedElementToNameDefault());
				}
				
				//TODO: UnsupportedOperationException in PhantomType(the PhantomTypeAdaptor provides a quick fix)
				within(new PhantomTypeAdaptor()){
					// find references for implicit related phantom types
					for (int i = 0; i < fImplicitRelatedPhantomTypes.length; i++) {
						setFType(fImplicitRelatedPhantomTypes[i]);
						setFReferences(null);
						
						jdtStatus.merge(base.initializeReferences(monitor));
						
						// add all references of the implicit related type
						otReferences.addAll(Arrays.asList(getFReferences()));
						
						// add all similar name references of the implicit related type to the maps
						otPreloadedElementToName.putAll(getFPreloadedElementToName());
						otPreloadedElementToSelection.putAll(getFPreloadedElementToSelection());
						otPreloadedElementToNameDefault.putAll(getFPreloadedElementToNameDefault());
					}
				}
			}finally{
				// ensure that the original focus type is reset after processing the implicit type references
				setFType(originalFocusType);
				
				// combine the ot references with the jdt references
				otReferences.addAll(Arrays.asList(jdtReferences));
				setFReferences(otReferences.toArray(new SearchResultGroup[otReferences.size()]));
				
				// combine and set the maps for the similar named elements 
				originalPreloadedElementToName.putAll(otPreloadedElementToName);
				setFPreloadedElementToName(originalPreloadedElementToName);
				originalPreloadedElementToSelection.putAll(otPreloadedElementToSelection);
				setFPreloadedElementToSelection(originalPreloadedElementToSelection);
				originalPreloadedElementToNameDefault.putAll(otPreloadedElementToNameDefault);
				setFPreloadedElementToNameDefault(originalPreloadedElementToNameDefault);
					
			}
			return jdtStatus;
		}
		initializeReferences <- replace initializeReferences;
		
		private RefactoringStatus checkRenameForImplicitRelatedTypes(IType type, OTTypeHierarchy otTypeHierarchy, IProgressMonitor pm) throws CoreException {
			RefactoringStatus status = new RefactoringStatus();
			for (int i = 0; i < fImplicitRelatedTypes.length; i++) {
				status.merge(checkShadowingInEnclosingTeams(fImplicitRelatedTypes[i]));
			}
			for (int i = 0; i < fImplicitRelatedPhantomTypes.length; i++) {
				status.merge(checkShadowingInEnclosingTeams(fImplicitRelatedPhantomTypes[i]));
			}
			return status;
		}

		private void findImplicitRelatedTypes(IType type, OTTypeHierarchy otTypeHierarchy, IProgressMonitor pm) throws JavaModelException {
			IType topmostType = findTopMostType(type, otTypeHierarchy);
			OTTypeHierarchy topmostTypeHierarchy = new OTTypeHierarchy(topmostType, topmostType.getJavaProject(), true);
			topmostTypeHierarchy.refresh(pm);
			
			ArrayList<IType> relatedTypes = new ArrayList<IType>();
			relatedTypes.addAll(Arrays.asList(topmostTypeHierarchy.getAllTSubtypes(topmostType)));

			// exchange the topmost type with the given type
			if(!topmostType.equals(type)){
				relatedTypes.remove(type);
				relatedTypes.add(topmostType);
			}
			
			fImplicitRelatedTypes = relatedTypes.toArray(new IType[relatedTypes.size()]);
			
			// search the implicit related phantom types
			topmostTypeHierarchy.setPhantomMode(true);
			ArrayList<IType> relatedPhantomTypes = new ArrayList<IType>();
			relatedPhantomTypes.addAll(Arrays.asList(topmostTypeHierarchy.getAllTSubtypes(topmostType)));
			relatedPhantomTypes.removeAll(relatedTypes);
			relatedPhantomTypes.remove(type);
			fImplicitRelatedPhantomTypes = relatedPhantomTypes.toArray(new IType[relatedPhantomTypes.size()]);

		}

		/**
		 * Finds the topmost declaration of a Role Type in the implicit type hierarchy.
		 * Returns the type itself, if it does not have any implicit super types.
		 * 
		 * @param type the role type to start the search
		 * @param otTypeHierarchy for the type
		 * @return the topmost Role Type for the given type
		 */
		private IType findTopMostType(IType type, OTTypeHierarchy otTypeHierarchy) {
			IType[] superTypes = otTypeHierarchy.getAllTSuperTypes(type);
			for (int i = 0; i < superTypes.length; i++) {
				if(otTypeHierarchy.getAllTSuperTypes(superTypes[i]).length == 0){
					return superTypes[i];
				}
			}
			return type;
		}

		private void addTypeDeclarationUpdate(TextChangeManager manager, IType type) throws CoreException {
			String name = RefactoringCoreMessages.RenameTypeRefactoring_update;
			int typeNameLength = type.getElementName().length();
			ICompilationUnit cu = type.getCompilationUnit();
			TextChangeCompatibility.addTextEdit(manager.get(cu), name, new ReplaceEdit(type.getNameRange().getOffset(), typeNameLength, getNewElementName()));
			

		}
		
		private void createChanges(IProgressMonitor pm) throws JavaModelException, CoreException{
			pm.beginTask("Create changes for implicit type declarations", fImplicitRelatedTypes.length);
			pm.subTask(""); //$NON-NLS-1$
			if(fTypeToRenameIsRole){
				for (int i = 0; i < fImplicitRelatedTypes.length; i++) {
					addTypeDeclarationUpdate(getFChangeManager(), fImplicitRelatedTypes[i]);
					pm.worked(1);
				}
			}
			pm.done();
		}
		createChanges <- after createChanges;
		
		callin Change createChange(IProgressMonitor monitor) throws CoreException{

			IType typeToRename = getFType();
		
			// save the compilation units of the implicit related type declarations
			if(fTypeToRenameIsRole){
				for (int i = 0; i < fImplicitRelatedTypes.length; i++) {
					IType type = fImplicitRelatedTypes[i];
					if (getFChangeManager().containsChangesIn(type.getCompilationUnit())) {
						TextChange textChange = getFChangeManager().get(type.getCompilationUnit());
						if (textChange instanceof TextFileChange) {
							((TextFileChange) textChange).setSaveMode(TextFileChange.FORCE_SAVE);
						}
					}
				}
			}
			
			// create the jdt change
			Change jdtChange = base.createChange(monitor);
			
			
			if(Flags.isTeam(typeToRename.getFlags())){
				// a role directory exists
				if(fRoleDirectory != null){
					// rename the directory to the new team name
					RenamePackageChange renameRoleFolderChange= new RenamePackageChange( fRoleDirectory, typeToRename.getPackageFragment().getElementName() + "." + getNewElementName(),  false);
					((DynamicValidationRefactoringChange)jdtChange).add(renameRoleFolderChange);
				}	
			}
			
			
			if(fTypeToRenameIsRole){
				
				// Update the compilation unit name if the role is declared in a role file
				for (int i = 0; i < fImplicitRelatedTypes.length; i++) {
					IType type = fImplicitRelatedTypes[i];
					
					if(isRoleFile(type)){
						String renamedCUName = JavaModelUtil.getRenamedCUName(type.getCompilationUnit(), getNewElementName());
						((DynamicValidationRefactoringChange)jdtChange).add(new RenameCompilationUnitChange(type.getCompilationUnit(), renamedCUName));
					}
				}
			}
			return jdtChange;
		}
		createChange <- replace createChange;
		
		
		callin RefactoringStatus checkNewElementName(String newName){
			RefactoringStatus jdtStatus = base.checkNewElementName(newName);
			if(fTypeToRenameIsRole){
				jdtStatus.merge(checkNewRoleName(newName));
			}
			return jdtStatus;
		}
		
		checkNewElementName <- replace checkNewElementName;
		
		/**
		 * If a regular type is renamed it must be checked if any existing roles would shadow the renamed type.
		 * Role types are separately handled in <code>checkShadowingInEnclosingTeams()</code>.
		 */
		callin RefactoringStatus checkRoleTypesInPackage() throws CoreException{
			RefactoringStatus jdtStatus = base.checkRoleTypesInPackage();
			
			if(jdtStatus == null){
				jdtStatus = new RefactoringStatus();
			}

			IType[] types = getAllTypesInPackage(getFType().getPackageFragment());
			for(int i = 0; i < types.length; i++){
				IType type = types[i];
				if(TypeHelper.isTeam(type.getFlags())){
					IType[] roles = getAllRoles(type);
					for (int j = 0; j < roles.length; j++) {
						IType role = roles[j];
						if(role.getElementName().equals(getNewElementName())){
							String msg = Messages.format("A role type named ''{0}'' exists in ''{1}'' and would shadow the renamed type (OTLD �1.4(a)).", new String[] { getNewElementName(),
									type.getFullyQualifiedName('.') });
							jdtStatus.addError(msg, JavaStatusContext.create(role));
						}
					}
				}
			}
			
			return jdtStatus;
		}
		
		checkRoleTypesInPackage <- replace checkTypesInPackage when(!fTypeToRenameIsRole);
		
		
		/**
		 * Roles need a more detailed analysis for name conflicts, that includes all roles of the enclosing team.
		 * 
		 */
		@SuppressWarnings("basecall")
		callin RefactoringStatus checkTypesInCompilationUnit(){
			RefactoringStatus status = new RefactoringStatus();
			try {
				status.merge(checkForExistingRoles(getFType()));
				status.merge(checkShadowingInEnclosingTeams(getFType()));
				return status;
			} catch (JavaModelException e) {
				// cache the exception to throw it later
				fCachedException = e;
				return status;
			}
		}
		
		checkTypesInCompilationUnit <- replace checkTypesInCompilationUnit when(fTypeToRenameIsRole);
		
		// helper

		/**
		 * Checks if the given role name is invalid because it is already used for OT specific interfaces.
		 * 
		 * @param newRoleName the new role name
		 * @return a <Code>RefactoringStatus</Code> that may signal an invalid role name
		 */
		private RefactoringStatus checkNewRoleName(String newRoleName) {
			RefactoringStatus status = new RefactoringStatus();
			// prevent invalid role names that are used for OT specific interfaces
			if(newRoleName.equals(CONFINED)
				|| newRoleName.equals(ICONFINED)
				|| newRoleName.equals(ILOWERABLE))
			{
				status.addFatalError(RefactoringCoreMessages.RenameTypeRefactoring_choose_another_name);
			}
			return status;
		}
		
		/**
		 * Checks if the new role name causes implicit overriding in super and sub teams.
		 * 
		 * @return the <code>RefactoringStatus</code> indicating implicit overriding
		 * @throws JavaModelException if the creation of a type hierarchy for the enclosing team failed
		 */
		private RefactoringStatus checkForRoleOverriding(IProgressMonitor pm) throws JavaModelException {
			Assert.isTrue(fTypeToRenameIsRole);
			
			IOTType otElement = OTModelManager.getOTElement(getFType());
			IOTType enclosingTeam = ((IRoleType)otElement).getTeam();
			
			pm.beginTask("Check Overriding", 1);
			pm.subTask(""); //$NON-NLS-1$
			RefactoringStatus status = new RefactoringStatus();
			try{
				// search for implicit overriding of inherited roles
				IType[] roles = TypeHelper.getInheritedRoleTypes(enclosingTeam);
				for (int i = 0; i < roles.length; i++) {
					IType currRole = roles[i];
					if(currRole.getElementName().equals(getNewElementName())){
						String msg = Messages.format("The renamed role type would override the inherited role type ''{0}''.",
								new String[] { currRole.getFullyQualifiedName('.') });
						status.addError(msg, JavaStatusContext.create(currRole));
						return status;
					}
				}
				
				
				// search for implicit overriding in the subtypes of the enclosing team
				OTTypeHierarchy teamHierarchy = new OTTypeHierarchy(enclosingTeam, enclosingTeam.getJavaProject(), true);
				teamHierarchy.refresh(pm);
				IType[] subtypes = teamHierarchy.getAllSubtypes(enclosingTeam);
				for (int i = 0; i < subtypes.length; i++) {
					IType[] declaredRoles = getDeclaredRoles(subtypes[i]);
					
					for (int j = 0; j < declaredRoles.length; j++) {
						IType currRole = declaredRoles[j];
						if(currRole.getElementName().equals(getNewElementName())){
							String msg = Messages.format("The renamed role type would be overridden by role type ''{0}''.",
									new String[] { currRole.getFullyQualifiedName('.') });
							status.addError(msg, JavaStatusContext.create(currRole));
							return status;
						}
					}
				}
				return status;
			}finally{
				pm.worked(1);
				pm.done();
			}
		}

		/**
		 * Checks if a package with the new team name already exists.
		 * 
		 * @param roleDirectory
		 * @return
		 * @throws JavaModelException
		 */
		private RefactoringStatus checkForConflictingRoleDirectoryName(IPackageFragment roleDirectory) throws JavaModelException {
			IJavaElement[] packages= ((IPackageFragmentRoot)roleDirectory.getParent()).getChildren();
			for (int i = 0;  i < packages.length; i++) {
				if(packages[i].getElementName().equals(getFType().getPackageFragment().getElementName() + "." + getNewElementName())){
					String msg = MessageFormat.format("The new team name ''{0}'' collides with the package ''{1}'', therefore a renaming of the role directory ''{2}'' is impossible.", new Object[]{getFType().getElementName(), packages[i].getElementName(), roleDirectory.getElementName()});
					return RefactoringStatus.createErrorStatus(msg);
				}
			}
			return null;
		}

		/**
		 * Checks for name conflicts within the enclosing team of the given role. Returns an <code>
		 * ERROR</code> <code>RefactoringStatus</code> if a role with the new name would shadow or would be shadowed by the new name.
		 * 
		 * @param roleType the role to be checked
		 * @return the <code>RefactoringStatus</code> indicating shadowing issues with other roles
		 * @throws JavaModelException
		 */
		private RefactoringStatus checkShadowingInEnclosingTeams(IType roleType) throws JavaModelException {
			RefactoringStatus status = new RefactoringStatus();
			if(!TypeHelper.isRole(roleType.getFlags())){
				return status;
			}
			
			IType outerTeam = getTeam(roleType);
			IType directEnclosingTeam = outerTeam;
			
			// Search for nested roles in the direct enclosing team that would shadow the renamed role type
			IType[] allRoles = TypeHelper.getAllRoleTypes(directEnclosingTeam);
			for (int i = 0; i < allRoles.length; i++) {
				if(allRoles[i].equals(OTModelManager.getOTElement(roleType))){
					// skip the renamed role type because it is already checked in the base's method checkEnclosedTypes()
					continue;
				}
				IType[] nestedRoles = getAllRoles(allRoles[i]);
				for (int j = 0; j < nestedRoles.length; j++) {
					IType role = nestedRoles[j];
					if(role.getElementName().equals(getNewElementName())){
						String msg = Messages.format("The role type ''{0}'' would shadow the renamed type (OTLD �1.4(a))."
								, new String[] { BasicElementLabels.getJavaElementName(role.getFullyQualifiedName('.')) });
						status.addError(msg, JavaStatusContext.create(role));
					}
				}
			}
			
			// If the enclosing team is a role itself recursively search role names that could be shadowed by the renamed role type
			while(TypeHelper.isRole(outerTeam.getFlags())){
				outerTeam = getTeam(outerTeam);
				IRoleType[] outerRoles = TypeHelper.getAllRoleTypes(outerTeam);
				for (int i = 0; i < outerRoles.length; i++) {
					if(outerRoles[i].equals(OTModelManager.getOTElement(directEnclosingTeam))){
						// skip the direct enclosing team, because this is checked in the base's method checkEnclosingTypes()
						continue;
					}
					IType role = outerRoles[i];
					if(role.getElementName().equals(getNewElementName())){
						String msg = Messages.format("The renamed role type would shadow the visible type ''{0}'' (OTLD �1.4(a))."
								, new String[] { BasicElementLabels.getJavaElementName(role.getFullyQualifiedName('.')) });
						status.addError(msg, JavaStatusContext.create(role));
					}
				}
			}
			
			return status;
		}
		
		private RefactoringStatus checkForExistingRoles(IType roleType) throws JavaModelException{
			RefactoringStatus status = new RefactoringStatus();
			if(!TypeHelper.isRole(roleType.getFlags())){
				return status;
			}
			
			// For the direct enclosing team all declared roles have to be checked
			IType enclosingTeam = getTeam(roleType);
			IType[] declaredRoles = getDeclaredRoles(enclosingTeam);
			for (int i = 0; i < declaredRoles.length; i++) {
				IOTType otElement = OTModelManager.getOTElement(declaredRoles[i]);
				if(otElement instanceof IRoleType){
					IRoleType role = (IRoleType) otElement;
					if(role.getElementName().equals(getNewElementName())){
						String msg = Messages.format(RefactoringCoreMessages.RenameTypeRefactoring_member_type_exists, new String[] { getNewElementLabel(),
								BasicElementLabels.getJavaElementName(role.getTeam().getFullyQualifiedName('.')) });
						status.addError(msg, JavaStatusContext.create(role));
					}
				}
			}		
			return status;
		}

		private IType getTeam(IType roleType) {
			IType enclosingTeam;
			IOTType otElement;
			if(roleType instanceof PhantomType){
				enclosingTeam = (IType) ((PhantomType)roleType).getParent();
			}else{
				otElement = OTModelManager.getOTElement(roleType);
				enclosingTeam = ((IRoleType)otElement).getTeam();
			}
			
			return enclosingTeam;
		}

		/**
		 * Checks if the given type is implemented in its own file as a top level type.
		 * @param roleType to be checked
		 * @return <code>true</code> if the role is implemented in its own file.
		 */
		private boolean isRoleFile(IType roleType) {
			String name = JavaCore.removeJavaLikeExtension(roleType.getCompilationUnit().getElementName());
			if (!(Checks.isTopLevel(roleType) && name.equals(roleType.getElementName())))
				return false;
			return true;
		}

		private IPackageFragment getRoleDirectoryForTeam(IType enclosingTeam) throws JavaModelException{
			// role directories can only be present for teams that are top level elements
			if(!Checks.isTopLevel(enclosingTeam)){
				return null;
			}
			
			// search for role directory
			IPackageFragment  fragment = enclosingTeam.getPackageFragment();
			if(fragment.hasSubpackages()){
				IJavaElement[] packages= ((IPackageFragmentRoot)fragment.getParent()).getChildren();
				for (int i = 0; i < packages.length; i++) {
					String name = packages[i].getElementName();
					String fragmentName = fragment.getElementName();
					// role directory found
					if(name.equals(fragmentName + "." + enclosingTeam.getElementName())){
						return (IPackageFragment)packages[i];
					}
				}
			}
			// no role directory found
			return null;
		}

		/**
		 * Searches all types within the given package, including binary and source declarations.
		 * 
		 * @param pack the package fragment to search in
		 * @return the found types
		 */
		private IType[] getAllTypesInPackage(IPackageFragment pack) throws JavaModelException{
			IJavaElement[] children = pack.getChildren();
			
			ArrayList<IType> types = new ArrayList<IType>();
			for(int i = 0; i < children.length; i++){
				IJavaElement child = children[i];
				
				if(child instanceof IClassFile){
					types.add(((IClassFile)child).getType());
				}
				
				if(child instanceof ICompilationUnit){
					types.addAll(Arrays.asList(((ICompilationUnit)child).getTypes()));
				}
			}
			return types.toArray(new IType[types.size()]);
		}

		/**
		 * Searches recursively all roles in a team.
		 */
		private IType[] getAllRoles(IType enclosingTeam) throws JavaModelException{
			if(Flags.isTeam(enclosingTeam.getFlags())){
				ArrayList<IType> result = new ArrayList<IType>();
				ArrayList<IType> roles = new ArrayList<IType>();
				roles.addAll(Arrays.asList(TypeHelper.getAllRoleTypes(enclosingTeam)));
				for (Iterator<IType> iterator = roles.iterator(); iterator.hasNext();) {
					IType roleType = (IType) iterator.next();
					result.addAll(Arrays.asList(getAllRoles(roleType)));
				}
				result.addAll(roles);
				return result.toArray(new IType[result.size()]);
			}else{
				return new IType[0];
			}
		}
		
		/**
		 * Finds all declared roles for the given team, including inline and external role file declarations.
		 * 
		 * @param teamType the team to search in
		 * @return the found roles
		 */
		private IType[] getDeclaredRoles(IType teamType) throws JavaModelException{
			if(!TypeHelper.isTeam(teamType.getFlags())){
				return new IType[0];
			}
			
			ArrayList<IType> allRoles = new ArrayList<IType>();
			IOTType otElement = OTModelManager.getOTElement(teamType);
			// add inline declared roles
			allRoles.addAll(Arrays.asList(otElement.getInnerTypes()));
			// add the found role files
			allRoles.addAll(Arrays.asList(getRoleFilesForTeam(teamType)));
			
			return allRoles.toArray(new IType[allRoles.size()]);
		}
		
		/**
		 * Finds all role file types for the given teamType.
		 * 
		 * @param teamType the team
		 * @return all found role file types
		 */
		private IType[] getRoleFilesForTeam(IType teamType) throws JavaModelException{
			IPackageFragment roleDirectory = getRoleDirectoryForTeam(teamType);
			IType[] roleFileTypes = new IType[0];
			if(roleDirectory != null){
				roleFileTypes = getAllTypesInPackage(roleDirectory);
			}
			return roleFileTypes;
		}
	}
}
