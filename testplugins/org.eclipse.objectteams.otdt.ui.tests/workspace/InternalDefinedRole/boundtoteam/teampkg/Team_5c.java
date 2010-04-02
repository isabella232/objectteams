package teampkg;

/**
 * $Id: Team_5c.java 5749 2005-05-30 11:52:29Z anklam $
 * 
 * testcase:
 * a bound role class with a method and a method mapping
 * the method is concrete and has no parameters
 * the method mapping is a before-callin mapping
 *  the base class of the role is a team
 */
public team class Team_5c
{
	public class SampleRole playedBy TeamC
	{
	    public  void roleMethod() {}
	    roleMethod <- before baseMethod;    
	}
}
