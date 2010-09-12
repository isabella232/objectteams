package cycle;
public class Base1 {
	public static class Inner {
		void foo() {}
		static class Leaf extends Inner {
			@Override void foo() {} 
		}
	}
}