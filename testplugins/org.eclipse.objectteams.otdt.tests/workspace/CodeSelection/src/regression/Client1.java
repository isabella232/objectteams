package regression;
public class Client1 {	
	private static A mc1(A a) 
	{
		return new A(a.cs) 
		{
			@Override
			public String mx(int i1) {
				return "x";
			}
		};
	}
}
