/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2006, 2013 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.UnresolvedReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateHelper;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;


/**
 *  Wraps a name reference for use as a type anchor, pretending to be a type reference.
 *  This is denoted by the syntax: R<@teamInstance>
 *  Note, that the syntax requires this to be a subclass of TypeReference whereas
 *  in fact a TypeAnchorReference represents a value (see also TypeValueParameter).
 *
 *  TypeAnchorReferences serve two purposes:
 *  + During type checking the type anchors of two types must be provably the same reference
 *    for two types to be compatible.
 *  + Creating an instance of a type with a TypeValueParameter automatically passes the
 *    TypeAnchorReference as an invisible argument to the constructor (comparable to outer instances).
 *
 *  Life-cycle of TypeAnchorReferences
 *  <ul>
 *  <li>Parser creates them from <@teamInstance>
 *  <li>internalResolveType of Parameterized{Single,Qualified}TypeReference
 *  	invokes resolveAnchor(..) and checkParameterizedTypeReference(..)
 *  <li>AllocationExpression.generateCode add the implicit argument,
 *      information is passed via a DependentTypeBinding
 *  </ul>
 *
 *  Additionally a type anchor reference is used by callout-to-private-role-method to construct
 *  the receiver expression. In that case also resolveType and generateCode are supported.
 *
 * @author stephan
 */
public class TypeAnchorReference extends TypeReference implements InvocationSite {

	// either NameReference or FieldReference
	public Reference anchor;
	public boolean isExpression = false;

	public TypeAnchorReference(Reference anchor, int sourceStart) {
		this.anchor = anchor;
		this.sourceStart = sourceStart;
		this.sourceEnd = anchor.sourceEnd;
		anchor.bits |= IsStrictlyAssigned;
	}

	@Override
	public TypeReference augmentTypeWithAdditionalDimensions(int additionalDimensions, Annotation[][] additionalAnnotations, boolean isVarargs) {
		throw new InternalCompilerError("Method not applicable"); //$NON-NLS-1$
	}

	@Override
	public void setBaseclassDecapsulation(DecapsulationState state) {
		super.setBaseclassDecapsulation(state);
		if (this.anchor instanceof QualifiedThisReference) // incl. QualifiedBaseReference
			((QualifiedThisReference)this.anchor).qualification.setBaseclassDecapsulation(state);
	}

	@Override
	protected TypeBinding getTypeBinding(Scope scope) {
		throw new InternalCompilerError("TypeAnchorReference is not intended to be used here"); //$NON-NLS-1$
	}

	@Override
	public char[][] getTypeName() {
		return getTypeName(this.anchor);
	}
	char[][] getTypeName(Reference reference) {
		char[][] result;
		if (reference instanceof SingleNameReference) {
			result= new char[][]{((SingleNameReference)reference).token};
		} else if (reference instanceof FieldReference) {
			FieldReference fieldRef = (FieldReference)reference;
			result = CharOperation.arrayConcat(getTypeName((Reference) fieldRef.receiver), fieldRef.token);
		} else if (reference instanceof QualifiedBaseReference) {
			QualifiedBaseReference baseRef = (QualifiedBaseReference) reference;
			char[][] tokens = baseRef.qualification.getTypeName();
			int len = tokens.length; 
			System.arraycopy(tokens, 0, result=new char[len+1][], 0, len);
			result[len] = IOTConstants.BASE;
		} else if (reference instanceof QualifiedThisReference) {
			QualifiedThisReference thisRef = (QualifiedThisReference) reference;
			char[][] tokens = thisRef.qualification.getTypeName();
			int len = tokens.length; 
			System.arraycopy(tokens, 0, result=new char[len+1][], 0, len);
			result[len] = "this".toCharArray(); //$NON-NLS-1$
		} else {
			char[][] orig= ((QualifiedNameReference)this.anchor).tokens;
			result= new char[orig.length][];
			System.arraycopy(orig, 0, result, 0, orig.length); // shallow copy
		}

		result[0] = CharOperation.concat(new char[]{'@'}, result[0]);
		return result;
	}

	@Override
	public char[] getLastToken() {
		if (this.anchor instanceof SingleNameReference)
			return ((SingleNameReference)this.anchor).token;
		if (this.anchor instanceof QualifiedBaseReference)
			return IOTConstants.BASE;
		if (this.anchor instanceof FieldReference)
			return ((FieldReference)this.anchor).token;
		char[][] tokens = ((QualifiedNameReference)this.anchor).tokens;
		return tokens[tokens.length-1];
	}

	@Override
	public TypeBinding resolveType(ClassScope classScope) {
		classScope.problemReporter().valueParamWrongPosition(this);
		return null;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds, int location) {
		if (!this.isExpression) {
			scope.problemReporter().valueParamWrongPosition(this);
			return null;
		}
		// support to interpret this reference as an expression (see CalloutImplementor)
		ITeamAnchor binding = resolveAnchor(scope);
		if (binding == null)
			return null;
		if (binding.isValidBinding()) {
			ReferenceBinding receiverType = null;
			int bit =  Binding.LOCAL;
			if (binding instanceof FieldBinding) {
				bit = Binding.FIELD;
				receiverType = ((FieldBinding)binding).declaringClass;
			}
			this.bits &= ~RestrictiveFlagMASK;
			this.bits |= bit;
			this.anchor.bits &= ~RestrictiveFlagMASK;
			this.anchor.bits |= bit;
			this.constant = Constant.NotAConstant;
			this.anchor.constant = Constant.NotAConstant;
			int depth = 0;
			if (receiverType != null && this.anchor instanceof InvocationSite) { // could be QualifiedBaseReference which sets its depth during resolveAnchor
				ReferenceBinding currentType = scope.enclosingSourceType();
				while (!currentType.isCompatibleWith(receiverType)) {
					depth++;
					currentType = currentType.enclosingType();
					if (currentType == null)
						return null; // shouldn't happen, if callout was constructed correctly.
				}
				((InvocationSite)this.anchor).setDepth(depth);
			}
		}
		return this.resolvedType = binding.getResolvedType();
	}

	/**
	 * Resolve this anchor reference to a team anchor.
	 * @return either a valid binding or null.
	 */
	public ITeamAnchor resolveAnchor(Scope scope) {
		ITeamAnchor result = resolveAnchor(scope, this.anchor);
		if (result != null)
			this.resolvedType = result.getResolvedType();
		return result;
	}
	
	ITeamAnchor resolveAnchor(Scope scope, Reference reference) {
		ITeamAnchor prefix = null;
		ITeamAnchor currentAnchor = null;
		char[] currentToken = null; // for lookup and creation of problem binding
		
		// be careful not to trigger fields() which may be where we are called from!
		if (reference instanceof SingleNameReference) {
			SingleNameReference singleAnchor = (SingleNameReference)reference;
			currentToken = singleAnchor.token;
			currentAnchor = findVariable(
					scope, currentToken, scope.isStatic(), singleAnchor.sourceStart, singleAnchor.sourceEnd);
			this.anchor.bits |= (this.bits & DepthMASK);
			// could be ProblemAnchorBinding
		} else if (reference instanceof FieldReference) {
			FieldReference fieldRef = (FieldReference)reference;
			Expression prefixExpr = fieldRef.receiver;
			if (!(prefixExpr instanceof Reference))
				throw new InternalCompilerError("Unexpected anchor prefix "+prefixExpr); //$NON-NLS-1$
			prefix = resolveAnchor(scope, (Reference)prefixExpr);
			currentToken = fieldRef.token;
			// fieldRef holds on to problem binding:
			fieldRef.binding = TypeAnalyzer.findField(((ReferenceBinding)prefix.getResolvedType()).getRealClass(), currentToken, false/*static*/, true/*outer*/); 
			currentAnchor = checkAnchor(scope, reference, currentToken, reference.sourceStart, reference.sourceEnd, fieldRef.binding);
		} else if (reference instanceof QualifiedBaseReference) {
			QualifiedBaseReference baseRef = (QualifiedBaseReference) reference;
			if (scope instanceof BlockScope)
				baseRef.resolveType((BlockScope)scope);
			else 
				baseRef.resolveType((ClassScope)scope);
			currentAnchor = baseRef.baseField;
		} else if (reference instanceof QualifiedThisReference) {
			QualifiedThisReference thisRef = (QualifiedThisReference) reference;
			if (scope instanceof BlockScope)
				thisRef.resolveType((BlockScope)scope);
			else 
				thisRef.resolveType((ClassScope)scope);
			if (thisRef.resolvedType.isTeam())
				currentAnchor = ((ReferenceBinding)thisRef.resolvedType).getTeamModel().getTThis();
		} else {
			boolean haveReportedProblem = false;
			long currentPos = 0;

			QualifiedNameReference qualifiedAnchor = (QualifiedNameReference)reference;
			char[][] tokens = qualifiedAnchor.tokens;
			currentToken = tokens[tokens.length-1]; // default, so we never use null name for problem binding
			// check maximal static prefix:
			Binding staticPrefix = null;
			int j;
			for (j = 1; j <= tokens.length; j++) {
				Binding current = scope.getTypeOrPackage(CharOperation.subarray(tokens, 0, j));
				if (current == null || !current.isValidBinding())
					break;
				else
					staticPrefix = current;
			}
			if (j > tokens.length) {
				scope.problemReporter().typeAnchorReferenceNotAValue(reference);
				haveReportedProblem = true;
			} else {
				// find first field:
				if (staticPrefix != null) {
					currentPos = qualifiedAnchor.sourcePositions[j-1];
					currentToken = tokens[j-1];
					if (staticPrefix instanceof ReferenceBinding) {
						currentAnchor = TypeAnalyzer.findField(((ReferenceBinding)staticPrefix).getRealClass(), currentToken, /*static*/true, /*outer*/true);
					} else {
						scope.problemReporter().typeAnchorReferenceNotAnObjectRef(
												(int)(currentPos>>>32), (int)currentPos);
						haveReportedProblem = true;
					}
				} else {
					currentPos = qualifiedAnchor.sourcePositions[0];
					currentToken = tokens[0];
					currentAnchor = findVariable(scope, currentToken,
												scope.isStatic(), (int)(currentPos>>>32), (int)currentPos);
					haveReportedProblem = currentAnchor == null;
				}
				if (currentAnchor != null) {
					if (!currentAnchor.hasValidReferenceType()) {
						if (j < tokens.length)	  // would need to process more tokens?
							currentAnchor = null; // replace with problem binding below
					} else {
						// find more fields:
						for (int i = j; i < tokens.length; i++) {
							TypeBinding fieldType = currentAnchor.getResolvedType().leafComponentType();
							if (fieldType instanceof SourceTypeBinding) {
								SourceTypeBinding stb = (SourceTypeBinding)fieldType;
								if ((stb.scope != null)
									&& (stb.scope.compilationUnitScope() != scope.compilationUnitScope()) 
									&& (stb.tagBits & (TagBits.BeginHierarchyCheck|TagBits.EndHierarchyCheck)) == (TagBits.BeginHierarchyCheck|TagBits.EndHierarchyCheck)
									&& StateHelper.isReadyToProcess(stb, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS))
									Dependencies.ensureBindingState(stb, ITranslationStates.STATE_LENV_DONE_FIELDS_AND_METHODS);
							}
							currentPos = qualifiedAnchor.sourcePositions[i];
							currentToken = tokens[i];
							FieldBinding nextField = currentAnchor.getFieldOfType(currentToken, /*static*/false, true);
							if (nextField == null || !nextField.hasValidReferenceType()) {
								currentAnchor = null; // replace with problem binding below
								break;
							}
							currentAnchor = nextField.setPathPrefix(currentAnchor);
						}
					}
				}
			}
			if (!haveReportedProblem) {
				if (currentAnchor == null) {
					scope.problemReporter().typeAnchorNotFound(currentToken,
							(int)(currentPos>>>32), (int)currentPos);
				} else if (!currentAnchor.hasValidReferenceType()) {
					scope.problemReporter().typeAnchorReferenceNotAnObjectRef(
							(int)(currentPos>>>32), (int)currentPos);
				}
			}
		}
		if (currentAnchor == null) {
			currentAnchor = new ProblemFieldBinding(scope.enclosingReceiverType(), currentToken, ProblemReasons.NotFound);
			((FieldBinding)currentAnchor).type =
					  reference.resolvedType = new ProblemReferenceBinding("UnresolvedType".toCharArray(), null, ProblemReasons.NotFound, true); //$NON-NLS-1$
		} else if (currentAnchor.isValidBinding()) {
			if (prefix != null && !(prefix instanceof TThisBinding))
				currentAnchor = currentAnchor.setPathPrefix(prefix);
			
			// fill anchor with resolved data:
			reference.resolvedType = currentAnchor.getResolvedType();
			reference.bits &= ~RestrictiveFlagMASK;  // clear bits
			if (currentAnchor instanceof FieldBinding) {
				reference.bits |= Binding.FIELD;
				// TODO(SH): must we remember a previous anchor to set this correctly?:
				if (reference instanceof NameReference)
					((NameReference)reference).actualReceiverType = ((FieldBinding)currentAnchor).declaringClass;
				if (reference instanceof FieldReference)
					((FieldReference)reference).actualReceiverType = ((FieldBinding)currentAnchor).declaringClass;
			} else {
				reference.bits |= Binding.LOCAL;
			}
			reference.constant = Constant.NotAConstant;
		}
		if (reference instanceof NameReference) {
			((NameReference)reference).binding = (Binding)currentAnchor;
			((NameReference)reference).resolveFinished();
		} else if (reference instanceof FieldReference) {
			((FieldReference)reference).binding = (FieldBinding)currentAnchor;
			//TODO(SH): this method doesn't exist, is the call needed?
			//((FieldReference)this.anchor).resolveFinished();
		}
		return currentAnchor;
	}

	public ITeamAnchor getResolvedAnchor() {
		if (this.anchor instanceof NameReference)
			return (ITeamAnchor) ((NameReference)this.anchor).binding;
		if (this.anchor instanceof FieldReference)
			return ((FieldReference)this.anchor).binding;
		if (this.anchor instanceof QualifiedBaseReference)
			return ((QualifiedBaseReference)this.anchor).baseField;
		return null;
	}

	/**
      * If this reference has a static prefix return its resolved type.
      * (obviously only for qualified anchors).
      */
	public ReferenceBinding resolveStaticPart(Scope scope) {
		// extract from above method:
		// (but cannot use this above, because more than one local variable
		//  would need to be returned to the caller).
		if (!(this.anchor instanceof QualifiedNameReference))
			return null;

		QualifiedNameReference qualifiedAnchor = (QualifiedNameReference)this.anchor;
		char[][] tokens = qualifiedAnchor.tokens;
		// check maximal static prefix:
		Binding staticPrefix = null;
		int j;
		for (j = 1; j <= tokens.length; j++) {
			Binding current = scope.getTypeOrPackage(CharOperation.subarray(tokens, 0, j));
			if (current == null || !current.isValidBinding())
				break;
			else
				staticPrefix = current;
		}
		if (j > tokens.length) {
			scope.problemReporter().typeAnchorReferenceNotAValue(this.anchor);
			return null;
		}
		if (staticPrefix instanceof ReferenceBinding)
			return (ReferenceBinding)staticPrefix;
		return null;
	}

	private ITeamAnchor findVariable(Scope scope, char[] token, boolean isStaticScope, int start, int end)
	{
		if (scope instanceof ClassScope && ((ClassScope)scope).superTypeReference != null) {
			scope.problemReporter().extendingExternalizedRole(((ClassScope)scope).superTypeReference);
			return null;
		}
		VariableBinding anchorBinding = null;
		switch (scope.kind) {
			case Scope.METHOD_SCOPE:
				// check arguments for possible anchor:
				AbstractMethodDeclaration method = ((MethodScope)scope).referenceMethod();
				if (method != null) {
					Argument[] arguments = method.arguments;
					if (arguments != null)
						for (int i = 0; i < arguments.length; i++)
							if (CharOperation.equals(arguments[i].name, token))
								return RoleTypeCreator.resolveTypeAnchoredToArgument(method, i);
				}

				//$FALL-THROUGH$
			case Scope.BLOCK_SCOPE:
			case Scope.BINDING_SCOPE:
				anchorBinding = scope.findVariable(token);
				break;
		}
		if (anchorBinding == null) {
			Binding binding = scope.getBinding(token, Binding.VARIABLE, this, true);
			if (binding instanceof VariableBinding)
				anchorBinding = (VariableBinding) binding;
		}
		return checkAnchor(scope, this.anchor, token, start, end, anchorBinding);
	}

	// post: return is either a valid anchor or null and problem has been reported.
	private ITeamAnchor checkAnchor(Scope scope, Reference reference, char[] token, int start, int end, ITeamAnchor anchorBinding) {
		if (anchorBinding == null) {
			scope.problemReporter().typeAnchorNotFound(token, start, end);
			return null;
		}
		if (anchorBinding instanceof ProblemFieldBinding) {
			if (reference instanceof NameReference)
				scope.problemReporter().invalidField((NameReference)reference, (FieldBinding)anchorBinding);
			else if (reference instanceof FieldReference)
				scope.problemReporter().invalidField((FieldReference)reference, ((FieldReference)reference).actualReceiverType);
			return null;
		}			
		if (!anchorBinding.hasValidReferenceType()) {
			scope.problemReporter().typeAnchorReferenceNotAnObjectRef(start, end);
			return null;
		}
		return anchorBinding;
	}

	/**
	 * Create a DependentTypeBinding from a type reference and a TypeAnchorReference.
	 * Performs the following checks:
	 * - does the type denoted by typeReference have a value parameter?
	 * - does this anchor reference match the declared type of the corresponding value parameter?
	 *
	 * PRE: this.anchor and typeReference have already been resolved,
	 *      however, resolving of typeReference has not yet considered any parameters.
	 *
	 * @param scope
	 * @param typeReference     the type reference decorated with this type anchor.
	 * @param typeParamPosition position within the type parameter list of the generic type
	 * @return a DependentTypeBinding, or and array thereof or null;
	 */
	public TypeBinding createDependentTypeBinding(
			Scope         scope,
			TypeReference typeReference,
			int           typeParamPosition)
	{
		TypeBinding type = typeReference.resolvedType;
		ITeamAnchor anchorBinding = null;
		if (this.anchor instanceof NameReference) 
			anchorBinding = (ITeamAnchor)((NameReference)this.anchor).binding;
		else if (this.anchor instanceof FieldReference)
			anchorBinding = ((FieldReference)this.anchor).binding;
		if(	   anchorBinding != null
			&& type instanceof ReferenceBinding
			&& type.isValidBinding())
		{
			ReferenceBinding refBinding = (ReferenceBinding)type;
			VariableBinding currentParam = refBinding.valueParamSynthArgAt(typeParamPosition);
			if (currentParam == null) {
				scope.problemReporter().typeHasNoValueParamAt(typeReference, refBinding, typeParamPosition);
				return null;
			}
			if (currentParam.type instanceof UnresolvedReferenceBinding) {
				currentParam.type = ((UnresolvedReferenceBinding)currentParam.type).resolve(scope.environment(), false);
			}
			if (currentParam.isValidBinding() && !anchorBinding.isTypeCompatibleWith((ReferenceBinding)currentParam.type))
			{
				scope.problemReporter().incompatibleValueParameter(this, currentParam);
				return null;
			}
			TypeBinding[] typeArguments = refBinding.isParameterizedType() ? ((ParameterizedTypeBinding)refBinding).arguments : null;
			return anchorBinding.getDependentTypeBinding(refBinding, typeParamPosition, typeArguments, typeReference.dimensions());
		} else {
			scope.problemReporter().invalidType(
								typeReference,
								new ProblemReferenceBinding(typeReference.getTypeName(), null, ProblemReasons.NotFound));
			return null;
		}
	}

	@Override
	public void generateCode(
			BlockScope currentScope,
			CodeStream codeStream,
			boolean valueRequired)
	{
		if (this.isExpression) {
			// support interpration as an expression (see CalloutImplementor)
			this.anchor.generateCode(currentScope, codeStream, valueRequired);
		} else
			super.generateCode(currentScope, codeStream, valueRequired); // trigger original error
	}

	@Override
	public void traverse(ASTVisitor visitor, ClassScope classScope) {
		if (visitor.visit(this, classScope))
			this.anchor.traverse(visitor, classScope);
		visitor.endVisit(this, classScope);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope))
			this.anchor.traverse(visitor, blockScope);
		visitor.endVisit(this, blockScope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append('@');
		return this.anchor.printExpression(indent, output);
	}

	/** 
	 * Fetch a bitset marking all those arguments that are referenced as a type anchor from an argument or type parameter.
     */
	public static boolean[] fetchAnchorFlags(Argument[] arguments, TypeParameter[] typeParameters) {
		boolean[] flags = new boolean[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i].type instanceof ParameterizedSingleTypeReference) {
				TypeReference[] parameters = ((ParameterizedSingleTypeReference)arguments[i].type).typeArguments;
				if (parameters.length > 0 && parameters[0] instanceof TypeAnchorReference)
					checkTypeAnchorXRef((TypeAnchorReference)parameters[0], arguments, i, flags);
			}
		}
		if (typeParameters != null)
			for (int i = 0; i < typeParameters.length; i++)
				if (typeParameters[i].type instanceof TypeAnchorReference) 
					checkTypeAnchorXRef((TypeAnchorReference)typeParameters[i].type, arguments, -1, flags);
		return flags;
	}
	private static void checkTypeAnchorXRef(TypeAnchorReference anchorRef, Argument[] arguments, int argPos, boolean[] flags) {
		if (anchorRef.anchor instanceof SingleNameReference) {
			char[] name = ((SingleNameReference)anchorRef.anchor).token;
			for (int j = 0; j < arguments.length; j++) {
				if (j == argPos) continue;
				if (CharOperation.equals(arguments[j].name, name)) {
					flags[j] = true;
					break;
				}
			}
		}		
	}

	// === implement InvocationSite: ===

	public TypeBinding[] genericTypeArguments() {
		return Binding.NO_TYPES;
	}

	public boolean isSuperAccess() {
		return false;
	}

	public boolean isTypeAccess() {
		return false;
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// TODO Auto-generated method stub
	}

	public void setDepth(int depth) {
		this.bits &= ~DepthMASK; // flush previous depth if any
		if (depth > 0) {
			this.bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
		}
	}

	public void setFieldIndex(int depth) {
		// ignored
	}
	
	public InferenceContext18 freshInferenceContext(Scope scope) {
		throw new InternalCompilerError("Method not applicable"); //$NON-NLS-1$
	}
}
