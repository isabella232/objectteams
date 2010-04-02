package p_type_search;

public team class T2 extends T1
{
    public void teamMethod(B1 as R1 role)
    {
    }

    public void teamMethod(B3 as R6 role)
    {
    }
    
    /** 
     * Note, that B3 is a subclass of {@link B2}
     */
    public class R6 playedBy B3
    {
    	callin boolean role6Method(R6 other) { return true; }
    	boolean role6Method(R6 other) <- replace boolean compareTo(B3 other);
    }
    
	public abstract class R0 implements I1 playedBy B1
	{
	}
		
	public class R1 extends R0 playedBy B1
	{ 
	    public C1 roleMethod(B4 b4)
	    {
	        return null;
	    }
	    C1 roleMethod(B4 b4) <- before C1 baseMethod3(B4 b4);
	    C1 roleMethod2(B4 b4) -> C1 baseMethod3(B4 b4);
	}

	public team class T3 implements I1 playedBy B1
	{
	    public class R5 playedBy B2
	    {
	        private B5 b5;
	    }
	}
}