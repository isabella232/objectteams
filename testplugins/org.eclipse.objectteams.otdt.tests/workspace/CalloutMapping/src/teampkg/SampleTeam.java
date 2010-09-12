package teampkg;

/**
 * @author kaschja
 * @version $Id: SampleTeam.java 16307 2007-09-22 01:16:08Z stephan $
 */
public team class SampleTeam
{
	public class SampleRole playedBy basepkg.SampleBase
    {
        public void rm1() -> void baseMethod1();
        abstract void rm2();
        rm2 -> baseMethod2;
        protected Object rm3(int i) -> Object baseMethod3(int i, double j)
        	with { i -> i, 0.0 -> j, result <- result }
        String getVal() -> get String val;
    }
}
