/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RefactoringUtil.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 * Johannes Gebauer - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.Corext;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.RippleMethodFinder2;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.search.OTSearchEngine;
import org.eclipse.objectteams.otdt.core.search.OTSearchRequestor;
import org.eclipse.objectteams.otdt.internal.core.InheritedMethodsRequestor;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchyTraverser;
import org.eclipse.objectteams.otdt.internal.refactoring.OTRefactoringPlugin;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.OTRefactoringCoreMessages;
import org.eclipse.objectteams.otdt.internal.refactoring.corext.base.OTRefactoringStatusCodes;

/**
 * This utility class is a part of the OT/J refactoring adaptation. It contains
 * some OT refactoring related helper methods.
 * 
 * @author brcan
 */
@SuppressWarnings("restriction")
public class RefactoringUtil implements ITeamConstants {
	/**
	 * Checks whether the top-level type of the given compilation unit is a
	 * regular class or a team class.
	 * 
	 * @param compUnit
	 *            the given compilation unit
	 * @param str
	 *            the string describing the error
	 * @return the refactoring status
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOOClass(ICompilationUnit compUnit, String str) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();
		IType type = robustFindPrimaryType(compUnit);

		if (Flags.isTeam(type.getFlags())) {
			status.addFatalError(OTRefactoringCoreMessages.getString(str));
		}
		return status;
	}

	/*
	 * Make a best guess to find the primary type of a CU even if names do not
	 * match.
	 */
	private static IType robustFindPrimaryType(ICompilationUnit compUnit) {
		IType type = compUnit.findPrimaryType();
		if (type != null)
			return type;
		IType[] types;
		try {
			types = compUnit.getTypes();
			if (types != null) {
				for (IType type2 : types) {
					if (Flags.isPublic(type2.getFlags()))
						return type2;
				}
				if (types.length > 0)
					return types[0];
			}
		} catch (JavaModelException e) {
			// nothing useful found, return null below
		}
		return null;
	}

	/**
	 * Returns all role types contained in the given project.
	 * 
	 * @param thisProject
	 *            the given project
	 * @return an array containing all role types of the given project
	 * @throws JavaModelException
	 * 
	 * @deprecated use getAllRoleClasses(IJavaProject, IProgressMonitor) instead
	 */
	public static IType[] getAllRoleTypes(IJavaProject thisProject) throws JavaModelException {
		List<IType> roles = new ArrayList<IType>();
		IPackageFragment[] packages = thisProject.getPackageFragments();

		for (int idx = 0; idx < packages.length; idx++) {
			if (packages[idx].getKind() != IPackageFragmentRoot.K_BINARY) {
				// get all compilation units in this package
				ICompilationUnit[] compUnits = packages[idx].getCompilationUnits();
				if (compUnits != null && compUnits.length != 0) {
					for (int idy = 0; idy < compUnits.length; idy++) {
						// get all top-level types declared
						// in this compilation unit
						IType[] types = compUnits[idy].getTypes();
						if (types != null && types.length != 0) {
							for (int idz = 0; idz < types.length; idz++) {
								// add role files
								if (TypeHelper.isRole(types[idz].getFlags())) {
									roles.add(types[idz]);
								}
								if (Modifier.isTeam(types[idz].getFlags())) {
									// get all roles of this team
									IType[] rolesOfTeam = types[idz].getTypes();
									for (int roleNr = 0; roleNr < rolesOfTeam.length; roleNr++) {
										roles.add(rolesOfTeam[roleNr]);
									}
								}
							}
						}
					}
				}
			}
		}
		return roles.toArray(new IType[roles.size()]);
	}

	/**
	 * Returns all role types contained in the given project and related
	 * projects.
	 * 
	 * @param project
	 *            the given project
	 * @param monitor
	 *            the progress monitor
	 * @return an array of role types
	 */
	public static IOTType[] getAllRoleClasses(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
		OTSearchRequestor requestor = new OTSearchRequestor();
		try {
			SearchPattern rolePattern = OTSearchEngine.createRoleTypePattern(IJavaSearchConstants.TYPE, SearchPattern.R_EXACT_MATCH);
			IJavaElement[] relevantProjects = getRelevantProjects(project);
			IJavaSearchScope scope = OTSearchEngine.createOTSearchScope(relevantProjects, true);
			OTSearchEngine engine = new OTSearchEngine();
			engine.search(rolePattern, scope, requestor, monitor);
		} catch (CoreException ex) {
			throw new JavaModelException(ex);
		}

		return requestor.getOTTypes();
	}

	public static ArrayList<IRoleType> getAllRolesForBase(IType baseType) throws CoreException {
        OTSearchEngine engine = new OTSearchEngine();
        IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
        SearchPattern pattern = SearchPattern.createPattern(baseType, IJavaSearchConstants.PLAYEDBY_REFERENCES);
        final ArrayList<IRoleType> roles = new ArrayList<IRoleType>();
        if (pattern == null)
            OTRefactoringPlugin.getInstance().getLog().log(new Status(Status.ERROR, OTRefactoringPlugin.PLUGIN_ID, "Error creating pattern")); //$NON-NLS-1$
        else
        	engine.search(
                pattern, 
                searchScope, 
                new SearchRequestor() {
                    public void acceptSearchMatch(SearchMatch match) throws CoreException
                    {
                        Object element = match.getElement();
                        if (element instanceof IType)
							roles.add((IRoleType) OTModelManager.getOTElement((IType) element));
                    }
                },
                null);
        return roles;
	}

	private static IJavaElement[] getRelevantProjects(IJavaProject srcProject) throws JavaModelException {
		List<IJavaProject> relevantProjects = new ArrayList<IJavaProject>();
		relevantProjects.add(srcProject); // this one is relevant for sure

		IJavaProject[] allJavaProjects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
		for (int idx = 0; idx < allJavaProjects.length; idx++) {
			IJavaProject prj = allJavaProjects[idx];
			if (prj.isOnClasspath(srcProject)) {
				relevantProjects.add(prj);
			}
		}
		return relevantProjects.toArray(new IJavaElement[relevantProjects.size()]);
	}

	/**
	 * Returns all bound base types of the given role types, that are declared
	 * by playedBy.
	 * 
	 * @param roleTypes
	 *            an array of IRoleTypes
	 * @return a list of bound base types
	 * @throws JavaModelException
	 */
	public static ArrayList<IType> getAllDeclaredBaseTypes(IOTType[] roleTypes) throws JavaModelException {
		ArrayList<IType> baseTypes = new ArrayList<IType>();
		for (int idx = 0; idx < roleTypes.length; idx++) {
			// IRoleType roleType =
			// (IRoleType)OTModelManager.getOTElement(roleTypes[idx]);
			IRoleType roleType = (IRoleType) roleTypes[idx];
			if (roleType.getBaseclassName() == null) {
				continue;
			}
			IType baseType = roleType.getBaseClass();
			if (baseType != null && !baseTypes.contains(baseType)) {
				baseTypes.add(baseType);
			}
		}
		return baseTypes;
	}

	public static IMethod[] hierarchyDeclaresMethodName(IProgressMonitor pm, IMethod method, String newName) throws CoreException {
		Set<IMethod> result = new HashSet<IMethod>();
		IType type = method.getDeclaringType();
		ITypeHierarchy hier = type.newTypeHierarchy(pm);
		IMethod foundMethod = Checks.findMethod(newName, method.getParameterTypes().length, false, type);
		if (foundMethod != null) {
			result.add(foundMethod);
		}
		IMethod[] foundInHierarchyClasses = classesDeclareMethodName(hier, Arrays.asList(hier.getAllClasses()), method, newName);
		if (foundInHierarchyClasses != null) {
			result.addAll(Arrays.asList(foundInHierarchyClasses));
		}
		IType[] implementingClasses = hier.getImplementingClasses(type);
		IMethod[] foundInImplementingClasses = classesDeclareMethodName(hier, Arrays.asList(implementingClasses), method, newName);
		if (foundInImplementingClasses != null) {
			result.addAll(Arrays.asList(foundInImplementingClasses));
		}
		return result.toArray(new IMethod[result.size()]);
	}

	private static IMethod[] classesDeclareMethodName(ITypeHierarchy hier, List<IType> classes, IMethod method, String newName) throws CoreException {
		Set<IMethod> result = new HashSet<IMethod>();
		IType type = method.getDeclaringType();
		List<IType> subtypes = Arrays.asList(hier.getAllSubtypes(type));

		int parameterCount = method.getParameterTypes().length;
		boolean isMethodPrivate = JdtFlags.isPrivate(method);

		for (Iterator<IType> iter = classes.iterator(); iter.hasNext();) {
			IType clazz = iter.next();
			IMethod[] methods = clazz.getMethods();
			boolean isSubclass = subtypes.contains(clazz);
			for (int j = 0; j < methods.length; j++) {
				IMethod foundMethod = Checks.findMethod(newName, parameterCount, false, new IMethod[] { methods[j] });
				if (foundMethod == null) {
					continue;
				}
				if (isSubclass || type.equals(clazz)) {
					result.add(foundMethod);
				} else if ((!isMethodPrivate) && (!JdtFlags.isPrivate(methods[j]))) {
					result.add(foundMethod);
				}
			}
		}
		return result.toArray(new IMethod[result.size()]);
	}

	/**
	 * Checks if the given method is declared in an interface. If the method's
	 * declaring type is an interface the method returns <code>false</code> if
	 * it is only declared in that interface.
	 */
	public static IMethod isDeclaredInInterface(IMethod method, OTTypeHierarchy hierarchy, IProgressMonitor pm) throws JavaModelException {
		assert isVirtual(method);
		try {
			IType[] classes = hierarchy.getAllClasses();
			IProgressMonitor subPm = new SubProgressMonitor(pm, 3);
			subPm.beginTask("", classes.length); //$NON-NLS-1$
			for (int idxAllHierarchyClasses = 0; idxAllHierarchyClasses < classes.length; idxAllHierarchyClasses++) {
				ITypeHierarchy superTypes = hierarchy.getOTSuperTypeHierarchy(classes[idxAllHierarchyClasses]);
				IType[] superinterfaces = superTypes.getAllSuperInterfaces(classes[idxAllHierarchyClasses]);

				for (int idxSuperInterfaces = 0; idxSuperInterfaces < superinterfaces.length; idxSuperInterfaces++) {
					IMethod found = Checks.findSimilarMethod(method, superinterfaces[idxSuperInterfaces]);
					if (found != null && !found.equals(method))
						return found;
				}
				subPm.worked(1);
			}
			return null;
		} finally {
			pm.done();
		}
	}

	public static boolean isVirtual(IMethod method) throws JavaModelException {
		IType declaringType = method.getDeclaringType();

		if (TypeHelper.isRole(declaringType.getFlags())) {
			if (method.isConstructor())
				return false;
			// note: private role method is virtual
			// if (JdtFlags.isPrivate(method))
			// return false;
			if (JdtFlags.isStatic(method))
				return false;
		} else {
			if (method.isConstructor())
				return false;
			if (JdtFlags.isPrivate(method))
				return false;
			if (JdtFlags.isStatic(method))
				return false;
		}
		return true;
	}

	public static IMethod overridesAnotherMethod(IMethod method, OTTypeHierarchy hier, IProgressMonitor pm) throws JavaModelException {
		IType declaringType = method.getDeclaringType();
		InheritedMethodsRequestor requestor = new InheritedMethodsRequestor(declaringType, true, true);
		OTTypeHierarchyTraverser traverser = new OTTypeHierarchyTraverser(requestor, OTTypeHierarchyTraverser.SUPER_HIERARCHY,
				OTTypeHierarchyTraverser.TRAVERSE_EXPLICIT_FIRST, false, false, pm);

		traverser.traverse();
		IMethod[] collectedMethods = requestor.getResult();

		for (int idx = collectedMethods.length - 1; idx >= 0; idx--) {
			if (method.isSimilar(collectedMethods[idx])) {
				return collectedMethods[idx];
			}
		}
		return null;
	}

	public static boolean isRoleMethod(IMethod method) throws JavaModelException {
		IType type = method.getDeclaringType();
		IOTType otType = OTModelManager.getOTElement(type);

		if (otType == null) {
			return false;
		} else if (otType.isRole()) {
			return true;
		} else {
			return false;
		}
	}

	public static String stripOffJavaSuffix(String compUnit) {
		int dot = compUnit.lastIndexOf('.');
		return compUnit.substring(0, dot);
	}

	/**
	 * Returns the corresponding DOM-AST node for the given role type.
	 * 
	 * @param role
	 *            the given role type
	 * @return node of type RoleTypeDeclaration
	 * @throws JavaModelException
	 */
	public static RoleTypeDeclaration getRoleClassDeclaration(IMember role) throws JavaModelException {
		ASTNode result = getASTNode(role, RoleTypeDeclaration.class);
		return (RoleTypeDeclaration) result;
	}

	/**
	 * Returns the corresponding DOM-AST node for the given method.
	 * 
	 * @param method
	 *            the given method
	 * @return node of type MethodDeclaration
	 * @throws JavaModelException
	 */
	public static MethodDeclaration getMethodDeclaration(IMember method) throws JavaModelException {
		ASTNode result = getASTNode(method, MethodDeclaration.class);
		return (MethodDeclaration) result;
	}

	/**
	 * Determines the focus type, i.e. the destination type of the refactored
	 * element (e.g. the destination type for an extracted or moved method).
	 * 
	 * @param topLevelType
	 *            the top-level type of the destination compilation unit
	 * @param destinationType
	 *            the destination type node
	 * @return the focus type, i.e. either a regular class (including team) or a
	 *         role class
	 */
	public static IType determineFocusType(IType topLevelType, ASTNode destinationType) {
		// if the destination of the extracted/moved code fragment/element is a
		// role type,
		// this role is the focus type...
		if (destinationType instanceof RoleTypeDeclaration) {
			RoleTypeDeclaration roleTypeDecl = (RoleTypeDeclaration) destinationType;
			ITypeBinding roleTypeBinding = ASTNodes.getTypeBinding(roleTypeDecl.getName());
			// if role is declared in the top-level type, get it...
			if (roleTypeBinding != null) {
				// Note: use String-based comparison for ITypeBinding <-> IType:
				String topLevelName = topLevelType.getFullyQualifiedName();
				String roleName = roleTypeBinding.getQualifiedName();
				if (roleName.equals(topLevelName))
					return topLevelType; // top level = focus (RoFi)
				if (roleTypeBinding.getDeclaringClass().getQualifiedName().equals(topLevelName)) {
					String simpleRoleName = roleTypeBinding.getName();
					// role name very likely starts with __OT__, strip it off:
					if (simpleRoleName.startsWith(IOTConstants.OT_DELIM))
						simpleRoleName = simpleRoleName.substring(IOTConstants.OT_DELIM_LEN);
					return TypeHelper.findRoleType(topLevelType, simpleRoleName);
				}
				// ...else find the nested role
				else {
					String relativeRoleName = getRelativeName(topLevelName, roleName);
					return TypeHelper.findNestedRoleType(topLevelType, relativeRoleName);
				}
			}
			return null;
		}
		// ...else the focus type is a team or regular class
		else {
			return topLevelType;
		}
	}

	/**
	 * Make the given qualified name relative, so that the simple name or
	 * rootType is the first part of the resulting name.
	 * 
	 * @param rootType
	 * @param fullyQualifiedName
	 * @return
	 */
	private static String getRelativeName(String rootType, String fullyQualifiedName) {
		assert fullyQualifiedName.startsWith(rootType) : "role type must start like enclosing team"; //$NON-NLS-1$
		int pos = rootType.lastIndexOf('.');
		if (pos == -1)
			pos = rootType.length();
		return fullyQualifiedName.substring(pos + 1);
	}

	public static RefactoringStatus checkOverloading(IMethod[] inheritedMethods, String newName, String[] newParameterTypes, IOverloadingMessageCreator msgCreator) {
		RefactoringStatus result = new RefactoringStatus();

		for (int idx = 0; idx < inheritedMethods.length; idx++) {
			// check if an inherited method or a local method has the
			// same name as the method to be refactored
			IMethod actualMethod = inheritedMethods[idx];
			if (actualMethod.getElementName().equals(newName)) {
				// get number of parameters of actual method
				int actualMethodParamCount = actualMethod.getParameterTypes().length;
				// get parameter types of actual method
				String[] actualMethodParamTypes = getParameterTypesOfActualMethod(actualMethod);
				// get number of parameters of method to be refactored
				int refactoredMethodParamCount = newParameterTypes == null ? 0 : newParameterTypes.length;

				// check if this method has a different number of parameters
				// than
				// the method to be refactored or...
				if (actualMethodParamCount != refactoredMethodParamCount) {
					// if overloading is present, issue a warning
					result.merge(addOverloadingWarning(msgCreator));
				}
				// ...if it has the same number of parameters but different
				// parameter types
				else if (newParameterTypes != null) {
					for (int idz = 0; idz < actualMethodParamTypes.length; idz++) {
						if (!actualMethodParamTypes[idz].equals(newParameterTypes[idz])) {
							// if overloading is present, issue a warning
							result.merge(addOverloadingWarning(msgCreator));
						}
					}
				}
			}
		}
		return result;
	}

	private static String[] getParameterTypesOfActualMethod(IMethod actualMethod) {
		String[] actualMethodParamTypes = actualMethod.getParameterTypes();
		String[] readableActualMethodParamTypes = new String[actualMethodParamTypes.length];
		for (int idy = 0; idy < actualMethodParamTypes.length; idy++) {
			// convert parameter types into readable names
			readableActualMethodParamTypes[idy] = Signature.toString(actualMethodParamTypes[idy]);
		}
		return readableActualMethodParamTypes;
	}

	@SuppressWarnings("unchecked")
	// List getBaseMappingElements()
	public static RefactoringStatus checkForAmbiguousBaseMethodSpecs(ArrayList<IRoleType> boundRoleTypes, String newMethodName, String oldMethodName, IAmbuguityMessageCreator msgCreator)
			throws JavaModelException {
		RefactoringStatus result = new RefactoringStatus();

		for (Iterator<IRoleType> iter = boundRoleTypes.iterator(); iter.hasNext();) {
			IRoleType boundRoleType = iter.next();
			// get all method mappings of bound role type
			AbstractMethodMappingDeclaration[] mappings = RefactoringUtil.getAllMethodMappings(boundRoleType);

			if (mappings != null && mappings.length != 0) {
				for (int idx = 0; idx < mappings.length; idx++) {
					AbstractMethodMappingDeclaration mapping = mappings[idx];
					if (mapping instanceof CallinMappingDeclaration) {
						CallinMappingDeclaration callinDecl = (CallinMappingDeclaration) mapping;
						List<MethodMappingElement> baseMethodSpecs = callinDecl.getBaseMappingElements();
						for (Iterator<MethodMappingElement> iterator = baseMethodSpecs.iterator(); iterator.hasNext();) {
							MethodMappingElement baseMethodSpec = iterator.next();
							String baseMethodName = baseMethodSpec.getName().getIdentifier();
							// check if base method specifier in callin mapping
							// has
							// no signature and if it has the same name as the
							// extracted
							// method
							if (!baseMethodSpec.hasSignature() && (baseMethodName.equals(newMethodName) || baseMethodName.equals(oldMethodName))) {
								// create the context
								RefactoringStatusContext context = createContext(boundRoleType, baseMethodSpec);
								// if it has the same name, issue an error
								result.merge(addAmbiguityFatalError(context, msgCreator));
							}
						}
					} else if (mapping instanceof CalloutMappingDeclaration) {
						CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration) mapping;
						MethodMappingElement baseElement = calloutDecl.getBaseMappingElement();
						// check if base element is a method spec and not a
						// field access spec
						if (baseElement instanceof MethodSpec) {
							MethodSpec baseMethodSpec = (MethodSpec) baseElement;
							String baseMethodName = baseMethodSpec.getName().getIdentifier();
							// check if base method specifier in callout mapping
							// has
							// no signature and if it has the same name as the
							// extracted
							// method
							if (!baseMethodSpec.hasSignature() && (baseMethodName.equals(newMethodName) || baseMethodName.equals(oldMethodName))) {
								// create the context
								RefactoringStatusContext context = createContext(boundRoleType, baseMethodSpec);
								// if it has the same name, issue an error
								result.merge(addAmbiguityFatalError(context, msgCreator));
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static RefactoringStatus checkForAmbiguousRoleMethodSpecs(IRoleType roleType, String newMethodName, IAmbuguityMessageCreator msgCreator) throws JavaModelException {
		RefactoringStatus result = new RefactoringStatus();
		// get all method mappings of role type
		AbstractMethodMappingDeclaration[] mappings = RefactoringUtil.getAllMethodMappings(roleType);

		if (mappings != null && mappings.length != 0) {
			for (int idx = 0; idx < mappings.length; idx++) {
				AbstractMethodMappingDeclaration mapping = mappings[idx];
				if (mapping instanceof CallinMappingDeclaration) {
					CallinMappingDeclaration callinDecl = (CallinMappingDeclaration) mapping;
					MethodSpec roleMethodSpec = (MethodSpec) callinDecl.getRoleMappingElement();
					String roleMethodName = roleMethodSpec.getName().getIdentifier();
					// check if role method specifier in callin mapping has
					// no signature and if it has the same name as the extracted
					// method
					if (!roleMethodSpec.hasSignature() && roleMethodName.equals(newMethodName)) {
						// create the context
						RefactoringStatusContext context = createContext(roleType, roleMethodSpec);
						// if it has the same name, issue an error
						result.merge(addAmbiguityFatalError(context, msgCreator));
					}
				} else if (mapping instanceof CalloutMappingDeclaration) {
					CalloutMappingDeclaration calloutDecl = (CalloutMappingDeclaration) mapping;
					MethodSpec roleMethodSpec = (MethodSpec) calloutDecl.getRoleMappingElement();
					String roleMethodName = roleMethodSpec.getName().getIdentifier();
					// check if role method specifier in callout mapping has
					// no signature and if it has the same name as the extracted
					// method
					if (!roleMethodSpec.hasSignature() && roleMethodName.equals(newMethodName)) {
						// create the context
						RefactoringStatusContext context = createContext(roleType, roleMethodSpec);
						// if it has the same name, issue an error
						result.merge(addAmbiguityFatalError(context, msgCreator));
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns all method mappings of the given role type.
	 * 
	 * @param roleType
	 *            the given role type
	 * @return an array of method mapping declarations
	 * @throws JavaModelException
	 */
	public static AbstractMethodMappingDeclaration[] getAllMethodMappings(IRoleType roleType) throws JavaModelException {
		RoleTypeDeclaration roleClassDecl = getRoleClassDeclaration(roleType);
		// get all callin and callout mappings of given role type
		AbstractMethodMappingDeclaration[] callinMappings = roleClassDecl.getCallIns();
		AbstractMethodMappingDeclaration[] calloutMappings = roleClassDecl.getCallOuts();

		AbstractMethodMappingDeclaration[] mappings = new AbstractMethodMappingDeclaration[callinMappings.length + calloutMappings.length];
		if (callinMappings.length != 0) {
			System.arraycopy(callinMappings, 0, mappings, 0, callinMappings.length);
			System.arraycopy(calloutMappings, 0, mappings, callinMappings.length, calloutMappings.length);
		} else {
			System.arraycopy(calloutMappings, 0, mappings, 0, calloutMappings.length);
		}
		return mappings;
	}

	public static RefactoringStatus addOverloadingWarning(IOverloadingMessageCreator msgCreator) {
		RefactoringStatus result = RefactoringUtil.createWarningStatus(msgCreator.createOverloadingMessage(),
				null, OTRefactoringStatusCodes.OVERLOADING);
		return result;
	}

	public static RefactoringStatus addAmbiguityFatalError(RefactoringStatusContext context, IAmbuguityMessageCreator msgCreator) {
		RefactoringStatus result = RefactoringUtil.createFatalErrorStatus(msgCreator.createAmbiguousMethodSpecifierMsg(),
				context, OTRefactoringStatusCodes.AMBIGUOUS_METHOD_SPECIFIER);
		return result;
	}

	
	/**
	 * Performs overloading checks for a team class or a regular class.
	 * 
	 * @param result
	 *            the actual refactoring status
	 * @param newMethodName
	 *            the name of the refactored method
	 * @param focusType
	 *            the destination type of the refactored method
	 * @param msgCreator a message creator object to provide refactoring specific error messages
	 * @param pm
	 *            the progress monitor
	 * @param newMethod
	 *            the refactored method
	 * @return the refactoring status containing a warning entry in case of
	 *         overloading; ok otherwise.
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingForTeamOrRegularClass(RefactoringStatus result, String newMethodName, String[] newParamTypes,
			IType focusType, IOverloadingMessageCreator msgCreator, IProgressMonitor pm) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();

		RefactoringStatusEntry overloading = result.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING);

		if (overloading == null) {
			IMethod[] methods = TypeHelper.getInheritedMethods(focusType, true, true, true, pm);
			status.merge(checkOverloading(methods, newMethodName, newParamTypes, msgCreator));
		}
		return status;
	}

	/**
	 * Performs overloading and ambiguity checks for a role type.
	 * 
	 * @param result
	 *            the actual refactoring status
	 * @param newMethodName
	 *            the name of the refactored method
	 * @param roleType
	 *            the focus role
	 * @param ambiguitymsgCreator a message creator object to provide refactoring specific error messages
	 * @param overloadingmsgCreator a message creator object to provide refactoring specific error messages
	 * @param pm
	 *            the progress monitor
	 * @param newMethod
	 *            the refactored method
	 * @return the refactoring status containing a warning entry in case of
	 *         overloading, or a warning and fatal error entry in case of
	 *         overloading and ambiguous method specs; ok otherwise.
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingAndAmbiguityForRole(RefactoringStatus result, String newMethodName, String[] newParamTypes,
			IRoleType roleType, IAmbuguityMessageCreator ambiguityMsgCreator, IOverloadingMessageCreator overloadingMsgCreator, IProgressMonitor pm) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();

		RefactoringStatusEntry overloading = result.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING);

		if (overloading == null) {
			// NOTE(gbr): result array of getInheritedMethods(..) also includes
			// private methods. The reason is that method bindings may refer
			// to hidden base methods (see OTJLD �3.4 and �4.6). This fact
			// must be considered in the overloading and ambiguity checks.
			IMethod[] methods = TypeHelper.getInheritedMethods(roleType, true, true, false, pm);
			status.merge(checkOverloading(methods, newMethodName, newParamTypes, overloadingMsgCreator));
		}
		if (status.hasWarning() || result.hasWarning()) {
			status.merge(checkForAmbiguousRoleMethodSpecs(roleType, newMethodName, ambiguityMsgCreator));
		}
		return status;
	}

	/**
	 * Performs overloading and ambiguity checks for a bound base type.
	 * 
	 * @param result
	 *            the actual refactoring status
	 * @param newMethodName
	 *            the name of the refactored method
	 * @param oldMethodName
	 *            the old name of the refactored method
	 * @param focusType
	 *            the regarded type
	 * @param roleTypes
	 *            the role types contained in the project
	 * @param ambiguitymsgCreator a message creator object to provide refactoring specific error messages
	 * @param overloadingmsgCreator a message creator object to provide refactoring specific error messages
	 * @param pm
	 *            the progress monitor
	 * @param newMethod
	 *            the refactored method
	 * @return the refactoring status containing a warning entry in case of
	 *         overloading, or a warning and fatal error entry in case of
	 *         overloading and ambiguous method specs; ok otherwise.
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingAndAmbiguityForBase(RefactoringStatus result, String newMethodName, String oldMethodName,
			String[] newParamTypes, IType focusType, IOTType[] roleTypes, IAmbuguityMessageCreator ambiguityMsgCreator, IOverloadingMessageCreator overloadingMsgCreator, IProgressMonitor pm) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();

		ArrayList<IRoleType> boundRoleTypes = new ArrayList<IRoleType>();
		for (int idx = 0; idx < roleTypes.length; idx++) {
			IRoleType roleType = (IRoleType) roleTypes[idx];
			IType baseClass = roleType.getBaseClass();
			if (baseClass != null && baseClass.equals(focusType)) {
				RefactoringStatusEntry overloadingThis = status.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING);
				RefactoringStatusEntry overloadingOther = result.getEntryMatchingCode(Corext.getPluginId(), OTRefactoringStatusCodes.OVERLOADING);

				// check overloading only once for all role types bound to the
				// same baseclass
				if (overloadingThis == null && overloadingOther == null) {
					// NOTE(gbr): result array of getInheritedMethods(..) also
					// includes
					// private methods. The reason is that method bindings may
					// refer
					// to hidden base methods (see OTJLD �3.4 and �4.6).
					// This fact
					// must be considered in the overloading and ambiguity
					// checks.
					IMethod[] baseMethods = TypeHelper.getInheritedMethods(baseClass, true, true, false, pm);
					status.merge(checkOverloading(baseMethods, newMethodName, newParamTypes, overloadingMsgCreator));
				}
				// add role that is bound to the base class to the list
				boundRoleTypes.add(roleType);
			}
		}
		if (status.hasWarning() || result.hasWarning()) {
			status.merge(checkForAmbiguousBaseMethodSpecs(boundRoleTypes, newMethodName, oldMethodName, ambiguityMsgCreator));
		}
		return status;
	}

	/**
	 * Performs overloading and ambiguity checks for a bound base type present
	 * in the super hierarchy of the focus (destination) type.
	 * 
	 * @param newMethodName
	 *            the name of the refactored method
	 * @param oldMethodName
	 *            the old name of the refactored method
	 * @param superTypes
	 *            the super types of the focus type
	 * @param msgCreator a message creator object to provide refactoring specific error messages
	 * @param roleType
	 *            the role type bound to the base type
	 * @return the refactoring status containing a fatal error entry in case of
	 *         ambiguous method specs; ok otherwise.
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkAmbiguityForBasePresentInFocusTypeSuperhierarchy(String newMethodName, String oldMethodName, IType[] superTypes,
			IAmbuguityMessageCreator msgCreator, IRoleType roleType) throws JavaModelException {
		RefactoringStatus status = new RefactoringStatus();
		ArrayList<IRoleType> roleList = new ArrayList<IRoleType>();

		IType baseClass = roleType.getBaseClass();
		if (baseClass != null) {
			for (int idx = 0; idx < superTypes.length; idx++) {
				if (baseClass.equals(superTypes[idx])) {
					roleList.add(roleType);
					status.merge(checkForAmbiguousBaseMethodSpecs(roleList, newMethodName, oldMethodName, msgCreator));
				}
			}
		}
		return status;
	}

	/**
	 * Checks overloading and ambiguity for the given method name and method
	 * declaration.
	 * 
	 * @param compUnit
	 *            the target compilation unit
	 * @param destination
	 *            the ast node representing the destination type
	 * @param newMethodName
	 *            the name of the new method
	 * @param ambiguitymsgCreator a message creator object to provide refactoring specific error messages
	 * @param overloadingmsgCreator a message creator object to provide refactoring specific error messages
	 * @param pm
	 *            the progress monitor
	 * @param newMethod
	 *            the method declaration of the new method
	 * @return the refactoring status
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingAndAmbiguity(ICompilationUnit compUnit, ASTNode destination, String newMethodName, String[] newParamTypes,
			IAmbuguityMessageCreator ambiguityMsgCreator, IOverloadingMessageCreator overloadingMsgCreator, IProgressMonitor pm) throws JavaModelException {
		// get top level type declared in compilation unit
		IType topLevelType = robustFindPrimaryType(compUnit);
		// determine focus type: can be either a regular class (including team)
		// or a role
		IType focusType = RefactoringUtil.determineFocusType(topLevelType, destination);

		return checkOverloadingAndAmbiguity(focusType, null, newMethodName, newParamTypes, ambiguityMsgCreator, overloadingMsgCreator, pm);
		
	}

	/**
	 * Checks overloading and ambiguity for the given method name and method
	 * declaration.
	 * 
	 * @param focusType
	 *            the focus type to check for ambiguity
	 * @param focusTypeHierarchy
	 *            the type hierarchy for the focus type - will be computed if
	 *            <code>null</code>
	 * @param newMethodName
	 *            the name of the new method
	 * @param newParamTypes
	 *            the parameters of the new method
	 * @param ambigutiymsgCreator a message creator object to provide refactoring specific error messages
	 * @param overloadingmsgCreator a message creator object to provide refactoring specific error messages
	 * @param pm
	 *            the progress monitor
	 * @return the refactoring status
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingAndAmbiguity(IType focusType, ITypeHierarchy focusTypeHierarchy, String newMethodName,
			String[] newParamTypes, IAmbuguityMessageCreator ambigutiyMsgCreator, IOverloadingMessageCreator overloadingMsgCreator, IProgressMonitor pm) throws JavaModelException {
		ICompilationUnit compUnit = focusType.getCompilationUnit();
		// create OT typehierarchy
		if (focusTypeHierarchy == null) {
			focusTypeHierarchy = new OTTypeHierarchy(focusType, compUnit.getJavaProject(), true);
			focusTypeHierarchy.refresh(pm);
		}
		// get all supertypes of focus type
		IType[] superTypes = focusTypeHierarchy.getAllSupertypes(focusType);
		// get all subtypes of focus type
		IType[] subTypes = focusTypeHierarchy.getAllSubtypes(focusType);
		// get all role types in this project
		IOTType[] roleTypes = RefactoringUtil.getAllRoleClasses(compUnit.getJavaProject(), pm);
		// get all base types
		ArrayList<IType> baseTypes = RefactoringUtil.getAllDeclaredBaseTypes(roleTypes);

		RefactoringStatus result = new RefactoringStatus();
		// if the focus type is a team,
		// check for overloading methods in team and its supertypes
		if (TypeHelper.isTeam(focusType.getFlags())) {
			result.merge(RefactoringUtil.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, focusType, overloadingMsgCreator, pm));
			if (result.getSeverity() == RefactoringStatus.WARNING) {
				return result;
			}
		}
		// if the focus type is a role,
		// check for overloading methods in role and its supertypes,
		// and for ambiguity in its method bindings
		if (TypeHelper.isRole(focusType.getFlags())) {
			IRoleType roleType = (IRoleType) OTModelManager.getOTElement(focusType);
			result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForRole(result, newMethodName, newParamTypes, roleType, ambigutiyMsgCreator, overloadingMsgCreator,
					pm));
			result.merge(RefactoringUtil.checkAmbiguityForBasePresentInFocusTypeSuperhierarchy(newMethodName, "", superTypes, ambigutiyMsgCreator, roleType));
		}
		// if the focus type is an unbound regular class,
		// check for overloading methods in this class and its supertypes
		else if (!baseTypes.contains(focusType)) {
			RefactoringUtil.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, focusType, overloadingMsgCreator, pm);
			if (result.getSeverity() == RefactoringStatus.WARNING) {
				return result;
			}
		}
		// if the focus type is a bound base class,
		// check for overloading methods in this base class and its supertypes,
		// and for ambiguity in method bindings of bound role classes
		else if (baseTypes.contains(focusType)) {
			result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForBase(result, newMethodName, "", newParamTypes, focusType, roleTypes,
					ambigutiyMsgCreator, overloadingMsgCreator, pm));
		}
		// if the focus type has subtypes,
		// check overloading in the super hierarchy of each type
		if (subTypes.length != 0) {
			for (int idx = 0; idx < subTypes.length; idx++) {
				IType subType = subTypes[idx];

				// if the actual subtype of the focus type is a role,
				// check for overloading methods in this role and its
				// supertypes and for ambiguity in method bindings
				if (TypeHelper.isRole(subType.getFlags())) {
					IRoleType roleType = (IRoleType) OTModelManager.getOTElement(subType);
					result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForRole(result, newMethodName, newParamTypes, roleType, ambigutiyMsgCreator,
							overloadingMsgCreator, pm));
					if (result.hasWarning()) {
						IType baseClass = roleType.getBaseClass();
						ArrayList<IRoleType> roleList = new ArrayList<IRoleType>();
						// if base class of role type is the focus type,
						// check also ambiguous base method specs
						if (baseClass != null && baseClass.equals(focusType)) {
							roleList.add(roleType);
							result.merge(RefactoringUtil.checkForAmbiguousBaseMethodSpecs(roleList, newMethodName, "", ambigutiyMsgCreator));
						}
						// else if base class of role type is a direct or
						// indirect
						// superclass of focus type, check also ambiguous base
						// method specs
						else {
							result.merge(RefactoringUtil.checkAmbiguityForBasePresentInFocusTypeSuperhierarchy(newMethodName, "", superTypes, ambigutiyMsgCreator,
									roleType));
						}
					}
				}
				// if the actual subtyp of the focus type is an unbound regular
				// class,
				// check for overloading methods in this class and its
				// supertypes
				else if (!baseTypes.contains(subType)) {
					result.merge(RefactoringUtil
							.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, subType, overloadingMsgCreator, pm));
				}
				// if the actual subtype of the focus type is a bound base
				// class,
				// check for overloading methods in this base class and its
				// supertypes
				// and for ambiguity in method bindings of bound role classes
				else if (baseTypes.contains(subType)) {
					result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForBase(result, newMethodName, "", newParamTypes, subType, roleTypes, ambigutiyMsgCreator,
							overloadingMsgCreator, pm));
				}
			}
		}
		return result;
	}

	// TODO(jsv): copy of checkOverloadingAndAmbiguity with ASTNode as
	// destination -> remove redundant code
	/**
	 * Checks overloading and ambiguity for the given method name and method
	 * declaration.
	 * 
	 * @param compUnit
	 *            the target compilation unit
	 * @param focusType
	 *            the type of the renamed method
	 * @param newMethodName
	 *            the name of the new method
	 * @param oldMethodName
	 *            the old name of the method
	 * @param ambiguitymsgCreator a message creator object to provide refactoring specific error messages
	 * @param overloadingmsgCreator a message creator object to provide refactoring specific error messages.
	 * @param pm
	 *            the progress monitor
	 * @param newMethod
	 *            the IMethod of the new method
	 * @return the refactoring status
	 * @throws JavaModelException
	 */
	public static RefactoringStatus checkOverloadingAndAmbiguity(ICompilationUnit compUnit, IType focusType, String newMethodName, String oldMethodName,
			String[] newParamTypes, IAmbuguityMessageCreator ambiguityMsgCreator, IOverloadingMessageCreator overloadingMsgCreator, IProgressMonitor pm) throws JavaModelException {
		// FIXME(SH): what is this unused code good for?
		// get top level type declared in compilation unit
		// IType topLevelType = compUnit.findPrimaryType();
		// determine focus type: can be either a regular class (including team)
		// or a role
		// IType focusType = RefactoringUtil.determineFocusType(topLevelType,
		// destination);

		// create OT typehierarchy
		OTTypeHierarchy completeHierarchy = new OTTypeHierarchy(focusType, compUnit.getJavaProject(), true);
		completeHierarchy.refresh(pm);
		// get all supertypes of focus type
		IType[] superTypes = completeHierarchy.getAllSupertypes(focusType);
		// get all subtypes of focus type
		IType[] subTypes = completeHierarchy.getAllSubtypes(focusType);
		// get all role types in this project
		IOTType[] roleTypes = RefactoringUtil.getAllRoleClasses(compUnit.getJavaProject(), pm);
		// get all base types
		ArrayList<IType> baseTypes = RefactoringUtil.getAllDeclaredBaseTypes(roleTypes);

		RefactoringStatus result = new RefactoringStatus();
		// if the focus type is a team,
		// check for overloading methods in team and its supertypes
		if (TypeHelper.isTeam(focusType.getFlags())) {
			result.merge(RefactoringUtil.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, focusType, overloadingMsgCreator, pm));
			if (result.getSeverity() == RefactoringStatus.WARNING) {
				return result;
			}
		}
		// if the focus type is a role,
		// check for overloading methods in role and its supertypes,
		// and for ambiguity in its method bindings
		if (TypeHelper.isRole(focusType.getFlags())) {
			IRoleType roleType = (IRoleType) OTModelManager.getOTElement(focusType);
			result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForRole(result, newMethodName, newParamTypes, roleType, ambiguityMsgCreator, overloadingMsgCreator,
					pm));
			result.merge(RefactoringUtil.checkAmbiguityForBasePresentInFocusTypeSuperhierarchy(newMethodName, oldMethodName, superTypes, ambiguityMsgCreator, roleType));
		}
		// if the focus type is an unbound regular class,
		// check for overloading methods in this class and its supertypes
		else if (!baseTypes.contains(focusType)) {
			RefactoringUtil.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, focusType, overloadingMsgCreator, pm);
			if (result.getSeverity() == RefactoringStatus.WARNING) {
				return result;
			}
		}
		// if the focus type is a bound base class,
		// check for overloading methods in this base class and its supertypes,
		// and for ambiguity in method bindings of bound role classes
		else if (baseTypes.contains(focusType)) {
			result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForBase(result, newMethodName, oldMethodName, newParamTypes, focusType, roleTypes,
					ambiguityMsgCreator, overloadingMsgCreator, pm));
		}
		// if the focus type has subtypes,
		// check overloading in the super hierarchy of each type
		if (subTypes.length != 0) {
			for (int idx = 0; idx < subTypes.length; idx++) {
				IType subType = subTypes[idx];

				// if the actual subtype of the focus type is a role,
				// check for overloading methods in this role and its
				// supertypes and for ambiguity in method bindings
				if (TypeHelper.isRole(subType.getFlags())) {
					IRoleType roleType = (IRoleType) OTModelManager.getOTElement(subType);
					result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForRole(result, newMethodName, newParamTypes, roleType, ambiguityMsgCreator,
							overloadingMsgCreator, pm));
					if (result.hasWarning()) {
						IType baseClass = roleType.getBaseClass();
						ArrayList<IRoleType> roleList = new ArrayList<IRoleType>();
						// if base class of role type is the focus type,
						// check also ambiguous base method specs
						if (baseClass != null && baseClass.equals(focusType)) {
							roleList.add(roleType);
							result.merge(RefactoringUtil.checkForAmbiguousBaseMethodSpecs(roleList, newMethodName, oldMethodName, ambiguityMsgCreator));
						}
						// else if base class of role type is a direct or
						// indirect
						// superclass of focus type, check also ambiguous base
						// method specs
						else {
							result.merge(RefactoringUtil.checkAmbiguityForBasePresentInFocusTypeSuperhierarchy(newMethodName, oldMethodName, superTypes,
									ambiguityMsgCreator, roleType));
						}
					}
				}
				// if the actual subtyp of the focus type is an unbound regular
				// class,
				// check for overloading methods in this class and its
				// supertypes
				else if (!baseTypes.contains(subType)) {
					result.merge(RefactoringUtil
							.checkOverloadingForTeamOrRegularClass(result, newMethodName, newParamTypes, subType, overloadingMsgCreator, pm));
				}
				// if the actual subtype of the focus type is a bound base
				// class,
				// check for overloading methods in this base class and its
				// supertypes
				// and for ambiguity in method bindings of bound role classes
				else if (baseTypes.contains(subType)) {
					result.merge(RefactoringUtil.checkOverloadingAndAmbiguityForBase(result, newMethodName, oldMethodName, newParamTypes, subType, roleTypes,
							ambiguityMsgCreator, overloadingMsgCreator, pm));
				}
			}
		}
		return result;
	}

	/**
	 * Creates a refactoring status context for the given role type and method
	 * spec.
	 * 
	 * @param roleType
	 *            the role containing the error
	 * @param methodSpec
	 *            the method spec that has caused the error
	 * @return the refactoring status context or null if the context cannot be
	 *         created
	 */
	public static RefactoringStatusContext createContext(IRoleType roleType, MethodMappingElement methodSpec) {
		RefactoringStatusContext context = JavaStatusContext.create(roleType.getCompilationUnit(), methodSpec);
		return context;
	}

	/**
	 * Creates a warning refactoring status.
	 * 
	 * @param msg
	 *            the message of the warning entry
	 * @param context
	 *            the context of the warning entry
	 * @param code
	 *            the problem code (@see <code>OTRefactoringStatusCodes</code>)
	 * @return the refactoring status
	 */
	public static RefactoringStatus createWarningStatus(String msg, RefactoringStatusContext context, int code) {
		return RefactoringStatus.createStatus(RefactoringStatus.WARNING, msg, context, Corext.getPluginId(), code, null);
	}

	/**
	 * Creates an error refactoring status.
	 * 
	 * @param msg
	 *            the message of the error entry
	 * @param context
	 *            the context of the error entry
	 * @param code
	 *            the problem code (@see <code>OTRefactoringStatusCodes</code>)
	 * @return the refactoring status
	 */
	public static RefactoringStatus createErrorStatus(String msg, RefactoringStatusContext context, int code) {
		return RefactoringStatus.createStatus(RefactoringStatus.ERROR, msg, context, Corext.getPluginId(), code, null);
	}

	/**
	 * Creates a fatal error refactoring status.
	 * 
	 * @param msg
	 *            the message of the fatal error entry
	 * @param context
	 *            the context of the fatal error entry
	 * @param code
	 *            the problem code (@see <code>OTRefactoringStatusCodes</code>)
	 * @return the refactoring status
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg, RefactoringStatusContext context, int code) {
		return RefactoringStatus.createStatus(RefactoringStatus.FATAL, msg, context, Corext.getPluginId(), code, null);
	}

	/**
	 * Checks, if method has a special OT name. Does not use the
	 * OTTypeHIerarchy, because of performance optimization (team constants are
	 * using)
	 * 
	 * @param current
	 *            method to check
	 * @param new method name, should be <code>null</code> or an empty String if
	 *        you wants to check the current method and not the new method with
	 *        the same arguments like the current method
	 * @return the refactoring status
	 */
	public static boolean isOTSpecialCase(IMethod origMethod, String newMethodName, boolean checkImplementingClasses, IProgressMonitor pm) throws CoreException {
		// TODO(jsv) what to do in case of interface is only used by base
		// class(es) ? to check the impolementing classes is a performance
		// overhead
		IType declaringType = origMethod.getDeclaringType();

		if (!Flags.isTeam(declaringType.getFlags())) {
			// heavyweight check for interfaces
			if (Flags.isInterface(declaringType.getFlags()) && checkImplementingClasses) {
				OTTypeHierarchy hier = new OTTypeHierarchy(declaringType, declaringType.getJavaProject(), true);
				hier.refresh(pm);
				IType[] impementingClasses = hier.getImplementingClasses(declaringType);
				boolean teamImplementsInterface = false;
				for (int idx = 0; idx < impementingClasses.length; idx++) {
					if (Flags.isTeam(impementingClasses[idx].getFlags())) {
						teamImplementsInterface = true;
						break;
					}
				}

				if (!teamImplementsInterface) {
					return false;
				}
			} else {
				return false;
			}
		}

		if (newMethodName == null || newMethodName.length() == 0)
			newMethodName = origMethod.getElementName();

		assert (virtualTeamMethodNames.length == virtualTeamMethodParamTypes.length)
				&& (virtualTeamMethodParamTypes.length == virtualTeamMethodReturnTypes.length);
		for (int i = 0; i < virtualTeamMethodNames.length; i++) {
			if (virtualTeamMethodNames[i].equals(newMethodName) && Checks.compareParamTypes(origMethod.getParameterTypes(), virtualTeamMethodParamTypes[i])
					&& virtualTeamMethodReturnTypes[i].equals(origMethod.getReturnType())) {
				return true;
			}
		}
		return false;
	}

	public static ASTNode getASTNode(IMember member, Class<? extends ASTNode> nodeClass) {
		if (member == null || member.getCompilationUnit() == null) {
			return null;
		}

		ICompilationUnit source = member.getCompilationUnit();

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		// NOTE(SH): cannot use member.getSourceRange() together with the AST:
		// member uses declarationSourceEnd (incl. comment) whereas
		// the ASTConverter uses bodyEnd instead (excluding comment/trailing
		// '}')
		// However, getNameRange() should be safe.
		try {
			ASTNode name = NodeFinder.perform(astRoot, member.getNameRange());
			return getParent(name, nodeClass);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Finds all overridden methods of a certain method.
	 * 
	 */
	public static IMethod[] getOverriddenMethods(IMethod method, IProgressMonitor monitor) throws CoreException {

		assert method != null;
		return RippleMethodFinder2.getRelatedMethods(method, monitor, null);
	}

	/**
	 * Locates the topmost method of an override ripple and returns it. If none
	 * is found, null is returned.
	 * 
	 * @param method
	 *            the IMethod which may be part of a ripple
	 * @param typeHierarchy
	 *            a ITypeHierarchy of the declaring type of the method. May be
	 *            null
	 * @param monitor
	 *            an IProgressMonitor
	 * @return the topmost method of the ripple, or null if none
	 * @throws JavaModelException
	 */
	public static IMethod getTopmostMethod(IMethod method, ITypeHierarchy typeHierarchy, IProgressMonitor monitor) throws JavaModelException {

		assert method != null;

		ITypeHierarchy hierarchy = typeHierarchy;
		IMethod topmostMethod = null;
		final IType declaringType = method.getDeclaringType();
		if (!declaringType.isInterface()) {
			if ((hierarchy == null) || !declaringType.equals(hierarchy.getType())) {
				hierarchy = new OTTypeHierarchy(declaringType, declaringType.getJavaProject(), false);
				hierarchy.refresh(monitor);
			}
			if (hierarchy instanceof OTTypeHierarchy) {
				IMethod inInterface = isDeclaredInInterface(method, (OTTypeHierarchy) hierarchy, monitor);
				if (inInterface != null && !inInterface.equals(method))
					topmostMethod = inInterface;
			}
		}
		if (topmostMethod == null) {
			if (hierarchy == null) {
				hierarchy = new OTTypeHierarchy(declaringType, declaringType.getJavaProject(), false);
				hierarchy.refresh(monitor);
			}
			if (hierarchy instanceof OTTypeHierarchy) {
				IMethod overrides = overridesAnotherMethod(method, (OTTypeHierarchy) hierarchy, monitor);
				if (overrides != null && !overrides.equals(method))
					topmostMethod = overrides;
			}
		}
		return topmostMethod;
	}
	
	public static IField fieldIsShadowedInType(String elementName, String typeSignature, IType type) throws JavaModelException {
		IField field = type.getField(elementName);
		if (field.exists() && !field.getTypeSignature().equals(typeSignature)) {
			return field;
		}
		return null;
	}

	public static MethodDeclaration methodToDeclaration(IMethod method, CompilationUnit node) throws JavaModelException {
		ICompilationUnit methodCU = (ICompilationUnit) method.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (!methodCU.equals(node.getJavaElement())) {
			node = RefactoringASTParser.parseWithASTProvider(methodCU, true, null);
		}
		Name result = (Name) NodeFinder.perform(node, method.getNameRange());
		return (MethodDeclaration) getParent(result, MethodDeclaration.class);
	}

	public static AbstractMethodMappingDeclaration methodMappingToDeclaration(IMethodMapping methodMapping, CompilationUnit node) throws JavaModelException {
		Name result = (Name) NodeFinder.perform(node, methodMapping.getNameRange());
		return (AbstractMethodMappingDeclaration) getParent(result, AbstractMethodMappingDeclaration.class);
	}

	public static RefactoringStatus createNotYetFullyOTAwareMsg(String refactoringName) {
		return RefactoringStatus.createInfoStatus(Messages.format("The ''{0}'' Refactoring is not yet fully OT-aware!", new Object[] { refactoringName }));
	}

	@SuppressWarnings("rawtypes")
	private static ASTNode getParent(ASTNode node, Class parentClass) {
		do {
			node = node.getParent();
		} while (node != null && !parentClass.isInstance(node));
		return node;
	}

	public static ASTNode typeToDeclaration(IType type, CompilationUnit node) throws JavaModelException {
		Name result = (Name) NodeFinder.perform(node, type.getNameRange());
		if (type.isAnonymous())
			return getParent(result, AnonymousClassDeclaration.class);
		return getParent(result, AbstractTypeDeclaration.class);
	}

	public static RefactoringStatus checkForExistingRoles(String refactoringName,
			IJavaProject project, IProgressMonitor pm) {
		try {
			if(project == null){
				return createNotYetFullyOTAwareMsg(refactoringName);
				
			}
			if(getAllRoleClasses(project, pm).length > 0){
				return createNotYetFullyOTAwareMsg(refactoringName);
			}
		} catch (JavaModelException e) {
			return createNotYetFullyOTAwareMsg(refactoringName);
		}
		return new RefactoringStatus();
	}

}
