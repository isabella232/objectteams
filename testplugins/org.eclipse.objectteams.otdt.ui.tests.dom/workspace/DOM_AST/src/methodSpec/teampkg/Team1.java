package methodSpec.teampkg;

import methodSpec.basepkg.MyClass;

/*
 * NOTE: The numbers at the end of each role member declaration are used to access 
 * it form test cases. Changing the order will break the corresponding test cases!
 */
public team class Team1 
{
	public class Role1 playedBy MyClass 
	{
		public abstract String roleGetString(int b, String str);                          //0
		String roleGetString(int b, String str) -> String getString(int a, String str);	  //1
		
        public abstract Integer roleGetInteger();                                         //2
		Integer roleGetInteger() -> Integer getInteger();                                 //3
		
        public abstract void test();                                                      //4
		test -> test;                                                                     //5
		
		public void roleMethod0() {}                                                      //6
		roleMethod0 <- before baseMethod0;                                                //7
	}
}
