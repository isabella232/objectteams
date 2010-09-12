/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.GuardPredicateDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.MethodSpec;
import org.eclipse.objectteams.otdt.internal.core.compiler.util.AstGenerator;

/**
 * MIGRATION_STATE: new, not yet: paramter mappings
 *
 * NEW for OTDT.
 * started from COPY&PASTE from RecoveredMethod
 *
 * Internal method mapping structure for parsing recovery
 */
public class RecoveredMethodMapping extends RecoveredElement implements TerminalTokens {

	public AbstractMethodMappingDeclaration methodMappingDeclaration;

	// yet unused structure of base method specs by their method declaration:
	public RecoveredMethod[] baseMethods;
	int baseMethodCount = 0;

public RecoveredMethodMapping(AbstractMethodMappingDeclaration methodMapping, RecoveredElement parent, int bracketBalance, Parser parser){
	super(parent, bracketBalance, parser);
	this.methodMappingDeclaration = methodMapping;
	this.foundOpeningBrace = false; // allways created before '{' is seen.
}
/*
 * Record a nested block declaration
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue) {

	// TODO(SH): should param mapping be treated as block?

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element
	*/
	if (this.methodMappingDeclaration.declarationSourceEnd > 0
		&& nestedBlockDeclaration.sourceStart
			> this.methodMappingDeclaration.declarationSourceEnd){
				if (this.parent == null){
					return this; // ignore
				} else {
					return this.parent.add(nestedBlockDeclaration, bracketBalanceValue);
				}
	}
	/* consider that if the opening brace was not found, it is there */
	if (!this.foundOpeningBrace){
		this.foundOpeningBrace = true;
		this.bracketBalance++;
	}

	return this;
}
@Override
public RecoveredElement add(Statement statement, int bracketBalanceValue)
{
	AstGenerator gen= new AstGenerator(statement.sourceStart, statement.sourceEnd);

	// adding a statement to a method mapping is interpreted as adding a guard predicate:
	GuardPredicateDeclaration predicate= new GuardPredicateDeclaration(
						this.methodMappingDeclaration.compilationResult,
						IOTConstants.PREDICATE_METHOD_NAME,
						/*base*/false,
						statement.sourceStart-5, // guess position of "when"
						statement.sourceStart-1);// -- " --
	predicate.returnType= gen.typeReference(TypeBinding.BOOLEAN);

	// create a suitable return statement:
	if (statement instanceof FieldDeclaration)
		predicate.statements = new Statement[] {((FieldDeclaration)statement).initialization};
	else if (statement instanceof Expression)
		predicate.updatePredicateExpression((Expression)statement, statement.sourceEnd+1);

	// add as method to enclosing type:
	this.parent.add(predicate, bracketBalanceValue);
	return this;
}
/**
 * This method records a base method spec as part of the method mapping.
 * (Note that the inherited method must be overridden and 'this' must be returned instead of 'parent').
 */
public RecoveredElement add(AbstractMethodDeclaration methodDeclaration, int bracketBalanceValue)
{
	// check whether methodDeclaration is the start of a new member rather than an encoding for a base method spec:
	Scanner scanner = parser().scanner;
	int saveCurrent= scanner.currentPosition;
	int saveStart=   scanner.startPosition;
	try {
		int nextToken = scanner.getNextToken();
		switch (nextToken) {
		case TerminalTokens.TokenNameLBRACE:   // it's a real method
		case TerminalTokens.TokenNameBINDIN:   // it's the start of a new callin mapping
		case TerminalTokens.TokenNameBINDOUT:  // it's the start of a new callout mapping
			// add real method or mapping to the class:
			return super.add(methodDeclaration, bracketBalanceValue);
		}
	} catch (InvalidInputException e) {
		// ignore
	} finally {
		// restore
		scanner.currentPosition= saveCurrent;
		scanner.startPosition=   saveStart;
	}

	if (!(methodDeclaration instanceof MethodDeclaration)) // see https://svn.objectteams.org/trac/ot/ticket/275
		return this.parent;

	// so it indeed seems to be a method spec (encoded as a method decl):
	if (this.baseMethods == null) {
		this.baseMethods = new RecoveredMethod[5];
		this.baseMethodCount = 0;
	} else {
		if (this.baseMethodCount == this.baseMethods.length) {
			System.arraycopy(
				this.baseMethods,
				0,
				(this.baseMethods = new RecoveredMethod[2 * this.baseMethodCount]),
				0,
				this.baseMethodCount);
		}
	}
	RecoveredMethod element = new RecoveredMethod(methodDeclaration, this, bracketBalanceValue, this.recoveringParser);
	this.baseMethods[this.baseMethodCount++] = element;
	/* if methodspec not finished, then methodspec becomes current */
	if (methodDeclaration.declarationSourceEnd == 0)
		return element;
	return this;
}
@Override
public RecoveredElement add(AbstractMethodMappingDeclaration methodMapping, int bracketBalanceValue)
{
	if (this.foundOpeningBrace)
		// after "{" nested mapping is most likely to be a misread param mapping. Discard.
		return this;
	return super.add(methodMapping, bracketBalanceValue);
}
@Override
public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalanceValue) {
	// check whether fieldDeclaration is a misread method spec rather than a real field
	switch (parser().currentToken) {
	case TerminalTokens.TokenNameBINDIN:   // it's the start of a new callin mapping
	case TerminalTokens.TokenNameBINDOUT:  // it's the start of a new callout mapping
		// ignore misread field:
		return this;
	}

	return super.add(fieldDeclaration, bracketBalanceValue);
}
/*
 * Answer the associated parsed structure
 */
public ASTNode parseTree(){
	return this.methodMappingDeclaration;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.methodMappingDeclaration.declarationSourceEnd;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered method mapping:\n"); //$NON-NLS-1$
	this.methodMappingDeclaration.print(tab + 1, result);
	for (int i = 0; i < this.baseMethodCount; i++)
		result.append(this.baseMethods[i].toString(tab + 1));
	return result.toString();
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void updateBodyStart(int bodyStart){
	this.foundOpeningBrace = true;
	this.methodMappingDeclaration.bodyStart = bodyStart;
}

public AbstractMethodMappingDeclaration updatedMethodMappingDeclaration(int bodyEnd)
{
	// update/transfer base methods
	MethodSpec[] baseMethodSpecs = this.methodMappingDeclaration.getBaseMethodSpecs();
	int existingCount= baseMethodSpecs==null ? null : baseMethodSpecs.length;
	if (this.baseMethodCount > existingCount) {
		if (this.methodMappingDeclaration.isCallout()) {
			CalloutMappingDeclaration callout= (CalloutMappingDeclaration)this.methodMappingDeclaration;
			callout.baseMethodSpec= new MethodSpec((MethodDeclaration) this.baseMethods[0].methodDeclaration);
		} else {
			CallinMappingDeclaration callinMapping= (CallinMappingDeclaration)this.methodMappingDeclaration;
			callinMapping.baseMethodSpecs= new MethodSpec[this.baseMethodCount];
			for (int i=0; i<this.baseMethodCount; i++)
				callinMapping.baseMethodSpecs[i]=
					new MethodSpec((MethodDeclaration)this.baseMethods[i].methodDeclaration);
		}
	}

// TODO(SH): recovery of parameter mapping details.
	if (   this.methodMappingDeclaration.declarationSourceEnd <= this.methodMappingDeclaration.declarationSourceStart
		&& bodyEnd > this.methodMappingDeclaration.declarationSourceStart)

		this.methodMappingDeclaration.declarationSourceEnd= bodyEnd;

	// update unfinished binding-level predicate:
	if (this.methodMappingDeclaration.isCallin()) {
		CallinMappingDeclaration callinDecl = (CallinMappingDeclaration) this.methodMappingDeclaration;
		if (   callinDecl.predicate != null
			&& callinDecl.predicate.bodyStart == 0)
		{
			Parser parser = parser();
			if (parser.expressionPtr > -1)
				parser.consumePredicateExpression();
			else
				callinDecl.predicate.tagAsHavingErrors();

		}
	}
	return this.methodMappingDeclaration;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd){

	/* in case the opening brace is close enough to the signature */
	if (this.bracketBalance == 0){
		/*
			if (parser.scanner.searchLineNumber(methodDeclaration.sourceEnd)
				!= parser.scanner.searchLineNumber(braceEnd)){
		 */
		switch(parser().lastIgnoredToken){
			case -1 :
				break;
			default:
				this.foundOpeningBrace = true;
				this.bracketBalance = 1; // pretend the brace was already there
		}
	}
	return super.updateOnOpeningBrace(braceStart, braceEnd);
}
public void updateParseTree(){
	updatedMethodMappingDeclaration(-1);
}
@Override
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd) {
	Parser parser = parser();
	if (this.baseMethodCount == 0 && parser.identifierPtr > -1) {
		// recover missing base method spec:
		MethodSpec baseSpec = null;
		if (   this.methodMappingDeclaration.isCallin()
			|| (((CalloutMappingDeclaration)this.methodMappingDeclaration).baseMethodSpec == null)) 
		{
			if (this.methodMappingDeclaration.hasSignature) {
				if (parser.identifierPtr > 0) {
					// Type selector
					baseSpec = new MethodSpec(parser.identifierStack[parser.identifierPtr], parser.identifierPositionStack[parser.identifierPtr--]);
					parser.identifierLengthPtr--;
				} else {
					// Type <missing>
					baseSpec = new MethodSpec("missing".toCharArray(), parser.identifierPositionStack[parser.identifierPtr]); //$NON-NLS-1$
				}
				baseSpec.hasSignature = true;
				boolean identifierAvailable =    (parser.identifierLengthPtr > -1 && parser.identifierLengthStack[parser.identifierLengthPtr] < 0)
										      || (parser.genericsIdentifiersLengthPtr > -1 && parser.genericsIdentifiersLengthStack[parser.genericsIdentifiersLengthPtr] > 0);
				if (identifierAvailable) {
					baseSpec.returnType = parser.getTypeReference(0);
					baseSpec.declarationSourceStart = baseSpec.returnType.sourceStart;
				}
			} else {
				// selector
				baseSpec = new MethodSpec(parser.identifierStack[parser.identifierPtr], parser.identifierPositionStack[parser.identifierPtr--]);
				parser.identifierLengthPtr--;
			}
			// don't overwrite existing base method spec:
			this.methodMappingDeclaration.checkAddBasemethodSpec(baseSpec);
		}
	}
	if (!this.foundOpeningBrace) {
		this.updateSourceEndIfNecessary(braceStart - 1, braceStart - 1);
		return this.parent.updateOnClosingBrace(braceStart, braceEnd);
	}
	return super.updateOnClosingBrace(braceStart, braceEnd);
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int braceStart, int braceEnd){
	if (this.methodMappingDeclaration.declarationSourceEnd == 0) {
		if(parser().rBraceSuccessorStart >= braceEnd) {
			this.methodMappingDeclaration.declarationSourceEnd = parser().rBraceEnd;
			this.methodMappingDeclaration.bodyEnd = parser().rBraceStart;
		} else {
			this.methodMappingDeclaration.declarationSourceEnd = braceEnd;
			this.methodMappingDeclaration.bodyEnd  = braceStart - 1;
		}
	}
	MethodSpec[] baseMethods = this.methodMappingDeclaration.getBaseMethodSpecs();
	if (baseMethods != null && baseMethods.length == 1) {
		MethodSpec baseMethod = baseMethods[0];
		if (baseMethod.sourceStart >= braceStart) {
			// next declaration claims what we erroneously read as a base method spec
			int start = baseMethod.sourceStart-1;
			MethodSpec newSpec = parser().newMethodSpec(new char[0], (((long)start)<<32) +start);
			if (this.methodMappingDeclaration.isCallin())
				((CallinMappingDeclaration)this.methodMappingDeclaration).baseMethodSpecs = new MethodSpec[] {newSpec};
			else
				((CalloutMappingDeclaration)this.methodMappingDeclaration).baseMethodSpec = newSpec;
		}
	}
}
public void setCallinModifier(int tokenID, int start, int end) {
	if (this.methodMappingDeclaration instanceof CallinMappingDeclaration) {
		CallinMappingDeclaration decl = ((CallinMappingDeclaration)this.methodMappingDeclaration);
		decl.callinModifier= tokenID;
		decl.modifierStart= start;
		decl.modifierEnd= end;
	}
}
}
