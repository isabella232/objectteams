package regression.bug316666;
public team class MyTeam {
        precedence R2_R, R1_R;

    protected abstract class R playedBy T73Mab1 {\n" +
        @SuppressWarnings(\"basecall\")\n" +
        callin void foo() { System.out.print(getClass().getName()); }\n" +
                 @SuppressWarnings(\"def-bind-ambiguity\")\n" +
        foo <- replace test; // lifting not recommended\n" +
    }\n" +
    protected class R1_R extends R\n" +
        base when (Team73Mab1.this.hasRole(base, R1_R.class))\n" +
    {\n" +
    }\n" +
    protected class R2_R extends R\n" +
        base when (Team73Mab1.this.hasRole(base, R2_R.class))\n" +
    {\n" +
    }\n" +
    \n" +
    Team73Mab1 (T73Mab1 as R1_R o) {}\n" +
    public static void main(String[] args) {\n" +
        T73Mab1 o = new T73Mab1();\n" +
        Team73Mab1 t = new Team73Mab1(o);\n" +
        t.activate();\n" +
        o.test();\n" +
    }
}
