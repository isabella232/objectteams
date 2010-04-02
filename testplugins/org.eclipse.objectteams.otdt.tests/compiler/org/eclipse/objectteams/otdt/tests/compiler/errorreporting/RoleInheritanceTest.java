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
 * $Id: RoleInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.compiler.errorreporting;

import org.eclipse.objectteams.otdt.tests.compiler.TestBase;

/**
 * This class contains tests concerning role inheritance.
 * 
 * @author kaschja
 * @version $Id: RoleInheritanceTest.java 23494 2010-02-05 23:06:44Z stephan $
 */
public class RoleInheritanceTest extends TestBase
{
    public RoleInheritanceTest(String testName)
    {
        super(testName);
    }

    /** 
	 * A role extends explicitly a role of a foreign team (non-superteam).
	 * Both roles have the same name.
	 * Comment: 
	 * A role can only extend explicitly another role of the same team.
	 */
	public void testRoleExtendsRoleWithSameNameOfForeignTeam1() 
	{
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role {} " +
			  NL + "}");
		
		createFile("TeamB","public team class TeamB " +
			  NL + "{ " +	
			  NL + "	protected class Role extends TeamA.Role {} " +
			  NL + "}");
				
		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A role extends explicitly a role of a foreign team (non-superteam).
	 * Comment: 
	 * A role can only extend explicitly another role of the same team.
	 */
	public void testRoleExtendsRoleOfForeignTeam1() 
	{
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class RoleA {} " +
			  NL + "}");
	
		createFile("TeamB","public team class TeamB " +
			  NL + "{ " +	
			  NL + "	protected class RoleB extends TeamA.RoleA {} " +
			  NL + "}");
			
		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A role inherits implicitly and explicitly a role from the superteam.
	 * Comment:
	 * A role may not inherit a role explicitly if it is already inherited
	 * implicitly from the superteam (by name-matching).
	 */ 
	public void testRoleInheritsRoleImplicitlyAndExplicitlyFromSuperTeam1()
	{
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role {} " +
			  NL + "}");
		
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role extends TeamA.Role {} " +
			  NL + "}");
				
		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A role inherits from an external class.
	 */ 
	public void testRoleExtendsExternalClass1()
	{
		createFile("MyClass","public class MyClass " +
			  NL + "{ " +	
			  NL + "}");
		
		createFile("Team","public team class Team " +
			  NL + "{ " +	
			  NL + "	protected class Role extends MyClass {} " +
			  NL + "}");
				
		compileFile("Team");
		
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * A role inherits explicitly from an abstract role.
	 * It does not implement the abstract methods.
	 * Comment:
	 * A role has to implement the abstract methods if it extends 
	 * an abstract role.
	 */
	public void testRoleInheritsExplicitlyFromAbstractRole1()
	{
		createFile("MyTeam","public team class MyTeam { " +
			  NL + "	abstract class Role1 {" +
			  NL + "	    public abstract void role1Method();" +
			  NL + "    }" +
			  NL + "    class Role2 extends Role1 {} " +
			  NL + "}");

		compileFile("MyTeam");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A role inherits implicitly from an abstract role.
	 * It does not implement the abstract methods.
	 * Comment:
	 * A role has to implement the abstract methods of the implicitly 
	 * inherited abstract role.
	 */
	public void testRoleInheritsImplicitlyFromAbstractRole1()
	{
		createFile("TeamA","public team class TeamA { " +
		      NL + "	abstract class RoleA {" +
			  NL + "	    public abstract void roleMethod();" +
			  NL + "    }" +
			  NL + "}");
				
		createFile("TeamB","public team class TeamB extends TeamA " +
     		  NL + "{ " +	
		      NL + "	protected class RoleA {} " +
		      NL + "}");

		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * An implicitly inherited role inherits explicitly from an external class.
	 * The super-role does not extend any class.
	 */
	public void testImplicitInheritedRoleExtendsExternalClass1()
	{
		createFile("ExternalClass","public class ExternalClass " +
			  NL + "{ " +	
			  NL + "}");

		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role {} " +
			  NL + "}");
		
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role extends ExternalClass {} " +
			  NL + "}");
				
		compileFile("TeamB");
		
		assertTrue(isCompilationSuccessful());
	}
	
	/**
	 * An implicitly inherited role inherits explicitly from an external class.
	 * The super-role extends a different class than the sub-role.
	 * Comment:
	 * An implicitly inherited role has to extend the same external class
	 * as the super-role.
	 */
	public void testImplicitInheritedRoleExtendsExternalClass2()
	{
		createFile("ExternalClass","public class ExternalClass " +
			  NL + "{ " +	
			  NL + "}");

		createFile("DifferentClass","public class DifferentClass " +
			  NL + "{ " +	
			  NL + "}");
			  
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role extends DifferentClass {} " +
			  NL + "}");
		
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role extends ExternalClass {} " +
			  NL + "}");
				
		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}
	
	/**
	 * A sub-role has a restricted visibility compared to the super-role.
	 * Comment:
	 * A role may not restrain the visibility of the implicitly inherited role.
	 * (does the private modifier actually makes sense in a role context?)
	 */
	public void testRestrictedVisibilityOfSubRole1()
	{
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	public class Role {} " +
			  NL + "}");
		
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role {} " +
			  NL + "}");
				
		compileFile("TeamB");
		
		assertFalse(isCompilationSuccessful());
	}

	/**
	 * An overwriting method in a sub-role has a restricted visibility compared
	 * to the overwritten method in the super-role.
	 * Comment:
	 * A method in a role may not restrain the visibility of the
	 * implicitly inherited role-method.
	 */
	public void testRestrictedVisibilityOfMethodInSubRole1()
	{
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role {" +
			  NL + "        public void roleMethod() {};" +
			  NL + "    }" +
			  NL + "}");
			
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role {" +
			  NL + "        private void roleMethod() {};" +
			  NL + "    }" +
			  NL + "}");
					
		compileFile("TeamB");
			
		assertFalse(isCompilationSuccessful());
	}

	/**
	 * An implicitly inherited role does not implement all the interfaces
	 * of the super-role.
	 * Commment:
	 * An implicitly inherited role has to implement all the interfaces of the
	 * super-role.
	 */
	public void testImplementationOfInterfacesBySubRole1()
	{
		createFile("IState","public interface IState" +
			  NL + "{" +
			  NL + "    public IState getState();" +
		      NL + "}");

		createFile("ITransfer","public interface ITransfer" +
			  NL + "{" +
			  NL + "    public void doTransfer();" +
			  NL + "}");
		
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role implements IState, ITransfer " +
			  NL + "    {" +
			  NL + "        public IState getState() {}" +
			  NL + "        public void doTransfer() {}" +
			  NL + "    }" +
			  NL + "}");
			
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role implements IState " +
			  NL + "    {" +
			  NL + "        public IState getState() {}" +
			  NL + "    }" +
			  NL + "}");
					
		compileFile("TeamB");
			
		assertFalse(isCompilationSuccessful());
	}

	/**
	 * An implicitly inherited role implements more interfaces than
	 * the super-role.
	 */
	public void testImplementationOfInterfacesBySubRole2()
	{
		createFile("IState","public interface IState" +
			  NL + "{" +
			  NL + "    public IState getState();" +
			  NL + "}");

		createFile("ITransfer","public interface ITransfer" +
			  NL + "{" +
			  NL + "    public void doTransfer();" +
			  NL + "}");
		
		createFile("TeamA","public team class TeamA " +
			  NL + "{ " +	
			  NL + "	protected class Role implements IState" +
			  NL + "    {" +
			  NL + "        public IState getState() { return null; }" +
			  NL + "    }" +
			  NL + "}");
			
		createFile("TeamB","public team class TeamB extends TeamA" +
			  NL + "{ " +	
			  NL + "	protected class Role implements IState, ITransfer " +
			  NL + "    {" +
			  NL + "        public IState getState() { return tsuper.getState(); }" +
			  NL + "        public void doTransfer() {}" +
			  NL + "    }" +
			  NL + "}");
					
		compileFile("TeamB");
			
		assertTrue(isCompilationSuccessful());
	}
}
