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
 * $Id: AstConverter.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
/**
 * ObjectTeams Eclipse source extensions
 * More information available at www.ObjectTeams.org
 *
 * @author Markus Witte
 *
 * @date 17.09.2003
 */
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import java.util.Arrays;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.LiftingTypeReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectMapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.AnchorMapping;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.FakeKind;

/**
 * Create AST-nodes from some other representation.
 *
 * @version $Id: AstConverter.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AstConverter implements ClassFileConstants, ExtraCompilerModifiers, IOTConstants {

	public static final char[] PRIVATE = "$private$".toCharArray(); //$NON-NLS-1$
	static final char[] VALUE_ARG     = "value".toCharArray(); //$NON-NLS-1$
	public static final char[] ROLE_ARG_NAME = "_OT$rolearg".toCharArray(); //$NON-NLS-1$

	// ==== Mostly For RoleSplitting ====

	/**
	 * Create the interface part of a role. Transfer superInterfaces from class part to interface.
	 *
	 * Only create AST, no bindings.
	 *
	 * @param teamDeclaration
	 * @param roleClassDeclaration
	 * @return the interface AST
	 */
	public static TypeDeclaration genRoleInterface(TypeDeclaration teamDeclaration, TypeDeclaration roleClassDeclaration)
	{
		TypeDeclaration typeDeclaration =
			new TypeDeclaration(roleClassDeclaration.compilationResult); // same result as class part (might be role file)
	    // make it an interface, but clear Team and final flag if set.
		// (there is no use in marking an interface as team: it has no member types!)
	    typeDeclaration.modifiers     = (roleClassDeclaration.modifiers
											| AccSynthIfc | AccRole)
										& ~(AccTeam|AccFinal);
	    typeDeclaration.isGenerated   = true;
		typeDeclaration.name          = roleClassDeclaration.name;
		typeDeclaration.sourceStart   = roleClassDeclaration.sourceStart;
	    typeDeclaration.sourceEnd     = roleClassDeclaration.sourceEnd;
	    if (roleClassDeclaration.isRoleFile())
	    	AstEdit.addTypeDeclaration(roleClassDeclaration.compilationUnit, typeDeclaration);

    	typeDeclaration.typeParameters= AstClone.copyTypeParameters(roleClassDeclaration.typeParameters);

		// transfer superInterfaces from class to interface:
	    if (roleClassDeclaration.superInterfaces != null) {
	        typeDeclaration.superInterfaces = roleClassDeclaration.superInterfaces;
			roleClassDeclaration.superInterfaces = null;
	    }
	    TypeReference superClass= roleClassDeclaration.superclass;
	    if (   superClass != null
	    	&& superClass instanceof SingleTypeReference
	    	&& CharOperation.equals(((SingleTypeReference)superClass).token, IOTConstants.CONFINED))
	    {
	    	// if class extends Confined mark the role interface to extend __OT__Confined instead of Object.
	    	AstGenerator gen= new AstGenerator(roleClassDeclaration.sourceStart, roleClassDeclaration.sourceEnd);
	    	typeDeclaration.superclass= gen.singleTypeReference(IOTConstants.OTCONFINED);
	    }
		return typeDeclaration;
	}

	/**
	 * Compute the modifiers for an abstract method to be included in a role interface.
	 *
	 * @param modifiers modifiers of the method as it appears in the role class.
	 * @return modifiers
	 */
	static int computeIfcpartModifiers(int modifiers) {
		return (modifiers
				| AstGenerator.AccIfcMethod)
		   		& ~(AccFinal|AccSynchronized|AccStrictfp|AccNative|AccDeprecated);
	}

	// ==== For CopyInheritance: ====

    public static AbstractMethodDeclaration createMethod(
            MethodBinding      methodBinding,
			ReferenceBinding   site,
            CompilationResult  compilationResult,
            DecapsulationState decapsulation,
            AstGenerator       gen)
    {

		if (methodBinding == null)
			return null;

		AbstractMethodDeclaration abstractMethodDeclaration;

		if (CharOperation.equals(methodBinding.selector, TypeConstants.INIT)) {
			ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);
			constructorDeclaration.selector = site != null ? site.sourceName : methodBinding.declaringClass.sourceName;
			abstractMethodDeclaration = constructorDeclaration;
		} else  {
			MethodDeclaration methodDeclaration = new MethodDeclaration(compilationResult);

			// on these special methods see class header comment in CopyInheritance:
			if (   CharOperation.prefixEquals(IOTConstants.CAST_PREFIX, methodBinding.selector)
				|| CharOperation.prefixEquals(IOTConstants._OT_LIFT_TO, methodBinding.selector))
				methodDeclaration.returnType = new SingleTypeReference(methodBinding.returnType.internalName(),0);
			else
	            methodDeclaration.returnType = gen.typeReference(methodBinding.returnType);

			methodDeclaration.returnType.setBaseclassDecapsulation(decapsulation);
            methodDeclaration.selector = methodBinding.selector;
            TypeVariableBinding[] typeVariables= methodBinding.typeVariables();
            if (typeVariables != Binding.NO_TYPE_VARIABLES) {
            	TypeParameter[] parameters= new TypeParameter[typeVariables.length];
            	for (int i = 0; i < typeVariables.length; i++)
					parameters[i]= gen.typeParameter(typeVariables[i]);
				methodDeclaration.typeParameters= parameters;
            }
			abstractMethodDeclaration = methodDeclaration;
		}

		abstractMethodDeclaration.modifiers = methodBinding.modifiers & ~ClassFileConstants.AccSynthetic;
		if (methodBinding.isAbstract())
			// AccSemicolonBody - flag does not exist in binary methods:
			abstractMethodDeclaration.modifiers |= AccSemicolonBody;
		abstractMethodDeclaration.arguments = createArguments(methodBinding.parameters, site, decapsulation, gen);
		abstractMethodDeclaration.isCopied = true;
		abstractMethodDeclaration.sourceMethodBinding=methodBinding;

		abstractMethodDeclaration.thrownExceptions = AstClone.copyExceptions(methodBinding, gen);

		return abstractMethodDeclaration;
	}


	public static FieldDeclaration createField(
            FieldBinding      fieldBinding,
            TypeDeclaration   roleDeclaration,
            AstGenerator      gen)
    {

		if(fieldBinding==null)
			return null;

		FieldDeclaration fieldDeclaration = new FieldDeclaration();
		fieldDeclaration.type = gen.typeReference(fieldBinding.type);

		fieldDeclaration.modifiers = fieldBinding.modifiers;
		fieldDeclaration.name = fieldBinding.name;
		if (fieldBinding.copyInheritanceSrc != null)
			fieldDeclaration.copyInheritanceSrc = fieldBinding.copyInheritanceSrc;
		else
			fieldDeclaration.copyInheritanceSrc = fieldBinding;
		
		AnnotationBinding[] annotBindings = fieldBinding.getAnnotations();
		if (annotBindings != Binding.NO_ANNOTATIONS) {
			ProblemReporter pr = fieldBinding.isStatic() ?
						roleDeclaration.staticInitializerScope.problemReporter() :
						roleDeclaration.initializerScope.problemReporter();
			Annotation[] annotDecls = new Annotation[annotBindings.length];
			boolean hasAnnotationError = false;
			for (int i = 0; i < annotBindings.length; i++) {
				AnnotationBinding binding = annotBindings[i];
				ElementValuePair[] elementValuePairs = binding.getElementValuePairs();
				char[][] annotTypeName = binding.getAnnotationType().compoundName;
				if (elementValuePairs == Binding.NO_ELEMENT_VALUE_PAIRS) {
					annotDecls[i] = gen.markerAnnotation(annotTypeName);
				} else {
					int numPairs = elementValuePairs.length;
					char[][] names = new char[numPairs][];
					Expression[] values = new Expression[numPairs];
					for (int j = 0; j<names.length; j++) {
						names[j] = elementValuePairs[j].getName();
						Object elementValue = elementValuePairs[j].getValue();
						values[j] = annotationValues(elementValue, gen, pr);
					}
					if (values.length == 0 || values[0] == null) {
						pr.unexpectedAnnotationStructure(annotTypeName, fieldBinding.name, gen.sourceStart, gen.sourceEnd);
						hasAnnotationError = true;
					} else
					if (   numPairs == 1 
						&& CharOperation.equals(names[0], TypeConstants.VALUE)) {
						annotDecls[i] = gen.singleMemberAnnotation(annotTypeName, values[0]);
					} else {
						annotDecls[i] = gen.normalAnnotation(annotTypeName, names, values);
					}
				}
			}
			if (!hasAnnotationError)
				fieldDeclaration.annotations = annotDecls;
		}

		//field initializations are copied using a RoleInitializationMethod
		return fieldDeclaration;
	}

	private static Expression annotationValues(Object elementValue, AstGenerator gen, ProblemReporter pr) {
		if (elementValue instanceof Object[]) {
			Object[] valuesArray = (Object[])elementValue;
			ArrayInitializer arrayInit = new ArrayInitializer();
			arrayInit.expressions = new Expression[valuesArray.length];
			for (int k = 0; k<valuesArray.length; k++) {
				arrayInit.expressions[k] = annotationValue(valuesArray[k], gen, pr);
				if (arrayInit.expressions[k] == null)
					return null; // error detected.
			}
			return arrayInit;
		} else {
			return annotationValue(elementValue, gen, pr);
		}
	}

	private static Expression annotationValue(Object elementValue, AstGenerator gen, ProblemReporter pr) {
		if (elementValue instanceof StringConstant)
			return gen.stringLiteral(((StringConstant)elementValue).stringValue().toCharArray());
		if (elementValue instanceof IntConstant)
			return gen.intLiteral(((IntConstant)elementValue).intValue());
		if (elementValue instanceof FieldBinding) {
			FieldBinding field = (FieldBinding) elementValue;
			if (field.isStatic()) {
				char[][] tokens = CharOperation.splitOn('.', field.declaringClass.readableName());
				tokens = CharOperation.arrayConcat(tokens, field.name);
				return gen.qualifiedNameReference(tokens);
			}
		}
		return null;
	}

	public static TypeDeclaration createNestedType (
            char[]            name,
            int               modifiers,
			boolean 		  isNestedType,
            boolean           isPurelyCopied,
			TypeDeclaration   teamDecl,
			ReferenceBinding  tsuperRole)
    {
		TypeDeclaration enclosingTypeDecl = teamDecl;
        if (tsuperRole != null && isNestedType)
        {
        	ReferenceBinding srcEnclosing = tsuperRole.enclosingType();
        	// TODO (SH): recurse even more
          	TypeDeclaration[] members = enclosingTypeDecl.memberTypes;
          	if (members != null)
          		for (int i=0; i<members.length; i++) {
          			if (CharOperation.equals(members[i].name, srcEnclosing.internalName())) {
          				enclosingTypeDecl =	members[i];
          				break;
          			}
          		}
        }

		TypeDeclaration nestedType =
            new TypeDeclaration(enclosingTypeDecl.compilationResult);
		nestedType.name      = name;
		nestedType.modifiers = modifiers;
        nestedType.isGenerated    = true;
        nestedType.isPurelyCopied = isPurelyCopied;
        int sStart = enclosingTypeDecl.sourceStart;
        int sEnd = enclosingTypeDecl.sourceEnd;
        if (enclosingTypeDecl.superclass != null) {
        	sStart = enclosingTypeDecl.superclass.sourceStart;
        	sEnd   = enclosingTypeDecl.superclass.sourceEnd;
        }
		nestedType.declarationSourceStart = sStart;
		nestedType.declarationSourceEnd   = sEnd;
        nestedType.sourceStart = sStart;
        nestedType.sourceEnd   = sEnd;
        nestedType.bodyStart   = sStart;
        nestedType.bodyEnd     = sEnd;
        AstGenerator gen = new AstGenerator(sStart, sEnd);
        if (tsuperRole != null && tsuperRole.isInterface()) {
        	ReferenceBinding superclass= tsuperRole.superclass();
        	if (superclass != null && CharOperation.equals(superclass.internalName(), IOTConstants.OTCONFINED)) {
        		nestedType.superclass= gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_TEAM_OTCONFINED);
        		nestedType.superclass.resolvedType= superclass;
        	}
        }

        if (tsuperRole != null && tsuperRole.isLocalType()) {
			nestedType.bits  |= ASTNode.IsLocalType;
			if (tsuperRole.isAnonymousType()) {
				// have no name, need at least a super type
				// (otherwise ClassScope.checkAndSetModifiers() will fail)
				ReferenceBinding superType;
				ReferenceBinding[] superIfcs = tsuperRole.superInterfaces();
				if (superIfcs != Binding.NO_SUPERINTERFACES)
					superType = superIfcs[0];
				else
					superType = tsuperRole.superclass();
				nestedType.allocation = new QualifiedAllocationExpression();
				nestedType.allocation.type = gen.typeReference(superType);
				nestedType.allocation.anonymousType = nestedType;
			}
			AstEdit.addLocalTypeDeclaration(enclosingTypeDecl, nestedType);
        } else {
        	AstEdit.addMemberTypeDeclaration(enclosingTypeDecl, nestedType);
        }

        if (tsuperRole != null && tsuperRole.roleModel.isRoleFile()) {
        	// for role copied from a role file create an enclosing CUD to allow for
        	// late role catch-up of this phantom role.
        	ProblemReporter reporter = enclosingTypeDecl.scope.problemReporter();
        	CompilationResult compilationResult= new CompilationResult("nofile".toCharArray(), 0, 0, 0);  //$NON-NLS-1$
        	CompilationUnitDeclaration cud = new CompilationUnitDeclaration(reporter, compilationResult, 0);
        	nestedType.compilationResult= compilationResult;
        	nestedType.compilationUnit = cud;
        	cud.types= new TypeDeclaration[] { nestedType };
        	char[][] enclosingName = enclosingTypeDecl.binding.compoundName;
			char[][] tokens= CharOperation.arrayConcat(enclosingName, nestedType.name);
        	long[] positions= new long[tokens.length];
        	Arrays.fill(positions, 0L);
        	cud.currentPackage= new ImportReference(enclosingName, positions, false, ClassFileConstants.AccTeam);
        }

        // Create TypeBindings for this type
        enclosingTypeDecl.scope.addGeneratedType(nestedType);

        int state= enclosingTypeDecl.getModel().getState();
        if (enclosingTypeDecl.isTeam())
        	state= enclosingTypeDecl.getTeamModel().getState();

        // Create Type-Hierarchy?
        nestedType.scope.connectTypeHierarchyForGenerated(state>=ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);


        // don't set past the state of the enclosing team:
        state = Math.min(state, ITranslationStates.STATE_LENV_CONNECT_TYPE_HIERARCHY);

        if (nestedType.getRoleModel() != null)
        	nestedType.getRoleModel().setState(state);
        if (nestedType.isTeam() && nestedType.getTeamModel() != null)
        	nestedType.getTeamModel().setState(state);

		return nestedType;
	}

	/**
	 * Create method arguments which match the given parameters.
	 * If a site is given, try to map all types to this new context.
	 * @param parameters
	 * @param site class for whose context types shall be adjusted, or null.
	 * @return an array of arguments names "arg<n>" with types according to parameters.
	 */
	private static Argument[] createArguments(
            TypeBinding[]      parameters,
			ReferenceBinding   site,
			DecapsulationState decapsulation,
			AstGenerator       gen)
    {
		if(parameters==null || parameters.length == 0)
			return null;

		int length=parameters.length;
		Argument[] arguments = new Argument[length];

		for(int i=0;i<length;i++){
			TypeBinding tb = parameters[i];
			if (site != null	&& (tb instanceof ReferenceBinding))
				tb = ConstantPoolObjectMapper.searchRoleClass((ReferenceBinding)tb, site);
			TypeReference tr = gen.typeReference(tb);
			tr.setBaseclassDecapsulation(decapsulation);
			char[] bez = ("arg"+i).toCharArray(); // dummy argument names, don't have original names. //$NON-NLS-1$
			arguments[i] = new Argument(bez,0,tr,0);

		}
		return arguments;
	}

    /** If a generated method was copied from tsuper, adjust its flags and argument names,
     *  but leave its signature for true overriding.
     *  Thus copying generated methods does the job of signature weakening.
     */
    public static MethodDeclaration findAndAdjustCopiedMethod(
    		TypeDeclaration teamDecl,
			char[]          methodName,
			Argument[]      arguments)
    {
        int argumentsCount = arguments == null ? 0 : arguments.length;
		AbstractMethodDeclaration foundMethod =
        	TypeAnalyzer.findMethodDecl(teamDecl, methodName, argumentsCount);
        if (foundMethod != null)
        {
        	if (!foundMethod.isCopied) {
        		// could mean different things
        		if (!foundMethod.isGenerated) {
        			// it is a user defined method!
        			foundMethod.scope.problemReporter().overridingPredefined(foundMethod);
        			return null;
        		}
        		throw new InternalCompilerError("Generated methods conflicting with each other: "+new String(foundMethod.selector)); //$NON-NLS-1$
        	}
        	foundMethod.isCopied = false;
        	foundMethod.isGenerated = true;
        	foundMethod.modifiers &= ~(AccAbstract|AccSemicolonBody);
        	for (int i = 0; i < argumentsCount; i++) {
            	foundMethod.arguments[i].updateName(arguments[i].name);
			}
        	if (foundMethod.binding != null) {
        		foundMethod.binding.modifiers &= ~(AccAbstract);
        		if (foundMethod.binding.copyInheritanceSrc != null) {
        			// not directly copied any more but overriding a tsuper version:
        			foundMethod.binding.addOverriddenTSuper(foundMethod.binding.copyInheritanceSrc);
        			foundMethod.binding.setCopyInheritanceSrc(null);
        		}
        	}
        	return (MethodDeclaration)foundMethod;
        }
        return null;
    }

	/**
	 * Create the declaration of a role method to be inserted into the role interface.
	 * @param typeDecl (only need the CompilationResult)
	 * @param classpartMethod
	 * @return the method AST
	 */
	public static MethodDeclaration genRoleIfcMethod(TypeDeclaration typeDecl, MethodDeclaration classpartMethod) {
		MethodDeclaration newmethod = AstClone.copyMethod(typeDecl, classpartMethod, null/*AstGenerator*/);
		// TODO (SH): AccNative is even illegal!
		newmethod.modifiers   = computeIfcpartModifiers(classpartMethod.modifiers);
		classpartMethod.interfacePartMethod = newmethod;
		return newmethod;
	}

	public static char[] getPrivateBridgeSelector(char[] selector, char[] roleName) {
		return CharOperation.concat(
				CharOperation.concat(OT_DOLLAR_NAME, roleName),
				CharOperation.concat(PRIVATE, selector));
	}

	public static boolean isPrivateBridgeSelector(char[] selector) {
		if (!CharOperation.prefixEquals(OT_DOLLAR_NAME, selector))
			return false;
		return CharOperation.indexOf(PRIVATE, selector, true, OT_DOLLAR_LEN) > -1;
	}

	/**
	 * Generate a method that bridges from a callout to a private role method.
	 * @param teamDecl
	 * @param roleName
	 * @param privateRoleMethod
	 * @param teamPart select whether the team part should be created or the role part.
	 * @return the new method.
	 */
	public static MethodDeclaration genBridgeForPrivateRoleMethod(TypeDeclaration   teamDecl,
																  TypeDeclaration   roleDecl,
															      char[]            roleName,
															      MethodDeclaration privateRoleMethod,
															      boolean           teamPart)
	{
		// teamMeth looks like this (delegates to role method below):
		// public T MyTeam._OT$R$private$m(R _OT$role, args) {
		//     [return] __OT__R._OT$R$private$m(_OT$role, args);
		// }

		// roleMeth looks like this (delegates to original private method):
		// public static T _OT$R$private$m(R _OT$role, args) {
		//    [return] ((__OT__R)_OT$role).m(args);
		// }

		AstGenerator gen = (teamPart && roleDecl.isRoleFile())  
			? new AstGenerator(teamDecl.sourceStart, teamDecl.sourceEnd) // have no better position
			: new AstGenerator(privateRoleMethod.sourceStart, privateRoleMethod.sourceEnd);

		MethodDeclaration meth = AstClone.copyMethod(teamPart?teamDecl:roleDecl, privateRoleMethod, gen);

		meth.modifiers = AccPublic;
		if (!teamPart)
			meth.modifiers |= AccStatic;

		meth.selector = getPrivateBridgeSelector(meth.selector, roleName);

		// add role argument to front of arguments:
		int len = 0;
		if (meth.arguments != null) {
			len = meth.arguments.length;
			System.arraycopy(meth.arguments, 0,
					meth.arguments = new Argument[len+1], 1,
					len);
			for (int i=1; i < len+1; i++)
				if (meth.arguments[i].type instanceof LiftingTypeReference) // defer the declared lifting to the original method
					meth.arguments[i].type = AstClone.copyTypeReference(((LiftingTypeReference)meth.arguments[i].type).baseReference);
		} else {
			meth.arguments = new Argument[1];
		}
		meth.arguments[0] = gen.argument(ROLE_ARG_NAME, gen.singleTypeReference(roleName), AccFinal);

		if (!roleDecl.isConverted) {
			genPrivateRoleMethodBridgeStatements(privateRoleMethod.selector, privateRoleMethod.isStatic(), len, meth, roleName, teamPart, gen); 
			meth.hasParsedStatements = true;
		}
		MethodModel model = MethodModel.getModel(meth);
		model._fakeKind= FakeKind.ROLE_FEATURE_BRIDGE;
		model._thisSubstitution = meth.arguments[0];
		if (teamPart)
			model._sourceDeclaringType = roleDecl;
		return meth;
	}

	public static void genPrivateRoleMethodBridgeStatements (char[]				selector,
															 boolean 		 	isStatic, // irrelevant for team part
															 int 				srcArgsLen, 
															 MethodDeclaration 	bridgeMethod,
															 char[] 		   	roleName,
															 boolean 		   	isTeamPart, 
															 AstGenerator 		gen) 
	{
		char[] roleClassName = CharOperation.concat(OT_DELIM_NAME, roleName);

		int offset = 0; // into call arguments (0 or 1)
		Expression   receiver;
		if (isTeamPart) {
			receiver = gen.singleNameReference(roleClassName); // __OT__R.
			selector = bridgeMethod.selector;						   // _OT$R$private$m
			offset = 1; // call arguments include role arg
		} else {
			if (isStatic)
				receiver = gen.singleNameReference(roleClassName); // __OT__R.
			else
				receiver = gen.castExpression(					   // ((__OT__R)_OT$role).
						gen.singleNameReference(ROLE_ARG_NAME),
						gen.singleTypeReference(roleClassName),
						CastExpression.RAW);
			// plain call arguments (no offset)
		}

		// call params differ (according to offset)
		Expression[] params = new Expression[srcArgsLen+offset];
		for (int i=0; i<srcArgsLen; i++)
			params[i+offset] = gen.singleNameReference(bridgeMethod.arguments[i+1].name);
		if (offset == 1)
			params[0] = gen.singleNameReference(ROLE_ARG_NAME);

		// assemble call:
		Expression call = gen.messageSend(receiver, selector, params);


		if (   bridgeMethod.returnType instanceof SingleTypeReference
			&& CharOperation.equals(((SingleTypeReference)bridgeMethod.returnType).token,
					                 TypeConstants.VOID))
		{
			bridgeMethod.statements = new Statement[]{ call };
		} else {
			bridgeMethod.statements = new Statement[]{ gen.returnStatement(call) };
		}
	}

	/**
	 * Generate a method that bridges from a callout to a private role field.
	 * @param teamDecl
	 * @param roleName
	 * @param privateRoleField
	 * @param isGetter select whether to generate a getter or a setter
	 * @return the new method.
	 */
	public static MethodDeclaration genBridgeForPrivateRoleField(TypeDeclaration  teamDecl,
																 TypeDeclaration  roleDecl,
															     char[]           roleName,
															     FieldDeclaration privateRoleField,
															     boolean          isGetter)
	{
		// getter looks like this:
		// public T MyTeam._OT$R$private$_OT$get$f(R _OT$role) {
		//     return ((__OT__R)_OT$role)._OT$get$f();
		// }

		// setter looks like this:
		// public void _OT$R$private$f(R _OT$role, T value) {
		//    ((__OT__R)_OT$role).OT_$set$f(value);
		// }

		AstGenerator gen = new AstGenerator(privateRoleField.sourceStart, privateRoleField.sourceEnd);


		TypeReference fieldTypeRef = AstClone.copyTypeReference(privateRoleField.type);
		Argument      roleArg      = gen.argument(ROLE_ARG_NAME, gen.singleTypeReference(roleName));
		char[]    accessorSelector = CharOperation.concat(
										isGetter ? IOTConstants.OT_GETFIELD : IOTConstants.OT_SETFIELD,
										privateRoleField.name);

		MethodDeclaration meth = gen.method(teamDecl.compilationResult,
										    AccPublic,
										    isGetter ?
										    		fieldTypeRef
										    :  		gen.typeReference(TypeBinding.VOID),
										    getPrivateBridgeSelector(accessorSelector, roleName),
											isGetter ?
													new Argument[] {
										    			roleArg,
										    		}
											:		new Argument[] {
										    			roleArg,
										    			gen.argument(
										    				VALUE_ARG,
										    				fieldTypeRef)
										    		});


		char[] roleClassName = CharOperation.concat(OT_DELIM_NAME, roleName);
		Expression receiver = gen.singleNameReference(roleClassName);

		// assemble call:
		MessageSend call = new MessageSend() {
			@Override
			protected AnchorMapping beforeMethodLookup(TypeBinding[] argumentTypes, Scope scope)
			{
				// this message send refers to a non-existant method, create the method on demand.
				TypeBinding returnType = ((MethodDeclaration)scope.methodScope().referenceMethod()).returnType.resolvedType;
				MethodBinding bridgeMethod = new MethodBinding(AccPublic|AccStatic,
						                                       this.selector,
						                                       returnType, // return type
						                                       argumentTypes,
						                                       null, // exceptions
						                                       (ReferenceBinding)this.actualReceiverType);
				((ReferenceBinding)this.actualReceiverType).addMethod(bridgeMethod);
				MethodModel.getModel(bridgeMethod)._fakeKind = MethodModel.FakeKind.ROLE_FEATURE_BRIDGE;
				return super.beforeMethodLookup(argumentTypes, scope);
			}
		};
		call.receiver  = receiver;
		call.selector  = accessorSelector;
		Expression roleArg2 = gen.castExpression(
						gen.singleNameReference(ROLE_ARG_NAME),
						gen.singleTypeReference(roleClassName),
						CastExpression.RAW);
		call.arguments = isGetter ?
							new Expression[] { roleArg2 }
				:           new Expression[] { roleArg2,
											   gen.singleNameReference(VALUE_ARG) };

		if (isGetter)
			meth.statements = new Statement[]{ gen.returnStatement(call) };
		else
			meth.statements = new Statement[]{ call };

		meth.hasParsedStatements = true;
		MethodModel model = MethodModel.getModel(meth);
		model._sourceDeclaringType = roleDecl;
		model._fakeKind= FakeKind.ROLE_FEATURE_BRIDGE;
		return meth;
	}

	/**
	 * Create the declaration of a role method to be inserted into the role interface.
	 * @param teamDecl
	 * @param classpartMethod
	 * @return the method AST
	 */
	public static MethodDeclaration genIfcMethodFromBinding(TypeDeclaration teamDecl,
													 MethodBinding classpartMethod,
													 AstGenerator  gen)
	{
		MethodDeclaration newmethod =
		    new MethodDeclaration(teamDecl.compilationResult);

		newmethod.selector    = classpartMethod.selector;
		newmethod.modifiers   = computeIfcpartModifiers(classpartMethod.modifiers);
		// special case of copying cast method for non-public role:
		if (   CharOperation.prefixEquals(IOTConstants.CAST_PREFIX, classpartMethod.selector)
			&& !((ReferenceBinding)classpartMethod.returnType).isPublic())
		{
			newmethod.modifiers = AccProtected|AstGenerator.AccIfcMethod; // see StandardElementGenerator.getCastMethod();
		}

		newmethod.isGenerated = true; // don't try to parse it etc..
		// no position, created from binding, so use pre-set default location
		gen.setMethodPositions(newmethod);

		newmethod.arguments = AstConverter.createArgumentsFromParameters(classpartMethod.parameters, gen);

		newmethod.returnType = gen.typeReference(classpartMethod.returnType);

		if (classpartMethod.typeVariables() != Binding.NO_TYPE_VARIABLES) {
			TypeVariableBinding[] typeVarBindings = classpartMethod.typeVariables;
			newmethod.typeParameters = new TypeParameter[typeVarBindings.length];
			for (int i = 0; i < typeVarBindings.length; i++) {
				newmethod.typeParameters[i] = gen.typeParameter(typeVarBindings[i]);
			}
		}

		newmethod.thrownExceptions = AstClone.copyExceptions(classpartMethod, gen);
		return newmethod;
	}

	/**
	 * Create arguments from type bindings by assigning dummy names.
	 * @param parameters template for the new arguments.
	 * @return an array of new Arguments (named "arg<n>")
	 */
	public static Argument[] createArgumentsFromParameters(TypeBinding[] parameters, AstGenerator gen) {
		if (parameters.length == 0)
			return null;
		Argument[] newArguments = new Argument[parameters.length];
		for (int a=0; a<parameters.length; a++) {
		    TypeBinding param = parameters[a];
		    newArguments[a] = new Argument(
		            ("arg"+a).toCharArray(),  //$NON-NLS-1$
		            gen.pos,
		            gen.typeReference(param),
		            /*modifiers*/0);
		}
		return newArguments;
	}
}

//Markus Witte}