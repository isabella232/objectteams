package basecall.teampkg;

import basecall.basepkg.MyClass;

public team class Team1 
{
	public class Role1 playedBy MyClass
    {
        callin void roleMethod0(int arg0, int arg1)
        {
            base.roleMethod0(arg0, arg1);
        }
        
        callin void roleMethod1()
        {
            base.roleMethod1();
        }
        
        callin int roleMethod2()
        {
            int res = base.roleMethod2();
            return res;
        }
        
        void roleMethod0(int arg0, int arg1) <- replace void baseMethod0(int arg0, int arg1)
            with
            {
                arg1 <- arg1,
                arg0 <- arg0
            };
        
        roleMethod1 <- replace baseMethod1;
            
        roleMethod2 <- replace baseMethod2;
    }
}
