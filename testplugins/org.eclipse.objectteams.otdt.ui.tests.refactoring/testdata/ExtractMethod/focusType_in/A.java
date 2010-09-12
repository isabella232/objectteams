package focusType_in;

public class A
{
	public void f1(String s, int x)
	{
		/*[*/f2(s,x);/*]*/
	}
	public void f2(String s, int x){}
	public void extracted(String str){}
}