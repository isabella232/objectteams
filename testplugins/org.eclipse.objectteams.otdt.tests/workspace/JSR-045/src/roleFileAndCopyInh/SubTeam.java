package roleFileAndCopyInh;

import roleFileAndCopyInh.SuperTeam;

public team class SubTeam extends SuperTeam
{
	public static void main(String[] args)
	{
		SubTeam subTeam = new SubTeam();
		subTeam.doIt();
	}

	public void doIt()
	{
	    final SuperTeam superTeam = new SuperTeam();
	    superTeam.RoleA roleA = superTeam.new RoleA();
		roleA.roleMethod();
	}
}
