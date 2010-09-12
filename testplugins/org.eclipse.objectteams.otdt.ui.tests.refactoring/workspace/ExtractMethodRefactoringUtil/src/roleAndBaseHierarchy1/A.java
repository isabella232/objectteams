package roleAndBaseHierarchy1;

public class A
{
    private void n(char c){}
    public void g(String s){}
    public void f(int x)
    {
        String s = "";
        /*[*/g(s);/*]*/
    }
}