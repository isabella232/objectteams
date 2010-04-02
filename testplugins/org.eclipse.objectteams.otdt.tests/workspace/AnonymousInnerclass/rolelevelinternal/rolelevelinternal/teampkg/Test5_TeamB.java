package rolelevelinternal.teampkg;

/**
 * $Id: Test5_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a team class with role class
 */
public team class Test5_TeamB
{
    public class SampleRole
    {
		Test56_TeamA rolelevelAttr = new Test56_TeamA()
		    {
		         public class RoleClass {}
		    };
    }
}
