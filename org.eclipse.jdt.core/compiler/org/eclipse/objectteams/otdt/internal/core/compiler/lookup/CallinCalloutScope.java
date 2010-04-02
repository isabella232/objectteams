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
 * $Id: CallinCalloutScope.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;

/**
 * NEW for OTDT.
 *
 * @author Markus Witte
 *
 */
public class CallinCalloutScope extends MethodScope {

	public CallinCalloutScope(ClassScope parent, ReferenceContext context) {
		super(parent, context, false); // non-static
        this.kind = BINDING_SCOPE;
		this.referenceContext = context;
	}

	/* Error management:
	 * 		keep null for all the errors that prevent the binding to be created
	 * 		otherwise return a correct binding binding (but without the element
	 *		that caused the problem)
	 */
	public CallinCalloutBinding createBinding(AbstractMethodMappingDeclaration bindingDeclaration) {

		// is necessary to ensure error reporting
		this.referenceContext = bindingDeclaration;
		bindingDeclaration.scope = this;
		SourceTypeBinding declaringClass = referenceType().binding;

		if(bindingDeclaration instanceof CallinMappingDeclaration){
			CallinMappingDeclaration callinBindingDeclaration = (CallinMappingDeclaration)bindingDeclaration;
			bindingDeclaration.binding =
				new CallinCalloutBinding(declaringClass, callinBindingDeclaration);
		} else {
			CalloutMappingDeclaration calloutBindingDeclaration = (CalloutMappingDeclaration)bindingDeclaration;
			int calloutModifier = 0;
			if (calloutBindingDeclaration.baseMethodSpec instanceof FieldAccessSpec)
				calloutModifier = ((FieldAccessSpec)calloutBindingDeclaration.baseMethodSpec).calloutModifier;

			bindingDeclaration.binding =
				new CallinCalloutBinding(
				calloutBindingDeclaration.isCalloutOverride(),null, declaringClass, calloutModifier, calloutBindingDeclaration.declaredModifiers);
		}
		TypeParameter[] typeParameters = bindingDeclaration.roleMethodSpec.typeParameters;
	    // do not construct type variables if source < 1.5
		if (typeParameters == null || compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) {
		    bindingDeclaration.binding.typeVariables = Binding.NO_TYPE_VARIABLES;
		} else {
			bindingDeclaration.binding.typeVariables = createTypeVariables(typeParameters, bindingDeclaration.binding);
		}

        //methods specs are resolved later;
        // see MethodMappingResolver.resolveCall{in.out}Mapping()
		return bindingDeclaration.binding;
	}

	@Override // only make visible to MethodSepc.
	public boolean connectTypeVariables(TypeParameter[] typeParameters, boolean checkForErasedCandidateCollisions) {
		return super.connectTypeVariables(typeParameters, checkForErasedCandidateCollisions);
	}

	public TypeBinding getType(char[] name) {
		AbstractMethodMappingDeclaration mapping = (AbstractMethodMappingDeclaration)this.referenceContext;
		if (mapping != null && mapping.binding != null && mapping.binding.isValidBinding()) {
			TypeVariableBinding typeVariable = mapping.binding.getTypeVariable(name);
			if (typeVariable != null)	return typeVariable;
		}
		return this.parent.getType(name); // for role files delegate to the OTClassScope (single name only)
	}

	@Override
	public MethodBinding referenceMethodBinding() {
		if (this.referenceContext instanceof CalloutMappingDeclaration) {
			return ((CalloutMappingDeclaration) this.referenceContext).roleMethodSpec.resolvedMethod;
		}
		return super.referenceMethodBinding();
	}

	/** API for non-compiler clients like selection and semantic highlighting:
     * Within a parameter mapping a reference may internally be resolved to a local variable.
	 * This method allows to retrieve the semantically referenced element (field)
	 * that should be identified in the UI with this reference.
	 */
	public static Binding maybeReResolveReference(SingleNameReference singleNameReference, Binding binding) {
		if (!(binding instanceof LocalVariableBinding))				 		// local
			return binding;
		LocalVariableBinding localBinding = (LocalVariableBinding)binding;
		if (   localBinding.declaringScope != null 		
			&& localBinding.declaringScope.isMethodMappingWrapper()) 		// defined in a method mapping wrapper
		{
			// hide internal local var but re-resolve:
			CallinCalloutScope mappingScope = ((MethodScope)localBinding.declaringScope).getDeclaringMappingScope();
			if (mappingScope != null)
				binding = mappingScope.getSurfaceBinding(singleNameReference, singleNameReference.baseclassDecapsulation.isAllowed());
		}
		return binding;
	}

	private Binding getSurfaceBinding(SingleNameReference nameReference, boolean isBaseSide) {
		if (isBaseSide) {
			for (MethodSpec spec: ((AbstractMethodMappingDeclaration)this.referenceContext).getBaseMethodSpecs())
				if (spec instanceof FieldAccessSpec) {
					if (CharOperation.equals(((FieldAccessSpec)spec).selector, nameReference.token))
						return ((FieldAccessSpec)spec).resolvedField;
				} else if (spec.arguments != null) {
					for (Argument arg : spec.arguments)
						if (CharOperation.equals(arg.name, nameReference.token))
							return arg.binding;
				}
		} else {
			MethodSpec spec = ((AbstractMethodMappingDeclaration)this.referenceContext).roleMethodSpec;
			if (spec.arguments != null)
				for (Argument arg : spec.arguments)
					if (CharOperation.equals(arg.name, nameReference.token))
						return arg.binding;
		}
		if (isBaseSide)
			return enclosingSourceType().baseclass().getField(nameReference.token, true);
		else
			return enclosingSourceType().getField(nameReference.token, true);
	}
}
