/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 *
 * Copyright 2004-2009 Berlin Institute of Technology, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 *
 * Please visit http://www.objectteams.org for updates and contact.
 *
 * Contributors:
 * Berlin Institute of Technology - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otre.util;

import java.util.HashSet;

import de.fub.bytecode.generic.ObjectType;

/**
 * @version $Id: BoundClass.java,v 1.8 2006-12-19 21:31:30 stephan Exp $ 
 * @author Christine Hundt
 */
public class BoundClass {
	private String name;
	
	private BoundClass _super;
	//private BoundClass _tsuper;
	private HashSet<String> adaptingTeams = new HashSet<String>(); 

	public BoundClass(String className, String teamName) {
		name = className;
		this.adaptingTeams.add(teamName);
		_super = null;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAdaptedByTeam(String teamName) {
		return this.adaptingTeams.contains(teamName);
	}
	
	public void addAdaptingTeam(String teamName) {
		this.adaptingTeams.add(teamName);
	}

	public void setSuper(BoundClass superClass) {
		_super = superClass;
	}
	
	public BoundClass getSuper() {
		return _super;
	}
	
	public boolean isSubClassOf(String anotherClass) {
		BoundClass superClass = _super;
		while (superClass!=null) {
			if (superClass.getName().equals(anotherClass)) {
				return true;
			}
			superClass = superClass.getSuper();
		}
		return false;
	}

	public void updateSuper(BoundClass newSuperBaseClass, ObjectType newSuperBaseType) {
		// FIXME(SH): implement ;-)		
		// test if newSuperBaseType is above or below _super.
		// also check if tsupers (i.e., more than one super) must be treated as well.
	}
	
//	public String toString() {
//		return name;
//	}
}
