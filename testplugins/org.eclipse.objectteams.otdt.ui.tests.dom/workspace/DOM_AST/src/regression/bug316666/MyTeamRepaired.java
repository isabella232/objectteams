package regression.bug316666;
public team class MyTeamRepaired {

    protected abstract team class R playedBy T73Mab1 {
    	precedence R2_R, R1_R;
    	
        @SuppressWarnings(\"basecall\")
        callin void foo() { System.out.print(getClass().getName()); }
        @SuppressWarnings(\"def-bind-ambiguity\")
        foo <- replace test; // lifting not recommended

    protected team class R1_R extends R
        base when (Team73Mab1.this.hasRole(base, R1_R.class))
    {
    
    protected class R2_R extends R\n" +
        base when (Team73Mab1.this.hasRole(base, R2_R.class))
    {
    }
    } // R1_R
    } // R
    
    Team73Mab1 (T73Mab1 as R1_R o) {}
    public static void main(String[] args) {
        T73Mab1 o = new T73Mab1();
        Team73Mab1 t = new Team73Mab1(o);
        t.activate();
        o.test();
    }
}
