/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BaseCallProblemReporterWrapper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.problem;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.BaseCallMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.SyntheticBaseCallSurrogate;


/**
 * NEW for OTDT:
 *
 * Intercept problems in a BaseCallMessageSend
 *
 * @author gis
 */
public class BaseCallProblemReporterWrapper extends ProblemReporterWrapper
{
	private BaseCallMessageSend _messageSend;

    public BaseCallProblemReporterWrapper(ProblemReporter wrappee, BaseCallMessageSend messageSend)
    {
        super(wrappee);
        this.referenceContext = wrappee.referenceContext;
        this._messageSend = messageSend;
    }
    @Override
    public void handle(
    		int problemId,
    		String[] problemArguments,
    		int elaborationId,
    		String[] messageArguments,
    		int severity,
    		int problemStartPosition,
    		int problemEndPosition,
    		ReferenceContext context,
    		CompilationResult unitResult)
    {
    	long nameSource = this._messageSend.getMessageSend().nameSourcePosition;
    	if (problemStartPosition > (int)(nameSource>>>32)) // affecting args only
    		this._wrappee.handle(problemId, problemArguments, elaborationId, messageArguments, severity,
        			problemStartPosition, problemEndPosition, context, unitResult);
    	else
    		super.handle(problemId, problemArguments, elaborationId, messageArguments, severity,
    				problemStartPosition, problemEndPosition, context, unitResult);
    }
	public void handle(
		int problemId,
		String[] problemArguments,
		String[] messageArguments,
		int problemStartPosition,
		int problemEndPosition,
		ReferenceContext context,
		CompilationResult unitResult)
	{
		if (problemId == IProblem.ParameterMismatch)
		{
			this._wrappee.baseCallDoesntMatchRoleMethodSignature(this._messageSend.getMessageSend());
		} else if (problemId == IProblem.CallToCallin) {
			return; // not a problem in a base call.
			// Note: we do not check this condition in MessageSend.resolveType,
			// because at that point it is difficult to detect that we have a base call!
		} else if (   problemId == IProblem.UndefinedName
				   && problemArguments != null
				   && problemArguments.length > 0
				   && problemArguments[0].startsWith(IOTConstants.OT_DOLLAR))
		{
			return; // don't complain unresolved _OT$result
		} else {
			this._wrappee.handle(
				problemId,
				problemArguments,
				messageArguments,
				problemStartPosition,
				problemEndPosition,
				context,
				unitResult);
		}

	}
	public void invalidMethod(MessageSend messageSend, MethodBinding method) {
		ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
		AbstractMethodDeclaration enclosingMethodDecl = ((AbstractMethodDeclaration)this.referenceContext);
		if (enclosingMethodDecl.ignoreFurtherInvestigation)
			return; // too probable that our error was caused by not resolving the enclosing method.
		char[] problemSelector = problemMethod.selector;
		problemSelector = SyntheticBaseCallSurrogate.stripBaseCallName(problemSelector);
		if (   problemMethod.closestMatch != null
			&& CharOperation.equals(
					problemSelector,
					enclosingMethodDecl.selector))
		{
			this._wrappee.baseCallDoesntMatchRoleMethodSignature(this._messageSend);
		} else {
			this._wrappee.baseCallNotSameMethod(enclosingMethodDecl, messageSend);
		}
	}
}
