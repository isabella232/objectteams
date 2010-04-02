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
 * $Id: FieldAccessSpec.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;


import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutScope;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * NEW for OTDT:
 *
 * Specifies a base field to be accessed via callout (get or set).
 *
 * What: Simulate the get/set methods to be created by the OTRE.
 * How:  see resolveFeature() and creatMethod().
 *       Note, that methods are generated as static methods, so that
 * 	     they will behave correctly when accessing private fields.
 *
 * What: Typechecking.
 * How:  Override these hooks of MethodSpec:
 * 			resolveFeature(), resolvedParameters(),
 * 			checkResolutionSuccess(), checkReturnType()
 *
 * What: Delegate some access to resolvedField
 * Which: resolvedType(), isValid(), problemId()
 *
 * @author stephan
 * @version $Id: FieldAccessSpec.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class FieldAccessSpec extends MethodSpec implements InvocationSite {

	public int calloutModifier; // either TokenNameget or TokenNameset (from TerminalTokens).
	public FieldBinding resolvedField; // the field in the corresponding base class
	/** Use this field instead of resolvedField.type,
	 *  in case we had to improve a team anchor. */
	private TypeBinding fieldType;

	public FieldAccessSpec(char[] name, long nameSourcePositions, boolean isSetter) {
		super(name, nameSourcePositions);
		this.calloutModifier = isSetter ? TerminalTokens.TokenNameset : TerminalTokens.TokenNameget;
	}

	/**
	 * @param other copy values from this spec
	 * @param calloutModifier either TokenNameset or TokenNameget
	 */
	public FieldAccessSpec(MethodSpec other, int calloutModifier) {
		super(other.selector, other.sourceStart, other.sourceEnd);
		this.returnType = other.returnType;
		this.calloutModifier = calloutModifier;
	}

	/**
	 * @param name
	 * @param type
	 * @param nameSourcePositions (s<<32+e encoded)
	 * @param calloutModifier either TokenNameset or TokenNameget
	 */
	public FieldAccessSpec(char[] name, TypeReference type, long nameSourcePositions, int calloutModifier) {
		super(name, nameSourcePositions);
		this.calloutModifier = calloutModifier;
		if (calloutModifier == TerminalTokens.TokenNameset) {
			// prepare the argument of a setter for parameter mapping
			// (name is identical to field name)
			this.arguments = new Argument[] {
				new Argument(name, nameSourcePositions, type, 0)
			};
			this.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
			this.returnType.sourceStart = this.sourceStart;
			this.returnType.sourceEnd   = this.sourceEnd;
		} else {
			this.returnType = type;
		}
	}

	/** like above, but for clients not using TerminalTokens. */
	public FieldAccessSpec(char[] name, TypeReference type, long nameSourcePositions, boolean isSetter) {
		this(name, type, nameSourcePositions, isSetter ? TerminalTokens.TokenNameset : TerminalTokens.TokenNameget);
	}

	@Override
	public void resolveFeature(ReferenceBinding baseType, BlockScope scope, boolean callinExpected, boolean isBaseSide, boolean allowEnclosing)
    {
    	// find field in type or superclass:
		this.resolvedField = TypeAnalyzer.findField(baseType, this.selector, /*don't check static*/false, /*outer*/false);
		if (this.resolvedField == null)
			this.resolvedField = new ProblemFieldBinding(baseType, this.selector, ProblemReasons.NotFound);
			// Note: if more resilience is desired, try to set resolvedField.resolvedType and proceed
   		if (!this.resolvedField.isValidBinding())
   			return;

		this.fieldType = resolvedType(); // may be improved below

   		TypeBinding fieldLeafType = this.fieldType.leafComponentType();
   		if (   fieldLeafType instanceof ReferenceBinding
			&& ((ReferenceBinding)fieldLeafType).isRole())
   		{
   			// find team anchor for role type of base field:
   			ITeamAnchor newAnchor = null;
   			if (baseType instanceof RoleTypeBinding)
   			{
	   			// base class is already a role: construct the full team anchor:
	   			RoleTypeBinding baseRole = (RoleTypeBinding)baseType;
	   			if (fieldLeafType instanceof RoleTypeBinding) {
	   				RoleTypeBinding fieldRole = (RoleTypeBinding)fieldLeafType;
	   				if (fieldRole.hasExplicitAnchor())
	   					newAnchor = fieldRole._teamAnchor.setPathPrefix(baseRole._teamAnchor);
	   				else
	   					newAnchor = baseRole._teamAnchor;
	   			} else {
	   				newAnchor = baseRole._teamAnchor;
	   			}
   			} else if (baseType.isTeam()) {
   				// base class is a team, construct simple anchor

   				ReferenceBinding enclRole = scope.enclosingSourceType();
   				newAnchor = TypeAnalyzer.findField(enclRole, IOTConstants._OT_BASE, /*don't check static*/false, /*outer*/false);
   			} // else fieldLeafType might already be a anchored type,
   			  // independent of _OT$base, leave it as it is
   			if (newAnchor != null && newAnchor.isValidBinding())
   				this.fieldType = newAnchor.getRoleTypeBinding(
   					(ReferenceBinding)fieldLeafType, resolvedType().dimensions());
   		}

   		// find accessor method which might have been generated already.
		char[] accessorSelector = getSelector();
		MethodBinding result = null;
		if (!this.resolvedField.isPrivate()) // don't reuse accessor to private field from super-base (see Trac #232)
			result = baseType.getMethod(scope, accessorSelector);
		// NOTE: could be optimized if type has no such method but exact type already has.
		//       but the the OTRE would need to be informed..

		if (   result == null
		    || !isMethodCompatible(result))
		{
			// record this field access for Attribute generation:
			RoleModel roleModel = scope.enclosingSourceType().roleModel;
			ReferenceBinding targetClass =  roleModel.addAccessedBaseField(this.resolvedField, this.calloutModifier);

			// create accessor method:
			result = createMethod(targetClass, accessorSelector);
			baseType.addMethod(result);
		}
		this.selector = accessorSelector;
		this.resolvedMethod = result;
		this.parameters = this.resolvedMethod.getSourceParameters();
    }

	private boolean isMethodCompatible(MethodBinding result) {
		TypeBinding methodType = null;
		switch (this.calloutModifier) {
		case TerminalTokens.TokenNameget:
			methodType = result.returnType;
			break;
		case TerminalTokens.TokenNameset:
			int valueArgPosition = this.resolvedField.isStatic() ? 0 : 1;
			if (result.parameters.length <= valueArgPosition)
				return false; // shouldn't happen
			methodType = result.parameters[valueArgPosition];
			break;
		}
		return this.fieldType.isCompatibleWith(methodType);
	}

	private char[] getSelector() {
    	if (this.calloutModifier == TerminalTokens.TokenNameget)
    		return CharOperation.concat(IOTConstants.OT_GETFIELD, this.selector);
    	else
    		return CharOperation.concat(IOTConstants.OT_SETFIELD, this.selector);
	}

	/**
	 *  Create a faked method binding representing the access method to be generated by OTRE.
	 */
	private MethodBinding createMethod(ReferenceBinding baseType, char[] accessorSelector) {
		if (baseType instanceof RoleTypeBinding)
			baseType = baseType.getRealClass();
    	if (this.calloutModifier == TerminalTokens.TokenNameget) {
    		// Use the actual field type rather than the expected type (role view)
    		// because several callouts to the same field could exist.
    		// RoleTypeCreator.maybeWrapQualifiedRoleType(MessageSend,BlockScope)
    		// will wrap the type using a faked _OT$base receiver.
    		return createGetAccessorBinding(baseType, this.resolvedField);
		} else {
    		TypeBinding declaredFieldType = this.hasSignature ?
					this.parameters[0] :
					this.fieldType;
    		TypeBinding[] argTypes = this.resolvedField.isStatic() ?
    									new TypeBinding[]{declaredFieldType} :
    									new TypeBinding[]{baseType, declaredFieldType};
			return new MethodBinding(
    				ClassFileConstants.AccPublic|ClassFileConstants.AccStatic,
						accessorSelector,
						TypeBinding.VOID,
						argTypes,
						Binding.NO_EXCEPTIONS,
						baseType);
    	}
	}

	/** Create a faked method binding for a getAccessor to a given base field. */
	public static MethodBinding createGetAccessorBinding(ReferenceBinding baseType,
												         FieldBinding     resolvedField)
	{
		TypeBinding[] argTypes = resolvedField.isStatic() ?
									new TypeBinding[0] :
									new TypeBinding[]{baseType};
		return new MethodBinding(
					ClassFileConstants.AccPublic|ClassFileConstants.AccStatic,
					CharOperation.concat(IOTConstants.OT_GETFIELD, resolvedField.name),
					resolvedField.type,
					argTypes,
					Binding.NO_EXCEPTIONS,
					baseType);
	}

    public TypeBinding resolvedType() {
    	if (this.fieldType != null)
    		return this.fieldType;  // may contain more precise team anchor than the field.
    	return this.resolvedField.type;
    }
    public TypeReference declaredType() {
    	if (!this.hasSignature)
    		return null;
    	if (isSetter())
    		return this.arguments[0].type;
    	else
    		return this.returnType;
    }
    /**
     * Chop of first argument, which actually is the receiver
     * (only staticness of the method makes it look differently.)
     */
    public TypeBinding[] resolvedParameters() {
    	TypeBinding[] methodParams = super.resolvedParameters();
    	if (this.resolvedField.isStatic())
    		return methodParams; // no base argument when accessing a static field
		TypeBinding[] result = new TypeBinding[methodParams.length-1];
    	System.arraycopy(methodParams, 1, result, 0, result.length);
    	return result;
    }

	public ReferenceBinding getDeclaringClass() {
		if (this.resolvedField != null)
			return this.resolvedField.declaringClass;
		return null;
	}

	public void checkResolutionSuccess(ReferenceBinding type, CallinCalloutScope scope)
	{
		if (this.resolvedField== null)
			this.resolvedField = new ProblemFieldBinding(type, this.selector,ProblemReasons.NotFound);
		else if (this.resolvedField.isValidBinding())
			return; // OK

		scope.problemReporter().boundMethodProblem(this, type, true/*isCallout*/);
	}

	@Override
	void checkDecapsulation(ReferenceBinding baseClass, Scope scope) {
		baseClass= baseClass.getRealClass(); // fields are in the class!
		if (!this.resolvedField.canBeSeenBy(baseClass, this, scope))
   			scope.problemReporter().decapsulation(this, baseClass, scope);
	}

	public boolean checkBaseReturnType(CallinCalloutScope scope, int bindDir)
	{
		TypeBinding accessorReturnType = (this.calloutModifier  == TerminalTokens.TokenNameget) ?
				resolvedType() :
				TypeBinding.VOID;
		if (!TypeAnalyzer.isSameType(scope.enclosingSourceType(), accessorReturnType, this.resolvedMethod.returnType))
		{
			// copied from MethodSpec, but may not be needed:
			if (RoleTypeCreator.isCompatibleViaBaseAnchor(scope, accessorReturnType, this.returnType.resolvedType, bindDir))
				return true;

			scope.problemReporter().differentTypeInFieldSpec(this);
			return false;
		}
		return true;
	}

	@Override
	public boolean checkParameterTypes(CallinCalloutScope scope, boolean isBase)
	{
		if (this.calloutModifier == TerminalTokens.TokenNameget)
			return true; // no parameter
		int argumentPosition = 0; // safer against AIOOBE
		if (this.resolvedField != null && !this.resolvedField.isStatic())
			argumentPosition = 1;
		TypeBinding accessorParamType = this.resolvedMethod.parameters[argumentPosition];
		ReferenceBinding baseclass = scope.enclosingReceiverType().baseclass();
		if (baseclass != null && baseclass.isTeam() && accessorParamType.isRole())
			accessorParamType = TeamModel.strengthenRoleType(baseclass, accessorParamType);
		if (!TypeAnalyzer.isSameType(
				scope.enclosingSourceType(),
				resolvedType(),
				accessorParamType))
		{
			scope.problemReporter().differentTypeInFieldSpec(
					this);
			return false;
		}
		return true;
	}


	public char[] getFieldName() {
		return this.resolvedField.name;
	}
	public boolean isSetter() {
		return this.calloutModifier == TerminalTokens.TokenNameset;
	}
	public boolean isPrivate() {
		return this.resolvedField != null && this.resolvedField.isPrivate();
	}
	public boolean isStatic() {
		return this.resolvedField != null && this.resolvedField.isStatic();
	}
	public boolean isValid() {
		return this.resolvedField.isValidBinding();
	}
	public int problemId() {
		return this.resolvedField.problemId();
	}

	public char[] readableName() {
		return this.resolvedField.readableName();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent,output);
		output.append(this.calloutModifier == TerminalTokens.TokenNameget? "get " : "set "); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.hasSignature)
			printReturnType(0,output);
		output.append(new String(this.selector));
		return output;
	}

	// implement InvocationSite
	public TypeBinding[] genericTypeArguments() {
		return null;
	}
	public boolean isSuperAccess() {
		return false;
	}
	public boolean isTypeAccess() {
		return this.resolvedField != null && this.resolvedField.isStatic();
	}
	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int depth) {
		// ignored
	}

	public void setFieldIndex(int depth) {
		// ignored
	}
}
