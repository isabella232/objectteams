/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010 GK Software AG.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ParameterMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ResultReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;


/**
 * This visitor replaces SingleNameReference("result") in param mappings
 * with ResultReferences, provided they are on the correct side of the binding token. 
 *
 * @author stephan
 */
public class ReplaceResultReferenceVisitor
    extends StackTransformStatementsVisitor
    implements IOTConstants
{

    private final AbstractMethodMappingDeclaration _methodMapping;
    private int _bindingDirection    = 0; // while traversing a ParameterMapping record its direction here

    public ReplaceResultReferenceVisitor(AbstractMethodMappingDeclaration mapping) {
    	this._methodMapping = mapping;
    }

    public boolean visit(ParameterMapping mapping, BlockScope scope)
    {
        this._bindingDirection = mapping.direction;
        return true;
    }
    public void endVisit(ParameterMapping mapping, BlockScope scope)
    {
        this._bindingDirection = 0;
        super.endVisit(mapping, scope);
    }
    /**
     * Translate "result" in the expression of a parameter mapping.
     * Upgrade the SingleNameReference to a ResultReference.
     */
    public boolean visit(SingleNameReference ref, BlockScope scope)
    {
        if (CharOperation.equals(ref.token, IOTConstants.RESULT))
        {
            boolean isResultDir = false;
            if (this._methodMapping.isCallin()) {
                isResultDir = this._bindingDirection == TerminalTokens.TokenNameBINDOUT;
                if (   !isResultDir
                	&& 	((CallinMappingDeclaration)this._methodMapping).callinModifier
					     == TerminalTokens.TokenNameafter)
                {
                	isResultDir = true;
                }
            } else
                isResultDir = this._bindingDirection == TerminalTokens.TokenNameBINDIN;
            if (isResultDir) {
            	AstGenerator gen = new AstGenerator(ref.sourceStart, ref.sourceEnd);
                ResultReference resultRef = gen.resultReference(ref, this._methodMapping);
                enterExpression(ref, resultRef, ref);
            } else {
                scope.problemReporter().illegalDirectionForResult(ref, this._methodMapping.isCallout());
                return false;
            }
        }
        return true;
    }
}