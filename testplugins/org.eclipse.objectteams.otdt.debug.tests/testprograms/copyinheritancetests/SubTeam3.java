package copyinheritancetests;

public team class SubTeam3 extends SuperTeam3 {
	@Override
	protected class R { /* empty */ }
	void test() {
		R r = new R();
		r.roleMethod1();
		System.out.println("finished");
	}
	public static void main(String [] args) {
		new SubTeam3().test();
	}
}