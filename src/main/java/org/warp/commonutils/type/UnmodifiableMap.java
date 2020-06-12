package org.warp.commonutils.type;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface UnmodifiableMap<K, V> extends UnmodifiableIterableMap<K, V> {

	/**
	 * Returns {@code true} if this map contains a mapping for the specified
	 * key.  More formally, returns {@code true} if and only if
	 * this map contains a mapping for a key {@code k} such that
	 * {@code Objects.equals(key, k)}.  (There can be
	 * at most one such mapping.)
	 *
	 * @param key key whose presence in this map is to be tested
	 * @return {@code true} if this map contains a mapping for the specified
	 *         key
	 * @throws ClassCastException if the key is of an inappropriate type for
	 *         this map
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map
	 *         does not permit null keys
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	boolean containsKey(Object key);
	
	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that
	 * {@code Objects.equals(key, k)},
	 * then this method returns {@code v}; otherwise
	 * it returns {@code null}.  (There can be at most one such mapping.)
	 *
	 * <p>If this map permits null values, then a return value of
	 * {@code null} does not <i>necessarily</i> indicate that the map
	 * contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to {@code null}.  The {@link #containsKey
	 * containsKey} operation may be used to distinguish these two cases.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or
	 *         {@code null} if this map contains no mapping for the key
	 * @throws ClassCastException if the key is of an inappropriate type for
	 *         this map
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map
	 *         does not permit null keys
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	V get(Object key);

	/**
	 * Returns the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @implSpec
	 * The default implementation makes no guarantees about synchronization
	 * or atomicity properties of this method. Any implementation providing
	 * atomicity guarantees must override this method and document its
	 * concurrency properties.
	 *
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the default mapping of the key
	 * @return the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key
	 * @throws ClassCastException if the key is of an inappropriate type for
	 * this map
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map
	 * does not permit null keys
	 * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @since 1.8
	 */
	default V getOrDefault(Object key, V defaultValue) {
		V v;
		return (((v = get(key)) != null) || containsKey(key))
				? v
				: defaultValue;
	}

	@NotNull
	ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator();

	/**
	 * Performs the given action for each entry in this map until all entries
	 * have been processed or the action throws an exception.   Unless
	 * otherwise specified by the implementing class, actions are performed in
	 * the order of entry set iteration (if an iteration order is specified.)
	 * Exceptions thrown by the action are relayed to the caller.
	 *
	 * @implSpec
	 * The default implementation is equivalent to, for this {@code map}:
	 * <pre> {@code
	 * for (Map.Entry<K, V> entry : map.entrySet())
	 *     action.accept(entry.getKey(), entry.getValue());
	 * }</pre>
	 *
	 * The default implementation makes no guarantees about synchronization
	 * or atomicity properties of this method. Any implementation providing
	 * atomicity guarantees must override this method and document its
	 * concurrency properties.
	 *
	 * @param action The action to be performed for each entry
	 * @throws NullPointerException if the specified action is null
	 * @throws ConcurrentModificationException if an entry is found to be
	 * removed during iteration
	 * @since 1.8
	 */
	void forEach(BiConsumer<? super K, ? super V> action);

	static <K, V> UnmodifiableMap<K, V> of(K[] keys, V[] values) {
		int keysSize = (keys != null) ? keys.length : 0;
		int valuesSize = (values != null) ? values.length : 0;

		if (keysSize == 0 && valuesSize == 0) {
			// return mutable map
			return new EmptyUnmodifiableMap<>();
		}

		return new MappedUnmodifiableMap<>(new Object2ObjectOpenHashMap<>(keys, values, 1.0f));
	}

	static <K, V> UnmodifiableMap<K, V> of(Map<K, V> map) {
		return new MappedUnmodifiableMap<K, V>(map);
	}

	class EmptyUnmodifiableMap<K, V> implements UnmodifiableMap<K, V> {

		private EmptyUnmodifiableMap() {}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public boolean containsKey(Object key) {
			return false;
		}

		@Override
		public V get(Object key) {
			return null;
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {

		}

		@NotNull
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new Iterator<>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Entry<K, V> next() {
					throw new NoSuchElementException();
				}
			};
		}

		@NotNull
		@Override
		public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() {
			return new ObjectIterator<>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Object2ObjectMap.Entry<K, V> next() {
					throw new NoSuchElementException();
				}
			};
		}

		@Override
		public Map<K, V> toUnmodifiableMap() {
			//noinspection unchecked
			return Object2ObjectMaps.EMPTY_MAP;
		}

		@Override
		public Stream<Entry<K, V>> stream() {
			return Stream.empty();
		}

		@Override
		public UnmodifiableIterableSet<K> toUnmodifiableIterableKeysSet(IntFunction<K[]> generator) {
			return UnmodifiableIterableSet.of(null);
		}
	}

	class MappedUnmodifiableMap<K, V> implements UnmodifiableMap<K, V> {

		private final Map<K,V> map;

		private MappedUnmodifiableMap(@NotNull Map<K, V> map) {
			this.map = map;
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public V get(Object key) {
			return map.get(key);
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {
			map.forEach(action);
		}

		@NotNull
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return map.entrySet().iterator();
		}

		@NotNull
		@Override
		public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() {
			if (map instanceof Object2ObjectMap) {
				return Object2ObjectMaps.fastIterator((Object2ObjectMap<K, V>) map);
			} else {
				var iterator = map.entrySet().iterator();
				var reusableEntry = new Object2ObjectMap.Entry<K, V>() {
					private K key;
					private V val;
					@Override
					public K getKey() {
						return key;
					}

					@Override
					public V getValue() {
						return val;
					}

					@Override
					public V setValue(V value) {
						throw new UnsupportedOperationException();
					}
				};
				return new ObjectIterator<>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Object2ObjectMap.Entry<K, V> next() {
						var next = iterator.next();
						reusableEntry.key = next.getKey();
						reusableEntry.val = next.getValue();
						return reusableEntry;
					}
				};
			}
		}

		@Override
		public Map<K, V> toUnmodifiableMap() {
			return Collections.unmodifiableMap(map);
		}

		@Override
		public Stream<Entry<K, V>> stream() {
			return map.entrySet().stream();
		}

		@Override
		public UnmodifiableIterableSet<K> toUnmodifiableIterableKeysSet(IntFunction<K[]> generator) {
			return UnmodifiableIterableSet.of(map.keySet().toArray(generator));
		}
	}
}
