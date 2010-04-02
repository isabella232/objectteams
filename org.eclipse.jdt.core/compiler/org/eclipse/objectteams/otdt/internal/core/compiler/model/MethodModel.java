/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodModel.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Fraunhofer FIRST - Initial API and implementation
 * 		Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.model;

import java.io.IOException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.AbstractAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.PlainAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.WordValueAttribute;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.IStatementsGenerator;

/**
 * A fragment of method {@link #toString()} has been copied from 
 * {@link AbstractMethodDeclaration} of the Eclipse JDT. 
 * 
 * MIGRATION_STATE: complete.
 *
 * What: Flag abstract creation methods in non-abstract team
 * Why:  Must forbid their use.
 *       Mediates between CopyInheritance.internalCreateCreationMethod() and MessageSend.resolve().
 *
 * @author stephan
 * @version $Id: MethodModel.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class MethodModel extends ModelElement {

	public static final int AccIfcMethodModiferMASK = ExtraCompilerModifiers.AccVisibilityMASK|ClassFileConstants.AccStatic;

    public static MethodModel getModel(AbstractMethodDeclaration decl) {
        MethodModel model = decl.model;
        if (model == null) {
        	if (decl.binding != null)
        		model= decl.binding.model;
        	if (model != null)
        		decl.model= model;
        	else
        		model = new MethodModel(decl);
        }
        return model;
    }
    public static MethodModel getModel(MethodBinding binding) {
    	MethodModel model = binding.model;
    	if (model == null)
    		model = new MethodModel(binding);
    	return model;
    }
    public void linkBinding(MethodBinding binding) {
    	this._binding = binding;
		binding.model = this;
	}

    private AbstractMethodDeclaration _decl     		= null;
    private MethodBinding 			  _binding 	        = null;
    private boolean    				  _callsBaseCtor    = false;
   	public  int                       callinFlags       = 0;
   	public  boolean                   isCallinForRoFi   = false;
   	// for method generated from a method mapping (currently: callin only):
   	public  AbstractMethodMappingDeclaration _declaringMapping = null;

    // for methods residing in a team but belonging to a role store the role type here:
    public TypeDeclaration _sourceDeclaringType = null;
    // also what was originally a 'this' reference may now be passed as first argument: 
	public Argument _thisSubstitution;

    // for creation methods store the original constructor here:
    public MethodBinding _srcCtor = null;

    // offset between byte code line number and source code line number
    public int _lineOffset;

	// for a callin method record all exceptions declared by bound base methods:
    public Set<ReferenceBinding> _baseExceptions;
    public void addBaseExceptions(ReferenceBinding[] exceptions) {
    	if (this._baseExceptions == null)
    		this._baseExceptions = new HashSet<ReferenceBinding> ();
    	for (ReferenceBinding exception : exceptions)
    		this._baseExceptions.add(exception);
    }

    /** Flag for transfering a bit from generated AST to binding. */
    public boolean _clearPrivateModifier = false;

   	public static enum ProblemDetail {
   		NoProblem,
		RoleInheritsNonPublic, // a non-public method inherited from a regular super class
		IllegalDefaultCtor     // a ctor in a bound role which does not assign the base reference
	}
	public ProblemDetail problemDetail;

	/** If a problem has been recorded with this method, report it now.
     * @return true iff a special problem has been reported.
	 */
	public boolean handleError(ProblemReporter reporter, MessageSend messageSend) {
		switch (this.problemDetail) {
		case RoleInheritsNonPublic:
			reporter.callToInheritedNonPublic(messageSend, this._binding);
			return true;
		case IllegalDefaultCtor:
			reporter.illegallyCopiedDefaultCtor(this._decl, this._decl.scope.referenceType());
			return true;
		default:
			return false;
		}
	}


	// TODO(SH): note that role feature bridges are not really faked, since they are actually generated (synthetic?)
    public static enum FakeKind { NOT_FAKED, BASECALL_SURROGATE, ROLE_FEATURE_BRIDGE, TEAM_REGISTRATION_METHOD }

    public FakeKind _fakeKind = FakeKind.NOT_FAKED;
    private MethodBinding _baseCallSurrogate = null;

    /** If this method is implemented by an inferred callout, store the (synthetic) mapping declaration here: */
    public CalloutMappingDeclaration _inferredCallout = null;

	public static boolean isFakedMethod(MethodBinding abstractMethod) {
		if (abstractMethod.model != null)
				return abstractMethod.model._fakeKind != FakeKind.NOT_FAKED;
		return false;
	}
	public static boolean isFakedMethod(MethodBinding abstractMethod, FakeKind fakeKind) {
		if (abstractMethod.model != null)
			return abstractMethod.model._fakeKind == fakeKind;
		return false;
	}
	private MethodModel(AbstractMethodDeclaration decl) {
        this._decl    = decl;
        decl.model = this;
        if (decl.binding != null) {
        	decl.binding.model = this;
        	this._binding = decl.binding;
        }
    }
    private MethodModel(MethodBinding binding) {
    	this._binding = binding;
    	binding.model = this;
    }

	/**
	 * Retreive the declaration (AST).
	 */
	public AbstractMethodDeclaration getDecl() {
		return this._decl;
	}

	public MethodBinding getBinding() {
		if (this._binding == null) {
			if (this._decl != null)
				this._binding = this._decl.binding;
		}
		return this._binding;
	}

	public void setBaseCallSurrogate(MethodBinding _baseCallSurrogate) {
		this._baseCallSurrogate = _baseCallSurrogate;
	}

	public MethodBinding getBaseCallSurrogate() {
		if (getBinding() == null)
			return null;
		this._binding.declaringClass.methods(); // generates basecall surrogates on demand
		return this._baseCallSurrogate;
	}

	/**
	 * @param flag from IOTConstants.CALLIN_FLAG_*
	 */
	public void addCallinFlag(int flag) {
		this.callinFlags |= flag & 0xFF;
		if (this._attributes != null) {
			for (int i = 0; i < this._attributes.length; i++) {
				if (this._attributes[i].nameEquals(IOTConstants.CALLIN_FLAGS)) {
					((WordValueAttribute)this._attributes[i]).addBits(flag);
					return;
				}
			}
		}
		// If no attribute was found this is due to skipping the
		// callin transformation phase (class has ignoreFurtherInvestigation).
		// However, we can still generate the appropriate attribute.
		addAttribute(WordValueAttribute.callinFlagsAttribute(flag));
	}
	public static void addCallinFlag(AbstractMethodDeclaration methodDecl, int flag) {
		getModel(methodDecl).addCallinFlag(flag);
	}
	public static boolean hasCallinFlag(MethodBinding method, int flag) {
		if (method.model == null)
			return false;
		return (method.model.callinFlags & flag) == flag;
	}

	private boolean isForbiddenCreationMethod = false;
	/**
	 *  Record the fact, that this method is an abstract creation method in
	 *  a non-abstract team, for which invocations must be forbidden.
	 */
	public void markAsForbiddenCreationMethod() {
		this.isForbiddenCreationMethod = true;
	}
	public boolean isForbiddenCreationMethod() {
		return this.isForbiddenCreationMethod;
	}

	// ======= copying of team ctors with declared lifting ======
	public TypeBinding[] liftedParams = null;

	// Store old and new selfcall for copied team constructors
	// BytecodeTransformer can directly use these for mapping.
	public MethodBinding oldSelfcall = null;
	public MethodBinding adjustedSelfcall = null;

	/**
	 * During copying of a team constructor, a self call has been replaced.
	 * Record this fact here and possibly in the constructor declaration
	 *
	 * @param oldCall
	 * @param newCall
	 */
	public void adjustSelfcall(MethodBinding oldCall, MethodBinding newCall) {
		this.oldSelfcall = oldCall;
		this.adjustedSelfcall = newCall;
		if (this._decl == null && !this._binding.declaringClass.isBinaryBinding()) {
			this._decl = this._binding.sourceMethod();
		}
		if (this._decl != null && this._decl instanceof ConstructorDeclaration) {
			ConstructorDeclaration ctor = (ConstructorDeclaration)this._decl;
			if (ctor.constructorCall != null)
				ctor.constructorCall.binding = newCall;
		}
	}
	/** After inserting a method into a role interface create an attribute to store its source modifiers. */
	public static boolean checkCreateModifiersAttribute(TypeDeclaration type, AbstractMethodDeclaration method)
	{
		if ((type.modifiers & IOTConstants.AccSynthIfc) != 0) {
			if (  (method.modifiers & ClassFileConstants.AccPublic) == 0
				|| method.isStatic()) // also static methods must be disguised
			{
				MethodModel model = getModel(method);
				model.addAttribute(WordValueAttribute.modifiersAttribute(method.modifiers));
				// if no binding is present yet remember this bit for transfer in MethodScope.createMethod().
				model._clearPrivateModifier = true;
				return true;
			}
		}
		return false;
	}



	// ====== byte code storage ======
	private byte[] _bytes=null;
	private int _structOffset = 0;
	private int[] _constantPoolOffsets = null;
	private ClassFile _classFile = null;
	private TypeBinding _returnType = null;

	/**
	 * Ensure we have bytes and constantPoolOffsets ready to use.
	 */
	private void setupByteCode() {
		if (this._bytes == null || this._constantPoolOffsets == null)
		{
			try {
				MethodBinding binding = (this._binding == null) ? this._decl.binding : this._binding;
				assert binding.declaringClass.isTeam();

				ClassFileReader reader = null;
				if (this._bytes == null) {
					if (this._classFile == null && this._decl != null) {
						char[] className = binding.declaringClass.constantPoolName();
						this._classFile = (ClassFile) this._decl.compilationResult.compiledTypes.get(className);
						if (!CharOperation.equals(className, this._classFile.referenceBinding.constantPoolName())) {
							this._classFile = null; // has been reset thus cannot be used for this type any more.
						}
					}
					// here we made the best attempt to obtain a classfile, use it if possible:
					if (this._classFile != null && this._classFile.referenceBinding == this._binding.declaringClass) {
						this._bytes = this._classFile.getBytes();
						this._structOffset += this._classFile.headerOffset; // structOffset did not yet include the headerOffset
						this._constantPoolOffsets = this._classFile.constantPool.offsets;
						this._classFile = null; // don't use any more
						return;
					}
				}
				if (this._bytes != null) {
					// create a reader for in-memory bytes in order to recalculate constant pool offsets
					reader = new ClassFileReader(this._bytes, RoleModel.NO_SOURCE_FILE); // STATE_BYTECODE_PREPARED
				} else {
	 				// Currently only team-ctors use a MethodModel for byte code retrieval.
					// Use the stored file name for reading the byte code from disc:
					reader = binding.declaringClass.getTeamModel().read();
					if (reader == null)
						throw new InternalCompilerError("No byte code available for "+new String(binding.readableName())); //$NON-NLS-1$
					this._bytes = reader.getBytes();
				}
				this._classFile = null; // don't use any more
				// now we have both a reader and bytes
				this._constantPoolOffsets = reader.getConstantPoolOffsets();
				// find bytecode offset of this method:
				char[] mySignature = this._binding.signature();
				for (IBinaryMethod m : reader.getMethods()) {
					if (   CharOperation.equals(m.getSelector(), this._binding.selector)
						&& CharOperation.equals(m.getMethodDescriptor(), mySignature))
					{
						this._structOffset = ((MethodInfo)m).getStructOffset();
						return;
					}
				}
				throw new InternalCompilerError("Method "+String.valueOf(this._binding.readableName())+"not found in class file "+String.valueOf(reader.getFileName())); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (ClassFormatException ex) {
				throw new InternalCompilerError(ex.getMessage());
			} catch (IOException ex) {
				throw new InternalCompilerError(ex.getMessage());
			}
		}

	}
	public int[] getConstantPoolOffsets() {
		setupByteCode();
		return this._constantPoolOffsets;
	}
	public byte[] getBytes() {
		if (this._bytes == null)
			setupByteCode();
		return this._bytes;
	}
	public int getStructOffset() {
		return this._structOffset;
	}
	/**
	 * @param bytes
	 * @param structOffset
	 * @param constantPoolOffsets
	 */
	public void recordByteCode(byte[] bytes, int structOffset, int[] constantPoolOffsets) {
		this._bytes = bytes;
		this._structOffset = structOffset;
		int olen = constantPoolOffsets.length;
		System.arraycopy(constantPoolOffsets, 0, this._constantPoolOffsets= new int[olen], 0, olen);
	}
	/**
	 * same as above, version to use when copying byte code from source method.
	 *
	 * @param classFile
	 * @param structOffset
	 */
	public void recordByteCode(ClassFile classFile, int structOffset) {
		this._classFile = classFile;
		int[] offsets = classFile.constantPool.offsets;
		int olen = classFile.constantPool.currentIndex; // copy only used indices. ConstantPoolObjectReader.getNonWideConstantIterator() depends on the correct number.
		System.arraycopy(offsets, 0, this._constantPoolOffsets= new int[olen], 0, olen);
		this._structOffset = structOffset; // still need to add full header offset after classfile has been finished
	}
	public static boolean hasProblem(MethodBinding binding) {
		if ((binding.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0)
			return true;
		AbstractMethodDeclaration ast = binding.sourceMethod();
		if (ast != null)
			return ast.ignoreFurtherInvestigation;
		return false;
	}
	private void setCallsBaseCtor() {
		if (!this._callsBaseCtor) {
			this._callsBaseCtor = true;
			// TODO(SH): for binary methods the following is redundant but doesn't harm:
			addAttribute(new PlainAttribute(IOTConstants.CALLS_BASE_CTOR));
		}
	}
	// ===============
	private IStatementsGenerator _statementsGenerator = null;
	public void setStatementsGenerator(IStatementsGenerator generator) {
		assert this._statementsGenerator == null;
		this._statementsGenerator = generator;
	}
	public boolean generateStatements() {
		if (this._statementsGenerator == null)
			return false;
		boolean result = this._statementsGenerator.generateAllStatements(this._decl);
		if (result)
			this._decl.resolveStatements();
		this._statementsGenerator = null;
		return result;
	}
	/**
	 * Register newStatements as a prefix for method's statements.
	 * If method has a pending statements generator, wait until the statements generator
	 * operates and let it combine both lists of statements.
	 * @param method
	 * @param newStatements
	 */
	public static void prependStatements(AbstractMethodDeclaration method, List<Statement> newStatements)
	{
		if (method.model != null && method.model._statementsGenerator != null) {
			method.model._statementsGenerator.prepend(newStatements);
		} else {
            int methodLen = (method.statements == null) ?
                    			0 :
                    			method.statements.length;
            int newLen = newStatements.size();
			Statement[] newStats = new Statement[newLen+methodLen];
			if (methodLen > 0)
			    System.arraycopy(method.statements, 0, newStats, newLen, methodLen);
			System.arraycopy(newStatements.toArray(), 0, newStats, 0, newLen);
			method.setStatements(newStats);
		}
	}
	/**
	 * @param decl
	 */
	public static void setCallsBaseCtor(ConstructorDeclaration decl) {
		MethodModel model = getModel(decl);
		model.setCallsBaseCtor();
	}

	public static void setCallsBaseCtor(MethodBinding binding) {
		MethodModel model = getModel(binding);
		model.setCallsBaseCtor();
	}

	// does the method contain a call to a base constructor?
	public static boolean callsBaseCtor(MethodBinding method) {
		if (method.model != null) {
			if (method.model._callsBaseCtor)
				return true;
		}

		if (method.copyInheritanceSrc != null)
			return callsBaseCtor(method.copyInheritanceSrc);
		return false;
	}
	public void storeModifiers(int modifiers) {
		addAttribute(WordValueAttribute.roleClassMethodModifiersAttribute(modifiers & MethodModel.AccIfcMethodModiferMASK));
		this._clearPrivateModifier = true;
		if (this._binding != null)
			this._binding.tagBits |= TagBits.ClearPrivateModifier;
	}

	/**
	 * Some modifiers may not be legal in byte code (due to role splitting).
	 * This method rewrites the modifiers if the given method has stored the
	 * real modifiers in either a Modifiers attribute (role ifc) or a
	 * RoleClassMethodModifiers attribute (role class).
	 *
	 * @param methodBinding
	 * @return rewritten modifiers or -1 signaling no change.
	 */
	public static int rewriteModifiersForBytecode(MethodBinding methodBinding) {
		if (methodBinding.model != null)
			return methodBinding.model.rewriteModifiersForBytecode();
		return -1;
	}

	private int rewriteModifiersForBytecode() {
		if (this._attributes != null)
			for (AbstractAttribute attr : this._attributes) {
				if (   attr.nameEquals(IOTConstants.MODIFIERS_NAME)
					|| attr.nameEquals(IOTConstants.ROLECLASS_METHOD_MODIFIERS_NAME))
				{
			    	int MASK = ExtraCompilerModifiers.AccVisibilityMASK;
			    	if (this._binding.declaringClass.isSynthInterface())
			    		 MASK |= ClassFileConstants.AccStatic;

					int flags = this._binding.modifiers;
					flags &= ~MASK;
					flags |= ClassFileConstants.AccPublic;
					return flags;
				}
			}
		return -1;
	}

	public static TypeBinding getReturnType(MethodBinding method) {
		MethodModel model = method.model;
		if (model != null && model._returnType != null)
			return model._returnType;

		if (method instanceof ProblemMethodBinding) {
			ProblemMethodBinding pmb = (ProblemMethodBinding)method;
			if (pmb.closestMatch != null)
				return getReturnType(pmb.closestMatch);
		}
		return method.returnType;
	}

	/**
	 * If the return type is a type variable,
	 * return that type variable, else null.
	 */
	public static TypeVariableBinding checkedGetReturnTypeVariable (MethodBinding method) {
		if (method instanceof ParameterizedMethodBinding)
			method= ((ParameterizedMethodBinding)method).original();
		TypeBinding originalReturn= method.returnType;
		if (originalReturn != null && originalReturn.isTypeVariable())
			return (TypeVariableBinding)originalReturn;
		return null;
	}

	public static boolean hasUnboundedReturnType (MethodBinding method) {
		TypeVariableBinding variableBinding= checkedGetReturnTypeVariable(method);
		if (variableBinding != null)
			return variableBinding.firstBound == null;
		return false;
	}

	// for binary methods and synthetic base call surrogates
	public static void saveReturnType(MethodBinding binding, TypeBinding returnType) {
		getModel(binding).saveReturnType(returnType);
	}
	// for source methods
	public static void saveReturnType(MethodDeclaration decl, TypeBinding returnType) {
		getModel(decl).saveReturnType(returnType);
	}
	private void saveReturnType(TypeBinding returnType) {
		this._returnType = returnType;
		if (returnType.isBaseType()) {
			int encodedType=0;
			switch (returnType.id) {
			case TypeIds.T_void:
				encodedType = IOTConstants.CALLIN_RETURN_VOID; break;
			case TypeIds.T_boolean:
				encodedType = IOTConstants.CALLIN_RETURN_BOOLEAN; break;
			case TypeIds.T_byte:
				encodedType = IOTConstants.CALLIN_RETURN_BYTE; break;
			case TypeIds.T_char:
				encodedType = IOTConstants.CALLIN_RETURN_CHAR; break;
			case TypeIds.T_short:
				encodedType = IOTConstants.CALLIN_RETURN_SHORT; break;
			case TypeIds.T_double:
				encodedType = IOTConstants.CALLIN_RETURN_DOUBLE; break;
			case TypeIds.T_float:
				encodedType = IOTConstants.CALLIN_RETURN_FLOAT; break;
			case TypeIds.T_int:
				encodedType = IOTConstants.CALLIN_RETURN_INT; break;
			case TypeIds.T_long:
				encodedType = IOTConstants.CALLIN_RETURN_LONG; break;
			default:
				throw new InternalCompilerError("contradiction: base type but none of the known base types"); //$NON-NLS-1$
			}
			addCallinFlag(encodedType);
	    }
	}

	public static MethodBinding getClassPartMethod(MethodBinding ifcMethod) {
		ReferenceBinding roleIfc = ifcMethod.declaringClass;
		if (!roleIfc.isRole())
			return null;
		ReferenceBinding roleClass = roleIfc.roleModel.getClassPartBinding();
		if (roleClass == null)
			return null;
		MethodBinding[] methods = roleClass.getMethods(ifcMethod.selector);
		if (methods == null)
			return null;
		for (MethodBinding method: methods) {
			if (method.parameters.length != ifcMethod.parameters.length)
				continue;
			if (CharOperation.equals(method.signature(), ifcMethod.signature())) // FIXME(SH): early triggering of signature()??
				return method;
		}
		return null;
	}

	/** An interface method is always abstract, consult the class method,
     *  if present to query staticness. */
	public static boolean isAbstract(MethodBinding binding) {
		if (binding.declaringClass.isSynthInterface())
			binding = getClassPartMethod(binding);
		if (binding == null)
			return true; // ifc method w/o a class part
		return binding.isAbstract();
	}

	public static boolean isCallinForRoFi(AbstractMethodDeclaration method) {
		MethodModel model = method.model;
		if (model == null) return false;
		return model.isCallinForRoFi;
	}

	public String toString() {
		if (this._binding != null)
			return new String(this._binding.readableName());
		StringBuffer output = new StringBuffer();

//{ObjectTeams: copied from AbstractMethodDeclaration.print() but with much less details:
		this._decl.printReturnType(0, output).append(this._decl.selector).append('(');
		if (this._decl.arguments != null) {
			for (int i = 0; i < this._decl.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this._decl.arguments[i].print(0, output);
			}
		}
		output.append(')');
// SH}
		return output.toString();
	}
	public static boolean isOverriding(MethodBinding methodBinding, CompilationUnitScope scope) {
		if (!methodBinding.declaringClass.isBinaryBinding()) {
			Dependencies.ensureBindingState(methodBinding.declaringClass, ITranslationStates.STATE_METHODS_VERIFIED);
			return methodBinding.isOverriding();
		}
		ReferenceBinding currentClass = methodBinding.declaringClass.superclass();
		while (currentClass != null) {
			MethodBinding candidate = currentClass.getExactMethod(methodBinding.selector, methodBinding.parameters, scope);
			if (candidate != null && candidate.isValidBinding())
				return true;
			currentClass = currentClass.superclass();
		}
		return false;
	}
	public static CalloutMappingDeclaration getImplementingInferredCallout(MethodBinding binding) {
		if (binding.model == null)
			return null;
		return binding.model._inferredCallout;
	}

	public static ReferenceBinding getRoleDeclaringThisMethodMapping(AbstractMethodDeclaration methodDecl) {
		if (methodDecl.model == null)
			return null;
		AbstractMethodMappingDeclaration declaringMapping = methodDecl.model._declaringMapping;
		if (declaringMapping == null || declaringMapping.scope == null)
			return null;
		return declaringMapping.scope.enclosingSourceType();
	}
}
