package teamlevel.teampkg;

import teamlevel.basepkg.SampleBase;

/**
 * $Id: Test7_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a team class with method with declared lifting
 */
public team class Test7_TeamB
{
    Test78_TeamA teamlevelAttr = new Test78_TeamA()
        {
             public void method(SampleBase as RoleClass paraObj) {}
        };
}