/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.codeassist.select.SelectionNodeFound;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel.FakeKind;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TSuperHelper;

/**
 * OTDT changes:
 *
 * What: new query isDeclaredLifting
 *
 * What: Support multiple strategies for resolve
 * 		 + base import scope
 *       + anchored role type
 *
 * What: Support baseclass decapsulation
 *
 * @version $Id: TypeReference.java 23404 2010-02-03 14:10:22Z stephan $
 */
public abstract class TypeReference extends Expression {

	public static final TypeReference[] NO_TYPE_ARGUMENTS = new TypeReference[0];
	static class AnnotationCollector extends ASTVisitor {
		List annotationContexts;
		TypeReference typeReference;
		int targetType;
		Annotation[] primaryAnnotations;
		int info = -1;
		int info2 = -1;
		LocalVariableBinding localVariable;
		Annotation[][] annotationsOnDimensions;
		Wildcard currentWildcard;

		public AnnotationCollector(
				TypeParameter typeParameter,
				int targetType,
				int typeParameterIndex,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = typeParameter.type;
			this.targetType = targetType;
			this.primaryAnnotations = typeParameter.annotations;
			this.info = typeParameterIndex;
		}

		public AnnotationCollector(
				LocalDeclaration localDeclaration,
				int targetType,
				LocalVariableBinding localVariable,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = localDeclaration.type;
			this.targetType = targetType;
			this.primaryAnnotations = localDeclaration.annotations;
			this.localVariable = localVariable;
		}

		public AnnotationCollector(
				LocalDeclaration localDeclaration,
				int targetType,
				int parameterIndex,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = localDeclaration.type;
			this.targetType = targetType;
			this.primaryAnnotations = localDeclaration.annotations;
			this.info = parameterIndex;
		}

		public AnnotationCollector(
				MethodDeclaration methodDeclaration,
				int targetType,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = methodDeclaration.returnType;
			this.targetType = targetType;
			this.primaryAnnotations = methodDeclaration.annotations;
		}

		public AnnotationCollector(
				FieldDeclaration fieldDeclaration,
				int targetType,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = fieldDeclaration.type;
			this.targetType = targetType;
			this.primaryAnnotations = fieldDeclaration.annotations;
		}
		public AnnotationCollector(
				TypeReference typeReference,
				int targetType,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = typeReference;
			this.targetType = targetType;
		}
		public AnnotationCollector(
				TypeReference typeReference,
				int targetType,
				int info,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = typeReference;
			this.info = info;
			this.targetType = targetType;
		}
		public AnnotationCollector(
				TypeReference typeReference,
				int targetType,
				int info,
				int typeIndex,
				List annotationContexts) {
			this.annotationContexts = annotationContexts;
			this.typeReference = typeReference;
			this.info = info;
			this.targetType = targetType;
			this.info2 = typeIndex;
		}
		public AnnotationCollector(
				TypeReference typeReference,
				int targetType,
				int info,
				List annotationContexts,
				Annotation[][] annotationsOnDimensions) {
			this.annotationContexts = annotationContexts;
			this.typeReference = typeReference;
			this.info = info;
			this.targetType = targetType;
			this.annotationsOnDimensions = annotationsOnDimensions;
		}
		private boolean internalVisit(Annotation annotation) {
			AnnotationContext annotationContext = null;
			if (annotation.isRuntimeTypeInvisible()) {
				annotationContext = new AnnotationContext(annotation, this.typeReference, this.targetType, this.primaryAnnotations, AnnotationContext.INVISIBLE, this.annotationsOnDimensions);
			} else if (annotation.isRuntimeTypeVisible()) {
				annotationContext = new AnnotationContext(annotation, this.typeReference, this.targetType, this.primaryAnnotations, AnnotationContext.VISIBLE, this.annotationsOnDimensions);
			}
			if (annotationContext != null) {
				annotationContext.wildcard = this.currentWildcard;
				switch(this.targetType) {
					case AnnotationTargetTypeConstants.THROWS :
					case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER :
					case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER :
					case AnnotationTargetTypeConstants.METHOD_PARAMETER :
					case AnnotationTargetTypeConstants.TYPE_CAST :
					case AnnotationTargetTypeConstants.TYPE_INSTANCEOF :
					case AnnotationTargetTypeConstants.OBJECT_CREATION :
					case AnnotationTargetTypeConstants.CLASS_LITERAL :
					case AnnotationTargetTypeConstants.CLASS_EXTENDS_IMPLEMENTS:
						annotationContext.info = this.info;
						break;
					case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND :
					case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND :
						annotationContext.info2 = this.info2;
						annotationContext.info = this.info;
						break;
					case AnnotationTargetTypeConstants.LOCAL_VARIABLE :
						annotationContext.variableBinding = this.localVariable;
						break;
					case AnnotationTargetTypeConstants.TYPE_ARGUMENT_METHOD_CALL :
					case AnnotationTargetTypeConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL :
						annotationContext.info2 = this.info2;
						annotationContext.info = this.info;
				}
				this.annotationContexts.add(annotationContext);
			}
			return true;
		}
		public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
			return internalVisit(annotation);
		}
		public boolean visit(NormalAnnotation annotation, BlockScope scope) {
			return internalVisit(annotation);
		}
		public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
			return internalVisit(annotation);
		}
		public boolean visit(Wildcard wildcard, BlockScope scope) {
			this.currentWildcard = wildcard;
			return true;
		}
		public boolean visit(Argument argument, BlockScope scope) {
			if ((argument.bits & ASTNode.IsUnionType) == 0) {
				return true;
			}
			for (int i = 0, max = this.localVariable.initializationCount; i < max; i++) {
				int startPC = this.localVariable.initializationPCs[i << 1];
				int endPC = this.localVariable.initializationPCs[(i << 1) + 1];
				if (startPC != endPC) { // only entries for non zero length
					return true;
				}
			}
			return false;
		}
		public boolean visit(Argument argument, ClassScope scope) {
			if ((argument.bits & ASTNode.IsUnionType) == 0) {
				return true;
			}
			for (int i = 0, max = this.localVariable.initializationCount; i < max; i++) {
				int startPC = this.localVariable.initializationPCs[i << 1];
				int endPC = this.localVariable.initializationPCs[(i << 1) + 1];
				if (startPC != endPC) { // only entries for non zero length
					return true;
				}
			}
			return false;
		}
		public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
			for (int i = 0, max = this.localVariable.initializationCount; i < max; i++) {
				int startPC = this.localVariable.initializationPCs[i << 1];
				int endPC = this.localVariable.initializationPCs[(i << 1) + 1];
				if (startPC != endPC) { // only entries for non zero length
					return true;
				}
			}
			return false;
		}
		public void endVisit(Wildcard wildcard, BlockScope scope) {
			this.currentWildcard = null;
		}
	}

//{ObjectTeams: for baseclass decapsulation (implement interface from Expression):
private DecapsulationState baseclassDecapsulation = DecapsulationState.NONE;
public void setBaseclassDecapsulation(DecapsulationState state) {
	this.baseclassDecapsulation = state;
}
@Override
public DecapsulationState getBaseclassDecapsulation() {
	return this.baseclassDecapsulation;
}
@Override
public void tagReportedBaseclassDecapsulation() {
	setBaseclassDecapsulation(DecapsulationState.REPORTED);
}
public int deprecationProblemId = IProblem.UsingDeprecatedType;
// SH}

/*
 * Answer a base type reference (can be an array of base type).
 */
public static final TypeReference baseTypeReference(int baseType, int dim, Annotation[][] dimAnnotations) {

	if (dim == 0) {
		switch (baseType) {
			case (TypeIds.T_void) :
				return new SingleTypeReference(TypeBinding.VOID.simpleName, 0);
			case (TypeIds.T_boolean) :
				return new SingleTypeReference(TypeBinding.BOOLEAN.simpleName, 0);
			case (TypeIds.T_char) :
				return new SingleTypeReference(TypeBinding.CHAR.simpleName, 0);
			case (TypeIds.T_float) :
				return new SingleTypeReference(TypeBinding.FLOAT.simpleName, 0);
			case (TypeIds.T_double) :
				return new SingleTypeReference(TypeBinding.DOUBLE.simpleName, 0);
			case (TypeIds.T_byte) :
				return new SingleTypeReference(TypeBinding.BYTE.simpleName, 0);
			case (TypeIds.T_short) :
				return new SingleTypeReference(TypeBinding.SHORT.simpleName, 0);
			case (TypeIds.T_int) :
				return new SingleTypeReference(TypeBinding.INT.simpleName, 0);
			default : //T_long
				return new SingleTypeReference(TypeBinding.LONG.simpleName, 0);
		}
	}
	switch (baseType) {
		case (TypeIds.T_void) :
			return new ArrayTypeReference(TypeBinding.VOID.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_boolean) :
			return new ArrayTypeReference(TypeBinding.BOOLEAN.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_char) :
			return new ArrayTypeReference(TypeBinding.CHAR.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_float) :
			return new ArrayTypeReference(TypeBinding.FLOAT.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_double) :
			return new ArrayTypeReference(TypeBinding.DOUBLE.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_byte) :
			return new ArrayTypeReference(TypeBinding.BYTE.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_short) :
			return new ArrayTypeReference(TypeBinding.SHORT.simpleName, dim, dimAnnotations, 0);
		case (TypeIds.T_int) :
			return new ArrayTypeReference(TypeBinding.INT.simpleName, dim, dimAnnotations, 0);
		default : //T_long
			return new ArrayTypeReference(TypeBinding.LONG.simpleName, dim, dimAnnotations, 0);
	}
}

// JSR308 type annotations...
public Annotation[][] annotations = null;

// allows us to trap completion & selection nodes
public void aboutToResolve(Scope scope) {
	// default implementation: do nothing
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}
public void checkBounds(Scope scope) {
	// only parameterized type references have bounds
}
public abstract TypeReference copyDims(int dim);
public abstract TypeReference copyDims(int dim, Annotation[][] annotationsOnDimensions);
public int dimensions() {
	return 0;
}
public AnnotationContext[] getAllAnnotationContexts(int targetType) {
	List allAnnotationContexts = new ArrayList();
	AnnotationCollector collector = new AnnotationCollector(this, targetType, allAnnotationContexts);
	this.traverse(collector, (BlockScope) null);
	return (AnnotationContext[]) allAnnotationContexts.toArray(new AnnotationContext[allAnnotationContexts.size()]);
}
/**
 * info can be either a type index (superclass/superinterfaces) or a pc into the bytecode
 * @param targetType
 * @param info
 * @param allAnnotationContexts
 */
public void getAllAnnotationContexts(int targetType, int info, List allAnnotationContexts) {
	AnnotationCollector collector = new AnnotationCollector(this, targetType, info, allAnnotationContexts);
	this.traverse(collector, (BlockScope) null);
}
/**
 * info can be either a type index (superclass/superinterfaces) or a pc into the bytecode
 * @param targetType
 * @param info
 * @param allAnnotationContexts
 */
public void getAllAnnotationContexts(int targetType, int info, List allAnnotationContexts, Annotation[][] annotationsOnDimensions) {
	AnnotationCollector collector = new AnnotationCollector(this, targetType, info, allAnnotationContexts, annotationsOnDimensions);
	this.traverse(collector, (BlockScope) null);
	if (annotationsOnDimensions != null) {
		for (int i = 0, max = annotationsOnDimensions.length; i < max; i++) {
			Annotation[] annotationsOnDimension = annotationsOnDimensions[i];
			if (annotationsOnDimension != null) {
				for (int j = 0, max2 = annotationsOnDimension.length; j< max2; j++) {
					annotationsOnDimension[j].traverse(collector, (BlockScope) null);
				}
			}
		}
	}
}
public void getAllAnnotationContexts(int targetType, int info, int typeIndex, List allAnnotationContexts) {
	AnnotationCollector collector = new AnnotationCollector(this, targetType, info, typeIndex, allAnnotationContexts);
	this.traverse(collector, (BlockScope) null);
}
public void getAllAnnotationContexts(int targetType, List allAnnotationContexts) {
	AnnotationCollector collector = new AnnotationCollector(this, targetType, allAnnotationContexts);
	this.traverse(collector, (BlockScope) null);
}
public Annotation[][] getAnnotationsOnDimensions() {
	return null;
}

public void setAnnotationsOnDimensions(Annotation [][] annotationsOnDimensions) {
	// nothing to do. Subtypes should react suitably.
}

public abstract char[] getLastToken();

/**
 * @return char[][]
 * TODO (jerome) should merge back into #getTypeName()
 */
public char [][] getParameterizedTypeName(){
	return getTypeName();
}
protected abstract TypeBinding getTypeBinding(Scope scope);
/**
 * @return char[][]
 */
public abstract char [][] getTypeName() ;

protected TypeBinding internalResolveType(Scope scope) {
	// handle the error here
	this.constant = Constant.NotAConstant;
	if (this.resolvedType != null) { // is a shared type reference which was already resolved
		if (this.resolvedType.isValidBinding()) {
			return this.resolvedType;
		} else {
			switch (this.resolvedType.problemId()) {
				case ProblemReasons.NotFound :
				case ProblemReasons.NotVisible :
				case ProblemReasons.InheritedNameHidesEnclosingName :
					TypeBinding type = this.resolvedType.closestMatch();
					if (type == null) return null;
					return scope.environment().convertToRawType(type, false /*do not force conversion of enclosing types*/);
				default :
					return null;
			}
		}
	}
	boolean hasError;
//{ObjectTeams: don't let SelectionNodeFound(null) prevent alternate searching strategies:
  SelectionNodeFound caughtException = null;
  TypeBinding type = null;
  try {
	// base import scope first:
	CompilationResult compilationResult = scope.referenceCompilationUnit().compilationResult();
	CompilationResult.CheckPoint cp = compilationResult.getCheckPoint(scope.referenceContext());
	try {
	  type = checkResolveUsingBaseImportScope(scope, false); // apply TOLERATE strategy only as a last resort below
	  // copied from below:
	  if (type != null && type.isValidBinding()) {
		type = scope.environment().convertToRawType(type, false /*do not force conversion of enclosing types*/);
		if (type.leafComponentType().isRawType()
				&& (this.bits & ASTNode.IgnoreRawTypeCheck) == 0
				&& scope.compilerOptions().getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
			scope.problemReporter().rawTypeReference(this, type);
		}
		return type;
	  }
	} catch (SelectionNodeFound snf) {
		if (snf.binding != null)
			throw snf; // found a valid node.
		caughtException = snf;
	} finally {
		if (   caughtException != null
			|| (type == null)
			|| !type.isValidBinding())
			compilationResult.rollBack(cp);
	}
// ~orig~:
	type = this.resolvedType = getTypeBinding(scope);
	if (type == null) {
		return null; // detected cycle while resolving hierarchy
	}
// :~giro~
  } catch (SelectionNodeFound snf) {
	if (snf.binding != null)
		throw snf; // found a valid node.
	caughtException = snf;
  }
// a third chance trying an anchored type:
  try {
    if (   (caughtException != null)
    	|| (this.resolvedType.problemId() == ProblemReasons.NotFound))
	{
    	// anchored type
	    TypeBinding result = resolveAnchoredType(scope);
	    if (result != null)             // did we do any better than before?
	    	type = this.resolvedType = result; // if non-null but ProblemBinding report below.
    }
  } catch (SelectionNodeFound snf2) {
	  caughtException = snf2; // throw the newer exception instead.
  }
//a forth chance trying a TOLERATED base imported type:
 try {
   if (   (caughtException != null)
	   || (this.resolvedType.problemId() == ProblemReasons.NotFound))
	{
   		if (this.baseclassDecapsulation == DecapsulationState.TOLERATED) {
   			TypeBinding result = checkResolveUsingBaseImportScope(scope, true);
   			if (result != null)             // did we do any better than before?
   				type = this.resolvedType = result; // if non-null but ProblemBinding report below.
   		}
   }
 } catch (SelectionNodeFound snf2) {
	  caughtException = snf2; // throw the newer exception instead.
 } finally {
	  // the attempt to prevent an exception failed:
	  if (caughtException != null)
		  throw caughtException;
 }
// SH}
	if ((hasError = !type.isValidBinding()) == true) {
		reportInvalidType(scope);
		switch (type.problemId()) {
			case ProblemReasons.NotFound :
			case ProblemReasons.NotVisible :
			case ProblemReasons.InheritedNameHidesEnclosingName :
				type = type.closestMatch();
				if (type == null) return null;
				break;
			default :
				return null;
		}
	}
//{ObjectTeams: Split method to make tail accessible:
	return checkResolvedType(type, scope, hasError);
}
public TypeBinding checkResolvedType(TypeBinding type, Scope scope, boolean hasError) {
// SH}
	if (type.isArrayType() && ((ArrayBinding) type).leafComponentType == TypeBinding.VOID) {
		scope.problemReporter().cannotAllocateVoidArray(this);
		return null;
	}
	if (!(this instanceof QualifiedTypeReference)   // QualifiedTypeReference#getTypeBinding called above will have already checked deprecation
			&& isTypeUseDeprecated(type, scope)) {
		reportDeprecatedType(type, scope);
	}
	type = scope.environment().convertToRawType(type, false /*do not force conversion of enclosing types*/);
	if (type.leafComponentType().isRawType()
			&& (this.bits & ASTNode.IgnoreRawTypeCheck) == 0
			&& scope.compilerOptions().getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
		scope.problemReporter().rawTypeReference(this, type);
	}
	if (this.annotations != null) {
		switch(scope.kind) {
			case Scope.BLOCK_SCOPE :
			case Scope.METHOD_SCOPE :
				resolveAnnotations((BlockScope) scope, this.annotations, new Annotation.TypeUseBinding(Binding.TYPE_USE));
				break;
		}
	}

	if (hasError) {
		// do not store the computed type, keep the problem type instead
		return type;
	}
	return this.resolvedType = type;
}

//{ObjectTeams: alternative strategies for resolving:
/** Try to resolve this reference from base imports. */
public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
	return null; // override to do something useful (only in SingleTypeReference).
}
/**
 * Try to resolve this type reference as an anchored type "t.R".
 */
TypeBinding resolveAnchoredType(Scope scope) {
	return null; // override to do something useful
}
protected boolean shouldAnalyzeRoleReference() {
	if ((this.bits & ASTNode.InsideJavadoc) != 0)
		return false; // don't complain about role encapsulation in java doc
	return   this.resolvedType.isRole()  // no need to check non-role inner of role: either inherited (non-rolish) or local (cannot be referenced)
		  && !TSuperHelper.isMarkerInterface(this.resolvedType);
}
/* check for use of protected role (for parameterized/qualified type references). */
protected boolean isIllegalQualifiedUseOfProtectedRole(Scope scope)
{
	  // implies ReferenceBinding
	if (   ((ReferenceBinding)this.resolvedType).isProtected() // here we only check protected roles
		&& !this.getBaseclassDecapsulation().isAllowed())
	{
		scope.problemReporter().qualifiedProtectedRole(this, (ReferenceBinding)this.resolvedType);
		// keep a problem binding,  clients may be interested in this information, see CodeSelectionTests.testRoleCreation3
		this.resolvedType = new ProblemReferenceBinding(((ReferenceBinding)this.resolvedType).compoundName, (ReferenceBinding)this.resolvedType, ProblemReasons.NotVisible);
		return true;
	}
	return false;
}
// SH}
public boolean isTypeReference() {
	return true;
}
//{ObjectTeams
public boolean isDeclaredLifting() {
	  return false;
}
//Markus Witte}

public boolean isParameterizedTypeReference() {
	return false;
}

protected void reportDeprecatedType(TypeBinding type, Scope scope, int index) {
	scope.problemReporter().deprecatedType(type, this, index);
}

protected void reportDeprecatedType(TypeBinding type, Scope scope) {
//{ObjectTeams:
  if (this.deprecationProblemId == 0)
	return;
  if (this.deprecationProblemId == IProblem.DeprecatedBaseclass)
	scope.problemReporter().deprecatedBaseclass(this, type);
  else
// SH}
	scope.problemReporter().deprecatedType(type, this, Integer.MAX_VALUE);
}

protected void reportInvalidType(Scope scope) {
//{ObjectTeams: suppress this in role feature bridge (the same will be reported against the original, too):
	if (scope.isFakeMethod(FakeKind.ROLE_FEATURE_BRIDGE)) {
		scope.referenceContext().tagAsHavingErrors();
		return;
	}
	// did we misread an OT keyword as a type reference (during syntax recovery)?
	if (!scope.environment().globalOptions.isPureJava) {
		char[] token = getLastToken();
        for (int j = 0; j < IOTConstants.OT_KEYWORDS.length; j++) {
			if (CharOperation.equals(token, IOTConstants.OT_KEYWORDS[j])) {
				if (scope.referenceContext().compilationResult().hasErrors())
					return; // assume this is a secondary error
			}
		}
	}
// SH}
	scope.problemReporter().invalidType(this, this.resolvedType);
}

public TypeBinding resolveSuperType(ClassScope scope) {
	// assumes the implementation of resolveType(ClassScope) will call back to detect cycles
	TypeBinding superType = resolveType(scope);
	if (superType == null) return null;

	if (superType.isTypeVariable()) {
		if (this.resolvedType.isValidBinding()) {
			this.resolvedType = new ProblemReferenceBinding(getTypeName(), (ReferenceBinding)this.resolvedType, ProblemReasons.IllegalSuperTypeVariable);
			reportInvalidType(scope);
		}
		return null;
	}
	return superType;
}

public final TypeBinding resolveType(BlockScope blockScope) {
	return resolveType(blockScope, true /* checkbounds if any */);
}

public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
	return internalResolveType(scope);
}

public TypeBinding resolveType(ClassScope scope) {
	return internalResolveType(scope);
}

public TypeBinding resolveTypeArgument(BlockScope blockScope, ReferenceBinding genericType, int rank) {
    return resolveType(blockScope, true /* check bounds*/);
}

public TypeBinding resolveTypeArgument(ClassScope classScope, ReferenceBinding genericType, int rank) {
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=294057, circularity is allowed when we are
	// resolving type arguments i.e interface A<T extends C> {}	interface B extends A<D> {}
	// interface D extends C {}	interface C extends B {}
	ReferenceBinding ref = classScope.referenceContext.binding;
	boolean pauseHierarchyCheck = false;
	try {
		if (ref.isHierarchyBeingConnected()) {
			ref.tagBits |= TagBits.PauseHierarchyCheck;
			pauseHierarchyCheck = true;
		}
	    return resolveType(classScope);
	} finally {
		if (pauseHierarchyCheck) {
			ref.tagBits &= ~TagBits.PauseHierarchyCheck;
		}
	}
}

public abstract void traverse(ASTVisitor visitor, BlockScope scope);

public abstract void traverse(ASTVisitor visitor, ClassScope scope);

protected void resolveAnnotations(BlockScope scope) {
	if (this.annotations != null) {
		resolveAnnotations(scope, this.annotations, new Annotation.TypeUseBinding(Binding.TYPE_USE));
	}
}
public int getAnnotatableLevels() {
	return 1;
}
}
