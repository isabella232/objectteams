package p_field_search;
import base p.Base1;
public team class FieldAccessInParameterMapping {
	protected class Role playedBy Base1 {
		public String value;
		String rm1() -> void baseMethod()
			with { 
				result <- value 
			}
		callin void rm2() { }
		void rm2() <- replace String baseMethod4() 
			with {
				value -> result
			}
	}
}