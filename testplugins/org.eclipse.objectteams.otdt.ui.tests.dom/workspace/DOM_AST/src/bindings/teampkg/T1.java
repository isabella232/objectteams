package bindings.teampkg;

public team class T1 extends T0
{
	public class R1 playedBy bindings.basepkg.C1 
	{
        
        m1 -> m1;

        String m2(Integer x) -> String m2(Integer val)
            with
            {
                x -> val,
                result <- result
            }

        Integer m3(String y) -> Integer m3(String s)
            with
            {
                y -> s,
                result <- result
            }
    
        ci1: Integer m5(Integer val) <- after Integer m5(Integer val);
        
        
        public abstract void m1();
        
        public Integer m5(Integer val)
        {
            return val;
        }
        
    }
	
	public class R2 playedBy bindings.basepkg.C1 
	{
        
        ci2: Integer m2(Integer val) <- after Integer m5(Integer val);

		public Integer m2(Integer val)
		{
			return val*2;
		}        
		ci3: Integer m3(Integer val) <- after Integer m5(Integer val);

		public Integer m3(Integer val)
		{
			return val*3;
		}
		precedence ci3, ci2;
    }
	precedence R1.ci1, ci2;
}
