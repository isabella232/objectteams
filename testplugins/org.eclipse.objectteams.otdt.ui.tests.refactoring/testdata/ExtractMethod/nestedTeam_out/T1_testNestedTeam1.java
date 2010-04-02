package nestedTeam_out;

public team class T1_testNestedTeam1
{
	public team class NestedTeam
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