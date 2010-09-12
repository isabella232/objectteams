import rolelevelinternal.basepkg.SampleBase;

package rolelevelinternal.teampkg;

/**
 * $Id: Test4_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 *  a role class (defined insight the file of its team class) with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is a role class with a method mapping 
 */
public team class Test4_SampleTeam
{
    public class Role1 playedBy SampleBase
    {
        public void roleMethod() {}
    }

    public class Role2
    {
        public void rolelevelMethod()
        {
        	Role1 localVar = new Role1()
        		{
            		roleMethod <- after baseMethod;
        		};
        }
    }
}
