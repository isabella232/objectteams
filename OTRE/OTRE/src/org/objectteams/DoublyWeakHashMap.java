/**********************************************************************
 * This file is part of the "Object Teams Runtime Environment"
 * 
 * Copyright 2010 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.objectteams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This class defines hash maps where both key and value are weak references.
 * It is implemented by delegating to a WeakHashMap and additionally
 * wrapping the value in a WeakReference.
 * 
 * @author stephan
 * @since 0.7.0
 * @param <K>
 * @param <V>
 */
public class DoublyWeakHashMap<K,V> implements Map<K,V> {

	private WeakHashMap<K, WeakReference<V>> map;
	
	public DoublyWeakHashMap() {
		this.map = new WeakHashMap<K, WeakReference<V>>();
	}
	public int size() {
		return this.map.size();
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	public V get(Object key) {
		return this.map.get(key).get();
	}

	public V put(K key, V value) {
		this.map.put(key, new WeakReference<V>(value));
		return value;
	}

	public V remove(Object key) {
		WeakReference<V> value = this.map.remove(key);
		return (value == null) ? null : value.get();
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		for (Entry<? extends K, ? extends V> entry : t.entrySet())
			this.map.put(entry.getKey(), new WeakReference<V>(entry.getValue()));
	}

	public void clear() {
		this.map.clear();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public Collection<V> values() {
		ArrayList<V> result = new ArrayList<V>(this.map.size());
		for (WeakReference<V> valRef : this.map.values())
			result.add(valRef.get());
		return result;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedFeatureException("Method entrySet is not implemented for DoublyWeakHashMap");
	}
}
