package p;

public class B{
	public int m()
	{
		return n();
	}

	private int n()
	{
		int baseResult;
		baseResult = base_m();
		int i = 1;
		return baseResult;
	}

	public int base_m()
	{
		return 1;
	}
}