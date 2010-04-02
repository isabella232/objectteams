package p;

public class O extends S
{
	private S _s;
	protected B _b;

	public void m(String str, int x)
	{
		_s.m(this, str, x);
	}
}