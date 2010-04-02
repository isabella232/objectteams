/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamMethodGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;


import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPrivate;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccProtected;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynchronized;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObject;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectMapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectReader;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * This class adds methods and fields from org.objectteams.Team to those teams that
 * extend a non-team class and thus cannot inherit these necessary members.
 * Methods are copied via byte-code copy, fields are generated at AST-level (incl. initialization).
 * 
 * Technical note: the way this generator is constructed it assumes that org.objectteams.Team
 * exists in binary form, i.e., a team leveraging this generator cannot be compiled together
 * with org.objectteams.Team.
 * 
 * @since 1.4.0
 */
public class TeamMethodGenerator {
	static final char[][] JAVA_LANG_WEAKHASHMAP = new char[][] {"java".toCharArray(), "util".toCharArray(), "WeakHashMap".toCharArray()};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static final char[][] JAVA_LANG_THREAD 		= new char[][] {"java".toCharArray(), "lang".toCharArray(), "Thread".toCharArray()}; 	   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static final char[][] JAVA_LANG_THREADLOCAL = new char[][] {"java".toCharArray(), "lang".toCharArray(), "ThreadLocal".toCharArray()};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/** Simple structure to represent one method of o.o.Team. */
	static class MethodDescriptor {
		String selector;
		String signature;
		Args   args;
		boolean hasBooleanReturn; 	// if false void return is added
		int     modifiers;
		
		// store from binary method of o.o.Team:
		int methodCodeOffset;
		ReferenceBinding declaringClass;
		MethodBinding binding;
		
		MethodDescriptor(String selector, String signature, Args   args, boolean hasBooleanReturn, int modifiers) {
			super();
			this.selector		 	= selector;
			this.signature 			= signature;
			this.args 				= args;
			this.hasBooleanReturn 	= hasBooleanReturn;
			this.modifiers    = modifiers;
		}

		Argument[] makeArgs(AstGenerator gen) {
			switch (this.args) {
				case THREAD:
					return new Argument[] { 
						gen.argument("thread".toCharArray(),  //$NON-NLS-1$
								     gen.qualifiedTypeReference(JAVA_LANG_THREAD))
					};
				case BOOLEAN:
					return new Argument[] { 
						gen.argument("flag".toCharArray(),  //$NON-NLS-1$
								     gen.baseTypeReference(TypeConstants.BOOLEAN))
					};
				default:
					return null;
			}
		}
		
		TypeReference makeReturnRef(AstGenerator gen) {
			if (this.hasBooleanReturn)
				return gen.baseTypeReference(TypeConstants.BOOLEAN);
			return gen.baseTypeReference(TypeConstants.VOID);
		}
	}
	enum Args { NONE, THREAD, BOOLEAN }
	/** Public methods to copy from o.o.Team: */
	@SuppressWarnings("nls")
	final static MethodDescriptor[] methodDescriptors = new MethodDescriptor[] {
    	new MethodDescriptor("activate",                         	"()V", 					Args.NONE, 		false,	AccPublic),
    	new MethodDescriptor("activate",   							"(Ljava/lang/Thread;)V",Args.THREAD,	false,	AccPublic),
    	new MethodDescriptor("deactivate", 							"()V", 					Args.NONE,		false,	AccPublic),
    	new MethodDescriptor("deactivate", 							"(Ljava/lang/Thread;)V",Args.THREAD,	false,	AccPublic),
    	new MethodDescriptor("isActive",   							"()Z", 					Args.NONE, 		true,	AccPublic),
    	new MethodDescriptor("isActive",   							"(Ljava/lang/Thread;)Z",Args.THREAD,	true,	AccPublic),
    	new MethodDescriptor("isExecutingCallin", 					"()Z", 					Args.NONE, 		true,	AccPublic),
    	new MethodDescriptor("deactivateForEndedThread",        	"(Ljava/lang/Thread;)V",Args.THREAD,	false,	AccPublic),
    	new MethodDescriptor("internalIsActiveSpecificallyFor", 	"(Ljava/lang/Thread;)Z",Args.THREAD,	true,	AccPublic),
    	new MethodDescriptor("_OT$setExecutingCallin",				"(Z)Z",					Args.BOOLEAN,	true,	AccPublic),
    	new MethodDescriptor("_OT$activateForAllThreads",          	"()V", 					Args.NONE, 		false,	AccPrivate),
		new MethodDescriptor("doRegistration", 						"()V", 					Args.NONE, 		false,	AccPrivate),
		new MethodDescriptor("doUnregistration", 					"()V", 					Args.NONE, 		false,	AccPrivate),
	};

	// ==== Currently, this is where we globally store the byte code of org.objectteams.Team: ====
	public static byte[] classBytes;
	public static int headerOffset;
	public static int[] constantPoolOffsets;
	// ==== ====
	
	/** When o.o.Team is read from .class file, record the byte code here. */
    public static synchronized void maybeRegisterTeamClassBytes(ClassFileReader teamClass, ReferenceBinding teamClassBinding) {
    	if (classBytes != null)
    		return;
    	classBytes = teamClass.getBytes();
    	headerOffset = teamClass.getHeaderOffset();
    	constantPoolOffsets = teamClass.getConstantPoolOffsets();
    	for (IBinaryMethod method : teamClass.getMethods()) {
			for (int s = 0; s < methodDescriptors.length; s++) {
				if (   String.valueOf(method.getSelector()).equals(methodDescriptors[s].selector)
					&& String.valueOf(method.getMethodDescriptor()).equals(methodDescriptors[s].signature))
				{
					methodDescriptors[s].methodCodeOffset = ((MethodInfo)method).getStructOffset();
					methodDescriptors[s].declaringClass = teamClassBinding;
					break;
				}
			}
    	}
    }
    /** When binary methods for o.o.Team are created record the relevant method bindings. */
    public static void registerTeamMethod(IBinaryMethod method, MethodBinding methodBinding) {
		for (int s = 0; s < methodDescriptors.length; s++) {
			if (   String.valueOf(method.getSelector()).equals(methodDescriptors[s].selector)
				&& String.valueOf(method.getMethodDescriptor()).equals(methodDescriptors[s].signature))
			{
				methodDescriptors[s].binding = methodBinding;
				break;
			}
		}    	
    }
    /** 
     * At the AST representing all relevant methods and fields from o.o.Team,
     * and prepare methods for byte-code copy.
     */
    public static void addMethodsAndFields(TypeDeclaration teamDecl) {
    	// FIXME(SH): test subteams of teams already treated by this generator!
    	AstGenerator gen = new AstGenerator(teamDecl);
    	
    	boolean hasBoundRole = false;
    	for (RoleModel role : teamDecl.getTeamModel().getRoles(false)) {
    		if (role.isBound()) {
    			hasBoundRole = true;
    			break;
    		}			
    	}
    	// methods:
		for (MethodDescriptor methodDescriptor : methodDescriptors) {
			MethodDeclaration newMethod = null;
			if (methodDescriptor.modifiers == AccPublic) {
				// public methods are always copied
				newMethod = new CopiedTeamMethod(teamDecl.compilationResult, methodDescriptor, gen);
			} else {
				// privates are only copied if bound roles exist, else created as empty
				if (hasBoundRole) {
					newMethod = new CopiedTeamMethod(teamDecl.compilationResult, methodDescriptor, gen);
				} else {
					newMethod = gen.method(teamDecl.compilationResult, 
							methodDescriptor.modifiers, 
							gen.baseTypeReference(TypeConstants.VOID), 
							methodDescriptor.selector.toCharArray(), 
							null,
							new Statement[0]); // regular empty method.
				}				
			}
			AstEdit.addGeneratedMethod(teamDecl, newMethod);
		}
		// fields:
		addFields(teamDecl, gen);
    }
    /** create field ASTs. */
    @SuppressWarnings("nls")
	static void addFields(TypeDeclaration teamDecl, AstGenerator gen) {
		// private WeakHashMap<Thread, Boolean> _OT$activatedThreads = new WeakHashMap<Thread, Boolean>();
    	addPrivateField(teamDecl, gen,
    			weakHashMapTypeReference(gen), 
    			"_OT$activatedThreads".toCharArray(),
    			gen.allocation(weakHashMapTypeReference(gen), null));
    	
    	// private Object _OT$registrationLock= new Object();
    	addPrivateField(teamDecl, gen,
    			gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT), 
    			"_OT$registrationLock".toCharArray(),
    			gen.allocation(gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT), null));
    	
    	// private boolean _OT$lazyGlobalActiveFlag = false;
    	addPrivateField(teamDecl, gen,
    			gen.baseTypeReference(TypeConstants.BOOLEAN),
    			"_OT$lazyGlobalActiveFlag".toCharArray(),
    			gen.booleanLiteral(false));

    	// private boolean _OT$isExecutingCallin = false;
    	addPrivateField(teamDecl, gen,
    			gen.baseTypeReference(TypeConstants.BOOLEAN),
    			"_OT$isExecutingCallin".toCharArray(),
    			gen.booleanLiteral(false));
    	
    	// private  int _OT$registrationState = _OT$UNREGISTERED;
    	addPrivateField(teamDecl, gen,
    			gen.baseTypeReference(TypeConstants.INT),
    			"_OT$registrationState".toCharArray(),
    			gen.intLiteral(0));
    	
    	// private boolean _OT$globalActive = false;
    	addPrivateField(teamDecl, gen,
    			gen.baseTypeReference(TypeConstants.BOOLEAN),
    			"_OT$globalActive".toCharArray(),
    			gen.booleanLiteral(false));
 
		// private ThreadLocal<Integer> _OT$implicitActivationsPerThread = new ThreadLocal<Integer>() {
		// 		protected synchronized Integer initialValue() {
		// 			return Integer.valueOf(0);
		// 		}
		// };
    	TypeDeclaration anonThreadLocal = gen.anonymousType(teamDecl.compilationResult);
    	anonThreadLocal.methods = new MethodDeclaration[] {
    		gen.method(	teamDecl.compilationResult, 
    				   	AccProtected|AccSynchronized, 
    				   	gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_INTEGER), 
    				   	"initialValue".toCharArray(), 
    				   	null/*arguments*/,
    				   	new Statement[] {
        					gen.returnStatement(gen.intLiteral(0)) // rely on autoboxing	
        			   	})
    	};
    	addPrivateField(teamDecl, gen,
    			threadLocalReference(gen),
    			"_OT$implicitActivationsPerThread".toCharArray(),
    			gen.anonymousAllocation(threadLocalReference(gen), null/*arguments*/, anonThreadLocal));
    }
	static void addPrivateField(TypeDeclaration teamDecl, AstGenerator gen, TypeReference type, char[] name, Expression init) {
    	FieldDeclaration field = gen.field(AccPrivate, type, name, init);
    	AstEdit.addField(teamDecl, field, true, false);
    	field.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;     	
    }
	static QualifiedTypeReference weakHashMapTypeReference(AstGenerator gen) {
		return gen.parameterizedQualifiedTypeReference(
				JAVA_LANG_WEAKHASHMAP, 
				new TypeReference[]{
					gen.qualifiedTypeReference(JAVA_LANG_THREAD),
					gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_BOOLEAN)
				});
	}
	static QualifiedTypeReference threadLocalReference(AstGenerator gen) {
		return gen.parameterizedQualifiedTypeReference(
				JAVA_LANG_THREADLOCAL,
				new TypeReference[] {
					gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_INTEGER)
				});
	}
    
	/** 
	 * Add fake method bindings for methods are will be generated by the OTRE.
	 * These bindings are needed by the TeamConstantPoolMapper.
	 */
	public static void addFakedTeamRegistrationMethods(ReferenceBinding teamBinding) {
		MethodBinding registrationMethod;
		registrationMethod = new MethodBinding(AccPublic,
		        "_OT$registerAtBases".toCharArray(), //$NON-NLS-1$
		        TypeBinding.VOID, // return type
		        Binding.NO_PARAMETERS,
		        Binding.NO_EXCEPTIONS, // exceptions
		        teamBinding);
		teamBinding.addMethod(registrationMethod);
		MethodModel.getModel(registrationMethod)._fakeKind = MethodModel.FakeKind.TEAM_REGISTRATION_METHOD;
		registrationMethod = new MethodBinding(AccPublic,
		        "_OT$unregisterFromBases".toCharArray(), //$NON-NLS-1$
		        TypeBinding.VOID, // return type
		        Binding.NO_PARAMETERS,
		        Binding.NO_EXCEPTIONS, // exceptions
		        teamBinding);
		teamBinding.addMethod(registrationMethod);
		MethodModel.getModel(registrationMethod)._fakeKind = MethodModel.FakeKind.TEAM_REGISTRATION_METHOD;
	}

	/** Special crippled method declarations that only serve as place-holder for copy-inheritance. */
    static class CopiedTeamMethod extends MethodDeclaration {
    	MethodDescriptor descriptor;
		public CopiedTeamMethod(CompilationResult compilationResult, MethodDescriptor descriptor, AstGenerator gen) {
			super(compilationResult);
			this.selector = descriptor.selector.toCharArray();
			this.descriptor = descriptor;
			this.isCopied = true;
			this.modifiers = descriptor.modifiers;
			this.arguments = descriptor.makeArgs(gen);
			this.returnType = descriptor.makeReturnRef(gen);
		}
    	@Override
    	public void resolve(ClassScope upperScope) {
    		// nop
    	}
    	@Override
    	public void analyseCode(ClassScope classScope, InitializationFlowContext initializationContext, FlowInfo flowInfo) {
    		// nop
    	}
    	@Override
    	public void generateCode(ClassScope classScope, ClassFile classFile) {
    		ConstantPoolObjectMapper mapper = new TeamConstantPoolMapper(this.descriptor.binding, this.binding);
    		ConstantPoolObjectReader reader = new ConstantPoolObjectReader( classBytes, 
    																		constantPoolOffsets, 
    																		this.descriptor.declaringClass.getTeamModel(), 
    																		this.scope.environment());
			new BytecodeTransformer().doCopyMethodCode( null /*srcRoleModel*/, this.binding, 								// source
    													(SourceTypeBinding)this.binding.declaringClass, this, 				// destination
    													classBytes, constantPoolOffsets, this.descriptor.methodCodeOffset, 	// source bytes 
    													reader, mapper,														// mapping strategies 
    													classFile);															// final destination
    	}
    }

    /** Simple constant mapper: only translate from o.o.Team to current team type. */
	static class TeamConstantPoolMapper extends ConstantPoolObjectMapper {

		ReferenceBinding srcType, dstType;
		public TeamConstantPoolMapper(MethodBinding srcMethodBinding, MethodBinding dstMethodBinding) {
			super(srcMethodBinding, dstMethodBinding);
			this.srcType = srcMethodBinding.declaringClass;
			this.dstType = dstMethodBinding.declaringClass;
		}
		@Override
		public ConstantPoolObject mapConstantPoolObject(ConstantPoolObject src_cpo, boolean addMarkerArgAllowed) {
			return mapConstantPoolObject(src_cpo);
		}
		@Override
		public ConstantPoolObject mapConstantPoolObject(ConstantPoolObject src_cpo) {
			int type=src_cpo.getType();
			
			TypeBinding clazz = null;
			switch (type) {
			case MethodRefTag:
				clazz = src_cpo.getMethodRef().declaringClass;
				break;
			case FieldRefTag:
				clazz = src_cpo.getFieldRef().declaringClass;
				break;
			case ClassTag:
				clazz = src_cpo.getClassObject();
			}
			if (clazz != this.srcType)
				return src_cpo; // only map references to o.o.Team
			
			// perform the mapping:
			switch(type){
				case FieldRefTag:
					return new ConstantPoolObject(
							FieldRefTag,
							mapField(src_cpo.getFieldRef()));
				case MethodRefTag:
					return new ConstantPoolObject(
							MethodRefTag,
							mapMethod(src_cpo.getMethodRef()));
				case ClassTag:
					return new ConstantPoolObject(
							ClassTag,
							this.dstType); // don't search further, we already have it.
			}
			//if no mapping is needed, return original ConstantPoolObject
			return src_cpo;
		}
		private MethodBinding mapMethod(MethodBinding methodRef) {
			for (MethodBinding dstBinding : this.dstType.methods())
				if (   CharOperation.equals(dstBinding.selector, methodRef.selector)
					&& CharOperation.equals(dstBinding.signature(), methodRef.signature()))
					return dstBinding;
			return methodRef;
		}
		private FieldBinding mapField(FieldBinding fieldRef) {
			for (FieldBinding dstBinding : this.dstType.fields())
				if (   CharOperation.equals(dstBinding.name, fieldRef.name)
					&& dstBinding.type == fieldRef.type)
					return dstBinding;
			return fieldRef;
		}
	}
}
