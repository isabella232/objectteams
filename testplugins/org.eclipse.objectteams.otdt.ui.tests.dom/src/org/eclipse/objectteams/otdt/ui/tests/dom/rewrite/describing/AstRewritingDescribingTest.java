/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.dom.rewrite.describing;

import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;



public class AstRewritingDescribingTest 
	extends org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest 
{

	public AstRewritingDescribingTest(String name) {
		super(name);
	}
	
	public static RoleTypeDeclaration findRoleTypeDeclaration (TypeDeclaration teamType, String roleName) 
	{
		for (Object member : teamType.bodyDeclarations()) {
			if (member instanceof RoleTypeDeclaration) { 
				RoleTypeDeclaration roleType = (RoleTypeDeclaration)member;
				if (roleType.getName().getIdentifier().equals(roleName))
					return (RoleTypeDeclaration)member;
			}
		}
		return null;
	}
	
	public static CallinMappingDeclaration findCallinMappingDeclaration(RoleTypeDeclaration typeDecl, String roleMethodName) 
	{
		CallinMappingDeclaration[] callinCallouts = typeDecl.getCallIns();
		for (int i= 0; i < callinCallouts.length; i++) {
			if (roleMethodName.equals(callinCallouts[i].getRoleMappingElement().getName().getIdentifier())) {
				return callinCallouts[i];
			}
		}
		return null;
	}	

}
