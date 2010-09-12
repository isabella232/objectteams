/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
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
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression.AbstractQualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;

/**
 * This class wraps a qualified allocation in order to defer the decision
 * whether or not to translate it into a role creator call.
 * This decision can only be made after the receiver (lhs of dot) is resolved.
 *
 * @author stephan
 * @version $Id: QualifiedAllocationExpression.java 23401 2010-02-02 23:56:05Z stephan $
 */
public abstract class OTQualifiedAllocationExpression extends AbstractQualifiedAllocationExpression {
    /** if this is set, we are using the translated version. */
    private MessageSend creatorCall = null;

    private Runnable preGenerateTask = null; // an optional task to be performed at the start of generateCode.

    /**
     * Wrap a given allocation expression.
     */
    public OTQualifiedAllocationExpression(TypeDeclaration anonymousType) {
    	super(anonymousType);
    }

    public OTQualifiedAllocationExpression() {
    	super();
    }


    /** Simply dispatch. */
    public FlowInfo analyseCode(
        BlockScope currentScope,
        FlowContext flowContext,
        FlowInfo flowInfo)
    {
        if (this.creatorCall == null)
            return super.analyseCode(currentScope, flowContext, flowInfo);
        else
            return this.creatorCall.analyseCode(currentScope, flowContext, flowInfo);
    }

    /** simply forward. */
    public Expression enclosingInstance() {
        return super.enclosingInstance();
    }

    /** Simply dispatch. */
    public void generateCode(
        BlockScope currentScope,
        CodeStream codeStream,
        boolean valueRequired)
    {
    	if (this.preGenerateTask != null)
    		this.preGenerateTask.run(); // transfer a local variable's resolvedPosition just before it is used for generating code
        if (this.creatorCall == null) {
            super.generateCode(currentScope, codeStream, valueRequired);
        } else {
       		this.creatorCall.generateCode(currentScope, codeStream, valueRequired);
        }
    }

    /**
	 * During resolve we make the decision which variant to use.
	 */
	public TypeBinding resolveType(BlockScope scope)
	{
	    this.constant = Constant.NotAConstant;

	    if (this.anonymousType == null && this.creatorCall == null) { // no double processing
	        if (this.enclosingInstance == null) // special case during code assist
	            return super.resolveType(scope);

        	if (this.enclosingInstance instanceof CastExpression)
				this.enclosingInstance.bits |= DisableUnnecessaryCastCheck; // will check later on (within super.resolveType())

	        TypeBinding enclosingInstanceType = this.enclosingInstance.resolveType(scope);

	        if (   !scope.isGeneratedScope()
	        	&& enclosingInstanceType != null
	        	&& enclosingInstanceType.isTeam())  // non reference types will trigger error reporting via super.resolveType()
	        {
	        	if (this.enclosingInstance instanceof NameReference) {
	        		final NameReference anchorRef = (NameReference)this.enclosingInstance;
	        		if (!((VariableBinding)anchorRef.binding).isFinal()) {

	        			// replace non-final anchor with fake-binding,
	        			// so that this type is not compatibly to anything else:
	        			char[] variableName = ((VariableBinding)anchorRef.binding).name;
	        			switch (anchorRef.bits & ASTNode.RestrictiveFlagMASK) {
	        			case Binding.LOCAL:
	        				final LocalVariableBinding localOrig = (LocalVariableBinding)anchorRef.binding;
	        				// mark the original as used before we procede with a fake copy:
							localOrig.useFlag = LocalVariableBinding.USED;
							anchorRef.binding = new LocalVariableBinding(variableName, enclosingInstanceType, ClassFileConstants.AccFinal, false)
							{
		        				@Override public int problemId() { return IProblem.AnchorNotFinal; }
		        			};
		        			this.preGenerateTask = new Runnable() { public void run() {
		        				// need to transfer this info from the real local to the fake one (don't have that info yet):
		        				((LocalVariableBinding)anchorRef.binding).resolvedPosition = localOrig.resolvedPosition;
		        			}};
		        			break;
	        			case Binding.FIELD:
	        				anchorRef.binding = new FieldBinding(variableName, enclosingInstanceType, ClassFileConstants.AccFinal, scope.referenceType().binding, Constant.NotAConstant)
	        				{
		        				@Override public int problemId() { return IProblem.AnchorNotFinal; }
		        			};
		        			break;
		        		default:
		        			throw new InternalCompilerError("Unexpected bits, neither local nor field "+anchorRef.bits+": "+anchorRef); //$NON-NLS-1$ //$NON-NLS-2$
	        			}
	        		}
	        	}

	            if (this.type.getTypeName().length > 1) {
	            	scope.problemReporter().roleCreationNotRelativeToEnclosingTeam(this);
	            	return null;
	            }

	            // now it's finally time to create the alternate version:
	            this.creatorCall = CopyInheritance.createConstructorMethodInvocationExpression(scope, this);
	            if (this.creatorCall == null)
	            	return null;
	        }
	    }
	    if (this.creatorCall == null) {
	        this.resolvedType = super.resolveType(scope);
	        // if enclosing is a role request a cast to the class part as required by the inner constructor
	        if (this.enclosingInstance != null) {
	        	TypeBinding enclosingType = this.enclosingInstance.resolvedType;
				if (enclosingType instanceof ReferenceBinding && ((ReferenceBinding)enclosingType).isDirectRole())
	        		this.enclosingInstanceCast = ((ReferenceBinding)enclosingType).getRealClass();
	        }
	        ReferenceBinding superType = null;
	        if (this.resolvedType instanceof ReferenceBinding)
	        	superType= ((ReferenceBinding)this.resolvedType).superclass();
	    	if (   superType != null
	    		&& (superType instanceof RoleTypeBinding))
	    	{
	    		RoleTypeBinding superRole = (RoleTypeBinding)superType;
		        if (superRole.hasExplicitAnchor())
		        	scope.problemReporter().extendingExternalizedRole(superRole, this.type);
	    	}
	    } else {  // === with creatorCall ===
	    	this.resolvedType = this.creatorCall.resolveType(scope);
	    	// when creating role nested instance, no cast of enclosing role needed in this branch,
	    	// because creator call is routed via the interface of the enclosing role.
	        if (this.resolvedType != null) {
	        	if (((ReferenceBinding)this.resolvedType).isAbstract())
	        	{
	        		if (!((ReferenceBinding)enclosingInstance().resolvedType).isAbstract())
	        			scope.problemReporter().abstractRoleIsRelevant(this, this.creatorCall.resolvedType);
	        	}
	        	if (this.resolvedType.isValidBinding()) {
	        		// FIXME(SH): remove cast unwrapping
	        		Expression createExpr = this.creatorCall;
	        		while (createExpr instanceof CastExpression) // may have been wrapped using CollectedReplacementsTransformer
	        			createExpr = ((CastExpression)createExpr).expression;

	        		this.binding = ((MessageSend)createExpr).binding; // store the method binding

	        		// using lift-ctor in a qualified way? (OTJDL 2.4.1(a))
	        		ReferenceBinding role = (ReferenceBinding)this.resolvedType;
	        		MethodBinding creator = this.binding;
	        		if (creator != null) {
	        			MethodBinding ctor = role.getExactConstructor(creator.parameters);
	        			if (Lifting.isLiftToConstructor(ctor, role))
	        				scope.problemReporter().qualifiedUseOfLiftingConstructor(ctor, this.creatorCall);
	        		}
	        	}
	        }
	    }
	    return this.resolvedType;
	}

    /** Simply dispatch. */
    public StringBuffer printExpression(int indent, StringBuffer output) {
        if (this.creatorCall == null)
            return super.printExpression(indent,output);
        else
            return this.creatorCall.printExpression(indent, output);
    }


    /** Simply dispatch. */
    public void traverse(ASTVisitor visitor, BlockScope scope)
    {
        if (this.creatorCall == null)
            super.traverse(visitor, scope);
        else
            this.creatorCall.traverse(visitor, scope);
    }

	public MethodBinding getMethodBinding() {
		if (this.creatorCall != null)
			return this.creatorCall.binding;
		return this.binding;
	}
}
