/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SwitchOnBaseTypeGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.transformer;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.WeakenedTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.Sorting;

/**
 * Creates an instanceof cascade as needed for lifting and for base predicate checks.
 *
 * @author stephan
 * @version $Id: SwitchOnBaseTypeGenerator.java 23417 2010-02-03 20:13:55Z stephan $
 */
public abstract class SwitchOnBaseTypeGenerator implements IOTConstants {

    /**
     * Create the statement for one base type in the big cascade.
     *
	 * @param role       the most suitable role for the detected base type.
	 * @param gen        use for AST-generation
	 * @return           the generated statement or null
	 */
	protected abstract Statement createCaseStatement(RoleModel role, AstGenerator gen);

	/**
     * Hook into createSwitchStatement(), which should create a default branch, if needed.
     *
	 * @param staticRoleType expected role type.
	 * @param gen            use for AST-generation
	 * @return           the generated statement or null
	 */
	protected abstract Statement createDefaultStatement(ReferenceBinding staticRoleType, AstGenerator gen);

	/**
	 * Create the instanceof cascade based on a given base object.
	 * Note that the previous two methods are hooks which should create the actual
	 * statements for the cascade template.
	 *
	 * @param teamType       the team context
	 * @param staticRoleType we know we are about to lift to this role type or better
	 * @param caseObjects    one role model for each bound and relevant subtype of staticRoleType
	 * @param gen            use for AST-generation
	 * @return the assembled statement
	 */
	protected Statement createSwitchStatement(
			ReferenceBinding teamType,
			ReferenceBinding staticRoleType,
			RoleModel[]      caseObjects,
			AstGenerator     gen)
	{
		boolean hasBindingAmbiguity = teamType.getTeamModel().ambigousLifting.size() > 0;
		if (caseObjects.length == 1 && ((teamType.tagBits & TagBits.HasAbstractRelevantRole) == 0) && !hasBindingAmbiguity) {
			// avoid instanceof alltogether.
			return createCaseStatement(caseObjects[0], gen);
		}
		ReferenceBinding staticBaseType = staticRoleType.baseclass();

		RoleModel[] rolesToSort = new RoleModel[caseObjects.length];
		System.arraycopy(caseObjects, 0, rolesToSort, 0, caseObjects.length);
		caseObjects = Sorting.sortRoles(rolesToSort);
		
		Statement[] stmts = new Statement[2];
	    Expression baseArg = gen.singleNameReference(baseVarName());
	    if (staticRoleType.baseclass() instanceof WeakenedTypeBinding)
	    	baseArg = gen.castExpression(
	    						baseArg,
	    						gen.typeReference(((WeakenedTypeBinding)staticRoleType.baseclass()).getStrongType()),
	    						CastExpression.RAW);
	    char[] LOCAL_BASE_NAME = "_OT$local$base".toCharArray(); //$NON-NLS-1$
		stmts[0] = gen.localVariable(LOCAL_BASE_NAME, gen.baseclassReference(staticBaseType), baseArg);
		
		IfStatement prevIf = null;

	    /* Normally:
	     *   if (_OT$local$base instanceof MySubBaseA)
	     *       <action for MySubRoleA playedBy MySubBaseA>
	     *   else if (_OT$local$base instanceof MySubBaseB)
	     *       <action for MySubRoleB playedBy MySubBaseB>
	     *   ...
	     * However, if binding ambiguities exist, we must use exact type comparison,
	     * because more specific bases may have to be reported by a LiftingFailedException:
	     *   if (_OT$local$base.getClass() == MySubBaseA.class)
	     *       <action for MySubRoleA playedBy MySubBaseA>
	     *   else if (_OT$local$base.getClass() == MySubBaseB.class)
	     *       <action for MySubRoleB playedBy MySubBaseB>
	     *   ...
	     */
	    for (int idx = caseObjects.length-1; idx >= 0; idx--) {
	        RoleModel object = caseObjects[idx];

	        Statement s = createCaseStatement(object, gen);
	        if (object.getBaseTypeBinding().equals(staticBaseType) && idx == 0 && !hasBindingAmbiguity) {
	        	// shortcut if last type matches the static type: no need for a final instanceof check
	        	if (prevIf == null)
	        		stmts[1] = s;
	        	else
	        		prevIf.elseStatement = s;
	        	return gen.block(stmts); // don't generate default.
	        } else {
		        Expression condition = object._hasBindingAmbiguity
		         ?	gen.equalExpression(gen.messageSend(gen.singleNameReference(LOCAL_BASE_NAME), "getClass".toCharArray(), null), //$NON-NLS-1$
		        		 				gen.classLiteralAccess(gen.baseclassReference(object.getBaseTypeBinding())),
		        		 				OperatorIds.EQUAL_EQUAL)
		         :  gen.instanceOfExpression(gen.singleNameReference(LOCAL_BASE_NAME),
						    				 gen.baseclassReference(object.getBaseTypeBinding()));
		        // more checks for known sub-classes of base:
		        Set<ReferenceBinding> otherBases = object.getTeamModel().getSubBases(object, caseObjects);
		        for (Iterator<ReferenceBinding> bases = otherBases.iterator(); bases.hasNext();) {
		        	ReferenceBinding baseBinding = bases.next();
		        	condition = new OR_OR_Expression(condition, 
		        									 gen.instanceOfExpression(gen.singleNameReference(LOCAL_BASE_NAME), 
		        											 				  gen.baseclassReference(baseBinding)), 
		        									 OperatorIds.OR_OR);
		        }
		        if (s != null) {
					IfStatement is = gen.ifStatement(condition, s);
			        if (prevIf == null)
			        	stmts[1] = is;				// this is the root "if"
			        else
			        	prevIf.elseStatement = is;	// hook into existing "if"
			        prevIf = is;
		        }
	        }
	    }
	    /*
	     * ...
	     * else
	     * 	<default action>
	     */
	    prevIf.elseStatement = createDefaultStatement(staticRoleType, gen);

	    return gen.block(stmts);
	}

	/** What name should be used to address the base object? */
	char[] baseVarName() {
		return BASE; // default: "base" (predicate method argument)
	}
}
