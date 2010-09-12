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
 * $Id: AnchorMapping.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.IDependentTypeSubstitution;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * NEW for OTDT.
 *
 * Record mappings regarding type anchors for one MessageSend or AllocationExpression.
 * Given an array of argument-expressions, it aids in finding a matching method.
 * For matching, the parameters of a candidate method are instantiated, ie.,
 * type anchors are filled in from the given arguments.
 *
 * We actually keep a stack (realized by link _previous) of AnchorMappings.
 * There may be nesting caused, eg., by following chain
 *    RoleTypeBinding.isCompatibleWith()
 * 	  -> TeamAnchor.hasSameBestNameAs()
 *       -> FieldBinding.resolveInitIfNeeded()
 * 	               which may resolve, eg., a MessageSend/AllocationExpression etc.
 *
 * @author stephan
 * @version $Id: AnchorMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AnchorMapping {

    // For each thread an instance can be registered:
	/*
	 * Note: this static ThreadLocal is OK since we manually cleanup using removeCurrentMapping().
	 */
    private static final ThreadLocal<AnchorMapping> currentMappings = new ThreadLocal<AnchorMapping>();

    public static boolean isDefined() {
    	return currentMappings.get() != null;
    }
    /**
     * When resolving a MessageSend or an AllocationExpression or similar,
     * some information is registered as to enable instantiation of parameters
     * when looking for a matching method.
     * Previously anchor mappings are saved in the _previous chain.
     *
     * @param receiver  receiver in the MessageSend or null
     * @param arguments actual expressions (typed as Statement because we use MethodSpec.arguments as faked expressions).
     * @param scope     context holding the current invocation expression
     * @return an instance that has been registerd as currentMapping.
     */
    public static AnchorMapping setupNewMapping(
            Expression   receiver,
            Statement[]  arguments,
            Scope   scope)
    {
    	AnchorMapping previous = currentMappings.get();

    	if (receiver instanceof CastExpression)
    		receiver = ((CastExpression)receiver).expression; // extract the real receiver.
        AnchorMapping newMapping = new AnchorMapping(receiver, arguments, scope);
        newMapping._previous = previous; // could be null.
		currentMappings.set(newMapping);
        return newMapping;
    }

    public static void removeCurrentMapping(AnchorMapping current)
    {
    	currentMappings.set(null); // safe for error scenarii
    	if (current != null)
    		currentMappings.set(current._previous); // restore or remove (if previous == null)
    }

    public static void allowInstantiation(boolean allow) {
 	    AnchorMapping currentMapping = currentMappings.get();
 	    if (currentMapping != null)
 	    	currentMapping._allowInstantiation = allow;
    }

    /**
     * Using a previously registered AnchorMapping (see setupNewMapping) instantiate
     * a list of parameters. This means to propagate type anchors into all role types.
     * @param scope          For resolving names.
     * @param parameters     formal parameters of 'currentMethod' - to be instantiated from this anchor mapping
     * @param currentMethod  a candidate for the current method call - can be null when checking method mappings 
     * @return new array or 'parameters'
     */
   public static TypeBinding[] instantiateParameters(Scope scope, TypeBinding[] parameters, MethodBinding currentMethod)
   {
	   AnchorMapping currentMapping = currentMappings.get();
	   if (currentMapping == null || !currentMapping._allowInstantiation)
	   		return parameters;
       return currentMapping.internalInstantiateParameters(scope, parameters, currentMethod);
   }

   private TypeBinding[] internalInstantiateParameters(Scope scope, TypeBinding[] parameters, MethodBinding currentMethod)
   {
	   if (this._arguments != null && this._arguments.length != parameters.length)
		   return parameters; // don't bother to instantiate if lenghts disagree (=> varargs and param-anchored types don't work together)
	   
   	   if (scope == null)
   	   	   scope = this._scope; // scope would be more specific but _scope is OK, too.
       TypeBinding[] newParams = null;
       boolean isMethodEnhanced = currentMethod != null && currentMethod.isCallin();
       int start = isMethodEnhanced ? MethodSignatureEnhancer.ENHANCING_ARG_LEN : 0; // don't map enhancement args, have no source expr.
       for (int i=start; i<parameters.length; i++)
       {
    	   TypeBinding newParameter = instantiateParameter(scope, parameters[i], i, currentMethod, isMethodEnhanced);
    	   if (newParameter != null && newParameter != parameters[i]) {
    		   if (newParams == null) {
    		       newParams = new TypeBinding[parameters.length];
    		       System.arraycopy(parameters, 0, newParams, 0, parameters.length);
    		   }
    		   newParams[i] = newParameter;
    	   }
       }
       if (newParams != null)
    	   return newParams;
       return parameters;
   }
   // PRE: this._arguments != null => this._arguments.length == parameters.length 
   private TypeBinding instantiateParameter(final Scope scope, final TypeBinding parameter, final int i, final MethodBinding currentMethod, final boolean isMethodEnhanced)
   {
	   return RoleTypeCreator.deepSubstitute(parameter, scope.environment(), 
			   	new IDependentTypeSubstitution() {
					@SuppressWarnings("synthetic-access")
					public TypeBinding substitute(DependentTypeBinding paramDependentType, TypeBinding[] typeArguments, int dimensions) {
						int srcIdx = isMethodEnhanced ? i-MethodSignatureEnhancer.ENHANCING_ARG_LEN : i;
						ITeamAnchor anchor = null;
						if (AnchorMapping.this._arguments != null)
							anchor = translateAnchor(scope, AnchorMapping.this._arguments[srcIdx], paramDependentType, currentMethod);
						
						// missing a required anchor?
						if (anchor == null && paramDependentType.hasAnchorWithinThisMethodsSignature(currentMethod))
							return new ProblemReferenceBinding(paramDependentType.sourceName(), paramDependentType, ProblemReasons.AnchorNotFound);
						
						if (anchor == null && AnchorMapping.this._receiver != null)
						{
							if (DependentTypeBinding.isDependentTypeOf(
									AnchorMapping.this._receiver.resolvedType,
									paramDependentType._teamAnchor))
							{
								DependentTypeBinding depReceiver = (DependentTypeBinding)AnchorMapping.this._receiver.resolvedType;
								return depReceiver._teamAnchor.getRoleTypeBinding(paramDependentType, typeArguments, dimensions);
							}
							ITeamAnchor newAnchor = TeamAnchor.getTeamAnchor(AnchorMapping.this._receiver);
							if (   newAnchor != null
									&& newAnchor.isValidBinding()
									&& newAnchor != paramDependentType._teamAnchor)
							{
								if (paramDependentType._teamAnchor.isPrefixLegal(scope.enclosingSourceType(), newAnchor))
								{
									newAnchor = paramDependentType._teamAnchor.setPathPrefix(newAnchor);
									return newAnchor.getRoleTypeBinding(paramDependentType, typeArguments, dimensions);
								}
							}
							if (DependentTypeBinding.isDependentTypeVariable(currentMethod.parameters[i])) {
								TypeVariableBinding typeVariable = (TypeVariableBinding) ((DependentTypeBinding)currentMethod.parameters[i]).type; 
								ITeamAnchor[] anchors = typeVariable.anchors;
								if (anchors != null && anchors[0] instanceof LocalVariableBinding) { // FIXME(SH): more positions?
									int pos = ((LocalVariableBinding)anchors[0]).resolvedPosition;
									if (pos < AnchorMapping.this._arguments.length)
										anchor = translateAnchor(scope, AnchorMapping.this._arguments[pos], (DependentTypeBinding) currentMethod.parameters[i], currentMethod);
								}
							}
						}
						if (isValidAnchor(anchor)) {
							// downgrading allowed:
							return anchor.getRoleTypeBinding(paramDependentType, typeArguments, dimensions);
						}
						return paramDependentType;
					}
				});
   }
   
   private ITeamAnchor translateAnchor(Scope scope, ASTNode typedNode, DependentTypeBinding paramDependentType, MethodBinding currentMethod) 
   {
	   ProblemReporter problemReporter = scope != null ? scope.problemReporter() : null;
	   ITeamAnchor anchor = null;
	   if (paramDependentType.hasAnchorWithinThisMethodsSignature(currentMethod) && this._arguments != null)
	   {
	       // anchored to an argument:
	       Statement anchorExpr = this._arguments[paramDependentType._argumentPosition];
	       ReferenceBinding roleType = paramDependentType.getRealType();
	       if (anchorExpr instanceof Expression)
	       		anchor = RoleTypeCreator.getAnchorVariableBinding(
	                    null,                   // site
						(Expression)anchorExpr, // anchor
						roleType,               // roleType
						problemReporter,		//
						typedNode);         	// typedNode
	       else // when resolving a method spec, we interpret an Argument as an anchor expression 
	       		anchor = ((Argument)anchorExpr).binding;

	   } else if (paramDependentType._teamAnchor instanceof TThisBinding)
	   {
	       // anchored to TThis (must be a real role):
		   RoleTypeBinding paramRoleType = (RoleTypeBinding)
		   			(paramDependentType instanceof WeakenedTypeBinding
		   			 ? paramDependentType.type
		   			 : paramDependentType);
	       ReferenceBinding tthisType = paramRoleType._staticallyKnownTeam;
	       ReferenceBinding site = scope != null ? scope.enclosingSourceType() : null;
	       ReferenceBinding roleRef = paramRoleType.getRealType();
	       // try to get anchor from _receiver:
	       if (  this._receiver != null
	       	   && TypeAnalyzer.isVariableRef(this._receiver))
	       {
	       	  boolean reportError = TeamModel.isTeamContainingRole(
	       	  		(ReferenceBinding)this._receiver.resolvedType.leafComponentType(),
					roleRef);
	          anchor = RoleTypeCreator.getAnchorVariableBinding(
	                site, this._receiver, paramRoleType,
	                reportError ? problemReporter : null,
	                null);
	       }
	       if (!isValidAnchor(anchor))
	          anchor = TThisBinding.getTThisForRole(roleRef, site);
	       if (isValidAnchor(anchor))
	       {
	       		if (!anchor.isTypeCompatibleWith(tthisType))
	          		anchor = null; // cancel if incompatible.
		   }
	   }
	return anchor;
}
	private boolean isValidAnchor(ITeamAnchor anchor) {
	   return
	   		(anchor != null)
		 && (anchor != RoleTypeBinding.NoAnchor)
		 && anchor.isValidAnchor();
   }

   /** Store successful instantiated parameters in the current mapping. */
   public static void storeInstantiatedParameters(MethodBinding candidate, TypeBinding[] parameters) {
	    AnchorMapping currentMapping = currentMappings.get();
		// just like in instantiateParameters we might or might not have a mapping:
	    if (currentMapping != null)
	    	currentMapping._instantiatedParameters.put(candidate, parameters);
	    // if currentMapping already has _instantiatedParameters
	    // method lookup will be ambiguous.
	    // => need not care about finding the right instantiatedParameters.
   }

   /**
    * Helper for SourceTypeBinding.getExactMethod: Role types are considered equal
    * if they have the same role, the same team and their anchors have the same bestname.
    * This method also considers an anchor mapping, if one is installed.
    * @param t1 formal parameter  - this one is to be translated using the anchor mapping
    * @param t2 actual argument type
    * @param currentMethod  a candidate for the current method call
    */
   public static boolean areTypesEqual(TypeBinding t1, TypeBinding t2, MethodBinding currentMethod) {
   		if (t1 == t2)
   			return true;
   		if (   t1 instanceof RoleTypeBinding
   			&& t2 instanceof RoleTypeBinding)
   		{
	   		AnchorMapping currentMapping = currentMappings.get();
	   		if (currentMapping != null)
	   			return currentMapping.areTypeEqual((RoleTypeBinding) t1, (RoleTypeBinding)t2, currentMethod);
   		}
   		return false;
   }

   private boolean areTypeEqual(RoleTypeBinding role1, RoleTypeBinding role2, MethodBinding currentMethod) 
   {
	   	ITeamAnchor anchor = translateAnchor(null/*scope*/, null/*node*/, role1, currentMethod);
	
		if (anchor != null)
			return role2.isSameType(role1, anchor);

   	    return false;
   }

    private AnchorMapping _previous = null; // realizes a stack of anchormappings.
    private Statement[]  _arguments = null; // either Expression or Argument
    private Expression   _receiver  = null;
    private Scope        _scope     = null;
    private HashMap<MethodBinding,TypeBinding[]> _instantiatedParameters = new HashMap<MethodBinding, TypeBinding[]>();
    private boolean       _allowInstantiation = true;

    private AnchorMapping(Expression receiver, Statement[] arguments, Scope scope)
    {
        this._arguments = arguments;
        this._receiver  = receiver;
        if (this._receiver instanceof ArrayReference)
        	this._receiver = ((ArrayReference)this._receiver).receiver;
        this._scope     = scope;
    }

    /**
     * After resolving a MessageSend check for all its arguments:
     * + Are all externalized roles anchored properly?
     * @param messageSend the message send being resolved
     * @param scope where does the send happen?
     * @return success.
     */
	public boolean checkInstantiatedParameters(MessageSend messageSend, Scope scope) {
		boolean success = true;
		boolean loweringPossible = Config.getLoweringPossible();
		Expression[] arguments = messageSend.arguments;
		TypeBinding[] parameters = this._instantiatedParameters.get(messageSend.binding);
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] instanceof RoleTypeBinding) {
					RoleTypeBinding roleType = (RoleTypeBinding)parameters[i];
					if (!roleType._teamAnchor.isFinal()) {
						scope.problemReporter().anchorPathNotFinal(arguments[i], roleType._teamAnchor, roleType.readableName());
						success = false;
						continue;
					}
					if (   roleType.hasExplicitAnchor()
						&& !roleType.isPublic()
						&& !arguments[i].getBaseclassDecapsulation().isAllowed())
					{
						scope.problemReporter().externalizingNonPublicRole(arguments[i], roleType);
						success = false;
					}
				} else if (   loweringPossible 										 // if we detected lowering possible before, recheck here
						   && RoleTypeBinding.isRoleType(arguments[i].resolvedType)) 
				{
					((DependentTypeBinding)arguments[i].resolvedType).recheckAmbiguousLowering(parameters[i], arguments[i], scope, messageSend.binding);
				}
			}
		}
		return success;
	}

	public TypeBinding[] getInstantiatedParameters(MethodBinding selectedMethod) {
		if (this._instantiatedParameters.containsKey(selectedMethod))
			return this._instantiatedParameters.get(selectedMethod);
		return Binding.NO_PARAMETERS;
	}

	@SuppressWarnings("nls")
	public String toString()
	{
		String NULL = "null";
		StringBuffer out = new StringBuffer();
		out.append("[allowInstantiation: ");
		out.append(this._allowInstantiation);
		out.append(", scope kind: ");
		out.append(this._scope == null ? NULL : computeScopeKind(this._scope));
		out.append(", receiver: ");
		out.append(this._receiver == null ? NULL : this._receiver.toString());
		out.append(", arguments: ");
		if(this._arguments == null)
		{
			out.append(NULL);
		}
		else
		{
			for(int idx = 0; idx < this._arguments.length; idx++)
			{
				out.append(this._arguments[idx] == null ? NULL : this._arguments[idx].toString() + ", ");
			}
		}
		out.append(" instantiated parameters: ");
		if(this._instantiatedParameters == null)
		{
			out.append(NULL);
		}
		else
		{
			for (Entry<MethodBinding, TypeBinding[]> entry : this._instantiatedParameters.entrySet()) {
				out.append('\n');
				out.append(String.valueOf(entry.getKey().readableName()));
				TypeBinding[] parameters = entry.getValue();
				for(int idx = 0; idx < parameters.length; idx++)
				{
					out.append(parameters[idx] == null ? NULL : parameters[idx].toString() + ", ");
				}
			}
		}
		out.append("]");

		return out.toString();
	}

	@SuppressWarnings("nls")
	private String computeScopeKind(Scope scope)
	{
		String result;
		if(scope == null)
		{
			return "scope is null";
		}
		switch(scope.kind)
		{
			case Scope.BLOCK_SCOPE :
				result = "BlockScope";
				break;
			case Scope.METHOD_SCOPE :
				result = "MethodScope";
				break;
			case Scope.CLASS_SCOPE :
				result = "ClassScope";
				break;
			case Scope.COMPILATION_UNIT_SCOPE :
				result = "CompilationUnitScope";
				break;
			case Scope.BINDING_SCOPE :
				result = "BindingScope";
				break;
			default :
				result = "unknown scope type";
		}
		return result;
	}
}
