package org.warp.commonutils.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public abstract class KVSafeBatching<T, U> extends Batching<Pair<T, U>> {

	public KVSafeBatching(int pingRefreshTimeMillis) {
		super(pingRefreshTimeMillis);
	}

	@Deprecated
	@Override
	public void offer(Pair<T, U>... actions) {
		offer(List.of(actions));
	}

	@Deprecated
	@Override
	public void offer(Collection<Pair<T, U>> actions) {
		Object[] keys = new Object[actions.size()];
		Object[] values = new Object[actions.size()];
		int i = 0;
		for (Pair<T, U> action : actions) {
			keys[i] = action.getKey();
			values[i] = action.getValue();
			i++;
		}
		offer_(keys, values);
	}

	public void offer(T key, U value) {
		this.offer_(key, value);
	}

	public void offer(T[] keys, U[] values) {
		if (keys.length == 1 && values.length == 1) {
			this.offer_(keys[0], values[0]);
		} else {
			this.offer_(keys, values);
		}
	}

	private void offer_(T key, U value) {
		super.offer(Pair.of(key, value));
	}

	private void offer_(Object[] keys, Object[] values) {
		if (keys.length != values.length) {
			throw new IllegalArgumentException("Keys and values count must be the same.");
		}
		List<Pair<T, U>> pairs = new ArrayList<>(keys.length);
		for (int i = 0; i < keys.length; i++) {
			pairs.add(Pair.of((T) keys[i], (U) values[i]));
		}
		super.offer(pairs);
	}

	@Override
	protected void executeBatch(Collection<Pair<T, U>> actions) {

	}

	@Override
	protected void executeDirect(Pair<T, U> action) {

	}

	@Override
	protected void executeDirect(Collection<Pair<T, U>> action) {

	}

	@Override
	public void close() {

	}
}
