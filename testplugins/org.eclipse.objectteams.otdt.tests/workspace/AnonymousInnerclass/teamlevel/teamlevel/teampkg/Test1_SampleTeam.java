package teamlevel.teampkg;

import teamlevel.ordinarypkg.OrdinaryClass;

/**
 * $Id: Test1_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is an ordinary class with a method
 */
public team class Test1_SampleTeam
{
    OrdinaryClass teamlevelAttr = new OrdinaryClass()
    	{
        	public void additionalMethod(){}
		};
}
