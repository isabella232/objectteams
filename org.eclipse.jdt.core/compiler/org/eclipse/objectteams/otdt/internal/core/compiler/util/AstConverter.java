/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2010 Fraunhofer Gesellschaft, Munich, Germany,
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
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Expression.DecapsulationState;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
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
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectMapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;

/**
 * Create AST-nodes from some other representation.
 *
 * @version $Id: AstConverter.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class AstConverter implements ClassFileConstants, ExtraCompilerModifiers, IOTConstants {

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

		fieldDeclaration.modifiers = (fieldBinding.modifiers &~ExtraCompilerModifiers.AccBlankFinal); // this modifier is not used on fieldDecl (AST), overlaps with AccReadOnly
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