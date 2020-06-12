package org.warp.commonutils.concurrency.executor;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.warp.commonutils.functional.IORunnable;
import org.warp.commonutils.functional.IOSupplier;
import org.warp.commonutils.locks.LockUtils;

/**
 * An Executor which executes tasks on the caller thread.
 * The tasks will be executed synchronously, so no overlapping between two tasks running on different threads will ever occur.
 * Calling threads might be suspended.
 * Executing a task has the same memory semantics as locking and releasing a java.util.concurrent.locks.{@link Lock}.
 */
public class ReadWriteExecutor {

	private final ReentrantReadWriteLock lock;

	public ReadWriteExecutor() {
		this.lock = new ReentrantReadWriteLock();
	}

	public void execute(LockMode lockMode, Runnable task) {
		LockUtils.lock(lockMode == LockMode.READ ? lock.readLock() : lock.writeLock(), task);
	}
	
	public void executeIO(LockMode lockMode, IORunnable task) throws IOException {
		LockUtils.lockIO(lockMode == LockMode.READ ? lock.readLock() : lock.writeLock(), task);
	}

	public <R> R execute(LockMode lockMode, Supplier<R> task) {
		return LockUtils.lock(lockMode == LockMode.READ ? lock.readLock() : lock.writeLock(), task);
	}

	public <R> R executeIO(LockMode lockMode, IOSupplier<R> task) throws IOException {
		return LockUtils.lockIO(lockMode == LockMode.READ ? lock.readLock() : lock.writeLock(), task);
	}
	
	public enum LockMode {
		READ,
		WRITE
	}
}