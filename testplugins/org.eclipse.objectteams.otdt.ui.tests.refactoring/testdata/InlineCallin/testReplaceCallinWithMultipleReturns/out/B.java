package p;

public class B{
	public void m()
	{
		n();
	}

	private void n()
	{
		if (true) {
			base_m();
			return;
		} else {
			base_m();
			return;
		}
		return;
	}

	public void base_m()
	{
	}
}