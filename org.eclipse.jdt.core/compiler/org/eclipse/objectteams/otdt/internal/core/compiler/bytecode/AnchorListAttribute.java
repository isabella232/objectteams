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
 * $Id: AnchorListAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding.IMethodProvider;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * MIGRATION_STATE: complete.
 *
 * Keep a list of all type anchors of one method signature
 * in order to make them persistent in class files.
 * Later when reading the byte code, anchored types are restored from this list.
 * Note that list positions are 1-based. 0 represents the return type.
 *
 * @author stephan
 * @version $Id: AnchorListAttribute.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AnchorListAttribute extends ListValueAttribute {

    private MethodBinding _method;
    private char[][]   _anchors;
    private static final char[] NO_ANCHOR = "_OT$NoAnchor".toCharArray(); //$NON-NLS-1$
    private static final String ARG_ANCHOR_PREFIX = "_OT$param"; //$NON-NLS-1$

    /**
     * Create an attribute from source:
     * @param method
     */
    public AnchorListAttribute(MethodBinding method) {
        super(IOTConstants.TYPE_ANCHOR_LIST, 0, 2);
        this._method = method;
        this._count = method.parameters.length+1;
    }

    /**
     * Create an attribute from byte code.
     *
     * (Invoked from AbstractAttribute.readAttribute(char[],MethodInfo,int,int,int[]))
     */
    public AnchorListAttribute(
            MethodInfo  info,
            int         readOffset,
            int         structOffset,
            int[]       constantPoolOffsets)
    {
        super(IOTConstants.TYPE_ANCHOR_LIST, 0, 2);
        this._methodInfo = info;
        readList(info, readOffset, structOffset, constantPoolOffsets);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#writeElementValue(int)
     */
    void writeElementValue(int i) {
        if (i==0) {
            writeElement(this._method.returnType);
        } else {
            writeElement(this._method.parameters[i-1]);
        }
    }

    /**
     * Write one list element: encode the type anchor as this:
     * (a) NO_ANCHOR    -> not an externalized role type
     * (b) name         -> best-name (path) to be resolved to a variable binding (see TeamAnchor.getBestName())
     * (c) _OT$param'n' -> argument 'n' of this method is the type anchor.
     *
     * @param type
     */
    private void writeElement(TypeBinding type) {
        if (type.isArrayType())
            type = type.leafComponentType();
        if (DependentTypeBinding.isDependentType(type)) {
            DependentTypeBinding roleType = (DependentTypeBinding)type;
            ITeamAnchor anchor = roleType._teamAnchor;
            if (anchor instanceof TThisBinding)
                writeName(NO_ANCHOR);
            else {
                if (roleType._argumentPosition > -1)
                    writeName((ARG_ANCHOR_PREFIX+roleType._argumentPosition).toCharArray());
                else
                    writeName(anchor.getBestName());
            }
        } else {
            writeName(NO_ANCHOR);
        }
    }

    /**
     * PHASE 1 of reading:
     * Read one list element from byte code: Read names and store
     * them in our array _anchors.
     *
     * (Invoked from MethodInfo.readDeprecatedAndSyntheticAttributes
     *     .. -> <init>(MethodInfo,int,int,int[]) -> ListValueAttribute.readList()
     * )
     */
    void read(int i)
    {
        if (i==0)
            this._anchors = new char[this._count][];

        char[] name = consumeName();
        if (!CharOperation.equals(name, NO_ANCHOR))
            this._anchors[i] = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute#evaluate(org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment)
     */
    public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
        assert(false);
    }

    /**
     * PHASE 2 or reading:
     * Once the method binding is created, store the anchor list into the method binding.
     */
    public boolean evaluate(MethodInfo info, MethodBinding methodBinding, LookupEnvironment environment)
    {
        if (info != this._methodInfo) return false;
        this._method = methodBinding;
        this._method.anchorList = this;
        return true;
    }

    /**
     *  PHASE 3 of reading:
     *  When types in the method signature are resolved, wrap them.
     *
     * (Hooked into RoleTypeBinding.wrapTypesInMethodSignature(..)).
     */
    public void wrapTypes(LookupEnvironment environment) {
        for (int i=0;i<this._count;i++) {
            if (this._anchors[i] != null) {
                if (i == 0)
                    this._method.returnType =
                        getWrappedType(
                           	this._method.returnType, this._anchors[i], this._method.declaringClass, this._method, environment);
                else
                    this._method.parameters[i-1] =
                        getWrappedType(
                        	this._method.parameters[i-1], this._anchors[i], this._method.declaringClass, this._method, environment);
            }
        }
    }

    /**
     * @param typeToWrap ReferenceBinding or array thereof
     * @param anchorName
     * @param site
     * @param declaringMethod where to look for arguments being used as type anchor.
     * @param environment
     * @return a wrapped version of typeToWrap
     */
    private TypeBinding getWrappedType(
    		TypeBinding typeToWrap, char[] anchorName,
			ReferenceBinding site, final MethodBinding declaringMethod, LookupEnvironment environment)
    {
    	assert !CharOperation.equals(anchorName, NO_ANCHOR) : "NO_ANCHOR should have been filtered out"; //$NON-NLS-1$

        ReferenceBinding type = (ReferenceBinding)typeToWrap.leafComponentType();
        if (CharOperation.prefixEquals(ARG_ANCHOR_PREFIX.toCharArray(), anchorName)) {
            // Type anchored to another argument:
            LocalVariableBinding anchor = new LocalVariableBinding(
                    anchorName, type.enclosingType(), 0, true); // name is irrelevant.
            // make sure this anchor can answer `anchor.declaringScope.referenceMethodBinding()`:
            anchor.declaringScope = new CallinCalloutScope(null, null) {
            	public MethodBinding referenceMethodBinding() {
            		return declaringMethod;
            	}
            };
            TypeBinding wrappedType = anchor.getRoleTypeBinding(type, typeToWrap.dimensions());

            // argument position is relevant:
            char[] tail = CharOperation.subarray(anchorName, ARG_ANCHOR_PREFIX.length(), -1);
            RoleTypeBinding wrappedRole = (RoleTypeBinding)wrappedType.leafComponentType();
			wrappedRole._argumentPosition = Integer.parseInt(String.valueOf(tail));
			wrappedRole._declaringMethod = new IMethodProvider() {
				public MethodBinding getMethod() { return declaringMethod; }
			};

            return wrappedType;
        } else {
        	return RoleTypeCreator.wrapTypeWithAnchorFromName(typeToWrap, anchorName, site, environment);
        }
    }

    /**
     * API: after resolving a method, check whether we need to store an
     * anchor list.
     *
     * @param decl
     */
    public static void checkAddAnchorList(AbstractMethodDeclaration decl) {
        if (decl.ignoreFurtherInvestigation)
            return;
        MethodBinding binding = decl.binding;
        if (binding.anchorList != null)
        	return;
        boolean relevantAnchorFound = false;
        if (binding.returnType != null)
        	relevantAnchorFound = hasRelevantAnchor(binding.returnType);
        int i=0;
        while (!relevantAnchorFound && i<binding.parameters.length) {
            relevantAnchorFound |= hasRelevantAnchor(binding.parameters[i++]);
        }
        if (relevantAnchorFound) {
            MethodModel model = MethodModel.getModel(decl);
            model.addAttribute(new AnchorListAttribute(binding));
        }
    }

    /**
     * Is `binding' an anchored type, whose anchor is not a TThisBinding?
     */
    private static boolean hasRelevantAnchor(TypeBinding binding) {
        if (binding.isArrayType())
            binding = binding.leafComponentType();
        if (binding.isBaseType())
            return false;
        ReferenceBinding refBinding = (ReferenceBinding)binding;
        if (!DependentTypeBinding.isDependentType(refBinding))
            return false;
        ITeamAnchor anchor = ((DependentTypeBinding)refBinding)._teamAnchor;
        return !(anchor instanceof TThisBinding);
    }

    /* (non-Javadoc)
     * @see org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ListValueAttribute#toString(int)
     */
    String toString(int i) {
        if (this._anchors != null) {
            if (this._anchors[i] == null)
                return "<>";             //$NON-NLS-1$
            return new String(this._anchors[i]);
        }
        if (i==0)
            return toString(this._method.returnType);
        else
            return toString(this._method.parameters[i-1]);
    }

    private String toString(TypeBinding type) {
        if (type instanceof RoleTypeBinding)
            return ((RoleTypeBinding)type)._teamAnchor.toString();
        return "<>"; //$NON-NLS-1$
    }
}
