package nestedTeam_out;

public team class T1_testNestedTeam2
{
	public team class NestedTeam
	{
		public class InnerRole
		{
			public void foo()
			{
				extracted();
			}

			protected void extracted()
			{
				/*[*/bar();/*]*/
			}

			public void bar(){}
		}
	}
}