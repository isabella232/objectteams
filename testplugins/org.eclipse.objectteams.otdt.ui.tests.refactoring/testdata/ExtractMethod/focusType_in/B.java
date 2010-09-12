package focusType_in;

public class B extends A
{
	public void g1(int x)
	{
		/*[*/g2(x);/*]*/
	}
	public void g2(int x){}
}