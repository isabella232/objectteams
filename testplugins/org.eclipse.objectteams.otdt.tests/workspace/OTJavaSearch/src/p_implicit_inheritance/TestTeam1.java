package p_implicit_inheritance;

public abstract team class TestTeam1
{
	public class TestSuperRole {
		void zork () {}
		
	}
    public abstract class TestRole1 extends TestSuperRole
    {
        public void roleMethod()
        {
        	privateRoleMethod();
        }
        
        private void privateRoleMethod()
        {
        	zork();
        }
        
        void gulp() {}
        public void bar()  { 
        	gulp();
        }        
        abstract void zork();
    }
    public class TestRole2 { 
        void gulp() {}
        void good() {
        	gulp();
        }
    }
    void noMatcher(TestSuperRole tsr) {
    	tsr.zork();
    }
}
