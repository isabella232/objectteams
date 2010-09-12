package p;

import java.util.List;

class A<E> {
	public <T extends Number> void m(A<String> a_of_string, List<Integer> li) {}
}

class Sub<E> extends A<E> {
	public <T extends Number> void m(A<String> a_of_string, List<Integer> li) {}
	
	void test() {
		A<String> a_of_string= new A<String>();
		a_of_string.m(a_of_string, null);
		new Sub<Double>().m(a_of_string, null);
	}
}