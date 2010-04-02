package inlined.dual;

import standard.AClass;
import standard.AClassExplicitExplicit;


/**
 * @author haebor
 * 
 * Examples where a role inherits from a role and a class at the same point
 * (the none dual examples inherit either explicit or implicit at one point)
 *  
 * @version $Id: ATeam.java 18657 2008-06-15 00:04:43Z stephan $
 */
public team class ATeam extends ASuperTeam
{
    public class ARoleImplicit_Explicit extends AClass {}
    
    public class ARoleImplicitImplicit_ImplicitExplicit {}
    
    public class ARoleImplicitExplicit_ExplicitExplicitExplicit 
    	extends AClassExplicitExplicit {}

    public class ARoleImplicitImplicitExplicit_ExplicitExplicitExplicit 
    	extends AClassExplicitExplicit {}
}
