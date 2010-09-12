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
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortType;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;


/**
 * MIGRATION_STATE: E3.2RC1a.
 *
 * Sole purpose of this visitor: Link local types to role models.
 *
 * @author stephan
 * @version $Id: TransformStatementsVisitor.java 12675 2006-06-22 00:31:42 +0000 (Thu, 22 Jun 2006) stephan $
 */
public class RecordLocalTypesVisitor
    extends StackTransformStatementsVisitor
    implements IOTConstants
{

    // -- manage scopes manually (block scopes are not yet created!) ---
    private Stack<ClassScope> _classScopeStack = new Stack<ClassScope>();


    // visit member type
    public boolean visit (TypeDeclaration type, ClassScope scope)
    {
        if (type.isTeam())
            if (StateHelper.hasState(type.binding, ITranslationStates.STATE_STATEMENTS_TRANSFORMED))
                return false; // don't descend again
        return true;
    }

    // ===  elements that may contain local types: ===

    @Override
    public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
    	this._classScopeStack.push(scope);
    	return true;
    }

    @Override
    public void endVisit(MethodDeclaration methodDecl, ClassScope scope) {
    	if (!this._classScopeStack.isEmpty())
    		this._classScopeStack.pop();
    }

    @Override
    public boolean visit(ConstructorDeclaration ctorDeclaration, ClassScope scope) {
    	this._classScopeStack.push(scope);
    	return true;
    }

    @Override
    public void endVisit(ConstructorDeclaration ctorDecl, ClassScope scope) {
    	if (!this._classScopeStack.isEmpty())
    		this._classScopeStack.pop();
    }

    @Override
    public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
    	if (scope == null)
    		return false; // no use in descending, also: don't pop existing scope by endVisit(FieldDecl)
    	ClassScope classScope = scope.classScope();
    	if (classScope != null)
    		this._classScopeStack.push(classScope);
    	return true;
    }

    @Override
    public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
    	if (!this._classScopeStack.isEmpty())
    		this._classScopeStack.pop();
    }

    /**
     * This is the pay-load of this visitor: add local types to the model of the enclosing role
     */
    public boolean visit(TypeDeclaration td, BlockScope scope) {
    	if((td.bits & ASTNode.IsLocalType)!=0)
    	{
			SourceTypeBinding enclosingType = null;
			TypeDeclaration enclosingTypeDecl = null;
			// find best guess for enclosing:
    		if (scope != null) {
    			enclosingType = scope.enclosingSourceType();
    			enclosingTypeDecl = scope.referenceType();
    		} else if (!this._classScopeStack.isEmpty()) {
    			ClassScope classScope = this._classScopeStack.peek();
    			if (classScope != null) { // someone pushed null?
    				enclosingTypeDecl = classScope.referenceContext;
    				enclosingType = enclosingTypeDecl.binding;
    			}
    		}
   			if (enclosingType != null)
			{
				if (enclosingType.isRole()) {
    				RoleModel model = enclosingType.roleModel;

    				// pre-set enclosing type, which would otherwise only be set during buildLocalType()
    				td.enclosingType = enclosingTypeDecl;
    				// create and link model, didn't know before that it is role-ish.
    				model.addLocalType(null, td.getRoleModel(model.getTeamModel()));
    			} else {
    				TypeModel model = enclosingType.model;
    				model.addLocalType(td);
    			}
			}
    		if (td.scope == null)
    			return false; // don't descend further, local type is not in a useful state yet.
    	}
        return true;
    }

    /** Replaces TypeDeclaration.traverse() which chickens out on ignoreFurtherInvestigation. */
	public void recordLocalTypesFor(TypeDeclaration type) {
		// adjusted COPY&PASTE from TypeDeclaration.traverse() tailored to only those
		// elements that can contain local types.
		try {
			if (this.visit(type, type.scope)) {
				if (type.memberTypes != null) {
					int length = type.memberTypes.length;
					for (int i = 0; i < length; i++)
						type.memberTypes[i].traverse(this, type.scope);
				}
				if (type.fields != null) {
					int length = type.fields.length;
					for (int i = 0; i < length; i++) {
						FieldDeclaration field;
						if ((field = type.fields[i]).isStatic()) {
							field.traverse(this, type.staticInitializerScope);
						} else {
							field.traverse(this, type.initializerScope);
						}
					}
				}
				if (type.methods != null) {
					int length = type.methods.length;
					for (int i = 0; i < length; i++)
						type.methods[i].traverse(this, type.scope);
				}
//{ObjectTeams:	method mappings: (yes: local types within parameter mappings!
				if (type.callinCallouts != null) {
					int callinCalloutsLength = type.callinCallouts.length;
					for (int i = 0; i < callinCalloutsLength; i++)
						type.callinCallouts[i].traverse(this, type.scope);
				}
// SH}
			}
			this.endVisit(type, type.scope);
		} catch (AbortType e) {
			// silent abort
		}
	}
}