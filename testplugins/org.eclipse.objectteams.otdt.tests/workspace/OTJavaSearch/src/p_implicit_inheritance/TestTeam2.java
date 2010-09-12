package p_implicit_inheritance;

public team class TestTeam2 extends TestTeam1
{
    public class TestRole1
    {
        public void roleMethod()
        {
        }
        
        public void roleMethod2()
        {
        }
        
        public void foo()
        {
        	privateRoleMethod();
        }
        
        void gulp() {}
        public void bar()  { // testing for bug 160301
        	gulp();
        }
        void zork() {}
    }
    public class TestRole2 {
    	@Override
        void gulp() {}
        public void bar()  { 
        	gulp();
        }            	
    }
}
