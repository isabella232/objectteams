package teamlevel.teampkg;

import teamlevel.basepkg.SampleBase;

/**
 * $Id: Test8_TeamB.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is a team class with method with declared lifting
 */
public team class Test8_TeamB
{
    public void teamlevelMethod()
    {
        Test78_TeamA localVar = new Test78_TeamA()
            {
                 public void method(SampleBase as RoleClass paraObj) {}
            };
    }
}   