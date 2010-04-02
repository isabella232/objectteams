package roleFile;

public team class TeamA
{
	public static void main(String[] args)
	{
		new TeamA().doIt();
	}
	
	public void doIt()
	{
		RoleA roleA = new RoleA();
		roleA.roleMethod();
	}
}