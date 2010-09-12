/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ReflectionGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import java.util.HashSet;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config.NotConfiguredException;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.LiftingEnvironment;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * MIGRATION_STATE: complete
 *
 * Creates the following reflective functions:
 * + boolean  hasRole(Object _OT$base_arg)
 * + boolean  hasRole(Object _OT$base_arg, Class class_arg)
 * + Object   getRole(Object _OT$base_arg)
 * + Object   getRole(Object _OT$base_arg, Class class_arg)
 * + Object[] getAllRoles()
 * + <T> T[]  getAllRoles(Class<T> class_arg)
 * + void     unregisterRole(Object _OT$role_arg)
 * + void     unregisterRole(Object _OT$role_arg, Class class_arg)
 *
 * @author stephan
 * @version $Id: ReflectionGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class ReflectionGenerator implements IOTConstants, ClassFileConstants {

	// method names:
	private static final char[] HAS_ROLE = "hasRole".toCharArray(); //$NON-NLS-1$
	private static final char[] GET_ROLE = "getRole".toCharArray(); //$NON-NLS-1$
	private static final char[] GET_ALL_ROLES   = "getAllRoles".toCharArray(); //$NON-NLS-1$
	public  static final char[] UNREGISTER_ROLE = "unregisterRole".toCharArray(); //$NON-NLS-1$

	// Type parameter of getAllRoles:
	private static final char[] T = "T".toCharArray(); //$NON-NLS-1$

	// variable names:
	private static final char[]   CLASS_ARG                  = "class_arg".toCharArray(); //$NON-NLS-1$
	private static final char[][] ILLEGAL_ARGUMENT_EXCEPTION = new char[][] {
			"java".toCharArray(), "lang".toCharArray(), "IllegalArgumentException".toCharArray() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};
	private static final char[]   FIRST_RESULT               = "first_result".toCharArray(); //$NON-NLS-1$
	private static final char[]   FIRST_NAME                 = "first_name".toCharArray(); //$NON-NLS-1$
	private static final char[]   FIRST_CACHE                = "first_cache".toCharArray(); //$NON-NLS-1$
	private static final char[]   FOUND_BASE                 = "found_base".toCharArray(); //$NON-NLS-1$
	private static final char[]   BASE_OBJ                   = "base_obj".toCharArray(); //$NON-NLS-1$

	// library classes/methods:
	// Class:
	private static final char[] IS_INSTANCE   = "isInstance".toCharArray(); //$NON-NLS-1$
	private static final char[] GET_NAME      = "getName".toCharArray(); //$NON-NLS-1$
	// Array:
	private static final char[][] ARRAY       = CharOperation.splitOn('.', "java.lang.reflect.Array".toCharArray()); //$NON-NLS-1$
	private static final char[] NEW_INSTANCE  = "newInstance".toCharArray(); //$NON-NLS-1$
	// Collection
	private static final char[] TO_ARRAY      = "toArray".toCharArray(); //$NON-NLS-1$
	private static final char[] ADD_ALL       = "addAll".toCharArray(); //$NON-NLS-1$
	private static final char[] ADD 		  = "add".toCharArray(); //$NON-NLS-1$
	private static final char[] SIZE		  = "size".toCharArray(); //$NON-NLS-1$
	// DoublyWeakHashMap:
	private static final char[] VALUES        = "values".toCharArray(); //$NON-NLS-1$
	private static final char[] REMOVE        = "remove".toCharArray(); //$NON-NLS-1$
	// String
	private static final char[] SUBSTRING     = "substring".toCharArray(); //$NON-NLS-1$
	private static final char[] ENDS_WITH     = "endsWith".toCharArray(); //$NON-NLS-1$

	/**
	 * Generate methods
	 * 		boolean hasRole(Object aBase);
	 *  	boolean hasRole(Object aBase, Class roleType);
	 * 		Object  getRole(Object aBase);
	 *  	Object  getRole(Object aBase, Class roleType);
	 *      void    unregisterRole(Object _OT$role_arg)
	 *      void    unregisterRole(Object _OT$role_arg, Class class_arg)
	 * Due to the similarities, we create all six methods simultaneously.
	 *
	 * @param teamDecl
	 */
	public static void createRoleQueryMethods(TypeDeclaration teamDecl)
	{
		long sourceLevel = teamDecl.scope.compilerOptions().sourceLevel;
		AstGenerator gen = new AstGenerator(sourceLevel, teamDecl.sourceStart, teamDecl.sourceEnd);
		AstGenerator gen2 = gen;
		if (sourceLevel >= ClassFileConstants.JDK1_5) {
			// gen2 produces positions that have a slight offset against the type:
			// we're adding @SuppressWarnings("all") which shouldn't affect other diagnostics
			// against the type's positions! (see usage of gen2 for more comments).
			gen2 = new AstGenerator(teamDecl.sourceStart, teamDecl.sourceEnd);
			gen2.shiftPosition();
		}

		TypeBinding booleanBinding = TypeBinding.BOOLEAN;
		TypeBinding objectBinding = teamDecl.scope.getJavaLangObject();
		ReferenceBinding classBinding = teamDecl.scope.getJavaLangClass();
		TypeBinding stringBinding = teamDecl.scope.getJavaLangString();
		TypeBinding hashMapBinding = teamDecl.scope.getType(WEAK_HASH_MAP, 3);
		TypeBinding objectArrayBinding;
		try {
			objectArrayBinding = Config.getLookupEnvironment().createArrayType(
													objectBinding, 1);
		} catch (NotConfiguredException e) {
			e.logWarning("Not creating reflective methods"); //$NON-NLS-1$
			return;
		}
		TypeReference objectCollectionRef;
		TypeReference wildcardCollectionRef;
		TypeReference roleTypeRef;
		TypeReference roleArrayTypeRef;
		if (sourceLevel >= ClassFileConstants.JDK1_5) {
			objectCollectionRef = gen.parameterizedQualifiedTypeReference(
												COLLECTION, new TypeBinding[] {objectBinding}, true/*deeply generic*/);
			wildcardCollectionRef = gen.parameterizedQualifiedTypeReference(COLLECTION, new TypeReference[] { new Wildcard(Wildcard.UNBOUND) });
			roleTypeRef= gen.singleTypeReference(T);
			roleArrayTypeRef= gen.arrayTypeReference(T, 1);
		} else {
			objectCollectionRef   = gen.qualifiedTypeReference(COLLECTION);
			wildcardCollectionRef = gen.qualifiedTypeReference(COLLECTION);
			roleTypeRef= gen.typeReference(objectBinding);
			roleArrayTypeRef= gen.typeReference(objectArrayBinding);
		}

		MethodDeclaration hasRole1 = findOrGeneratePublicMethod(
				teamDecl, booleanBinding, HAS_ROLE, 					// boolean hasRole(Object _OT$base_arg)
				new Argument[] {
						gen.argument(_OT_BASE_ARG, gen.singleTypeReference(objectBinding))
				},
				gen
		);
		MethodDeclaration hasRole2 = findOrGeneratePublicMethod(	// boolean hasRole(Object _OT$base_arg, java.lang.Class class_arg)
				teamDecl, booleanBinding, HAS_ROLE,
				new Argument[] {
					gen.argument(_OT_BASE_ARG, gen.singleTypeReference(objectBinding)),
					gen.argument(CLASS_ARG, gen.qualifiedTypeReference(classBinding.compoundName))
				},
				gen
		);
		MethodDeclaration getRole1 = findOrGeneratePublicMethod(	// Object getRole(Object _OT$base_arg)
				teamDecl, objectBinding, GET_ROLE,
				new Argument[] {
						gen2.argument(_OT_BASE_ARG, gen.singleTypeReference(objectBinding))
				},
				gen2
		);
		if (sourceLevel >= ClassFileConstants.JDK1_5) {
			getRole1.annotations = new Annotation[] {
				// report neither potential null access nor unnecessary SuppressWarnings:
				// (first_name is accessed in ctor call for DuplicateRoleException without explicit null-check.)
				gen2.singleStringsMemberAnnotation(TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new char[][]{"all".toCharArray()}) //$NON-NLS-1$
				// Note: would like to say @SuppressWarnings({"null", "suppressWarnings"}).
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252518
			};
		}
		MethodDeclaration getRole2 = findOrGeneratePublicMethod(	// <T> T getRole(Object _OT$base_arg, java.lang.Class<T> class_arg)
				teamDecl, roleTypeRef, GET_ROLE,
				new Argument[] {
					gen.argument(_OT_BASE_ARG, gen.singleTypeReference(objectBinding)),
					gen.argument( // java.lang.Class<T> class_arg
							CLASS_ARG,
							gen.parameterizedQualifiedTypeReference(
								classBinding.compoundName,
								new TypeReference[] { gen.singleTypeReference(T) }
							)
					)
				},
				gen
		);
		if (sourceLevel >= ClassFileConstants.JDK1_5)
			getRole2.typeParameters= new TypeParameter[] { gen.unboundedTypeParameter(T) };
		MethodDeclaration getARoles1 = findOrGeneratePublicMethod(	// Object[] getAllRoles()
				teamDecl, objectArrayBinding, GET_ALL_ROLES,
				null,
				gen);
		MethodDeclaration getARoles2 = findOrGeneratePublicMethod( 	// <T> T[] getAllRoles(Class<T> class_arg)
				teamDecl, roleArrayTypeRef, GET_ALL_ROLES,
				new Argument[] {
						gen.argument( // java.lang.Class<T> class_arg
							CLASS_ARG,
							gen.parameterizedQualifiedTypeReference(
								classBinding.compoundName,
								new TypeReference[] { gen.singleTypeReference(T) }
							)
						)
				},
				gen);
		if (sourceLevel >= ClassFileConstants.JDK1_5)
			getARoles2.typeParameters= new TypeParameter[] { gen.unboundedTypeParameter(T) };
		MethodDeclaration unregRole1 = findOrGeneratePublicMethod(	// void unregisterRole(Object _OT$base_arg)
				teamDecl, TypeBinding.VOID, UNREGISTER_ROLE,
				new Argument[] {
					gen.argument(_OT_ROLE_ARG, gen.singleTypeReference(objectBinding))
				},
				gen2);
		if (sourceLevel >= ClassFileConstants.JDK1_5) {
			unregRole1.annotations = new Annotation[] {
				// found_base is accessed to call remove(role) without explicit null-check. [see also getRole1 above].
				gen2.singleStringsMemberAnnotation(TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new char[][]{"all".toCharArray()}) //$NON-NLS-1$
			};
		}
		MethodDeclaration unregRole2 = findOrGeneratePublicMethod(	// void unregisterRole(Object _OT$base_arg, Class class_arg)
				teamDecl, TypeBinding.VOID, UNREGISTER_ROLE,
				new Argument[] {
					gen.argument(_OT_ROLE_ARG, gen.singleTypeReference(objectBinding)),
					gen.argument(CLASS_ARG, gen.qualifiedTypeReference(classBinding.compoundName))
				},
				gen);

		RoleModel[] roles = teamDecl.getTeamModel().getRoles(false/*no synth ifc*/);
		int h1 = 0; // hasRole1
		int g1 = 0; // getRole1
		int ga = 0; // getAllRoles1
		int g2 = 0; // getAllRoles2
		int m2 = 0; // hasRole2, getRole2
		int u1 = 0; // unregRole1
		int u2 = 0; // unregRole2
		Statement[] hasStats1 = new Statement[roles.length];     // at most this many elements..
		Statement[] hasStats2 = new Statement[roles.length];     // .. compact below.
		Statement[] getStats1 = new Statement[roles.length+2];   // ... (plus 2 locals)
		Statement[] getStats2 = new Statement[roles.length];     // ...
		Statement[] getAStats1 = new Statement[roles.length+1];  // ... (plus 1 local)
		Statement[] getAStats2 = new Statement[roles.length+2];  // ... (plus prefix (1 local) + postfix (1 block))
		Statement[] unregStats1 = new Statement[roles.length+3]; // ... (plus 3 locals)
		Statement[] unregStats2 = new Statement[roles.length];   // ...

		getStats1  [g1++] = gen.localVariable(FIRST_RESULT, objectBinding,     gen.nullLiteral());
		getStats1  [g1++] = gen.localVariable(FIRST_NAME,   stringBinding,     gen.nullLiteral());
		getAStats1 [ga++] = gen.localVariable(FIRST_RESULT, objectCollectionRef, createMTList(gen, objectBinding));
		getAStats2 [g2++] = gen.localVariable(VALUES,       wildcardCollectionRef, gen.nullLiteral());
		unregStats1[u1++] = gen2.localVariable(FIRST_NAME,   stringBinding,     		gen2.nullLiteral());
		unregStats1[u1++] = gen2.localVariable(FIRST_CACHE,  hashMapBinding.erasure(),  gen2.nullLiteral());
		unregStats1[u1++] = gen2.localVariable(FOUND_BASE,   objectBinding,     		gen2.nullLiteral());
		HashSet<String> processedCaches = new HashSet<String>();
		for (int i = 0; i < roles.length; i++) {
			if (roles[i].isSynthInterface()) continue;
			if (TypeAnalyzer.isTopConfined(roles[i].getBinding())) continue;
			if (roles[i].isIgnoreFurtherInvestigation()) continue;
			RoleModel boundRootRole = roles[i].getBoundRootRole();
			if (boundRootRole != null && boundRootRole.isIgnoreFurtherInvestigation()) continue;
			char[] cacheName = LiftingEnvironment.getCacheName(boundRootRole);
			if (cacheName != null) {
				String cacheString = new String(cacheName); // String for hashing!
				ReferenceBinding roleType = roles[i].getInterfacePartBinding();
				if ( !processedCaches.contains(cacheString))
				{
					// one lookup per cache:
					processedCaches.add(cacheString);
					hasStats1  [h1++] = createIfContainsTrue    (          cacheName, gen);
					getStats1  [g1++] = createIfContainsGet     (          cacheName, gen2, g1==3);
					getAStats1 [ga++] = createAddAll            (          cacheName, gen);
					unregStats1[u1++] = createRememberIfContains(roleType, cacheName, gen2, u1==4);
				}
				// one lookup per bound role class:
				hasStats2  [m2]   = createIfTypeEqualAndContains(roleType, cacheName, gen, objectBinding);
				getStats2  [m2++] = createIfTypeEqualAndGet     (roleType, cacheName, gen);
				getAStats2 [g2++] = createIfTypeEqualFetchValues(roleType, cacheName, gen);
				unregStats2[u2++] = createRemove                (roleType, cacheName, gen);
			}
		}
		if (g2 > 1)
			getAStats2[g2++] = createFilterValues(gen);
		else
			getAStats2[g2++] = createThrowNoSuchRole(gen); // no caches to search
		boolean needsAllMethods =    !TypeAnalyzer.isOrgObjectteamsTeam(teamDecl.binding)
								  && !teamDecl.binding.superclass().isTeam();
		if (h1 > 0 || needsAllMethods) {
			System.arraycopy(
					hasStats1, 0,
					hasStats1 = new Statement[h1+1], 0,
					h1);
			System.arraycopy(
					getStats1, 0,
					getStats1 = new Statement[g1+1], 0,
					g1);
			System.arraycopy(
					getAStats1, 0,
					getAStats1 = new Statement[ga+1], 0,
					ga);
			System.arraycopy(
					unregStats1, 0,
					unregStats1 = new Statement[(u1>3) ? (u1+1) : u1], 0,
					u1);
			// no role instance found means: return false:
			hasStats1[h1] = gen.returnStatement(gen.booleanLiteral(false));
			// no duplicate means: return first_result;
			getStats1[g1] = gen.returnStatement(gen.singleNameReference(FIRST_RESULT));
			getAStats1[ga]= gen.returnStatement(
									gen.messageSend(
											gen.singleNameReference(FIRST_RESULT),
											TO_ARRAY,
											null));
			// no duplicate means: if found remove from first_cache;
			if (u1 > 3)
				unregStats1[u1]=createRemoveIfFound(gen2); // if u1 <= 3 this would not be reachable due to definite null
			hasRole1.setStatements(hasStats1);
			getRole1.setStatements(getStats1);
			getARoles1.setStatements(getAStats1);
			unregRole1.setStatements(unregStats1);
			checkedAddMethod(teamDecl, hasRole1);
			checkedAddMethod(teamDecl, getRole1);
			checkedAddMethod(teamDecl, getARoles1);
			checkedAddMethod(teamDecl, unregRole1);
		}
		if (m2 > 0 || needsAllMethods) {
			System.arraycopy(
					hasStats2, 0,
					hasStats2 = new Statement[m2+1], 0,
					m2);
			System.arraycopy(
					getStats2, 0,
					getStats2 = new Statement[m2+1], 0,
					m2);
			System.arraycopy(
					unregStats2, 0,
					unregStats2 = new Statement[u2+1], 0,
					u2);
			// role class not found means: illegal argument:
			hasStats2[m2]   = createThrowNoSuchRole(gen);
			getStats2[m2]   = createThrowNoSuchRole(gen);
			unregStats2[u2] = createThrowNoSuchRole(gen);
			hasRole2.setStatements(hasStats2);
			getRole2.setStatements(getStats2);
			unregRole2.setStatements(unregStats2);
			checkedAddMethod(teamDecl, hasRole2);
			checkedAddMethod(teamDecl, getRole2);
			checkedAddMethod(teamDecl, unregRole2);
		}
		if (g2 > 2 || needsAllMethods) {
			System.arraycopy(
					getAStats2, 0,
					getAStats2 = new Statement[g2], 0,
					g2);
			getARoles2.setStatements(getAStats2);
			checkedAddMethod(teamDecl, getARoles2);
		}
	}

	private static Expression createMTList(AstGenerator gen, TypeBinding elemBinding) {
		return gen.allocation(
					gen.sourceLevel >= ClassFileConstants.JDK1_5 ?
							gen.parameterizedQualifiedTypeReference(
									ARRAY_LIST, new TypeBinding[]{elemBinding}, true/*deeply generic*/) :
							gen.qualifiedTypeReference(ARRAY_LIST),
					null);
	}

	//
	private static Statement createIfContainsTrue(char[] cacheName, AstGenerator gen)
	{
		/*
		 * for each cache (by cacheName) generate:
		 * 		if (<cacheName>.containsKey(_OT$base_arg))
		 * 			return true;
		 */
		return gen.ifStatement(
					gen.messageSend(
							gen.fieldReference(gen.thisReference(), cacheName),
							CONTAINS_KEY,
							new Expression[] { gen.singleNameReference(_OT_BASE_ARG) }
					),
					gen.block(new Statement[] {
							gen.returnStatement(gen.booleanLiteral(true))
					})
				);
	}

	private static Statement createAddAll(char[] cacheName, AstGenerator gen)
	{
		/*
		 * for each cache (by cacheName) generate:
		 * 		<first_result>.addAll(<cacheName>.values());
		 */
		return gen.messageSend(
					gen.singleNameReference(FIRST_RESULT),
					ADD_ALL,
					new Expression[] {
						gen.messageSend(
							gen.fieldReference(gen.thisReference(), cacheName),
							VALUES,
							null
						)
					}
				);
	}

	private static Statement createIfContainsGet(
			char[] cacheName, AstGenerator gen, boolean isFirstStat)
	{
		/*
		 * for each cache (by cacheName) generate:
		 * 		if (<cacheName>.containsKey(_OT$base_arg)) {
		 * 			if (first_result == null) {
		 * 				first_result = <cacheName>.get(_OT$base_arg);
		 * 				first_name = <cacheName>;
		 *          } else {
		 * 			    throw new DuplicateRoleException(<roleName1>, <roleName2>);
		 *          }
		 *      }
		 * where both roleNames are derived from the respective cache name.
		 * Omit the inner if, if this is the first statement in the method
		 * (take only the inner then part).
		 */
		Block innerThen = gen.block(new Statement[] {
					gen.assignment(
						gen.singleNameReference(FIRST_RESULT),
						gen.messageSend(
							gen.fieldReference(gen.thisReference(),cacheName),
							GET,
							new Expression[] {
								gen.singleNameReference(_OT_BASE_ARG)
							}
						)
					),
					gen.assignment(
						gen.singleNameReference(FIRST_NAME),
						gen.stringLiteral(cacheName)
					)
				});
		return gen.ifStatement(
					gen.messageSend(
							gen.fieldReference(gen.thisReference(), cacheName),
							CONTAINS_KEY,
							new Expression[] { gen.singleNameReference(_OT_BASE_ARG) }
					),
					isFirstStat ?
						innerThen :
						gen.block(new Statement[] {
							gen.ifStatement(
								new EqualExpression(
									gen.singleNameReference(FIRST_RESULT),
									gen.nullLiteral(),
									OperatorIds.EQUAL_EQUAL
								),
								innerThen,
								gen.block(new Statement[] {
									createThrowDuplicate(cacheName, gen)
								})
							)
						})
				);
	}

	private static Statement createRememberIfContains(
			ReferenceBinding roleType, char[] cacheName, AstGenerator gen, boolean isFirstStat)
	{
		/*
		 * for each cache (by cacheName) generate:
		 * 		if (_OT$role_arg instanceof <roleType>)
		 *      {
		 *			found_base = ((<roleType>)_OT$role_arg)._OT$getBase();
		 * 			if (<cacheName>.containsKey(found_base))
		 *   	    {
		 * 				if (first_result == null) {
		 * 					first_cache = <cacheName>;
		 * 					first_name = "<cacheName>";
		 *          	} else {
		 * 			    	throw new DuplicateRoleException(<roleName1>, <roleName2>);
		 *          	}
		 * 			}
		 *      }
		 * where both roleNames are derived from the respective cache name.
		 * Omit the inner if, if this is the first statement in the method
		 * (take only the inner then part).
		 */
		Block innerThen = gen.block(new Statement[] {
					gen.assignment(
						gen.singleNameReference(  FIRST_CACHE),
						gen.fieldReference(gen.thisReference(),cacheName)
					),
					gen.assignment(
						gen.singleNameReference(FIRST_NAME),
						gen.stringLiteral(cacheName)
					),
				});
		return gen.ifStatement(
					gen.instanceOfExpression(
					    gen.singleNameReference(_OT_ROLE_ARG),
						gen.singleTypeReference(roleType)
					),
					gen.block(new Statement[] {
						gen.assignment(
							gen.singleNameReference(FOUND_BASE),
							gen.messageSend(
								gen.castExpression(
										gen.singleNameReference(_OT_ROLE_ARG),
										gen.singleTypeReference(roleType),
										CastExpression.RAW),
								_OT_GETBASE,
								new Expression[0]
							)
						),
						gen.ifStatement(
							gen.messageSend(
								gen.fieldReference(gen.thisReference(), cacheName),
								CONTAINS_KEY,
								new Expression[] { gen.singleNameReference(FOUND_BASE) }
							),
							isFirstStat ?
								innerThen :
								gen.block(new Statement[] {
									gen.ifStatement(
										new EqualExpression(
											gen.singleNameReference(FIRST_CACHE),
											gen.nullLiteral(),
											OperatorIds.EQUAL_EQUAL
										),
										innerThen,
										gen.block(new Statement[] {
											createThrowDuplicate(cacheName, gen)
										})
									)
								})
						)
					})
				);
	}

	private static Statement createRemoveIfFound (AstGenerator gen)
	{
		/*
		 * For the end of unregisterRole(Object) create:
		 * 		if (first_cache != null) {
		 * 			first_cache.remove(_OT$base_arg);
		 *          ((IBoundBase)found_base)._OT$removeRole(_OT$role_arg);
		 *      }
		 */
		return gen.ifStatement(
					new EqualExpression(
						gen.singleNameReference(FIRST_CACHE),
						gen.nullLiteral(),
						OperatorIds.EQUAL_EQUAL
					),
					gen.block(null),
					gen.block(new Statement[] { // "else" instead of negation
						gen.messageSend(
							gen.singleNameReference(FIRST_CACHE),
							REMOVE,
							new Expression[] { gen.singleNameReference(FOUND_BASE) }
						),
						// OTDYN: Slightly different methods depending on the weaving strategy:
						CallinImplementorDyn.DYNAMIC_WEAVING
						? gen.messageSend(
							gen.castExpression(
								gen.singleNameReference(FOUND_BASE),
								gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
								CastExpression.RAW),
							ADD_REMOVE_ROLE,
							new Expression[] {gen.singleNameReference(_OT_ROLE_ARG), 
											  gen.booleanLiteral(false)}) // isAdding=false
						: gen.messageSend(
							gen.castExpression(
								gen.singleNameReference(FOUND_BASE),
								gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
								CastExpression.RAW),
							REMOVE_ROLE,
							new Expression[] {gen.singleNameReference(_OT_ROLE_ARG)})
					})
				);
	}


	private static Statement createIfTypeEqualAndContains(
			ReferenceBinding roleType, char[] cacheName, AstGenerator gen, TypeBinding objectBinding)
	{
		/*
		 * for each bound roleType generate:
		 *      if (class_arg == <roleType>.class)) {
		 *          if (class_arg.getName().endsWith("_OT__<cacheName.tail>"))
		 *      		return <cacheName>.containsKey(_OT$base_arg);
		 *          else
		 *              // class_arg is more specific than the root of the cache,
		 * 			    // need an additional instanceof check:
		 * 				return class_arg.isInstance(<cacheName>.get(_OT$base_arg));
		 * 		}
		 */
		return gen.ifStatement(
					// (class_arg == <roleType>.class)
					new EqualExpression(
							gen.singleNameReference(CLASS_ARG),
							gen.classLiteralAccess(gen.singleTypeReference(roleType)),
							OperatorIds.EQUAL_EQUAL
					),
					gen.block(new Statement[] {
						gen.ifStatement(
							// (class_arg.getName().endsWith("<cacheName.tail>"))
							gen.messageSend(
								gen.messageSend(
									gen.singleNameReference(CLASS_ARG),
									GET_NAME,
									new Expression[0]
								),
								ENDS_WITH,
								new Expression[] { gen.stringLiteral(
									CharOperation.concat(
											OT_DELIM_NAME,
											CharOperation.subarray(cacheName, CACHE_PREFIX.length, -1)))
								}
							),
							gen.block(new Statement[] {
							    gen.returnStatement(
							    	// <cacheName>.containsKey(_OT$base_arg)
									gen.messageSend(
										gen.fieldReference(gen.thisReference(), cacheName),
										CONTAINS_KEY,
										new Expression[] { gen.singleNameReference(_OT_BASE_ARG) }
									)
								),
							}),
							gen.block(new Statement[] {
							    gen.returnStatement(
							    	// class_arg.isInstance(<cacheName>.get(_OT$base_arg))
									gen.messageSend(
										gen.singleNameReference(CLASS_ARG),
										IS_INSTANCE,
										new Expression[] {
											gen.messageSend(
												gen.fieldReference(gen.thisReference(), cacheName),
												GET,
												new Expression[] { gen.singleNameReference(_OT_BASE_ARG) },
												objectBinding // pretend to return object even if role is confined (avoid lowering)
											)
										}
									)
								)
							})
						)
					})
				);
	}

	private static Statement createIfTypeEqualAndGet(
			ReferenceBinding roleType, char[] cacheName,AstGenerator gen)
	{
		/*
		 * for each bound roleType generate:
		 *      if (class_arg == <roleType>.class)) {
		 *      	return (T)<cacheName>.getKey(_OT$base_arg);
		 * 		}
		 */
		Expression messageSendGet = gen.messageSend(
				gen.fieldReference(gen.thisReference(), cacheName),
				GET,
				new Expression[] { gen.singleNameReference(_OT_BASE_ARG) }
		);
		if (gen.sourceLevel >= ClassFileConstants.JDK1_5)
			messageSendGet= gen.castExpression(
								messageSendGet,
								gen.singleNameReference(T),
								CastExpression.RAW
							);
		return gen.ifStatement(
					new EqualExpression(
							gen.singleNameReference(CLASS_ARG),
							gen.classLiteralAccess(gen.singleTypeReference(roleType)),
							OperatorIds.EQUAL_EQUAL
					),
					gen.block(new Statement[] {
					    gen.returnStatement(messageSendGet),
					})
				);
	}


	private static Statement createIfTypeEqualFetchValues(
			ReferenceBinding roleType, char[] cacheName,AstGenerator gen)
	{
		/*
		 * for each bound roleType generate:
		 *      if (class_arg == <roleType>.class)) {
		 *      	values= <cacheName>.values();
		 *      }
		 */
		return gen.ifStatement(
					new EqualExpression(
						gen.castExpression(
							gen.singleNameReference(CLASS_ARG),
							gen.singleNameReference(OBJECT), CastExpression.RAW
						),
						gen.castExpression(
							gen.classLiteralAccess(gen.singleTypeReference(roleType)),
							gen.singleNameReference(OBJECT), CastExpression.RAW
						),
						OperatorIds.EQUAL_EQUAL
					),
					gen.block(new Statement[] {
							gen.assignment(
								gen.singleNameReference(VALUES),
								gen.messageSend(
									gen.fieldReference(gen.thisReference(), cacheName),
									VALUES,
									null
								)
					        )
					})
			   );
	}

	private static Statement createFilterValues(AstGenerator gen)
	{
		/*
		 * once create (after checking for all roles):
		 *   {
		 *      if (values == null)
		 *          throw new IllegalArgumentException("No such bound role type in this team: "+<class_arg>.getName());
		 * 	    ArrayList<Object> result= new ArrayList<Object>(values.size());
		 *      for (Object o : values) {
		 *        	if (class_arg.isInstance(o))
		 *              result.add(o);
		 *      }
		 *    	return result.toArray((T[])java.lang.reflect.Array.newInstance(class_arg, result.size());
		 *   }
		 */
		TypeReference arrayList= gen.parameterizedQualifiedTypeReference(
				ARRAY_LIST,
				new TypeReference[] { gen.singleTypeReference(OBJECT) }
		);
		char[] loopVariable = "o".toCharArray(); //$NON-NLS-1$
		return gen.block(new Statement[] {
						gen.ifStatement(
							gen.nullCheck(gen.singleNameReference(VALUES)),
							createThrowNoSuchRole(gen)
						),
						gen.localVariable(RESULT, arrayList,
							gen.allocation(
								AstClone.copyTypeReference(arrayList),
									new Expression[] {
										gen.messageSend(
											gen.singleNameReference(VALUES),
											SIZE,
											null
										)
									}
							)
						),
						gen.foreach(
							gen.localVariable(loopVariable, OBJECT, null),
							gen.singleNameReference(VALUES),
							gen.ifStatement(
								gen.messageSend(gen.singleNameReference(CLASS_ARG), IS_INSTANCE,
										        new Expression[] {gen.singleNameReference(loopVariable)}
								),
								gen.messageSend(gen.singleNameReference(RESULT), ADD,
												new Expression[] {gen.singleNameReference(loopVariable)}
								)
							)
						),
					    gen.returnStatement(
					    	gen.messageSend(gen.singleNameReference(RESULT), TO_ARRAY,
					    					new Expression[] {
					    						gen.castExpression(
							    					gen.messageSend(
							    						gen.qualifiedNameReference(ARRAY),
							    						NEW_INSTANCE,
							    						new Expression[] {
							    							gen.singleNameReference(CLASS_ARG),
							    							gen.messageSend(gen.singleNameReference(RESULT), SIZE, null),
							    						}
							    					),
							    					(gen.sourceLevel >= ClassFileConstants.JDK1_5)
							    						? gen.arrayTypeReference(T, 1)
							    						: gen.arrayTypeReference(OBJECT, 1),
							    					CastExpression.RAW
							    				)
					    	                }
					    	)
						),
					});
	}


	private static Statement createRemove(
			ReferenceBinding roleType, char[] cacheName, AstGenerator gen)
	{
		/*
		 * for each bound role create:
		 * 		if (class_arg == <roleType>.class) {
		 *          IBoundBase base_obj = ((<roleType>)_OT$role_arg)._OT$getBase();
		 * 			<cacheName>.remove(base_obj);
		 *          ((IBoundBase)base_obj)._OT$removeRole(_OT$role_arg);
		 *          return;
		 *      }
		 */
		return gen.ifStatement(
				new EqualExpression(
						gen.singleNameReference(CLASS_ARG),
						gen.classLiteralAccess(gen.singleTypeReference(roleType)),
						OperatorIds.EQUAL_EQUAL
				),
				gen.block(new Statement[] {
					gen.localVariable(
						BASE_OBJ,
						gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
						gen.messageSend(
							gen.castExpression(
									gen.singleNameReference(_OT_ROLE_ARG),
									gen.singleTypeReference(roleType),
									CastExpression.RAW
								),
								_OT_GETBASE,
								new Expression[0]
							)),
					gen.messageSend(
						gen.fieldReference(gen.thisReference(), cacheName),
						REMOVE,
						new Expression[] {
						    gen.singleNameReference(BASE_OBJ)
						}
					),
					// OTDYN: Slightly different methods depending on the weaving strategy:
					CallinImplementorDyn.DYNAMIC_WEAVING
					? gen.messageSend(
						gen.castExpression(
							gen.singleNameReference(BASE_OBJ),
							gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
							CastExpression.RAW),
						ADD_REMOVE_ROLE,
						new Expression[] {gen.singleNameReference(_OT_ROLE_ARG), 
										  gen.booleanLiteral(false)}) // isAdding=false
					: gen.messageSend(
						gen.castExpression(
							gen.singleNameReference(BASE_OBJ),
							gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
							CastExpression.RAW),
						REMOVE_ROLE,
						new Expression[] {gen.singleNameReference(_OT_ROLE_ARG)}),
					gen.returnStatement(null)
				})
			);
	}


	private static Statement createThrowNoSuchRole(AstGenerator gen)
	{
		/*
		 * throw new IllegalArgumentException("No such role type in this team: "+class_arg.getName());
		 */
		return gen.throwStatement(
					gen.allocation(
						gen.qualifiedTypeReference(ILLEGAL_ARGUMENT_EXCEPTION),
						new Expression[] {
							new BinaryExpression(
								gen.stringLiteral("No such bound role type in this team: ".toCharArray()), //$NON-NLS-1$
								gen.messageSend(
										gen.singleNameReference(CLASS_ARG),
										GET_NAME,
										new Expression[0]),
								OperatorIds.PLUS
							)
						}
					)
				);
	}


	private static Statement createThrowDuplicate(char[] secondName, AstGenerator gen)
	{
		/*
		 * throw new DuplicateRoleException(roleName1, roleName2);
		 */
		return gen.throwStatement(
					gen.allocation(
						gen.qualifiedTypeReference(ORG_OBJECTTEAMS_DUPLICATE_ROLE),
						new Expression[] {
							gen.messageSend(
								gen.singleNameReference(FIRST_NAME),
								SUBSTRING,
								new Expression[]{
									gen.intLiteral(CACHE_PREFIX.length),
								}
							),
							gen.stringLiteral(CharOperation.subarray(
									secondName, CACHE_PREFIX.length, -1))
						}
					)
				);
	}

	private static MethodDeclaration findOrGeneratePublicMethod(
			TypeDeclaration   teamDecl,
			TypeBinding       returnType,
			char[]            methodName,
			Argument[]        arguments,
			AstGenerator      gen)
	{
		MethodDeclaration foundMethod =
			AstConverter.findAndAdjustCopiedMethod(teamDecl, methodName, arguments);
		if (foundMethod != null)
			return foundMethod;

		return gen.method(teamDecl.compilationResult, AccPublic, returnType, methodName, arguments);
	}

	private static MethodDeclaration findOrGeneratePublicMethod(
			TypeDeclaration   teamDecl,
			TypeReference     returnType,
			char[]            methodName,
			Argument[]        arguments,
			AstGenerator      gen)
	{
		MethodDeclaration foundMethod =
			AstConverter.findAndAdjustCopiedMethod(teamDecl, methodName, arguments);
		if (foundMethod != null)
			return foundMethod;

		return gen.method(teamDecl.compilationResult, AccPublic, returnType, methodName, arguments);
	}

	private static void checkedAddMethod(TypeDeclaration teamDecl, MethodDeclaration methodDecl)
	{
		AbstractMethodDeclaration foundMethod = TypeAnalyzer.findMethodDecl(
				teamDecl,
				methodDecl.selector,
				methodDecl.arguments == null ?
						0 :
						methodDecl.arguments.length
		);
		if (foundMethod == methodDecl)
			return;
		AstEdit.addGeneratedMethod(teamDecl, methodDecl);
	}

	public static boolean isReflectionMethod(MethodBinding method) {
		int nParams = method.parameters.length;
		if (CharOperation.equals(method.selector, HAS_ROLE))
			return (nParams == 1) || (nParams == 2);
		if (CharOperation.equals(method.selector, GET_ROLE))
			return (nParams == 1) || (nParams == 2);
		if (CharOperation.equals(method.selector, GET_ALL_ROLES))
			return (nParams == 0) || (nParams == 1);
		if (CharOperation.equals(method.selector, UNREGISTER_ROLE))
			return (nParams == 1) || (nParams == 2);
		return false;
	}

}
