/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010, 2015 Stephan Herrmann
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;


import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccFinal;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPrivate;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccProtected;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynchronized;
import static org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers.AccVisibilityMASK;
import static org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn.*;

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
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions.WeavingScheme;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.BytecodeTransformer;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObject;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectMapper;
import org.eclipse.objectteams.otdt.internal.core.compiler.bytecode.ConstantPoolObjectReader;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
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
		Type   args;
		Type   returnType;
		int    modifiers;
		
		// store from binary method of o.o.Team:
		int methodCodeOffset;
		ReferenceBinding declaringClass;
		MethodBinding binding;
		
		MethodDescriptor(String selector, String signature, Type args, Type returnType, int modifiers) {
			super();
			this.selector	= selector;
			this.signature 	= signature;
			this.args 		= args;
			this.returnType	= returnType;
			this.modifiers  = modifiers;
		}

		@SuppressWarnings("nls")
		Argument[] makeArgs(AstGenerator gen) {
			switch (this.args) {
				case THREAD:
					return new Argument[] { 
						gen.argument("thread".toCharArray(), gen.qualifiedTypeReference(JAVA_LANG_THREAD))
					};
				case BOOLEAN:
					return new Argument[] { 
						gen.argument("flag".toCharArray(), gen.baseTypeReference(TypeConstants.BOOLEAN))
					};
				case INT:
					return new Argument[] { 
						gen.argument("flags".toCharArray(), gen.baseTypeReference(TypeConstants.INT))
					};
				case OTDYNARGS1:
					return new Argument[] {
						gen.argument("base".toCharArray(),			gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE2)),
						gen.argument("teams".toCharArray(), 		gen.qualifiedArrayTypeReference(IOTConstants.ORG_OBJECTTEAMS_ITEAM, 1)),
						gen.argument("idx".toCharArray(), 			gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("callinIds".toCharArray(), 	gen.arrayTypeReference(TypeConstants.INT, 1)),
						gen.argument("boundMethodId".toCharArray(), gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("args".toCharArray(), 			gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1))
					};
				case OTDYNARGS2:
					return new Argument[] {
						gen.argument("base".toCharArray(),			gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE2)),
						gen.argument("teams".toCharArray(), 		gen.qualifiedArrayTypeReference(IOTConstants.ORG_OBJECTTEAMS_ITEAM, 1)),
						gen.argument("idx".toCharArray(), 			gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("callinIds".toCharArray(), 	gen.arrayTypeReference(TypeConstants.INT, 1)),
						gen.argument("boundMethodId".toCharArray(), gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("args".toCharArray(), 			gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1)),
						gen.argument("baseCallArgs".toCharArray(), 	gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1)),
						gen.argument("isBaseCall".toCharArray(), 	gen.baseTypeReference(TypeConstants.INT))
					};
				case OTDYNARGS3:
					return new Argument[] {
						gen.argument("base".toCharArray(),			gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE2)),
						gen.argument("callinId".toCharArray(), 		gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("boundMethodId".toCharArray(), gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("args".toCharArray(), 			gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1))
					};
				case OTDYNARGS4:
					return new Argument[] {
						gen.argument("base".toCharArray(),			gen.qualifiedTypeReference(IOTConstants.ORG_OBJECTTEAMS_IBOUNDBASE2)),
						gen.argument("callinId".toCharArray(), 		gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("boundMethodId".toCharArray(), gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("args".toCharArray(), 			gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1)),
						gen.argument("result".toCharArray(), 		gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT))
					};
				case OTDYNARGS5:
					return new Argument[] {
						gen.argument("callinId".toCharArray(), 		gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("boundMethodId".toCharArray(), gen.baseTypeReference(TypeConstants.INT)),
						gen.argument("args".toCharArray(), 			gen.qualifiedArrayTypeReference(TypeConstants.JAVA_LANG_OBJECT, 1))
					};
				default:
					return null;
			}
		}
		
		TypeReference makeReturnRef(AstGenerator gen) {
			switch (this.returnType) {
				case BOOLEAN:
					return gen.baseTypeReference(TypeConstants.BOOLEAN);
				case INT:
					return gen.baseTypeReference(TypeConstants.INT);
				case NONE:
					return gen.baseTypeReference(TypeConstants.VOID);
				case OBJECT:
					return gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT);
				default:
					throw new InternalCompilerError("Unexpected return type "+this.returnType); //$NON-NLS-1$
			}
		}
	}
	enum Type { 
		NONE {
			@Override public int length() { return 0; }
		}, 
		THREAD, BOOLEAN, INT, OBJECT, 
		OTDYNARGS1 { @Override int length() { return 6; } },
		OTDYNARGS2 { @Override int length() { return 8; } },
		OTDYNARGS3 { @Override int length() { return 4; } },
		OTDYNARGS4 { @Override int length() { return 5; } },
		OTDYNARGS5 { @Override int length() { return 3; } };
		int length() {
			return 1;
		}
	}
	/** Public methods to copy from o.o.Team: */
	@SuppressWarnings("nls")
	MethodDescriptor[] methodDescriptors = new MethodDescriptor[] {
    	new MethodDescriptor("activate",                         	"()V", 					Type.NONE, 		Type.NONE,		AccPublic),
    	new MethodDescriptor("activate",   							"(Ljava/lang/Thread;)V",Type.THREAD,	Type.NONE,		AccPublic),
    	new MethodDescriptor("deactivate", 							"()V", 					Type.NONE,		Type.NONE,		AccPublic),
    	new MethodDescriptor("deactivate", 							"(Ljava/lang/Thread;)V",Type.THREAD,	Type.NONE,		AccPublic),
    	new MethodDescriptor("isActive",   							"()Z", 					Type.NONE, 		Type.BOOLEAN,	AccPublic|AccFinal),
    	new MethodDescriptor("isActive",   							"(Ljava/lang/Thread;)Z",Type.THREAD,	Type.BOOLEAN,	AccPublic|AccFinal),
    	new MethodDescriptor("isExecutingCallin", 					"()Z", 					Type.NONE, 		Type.BOOLEAN,	AccPublic),
    	new MethodDescriptor("deactivateForEndedThread",        	"(Ljava/lang/Thread;)V",Type.THREAD,	Type.NONE,		AccPublic),
    	new MethodDescriptor("internalIsActiveSpecificallyFor", 	"(Ljava/lang/Thread;)Z",Type.THREAD,	Type.BOOLEAN,	AccPublic),
    	new MethodDescriptor("_OT$setExecutingCallin",				"(Z)Z",					Type.BOOLEAN,	Type.BOOLEAN,	AccPublic),
    	new MethodDescriptor("_OT$activateForAllThreads",          	"()V", 					Type.NONE, 		Type.NONE,		AccPrivate),
    	new MethodDescriptor("_OT$saveActivationState",				"()I",					Type.NONE,		Type.INT, 		AccPublic|AccSynchronized),
    	new MethodDescriptor("_OT$restoreActivationState",			"(I)V",					Type.INT,		Type.NONE,		AccPublic),
		new MethodDescriptor("doRegistration", 						"()V", 					Type.NONE, 		Type.NONE,		AccPrivate),
		new MethodDescriptor("doUnregistration", 					"()V", 					Type.NONE, 		Type.NONE,		AccPrivate),
	};
	@SuppressWarnings("nls")
	final MethodDescriptor[] methodDescriptorsDyn = new MethodDescriptor[] {
		new MethodDescriptor("_OT$callAllBindings",		"(Lorg/objectteams/IBoundBase2;[Lorg/objectteams/ITeam;I[II[Ljava/lang/Object;)Ljava/lang/Object;", 
																							Type.OTDYNARGS1,		Type.OBJECT,	AccPublic),
		new MethodDescriptor("_OT$callNext",			"(Lorg/objectteams/IBoundBase2;[Lorg/objectteams/ITeam;I[II[Ljava/lang/Object;[Ljava/lang/Object;Z)Ljava/lang/Object;", 
																							Type.OTDYNARGS2,		Type.OBJECT,	AccPublic),
		new MethodDescriptor("_OT$callReplace",			"(Lorg/objectteams/IBoundBase2;[Lorg/objectteams/ITeam;I[II[Ljava/lang/Object;)Ljava/lang/Object;", 
																							Type.OTDYNARGS1,		Type.OBJECT,	AccPublic),
		new MethodDescriptor("_OT$callBefore",			"(Lorg/objectteams/IBoundBase2;II[Ljava/lang/Object;)V", 
																							Type.OTDYNARGS3,		Type.NONE,		AccPublic),
		new MethodDescriptor("_OT$callAfter",			"(Lorg/objectteams/IBoundBase2;II[Ljava/lang/Object;Ljava/lang/Object;)V", 
																							Type.OTDYNARGS4,		Type.NONE,		AccPublic),
		new MethodDescriptor("_OT$callOrigStatic",		"(II[Ljava/lang/Object;)Ljava/lang/Object;", 
																							Type.OTDYNARGS5,		Type.OBJECT,	AccPublic)
	};

	// ==== Currently, this is where we globally store the byte code of org.objectteams.Team: ====
	public byte[] classBytes;
	public int[] constantPoolOffsets;
	// --- variant if o.o.Team is a SourceTypeBinding:
	public SourceTypeBinding ooTeamBinding;
	// ==== ====
	
	public TeamMethodGenerator(WeavingScheme weavingScheme) {
		if (weavingScheme == WeavingScheme.OTDRE) {
			int l1 = this.methodDescriptors.length;
			int l2 = this.methodDescriptorsDyn.length;
			MethodDescriptor[] all = new MethodDescriptor[l1+l2];
			System.arraycopy(this.methodDescriptors, 0, all, 0, l1);
			System.arraycopy(this.methodDescriptorsDyn, 0, all, l1, l2);
			this.methodDescriptors = all;
		}
	}

    /** When binary methods for o.o.Team are created record the relevant method bindings. */
    public void registerTeamMethod(IBinaryMethod method, MethodBinding methodBinding) {
    	String selector = String.valueOf(method.getSelector());
    	String descriptor = String.valueOf(method.getMethodDescriptor());
		registerTeamMethod(methodBinding.declaringClass, methodBinding, selector, descriptor, -1/*structOffset not yet known*/);    	
    }
	/** When o.o.Team is read from .class file, record the byte code here. */
    public synchronized void maybeRegisterTeamClassBytes(ClassFileReader teamClass, ReferenceBinding teamClassBinding) {
    	if (this.classBytes != null)
    		return;
    	this.classBytes = teamClass.getBytes();
    	this.constantPoolOffsets = teamClass.getConstantPoolOffsets();
    	for (IBinaryMethod method : teamClass.getMethods()) {
    		if (this.classBytes == null && method instanceof MethodInfo) {
    			// repair if class already nulled its byte reference:
    			this.classBytes = ((MethodInfo)method).reference;
    			this.constantPoolOffsets = ((MethodInfo)method).constantPoolOffsets;
    		}
    		String selector = String.valueOf(method.getSelector());
    		String descriptor = String.valueOf(method.getMethodDescriptor());
    		int structOffset = ((MethodInfo)method).getStructOffset();
    		// relevant new info is structOffset, everything has already  been registered from registerTeamMethod(IBinaryMethod,MethodBinding)
    		registerTeamMethod(teamClassBinding, null, selector, descriptor, structOffset);
    	}
    }
	private boolean registerTeamMethod(ReferenceBinding declaringClass, MethodBinding methodBinding, String selector, String descriptor, int structOffset) {
		for (int s = 0; s < this.methodDescriptors.length; s++) {
			if (   selector.equals(this.methodDescriptors[s].selector)
				&& descriptor.equals(this.methodDescriptors[s].signature))
			{
				if (methodBinding != null)
					this.methodDescriptors[s].binding = methodBinding;
				this.methodDescriptors[s].declaringClass = declaringClass;
				this.methodDescriptors[s].methodCodeOffset = structOffset;
				return true;
			}
		}
		return false;
	}
	// --- alternative initialization when compiling o.o.Team from source: ---
    public synchronized void registerOOTeamClass(SourceTypeBinding orgObjectteamsTeamBinding) {
    	if (this.classBytes != null)
    		return;
    	this.ooTeamBinding = orgObjectteamsTeamBinding;
    }
	public boolean requestBytes() {
		if (this.classBytes != null)
			return true;
		if (this.ooTeamBinding != null) {
			if (isOOTConverted())
				return false;
			boolean result = Dependencies.ensureBindingState(this.ooTeamBinding, ITranslationStates.STATE_BYTE_CODE_GENERATED);
			// the above causes callbacks to registerSourceMethodBytes(), info will be stored in corresponding MethodModels
			// finish collecting info:
			MethodModel model = this.methodDescriptors[0].binding.model;
			this.classBytes = model.getBytes();
			this.constantPoolOffsets = model.getConstantPoolOffsets();
			return result;
		}
		return false; // shouldn't
	}
	boolean isOOTConverted() {
		if (this.ooTeamBinding == null)
			return false;
		ClassScope scope = this.ooTeamBinding.scope;
		if (scope == null)
			return false;
		TypeDeclaration typeDecl = scope.referenceContext;
		return typeDecl.isConverted;
	}
	public boolean registerSourceMethodBytes(MethodBinding method) {
		String selector = String.valueOf(method.selector);
		String signature = String.valueOf(method.signature());
		return registerTeamMethod(method.declaringClass, method, selector, signature, -1/*no structOffset before class is complete*/);
	}
	// ==============
    /** 
     * Add the AST representing all relevant methods and fields from o.o.Team,
     * and prepare methods for byte-code copy.
     */
    public void addMethodsAndFields(TypeDeclaration teamDecl) {
    	// FIXME(SH): test subteams of teams already treated by this generator!
    	AstGenerator gen = new AstGenerator(teamDecl);
    	
    	boolean hasBoundRole = false;
    	// check what will be generated:
    	boolean hasCallinBefore = false, hasCallinAfter = false, hasCallinReplace = false;
		for (RoleModel role : teamDecl.getTeamModel().getRoles(false)) {
			if (role.isBound())
				hasBoundRole = true;
			ReferenceBinding roleBinding = role.getBinding();
			if (roleBinding == null) continue; // FIXME(SH): check if this is OK
			if (roleBinding.callinCallouts != null) {
				for (CallinCalloutBinding mappingBinding : roleBinding.callinCallouts) {
					if (mappingBinding.isCallin()) {
						switch (mappingBinding.callinModifier) {
							case TerminalTokens.TokenNamebefore: 	hasCallinBefore = true; 	break;
							case TerminalTokens.TokenNameafter: 	hasCallinAfter = true; 		break;
							case TerminalTokens.TokenNamereplace: 	hasCallinReplace = true; 	break;
						}
						// note: we cannot detect static base methods, because the mapping has not yet been resolved.
						// hence we overwrite _OT$callOrigStatic after the fact in CallinImplementorDyn.generateCallOrigStatic().
					}
				}
			}
		}

		// methods:
		for (MethodDescriptor methodDescriptor : this.methodDescriptors) {
			MethodDeclaration newMethod = null;
			// don't duplicate methods that will be generated later
			if (willBeGenerated(methodDescriptor, teamDecl, hasCallinBefore, hasCallinAfter, hasCallinReplace))
				continue;
			if ((methodDescriptor.modifiers & AccVisibilityMASK) == AccPublic) {
				// public methods are always copied
				newMethod = new CopiedTeamMethod(teamDecl.compilationResult, methodDescriptor, gen);
			} else {
				// privates are only copied if bound roles exist, else created as empty
				if (hasBoundRole) {
					newMethod = new CopiedTeamMethod(teamDecl.compilationResult, methodDescriptor, gen);
				} else {
					newMethod = gen.method(teamDecl.compilationResult, 
							methodDescriptor.modifiers, 
							methodDescriptor.makeReturnRef(gen), 
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

	boolean willBeGenerated(MethodDescriptor methodDescriptor, TypeDeclaration teamDecl,
			boolean hasCallinBefore, boolean hasCallinAfter, boolean hasCallinReplace)
	{
		char[] selector = methodDescriptor.selector.toCharArray();
		if (CharOperation.equals(OT_CALL_BEFORE, selector))
			return hasCallinBefore;
		else if (CharOperation.equals(OT_CALL_AFTER, selector))
			return hasCallinAfter;
		else if (CharOperation.equals(OT_CALL_REPLACE, selector) || CharOperation.equals(OT_CALL_NEXT, selector))
			return hasCallinReplace;
		return false;
	}

	/** create field ASTs. */
    @SuppressWarnings("nls")
	void addFields(TypeDeclaration teamDecl, AstGenerator gen) {
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
	void addPrivateField(TypeDeclaration teamDecl, AstGenerator gen, TypeReference type, char[] name, Expression init) {
    	FieldDeclaration field = gen.field(AccPrivate, type, name, init);
    	boolean teamHasProblems = teamDecl.ignoreFurtherInvestigation;
    	AstEdit.addField(teamDecl, field, !teamHasProblems, false, false);
    	if (!teamHasProblems)
    		field.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;     	
    }
	QualifiedTypeReference weakHashMapTypeReference(AstGenerator gen) {
		return gen.parameterizedQualifiedTypeReference(
				JAVA_LANG_WEAKHASHMAP, 
				new TypeReference[]{
					gen.qualifiedTypeReference(JAVA_LANG_THREAD),
					gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_BOOLEAN)
				});
	}
	QualifiedTypeReference threadLocalReference(AstGenerator gen) {
		return gen.parameterizedQualifiedTypeReference(
				JAVA_LANG_THREADLOCAL,
				new TypeReference[] {
					gen.qualifiedTypeReference(TypeConstants.JAVA_LANG_INTEGER)
				});
	}
    
	/** 
	 * Add fake method bindings for methods that will be generated by the OTRE.
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
    public class CopiedTeamMethod extends MethodDeclaration {
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
    	public void analyseCode(ClassScope classScope, FlowContext flowContext, FlowInfo flowInfo) {
    		// nop
    	}
    	@Override
    	public void generateCode(ClassScope classScope, ClassFile classFile) {
    		if (isOOTConverted())
    			return; // don't actually try to generate, o.o.Team has no byte codes in this scenario
    		this.binding.copyInheritanceSrc = this.descriptor.binding;
    		ConstantPoolObjectMapper mapper = new TeamConstantPoolMapper(this.descriptor.binding, this.binding);
    		byte[] bytes;
    		int[] offsets;
    		int structOffset;
    		if (this.descriptor.methodCodeOffset == -1) {
    			MethodModel srcModel = this.descriptor.binding.model;
    			bytes        = srcModel.getBytes();
    			offsets      = srcModel.getConstantPoolOffsets();
    			structOffset = srcModel.getStructOffset();
    		} else {
    			bytes        = TeamMethodGenerator.this.classBytes;
    			offsets      = TeamMethodGenerator.this.constantPoolOffsets;
    			structOffset = this.descriptor.methodCodeOffset;
    		}
			ConstantPoolObjectReader reader = new ConstantPoolObjectReader( bytes, 
    																		offsets, 
    																		this.descriptor.declaringClass.getTeamModel(), 
    																		this.scope.environment());
			new BytecodeTransformer().doCopyMethodCode( null /*srcRoleModel*/, this.binding, 					// source
    													(SourceTypeBinding)this.binding.declaringClass, this, 	// destination
    													bytes, 				 									// source bytes
    													offsets,
    													structOffset,
    													reader, mapper,											// mapping strategies 
    													classFile);												// final destination
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
			if (TypeBinding.notEquals(clazz, this.srcType))
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
					&& TypeBinding.equalsEquals(dstBinding.type, fieldRef.type))
					return dstBinding;
			return fieldRef;
		}
	}
}
