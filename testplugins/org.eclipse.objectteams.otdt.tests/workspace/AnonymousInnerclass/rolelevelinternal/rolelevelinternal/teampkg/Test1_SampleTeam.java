import rolelevelinternal.ordinarypkg.OrdinaryClass;

package rolelevelinternal.teampkg;

/**
 * $Id: Test1_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with an attribut
 * attribut assignment with instantiation of an anonymous class
 * the anonymous class is an ordinary class with a method
 */
public team class Test1_SampleTeam
{
    public class SampleRole
    {
        OrdinaryClass rolelevelAttr = new OrdinaryClass()
             {
                 public void additionalMethod() {}
             };        
    }
}
