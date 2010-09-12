package statements_out; // 9,16 - 9, 35
/**
 * @author stephan
 *
 */
public team class T_testWithin1 {
	public static void main(String[] args) {
		int i = 7;
		T_testWithin1 extracted = new T_testWithin1();
		within(extracted) {
			System.out.println("within1");
		}
		System.err.println("after");
	}
}
 