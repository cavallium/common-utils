package org.warp.commonutils.concurrency.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;
import org.jetbrains.annotations.NotNull;

public class BoundedExecutor {

	private final Executor executor;
	private final int maxQueueSize;
	private final Semaphore semaphore;
	private final StampedLock drainAllLock = new StampedLock();

	public BoundedExecutor(Executor executor, int maxQueueSize) {
		this.executor = executor;
		this.maxQueueSize = maxQueueSize > 0 ? maxQueueSize : Integer.MAX_VALUE;
		this.semaphore = new Semaphore(maxQueueSize);
	}

	public void executeButBlockIfFull(@NotNull Runnable command) throws RejectedExecutionException, InterruptedException {
		var drainAllLockRead = drainAllLock.readLockInterruptibly();
		semaphore.acquire();
		try {
			executor.execute(() -> {
				try {
					semaphore.release();
					command.run();
				} finally {
					drainAllLock.unlockRead(drainAllLockRead);
				}
			});
		} catch (RejectedExecutionException | NullPointerException ex) {
			drainAllLock.unlockRead(drainAllLockRead);
			throw ex;
		}
	}

	public void drainAll(DrainAllMethodLambda runnableWhenDrained) throws InterruptedException {
		var drainAllWriteLock = drainAllLock.writeLockInterruptibly();
		try {
			runnableWhenDrained.run();
		} finally {
			drainAllLock.unlockWrite(drainAllWriteLock);
		}
	}

	public interface DrainAllMethodLambda {
		void run() throws InterruptedException;
	}
}
