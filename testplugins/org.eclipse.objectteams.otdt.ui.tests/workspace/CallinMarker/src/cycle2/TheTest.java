package cycle2;
public class TheTest {
        final B b = new B();
        final A a = new A<@b>();

        public void run() {
                within(b) {
                        a.run();                        
                }
        }
        public static void main(String[] args) {
			TheTest theTest = new TheTest();
			theTest.run();
		}
 }