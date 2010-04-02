package p;

public class B{
	public int m()
	{
		return n();
	}

	private int n()
	{
		return base_m();
	}

	public int base_m()
	{
		return 1;
	}
}