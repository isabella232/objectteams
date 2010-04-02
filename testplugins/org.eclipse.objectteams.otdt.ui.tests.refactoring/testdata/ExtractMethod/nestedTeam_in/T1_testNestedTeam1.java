package nestedTeam_in;

public team class T1_testNestedTeam1
{
	public team class NestedTeam
	{
		public void foo()
		{
			/*[*/bar();/*]*/
		}

		public void bar(){}
	}
}