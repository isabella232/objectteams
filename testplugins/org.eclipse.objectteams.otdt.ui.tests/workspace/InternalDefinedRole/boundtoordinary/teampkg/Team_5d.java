package teampkg;

/**
 * $Id: Team_5d.java 5749 2005-05-30 11:52:29Z anklam $
 * 
 * testcase:
 * a bound role class with a method and a method mapping
 * the method is concrete, has no parameters but a callin modifier
 * the method mapping is a replace-callin mapping
 * the base class of the role is an ordinary class
 */
public team class Team_5d
{
	public class SampleRole playedBy basepkg.SampleBase
	{
	    public callin void roleMethod() {}
	    roleMethod <- replace baseMethod;    
	}
}
