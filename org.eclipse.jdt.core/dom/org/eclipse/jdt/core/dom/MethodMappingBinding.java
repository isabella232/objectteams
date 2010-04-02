/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodMappingBinding.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.InferenceKind;


/**
 * Internal implementation of callin/callout mapping bindings.
 *
 * @author mkr
 */
class MethodMappingBinding implements IMethodMappingBinding
{

	private org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding _binding;
	private BindingResolver _resolver;
	private String _name;
	private ITypeBinding _declaringClass;
    private ITypeBinding _baseClass; 
    private IMethodBinding _roleMethod;
    private IMethodBinding[] _baseMethods;
	
	MethodMappingBinding(
            BindingResolver resolver,
            org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding binding)
    {
		_resolver = resolver;
		_binding = binding;
	}
	
	// svenk: added to hold annotations for method getAnnotations()
  private IAnnotationBinding[] annotations;

	/*
	 * @see IBinding#getName()
	 */
	public String getName()
    {
		if (_name == null)
		{
		    _name = new String(_binding.readableName());
		}
        
		return _name;
	}

	/*
	 * @see IMethodMappingBinding#getDeclaringRoleClass()
	 */
	public ITypeBinding getDeclaringRoleClass()
    {
		if (_declaringClass == null)
        {
			_declaringClass = _resolver.getTypeBinding(_binding._declaringRoleClass);
		}
        
		return _declaringClass;
	}

    /*
     * @see IMethodMappingBinding#getReferencedBaseClass()
     */
    public ITypeBinding getReferencedBaseClass()
    {
        if (_baseClass == null)
        {            
            _baseClass = _resolver.getTypeBinding(_binding._declaringRoleClass.baseclass());
            //mkr: This is a workaround because _binding.referencedBaseClass is null
            // _baseClass = _resolver.getTypeBinding(_binding._referencedBaseClass);
        }
        
        return _baseClass;
    }
    
    /*
     * @see IMethodMappingBinding#getRoleMethod()
     */
    public IMethodBinding getRoleMethod()
    {
        if (_roleMethod == null)
        {
            _roleMethod = _resolver.getMethodBinding(_binding._roleMethodBinding);
        }
        
        return _roleMethod;
    }
    
    /*
     * @see IMethodMappingBinding#getBaseMethods()
     */
    public IMethodBinding[] getBaseMethods()
    {
        if (_baseMethods == null)
        {
        	MethodBinding[] methodBindings = this._binding._baseMethods;
			if (methodBindings == null)
        		return new IMethodBinding[0];
        	this._baseMethods = new IMethodBinding[methodBindings.length];
        	for (int i = 0; i < methodBindings.length; i++) 
				this._baseMethods[i] = _resolver.getMethodBinding(methodBindings[i]);            
        }
        
        return _baseMethods;
    }

    public String[] getBaseArgumentNames() {
    	MethodBinding[] methodBindings = this._binding._baseMethods;
		if (methodBindings != null && methodBindings.length > 0) 
		{
			String[] result = new String[methodBindings[0].parameters.length];
			AbstractMethodDeclaration methodDecl = methodBindings[0].sourceMethod();
			if (methodDecl != null) {
				Argument[] args = methodDecl.arguments;
				if (args != null) {
					for (int i = 0; i < args.length; i++)
						result[i] = String.valueOf(args[i].name);
					
					return result;
				}
			}
			for (int i = 0; i < result.length; i++)
				result[i] = "arg"+i;  //$NON-NLS-1$
			return result;
		}
    	return new String[0];
    }
    
	/*
	 * @see IBinding#getKind()
	 */
	public int getKind()
    {
		return IBinding.METHOD_MAPPING;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers()
    {
        if (_binding.type == org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding.CALLIN)
        {
            switch (_binding.callinModifier)
            {
                case TerminalTokens.TokenNamebefore:
                    return Modifier.OT_BEFORE_CALLIN;
                case TerminalTokens.TokenNameafter:
                    return Modifier.OT_AFTER_CALLIN;
                case TerminalTokens.TokenNamereplace:
                    return  Modifier.OT_REPLACE_CALLIN;
                default:
                    return 0;
            }
        }
//        else if (_binding.type == org.eclipse.jdt.internal.compiler.lookup.CallinCalloutBinding.CALLOUT)
//        {
//            // As for now, CalloutMappings have no modifier,
//            // since get and set are properties of FieldAccessSpec.
//        }
        
	    return 0;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated()
    {
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic()
    {
		return false;
	}
	
	public boolean isCallin() {
		return _binding.isCallin();
	}

	/*
	 * @see IBinding#getKey()
	 */
    public String getKey()
    {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getDeclaringRoleClass().getKey());
		buffer.append('/');
        buffer.append(this.getName());
		buffer.append(')');

        return buffer.toString();
	}
	
	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString()
    {
		return _binding.toString();
	}

	public IJavaElement getJavaElement() {
		// SH: could not find a path that could possibly call this method [26.2.07]
		return null;
	}

	public boolean isEqualTo(IBinding other) {
		if (this == other) 
			return true;
		if (other == null)
			return false;
		
		if (!(other instanceof MethodMappingBinding)) {
			// consider a callout as equal to the role method it defines.
			if (!this.isCallin() && this.getRoleMethod().isEqualTo(other))
				return true;
			return false;
		}
		// untested below [06.02.09]
		org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding otherBinding = ((MethodMappingBinding) other)._binding;
		if (BindingComparator.isEqual(this._binding, otherBinding)) 
			return true;
		return false;
	}
	
// (svenk: implement method from IBinding
  public IAnnotationBinding[] getAnnotations() {
    if (this.annotations != null) {
      return this.annotations;
    }
    org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding[] annots = this._binding.getAnnotations();
    int length = annots == null ? 0 : annots.length;
    if (annots == null) {
      return this.annotations = AnnotationBinding.NoAnnotations;
    }
    IAnnotationBinding[] domInstances = new AnnotationBinding[length];
    for (int i = 0; i < length; i++) {
      final IAnnotationBinding annotationInstance = this._resolver.getAnnotationInstance(annots[i]);
      if (annotationInstance == null) {
        return this.annotations = AnnotationBinding.NoAnnotations;
      }
      domInstances[i] = annotationInstance;
    }
    return this.annotations = domInstances;
  }
// svenk)

	public boolean isRecovered() {
		// method mappings are not (yet) recovered (cf. e.g., DefaultBindingResolver.getVariableBinding())
		return false;
	}



	public InferenceKind getInferenceKind() {
		if (this._binding != null)
			return this._binding.inferred;
		return InferenceKind.NONE;
	}

}