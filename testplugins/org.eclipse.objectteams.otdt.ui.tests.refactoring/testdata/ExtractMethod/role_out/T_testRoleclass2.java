package role_out;

public team class T_testRoleclass2
{
	public class R
	{
		private int a, b, c;

		public void foo()
		{
			extracted();
		}

		protected void extracted()
		{
			/*[*/c = a + b;/*]*/
		}

		public void bar()
		{
			c = a + b;
		}
	}
}