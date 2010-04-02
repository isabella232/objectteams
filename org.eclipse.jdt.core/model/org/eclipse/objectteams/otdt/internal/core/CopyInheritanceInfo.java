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
 * $Id: CopyInheritanceInfo.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.PhantomType;


/**
 * @author Michael Krueger (mkr)
 * @version $Id: CopyInheritanceInfo.java 23416 2010-02-03 19:59:31Z stephan $
 */
//TODO(mkr) rename to TeamInfo
class CopyInheritanceInfo
{
    // role name -> declared role type or phantom role type
	private Map<String, IType> _roles = new HashMap<String, IType>();
    
    // role name -> explicit superrole name or explicit superclass type
	private Map<String, Object> _superclasses = new HashMap<String, Object>();
    
    // role name -> list of superinterface role names or superinterface types (nested elements are either String or IType)
	private Map<String, List<Object>> _superInterfaces = new HashMap<String, List<Object>>();

    private OTTypeHierarchy _hierarchy;
    private IType _team;
	    
	public CopyInheritanceInfo(OTTypeHierarchy hierarchy, IType teamType)
	{
		_hierarchy = hierarchy;
		_team = teamType;
	}
    
    /**
     * Adds the given roles as declared roles to this CopyInheritenceInfo.
     * This should happen before inheriting from other CopyInheritanceInfos.
     * @param roles
     * @throws JavaModelException
     */
	protected void addDeclaredRoles(IType[] roles) throws JavaModelException
	{
	    for (int idx = 0; idx < roles.length; idx++)
	    {
	        IType role = roles[idx];
	        _roles.put(role.getElementName(), role);
            if(!role.isInterface())
            {
                // initial superclass, might get changed later
                if (OTTypeHierarchyHelper.isTeam(role))
                {
                	// FIXME(SH): adjust for Trac #144:
                    _superclasses.put(role.getElementName(),
                                      _hierarchy.ORG_OBJECTTEAMS_TEAM);
                    _hierarchy.addRootClass(_hierarchy.ORG_OBJECTTEAMS_TEAM);
                }
                else
                {
                    _superclasses.put(role.getElementName(),
                                      _hierarchy.JAVA_LANG_OBJECT);
                    _hierarchy.addRootClass(_hierarchy.JAVA_LANG_OBJECT);
                }
            }
	    }      
	}

    /**
     * @param roles
     * @throws JavaModelException
     */
	protected void addDeclaredInheritance(IType[] roles) throws JavaModelException
	{
	    for (int roleIdx = 0; roleIdx < roles.length; roleIdx++)
	    {
	        IType role = roles[roleIdx];
	        String roleName = role.getElementName();
	        TypeHierarchyConnector connector = null;
	        
            // add declared superclass
	        if (!role.isInterface())
	        {
	            //FIXME(mkr) problem with binary types (qualified name)?
	            String superclassName = role.getSuperclassName();
	            if (superclassName != null)
	            {
	                if (_roles.containsKey(superclassName))
	                {
	                    _superclasses.put(roleName, superclassName);
	                }
	                else
	                {
	                    if (connector == null)
	                    {
	                        connector =
	                            _hierarchy.getTypeHierarchyConnector(role);                  
	                        _hierarchy.connect(connector);
	                    }
	                    IType superclassType =
	                        connector.getSuperclass(role);
	                    _superclasses.put(roleName, superclassType);
	                }                               
	            }
	        }
	        
            // add declared super interfaces            
	        String[] superInterfaceNames = role.getSuperInterfaceNames();
	        
	        List<Object> existantSuperInterfaces = _superInterfaces.get(roleName);
	        if (existantSuperInterfaces == null && superInterfaceNames.length > 0)
	        {
	            existantSuperInterfaces = new LinkedList<Object>();
	            _superInterfaces.put(roleName, existantSuperInterfaces);
	        }
            
	        for (int idx = 0; idx < superInterfaceNames.length; idx++)
	        {
	            String superInterfaceName = superInterfaceNames[idx];
	            
	            if (_roles.containsKey(superInterfaceName))
	            {
	                if (!existantSuperInterfaces.contains(superInterfaceName))
	                {
	                    existantSuperInterfaces.add(superInterfaceName);
	                }
	            }
	            else
	            {
	                if (connector == null)
	                {
	                    connector =
	                        _hierarchy.getTypeHierarchyConnector(role);
	                    _hierarchy.connect(connector);
	                }
	                
	                IType[] superInterfaceTypes =
	                    connector.getSuperInterfaces(_roles.get(roleName));
	                for (int interfaceIdx = 0; interfaceIdx < superInterfaceTypes.length; interfaceIdx++)
	                {
	                    IType superInterfaceType = superInterfaceTypes[interfaceIdx];
	                    if (superInterfaceType.getElementName().equals(superInterfaceName))
	                    {
	                        existantSuperInterfaces.add(superInterfaceType);
	                        break;
	                    }                    
	                }
	            }
	        }
	    }
	}
    
	protected void inherit(CopyInheritanceInfo superInfo) throws JavaModelException
	{
        if (superInfo != null)
        {
            addInheritedRoles(superInfo._roles);
            addInheritedSuperClasses(superInfo._superclasses);
            addInheritedSuperInterfaces(superInfo._superInterfaces);
        }
	}
		
    
    protected void updateHierarchy()
    {
        for (Iterator<String> roleIter = _roles.keySet().iterator(); roleIter.hasNext();)
        {
            String roleName = roleIter.next();
            IType role = _roles.get(roleName);
            
            IType superclass = getSuperclass(roleName);
            if (superclass != null)
            {
                _hierarchy.cacheSuperclass(role, superclass);
            }

            IType[] superInterfaces = getSuperInterfaces(roleName);
            if (superInterfaces != null)
            {                
                _hierarchy.cacheSuperInterfaces(role, superInterfaces);
            }
        }
    }
    
    protected void updateHierarchy(OTTypeHierarchy hierarchy)
    {
        for (Iterator<String> roleIter = _roles.keySet().iterator(); roleIter.hasNext();)
        {
            String roleName = roleIter.next();
            IType role = _roles.get(roleName);
            
            IType superclass = getSuperclass(roleName);
            if (superclass != null)
            {
                hierarchy.cacheSuperclass(role, superclass);
            }

            IType[] superInterfaces = getSuperInterfaces(roleName);
            if (superInterfaces != null)
            {                
                hierarchy.cacheSuperInterfaces(role, superInterfaces);
            }
        }
    }
    
    
    /**
     * Adds inherited roles as PhantomTypes to the roles of this
     * CopyInheritanceInfo, unless there is already a declared role
     * with the same name.
     * @param tsuperRoles
     * @throws JavaModelException
     */
	private void addInheritedRoles(Map<String, IType> tsuperRoles) throws JavaModelException
	{        
	    for (Iterator<String> iter = tsuperRoles.keySet().iterator(); iter.hasNext();)
	    {
	        String roleName = iter.next();
            IType tsuperRole = tsuperRoles.get(roleName);

            IType localRoleType = null;
            if (_roles.containsKey(roleName))
            {
                localRoleType = _roles.get(roleName);
            }
            else
	        {
                localRoleType = new PhantomType(_team, tsuperRole);
	            _roles.put(roleName, localRoleType);
	        }

            _hierarchy.addTSupertype(localRoleType, tsuperRole);
            
            // update the real type of the PhantomType wrapper
            if (localRoleType instanceof PhantomType)
            {
                ((PhantomType)localRoleType).setRealType(tsuperRole);
            }
        }        
	}
    
    private  void addInheritedSuperClasses(Map<String, Object> superclasses)
    {
        // overwrite existant superclasses
        _superclasses.putAll(superclasses);
    }
    
    private void addInheritedSuperInterfaces(Map<String, List<Object>> superInterfaces)
    {        
        // add new interfaces to existant super interfaces
        for (Iterator<String> iter = superInterfaces.keySet().iterator(); iter.hasNext();)
        {
            String roleName = iter.next();
            List<Object> existantSuperInterfaces = _superInterfaces.get(roleName);
            if (existantSuperInterfaces == null)
            {
                existantSuperInterfaces = new LinkedList<Object>();
                _superInterfaces.put(roleName, existantSuperInterfaces);
            }
            //FIXME(mkr) filter entries
            existantSuperInterfaces.addAll(superInterfaces.get(roleName));
        }
    }

    private IType getSuperclass(String roleName)
    {
        IType result = null;
        Object superclassObj = _superclasses.get(roleName);
        if (superclassObj instanceof String)
        {
            String superclassName = (String)superclassObj;
            result = _roles.get(superclassName);
        }
        else
        {
            result = (IType)superclassObj;
        }
        
        return result;
    }
    
    private IType[] getSuperInterfaces(String roleName)
    {
        List<Object> superInterfaces = _superInterfaces.get(roleName);
        if (superInterfaces == null)
        {
            return null;
        }
        
        IType[] result = new IType[superInterfaces.size()];
        int idx = 0;
        for (Iterator<Object> iter = superInterfaces.iterator(); iter.hasNext();)
        {
            Object superInterfaceObj = iter.next();
            if (superInterfaceObj instanceof String)
            {
                String superInterfaceName = (String)superInterfaceObj;
                IType superInterfaceType = _roles.get(superInterfaceName);
                result[idx++] = superInterfaceType;
            }
            else
            {
                IType superInterfaceType = (IType)superInterfaceObj;
                result[idx++] = superInterfaceType;
            }            
        }
        
        return result;
    }
    
    @SuppressWarnings("nls")
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(_team.getElementName() + "\n");
		buffer.append("Roles...\n");
		for (Iterator<String> iter = _roles.keySet().iterator(); iter.hasNext();)
        {
            String roleName = iter.next(); 
			IType role = _roles.get(roleName);
			buffer.append("- ");
            buffer.append(role.getFullyQualifiedName('.'));            
			buffer.append("\n");            
		}
				
		buffer.append("Superclasses...\n");
		for (Iterator<String> iter = _superclasses.keySet().iterator(); iter.hasNext();)
		{
            String roleName = iter.next(); 
			Object superclass = _superclasses.get(roleName);
            if (superclass instanceof String)
            {
				buffer.append("  ");
				buffer.append(roleName);
				buffer.append(" -> ");
				buffer.append((String)superclass);
				buffer.append("\n");   
                
            }
            else if (superclass instanceof IType)
            {
                buffer.append("  ");
                buffer.append(roleName);
                buffer.append(" -> ");
                buffer.append(((IType)superclass).getFullyQualifiedName('.'));
                buffer.append("\n");       
            }
		}
		
		buffer.append("Superinterfaces...\n");
		for (Iterator<String> iter = _superInterfaces.keySet().iterator(); iter.hasNext();)
        {
			String roleName = iter.next();
            
			List<Object> superInterfaces = _superInterfaces.get(roleName);
			for (int idx = 0; idx < superInterfaces.size(); idx++)
            {
				Object superInterface = superInterfaces.get(idx);
                if (superInterface instanceof String)
                {
                    buffer.append("  ");
                    buffer.append(roleName);
                    buffer.append(" -> ");
                    buffer.append((String)superInterface);
                    buffer.append("\n");   
                    
                }
                else if (superInterface instanceof IType)
                {
                    buffer.append("  ");
                    buffer.append(roleName);
                    buffer.append(" -> ");
                    buffer.append(((IType)superInterface).getFullyQualifiedName('.'));
                    buffer.append("\n");           
                }
			}   
		}
		
		return buffer.toString();
	}

    
}