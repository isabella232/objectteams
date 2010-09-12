package p;

import java.util.ArrayList;
import java.util.List;

class A<E> {
	public <T extends Number> void m(List<Integer> li, A<String> a_of_string) {}
}

class Sub<E> extends A<E> {
	public <T extends Number> void m(List<Integer> li, A<String> a_of_string) {}
	
	void test() {
		A<String> a_of_string= new A<String>();
		a_of_string.m(new ArrayList<Integer>(1), a_of_string);
		new Sub<Double>().m(new ArrayList<Integer>(2), a_of_string);
	}
}