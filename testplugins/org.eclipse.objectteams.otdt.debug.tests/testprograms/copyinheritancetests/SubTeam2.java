package copyinheritancetests;

public team class SubTeam2 extends SuperTeam {
	@Override
	protected class R { /* empty */ }
	void test() {
		R r = new R();
		r.roleMethod1();
		System.out.println("finished");
	}
	public static void main(String [] args) {
		new SubTeam2().test();
	}
}