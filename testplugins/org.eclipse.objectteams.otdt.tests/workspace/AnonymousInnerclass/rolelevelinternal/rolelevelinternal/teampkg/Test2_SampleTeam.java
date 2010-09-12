import rolelevelinternal.ordinarypkg.OrdinaryClass;

package rolelevelinternal.teampkg;

/**
 * $Id: Test2_SampleTeam.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a role class (defined insight the file of its team class) with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is an ordinary class
 */
public team class Test2_SampleTeam
{
    public class SampleRole
    {
          public void rolelevelMethod()
          {
            OrdinaryClass localVar = new OrdinaryClass()
                {
                     public void additionalMethod() {}
                };
          }        
    }
}
