package callins.teampkg;

import callins.basepkg.MyClass;

public team class Team1 
{
	public class Role1 playedBy MyClass
    {
        roleMethod0 <- before baseMethod0;
        roleMethod1 <- replace baseMethod1;
        callinName: roleMethod2 <- after baseMethod2;
        roleMethod3 <- before baseMethod3, baseMethod4, baseMethod5;
        int roleMethod4(Integer val) <- after int baseMethod7(int num) with
        {
            val <- new Integer(num)
        };
        <T extends MyClass> T roleMethod5() <- replace MyClass+ baseMethod8();

        public void roleMethod0() {}
        public void roleMethod1() {}
        public void roleMethod2() {}
        public void roleMethod3() {}
        public int roleMethod4(Integer val) {}
        callin <T extends MyClass> T roleMethod5() { return base.roleMethod5(); } 
    }
}
