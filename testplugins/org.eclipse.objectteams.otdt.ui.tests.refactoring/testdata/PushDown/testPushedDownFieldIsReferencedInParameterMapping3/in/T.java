package p;

public team class T {
	protected class R1 playedBy B {
		public final int f = 0;

		callin void rm(){ /* empty */ }
		
		void rm() <- replace int bm() with {
			f -> result 
		}
	}
	
	protected class R2 extends R1 {
		
	}
}