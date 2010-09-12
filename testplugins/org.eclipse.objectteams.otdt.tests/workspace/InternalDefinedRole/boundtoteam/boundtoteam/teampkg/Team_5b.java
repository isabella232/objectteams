package boundtoteam.teampkg;

/**
 * $Id: Team_5b.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase
 * a bound role class with a method and a method mapping
 * the method is abstract and has no parameters
 * the method mapping is a callout mapping (=>)
 *  the base class of the role is a team
 */
public team class Team_5b
{
	public class SampleRole playedBy TeamC
	{
	    public void roleMethod() {}
	    roleMethod => baseMethod;    
	}
}
