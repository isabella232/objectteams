package java.util;

public interface Collection<E> extends Iterable<E> {
	void addAll(Collection<? extends E> other);
	E[] toArray();
	public int size();
}
