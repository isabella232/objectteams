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
 * $Id: WithinStatement.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * NEW for OTDT.
 *
 * Represent new block statement
 * 		within (teamExpr) { action; }
 * This is translated to (pos is source code position to guarantee fresh names):
 * <pre>
 * 		Team __OT__team$<pos> = <teamExpr>;                   // evaluate <teamExpr> only once!
 *      int _OT$save<pos> = __OT__team$<pos>._OT$saveActivationState();
 *      __OT__team$<pos>.activate();
 *      try {
 * 			<action>
 *      } finally {
*      	    __OT__team$<pos>.restoreActivationState(_OT$save<pos>);
 *      }
 * </pre>
 *
 * @author Markus Witte
 * @version $Id: WithinStatement.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class WithinStatement extends Block implements IOTConstants
{
	/** 
	 * This class marks the single name reference refering to the synthetic local variable.
	 * As such it is actually a substitute for the team expression.
	 * Used for error reporting (null checks). 
	 */
	public class SubstitutedReference extends SingleNameReference 
	{		
		public SubstitutedReference(char[] teamVarName, long pos) {
			super(teamVarName, pos);
		}
		public Expression getExpression() {
			return WithinStatement.this.teamVarDecl.initialization;
		}
	}

	// variable names:
	private static final String SAVE_VAR_PREFIX = OT_DOLLAR+"save"; //$NON-NLS-1$
	private static final String TEAM_VAR_PREFIX = OT_DOLLAR + "team$"; //$NON-NLS-1$

	// activation:
	public static final char[] ACTIVATE_NAME   = "activate".toCharArray();  //$NON-NLS-1$
    public static final char[] DEACTIVATE_NAME = "deactivate".toCharArray(); //$NON-NLS-1$
	public static final char[] SAVE_STATE_NAME = (OT_DOLLAR+"saveActivationState").toCharArray();  //$NON-NLS-1$
	public static final char[] RESTORE_ACTIVATION_NAME = (OT_DOLLAR+"restoreActivationState").toCharArray(); //$NON-NLS-1$

	private char[]           teamVarName;
    private LocalDeclaration teamVarDecl; // statements[0]
    private Statement        body;        // ((TryStatement)statements[3]).tryBlock


    public WithinStatement(
            Expression teamExpr, Statement action, int s, int e)
    {
        super(2); // two declarations: the team variable and the level variable
        this.sourceStart  = s;
        this.sourceEnd    = e;
        AstGenerator gen = new AstGenerator(s, e);

        // GEN: org.objectteams.Team __OT__team$<pos> = <teamExpr>;
        // use source position to generate unique name:
        this.teamVarName         = (WithinStatement.TEAM_VAR_PREFIX + s).toCharArray();
        char[] saveVarName = (SAVE_VAR_PREFIX + s).toCharArray();

        this.teamVarDecl = gen.localVariable(
        		this.teamVarName,
        		gen.qualifiedTypeReference(ORG_OBJECTTEAMS_ITEAM),
				teamExpr);

        // GEN: int _OT$save<pos> = __OT__team$<pos>._OT$saveActivationState();
        LocalDeclaration saveVarDecl = gen.localVariable(
                saveVarName,
                gen.singleTypeReference(TypeBinding.INT),
                teamMethodInvocation(SAVE_STATE_NAME, gen, null));
        // GEN: __OT__team$<pos>.activate();
        MessageSend activateSend =
				teamMethodInvocation(ACTIVATE_NAME, gen, null);

        // ensure action is a Block
        this.body         = action;
        Block actionBlock;
        if (action instanceof Block) {
            actionBlock = (Block)action;
        } else {
            actionBlock = new Block(0); // no declarations
            actionBlock.statements = new Statement[]{action};
        }

        /* GEN resetter block:
         * {
         *      __OT__team$<pos>_OT$restoreActivationState(_OT$save<pos>);
         * }
         */
        Block deactivateBlock = new Block(0);
        deactivateBlock.sourceStart = s;
        deactivateBlock.sourceEnd = e;
        deactivateBlock.statements = new Statement[]{
        		teamMethodInvocation(RESTORE_ACTIVATION_NAME, gen, gen.singleNameReference(saveVarName))
        };

        // assemble action and deactivation into a try-finally:
        TryStatement tryStatement = new TryStatement();
        tryStatement.sourceStart  = s;
        tryStatement.sourceEnd    = e;
        tryStatement.tryBlock     = actionBlock; // the actual source body
        tryStatement.finallyBlock = deactivateBlock;

        // final assembly:
        this.statements = new Statement[] {
            this.teamVarDecl, // Team __OT__team$<pos> = <teamExpr>;
            saveVarDecl, // _OT$save<pos> = __OT__team$<pos>._OT$saveActivationState();
			activateSend,// __OT__team$<pos>.activate();
            tryStatement // try { <action> } finally { <reset> }
        };
    }

    /** Generate:
     *
     * __OT__team$<pos>.<methodName>();
     *
     * @param arg if non-null, pass this as the only argument to <methodName>(arg).
     */
    private MessageSend teamMethodInvocation(
    		char[] methodName, AstGenerator gen, Expression arg)
    {
    	return gen.messageSend(
                new SubstitutedReference(this.teamVarName, gen.pos),
				methodName,
				(arg == null) ?
						new Expression[0] :
						new Expression[] { arg });
    }

    // modified version of Block.resolve(BlockScope)
    public void resolve(BlockScope upperScope)
    {
        this.scope = new BlockScope(upperScope, this.explicitDeclarations);
        // special treatment of type errors for teamExpr:
        Expression teamExpr = this.teamVarDecl.initialization;
        teamExpr.resolve(this.scope);
        TypeBinding tb = teamExpr.resolvedType;

        if ((tb instanceof ReferenceBinding)
                && ((ReferenceBinding)tb).isTeam())
        {
        	this.teamVarDecl.initialization = null;     // don't resolve again...
            this.teamVarDecl.resolve(this.scope);  // no type error possible any more.
            this.teamVarDecl.initialization = teamExpr; // ... restore.
        } else {
            this.scope.problemReporter().withinStatementNeedsTeamInstance(teamExpr);
            // don't procede because we don't want to see errors related
            // to the generated variable...
            return;
		}
        for (int i = 1; i < this.statements.length; i++) // skip [0]: it's the teamVarDecl.
            this.statements[i].resolve(this.scope);
    }

    /**
     * Gets the team expression via the initialization field
     * of the local team declaration.
     * @return Team Expression
     */
    public Expression getTeamExpression()
    {
        return this.teamVarDecl.initialization;
    }

	/**
	 * Extract the action block.
 	 */
    public Block getAction() {
    	Statement tryAction = this.statements[3];
    	if (tryAction instanceof TryStatement)
    		return ((TryStatement)tryAction).tryBlock;
    	return null;
    }

	public void traverse(ASTVisitor visitor, BlockScope blockScope) 
	{
		if (visitor.visit(this, blockScope)) {
			this.getTeamExpression().traverse(visitor, blockScope);
			Statement action = getAction();
			if (action != null)
				action.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}

	public StringBuffer print(int indent, StringBuffer output)
	{
		printIndent(indent, output);
		output.append("within ("); //$NON-NLS-1$
        this.teamVarDecl.initialization.printExpression(0,output).append(")\n"); //$NON-NLS-1$
        this.body.print(indent+1,output).append(";"); //$NON-NLS-1$
        return output;
	}
}