package teamlevel.teampkg;

/**
 * $Id: Test5_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a team class with role class
 */
public team class Test5_TeamB
{
    Test56_TeamA teamlevelAttr = new Test56_TeamA()
        {
             public class RoleClass {}
        };
}
