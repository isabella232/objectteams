package statements_in; // 21, 13, 21, 20
public team class T_testTypeAnchor2 {
	public final R0 r;
	T_testTypeAnchor2()
	{
		r = new R0(new InnerTeam());
	}
	public class R0 {
		public final InnerTeam it;
		R0(InnerTeam it) {
			this.it = it;
		}
	}
	public team class InnerTeam {
		public class R1 {
			
		}		
	}
	public static void main(String[] args) {
		final T_testTypeAnchor2 t1 = new T_testTypeAnchor2();
		R1<@t1.r.it> r1 = null;
	}
}
