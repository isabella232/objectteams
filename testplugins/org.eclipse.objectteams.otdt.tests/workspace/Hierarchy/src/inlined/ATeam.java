package inlined;

import standard.AClass;
import standard.AClassExplicit;
import standard.AnInterface1;

/**
 * @author haebor
 * @version $Id: ATeam.java 18657 2008-06-15 00:04:43Z stephan $
 */
public team class ATeam extends ASuperTeam
{
    public class ARole {}
    
    public class ARoleImplicit {}
    
    public class ARoleImplicitImplicit {}
    
    public class ARoleExplicit extends AClass {}
    
    public class ARoleExplicitExplicit extends AClassExplicit {}
    
    public class ARoleImplicitExplicit {}
    
    public class ARoleImplicitExplicitExplicit {}

    public class ARoleImplicitImplicitExplicit {}

    public class ARoleImplicitImplicitExplicitExplicit {}

    public class ARoleImplementing1 implements AnInterface1 {}
    
    public class ARoleImplicitImplementing1 {}

    public interface ARoleInterface {}
    public interface ARoleInterfaceImplicit {}
    public interface ARoleInterfaceImplicitImplicit {}
}
