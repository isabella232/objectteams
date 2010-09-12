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
 * $Id: MethodMappingImplementor.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.mappings;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReplaceResultReferenceVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * moved here from package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.resolve
 *
 * Generalize some functions of implementing callout and callin.
 *
 * @author stephan
 * @version $Id: MethodMappingImplementor.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class MethodMappingImplementor {

	// discriminate callout/in by this flag:
	int bindingDirection;

	/**
	 * Make arguments for message send which implements a callout or a callin wrapper.
	 * For a callout mapping this method is used only if signatures are given
	 * (and possible param-mappings).
	 *
	 * @param methodMapping here the method mapping is declared.
	 * @param wrapperMethodDeclaration provided parameters are taken from here
	 * @param sourceMethodSpec <ul>
	 * 			<li>look here for param-positions (see AbstractMethodMappingDeclaration.positions)
	 * 			<li>and check the need of lifting.</ul>
	 * @param isFieldAccess
	 * 			if we don't call a method but access a field
	 * 			which is emulated by a static base method.
	 * @param hasResultArgument
	 * 			has a "result" argument been added to the wrapper?
	 *          (used to access the base-method-result within a after-callin (param-mapping+predicate))
	 */
	Expression[] makeWrapperCallArguments(
			AbstractMethodMappingDeclaration methodMapping,
			MethodDeclaration      wrapperMethodDeclaration,
			MethodSpec             sourceMethodSpec,
			boolean                isFieldAccess,
			boolean                hasResultArgument)
	{
		// prepare parameter mappings:
		if (methodMapping.mappings != null) {
			methodMapping.traverse(new ReplaceResultReferenceVisitor(methodMapping), methodMapping.scope.classScope());
			if (methodMapping.isReplaceCallin())
				((CallinMappingDeclaration)methodMapping).checkResultMapping();
		}
		
	    Argument[]   wrapperMethodArguments = wrapperMethodDeclaration.arguments;
	    Expression[] arguments;

	    boolean hasArgError = false;

	    // parameters of the implemented method drive the loop,
	    // additional provided arguments may be ignored.

	    // (Typechecking is left to regular resolve methods.)

	    TypeBinding[] implParameters = getImplementationParamters(
				methodMapping, wrapperMethodDeclaration);

    	int implementationArgLen = implParameters.length;

		int expressionsOffset = 0;
        if (isFieldAccess) {
        	// field access is mapped to static method with additional first parameter _OT$base (unless static):
        	if (!((FieldAccessSpec)methodMapping.getBaseMethodSpecs()[0]).isStatic())
        		expressionsOffset = 1;

        	ReferenceBinding baseType = methodMapping.scope.enclosingSourceType().baseclass();
        	arguments = new Expression[implementationArgLen+expressionsOffset];
        	if (expressionsOffset > 0) {
	        	// TODO(SH): generalize this and the corresponding statement in
	        	//           CalloutImplementor.makeArguments().
	        	// cast needed against weakened _OT$base reference.
	        	MethodSpec baseSpec = ((CalloutMappingDeclaration)methodMapping).baseMethodSpec;
	        	AstGenerator gen = new AstGenerator(baseSpec);
	    		arguments[0] = new CastExpression(
						gen.singleNameReference(IOTConstants._OT_BASE),
						gen.baseclassReference(baseType),
						baseType.isRole() ? CastExpression.NEED_CLASS : CastExpression.RAW); // FIXME (see also CalloutImplementor.makeArguments)
        	}
        } else {
        	arguments = new Expression[implementationArgLen];
        }

    	// bind argument because getArgument might want to link bestnames
        // (need to bind all arguments in order!)
        if (   wrapperMethodArguments != null
        	&& wrapperMethodDeclaration.binding.parameters != Binding.NO_PARAMETERS)
	    	for (int idx = 0; idx < wrapperMethodArguments.length; idx++)
	    		wrapperMethodArguments[idx].bind(
	        			wrapperMethodDeclaration.scope,
						wrapperMethodDeclaration.binding.parameters[idx],
						/*used*/false);

        for(int idx = 0; idx < implementationArgLen; idx++)
	    {
	        arguments[idx+expressionsOffset] = getArgument(
								                methodMapping,
								                wrapperMethodDeclaration,
												implParameters,
								                idx,
												hasResultArgument,
												sourceMethodSpec);
	        if (arguments[idx+expressionsOffset] == null)
	            hasArgError = true;    // keep going to check usage of arguments in mapping
	    }

	    if (hasArgError) {
	        wrapperMethodDeclaration.statements = new Statement[0]; // leave a clean state.
	        return null;
	    }
	    return arguments;
	}
	/**
	 * Get the parameters of the implemented method to be called from the wrapper.
	 * Overridden in CallinImplementor to account for signature enhancing
	 *
	 * @param methodMapping
	 * @param wrapperMethod
	 * @return types array
	 */
	TypeBinding[] getImplementationParamters(AbstractMethodMappingDeclaration methodMapping, MethodDeclaration wrapperMethod)
	{
		return methodMapping.getImplementationMethodSpec().resolvedParameters();
	}

	/** If original is a type variable or contains a type variable replace that type variable
	 *  with a corresponding type variable from variables.
	 *  This method is used to adjust the scope of type variables that originally were
	 *  resolved in the method mappings scope, but should be resolved in the wrapper method scope.
	 */
	TypeBinding substituteVariables(TypeBinding original, TypeVariableBinding[] variables) {
		if (original.isTypeVariable()) {
			for (int i = 0; i < variables.length; i++)
				if (CharOperation.equals(original.internalName(), variables[i].sourceName))
					return variables[i];
		} else if (original.isParameterizedType()) {
			ParameterizedTypeBinding pt= (ParameterizedTypeBinding)original;
			TypeBinding[] args= pt.arguments;
			if (args != null) {
				int l= args.length;
				System.arraycopy(args, 0, args= new TypeBinding[l], 0, l);
				boolean changed= false;
				for (int i= 0; i < l; i++) {
					TypeBinding tb= substituteVariables(args[i], variables);
					if (tb != args[i]) {
						args[i] = tb;
						changed= true;
					}
				}
				if (changed)
					return new ParameterizedTypeBinding((ReferenceBinding)pt.erasure(), args, pt.enclosingType(), pt.environment);
			}
		}
		return original;
	}

	/**
	 * Map one argument to yield the expression to pass to the implemented method.
	 *
	 * @param methodMapping      lookup method spec and parameter mapping here
	 * @param wrapperDeclaration use args from this method if no mapping is involved
	 * @param implParameters     parameters of the implemented method to invoke
	 * @param idx                argument position on the target side
     * @param hasResultArgument  as a 'result' argument been prepended to the wrapper args?
	 * @param sourceMethodSpec   this signature defines the provided args
	 * @return an argument expression
	 */
	Expression getArgument(
			AbstractMethodMappingDeclaration methodMapping,
			MethodDeclaration                wrapperDeclaration,
			TypeBinding[]                    implParameters,
			int                              idx,
			boolean                          hasResultArgument,
			MethodSpec                       sourceMethodSpec)
	{return null;}

	/**
	 * Make a copy of the given "arguments"
	 * @param arguments types for arguments to be generated
	 * @param declaredMethodSpec try to lookup names and positions from here.
	 * @return array of arguments with given types an names:
	 *         methodSpec-argument name if available or OT$argN (N ascending)
	 */
	protected Argument[] copyArguments(AstGenerator gen, Scope scope, TypeBinding[] arguments, MethodSpec declaredMethodSpec)
	{
	    if(arguments == null || arguments.length == 0)
	    	return null;

		ITeamAnchor baseSideAnchor = RoleTypeCreator.getPlayedByAnchor(scope);

		boolean useDeclaredArgs = (declaredMethodSpec.arguments != null) && (declaredMethodSpec.arguments.length == arguments.length);
		Argument[] result = new Argument[arguments.length];
		for (int idx = 0; idx < arguments.length; idx++)
	    {
			char[] argName;
			long pos = (((long)declaredMethodSpec.sourceStart)<<32) + declaredMethodSpec.sourceEnd;
	    	int argModifiers = ClassFileConstants.AccFinal; // in case an argument serves as a type anchor.
			if(useDeclaredArgs) {
				Argument argument = declaredMethodSpec.arguments[idx];
				argName = argument.name;
	        	pos     = (((long)argument.sourceStart) << 32) + argument.sourceEnd;
	        	argModifiers = argument.modifiers;
			} else {
				argName = (IOTConstants.OT_DOLLAR_ARG + idx).toCharArray();
			}
			TypeBinding argType = arguments[idx];
	    	TypeReference argTypeReference = null;
			if (this.bindingDirection == TerminalTokens.TokenNameBINDIN) { // only relevant for callin
				argTypeReference = getAnchoredTypeReference(gen, baseSideAnchor, argType);
			}
			if (argTypeReference == null) {
				argTypeReference = gen.typeReference(argType);
			}
	    	// don't use AstGenerator: hand-crafted source position:
	        result[idx] = new Argument(argName, pos, argTypeReference, argModifiers);
	    }
		return result;
	}
	/**
	 * Try whether type is a role type to be interpreted relative to the anchor of a "playedBy t.R"
	 * @param gen
	 * @param baseSideAnchor
	 * @param type
	 * @return an anchored type reference or null
	 */
	TypeReference getAnchoredTypeReference(AstGenerator gen, ITeamAnchor baseSideAnchor, TypeBinding type)
	{
		TypeReference argTypeReference = null;
		int dims = type.dimensions();
		type = type.leafComponentType();
		if (type instanceof RoleTypeBinding) {
			RoleTypeBinding roleTypeArg = (RoleTypeBinding)type;
			if (roleTypeArg.hasExplicitAnchor())
			{
				// originally anchored to _OT$base? change to 'base' now:
				if (roleTypeArg._teamAnchor.isBaseAnchor())
				{
					// witness: 4.3.1-otjld-parameter-mapping-11a
					TypeReference anchorRef = new TypeAnchorReference(
						gen.singleNameReference(IOTConstants.BASE), gen.sourceStart);
					argTypeReference = new ParameterizedSingleTypeReference(
							roleTypeArg.internalName(),
							new TypeReference[] {anchorRef},
							dims,
							gen.pos);
				}
			} else if (baseSideAnchor != null) {
				// witness in 1.1.22-otjld-layered-teams-1, B.1.1-otjld-sh-42
				ReferenceBinding baseSideTeam = (ReferenceBinding)baseSideAnchor.getResolvedType();
				if (TeamModel.isTeamContainingRole(baseSideTeam, roleTypeArg))
					argTypeReference = gen.roleTypeReference(baseSideAnchor, roleTypeArg, dims);
			}
		}
		return argTypeReference;
	}

	/**
	 * Generate a single name argument expression.
	 * @param argName
	 * @param targetMethodSpec (just for source positions ..)
	 * @return an argument reference
	 */
	Expression genSimpleArgExpr(char[] argName, MethodSpec targetMethodSpec) {
		AstGenerator gen = new AstGenerator(targetMethodSpec.sourceStart, targetMethodSpec.sourceEnd);
		return gen.singleNameReference(argName);
	}
	protected TypeParameter[] getTypeParameters(boolean hasSignature, MethodBinding rrrBinding, MethodSpec roleMethodSpec,
			final AstGenerator gen) {
				TypeParameter[] typeParams = null;
				if (hasSignature) {
					// from method spec:
					typeParams= roleMethodSpec.typeParameters;
					if (typeParams != null)
						typeParams= AstClone.copyTypeParameters(typeParams);
				} else {
					// from role method:
					TypeVariableBinding[] typeVariables = rrrBinding.typeVariables;
					if (typeVariables != Binding.NO_TYPE_VARIABLES) {
						typeParams= new TypeParameter[typeVariables.length];
						for (int i = 0; i < typeVariables.length; i++)
							typeParams[i]= gen.typeParameter(typeVariables[i]);
					}
				}
				return typeParams;
			}


}
