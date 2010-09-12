package callouts.teampkg;

import callouts.basepkg.MyClass;

/*
 * NOTE: The numbers at the end of each role member declaration are used to access 
 * it form test cases. Changing the order will break the corresponding test cases!
 */
public team class Team1 
{
	public class Role1 playedBy MyClass 
	{
		public abstract void fooBar();										//0
		public void fooBar2() {}	             							//1
		fooBar -> foo;														//2
		void fooBar2() => void foo();											//3
		
		public abstract String getBaseField();									//4
		String getBaseField() -> get String fieldName;							//5
		public abstract void setBaseField();									//6
		setBaseField -> set fieldName;										//7
		
		public abstract String roleMethodWithMapping(Integer x, Boolean b1);					        //8
		String roleMethodWithMapping(Integer x, Boolean b1) -> String baseMethod(int y, bool b2) with	    //9
		{
			x.intValue() -> y,
			b1.booleanValue() -> b2,
			result <- result + "test"
		}
		protected String baseToString() -> String toString(); // 10
	}
}
