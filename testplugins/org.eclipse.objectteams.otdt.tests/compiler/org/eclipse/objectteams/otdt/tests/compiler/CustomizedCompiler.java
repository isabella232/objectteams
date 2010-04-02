/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CustomizedCompiler.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler;

import java.util.Vector;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * @author ike
 *
 */
public class CustomizedCompiler extends Compiler
{
    private Vector<ICallbackClient> _callBacks;
    
    /**
     * @param environment
     * @param policy
     * @param settings
     * @param requestor
     * @param problemFactory
     */
    public CustomizedCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerOptions settings, ICompilerRequestor requestor, IProblemFactory problemFactory)
    {
        super(environment, policy, settings, requestor, problemFactory);
    }
    
    public void addCallBack(ICallbackClient c)
    {
        if (_callBacks == null)
            _callBacks = new Vector<ICallbackClient>();

        _callBacks.add(c);
    }
    	
    @Override
    public void process(CompilationUnitDeclaration unit, int i) {
    	super.process(unit, i);
//{ObjectTeams: callback all waiting tests
    	for (ICallbackClient cbc : this._callBacks)
			cbc.callback(unit);
//ike}					
    }
}
