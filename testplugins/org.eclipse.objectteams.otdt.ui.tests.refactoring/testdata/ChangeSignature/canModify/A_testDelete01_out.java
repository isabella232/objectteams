package p;
class A{
	/**
	 * @deprecated Use {@link #m(int)} instead
	 */
	private void m(int i, int x){
		m(i);
	}

	private void m(int i){
	}
}
team class MyTeam {
	protected class R playedBy A {
		void m(int i) <- after void m(int i);
		void m(int i) {}
	}
}