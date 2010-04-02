package inlined.dual;

import standard.AClass;

/**
 * @author haebor
 * @version $Id: ASuperTeam.java 18658 2008-06-15 10:08:27Z stephan $
 */
public team class ASuperTeam extends ASuperSuperTeam 
{
    public class ARoleImplicit_Explicit {}
    
    public class ARoleImplicitImplicit_ImplicitExplicit extends AClass {}
    
    public class ARoleImplicitExplicit_ExplicitExplicitExplicit 
    	extends AClass {}

    public class ARoleImplicitImplicitExplicit_ExplicitExplicitExplicit {}
}
