package org.warp.commonutils.concurrency.future;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SizedFutureList<T> {

	private final CompletableFuture<List<CompletableFuture<T>>> data;
	private final CompletableFuture<Integer> size;

	public SizedFutureList(CompletableFuture<List<CompletableFuture<T>>> data, CompletableFuture<Integer> size) {
		this.data = data;
		this.size = size;
	}

	public static <T> SizedFutureList<T> empty() {
		return new SizedFutureList<>(CompletableFuture.completedFuture(List.of()), CompletableFuture.completedFuture(0));
	}

	public CompletableFuture<List<CompletableFuture<T>>> getData() {
		return data;
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
		SizedFutureList<?> that = (SizedFutureList<?>) o;
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
