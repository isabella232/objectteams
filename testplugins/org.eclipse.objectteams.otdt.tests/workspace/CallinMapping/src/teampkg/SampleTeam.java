package teampkg;

/**
 * @author kaschja
 * @version $Id: SampleTeam.java 16307 2007-09-22 01:16:08Z stephan $
 */
public team class SampleTeam
{
	public class SampleRole playedBy basepkg.SampleBase
    {
        public void roleMethod1() {}
        public void roleMethod2() {}
        public void roleMethod3() {}
        public void roleMethod4() {}
        public void roleMethod5(int i) {}
        public <T extends Object> T roleMethod6() {}
    
	    roleMethod1 <- after baseMethod1;
	    roleMethodA <- before baseMethod2;
	    roleMethod2 <- replace baseMethodA;
	    roleMethodB <- after baseMethodB;

	    roleMethod3 <- before baseMethod3, baseMethod4, baseMethodC;

	    roleMethod4 <- replace baseMethod4;
	    
	    void roleMethod5(int ir)  <- after void baseMethod5(int ib) with { ir <- ib }
	    
	    <T extends Object> T roleMethod6() <- after Object+ baseMethod6();
    }
}
