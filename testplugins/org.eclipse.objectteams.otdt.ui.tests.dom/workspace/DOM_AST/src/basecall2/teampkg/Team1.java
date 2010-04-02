package basecall2.teampkg;

import basecall2.basepkg.MyClass;

public team class Team1 
{
	protected class Role1 playedBy MyClass
    {
        callin void roleMethod0(int arg0, int arg1)
        {
        		try {
					base.roleMethod0(arg0, arg1);
				} catch (RuntimeException e)
				{}
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
        
        roleMethod0 <- replace baseMethod0;
        
        roleMethod1 <- replace baseMethod1;
            
        roleMethod2 <- replace baseMethod2;
        
    }
}