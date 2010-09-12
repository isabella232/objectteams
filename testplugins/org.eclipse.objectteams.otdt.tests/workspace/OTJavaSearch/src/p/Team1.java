package p;

public team class Team1
{
	
	public abstract class Role0 playedBy Base1
	{
		public abstract void roleMethod3();
	}
	
	public class Role1 extends Role0 playedBy Base1
	{
		public abstract void roleMethod();

		roleMethod -> baseMethod;
		
		roleMethod3 -> baseMethod; 
		
		public void testMethod()
		{
			roleMethod();
		}
	}
	
	public class Role2 playedBy Base1
	{
	    public void role2Method()
	    {
	    }
	    
	    role2Method <- after baseMethod;
	    
	    public void testMethod()
		{
			role2Method();
		}
	}
}
