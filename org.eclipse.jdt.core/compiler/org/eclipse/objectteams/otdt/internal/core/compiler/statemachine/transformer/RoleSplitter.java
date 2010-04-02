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
 * $Id: RoleSplitter.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccAbstract;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccFinal;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPrivate;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynthetic;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccSemicolonBody;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DELIM_LEN;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.OT_DELIM_NAME;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnKeyword;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedTypeReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TypeModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.ProblemDetail;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstClone;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;


/**
 * MIGRATION_STATE: complete.
 *
 * This transformer splits each role class into a class part and an interface part.
 *
 * Entries for STATE_ROLES_SPLIT:
 * + createInterfacePart
 * + transformClassPart
 *
 * Entry for STATE_ROLES_LINKED (directly from Dependencies.ensureRoleState()):
 * + linkScopes
 * + linkSuperAndBaseInIfc
 *
 * @author Markus Witte
 * @version $Id: RoleSplitter.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleSplitter
{

	/**
	 * createInterfacePart:
     * + create a type declaration
     * + copy field and method declarations
     * + copy the "playedBy" relation (baseclass).
     * Not yet connecting tsuper-interfaces (see addImplementsInterfaceReference)
     * @param teamDeclaration      enclosing team
     * @param roleClassDeclaration role class being split.
     */
	public static TypeDeclaration createInterfacePart(
            TypeDeclaration       teamDeclaration,
            TypeDeclaration roleClassDeclaration)
    {
		TypeDeclaration interfaceDeclaration = AstConverter.genRoleInterface(teamDeclaration, roleClassDeclaration);
		// rename role class before adding interface to avoid name collision:
		renameClass(roleClassDeclaration);
		AstEdit.addMemberTypeDeclaration(teamDeclaration, interfaceDeclaration);
		createInterfaceStatements(
                teamDeclaration,
                interfaceDeclaration,
                roleClassDeclaration);
		interfaceDeclaration.baseclass = AstClone.copyTypeReference(roleClassDeclaration.baseclass);
        // Note: superclass is not copied here, but in setupInterfaceForExtends,
        //       because we need to know whether superclass is a role or not
        //       (see setupInterfaceForExtends() for details.)

		if (   (roleClassDeclaration.bits & ASTNode.IsLocalType) != 0
			&& roleClassDeclaration.scope != null)
		{
			((BlockScope)roleClassDeclaration.scope.parent).addLocalType(interfaceDeclaration);
		}

		return interfaceDeclaration;
	}



	/**
	* createInterfaceStatements
    * add to the interface:
    * + static final fields (move)
    * + method declarations (copy)
    *
    * This methods adds declarations to the interface, which may have to be
    * discarded later: If a role method overrides an implicitly inherited method,
    * the interface should not repeat the method for the sake of signature weakening.
    * CopyInheritance.weakenInterfaceSignatures does the job of cleanup.
	*/
	private static void createInterfaceStatements(
            final TypeDeclaration teamDecl,
            TypeDeclaration       roleIfcDecl,
            final TypeDeclaration roleClassDecl)
    {
		// Alle Methoden und Felder in memberType durchgehen und in transformiertem Zustand in das interface einf?gen

		FieldDeclaration[] fields;
		AbstractMethodDeclaration[] methods;

		fields = roleClassDecl.fields;
		methods = roleClassDecl.methods;

		if (fields != null) {
			int fieldsLength = fields.length;
			for (int i = 0; i < fieldsLength; i++) {
				FieldDeclaration field = fields[i];
				// Only static final fields which are not private
				if (   field.isStatic()
					&& (field.modifiers & AccFinal) != 0
                    && (field.modifiers & AccPrivate) == 0)
                {
                    // move the field:
					AstEdit.addField(roleIfcDecl, field, false, false/*typeProblem*/);
					AstEdit.removeField(roleClassDecl, field);
				}
				if ((field.modifiers & AccPrivate) != 0) {
					// private fields need a wrapper in the team for access by callout:
					final MethodDeclaration bridge1 = AstConverter.genBridgeForPrivateRoleField(
														  teamDecl, roleClassDecl, roleIfcDecl.name, field, true);
					AstEdit.addMethodDeclOnly(teamDecl, bridge1, false);
					if (teamDecl.binding != null && bridge1.binding == null)
						roleIfcDecl.getRoleModel()._state.addJob(ITranslationStates.STATE_ROLES_LINKED,
							new Runnable() { public void run() {
								teamDecl.binding.resolveGeneratedMethod(bridge1, false);
							}});

					final MethodDeclaration bridge2 = AstConverter.genBridgeForPrivateRoleField(
														  teamDecl, roleClassDecl, roleIfcDecl.name, field, false);
					AstEdit.addMethodDeclOnly(teamDecl, bridge2, false);
					if (teamDecl.binding != null && bridge1.binding == null)
						roleIfcDecl.getRoleModel()._state.addJob(ITranslationStates.STATE_ROLES_LINKED,
							new Runnable() { public void run() {
								teamDecl.binding.resolveGeneratedMethod(bridge2, false);
							}});
				}
			}
		}

		if (methods != null) {
			int methodsLength = methods.length;
			for (int i = 0; i < methodsLength; i++) {
				AbstractMethodDeclaration abstractMethod = methods[i];

                // Interface can't take constructors.
				if (abstractMethod instanceof MethodDeclaration)
				{
					final MethodDeclaration method = (MethodDeclaration) abstractMethod;

                    // callin methods are actually copied because the wrapper calling the actual
                    // role method would otherwise be quite difficult to translate.

					if (((abstractMethod.modifiers) & AccPrivate) == 0) {
						final MethodDeclaration newmethod = AstConverter.genRoleIfcMethod(teamDecl, method);
						AstEdit.addMethod(roleIfcDecl, newmethod);
						roleIfcDecl.getRoleModel()._state.addJob(ITranslationStates.STATE_ROLE_HIERARCHY_ANALYZED, // calls methods(); 
							new Runnable() { public void run() {
								if (   method.binding != null 
									&& (method.binding.modifiers & ClassFileConstants.AccDeprecated) != 0 
									&& newmethod.binding != null)
								{
									newmethod.binding.modifiers |= ClassFileConstants.AccDeprecated;
									newmethod.binding.tagBits |= TagBits.AnnotationDeprecated;
								}
							}});
					} else {
						// private methods add a wrapper chain to team/role:
						final MethodDeclaration bridge1 = AstConverter.genBridgeForPrivateRoleMethod(
																teamDecl, roleClassDecl, roleIfcDecl.name, method, true);
						AstEdit.addMethodDeclOnly(teamDecl, bridge1, false);
						if (teamDecl.binding != null && bridge1.binding == null)
							roleIfcDecl.getRoleModel()._state.addJob(ITranslationStates.STATE_ROLES_LINKED,
								new Runnable() { public void run() {
									teamDecl.binding.resolveGeneratedMethod(bridge1, false);
								}});
						final MethodDeclaration bridge2 = AstConverter.genBridgeForPrivateRoleMethod(
														        teamDecl, roleClassDecl, roleIfcDecl.name, method, false);
						AstEdit.addMethodDeclOnly(roleClassDecl, bridge2, false);
						if (teamDecl.binding != null && bridge2.binding == null)
							roleClassDecl.getRoleModel()._state.addJob(ITranslationStates.STATE_ROLES_LINKED,
								new Runnable() { public void run() {
									roleClassDecl.binding.resolveGeneratedMethod(bridge2, false);
								}});
					}
                }
			}
		}
	}

	/**
     * createClassPart
     *    +renameClassName                        Klassenname umbenennen: <class> T1.R1 -> <class> T1.__OT__R1
     *    +renameSuperReference                   SuperKlasse umbenennen: <extends> T1.R1(args) -> <extends> T1.__OT__R1(args)
     *    +renameConstructors                     Konstruktoren umbenennen: <constructor> T1.R1(args) -> T1.__OT__R1(args)
     *    +addImplementsInterfaceReference        Klasse mit Interface verbinden
     *
     * + strip method modifiers: make everything public
     *   (access control is via the interface).
     */
	public static void transformClassPart(
            TypeDeclaration       teamDeclaration,
            TypeDeclaration roleClassDeclaration)
    {
		// rename already happened during createInterfacePart.
		char[] oldName = CharOperation.subarray(roleClassDeclaration.name, IOTConstants.OT_DELIM_LEN, -1);

		renameSuperReference(roleClassDeclaration);
		renameConstructors(roleClassDeclaration);
        AstEdit.addImplementsInterfaceReference(oldName, roleClassDeclaration);

        AbstractMethodDeclaration[] methods = roleClassDeclaration.methods;
        if(methods != null)
        {
	        for (int i=0;i<methods.length; i++)
	        {
	            // Access control is only via the interfaces.
	            // Methods invoked via an interface must be public (says JVM-spec).
                // TODO (SH): within role methods, role type arguments are casted
                //   to the class type (weakenSignature), thereby bypassing access control!
                if (!methods[i].isConstructor()) {
                	// modifiers of public and private methods are not changed
                	// (privates are not touched by role splitting)
                	if ((methods[i].modifiers & (AccPublic|AccPrivate)) == 0)
                	{
                		MethodModel.getModel(methods[i]).storeModifiers(methods[i].modifiers);
                	}
                }
	        }
	    }
	}


    /*
	* renameClass 						Klasse umbenennen: <class> T1.R1 -> <class> T1.__OT__R1
	*/
	private static void renameClass(TypeDeclaration classTypeDeclaration) {
		classTypeDeclaration.name = CharOperation.concat(
                OT_DELIM_NAME,
                classTypeDeclaration.name);
		if (classTypeDeclaration.binding != null)
			classTypeDeclaration.binding.sourceName = classTypeDeclaration.name;
	}

	/*
	 * renameConstructors 						Konstruktoren umbenennen: <constructor> T1.R1(args) -> T1.__OT__R1(args)
	 */
	private static void renameConstructors(TypeDeclaration classTypeDeclaration) {
        // ConstructorName
		char[] newname = classTypeDeclaration.name;

		if (classTypeDeclaration.methods != null) {
			for (int i = 0; i < classTypeDeclaration.methods.length; i++) {
				AbstractMethodDeclaration method = classTypeDeclaration.methods[i];
				if (method.isConstructor()) {
					method.selector = newname;
				}
			}
		}
	}

	/*
	* Der Name der TypeReference muss umbenannt werden  von  Superclass nach __OT__Superclass
	*/
	private static void renameSuperReference(TypeDeclaration classTypeDeclaration){
		TypeReference reference = classTypeDeclaration.superclass;
        if (reference != null) {
        	try {
	        	if (reference instanceof CompletionOnKeyword)
	        		return; // not a real type reference!
	        	if (reference instanceof CompletionOnSingleTypeReference)
	        		return; // inserting __OT__ here would break completion
	        	if (reference instanceof CompletionOnQualifiedTypeReference)
	        		return; // inserting __OT__ here would break completion
        	} catch (NoClassDefFoundError e) {
        		// ignore this because batch mode has no CompletionnOnKeyword.
        	}
            if(reference instanceof SingleTypeReference){
                SingleTypeReference singRef = (SingleTypeReference)reference;
                singRef.token = CharOperation.concat(
                        OT_DELIM_NAME,
                        singRef.token);
            } else if(reference instanceof QualifiedTypeReference){
                QualifiedTypeReference qualRef = (QualifiedTypeReference)reference;
                int tokenPos = qualRef.tokens.length - 1;
                qualRef.tokens[tokenPos] = CharOperation.concat(
                        OT_DELIM_NAME,
                        qualRef.tokens[tokenPos]);
            }
            // At this point, we can't really reckognize whether superclass is a role,
            // for that reason ClassScope.checkAdjustSuperclass() might have to revert
            // some of our replacements.
        }
	}

    /**
     * After bindings have been created the synthetic interface is setup
     * to mirror the extends of the role class.
     * This method only treats the case of regular superclasses:
     *   + copy signatures for all methods that are directly or inderictly
     *     inherited (not including java.lang.Object).
     * (role superclasses are treated in linkSuperAndBaseInIfc()).
     *
     * @param teamDecl
     * @param roleClassBinding
     * @param roleIfcDecl
     */
    public static void setupInterfaceForExtends(
            TypeDeclaration teamDecl,
			TypeDeclaration roleClass,
            TypeDeclaration roleIfcDecl)
    {
        ReferenceBinding superClass = roleClass.binding.superclass();
        if (superClass == null)
        	return; // current must be Confined
        if (   superClass.isDirectRole()
        	&& !CharOperation.equals(superClass.internalName(), IOTConstants.OTCONFINED))
        {
             return; // already processed in linkSuperAndBaseInIfc()
        }
        ReferenceBinding[] tsupers = roleClass.getRoleModel().getTSuperRoleBindings();
        for (ReferenceBinding tsuperRole : tsupers)
			if (tsuperRole.superclass() == superClass)
				return; // already included via tsuper.

        // workaround for mixed binary/source roles (cause for this situation unknown):
        if (roleIfcDecl == null)
        	return;

        ReferenceBinding ifcBinding = roleIfcDecl.binding;
        ReferenceBinding javaLangObject = teamDecl.scope.getJavaLangObject();

    	AstGenerator gen;
    	if (roleClass.superclass != null)
    		gen = new AstGenerator(roleClass.superclass.sourceStart, roleClass.superclass.sourceEnd);
    	else
    		gen = new AstGenerator(roleClass.sourceStart, roleClass.sourceEnd);

        while (
            superClass != null &&
            superClass != javaLangObject)
        {
            MethodBinding[] methods = superClass.methods();
            methodLoop: for (int i=0; i<methods.length; i++) {
                MethodBinding m = methods[i];
                if (m.isConstructor())  continue; // not inherited
                if (m.isPrivate())	 	continue; // not inherited
                if (m.isStatic())       continue; // not applicable in roles
                if (!m.isPublic()) { 			  // not visible via interface
                	ProblemMethodBinding problemMethod = new ProblemMethodBinding(m, m.selector, m.parameters, ProblemReasons.NotVisible);
                	problemMethod.modifiers = m.modifiers;
                	problemMethod.modifiers |= AccAbstract|AccSemicolonBody; // don't confuse the MethodVerifier with class method in ifc
                	problemMethod.thrownExceptions = m.thrownExceptions;
                	problemMethod.returnType = m.returnType;
                	MethodModel.getModel(problemMethod).problemDetail = ProblemDetail.RoleInheritsNonPublic;
					ifcBinding.addMethod(problemMethod); // adding binding only as to support error reporting without generating new code
                	continue;
                }

                for (int j = 0; j < IOTConstants.OT_KEYWORDS.length; j++) {
					if (CharOperation.equals(m.selector, IOTConstants.OT_KEYWORDS[j])) {
						roleClass.scope.problemReporter().inheritedNameIsOTKeyword(
								m, gen.sourceStart, gen.sourceEnd);
						continue methodLoop;
					}
				}
                MethodBinding declaredMethod = TypeAnalyzer.findCompatibleMethod(ifcBinding, m);

                if (declaredMethod == null) {
                    MethodDeclaration newmethod = AstConverter.genIfcMethodFromBinding(teamDecl, m, gen);

                    // the following also creates bindings, which helps to avoid
                    // entering the same signature twice
                    // (next time findCompatibleMethod() above will also find the new method).
                    boolean wasSynthetic = false;
                    if ((newmethod.modifiers & AccSynthetic) != 0) {
                    	wasSynthetic = true;
                    	newmethod.modifiers &= ~AccSynthetic;
                    }
                    AstEdit.addMethod(roleIfcDecl, newmethod, wasSynthetic, false);
                    if (newmethod.binding != null) {
                    	newmethod.binding.tagBits |= TagBits.ClearPrivateModifier;
                    	newmethod.binding.copyInheritanceSrc = m;
                    }
                } else {
                	if (!Protections.isAsVisible(declaredMethod.modifiers, m.modifiers))
                		roleClass.scope.problemReporter().visibilityConflict(declaredMethod, m);
                }
            }
            superClass = superClass.superclass();
        }

    }



	/**
	 * After roles have been split and bindings have been completed, transfer
	 * type linkage from classPart to ifcPart: baseclass and superclass.
	 * In this case the superclass will be represented by a super interface.
	 * Note: tsuper roles are not yet copied. We might need to adjust types from
	 * super team to current team later (CopyInheritance.TypeLevel.adjustSuperinterfaces).
     */
	public static void linkSuperAndBaseInIfc(RoleModel role) {
		if (!role.isSourceRole())
			return; // local type nested in a proper role
		if (role.getBinding().isBinaryBinding())
			return; // already linked.

		ReferenceBinding classPart = role.getClassPartBinding();
		ReferenceBinding ifcPart = role.getInterfacePartBinding();

		// baseclass:
		if (classPart != null && ifcPart != null) {
			ifcPart.baseclass = classPart.baseclass;
		}

		// superclass:
        if (   classPart != null
        	&& classPart.superclass() != null) // else current must be Confined
		{
            ReferenceBinding superClass = classPart.superclass();
		    if (superClass.isDirectRole()) {
		        TypeDeclaration interfaceAst = role.getInterfaceAst();
		        if (interfaceAst != null) {
					ReferenceBinding superIfc = superClass.transferTypeArguments(superClass.getRealType());
					// special: linking an ifc from a custom Confined type to "Confined" from the correct team
					// (note that in this case the superclass is actually o.o.Team$__OT__Confined!)
					if (   !CharOperation.equals(ifcPart.internalName(), IOTConstants.CONFINED)
						&& CharOperation.equals(superIfc.internalName(), IOTConstants.CONFINED))
						superIfc = ifcPart.enclosingType().getMemberType(IOTConstants.CONFINED);
					AstEdit.addImplementsBinding(interfaceAst, superIfc);
				} else
		        	System.out.println("Binary interface part of source role: "+role); //binary binding? can we cope with that?
		    }
        }
	}



	/**
	 * After bindings and scopes are created make the interface scope
	 * inherit from the class part scope (important for nested types,
	 * which may appear in interface signatures).
	 * @param model
	 */
	public static void linkScopes(RoleModel model) {
		if (model._classPart != null && model._interfacePart != null) {
			// may already have error
			// (we observed duplicateNestedType, nestedHidesEnclosing)
			if (TypeModel.isIgnoreFurtherInvestigation(model._interfacePart))
				model._classPart.tagAsHavingErrors();
			else
				model._interfacePart.scope.parent = model._classPart.scope;
		}
	}



	public static boolean isClassPartName(char[] typeName) {
		return CharOperation.prefixEquals(OT_DELIM_NAME, typeName);
	}

	public static char[] getInterfacePartName(char[] classPartName) {
		return CharOperation.subarray(classPartName, OT_DELIM_LEN, -1);
	}
}
