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
 * $Id: LiftingEnvironment.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;

import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccPublic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccSynthetic;
import static org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants.AccTransient;
import static org.eclipse.objectteams.otdt.core.compiler.IOTConstants.CACHE_PREFIX;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.StateMemento;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.SourcePosition;
import org.eclipse.objectteams.otdt.internal.core.compiler.smap.StepOverSourcePosition;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.ReflectionGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.RoleMigrationImplementor;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.SerializationGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.StandardElementGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer.TeamMethodGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstConverter;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstEdit;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Protections;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.TypeAnalyzer;

/**
 * This class stores context information, esp. the hierarchies of roles of a given
 * team. This information is used for creating the lifting infrastructure and
 * a few related things.
 * It is also responsible for create the role caches and a method for their initialization.
 *
 * @author Markus Witte
 * @version $Id: LiftingEnvironment.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class LiftingEnvironment
{
    private ProblemReporter _problemReporter;
    private TypeDeclaration _teamType;

    /* all roles of the current team. */
    private TreeNode[] _roles;
    /* create one role cache for each of these: */
    private TreeNode[] _boundRootRoles;

    /* key:    target role to lift to
     * values: sub-roles that can be created by the liftTo method. */
    private HashMap<RoleModel,RoleModel[]> _caseObjects = new HashMap<RoleModel,RoleModel[]>();
    /**
     * Setup a lifting environment by analyzing a team's role hierarchies.
     * Stores the results of analysis in this lifting environment.
     * @param teamType
     */
    public LiftingEnvironment(TypeDeclaration teamType)
    {
    	init(teamType);
    }
    public void init(TypeDeclaration teamType)
    {
        this._problemReporter = teamType.scope.problemReporter();
        this._teamType        = teamType;
        this._roles = getRoles();
        connectHierarchy(this._roles);
        this._boundRootRoles = filterBoundRootNodes(this._roles);
    }

	/**
	 * Analyze the role hierarchy and create:
	 * + RoleBaseBindingAttribute
	 * + _OT$base fields
     * + liftto constructors (includes removal of default constructors)
	 *
	 * @param lateRole if set create infrastructure only for this late accepted role file.
     */
    public void createRoleBaseLinkage(RoleModel lateRole, boolean needMethodBodies)
    {
    	if(lateRole != null && lateRole.isSynthInterface())
    		return; // no base field here. FIXME(SH): what about addRoleBaseBindingAttribute?

        TreeNode[] boundRoles = getBoundRoles();
        if ((boundRoles == null) || (boundRoles.length == 0))
            return;

        Lifting lifting = new Lifting();
        for (int i = 0; i < boundRoles.length; i++)
        {
            TreeNode currentNode = boundRoles[i];
        	RoleModel currentRole = currentNode.getTreeObject();
			if ((lateRole != null) && (currentRole != lateRole))
        		continue; // only process 'lateRole'
            this._caseObjects.put(currentRole, createOneRoleBaseLinkage(lifting, currentNode, needMethodBodies));
            if (lateRole != null)
            	return; // after processing one role we're done
        }
        if (lateRole != null && !lateRole.hasBaseclassProblem()) // after base class prob don't expect too much.
        	throw new InternalCompilerError("late role not found in boundRoles: "+new String(lateRole.getBinding().internalName())); //$NON-NLS-1$
    }

    /**
     * Creates the following infrastructure for lifting:
     * + role caches
     * + liftto methods
     * + reflection methods
     *
     * @param lateRole
     * @param needMethodBodies
     */
    public void createLiftingInfrastructure(RoleModel lateRole, boolean needMethodBodies) {
        TreeNode[] boundRoles = getBoundRoles();
        if ((boundRoles == null) || (boundRoles.length == 0)) {
            if (   lateRole == null
            	&& needMethodBodies
            	&& !TypeAnalyzer.isOrgObjectteamsTeam(this._teamType.binding)
            	&& !Protections.hasClassKindProblem(this._teamType.binding))
            		ReflectionGenerator.createRoleQueryMethods(this._teamType);
            return;
        }
        
        if (lateRole == null)
			TeamMethodGenerator.addFakedTeamRegistrationMethods(this._teamType.binding);

        Lifting lifting = new Lifting();
        // create lift methods only after all bound base classes have been seen
        for (int i = 0; i < boundRoles.length; i++)
        {
        	if (lateRole != null && boundRoles[i].getTreeObject() != lateRole)
        		continue;
        	RoleModel[] cases = this._caseObjects.get(boundRoles[i].getTreeObject());
        	if (cases != null)
        		lifting.createLiftToMethod(this._teamType, boundRoles[i], cases);
        }
        // the the TreeNode corresponding to the current lateRole:
        TreeNode node = null;
        for (int i = 0; i < boundRoles.length; i++)
        {
        	if (lateRole == boundRoles[i].getTreeObject()) {
        		node = boundRoles[i];
            	break; // only process this one role
        	}
        }
        // Different strategies for early/late roles:
        // 1. role caches
        // 2. reflection methods
        if (lateRole == null) {
            generateRoleCaches(this._teamType);
            // TODO(SH): split into decl and statements (see fillGeneratedMethods()).
            if (needMethodBodies && !Protections.hasClassKindProblem(this._teamType.binding))
            	ReflectionGenerator.createRoleQueryMethods(this._teamType);
        } else {
        	// isTopBound?
    		if (node != null && node.getTopmostBoundParent(true) == node) {
    			generateRoleCache(this._teamType, lateRole);
    		}
        }
    }
    
	/**
	 * @param lifting
     * @param node representing the role to generate for
     * @return array of case objects for lift method creation
	 */
	private RoleModel[] createOneRoleBaseLinkage(
			Lifting lifting,
			TreeNode node,
			boolean  needMethodBodies)
	{
		RoleModel[] result = null;
		RoleModel roleModel = node.getTreeObject();
        if (roleModel.hasBaseclassProblem())
            return null;

        TypeDeclaration roleType = roleModel.getAst();
        if (roleType != null && roleType.isRoleFile() && roleType.compilationUnit != null)
        	needMethodBodies = roleType.compilationUnit.parseMethodBodies;

        // before accessing base class check its presence:
        if (roleModel.getBaseTypeBinding() == null && roleType == null) // stale binary
        	this._problemReporter.staleSubRole(roleModel.getBinding(), roleModel.getBinding().superclass()); // this aborts

		// create elements for the team:
        // Attribute:
        roleModel.getTeamModel().addRoleBaseBindingAttribute(
                roleModel.getBinding().attributeName(),
                roleModel.getBaseclassAttributename(false), // don't include anchor
                roleModel.getBaseTypeBinding().isInterface());
        // TODO(SH): move to STATE_FULL_LIFTING: (?)
        // Analysis may detect errors and prepares for liftmethod generation.
        if (needMethodBodies) {
			RoleHierarchieAnalyzer analyzer =
			    new RoleHierarchieAnalyzer(this._teamType, this._problemReporter);
	        if (!roleModel.getBinding().isHierarchyInconsistent())
	        	result = analyzer.analyze(node);
        }

		// specific elements for the role only if not binary:
        if (!roleModel.getBinding().isBinaryBinding()) {
			if (roleType != null  && !roleModel.getBinding().isSynthInterface())
			{
			    StandardElementGenerator.generatePlayedByElements(
			            roleType.getRoleModel(),
			            node.getBoundParent(false));
		    	lifting.createLiftToConstructorDeclaration(node, needMethodBodies);
		    	AstEdit.removeDefaultConstructor(roleType);
		    	RoleMigrationImplementor.checkAddMigrateToBaseMethod(roleType, node);
			}
        } else {
        	// mini version for binaries:
        	if (!roleModel.getBinding().isInterface())
        		setBoundRootRole(node); // otherwise done in createLiftToConstructorDeclaration()
        }
		return result;
	}

	private static void setBoundRootRole(TreeNode roleNode) {
        // for determining the cache interfaces are allowed:
        RoleModel boundRootRoleModel = roleNode.getTopmostBoundParent(true).getTreeObject();
        if (boundRootRoleModel != null)
        	roleNode.getTreeObject()._boundRootRole = boundRootRoleModel;
	}

	/**
	 * @param boundRootRole
	 */
	public static char[] getCacheName(RoleModel boundRootRole) {
		if (boundRootRole == null)
			return null;
	    return CharOperation.concat(CACHE_PREFIX, boundRootRole.getBinding().sourceName());
	}

	/**
     * For each bound root role one lifting cache is generated,
     * unless the cache is already inherited from the super team.
     * @param teamType
     */
    private void generateRoleCaches(TypeDeclaration teamType) {
        if (this._boundRootRoles == null || this._boundRootRoles.length == 0)
            return;
        for (int i=0; i<this._boundRootRoles.length; i++){
            generateRoleCache(teamType, this._boundRootRoles[i].getTreeObject());
        }
        generateInitCaches(teamType);
    }

    /**
	 * Generate a role cache regarding 'boundRootRole' and record it in the team model.
	 * @param teamDecl
	 * @param boundRootRole
	 */
	private static void generateRoleCache (TypeDeclaration teamDecl, RoleModel boundRootRole)
	{
	    char[] cacheName = getCacheName(boundRootRole);
	    FieldBinding foundField = TypeAnalyzer.findField(
	    								teamDecl.binding, cacheName, /*static*/false, /*outer*/false,
	    								ITranslationStates.STATE_FULL_LIFTING); // generated in this state

		if (foundField != null)
	        return;
	    int sStart, sEnd;
	    TypeDeclaration rootAst = boundRootRole.getAst();
	    if (rootAst != null) {
	    	sStart = rootAst.sourceStart;
	    	sEnd   = rootAst.sourceEnd;
	    } else {
	    	sStart = teamDecl.sourceStart;
	    	sEnd   = teamDecl.sourceEnd;
	    }
	    boolean usingRawType = boundRootRole.getBaseTypeBinding().isParameterizedType();
	    AstGenerator gen = new AstGenerator(sStart, sEnd);
	    if (usingRawType)
	    	gen.shiftPosition(); // introduce a minimal offset (cf. also gen2 in ReflectionGenerator#createRoleQueryMethods)

		QualifiedTypeReference fieldTypeRef;
	    fieldTypeRef = gen.getCacheTypeReference(teamDecl.scope, boundRootRole);

	    // Note (SH): caches are not initialized directly, but via method _OT$initCaches,
	    // which in turn is invoked as initialization of synthetic field _OT$cacheInitTrigger.
	    // This way, further initialization calls can easily be added elsewhere,
	    // like in team-ctors which add arg-lifting into the byte code.

		// caches are not final for the sake of initCaches-methods.
	    FieldDeclaration field = gen.field(
	    		(AccPublic|AccSynthetic|AccTransient), fieldTypeRef, cacheName, /*alloc*/null);
	    // Note (SH): if the cache would be generated with lesser access rights
	    // (e.g., protected), we would need synthetic access methods, when accessing
	    // across packages, and all accesses would have to be redirected.
	    // Somehow, this doesn't happen automagically.
	    // But by declaring the caches public the issue is completely avoided,
	    // hoping, that no-one maliciously accesses the cache.
	    
	    // if base/role are generic, we refer to them via the raw type, suppress that warning:
	    if (usingRawType) {
		    field.annotations = new Annotation[] {
		    	gen.singleStringsMemberAnnotation(TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new char[][]{"rawtypes".toCharArray()}) //$NON-NLS-1$ 
		    };
	    }
	    
	    AstEdit.addField(teamDecl, field, true, false/*typeProblem*/);
		teamDecl.getTeamModel().addCache(field);
	}

    /**
     * Create a method that initializes all caches introduced in a given team.
     * Only create method declaration, no statements yet.
     * (see fillGeneratedMethods()).
     *
     * @param teamType the type to add the method to.
     * @return the _OT$initCaches method
     */
    private static MethodDeclaration generateInitCaches(TypeDeclaration teamType)
    {
    	MethodDeclaration initMethod =
    		AstConverter.findAndAdjustCopiedMethod(
    				teamType, IOTConstants.OT_INIT_CACHES, null);
    	if (initMethod == null)
    	{
    		AstGenerator gen = new AstGenerator(teamType.sourceStart, teamType.sourceEnd);
    		gen.shiftPosition(); // don't let @SuppressWarnings interfere with non-generated code.
    		
    		initMethod = gen.method(
    			teamType.compilationResult,
				ClassFileConstants.AccPrivate, // no overriding!!
				TypeBinding.BOOLEAN,
				IOTConstants.OT_INIT_CACHES,
				null);
    		initMethod.statements = new Statement[0]; // to be filled by fillInitCaches()
    		initMethod.annotations = new Annotation[] { // in case base type is a raw type
    			gen.singleStringsMemberAnnotation(TypeConstants.JAVA_LANG_SUPPRESSWARNINGS, new char[][]{"all".toCharArray()}) //$NON-NLS-1$	
    		};
    		AstEdit.addMethod(teamType, initMethod);
    		initMethod.modifiers |= ExtraCompilerModifiers.AccLocallyUsed; // prevent 'unused' warning

    		if (teamType.isRole()) {
	    		TypeDeclaration ifcPart = teamType.getRoleModel().getInterfaceAst();
    			if (ifcPart != teamType) {
    				AbstractMethodDeclaration methodIfcPart =
    						TypeAnalyzer.findMethodDecl(ifcPart, IOTConstants.OT_INIT_CACHES, 0);
    				if (methodIfcPart == null) {
	    				methodIfcPart = AstConverter.genRoleIfcMethod(
	    						teamType.enclosingType, initMethod);
	    				AstEdit.addMethod(ifcPart, methodIfcPart);
    				}
    			}
    		}

    		// Serialization: generate restore methods to initialize caches/register roles:
    		SerializationGenerator.generateRestoreMethods(teamType, gen);    		
    	}

    	return initMethod;
    }

    /**
     * TODO(SH): use IStatementsGenerator instead?
     * After late roles are in place create the statements for some generated methods.
     * @param teamType
     */
    public static void fillGeneratedMethods(TypeDeclaration teamType) {
    	fillInitCaches(teamType, teamType.getTeamModel().caches);
    }
    private static void fillInitCaches(TypeDeclaration teamType, FieldDeclaration[] caches)
    {
    	/*
    	 * generate:
    	 * {
    	 * 		// for each cache declared in this team:
    	 * 		if (_OT$cache<x> == null) {
    	 * 			_OT$cache<c> = new WeakHashMap<Bx,Rx>();
    	 *      }
    	 *      // Note: no super call, super team's ctor is already responsible for invoking its (private) initCaches
    	 * }
    	 */
    	AbstractMethodDeclaration initMethod = TypeAnalyzer.findMethodDecl(teamType, IOTConstants.OT_INIT_CACHES, 0);
    	if (initMethod == null) {
    		// not yet generated? (maybe no bound roles/caches at that time).
    		if (teamType.getTeamModel().caches.length == 0)
    			return;
    		initMethod = generateInitCaches(teamType);
    	}
    	AstGenerator gen = new AstGenerator(initMethod); // re-use position
    	Statement[] statements = new Statement[caches.length+1];
    	for (int i = 0; i < caches.length; i++) {
    		// FIXME(SH): unclear if needed after allowing generated qualified role type referneces:
    		TypeReference cacheTypeRef = caches[i].type; // robustness, but with wrong source position
    		if (caches[i].type.resolvedType instanceof ParameterizedTypeBinding) {
    			// reconstruct a type reference from the resolved cache type
    			ParameterizedTypeBinding oldBinding = (ParameterizedTypeBinding)cacheTypeRef.resolvedType;
    			if (oldBinding.arguments.length == 2) {
					ReferenceBinding roleBinding = (ReferenceBinding)oldBinding.arguments[1];
					// respect different status for base/role types (scope, decapsulation).
					cacheTypeRef = gen.getCacheTypeReference(teamType.scope, roleBinding.roleModel);
				}
    		}
			statements[i] = gen.ifStatement(
					new EqualExpression(
        				gen.singleNameReference(caches[i].name),
						gen.nullLiteral(),
						OperatorIds.EQUAL_EQUAL),
				    gen.block(new Statement[] {
				    	gen.assignment(
				    		gen.singleNameReference(caches[i].name),
				    		gen.allocation(cacheTypeRef, new Expression[0]))
			}));

		}
    	statements[caches.length] = gen.returnStatement(gen.booleanLiteral(true));
    	initMethod.setStatements(statements);
    	// Serialization:
    	SerializationGenerator.fillRestoreRole(teamType, caches);

    	// ===== also add the field that triggers invocation of this method from all constructors: =====

		//save source positions from AstGenerator (ike)
    	SourcePosition savePos = gen.getSourcePosition();
		try {
			//set STEP_OVER source positions (ike)
			gen.setSourcePosition(new StepOverSourcePosition());

			ThisReference thisReference = gen.thisReference();
			thisReference.resolvedType = teamType.binding.getRealClass(); // avoid wrapping of nested team
			thisReference.constant = Constant.NotAConstant;

			FieldDeclaration trigger = gen.field(
					ClassFileConstants.AccPrivate | ClassFileConstants.AccSynthetic,
					gen.singleTypeReference("boolean".toCharArray()), //$NON-NLS-1$
					IOTConstants.CACHE_INIT_TRIGGERER,
					gen.messageSend(thisReference, IOTConstants.OT_INIT_CACHES, new Expression[0]));
			AstEdit.addField(teamType, trigger, true, false/*typeProblem*/);
			trigger.binding.modifiers |= ExtraCompilerModifiers.AccLocallyUsed; //  prevent 'unused' warning;

			// resolve them both:
			if (StateMemento.hasMethodResolveStarted(teamType.binding)) {
				initMethod.resolve(teamType.scope);
				trigger.resolve(teamType.initializerScope);
			}

		} finally {
			//restore source postions (ike)
			gen.setSourcePosition(savePos);
		}
    }

    // ==== ANALYSING THE ROLE HIERARCHY ====

    /**
     * Get all bound roles of this team.
     * @return array > 0 or null;
     */
    private TreeNode[] getBoundRoles()
    {
        TreeNode[] allRoles = this._roles;
        if ((allRoles == null) || (allRoles.length == 0))
        {
            return null;
        }
        Vector<TreeNode> boundRoles = new Vector<TreeNode>(0, 1);
        for (int i = 0; i < allRoles.length; i++)
        {
            TreeNode node = allRoles[i];
            RoleModel roleModel = node.getTreeObject();
            if (   roleModel.isBound()
            	&& !roleModel.hasBaseclassProblem())
            {
                boundRoles.add(node);
            }
        }
        if (boundRoles.size() == 0)
        {
            return null;
        }
        return boundRoles.toArray(new TreeNode[boundRoles.size()]);
    }

    /**
     * Linking TreeNodes for all roles: add children to parents in roles.
     * Finds all pairs of parent-child.
     * @param roles
     */
    private void connectHierarchy(TreeNode[] roles)
    {
    	TeamModel teamModel = this._teamType.getTeamModel();
    	HashSet<ReferenceBinding> baseTypes = new HashSet<ReferenceBinding>();
        for (int i = 0; i < roles.length; i++) {
            TreeNode parentNode = roles[i];
            // TODO(SH): potential for optimization: start with "j=_numChildrenSeen"
            for (int j = 0; j < roles.length; j++) {
                if (i == j) continue;
                TreeNode childNode = roles[j];
                if ((parentNode.getTreeObject()).isSuperTypeOf(
                        childNode.getTreeObject()))
                {
                	// connect TreeNodes:
                    parentNode.add(childNode);
                    // build BoundClassesHierarchy (role side):
    		    	teamModel.addBoundClassLink(
    		    			childNode.getTreeObject().getBinding(),
    		    			parentNode.getTreeObject().getBinding());
                }
            }
            // remember base types:
            ReferenceBinding parentBase = parentNode.getTreeObject().getBaseTypeBinding();
            if (parentBase != null)
            	baseTypes.add(parentBase);
        }
        // build BoundClassesHierarchy (base side):
        for(ReferenceBinding childBase: baseTypes) {
            ReferenceBinding currentBase = childBase.superclass();
            while (currentBase != null) {
            	if (baseTypes.contains(currentBase)) {
            		teamModel.addBoundClassLink(childBase, currentBase);
            		break;
            	}
            	currentBase = currentBase.superclass();
            }
        }
    }


    /**
     * Collect all direct roles from a given team only omitting
     * synthetic role interfaces.
     * @return non-null array.
     */
    private TreeNode[] getRoles()
    {
        ReferenceBinding[] roles = this._teamType.binding.memberTypes;
        TreeNode[] treeNodes = new TreeNode[roles.length];
        int count = 0;
        for (int j = 0; j < roles.length; j++)
        {
            ReferenceBinding role = roles[j];
            if (role.isEnum())
            	continue;
            if (!role.roleModel.isSynthInterface())
            {
                RoleModel roleModel = role.roleModel;
                TreeNode treeNode = new TreeNode(roleModel);
                treeNodes[count++] = treeNode;
            }
        }
        TreeNode[] treeRoles = new TreeNode[count];
        System.arraycopy(treeNodes, 0, treeRoles, 0, count);
        return treeRoles;
    }

    /**
     * Extract from a set of roles only those that are bound and have no
     * bound parent (super-type).
     * @param treeNodes
     * @return bound root roles.
     */
    private static TreeNode[] filterBoundRootNodes(TreeNode[] treeNodes)
    {
        TreeNode[] liftingTypeTrees = new TreeNode[treeNodes.length];
        int count = 0;
        for (int i = 0; i < treeNodes.length; i++)
        {
            TreeNode node = treeNodes[i];
            if (    node.getTreeObject().isBound()
                && !node.hasBoundParent(true)
                && !node.getTreeObject().hasBaseclassProblem())
            {
                liftingTypeTrees[count++] = node;
            }
        }
        TreeNode[] rootNodeTrees = new TreeNode[count];
        System.arraycopy(liftingTypeTrees, 0, rootNodeTrees, 0, count);
        return rootNodeTrees;
    }

    public String toString()
    {
        String str = ""; //$NON-NLS-1$
        if (this._boundRootRoles == null)
        {
            return str;
        }
        str += "RootRolesHierarchy:\n"; //$NON-NLS-1$
        for (int i = 0; i < this._boundRootRoles.length; i++)
        {
            TreeNode roleNode = this._boundRootRoles[i];
            str += "RootRole:\n"; //$NON-NLS-1$
            str += roleNode.toString(1);
        }
        str += "\n";      //$NON-NLS-1$
        return str;
    }
}
