package boundtoordinary.teampkg;

/**
 * $Id: Team_4b.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * an internal defined bound role class with an innerclass
 * the innerclass is a team class
 * the base class of the role is an ordinary class
 */
public team class Team_4b
{
	public team class SampleRole playedBy boundtoordinary.basepkg.SampleBase
	{
	    public team class AnInnerTeamClass {}
	}
}
