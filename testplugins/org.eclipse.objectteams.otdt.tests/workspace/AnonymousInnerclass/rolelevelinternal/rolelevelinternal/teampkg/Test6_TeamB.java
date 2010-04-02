package rolelevelinternal.teampkg;

/**
 * $Id: Test6_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * instantiation of an anonymous class inside the method
 * the anonymous class is a team class with role class
 */
public team class Test6_TeamB
{
    public class SampleRole
    {
	    public void rolelevelMethod()
	    {
	        Test56_TeamA localVar = new Test56_TeamA()
	                {
	                     public class RoleClass {}
	                };
	    }
    }
}
