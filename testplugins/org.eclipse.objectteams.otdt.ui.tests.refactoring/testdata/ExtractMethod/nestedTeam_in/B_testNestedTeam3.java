package nestedTeam_in;

public class B_testNestedTeam3
{
    public void m1(int x)
    {
        /*[*/m2(x);/*]*/
    }
    private void m2(int x){}
    public void extracted(){}
}
