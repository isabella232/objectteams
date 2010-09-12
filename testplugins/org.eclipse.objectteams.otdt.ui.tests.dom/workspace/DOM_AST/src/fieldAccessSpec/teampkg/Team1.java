package fieldAccessSpec.teampkg;

import fieldAccessSpec.basepkg.MyClass;

/*
 * NOTE: The numbers at the end of each role member declaration are used to access 
 * it form test cases. Changing the order will break the corresponding test cases!
 */
public team class Team1 
{
	public class Role1 playedBy MyClass 
	{
		public abstract String getTestString(int b, String str);  //0
		getTestString -> get _string;                             //1

		public abstract void setTestInteger(Integer i);           //2
		void setTestInteger(Integer i) -> set Integer _integer;   //3
	}
}
