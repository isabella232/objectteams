package boundtoordinary.teampkg;

/**
 * $Id: Team_5e.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a bound role class with a method and a method mapping
 * the method is concrete and has no parameters
 * the method mapping is an after-callin mapping
 * the base class of the role is an ordinary class
 */
public team class Team_5e
{
	public class SampleRole playedBy boundtoordinary.basepkg.SampleBase
	{
	    public void roleMethod() {}
	    roleMethod <- after baseMethod;    
	}
}
