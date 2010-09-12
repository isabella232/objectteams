package p;

public class B{
	
	private int f = 1;
	
	public void m()
	{
		base_m();
		n();
	}

	private void n()
	{
		int g = f;
	}

	public void base_m()
	{
	}
}