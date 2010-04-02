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
 * $Id: Lifting.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.RoleInitializationMethod;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.mappings.CallinImplementorDyn;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.MethodModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.AbstractStatementsGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.SwitchOnBaseTypeGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;


/**
 * This class generates the AST for liftTo methods and constructors.
 * Also declared lifting arguments are translated here.
 *
 * Special cases:
 * Declared lifting in team constructors requires several hacks:
 * A Lifting can only happen _after_ the self-call (it needs access to the team instance!)!
 *   This is bad, because a self-call cannot access the lifted roles.
 * B If the self call should pass the lifted role, an additional ctor has to be created:
 *   - a copy of the original super ctor which does the lifting in its body
 * C If a constructor has role types, it must expect to be called via self-call
 *   with base objects instead. For this case two more tricks are needed:
 *   1. the super call in this constructor is extracted to a new constructor of this class.
 *   2. local variables for role arguments are created just like for declared lifting,
 *      however, instead of lifting these variables are initialized without translation.
 *      A specific sequence of "6 nops, load, 3 nops, store" leaves room for inserting the
 *      lifting translation during byte-code copy.
 * 		Before patching (produced by LocalDeclaration.generateCode())
 * 			nop, nop, nop, nop, nop, nop, <push base value>, nop, nop, nop, store_n
 *      After patching (done by BytecodeTransformer.adjustCode() w/ helper funcs):
 * 			aload_0
 * 			invokevirtual _OT$initCaches()Z
 * 			pop // the boolean return from initcaches
 * 			aload_0
 * 			<push base value> // an aload_x
 * 			invokevirtual _OT$liftTo$<MyRole>(<MyBase>)<MyRole>
 * 			store_n
 *   With this preparation, a sub team may create a copy of this constructor which
 *   indeed performs the lifting in its body.
 *   As self-call it calls chaining ctor from step 1.
 * D All generated ctors are distinguished by a regular marker arg.
 *
 * This is, how different classes contribute to this behavior:
 * -> Dependencies.establishTypesAdjusted
 *    -> Lifting.prepareArgLifting
 * 			- change the self-call to use the chaining version, to be created later
 * 	        - create local variables:
 * 				dummy (reserve a slot for marker arg to be added later, set LocalDeclaration.isPlaceHolder)
 *              one variable for each role type argument (set LocalDeclaration.isPreparingForLifting)
 * -> ExplicitConstructorCall.resolve
 * 	  -> Lifting.createChainingCtor (containing nothing but a super call)
 *    -> MethodModel.createLiftingCopy (need to store original signature with roles)
 *
 * -> LocalDeclaration.generateCode
 * 		 pretend dummy var is used (isPlaceholder)
 *       generate nops to reserve space for lifting call (isPreparingForLifting)
 *
 * -> BytecodeTransformer
 * 		 replace nops with lifting
 *
 *
 * @author brcan, haebor, mac, stephan
 * @version $Id: Lifting.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class Lifting extends SwitchOnBaseTypeGenerator
					 implements TypeIds, ClassFileConstants, ExtraCompilerModifiers
{
    private RoleModel _boundRootRoleModel = null;
    private AstGenerator _gen = null;
	private long _sourceLevel;
	public char[] variableName = MY_ROLE;

    // ==== GENERAL API: names of liftTo methods: ====

    public static boolean isLiftToMethod (MethodBinding method) {
		return CharOperation.prefixEquals(IOTConstants._OT_LIFT_TO, method.selector);
	}

	public static char[] getLiftMethodName(TypeBinding roleType) {
	    // TODO (SH) if roleType is not bound search for bound child role.
	    assert( !roleType.isBaseType());
	    if (roleType.isArrayType())
	        roleType = roleType.leafComponentType();
	    return getLiftMethodName(roleType.sourceName());
	}

	public static char[] getLiftMethodName(char[] roleName) {
	    return CharOperation.concat(IOTConstants._OT_LIFT_TO, roleName);
	}

	// ==== MORE API: ====
    /**
     * API:
     * Generate the call that lifts a base object to its role.
     *
	 * @param scope used for lookup during AST generation.
	 * @param teamExpr
	 * @param unliftedExpr
	 * @param providedType
	 * @param expectedRole
	 * @param needLowering when lifting a callin param we also need to generate the lowering operation
	 *        for the base call
	 * @return teamExpr.liftTo<expectedRole>?(unliftedExpr)
	 */
	public static MessageSend liftCall(
			BlockScope       scope,
	        Expression       teamExpr,
	        Expression       unliftedExpr,
			TypeBinding      providedType,
	        TypeBinding 	 expectedRole,
	        boolean          needLowering)
	{
		AstGenerator gen= new AstGenerator(unliftedExpr.sourceStart, unliftedExpr.sourceEnd);
		return liftCall(scope, teamExpr, unliftedExpr, providedType, expectedRole, needLowering, gen);
	}
	public static MessageSend liftCall(
			BlockScope       scope,
	        Expression       teamExpr,
	        Expression       unliftedExpr,
			TypeBinding      providedType,
	        TypeBinding 	 expectedRole,
	        boolean          needLowering,
	        AstGenerator     gen)
	{
		if (providedType.isArrayType())
		{
			// trigger creation of array lifter
			if (needLowering) {
				Expression dumExpr= new SingleNameReference("dummy".toCharArray(), 0L); //$NON-NLS-1$
				new ArrayLowering().ensureTransformMethod(scope, dumExpr, expectedRole, providedType, false);
			}

			ArrayLifting arrayLifting = new ArrayLifting();
			return arrayLifting.liftArray(scope, teamExpr, unliftedExpr, expectedRole);
		}
	    // TODO (SH): record necessity of array lifting if binding.isArrayType()
		MessageSend send = gen.messageSend(
		        teamExpr,
		        getLiftMethodName(expectedRole),
		        new Expression[] { unliftedExpr });
	    send.constant = Constant.NotAConstant;
	    return send;
	}

    /*
            public MyRole(MyBase base)
             {
                     _OT$base = base;
                     _OT$cache_OT$RootRole.put(_OT$base, this);
             }
       or (with bound parent):
            public MyRole(MyBase base)
             {
                    super(base);
             }
    */
    public ConstructorDeclaration createLiftToConstructorDeclaration(
            TreeNode roleNode,
			boolean  needMethodBodies)
    {
        TreeNode instantiableBoundRootRoleNode = roleNode.getTopmostBoundParent(false);
        if (instantiableBoundRootRoleNode == null) return null;

        TypeDeclaration roleDecl = roleNode.getTreeObject().getAst();
        if (instantiableBoundRootRoleNode == TreeNode.ProblemNode) {
            roleDecl.scope.problemReporter().
                    overlappingRoleHierarchies(roleDecl, TreeNode.ProblemNode.toString());
            return null;
        }
        TypeDeclaration roleType = roleNode.getTreeObject().getAst();
        ConstructorDeclaration existingConstructor = findLiftToConstructor(roleType);
        if (   existingConstructor == null
        	&& !roleNode.hasBoundParent(false)
        	&& !roleNode.getTreeObject().isIgnoreFurtherInvestigation())
        {
            ReferenceBinding parent = roleNode.getTreeObject().getBinding().superclass();
            MethodBinding defCtor = parent.getExactConstructor(Binding.NO_PARAMETERS);
            boolean hasEmptyCtor = (defCtor != null)
					            	? defCtor.isValidBinding()
					            	: (parent.getMethods(TypeConstants.INIT) == Binding.NO_METHODS);
            if (!hasEmptyCtor) {
                roleDecl.scope.problemReporter().
                        missingEmptyCtorForLiftingCtor(roleDecl, parent);
                return null;
            }
        }

        // for determining the cache interfaces are allowed:
        this._boundRootRoleModel = roleNode.getTopmostBoundParent(true).getTreeObject();
        if (this._boundRootRoleModel != null)
        	roleNode.getTreeObject()._boundRootRole = this._boundRootRoleModel;

        TypeDeclaration       teamTypeDeclaration = roleType.enclosingType;
        ReferenceBinding      baseClassBinding    = roleType.binding.baseclass();

        if (baseClassBinding == null &&
            ((roleType.binding.tagBits & TagBits.HierarchyHasProblems) != 0))
            return null; // assume base class could not be resolved.

        AstGenerator gen = new AstGenerator(roleType.sourceStart, roleType.sourceEnd);
        // public MyRole(MyBase base)
        ConstructorDeclaration generatedConstructor =
            gen.constructor(teamTypeDeclaration.compilationResult,
            		ClassFileConstants.AccPublic,
					roleType.name,
					new Argument[] {          		                  // (MyBase base)
                    	gen.argument(
                   			BASE,                                     // name
							gen.baseclassReference(baseClassBinding)) // type
                	}
            );


		// default arg name is base.
        char[] baseArgName = BASE;
        if (existingConstructor != null) {
            if (existingConstructor.isCopied) {
                if (roleNode.getTreeObject().getImplicitSuperRole().isBound()) // parent must be class.
                    return null; // copied constructor has everything we need.
            }
            // use the argument name of the existing constructor
            baseArgName = existingConstructor.arguments[0].name;
        }

        if (needMethodBodies) {
	        if (instantiableBoundRootRoleNode == roleNode)
	            genLiftToConstructorStatements(
	            		baseClassBinding,
	            		roleType,
	                    generatedConstructor,
	                    baseArgName,
	                    gen);
	        else
	            genLiftToConstructorSuperCall(
	                    baseClassBinding,
	                    roleType,
	                    generatedConstructor,
	                    baseArgName,
						gen);
        }

        this._boundRootRoleModel = null;

        if (existingConstructor != null) {
            //if constructor exists with same signature, merge statements.
        	if (needMethodBodies) {
	            // Also see Parser.parse(ConstructorDeclaration) for merging and
	            // for checking illegal explicit super call.
	            if (   existingConstructor.statements != null) {
	                int len1 = generatedConstructor.statements.length;
	                int len2 = existingConstructor.statements.length;
	                Statement[] newStatements = new Statement[len1+len2];
	                System.arraycopy(
	                        generatedConstructor.statements, 0,
	                        newStatements, 0,
	                        len1);
	                System.arraycopy(
	                        existingConstructor.statements, 0,
	                        newStatements, len1,
	                        len2);
	                existingConstructor.setStatements(newStatements);
	            } else {
	                existingConstructor.setStatements(generatedConstructor.statements);
	            }
	            // Keep arguments.
	            // If constructorCall is explicit keep it.
	            if (   existingConstructor.constructorCall == null
	            	|| existingConstructor.constructorCall.isImplicitSuper())
	            	existingConstructor.constructorCall = generatedConstructor.constructorCall;
        	}
            return existingConstructor;
        } else {
            //otherhwise insert new constructor
            AstEdit.addMethod(roleType, generatedConstructor);

            return generatedConstructor;
        }
    }

    private static ConstructorDeclaration findLiftToConstructor(
            TypeDeclaration  roleType)
    {
        if (roleType.methods == null)
            return null;
        // force bindings for constructors:
        roleType.binding.getMethods(TypeConstants.INIT);
        for (int i = 0; i < roleType.methods.length; i++) {
            AbstractMethodDeclaration method = roleType.methods[i];

            if (isLiftToConstructor(method, roleType.binding))
                return (ConstructorDeclaration) method;
        }

        return null;
    }

    /**
     * @param baseClassBinding
     * @param liftToConstructorDeclaration generated constructor
     * @param baseArgName name of the base argument, either generated or from source
	 * @param gen for generating AST nodes
     */
    private static void genLiftToConstructorSuperCall(
            ReferenceBinding       baseClassBinding,
            TypeDeclaration        roleType,
            ConstructorDeclaration liftToConstructorDeclaration,
            char[]                 baseArgName,
			AstGenerator           gen)
    {
        liftToConstructorDeclaration.constructorCall =
                gen.explicitConstructorCall(ExplicitConstructorCall.Super);
        		// mode maybe refined in ExplicitConstructorCall.resolve()->updateFromTSuper()
        liftToConstructorDeclaration.constructorCall.arguments =
                new Expression[] {
                    gen.singleNameReference(baseArgName)
                };
        // start with an empty statements list, except for this._OT$InitFields();
        liftToConstructorDeclaration.setStatements(new Statement[] {
        		RoleInitializationMethod.genInvokeInitMethod(
					    					gen.thisReference(),
					    					roleType.binding,
					    					gen)
        });
    }

    private void genLiftToConstructorStatements(
    	ReferenceBinding       baseClassBinding,
    	TypeDeclaration        roleType,
        ConstructorDeclaration liftToConstructorDeclaration,
        char[]                 baseArgName,
        AstGenerator           gen)
    {
        liftToConstructorDeclaration.constructorCall =
        		gen.explicitConstructorCall(ExplicitConstructorCall.Super);
    	Statement[] statements = new Statement[4];
        // _OT$base = <baseArgName>;
        SingleNameReference lhs = gen.singleNameReference(_OT_BASE);
        statements[0] = gen.assignment(lhs, gen.singleNameReference(baseArgName));

    	Statement[] regStats = genRoleRegistrationStatements(
    								  this._boundRootRoleModel.getAst().scope,
    								  this._boundRootRoleModel,
    								  baseClassBinding,
    								  liftToConstructorDeclaration,
    								  gen);
    	System.arraycopy(regStats, 0, statements, 1, regStats.length);

        // after initializing _OT$base and storing in the cache we are ready to execute field initializers:

        // this._OT$InitFields();
    	statements[3] = RoleInitializationMethod.genInvokeInitMethod(
									    					gen.thisReference(),
									    					roleType.binding,
									    					gen);
    	liftToConstructorDeclaration.setStatements(statements);
    }

    /**
     * @param baseClassBinding
     * @param liftToConstructorDeclaration
     * @param gen
     */
    public static Statement[] genRoleRegistrationStatements(
    		Scope                  scope,
    		RoleModel              boundRootRoleModel,
        	ReferenceBinding       baseClassBinding,
            ConstructorDeclaration liftToConstructorDeclaration,
            AstGenerator           gen)
    {
    	Statement[] statements = new Statement[2];


        // _OT$cache_OT$RootRole.put(<baseArgName>, this);
        Expression _this = gen.thisReference();
        if (scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5)
        {
        	_this = gen.castExpression(
        			_this, gen.typeReference(boundRootRoleModel.getBinding()), CastExpression.DO_WRAP);
        }
        statements[0] =
        	 gen.messageSend(
            		gen.singleNameReference(
                            LiftingEnvironment.getCacheName(boundRootRoleModel)),
    				PUT,
    				new Expression[] {
            				gen.baseNameReference(_OT_BASE),
    						_this
            		});

        // ((IBoundBase)_OT$base).addRole(this); // prevent premature garbage collection
        statements[1] =
			// OTDYN: Slightly different methods depending on the weaving strategy:
        	CallinImplementorDyn.DYNAMIC_WEAVING 
        	? gen.messageSend(
					gen.castExpression(
							gen.singleNameReference(_OT_BASE),
							gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
							CastExpression.RAW),
					ADD_REMOVE_ROLE,
					new Expression[] {
						gen.castExpression( // here an (unnecessary) cast to j.l.Object prevents a warning re OTJLD 2.2(f):
								gen.thisReference(), // the payload 
								gen.qualifiedNameReference(TypeConstants.JAVA_LANG_OBJECT), 
								CastExpression.RAW),
						gen.booleanLiteral(true)}) // isAdding
			: gen.messageSend(
					gen.castExpression(
						gen.singleNameReference(_OT_BASE),
						gen.qualifiedTypeReference(ORG_OBJECTTEAMS_IBOUNDBASE),
						CastExpression.RAW),
					ADD_ROLE,
					new Expression[] {
						gen.castExpression( // here an (unnecessary) cast to j.l.Object prevents a warning re OTJLD 2.2(f):
								gen.thisReference(), // the payload 
								gen.qualifiedNameReference(TypeConstants.JAVA_LANG_OBJECT), 
								CastExpression.RAW)});
        return statements;
    }

	public static boolean isLiftingCtor(MethodBinding binding) {
		if (!binding.isConstructor())
			return false;
		if (binding.parameters.length != 1)
			return false;
		return binding.parameters[0] == binding.declaringClass.baseclass();
	}

    public static boolean isLiftToConstructor(
            AbstractMethodDeclaration method,
            ReferenceBinding role)
    {
        if (!method.isConstructor())
            return false;
        if (method.ignoreFurtherInvestigation)
            return false;
        if (    method.arguments == null
            || (method.arguments.length != 1))
            return false;
        if (method.binding == null) {
        	assert method.scope.referenceType().ignoreFurtherInvestigation : "binding should only be missing in a problem type"; //$NON-NLS-1$
        	return false;
        }
        TypeBinding param = method.binding.parameters[0];
		if (param.isBaseType())
        	return false;
		if (param.isArrayType())
			return false;
		if (((ReferenceBinding)param).isRole())
			param = TeamModel.strengthenRoleType(role, param);
        ReferenceBinding paramClass = ((ReferenceBinding)param).getRealClass();
        // implicitly refined base may be weakened, apply the weak type:
        ReferenceBinding baseclass = WeakenedTypeBinding.getBytecodeType(role.baseclass());
        return RoleTypeBinding.type_eq(paramClass, baseclass);
    }

    public static boolean isLiftToConstructor(
            MethodBinding method,
            ReferenceBinding role)
    {
    	if (method == null)
    		return false;
        if (!method.isConstructor())
            return false;
        if (!method.isValidBinding())
            return false;
        if (    method.parameters == null
            || (method.parameters.length != 1))
            return false;
        TypeBinding param = method.parameters[0];
		if (param.isBaseType())
        	return false;
		if (param.isArrayType())
			return false;
        ReferenceBinding paramClass = ((ReferenceBinding)param).getRealClass();
        ReferenceBinding baseclass = role.baseclass();
        if (baseclass == null)
        	return false;
		return RoleTypeBinding.type_eq(paramClass, baseclass.getRealClass());
    }

    /*
        MyRole _OT$liftToMyRole(MyBase base)
        {
        	synchronized (OT_$cache_OT$RootRole) {
	            MyRole myRole = null;

	            if(base == null)
	            {
	                return null;
	            }
	        // for base-anchored bases (playedBy BaseRole<@base>):
      			if ((base._OT$getTeam() != _OT$base))
          		{
            		throw new org.objectteams.LiftingVetoException(this, (java.lang.Object) base);
          		}
          	//
	            if(!_OT$cache_OT$RootRole.containsKey(base))
	            {
	                switch(base.getMyTeam_OT$Tag)
	                {
	                    case 4:
	                        myRole = new __OT__MySubRoleA((MySubBaseA)base);
	                        break;
	                    case 5:
	                        myRole = new __OT__MySubRoleB((MySubBaseB)base);
	                        break;
	                    default:
	                        throw new LiftingFailedException((Object)base, "MyRole");
	                }
	            }
	            else
	            {
	                RootRole role = _OT$cache_OT$RootRole.get(base);

	                try
	                {
	                    myRole = (MyRole)role;
	                }
	                catch(ClassCastException classcastexception)
	                {
	                    throw new WrongRoleException(MyTeam$MyRole.class, base, role);
	                }
	            }
	            return myRole;
	        }
        }
    */

    /**
     * Create a liftTo method an add it to the team, including creation of bindings.
     *
     * @param teamTypeDeclaration type to add the lift method to.
     * @param roleNode the Role for which the LiftTo Method is to be created
     * @param caseObjects all informations for creating case statements,.
     *   i.e., the role classes to which lifting could occur.
     */
    public void createLiftToMethod (
        TypeDeclaration teamTypeDeclaration, TreeNode roleNode, final RoleModel[] caseObjects)
	{

        TreeNode boundRootRoleNode = roleNode.getTopmostBoundParent(true);
        if (boundRootRoleNode == null) return;

        RoleModel roleModel = roleNode.getTreeObject();

        TypeDeclaration typeDecl = roleModel.getAst(); // only for positions
        if (typeDecl == null)
        	typeDecl = teamTypeDeclaration;

        if (boundRootRoleNode == TreeNode.ProblemNode) {
            typeDecl.scope.problemReporter().
                    overlappingRoleHierarchies(typeDecl, TreeNode.ProblemNode.toString());
            return;
        }

        this._boundRootRoleModel = boundRootRoleNode.getTreeObject();
        this._gen = new AstGenerator(typeDecl.sourceStart, typeDecl.sourceEnd);
        this._sourceLevel= typeDecl.scope.compilerOptions().sourceLevel;

        try {
	        final ReferenceBinding teamBinding        = teamTypeDeclaration.binding;
	        final ReferenceBinding roleClassBinding   = roleModel.getBinding();
	        final ReferenceBinding baseClassBinding   = roleClassBinding.baseclass();

	        if ((roleClassBinding.tagBits & TagBits.HasLiftingProblem) != 0) {
	        	this._boundRootRoleModel = null;
	        	this._gen = null;
	        	return; // lift method won't work
	        }

	        char[] methodName = getLiftMethodName(roleClassBinding.sourceName());
	        Argument[] arguments = new Argument[] {
				    this._gen.argument(BASE,  this._gen.baseclassReference(baseClassBinding))
			};

	        MethodDeclaration liftToMethodDeclaration =
	        	AstConverter.findAndAdjustCopiedMethod(teamTypeDeclaration, methodName, arguments);

	        boolean needToAdd = false;
	        if (liftToMethodDeclaration == null) {
	        	liftToMethodDeclaration = createLiftToMethodDeclaration(
											        teamTypeDeclaration,
											        roleClassBinding,
													methodName,
													arguments,
											        baseClassBinding);
	        	this._gen.maybeAddTypeParametersToMethod(baseClassBinding, liftToMethodDeclaration);
	        	needToAdd = true;
	        }

	        if (caseObjects.length == 0) {
	            if (teamBinding.isAbstract()) {
	                liftToMethodDeclaration.modifiers |= AccAbstract|AccSemicolonBody;
	                if (liftToMethodDeclaration.binding != null)
	                	liftToMethodDeclaration.binding.modifiers |= AccAbstract;
	            } else {
	            	assert roleModel._hasBindingAmbiguity;
	            	liftToMethodDeclaration.setStatements(new Statement[] {
	            		genLiftingFailedException(BASE, roleClassBinding, this._gen)
	            	});
	            	needToAdd = true;
	            }
	        } else {
	        	final MethodDeclaration newMethod = liftToMethodDeclaration;
	        	final AstGenerator gen = this._gen;
	        	final RoleModel boundRootRole = this._boundRootRoleModel;
	            MethodModel.getModel(newMethod).setStatementsGenerator(new AbstractStatementsGenerator() {
	      			@SuppressWarnings("synthetic-access")
					public boolean generateStatements(AbstractMethodDeclaration methodDecl) {
	      				try {
		      		        Lifting.this._gen = gen;
		      		        Lifting.this._boundRootRoleModel = boundRootRole;
		      				return createLiftToMethodStatements(
						                    newMethod,
						                    teamBinding,
						                    roleClassBinding,
						                    baseClassBinding,
						                    caseObjects);
	      				} finally {
	      					Lifting.this._gen = null;
	      					Lifting.this._boundRootRoleModel = null;
	      				}
	      			}
	            });
	        }

	        if (needToAdd) {
	        	if (teamTypeDeclaration.isRole()) {
	        		TypeDeclaration interfaceAst = teamTypeDeclaration.getRoleModel().getInterfaceAst();
	        		if (interfaceAst != null) {
	        			MethodDeclaration ifcMethod = AstConverter.genRoleIfcMethod(interfaceAst, liftToMethodDeclaration);
						AstEdit.addMethod(interfaceAst, ifcMethod);
						liftToMethodDeclaration.modifiers = liftToMethodDeclaration.modifiers
                        										& ~AccProtected
                        										| AccPublic;
	        		}
	        	}
	        	AstEdit.addMethod(teamTypeDeclaration, liftToMethodDeclaration);
	        }

        } finally {
        	this._boundRootRoleModel = null;
        	this._gen = null;
        }
    }

    private MethodDeclaration createLiftToMethodDeclaration(
            TypeDeclaration  teamDecl,
            ReferenceBinding returnType,
			char[]           methodName,
			Argument[]       arguments,
            ReferenceBinding baseType)
    {
        return this._gen.method(teamDecl.compilationResult,
  /*modifiers*/	  returnType.modifiers & (AccPublic|AccProtected|AccSynchronized),
/*return type*/   createRoleTypeReference(returnType),
   /*selector*/	  methodName,
  /*arguments*/	  arguments
        );
    }

    // type reference may need to be parameterized if role type has type variables
	private TypeReference createRoleTypeReference(ReferenceBinding roleType) {
		TypeVariableBinding[] typeVariables = roleType.typeVariables();
    	if (typeVariables == Binding.NO_TYPE_VARIABLES)
    		return this._gen.typeReference(roleType);
		TypeReference[] typeParameters = new TypeReference[typeVariables.length];
		for (int i=0; i<typeVariables.length; i++)
			typeParameters[i] = this._gen.typeReference(typeVariables[i]);
		return this._gen.parameterizedSingleTypeReference(roleType.internalName(), typeParameters, 0);
	}

    private boolean createLiftToMethodStatements(
        MethodDeclaration liftToMethodDeclaration,
        ReferenceBinding teamBinding,
        ReferenceBinding roleClassBinding,
        ReferenceBinding baseClassBinding,
        RoleModel[] caseObjects)
    {
        liftToMethodDeclaration.setStatements(
        	new Statement[] {
	        	this._gen.synchronizedStatement(createCacheFieldRef(), new Statement[] {

	        		// MyRole myRole = null;
	        		this._gen.localVariable(
	        				MY_ROLE,
	        				createRoleTypeReference(roleClassBinding),
							this._gen.nullLiteral()),


					// if(base == null)
					createSanityCheck(),

					// conditional generation (see below)
					maybeCreateTeamMemberCheck(baseClassBinding),

					// if(!_OT$team_param._OT$cache_OT$RootRole.containsKey(base))
					createRoleExistentCheck(
		                roleClassBinding,
		                baseClassBinding,
		                teamBinding,
		                caseObjects),

					// return ...
					createReturnStatement(roleClassBinding)
	        	})
        });
        return true;
    }

	private IfStatement createSanityCheck() {
        // if
        return this._gen.ifStatement(
        			// (base == null)
        			this._gen.nullCheck(this._gen.singleNameReference(BASE)),
					// (then): return null;
				    this._gen.block(new Statement[] {
				    		this._gen.returnStatement(this._gen.nullLiteral())
				    })
			   );
    }

	/**
	 * If a base class is an externalized role, lifting must check, whether the base object
	 * actually belongs to the team given in the externalized role's team anchor.
	 * (assuming: class R playedBy teamAnchor.BaseRole)
	 */
	private Statement maybeCreateTeamMemberCheck(ReferenceBinding baseClass)
	{
		// if (((<__OT__BaseRole>)base).getTeam() != <teamAnchor>)
		//     throw new LiftingVetoException(this, base);
		if (! (baseClass instanceof RoleTypeBinding))
			return this._gen.emptyStatement();
		RoleTypeBinding baseRole = (RoleTypeBinding)baseClass;
		if (!baseRole.hasExplicitAnchor())
			return this._gen.emptyStatement();
		return this._gen.ifStatement(
				new EqualExpression(
					this._gen.messageSend(
						this._gen.baseNameReference(BASE),
						_OT_GETTEAM,
						null),
					this._gen.singleNameReference(baseRole._teamAnchor.internalName()),
					OperatorIds.NOT_EQUAL),
				this._gen.block(new Statement[] {
					this._gen.throwStatement(
						this._gen.allocation(
							this._gen.qualifiedTypeReference(ORG_OBJECTTEAMS_LIFTING_VETO),
							new Expression[] {
								this._gen.thisReference(),
								this._gen.singleNameReference(BASE)
							}
						)
					)
				}));
	}

    private IfStatement createRoleExistentCheck(
            ReferenceBinding returnType,
            ReferenceBinding baseType,
            ReferenceBinding teamType,
            RoleModel[]      caseObjects)
    {
    	// if
        return this._gen.ifStatement(
        		// (!_OT$team_param._OT$cache_OT$RootRole.containsKey(base))
        		createRoleExistentCheck(baseType),
				// (then:)
				this._gen.block(
					// (create a role of the best matching type:)
					new Statement[] {
//					/*debug*/createPrintBaseTag(teamType, returnType, caseObjects, _gen),
					createSwitchStatement(teamType, returnType, caseObjects, this._gen)}),
				// (else: return existing role)
				createElseBlock(returnType, teamType));
    }

    private UnaryExpression createRoleExistentCheck(
        ReferenceBinding baseType)
    {
        // !_OT$team_param._OT$cache_OT$RootRole.containsKey(base)
        return new UnaryExpression(
        		this._gen.messageSend(
		        		createCacheFieldRef(),
						CONTAINS_KEY,
						new Expression[] {
		        				this._gen.singleNameReference(BASE),
		        		}),
        		OperatorIds.NOT);
    }

    /*
	 * see SwitchOnBaseTypeGenerator.createCaseStatement(RoleModel,AstGenerator).
	 */
	protected Statement createCaseStatement(RoleModel role, AstGenerator gen)
	{
        /* case 4:
         *     myRole = _OT$team_param. new MySubRoleA((MySubBaseA)base);
         *     break;
         * case 5:
         * case 6:
         *     myRole = _OT$team_param. new MySubRoleB((MySubBaseB)base);
         *     break;
         */
		if (role.hasBaseclassProblem())
			return null;

		return
			gen.assignment(
		    	// myRole = ...
		        gen.singleNameReference(this.variableName),

		        // ... new MySubRoleB((MySubBaseB)base)
		        gen.allocation(
					createRoleTypeReference(role.getBinding()),
					new Expression[] {
		        		// use cast to establish actual type of base object:
		                gen.castExpression(
		                    gen.singleNameReference(BASE),
		                    gen.baseclassReference( // implicitly refined base may be weakened, apply the weak type:
		                    		WeakenedTypeBinding.getBytecodeType(role.getBaseTypeBinding())),
							CastExpression.DO_WRAP)
		    		}));
	}

    /*
	 * see SwitchOnBaseTypeGenerator.createDefaultStatement(ReferenceBinding,AstGenerator).
	 */
	protected Statement createDefaultStatement(ReferenceBinding roleType, AstGenerator gen)
	{
// disabled implementation of TPX-487 option (2)
//		/* 0:  break; // nop */
//		statements.add (gen.caseStatement(gen.intLiteral(0)));
//		statements.add (gen.breakStatement());
        /*
         * default:
         *     throw new LiftingFailedException(base, "MyRole");
         */
        return genLiftingFailedException(BASE, roleType, gen);
	}

	public static ThrowStatement genLiftingFailedException(char[] baseVarName, ReferenceBinding roleType, AstGenerator gen) {
		// throw new LiftingFailedException(base, "MyRole");
		return
			gen.throwStatement(
				gen.allocation(
					gen.qualifiedTypeReference(new char[][] {
			                ORG, OBJECTTEAMS,
			                LIFTING_FAILED_EXCEPTION
			            }),
					new Expression[] {
			            gen.singleNameReference(baseVarName),
			            gen.stringLiteral(roleType.sourceName())
					}));
	}

	// try to return casted local role
    private Block createElseBlock(
            ReferenceBinding returnType,
            ReferenceBinding teamType)
    {
        // else{ ... }

        // try { myRole = (MyRole)role }
        // catch (ClassCastException ex) { throw new WrongRoleException ( ... ); }
        TryStatement tryStatement = new TryStatement();
        tryStatement.tryBlock = createTryCastBlock(returnType);
        createCatchClassCastExceptionBlock(returnType, teamType, tryStatement);

        return this._gen.block2(
                createCacheLookupLocalDeclaration(),
                tryStatement
        );
    }

    private LocalDeclaration createCacheLookupLocalDeclaration() {
        // RootRole role = _OT$team_param._OT$cache_OT$RootRole.get(base);
        char[] roleType = this._boundRootRoleModel.getName();
		Expression getCall = this._gen.messageSend(  			        // init
			createCacheFieldRef(),
			GET,
			new Expression [] {
				this._gen.singleNameReference(BASE)
			});
		if (this._sourceLevel < ClassFileConstants.JDK1_5)
			getCall= this._gen.castExpression(getCall, this._gen.singleTypeReference(roleType), CastExpression.RAW);
		return this._gen.localVariable(
                	ROLE,        					   		 // name
					this._gen.singleTypeReference(roleType), // type
					getCall);
    }


    private FieldReference createCacheFieldRef()
    {
        // _OT$team_param._OT$cache_OT$RootRole
        return this._gen.fieldReference(
    				ThisReference.implicitThis(),
					LiftingEnvironment.getCacheName(this._boundRootRoleModel));
    }

    private Block createTryCastBlock(
            ReferenceBinding returnType)
    {
        /*
         * try
         * {
         *      myRole = (MyRole)role;
         * }
         */
        return this._gen.block(new Statement[]{
		        	this._gen.assignment(
		        		this._gen.singleNameReference(MY_ROLE),
						this._gen.castExpression(
								this._gen.singleNameReference(ROLE),
								createRoleTypeReference(returnType),
								CastExpression.DO_WRAP))
		        });
    }

    private void createCatchClassCastExceptionBlock(
            ReferenceBinding returnType,
            ReferenceBinding teamType,
            TryStatement     tryStatement)
    {
        // catch(ClassCastException classcastexception)
        tryStatement.catchArguments = new Argument[] {
                this._gen.argument(
                    CLASS_CAST_EXCEPTION,                             // name
                    this._gen.singleTypeReference(_CLASS_CAST_EXCEPTION_)) // type
        };

        // throw new WrongRoleException(MyTeam$MyRole.class, base, role);
        tryStatement.catchBlocks = new Block[]{
        	this._gen.block(new Statement[] {
        		this._gen.throwStatement(
    				this._gen.allocation(
		        		this._gen.qualifiedTypeReference(new char[][] {
		                    ORG, OBJECTTEAMS,
		                    WRONG_ROLE_EXCEPTION
		                }),
						new Expression[] {
			                this._gen.classLiteralAccess(this._gen.typeReference(returnType)),
			                this._gen.singleNameReference(BASE),
			                this._gen.singleNameReference(ROLE)
		        		}))
        	})
		};
    }

    private ReturnStatement createReturnStatement(ReferenceBinding returnType)
    {
        // return myRole;
        return this._gen.returnStatement(this._gen.singleNameReference(MY_ROLE));
    }

    /**
	 * API for AbstractMethodDeclaration:
	 *
     *  Create a byte code sequence for a runtime check in a creation method:
     *  R _OT$create_OT$R(B b) {
     * 	   if (this._OT$cache_OT$R.contains(b))
     *  	  throw new DuplicateRoleException("R");
     *     // continue regular code.
     *  }
     *
     * Note, the need for this runtime check is detected quite late.
     * At this point it is easier to create the byte code sequence directly,
     * rather the creating AST first.
     */
	public static void createDuplicateRoleCheck(CodeStream codeStream, AbstractMethodDeclaration method)
	{
		MethodBinding binding = method.binding;
		Scope scope = method.scope;

		ReferenceBinding roleType = (ReferenceBinding)binding.returnType;
		String roleName = new String(roleType.readableName());
		// TODO(SH): check why roleType.getRoleModel().getClassPartBinding().roleModel may yield a different result.
		// I think it occured in haeder/stopwatch from smile-CVS.
		//RoleModel role = roleType.getRealClass().roleModel;
		char[] cacheName = LiftingEnvironment.getCacheName(roleType.roleModel.getBoundRootRole());

		ReferenceBinding teamBinding = roleType.enclosingType();//role.getTeamModel().getBinding();
		FieldBinding cache = TypeAnalyzer.findField(
								teamBinding, cacheName, /*static*/false, /*outer*/false,
								ITranslationStates.STATE_FULL_LIFTING); // generated by this state
		if (cache == null)
			throw new InternalCompilerError("generated cache field not found: "+new String(cacheName)); //$NON-NLS-1$
		ReferenceBinding map = (ReferenceBinding)scope.getType(IOTConstants.WEAK_HASH_MAP, 3);
		MethodBinding contains = map.getMethod(scope, IOTConstants.CONTAINS_KEY);

		ReferenceBinding exc = (ReferenceBinding)scope.getType(IOTConstants.ORG_OBJECTTEAMS_DUPLICATE_ROLE, 3);
		TypeBinding[] types = new TypeBinding[] { scope.getJavaLangString() };
		MethodBinding excInit = exc.getExactConstructor(types);

		BranchLabel normalCase = new BranchLabel(codeStream);

		codeStream.aload_0();               								// this
		codeStream.fieldAccess(Opcodes.OPC_getfield, cache, teamBinding);	// getfield      MyTeam._OT$cache_OT$R Ljava/util/WeakHashMap;
		codeStream.aload_1();               								// arg0
		codeStream.invoke(Opcodes.OPC_invokevirtual, contains, map); 		// invokevirtual java.util.WeakHashMap.containsKey (Ljava/lang/Object;)Z
		codeStream.ifeq(normalCase);        								// false -> #endif
		codeStream.new_(exc);               								// new           <org.objectteams.DuplicateRoleException>
		codeStream.dup();                   								// dup
		codeStream.ldc(roleName);           								// ldc "R"
		codeStream.invoke(Opcodes.OPC_invokespecial, excInit, exc); 		// invokespecial org.objectteams.DuplicateRoleException.<init> (Ljava/lang/String;)V
		codeStream.athrow();                								// athrow
		normalCase.place();                 								// #endif
	}
}
