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
			int maxPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		return create(maxQueueSize, corePoolSize, maxPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueSizeStatus);
	}

	static BoundedExecutorService create(int maxQueueSize,
			int corePoolSize,
			int maxPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			ThreadFactory threadFactory,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		var threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
				maxPoolSize,
				keepAliveTime,
				unit,
				new LinkedBlockingQueue<>(),
				threadFactory
		);
		return new BlockingOnFullQueueExecutorServiceDecorator(threadPoolExecutor, maxPoolSize, Duration.ofDays(1000000));
	}

	static BoundedExecutorService create(int maxQueueSize,
			int corePoolSize,
			int maxPoolSize,
			long keepAliveTime,
			TimeUnit unit,
			ThreadFactory threadFactory,
			Duration queueItemTtl,
			@Nullable BiConsumer<Boolean, Integer> queueSizeStatus) {
		var threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
				maxPoolSize,
				keepAliveTime,
				unit,
				new LinkedBlockingQueue<>(),
				threadFactory
		);
		return new BlockingOnFullQueueExecutorServiceDecorator(threadPoolExecutor, maxPoolSize, queueItemTtl);
	}

	@Deprecated
	default void executeButBlockIfFull(Runnable task) throws InterruptedException {
		this.execute(task);
	}
}
