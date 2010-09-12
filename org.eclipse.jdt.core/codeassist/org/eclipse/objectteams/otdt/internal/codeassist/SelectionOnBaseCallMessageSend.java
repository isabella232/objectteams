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
 * $Id: SelectionOnBaseCallMessageSend.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;

/**
 * @author haebor
 */
public class SelectionOnBaseCallMessageSend extends BaseCallMessageSend
{
    /**
     * @param wrappee
     */
    public SelectionOnBaseCallMessageSend(MessageSend wrappee, int baseEndPosition)
    {
        super(wrappee, baseEndPosition);
    }

	public TypeBinding resolveType(BlockScope scope)
	{
		try {
			super.resolveType(scope);
		} catch (SelectionNodeFound snf) {
			if (   (snf.binding instanceof MethodBinding)
				&& MethodModel.isFakedMethod((MethodBinding)snf.binding))
			{
				// fake method (e.g. basecall surrogate) is not valid, continue below
			} else {
				throw snf;
			}
		}

		MessageSend wrappee = (MessageSend) this._sendOrig; // _wrappee might have been replaced with an Assignment.

		// tolerate some error cases
		if(wrappee.binding == null ||
					!(wrappee.binding.isValidBinding() ||
					        wrappee.binding.problemId() == ProblemReasons.NotVisible
						|| wrappee.binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
						|| wrappee.binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
						|| wrappee.binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext)) {
			throw new SelectionNodeFound();
		}
		else
		{
			// wrappee.binding is the base call surrogate, find the proper enclosing method instead:
			MethodBinding callinMethod = null;
			MethodScope methodScope = scope.methodScope();
			if (methodScope == null)
				throw new SelectionNodeFound();

			MemberTypeBinding role = null;
		    SourceTypeBinding site = scope.enclosingSourceType();

		    if (site.isLocalType()) {
		    	MethodDeclaration callinDecl = getOuterCallinMethod(methodScope);
		    	if (callinDecl == null)
		    		throw new SelectionNodeFound();
		    	callinMethod = callinDecl.binding;
		    	role = (MemberTypeBinding)callinMethod.declaringClass;
		    } else {
		    	callinMethod = methodScope.referenceMethod().binding;
		    	role = (MemberTypeBinding)site;
		    }

	        // find base methods bound in callin mappings which have callinMethod as their role method:
	        MethodBinding[] baseBindings = findBaseMethodBindings(role, callinMethod);
	        if(baseBindings == null || baseBindings.length == 0) {
	        	throw new SelectionNodeFound();
	        } else {
		        throw new SelectionNodesFound(baseBindings);
	        }
		}
	}


	/**
	 * Looks up methods in the corresponding base class that match the roleMethod
	 * @param role
	 * @param roleMethod
	 * @return
	 */
	private MethodBinding[] findBaseMethodBindings(MemberTypeBinding role, MethodBinding roleMethod)
	{
        List<CallinMappingDeclaration> foundMappings = findMappings(role, roleMethod);
        ReferenceBinding baseClass = role.getRealClass().baseclass();

        if(foundMappings.isEmpty() || baseClass == null)
        {
            return null;
        }
        int baseMethodsCount = 0;
        for (CallinMappingDeclaration mapping : foundMappings)
			baseMethodsCount += mapping.baseMethodSpecs.length;

        MethodBinding[] baseMethodBindings = new MethodBinding[baseMethodsCount];

        for (CallinMappingDeclaration foundMapping : foundMappings)
	        for(int idx = 0; idx < foundMapping.baseMethodSpecs.length; idx++)
		        baseMethodBindings[--baseMethodsCount] = foundMapping.baseMethodSpecs[idx].resolvedMethod;

        return baseMethodBindings;
	}

	/**
	 * Try to find all CallinMethodMappings that reference the given roleMethod
     * @param role Where to look for
     * @param roleMethod Method that should be referenced by a CallinMapping
     * @return
     */
    private List<CallinMappingDeclaration> findMappings(MemberTypeBinding role, MethodBinding roleMethod)
    {
        TypeDeclaration roleDeclaration = (TypeDeclaration)role.model.getAst();

        ArrayList<CallinMappingDeclaration> foundMappings = new ArrayList<CallinMappingDeclaration>();

        AbstractMethodMappingDeclaration[] mappings = roleDeclaration.callinCallouts;
        if(mappings != null) {
	        for(int idx = 0; idx < mappings.length; idx++)
	        {
	            if(mappings[idx].binding._roleMethodBinding == roleMethod)
	            {
	                if(mappings[idx] instanceof CallinMappingDeclaration)
	                {
		                foundMappings.add((CallinMappingDeclaration)mappings[idx]);
	                }
	            }
	        }
        }
        return foundMappings;
    }

    public StringBuffer printExpression(int indent, StringBuffer output)
	{
		MessageSend wrappee = (MessageSend) this._wrappee;

		output.append("<SelectOnBaseCallMessageSend:"); //$NON-NLS-1$
		if (!wrappee.receiver.isImplicitThis()) wrappee.receiver.printExpression(0, output).append('.');
		output.append(wrappee.selector).append('(');
		if (wrappee.arguments != null)
		{
			for (int i = 1; i < wrappee.arguments.length; i++) // 1: skip initial arg 'isSuperAccess'
			{
				if (i > 0) output.append(", "); //$NON-NLS-1$
				wrappee.arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>"); //$NON-NLS-1$
	}

}
