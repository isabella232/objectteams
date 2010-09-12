/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractMethodDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AnchorListAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Config;
import org.eclipse.objectteams.otdt.internal.core.compiler.lifting.Lifting;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.ProblemDetail;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.InsertTypeAdjustmentsVisitor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.MethodSignatureEnhancer;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.TransformStatementsVisitor;

/**
 * OTDT changes:
 *
 * FIELDS:
 * What: Link to MethodModel and original binding (CopyInheritance)
 * 		 Add flags for different kinds of synthetic methods
 *
 * GENERATE/COPY BYTE CODE:
 * What:
 * 		1) store the completed byte code in RoleModel
 * 		1b) mark erroneous methods to prevent byte code copy (have not byte code)
 * 		2) switch between actual generateCode and adjusting copy
 * 		3) write OT-specific byte code attributes during generate code
 *
 * What: Provide a endOfMethodHook in generateCode (used by RoleInitializationMethod)
 * Why:  See RoleInitializationMethod.endOfMethodHook
 *
 * What: Support static role methods.
 * Why:  while disallowed in pure Java, static role methods are allowed and required
 *       a synthetic argument for passing the enclosing team instance.
 *       A second dummy argument (int) is used to align argument positions with those
 *       of a nested class ctor where local(0) == this.
 * Where:
 *       AbstractMethodDeclration.generateCode
 *       	respect binding.declaringClass.enclosingInstancesSlotSize
 *       	when computing local variable positions.
 *       MethodBinding.signature
 *       	prepend synthetic arguments "ILMyTeam;" to the signature.
 *       CodeStream.initMaxLocals
 *       	add 2 for synthetic arguments of static role methods.
 *       MesasgeSend.generateCode
 *       	generate synthetic argument values when invoking a static role method:
 *       	"iconst_0; aload_(enclosing_team_instance)"
 *		 BlockScope.getEmulationPath(..)
 *			now also applies to MyTeam.this expressions within static role methods
 *       MethodSpec.getResolvedParamters
 *       	include synthetic arguments
 *       BinaryTypeBinding.createMethod
 *          skip synthetic arguments
 *       MemberTypeBinding.checkSyntheticArgsAndFields
 *          create synth arg for role-interface, too (ifcs are usually skipped)
 *          in order to get consistent signatures for ifc- and class-methods.
 *
 * TRANSLATE:
 * What: perform type adjustments on statements
 * How:  resolveStatements triggers InsertTypeAdjustmentsVisitor and CollectedReplacementsTransformer.
 *
 * What: Add anchor list attribute (TODO (SH): unify this with base name strategy of team anchors)
 *
 * @version $Id: AbstractMethodDeclaration.java 23404 2010-02-03 14:10:22Z stephan $
 */
public abstract class AbstractMethodDeclaration
	extends ASTNode
	implements ProblemSeverities, ReferenceContext {

//	{ObjectTeams
    // this holds additional info for role methods:
    public MethodModel model = null;
	public MethodModel getModel() {
		if (this.model != null)
			return this.model;
		if (this.binding != null)
			return this.binding.model;
		return null;
	}
	//Reference to original Binding in case of CopyInheritance
	public MethodBinding sourceMethodBinding;
	//For Methods which are created in the context of Implicit-Inheritance
	public boolean isCopied=false;
    //For other generated methods (lift methods, role constructors, creation methods, callin/out-wrappers)
    public boolean isGenerated=false;
    /**
     * Callout bindings may reuse an existing method declaration.
     * Also guard predicates are marked with this flag.
     * All other generated methods are purely generated.
     */
    public boolean isReusingSourceMethod=false;
    //For Methods that are tsuper versions (i.e., augmented with a marker arg:
    public boolean isTSuper=false;
    //Some clients don't invoke getMethodBodies, yet some statements might be generated,
    // this flag marks if source statements have been parsed:
    public boolean hasParsedStatements=false;
    //For callin/callout wrappers store the kind of the generating mapping:
    public enum WrapperKind {
    	NONE, CALLOUT, CALLIN;

		public boolean none()    { return this == NONE; }
		public boolean any()     { return this != NONE; }
		public boolean callout() { return this == CALLOUT; }
		public boolean _callin() { return this == CALLIN; }
    }
    public WrapperKind isMappingWrapper = WrapperKind.NONE;
    // links from a role method to its representation in the interface after role splitting:
    public AbstractMethodDeclaration interfacePartMethod = null;

    // ==== additional queries: ====
    /**
     * Is the method copied but not an interface method?
     * Only those methods have byte code to copy from the tsuper role.
     */
    public boolean isRelevantCopied() {
    	if (this.model != null && this.model.problemDetail == ProblemDetail.IllegalDefaultCtor) {
    		((ConstructorDeclaration)this).createExceptionStatements();
    		return false;
    	}
        return this.isCopied && ((this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) == 0);
    }
	protected boolean areStatementsMissing() {
		return !this.hasParsedStatements;
	}
	public void setStatements(Statement[] statements) {
		this.statements = statements;
		this.hasParsedStatements = true;
	}
	public boolean isBasePredicate() {
		return false;
	}
	public boolean isCalloutWrapper() {
    	return this.isMappingWrapper.callout();
    }
    /** Callin method or replace callin wrapper */
    public boolean isAnyCallin() {
    	return (this.modifiers & ExtraCompilerModifiers.AccCallin) != 0;
    }
    /** Source level callin method only. */
    public boolean isCallin() {
    	return (this.modifiers & ExtraCompilerModifiers.AccCallin) != 0 && this.isMappingWrapper.none();
    }
//Markus Witte+SH}

	public MethodScope scope;
	//it is not relevent for constructor but it helps to have the name of the constructor here
	//which is always the name of the class.....parsing do extra work to fill it up while it do not have to....
	public char[] selector;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiers;
	public int modifiersSourceStart;
	public Annotation[] annotations;
	public Argument[] arguments;
	public TypeReference[] thrownExceptions;
	public Statement[] statements;
	public int explicitDeclarations;
	public MethodBinding binding;
	public boolean ignoreFurtherInvestigation = false;

	public Javadoc javadoc;

	public int bodyStart;
	public int bodyEnd = -1;
	public CompilationResult compilationResult;

	AbstractMethodDeclaration(CompilationResult compilationResult){
		this.compilationResult = compilationResult;
	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel, CategorizedProblem problem) {

		switch (abortLevel) {
			case AbortCompilation :
				throw new AbortCompilation(this.compilationResult, problem);
			case AbortCompilationUnit :
				throw new AbortCompilationUnit(this.compilationResult, problem);
			case AbortType :
				throw new AbortType(this.compilationResult, problem);
			default :
				throw new AbortMethod(this.compilationResult, problem);
		}
	}

	public abstract void analyseCode(ClassScope classScope, InitializationFlowContext initializationContext, FlowInfo info);

	/**
	 * Bind and add argument's binding into the scope of the method
	 */
	public void bindArguments() {

		if (this.arguments != null) {
			// by default arguments in abstract/native methods are considered to be used (no complaint is expected)
//{ObjectTeams: for generated methods we may reach here without binding.parameters
/* orig:
			if (this.binding == null) {
  :giro */
			if (   this.binding == null
				|| this.binding.parameters == Binding.NO_PARAMETERS) {
// SH}
				for (int i = 0, length = this.arguments.length; i < length; i++) {
					this.arguments[i].bind(this.scope, null, true);
				}
				return;
			}
			boolean used = this.binding.isAbstract() || this.binding.isNative();
			AnnotationBinding[][] paramAnnotations = null;
			for (int i = 0, length = this.arguments.length; i < length; i++) {
				Argument argument = this.arguments[i];
				argument.bind(this.scope, this.binding.parameters[i], used);
				if (argument.annotations != null) {
					if (paramAnnotations == null) {
						paramAnnotations = new AnnotationBinding[length][];
						for (int j=0; j<i; j++) {
							paramAnnotations[j] = Binding.NO_ANNOTATIONS;
						}
					}
					paramAnnotations[i] = argument.binding.getAnnotations();
				} else if (paramAnnotations != null) {
					paramAnnotations[i] = Binding.NO_ANNOTATIONS;
				}
			}
			if (paramAnnotations != null)
				this.binding.setParameterAnnotations(paramAnnotations);
		}
	}

	/**
	 * Record the thrown exception type bindings in the corresponding type references.
	 */
	public void bindThrownExceptions() {

		if (this.thrownExceptions != null
			&& this.binding != null
			&& this.binding.thrownExceptions != null) {
			int thrownExceptionLength = this.thrownExceptions.length;
			int length = this.binding.thrownExceptions.length;
			if (length == thrownExceptionLength) {
				for (int i = 0; i < length; i++) {
					this.thrownExceptions[i].resolvedType = this.binding.thrownExceptions[i];
				}
			} else {
				int bindingIndex = 0;
				for (int i = 0; i < thrownExceptionLength && bindingIndex < length; i++) {
					TypeReference thrownException = this.thrownExceptions[i];
					ReferenceBinding thrownExceptionBinding = this.binding.thrownExceptions[bindingIndex];
					char[][] bindingCompoundName = thrownExceptionBinding.compoundName;
					if (bindingCompoundName == null) continue; // skip problem case
					if (thrownException instanceof SingleTypeReference) {
						// single type reference
						int lengthName = bindingCompoundName.length;
						char[] thrownExceptionTypeName = thrownException.getTypeName()[0];
						if (CharOperation.equals(thrownExceptionTypeName, bindingCompoundName[lengthName - 1])) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					} else {
						// qualified type reference
						if (CharOperation.equals(thrownException.getTypeName(), bindingCompoundName)) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					}
				}
			}
		}
	}

	public CompilationResult compilationResult() {

		return this.compilationResult;
	}

	/**
	 * Bytecode generation for a method
	 * @param classScope
	 * @param classFile
	 */
	public void generateCode(ClassScope classScope, ClassFile classFile) {

//{ObjectTeams: if copied for implicit inheritance just adjust and write out
        // note: the same code is also inserted into ConstructorDeclaration.generateCode
        if(isRelevantCopied())
        {
        	if (!this.ignoreFurtherInvestigation)
        		new BytecodeTransformer().checkCopyMethodCode(classFile, this);
	        if (this.binding.bytecodeMissing) { // copy did not succeed
				int problemsLength;
				CategorizedProblem[] problems =
					this.scope.referenceCompilationUnit().compilationResult.getProblems();
				CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemConstructor(this, this.binding, problemsCopy);
	        }
            return;
        }
        if ((this.binding != null) && areStatementsMissing() && !isAbstract()) {
        	this.binding.bytecodeMissing = true;
        	return;
        }
// SH}
		int problemResetPC = 0;
		classFile.codeStream.wideMode = false; // reset wideMode to false
		if (this.ignoreFurtherInvestigation) {
			// method is known to have errors, dump a problem method
			if (this.binding == null)
				return; // handle methods with invalid signature or duplicates
			int problemsLength;
			CategorizedProblem[] problems =
				this.scope.referenceCompilationUnit().compilationResult.getProblems();
			CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
			System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
			classFile.addProblemMethod(this, this.binding, problemsCopy);
//{ObjectTeams: mark this erroneous, so we don't try to copy the byte code later:
            if ((!this.binding.isNative()) && (!this.binding.isAbstract()))
                this.binding.bytecodeMissing = true;
            // similarly for contained local types:
            traverse(new ASTVisitor() {
            	public boolean visit(TypeDeclaration type, BlockScope blockScope) {
            		type.tagAsHavingErrors();
            		return true;
            	}
			}, classScope);
// SH}
			return;
		}
		// regular code generation
//{ObjectTeams: callin wrapper for RoFi? -> prepare codeStream for different line ends
		int[] lineEndsSave = null;
		if (this.isMappingWrapper == WrapperKind.CALLIN && MethodModel.isCallinForRoFi(this)) {
			lineEndsSave = classFile.codeStream.lineSeparatorPositions;
			classFile.codeStream.lineSeparatorPositions = this.compilationResult.getLineSeparatorPositions();
			// this leverages the fact that a callin wrapper has the compilation result from the role!
		}
// SH}
		try {
			problemResetPC = classFile.contentsOffset;
			this.generateCode(classFile);
		} catch (AbortMethod e) {
			// a fatal error was detected during code generation, need to restart code gen if possible
			if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
				// a branch target required a goto_w, restart code gen in wide mode.
				try {
					classFile.contentsOffset = problemResetPC;
					classFile.methodCount--;
					classFile.codeStream.resetInWideMode(); // request wide mode
					this.generateCode(classFile); // restart method generation
				} catch (AbortMethod e2) {
					int problemsLength;
					CategorizedProblem[] problems =
						this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
					CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
					System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
					classFile.addProblemMethod(this, this.binding, problemsCopy, problemResetPC);
				}
			} else {
				// produce a problem method accounting for this fatal error
				int problemsLength;
				CategorizedProblem[] problems =
					this.scope.referenceCompilationUnit().compilationResult.getAllProblems();
				CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
				System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
				classFile.addProblemMethod(this, this.binding, problemsCopy, problemResetPC);
			}
//{ObjectTeams: resetting from above
		} finally {
			if (lineEndsSave != null)
				classFile.codeStream.lineSeparatorPositions = lineEndsSave;
// SH}
		}
	}

	public void generateCode(ClassFile classFile) {

		classFile.generateMethodInfoHeader(this.binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(this.binding);
//{ObjectTeams: write OT-specific byte code attributes
        if (this.model != null)
            attributeNumber += this.model.writeAttributes(classFile);
// SH}
		if ((!this.binding.isNative()) && (!this.binding.isAbstract())) {
			int codeAttributeOffset = classFile.contentsOffset;
			classFile.generateCodeAttributeHeader();
			CodeStream codeStream = classFile.codeStream;
			codeStream.reset(this, classFile);
//{ObjectTeams:	static role methods have a synthetic team reference:
		  // cf. ConstructorDeclaration.internalGenerateCode().
		  ReferenceBinding declaringClass = this.binding.declaringClass;
		  SyntheticArgumentBinding syntheticArgumentBinding= null;
		  if (this.binding.needsSyntheticEnclosingTeamInstance()) {
			MemberTypeBinding nestedType = (MemberTypeBinding) declaringClass;
			this.scope.computeLocalVariablePositions(// consider synthetic arguments if any
				nestedType.getEnclosingInstancesSlotSize() + 1, // 1 for a dummy this arg
				codeStream);
			// add dummy argument only now, so it won't be resolved after regular args:
			syntheticArgumentBinding = new SyntheticArgumentBinding(nestedType.enclosingType());
			syntheticArgumentBinding.resolvedPosition= 1; // constantly;
			this.scope.extraSyntheticArguments= new SyntheticArgumentBinding[] { syntheticArgumentBinding };
			codeStream.record(syntheticArgumentBinding);
			syntheticArgumentBinding.recordInitializationStartPC(0);
		  } else
// SH}
			// initialize local positions
			this.scope.computeLocalVariablePositions(this.binding.isStatic() ? 0 : 1, codeStream);
//{ObjectTeams: runtime check in creation method?
	        if (this.binding.roleCreatorRequiringRuntimeCheck)
	        	Lifting.createDuplicateRoleCheck(codeStream, this);
// SH}
			// arguments initialization for local variable debug attributes
			if (this.arguments != null) {
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					LocalVariableBinding argBinding;
					codeStream.addVisibleLocalVariable(argBinding = this.arguments[i].binding);
					argBinding.recordInitializationStartPC(0);
				}
			}
			if (this.statements != null) {
				for (int i = 0, max = this.statements.length; i < max; i++)
					this.statements[i].generateCode(this.scope, codeStream);
			}
			// if a problem got reported during code gen, then trigger problem method creation
			if (this.ignoreFurtherInvestigation) {
				throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
			}
//{ObjectTeams: fieldInit-hook
			endOfMethodHook(classFile);
// SH}
			if ((this.bits & ASTNode.NeedFreeReturn) != 0) {
				codeStream.return_();
			}
			// local variable attributes
			codeStream.exitUserScope(this.scope);
			codeStream.recordPositionsFrom(0, this.declarationSourceEnd);
//{ObjectTeams: finish faked argument for static role method:
			if (   syntheticArgumentBinding != null
				&& syntheticArgumentBinding.initializationPCs != null) // null happens when codeStream doesn't care about positions
				syntheticArgumentBinding.recordInitializationEndPC(classFile.codeStream.position);
// SH}
			try {
				classFile.completeCodeAttribute(codeAttributeOffset);
			} catch (NegativeArraySizeException e) {
				throw new AbortMethod(this.scope.referenceCompilationUnit().compilationResult, null);
			}
//{ObjectTeams: record the completed byte code
            maybeRecordByteCode(classFile, methodAttributeOffset-6); // include method header
// SH}
			attributeNumber++;
		} else {
			checkArgumentsSize();
		}
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}
//{ObjectTeams: recording byte code
	public void maybeRecordByteCode(ClassFile classFile, int methodAttributeOffset) {
		RoleModel.maybeRecordByteCode(
		        this,
		        classFile,
		        methodAttributeOffset);
		boolean shouldRecordTeamMethod = false;
		if (this.isMappingWrapper._callin()) {
			AbstractMethodMappingDeclaration mapping = this.model._declaringMapping;
			if (   mapping != null 
				&& mapping.isCallin()
				&& mapping.roleMethodSpec.isPrivate())
				shouldRecordTeamMethod = true;
		} else if (this.binding.declaringClass.id == IOTConstants.T_OrgObjectTeamsTeam) {
			if (this.scope.environment().getTeamMethodGenerator().registerSourceMethodBytes(this.binding))
				shouldRecordTeamMethod = true;
		}
		if (shouldRecordTeamMethod)
			MethodModel.getModel(this).recordByteCode(classFile, methodAttributeOffset);
	}
// SH}	
//{ObjectTeams: new hook
	/**
	 * Hook for adding statements just before the final return.
	 * Used for field initialization by RoleInitializationMethod.
	 */
	protected void endOfMethodHook(ClassFile classfile) {
		// NOOP.
	}
// SH}

	private void checkArgumentsSize() {
		TypeBinding[] parameters = this.binding.parameters;
		int size = 1; // an abstact method or a native method cannot be static
		for (int i = 0, max = parameters.length; i < max; i++) {
			switch(parameters[i].id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					size += 2;
					break;
				default :
					size++;
					break;
			}
			if (size > 0xFF) {
				this.scope.problemReporter().noMoreAvailableSpaceForArgument(this.scope.locals[i], this.scope.locals[i].declaration);
			}
		}
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public boolean isAbstract() {

		if (this.binding != null)
			return this.binding.isAbstract();
		return (this.modifiers & ClassFileConstants.AccAbstract) != 0;
	}

	public boolean isAnnotationMethod() {

		return false;
	}

	public boolean isClinit() {

		return false;
	}

	public boolean isConstructor() {

		return false;
	}

	public boolean isDefaultConstructor() {

		return false;
	}

	public boolean isInitializationMethod() {

		return false;
	}

	public boolean isMethod() {

		return false;
	}

	public boolean isNative() {

		if (this.binding != null)
			return this.binding.isNative();
		return (this.modifiers & ClassFileConstants.AccNative) != 0;
	}

	public boolean isStatic() {

		if (this.binding != null)
			return this.binding.isStatic();
		return (this.modifiers & ClassFileConstants.AccStatic) != 0;
	}

	/**
	 * Fill up the method body with statement
	 * @param parser
	 * @param unit
	 */
	public abstract void parseStatements(Parser parser, CompilationUnitDeclaration unit);

	public StringBuffer print(int tab, StringBuffer output) {

		if (this.javadoc != null) {
			this.javadoc.print(tab, output);
		}
		printIndent(tab, output);
		printModifiers(this.modifiers, output);
		if (this.annotations != null) printAnnotations(this.annotations, output);

		TypeParameter[] typeParams = typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				typeParams[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeParams[max].print(0, output);
			output.append('>');
		}

		printReturnType(0, output).append(this.selector).append('(');
		if (this.arguments != null) {
//{ObjectTeams: retrench enhanced callin args:
			int firstArg = 0;
			if (isCallin())
				firstArg = MethodSignatureEnhancer.ENHANCING_ARG_LEN;
			for (int i = firstArg; i < this.arguments.length; i++) {
/* orig:
			for (int i = 0; i < this.arguments.length; i++) {
  :giro */
// SH}
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (this.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < this.thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(tab + 1, output);
		return output;
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		if (isAbstract() || (this.modifiers & ExtraCompilerModifiers.AccSemicolonBody) != 0)
			return output.append(';');

		output.append(" {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (int i = 0; i < this.statements.length; i++) {
				output.append('\n');
				this.statements[i].printStatement(indent, output);
			}
		}
//{ObjectTeams: signal missing body for copy inherited methods
		else
		{
			if(this.isCopied==true && this.statements == null){
				output.append("\n"); //$NON-NLS-1$
				printIndent(indent == 0 ? 0 : indent - 1,output);
				output.append("     /* CopyInheritance */"); //$NON-NLS-1$
			}
		}
//Markus Witte}
		output.append('\n');
		printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
		return output;
	}

	public StringBuffer printReturnType(int indent, StringBuffer output) {

		return output;
	}

	public void resolve(ClassScope upperScope) {

		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
		}

		try {
			bindArguments();
			bindThrownExceptions();
//{ObjectTeams: no javadoc in generated / copied methods
		  if (!(this.isGenerated || this.isCopied))
// SH}
			resolveJavadoc();
			resolveAnnotations(this.scope, this.annotations, this.binding);
			resolveStatements();
			// check @Deprecated annotation presence
			if (this.binding != null
					&& (this.binding.getAnnotationTagBits() & TagBits.AnnotationDeprecated) == 0
					&& (this.binding.modifiers & ClassFileConstants.AccDeprecated) != 0
					&& this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
				this.scope.problemReporter().missingDeprecatedAnnotationForMethod(this);
			}
		} catch (AbortMethod e) {
			// ========= abort on fatal error =============
			this.ignoreFurtherInvestigation = true;
		}
	}

	public void resolveJavadoc() {

		if (this.binding == null) return;
		if (this.javadoc != null) {
			this.javadoc.resolve(this.scope);
			return;
		}
//{ObjectTeams: do nothing for generated methods:
		if (this.scope.isGeneratedScope())
			return;
// SH}
		if (this.binding.declaringClass != null && !this.binding.declaringClass.isLocalType()) {
			// Set javadoc visibility
			int javadocVisibility = this.binding.modifiers & ExtraCompilerModifiers.AccVisibilityMASK;
			ClassScope classScope = this.scope.classScope();
			ProblemReporter reporter = this.scope.problemReporter();
			int severity = reporter.computeSeverity(IProblem.JavadocMissing);
			if (severity != ProblemSeverities.Ignore) {
				if (classScope != null) {
					javadocVisibility = Util.computeOuterMostVisibility(classScope.referenceType(), javadocVisibility);
				}
				int javadocModifiers = (this.binding.modifiers & ~ExtraCompilerModifiers.AccVisibilityMASK) | javadocVisibility;
				reporter.javadocMissing(this.sourceStart, this.sourceEnd, severity, javadocModifiers);
			}
		}
	}

	public void resolveStatements() {
//{ObjectTeams:
//      by now the signature is all prepared and wrapped:
        AnchorListAttribute.checkAddAnchorList(this);
//      still waiting for statements to be generated?
        if (this.isGenerated && this.statements == null)
			return;
        // currently only for callins within types with errors:
        // TODO: could this obsolete STATE_STATEMENTS_TRANSFORMED?
        TransformStatementsVisitor.checkTransformStatements(this);
// SH}

//{ObjectTeams: prepare Config for InsertTypeAdjustmentsVisitor:
      Config oldConfig = Config.createOrResetConfig(this);
	  try {
	// orig:
		if (this.statements != null) {
			for (int i = 0, length = this.statements.length; i < length; i++) {
				this.statements[i].resolve(this.scope);
	// :giro
				// order in this condition is relevant, first part resets flags!
	            if (   Config.requireTypeAdjustment()
	               	&& !this.scope.problemReporter().referenceContext.hasErrors())
	            {
	                this.statements[i].traverse(new InsertTypeAdjustmentsVisitor(), this.scope);
	            }
// SH}
			}
		} else if ((this.bits & UndocumentedEmptyBlock) != 0) {
			this.scope.problemReporter().undocumentedEmptyBlock(this.bodyStart-1, this.bodyEnd+1);
		}
//{ObjectTeams:
	  } finally {
	   	Config.removeOrRestore(oldConfig, this);
	  }
// SH}
	}

	public void tagAsHavingErrors() {
		this.ignoreFurtherInvestigation = true;
	}

//{ObjectTeams: and remove it again:
	public void resetErrorFlag() {
		this.ignoreFurtherInvestigation = false;
	}
// SH}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {
		// default implementation: subclass will define it
	}

	public TypeParameter[] typeParameters() {
	    return null;
	}
}
