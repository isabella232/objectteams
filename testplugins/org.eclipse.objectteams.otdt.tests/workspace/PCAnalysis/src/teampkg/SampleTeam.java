package teampkg;

/**
 * @author kaschja
 * @version $Id: SampleTeam.java 7860 2005-11-23 16:00:20Z jwloka $
 */
public team class SampleTeam
{
	public class SampleRole playedBy basepkg.SampleBase
    {
        public void roleMethod1() {}
        public void roleMethod2() {}
        public void roleMethod3() {}
        public void roleMethod4() {}
    
	    roleMethod1 <- after baseMethod1;
	    roleMethodA <- before baseMethod2;
	    roleMethod2 <- replace baseMethodA;
	    roleMethodB <- after baseMethodB;

	    roleMethod3 <- before baseMethod3, baseMethod4, baseMethodC;

	    roleMethod4 <- replace baseMethod4;
    }
}
