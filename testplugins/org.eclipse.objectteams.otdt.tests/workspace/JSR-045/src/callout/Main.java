package callout;
 public class Main {

    public static void main(String[] args)
    {
        SubTeam teamB = new SubTeam();
        teamB.activate();
        teamB.doit(new BaseClass());
    }
}