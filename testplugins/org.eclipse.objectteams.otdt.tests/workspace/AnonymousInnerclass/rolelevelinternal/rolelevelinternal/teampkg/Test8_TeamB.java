import rolelevelinternal.basepkg.SampleBase;

package rolelevelinternal.teampkg;

/**
 * $Id: Test8_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * instantiation of an anonymous class inside the method
 * the anonymous class is a team class with method with declared lifting
 */
public team class Test8_TeamB
{
    public class SampleRole
    {
	    public void rolelevelMethod()
	    {
	        Test78_TeamA localVar = new Test78_TeamA()
	            {
	                 public void method(SampleBase as RoleClass paraObj) {}
	            };
	    }
    }
}   