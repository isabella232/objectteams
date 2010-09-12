package teamlevel.teampkg;

import teamlevel.ordinarypkg.OrdinaryClass;

/**
 * $Id: Test2_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 *
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is an ordinary class
 */
public team class Test2_SampleTeam
{
    public void teamlevelMethod()
    {
        OrdinaryClass localVar = new OrdinaryClass()
        	{
            	public void additionalMethod() {}
        	};
    }
} 