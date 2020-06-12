package org.warp.commonutils.type;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import org.jetbrains.annotations.NotNull;

public interface UnmodifiableIterableSet<K> extends Iterable<K> {

	int size();

	boolean isEmpty();

	void forEach(Consumer<? super K> action);

	Set<K> toUnmodifiableSet();

	<V> UnmodifiableIterableMap<K,V> toUnmodifiableIterableMapSetValues(V[] values);

	<K2> UnmodifiableIterableMap<K2,K> toUnmodifiableIterableMapSetKeys(K2[] keys);

	<V> UnmodifiableMap<K,V> toUnmodifiableMapSetValues(V[] values);

	<K2> UnmodifiableMap<K2,K> toUnmodifiableMapSetKeys(K2[] keys);

	static <K> UnmodifiableIterableSet<K> of(K[] items) {
		int keysSize = (items != null) ? items.length : 0;

		if (keysSize == 0) {
			// return mutable map
			return new UnmodifiableIterableSet<K>() {
				@NotNull
				@Override
				public Iterator<K> iterator() {
					return new Iterator<>() {
						@Override
						public boolean hasNext() {
							return false;
						}

						@Override
						public K next() {
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
				public void forEach(Consumer<? super K> action) {}

				@Override
				public Set<K> toUnmodifiableSet() {
					//noinspection unchecked
					return ObjectSets.EMPTY_SET;
				}

				@Override
				public <V> UnmodifiableIterableMap<K, V> toUnmodifiableIterableMapSetValues(V[] values) {
					return UnmodifiableIterableMap.of(null, values);
				}

				@Override
				public <K2> UnmodifiableIterableMap<K2, K> toUnmodifiableIterableMapSetKeys(K2[] keys) {
					return UnmodifiableIterableMap.of(keys, null);
				}

				@Override
				public <V> UnmodifiableMap<K, V> toUnmodifiableMapSetValues(V[] values) {
					return UnmodifiableMap.of(null, values);
				}

				@Override
				public <K2> UnmodifiableMap<K2, K> toUnmodifiableMapSetKeys(K2[] keys) {
					return UnmodifiableMap.of(keys, null);
				}
			};
		}

		return new UnmodifiableIterableSet<K>() {
			@Override
			public int size() {
				return keysSize;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public void forEach(Consumer<? super K> action) {
				for (int i = 0; i < items.length; i++) {
					action.accept(items[i]);
				}
			}

			@Override
			public Set<K> toUnmodifiableSet() {
				return ObjectSets.unmodifiable(new ObjectOpenHashSet<>(items, 1.0f));
			}

			@Override
			public <V> UnmodifiableIterableMap<K, V> toUnmodifiableIterableMapSetValues(V[] values) {
				return UnmodifiableIterableMap.of(items, values);
			}

			@Override
			public <K2> UnmodifiableIterableMap<K2, K> toUnmodifiableIterableMapSetKeys(K2[] keys) {
				return UnmodifiableIterableMap.of(keys, items);
			}

			@Override
			public <V> UnmodifiableMap<K, V> toUnmodifiableMapSetValues(V[] values) {
				return UnmodifiableMap.of(items, values);
			}

			@Override
			public <K2> UnmodifiableMap<K2, K> toUnmodifiableMapSetKeys(K2[] keys) {
				return UnmodifiableMap.of(keys, items);
			}

			@NotNull
			@Override
			public Iterator<K> iterator() {
				return new ObjectOpenHashSet<K>(items, 1.0f).iterator();
			}
		};
	}

	static <K> UnmodifiableIterableSet<K> of(Set<K> items, IntFunction<K[]> generator) {

		return new UnmodifiableIterableSet<K>() {
			@Override
			public int size() {
				return items.size();
			}

			@Override
			public boolean isEmpty() {
				return items.isEmpty();
			}

			@Override
			public void forEach(Consumer<? super K> action) {
				items.forEach(action);
			}

			@Override
			public Set<K> toUnmodifiableSet() {
				return Collections.unmodifiableSet(items);
			}

			@Override
			public <V> UnmodifiableIterableMap<K, V> toUnmodifiableIterableMapSetValues(V[] values) {
				return UnmodifiableIterableMap.of(items.toArray(generator), values);
			}

			@Override
			public <K2> UnmodifiableIterableMap<K2, K> toUnmodifiableIterableMapSetKeys(K2[] keys) {
				return UnmodifiableIterableMap.of(keys, items.toArray(generator));
			}

			@Override
			public <V> UnmodifiableMap<K, V> toUnmodifiableMapSetValues(V[] values) {
				return UnmodifiableMap.of(items.toArray(generator), values);
			}

			@Override
			public <K2> UnmodifiableMap<K2, K> toUnmodifiableMapSetKeys(K2[] keys) {
				return UnmodifiableMap.of(keys, items.toArray(generator));
			}

			@NotNull
			@Override
			public Iterator<K> iterator() {
				return items.iterator();
			}
		};
	}
}
