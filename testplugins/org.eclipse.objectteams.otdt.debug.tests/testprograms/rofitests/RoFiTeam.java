package rofitests;

public team class RoFiTeam {
	void doit() {
		System.out.println("doit");
		RoFiRole rfr = new RoFiRole();
		rfr.doRolish();
	}
	public static void main(String[] args) {
		RoFiTeam rft = new RoFiTeam();
		rft.doit();
	}
}