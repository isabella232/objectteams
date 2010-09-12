/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/** Generate methods that should be invoked by custom serialization code. */
public class SerializationGenerator {

	// method names
	private static final char[] RESTORE_ROLE = "restoreRole".toCharArray(); //$NON-NLS-1$
	private static final char[] RESTORE = "restore".toCharArray(); //$NON-NLS-1$
	// references to API
	private static final char[] IS_ASSIGNABLE_FROM = "isAssignableFrom".toCharArray(); //$NON-NLS-1$
	private static final char[] PUT = "put".toCharArray(); //$NON-NLS-1$
	// internal names
	private static final char[] ROLE_ARG_NAME = "role".toCharArray(); //$NON-NLS-1$
	private static final char[] CLASS_ARG_NAME = "clazz".toCharArray(); //$NON-NLS-1$
	private static final char[] CASTED_ROLE = "castedRole".toCharArray(); //$NON-NLS-1$

	/** 
	 * Generate a restore() and a restoreRole(Class,Object) method.
	 * The latter remains empty at this point and must be filled later using 
	 * {@link #fillRestoreRole(TypeDeclaration,FieldDeclaration[])}.
	 * @param teamType the AST where to add the methods.
	 * @param gen AstGenerator with proper positions for generating.
	 */
	public static void generateRestoreMethods(TypeDeclaration teamType, AstGenerator gen) {
		boolean superIsTeam = teamType.binding.superclass.isTeam();
		MethodDeclaration restore = gen.method(teamType.compilationResult, ClassFileConstants.AccProtected,
				gen.singleTypeReference(TypeConstants.VOID), RESTORE, null/*arguments*/,
				new Statement[] {
					superIsTeam
					? gen.messageSend(gen.superReference(), RESTORE, null)
					: gen.emptyStatement(),
					gen.messageSend(gen.thisReference(), IOTConstants.OT_INIT_CACHES, null)
		});
		if (superIsTeam)
			restore.annotations = new Annotation[] {
					gen.markerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE)
			};
		AstEdit.addMethod(teamType, restore);
		
		MethodDeclaration restoreRole = gen.method(
				teamType.compilationResult, 
				ClassFileConstants.AccProtected,
				TypeBinding.VOID, 
				RESTORE_ROLE, 
				new Argument[] {
					gen.argument(CLASS_ARG_NAME, gen.parameterizedQualifiedTypeReference(TypeConstants.JAVA_LANG_CLASS, new TypeReference[]{gen.wildcard(Wildcard.UNBOUND)})),
					gen.argument(ROLE_ARG_NAME, gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT))
				});
		if (superIsTeam)
			restoreRole.annotations = new Annotation[] {
					gen.markerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE)
			};
		AstEdit.addMethod(teamType, restoreRole);    	
	}

	/**
	 * Fill in the statement of a previously generated restoreRole() method.
	 * @param teamType type declaration assumeably holding an empty restoreRole method.
	 * @param caches   all declarations of role caches of this team type.
	 */
	public static void fillRestoreRole(TypeDeclaration teamType, FieldDeclaration[] caches) {
		AbstractMethodDeclaration restoreMethod = TypeAnalyzer.findMethodDecl(teamType, RESTORE_ROLE, 2);
		if (restoreMethod == null) {
			return;
		}
		boolean superIsTeam = teamType.binding.superclass.isTeam();

		AstGenerator gen = new AstGenerator(restoreMethod); // re-use position
		Statement[] statements = new Statement[caches.length+(superIsTeam?1:0)];
		
		// find the matching cache for argument Class clazz:
		for (int i = 0; i < caches.length; i++) {
			// FIXME(SH): unclear if needed after allowing generated qualified role type referneces:
			TypeReference cacheTypeRef = caches[i].type; // robustness, but with wrong source position
			
			if (! (caches[i].type.resolvedType instanceof ParameterizedTypeBinding)
				|| ((ParameterizedTypeBinding)cacheTypeRef.resolvedType).arguments.length != 2)
				throw new InternalCompilerError("Unexpected resolved cache type "+cacheTypeRef.resolvedType); //$NON-NLS-1$
	
			// reconstruct a type reference from the resolved cache type
			ParameterizedTypeBinding oldBinding = (ParameterizedTypeBinding)cacheTypeRef.resolvedType;
			ReferenceBinding roleBinding = (ReferenceBinding)oldBinding.arguments[1];
			// respect different status for base/role types (scope, decapsulation).
			cacheTypeRef = gen.getCacheTypeReference(teamType.scope, roleBinding.roleModel);
			
			statements[i] = gen.ifStatement(
					// if (Role.class.isAssignableFrom(clazz)) { ...
					gen.messageSend(gen.classLiteralAccess(gen.typeReference(roleBinding)), 
									IS_ASSIGNABLE_FROM, 
									new Expression[] { gen.singleNameReference(CLASS_ARG_NAME)}),
				    gen.block(new Statement[] {
				    	// Role castedRole = (Role) role; 
				    	gen.localVariable(CASTED_ROLE, roleBinding, 
				    					  gen.castExpression(gen.singleNameReference(ROLE_ARG_NAME), 
				    							  			 gen.typeReference(roleBinding), CastExpression.RAW)),
				    	// Base base = role._OT$getBase();
				    	gen.localVariable(IOTConstants.BASE, gen.baseclassReference(roleBinding.baseclass(), true /*erase*/),
				    					  gen.messageSend(
				    							  gen.singleNameReference(CASTED_ROLE), 
				    							  IOTConstants._OT_GETBASE, 
				    							  null/*arguments*/)),
				    	// <roleCache[i]>.put(base, castedRole);
				    	gen.messageSend(
				    		gen.singleNameReference(caches[i].name),
				    		PUT,
				    		new Expression[] {
				    			gen.baseNameReference(IOTConstants.BASE),
				    			gen.singleNameReference(CASTED_ROLE)
				    		}),
				    	// ((IBoundBase)base)._OT$addRole(castedRole);
				    	gen.messageSend(
				    		gen.castExpression(gen.singleNameReference(IOTConstants.BASE), 
				    						   gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE), CastExpression.RAW),
				    		IOTConstants.ADD_ROLE,
				    		new Expression[]{gen.singleNameReference(CASTED_ROLE)}),
				    	// return; // don't consult further caches
				    	gen.returnStatement(null)
			}));
		}
		
		if (superIsTeam) {
			// if no suitable cache found so far:
			// super.restoreRole(clazz, role);
			statements[caches.length] = gen.messageSend(gen.superReference(), RESTORE_ROLE,
						new Expression[] {gen.singleNameReference(CLASS_ARG_NAME),
										  gen.singleNameReference(ROLE_ARG_NAME)});
		}
		restoreMethod.setStatements(statements);
		if (StateMemento.hasMethodResolveStarted(teamType.binding))
			restoreMethod.resolve(teamType.scope);
	}

	public static boolean isSerializationMethod(MethodBinding method) {
		int nParams = method.parameters.length;
		if (CharOperation.equals(method.selector, RESTORE))
			return (nParams == 0);
		if (CharOperation.equals(method.selector, RESTORE_ROLE))
			return (nParams == 2);
		return false;
	}

}
