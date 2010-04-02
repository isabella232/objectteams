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
 * $Id: PotentialLowerExpression.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lowering;

/**
 * NEW for OTDT.
 *
 * This class wraps an expression which may or may not require lowering.
 *
 * Note: all elements generated in Lowering.lowerExpression must be resolved
 * manually, since PotentialLowerExpression must already resolve the expression.
 * So the wrapping expression can not use resolveType(..) again.
 *
 * @author stephan
 * @version $Id: PotentialLowerExpression.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class PotentialLowerExpression extends PotentialTranslationExpression {


    /**
     * Create a wrapper for an expression to defer the decission of lowering until
     * resolveType(..).
     * @param expression expression to be wrapped
     * @param expectedType what the context expects from this expression
     */
    public PotentialLowerExpression(
            Expression  expression,
            TypeBinding expectedType)
    {
        super(expression, expectedType);
        this.operator     = "lower"; //$NON-NLS-1$
    }

    public TypeBinding resolveType(BlockScope scope)
    {
        TypeBinding rawType = this.expression.resolveType(scope);
        if (rawType == null)
        	return null; // no chance
        this.resolvedType = this.expectedType; // be optimistic.
        this.checked = true;

        if (rawType.isBaseType() || this.expectedType.isBaseType()) {
        	// perhaps an auto(un)boxing instead of lowering?
        	if (scope.isBoxingCompatibleWith(rawType, this.expectedType))
	       		return rawType;
        }

       	TypeBinding compatibleType = compatibleType(scope, rawType);
        if (compatibleType != null)
        	return compatibleType;

        TypeBinding roleSideType;
        TypeBinding baseType;
        if (rawType.isArrayType()) {
            if (!(this.expectedType instanceof ArrayBinding))
                throw new InternalCompilerError("mapping array to scalar"); //$NON-NLS-1$
            roleSideType = rawType.leafComponentType();
            baseType     = this.expectedType.leafComponentType();
        } else {
            roleSideType = rawType;
            baseType     = this.expectedType;
        }
        boolean incompatibilityFound = false;
        ReferenceBinding roleType = null;
        if (!(roleSideType instanceof ReferenceBinding)) {
            incompatibilityFound = true;
        } else {
            roleType = (ReferenceBinding)roleSideType;
            if (   !roleType.isDirectRole()
                || !(baseType instanceof ReferenceBinding))
                incompatibilityFound = true;
        }
        if (incompatibilityFound)
        	return reportIncompatibility(scope, rawType);

        boolean oldLower = Config.getLoweringRequired();
        try {
        	Config.setLoweringRequired(false);
	        if (!roleType.baseclass().isCompatibleWith(baseType)) {
	            scope.problemReporter().typeMismatchErrorPotentialLower(
	                    this.expression, rawType, this.expectedType, baseType);
	            this.resolvedType = null;
	            return null;
	        }
	        if (Config.getLoweringRequired())
	            throw new InternalCompilerError("Compiler incomplete: unexpected base: is a role, too"); //$NON-NLS-1$
        } finally {
        	Config.setLoweringRequired(oldLower);
        }


        // successfully recognized the need for lowering, create the lowering now:
        this.rawExpression = this.expression;
        this.operator = "lower"; // redundant; //$NON-NLS-1$
        this.expression = new Lowering().lowerExpression(scope, this.expression, rawType, this.expectedType, true/*needNullCheck*/);
        return this.resolvedType = this.expression.resolvedType = this.expectedType;
    }
    @Override
    public void computeConversion(Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType)
    {
    	this.expression.computeConversion(scope, runtimeType, compileTimeType);
    }
}
