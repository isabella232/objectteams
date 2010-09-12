package p;
class A {
	void m(int a, boolean b, String ignore) {}
}
class B {
	void bm(int a2, boolean b2) {}
}
team class MyTeam {
	protected class R1 extends A playedBy B {
		void m(int a, boolean b, String ignore) => bm(int a2, boolean b2);
	}
	protected class R2 playedBy A {
		void n(int a3, boolean b3) <- replace void m(int a, boolean b, String ignore);
		callin void n(int a3, boolean b3) {}
	}
}