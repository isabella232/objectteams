team package roleFile_out.TeamWithRoleFile;

public class R_testRoleFile2
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