/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ParameterMappingTemplateResolver.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.templates;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.util.MethodData;

/**
 * This resolver proposes the parameter names to be used in a callin/callout parameter mapping
 * created from a completion template.
 * 
 * @author stephan
 * @since 1.2.4
 */
public class ParameterMappingTemplateResolver extends TemplateVariableResolver {

	// types of this resolver:
	private static final String CALLIN_PARAMETER_NAME  = "callin_parameter_name"; //$NON-NLS-1$
	private static final String CALLOUT_PARAMETER_NAME = "callout_parameter_name"; //$NON-NLS-1$
	
	private static final String VOID = "V";//$NON-NLS-1$

	// special expansions:
	private static final String RESULT = "result"; //$NON-NLS-1$
	private static final String NOT_APPLICABLE = "$not applicable$"; //$NON-NLS-1$

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		if (context instanceof JavaContext) {
			IMethodMapping methodMapping = findEnclosingMethodMapping((JavaContext)context);
			if (methodMapping != null) {
				variable.setValue(NOT_APPLICABLE);
				String[] argumentNames = null;
				if (!methodMapping.hasSignature()) {
					return;
				} else if (this.getType().equals(CALLIN_PARAMETER_NAME) && methodMapping.getElementType() == IOTJavaElement.CALLIN_MAPPING) 
				{			
					MethodData method = methodMapping.getRoleMethodHandle();			
					argumentNames = method.getArgumentNames();
				} else if (this.getType().equals(CALLOUT_PARAMETER_NAME) && methodMapping.getElementType() == IOTJavaElement.CALLOUT_MAPPING) 
				{
					MethodData method = ((ICalloutMapping) methodMapping).getBaseMethodHandle();			
					argumentNames = method.getArgumentNames();
				} else {
					if (isResultMappingSupported(methodMapping))
						variable.setValue(RESULT);
					else 
						variable.setResolved(false);
					return;
				}
				if (argumentNames != null && argumentNames.length > 0)
					variable.setValues(argumentNames);
				else 
					variable.setResolved(false);
			}
		}
	}


	private IMethodMapping findEnclosingMethodMapping(JavaContext jContext) {
		IJavaElement callinMapping = jContext.findEnclosingElement(IOTJavaElement.CALLIN_MAPPING);
		if (callinMapping != null)
			return (IMethodMapping) callinMapping;
		return (IMethodMapping) jContext.findEnclosingElement(IOTJavaElement.CALLOUT_MAPPING);
	}

	/* Only propose a result mapping if a result value is actually expected. */
	private boolean isResultMappingSupported(IMethodMapping element) 
	{
		if (element.getElementType() == IOTJavaElement.CALLOUT_MAPPING)
			return !element.getRoleMethodHandle().getReturnType().equals(VOID); 

		if (element.getElementType() == IOTJavaElement.CALLIN_MAPPING) {
			if (((ICallinMapping) element).getCallinKind() != ICallinMapping.KIND_REPLACE)
				return false;
			try {
				for (IMethod boundBaseMethod : ((ICallinMapping) element).getBoundBaseMethods())
					if (boundBaseMethod.getReturnType().equals(VOID)) 
						return false;
			} catch (JavaModelException e) {
				return false;
			}
			return true;
		}
		return false;
	}
	
}
