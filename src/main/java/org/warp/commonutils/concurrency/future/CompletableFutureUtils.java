package org.warp.commonutils.concurrency.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CompletableFutureUtils {

	/**
	 * Aggregate multiple {@link CompletableFuture} lists into a single {@link CompletableFuture} list
	 *
	 * @param futureLists A collection of {@link CompletableFuture} lists.
	 * @param <T>         List elements type
	 * @return {@link CompletableFuture} list
	 */
	public static <T> CompletableFuture<List<T>> aggregate(Collection<CompletableFuture<List<T>>> futureLists) {
		final CompletableFuture<List<T>> identityAggregatedResult = CompletableFuture.completedFuture(new ArrayList<T>());

		return futureLists.parallelStream().reduce(identityAggregatedResult, (currentAggregatedResult, futureList) -> {
			return currentAggregatedResult.thenApplyAsync((aggregatedList) -> {
				aggregatedList.addAll(futureList.join());
				return aggregatedList;
			});
		});
	}

	/**
	 * Creates a new empty collection of disaggregated future results future lists
	 */
	public static <T> Collection<CompletableFuture<List<CompletableFuture<T>>>> createDisaggregatedResultsList() {
		return new ArrayList<>(10);
	}

	/**
	 * Add a
	 * @param disaggregatedResults
	 * @param result
	 * @param <T>
	 */
	public static <T> void addDisaggregated(
			Collection<CompletableFuture<List<CompletableFuture<T>>>> disaggregatedResults,
			CompletableFuture<List<CompletableFuture<T>>> result) {
		disaggregatedResults.add(result);
	}

	/**
	 * Add a result
	 */
	public static <T, U extends T> void addDisaggregatedCast(
			Collection<CompletableFuture<List<CompletableFuture<T>>>> disaggregatedResults,
			CompletableFuture<List<CompletableFuture<U>>> result) {
		addDisaggregatedCastForced(disaggregatedResults, result);
	}

	public static <T, U> void addDisaggregatedCastForced(
			Collection<CompletableFuture<List<CompletableFuture<T>>>> disaggregatedResults,
			CompletableFuture<List<CompletableFuture<U>>> result) {
		disaggregatedResults.add(result.thenApply((originalList) -> {
			List<CompletableFuture<T>> resultList = new ArrayList<>();
			for (CompletableFuture<U> originalFuture : originalList) {
				resultList.add(originalFuture.thenApply((originalValue) -> {
					//noinspection unchecked
					return (T) originalValue;
				}));
			}
			return resultList;
		}));
	}

	public static <T> Set<T> collectToSet(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().parallelStream().map(CompletableFuture::join).collect(Collectors.toSet());
	}

	public static <T> Set<T> collectToSet(CompletableFuture<List<CompletableFuture<T>>> futureList, int limit) {
		return futureList.join().parallelStream().map(CompletableFuture::join).limit(10).collect(Collectors.toSet());
	}

	public static <T> List<T> collectToList(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().stream().map(CompletableFuture::join).collect(Collectors.toList());
	}

	public static <T> List<T> collectToList(CompletableFuture<List<CompletableFuture<T>>> futureList, int limit) {
		return futureList.join().stream().map(CompletableFuture::join).limit(limit).collect(Collectors.toList());
	}

	public static <T> LinkedHashSet<T> collectToLinkedSet(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().stream().map(CompletableFuture::join).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static <T> LinkedHashSet<T> collectToLinkedSet(CompletableFuture<List<CompletableFuture<T>>> futureList,
			int limit) {
		return futureList.join().stream().map(CompletableFuture::join).limit(limit)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static <T> TreeSet<T> collectToTreeSet(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().stream().map(CompletableFuture::join).collect(Collectors.toCollection(TreeSet::new));
	}

	public static <T> TreeSet<T> collectToTreeSet(CompletableFuture<List<CompletableFuture<T>>> futureList, int limit) {
		return futureList.join().stream().map(CompletableFuture::join).limit(limit)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public static <T> TreeSet<T> collectToTreeSet(CompletableFuture<List<CompletableFuture<T>>> futureList, Comparator<T> comparator) {
		return futureList.join().stream().map(CompletableFuture::join).collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
	}

	public static <T> TreeSet<T> collectToTreeSet(CompletableFuture<List<CompletableFuture<T>>> futureList, Comparator<T> comparator, int limit) {
		return futureList.join().stream().map(CompletableFuture::join).limit(limit)
				.collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
	}

	public static <T> Optional<T> anyOrNull(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().parallelStream().map(CompletableFuture::join).findAny();
	}

	public static <T> Optional<T> firstOrNull(CompletableFuture<List<CompletableFuture<T>>> futureList) {
		return futureList.join().stream().map(CompletableFuture::join).findFirst();
	}

	public static <T> void forEachOrdered(CompletableFuture<List<CompletableFuture<T>>> futureList,
			Consumer<T> consumer) {
		forEachOrdered(futureList, consumer, false);
	}

	public static <T> void forEachOrdered(CompletableFuture<List<CompletableFuture<T>>> futureList,
			Consumer<T> consumer, boolean reverse) {
		var futures = futureList.join();
		if (reverse) {
			Collections.reverse(futures);
		}
		futures.stream().map(CompletableFuture::join).forEachOrdered(consumer);
	}

	public static <T> void forEach(CompletableFuture<List<CompletableFuture<T>>> futureList, Consumer<T> consumer) {
		futureList.join().parallelStream().map(CompletableFuture::join).forEach(consumer);
	}

	public static <T> CompletableFuture<T> catchUncheckedExceptions(Supplier<CompletableFuture<T>> supplier) {
		try {
			return supplier.get();
		} catch (Exception exception) {
			return CompletableFuture.failedFuture(exception);
		}
	}

	public static CompletableFuture<Void> runSequence(Collection<CompletableFuture<?>> collection) {
		if (collection.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		} else {
			var result = new CompletableFuture<Void>();
			for (CompletableFuture<?> completableFuture : collection) {
				result = result.thenCompose(x -> completableFuture.thenRun(() -> {}));
			}
			return result;
		}
	}

	public static CompletableFuture<Void> runSequenceAsync(Collection<CompletableFuture<?>> collection, ExecutorService executorService) {
		var result = CompletableFuture.<Void>completedFuture(null);
		for (CompletableFuture<?> completableFuture : collection) {
			result = result.thenComposeAsync(x -> completableFuture.thenRun(() -> {}), executorService);
		}
		return result;
	}

	public static <T> CompletableFuture<T> applySequenceAsync(T initialValue, Collection<Function<T, CompletableFuture<T>>> collection, ExecutorService executorService) {
		var result = CompletableFuture.completedFuture(initialValue);
		for (Function<T, CompletableFuture<T>> item : collection) {
			result = result.thenComposeAsync(item, executorService);
		}
		return result;
	}
}
