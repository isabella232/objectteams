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
 * $Id: AstClone.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import java.util.Arrays;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.QualifiedBaseReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeValueParameter;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;

/**
 * Cloning AST nodes.
 *
 * @author Markus Witte
 * @version $Id: AstClone.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AstClone
{

    /**
     * This method will duplicate a Type Reference
     * @param typeReference the TypeReference to duplicate
     * @return the duplicated TypeReference
     */
    public static TypeReference copyTypeReference(TypeReference typeReference, AstGenerator gen)
    {
        if (typeReference == null) return null;
        if (gen == null)
        	return copyTypeReference(typeReference);

        TypeReference result = null;
        int dims = typeReference.dimensions();
        if (typeReference instanceof Wildcard) {
        	Wildcard wildcard = (Wildcard) typeReference;
        	Wildcard newWildcard = gen.wildcard(wildcard.kind);
        	newWildcard.bound = copyTypeReference(wildcard.bound);
        	result = newWildcard;
        } else if (typeReference instanceof SingleTypeReference)
        {
        	char[] name = ((SingleTypeReference)typeReference).token;
            if (typeReference instanceof ParameterizedSingleTypeReference) {
            	ParameterizedSingleTypeReference pTypeReference = (ParameterizedSingleTypeReference)typeReference;
            	TypeReference[] newArgs = copyTypeArguments(typeReference, gen.pos, pTypeReference.typeArguments);
            	result = gen.parameterizedSingleTypeReference(name, newArgs, dims);
            } else {
	            if (dims > 0)
	                result = gen.arrayTypeReference(name, dims);
	            else
	                result = gen.singleTypeReference(name);
            }
        }
        else if (typeReference instanceof QualifiedTypeReference)
        {
            QualifiedTypeReference qualifiedTypeReference =
                (QualifiedTypeReference) typeReference;

    	    char[][] typeName = CharOperation.deepCopy(qualifiedTypeReference.tokens);
            if (typeReference instanceof ParameterizedQualifiedTypeReference) {
            	ParameterizedQualifiedTypeReference pqTypeReference = (ParameterizedQualifiedTypeReference)typeReference;
            	int len= pqTypeReference.typeArguments.length;
            	TypeReference[][] newArgs = new TypeReference[len][];
            	for (int i=0; i<len; i++)
            		newArgs[i] = copyTypeArray(pqTypeReference.typeArguments[i]); // sufficient; qualified types are not dependent.
            	result = gen.parameterizedQualifiedTypeReference(typeName, newArgs, dims);
            } else {
            	QualifiedTypeReference qualifiedResult;
	            if (dims > 0)
	                qualifiedResult = gen.qualifiedArrayTypeReference(typeName, dims);
	            else
	                qualifiedResult = gen.qualifiedTypeReference(typeName);
	            qualifiedResult.isGenerated = qualifiedTypeReference.isGenerated;
	            result = qualifiedResult;
            }
        }

        else if (typeReference instanceof LiftingTypeReference)
        {
            LiftingTypeReference liftingRef = (LiftingTypeReference) typeReference;
            result = gen.liftingTypeReference(AstClone.copyTypeReference(liftingRef.baseReference),
            								  AstClone.copyTypeReference(liftingRef.roleReference),
            								  liftingRef.roleToken,
            								  liftingRef.baseTokens);
        }
        else if (typeReference instanceof TypeAnchorReference)
        {
        	TypeAnchorReference anchorRef = (TypeAnchorReference)typeReference;
        	result = new TypeAnchorReference(copyReference(anchorRef.anchor), gen.sourceStart);
        	// TODO(SH): need to set any further fields??
        }
        if (result != null) {
        	result.setBaseclassDecapsulation(typeReference.getBaseclassDecapsulation());
        	result.bits = typeReference.bits;
        	return result;
        }
        throw new InternalCompilerError("Unexpected kind of type reference: " + typeReference.getClass().getName()); //$NON-NLS-1$
    }
    public static TypeReference copyTypeReference(TypeReference typeReference)
    {
        if (typeReference == null) return null;

        TypeReference result = null;
        int dims = typeReference.dimensions();
        if (typeReference instanceof Wildcard) {
        	Wildcard wildcard = (Wildcard) typeReference;
        	Wildcard newWildcard = new Wildcard(wildcard.kind);
        	newWildcard.sourceStart = wildcard.sourceStart;
        	newWildcard.sourceEnd   = wildcard.sourceEnd;
        	newWildcard.bound = copyTypeReference(wildcard.bound);
        	result = newWildcard;
        } else if (typeReference instanceof SingleTypeReference)
        {
        	char[] name = ((SingleTypeReference)typeReference).token;
            long pos = (((long)typeReference.sourceStart)<<32) + typeReference.sourceEnd;
            if (typeReference instanceof ParameterizedSingleTypeReference) {
            	ParameterizedSingleTypeReference pTypeReference = (ParameterizedSingleTypeReference)typeReference;
            	TypeReference[] newArgs = copyTypeArguments(typeReference, pos, pTypeReference.typeArguments);
            	result = new ParameterizedSingleTypeReference(name, newArgs, dims, pos);
            } else {
	            if (dims > 0)
	                result = new ArrayTypeReference(name, dims, pos);
	            else
	                result = new SingleTypeReference(name, pos);
            }
        }
        else if (typeReference instanceof QualifiedTypeReference)
        {
            QualifiedTypeReference qualifiedTypeReference =
                (QualifiedTypeReference) typeReference;

    	    char[][] typeName = CharOperation.deepCopy(qualifiedTypeReference.tokens);
            long[] poss = qualifiedTypeReference.sourcePositions;
            if (typeReference instanceof ParameterizedQualifiedTypeReference) {
            	ParameterizedQualifiedTypeReference pqTypeReference = (ParameterizedQualifiedTypeReference)typeReference;
            	int len= pqTypeReference.typeArguments.length;
            	TypeReference[][] newArgs = new TypeReference[len][];
            	for (int i=0; i<len; i++)
            		newArgs[i] = copyTypeArray(pqTypeReference.typeArguments[i]); // sufficient; qualified types are not dependent.
            	result = new ParameterizedQualifiedTypeReference(typeName, newArgs, dims, poss);
            } else {
	            if (dims > 0)
	                result = new ArrayQualifiedTypeReference(typeName, dims, poss, qualifiedTypeReference.isGenerated);
	            else
	                result = new QualifiedTypeReference(typeName, poss, qualifiedTypeReference.isGenerated);
            }
        }

        else if (typeReference instanceof LiftingTypeReference)
        {
            LiftingTypeReference liftingRef = (LiftingTypeReference) typeReference;
            LiftingTypeReference newLiftingRef = new LiftingTypeReference();
            newLiftingRef.baseReference = AstClone.copyTypeReference(liftingRef.baseReference);
            newLiftingRef.roleReference = AstClone.copyTypeReference(liftingRef.roleReference);
            newLiftingRef.sourceStart = liftingRef.sourceStart;
            newLiftingRef.sourceEnd   = liftingRef.sourceEnd;

            newLiftingRef.roleToken  = liftingRef.roleToken;
            newLiftingRef.baseTokens = new char[liftingRef.baseTokens.length][];
            System.arraycopy(liftingRef.baseTokens, 0, newLiftingRef.baseTokens, 0, liftingRef.baseTokens.length);

            result = newLiftingRef;
        }
        else if (typeReference instanceof TypeAnchorReference)
        {
        	TypeAnchorReference anchorRef = (TypeAnchorReference)typeReference;
        	result = new TypeAnchorReference(copyReference(anchorRef.anchor), anchorRef.sourceStart);
        	// TODO(SH): need to set any further fields??
        }
        if (result != null) {
        	result.setBaseclassDecapsulation(typeReference.getBaseclassDecapsulation());
        	result.bits = typeReference.bits;
        	return result;
        }
        throw new InternalCompilerError("Unexpected kind of type reference: " + typeReference.getClass().getName()); //$NON-NLS-1$
    }


	public static TypeReference[] copyTypeArguments(TypeReference typeReference, long pos, TypeReference[] arguments)
	{
		TypeReference[] newArgs = copyTypeArray(arguments);
		if (   typeReference.resolvedType != null
			&& typeReference.resolvedType instanceof DependentTypeBinding)
		{
			DependentTypeBinding dependentTypeBinding = (DependentTypeBinding)typeReference.resolvedType;
			if (dependentTypeBinding.hasExplicitAnchor()) {
				// recover TypeAnchorReference which is consumed during resolve.
				AstGenerator gen = new AstGenerator(pos);
				ITeamAnchor anchor = dependentTypeBinding._teamAnchor;
				TypeAnchorReference anchorRef = gen.typeAnchorReference(anchor);
				int len = newArgs.length;
				System.arraycopy(
						newArgs, 0,
						newArgs = new TypeReference[len+1], 1, len);
				newArgs[0] = anchorRef;
			}
		}
		return newArgs;
	}


    /**
     * @param references
     * @return  an arrays of copies of references' elements
     */
    public static TypeReference[] copyTypeArray(TypeReference[] references) {
        if (references == null) return null;
        TypeReference[] result = new TypeReference[references.length];
        for (int i=0; i<references.length; i++)
            result[i] = copyTypeReference(references[i]);
        return result;
    }

    public static Reference copyReference(Reference nameRef) {
    	if (nameRef instanceof SingleNameReference) {
    		SingleNameReference singleRef = (SingleNameReference)nameRef;
    		return new SingleNameReference(singleRef.token, ((long)singleRef.sourceStart<<32)+singleRef.sourceEnd);
    	} else if (nameRef instanceof QualifiedNameReference){
    		QualifiedNameReference qualRef = (QualifiedNameReference)nameRef;
    		return new QualifiedNameReference(qualRef.tokens, qualRef.sourcePositions, qualRef.sourceStart, qualRef.sourceEnd);
    	} else if (nameRef instanceof FieldReference) {
    		FieldReference fieldRef = (FieldReference) nameRef;
    		FieldReference clone = new FieldReference(fieldRef.token, fieldRef.nameSourcePosition);
    		clone.receiver = copyReference((Reference) fieldRef.receiver);
    		return clone;
    	} else if (nameRef instanceof QualifiedBaseReference) {
    		QualifiedBaseReference baseRef = (QualifiedBaseReference) nameRef;
    		return new QualifiedBaseReference(copyTypeReference(baseRef.qualification), baseRef.sourceStart, baseRef.sourceEnd);
    	} else if (nameRef instanceof QualifiedThisReference) {
    		QualifiedThisReference thisRef = (QualifiedThisReference) nameRef;
    		return new QualifiedThisReference(copyTypeReference(thisRef.qualification), thisRef.sourceStart, thisRef.sourceEnd);
    	}
    	throw new InternalCompilerError("Unexpected reference type "+nameRef.getClass()+" for "+nameRef); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     */
    public static TypeReference[] copyExceptions(MethodBinding method, AstGenerator gen) {
    	TypeBinding[] types = method.thrownExceptions;
        if (types == null || types == Binding.NO_EXCEPTIONS)
        	return null;

        return copyTypeArray(types, gen);
    }

	/**
     * @param types
     * @return an arrays of type references build according to the types in types
     */
    private static TypeReference[] copyTypeArray(TypeBinding[] types, AstGenerator gen) {
        if (types == null) return null;
        TypeReference[] result = new TypeReference[types.length];
        for (int i=0; i<types.length; i++)
            result[i] = gen.typeReference(types[i]);
        return result;
    }

    /**
     * @param arguments
	 * @param gen       for source positions, may be null.
     * @return a deep copy of arguments
     */
    public static Argument[] copyArguments(Argument[] arguments, AstGenerator gen) {
        if (arguments == null) return null;
        Argument[] result = new Argument[arguments.length];
        for (int i=0; i<arguments.length; i++) {
            Argument argument = arguments[i];
            if (gen != null)
            	result[i] = gen.argument(
                    argument.name,
                    copyTypeReference(argument.type, gen),
                    argument.modifiers);
            else
            	result[i] = new Argument(
            			argument.name,
            			(((long)argument.sourceStart)<<32)+argument.sourceEnd,
                        copyTypeReference(argument.type),
                        argument.modifiers);
        }
        return result;
    }


    /**
     * Copy all source locations from srcMethod to generated tgtMethod
     * @param method
     * @param newmethod
     */
    public static void copySrcLocation(
            AbstractMethodDeclaration srcMethod,
            AbstractMethodDeclaration tgtMethod)
    {
        tgtMethod.sourceStart = srcMethod.sourceStart;
        tgtMethod.sourceEnd   = srcMethod.sourceEnd;
        tgtMethod.declarationSourceStart = srcMethod.declarationSourceStart;
        tgtMethod.declarationSourceEnd   = srcMethod.declarationSourceEnd;
	    tgtMethod.bodyStart = srcMethod.bodyStart;
	    tgtMethod.bodyEnd   = srcMethod.bodyEnd;
	    tgtMethod.modifiersSourceStart = srcMethod.modifiersSourceStart;
    }


	/**
	 * Copies these properties of a method:
	 * selector, arguments, returnType, thrownExceptions, src-locations,
	 * and marks it as generated.
	 *
	 * @param type
	 * @param md
	 * @return a partial copy of md
	 */
	public static MethodDeclaration copyMethod(TypeDeclaration type, MethodDeclaration md, AstGenerator gen) {
		MethodDeclaration newmethod =
			new MethodDeclaration(type.compilationResult);

		newmethod.selector    = md.selector;
		newmethod.isGenerated = true; // don't try to parse it etc..
		newmethod.arguments   = AstClone.copyArguments(md.arguments, gen);
		newmethod.returnType  = AstClone.copyTypeReference(md.returnType, gen);
		newmethod.thrownExceptions =
		                        AstClone.copyTypeArray(md.thrownExceptions);
		newmethod.typeParameters =
								AstClone.copyTypeParameters(md.typeParameters);
		if (gen != null)
			gen.setMethodPositions(newmethod);
		else
			AstClone.copySrcLocation(md, newmethod);
		return newmethod;
	}


	public static TypeParameter[] copyTypeParameters(TypeParameter[] typeParameters) {
		if (typeParameters == null)
			return null;
		int len = typeParameters.length;
		TypeParameter[] result = new TypeParameter[len];
		for (int i=0; i<len; i++) {
			if (typeParameters[i] instanceof TypeValueParameter) {
				long poss = ((long)typeParameters[i].sourceStart)<<32 + (long)typeParameters[i].sourceEnd;
				result[i] = new TypeValueParameter(typeParameters[i].name, poss);
				result[i].declarationSourceStart = typeParameters[i].declarationSourceStart;
			} else {
				result[i] = new TypeParameter();
				result[i].bounds = copyTypeArray(typeParameters[i].bounds);
				result[i].bits = typeParameters[i].bits;
				result[i].declarationSourceStart = typeParameters[i].declarationSourceStart;
				result[i].declarationSourceEnd = typeParameters[i].declarationSourceEnd;
				result[i].sourceStart = typeParameters[i].sourceStart;
				result[i].sourceEnd = typeParameters[i].sourceEnd;
				result[i].declarationEnd = typeParameters[i].declarationEnd;
				result[i].name = typeParameters[i].name;
				result[i].type = copyTypeReference(typeParameters[i].type);
			}
		}
		return result;
	}


	public static ImportReference copyImportReference(char[][] currentPackageName)
	{
		int len = currentPackageName.length;
		if (len == 0)
			return null;
		long[] poss = new long[len];
		Arrays.fill(poss, 0L);
		return new ImportReference(currentPackageName, poss, false, 0);
	}
}
// Markus Witte}
