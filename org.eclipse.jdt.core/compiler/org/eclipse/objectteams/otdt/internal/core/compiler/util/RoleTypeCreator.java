/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2015 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleTypeCreator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.CompilationResult.CheckPoint;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLowerExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.DeclaredLifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.ArrayLowering;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ProblemAnchorBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance.CopyInheritance;

/**
 * This class is a non-instantiate-utility for creating RoleTypeBindings.
 *
 * @author stephan
 */
public class RoleTypeCreator implements TagBits {

    // when wrapping types in a method signature, don't search the exact role
    // (signature weakening!)
    // TODO (Threads) move this field to Dependencies.Config!
    static boolean doingSignatures = false;

	/** Simulate map() HOF over all arguments of a type for recursive wrapping. */
	public interface TypeArgumentUpdater {
		/** the function argument to map(). */
		public abstract TypeBinding updateArg(ReferenceBinding arg);
	}

	/**
     * This method may wrap a type as an explicitly anchored role type if appropriate.
     *
     * If wrapping is not applicable, return the typeToWrap unmodified.
     *
     * @param scope (NON-NULL) defines the scope determining tthis if applicable.
     * @param anchorExpr (NON-NULL)
     * @param typeToWrap
     * @param typedNode position for error reporting (NON-NULL if problemReporter != null)
     * @return RoleTypeBinding or ArrayBinding or TypeBinding(original) or null
     */
    public static TypeBinding maybeWrapQualifiedRoleType(
    		final Scope      scope,
            final Expression anchorExpr,
            TypeBinding      typeToWrap,
            final ASTNode    typedNode)
    {
        /* invocations:
         * QualifiedAllocationExpression.resolveType()	rec.new R()		R
         * FieldReference.resolveType()					rec.r			type(r)
         * maybeWrapQualifiedRoleType(MessageSend,BlockScope)
         * 	  for MessageSend.resolveType()             rec.m() 		returnType(m)
         */

        if (typeToWrap == null) return null;

        if (TSuperHelper.isMarkerInterface(typeToWrap))
        	return typeToWrap;

        TypeBinding originalType = typeToWrap;

        // consider arrays:
        int dimensions = typeToWrap.dimensions();
		typeToWrap     = typeToWrap.leafComponentType();

		// easy problems first:
        if (!(typeToWrap instanceof ReferenceBinding) || typeToWrap.isEnum())
            return originalType;
        ReferenceBinding refBinding = (ReferenceBinding)typeToWrap;

        if (scope instanceof BlockScope && avoidWrapRoleType((BlockScope) scope, anchorExpr))
        	return originalType;

		if (refBinding instanceof CaptureBinding)
			return ((CaptureBinding) refBinding).maybeWrapQualifiedRoleType(scope, anchorExpr, typedNode, originalType);

    	TypeBinding wrappedRoleType = internalWrapQualifiedRoleType(scope, anchorExpr, originalType, typedNode, refBinding, dimensions);
		return wrappedRoleType.maybeWrapRoleType(typedNode, new TypeArgumentUpdater() {
						public TypeBinding updateArg(ReferenceBinding arg) {
			        		if (arg instanceof WildcardBinding)
					        	return ((WildcardBinding) arg).maybeWrapQualifiedRoleType(scope, anchorExpr, typedNode);
			        		return arg;
						}
					});
    }
    static TypeBinding internalWrapQualifiedRoleType (
    		final Scope      scope,
            final Expression anchorExpr,
            TypeBinding      originalType,
            final ASTNode    typedNode,
            ReferenceBinding refBinding,
            int				 dimensions) 
    {
    	ReferenceBinding site = scope.enclosingSourceType();
        assert( ! (site == null));

        boolean needAnchor = true;

        // already wrapped?
        ITeamAnchor existingAnchor = retrieveAnchor(refBinding);

        if (existingAnchor == null) {
        	if (!refBinding.isDirectRole())
        		return originalType.maybeWrapRoleType(typedNode, new TypeArgumentUpdater() {
					public TypeBinding updateArg(ReferenceBinding arg) {
						return maybeWrapQualifiedRoleType(scope, anchorExpr, arg, typedNode);
					}
        		});
        } else {
            if (  !(existingAnchor instanceof TThisBinding)) {
            	// possibly have two significant anchors..
                // if a relevant anchor exists, we could well be content with typeToWrap!
                needAnchor = false;
            } else {
            	// do handle tthis RoleTypeBindings, too, because we might need to
            	// set a new anchor
            	// (report errors only, if this type is not already acceptable).
            	needAnchor = (TeamModel.findEnclosingTeamContainingRole(site, refBinding) == null);
            }
        }

		ProblemReporter problemReporter = scope.problemReporter();
        ITeamAnchor variableBinding = getAnchorVariableBinding(
								                site, anchorExpr, refBinding,
								                needAnchor ? problemReporter : null, // no reporting if not needed
								                typedNode);
        // only report one error (referenceContext is reset during reporting)
        if (problemReporter.referenceContext == null) {
        	class NullReporter extends ProblemReporter {
        		NullReporter(ProblemReporter orig) {
        			super(orig.policy, orig.options, orig.problemFactory);
        		}
        		@Override
        		public void handle(int problemId, String[] problemArguments, int elaborationId, String[] messageArguments, int severity, int problemStartPosition, int problemEndPosition, ReferenceContext context, CompilationResult unitResult)
        		{ /* NO-OP! */ }
        	}
        	problemReporter = new NullReporter(problemReporter);
        }
        // report errors:
        boolean decapsulationAllowed = false;
        if (typedNode instanceof Expression)
        	decapsulationAllowed = ((Expression)typedNode).getBaseclassDecapsulation().isAllowed();
        	
        if (   (variableBinding instanceof VariableBinding)
			&& (((VariableBinding)variableBinding).otBits & IOTConstants.IsFreshTeamInstance) != 0) 
        {
        	if (!refBinding.isRoleType())
        		return variableBinding.getDependentTypeBinding(refBinding, -1, null, dimensions);
        	return 
        		originalType;
    	} else if (variableBinding == null)  {
            if (needAnchor)
                problemReporter.missingTypeAnchor(typedNode, refBinding);
        } else if (variableBinding == RoleTypeBinding.NoAnchor) {
        	if (existingAnchor != null) {
        		variableBinding = TeamAnchor.maybeImproveAnchor(site, existingAnchor, anchorExpr);
        		if (variableBinding != null && variableBinding != existingAnchor)
        			return variableBinding.getRoleTypeBinding(refBinding, dimensions);
        		return originalType;
        	}
        	if (needAnchor)
        		problemReporter.noTeamAnchorInScope(anchorExpr, refBinding);
        } else {
        	if (!variableBinding.isFinal()) {
        		// old version: directly report:
        		//problemReporter.anchorPathNotFinal(anchorExpr, variableBinding, refBinding.sourceName());
        		// new version: don't complain now but use a non-compatible anchor:
        		TypeBinding variableType = variableBinding.getResolvedType();
        		if (variableType.isRole())
        			variableType = ((ReferenceBinding)variableType).getRealClass(); // will be asked for members later
				variableBinding = new LocalVariableBinding(variableBinding.internalName(), variableType, ClassFileConstants.AccFinal, false) {
        			@Override public int problemId() { return IProblem.AnchorNotFinal; }
        		};
        	}
        	if (   !(variableBinding instanceof TThisBinding)
        		&& !isThisLike(anchorExpr)
        	    && !refBinding.isPublic()
        	    && !decapsulationAllowed)
	        {
	       		problemReporter.externalizingNonPublicRole(typedNode, refBinding);
	        } else {
		        if (   existingAnchor != null
		        	&& !(existingAnchor instanceof TThisBinding)
		        	&& !existingAnchor.hasSameBestNameAs(variableBinding))
		        {
		        	variableBinding = TeamAnchor.maybeImproveAnchor(site, existingAnchor, anchorExpr);
		        	if (variableBinding == null)
		        		return originalType; // cannot merge anchors -> original type cannot be improved.
		        }
		        // delegate to the principal function:
		        TypeBinding[] typeArguments = refBinding.isParameterizedType() ? ((ParameterizedTypeBinding)refBinding).arguments : null;
		        return getAnchoredType(scope, typedNode, variableBinding, refBinding, typeArguments, dimensions);
	        }
        }
        return originalType;
    }

    private static boolean isThisLike(Expression expression) {
    	if (expression instanceof SingleNameReference)
    		return ((SingleNameReference) expression).isThisLike;
    	return false;
    }

    static ITeamAnchor retrieveAnchor(ReferenceBinding refBinding) {
		if (refBinding instanceof DependentTypeBinding)
            return ((DependentTypeBinding)refBinding)._teamAnchor;
        return null;
    }
	/**
	 * Version to use by MessageSend.
	 * May specialize role type to revert signature weakening.
	 *
	 * @param send retrieve all relevant information from this message send.
	 * @param scope
	 * @return wrapped type or original or null
	 */
	public static TypeBinding maybeWrapQualifiedRoleType(MessageSend send, BlockScope scope)
	{
		Expression receiver = send.receiver;

		TypeBinding returnType = send.binding.returnType;

		if (   returnType == null
			|| !(returnType.leafComponentType() instanceof ReferenceBinding)
			|| returnType.leafComponentType().isEnum())
	    	return returnType;

		int dimensions = returnType.dimensions();
		ReferenceBinding refReturn = (ReferenceBinding)returnType.leafComponentType();

		// don't try externalized non-public role if compatibility can be established with plain types:
        if (send.expectedType != null && !(send.expectedType instanceof DependentTypeBinding))
        	if (   refReturn.isRole()
        		&& !refReturn.isPublic()
        		&& returnType.isCompatibleWith(send.expectedType))
        		return returnType;

        boolean isCalloutGet = CharOperation.prefixEquals(IOTConstants.OT_GETFIELD, send.selector);
//        // FIXME(SH): test with field of role type
        {
        	AbstractMethodDeclaration referenceMethod = scope.methodScope().referenceMethod();
        	if (referenceMethod != null)
        		isCalloutGet &= (referenceMethod.isMappingWrapper.callout());
        }
        if (!isCalloutGet) {
	        if (refReturn instanceof WeakenedTypeBinding) {
	        	WeakenedTypeBinding weakenedReturn = (WeakenedTypeBinding)refReturn;
	        	if (dimensions > 0)
	        		throw new InternalCompilerError("array not yet supported in this position"); //$NON-NLS-1$
				ReferenceBinding strongType = weakenedReturn.getStrongType();
				if (TypeBinding.notEquals(strongType.getRealType(), weakenedReturn.weakenedType)) {
					if (send.expectedType == null || !weakenedReturn.weakenedType.isCompatibleWith(send.expectedType))
						send.valueCast = strongType;
				}
	        	TypeBinding instantiated = maybeWrapQualifiedRoleType(
		    									scope,
		    									send.receiver,
		    									strongType,
		    									send);
	       		return instantiated;
	        }
		    if (refReturn instanceof RoleTypeBinding)
		    {
		    	RoleTypeBinding roleReturn = (RoleTypeBinding)refReturn;
		    	if (   roleReturn._argumentPosition > -1
		    		&& send.binding.original() == roleReturn._declaringMethod.getMethod())
		    	{
		    		ReferenceBinding roleEnclosing = roleReturn.enclosingType();

		    		// anchored to argument, instantiate directly:
		    		Expression anchorExpr = send.arguments[roleReturn._argumentPosition];
		    		TypeBinding anchorType = anchorExpr.resolvedType;
		    		
		    		ITeamAnchor anchor = null;
		    		if (anchorType.isRole() && ((ReferenceBinding)anchorType).isCompatibleViaLowering(roleEnclosing)) {
		    			// see 1.1.31-otjld-stacked-teams-1 f.
		    			if (anchorExpr.isThis())
		    				anchor = ((ReferenceBinding)anchorType).getField(IOTConstants._OT_BASE, true);
		    			else
		    				return returnType; // cannot improve, error will be reported upstream		    			
		    		} else {
		    			if (anchorType.isRole())
		    				anchorType = ((ReferenceBinding)anchorType).getRealClass(); // ignore role-ness of team anchor
		    			if (!anchorType.isCompatibleWith(roleEnclosing)) 
			    			return returnType; // cannot improve, anchor doesn't match expected team.
			    		anchor = (ITeamAnchor)((NameReference)anchorExpr).binding;
		    		}
		    		return anchor.getRoleTypeBinding(roleReturn, returnType.dimensions());
		    	}

		    	// retrieve existing information:
		        ITeamAnchor      anchor       = roleReturn._teamAnchor;
		        ReferenceBinding receiverType = (ReferenceBinding)send.actualReceiverType;
		        if (   anchor instanceof TThisBinding  // not yet instantiated
		            && (  receiverType.isTeam()
		                ||receiverType.isRole()))
		        {
		            roleReturn = (RoleTypeBinding)TeamModel.strengthenRoleType(receiverType, roleReturn);
		            if (dimensions > 0)
		            	returnType = roleReturn.getArrayType(dimensions);
		            else
		            	returnType = roleReturn;
		        }
		        if (avoidWrapRoleType(scope, receiver))
		        	// don't use synthetic _OT$role as additional anchor
		        	return returnType;
		    }
        } else {
        	if (send.arguments != null && send.arguments.length > 0)
        	{ // no arguments if accessed field is static
		    	// for wrapping types of a field access method, fake a _OT$base receiver
		    	// (although method is actually static)
		    	// see also comment in FieldAccessSpec.createMethod().
		    	receiver = new SingleNameReference(IOTConstants._OT_BASE,0);
		    	receiver.resolve(scope);
        	}
	    }
	    return maybeWrapQualifiedRoleType(
	    			scope,
					receiver,
					returnType,
					send);
	}

	static boolean avoidWrapRoleType(BlockScope scope, Expression receiver) {
        if (scope.compilerOptions().weavingScheme == WeavingScheme.OTRE) {
        	// don't use synthetic _OT$role as additional anchor
        	return CallinImplementor.avoidWrapRoleType(scope, receiver);
        } else {
        	// same here, just different way to detect:
        	return isThisLike(receiver);
        }
	}

	/**
	 * Given a receiver which determines a bestNamePath try to wrap the
	 * given type and instantiate its bestNamePath accordingly.
	 *
	 * @param site 			type where resolving takes place
	 * @param type          type to wrap
	 * @param firstVariable first element in a receiver path
	 * @param otherBindings subsequent elements in a receiver path
	 * @param mergePath     should paths from existing and new anchor be merged?
	 * @param problemReporter
	 * @param node          the node to use for error reporting
	 * @param positions
	 */
	public static ITeamAnchor getAnchorFromQualifiedReceiver(
			ReferenceBinding site,
			ReferenceBinding type,
			VariableBinding  firstVariable,
			FieldBinding[]   otherBindings,
			boolean          mergePath,
			ProblemReporter  problemReporter,
			Expression       node,
			long[]           positions)
	{
		assert firstVariable.type instanceof ReferenceBinding; // how else could we have otherBindings?

		// remember the last team in the path, which is a suitable anchor for type.
		ITeamAnchor candidateTeam = null;
		candidateTeam = firstVariable.asAnchorFor(type);
		if (candidateTeam != null && !candidateTeam.isFinal()) {
			assert problemReporter != null && positions != null;
			SingleNameReference anchorExpr = new SingleNameReference(firstVariable.name, positions[0]);
			problemReporter.anchorPathNotFinal(anchorExpr, candidateTeam, type.readableName());
			return null;
		}

		if (otherBindings != null) {
			ITeamAnchor currentVariable = firstVariable;
			for (int i = 0; i < otherBindings.length; i++) {
				// construct a variable with proper bestNamePath:
				currentVariable = otherBindings[i].setPathPrefix(currentVariable);
				if (currentVariable.hasValidReferenceType()) {
					// check for final:
					if (!currentVariable.isFinal()) {
						if (problemReporter != null) {
							assert positions != null;
							int start = (int)(positions[0] >>> 32);
							int end = (int)(positions[i+1] & 0xFFFFFFFF);
							// faked source: need only sourceStart, sourceEnd:
							QualifiedNameReference fakedAnchor = new QualifiedNameReference(new char[0][], new long[0], start, end);
							problemReporter.anchorPathNotFinal(fakedAnchor, currentVariable, type.readableName());
						}
						return null;
					}
					if (currentVariable.isTeamContainingRole(type))
						candidateTeam = currentVariable;
				}
			}
		}

		if (candidateTeam == null) {
			if (   type instanceof RoleTypeBinding
				&& ((RoleTypeBinding)type).hasExplicitAnchor())
			{
				if (mergePath) {
					ITeamAnchor anchor = TeamAnchor.maybeImproveAnchor(site, ((RoleTypeBinding)type)._teamAnchor, node);
					if (anchor != null)
						return anchor;
				} else {
					return null;
				}
			}
			if (problemReporter != null)
				problemReporter.noTeamAnchorInScope(node, type);
		} else {
			if (   type instanceof RoleTypeBinding
				&& ((RoleTypeBinding)type).hasExplicitAnchor())
			{
				if (mergePath) {
					candidateTeam = ((RoleTypeBinding)type)._teamAnchor.setPathPrefix(candidateTeam);
				}
			}
		}
		return candidateTeam;
	}

	/**
	     * This method assumes that the enclosing type of scope can be used for
	     * tthis anchors, i.e., types are used unqualified.
	     *
	     * @param scope determines tthis (NON-NULL).
	     * @param typeToWrap
	     * @param typedNode
	     * @return valid type, null (possibly after reporting error), or (unreported) problem
	     */
	    public static TypeBinding maybeWrapUnqualifiedRoleType (
	    		Scope            scope,
	            TypeBinding      typeToWrap,
	            ASTNode          typedNode)
	    {
			/* external invocations (via maybeWrapUnqualifiedRoleType(TypeBinding, Scope, AstNode)?):
	         *      AllocationExpression.resolveType()      new R       R
	         *      ArrayAllocationExpression.resolveType() new R[]     R
	         *      Assignment.resolveType()                lhs = r     type(r)
	         *      CastExpression.resolveType()            (R)expr     R
	         *      LocalDeclaration.resolve()              R l = r     R, type(r)
	         *      PotentialLiftExpression.resolveType()   liftToR(b)  R
	         *      RoleTypeReference.resolveType()			this.R      R
	         *      SingleNameReference.resolveType()       n           type(n)
	         *      ThisReference.resolveType()             this        type(this)
	         *      QualifiedThisReference.resolveType()    Mid.this    type(this)
	         * other invocations:
	         *      MethodVerifier.areTypesEqual()
	         * 	 	CopyInheritance.copyCastToMethods()
	         *      AbstractMethodMappingDeclaration.resolveMethodSpecs()
	         * other internal invocation:
	         *      wrapTypesInMethodBindingSignature()
	         *      wrapTypesInMethodDeclSignature()
	         * 			(via maybeWrapUnqualifiedRoleType(TypeBinding, Scope, AstNode))
	         *
	         */
	    	ReferenceBinding site = scope.enclosingSourceType();
	    	MethodScope methodScope = scope.methodScope();
	    	if (   methodScope != null
	    		&& methodScope.referenceMethod() != null
	    		&& methodScope.referenceMethod().isMappingWrapper._callin()
//{OTDyn
	    		&& scope.compilerOptions().weavingScheme == WeavingScheme.OTRE) // this heuristic doesn't work for dyn weaving, FIXME(SH): check if still needed!
// SH}
	    	{
	    		// in a callin wrapper, for visibility reasons, pretend we are in the
	    		// scope of the role (which is, where the declaration actually occurs):
	    		char[] selector = methodScope.referenceMethod().selector;
	    		int secondDollar = CharOperation.indexOf('$', selector, 4); // skip _OT$
	    		char[] roleName = CharOperation.subarray(selector, 4, secondDollar);
	    		site = site.getMemberType(roleName);
	    	}
	        return maybeWrapUnqualifiedRoleType(scope,
	        									site,
	        									typeToWrap,
	        									typedNode,
	        									scope.problemReporter());
	    }
	    // pure binding version:
	    public static TypeBinding maybeWrapUnqualifiedRoleType (TypeBinding typeToWrap,
	    														ReferenceBinding site)
	    {
	    	return maybeWrapUnqualifiedRoleType(null, site, typeToWrap, null, null);
	    }
	    // common implementation:
	    public static TypeBinding maybeWrapUnqualifiedRoleType (final Scope            scope,
	    												 		final ReferenceBinding site,
	    												              TypeBinding      typeToWrap,
	    												        final ASTNode          typedNode,
	    												              ProblemReporter  problemReporter)
	    {
	        assert( ! (site == null));
	        if (typeToWrap == null) return null;
	        if (!typeToWrap.isValidBinding())
	        	return typeToWrap; // don't tamper with already broken type

	        if (TSuperHelper.isMarkerInterface(typeToWrap))
	        	return typeToWrap;

	        final TypeBinding originalType = typeToWrap;

	        // consider arrays:
	        int dimensions = typeToWrap.dimensions();
			typeToWrap     = typeToWrap.leafComponentType();
			
			// consider parameterized:
			TypeBinding[] arguments = null;
			if (typeToWrap.isParameterizedType()) 
				arguments = ((ParameterizedTypeBinding)typeToWrap).arguments;

			// easy problems first:
	        if (!(typeToWrap instanceof ReferenceBinding) || typeToWrap.isEnum())
	            return originalType;
	        
	        if (typeToWrap instanceof UnresolvedReferenceBinding) {
	        	// defer wrapping until resolve():
	        	final UnresolvedReferenceBinding rawUnresolved = (UnresolvedReferenceBinding) typeToWrap;
	        	final ProblemReporter originalReporter = problemReporter; 
	        	return new UnresolvedReferenceBinding(rawUnresolved.compoundName, rawUnresolved.getPackage()) {
	        		@Override
	        		public ReferenceBinding resolve(LookupEnvironment environment, boolean convertGenericToRawType) {
	        			ReferenceBinding type = rawUnresolved.resolve(environment, convertGenericToRawType);
	        			return (ReferenceBinding) maybeWrapUnqualifiedRoleType(scope, site, type, typedNode, originalReporter);
	        		}
	        		@Override public boolean hasTypeAnnotations() 			{ return rawUnresolved.hasTypeAnnotations(); }
	        		@Override public boolean hasNullTypeAnnotations() 		{ return rawUnresolved.hasNullTypeAnnotations(); }
	        		@Override public AnnotationBinding[] getAnnotations() 	{ return rawUnresolved.getAnnotations(); }
	        		{ this.tagBits = rawUnresolved.tagBits; }
	        	};
	        }
	        if (typeToWrap instanceof IntersectionTypeBinding18) { // FIXME (recurse?)
	        	return originalType;
	        }
	        ReferenceBinding refBinding = (ReferenceBinding)typeToWrap;
	        if (  !refBinding.isDirectRole()) {
	        	final ProblemReporter reporter = problemReporter;
	            return originalType.maybeWrapRoleType(typedNode, new TypeArgumentUpdater() {
					public TypeBinding updateArg(ReferenceBinding arg) {
						return maybeWrapUnqualifiedRoleType(scope, site, arg, typedNode, reporter);
					}
	        	});
	        }

	        // already wrapped:
			if (typeToWrap instanceof RoleTypeBinding) {
	            RoleTypeBinding roleType = (RoleTypeBinding)typeToWrap;
	            if (  !(roleType._teamAnchor instanceof TThisBinding)) {
	                return originalType; // cannot improve
	            } else {
	            	// do handle tthis RoleTypeBindings, too, because we might need to
	            	// set a new anchor
	            	// (but don't report errors, if this type is already acceptable).
	            	if (TeamModel.findEnclosingTeamContainingRole(site, roleType) != null)
	            		problemReporter = null;
	            }
	        }

	        VariableBinding  variableBinding = null;
	        ReferenceBinding teamBinding = TeamModel.findEnclosingTeamContainingRole(site, refBinding);
	        if (teamBinding != null)
		        variableBinding = TThisBinding.getTThisForRole(refBinding, teamBinding);
		    if (variableBinding == null)
		        variableBinding = cannotWrapType(refBinding, problemReporter, typedNode);

			// handle problems (reported ones and yet unreported ones):
			assert (variableBinding != null);
			if (variableBinding == RoleTypeBinding.NoAnchor) {
	            return originalType;
	        } else if (!variableBinding.isFinal()) {
	            if (problemReporter != null)
	                problemReporter.anchorPathNotFinal(null, variableBinding, refBinding.sourceName()); // TODO
	            return null;
	        } else if (   !(variableBinding instanceof TThisBinding)
	        		   && !refBinding.isPublic())
	        {
	        	if (problemReporter != null)
	        		problemReporter.externalizingNonPublicRole(typedNode, refBinding);
	            return null;
	        }

	        // delegate to the principal function:
	        return getAnchoredType(scope, typedNode, variableBinding, refBinding, arguments, dimensions);
	    }

	/**
	 * @deprecated  legacy signature
	 */
	public static TypeBinding maybeWrapUnqualifiedRoleType(TypeBinding typeToWrap, Scope scope, ASTNode typedNode) {
		return maybeWrapUnqualifiedRoleType(scope, typeToWrap, typedNode);
	}

	/**
	 * Wrap all role types in a method signature. Only treat unqualified
	 * types here, explicit anchors are handled in QualifiedTypeReference.
	 *
	 * @param method binding to wrap
	 * @param decl AST
	 */
	public static void wrapTypesInMethodDeclSignature (
	        MethodBinding method, @NonNull AbstractMethodDeclaration decl)
	{
		if (   decl.ignoreFurtherInvestigation
			|| method == null
			|| method.isSynthetic()) // don't wrap role field accessors
			return;
		if ((method.otBits & IOTConstants.HasWrappedSignature) != 0) // no double wrapping
			return;
	    doingSignatures = true;
	    method.otBits |= IOTConstants.HasWrappedSignature;
	    ReferenceBinding site = method.declaringClass;
	    assert !(site instanceof BinaryTypeBinding);
	    TypeReference typedExpr = null;
	    if (decl instanceof MethodDeclaration)
	        typedExpr = ((MethodDeclaration)decl).returnType;
	    ITeamAnchor defaultAnchor = null; // for methods moved outside their role instance context
	    if (method.model != null && method.model._thisSubstitution != null) {
	    	ReferenceBinding sourceDeclaringType = (ReferenceBinding) method.model._thisSubstitution.binding.type;
	    	if (sourceDeclaringType.getRealClass().isTeam()) { // only for nested teams
	    		method.model._thisSubstitution.bind(decl.scope, null, true);
	    		defaultAnchor = method.model._thisSubstitution.binding; // the original 'this' passed as first argument
	    	}
	    }
		if (   CopyInheritance.isCreator(method)
			|| Lifting.isLiftToMethod(method))
		{
			int dimensions = method.returnType.dimensions();

			// get the most specific anchor:
			ReferenceBinding returnRef = (ReferenceBinding)method.returnType.leafComponentType();
			VariableBinding  anchor  = method.declaringClass.getTeamModel().getTThis();
			// get the wrapped role type:
			TypeBinding roleArrayType = anchor.getRoleTypeBinding(returnRef, dimensions);
			DependentTypeBinding roleType = (DependentTypeBinding)roleArrayType.leafComponentType();
			// find least specific super-team containing this role:
			ReferenceBinding lastType = roleType;
			char[] typeName = CharOperation.subarray(decl.selector,	IOTConstants.CREATOR_PREFIX_NAME.length, -1);
			while (site != null) {
				ReferenceBinding memberType = site.getMemberType(typeName);
				if (memberType == null)
					break;
				lastType = memberType;
				site = site.superclass();
			}
			// if a tsuper was actually found, use a weakened type binding:
			if (WeakenedTypeBinding.requireWeakening(roleType, lastType))
				method.returnType = WeakenedTypeBinding.makeWeakenedTypeBinding(roleType, lastType, dimensions);
			else
				method.returnType = roleArrayType;
		} else {
			method.returnType = maybeWrapSignatureType(method.returnType, decl.scope, typedExpr, defaultAnchor);
			checkArrayLoweringForReturn(method, decl.scope);
		}

	    TypeBinding[] parameters = method.parameters;
	    Argument[]    arguments  = decl.arguments;
	    for (int i=0; i<parameters.length; i++)
	    {
	        Argument argument = (arguments != null) ? arguments[i] : null;
			parameters[i] = maybeWrapSignatureType(parameters[i], decl.scope, argument, defaultAnchor);
	        // in case resolveTypesFor already created the binding,
	        // update its type:
	        if (argument != null && argument.binding != null)
	        	argument.binding.type = parameters[i];
	    }
	    doingSignatures = false;
	}
	private static void checkArrayLoweringForReturn(MethodBinding method, BlockScope scope) {
		int dimensions = method.returnType.dimensions();
		if (dimensions == 0)
			return; // not an array
		if ((method.declaringClass.isRole() && !method.isPublic()) || method.isPrivate())
			return; // not visible
		TypeBinding leafReturn = method.returnType.leafComponentType();
		if (!leafReturn.isRole())
			return; // not a role type
		TypeBinding baseType = ((ReferenceBinding) leafReturn).baseclass();
		if (baseType == null)
			return; // not bound
		baseType = scope.createArrayType(baseType, dimensions);
		new ArrayLowering(null).ensureTransformMethod(scope, null, method.returnType, baseType, false);
	}

	/* Wraps either relative to defaultAnchor or unqualified. */
	private static TypeBinding maybeWrapSignatureType(TypeBinding type, MethodScope scope, ASTNode typedNode, ITeamAnchor defaultAnchor) {
		if (   defaultAnchor != null 
			&& !type.leafComponentType().isBaseType()
			&& defaultAnchor.isTeamContainingRole((ReferenceBinding) type.leafComponentType()))
		{
			return defaultAnchor.getDependentTypeBinding(
						(ReferenceBinding) type.leafComponentType(), 
						0, // typeParamPosition 
						Binding.NO_PARAMETERS, 
						type.dimensions());
		} else {
			return maybeWrapUnqualifiedRoleType(type, scope, typedNode);
		}
	}

	/**
	 * Wrap all role types in a method signature. Only treat unqualified
	 * types here, explicit anchors are handled in QualifiedTypeReference.
	 *
	 * Mainly used for binary types, but also for faked base call surrogates.
	 *
	 * @param method binding to wrap
	 * @param environment used for type lookup
	 */
	public static void wrapTypesInMethodBindingSignature (
			MethodBinding method, LookupEnvironment environment)
	{
		if ((method.otBits & IOTConstants.HasWrappedSignature) != 0) // no double wrapping
			return;
	    doingSignatures = true;
	    try {
	    	method.otBits |= IOTConstants.HasWrappedSignature;
		    ReferenceBinding site = method.declaringClass;
		    if (method.anchorList != null)
		        method.anchorList.wrapTypes(environment);
			if (CopyInheritance.isCreator(method)) {
				// make sure the return type uses the most specific anchor
				// (this is important because signature weakening would otherwise
				// loose information essential for access control).
				ReferenceBinding returnRef = (ReferenceBinding)method.returnType;
				ReferenceBinding team0   = returnRef.enclosingType();
				if (team0 == null || team0.getTeamModel() == null)
					return; // happens when org.objectteams.Team is missing
				VariableBinding  anchor0 = team0.getTeamModel().getTThis();
				DependentTypeBinding  type1   = (DependentTypeBinding)anchor0.getRoleTypeBinding(returnRef, returnRef.dimensions());
				VariableBinding  anchor  = method.declaringClass.getTeamModel().getTThis();
				method.returnType = type1.maybeInstantiate(anchor, 0);
				// TODO (SH): check whether this can be generalized to apply for other methods, too.
				// Note: LiftTo-Methods currently cannot use this strategy: they need the original
				//       type for creating the
			} else {
				if (!CharOperation.prefixEquals(DeclaredLifting.OT_LIFT_DYNAMIC, method.selector)) // return of this method must not be wrapped
					method.returnType = maybeWrapUnqualifiedRoleType(method.returnType, site);
			}
		    TypeBinding[] parameters = method.parameters;
		    for (int i=0; i<parameters.length; i++)
		    {
		        parameters[i] = maybeWrapUnqualifiedRoleType(parameters[i], site);
		    }
	    } finally {
	    	doingSignatures = false;
	    }
	}

	/**
	 * Extract a variable binding from an expression, which is
	 * suitable as an anchor for a role type.
	 *
	 * Note, that checking for final is not done here!
	 *
	 * @param site       context for type name resolution
	 * @param anchorExpr should refer to a (final) variable holding a Team instance
	 * @param roleType   Use this if the anchor expression is 'this' to
	 *    retrieve the appropriate tthis binding.
	 * @param problemReporter for error reporting or null (no reporting)
	 * @param typedNode  expression whose type is currently being resolved (only for error positions)
	 * @return a valid anchor or NoAnchor(reported via cannotWrap) or null (also reported)
	 */
	public static ITeamAnchor getAnchorVariableBinding(
	        ReferenceBinding site,
/*nonnull*/ Expression       anchorExpr,
	        ReferenceBinding roleType,
			ProblemReporter  problemReporter,
	        ASTNode          typedNode)
	{
	    ITeamAnchor      anchorBinding   = null;

		// unwrap meaningless cast:
		if (anchorExpr instanceof CastExpression) {
			CastExpression cast = (CastExpression)anchorExpr;
			if (RoleTypeBinding.isRoleWithExplicitAnchor(cast.resolvedType))
				anchorBinding = ((RoleTypeBinding)cast.resolvedType)._teamAnchor;
			else
				anchorExpr = ((CastExpression)anchorExpr).expression;
		}

		if (anchorExpr instanceof PotentialLowerExpression)
		    anchorExpr = ((PotentialLowerExpression)anchorExpr).expression;
		if (anchorExpr instanceof ThisReference)
		{
			ReferenceBinding teamBinding = (ReferenceBinding)anchorExpr.resolvedType;
			ReferenceBinding enclosingTeam = TeamModel.findEnclosingTeamContainingRole(teamBinding, roleType);
			if (enclosingTeam == null) {
		        if ((problemReporter != null)) {
		        	ASTNode location = anchorExpr;
		        	if (location.sourceEnd == 0)
		        		location = typedNode;
		            problemReporter.typeAnchorNotEnclosingTeam(
		            					location, teamBinding, roleType);
		        }
		        return null;
		    }
		    anchorBinding = TThisBinding.getTThisForRole(roleType, enclosingTeam);
		    if (anchorBinding == null)
		    	return cannotWrapType(roleType, problemReporter, typedNode);
		} else {
		    // extract the name reference from a type anchor reference used as expression:
			if (   anchorExpr instanceof TypeAnchorReference
				&& ((TypeAnchorReference)anchorExpr).isExpression)
			{
				anchorExpr = ((TypeAnchorReference)anchorExpr).anchor;
			}

		    if (anchorExpr instanceof ArrayReference)
		    	anchorExpr = ((ArrayReference)anchorExpr).receiver;

		    if (anchorExpr instanceof FieldReference)
		    {
		        anchorBinding = ((Reference)anchorExpr).fieldBinding();
		    }
		    else if (anchorExpr.isTypeReference())
		    {
		        anchorBinding = null; // not an instance: not usable.
		        ReferenceBinding teamBinding = TeamModel.findEnclosingTeamContainingRole(site, roleType);
		        if (teamBinding == null) {
		            if ((problemReporter != null))
		                problemReporter.
		                        missingTypeAnchor(anchorExpr, roleType);
		            return null;
		        }
		        anchorBinding = TThisBinding.getTThisForRole(roleType, teamBinding);
		        if (anchorBinding == null) {
		            if ((problemReporter != null))
		                problemReporter.
		                        typeAnchorIsNotAVariable(anchorExpr, roleType.sourceName());
		            return null;
		        }
		    }
		    else if (anchorExpr instanceof NameReference)
		    {
		    	if (anchorExpr instanceof QualifiedNameReference) {
		    		QualifiedNameReference qRef = (QualifiedNameReference)anchorExpr;
		    		anchorBinding = getAnchorFromQualifiedReceiver(
		    							site,
		    							roleType,
										(VariableBinding)qRef.binding,
										qRef.otherBindings,
										/*mergePaths*/false,
										problemReporter,
										anchorExpr,
										qRef.sourcePositions);
		    		if (anchorBinding == null)
		    			return RoleTypeBinding.NoAnchor; // already reported
		    	} else {
		    		if (((NameReference)anchorExpr).binding instanceof VariableBinding) {
		    			anchorBinding = (ITeamAnchor)((NameReference)anchorExpr).binding;
		    			if (roleType.isTypeVariable()) {
		    				ITeamAnchor[] anchors = ((TypeVariableBinding)roleType).anchors;
		    				if (anchors != null)
		    					return anchorBinding; // avoid analysis which requires knowledge about the role type
		    			}

// FIXME(SH): manual resolving of base-anchor?
//	            			if (CharOperation.equals(((SingleNameReference)anchorExpr).token, IOTConstants._OT_BASE))
//	            			{
//	            				ReferenceBinding anchorSite = ((FieldBinding)anchorBinding).declaringClass;
//	            				if (    anchorSite != site &&
//	            					site.isCompatibleWith(anchorSite)) {
//	            					anchorBinding = new FieldBinding((FieldBinding)anchorBinding, site);
//	            					((FieldBinding)anchorBinding).type = site.baseclass();
//	            				}
//	            			}
		    		} else {
		                if ((problemReporter != null))
		                    problemReporter.typeAnchorIsNotAVariable(anchorExpr, roleType.sourceName());
		                return null;
		    		}
		    	}
		    }
		    else if (anchorExpr instanceof QualifiedAllocationExpression)
		    {
		    	// propagate anchor from resolved type:
		    	QualifiedAllocationExpression allocation = (QualifiedAllocationExpression)anchorExpr;
		    	return ((RoleTypeBinding)allocation.resolvedType)._teamAnchor;
		    }
		    else if (anchorExpr instanceof MessageSend)
		    {
		    	TypeBinding receiverLeaf = ((MessageSend)anchorExpr).actualReceiverType.leafComponentType();
		    	if (RoleTypeBinding.isRoleWithExplicitAnchor(receiverLeaf)) {
		    		anchorBinding = ((RoleTypeBinding)receiverLeaf)._teamAnchor;
		    	} else {
		    		// regression fix during work on https://bugs.eclipse.org/331877
		    		if (anchorExpr.resolvedType != null && anchorExpr.resolvedType.isRoleType())
		    			return ((DependentTypeBinding)anchorExpr.resolvedType)._teamAnchor;
		    		return cannotWrapType(roleType, problemReporter, typedNode);
		    	}
		    }
		    else if (anchorExpr instanceof AllocationExpression) 
		    {
		    	// this anchor matches nothing
		    	String displayName = "fresh-instance-of-"+((AllocationExpression)anchorExpr).type.toString(); //$NON-NLS-1$
				LocalVariableBinding fakeVariable = new LocalVariableBinding(displayName.toCharArray(), roleType.enclosingType(), ClassFileConstants.AccFinal, false);
				fakeVariable.otBits |= IOTConstants.IsFreshTeamInstance;
				return fakeVariable;
		    }
		    else if (anchorBinding == null)
		    {
		        return cannotWrapType(roleType, problemReporter, typedNode);
		    }
		    /*
		     * anchorBinding = non-null
		     *      FieldReference
		     *      NameReference
		     *      isTypeReference() -> TThisBinding
		     */
		    assert (anchorBinding != null);
		}
		/*
		 * variableBinding = non-null, anchorType = non-null:
		 *      ThisReference
		 *      + all others that did not already quit with an error.
		 */
		 // if ((problemReporter != null))
		 //   assert anchorBinding != null; // redundant
	    if (!anchorBinding.isTeamContainingRole(roleType))
	    {
	        anchorBinding = anchorBinding.retrieveAnchorFromAnchorRoleTypeFor(roleType);
	        if (anchorBinding == null)
	        {
	        	if (   roleType instanceof DependentTypeBinding
	        		&& ((DependentTypeBinding)roleType).hasExplicitAnchor())
	        		return cannotWrapType(roleType, problemReporter, typedNode); // not improved
	            ReferenceBinding teamBinding = TeamModel.findEnclosingTeamContainingRole(site, roleType);
	            if (teamBinding == null)
	                return cannotWrapType(roleType, problemReporter, typedNode);
	            anchorBinding = TThisBinding.getTThisForRole(roleType, teamBinding);
	        }
	    }
	    assert (anchorBinding != null);
	    return anchorBinding.asAnchorFor(roleType);
	}

	/**
	 * Report that a role type has no suitable type anchor.
	 * Return NoAnchor to stop further resolving.
	 *
	 * @param roleType
	 * @param problemReporter if non-null use this for reporting else don't report
	 * @param typedNode
	 * @return RoleTypeBinding.NoAnchor
	 */
	private static VariableBinding cannotWrapType(
	    ReferenceBinding roleType,
	    ProblemReporter  problemReporter,
	    ASTNode          typedNode)
	{
	    if (   !roleType.isRoleType()
	        && problemReporter != null)
	    {
	    	if ((typedNode.bits & ASTNode.IsGeneratedWithProblem) == 0) {
		    	ReferenceContext ctx = problemReporter.referenceContext;
		        problemReporter.missingTypeAnchor(typedNode,roleType);
		        problemReporter.referenceContext = ctx; // nulled out during reporting.
	    	}
	    }
	    return RoleTypeBinding.NoAnchor;
	}

	/**
	 * Resolve an anchored type given the tokens of its path including the type.
	 *
	 * TODO(SH): validate: this hands out the ifc-part of an anchor's type!
	 *
	 * @param scope
	 * @param typeExpression used for baseclass decapsulation
	 * @param tokens
	 * @param dimensions >0 signals an array type
	 * @return a RoleTypeBinding or null (path does not denote an anchored type) or
	 *         a ProblemReferenceBinding with reason AnchorNotFinal or AnchorNotATeam.
	 *         For ProblemReferenceBindings the problem should not be reported yet.
	 */
	public static TypeBinding resolveAnchoredType(
			Scope scope, Expression typeExpression, char[][] tokens, int dimensions)
	{
		// first try a static prefix "some.package.Some.Type":
		int variableStart = 0;
		ReferenceBinding staticType = null;
		boolean havePackagePrefix = false;
		// check at most length-2 elements, leaving space for anchor.Type:
		for (int i = 0; i < tokens.length-2; i++) {
			Binding staticPart = scope.getTypeOrPackage(CharOperation.subarray(tokens, 0, i+1));
			if (staticPart == null || !staticPart.isValidBinding()) {
				break;
			}
			if (staticPart instanceof ReferenceBinding) {
				staticType = (ReferenceBinding)staticPart;
				variableStart = i+1;
			} else {
				havePackagePrefix = true;
			}
		}

		// get the first variable:
		ITeamAnchor anchor;
		if (staticType != null) {
			boolean isStatic = true;
			// Skip "this" in "Outer.this", because it is not the start of a variable part:
			if (CharOperation.equals(tokens[variableStart], "this".toCharArray())) { //$NON-NLS-1$
				// static type remains as it is
				variableStart++;
				if (variableStart >= tokens.length)
					return null;
				isStatic = false;
			}
			anchor = TypeAnalyzer.findField(staticType, tokens[variableStart], isStatic, /*outer*/ true);
		} else {
			try {
				anchor = findResolvedVariable(scope, tokens[0]);
			} catch (InternalCompilerError ice) {
				// workaround to avoid "Prematurely searching field ..." regarding FQ class names
				if (havePackagePrefix)
					return null;
				else
					throw ice;
			}
		}

	    if (anchor == null)
	    	return null;

	    // from this point, as we found token[0] is a valid variable, don't return
	    // null any more but only problems.

	    ProblemReferenceBinding foundProblem= null;
	    if (anchor.hasValidReferenceType() && !anchor.isFinal()) {
	    	// t.T with t not a Team => plain Java error
	    	if (!anchor.isTeam() && variableStart == tokens.length-2)
	    		return new ProblemReferenceBinding(tokens, null, ProblemReasons.NotFound);

	    	// OT-error on first segment might be irrelevant if the rest doesn't
	    	// resolve to type any way:
	    	foundProblem= new ProblemReferenceBinding(anchor, tokens[tokens.length-1], null, ProblemReasons.AnchorNotFinal);
	    }

	    // build the anchor with appropriate bestNamePath from subsequent fields:
	    TypeBinding resolved= resolveOtherPathElements(scope, typeExpression, anchor, tokens, variableStart+1, dimensions);
	    if (resolved != null && resolved.isValidBinding() && foundProblem != null)
	    	return foundProblem; // other elements are OK => report the first problem.

	    return resolved;
	}

	/**
	 * Construct a path from an initial anchor and subsequent names.
	 *
	 * @param scope
	 * @param typeExpression used for baseclass decapsulation
	 * @param anchor    initial anchor
	 * @param tokens    names of subsequent fields
	 * @param startIdx  index into tokens where to start
	 * @param dimensions dimensions for the RoleTypeBinding to construct/retrieve
	 * @return a RoleTypeBinding or null (path does not denote an anchored type) or
	 *         a ProblemReferenceBinding with reason AnchorNotFinal or AnchorNotATeam.
	 *         For ProblemReferenceBindings the problem should not be reported yet.
	 */
	private static TypeBinding resolveOtherPathElements(
			Scope scope, Expression typeExpression, ITeamAnchor anchor, char[][] tokens, int startIdx, int dimensions)
	{
		ITeamAnchor current = anchor;
	    for (int i = startIdx; i < tokens.length-1; i++) {
	       	if (!anchor.hasValidReferenceType())
	       		return null;
			current = current.getFieldOfType(tokens[i], /*static*/false, /*outer*/ false); // TODO(SH): check outer navigation in paths.
	        if (current == null)
	            return new ProblemReferenceBinding(
	            		CharOperation.subarray(tokens, 0, i+1),
	            		null,
						ProblemReasons.AnchorNotFound);
		    if (!current.isFinal())
		    	return new ProblemReferenceBinding(
		    			current,
		    			tokens[tokens.length-1],
		    			null,
						ProblemReasons.AnchorNotFinal);
	       	anchor = current.setPathPrefix(anchor);
		}
	    if (!anchor.hasValidReferenceType())
		    return null;

	    // final component before the type must be a team:
		if (!anchor.isTeam()) {
			// last component must be a member type:
			ReferenceBinding anchorType= (ReferenceBinding)anchor.getResolvedType();
			ReferenceBinding last= anchorType.getMemberType(tokens[tokens.length-1]);

			// plain Java error?
			if (last==null || !last.isValidBinding())
				return new ProblemReferenceBinding(tokens, null, ProblemReasons.NotFound);

			// OT error!
			return new ProblemReferenceBinding(anchorType.compoundName, anchorType, ProblemReasons.AnchorNotATeam);
		}

		// delegate for role type lookup:
		return resolveRoleTypeFromAnchor(scope, typeExpression, anchor, tokens[tokens.length-1], dimensions, scope.problemReporter());
	}

	/**
	 * Find a variable by name in scope, possibly resolving a field for this purpose.
	 * @param scope
	 * @param name
	 * @return valid or null binding.
	 */
	public static VariableBinding findResolvedVariable(Scope scope, char[] name) {
		if (scope.kind == Scope.COMPILATION_UNIT_SCOPE)
			return null; // no single name variables in compilation unit scopes

		// first try immediate scope:
		VariableBinding anchor = scope.findVariable(name);
		if (anchor != null)
			return anchor;

		// get class-part scope:
		Scope classPartScope = scope;
		TypeDeclaration type = scope.referenceType();
		if (type != null && type.isRole() && type.isInterface()) { // type is null inside ModuleDeclaration
			TypeDeclaration classPartAst = type.getRoleModel().getClassPartAst();
			if (classPartAst != null)
				classPartScope = classPartAst.scope;
		}

		// find anchor-field in the class-part, if different from direct scope:
		if (classPartScope != scope)
			anchor = classPartScope.findVariable(name);
		if (anchor != null)
			return anchor;

		// travel out through enclosing scopes:
		anchor = findResolvedVariable(scope.parent, name);
		if (anchor != null)
			return anchor;

		// other fields of the enclosing type may not yet be resolved:
		return resolveField(classPartScope, name);
	}

	private static FieldBinding resolveField(Scope scope, char[] name) {
		if (scope == null) return null;
		if (scope instanceof CompilationUnitScope) return null;
		ReferenceBinding type = scope.enclosingSourceType();
		return TypeAnalyzer.findField(type, name, scope.isStatic(), /*outer*/true);
	}

	/**
	 * Given a variable of a team type, lookup the role type and create a RoleTypeBinding.
	 *
	 * @param scope          for error reporting
	 * @param typeExpression used for baseclass decapsulation
	 * @param anchor
	 * @param roleName
	 * @param dimensions
	 * @param problemReporter
	 * @return valid type or (unreported) problem.
	 */
	public static TypeBinding resolveRoleTypeFromAnchor(
			Scope           scope,
			Expression      typeExpression,
			ITeamAnchor     anchor,
			char[]          roleName,
			int             dimensions,
			ProblemReporter problemReporter)
	{

	    // retrieve role from its team:
	    ReferenceBinding roleType = anchor.getMemberTypeOfType(roleName);
	    if (roleType == null) {
	    	// prepare for getMemberType:
	    	ReferenceContext contextSave = problemReporter.referenceContext;
	    	Dependencies.ensureTeamState(anchor.getTeamModelOfType(), ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);
		    roleType = anchor.getMemberTypeOfType(roleName);
	    	problemReporter.referenceContext = contextSave;
	    	if (roleType == null)
	    		return new ProblemReferenceBinding(roleName, null, ProblemReasons.NotFound, true);
	    }

	    // call the principal method:
	    return getAnchoredType(scope, typeExpression, anchor, roleType, null/*typeArguments*/, dimensions); // FIXME(SH): type arguments?
	}

	/**
	 * This function does the actual wrapper creation after the anchor and the role type have
	 * been prepared.
	 *
	 * PRE: anchor actually refers to a team.
	 *
	 * @param scope           for error reporting
	 * @param typedNode		  used for baseclass decapsulation
	 * @param variableBinding anchor the type to this variable (full path),
	 * 						  this variableBinding is never merged with an anchor of roleBinding.
	 * @param roleBinding
	 * @param arguments
	 * @param dimensions if > 0 create an array of role type with these dimensions
	 *
	 * @return valid type or a ProblemReferenceBinding with one of the following reasons:
	 * 	  NotVisible, AnchorNotFinal
	 */
	public static TypeBinding getAnchoredType(
			Scope			 	scope,
			ASTNode          	typedNode,
	        ITeamAnchor      	variableBinding,
	        ReferenceBinding 	roleBinding,
	        TypeBinding[] 		arguments,
			int			     	dimensions)
	{
		if (!(variableBinding instanceof TThisBinding)) {
			DecapsulationState decapsulation = DecapsulationState.NONE;
			if (typedNode instanceof Expression) {
				Expression expr = ((Expression)typedNode);
				decapsulation = expr.getBaseclassDecapsulation(roleBinding);
			}
			int problemReason = 0;
			if (!roleBinding.isPublic()) {
				if (decapsulation.isAllowed()) {
					if (scope != null)
						scope.problemReporter().decapsulation((Expression)typedNode, roleBinding);
				} else {
					problemReason = ProblemReasons.NotVisible;
				}
			} else if (!variableBinding.isFinal()) {
				problemReason = ProblemReasons.AnchorNotFinal;
			}
			if (problemReason != 0)
				return new ProblemReferenceBinding(
						variableBinding,
						roleBinding.sourceName(),
						roleBinding,
						problemReason);
	   	}

	    if (roleBinding instanceof DependentTypeBinding)
	    {
	        DependentTypeBinding wrappedRole = (DependentTypeBinding)roleBinding;
	        if (wrappedRole._teamAnchor != variableBinding)
	        	return wrappedRole.maybeInstantiate(variableBinding, dimensions);
	        if (dimensions > 0)
	        	return wrappedRole.getArrayType(dimensions);
	        return wrappedRole;
	        // TODO (SH): check compatibility of anchors!
	    }
	    if (!variableBinding.isTeam())
		{
	    	throw new InternalCompilerError("ANCHOR IS NOT A TEAM"); //$NON-NLS-1$
	    }
	    if (!roleBinding.isInterface())
	    {
	    	roleBinding = roleBinding.roleModel.getInterfacePartBinding();
	        if (roleBinding == null)
	            throw new InternalCompilerError("Role class has no interface"); //$NON-NLS-1$
	    }
	    TypeBinding typeBinding = variableBinding.getRoleTypeBinding(roleBinding, arguments, dimensions);
	    return typeBinding;
	}

	/**
	 * Try to resolve an argument or return type as an anchored type.
	 *
	 * PRE: Regular type resolution has already failed.
	 *
	 * Currently only handles references of two components: anchor and Type.
	 * (for parameters this is probably OK?)
	 * NO: TODO(SH): arguments could also be anchored to arbitrary expressions/paths!
	 *
     * @param type type reference to be analyzed (resolvedType is null or invalid)
	 * @param arguments the arguments of the current method
	 * @param index position of the argument to be analyzed
     *        == arguments.length means: analyzing return type.
	 * @param scope scope of the current method.
	 * @return a RoleTypeBinding or null (after reporting error)
	 */
	public static TypeBinding getTypeAnchoredToParameter(
			TypeReference type,
	        Argument[]    arguments,
	        int           index,
	        MethodScope   scope,
	        CheckPoint    cp)
	{
	    // we only handle QualifiedTypeReferences of length 2, or QualifiedArrayTypeReferences.
	    if (! (type instanceof QualifiedTypeReference))
	    {
	    	return null; // not better than before
	    }
	    QualifiedTypeReference argType  = (QualifiedTypeReference)type;

	    // look for anchor in argument list:
	    VariableBinding anchor = null;
	    char[]          anchorName = argType.tokens[0];
	    int argPos;
	    for (argPos=0; argPos<index; argPos++)
	    {
	        Argument argument = arguments[argPos];
	        // ensure arguments are bound, which must happen in correct order.
            argument.bind(scope, argument.type.resolvedType, /*used*/false);
	        if (CharOperation.equals(argument.name, anchorName)) // compare possible anchor
	        {
	        	argument.binding.useFlag = LocalVariableBinding.USED; // used as anchor
	            anchor = argument.binding;
	            if (scope.classScope().referenceContext.isConverted)
	            	anchor.modifiers |= ClassFileConstants.AccFinal; // lost during conversion.
	            break;
	        }
	    }
	    if (anchor == null)
	    {
	    	argPos = -1; // mark as not found in argument list
	    	anchor = findAnchorInScope(scope, anchorName);
	    }
	    if (anchor == null)
	        return null; // not better than before.

	    if (!anchor.isFinal())
	    {
	    	char[][] typeName = type.getTypeName();
	        scope.problemReporter().anchorPathNotFinal(argType, anchor, typeName[typeName.length-1]);
	        return null;
	    }

	    // defensive programming:
	    if (anchor.type == null) return null;
	    // anchor must be a team:
	    if (!anchor.type.isTeam()) {
	    	if (!anchor.type.isValidBinding())
	    		return null; //can't decide whether this is a valid team or not.
			reportAnchorIsNotATeam(scope, argType);
			return null;
	    }

	    // delegate for role type lookup:
	    TypeBinding anchoredType = resolveOtherPathElements(
	    								scope, type, anchor, argType.tokens, 1, argType.dimensions());

	    if (anchoredType != null) {
	    	if (!anchoredType.isValidBinding()) {
	    		scope.problemReporter().invalidType(type, anchoredType);
	    		return null;
	    	}
	    	if (anchoredType.leafComponentType().isRoleType()) {
		    	// prepare for creating an AnchorListAttribute
		        RoleTypeBinding leafRoleType = (RoleTypeBinding)anchoredType.leafComponentType();
				leafRoleType._argumentPosition = argPos;
				final AbstractMethodDeclaration methodDecl = scope.referenceMethod();
		        leafRoleType._declaringMethod = new DependentTypeBinding.IMethodProvider() {
		        	public MethodBinding getMethod() {
		        		return methodDecl.binding;
		        	}
		        };
	    	}
	    	scope.referenceContext.compilationResult().rollBack(cp);
	    	scope.problemReporter().deprecatedPathSyntax(type);
	    }
	    return anchoredType;
	}

	private static boolean isConvertedArgument(ITeamAnchor anchor, Scope scope) {
		if (!(anchor instanceof VariableBinding)) return false; // impossible/defensive
		if ((((VariableBinding)anchor).tagBits & TagBits.IsArgument) == 0)
			return false;
		return scope.classScope().referenceContext.isConverted;
	}

	/**
	 * Retrieve the team anchor representing a specific method argument
	 * @param method       use this method's arguments
	 * @param anchorArgPos position within the method's argument list
	 * @return a valid team anchor or a ProblemAnchorBinding or null;
	 */
	public static ITeamAnchor resolveTypeAnchoredToArgument(AbstractMethodDeclaration method, int anchorArgPos)
	{
		MethodScope scope = method.scope;
		Argument[] arguments = method.arguments;
		// ensure arguments upto the anchor are bound, which must happen in correct order:
		for (int i = 0; i <= anchorArgPos; i++)
			arguments[i].bind(scope, arguments[i].type.resolvedType, /*used*/i==anchorArgPos);
		TeamAnchor anchor = arguments[anchorArgPos].binding; // SH: bounds check?
		((LocalVariableBinding)anchor).resolvedPosition = anchorArgPos;

		// check anchor for error
		if (anchor == null)
			return null;
	    if (   !anchor.isFinal()
	    	&& !isConvertedArgument(anchor, scope))
	    {
	    	return new ProblemAnchorBinding(anchor, ProblemReasons.AnchorNotFinal);
	    }

	    // check anchor.type:
	    if (anchor.type == null)
	    	return null;
	    // anchor must be a team:
	    if (!anchor.type.isTeam()) {
	    	if (anchor.type.isValidBinding()) //otherwise we can't decide whether this is a valid team or not.
	    		scope.problemReporter().illegalTypeAnchorNotATeam(arguments[anchorArgPos]);
			return null;
	    }
		return anchor;
	}

	private static VariableBinding findAnchorInScope(MethodScope scope, char[] anchorName) {
		// search for variable in class scope (could be obsolete, check this)
		SingleNameReference invocationSite = new SingleNameReference("this".toCharArray(), 0); //$NON-NLS-1$
		invocationSite.binding = scope.enclosingSourceType();
		Binding binding = scope.getBinding(anchorName, Binding.VARIABLE, invocationSite, true);
		if (binding instanceof VariableBinding)
			return (VariableBinding)binding;
		return null;
	}

	private static void reportAnchorIsNotATeam(MethodScope scope, QualifiedTypeReference argType) {
		// extract all but the last element from typeReference (which yields the anchor):
		char[][] tokens = CharOperation.subarray(argType.tokens, 0, argType.tokens.length-1);
		long[] sourcePositions = new long[tokens.length];
		System.arraycopy(argType.sourcePositions, 0, sourcePositions, 0, tokens.length);
		int sourceEnd = (int)(sourcePositions[tokens.length-1] & 0xFFFFFFFF);
		QualifiedNameReference prefix = new QualifiedNameReference(tokens, sourcePositions, argType.sourceStart, sourceEnd);
		scope.problemReporter().illegalTypeAnchorNotATeam(prefix);
	}

	/**
	 * Note: the reason for having this method in this class is to let it respect doingSignatures.
	 *
	 * Starting at (and including) 'site' look for a role that is identical or
	 * a tsub-role of 'role'
	 * @param role
	 * @param site
	 * @return non-null, but possibly problem binding.
	 */
	public static ReferenceBinding findExactRole(
	    ReferenceBinding role,
	    ReferenceBinding site)
	{
	    if ((site != null) &&
	        !doingSignatures)
	    {
	    	ReferenceBinding teamBinding = TeamModel.findEnclosingTeamContainingRole(site, role);
	    	if (teamBinding == null){
	    		return new ProblemReferenceBinding(
	                    site.sourceName(),
	                    role,
	                    ProblemReasons.NoTeamContext, true);
	    	}
	    	return teamBinding.getMemberType(role.internalName());
	    }
	    return role;
	}

	/**
     * Given a raw role type and an anchor path create the corresponding RoleTypeBinding.
     * PRE: the type is not anchored to a parameter.
     *
	 * @param typeToWrap  wrap this role type
	 * @param anchorName  a char[] encoded anchor path ('.' separated, inner classes '$' separated).
	 * @param site        look here for first field as anchor
	 * @param environment used for lookup of a static prefix (types), if given.
	 */
	public static TypeBinding wrapTypeWithAnchorFromName(
			TypeBinding typeToWrap, char[] anchorName, ReferenceBinding site, LookupEnvironment environment)
	{
		ReferenceBinding leafType = (ReferenceBinding)typeToWrap.leafComponentType();
		if (leafType.isRawType())
			leafType = ((ParameterizedTypeBinding)leafType).genericType();
		if (!DependentTypeBinding.mayTakeValueParam(leafType)) {
			assert false : "this method should only be called for types requiring an anchor"; //$NON-NLS-1$
			return typeToWrap; // TODO (SH): check why we came here in the first place!!
		}
		// FIXME (SH): also check roles from an unrelated team
		//             (= is the role compatible to the anchor?)

		char[][] tokens = CharOperation.splitOn('.', anchorName);
		char[][] prefixTokens = tokens;
		TypeBinding typePrefix = null;
		while (prefixTokens.length > 0) {
			typePrefix = environment.askForType(prefixTokens);
			if (typePrefix != null)
				break;
			prefixTokens = CharOperation.subarray(prefixTokens, 0, prefixTokens.length-1);
		}
		ReferenceBinding currentType = site;
		int idx = 0;
		if (typePrefix != null) {
			idx = prefixTokens.length;
			currentType = (ReferenceBinding)typePrefix;
		}
		ITeamAnchor currentVar = TypeAnalyzer.findField(currentType, tokens[idx++], /*don't check static*/false, /*outer*/true);
		for (int i = idx; i < tokens.length; i++) {
			if (currentVar == null) {
				environment.problemReporter.abortDueToInternalError("Type anchor in class file "+new String(site.readableName())+" unresolvable path component: "+new String(tokens[i-1]));  //$NON-NLS-1$//$NON-NLS-2$
				return null;
			}
			ITeamAnchor next = currentVar.getFieldOfType(tokens[i], /*don't check static*/false, /*outer*/true);
			currentVar = next.setPathPrefix(currentVar);
		}
		if (!currentVar.isTeam()) {
			environment.problemReporter.abortDueToInternalError("Type anchor in class file "+new String(site.readableName())+" does not resolve to a team: "+new String(anchorName)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
        TypeBinding[] typeArguments = typeToWrap.isParameterizedType() ? ((ParameterizedTypeBinding)typeToWrap).arguments : null;
		return currentVar.getDependentTypeBinding(leafType, -1/*no argpos*/, typeArguments, typeToWrap.dimensions());
	}

	/**
	 * Given a type from the base side of a method mapping, try whether the type
	 * shoud be instantiated with an anchor given in a playedBy declaration.
	 *
	 * @param scope        context from which to search the playedBy declaration
	 * @param baseSideType type to improve.
	 * @return a valid binding
	 */
	public static ReferenceBinding maybeInstantiateFromPlayedBy(
											final Scope scope,
											ReferenceBinding baseSideType)
	{
		TypeArgumentUpdater typeArgumentUpdater = new TypeArgumentUpdater () {
			public TypeBinding updateArg(ReferenceBinding arg) {
				return maybeInstantiateFromPlayedBy(scope, arg);
			}
		};
		if (!baseSideType.isDirectRole())
			return (ReferenceBinding)baseSideType.maybeWrapRoleType(null, typeArgumentUpdater);

		ITeamAnchor baseSideAnchor = getPlayedByAnchor(scope);
// FIXME(SH): strengthen base type?
//		if (baseSideType.isRole()) {
//			ReferenceBinding baseSite = scope.enclosingSourceType().baseclass();
//			if (baseSite != null)
//				baseSideType = (ReferenceBinding)TeamModel.strengthenRoleType(baseSite, baseSideType);
//		}
		if (baseSideAnchor != null) {
			if (baseSideType instanceof DependentTypeBinding) {
				RoleTypeBinding baseSideRole = RoleTypeBinding.getRoleTypeBinding(baseSideType);
				return (ReferenceBinding)baseSideRole.maybeInstantiate(baseSideAnchor, 0);
			} else {
				return (ReferenceBinding)baseSideAnchor.getRoleTypeBinding(baseSideType, 0);
			}
		}

		return baseSideType;
	}

	/**
	 * Check whether we are in a scope for which a playedBy binding exists,
	 * that binds to a role with an explicit anchor.
	 */
	public static ITeamAnchor getPlayedByAnchor(Scope scope) {
		ReferenceBinding currentType = scope.enclosingSourceType();
		ReferenceBinding baseclass = currentType.baseclass();
		if (   currentType.isDirectRole()
			&& baseclass != null)
		{
			if (baseclass.isDirectRole())
			{
				if (baseclass instanceof WeakenedTypeBinding)
					baseclass = ((WeakenedTypeBinding)baseclass).getStrongType();
				if (RoleTypeBinding.isRoleWithExplicitAnchor(baseclass))
					return ((RoleTypeBinding)baseclass)._teamAnchor;
			}
			if (baseclass.isTeam()) {
				return currentType.getField(IOTConstants._OT_BASE, true);
			}
		}
		return null;
	}

	/** Compatibility of the kind:
	 * <pre>
	 *     team class BT {
	 *     	   class BR {}
	 *     	   void setBR(BR br); // br: tthis[BT].BR
	 *     }
	 *     class R playedBy BT {
	 *     	   void setBR(base.BR br1) <- after void setBR(BR br2); // br2 compatible to br1 ?
	 *     }
	 * </pre>
	 * @param scope retrieve playedBy from this scope.
     * @param baseType
	 * @param roleType
	 * @param bindDir asking for compatibility role->base or base->role?
	 */
	public static boolean isCompatibleViaBaseAnchor(Scope scope, TypeBinding baseType, TypeBinding roleType, int bindDir)
	{
		// TODO(SH): optimize: first check whether requiredType is base-anchored!
		if (baseType instanceof ReferenceBinding) {
			TypeBinding baseAnchoredBase = maybeInstantiateFromPlayedBy(
					scope, (ReferenceBinding)baseType);
			if (bindDir == TerminalTokens.TokenNameBINDIN) {
				if (baseAnchoredBase.isCompatibleWith(roleType))
					return true;
			} else {
				if (roleType.isCompatibleWith(baseAnchoredBase))
					return true;
			}
		}
		return false;
	}

	/** 
	 * Decompose a given type into atomic types (taking into account arrays and type arguments)
	 * perform a given type substitution for each atomic type and 
	 * re-assemble to a type of the same shape as the given type. 
	 * @param original      the given type to be substituted
	 * @param environment   for creation of parameterized types and array types
	 * @param substitution  what to perform on each atomic dependent type
	 * @return either null (after an error should have been reported) or the original type or a legal substitution
	 */
	public static TypeBinding deepSubstitute(TypeBinding original, LookupEnvironment environment, IDependentTypeSubstitution substitution) {
	   TypeBinding type = original;
	   int dimensions = type.dimensions();
	   type = type.leafComponentType();
	   
	   TypeBinding[] typeArguments = null;
	   boolean hasInstantiated = false;
	   if (type.isParameterizedType()) {
		   ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) type;
		   if (ptb.arguments != null) {
			   typeArguments = new TypeBinding[ptb.arguments.length];
			   for (int j = 0; j < ptb.arguments.length; j++) {
				   TypeBinding givenTypeArgument = ptb.arguments[j];
				   typeArguments[j] = deepSubstitute(givenTypeArgument, environment, substitution);
				   if (typeArguments[j] == null)
					   typeArguments[j] = givenTypeArgument;
				   else if (typeArguments[j] != givenTypeArgument) //$IDENTITY-COMPARISON$
					   hasInstantiated = true;
			   }
			   if (hasInstantiated)
				   type = ptb.genericType();
			   else
				   typeArguments = null;
		   }
	   }

       if (type instanceof DependentTypeBinding) {
    	   TypeBinding substituted = substitution.substitute((DependentTypeBinding) type, typeArguments, dimensions); 
           if (substituted != type) // includes substituted == null //$IDENTITY-COMPARISON$
        	   return substituted;
       }
       if (hasInstantiated) {
    	   TypeBinding parameterized = environment.createParameterizedType((ReferenceBinding)type, typeArguments, original.enclosingType());
    	   if (dimensions == 0)
    		   return parameterized;
    	   return environment.createArrayType(parameterized, dimensions);
       }
       return original;
	}
}
