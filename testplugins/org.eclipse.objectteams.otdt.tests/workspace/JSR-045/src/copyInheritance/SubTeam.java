package copyInheritance;

public team class SubTeam extends SuperTeam
{
	public class RoleA {
		public void roleMethod0() {
			tsuper.roleMethod0();
			System.out.println("testy");
		}
	}
	public static void main(String[] args)
	{
		SubTeam subTeam = new SubTeam();
		subTeam.doIt();
	}

	public void doIt()
	{
		RoleA roleA = new RoleA();
		roleA.roleMethod0();
	}
}
