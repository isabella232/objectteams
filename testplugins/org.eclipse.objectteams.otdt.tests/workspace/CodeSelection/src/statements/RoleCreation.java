package statements;

public team class RoleCreation {
	public class R {
		protected R(String val) {
			// empty
		}
	}
	protected class RProtected {
		protected RProtected(String val) {
			// empty
		}		
	}
	void create(final RoleCreation t) {
		R<@t> r1 = new R<@t>("1");
		R r2     = new R("2");
		RProtected<@t> r3 = new RProtected<@t>("3");
		RProtected     r4 = new RProtected("4");
	}
}