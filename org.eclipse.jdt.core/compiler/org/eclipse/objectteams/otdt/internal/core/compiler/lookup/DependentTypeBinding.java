/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DependentTypeBinding.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import java.util.WeakHashMap;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReflectionGenerator;

/**
 * NEW for OTDT
 *
 * This class handles the dependent type mechanisms for role types and types with value parameters.
 *
 * For documentation of declaration of such types see TypeValueParameter.
 * For documentation of application of such types see TypeAnchorReference.
 *
 * Current limitations:
 * - Only one value parameter is supported, yet.
 * 
 * Design note: since 1.3.0 this class is a subclass of ParameterizedTypeBinding in order to
 * support combinations of value parameters and type parameters, specifically, generic role types.
 *
 * @author stephan
 * @since 0.9.0.
 */
public class DependentTypeBinding extends ParameterizedTypeBinding {

	// TODO(SH): in order to support multiple anchors, turn the next fields into arrays!!
	/** The Team to which this role type is anchored, given by a variable which must be final. */
    public ITeamAnchor       _teamAnchor;
    /** For externalized roles anchored to a method argument: index into the method argument list pointing to the team anchor. */
    public int               _argumentPosition = -1;
    public IMethodProvider   _declaringMethod = null;   // the method declaring the anchor argument

    /** A type/method with value parameters stores the field/argument representing that parameter here: */
    public VariableBinding   _matchingVariable;
    /** For value parameters: index into the type parameter list: */
    public int               _valueParamPosition = -1;

    public static interface IMethodProvider {
    	public MethodBinding getMethod();
    }

    // REGISTRIES/CACHES:
    // caching instantiations by anchor:
    private WeakHashMap<ITeamAnchor, DependentTypeBinding> _instantiatedTypes = new WeakHashMap<ITeamAnchor, DependentTypeBinding>();
    // Each DependentTypeBinding stores unique bindings for any dimension of arrays of this type.
	private ArrayBinding[] _arrayBindings = null;

	/** Public constructor for clients (FIXME: let only environment create us) */
	public DependentTypeBinding(ReferenceBinding type, TypeBinding[] typeArguments, ITeamAnchor teamAnchor, int paramPosition, ReferenceBinding enclosingType, LookupEnvironment lookupEnvironment) 
	{
		super(type, typeArguments, enclosingType, lookupEnvironment);
		initializeFromType(type);
		initializeDependentType(teamAnchor, paramPosition);		
	}

	/** For subclasses. Still need to call initializeDependentType afterwards. */
	DependentTypeBinding(ReferenceBinding type, TypeBinding[] arguments, ReferenceBinding enclosingType, LookupEnvironment environment)
	{
		super(type, arguments, enclosingType, environment);
		initializeFromType(type);
	}
	private void initializeFromType(ReferenceBinding givenType) {
		this.model = givenType.model; // shared model from type.
		this.modifiers      = givenType.modifiers;
		this.tagBits        = givenType.tagBits | TagBits.IsDependentType;
		this.tagBits 		&= ~(TagBits.AreMethodsSorted|TagBits.AreMethodsComplete); // in case the generic type was already processed
		
		this.compoundName     	= givenType.compoundName;
		this.sourceName       	= givenType.sourceName;
		this.constantPoolName 	= givenType.constantPoolName;
		this.callinCallouts 	= givenType.callinCallouts;
		this.precedences    	= givenType.precedences;
		this.fileName       	= givenType.fileName;
		this.fPackage       	= givenType.fPackage;
		this.teamPackage    	= givenType.teamPackage;

		if (givenType.isTypeVariable()) { 									// concrete type unknown ...
			ITeamAnchor[] anchors = ((TypeVariableBinding)givenType).anchors;
			if (anchors != null && anchors[0] instanceof LocalVariableBinding) {
				final LocalVariableBinding anchor = (LocalVariableBinding) anchors[0];
				if ((anchor.tagBits & TagBits.IsArgument) != 0) {		// ... but known to be anchored to an argument
					this._argumentPosition = anchor.resolvedPosition;
					this._declaringMethod = new IMethodProvider() { public MethodBinding getMethod() {
						return ((MethodScope)anchor.declaringScope).referenceMethodBinding();
					}};
				}
			}
		}
		// registerAnchor();
	}

	void initializeDependentType(ITeamAnchor anchor, int valueParamPosition) 
	{
		// don't store baseclass, will be initialized by baseclass()

		this._valueParamPosition = valueParamPosition;
		SyntheticArgumentBinding[] valParams = this.type.valueParamSynthArgs();
		if (valueParamPosition > -1) // subtypes don't set paramPosition/matchingField
			if (valueParamPosition < valParams.length) // defensive
				this._matchingVariable = valParams[valueParamPosition].matchingField;

		this._teamAnchor = anchor;
		if (anchor instanceof LocalVariableBinding)
			((LocalVariableBinding)anchor).useFlag = LocalVariableBinding.USED;
		else if (anchor instanceof FieldBinding)
			((FieldBinding)anchor).modifiers |= ExtraCompilerModifiers.AccLocallyUsed; // avoid unused-warning
	}

	public ITeamAnchor getAnchor() { return this._teamAnchor; }

    /* answer a (cached) array type whose elements are of the current RoleType. */
    // TODO(SH): should we replace this with Scope.createArray??
	public ArrayBinding getArrayType(int dims) {
	    if ((this._arrayBindings == null) || (this._arrayBindings.length < dims)) {
	        ArrayBinding[] oldArrays = this._arrayBindings;
	        this._arrayBindings = new ArrayBinding[dims];
	        if (oldArrays != null)
	            System.arraycopy(oldArrays, 0, this._arrayBindings, 0, oldArrays.length);
	    }
	    if (this._arrayBindings[dims-1] == null)
	        this._arrayBindings[dims-1] = new ArrayBinding(this, dims, this.environment);
	    return this._arrayBindings[dims-1];
	}

	/**
	 * Register an anchor-RTB pair at both sides.
	 */
	protected void registerAnchor() {
	    this._instantiatedTypes.put(this._teamAnchor, this);
	}

	/** Does this type have an anchor other than tthis? */
	public boolean hasExplicitAnchor() {
		return this._teamAnchor != null &&
			   this._teamAnchor.isValidBinding() &&
			   !(this._teamAnchor instanceof TThisBinding);
	}

	/** Answer whether this dependent type is anchored within a method's signature,
     *  and if a method is given, also check whether that's the one.
     */
	public boolean hasAnchorWithinThisMethodsSignature(MethodBinding method) {
		if (method != null) {
			if (this._declaringMethod == null)
				return false;
			if (method != this._declaringMethod.getMethod())
				return false;
		}
		return this._argumentPosition > -1;
	}

	/**
	 * Refine the anchor of this role type to be 'anchor'.
	 * Retrieve from cache whenever possible.
	 * Donot merge anchors.
	 * Donot downgrade explicit anchor to TThis.
	 *
	 * @param anchor
	 * @param dimensions
	 * @return a role type binding, possibly this.
	 */
	public TypeBinding maybeInstantiate(ITeamAnchor anchor, int dimensions) {
		RoleTypeBinding cached = (RoleTypeBinding)this._instantiatedTypes.get(anchor);
		if (cached != null)
			return cached;
	    if (   anchor == null
	    	|| anchor == this._teamAnchor)
	    {
	    	if (dimensions > 0)
	    		return getArrayType(dimensions);
	    	return this;
	    }
	    if (anchor instanceof TThisBinding) {
	    	if (   !(this._teamAnchor instanceof TThisBinding)
	    		|| this._teamAnchor.isTypeCompatibleWithTypeOf(anchor))
	    	{
	    		// don't downgrade:
	        	if (dimensions > 0)
	        		return getArrayType(dimensions);
	    		return this;
	    	}
	    }

	    if (!anchor.isTypeCompatibleWithTypeOf(this._teamAnchor))
	    	return this;
	    // clone:
	    return forAnchor(anchor, dimensions);
	}

	// hook within maybeInstantiate:
	TypeBinding forAnchor(ITeamAnchor anchor, int dimensions) {
		return anchor.getRoleTypeBinding(this, dimensions); // TODO(SH): use a shared type instead of this?
	}

	public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
		this.transferTypeArguments(this.type).collectSubstitutes(scope, actualType, inferenceContext, constraint);
	}
	
	@Override
	public ReferenceBinding transferTypeArguments(ReferenceBinding other) {
		if (this.arguments == null) // although subclass of ParameterizedTypeBinding, we don't necessarily have arguments
			return other;
		return super.transferTypeArguments(other);
	}

	public static boolean mayTakeValueParam(TypeBinding binding) {
		if (binding.isRole())
			return true;
		if (binding.isTypeVariable())
			return true; // could be anchored type variable
		return binding.valueParamSynthArgs() != NO_SYNTH_ARGUMENTS;
	}
    public static boolean isDependentType(TypeBinding binding) {
    	if (binding == null) return false;
    	return (binding.tagBits & TagBits.IsDependentType) != 0;
    }

    public static boolean isPlainDependentType(TypeBinding binding) {
    	if (binding == null) return false;
    	return (binding.tagBits & (TagBits.IsDependentType|TagBits.IsWrappedRole)) == TagBits.IsDependentType;
	}
    @Override
    public DependentTypeBinding asPlainDependentType() {
    	if ((this.tagBits & (TagBits.IsWrappedRole)) == 0)
    		return this;
    	return null;
    }
    
    /**
     * Is type a dependent type and declaredAnchor the field representing its type value parameter?
     */
    public static boolean isDependentTypeOf(TypeBinding type, ITeamAnchor declaredAnchor) {
    	if (!isPlainDependentType(type))
    		return false;
    	DependentTypeBinding depType = (DependentTypeBinding)type;
    	return depType._matchingVariable == declaredAnchor;
    }

	public static boolean isDependentTypeVariable(TypeBinding binding) {
		if ((binding.tagBits & (TagBits.IsDependentType|TagBits.IsWrappedRole)) == 0)
			return false;
		return ((DependentTypeBinding)binding).type.kind() == TYPE_PARAMETER;
	}

	@Override
	public boolean isEquivalentTo(TypeBinding otherType) {
		if (! (otherType instanceof DependentTypeBinding))
			return this.type.isEquivalentTo(otherType);
		DependentTypeBinding otherDep = (DependentTypeBinding)otherType;
		// same best name implies that checking of simple names suffices (thanks to OTJLD 1.4(c)):
		return otherDep._teamAnchor.hasSameBestNameAs(this._teamAnchor)
			&& CharOperation.equals(otherDep.internalName(), internalName());
	}

	@Override
	public boolean isProvablyDistinct(TypeBinding otherType) {
		if (DependentTypeBinding.isDependentType(otherType))
			otherType = ((DependentTypeBinding)otherType).getRealType();
			// for internal casting purposes type anchors can be ignored:
		return getRealType().isProvablyDistinct(otherType);
	}
	
	@Override
	public int kind() {
		return (this.arguments != null) ? Binding.PARAMETERIZED_TYPE : this.type.kind();
	}

	/**
	 * Re-check lowering ambiguity (OTJLD 2.2(f)) after type checking had detected this situation before. 
	 * (see {@link Config#getLoweringPossible()}).
	 * 
	 * @param otherType declared type to which a value of this type is being attached.
	 * @param location  AST node for problem reporting
	 * @param scope     scope for problem reporting
	 * @param resolvedMethod when resolving a message send this is the resolved method
	 */
	public void recheckAmbiguousLowering(TypeBinding otherType, ASTNode location, Scope scope, MethodBinding resolvedMethod) {
		if (scope.isGeneratedScope()) return;
		if (   otherType instanceof ReferenceBinding
		    && ((ReferenceBinding)otherType).id == TypeIds.T_JavaLangObject
		    && baseclass() != null)
	    {
			if (resolvedMethod != null) {
				if (resolvedMethod.declaringClass.isTeam()) {
					// don't report against unregisterRole(Object), unregisterRole(Object,Class)
					//  -- we know arg should be the unlowered role!
					if (   CharOperation.equals(resolvedMethod.selector, ReflectionGenerator.UNREGISTER_ROLE)
						&& (   resolvedMethod.parameters.length == 1
						    || resolvedMethod.parameters.length == 2))
						return; 
				}
			}
			scope.problemReporter().ambiguousUpcastOrLowering(location, otherType, this);
	    }
	}

	// ===>>> delegating methods:
	@Override
	public FieldBinding[] fields() {
		return this.type.fields();
	}
	@Override
	public FieldBinding getField(char[] fieldName, boolean needResolve) {
		return this.type.getField(fieldName, needResolve);
	}
	@Override
	public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
		return this.type.getExactConstructor(argumentTypes);
	}
	@Override
	public ReferenceBinding getMemberType(char[] typeName) {
		return this.type.getMemberType(typeName);
	}
	@Override
	public SyntheticArgumentBinding[] valueParamSynthArgs() {
		return this.type.valueParamSynthArgs();
	}

	@Override
    public TeamModel getTeamModel() {
    	if (this._teamModel == null && this.type != null)
    		this._teamModel = this.type.getTeamModel();
    	return this._teamModel;
    }

	@Override
	public ReferenceBinding getRealType() {
		return this.type.getRealType();
	}

	@Override
	public ReferenceBinding getRealClass() {
		return this.type.getRealClass();
	}

	@Override
	public ReferenceBinding rawBaseclass() {
		return this.type.rawBaseclass();
	}

	@Override
	public void setIsBoundBase(ReferenceBinding roleType) {
		super.setIsBoundBase(roleType);
		this.type.setIsBoundBase(roleType);
	}
	// delegating methods <<<===

    @SuppressWarnings("nls")
	public String toString() {
    	String anchorStr = "";
    	ITeamAnchor[] bestNamePath = this._teamAnchor.getBestNamePath(false);
		for (int i = 0; i < bestNamePath.length; i++) {
    		if (i>0)
    			anchorStr += ".";
			anchorStr += new String(bestNamePath[i].readableName());
		}
        return
            new String(sourceName())
            + "<@" + anchorStr + ">";
    }

    @Override
	public boolean appendReadableValueParameterNames(StringBuffer buf) /*@anchor[,...]*/ {
    	buf.append('@');
    	buf.append(anchorName());
    	return true;
    }

	private char[] anchorName() {
		char[] anchorName = new char[0];
    	ITeamAnchor[] bestNamePath = this._teamAnchor.getBestNamePath(false);
		for (int i = 0; i < bestNamePath.length; i++) {
    		char[] segment = bestNamePath[i].readableName();
    		if (CharOperation.equals(segment, IOTConstants._OT_BASE))
    			segment = "base".toCharArray(); //$NON-NLS-1$
			anchorName = CharOperation.concatWith(
    				new char[][]{anchorName, segment},
					'.');
		}
		return anchorName;
	}

}
