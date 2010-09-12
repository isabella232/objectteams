package roleAndBaseHierarchy1;

public class B extends A
{
    public void h(){}
    public void f(int x)
    {
        /*[*/h();/*]*/
    }
}