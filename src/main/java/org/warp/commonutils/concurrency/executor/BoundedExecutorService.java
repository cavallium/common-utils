package org.warp.commonutils.concurrency.executor;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

public interface BoundedExecutorService extends ExecutorService {

	@Deprecated
	static BoundedExecutorService create(int maxQueueSize,
			int corePoolSize,
			long keepAliveTime,
			TimeUnit unit,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		return create(maxQueueSize, corePoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueSizeStatus);
	}

	static BoundedExecutorService create(int maxQueueSize,
			int corePoolSize,
			long keepAliveTime,
			TimeUnit unit,
			ThreadFactory threadFactory,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		var threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
				corePoolSize,
				keepAliveTime,
				unit,
				new LinkedBlockingQueue<>(),
				threadFactory
		);
		return create(maxQueueSize, corePoolSize, keepAliveTime, unit, threadFactory, Duration.ofDays(1000000), queueSizeStatus);
	}

	static BoundedExecutorService create(int maxQueueSize,
			int corePoolSize,
			long keepAliveTime,
			TimeUnit unit,
			ThreadFactory threadFactory,
			Duration queueItemTtl,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		var queue = new LinkedBlockingQueue<Runnable>();
		var threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
				corePoolSize,
				keepAliveTime,
				unit,
				queue,
				threadFactory
		);
		return new BlockingOnFullQueueExecutorServiceDecorator(threadPoolExecutor,
				maxQueueSize,
				queueItemTtl,
				queue::size,
				queueSizeStatus
		);
	}

	@Deprecated
	default void executeButBlockIfFull(Runnable task) throws InterruptedException {
		this.execute(task);
	}
}
