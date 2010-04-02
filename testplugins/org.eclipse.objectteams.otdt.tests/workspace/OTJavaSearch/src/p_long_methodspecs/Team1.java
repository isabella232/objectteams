package p_long_methodspecs;

public team class Team1
{
	public class Role1 playedBy Base1
	{
		public abstract void roleMethodWithAbstractDecl();
		void roleMethodWithAbstractDecl() -> void baseMethod();
		
		void roleMethodWithoutAbstractDecl() -> void baseMethod();
		
		public void testMethod()
		{
			roleMethodWithAbstractDecl();
			roleMethodWithoutAbstractDecl();
		}
	}
	
	public class Role2 playedBy Base1
	{
	    public void role2Method()
	    {
	    }
	    
	    void role2Method() <- after void baseMethod();
	    
	        
	    public void testMethod()
		{
			role2Method();
		}
	}
}