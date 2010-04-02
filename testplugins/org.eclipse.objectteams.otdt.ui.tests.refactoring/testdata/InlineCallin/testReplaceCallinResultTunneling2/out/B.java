package p;

public class B{
	public int m()
	{
		return n();
	}

	private int n()
	{
		base_m();
		int i = 1;
		return 5;
	}

	public int base_m()
	{
		return 1;
	}
}