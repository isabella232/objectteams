package testbug411591;

public class Main {
	public static void main(String... args) {
		Runnable r = new Runnable() {
			public void run() {}
		};
		new T().test();
		new T2().test();
	}
}