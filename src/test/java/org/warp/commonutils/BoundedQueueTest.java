package org.warp.commonutils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.warp.commonutils.concurrency.executor.BoundedExecutorService;
import org.warp.commonutils.type.ShortNamedThreadFactory;

public class BoundedQueueTest {

	@Test
	public void testBoundedQueue() throws InterruptedException {
		int maxQueueSize = 2;
		AtomicInteger queueSize = new AtomicInteger();
		AtomicReference<AssertionFailedError> failedError = new AtomicReference<>();
		var executor = BoundedExecutorService.create(maxQueueSize,
				1,
				1,
				0L,
				TimeUnit.MILLISECONDS,
				new ShortNamedThreadFactory("test"),
				(isQueueFull, currentQueueSize) -> {
					try {
						if (currentQueueSize >= maxQueueSize) {
							Assertions.assertTrue(isQueueFull);
						} else {
							Assertions.assertFalse(isQueueFull);
						}
					} catch (AssertionFailedError ex) {
						if (failedError.get() == null) {
							failedError.set(ex);
						}
						ex.printStackTrace();
					}
				}
		);

		for (int i = 0; i < 10000; i++) {
			queueSize.incrementAndGet();
			executor.executeButBlockIfFull(queueSize::decrementAndGet);
		}

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		Assertions.assertNull(failedError.get());
	}
}
