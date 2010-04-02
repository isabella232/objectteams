package p;

public class B{
	public int m()
	{
		int baseResult = base_m();
		n();
		return baseResult;
	}

	private void n()
	{
	}

	public int base_m()
	{
		return 1;
	}
}