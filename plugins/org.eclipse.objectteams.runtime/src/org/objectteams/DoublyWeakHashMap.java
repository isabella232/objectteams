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
 * @param <K> type of keys: a base class
 * @param <V> type of values: a role class
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

	// used from hasRole() and lifting (duplicate role check)
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedFeatureException("Method containsValue is not implemented for internal class DoublyWeakHashMap.");
	}

	// used from getRole()
	public V get(Object key) {
		WeakReference<V> valRef = this.map.get(key);
		return valRef == null ? null : valRef.get();
	}

	// used from migrateToBase() and lifting constructor
	public synchronized V put(K key, V value) {
		this.map.put(key, new WeakReference<V>(value));
		return value;
	}

	// used from unregisterRole(), migrateToBase()
	public synchronized V remove(Object key) {
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

	// used from getAllRoles() et al.
	public synchronized Collection<V> values() {
		ArrayList<V> result = new ArrayList<V>(this.map.size());
		for (WeakReference<V> valRef : this.map.values()) {
			V value = valRef.get();
			if (value != null)
				result.add(value);
		}
		return result;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new UnsupportedFeatureException("Method entrySet is not implemented for internal class DoublyWeakHashMap.");
	}
}
