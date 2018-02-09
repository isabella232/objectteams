/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2018 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
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
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import static org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants.STEP_OVER_LINENUMBER;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.CompilationResult.CheckPoint;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.FieldAccessSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.PotentialLiftExpression;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.QualifiedBaseReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.ResultReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TSuperMessageSend;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TsuperReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TThisBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * This factory creates "generated" elements, ie., elements that have
 * no direct connection to source code. Most functions are just simple factory
 * methods which however set source positions.
 *
 * @author stephan
 */
public class AstGenerator extends AstFactory {

	static final int AccIfcMethod = AccAbstract|AccSemicolonBody;
	public long sourceLevel = ClassFileConstants.JDK1_5;

	/** 
	 * If set to non-null, base anchored types matching this anchor are not encoded using the base field 
	 * (possibly with static prefix),
	 * but a simple name "base" is used, representing the base argument in a callin wrapper.
	 */
	public ITeamAnchor replaceableBaseAnchor = null;
	
	/**
	 * If this reference is non-null, qualifications in qualified this references that refer
	 * to <code>replaceableEnclosingClass</code>'s super class should be updated to 
	 * <code>replaceableEnclosingClass</code> itself (strengthening during copy).
	 */
	public ReferenceBinding replaceableEnclosingClass;
	
	public AstGenerator(int start, int end) {
		super(start, end);
	}

	public AstGenerator(long sourceLevel, int start, int end) {
		super(start, end);
		this.sourceLevel = sourceLevel;
	}

	public AstGenerator(long pos) {
		super((int)(pos>>>32), (int)pos);
	}

	public AstGenerator(ASTNode node) {
		super(0,0);
		retargetFrom(node);
	}

	public void setPositions(Expression expression) {
		expression.sourceStart = this.sourceStart;
		expression.sourceEnd   = this.sourceEnd;
	}

	public LocalDeclaration localVariable (char[] name, char[] type, Expression init) {
		return localVariable(name, new SingleTypeReference(type, this.pos), init);
	}

	public LocalDeclaration localVariable (char[] name, TypeBinding type, Expression init) {
		TypeReference typeReference = typeReference(type);
		typeReference.sourceStart = this.sourceStart;
		typeReference.sourceEnd   = this.sourceEnd;

		return localVariable(name, typeReference, init);
	}

	public LocalDeclaration localBaseVariable (char[] name, TypeBinding type, Expression init) {
		TypeReference typeReference = baseclassReference(type);
		typeReference.sourceStart = this.sourceStart;
		typeReference.sourceEnd   = this.sourceEnd;

		return localVariable(name, typeReference, init);
	}

	public LocalDeclaration localVariable(char[] name, TypeReference typeReference, Expression init) {
		LocalDeclaration variable = new LocalDeclaration(name, this.sourceStart, this.sourceEnd);
		variable.initialization = init;
		variable.declarationSourceStart = this.sourceStart;
		variable.declarationSourceEnd   = this.sourceEnd;
		variable.type = typeReference;
		variable.isGenerated = true;
		return variable;
	}

	public LocalDeclaration localVariable(char[] name, TypeReference typeReference, int modifiers, Expression init) {
		LocalDeclaration variable = new LocalDeclaration(name, this.sourceStart, this.sourceEnd);
		variable.initialization = init;
		variable.declarationSourceStart = this.sourceStart;
		variable.declarationSourceEnd   = this.sourceEnd;
		variable.type = typeReference;
		variable.modifiers = modifiers;
		variable.isGenerated = true;
		return variable;
	}

	public SingleNameReference singleNameReference(char[] name) {
		SingleNameReference ref = new SingleNameReference(name, this.pos);
		ref.isGenerated = true;
		return ref;
	}
	public QualifiedNameReference qualifiedNameReference(char[][] tokens) {
		long[] poss = new long[tokens.length];
		Arrays.fill(poss, this.pos);
		return new QualifiedNameReference(tokens, poss, this.sourceStart, this.sourceEnd);
	}
	public QualifiedNameReference qualifiedBaseNameReference(char[][] tokens) {
		QualifiedNameReference ref = qualifiedNameReference(tokens);
		ref.baseclassDecapsulation = DecapsulationState.REPORTED;
		return ref;
	}
	public QualifiedNameReference qualifiedNameReference(ReferenceBinding type) {
		char[][] tokens = CharOperation.splitOn('.', type.readableName());
		return qualifiedNameReference(tokens);
	}
	/**
	 * Qualified reference to a static field.
	 * @param field a static field
	 * @return a resolved qualified reference to the given field.
	 */
	public QualifiedNameReference qualifiedNameReference(FieldBinding field) {
		char[][] className = TypeAnalyzer.compoundNameOfReferenceType(field.declaringClass, true, false);
		int len = className.length;
		char[][] qualifiedName = new char[len+1][];
		System.arraycopy(className, 0, qualifiedName, 0, len);
		qualifiedName[len] = field.name;
		QualifiedNameReference qualifiedNameReference = qualifiedNameReference(qualifiedName);
		qualifiedNameReference.binding = field;
		qualifiedNameReference.actualReceiverType = field.declaringClass;
		qualifiedNameReference.resolvedType = field.type;
		qualifiedNameReference.bits &= ~ASTNode.RestrictiveFlagMASK;
		qualifiedNameReference.bits |= Binding.FIELD;
		qualifiedNameReference.constant = Constant.NotAConstant;
		return qualifiedNameReference;
	}
	public NameReference nameReference (ReferenceBinding type) {
	    char[] typeName = "void".toCharArray(); //$NON-NLS-1$
	    char [][] qname = TypeAnalyzer.compoundNameOfReferenceType(type, true, true);
	    char[] sname;
	    if ((qname != null) && (qname.length==1)) {
	        sname = qname[0]; // use this
	        qname = null;     // not this
	    } else {
	        sname = typeName;
	    }

	    NameReference nr = null;
	    if (qname == null)
	        nr = singleNameReference(sname);
	    else {
	        long[] poss = new long[qname.length];
	        Arrays.fill(poss, 0);
	        nr = qualifiedNameReference(qname);
	    }
	    return nr;
	}
	public SingleNameReference baseNameReference(char[] name) {
		SingleNameReference result = singleNameReference(name);
		result.baseclassDecapsulation = DecapsulationState.REPORTED;
		return result;
	}
	public NameReference baseNameReference(ReferenceBinding type) {
		NameReference result = nameReference(type);
		result.baseclassDecapsulation = DecapsulationState.REPORTED;
		return result;
	}
	public TypeReference baseTypeReference(char[] name) {
		SingleTypeReference result = singleTypeReference(name);
		result.setBaseclassDecapsulation(DecapsulationState.REPORTED);
		return result;
	}

	public TypeReference baseTypeReference(ReferenceBinding type) {
		TypeReference result = typeReference(type);
		result.setBaseclassDecapsulation(DecapsulationState.REPORTED);
		return result;
	}

	public Expression thislikeNameReference(char[] name) {
		SingleNameReference result = singleNameReference(name);
		result.isThisLike = true;
		return result;
	}

	public ResultReference resultReference(
    		SingleNameReference ref, AbstractMethodMappingDeclaration mapping)
    {
    	return new ResultReference(ref, mapping);
    }

	public FieldReference fieldReference (Expression receiver, char[] name) {
		return fieldReference(receiver, name, DecapsulationState.NONE);
	}

	public FieldReference fieldReference (Expression receiver, char[] name, DecapsulationState baseDecapsulation) {
		FieldReference field = new FieldReference(name, this.pos);
		field.receiver = receiver;
		if (baseDecapsulation != DecapsulationState.NONE)
			field.setBaseclassDecapsulation(baseDecapsulation);
		return field;
	}

	/**
	 * Create a field reference from its elements.
	 * Perform necessary internal initializations without calling resolveType()
	 * @param receiver what's left of the dot
	 * @param field binding of the field being referenced
	 */
	public FieldReference resolvedFieldReference(
	        Expression   receiver,
	        FieldBinding field)
	{
	    FieldReference fieldRef = new FieldReference(field.name, (((long)this.sourceStart)<<32) + this.sourceEnd);
	    fieldRef.receiver = receiver;
	    fieldRef.actualReceiverType = receiver.resolvedType;
	    fieldRef.resolvedType = field.type;
	    fieldRef.binding = field; // codegenBinding will be set from analyseCode->manageSyntheticAccessIfNecessary
	    fieldRef.constant = Constant.NotAConstant;
	    return fieldRef;
	}

	public SingleTypeReference singleTypeReference(TypeBinding type) {
		return new SingleTypeReference(type.sourceName(), this.pos);
	}

	public SingleTypeReference singleTypeReference(char[] name) {
		return new SingleTypeReference(name, this.pos);
	}

	public ArrayTypeReference arrayTypeReference(char[] name, int dims) {
		return new ArrayTypeReference(name, dims, this.pos);
	}

	public NullLiteral nullLiteral() {
		NullLiteral result = new NullLiteral(this.sourceStart, this.sourceEnd);
		result.constant = Constant.NotAConstant;
		return result;
	}
	public Expression nullCheck(Expression value) {
		return internalNullCheck(value, OperatorIds.EQUAL_EQUAL);
	}
	public Expression nonNullCheck(Expression value) {
		return internalNullCheck(value, OperatorIds.NOT_EQUAL);
	}
	Expression internalNullCheck(Expression value, int operator) {
		EqualExpression result = new EqualExpression(
				value,
				nullLiteral(),
				operator) {
			protected void checkVariableComparison(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, FlowInfo initsWhenTrue, FlowInfo initsWhenFalse, LocalVariableBinding local, int nullStatus, Expression reference) {
				// similar to super version: do mark flowInfo but avoid reporting any problems
				switch (nullStatus) {
					case FlowInfo.NULL :
						if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
							initsWhenTrue.markAsComparedEqualToNull(local); // from thereon it is set
							initsWhenFalse.markAsComparedEqualToNonNull(local); // from thereon it is set
						} else {
							initsWhenTrue.markAsComparedEqualToNonNull(local); // from thereon it is set
							initsWhenFalse.markAsComparedEqualToNull(local); // from thereon it is set
						}
						break;
					case FlowInfo.NON_NULL :
						if (((this.bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
							initsWhenTrue.markAsComparedEqualToNonNull(local); // from thereon it is set
						}
						break;
				}
			}
		};
		result.sourceStart = this.sourceStart;
		result.sourceEnd = this.sourceEnd;
		result.constant = Constant.NotAConstant;
		result.isGenerated = true;
		return result;
	}
	public IntLiteral intLiteral(int val) {
		IntLiteral result = IntLiteral.buildIntLiteral(String.valueOf(val).toCharArray(), this.sourceStart, this.sourceEnd);
		return result;
	}
	public TeamModel.UpdatableIntLiteral updatableIntLiteral(int val) {
		return new TeamModel.UpdatableIntLiteral(val, this.sourceStart, this.sourceEnd);
	}
	public Literal booleanLiteral(boolean val) {
		MagicLiteral result = val
			? new TrueLiteral(this.sourceStart, this.sourceEnd)
			: new FalseLiteral(this.sourceStart, this.sourceEnd);
		result.isGenerated = true;
		return result;
	}
	public StringLiteral stringLiteral(char[] cs) {
		return new StringLiteral(cs, this.sourceStart, this.sourceEnd, /*lineNumber*/0);
	}
	/** Create a verbatim class literal access (no need to replace by _OT$getClass$R()).*/
	public Expression classLiteralAccess(TypeReference reference) {
		return new ClassLiteralAccess(this.sourceEnd, reference, true);
	}

	/**
	 * Note: only use this if you are positively sure that compoundName contains no '$'!
	 */
	public QualifiedTypeReference qualifiedTypeReference(char[][] compoundName) {
		long[] poss = new long[compoundName.length];
		Arrays.fill(poss, this.pos);
		QualifiedTypeReference reference = new QualifiedTypeReference(compoundName, poss);
		reference.bits |= ASTNode.IgnoreRawTypeCheck;
		reference.isGenerated = true;
		return reference;
	}

	public QualifiedTypeReference qualifiedArrayTypeReference(char[][] compoundName, int dims) {
		long[] poss = new long[compoundName.length];
		Arrays.fill(poss, this.pos);
		ArrayQualifiedTypeReference reference = new ArrayQualifiedTypeReference(compoundName, dims, poss);
		reference.bits |= ASTNode.IgnoreRawTypeCheck;
		reference.isGenerated = true;
		return reference;
	}

	public SingleTypeReference parameterizedSingleTypeReference(char[] name, TypeReference[] typeParameters, int dimensions)
	{
		if (this.sourceLevel < ClassFileConstants.JDK1_5)
			return singleTypeReference(name);
		return new ParameterizedSingleTypeReference(name, typeParameters, 0, this.pos);
	}

	public QualifiedTypeReference parameterizedQualifiedTypeReference(
				char[][] compoundName,
				TypeReference[] lastTypeParameters)
	{
		TypeReference[][] typeParameters = new TypeReference[compoundName.length][];
		typeParameters[compoundName.length-1] = lastTypeParameters;
		return parameterizedQualifiedTypeReference(compoundName, typeParameters, 0);
	}
	public QualifiedTypeReference parameterizedQualifiedTypeReference(
				char[][] compoundName,
				TypeReference[][] typeParameters, int dims)
	{
		if (this.sourceLevel < ClassFileConstants.JDK1_5)
			return qualifiedTypeReference(compoundName);
		long[] poss = new long[compoundName.length];
		Arrays.fill(poss, this.pos);
		return new ParameterizedQualifiedTypeReference(compoundName, typeParameters, dims, poss);
	}

	/**
	 * This method is only used for non-role types (incl. type parameters).
	 * @param compoundName		  name of the main type
	 * @param lastTypeParameters  bindings representing type parameters
	 * @return a parameterized qualified type reference
	 */
	public QualifiedTypeReference parameterizedQualifiedTypeReference(
				char[][] compoundName,
				TypeBinding[] lastTypeParameters)
	{
		TypeReference[] parameterRefs = new TypeReference[lastTypeParameters.length];
		for(int i = 0; i < lastTypeParameters.length; i++) {
			parameterRefs[i] = typeReference(lastTypeParameters[i], true/*generic*/);
		}
		return parameterizedQualifiedTypeReference(compoundName, parameterRefs);
	}

	public TypeReference typeReference(TypeBinding type) {
		return typeReference(type, true);
	}
	public TypeReference typeReference(TypeBinding type, boolean makeGeneric) {
        int dims = 0;
        TypeBinding elementType = type;

        if (type instanceof ArrayBinding) {
            ArrayBinding ab = (ArrayBinding)type;
            dims = ab.dimensions;
            elementType = ab.leafComponentType;
        }
        TypeReference typeReference = createArrayTypeReference(elementType, dims, makeGeneric);
        typeReference.bits |= ASTNode.IgnoreRawTypeCheck;
        typeReference.isGenerated = true;
        typeReference.deprecationProblemId = 0;
        if (!type.isValidBinding())
        	typeReference.bits |= ASTNode.IsGeneratedWithProblem;
		return typeReference;
	}
	public TypeReference baseclassReference(TypeBinding type) {
		return baseclassReference(type, false);
	}
	public TypeReference baseclassReference(TypeBinding type, boolean erase) {
		erase &= type.isParameterizedType();
		if (erase)
			type = type.erasure();
		TypeReference result = typeReference(type);
		result.setBaseclassDecapsulation(DecapsulationState.REPORTED);
		TypeReference[] parameters = null;
		if (result instanceof ParameterizedSingleTypeReference)
			parameters = ((ParameterizedSingleTypeReference)result).typeArguments;
		else if (result instanceof ParameterizedQualifiedTypeReference) {
			TypeReference[][] allParams = ((ParameterizedQualifiedTypeReference)result).typeArguments;
			if (allParams != null)
				parameters = allParams[allParams.length-1]; // TODO for now only treat the last set of params
		}
		if (parameters != null)
			for (TypeReference parameter : parameters)
				parameter.setBaseclassDecapsulation(DecapsulationState.REPORTED);

		if (erase)
			result.bits |= ASTNode.IgnoreRawTypeCheck;
		return result;
	}
	public ParameterizedSingleTypeReference roleTypeReference(ITeamAnchor baseSideAnchor, ReferenceBinding roleType, int dims)
	{

		TypeAnchorReference anchorRef = typeAnchorReference(baseSideAnchor);
		TypeReference[] typeParameters;
		TypeBinding[] typeArguments = roleType.isParameterizedType() ? ((ParameterizedTypeBinding) roleType).arguments : null;
		if (typeArguments != null) {
			typeParameters = new TypeReference[typeArguments.length+1];
			typeParameters[0] = anchorRef;
			for (int i = 0; i < typeArguments.length; i++)
				typeParameters[i+1] = typeReference(typeArguments[i]);
		} else {
			typeParameters = new TypeReference[] { anchorRef };
		}
		ParameterizedSingleTypeReference result = new ParameterizedSingleTypeReference(
				roleType.internalName(),
				typeParameters,
				dims,
				this.pos);
		return result;
	}

    public TypeReference createArrayTypeReference(
            TypeBinding elementType,
            int dims)
    {
    	TypeReference typeReference = createArrayTypeReference(elementType, dims, true);
    	typeReference.deprecationProblemId = 0;
    	typeReference.bits |= ASTNode.IgnoreRawTypeCheck;
    	typeReference.isGenerated = true;
		return typeReference;
    }

    private TypeReference createArrayTypeReference(
            TypeBinding elementType,
            int dims,
            boolean makeGeneric)
    {
    	// check generics:
    	if (elementType.isTypeVariable()) {
    		TypeVariableBinding typeVariable = (TypeVariableBinding)elementType;
    		char[] variableName = typeVariable.sourceName();
			if (dims == 0)
				return new SingleTypeReference(variableName, this.pos);
			else
				return new ArrayTypeReference(variableName, dims, this.pos);
    	} else if (makeGeneric && elementType.isParameterizedType()) {
    		// this branch currently cannot handle references of this shape: Outer<T>.Inner
    		// should that be needed at some point the following variant might do:
/*
			ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)elementType;
			char[][] compoundName = paramType.compoundName;
			char[]tokenString = CharOperation.concatWith(compoundName, '$');
			compoundName = CharOperation.splitOn('$', tokenString);
			TypeReference[][] arguments = new TypeReference[compoundName.length][];
			int argPos = compoundName.length-1;
			boolean haveArguments = false;			
			do {
				TypeBinding[] argumentTypes = paramType.arguments;
				if (argumentTypes != null) {
					haveArguments = true;
					TypeReference[] currentArgs = new TypeReference[argumentTypes.length];
					arguments[argPos] = currentArgs;
					for (int i = 0; i < argumentTypes.length; i++) {
						currentArgs[i] = typeReference(argumentTypes[i]);
					}
				}
				ReferenceBinding enclosing = paramType.enclosingType();
				argPos--;
				if (enclosing instanceof ParameterizedTypeBinding)
					paramType = (ParameterizedTypeBinding) enclosing;
				else break;
			} while (argPos >= 0);
			if (haveArguments) {
				long[] poss = new long[compoundName.length];
				Arrays.fill(poss, this.pos);
				return new ParameterizedQualifiedTypeReference(compoundName, arguments, dims, poss);
			}
 */
			ParameterizedTypeBinding paramType = (ParameterizedTypeBinding)elementType;
			TypeBinding[] argumentTypes = paramType.arguments;
    		if (argumentTypes != null) {
    			char[][] compoundName = paramType.compoundName;
    			char[]tokenString = CharOperation.concatWith(compoundName, '$');
    			compoundName = CharOperation.splitOn('$', tokenString);
				TypeReference[][] arguments = new TypeReference[compoundName.length][];
				TypeReference[] lastArgs = new TypeReference[argumentTypes.length];
				arguments[compoundName.length-1] = lastArgs;
	    		for (int i = 0; i < argumentTypes.length; i++) {
					lastArgs[i] = typeReference(argumentTypes[i]);
				}
	    		long[] poss = new long[argumentTypes.length];
	    		Arrays.fill(poss, this.pos);
	    		return new ParameterizedQualifiedTypeReference(compoundName, arguments, dims, poss);
    		}
    	} else if (elementType.isWildcard()) {
    		WildcardBinding wildcard = (WildcardBinding)elementType;
    		Wildcard result = new Wildcard(wildcard.boundKind);
    		result.sourceStart = this.sourceStart;
    		result.sourceEnd   = this.sourceEnd;
    		if (wildcard.bound != null)
    			result.bound = typeReference(wildcard.bound);
    		// Note(SH): I don't see dims to be relevant here, OK?
    		return result;
    	}

    	// from this point: not generic:
        char[] typeName = "void".toCharArray(); //$NON-NLS-1$
        char [][] qname = null;
        TypeAnchorReference anchorRef = null;
        if (elementType instanceof BaseTypeBinding) {
        	typeName = ((BaseTypeBinding) elementType).simpleName;
        } else if (elementType instanceof ReferenceBinding) {
            ReferenceBinding referenceBinding = (ReferenceBinding)elementType;
			qname = TypeAnalyzer.compoundNameOfReferenceType(referenceBinding, true, false);
            if (   referenceBinding instanceof DependentTypeBinding
            	&& ((DependentTypeBinding)referenceBinding).hasExplicitAnchor())
            {
            	DependentTypeBinding depBind = (DependentTypeBinding)referenceBinding;
            	anchorRef = typeAnchorReference(depBind.getAnchor());
            	typeName = referenceBinding.internalName();
            	qname = null;
            }
        }
        char[] sname;
        if ((qname != null) && (qname.length==1)) {
            sname = qname[0]; // use this
            qname = null;     // not this
        } else {
            sname = typeName;
        }
        long[] poss = null;
        if (qname != null) {
        	poss = new long[qname.length];
        	Arrays.fill(poss, this.pos);
        }

        if (anchorRef == null) {
	        if (dims == 0) {
	            if (qname == null)
	           		return new SingleTypeReference(sname, this.pos);
	            else
	                return new QualifiedTypeReference(qname, poss);
	        } else {
	            if (qname == null)
	                return new ArrayTypeReference(sname, dims, this.pos);
	            else {
	                return new ArrayQualifiedTypeReference(qname, dims, poss);
	            }
	        }
        } else {
			TypeReference[] typeReferences = new TypeReference[]{anchorRef};
        	assert qname == null;
			return new ParameterizedSingleTypeReference(sname, typeReferences, dims, this.pos);
        }
    }

    public TypeAnchorReference typeAnchorReference(ITeamAnchor anchor) {
    	ReferenceBinding firstClass= null;
    	if (   (anchor instanceof FieldBinding)
    		&& CharOperation.equals(((FieldBinding)anchor).name, IOTConstants._OT_BASE))
    	{
    		Reference anchorRef;
    		if (this.replaceableBaseAnchor == anchor) {
				anchorRef = singleNameReference(BASE);
			} else {
				firstClass = ((FieldBinding)anchor).declaringClass;
	    		if (this.replaceableEnclosingClass != null)
	    			firstClass = strengthenEnclosing(firstClass, this.replaceableEnclosingClass);
				anchorRef = new QualifiedBaseReference(typeReference(firstClass), this.sourceStart, this.sourceEnd);
			}
    		return new TypeAnchorReference(anchorRef, this.sourceStart);
    	} else {
    		firstClass= anchor.getFirstDeclaringClass();
    	}
    	Reference nameRef = null;
    	ITeamAnchor[] path = anchor.getBestNamePath();
    	if (firstClass != null) {
    		if (this.replaceableEnclosingClass != null)
    			firstClass = strengthenEnclosing(firstClass, this.replaceableEnclosingClass);
	        nameRef = qualifiedThisReference(firstClass);
	    	for (int i=0; i<path.length; i++)
	    		if (!(path[i] instanceof TThisBinding))
	    			nameRef = fieldReference(nameRef, path[i].internalName());
    	} else {
    		if (anchor instanceof TThisBinding)
    			nameRef = qualifiedThisReference(anchor.getFirstDeclaringClass());
    		else
    			nameRef = qualifiedNameReference(anchor.tokens());
    	}
    	TypeAnchorReference anchorRef = new TypeAnchorReference(nameRef, this.sourceStart);
    	return anchorRef;
    }

    private ReferenceBinding strengthenEnclosing(ReferenceBinding currentType, ReferenceBinding strongEnclosing) {
    	while (strongEnclosing != null) {
			if (TypeBinding.equalsEquals(strongEnclosing, currentType) || TypeBinding.equalsEquals(strongEnclosing.superclass(), currentType))
				return strongEnclosing;
			ReferenceBinding currentOuter = currentType.enclosingType();
			if (currentOuter != null) {
				ReferenceBinding strongOuter = strengthenEnclosing(currentOuter, strongEnclosing);
				if (strongOuter != null)
					return strongOuter.getMemberType(currentType.internalName());
			}
			strongEnclosing = strongEnclosing.enclosingType();
    	}
		return null;
	}

	public TypeParameter typeParameter(TypeVariableBinding typeVarBinding) {
    	TypeParameter result = unboundedTypeParameter(typeVarBinding.sourceName);
    	result.type = typeReference(typeVarBinding.superclass);
    	ReferenceBinding[] superInterfaces = typeVarBinding.superInterfaces;
		if (superInterfaces != Binding.NO_SUPERINTERFACES && superInterfaces.length > 0)
		{
    		result.type = typeReference(superInterfaces[0]);
    		if (superInterfaces.length > 1) {
	    		result.bounds = new TypeReference[superInterfaces.length-1];
	    		for (int i = 0; i < superInterfaces.length-1; i++) {
					result.bounds[i] = typeReference(superInterfaces[i-1]);
					result.bounds[i].bits |= ASTNode.IsSuperType;
				}
    		}
    	}
		return result;
    }

    public TypeParameter baseBoundedTypeParameter (char[] name, ReferenceBinding roleType) {
    	TypeParameter result = unboundedTypeParameter(name);
    	result.type = typeReference(roleType);
    	result.type.bits |= ASTNode.IsRoleType;
    	return result;
    }

	public TypeParameter unboundedTypeParameter(char[] name) {
		TypeParameter result = new TypeParameter();
		result.name = name;
    	result.sourceStart = this.sourceStart;
    	result.sourceEnd   = this.sourceEnd;
    	result.declarationSourceStart = this.sourceStart;
    	result.declarationSourceEnd   = this.sourceEnd;
		return result;
	}

	public Wildcard wildcard(int kind) {
		Wildcard result = new Wildcard(kind);
		result.sourceStart = this.sourceStart;
		result.sourceEnd   = this.sourceEnd;
		return result;
	}
	
	public LiftingTypeReference liftingTypeReference(TypeReference baseReference, TypeReference roleReference, 
													 char[] roleToken, char[][] baseTokens) 
	{
		LiftingTypeReference result = new LiftingTypeReference();
		result.sourceStart   = this.sourceStart;
		result.sourceEnd     = this.sourceEnd;
		result.baseReference = baseReference;
		result.roleReference = roleReference;
		int len = baseTokens.length;
		result.baseTokens 	 = new char[len][];
		System.arraycopy(baseTokens, 0, result.baseTokens, 0, len);
		result.roleToken     = roleToken;
		return result;
	}
	
	public FieldDeclaration field(int modifiers, TypeReference typeRef, char[] name, Expression init)
	{
		FieldDeclaration field = new FieldDeclaration(name, this.sourceStart, this.sourceEnd);
	    field.declarationSourceStart = this.sourceStart;
	    field.declarationSourceEnd   = this.sourceEnd;
	    field.declarationEnd         = this.sourceEnd;
	    field.initialization = init;
	    field.type = typeRef;
	    field.modifiers = modifiers;
	    field.isGenerated = true;
		return field;
	}

	public ConstructorDeclaration constructor(CompilationResult compilationResult,
			int modifiers, char[] selector, Argument[] arguments)
	{
		ConstructorDeclaration newMethod = new ConstructorDeclaration(compilationResult);
		setMethodPositions(newMethod);
		newMethod.isGenerated = true;

		newMethod.modifiers = modifiers;
		newMethod.selector = selector;
		newMethod.arguments = arguments;
		return newMethod;
	}

	public MethodDeclaration method(CompilationResult compilationResult,
			int modifiers, TypeBinding returnType, char[] selector, Argument[] arguments)
	{
		TypeReference returnTypeRef = null;
		if (returnType != null)
			returnTypeRef = typeReference(returnType);
		return method(compilationResult,
				modifiers,
				returnTypeRef,
				selector,
				arguments);
	}

	public MethodDeclaration method(CompilationResult compilationResult,
			int modifiers, TypeReference returnTypeRef, char[] selector, Argument[] arguments)
	{
		MethodDeclaration newMethod = new MethodDeclaration(compilationResult);
		setMethodPositions(newMethod);
		newMethod.isGenerated = true;

		newMethod.modifiers  = modifiers;
		newMethod.returnType = returnTypeRef;
		newMethod.selector   = selector;
		newMethod.arguments  = arguments;
		return newMethod;
	}

	public MethodDeclaration method(CompilationResult compilationResult,
			int modifiers, TypeReference returnType, char[] selector, Argument[] arguments, Statement[] statements)
	{
		MethodDeclaration newMethod = method(compilationResult, modifiers, returnType, selector, arguments);
		newMethod.statements = statements;
		newMethod.hasParsedStatements = true;
		return newMethod;
	}

	/** If typeBinding has a free type parameter, we need to push this param out as a method parameter. */
	public void maybeAddTypeParametersToMethod(ReferenceBinding typeBinding, MethodDeclaration methodDecl) 
	{
		if (typeBinding.isParameterizedType()) {
			TypeBinding[] arguments = ((ParameterizedTypeBinding)typeBinding).arguments;
			TypeParameter[] methodTypeParams = new TypeParameter[arguments.length];
			for (int i = 0; i < methodTypeParams.length; i++)
				// FIXME(SH): what if arguments are not TypeVariableBinding (how could that happen?)
				methodTypeParams[i] = typeParameter((TypeVariableBinding)arguments[i]);
			methodDecl.typeParameters = methodTypeParams;
		}
	}

	public Argument argument(char[] name, TypeReference type) {
		Argument result = new Argument(name, this.pos, type, 0 /*modifiers*/);
		result.declarationSourceStart = this.sourceStart;
		return result;
	}

	public Argument argument(char[] name, TypeReference type, int modifiers) {
		Argument result = new Argument(name, this.pos, type, modifiers);
		result.declarationSourceStart = this.sourceStart;
		return result;
	}

	public MessageSend messageSend(Expression receiver, char[] selector, Expression[] parameters) {
		MessageSend messageSend =  new MessageSend();
		messageSend.isGenerated = true;
		messageSend.sourceStart = this.sourceStart;
		messageSend.sourceEnd   = this.sourceEnd;
		messageSend.statementEnd = this.sourceEnd;
		messageSend.nameSourcePosition = this.pos;
		messageSend.receiver = receiver;
		messageSend.selector = selector;
		messageSend.arguments = parameters;
		return messageSend;
	}
	public MessageSend tsuperMessageSend(Expression receiver, char[] selector, Expression[] parameters) {
		TSuperMessageSend messageSend =  new TSuperMessageSend();
		messageSend.isGenerated = true;
		messageSend.sourceStart = this.sourceStart;
		messageSend.sourceEnd   = this.sourceEnd;
		messageSend.statementEnd = this.sourceEnd;
		messageSend.nameSourcePosition = this.pos;
		messageSend.receiver = receiver;
		messageSend.tsuperReference = new TsuperReference(this.sourceStart, this.sourceEnd);
		messageSend.selector = selector;
		messageSend.arguments = parameters;
		return messageSend;
	}

	// function type for the next method:
	public static interface IRunInScope { void run(BlockScope scope); }

	/**
	 * Create a message send that has a custom resolve method (see inside for details).
	 */
	public MessageSend messageSendWithResolveHook(Expression receiver, final MethodBinding method, Expression[] parameters, final IRunInScope hook) {
		MessageSend messageSend = new MessageSend() {
			@Override
			public TypeBinding resolveType(BlockScope scope) {
				this.constant = Constant.NotAConstant;
				// arguments always need resolving:
				if (this.arguments != null) {
					int length = this.arguments.length;
					for (int i = 0; i < length; i++){
						Expression argument = this.arguments[i];
						if (argument.resolvedType == null)
							argument.resolveType(scope);
					}
				}
				// skip the receiver unless its again a hooked message send:
				if (this.receiver.getClass() == this.getClass())
					this.receiver.resolveType(scope);
				
				this.binding = method;
				this.resolvedType = method.returnType;

				// the main payload:
				hook.run(scope);
				
				this.actualReceiverType = this.binding.declaringClass;
				return this.resolvedType;
			}
		};
		messageSend.isGenerated = true;
		messageSend.sourceStart = this.sourceStart;
		messageSend.sourceEnd   = this.sourceEnd;
		messageSend.statementEnd = this.sourceEnd;
		messageSend.nameSourcePosition = this.pos;
		messageSend.receiver = receiver;
		messageSend.selector = method.selector;
		messageSend.arguments = parameters;
		return messageSend;
	}

	public MessageSend messageSend(Expression receiver, char[] selector, Expression[] parameters, final TypeBinding resolvedReturn) {
		MessageSend messageSend = new MessageSend() {
			@Override
			public TypeBinding resolveType(BlockScope scope) {
				super.resolveType(scope);
				return this.resolvedType = resolvedReturn;
			}
		};
		messageSend.isGenerated = true;
		messageSend.sourceStart = this.sourceStart;
		messageSend.sourceEnd   = this.sourceEnd;
		messageSend.statementEnd = this.sourceEnd;
		messageSend.nameSourcePosition = this.pos;
		messageSend.receiver = receiver;
		messageSend.selector = selector;
		messageSend.arguments = parameters;
		return messageSend;
	}

	/** Create a message send to a method that will be created by the otre. */
	public MessageSend fakeMessageSend(Expression receiver, char[] selector, Expression[] parameters, 
									   final ReferenceBinding receiverType, final TypeBinding resolvedReturn) 
	{
		MessageSend messageSend = new MessageSend() {
			@Override
			public TypeBinding resolveType(BlockScope scope) {
				ReferenceContext referenceContext = scope.referenceContext();
				CheckPoint cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
				super.resolveType(scope);
				referenceContext.compilationResult().rollBack(cp);
				this.binding = new MethodBinding(ClassFileConstants.AccStatic|ClassFileConstants.AccPublic, this.selector, 
												 resolvedReturn, this.binding.parameters, Binding.NO_EXCEPTIONS, receiverType);
				return this.resolvedType = resolvedReturn;
			}
		};
		messageSend.isGenerated = true;
		messageSend.sourceStart = this.sourceStart;
		messageSend.sourceEnd   = this.sourceEnd;
		messageSend.statementEnd = this.sourceEnd;
		messageSend.nameSourcePosition = this.pos;
		messageSend.receiver = receiver;
		messageSend.selector = selector;
		messageSend.arguments = parameters;
		return messageSend;
	}

	public AllocationExpression allocation(TypeReference typeRef, Expression[] arguments) {
		AllocationExpression result = new AllocationExpression();
		result.sourceStart = this.sourceStart;
		result.sourceEnd   = this.sourceEnd;
		result.statementEnd = this.sourceEnd;
		result.type = typeRef;
		result.arguments = arguments;
		result.isGenerated = true;
		return result;
	}

	public ArrayAllocationExpression arrayAllocation(TypeReference typeRef, int dims, Expression[] arguments)
	{
    	ArrayAllocationExpression allocation = new ArrayAllocationExpression();
    	allocation.type = typeRef;
    	if (arguments == null) {
			allocation.dimensions = new Expression[]{intLiteral(dims)};
		} else {
			allocation.dimensions = new Expression[dims];
			allocation.initializer = new ArrayInitializer();
			allocation.initializer.expressions = arguments;
		}
    	return setPos(allocation);
	}
	public QualifiedAllocationExpression qualifiedAllocation(
			Expression enclosingInstance, SingleTypeReference typeRef, Expression[] arguments)
	{
		QualifiedAllocationExpression result = new QualifiedAllocationExpression();
		result.enclosingInstance = enclosingInstance;
		result.type              = typeRef; // Note: QualifiedAllocationExpression for roles really needs a SingleTypeReference.
		result.arguments         = arguments;
		return setPos(result);
	}

	public QualifiedAllocationExpression anonymousAllocation(
			TypeReference superType, Expression[] arguments, TypeDeclaration anonymousType) 
	{
		QualifiedAllocationExpression result = new QualifiedAllocationExpression();
		result.enclosingInstance = null;
		result.type              = superType;
		result.arguments         = arguments;
		result.anonymousType = anonymousType;
		anonymousType.allocation = result;
		return setPos(result);		
	}

	public TypeDeclaration anonymousType(CompilationResult compilationResult) {
		TypeDeclaration anonymousType = new TypeDeclaration(compilationResult);
		anonymousType.sourceStart = this.sourceStart;
		anonymousType.sourceEnd   = this.sourceEnd;
		anonymousType.declarationSourceStart = this.sourceStart;
		anonymousType.declarationSourceEnd   = this.sourceEnd;
		anonymousType.bodyStart  = this.sourceStart;
		anonymousType.bodyEnd    = this.sourceEnd;
		anonymousType.name = CharOperation.NO_CHAR;
		anonymousType.bits |= (ASTNode.IsAnonymousType|ASTNode.IsLocalType);
		anonymousType.isGenerated = true;
		return anonymousType;
	}
	
	public ExplicitConstructorCall explicitConstructorCall(int accessMode) {
		ExplicitConstructorCall call = new ExplicitConstructorCall(accessMode);
		call.sourceStart = this.sourceStart;
		call.sourceEnd = this.sourceEnd;
		return call;
	}

	public CastExpression castExpression(Expression expression, TypeReference type, int kind)
	{
        CastExpression cast = new CastExpression(
                expression,
                type,
				kind);
        cast.constant = Constant.NotAConstant;
        return setPos(cast);
	}
	/**
	 * Create a cast expression from its elements (reference types only).
	 * Perform necessary internal initializations without calling resolveType()
	 * @param expr
	 * @param type
	 * @param kind one of CastExpression.{DO_WRAP,NEED_CLASS,RAW}
	 *
	 * @return null is used to signal no cast created (because expr is 'this').
	 */
	public CastExpression resolvedCastExpression(
							        Expression   expr,
							        TypeBinding  type,
									int kind)
	{
	    if (expr instanceof ThisReference)
	        return null; // never useful to cast 'this'!
	    TypeReference typeRef = typeReference(type);
	    typeRef.sourceStart = this.sourceStart;
	    typeRef.sourceEnd   = this.sourceEnd;
	    typeRef.resolvedType = type;

	    CastExpression cast = new CastExpression(expr, typeRef, kind);
		typeRef.resolvedType =
		    cast.resolvedType = type;
	    cast.constant = Constant.NotAConstant;
	    cast.tagAsNeedCheckCast();
	    return setPos(cast);
	}

	public InstanceOfExpression instanceOfExpression(Expression expression, TypeReference type) {
		InstanceOfExpression result = new InstanceOfExpression(expression, type);
		return result;
	}
	public ReturnStatement returnStatement(Expression expression) {
		return new ReturnStatement(expression, this.sourceStart, this.sourceEnd);
	}
	public ReturnStatement returnStatement(Expression expression, boolean synthetic) {
		ReturnStatement returnStatement = new ReturnStatement(expression, this.sourceStart, this.sourceEnd);
		if (synthetic)
			returnStatement.isGenerated = true;
		return returnStatement;
	}
	public IfStatement ifStatement(Expression condition, Statement thenStatement) {
		return new IfStatement(condition, thenStatement, this.sourceStart, this.sourceEnd);
	}
	public IfStatement ifStatement(Expression condition, Statement thenStatement, Statement elseStatement)
	{
		IfStatement result = new IfStatement(condition, thenStatement, this.sourceStart, this.sourceEnd);
		result.elseStatement = elseStatement;
		return result;
	}
	public IfStatement ifStatement(Expression condition, Block thenBlock, Block elseBlock) {
		IfStatement result = new IfStatement(condition, thenBlock, this.sourceStart, this.sourceEnd);
		result.elseStatement = elseBlock;
		return result;
	}
	/**
	 * Assemble an ifstatement with negated condition, that hides the branch instruction from the debugger,
	 * even when stepping into the condition. 
	 */
	public IfStatement stealthIfNotStatement(final Expression condition, Statement thenStatement) {
		// mark step-over after the condition:
		Expression recordingCondition = new Expression() {
			public StringBuffer printExpression(int indent, StringBuffer output) { return condition.print(indent, output); }
			public TypeBinding resolveType(BlockScope scope) {
				return condition.resolveType(scope); 
			}
			public void computeConversion(Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType) {
				condition.computeConversion(scope, runtimeType, compileTimeType);
				this.constant = condition.constant;
				this.bits = condition.bits;
			}
			public Constant optimizedBooleanConstant() { 
				this.constant = condition.optimizedBooleanConstant();
				this.bits = condition.bits;
				return this.constant; 
			}
			public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
				return condition.analyseCode(currentScope, flowContext, flowInfo, valueRequired);
			}
			public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
				return condition.analyseCode(currentScope, flowContext, flowInfo);
			}
			public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
				condition.generateCode(currentScope, codeStream, valueRequired);
				// payload:
				codeStream.pcToSourceMap[codeStream.pcToSourceMapSize++] = codeStream.position;
				codeStream.pcToSourceMap[codeStream.pcToSourceMapSize++] = STEP_OVER_LINENUMBER;
			}
		};
		return new IfStatement(
				new UnaryExpression( // NOT
						recordingCondition,
						OperatorIds.NOT),
				thenStatement, 
				this.sourceStart, this.sourceEnd);
	}
	public ThrowStatement throwStatement(Expression exception) {
		return new ThrowStatement(exception, this.sourceStart, this.sourceEnd);
	}
	public CaseStatement caseStatement(Expression constExpr) {
		return new CaseStatement(constExpr, this.sourceStart, this.sourceEnd);
	}
	public BreakStatement breakStatement() {
		return new BreakStatement(null, this.sourceStart, this.sourceEnd);
	}
	public Assignment assignment(Reference lhs, Expression rhs) {
		Assignment ass = new Assignment(lhs, rhs, this.sourceEnd);
		ass.statementEnd = this.sourceEnd;
		return ass;
	}

	public Block block2(Statement stmt1, Statement stmt2) {
		int explicitDeclarations = 0;
		if (stmt1 instanceof LocalDeclaration) explicitDeclarations++;
		if (stmt2 instanceof LocalDeclaration) explicitDeclarations++;
		Block result = new Block(explicitDeclarations);
		result.sourceStart = this.sourceStart;
		result.sourceEnd = this.sourceEnd;
		result.statements = new Statement[] { stmt1, stmt2 };
		return result;
	}

	public Block block(Statement[] stmts) {
		int explicitDeclarations = 0;
		if (stmts != null)
			for (Statement statement : stmts)
				if (statement instanceof LocalDeclaration)
					explicitDeclarations++;

		Block result = new Block(explicitDeclarations);
		result.sourceStart = this.sourceStart;
		result.sourceEnd = this.sourceEnd;
		result.statements = stmts;
		return result;
	}

	public SynchronizedStatement synchronizedStatement(Expression expr, Statement[] stmts)
	{
		return new SynchronizedStatement(expr, block(stmts), this.sourceStart, this.sourceEnd);
	}

	public Statement statement(List<Statement> stmtList) {
		if (stmtList.isEmpty())
			return emptyStatement();
		if (stmtList.size() == 1)
			return stmtList.get(0);
		Statement[] stmtArray = new Statement[stmtList.size()];
		stmtList.toArray(stmtArray);
		return block(stmtArray);
	}

	public ForeachStatement foreach(
								LocalDeclaration variable,
								Expression       collection,
								Statement        body)
	{
		ForeachStatement foreach = new ForeachStatement(variable, this.sourceStart);
		foreach.collection = collection;
		foreach.action = body;
		if (this.sourceLevel < ClassFileConstants.JDK1_5)
			foreach.markRaw();
		return foreach;
	}

	public TryStatement tryFinally(Statement[] tryStatements, Statement[] finallyStatements) {
		TryStatement stat = new TryStatement();
		stat.sourceStart = this.sourceStart;
		stat.sourceEnd   = this.sourceEnd;
		stat.tryBlock = block(tryStatements);
		stat.finallyBlock = block(finallyStatements);
		if (finallyStatements != null) {
			int len = finallyStatements.length;
			if (len > 0) {
				stat.finallyBlock.sourceStart = finallyStatements[0].sourceStart;
				stat.finallyBlock.sourceEnd   = finallyStatements[len-1].sourceEnd;
			}
		}
		return stat;
	}

	public TryStatement tryCatch(Statement[] tryStatements, Argument exceptionArgument, Statement[] catchStatements) {
		TryStatement stat = new TryStatement();
		stat.sourceStart = this.sourceStart;
		stat.sourceEnd   = this.sourceEnd;
		stat.tryBlock = block(tryStatements);
		stat.catchArguments = new Argument[] { exceptionArgument };
		stat.catchBlocks = new Block[] { block(catchStatements) };
		return stat;
	}

	public TryStatement tryCatch(Statement[] tryStatements, Argument[] exceptionArguments, Statement[][] catchStatementss) {
		TryStatement stat = new TryStatement();
		stat.sourceStart = this.sourceStart;
		stat.sourceEnd   = this.sourceEnd;
		stat.tryBlock = block(tryStatements);
		stat.catchArguments = exceptionArguments;
		stat.catchBlocks = new Block[catchStatementss.length];
		for(int i=0; i<catchStatementss.length; i++)
			stat.catchBlocks[i] = block(catchStatementss[i]);
		return stat;
	}

	/** Generate a full try-catch-finally statement. */
	public TryStatement tryStatement(Statement[] tryStatements,
									 Argument[] exceptionArguments, Statement[][] catchStatementss,
									 Statement[] finallyStatements)
	{
		TryStatement stat = new TryStatement();
		stat.sourceStart = this.sourceStart;
		stat.sourceEnd   = this.sourceEnd;
		stat.tryBlock = block(tryStatements);
		stat.catchArguments = exceptionArguments;
		stat.catchBlocks = new Block[catchStatementss.length];
		for(int i=0; i<catchStatementss.length; i++)
			stat.catchBlocks[i] = block(catchStatementss[i]);
		stat.finallyBlock = block(finallyStatements);
		if (finallyStatements != null) {
			int len = finallyStatements.length;
			if (len > 0) {
				stat.finallyBlock.sourceStart = finallyStatements[0].sourceStart;
				stat.finallyBlock.sourceEnd   = finallyStatements[len-1].sourceEnd;
			}
		}
		return stat;
	}

	/**
	 * Create a lifting of an expression to an expectedType if expectedType is in deed
	 * a role type different from the type of expression (deferred decision).
	 * Assumes that the role must be within the current scope so that a qualified this
	 * suffices as a receiver for the lift call.
	 *
	 * @param receiver     receiver for the lift call (referring to some team instance)
	 * @param expression   the exression to be lifted
	 * @param expectedType the required role(?) type
	 * @param reversible   is reversibility of this operation requested (callin replace)?
	 * @return a PotentialLiftExpression or the original expression
	 */
	public Expression potentialLift(Expression  receiver,
								    Expression  expression,
								    TypeBinding expectedType,
								    boolean     reversible)
	{
		if (expectedType.leafComponentType().isBaseType())
			return expression; // cannot lift to simple
		ReferenceBinding expectedRef = (ReferenceBinding)expectedType.leafComponentType();
		ReferenceBinding teamBinding = expectedRef.enclosingType();
		if (teamBinding == null || !teamBinding.isTeam())
			return expression;
		if (receiver == null)
			receiver = new QualifiedThisReference(
					typeReference(teamBinding), this.sourceStart, this.sourceEnd);
		PotentialLiftExpression lifter = new PotentialLiftExpression(
				receiver,
				expression,
				expectedType);
		lifter.requireReverseOperation = reversible;
		return lifter;
	}

	public ThisReference thisReference() {
		return new ThisReference(this.sourceStart, this.sourceEnd);
	}


	public SuperReference superReference() {
		return new SuperReference(this.sourceStart, this.sourceEnd);
	}

	public ThisReference tsuperReference() {
		return new TsuperReference(this.sourceStart, this.sourceEnd);
	}

	/**
	 * @param binding prefix the "this" reference by this type.
	 * @return Outer.this where "Outer" is identified by binding
	 */
	public Reference qualifiedThisReference(ReferenceBinding binding) {
		return new QualifiedThisReference(
				typeReference(binding),
				this.sourceStart, this.sourceEnd);
	}
	/**
	 * @param reference prefix the "this" reference by this type.
	 * @return Outer.this where "Outer" is identified by reference
	 */
	public Expression qualifiedThisReference(TypeReference reference) {
		return new QualifiedThisReference(reference, this.sourceStart, this.sourceEnd);
	}
	public Statement emptyStatement() {
		return new EmptyStatement(this.sourceStart, this.sourceEnd) {
        			@Override public void resolve(BlockScope scope) { /* nop: no warning. */ } };
	}

	/**
	 * For debuggin purposes: create a println with given string as literal argument
	 * @return the statement (message send)
	 */
	public MessageSend println(String msg) {
		return messageSend(
				new QualifiedNameReference(
						new char[][]{"System".toCharArray(), "out".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
						new long[]{this.pos, this.pos},
						this.sourceStart, this.sourceEnd),
				"println".toCharArray(), //$NON-NLS-1$
				new Expression[] {stringLiteral(msg.toCharArray())}
		);
	}
	/** print a message plus the value of an expression. */
	public MessageSend println(String msg, Expression expr) {
		return messageSend(
				new QualifiedNameReference(
						new char[][]{"System".toCharArray(), "out".toCharArray()}, //$NON-NLS-1$ //$NON-NLS-2$
						new long[]{this.pos, this.pos},
						this.sourceStart, this.sourceEnd),
				"println".toCharArray(), //$NON-NLS-1$
				new Expression[] {
					new BinaryExpression(stringLiteral(msg.toCharArray()), expr, OperatorIds.PLUS)}
		);
	}

	/**
	 * A type reference for the cache of a given bound root role.
     * If source level >= 1.5 use a generic type for the cache!
     * Generic type respects different status for base/role types:
     * - base is resolved via base import scope and supports decapsulation.
	 */
	public QualifiedTypeReference getCacheTypeReference(Scope scope, RoleModel boundRootRole) {
		if ((scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5)) {
	    	ReferenceBinding baseTypeBinding = boundRootRole.getBaseTypeBinding();
	    	if (baseTypeBinding == null)
	    		baseTypeBinding = scope.getJavaLangObject();
			ReferenceBinding rootRoleBinding = boundRootRole.getInterfacePartBinding();

			ParameterizedQualifiedTypeReference fieldTypeRef;
			TypeReference[] typeArguments = new TypeReference[] {
				baseclassReference(baseTypeBinding, true/*erase*/),
				singleTypeReference(rootRoleBinding.sourceName())	
			};
			fieldTypeRef = (ParameterizedQualifiedTypeReference)parameterizedQualifiedTypeReference(
								WEAK_HASH_MAP,
								typeArguments);
			return fieldTypeRef;
	    } else {
	    	return new QualifiedTypeReference(WEAK_HASH_MAP, new long[]{this.pos,this.pos,this.pos});
	    }
	}

	/**
	 * @param resultExpr
	 * @return a message send for the boxed value.
	 */
	public MessageSend createBoxing(Expression resultExpr, BaseTypeBinding type)
	{
		char[][] boxedType = boxTypeName(type);
		return messageSend(qualifiedTypeReference(boxedType), TypeConstants.VALUEOF, new Expression[]{resultExpr});
	}

	public static char[][] boxTypeName(BaseTypeBinding type) {
		switch (type.id) {
		case TypeIds.T_byte: 	return TypeConstants.JAVA_LANG_BYTE;
		case TypeIds.T_boolean: return TypeConstants.JAVA_LANG_BOOLEAN;
		case TypeIds.T_int: 	return TypeConstants.JAVA_LANG_INTEGER;
		case TypeIds.T_short: 	return TypeConstants.JAVA_LANG_SHORT;
		case TypeIds.T_long : 	return TypeConstants.JAVA_LANG_LONG;
		case TypeIds.T_double: 	return TypeConstants.JAVA_LANG_DOUBLE;
		case TypeIds.T_float: 	return TypeConstants.JAVA_LANG_FLOAT;
		case TypeIds.T_char: 	return TypeConstants.JAVA_LANG_CHARACTER;
		default: throw new InternalCompilerError("trying to box non-primitive type"); //$NON-NLS-1$
		}
	}

	public Expression createUnboxing(Expression expression, BaseTypeBinding basicType) {
		char[][] boxedTypeName = boxTypeName(basicType);
		char[] selector = CharOperation.concat(basicType.sourceName(), "Value".toCharArray()); //$NON-NLS-1$
		return messageSend(
					castExpression(
							expression,
							qualifiedTypeReference(boxedTypeName),
							CastExpression.RAW),
					selector,
					null);
	}
	public Expression createCastOrUnboxing(Expression expression, TypeBinding expectedType, boolean baseAccess) {
		if (expectedType.isBaseType())
			return createUnboxing(expression, (BaseTypeBinding)expectedType);
		else if (baseAccess)
			return castExpression(expression, baseclassReference(expectedType), CastExpression.RAW);
		else
			return castExpression(expression, typeReference(expectedType), CastExpression.RAW);
	}
	public Expression createCastOrUnboxing(Expression expression, TypeBinding expectedType, Scope originalScope) {
		if (expectedType.isBaseType())
			return createUnboxing(expression, (BaseTypeBinding)expectedType);
		else
			return castExpression(expression, alienScopeTypeReference(typeReference(expectedType), originalScope), CastExpression.RAW);
	}
	
	// ========= Method Mappings: =========
	public CalloutMappingDeclaration calloutMappingDeclaration(
			CompilationResult compilationResult) {
		CalloutMappingDeclaration result = new CalloutMappingDeclaration(compilationResult);
		result.sourceStart = this.sourceStart;
		result.sourceEnd = this.sourceEnd;
		return result;
	}
	public MethodSpec methodSpec(char[] selector) {
		return new MethodSpec(selector, this.sourceStart, this.sourceEnd);
	}
	public FieldAccessSpec fieldAccessSpec(char[] fieldName, TypeBinding type, boolean isSetter) {
		return new FieldAccessSpec(fieldName, typeReference(type), this.pos,
								   isSetter?TerminalTokens.TokenNameset:TerminalTokens.TokenNameget);
	}

	// ========= DETAILED EXPRESSIONS ===========
	public ArrayReference arrayReference(Expression expression, int index) {
		return setPos(new ArrayReference(expression, intLiteral(index)));
	}

	public ArrayReference arrayReference(Expression expression, Expression indexExpression) {
		return setPos(new ArrayReference(expression, indexExpression));
	}

	public EqualExpression equalExpression (Expression left, Expression right, int operator)
	{
		return setPos(new EqualExpression(left, right, operator));
	}
	public <E extends Expression> E setPos(E e) {
		e.sourceStart = this.sourceStart;
		e.sourceEnd = this.sourceEnd;
		e.statementEnd = this.sourceEnd;
		return e;
	}
	// ========== ANNOTATION =============
	public SingleMemberAnnotation singleMemberAnnotation(char[][] compoundName, Expression memberValue) {
		SingleMemberAnnotation result = new SingleMemberAnnotation(qualifiedTypeReference(compoundName), this.sourceStart);
		result.sourceEnd = this.sourceEnd;
		result.declarationSourceEnd = this.sourceEnd;
		result.memberValue = memberValue;
		return result;
	}
	public SingleMemberAnnotation singleStringsMemberAnnotation(char[][] compoundName, char[][] memberValues) {
		SingleMemberAnnotation result = new SingleMemberAnnotation(qualifiedTypeReference(compoundName), this.sourceStart);
		result.sourceEnd = this.sourceEnd;
		result.declarationSourceEnd = this.sourceEnd;
		ArrayInitializer arrayInitializer = new ArrayInitializer();
		arrayInitializer.expressions = new Expression[memberValues.length];
		for (int i = 0; i < memberValues.length; i++)
			arrayInitializer.expressions[i] = stringLiteral(memberValues[i]);
		result.memberValue = arrayInitializer;
		return result;
	}
	
	public NormalAnnotation normalAnnotation(char[][] compoundName, char[][] names, Expression[] values) {
		assert names.length == values.length : "names and values must have same length"; //$NON-NLS-1$
		NormalAnnotation result = new NormalAnnotation(qualifiedTypeReference(compoundName), this.sourceStart);
		result.sourceEnd = this.sourceEnd;
		result.declarationSourceEnd = this.sourceEnd;
		MemberValuePair[] pairs = new MemberValuePair[names.length];
		for (int i = 0; i < names.length; i++) {
			pairs[i] = new MemberValuePair(names[i], this.sourceStart, this.sourceEnd, values[i]);
		}
		result.memberValuePairs = pairs;
		return result;
	}
	
	public MarkerAnnotation markerAnnotation(char[][] compoundName) {
		return new MarkerAnnotation(qualifiedTypeReference(compoundName), this.sourceStart);
	}

	public void addNonNullAnnotation(Argument argument, LookupEnvironment environment) {
		addNullAnnotation(argument, environment, true);
	}

	public void addNullAnnotation(Argument argument, LookupEnvironment environment, boolean nonNull) {
		CompilerOptions compilerOptions = environment.globalOptions;
		if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
			char[][] annotationName = nonNull ? environment.getNonNullAnnotationName() : environment.getNullableAnnotationName();
			if (compilerOptions.sourceLevel < ClassFileConstants.JDK1_8) {
				argument.annotations = new Annotation[]{ markerAnnotation(annotationName) };
			} else {
				int levels = argument.type.getAnnotatableLevels();
				argument.type.annotations = new Annotation[levels][];
				argument.type.annotations[levels-1] = new Annotation[] { markerAnnotation(annotationName) };
			}
		}
	}

	/**
	 * Wrap the baseclass reference from a tsuper role to a new type reference
	 * yet using the original scope for resolving.
	 * Also used for type references in callin wrappers
	 */
	public TypeReference alienScopeTypeReference(TypeReference original, Scope origScope)
	{
		if (original instanceof IAlienScopeTypeReference)
			origScope = ((IAlienScopeTypeReference)original).getAlienScope();
//		if (origScope.parent.kind == Scope.CLASS_SCOPE)
//			origScope = (ClassScope)origScope.parent;
	
		TypeReference result;
		if (original instanceof ParameterizedSingleTypeReference) {
			ParameterizedSingleTypeReference pstRef = (ParameterizedSingleTypeReference) original;
			TypeReference[] typeArguments = AstClone.copyTypeArguments(original, this.pos, pstRef.typeArguments);
			result = new AlienScopeParameterizedSingleTypeReference(pstRef.token, typeArguments, pstRef.dimensions, this.pos, origScope);
		} else if (original instanceof SingleTypeReference) {
			if (original instanceof ArrayTypeReference && original.dimensions() > 0) { // could be parameterized type reference
				ArrayTypeReference singleTypeRef = (ArrayTypeReference) original;
				result = new AlienScopeArrayTypeReference(singleTypeRef.token, this.pos, singleTypeRef.dimensions, origScope);				
			} else {
				SingleTypeReference singleTypeRef = (SingleTypeReference) original;
				result = new AlienScopeSingleTypeReference(singleTypeRef.token, this.pos, origScope);
			}
		} else if (original instanceof QualifiedTypeReference) {
			if (original instanceof ArrayQualifiedTypeReference && original.dimensions() > 0) { // could be parameterized type reference
				ArrayQualifiedTypeReference qTypeRef= (ArrayQualifiedTypeReference)original;
				result = new AlienScopeArrayQualifiedTypeReference(qTypeRef.tokens, qTypeRef.sourcePositions, qTypeRef.dimensions(), origScope);				
			} else if (original instanceof ParameterizedQualifiedTypeReference) {
				ParameterizedQualifiedTypeReference qTypeRef= (ParameterizedQualifiedTypeReference)original;
				result = new AlienScopeParameterizedQualifiedTypeReference(qTypeRef, origScope);
			} else {
				QualifiedTypeReference qTypeRef= (QualifiedTypeReference)original;
				result = new AlienScopeQualifiedTypeReference(qTypeRef.tokens, qTypeRef.sourcePositions, origScope);
			}
		} else {			
			throw new InternalCompilerError("Unexpected type reference: "+original); //$NON-NLS-1$
		}
		result.setBaseclassDecapsulation(DecapsulationState.REPORTED);
		result.bits |= ASTNode.IgnoreRawTypeCheck;
		return result;
	}
}
