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
 * $Id: RoleHierarchieAnalyzer.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lifting;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.objectteams.otdt.core.compiler.Pair;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;

/**
 * MIGRATION_STATE: complete.
 *
 * step 3 (hierarchie analysis) of smart lifting algorithm
 * step 3 = step 4 + step 5
 * @author brcan ,kaschja
 * @version $Id: RoleHierarchieAnalyzer.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class RoleHierarchieAnalyzer
{
	private ProblemReporter _problemReporter;
	private TypeDeclaration _teamTypeDeclaration;

	public RoleHierarchieAnalyzer(TypeDeclaration teamTypeDeclaration, ProblemReporter problemReporter)
	{
		this._teamTypeDeclaration = teamTypeDeclaration;
		this._problemReporter = problemReporter;
	}

	/**
	 * step 3 of smart lifting algorithm
	 * @param role the role whose sub-hierarchie is analyzed
	 * @return the roles which are filtered by the analysis
	 */
	public RoleModel[] analyze(TreeNode role)
	{
		RoleModelList relevantRoles = getRelevantRoles(role);
        checkInstantiability(relevantRoles);
        RoleModelList result = foldBaseClasses(relevantRoles);
		RoleModel[] resultArray = result.toArray();
		role.getTreeObject().setSubRoles(resultArray);
		return resultArray;
	}

	/**
	 * step 4 of smart lifting algorithm (elimination of irrelevant roles)
	 * @param role the role whose sub-hierarchie is analyzed
	 * @return vector of RoleTreeObjects
	 */
	private RoleModelList getRelevantRoles(TreeNode role)
	{
		RoleModelList result   = new RoleModelList();
		TreeNode[] 	  children = role.getChildren();
		RoleModel     parent   = role.getTreeObject();

		// stop recursion if the role has no children
		if (children == null)
		{
            if (!parent.hasBaseclassProblem())
			    result.add(parent);
			return result;
		}

        result.add(analyzeBaseTypeBindingOfChildren(children, parent));

		// invoke this very method recursively for every child
	    for (int idx = 0; idx < children.length; idx++)
        {
            RoleModelList relevantChildren = getRelevantRoles(children[idx]);
            if (!relevantChildren.isEmpty())
			    result.addList(relevantChildren);
	    }

		return result;
	}

	public void checkInstantiability(RoleModelList relevantRoles) {
        TeamModel teamModel = null;
        int i=0;
        while (i<relevantRoles.getSize()) {
            RoleModel role  = relevantRoles.get(i);
            if (teamModel == null)
                teamModel = role.getTeamModel();
            if (role.getBinding().isAbstract()) {
                if (!teamModel.getBinding().isAbstract()) {
                	if (   role.getBinding().isPublic()
                        && !role.getBinding().isInterface())
                		// public role classes could be instantiated from any part of the program,
                		// Force the team to be abstract, too:
                        this._problemReporter.abstractRelevantRole(
                                role.getAst(), teamModel.getBinding());
                    else if (role.getBaseTypeBinding().isAbstract())
                    	// if base is abstract, too, we could be lucky, just warn:
                        this._problemReporter.abstractPotentiallyRelevantRole(
                                role.getAst(), teamModel.getBinding());
                    else
                        this._problemReporter.abstractRelevantRole(
                                role.getAst(), teamModel.getBinding());
                }
                relevantRoles.remove(i);
            } else
                i++;
        }
    }

	/**
	 * analyze whether a role has children with the same "playedBy" relation
	 * @param children
	 * @param parent
	 */
    private RoleModel analyzeBaseTypeBindingOfChildren(
        TreeNode[] children,
        RoleModel parent)
    {
    	ReferenceBinding parentBaseType = parent.getBaseTypeBinding();

    	// check the "playedBy" relation for each sub-role class
    	// whether it is identical to the parent class
    	for (int idx = 0; idx < children.length; idx++)
        {
            RoleModel child = children[idx].getTreeObject();
            if (child.hasBaseclassProblem())
                continue;
            if(child.getBaseTypeBinding() == parentBaseType)
            	return null; // not relevant, child can be used for lifting
        }
    	// if the role has no children with the same "playedBy"
    	// relations, it is relevant
       	return parent;
    }

	/**
	 * step 5 of smart lifting algorithm (foldBaseClasses)
	 * @param roles vector of RoleModels (result of getRelevantRoles())
	 * @return vector of RoleModels
	 */
	private RoleModelList foldBaseClasses(RoleModelList roles)
	{
		if ((roles == null) || roles.isEmpty())
		{
			return new RoleModelList();
		}

	    return filterAmbiguousBaseRoleBindings(roles);
	}

    private RoleModelList filterAmbiguousBaseRoleBindings(RoleModelList roles)
    {
		RoleModelList  result 		   = new RoleModelList();
		RoleModelList  rolesToAnalyze = new RoleModelList();

		HashSet<RoleModel> ambiguitySet   = new HashSet<RoleModel>();

        RoleModel role 	= roles.get(0);
        ReferenceBinding baseBinding = role.getBaseTypeBinding();

        ambiguitySet.add(role);

        // filter roles with ambiguous base to role bindings
        for (int idx = 1; idx < roles.getSize(); idx++)
        {
            RoleModel  otherRole 	  = roles.get(idx);
        	ReferenceBinding otherBase = otherRole.getBaseTypeBinding();

        	// check whether both roles are bound to the same base
        	if (baseBinding == otherBase)
        	{
        		ambiguitySet.add(roles.get(idx));
        	}
        	else
        	{
        		rolesToAnalyze.add(roles.get(idx));
        	}
        }

        // there was only one reference on base
        if (ambiguitySet.size() == 1)
        {
        	result.add(role);
        }
        else
        {
        	ReferenceBinding[] commonSupers = getCommonBoundSuperRoles(ambiguitySet.iterator());
        	if (commonSupers.length > 0) {
        		this._problemReporter.potentiallyAmbiguousRoleBinding(this._teamTypeDeclaration, ambiguitySet);
        		for (ReferenceBinding commonSuper : commonSupers)
        			this._teamTypeDeclaration.getTeamModel().ambigousLifting.add(new Pair<ReferenceBinding, ReferenceBinding>(baseBinding, commonSuper));

        		for (int i = 0; i < commonSupers.length; i++) {
					commonSupers[i].roleModel._hasBindingAmbiguity = true;
				}
        	}
        }

		// call this very method recursively if there are still roles to analyze
		if (!rolesToAnalyze.isEmpty())
		{
			result.addList(filterAmbiguousBaseRoleBindings(rolesToAnalyze));
		}

		return result;
	}

	/**
	 * Given that for roles binding ambiguity exists, find the set of common
	 * bound super roles.
	 *
	 * @param roles
	 * @return array of super roles with baseclass binding and more than one
	 *           sub-role in 'roles'
	 */
	private ReferenceBinding[] getCommonBoundSuperRoles(Iterator<RoleModel> roles) {
		HashSet<ReferenceBinding> allBoundSupers = new HashSet<ReferenceBinding>();
		LinkedList<ReferenceBinding> commonBoundSupers = new LinkedList<ReferenceBinding>();
		while (roles.hasNext()) {
			RoleModel role = roles.next();
			ReferenceBinding currentRole = role.getBinding();
			while (true) {
				currentRole = currentRole.superclass();
				if (currentRole == null || ! currentRole.isRole())
					break;
				if (currentRole.baseclass() == null)
					continue;
				if (allBoundSupers.contains(currentRole))
					commonBoundSupers.add(currentRole);
				else
					allBoundSupers.add(currentRole);
			}
		}
		ReferenceBinding[] result = new ReferenceBinding[commonBoundSupers.size()];
		commonBoundSupers.toArray(result);
		return result;
	}
}
