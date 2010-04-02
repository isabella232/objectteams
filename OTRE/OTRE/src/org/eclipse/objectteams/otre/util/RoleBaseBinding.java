/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleBaseBinding.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @version $Id: RoleBaseBinding.java 23408 2010-02-03 18:07:35Z stephan $
 * @author Christine Hundt
 */
public class RoleBaseBinding {

	private BoundClass roleClass;
	private BoundClass baseClass;

	private ListValueHashMap<MethodBinding> roleMethodBindings = new ListValueHashMap<MethodBinding>();
	private ListValueHashMap<MethodBinding> baseMethodBindings = new ListValueHashMap<MethodBinding>();
	
    /**
     * 
     */
    public RoleBaseBinding() {
    }

    /**
     * @param _roleClassName
     * @param _baseClassName
     * @param teamClassName
     */
    public RoleBaseBinding(String _roleClassName, String _baseClassName, String teamClassName) {
    	roleClass = new BoundClass(_roleClassName, teamClassName);
    	baseClass = CallinBindingManager.getBoundBaseClass(_baseClassName, teamClassName);
    }

    /**
     * @param bindingFileName
     * @param bindingLineNumber
     * @param bindingLineOffset
     * @param bindingLabel
     * @param roleMethodName
     * @param roleMethodSignature
     * @param isStaticRoleMethod
     * @param wrapperName
     * @param wrapperSignature
     * @param modifier
     * @param baseMethodName
     * @param baseMethodSignature
     * @param isStaticBaseMethod
     * @param baseIsCallin
     * @param translationFlags
     * @param liftMethodName
     * @param liftMethodSignature
     */
    public void addMethodBinding(
            String bindingFileName, int bindingLineNumber, int bindingLineOffset,
			String bindingLabel, String roleMethodName, String roleMethodSignature,
			boolean isStaticRoleMethod, String wrapperName, String wrapperSignature, String modifier, 
			String baseMethodName, String baseMethodSignature, 
			boolean isStaticBaseMethod, boolean baseIsCallin, boolean covariantBaseReturn,
			int translationFlags, String liftMethodName, String liftMethodSignature)
	{
		MethodBinding mb = new MethodBinding(bindingFileName, bindingLineNumber, bindingLineOffset,
																		   bindingLabel, roleMethodName, roleMethodSignature, isStaticRoleMethod,
							   											   wrapperName, wrapperSignature, modifier,
							   											   baseMethodName, baseMethodSignature, 
							   											   isStaticBaseMethod, baseIsCallin, covariantBaseReturn, 
							   											   translationFlags,
																		   liftMethodName,
							   											   liftMethodSignature, this);
		// TODO: check, if the key has to include the 'binding_label'
		String baseMethodKey = MethodBinding.getBaseMethodKey(baseMethodName, baseMethodSignature);
		String roleMethodKey = roleMethodName + '.' + roleMethodSignature;
		roleMethodBindings.put(roleMethodKey, mb);
		baseMethodBindings.put(baseMethodKey, mb);
    }

	/**
	 * @return
	 */
	public List<MethodBinding> getBaseMethodBindings() {
		return baseMethodBindings.getFlattenValues();
	}
	
	/**
	 * @param baseMethodName
	 * @param baseMethodSignature
	 * @return
	 */
	public List<MethodBinding> getBaseMethodBindings(String baseMethodName, String baseMethodSignature) {
		String baseMethodKey = MethodBinding.getBaseMethodKey(baseMethodName, baseMethodSignature);
		return baseMethodBindings.get(baseMethodKey);
	}

	/**
	 * @return
	 */
	public List<MethodBinding> getRoleMethodBindings() {
		return roleMethodBindings.getFlattenValues();
	}
	
	/**
	 * @param roleMethodName
	 * @param roleMethodSignature
	 * @return
	 */
	public List<MethodBinding> getRoleMethodBindings(String roleMethodName, String roleMethodSignature) {
		String roleMethodKey = roleMethodName + '.' + roleMethodSignature;
		return roleMethodBindings.get(roleMethodKey);
	}
	
	/**
	 * @param roleMethodName
	 * @param roleMethodSignature
	 * @return
	 */
	public boolean hasRoleMethodBinding(String roleMethodName, String roleMethodSignature) {
		String signatureWithoutReturnType = roleMethodSignature.substring(0, roleMethodSignature.lastIndexOf(')') + 1);
		Set<String> bindingKeys = roleMethodBindings.keySet();
		Iterator<String> it = bindingKeys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String keyWithoutReturnType = key.substring(0, key.lastIndexOf(')') + 1);
			if (keyWithoutReturnType.equals(roleMethodName + '.'
					+ signatureWithoutReturnType))
				return true;
		}
		return false;
	}
	
	public Set<String> getRoleMethodSignatures() {
		return roleMethodBindings.keySet();	
	}
	
	/**
	 * @return
	 */
	public BoundClass getRoleClass() {
		return roleClass;
	}
	
	/**
	 * @return
	 */
	public BoundClass getBaseClass() {
			return baseClass;
		}
	
    /**
     * @return
     */
    public String getRoleClassName() {
		return roleClass.getName();
	}

	/**
	 * @return
	 */
	public String getBaseClassName() {
		return baseClass.getName();
	}

	/**
	 * Collect all base method signatures for this role-base pair.
	 * @result List <String[] {name, signature}>
	 */
	public List<String[]> getBaseSignatures () {
		List<String[]> result = new LinkedList<String[]>();
		List<MethodBinding> baseMethodBindingList = getBaseMethodBindings();
		Iterator<MethodBinding> it = baseMethodBindingList.iterator();
		while (it.hasNext()) {
			MethodBinding mb = it.next();
			result.add(new String [] {
				mb.getBaseMethodName(),
				mb.getBaseMethodSignature()
			});
		}
        return result;
	}

    /**
     * @param rbb
     * @return
     */
    public boolean equals(RoleBaseBinding rbb) {
        return roleClass.getName().equals(rbb.getRoleClassName())
			&& baseClass.getName().equals(rbb.getBaseClassName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder out = new StringBuilder(64);
        out.append(roleClass.getName());
        out.append(" <-> ");
        out.append(baseClass.getName());
	    out.append("\nmethod bindings:\n");
	    List mbsList = getBaseMethodBindings();
	    Iterator it = mbsList.iterator();
	    while (it.hasNext()) {
			MethodBinding mb = (MethodBinding)it.next();
			out.append("\n");
            out.append(mb.getBaseMethodName());
            out.append(".");
            out.append(mb.getBaseMethodSignature());
            out.append(":");
            out.append(mb.toString());
	    }
        return out.toString();
    }
}
