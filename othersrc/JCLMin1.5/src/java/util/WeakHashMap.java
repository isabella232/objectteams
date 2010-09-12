package java.util;

public class WeakHashMap<K,V> extends AbstractMap<K,V>  {
	public V put(K k, V v) { return v; }
	public void clear() {}
	public boolean isEmpty() { return true; }
	public boolean containsKey(V v) { return true; }
	public V remove(Object o) { return null; }
}
