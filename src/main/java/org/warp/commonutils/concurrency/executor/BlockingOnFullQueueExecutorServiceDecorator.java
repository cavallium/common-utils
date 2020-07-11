package org.warp.commonutils.concurrency.executor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockingOnFullQueueExecutorServiceDecorator implements BoundedExecutorService {

	private static final class PermitReleasingRunnableDecorator implements Runnable {

		@Nonnull
		private final Runnable delegate;

		@Nonnull
		private final Semaphore semaphore;

		private PermitReleasingRunnableDecorator(@Nonnull final Runnable task, @Nonnull final Semaphore semaphoreToRelease) {
			this.delegate = task;
			this.semaphore = semaphoreToRelease;
		}

		@Override
		public void run() {
			try {
				this.delegate.run();
			}
			finally {
				// however execution goes, release permit for next task
				this.semaphore.release();
			}
		}

		@Override
		public final String toString() {
			return String.format("%s[delegate='%s']", getClass().getSimpleName(), this.delegate);
		}
	}

	private static final class PermitReleasingCallableDecorator<T> implements Callable<T> {

		@Nonnull
		private final Callable<T> delegate;

		@Nonnull
		private final Semaphore semaphore;

		private PermitReleasingCallableDecorator(@Nonnull final Callable<T> task, @Nonnull final Semaphore semaphoreToRelease) {
			this.delegate = task;
			this.semaphore = semaphoreToRelease;
		}

		@Override
		public T call() throws Exception {
			try {
				return this.delegate.call();
			} finally {
				// however execution goes, release permit for next task
				this.semaphore.release();
			}
		}

		@Override
		public final String toString() {
			return String.format("%s[delegate='%s']", getClass().getSimpleName(), this.delegate);
		}
	}

	private volatile boolean ignoreTaskLimit;

	@Nonnull
	private final Semaphore taskLimit;

	@Nonnull
	private final Duration timeout;

	private final int maximumTaskNumber;

	@Nonnull
	private final Supplier<Integer> queueSizeSupplier;

	private final @Nullable BiConsumer<Boolean, Integer> queueSizeStatus;

	@Nonnull
	private final Object queueSizeStatusLock;

	@Nonnull
	private final ExecutorService delegate;

	public BlockingOnFullQueueExecutorServiceDecorator(@Nonnull final ExecutorService executor, final int maximumTaskNumber, @Nonnull final Duration maximumTimeout, @Nonnull Supplier<Integer> queueSizeSupplier, @Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		this.delegate = Objects.requireNonNull(executor, "'executor' must not be null");
		if (maximumTaskNumber < 1) {
			throw new IllegalArgumentException(String.format("At least one task must be permitted, not '%d'", maximumTaskNumber));
		}
		this.timeout = Objects.requireNonNull(maximumTimeout, "'maximumTimeout' must not be null");
		if (this.timeout.isNegative()) {
			throw new IllegalArgumentException("'maximumTimeout' must not be negative");
		}
		this.maximumTaskNumber = maximumTaskNumber;
		this.queueSizeSupplier = queueSizeSupplier;
		this.queueSizeStatus = queueSizeStatus;
		this.queueSizeStatusLock = new Object();
		this.taskLimit = new Semaphore(maximumTaskNumber);
	}

	private void preExecute(Object command) {
		Objects.requireNonNull(command, "'command' must not be null");

		var queueSize = queueSizeSupplier.get();
		synchronized (queueSizeStatusLock) {
			if (queueSizeStatus != null) queueSizeStatus.accept(queueSize >= maximumTaskNumber, queueSize);
		}

		if (!ignoreTaskLimit) {
			try {
				if (this.taskLimit.availablePermits() == 0) {
					synchronized (queueSizeStatusLock) {
						if (queueSizeStatus != null)
							queueSizeStatus.accept(true,
									maximumTaskNumber + (taskLimit.hasQueuedThreads() ? taskLimit.getQueueLength() : 0)
							);
					}
				}
				// attempt to acquire permit for task execution
				if (!this.taskLimit.tryAcquire(this.timeout.toMillis(), MILLISECONDS)) {
					throw new RejectedExecutionException(String.format("Executor '%s' busy", this.delegate));
				}
			} catch (final InterruptedException e) {
				// restore interrupt status
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	public final void execute(final Runnable command) {
		preExecute(command);

		this.delegate.execute(new PermitReleasingRunnableDecorator(command, this.taskLimit));
	}


	@Override
	public void shutdown() {
		this.ignoreTaskLimit = true;
		while (this.taskLimit.hasQueuedThreads()) {
			this.taskLimit.release(10);
		}
		this.delegate.shutdown();
	}

	@NotNull
	@Override
	public List<Runnable> shutdownNow() {
		this.ignoreTaskLimit = true;
		while (this.taskLimit.hasQueuedThreads()) {
			this.taskLimit.release(10);
		}
		return this.delegate.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return this.delegate.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return this.delegate.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		return this.delegate.awaitTermination(timeout, unit);
	}

	@NotNull
	@Override
	public <T> Future<T> submit(@NotNull Callable<T> task) {
		preExecute(task);

		return this.delegate.submit(new PermitReleasingCallableDecorator<>(task, this.taskLimit));
	}

	@NotNull
	@Override
	public <T> Future<T> submit(@NotNull Runnable task, T result) {
		preExecute(task);

		return this.delegate.submit(new PermitReleasingRunnableDecorator(task, this.taskLimit), result);
	}

	@NotNull
	@Override
	public Future<?> submit(@NotNull Runnable task) {
		preExecute(task);

		return this.delegate.submit(new PermitReleasingRunnableDecorator(task, this.taskLimit));
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
		throw new UnsupportedOperationException("invokeAll(tasks) is not supported");
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks,
			long timeout,
			@NotNull TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException("invokeAll(tasks, timeout, unit) is not supported");
	}

	@NotNull
	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException("invokeAny(tasks) is not supported");
	}

	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("invokeAny(tasks, timeout, unit) is not supported");
	}

	@Override
	public final String toString() {
		return String.format("%s[availablePermits='%s',timeout='%s',delegate='%s']", getClass().getSimpleName(), this.taskLimit.availablePermits(),
				this.timeout, this.delegate);
	}
}