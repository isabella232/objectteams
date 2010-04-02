package bindings.basepkg;

public class C1
{
    public void m1()
    {}

    public String m2(Integer i)
    {
        return Integer.toString(i.intValue());
    }
    
    public Integer m3(String s)
    {
        return Integer.valueOf(s);
    }

    public String m4(String s)
    {
        return s;
    }
    
    public Integer m5(Integer i)
    {
        return i;
    }


}
