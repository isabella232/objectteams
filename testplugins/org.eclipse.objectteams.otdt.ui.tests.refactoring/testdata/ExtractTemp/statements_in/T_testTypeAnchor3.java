package statements_in; // 19, 13, 19, 20
public team class T_testTypeAnchor2 {
	public final R0 r;
	T_testTypeAnchor2()
	{
		r = new R0();
	}
	public team class R0 {
		public final R1 r1;
		R0() {
			this.it = it;
		}
		public team class R1 {
			public class R2 {}
		}		
	}
	public static void main(String[] args) {
		final T_testTypeAnchor2 t1 = new T_testTypeAnchor2();
		R2<@t1.r.r1> r2 = null;
	}
}
