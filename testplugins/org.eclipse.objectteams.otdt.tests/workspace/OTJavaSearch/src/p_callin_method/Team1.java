package p_callin_method;

public team class Team1
{
	public class Role1 playedBy Base1
	{
		callin void callinMethod()
		{
			base.callinMethod();
		}
		
		callinMethod <- replace baseMethod;
		
		callin void callinMethodParam(String str) 
		{
			base.callinMethodParam(str);
			privateRoleMethod(str);
		}
		
		callinMethodParam <- replace baseMethodParam;
		
		private void privateRoleMethod(String str) {
			; // nop
		}
	}
}
