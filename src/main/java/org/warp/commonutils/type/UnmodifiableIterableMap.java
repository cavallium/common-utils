package org.warp.commonutils.type;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface UnmodifiableIterableMap<K, V> extends Iterable<Entry<K, V>> {

	/**
	 * Returns the number of key-value mappings in this map.  If the
	 * map contains more than {@code Integer.MAX_VALUE} elements, returns
	 * {@code Integer.MAX_VALUE}.
	 *
	 * @return the number of key-value mappings in this map
	 */
	int size();

	/**
	 * Returns {@code true} if this map contains no key-value mappings.
	 *
	 * @return {@code true} if this map contains no key-value mappings
	 */
	boolean isEmpty();

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

	Map<K, V> toUnmodifiableMap();

	Stream<Entry<K, V>> stream();

	UnmodifiableIterableSet<K> toUnmodifiableIterableKeysSet(IntFunction<K[]> generator);

	@SuppressWarnings("SuspiciousSystemArraycopy")
	static <K, V> UnmodifiableIterableMap<K, V> ofObjects(Object[] keys, Object[] values) {
		if (keys == null || values == null || (keys.length == 0 && values.length == 0)) {
			return UnmodifiableIterableMap.of(null, null);
		} else if (keys.length == values.length) {
			//noinspection unchecked
			K[] keysArray = (K[]) Array.newInstance(keys[0].getClass(), keys.length);
			System.arraycopy(keys, 0, keysArray, 0, keys.length);
			//noinspection unchecked
			V[] valuesArray = (V[]) Array.newInstance(values[0].getClass(), keys.length);
			System.arraycopy(values, 0, valuesArray, 0, values.length);
			return UnmodifiableIterableMap.of(keysArray, valuesArray);
		} else {
			throw new IllegalArgumentException("The number of keys doesn't match the number of values.");
		}
	}

	static <K, V> UnmodifiableIterableMap<K, V> of(K[] keys, V[] values) {
		int keysSize = (keys != null) ? keys.length : 0;
		int valuesSize = (values != null) ? values.length : 0;

		if (keysSize == 0 && valuesSize == 0) {
			// return mutable map
			return new EmptyUnmodifiableIterableMap<>();
		}

		if (keysSize != valuesSize) {
			throw new IllegalArgumentException("The number of keys doesn't match the number of values.");
		}

		return new ArrayUnmodifiableIterableMap<>(keys, values, keysSize);
	}

	class EmptyUnmodifiableIterableMap<K, V> implements UnmodifiableIterableMap<K, V> {

		private EmptyUnmodifiableIterableMap() {}

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

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {}

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

	class ArrayUnmodifiableIterableMap<K, V> implements UnmodifiableIterableMap<K, V> {

		private final K[] keys;
		private final V[] values;
		private final int keysSize;

		private ArrayUnmodifiableIterableMap(K[] keys, V[] values, int keysSize) {
			this.keys = keys;
			this.values = values;
			this.keysSize = keysSize;
		}

		@NotNull
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new Object2ObjectOpenHashMap<K, V>(keys, values, 1.0f).entrySet().iterator();
		}

		@Override
		public int size() {
			return keysSize;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void forEach(BiConsumer<? super K, ? super V> action) {
			for (int i = 0; i < keys.length; i++) {
				action.accept(keys[i], values[i]);
			}
		}

		@Override
		public Map<K, V> toUnmodifiableMap() {
			return Object2ObjectMaps.unmodifiable(new Object2ObjectOpenHashMap<>(keys, values, 1.0f));
		}

		@Override
		public Stream<Entry<K, V>> stream() {
			//noinspection UnstableApiUsage
			return Streams.zip(Stream.of(keys), Stream.of(values), Map::entry);
		}

		@Override
		public UnmodifiableIterableSet<K> toUnmodifiableIterableKeysSet(IntFunction<K[]> generator) {
			return UnmodifiableIterableSet.of(keys);
		}
	}
}
