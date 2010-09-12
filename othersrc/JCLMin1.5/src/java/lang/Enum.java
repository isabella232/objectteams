package java.lang;

@SuppressWarnings("serial")
public abstract class Enum<T extends Enum<T>> implements Comparable<T>, java.io.Serializable {
	protected Enum(String name, int ordinal) {
	}
	public final String name() {
		return null;
	}
	public final int ordinal() {
		return 0;
	}
}