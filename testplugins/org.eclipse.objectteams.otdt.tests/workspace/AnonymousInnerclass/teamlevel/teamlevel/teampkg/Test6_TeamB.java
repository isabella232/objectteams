package teamlevel.teampkg;

/**
 * $Id: Test6_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is a team class with role class
 */
public team class Test6_TeamB
{
    public void teamlevelMethod()
        {
            Test56_TeamA localVar = new Test56_TeamA()
                {
                     public class RoleClass {}
                };
        }
}
