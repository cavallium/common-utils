package org.warp.commonutils.concurrency.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

class BoundedExecutorServiceImpl extends ThreadPoolExecutor implements BoundedExecutorService {

	private final Semaphore semaphore;
	private final @Nullable BiConsumer<Boolean, Integer> queueSizeStatus;
	private final int maxQueueSize;
	private final Object queueSizeStatusLock = new Object();

	/**
	 *
	 * @param maxQueueSize
	 * @param corePoolSize
	 * @param maxPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param queueSizeStatus Status. The boolean indicates if the queue is full, the integer indicates the current queue size
	 */
	public BoundedExecutorServiceImpl(int maxQueueSize,
	                       int corePoolSize,
	                       int maxPoolSize,
	                       long keepAliveTime,
	                       TimeUnit unit,
	                       ThreadFactory threadFactory,
	                       @Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		super(corePoolSize, maxPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(), threadFactory);
		if (maxQueueSize < 0) {
			throw new IllegalArgumentException();
		}
		this.maxQueueSize = maxQueueSize;
		this.queueSizeStatus = queueSizeStatus;
		semaphore = new Semaphore(maxQueueSize);
	}

	/**
	 * Submits task to execution pool, but blocks while number of running threads
	 * has reached the bound limit
	 */
	@Override
	public <T> Future<T> submitButBlockIfFull(final Callable<T> task) throws InterruptedException {
		return this.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		preChecks();
		return super.submit(task);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		preChecks();
		return super.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		preChecks();
		return super.submit(task, result);
	}

	/**
	 * Submits task to execution pool, but blocks while number of running threads
	 * has reached the bound limit
	 */
	@Override
	public void executeButBlockIfFull(final Runnable task) throws InterruptedException {
		this.execute(task);
	}

	@Override
	public void execute(Runnable command) {
		preChecks();
		super.execute(command);
	}

	private void preChecks() {
		try {
			blockIfFull();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		var queueSize = getQueue().size();
		synchronized (queueSizeStatusLock) {
			if (queueSizeStatus != null) queueSizeStatus.accept(queueSize >= maxQueueSize, queueSize);
		}

		semaphore.release();

		super.beforeExecute(t, r);
	}

	private void blockIfFull() throws InterruptedException {
		if (semaphore.availablePermits() == 0) {
			synchronized (queueSizeStatusLock) {
				if (queueSizeStatus != null) queueSizeStatus.accept(true, maxQueueSize + (semaphore.hasQueuedThreads() ? semaphore.getQueueLength() : 0));
			}
		}
		semaphore.acquire();
	}
}