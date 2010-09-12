package teamlevel.teampkg;

import teamlevel.basepkg.SampleBase;

/**
 * $Id: Test4_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is a role class with a method mapping
 */
public team class Test4_SampleTeam
{
    public void teamlevelMethod()
    {
        SampleRole localVar = new SampleRole()
            {
                 roleMethod <- after baseMethod;
            };
    }                    

	public class SampleRole playedBy SampleBase
	{
	    public void roleMethod() {}
    }
}  