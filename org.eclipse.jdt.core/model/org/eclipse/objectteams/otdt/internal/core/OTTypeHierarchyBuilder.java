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
 * $Id: OTTypeHierarchyBuilder.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.PhantomType;

/**
 * This class builds the OTTypeHierarchy.
 *
 * If the focus type is a role, the hierarchy is created by creating and traversing
 * the typehierarchy of the enclosing team. During the traversal of the enclosing
 * team hierarchy, all infos about roles are collected in CopyInheritanceInfos.
 * These infos are used build the hierarchy.
 * 
 * If the focus type is not a role, the creation is delegated to the normal
 * TypeHierarchy via a TypeHierarchyConnector.
 * 
 * The subtypehierarchy is created by propagating CopyInheritanceInfos
 * downwards the enclosing team hierarchy.
 * 
 * OTPotentialSubTypesFinder and MostOuterTeamSetBuilder
 * are no longer needed.
 * 
 * @author Michael Krueger (mkr)
 * @version $Id: OTTypeHierarchyBuilder.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTTypeHierarchyBuilder
{
    public static final int INIT_SIZE = 23; //FIXME(mkr) choose initial size
    protected OTTypeHierarchy _hierarchy;
    protected OTTypeHierarchy _enclosingTeamHierarchy;
    protected IType _focusType;
        
    public OTTypeHierarchyBuilder(OTTypeHierarchy hierarchy)
    {
        this._hierarchy = hierarchy;
        this._focusType = this._hierarchy.getType();
    }
    
    /**
     * Builds OTTypeHierarchy for roles or non-roles with or without subtypes. 
     * @param computeSubTypes
     * @throws JavaModelException
     */
    public void build(boolean computeSubTypes) throws JavaModelException
    {
        try
        {
            this._hierarchy.initialize(INIT_SIZE);
            
            if (OTTypeHierarchyHelper.isRole(this._focusType))
            {
                // focus type is a role
                buildOTTypeHierarchy(computeSubTypes);
                if (computeSubTypes)
                {
                    buildOTSubtypeHierarchyForRoleType();
                }
            }
            else
            {
                // focus type is not a role
                buildTypeHierarchy(computeSubTypes);
                if (computeSubTypes)
                {
                    // focus type may have roles as subtypes.
                    buildOTSubtypeHierarchy(this._focusType);
                }
            }
            
            this._hierarchy._hierarchyCache.cacheOTTypeHierarchy(this._hierarchy);
        }
        finally
        {
            //TODO(mkr) cleanup
        }
    }

    protected void buildOTTypeHierarchy(boolean computeSubtypes) throws JavaModelException
    {
    	IType enclosingType =
    		  ((IRoleType)OTModelManager.getOTElement(this._focusType)).getTeamJavaType();
    	if (enclosingType == null)
    		throw new JavaModelException(new JavaModelStatus(IStatus.ERROR, "Enclosing team not found for "+this._focusType.getElementName()+" perhaps this element is not on the build path?"));
        this._enclosingTeamHierarchy = new OTTypeHierarchy(enclosingType, enclosingType.getJavaProject(), computeSubtypes);
        this._enclosingTeamHierarchy.refresh(new NullProgressMonitor());
        traverseEnclosingTeamHierarchy(enclosingType);    
    }
    
    /**
     * Builds the normal TypeHierarchy.
     * @param computeSubtypes
     * @throws JavaModelException
     */
    protected void buildTypeHierarchy(boolean computeSubtypes) throws JavaModelException
    {
        TypeHierarchyConnector connector = this._hierarchy.getProject() != null
             ? new TypeHierarchyConnector(this._hierarchy.getType(), this._hierarchy.getProject(), computeSubtypes)
        	 : new TypeHierarchyConnector(this._hierarchy.getType(), this._hierarchy.scope, computeSubtypes);
        connector.refresh(new NullProgressMonitor());
        this._hierarchy.connect(connector);
    }
    
    protected CopyInheritanceInfo traverseEnclosingTeamHierarchy(IType teamType)
            throws JavaModelException
    {
        if (teamType.equals(this._enclosingTeamHierarchy.ORG_OBJECTTEAMS_TEAM)
            || teamType.equals(this._enclosingTeamHierarchy.JAVA_LANG_OBJECT))
        {
            return null;
        }
                
//FIXME(mkr) Use cached CopyInheritanceInfos or better remove commented code.
// Right now using the cached infos
// results to incomplete hierarchies (missing tsupers), because implicit
// inheritance is written in the hierarchy during creation of CopyInheritanceInfos.
//
//        CopyInheritanceInfo info = _enclosingTeamHierarchy.getCopyInheritanceInfo(teamType);
//        if (info != null)
//        {
//            info.updateHierarchy(_hierarchy);
//            return info;
//        }
        CopyInheritanceInfo info = new CopyInheritanceInfo(this._hierarchy, teamType);        
                
        IType[] roles = getDeclaredRoles(teamType);

        // Add declared roles to CopyInheritanceInfo
        if (roles != null)
        {
            info.addDeclaredRoles(roles);
        }

        // The Team might be a role with implicit supertypes.
        // Traverse the team's implicit supertypes to find the implicit
        // super roles (tsuperN..tsuper1) of each role in the team.
        IType[] tsuperTypes = this._enclosingTeamHierarchy.getTSuperTypes(teamType);
        for (int idx = 0; idx < tsuperTypes.length; idx++) {
            IType tsuperType = tsuperTypes[idx];
            CopyInheritanceInfo superInfo = traverseEnclosingTeamHierarchy(tsuperType);
            info.inherit(superInfo);
        }
        
        // Traverse the team's explicit supertype to find the implicit
        // super role (tsuper0) of each role in the team.
        IType superclass = this._enclosingTeamHierarchy.getExplicitSuperclass(teamType);
        if (superclass != null)
        {
            CopyInheritanceInfo superInfo = traverseEnclosingTeamHierarchy(superclass);
            info.inherit(superInfo);
        }
                        
        // Supertypes of the team's declared roles
        if (roles != null)
        {
            info.addDeclaredInheritance(roles);
        }
        
        info.updateHierarchy();
        //FIXME(mkr) Why enclosing Hierarchy?
        this._enclosingTeamHierarchy.setCopyInheritanceInfo(teamType, info);
        
        return info;       
    }

    /**
     * Builds Subtypehierarchy for non-roles.
     * @param type
     * @throws JavaModelException
     */
    protected void buildOTSubtypeHierarchy(IType type) throws JavaModelException
    {
        // type is not a role, _enclosingTeamHierarchy doesn't exist.
        // subtypes might be roles.
        IType[] subtypes = this._hierarchy.getSubtypes(type);
        for (int idx = 0; idx < subtypes.length; idx++)
        {
            IType subtype = subtypes[idx];
            if (OTTypeHierarchyHelper.isRole(subtype))
            {
                OTTypeHierarchy additional = new OTTypeHierarchy(subtype, subtype.getJavaProject(), true);
                additional.refresh(new NullProgressMonitor());

                this._hierarchy.integrate(additional);
            }
            else
            {
                buildOTSubtypeHierarchy(subtype);
            }            
        }
        
    }

    /**
     * Builds the Subtypehierarchy for roles.
     * @throws JavaModelException
     */
    protected void buildOTSubtypeHierarchyForRoleType() throws JavaModelException
    {
        // _focusType is a role, _enclosingTeamHierarchy exists.
        // subtypes are definitely roles.
        IType encTeam = this._enclosingTeamHierarchy.getFocusType();
        traverseDown(null, encTeam);
        
    }
    
    /**
     * Propagates CopyInheritanceInfos down the enclosing Teamhierarchy
     * and builds the subtypehierarchy for roles. 
     * @param encSuper Superclass of enclosing team
     * @param encType enclosing team
     * @throws JavaModelException
     */
    protected void traverseDown(IType encSuper, IType encType) throws JavaModelException
    {
        if (encSuper != null)
        {
            CopyInheritanceInfo superInfo = this._enclosingTeamHierarchy.getCopyInheritanceInfo(encSuper);
            CopyInheritanceInfo typeInfo  = this._enclosingTeamHierarchy.getCopyInheritanceInfo(encType);
            if (typeInfo == null)
            {
                typeInfo = new CopyInheritanceInfo(this._hierarchy, encType);
                IType[] roles = getDeclaredRoles(encType);

                // Add declared roles to CopyInheritanceInfo
                if (roles != null)
                {
                    typeInfo.addDeclaredRoles(roles);
                }
                typeInfo.inherit(superInfo);

                // Supertypes of the team's declared roles
                if (roles != null)
                {
                    typeInfo.addDeclaredInheritance(roles);
                }
                
                typeInfo.updateHierarchy();
                
                this._enclosingTeamHierarchy.setCopyInheritanceInfo(encType, typeInfo);                
            }
        }

        IType[] subtypes = this._enclosingTeamHierarchy.getSubtypes(encType);
        for (int idx = 0; idx < subtypes.length; idx++)
        {
            IType encSub = subtypes[idx];
            traverseDown(encType, encSub);                
        }
    }
        
    private static IType[] getDeclaredRoles(IType teamType) throws JavaModelException
    {
        if (teamType instanceof PhantomType)
        {
            return null;
        }
        else
        {
            IOTType iot = OTModelManager.getOTElement(teamType);        
            assert(iot != null && iot.isTeam());
            
            return iot.getRoleTypes();
        }
    
    }

}
