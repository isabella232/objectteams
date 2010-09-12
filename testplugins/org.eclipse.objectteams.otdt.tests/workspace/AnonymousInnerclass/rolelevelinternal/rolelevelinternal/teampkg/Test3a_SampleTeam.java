import rolelevelinternal.basepkg.SampleBase;

package rolelevelinternal.teampkg;

/**
 * $Id: Test3a_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 *  a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is a role class with an after-callin method mapping 
 */
public team class Test3a_SampleTeam
{
    public class Role1 playedBy SampleBase \n"
    {
	    public void roleMethod() {}
	}
    
    public class Role2
    {
        Role1 rolelevelAttr = new Role1()
            {
                roleMethod <- after baseMethod;
            };
    }
}
