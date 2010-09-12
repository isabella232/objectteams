package java.lang;

public class String implements CharSequence {
	public int length() { return 0; }
	public boolean endsWith(String postfix) { return false; }
	public String substring(int start) { return this; }
}
