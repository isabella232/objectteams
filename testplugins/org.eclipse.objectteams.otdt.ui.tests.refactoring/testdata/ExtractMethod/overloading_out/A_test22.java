package overloading_out;

public class A_test22
{
	public void foo()
	{
		extracted();
	}

	protected void extracted()
	{
		/*[*/bar();/*]*/
	}

	public void bar()
	{

	}

	public void extracted(String str)
	{

	}
}