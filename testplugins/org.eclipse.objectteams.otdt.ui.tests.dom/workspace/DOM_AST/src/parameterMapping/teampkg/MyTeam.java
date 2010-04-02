package parameterMapping.teampkg;

import parameterMapping.basepkg.MyClass;

public team class MyTeam
{
	public class Role1 playedBy MyClass 
	{
		public abstract void roleMethod0(Integer integer);
		public abstract void roleMethod1(int val);
		public abstract Integer roleMethod2(int val);
		public abstract Integer roleMethod3(String string, int val);

		void roleMethod0(Integer integer) -> void baseMethod0(int val) with
		{
			integer.intValue() -> val
		}
		
		void roleMethod1(int val) -> void baseMethod1(Integer integer) with
		{
			new Integer(val) -> integer
		}

		Integer roleMethod2(int val) -> int baseMethod2(Integer integer) with
		{
			new Integer(val) -> integer,
			result <- new Integer(result)
		}		

		Integer roleMethod3(String string, int val) -> int baseMethod3(String string, Integer integer) with
		{
			string -> string,
			new Integer(val) -> integer,
			result <- new Integer(result)
		}		
	}

	public class Role2 playedBy MyClass 
	{
		 callin int roleMethod4 (Integer roleInteger)
		 {
		 	return 0;
		 }
		 
		 int roleMethod5 (Integer roleInteger)
		 {
		 	return 0;
		 }
		 
		 int roleMethod6 (Integer roleInteger)
		 {
		 	return 0;
		 }
		 
		 int roleMethod4(Integer roleInteger) <-  replace int baseMethod4(Integer integer) with
		 {
		 	roleInteger <-  integer,
			result -> result
		 }

		 int roleMethod5(Integer roleInteger) <-  after int baseMethod5(Integer integer) with
		 {
		 	roleInteger <-  integer,
			result -> result // illegal but shouldn't throw exception
		 }
		 
		 int roleMethod6(Integer roleInteger) <-  before int baseMethod6(Integer integer) with
		 {
		 	roleInteger <-  integer
		 }
	}
}