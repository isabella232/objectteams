/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2002-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodBinding.java 23408 2010-02-03 18:07:35Z stephan $
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

import org.eclipse.objectteams.otre.OTConstants;

/**
 * @version $Id: MethodBinding.java 23408 2010-02-03 18:07:35Z stephan $
 * @author Christine Hundt
 */
public class MethodBinding {

	private String bindingFileName;
	private int bindingLineNumber;
	private int bindingLineOffset;
	
	private String bindingLabel;
	
	private BoundMethod baseMethod;
	private BoundMethod roleMethod;

	private boolean isStaticBaseMethod;
	private boolean isStaticRoleMethod;
	private boolean covariantBaseReturn;
	
	private int translationFlags;
	
   	private String wrapperName;
	private String wrapperSignature;

   	private String modifier;
   	private String liftMethodName;
   	private String liftMethodSignature;
    
    private String roleClassName;
    
    // back reference for the class binding containing this method binding.
    private RoleBaseBinding classBinding;

    /**
     * 
     */
    public MethodBinding() {}

    /**
     * @param bindingFileName
     * @param bindingLineNumber
     * @param bindingLineOffest
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
     * @param liftMethodName
     * @param liftMethodSignature
     * @param classBinding
     */
    public MethodBinding(
            String bindingFileName, int bindingLineNumber, int bindingLineOffest,
			String bindingLabel, String roleMethodName, String roleMethodSignature, boolean isStaticRoleMethod, 
			String wrapperName, String wrapperSignature, String modifier, 
			String baseMethodName, String baseMethodSignature, 
			boolean isStaticBaseMethod,	boolean baseIsCallin, boolean covariantBaseReturn, 
			int translationFlags, 
			String liftMethodName, String liftMethodSignature, RoleBaseBinding classBinding) 
	{
    	this.bindingFileName = bindingFileName;
    	this.bindingLineNumber = bindingLineNumber;
    	this.bindingLineOffset = bindingLineOffest;
    	
    	this.bindingLabel = bindingLabel;
		roleMethod = new BoundMethod(roleMethodName, roleMethodSignature, false, this);
		baseMethod = new BoundMethod(baseMethodName, baseMethodSignature, baseIsCallin, this);
		// BoundMethod object for one method is not unique!
		// but if it would be unique the 'binding' link would not be unique
		this.isStaticRoleMethod  = isStaticRoleMethod;
		this.isStaticBaseMethod  = isStaticBaseMethod;
		this.covariantBaseReturn = covariantBaseReturn;
		this.translationFlags    = translationFlags;
        this.wrapperName         = wrapperName;
        this.wrapperSignature    = wrapperSignature;
        this.modifier            = modifier;
        this.liftMethodName      = liftMethodName;
        this.liftMethodSignature = liftMethodSignature;
        this.classBinding        = classBinding;
        this.roleClassName       = classBinding.getRoleClassName();
    }

    /**
     * @return
     */
    public String getRoleClassName() {
		return roleClassName;
    }

    /**
     * @return
     */
    public String getBindingFileName() {
		return bindingFileName;
	}
	
	/**
	 * @return
	 */
	public int getBindingLineNumber() {
		return bindingLineNumber;
	}
	
	/**
	 * @return
	 */
	public int getBindingLineOffset() {
		return bindingLineOffset;
	}
    
    /**
     * @return
     */
    public String getBindingLabel() {
    	return bindingLabel;
    }
    
    /**
     * @return
     */
    public String getQualifiedBindingLabel() {
    	String result = roleClassName;
    	// remove the outermost team (use the '$' to distinguish pack1.Team1$Role from Team1$__OT__Team2$Role):
    	result = result.substring(result.indexOf('$') + 1);
    	// replace "$" by "." in the role class name:
    	result = result.replace('$', '.');
    	// remove  "__OT__" prefixes in role class names:
    	result = result.replace(OTConstants.OTDT_PREFIX, ""); //$NON-NLS-1$
    	// add the binding label and return:    	
    	return result + '.' + bindingLabel;
    }
    
    /**
     * @return
     */
    public String getRoleMethodName() {
		return roleMethod.getName();
    }
    
    /**
     * @return
     */
    public String getRoleMethodSignature() {
		return roleMethod.getSignature();
    }
	
	/**
	 * @return
	 */
	public String getWrapperName() {
		return wrapperName;
	}
	
	/**
	 * @return
	 */
	public String getWrapperSignature() {
		return wrapperSignature;
	}

    /**
     * @return
     */
    public String getBaseClassName() {
		return classBinding.getBaseClassName();
    }

    /**
     * @return
     */
    public String getBaseMethodName() {
		return baseMethod.getName();
    }

    /**
     * @return
     */
    public String getBaseMethodSignature() {
		return baseMethod.getSignature();
    }

    /**
     * @return
     */    
    public boolean baseMethodIsCallin() {
    	return baseMethod.getIsCallin();
    }
 
    /**
     * @return
     */    
    public String getModifier() {
        return modifier;
    }
	
    /**
     * @return
     */
    public boolean isReplace() {
		return modifier.equals("replace");
	}
	
	/**
	 * @return
	 */
	public boolean isAfter() {
		return modifier.equals("after");
	}
	
	/**
	 * @return
	 */
	public boolean isBefore() {
		return modifier.equals("before");
	}
	
	/**
	 * @return Returns the liftMethodName.
	 */
	public String getLiftMethodName() {
		return liftMethodName;
	}
	
	/**
	 * @return Returns the liftMethodSignature.
	 */
	public String getLiftMethodSignature() {
		return liftMethodSignature;
	}
	
	/**
	 * @return Returns the corresponding class binding.
	 */
	public RoleBaseBinding getClassBinding() {
		return classBinding;
	}
	
	/**
	 * Returns the most super bound base class of the base class belonging to
	 * this MethodBinding. Note: only consider base classes bound by roles of
	 * the corresponding team!
	 * 
	 * @return the most super bound base class
	 */
	public String getRootBoundBase() {
		BoundClass bc = classBinding.getRoleClass();
		while (bc.getSuper() != null && CallinBindingManager.isBoundRoleClass(bc.getSuper().getName()) ) {
            bc = bc.getSuper();
        }
		RoleBaseBinding rbb = CallinBindingManager.getRoleBaseBinding(bc.getName());
		return rbb.getBaseClassName();
	}
	
	/**
	 * Returns the name of the team surrounding the role class of this binding.
	 * 
	 * @return the name of the corresponding team
	 */
	public String getTeamClassName() {
		int dollarIndex = roleClassName.lastIndexOf('$');
		// return everything before the last '$', because for nested teams there are more than one:
        return roleClassName.substring(0, dollarIndex);
	}
	
	/**
	 * @param anotherMB
	 * @return
	 */
	public boolean overridesMethodBinding(MethodBinding anotherMB) {
		if (anotherMB == null)
			return false;
		if (!bindingLabel.equals(anotherMB.getBindingLabel()))
			return false;
		return classBinding.getRoleClass().isSubClassOf(anotherMB.getClassBinding().getRoleClassName());
	}
	
	/**
	 * @param bindingLabel
	 * @param teamName
	 * @return
	 */
	public boolean inheritsBindingLabel(String bindingLabel, String teamName) {
		String prefix = teamName + "$__OT__";
		int dotIndex = bindingLabel.lastIndexOf('.');
		String classOfLabel = prefix + bindingLabel.substring(0, dotIndex);
		if (!bindingLabel.substring(dotIndex + 1).equals(this.bindingLabel))
			return false;
		
		if (classBinding.getRoleClass().isSubClassOf(classOfLabel))
			return true;
		return false;
	}

    /**
     * @param mb
     * @return
     */
    public boolean equals(MethodBinding mb) {
        return roleMethod.getName().equals(mb.getRoleMethodName())
			&& roleMethod.getSignature().equals(mb.getRoleMethodSignature())
			&& classBinding.getBaseClassName().equals(mb.getBaseClassName())
			&& baseMethod.getName().equals(mb.getBaseMethodName())
			&& baseMethod.getSignature().equals(mb.getBaseMethodSignature())
			&& modifier.equals(mb.getModifier());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder result = new StringBuilder(32);
        result.append("\t");
        result.append(getQualifiedBindingLabel());
        result.append(": ");
        result.append(roleMethod.getName());
        result.append(roleMethod.getSignature());
        result.append(" <-> ");
        result.append(modifier);
        result.append(" ");
        result.append(baseMethod.getName());
        result.append(baseMethod.getSignature());
        if (this.covariantBaseReturn)
        	result.append('+');
        return result.toString();
    }

	/**
	 * @return
	 */
	public boolean hasStaticRoleMethod() {
		return isStaticRoleMethod;
	}
	
    /**
     * @return
     */
	public boolean hasStaticBaseMethod() {
		return isStaticBaseMethod;
	}

	public int getTranslationFlags() {
		return this.translationFlags;
	}

	/** For base methods provide a key without the trailing return type to cater for covariance. */ 
	static String getBaseMethodKey(String baseMethodName, String baseMethodSignature) {
		int pos= baseMethodSignature.lastIndexOf(')');
		String baseMethodKey = baseMethodName + '.' + baseMethodSignature.substring(0, pos+1);
		return baseMethodKey;
	}

	/** 
	 * Is the method specified by mName and mSig a match for this method binding?
	 * @param mName method name
	 * @param mSig full method signature
	 * @param strict if true covariance is not supported
	 */
	public boolean matchesMethod(String mName, String mSig, boolean strict) {
		String baseMethodName = getBaseMethodName();
		String baseMethodSignature = getBaseMethodSignature();
		if (this.covariantBaseReturn && !strict) {
			return getBaseMethodKey(mName, mSig).equals(
				   getBaseMethodKey(baseMethodName, baseMethodSignature));
		} else {
			return mName.equals(baseMethodName) 
				&& mSig.equals(baseMethodSignature);
		}
	}
}
