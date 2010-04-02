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
 * $Id: AstEdit.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/

package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeContainerMethod;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;


/**
 * MIGRATION_STATE: E 3.2 RC1a.
 *
 * Utility class for adding or removing elements to/from arrays within the AST.
 *
 * @author Markus Witte
 * @version $Id: AstEdit.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AstEdit {

	/**
	 * add a field to TypeDeclaration
	 * @param decl
	 * @param field
	 * @param createBinding should FieldBinding be created?
	 * @param hasTypeProblem
	 */
	public static void addField(TypeDeclaration decl, FieldDeclaration field, boolean createBinding, boolean hasTypeProblem) {
        if (field.declarationSourceStart <= 0 ||
                field.declarationSourceEnd <= 0 ||
                field.declarationEnd <= 0 ||
                field.sourceStart <= 0 ||
                field.sourceEnd <= 0)
        {
            //throw new InternalCompilerError("Generated field is missing some source range information.");
            //System.err.println("Generated field is missing some source range information: " + String.valueOf(field.name));
        }

        int length;
		FieldDeclaration[] fields;
		if ((fields = decl.fields) == null) {
			length = 0;
			fields = new FieldDeclaration[1];
		} else {
			length = fields.length;
			System.arraycopy(
				fields,
				0,
				(fields = new FieldDeclaration[length + 1]),
				0,
				length);
		}

		fields[length] = field;

		decl.fields = fields;
		boolean modifiersAdjusted = FieldModel.checkCreateModifiersAttribute(decl, field);
        if (decl.binding != null && createBinding) {
            FieldBinding binding = decl.scope.addGeneratedField(field, hasTypeProblem);
            if (modifiersAdjusted)
            	binding.tagBits |= TagBits.ClearPrivateModifier;
        }
	}

    /**
     * remove field from TypeDeclaration (search by equals())
     * @param decl
     * @param field
     */
    public static void removeField(TypeDeclaration decl, FieldDeclaration field) {
        int pos = -1;
        int length;
        length = decl.fields.length;
        FieldDeclaration[] fields = new FieldDeclaration[length - 1];

        if (decl.fields != null) {
            for (int i = 0; i < decl.fields.length; i++) {
                if (field.equals(decl.fields[i]))
                    pos = i;
            }
        }

        if (pos >= 0) {
            System.arraycopy(decl.fields, 0, fields, 0, pos);
            System.arraycopy(
                decl.fields, pos + 1,
                fields,      pos,
                length - (pos + 1));
            decl.fields = fields;
        }
    }

	/**
	 * Adds a new Method to TypeDeclaration and resolve its types.
	 *
   	 * @param classTypeDeclaration
 	 * @param methodDeclaration
 	 */
    public static void addMethod(
    		TypeDeclaration classTypeDeclaration,
            AbstractMethodDeclaration methodDeclaration)
    {
    	addMethod(classTypeDeclaration, methodDeclaration, false /*not synthetic*/, false/*addToFront*/);
    }

    /**
     * This variant ensures, that if the type is a role the method is added to the class plus
     * an abstract version to the interface.
     */
    public static void addGeneratedMethod(
    		TypeDeclaration   classTypeDeclaration,
            MethodDeclaration methodDeclaration)
    {
    	if (   classTypeDeclaration.isRole()
    		&& (methodDeclaration.modifiers & ClassFileConstants.AccPublic) != 0)
    	{
    		RoleModel roleModel = classTypeDeclaration.getRoleModel();
    		if (roleModel != null && roleModel.getClassPartAst() != null) {
    			TypeDeclaration ifcPart = roleModel.getInterfaceAst();
    			if (ifcPart != null) {
    				// look for duplicates, assuming name is sufficient for comparison.
    				AbstractMethodDeclaration ifcMethod = null;
    				if (ifcPart.methods != null)
    					for(AbstractMethodDeclaration method : ifcPart.methods)
    						if (CharOperation.equals(method.selector, methodDeclaration.selector)) {
    							ifcMethod = method;
    							break;
    						}
    				if (ifcMethod == null) {
    					ifcMethod = AstConverter.genRoleIfcMethod(ifcPart, methodDeclaration);
    					addMethod(ifcPart, ifcMethod);
    				}
    			}
    			classTypeDeclaration = roleModel.getClassPartAst(); // proceed with this one
    		}
    	}
    	addMethod(classTypeDeclaration, methodDeclaration);
    }
	/**
	 * Adds a new Method to TypeDeclaration and resolve its types.
	 *
   	 * @param classTypeDeclaration
	 * @param methodDeclaration
	 * @param wasSynthetic was the method copied from a synthetic method?
	 * @param addToFront should the method be added to the front of 'methods'?
 	 */
	public static void addMethod(
            TypeDeclaration classTypeDeclaration,
            AbstractMethodDeclaration methodDeclaration,
			boolean wasSynthetic,
			boolean addToFront)
    {
        boolean modifiersAdjusted = addMethodDeclOnly(classTypeDeclaration, methodDeclaration, addToFront);
		if (classTypeDeclaration.binding != null) {
			classTypeDeclaration.binding.resolveGeneratedMethod(methodDeclaration, wasSynthetic);
			if (modifiersAdjusted)
				methodDeclaration.binding.tagBits |= TagBits.ClearPrivateModifier;
		}
	}

	public static boolean addMethodDeclOnly(TypeDeclaration classTypeDeclaration, AbstractMethodDeclaration methodDeclaration, boolean addToFront) {
		if (methodDeclaration.declarationSourceStart <= 0 ||
                methodDeclaration.declarationSourceEnd <= 0 ||
                methodDeclaration.sourceStart <= 0 ||
                methodDeclaration.sourceEnd <= 0 ||
//            	methodDeclaration.bodyStart <= 0 ||
            	methodDeclaration.bodyEnd <= 0)
        {
            //throw new InternalCompilerError("Generated method is missing some source range information.");
//            System.err.print("Generated method is missing some source range information: " + String.valueOf(methodDeclaration.selector));
//            String type = classTypeDeclaration.isInterface() ? "interface" : "class";
//            System.err.println("   (" + type + " " + String.valueOf(classTypeDeclaration.name) + ")");
        }

		int length;
		AbstractMethodDeclaration[] methods;
		if ((methods = classTypeDeclaration.methods) == null) {
			length = 0;
			methods = new AbstractMethodDeclaration[1];
		} else {
			length = methods.length;
			System.arraycopy(
				methods,
				0,
				(methods = new AbstractMethodDeclaration[length + 1]),
				addToFront ? 1 : 0,
				length);
		}

		if (addToFront)
			methods[0] = methodDeclaration;
		else
			methods[length] = methodDeclaration;

		classTypeDeclaration.methods = methods;
		boolean modifiersAdjusted = MethodModel.checkCreateModifiersAttribute(classTypeDeclaration, methodDeclaration);
		return modifiersAdjusted;
	}

    /**
     * Remove a given method from AST and binding of its declaring type.
     * @param decl the type to be edited
     * @param method the method to be removed.
     */
    public static void removeMethod(TypeDeclaration decl, MethodBinding method) {
        int pos = -1;
        int length;
        {   // AST:
            length = decl.methods.length;
            AbstractMethodDeclaration[] methods = new AbstractMethodDeclaration[length - 1];

            if (decl.methods != null) {
                for (int i = 0; i < decl.methods.length; i++) {
                    if (method == decl.methods[i].binding) {
                        pos = i;
                        break;
                    }
                }
            }

            if (pos >= 0) {
                System.arraycopy(decl.methods, 0, methods, 0, pos);
                System.arraycopy(
                    decl.methods, pos + 1,
                    methods,      pos,
                    length - (pos + 1));
                decl.methods = methods;
            }
        }
        {   // and now the binding:
            decl.binding.removeMethod(method);
        }
    }

	/**
	 * After we found a bound role (binding might be inherited through some
	 * dimension), we have to ensure, that it has no default constructor,
	 * because those are superceded by lifting constructors.
	 * @param roleType
	 */
	public static void removeDefaultConstructor(TypeDeclaration roleType) {
	    AbstractMethodDeclaration[] methods = roleType.methods;
	    if (methods != null) {
	        for (int i=0; i<methods.length; i++) {
	            if (methods[i] instanceof ConstructorDeclaration) {
	                if (((ConstructorDeclaration)methods[i]).isDefaultConstructor())
	                    removeMethod(roleType, methods[i].binding);
	            }
	        }
	    }
	}

	/**
	 * Adds a MemberTypeDeclaration to TypeDeclaration and set enclosingType.
	 * (no further processing besides array growing).
	 * Add to end of array, so loops iterating over exactly this array
	 * have a chance to catch the newly added member type.
     *
	 * @param typeDeclaration
	 * @param memberTypeDeclaration
	 */
	public static void addMemberTypeDeclaration(
            TypeDeclaration       typeDeclaration,
            TypeDeclaration memberTypeDeclaration)
    {
		boolean found= false;
		// converted types may already have ROFI member:
		if (   typeDeclaration.isConverted
			&& typeDeclaration.memberTypes != null
			&& memberTypeDeclaration.isRoleFile())
		{
			for(int i=0; i < typeDeclaration.memberTypes.length; i++) {
				if (CharOperation.equals(typeDeclaration.memberTypes[i].name, memberTypeDeclaration.name))
				{
					// replace converted member with new one:
					typeDeclaration.memberTypes[i]= memberTypeDeclaration;
					found= true;
					break;
				}
			}
		}
		if (!found) {
			int length = 0;
			if (typeDeclaration.memberTypes != null) {
				length = typeDeclaration.memberTypes.length;
				System.arraycopy(
					typeDeclaration.memberTypes,
					0,
					(typeDeclaration.memberTypes = new TypeDeclaration[length + 1]),
					0,
					length);
			} else {
				typeDeclaration.memberTypes = new TypeDeclaration[1];
			}

			// add to the end of the array. This action may be triggered within
			// a loop over all memberTypes which needs to see the newly added type, too!
			// (see TypeDeclaration.resolve())
			typeDeclaration.memberTypes[length] = memberTypeDeclaration;
		}
		// ROFI: role files are only now linked to their team:
		assert memberTypeDeclaration.enclosingType == null;
		memberTypeDeclaration.enclosingType = typeDeclaration;
 		memberTypeDeclaration.bits |= ASTNode.IsMemberType;
	}

	/**
	 * Adds a TypeDeclaration to a CompilationUnitDeclaration and link back.
	 * (no further processing besides array growing).
     *
	 * @param compilationUnitDeclaration
	 * @param typeDeclaration
	 */
	public static void addTypeDeclaration(
            CompilationUnitDeclaration unitDeclaration,
            TypeDeclaration            typeDeclaration)
    {
		if (unitDeclaration.types != null) {
			int length;
			length = unitDeclaration.types.length;
			System.arraycopy(
				unitDeclaration.types,
				0,
				(unitDeclaration.types = new TypeDeclaration[length + 1]),
				1,
				length);
		} else {
			unitDeclaration.types = new TypeDeclaration[1];
		}

		unitDeclaration.types[0] = typeDeclaration;
		assert typeDeclaration.compilationUnit == null;
		typeDeclaration.compilationUnit = unitDeclaration;
	}

	/**
	 * Local types are added using a dummy wrapper method to ensure further processing
	 * @param enclosingTypeDecl
	 * @param nestedType
	 */
	public static void addLocalTypeDeclaration(
				TypeDeclaration enclosingTypeDecl,
				TypeDeclaration nestedType)
	{
		TypeContainerMethod wrapper = new TypeContainerMethod(
					enclosingTypeDecl.compilationResult,
					nestedType);
		nestedType.enclosingType = enclosingTypeDecl;
		// add this method to the front, because type container must generateCode(),
		// before the method using this type gets its byte code copied.
		// (for the local type's ctor MethodBinding.signature() must not be called before
		//  synthetic outer locals are in place, which happens during TypeDeclaration.generateCode)
		addMethod(enclosingTypeDecl, wrapper, false/*wasSynthetic*/, true/*addToFront*/);
	}


	/**
	 * Adds a new binding in the superInterfaces array of type's binding
	 * @param typeDeclaration
	 * @param resolvedSuper
	 */
	public static void addImplementsBinding(
            TypeDeclaration  typeDeclaration,
            ReferenceBinding resolvedSuper)
    {
		boolean bindingPresent= typeDeclaration.binding != null
							 && ((typeDeclaration.binding.tagBits & TagBits.BeginHierarchyCheck) != 0);
		assert (resolvedSuper != null && bindingPresent);
		SourceTypeBinding typeBinding = typeDeclaration.binding;
		ReferenceBinding[] superInterfaces= typeBinding.superInterfaces;
		int length= 0;
		if (superInterfaces == null) {
			superInterfaces= new ReferenceBinding[1];
		} else {
			for (ReferenceBinding superIfc : superInterfaces)
				if (superIfc == resolvedSuper)
					return; // already present

			length= superInterfaces.length;
			System.arraycopy(
					superInterfaces, 0,
					(superInterfaces = new ReferenceBinding[length + 1]), 1,
					length);
		}
		superInterfaces[0]= resolvedSuper;

		typeBinding.superInterfaces= superInterfaces;
		// compatibility may have changed, clear negative cache entries:
		typeBinding.resetIncompatibleTypes();
    }
	/**
	 * Adds a new reference in the implements array of typeDeclaration
	 * @param typeDeclaration
	 * @param reference
	 */
	public static void addImplementsReference(
            TypeDeclaration  typeDeclaration,
            TypeReference    reference)
    {
		reference=AstClone.copyTypeReference(reference);
		if (reference != null) {
			int length= 0;
			TypeReference[] superInterfaces= typeDeclaration.superInterfaces;
			if (superInterfaces == null) {
				superInterfaces = new TypeReference[1];
			} else {
				length = superInterfaces.length;
				System.arraycopy(
					superInterfaces, 0,
					(superInterfaces = new TypeReference[length + 1]), 1,
					length);
			}
			superInterfaces[0] = reference;

			typeDeclaration.superInterfaces = superInterfaces;
		}
	}


	/** Append one expression to an existing array of expressions. */
	public static Expression[] extendExpressionArray(
			Expression[] exprs,
			Expression   lastExpr)
	{
		if (exprs == null)
			return new Expression[]{lastExpr};
		int len = exprs.length;
		System.arraycopy(
				exprs, 0,
				exprs = new Expression[len+1], 0,
				len);
		exprs[len] = lastExpr;
		return exprs;
	}


	/** Append one type to an existing array of types. */
	public static TypeBinding[] extendTypeArray(
			TypeBinding[]    types,
			TypeBinding      lastType)
	{
		int len = types.length;
		System.arraycopy(
				types, 0,
				types = new TypeBinding[len+1], 0,
				len);
		types[len] = lastType;
		return types;
	}

	/**
	 * Add the (implements) link between a class part and its interface part.
	 * @param interfaceName
	 * @param roleClassDecl
	 */
	public static void addImplementsInterfaceReference(
	        char[] interfaceName,
	        TypeDeclaration roleClassDecl)
	{
	    long pos = (((long)roleClassDecl.sourceStart)<<32)+roleClassDecl.sourceEnd;
	    AstGenerator gen = new AstGenerator(pos);
	    TypeReference implementsRef = null;
	    if (roleClassDecl.typeParameters == null) {
	    	implementsRef = gen.singleTypeReference(interfaceName);
	    } else {
	    	int len = roleClassDecl.typeParameters.length;
	    	TypeReference[] args =new TypeReference[len];
	    	for (int i = 0; i < len; i++) {
				args[i] = gen.singleTypeReference(roleClassDecl.typeParameters[i].name);
				args[i].bits |= ASTNode.IsGenerated;
			}
	    	implementsRef = new ParameterizedSingleTypeReference(interfaceName, args, 0, pos);
	    }
	    implementsRef.bits |= ASTNode.IsGenerated;
	    addImplementsReference(roleClassDecl, implementsRef);
	}



}
