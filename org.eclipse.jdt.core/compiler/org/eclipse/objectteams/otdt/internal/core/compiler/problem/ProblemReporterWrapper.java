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
 * $Id: ProblemReporterWrapper.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.problem;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Inherit from this class reimplement <b>all</b> those methods, that might get called during
 * your AST element's resolveType() method. You can report your own instead
 * of the original errors.
 *
 * You must delegate all errors, that you do not handle yourself, to the protected
 * _wrappee object, like that:
 * <code>
 * public void cannotDireclyInvokeAbstractMethod(
 *		MessageSend messageSend,
 *		MethodBinding method) {
 *		    _wrappee.cannotDireclyInvokeAbstractMethod(messageSend, method);
*	}
 * </code>
 * @author gis
 */
public abstract class ProblemReporterWrapper extends ProblemReporter
{
    protected ProblemReporter _wrappee;

    public ProblemReporterWrapper(ProblemReporter wrappee)
    {
        super(wrappee.policy, wrappee.options, wrappee.problemFactory);

        this._wrappee = wrappee;
    }

    private static boolean ASSERT_ENABLED = false;
    static {
        assert(ASSERT_ENABLED = true); // intentional side-effect!
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
        System.err.println("The following problem should be handled by " + getClass().getName() + ": " + problemId);

        if (ASSERT_ENABLED)
        {
            try
            {
                this._wrappee.handle(
                    problemId,
                    problemArguments,
                    elaborationId,
                    messageArguments,
                    severity,
                    problemStartPosition,
                    problemEndPosition,
                    context,
                    unitResult);
            }
            catch (RuntimeException ex)
            {
                // ignore any thrown exception, we want the assert to fail!
            }
            assert(false);
        }
        else
        {
            this._wrappee.handle(
                problemId,
                problemArguments,
                elaborationId,
                messageArguments,
                severity,
                problemStartPosition,
                problemEndPosition,
                context,
                unitResult);
        }
    }
}
