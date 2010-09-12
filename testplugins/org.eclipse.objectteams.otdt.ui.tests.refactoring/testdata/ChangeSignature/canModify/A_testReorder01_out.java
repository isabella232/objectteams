package p;
class A {
	void m(String ignore, boolean b, int a) {}
}
class B {
	void bm(int a2, boolean b2) {}
}
team class MyTeam {
	protected class R1 extends A playedBy B {
		void m(String ignore, boolean b, int a) => bm(int a2, boolean b2) with {
			b -> b2,
			a -> a2
		}
	}
	protected class R2 playedBy A {
		void n(int a3, boolean b3) <- replace void m(String ignore, boolean b, int a) with {
			b3 <- b,
			a3 <- a
		}
		callin void n(int a3, boolean b3) {}
	}
}