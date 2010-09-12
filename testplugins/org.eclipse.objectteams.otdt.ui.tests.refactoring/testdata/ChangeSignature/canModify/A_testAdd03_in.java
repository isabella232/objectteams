package p;
team class A{
	void m(int i);
	protected class R extends A playedBy B {
		void m(int i) <- after void b(int i);
	}
}
class B {
	void b(int i) {}
}