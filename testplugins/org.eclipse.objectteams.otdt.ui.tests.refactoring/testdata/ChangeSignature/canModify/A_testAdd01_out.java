package p;
class A{
	/**
	 * @deprecated Use {@link #m(int,int)} instead
	 */
	private void m(int i){
		m(i, 0);
	}

	private void m(int i, int x){
	}
}
team class MyTeam {
	protected class R playedBy A {
		void m(int i) <- after void m(int i, int x);
		void m(int i) {}
	}
}