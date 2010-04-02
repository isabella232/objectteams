package focusType_in;

public class C extends B
{
	public void h1(int x, int y)
	{
		/*[*/int a, b;
			 a = x;
			 b = y;
			 h2(a, b);/*]*/
	}
	public void h2(int a, int b){}
}