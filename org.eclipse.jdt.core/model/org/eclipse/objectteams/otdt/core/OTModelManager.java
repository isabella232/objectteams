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
 * $Id: OTModelManager.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.objectteams.otdt.core.util.MethodData;
import org.eclipse.objectteams.otdt.internal.core.BinaryRoleType;
import org.eclipse.objectteams.otdt.internal.core.CallinMapping;
import org.eclipse.objectteams.otdt.internal.core.CalloutMapping;
import org.eclipse.objectteams.otdt.internal.core.CalloutToFieldMapping;
import org.eclipse.objectteams.otdt.internal.core.MappingElementInfo;
import org.eclipse.objectteams.otdt.internal.core.OTJavaElement;
import org.eclipse.objectteams.otdt.internal.core.OTModel;
import org.eclipse.objectteams.otdt.internal.core.OTType;
import org.eclipse.objectteams.otdt.internal.core.RoleFileType;
import org.eclipse.objectteams.otdt.internal.core.RoleType;


/**
 * Manager provides connection between JavaModel and OTM.
 * 
 * @author jwloka
 * @version $Id: OTModelManager.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTModelManager
{
	private final static OTModel MAPPING = OTModel.getSharedInstance();
	
	private static OTModelManager _singleton;

    private OTModelReconcileListener _reconcileListener;
	
	protected OTModelManager()
	{
		_singleton = this;

		_reconcileListener = new OTModelReconcileListener();
		JavaCore.addElementChangedListener(_reconcileListener, ElementChangedEvent.POST_RECONCILE);
	}
	
	public static OTModelManager getSharedInstance()
	{
		if (_singleton == null)
		{
			new OTModelManager();
		}
		
		return _singleton;
	}
	
	public static void dispose()
	{
	    if (_singleton != null)
	    {
	        JavaCore.removeElementChangedListener(_singleton._reconcileListener);
	        OTModel.dispose();
	    }
	    
	    _singleton = null;
	}

	/** Register a type of which we don't yet have the flags. */
	public void addUnopenedType(IType type) {
		MAPPING.addUnopenedType(type);
	}

	/**
	 * Add the propriate Object Teams Model element for a given IType 
	 */
	public IOTType addType(IType elem, int typeDeclFlags, String baseClassName, String baseClassAnchor, boolean isRoleFile)
	{
		IJavaElement parent = elem.getParent();
		IOTType result = null; 
				
		switch (parent.getElementType())
		{
		    case IJavaElement.COMPILATION_UNIT:
		    case IJavaElement.CLASS_FILE:
		    	if(isRoleFile)
				{   //  could also be a teeam, which is handled inside the constructor
		    		if (elem.isBinary())
		    		{
			        	MAPPING.addOTElement( result = new BinaryRoleType(elem, 
								parent, 
								typeDeclFlags, 
								baseClassName,
								baseClassAnchor));
		    		}
		    		else
		    		{
			        	MAPPING.addOTElement( result = new RoleFileType(elem, 
								parent, 
								typeDeclFlags, 
								baseClassName,
								baseClassAnchor));
		    		}
				} 
		    	else if (TypeHelper.isTeam(typeDeclFlags))
				{
					MAPPING.addOTElement( result = new OTType(OTJavaElement.TEAM, elem, null, typeDeclFlags) );
				}
				break;
		    case IJavaElement.TYPE:
				IType   encType   = (IType)parent;
				IOTType otmParent = MAPPING.getOTElement(encType); 
					
				result = maybeAddRoleType(elem, otmParent, typeDeclFlags, baseClassName, baseClassAnchor);			
	    		break;
	    	//do nothing if anonymous type
	    	case IJavaElement.METHOD:	    		
	    		break;
	    	case IJavaElement.INITIALIZER:
	    		break;
	    	case IJavaElement.FIELD:
				break;
//TODO (jwl) Wether anonymous types are roles or not will be discoverable with 
//	    	 a future implementation (probably with the help of a newer version of the compiler)
	    		
//		    	case IJavaElement.METHOD:
//	    	    IMethod encMethod   = (IMethod)parent;
//	    	    otmParent = MAPPING.getOTElement(encMethod.getDeclaringType());
//	    	    
//	    	    addRoleType(elem, otmParent, typeDeclFlags, baseClassName);
//	    	    break;
//		    case IJavaElement.INITIALIZER:
//	    	    IInitializer encInitializer   = (IInitializer)parent;
//	    	    otmParent = MAPPING.getOTElement(encInitializer.getDeclaringType());
//	    	    
//	    	    addRoleType(elem, otmParent, typeDeclFlags, baseClassName);
//	    	    break;
//	    	case IJavaElement.FIELD:
//	    	    IField encField   = (IField)parent;
//	    	    otmParent = MAPPING.getOTElement(encField.getDeclaringType());
//	    	    
//	    	    addRoleType(elem, otmParent, typeDeclFlags, baseClassName);
//	    	    break;
		    default:
		    	new Throwable("Warning: unexpected parent for OT element: " + parent).printStackTrace(); //$NON-NLS-1$
		    	break;
		}
		return result;
	}

	private IOTType maybeAddRoleType(IType elem, IOTType otmParent, 
	        int typeDeclFlags, String baseClassName, String baseClassAnchor)
    {
		IOTType result = null;
        if ((otmParent != null) 
        	&& (TypeHelper.isTeam(otmParent.getFlags()) 
        		|| (TypeHelper.isRole(otmParent.getFlags())) ) )
        {				
        	MAPPING.addOTElement( result = new RoleType(elem, 
        	        							otmParent, 
        	        							typeDeclFlags, 
        	        							baseClassName,
        	        							baseClassAnchor));
        }
        return result;
    }

	
    /**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
    public ICallinMapping addCallinBinding(IType role, MappingElementInfo info)
	{
		IOTType otmRole = MAPPING.getOTElement(role);
		
		if ((otmRole != null) && (otmRole instanceof IRoleType))
		{
//{OTModelUpdate
		    MethodData corrRoleMethData = info.getRoleMethod();
		    IMethod correspondingRoleMethod = 
		        role.getMethod(corrRoleMethData.getSelector(), 
		                       corrRoleMethData.getArgumentTypes()); 
//haebor}							   
			return new CallinMapping(info.getDeclarationSourceStart(),
						      info.getSourceStart(),
						      info.getSourceEnd(),
							  info.getDeclarationSourceEnd(),
							  (IRoleType)otmRole,
//{OTModelUpdate
//orig:	(IMethod) otmRole.getParent(),
						      correspondingRoleMethod,
//haebor}							           
							  info.getCallinName(),
							  info.getCallinKind(),
							  info.getRoleMethod(),
							  info.getBaseMethods(), info.hasSignature());   
		}
		
		return null;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public ICalloutMapping addCalloutBinding(IType role, MappingElementInfo info)
	{
		IOTType otmRole = MAPPING.getOTElement(role);
		
		if ((otmRole != null) && (otmRole instanceof IRoleType))
		{
//{OTModelUpdate
		    MethodData corrRoleMethData = info.getRoleMethod();
		    IMethod correspondingRoleMethod = 
		        role.getMethod(corrRoleMethData.getSelector(), 
		                       corrRoleMethData.getArgumentTypes()); 
//haebor}							   
			MethodData[] baseMethods = info.getBaseMethods();
			return new CalloutMapping(info.getDeclarationSourceStart(),
						       info.getSourceStart(),
						       info.getSourceEnd(),
							   info.getDeclarationSourceEnd(),
							   (IRoleType)otmRole,
//{OTModelUpdate
//orig:	(IMethod) otmRole.getParent(),
							   correspondingRoleMethod,
//haebor}							           
							   info.getRoleMethod(),
							   baseMethods == null ? null : baseMethods[0],
							   info.hasSignature(),
							   info.isOverride(),
							   info.getDeclaredModifiers());
		}
		
		return null;
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
    public ICalloutToFieldMapping addCalloutToFieldBinding(IType role, MappingElementInfo info)
    {
        IOTType otmRole = MAPPING.getOTElement(role);
        
        if ((otmRole != null) && (otmRole instanceof IRoleType))
        {
            MethodData corrRoleMethData = info.getRoleMethod();
            IMethod correspondingRoleMethod = 
                role.getMethod(corrRoleMethData.getSelector(), 
                               corrRoleMethData.getArgumentTypes()); 
            return new CalloutToFieldMapping(info.getDeclarationSourceStart(),
            		   info.getSourceStart(),
            		   info.getSourceEnd(),
                       info.getDeclarationSourceEnd(),
                       (IRoleType)otmRole,
                       correspondingRoleMethod,
					   info.getRoleMethod(),
					   info.getBaseField(),
					   info.hasSignature(),
					   info.isOverride());
        }
        
        return null;
    }
    
    public void addOTElement(IOTType otType)
    {
        MAPPING.addOTElement(otType);
    }

	/**
	 * Returns associated OTM element for a given type if there is one.
	 * 
	 * @return corresponding OTM element or null if no such element exists
	 */
    public static IOTType getOTElement(IType type)
    {
        return MAPPING.getOTElement(type);
    }

    public static boolean hasOTElementFor(IType type)
    {
        return MAPPING.hasOTElementFor(type);
    }
        
    public static void removeOTElement(IType type)
    {
    	removeOTElement(type, false);
    }

	/**
	 * @see OTModel#removeOTElement(IType, boolean)  
	 */
	public static void removeOTElement(IType type, boolean hasChanged)
	{
		MAPPING.removeOTElement(type, hasChanged);
	}
	
	/**
	 * Utility function.
	 * @param type
	 * @return
	 */
	public static boolean isTeam(IType type) {
		if (hasOTElementFor(type))
		{
			IOTType ottype = getOTElement(type);
			return ottype.isTeam();
		}
		return false;
	}
	
	/**
	 * returns true if this member belongs to a role. Takes binary role-interfaceparts into account.
	 * FIXME (carp): this is mostly a workaround for binary role-interfaceparts. When this problem is
	 * fixed conceptually, check and "port" all clients of this method.
	 */
	public static boolean belongsToRole(IMember member) 
	{
		IType enclosing = member.getDeclaringType();
		if (enclosing != null)
		{
			IOTType otType = getOTElement(enclosing);
			return otType != null && otType.isRole();
		}
		
		return false;
	}
}
