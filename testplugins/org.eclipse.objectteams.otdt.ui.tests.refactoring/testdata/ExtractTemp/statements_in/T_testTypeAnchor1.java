package statements_in; // 15, 13, 15, 18
public team class T_testTypeAnchor1 {
	public final InnerTeam it;
	T_testTypeAnchor1()
	{
		it = new InnerTeam();
	}
	public team class InnerTeam {
		public class R1 {
			
		}		
	}
	public static void main(String[] args) {
		final T_testTypeAnchor1 t1 = new T_testTypeAnchor1();
		R1<@t1.it> r1 = null;
	}
}
