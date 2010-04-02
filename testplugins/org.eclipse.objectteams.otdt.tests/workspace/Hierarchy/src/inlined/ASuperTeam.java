package inlined;

import standard.AClass;
import standard.AClassExplicit;
import standard.AnInterface1;


/**
 * @author haebor
 * @version $Id: ASuperTeam.java 18658 2008-06-15 10:08:27Z stephan $
 */
public team class ASuperTeam extends ASuperSuperTeam 
{
    public class ARoleImplicit
    {
    }
    
    public class ARoleImplicitImplicit
    {
    }
    
    public class ARoleImplicitExplicit extends AClass
    {
    }
    
    public class ARoleImplicitExplicitExplicit extends AClassExplicit
    {
    }

    public class ARoleImplicitImplicitExplicit
    {
    }

    public class ARoleImplicitImplicitExplicitExplicit
    {
    }
    
    public class ARoleImplicitImplementing1 implements AnInterface1 
	{
	}

    public interface ARoleInterfaceImplicit {}
    public interface ARoleInterfaceImplicitImplicit {}
}
