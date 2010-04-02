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
 * $Id: TypeHelper.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.ExceptionHandler;
import org.eclipse.objectteams.otdt.internal.core.InheritedMethodsRequestor;
import org.eclipse.objectteams.otdt.internal.core.OTType;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchyTraverser;

/**
 * @author jwloka
 * @version $Id: TypeHelper.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class TypeHelper
{
	public static final String JAVA_LANG_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	public static final String ORG_OBJECTTEAMS_TEAM = new String(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM);
	
    private static final String ICONFINED  = new String(IOTConstants.ICONFINED);
	private static final String CONFINED   = new String(IOTConstants.CONFINED);
	private static final String ILOWERABLE = new String(IOTConstants.ILOWERABLE);
	private static final String TSUPER_OT_TEAM = "TSuper__OT__Team"; //$NON-NLS-1$
    /**
     * Returns whether the given integer includes the <code>team</code> modifier.
     *
     * @return <code>true</code> if the <code>team</code> modifier is included
     */
    public static boolean isTeam(int flags)
    {
        return (flags & ClassFileConstants.AccTeam) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>role</code> modifier.
     *
     * @return <code>true</code> if the <code>role</code> modifier is included
     */
    public static boolean isRole(int flags)
    {
        return (flags & ExtraCompilerModifiers.AccRole) != 0;
    }

    /**
     * Returns all super types __including the given type__ for a given type.
     * The hierarchy is created in the sense of ordinary object inheritance.
     * 
     * The returned elements are arranged from the current to the top most 
     * parent.
     */
    public static IType[] getSuperTypes(IType type) throws JavaModelException
    {
        List<IType> parents = new LinkedList<IType>();

        if (type != null)
        {
            parents.add(type);

            // compute hierarchy and add all supertypes
            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
            parents.addAll(Arrays.asList(hierarchy.getAllSupertypes(type)));
        }

        return parents.toArray(new IType[parents.size()]);
    }

    /**
     * Returns the corresponding Java elements (ITypes) from the JavaModel of 
     * all super types __including the given type__ for a given role. The 
     * hierarchy is created in the sense of implicit inheritance.
     * 
     * The returned elements are arranged from the current to the top most 
     * parent.
     */
    public static IType[] getImplicitSuperTypes(IRoleType role) throws JavaModelException
    {
        List<IJavaElement>  parents = new LinkedList<IJavaElement>();

        while (role != null)
        {
            parents.add(role.getCorrespondingJavaElement());						
            role = getParentTeamRoleType(role);
        }

        return parents.toArray(new IType[parents.size()]);
    }

	/**
	 * Climbs up implicit hierarchy for a given role and returns corresponding
	 * role within the parent team.
	 * @return implicit parent role or null if role has no implicit inheritance
	 */
    protected static IRoleType getParentTeamRoleType(IRoleType role) throws JavaModelException
    {
        IRoleType result = null;

    	IType          teamType  = (IType)role.getTeam().getCorrespondingJavaElement();
        ITypeHierarchy hierarchy = teamType.newSupertypeHierarchy(new NullProgressMonitor());

        if (hierarchy != null)
        {
            IType parentTeam = hierarchy.getSuperclass(teamType);

            while (parentTeam != null && !parentTeam.getFullyQualifiedName().equals(JAVA_LANG_OBJECT))
            {
                result = getTeamRoleType(parentTeam, role);
                if (result != null)
                	return result;
                parentTeam = hierarchy.getSuperclass(parentTeam);
            }
        }

        return result;
    }

    /**
     * Tries to find a role in the given team with the same name
     * as the given role
     */
    protected static IRoleType getTeamRoleType(IType teamType, IRoleType role)
    	throws JavaModelException
    {
        IRoleType result   = null;
        IType[]   children = teamType.getTypes();

        if (children != null)
        {
			String roleName = role.getCorrespondingJavaElement().getElementName();

            // search in children for a role with the same simple name
            for (int idx = 0; idx < children.length; idx++)
            {
                if (children[idx].getElementType() == IJavaElement.TYPE
                    && children[idx].getElementName().equals(roleName))
                {
                    IOTType otType = OTModelManager.getOTElement(children[idx]);
                    
                    if ((otType != null) && (otType instanceof IRoleType))
                    {
                    	result = (IRoleType)otType; 
                    }
                    else
                    {
                    	//TODO(ak): role not present in OTM
                    	ExceptionHandler.getOTDTCoreExceptionHandler().logException(
                    		"Role " + roleName + " not present in OTM!", //$NON-NLS-1$ //$NON-NLS-2$
                    		new Exception("Unexpected model state!")); //$NON-NLS-1$
                    }
					break;
                }
            }
        }

        return result;
    }

	/**
	 * Find a role type within a given team. 
	 * Respect inline roles and role files.
	 * 
	 * @param teamType where to look
	 * @param roleName what to look for
	 * @return null in cases the type is not found
	 */
	public static IType findRoleType(IType teamType, String roleName) 
	{
		IType roleType = teamType.getType(roleName);
		
		// inline roles exit here:
		if (roleType.exists())
			return roleType;
		
		// look for a team package:
		// TODO(SH): does not yet account for all kinds of nesting!
		IJavaElement parent = teamType.getParent();
		if (parent.getElementType() == IJavaElement.COMPILATION_UNIT)
			parent = parent.getParent();
		if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) 
		{
			IPackageFragment enclosingPackage = (IPackageFragment)parent;
			IPackageFragmentRoot root = (IPackageFragmentRoot)enclosingPackage.getParent(); 
			try 
			{
				IPackageFragment teamPackage = root.getPackageFragment(enclosingPackage.getElementName()+'.'+teamType.getElementName());
				if (teamPackage.exists()) 
				{
					
					// found the team package, look for the role file:
					IJavaElement[] cus = teamPackage.getChildren();
					for (int i = 0; i < cus.length; i++) {
						if (cus[i].getElementType() == IJavaElement.COMPILATION_UNIT) {
							ICompilationUnit cu = (ICompilationUnit)cus[i];
							IJavaElement[] children = cu.getChildren();
							for (int j = 0; j < children.length; j++) {
								if (   children[j].getElementType() == IJavaElement.TYPE
									&& children[j].getElementName().equals(roleName))
								{
									return (IType)children[j];
								}
							}							
						}
					}
				}
			} catch (JavaModelException jme) {
				return null;
			}
		}
		return null;
	}

    /**
     * Find a role type nested within a given team.
     * @param teamType teamType where to look, e.g. <code>T1</code>.
     * @param qualRoleName role name relative to the given team, e.g. <code>"T1.T2.T3.R1"</code>.
     * @return found role type or <code>null</code>, e.g. <code>T1.T2.T3.R1</code>.
     */
    public static IType findNestedRoleType(IType teamType, String qualRoleName) 
    {
        if (teamType == null || qualRoleName == null)
        {
        	return null;
        }
    	String[] path = qualRoleName.split("\\."); //$NON-NLS-1$
        IType currType = null;
        for (int idx = 0; idx < path.length; idx++)
        {
			String currName = path[idx];
            if (currType != null)
            {
                currType = findRoleType(currType, currName);
            }
			else if (currType == null && teamType.getElementName().equals(currName))
            {
				currType = teamType;
            }
            else
            {
            	return null;
            }
		}
        
        return currType;
    }
    
    
    /**
     * Returns the method mapping element that binds the given method or
     * null, if there is none. Search scope is the role, that declares
     * the given method, in the given team or one of its super teams.
     * @param method
     * @param teamType
     * @return
     * @throws JavaModelException
     */
    //TODO(mkr) compare methods and method mappings w/o signature
    public static IMethodMapping getMethodMapping(IMethod method, IType teamType)
        throws JavaModelException
    {
        if ( (method == null) || (teamType == null) )
        {
            return null;
        }
        
        IType t = OTModelManager.getOTElement(method.getDeclaringType());
        IRoleType role = TypeHelper.getTeamRoleType(teamType, (IRoleType)t);
        if ( !(role instanceof IRoleType) )
        {
            return null;
        }
        
        String signature = TypeHelper.getMethodSignature(method);
        IRoleType[] roles = getImplicitSuperRoles(role);
        for (int roleIdx = 0; roleIdx < roles.length; roleIdx++)
        {
        	IRoleType curRole = roles[roleIdx];
            IMethodMapping[] mappings = curRole.getMethodMappings();
            for (int methIdx = 0; methIdx < mappings.length; methIdx++) {
				IMethodMapping mapping = mappings[methIdx];
                if (signature.equals(TypeHelper.getMethodSignature(mapping.getRoleMethod())))
                {
                	return mapping;
                }
			}
		}
        return null;
    }

	/**
     * Returns all super types __including the given type__ for a given role.
     * The hierarchy is created in the sense of ordinary object inheritance.
     * 
     * This method uses TypeHelper.getSuperTypes(), but for role types 
     * the supertype hierarchy needs to be build not on the IRoleType,
     * but on the corresponding java element.
     * 
     * The returned elements are arranged from the current to the top most 
     * parent.
     */
    public static IType[] getRoleSuperTypes(IRoleType role) throws JavaModelException
    {
        if (role != null)
        {
            IType type = (IType)((IRoleType)role).getCorrespondingJavaElement();
            return getSuperTypes(type);
        }
        else
        {
            return null;
        }
    }
    
    
    /**
     * Get all teams in the super type hierarchy for the specified team,
     * including the team itself. The returned elements are arranged
     * from the current to the top most parent.
     * This method is different from getSuperTypes(IType) using
     * hierarchy.getAllSuperclasses(IType) for accessing the
     * super type hierarchy. Also the type hierarchy is build not
     * on the team type, but on the corresponding Java element.
     */
    public static IType[] getSuperTeams(IType teamType) throws JavaModelException
    {
        List<IType> result = new LinkedList<IType>();

        if (teamType != null)
        {
            //Corresponding Java element needed for correct super hierarchy
            if (teamType instanceof OTType)
            {
                teamType = (IType)((OTType)teamType).getCorrespondingJavaElement();
            }
            result.add(teamType);

            ITypeHierarchy hierarchy =
                teamType.newSupertypeHierarchy(new NullProgressMonitor());
        
            IType[] superTeams = hierarchy.getAllSuperclasses(teamType);
            result.addAll(Arrays.asList(superTeams));
        }

        return result.toArray(new IType[result.size()]);
    }    
    
    
    /**
     * Gets implicit inherited methods of specified role of a team.
     * @param teamType The role's team
     * @param roleName Name of the Role
     * @return Array of role methods
     * @throws JavaModelException if creation of supertype hierarchy fails
     */    
    public static IMethod[] getInheritedRoleMethods(IType teamType, String roleName)
        throws JavaModelException
    {
        return getRoleMethods(teamType, roleName, true);
    }
    

    /**
     * Gets all methods of the specified role of a team.
     * @param teamType The role's team
     * @param roleName Name of the Role
     * @return Array of role methods
     * @throws JavaModelException if creation of supertype hierarchy fails
     */
    //TODO(mkr) find regular inherited methods via extends, too.
    public static IMethod[] getAllRoleMethods(IType teamType, String roleName)
        throws JavaModelException
    {
        return getRoleMethods(teamType, roleName, false);
    }
    
    /**
     * Gets all methods of the specified role of a team.
     * When only implicit inherited methods
     * @param teamType The role's team
     * @param roleName Name of the Role
     * @param onlySuperTeams ignore the given team
     * @return Array of role methods
     * @throws JavaModelException if creation of supertype hierarchy fails
     */
    public static IMethod[] getRoleMethods(IType teamType, String roleName, boolean onlySuperTeams)
        throws JavaModelException
    {
        Map<String, IMethod> result = new HashMap<String, IMethod>();
        
        IType[] teams = getSuperTeams(teamType);
        
        // for all superteams
        for (int teamsIdx = 0; teamsIdx < teams.length; teamsIdx++) {
            
            if (onlySuperTeams && teamsIdx == 0)
            {
                continue;
            }
            
            if (teams[teamsIdx].getFullyQualifiedName().compareTo(ORG_OBJECTTEAMS_TEAM) == 0)
            {
                break;
            }
            
            IType currentRole = OTModelManager.getOTElement(teams[teamsIdx]).getRoleType(roleName);
            if (currentRole != null)
            {
                IMethod[] roleMethods = currentRole.getMethods();
                for (int methIdx = 0; methIdx < roleMethods.length; methIdx++)
                {
                    // filter _OT$ methods
                    if (roleMethods[methIdx].getElementName().startsWith(IOTConstants.OT_DOLLAR))
                    {
                        continue;
                    }
                    
                    if (!roleMethods[methIdx].isConstructor())
                    {
                        String key = getMethodSignature(roleMethods[methIdx]);

                        if (!result.containsKey(key))
                        {
                            result.put(key, roleMethods[methIdx]);
                        }
                    }
                }
            }
        }
        
        return result.values().toArray(new IMethod[result.size()]);
    
    }

    /**
     * Returns ALL regular inherited (extends) methods of a role type.
     * If there is a role in the regular supertype hierarchy, the implicit
     * inherited methods are included.
     * @param role
     * @return
     * @throws JavaModelException
     */
    public static List<IMethod> getAllRegularInheritedMethods(IRoleType role)
        throws JavaModelException
    {
        LinkedList<IMethod> result = new LinkedList<IMethod>();
        IType[] superTypes = getRoleSuperTypes(role);
        for (int typeIdx = 1; typeIdx < superTypes.length; typeIdx++)
        {
            if( !(superTypes[typeIdx].getFullyQualifiedName().equals(JAVA_LANG_OBJECT)) )
            {
                IOTType curRole = OTModelManager.getOTElement(superTypes[typeIdx]);
                if ( (curRole != null) && (curRole instanceof IRoleType) )
                {
                    result.addAll(getAllImplicitInheritedMethods((IRoleType)curRole));
                }
                result.addAll(Arrays.asList(superTypes[typeIdx].getMethods()));
            }
        }
        return result;
    }
    
    //TODO(mkr) remove
    public static List<IMethod> getAllImplicitInheritedMethods(IRoleType role)
        throws JavaModelException
    {
        LinkedList<IMethod> result = new LinkedList<IMethod>();
        IType[] superRoles = getImplicitSuperRoles(role);
        for (int roleIdx = 1; roleIdx < superRoles.length; roleIdx++)
        {
            result.addAll(Arrays.asList(superRoles[roleIdx].getMethods()));
        }
        return result;    
    }
    
    //TODO(mkr) check dependencies with getAllRegularInheritedMethods()
    //TODO(mkr) this one needs more tests.
    public static IMethod[] getRoleMethodsComplete(IRoleType role)
        throws JavaModelException
    {
	    Map<String,IMethod> result = new HashMap<String,IMethod>();
	    List<IMethod> methods = new LinkedList<IMethod>();
	    IRoleType[] roles = getImplicitSuperRoles(role);
	    
	    // for all roles add declared role methods
	    for (int roleIdx = 0; roleIdx < roles.length; roleIdx++)
	    {
	        methods.addAll(Arrays.asList(roles[roleIdx].getMethods()));
	    }
	    
	    // for all roles add regular inherited methods
	    for (int roleIdx = 0; roleIdx < roles.length; roleIdx++)
	    {
	        methods.addAll(getAllRegularInheritedMethods(roles[roleIdx]));
	    }
	    
	    for (Iterator<IMethod> methIdx = methods.iterator(); methIdx.hasNext();)
	    {
	        IMethod meth = methIdx.next();
	
	        // Just in case
	        if (meth == null)
	        {
	            continue;
	        }
	
	        // filter _OT$ methods
	        if (meth.getElementName().startsWith(IOTConstants.OT_DOLLAR))
	        {
	            continue;
	        }
	        
	        // filter constructors and main()
	        if (meth.isConstructor() || meth.isMainMethod())
	        {
	            continue;
	        }
	        
	        String key = getMethodSignature(meth);
	        if (!result.containsKey(key))
	        {
	            result.put(key, meth);
	        }
	    }
	    
	    return result.values().toArray(new IMethod[result.size()]);
	}
    
    /**
     * Returns all implicit super role classes __including__ the given role
     * for a given role. This is similar to
     * <code>TypeHelper.getImplicitSuperType()</code>,
     * except the result is typed different and the supertype hierarchy of
     * the enclosing team is created only once.
     * The returned elements are arranged from the current to the top most 
     * parent.
     * @param role
     * @return Roles in the type hierarchy of the enclosing team with the same
     *         name as the given role.
     * @throws JavaModelException
     */
    public static IRoleType[] getImplicitSuperRoles(IRoleType role)
        throws JavaModelException 
    {

        List<IRoleType> result = new LinkedList<IRoleType>();
        IType teamType = role.getTeam();
        IType[] teams  = getSuperTeams(teamType); 
        
        // for all superteams
        for (int teamsIdx = 0; teamsIdx < teams.length; teamsIdx++) {
            
            if (teams[teamsIdx].getFullyQualifiedName().compareTo(ORG_OBJECTTEAMS_TEAM) == 0)
            {
                break;
            }
            else
            {
                IRoleType superRole =
                    TypeHelper.getTeamRoleType(teams[teamsIdx], role);
                if (superRole != null)
                {
                    result.add(superRole);
                }
            }
        }
        return result.toArray(new IRoleType[result.size()]);
    }
    
    
    /**
     * Gets only inherited role types for a specified team.
     * @param teamType Team
     * @return Array of found role types
     * @throws JavaModelException if creation of type hierarchy fails
     */
    public static IRoleType[] getInheritedRoleTypes(IType teamType)
        throws JavaModelException
    {
        return getRoleTypes(teamType, true);
    }

    
    /**
     * Gets inherited and declared role types for a specified team.
     * @param teamType Team
     * @return Array of found role types
     * @throws JavaModelException if creation of type hierarchy fails
     */
    public static IRoleType[] getAllRoleTypes(IType teamType)
        throws JavaModelException
    {
        return getRoleTypes(teamType, false);
    }
    
    
    /**
     * Gets role types for a specified team.
     * @param teamType Team
     * @param onlySuperTeams wether to collect only implicitly inherited roles
     * @return Array of found role types
     * @throws JavaModelException if creation of type hierarchy fails
     */
    public static IRoleType[] getRoleTypes(IType teamType, boolean onlySuperTeams)
        throws JavaModelException
    {
        Map<String, IOTType> result = new HashMap<String, IOTType>();
        IType[] roles = null;
        IType currentRole = null;

        IType[] superTeams = getSuperTeams(teamType); 
        
        for (int teamsIdx = 0; teamsIdx < superTeams.length; teamsIdx++) 
        {
            if (onlySuperTeams && teamsIdx == 0)
            {
                continue;
            }
            
            if (superTeams[teamsIdx].getFullyQualifiedName().compareTo(ORG_OBJECTTEAMS_TEAM) == 0)
            {
                break;
            }

            // find implicit roles of superteams
            roles = OTModelManager.getOTElement(superTeams[teamsIdx]).getRoleTypes();
            for (int roleIdx = 0; roleIdx < roles.length; roleIdx++) 
            {
                currentRole = roles[roleIdx];
                
                // when handling binary types, ignore OT elements
                if (currentRole.isBinary())
                {          
                    if (currentRole.isInterface())
                    {
                        String name = currentRole.getElementName();
                        // ignore OT specific interaces
                        if (name.equals(TSUPER_OT_TEAM)
                             || name.equals(ILOWERABLE)
                             || name.equals(CONFINED)
                             || name.equals(ICONFINED))
                        {                            
                            continue;
                        }
                    }
                    else
                    {
                        continue; // ignore binary OT_ classes
                    }
                }

                if (!result.containsKey(currentRole.getElementName())) 
                {
                    result.put(currentRole.getElementName(),
                               OTModelManager.getOTElement(currentRole));
                }
            }
        }
        
        return result.values().toArray(new IRoleType[result.size()]);
    }

    /**
     * Returns the method signature of the specified <code>IMethod</code>.
     * @param method an <code>IMethod</code>
     * @return methodname and parameter types, e.g. "myMethod(String, Object)".
     */
    public static String getMethodSignature(IMethod method)
    {
        StringBuffer result = new StringBuffer();
    
        if (method != null)
        {
            result.append(method.getElementName());
            result.append('(');
            
            String[] parameterTypes = method.getParameterTypes();
            for (int idx = 0; idx < parameterTypes.length; idx++)
            {
                String curType = Signature.toString(parameterTypes[idx]);
                result.append(curType);
                if (idx < parameterTypes.length - 1)
                {
                    result.append(", "); //$NON-NLS-1$
                }
            }
            result.append(')');
        }
    
        return result.toString();
    }
    
    @SuppressWarnings("unchecked") // uses method.parameters(), which returns a raw List.
	public static String getMethodSignature(MethodDeclaration method)
    {
    	StringBuffer result = new StringBuffer();
    	
    	if (method != null)
    	{
    		result.append(method.getName().getIdentifier());
    		result.append('(');
    		
    		Iterator params = method.parameters().iterator();
    		while (params.hasNext())
    		{
    			SingleVariableDeclaration param = (SingleVariableDeclaration) params.next();
    			result.append(param.getType().resolveBinding().getName());
    			if (params.hasNext())
    			{
    				result.append(", "); //$NON-NLS-1$
    			}
    		}
    		result.append(')');
    	}
    	return result.toString();
    }
    

	public static IMethod[] getInheritedMethods(IOTType iotType, 
			boolean includeFocusType,
			boolean includeRootClass,
			boolean checkVisibility,
			IProgressMonitor pm) throws JavaModelException
	{
		return getInheritedMethods((IType)iotType.getCorrespondingJavaElement(), 
				includeFocusType,
				includeRootClass,
				checkVisibility,
				pm);
	}
	
	public static IMethod[] getInheritedMethods(IType type, 
			boolean includeFocusType,
			boolean includeRootClass,
			boolean checkVisibility, 
			IProgressMonitor pm )throws JavaModelException
	{
		InheritedMethodsRequestor requestor = new InheritedMethodsRequestor(type, false, checkVisibility);
    	OTTypeHierarchyTraverser traverser = new OTTypeHierarchyTraverser(
    			requestor,
				OTTypeHierarchyTraverser.SUPER_HIERARCHY,
				OTTypeHierarchyTraverser.TRAVERSE_IMPLICIT_FIRST,
				includeFocusType, 
				includeRootClass,
				pm);
    	
    	traverser.traverse();
		return requestor.getResult();
	}

	/**
	 * Returns something like somepackage.OuterTeam$__OT__InnerTeam$Role for the given type
	 * @throws JavaModelException
	 */
	public static String getQualifiedRoleSplitName(IType type) throws JavaModelException
	{
        IType currentType = type;
        StringBuffer result = new StringBuffer();

        while (currentType != null) {
            result.insert(0, currentType.getElementName());
            
	        if (currentType.isClass()) {
	            int flags = currentType.getFlags();
	            if (/*Flags.isTeam(flags) &&*/ Flags.isRole(flags))
	                result.insert(0, IOTConstants.OT_DELIM_NAME);
	        }
	        currentType = currentType.getDeclaringType();
	        if (currentType != null) // enclosing available, insert separator
	            result.insert(0, '$');
	    }
        
    	String packageName = type.getPackageFragment().getElementName();
    	if (packageName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
    		return result.toString();
    	}
    	
    	return packageName + '.' + result.toString();
	}

    public static boolean isOrgObjectTeamsTeam(IType type)
    {
        return CharOperation.equals(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM, type.getFullyQualifiedName().toCharArray());
    }
}

