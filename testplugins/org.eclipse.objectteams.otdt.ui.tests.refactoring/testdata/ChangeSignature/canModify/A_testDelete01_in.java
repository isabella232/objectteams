package p;
class A{
	private void m(int i, int x){
	}
}
team class MyTeam {
	protected class R playedBy A {
		void m(int i) <- after void m(int i, int x);
		void m(int i) {}
	}
}