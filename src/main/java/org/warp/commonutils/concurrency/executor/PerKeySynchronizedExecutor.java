
package org.warp.commonutils.concurrency.executor;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import org.warp.commonutils.functional.IORunnable;
import org.warp.commonutils.functional.IOSupplier;
import org.warp.commonutils.random.HashUtil;

/**
 * An Executor which executes tasks on the caller thread.
 * The tasks will be executed synchronously on a <b>per-key basis</b>.
 * By saying <b>per-key</b>, we mean that thread safety is guaranteed for threads calling it with equals keys.
 * When two threads calling the executor with equals keys, the executions will never overlap each other.
 * On the other hand, the executor is implemented so calls from different threads, with keys that are not equals, will be executed concurrently with minimal contention between the calls.
 * Calling threads might be suspended.
 * Calling execute from different threads with equals keys has the same memory semantics as locking and releasing a java.util.concurrent.locks.{@link Lock}.
 */
public final class PerKeySynchronizedExecutor<KEY_TYPE> extends ReadWriteExecutor implements Closeable {

	private static final int BASE_CONCURRENCY_LEVEL = 32;

	private final int concurrencyLevel;

	private final ConcurrencySegment<KEY_TYPE, SynchronizedExecutor>[] segments;

	private boolean closed = false;

	public PerKeySynchronizedExecutor() {
		this(BASE_CONCURRENCY_LEVEL);
	}

	@SuppressWarnings({"unchecked"})
	public PerKeySynchronizedExecutor(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
		segments = (ConcurrencySegment<KEY_TYPE, SynchronizedExecutor>[]) new ConcurrencySegment[concurrencyLevel];
		for (int i = 0; i < concurrencyLevel; i++) {
			segments[i] = new ConcurrencySegment<>(SynchronizedExecutor::new);
		}
	}

	public void execute(KEY_TYPE key, Runnable task) {
		super.execute(LockMode.READ, () -> {
			if (closed) throw new IllegalStateException(PerKeySynchronizedExecutor.class.getSimpleName() + " is closed");
			int segmentIndex = HashUtil.boundedHash(key, concurrencyLevel);
			ConcurrencySegment<KEY_TYPE, SynchronizedExecutor> s = segments[segmentIndex];
			SynchronizedExecutor executor = s.getValue(key);
			try {
				executor.execute(task);
			} finally {
				s.releaseKey(key);
			}
		});
	}

	public void executeIO(KEY_TYPE key, IORunnable task) throws IOException {
		super.executeIO(LockMode.READ, () -> {
			if (closed) throw new IllegalStateException(PerKeySynchronizedExecutor.class.getSimpleName() + " is closed");
			int segmentIndex = HashUtil.boundedHash(key, concurrencyLevel);
			ConcurrencySegment<KEY_TYPE, SynchronizedExecutor> s = segments[segmentIndex];
			SynchronizedExecutor executor = s.getValue(key);
			try {
				executor.executeIO(task);
			} finally {
				s.releaseKey(key);
			}
		});
	}

	public <R> R execute(KEY_TYPE key, Supplier<R> task) {
		return super.execute(LockMode.READ, () -> {
			if (closed) throw new IllegalStateException(PerKeySynchronizedExecutor.class.getSimpleName() + " is closed");
			int segmentIndex = HashUtil.boundedHash(key, concurrencyLevel);
			ConcurrencySegment<KEY_TYPE, SynchronizedExecutor> s = segments[segmentIndex];
			SynchronizedExecutor executor = s.getValue(key);
			try {
				return executor.execute(task);
			} finally {
				s.releaseKey(key);
			}
		});
	}

	public <R> R executeIO(KEY_TYPE key, IOSupplier<R> task) throws IOException {
		return super.executeIO(LockMode.READ, () -> {
			if (closed) throw new IllegalStateException(PerKeySynchronizedExecutor.class.getSimpleName() + " is closed");
			int segmentIndex = HashUtil.boundedHash(key, concurrencyLevel);
			ConcurrencySegment<KEY_TYPE, SynchronizedExecutor> s = segments[segmentIndex];
			SynchronizedExecutor executor = s.getValue(key);
			try {
				return executor.executeIO(task);
			} finally {
				s.releaseKey(key);
			}
		});
	}

	@Override
	public void close() {
		super.execute(LockMode.WRITE, () -> {
			closed = true;
		});
	}
}