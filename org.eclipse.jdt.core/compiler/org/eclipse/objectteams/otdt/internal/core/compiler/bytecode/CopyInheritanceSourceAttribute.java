/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2009 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.bytecode;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;

/**
 *
 * @author stephan
 * @since 1.2.8 (previously a SingleValueAttribute sufficed).
 */
public class CopyInheritanceSourceAttribute extends AbstractAttribute {

	public char[] sourceMethodDesignator;
	// line number offset between original method and re-mapped copy:
	public int lineOffset;

    // for supporting deferred evaluation:
    private MethodBinding method;
    private LookupEnvironment environment;

	public CopyInheritanceSourceAttribute(char[] sourceMethodDesignator, int lineOffset) {
		super(COPY_INHERITANCE_SOURCE_NAME);
		this.sourceMethodDesignator = sourceMethodDesignator;
		this.lineOffset = lineOffset;
	}

	/**
	 * Create a new "CopyInheritanceSrc" attribute.
	 */
	public static AbstractAttribute copyInherSrcAttribute(MethodBinding srcMethod, MethodModel destModel)
	{
		if (srcMethod.declaringClass instanceof LocalTypeBinding)
			((LocalTypeBinding)srcMethod.declaringClass).computeConstantPoolName();
		char[] name = srcMethod.declaringClass.constantPoolName();//CharOperation.concatWith(srcMethod.declaringClass.compoundName, '.');
		name = CharOperation.append(name, '.');
		name = CharOperation.concat(name, srcMethod.selector);
		name = CharOperation.concat(name, srcMethod.signature());
		int offset = 0;
		MethodModel model = srcMethod.model;
		if (model != null)
			offset = model._lineOffset;
		// offset in destModel and attribute will be updated by BytecodeTransformer (needs full line number info of source)
		return new CopyInheritanceSourceAttribute(name, offset);
	}

	@Override
	int size() {
		return  6 + 6;  // constant length: name(2), lineOffset(4)
	}
	@Override
	public void write(ClassFile classFile) {
		super.write(classFile);
		writeName(this._name);
		writeInt(6);

		writeName(this.sourceMethodDesignator);
		writeInt(this.lineOffset);
        writeBack(classFile);
	}

	/**
	 *  Read a CopyInheritanceSrc from byte code.
	 */
	public static AbstractAttribute readcopyInherSrc(
			MethodInfo      info,
	        int             readOffset,
			int             structOffset,
	        int[]           constantPoolOffsets)
	{
	    int    idx        = info.u2At(readOffset);
		int    utf8Offset = constantPoolOffsets[idx] - structOffset;
	    char[] value      = info.utf8At(utf8Offset + 3, info.u2At(utf8Offset + 1));
	    int    lineOffset = 0;
	    if (info.u2At(readOffset-2) > 2) // old attribute length was 2 (without lineOffset)
	    	lineOffset    = (int)info.u4At(readOffset+2);
	    AbstractAttribute result = new CopyInheritanceSourceAttribute(value, lineOffset);
	    result._methodInfo = info;
	    return result;
	}

	public void evaluate(Binding binding, LookupEnvironment environment, char[][][] missingTypeNames) {
		/* noop */
	}

    /** Evaluate method level attributes */
    public boolean evaluate(MethodInfo info, MethodBinding method, LookupEnvironment environment) {
    	if (this._methodInfo != info) return false;
		MethodModel.getModel(method)._lineOffset = this.lineOffset;
		TypeModel model = null;
		ReferenceBinding declaringClass = method.declaringClass;
		if (declaringClass.isRole()) {
    		if (   declaringClass.roleModel == null
    			&& declaringClass.enclosingType() instanceof MissingTypeBinding)
    			return false; // what's wrong here??
    		model = declaringClass.roleModel;
		} else if (declaringClass.isTeam()) {
			model = declaringClass.getTeamModel();
		} else {
			throw new InternalCompilerError("copied method in regular class?"); //$NON-NLS-1$
		}
		model.addAttribute(this); // defer evaluation
		this.method = method;
		this.environment = environment;
		return true;
    }

    public void evaluateLateAttribute(ReferenceBinding roleBinding, int state)
    {
		switch (state) {
		case ITranslationStates.STATE_ROLE_FEATURES_COPIED:
			break; // go aheady and evaluate
		case ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED:
			if (this.method == null) return; // can't work
			if (this.method.copyInheritanceSrc != null) return; // have already worked
			break;
		default:
			return; // only in above to states
		}

		// will need a method binding from a tsuper role:
		ReferenceBinding teamBinding = this.method.declaringClass.enclosingType();
		if (teamBinding != null && teamBinding.superclass() != null)
			Dependencies.ensureBindingState(teamBinding.superclass(), ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);
		else
			assert this.method.declaringClass.isLocalType(); // enclosing type not set for these.

		int dotPos = CharOperation.indexOf('.', this.sourceMethodDesignator);
		int lparPos = CharOperation.indexOf('(', this.sourceMethodDesignator);
		char[] selector  = CharOperation.subarray(this.sourceMethodDesignator, dotPos+1, lparPos);
		char[] signature = CharOperation.subarray(this.sourceMethodDesignator, lparPos, -1);
		ReferenceBinding type = this.environment.getTypeFromConstantPoolName(this.sourceMethodDesignator, 0, dotPos, false, null); // FIXME(GENERIC): determine last params!
		if (type == null)
			return;
		if (type instanceof UnresolvedReferenceBinding)
			type = resolveReferenceType(this.environment, (UnresolvedReferenceBinding)type);
		if (type instanceof MissingTypeBinding)
		{
			ProblemReporter problemReporter= this.environment.problemReporter;
			ReferenceBinding current= roleBinding;
			// search a source type to report against:
			while (current != null && current.isBinaryBinding())
				current= current.enclosingType();
			if (current != null) {
				Scope scope= ((SourceTypeBinding)current).scope;
				if (scope != null)
					problemReporter= scope.problemReporter();
			}
    		problemReporter.staleTSuperRole(roleBinding, type, new String(CharOperation.concat(selector, signature)));
    		return;
		}
		if (roleBinding.roleModel.hasTSuperRole(type)) {
    		Dependencies.ensureBindingState(type, ITranslationStates.STATE_LATE_ATTRIBUTES_EVALUATED);

    		if (this.method.isAnyCallin())
    			signature = MethodSignatureEnhancer.generalizeReturnInSignature(signature);
    		MethodBinding[] methods = type.getMethods(selector);
    		for (int i = 0; i < methods.length; i++) {
				if (CharOperation.equals(methods[i].signature(), signature)) {
					MethodBinding origin = methods[i].copyInheritanceSrc;
					if (origin == null)
						origin = methods[i];
					this.method.setCopyInheritanceSrc(origin);
		            return;
				}
			}
		} else {
			if (roleBinding.isBinaryBinding())
				return; // silently ignore ;-)
		}
		this.environment.problemReporter.staleTSuperRole(roleBinding, type, new String(CharOperation.concat(selector, signature)));
    }
}
