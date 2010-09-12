package p;
team class A{
	void m(int i, int x);
	protected class R extends A playedBy B {
		void m(int i, int x) <- after void b(int i);
	}
}
class B {
	void b(int i) {}
}