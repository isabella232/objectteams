/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2008 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleMigrationImplementor.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynchronized;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.LiftingEnvironment;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.TreeNode;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.TeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.RoleTypeCreator;

/**
 * Implements everything related to ITeamMigratable and IBaseMigratable.
 * Things happen in three phases:
 * (1) check "implements" declaration,
 * (2) check invocations of generated methods
 * (3) actually generate the method (empty ast, instructions are binary only)
 *
 * @author stephan
 * @since 1.2.5
 */
public class RoleMigrationImplementor
{
	static final String TEAM = "Team"; //$NON-NLS-1$
	static final String BASE = "Base"; //$NON-NLS-1$
	static final char[] TYPEPARAM = "_OT$param$".toCharArray(); //$NON-NLS-1$
	static final char[][] JAVA_LANG_NULLPOINTEREXCEPTION = new char[][]{"java".toCharArray(), "lang".toCharArray(), "NullPointerException".toCharArray()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


	/**
	 * PHASE 1: check if <code>implements IXXXMigratable</code> is declared, and if so, check if this declaration is legal.
	 *
	 * @param sourceType		current (role?) type
	 * @param superInterfaceRef implements reference
	 * @param superInterface    resolved superinterface type
	 * @param scope				class scope of current type
	 * @return false if an IXXXMigratable declaration was found but detected to be illegal.
	 */
	public static boolean checkMigratableInterfaces(SourceTypeBinding sourceType, TypeReference superInterfaceRef, ReferenceBinding superInterface, ClassScope scope)
	{
		if (CharOperation.equals(superInterface.compoundName, IOTConstants.ORG_OBJECTTEAMS_ITEAMMIGRATABLE)) {
			if (!sourceType.isRole()) {
				scope.problemReporter().migrateNonRole(superInterfaceRef, sourceType);
				return false;
			} else if (scope.referenceContext.baseclass != null) {
				scope.problemReporter().migrateBoundRole(superInterfaceRef, sourceType);
				return false;
			} else if (!sourceType.enclosingType().isFinal()) {
				scope.problemReporter().migrateWithinNonFinalTeam(superInterfaceRef, sourceType.enclosingType());
				return false;
			}
		}
		if (CharOperation.equals(superInterface.compoundName, IOTConstants.ORG_OBJECTTEAMS_IBASEMIGRATABLE)) {
			if (!sourceType.isRole()) {
				scope.problemReporter().baseMigrateNonRole(superInterfaceRef, sourceType);
				return false;
			} else if (scope.referenceContext.baseclass == null) {
				scope.problemReporter().baseMigrateUnboundRole(superInterfaceRef, sourceType);
				return false;
			}
		}
		return true;
	}

	/**
	 * PHASE 2: Type checking for invocations of migrateToXXX().
	 *
	 * @param originalMethod method before type parameter substitution
	 * @param arguments		 actual argument types of the current invocation
	 * @param substitutes    type variable bindings
	 * @param scope
	 * @param invocationSite
	 * @return null if not a migrate-method call, otherwise (a) a problem method binding (ProblemAlreadyReported) or (b) a valid method substitute
	 */
	public static MethodBinding getMigrateMethodSubstitute(MethodBinding  originalMethod,
														   TypeBinding[]  arguments,
														   TypeBinding[]  substitutes,
														   Scope          scope,
														   InvocationSite invocationSite)
	{
		if (!(invocationSite instanceof MessageSend))
			return null;
		MessageSend send = (MessageSend)invocationSite;
		if (!(send.actualReceiverType instanceof ReferenceBinding))
			return null;

		ReferenceBinding roleType = ((ReferenceBinding)send.actualReceiverType).getRealType();
		TypeBinding typeArgument = null;
		boolean haveReportedError = false;

		if (CharOperation.equals(originalMethod.selector, IOTConstants.MIGRATE_TO_TEAM))
		{
			Expression sendArgument = send.arguments[0];
			ITeamAnchor anchorBinding = TeamAnchor.getTeamAnchor(sendArgument);
			if (anchorBinding == null) {
				scope.problemReporter().migrateToNonTeam(sendArgument);
				haveReportedError = true;
			} else {
				ReferenceBinding anchorType = (ReferenceBinding) anchorBinding.getResolvedType();
				if (anchorType.getRealClass() != roleType.enclosingType()) {
					scope.problemReporter().migrateToWrongTeam(sendArgument, anchorType, roleType);
					haveReportedError = true;
				}
			}
			if (!haveReportedError)
				typeArgument = RoleTypeCreator.getAnchoredType(scope, send, anchorBinding, roleType, null, 0); // FIXME(SH): type parameters
		}
		else if (CharOperation.equals(originalMethod.selector, IOTConstants.MIGRATE_TO_BASE))
		{
			TypeBinding baseType = arguments[0];
			if (!baseType.isCompatibleWith(roleType.baseclass())) {
				scope.problemReporter().migrateToWrongBase(send.arguments[0], baseType, roleType, roleType.baseclass());
				haveReportedError = true;
			}

			typeArgument = baseType;
		} else {
			return null;
		}
		if (haveReportedError)
			return new ProblemMethodBinding(originalMethod, originalMethod.selector, substitutes, ProblemReasons.ProblemAlreadyReported);
		// substitution was successful
		return new ParameterizedGenericMethodBinding(originalMethod, new TypeBinding[]{typeArgument}, scope.environment());
	}


	/**
	 * PHASE 3 (Team):<br/>
	 * Implement <code>&lt;R&gt; R migrateToTeam(ITeam otherTeam);</code>
	 */
	public static void addMigrateToTeamMethod(TypeDeclaration roleClassDecl) {
		AstGenerator gen = new AstGenerator(roleClassDecl.sourceStart, roleClassDecl.sourceEnd);
		doAddMigrateMethod(roleClassDecl,
						   IOTConstants.MIGRATE_TO_TEAM,
						   gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_ITEAM),
						   new SingleTypeReference (RoleMigrationImplementor.TYPEPARAM, gen.pos) {
								@Override /** faked resolving: always use the declaring type as return type. */
								public TypeBinding resolveType(ClassScope scope) {
									return this.resolvedType = scope.enclosingSourceType();
								}
						   },
						   TEAM,
						   null); // cacheName
	}

	/**
	 * PHASE 3 (Base):<br/>
	 * Check if role is bound and base field is not-final
	 * (as setup by {@link StandardElementGenerator#checkCreateBaseField(TypeDeclaration, ReferenceBinding, boolean)}).<br/>
	 * If so generate <code>&lt;B&gt; void migrateToBase(B otherBase);</code>
	 *
	 * @param roleClassDecl
	 * @param node          node from role hierarchy, used for determining the appropriate cache name.
	 */
	public static void checkAddMigrateToBaseMethod(TypeDeclaration roleClassDecl, TreeNode node) {
		AstGenerator gen = new AstGenerator(roleClassDecl.sourceStart, roleClassDecl.sourceEnd);
		FieldBinding baseField = roleClassDecl.scope.getField(roleClassDecl.binding, IOTConstants._OT_BASE, gen.singleNameReference(IOTConstants._OT_BASE));
		if (baseField == null) {
			assert !node.getTreeObject().isBound() : "bound role must have base field added"; //$NON-NLS-1$
			return;
		}
		if (baseField.isFinal())
			return;
		char[] cacheName = LiftingEnvironment.getCacheName(node.getTopmostBoundParent(true).getTreeObject());

		doAddMigrateMethod(roleClassDecl,
						   IOTConstants.MIGRATE_TO_BASE,
						   gen.singleTypeReference(TYPEPARAM),
						   gen.singleTypeReference(TypeConstants.VOID),
						   BASE,
						   cacheName);
	}

	/* Common implementation for both migrate methods. */
	private static void doAddMigrateMethod(TypeDeclaration roleClassDecl,
										   char[] 		   selector,
										   TypeReference   argumentTypeRef,
										   TypeReference   returnTypeRef,
										   final String    kind,
										   final char[]    cacheName)
	{
		AstGenerator gen = new AstGenerator(roleClassDecl.sourceStart, roleClassDecl.sourceEnd);
		MethodDeclaration migrate = new MethodDeclaration(roleClassDecl.compilationResult)
		{
			@Override
			protected void endOfMethodHook(ClassFile classfile) {
				// common code for both variants:
				CodeStream codeStream = classfile.codeStream;
				// if (otherTeam == null)
				BranchLabel goOn = new BranchLabel(codeStream);
				codeStream.aload_1(); //otherTeam / otherBase
				codeStream.ifnonnull(goOn);
				// { throw new NullPointerException("Team/base argument must not be null"); }
				ReferenceBinding npeBinding = (ReferenceBinding)this.scope.getType(JAVA_LANG_NULLPOINTEREXCEPTION, 3);
				codeStream.new_(npeBinding);
				codeStream.dup();
				MethodBinding npeStringCtor = getStringArgCtor(npeBinding);
				if (npeStringCtor == null) throw new InternalCompilerError("Expected constructor NullPointerException.<init>(String) not found"); //$NON-NLS-1$
				codeStream.ldc(kind+" argument must not be null"); //$NON-NLS-1$
				codeStream.invoke(Opcodes.OPC_invokespecial, npeStringCtor, npeBinding);
				codeStream.athrow();
				goOn.place();

				// specific code:
				if (kind == TEAM)
					genMigrateToTeamInstructions(codeStream, this.scope.enclosingSourceType());
				else
					genMigrateToBaseInstructions(codeStream, this.scope.enclosingSourceType(), this.scope, cacheName);
			}
			private MethodBinding getStringArgCtor(ReferenceBinding npeBinding) {
				MethodBinding[] ctors = npeBinding.getMethods(TypeConstants.INIT);
				for (MethodBinding ctor : ctors) {
					if (ctor.parameters.length == 1 && ctor.parameters[0].id == TypeIds.T_JavaLangString)
						return ctor;
				}
				return null;
			}
			@Override
			public void analyseCode(ClassScope classScope, InitializationFlowContext initializationContext, FlowInfo flowInfo) {
				// noop
			}
		};
		gen.setMethodPositions(migrate);
		migrate.isGenerated = true;

		migrate.modifiers  = AccPublic | AccSynchronized;
		migrate.typeParameters = new TypeParameter[] {gen.unboundedTypeParameter(RoleMigrationImplementor.TYPEPARAM)};
		migrate.returnType = returnTypeRef;
		migrate.selector   = selector;
		migrate.arguments  = new Argument[] { gen.argument(("other"+kind).toCharArray(), argumentTypeRef) }; //$NON-NLS-1$
		migrate.statements = new Statement[0];
		migrate.hasParsedStatements = true;
		AstEdit.addMethod(roleClassDecl, migrate);
	}


	private static void genMigrateToTeamInstructions(CodeStream codeStream, SourceTypeBinding roleBinding) {
		// this.this$n = (MyTeam)otherTeam
		codeStream.aload_0(); // this
		codeStream.aload_1(); // otherTeam
		codeStream.checkcast(roleBinding.enclosingType());
		codeStream.fieldAccess(Opcodes.OPC_putfield, enclosingInstanceField(roleBinding), roleBinding);
		codeStream.aload_0();
		codeStream.areturn();
		// not handling caches here, cf. IProblem.MigrateBoundRole
	}


	private static void genMigrateToBaseInstructions(CodeStream codeStream, SourceTypeBinding roleBinding, Scope scope, char[] cacheName)
	{
		FieldBinding baseField = roleBinding.getField(IOTConstants._OT_BASE, true);
		// accessing the cache (using remove() and put()):
		ReferenceBinding cacheTypeBinding = (ReferenceBinding) scope.getType(IOTConstants.WEAK_HASH_MAP, 3);
		MethodBinding remove = cacheTypeBinding.getMethod(scope, "remove".toCharArray()); //$NON-NLS-1$
		MethodBinding put    = cacheTypeBinding.getMethod(scope, "put".toCharArray()); //$NON-NLS-1$
		// accessing the base object (using _OT$removeRole() and _OT$addRole()):
		ReferenceBinding iboundBase = (ReferenceBinding) scope.getType(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE, 3);

		// remove old from cache
		codeStream.aload_0();														// this
		codeStream.fieldAccess(Opcodes.OPC_getfield,
							   enclosingInstanceField(roleBinding), 				// this.this$n
							   roleBinding);					
		codeStream.fieldAccess(Opcodes.OPC_getfield,
							   roleBinding.enclosingType().getField(cacheName, true),// this.this$n._OT$cache$R
							   roleBinding.enclosingType());
		codeStream.dup(); // for use in put() below
		codeStream.aload_0();														// this
		codeStream.fieldAccess(Opcodes.OPC_getfield,
							   baseField,											// this._OT$base
							   roleBinding);
		codeStream.dup(); // share for nested method call			// this._OT$base
			// remove role from this (old) base
			genAddOrRemoveRole(codeStream, scope, iboundBase, false);// -> void		// -> base._OT$removeRole(this)
		codeStream.invoke(Opcodes.OPC_invokevirtual,
						  remove,													// -> cache.remove(base)
						  cacheTypeBinding);
		codeStream.pop(); // discard result

		// this._OT$base = (MyBase)otherBase
		codeStream.aload_0(); // this
		codeStream.aload_1(); // otherBase
		codeStream.checkcast(roleBinding.baseclass());
		codeStream.fieldAccess(Opcodes.OPC_putfield, baseField, roleBinding);

		// add new to cache (cache is still on the stack)
		codeStream.aload_1(); // otherBase
		codeStream.aload_0(); // this (role)
		codeStream.invoke(Opcodes.OPC_invokevirtual, put, cacheTypeBinding);

		// add to new base:
		codeStream.aload_1(); // otherBase
		genAddOrRemoveRole(codeStream, scope, iboundBase, true);	// -> void		// -> base._OT$addRemoveRole(this, false)

		codeStream.return_();
	}

	// pre: call target (base) is on stack
	static void genAddOrRemoveRole(CodeStream codeStream, Scope scope, ReferenceBinding iboundBase, boolean isAdding) {
		codeStream.aload_0(); // this
		// OTDYN: Slightly different methods depending on the weaving strategy:
		if (CallinImplementorDyn.DYNAMIC_WEAVING) {
			// _OT$addOrRemoveRole(role, isAdding)
			if (isAdding) 
				codeStream.iconst_1();  // isAdding=true
			else
				codeStream.iconst_0(); 	// isAdding=false	
			codeStream.invoke(Opcodes.OPC_invokeinterface,
						  iboundBase.getMethod(scope, IOTConstants.ADD_REMOVE_ROLE),
						  iboundBase);
		} else {
			// _OT$addRole(role) or _OT$removeRole(role):
			codeStream.invoke(Opcodes.OPC_invokeinterface, 
							  isAdding 
							  		? iboundBase.getMethod(scope, IOTConstants.ADD_ROLE) 
							  		: iboundBase.getMethod(scope, IOTConstants.REMOVE_ROLE), 
							  iboundBase);
		}
	}

	private static FieldBinding enclosingInstanceField(SourceTypeBinding roleBinding) {
		return roleBinding.getSyntheticField(roleBinding.enclosingType(), true);
	}
}
