package nestedTeam_in;

public team class T1_testNestedTeam2
{
	public team class NestedTeam
	{
		public class InnerRole
		{
			public void foo()
			{
				/*[*/bar();/*]*/
			}

			public void bar(){}
		}
	}
}