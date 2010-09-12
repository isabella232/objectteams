package callin_after_before;
public class Main
{
    public static void main(String[] args)
    {
        TeamA tA = new TeamA();
        tA.activate();

        TeamB tB = new TeamB();
        tB.activate();
        
        BaseClass s = new BaseClass();
        s.baseMethod();
    }
}