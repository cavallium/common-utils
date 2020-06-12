package org.warp.commonutils.batch;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.warp.commonutils.concurrency.executor.BoundedExecutorService;
import org.warp.commonutils.functional.TriConsumer;
import org.warp.commonutils.type.IntWrapper;
import org.warp.commonutils.type.ShortNamedThreadFactory;
import org.warp.commonutils.type.VariableWrapper;

public class ParallelUtils {

	public static <K, V> void parallelize(Consumer<BiConsumer<K, V>> iterator,
			int maxQueueSize,
			int parallelism,
			int groupSize,
			BiConsumer<K, V> consumer) {
		BoundedExecutorService parallelExecutor = BoundedExecutorService.create(maxQueueSize, parallelism, parallelism * 2, 0, TimeUnit.MILLISECONDS, new ShortNamedThreadFactory("ForEachParallel"), (a, b) -> {});
		final int CHUNK_SIZE = groupSize;
		IntWrapper count = new IntWrapper(CHUNK_SIZE);
		VariableWrapper<Object[]> keys = new VariableWrapper<>(new Object[CHUNK_SIZE]);
		VariableWrapper<Object[]> values = new VariableWrapper<>(new Object[CHUNK_SIZE]);
		iterator.accept((key, value) -> {
			keys.var[CHUNK_SIZE - count.var] = key;
			values.var[CHUNK_SIZE - count.var] = value;
			count.var--;
			if (count.var == 0) {
				count.var = CHUNK_SIZE;
				Object[] keysCopy = keys.var;
				Object[] valuesCopy = values.var;
				keys.var = new Object[CHUNK_SIZE];
				values.var = new Object[CHUNK_SIZE];
				try {
					parallelExecutor.executeButBlockIfFull(() -> {
						for (int i = 0; i < CHUNK_SIZE; i++) {
							//noinspection unchecked
							consumer.accept((K) keysCopy[i], (V) valuesCopy[i]);
						}
					});
				} catch (InterruptedException e) {
					throw new CompletionException(e);
				}
			}
		});
		parallelExecutor.shutdown();
		try {
			parallelExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Parallel forEach interrupted", e);
		}
	}

	public static <K1, K2, V> void parallelize(Consumer<TriConsumer<K1, K2, V>> iterator,
			int maxQueueSize,
			int parallelism,
			int groupSize,
			TriConsumer<K1, K2, V> consumer) {
		BoundedExecutorService parallelExecutor = BoundedExecutorService.create(maxQueueSize, parallelism, parallelism * 2, 0, TimeUnit.MILLISECONDS, new ShortNamedThreadFactory("ForEachParallel"), (a, b) -> {});
		final int CHUNK_SIZE = groupSize;
		IntWrapper count = new IntWrapper(CHUNK_SIZE);
		VariableWrapper<Object[]> keys1 = new VariableWrapper<>(new Object[CHUNK_SIZE]);
		VariableWrapper<Object[]> keys2 = new VariableWrapper<>(new Object[CHUNK_SIZE]);
		VariableWrapper<Object[]> values = new VariableWrapper<>(new Object[CHUNK_SIZE]);
		iterator.accept((key1, key2, value) -> {
			keys1.var[CHUNK_SIZE - count.var] = key1;
			keys2.var[CHUNK_SIZE - count.var] = key2;
			values.var[CHUNK_SIZE - count.var] = value;
			count.var--;
			if (count.var == 0) {
				count.var = CHUNK_SIZE;
				Object[] keys1Copy = keys1.var;
				Object[] keys2Copy = keys2.var;
				Object[] valuesCopy = values.var;
				keys1.var = new Object[CHUNK_SIZE];
				keys2.var = new Object[CHUNK_SIZE];
				values.var = new Object[CHUNK_SIZE];
				try {
					parallelExecutor.executeButBlockIfFull(() -> {
						for (int i = 0; i < CHUNK_SIZE; i++) {
							//noinspection unchecked
							consumer.accept((K1) keys1Copy[i], (K2) keys2Copy[i], (V) valuesCopy[i]);
						}
					});
				} catch (InterruptedException e) {
					throw new CompletionException(e);
				}
			}
		});
		parallelExecutor.shutdown();
		try {
			parallelExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Parallel forEach interrupted", e);
		}
	}
}
