package copyinheritancetests;

public team class SubTeam extends SuperTeam {
	void test() {
		R r = new R();
		r.roleMethod1();
		System.out.println("finished");
	}
	public static void main(String [] args) {
		new SubTeam().test();
	}
}