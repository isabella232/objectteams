package p;
abstract class A{
	abstract void m(int i, int x);
}
class B {
	void m(int i) {}
}
team class MyTeam {
	protected class R extends A playedBy B {
		void m(int i, int x) -> void m(int i);
	}
}