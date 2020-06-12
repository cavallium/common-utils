package org.warp.commonutils.concurrency.future;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SizedFutureSet<T> {

	private final CompletableFuture<List<CompletableFuture<T>>> data;
	private final CompletableFuture<Integer> size;

	public SizedFutureSet(CompletableFuture<List<CompletableFuture<T>>> data, CompletableFuture<Integer> size) {
		this.data = data;
		this.size = size;
	}

	public static <T> SizedFutureSet<T> empty() {
		return new SizedFutureSet<>(CompletableFuture.completedFuture(List.of()), CompletableFuture.completedFuture(0));
	}

	public CompletableFuture<LinkedHashSet<CompletableFuture<T>>> getFutureDataOrdered() {
		return data.thenApply(LinkedHashSet::new);
	}

	public CompletableFuture<Set<CompletableFuture<T>>> getFutureDataUnordered() {
		return data.thenApply(HashSet::new);
	}

	public LinkedHashSet<T> getDataOrdered() {
		return CompletableFutureUtils.collectToLinkedSet(data);
	}

	public Set<T> getDataUnordered() {
		return CompletableFutureUtils.collectToSet(data);
	}

	public CompletableFuture<Integer> getSize() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SizedFutureSet<?> that = (SizedFutureSet<?>) o;
		return Objects.equals(data, that.data) && Objects.equals(size, that.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, size);
	}

	@Override
	public String toString() {
		return "SizedFutureList{" + "data=" + data + ", size=" + size + '}';
	}
}
