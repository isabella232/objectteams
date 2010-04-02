package copyInheritance;
public team class SuperTeam extends SuperSuperTeam
{
	public class RoleA
	{
		public void roleMethod2()
		{
			System.out.println("Test");
		}
		public void roleMethod0() {
			tsuper.roleMethod0();
			roleMethod2();
		}
	}
}