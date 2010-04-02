/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: NameReference.java 19881 2009-04-13 23:35:46Z stephan $
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * OTDT changes:
 * What: support baseclass decapsulation.
 */
public abstract class NameReference extends Reference implements InvocationSite {

	public Binding binding; //may be aTypeBinding-aFieldBinding-aLocalVariableBinding

	public TypeBinding actualReceiverType;	// modified receiver type - actual one according to namelookup

//{ObjectTeams: for baseclass decapsulation (implement interface from Expression):
	public DecapsulationState baseclassDecapsulation = DecapsulationState.NONE;
	public void setBaseclassDecapsulation(DecapsulationState state) {
		this.baseclassDecapsulation = state;
	}
	@Override
	public DecapsulationState getBaseclassDecapsulation() {
		return this.baseclassDecapsulation;
	}
	@Override
	public void tagReportedBaseclassDecapsulation() {
		this.baseclassDecapsulation = DecapsulationState.REPORTED;
	}
	@Override
	protected boolean checkBaseclassDecapsulation(Scope scope) {
		if (   this.binding instanceof ProblemReferenceBinding
			&& this.binding.problemId() == ProblemReasons.NotVisible
			&& this.getBaseclassDecapsulation().isAllowed())
		{
			TypeBinding closestMatch = ((ProblemReferenceBinding)this.binding).closestMatch();
			if (closestMatch == null)
				return false;
			this.binding = closestMatch;
			if (this.binding.kind() == Binding.TYPE)
				this.resolvedType = (TypeBinding)this.binding;
			// no reporting: only the type reference after "playedBy" actually reports.
			return true;
		}
		return false;
	}
// SH}


	//the error printing
	//some name reference are build as name reference but
	//only used as type reference. When it happens, instead of
	//creating a new objet (aTypeReference) we just flag a boolean
	//This concesion is valuable while their are cases when the NameReference
	//will be a TypeReference (static message sends.....) and there is
	//no changeClass in java.
public NameReference() {
	this.bits |= Binding.TYPE | Binding.VARIABLE; // restrictiveFlag
}

public FieldBinding fieldBinding() {
	//this method should be sent ONLY after a check against isFieldReference()
	//check its use doing senders.........
	return (FieldBinding) this.binding ;
}

public boolean isSuperAccess() {
	return false;
}

public boolean isTypeAccess() {
	// null is acceptable when we are resolving the first part of a reference
	return this.binding == null || this.binding instanceof ReferenceBinding;
}

public boolean isTypeReference() {
	return this.binding instanceof ReferenceBinding;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	if (receiverType == null) return; // error scenario only
	this.actualReceiverType = receiverType;
}

public void setDepth(int depth) {
	this.bits &= ~DepthMASK; // flush previous depth if any
	if (depth > 0) {
		this.bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
	}
}

public void setFieldIndex(int index){
	// ignored
}

public abstract String unboundReferenceErrorName();

//{ObjectTeams: hook after this reference has been fully resolved
public void resolveFinished() { /* noop  */ }
// SH}
}
