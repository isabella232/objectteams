package teamlevel.teampkg;

import teamlevel.basepkg.SampleBase;

/**
 * $Id: Test3_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a role class with a method mapping
 */
public team class Test3_SampleTeam
{
    	SampleRole teamlevelAttr = new SampleRole()
        {
             roleMethod <- after baseMethod;
        };

		public class SampleRole playedBy SampleBase
		{
		    public void roleMethod() {}
	    }
}
