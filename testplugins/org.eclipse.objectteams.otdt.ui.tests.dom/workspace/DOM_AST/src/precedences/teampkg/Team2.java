package precedences.teampkg;

import precedences.basepkg.MyClass;

public team class Team1 
{
	public class Role1 playedBy MyClass
    {
        roleMethod0 <- before baseMethod1;
        callin1: roleMethod1 <- before baseMethod2;
        callin2: roleMethod2 <- before baseMethod2, baseMethod4, baseMethod5;
        callin3: roleMethod3 <- before baseMethod0, baseMethod4;

        callinA2: roleMethod2 <- after baseMethod2;
        callinA3: roleMethod3 <- after baseMethod3;

        public void roleMethod0() {}
        public void roleMethod1() {}
        public void roleMethod2() {}
        public void roleMethod3() {}
    
        precedence callin1, callin2;
        precedence callin3, callin2;
        
        precedence after callinA3, callinA2;
    }
	public class Role2 {}
	
	precedence Role1.callin2, Role1.callin1;
	precedence Role2, Role1;
}
