package withinstatement.basepkg;

import withinstatement.teampkg.Team1;

public class MyClass
{
    Team1 myTeam = new Team1();
    
	void foo() {};
	
	void withinNewTeamEmptyBody() {
		within(new Team1()) {}
	}
	
	void withinNewTeam() {
		within(new Team1()) {
			foo();
		}
	}
	
	void withinSimpleEmptyBody() {	
		within(myTeam) {}
	}
	
	void withinSimple() {
		within(myTeam) {
			foo();
		}
	}
    
    void withinViaMethod()
    {
        within(getTeam(123)) {
            foo();
        }
    }
    
    void withinDummy() {
        Team1 t = new Team1();
        Object o = t;
    }
    
    Team1 getTeam(int dummy)
    {
        return myTeam;
    }
    
}
