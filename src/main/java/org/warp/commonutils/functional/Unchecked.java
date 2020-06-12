package org.warp.commonutils.functional;

import java.util.function.Function;

public class Unchecked<T> implements Function<T, UncheckedResult> {

	private final UncheckedConsumer<T> uncheckedConsumer;

	public Unchecked(UncheckedConsumer<T> uncheckedConsumer) {
		this.uncheckedConsumer = uncheckedConsumer;
	}

	public static <T> Unchecked<T> wrap(UncheckedConsumer<T> uncheckedConsumer) {
		return new Unchecked<>(uncheckedConsumer);
	}

	@Override
	public UncheckedResult apply(T t) {
		try {
			uncheckedConsumer.consume(t);
			return new UncheckedResult();
		} catch (Exception e) {
			return new UncheckedResult(e);
		}
	}

	public interface UncheckedConsumer<T> {
		public void consume(T value) throws Exception;
	}
}
