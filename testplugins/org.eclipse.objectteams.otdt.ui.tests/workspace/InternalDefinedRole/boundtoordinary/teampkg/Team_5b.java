package teampkg;

/**
 * $Id: Team_5b.java 5749 2005-05-30 11:52:29Z anklam $
 * 
 * testcase
 * a bound role class with a method and a method mapping
 * the method is abstract and has no parameters
 * the method mapping is a callout mapping (=>)
 * the base class of the role is an ordinary class
 */
public team class Team_5b
{
	public class SampleRole playedBy basepkg.SampleBase
	{
	    public void roleMethod() {}
	    roleMethod => baseMethod;    
	}
}
