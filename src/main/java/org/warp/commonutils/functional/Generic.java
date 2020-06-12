package org.warp.commonutils.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import org.warp.commonutils.functional.Unchecked.UncheckedConsumer;

public class Generic {
	public static <T, U> Function<T, U> function(Function<Object, U> fnc) {
		return (Function<T, U>) fnc;

	}
	public static <T> Consumer<T> consumer(Consumer<Object> fnc) {
		return (Consumer<T>) fnc;
	}

	public static <T> UncheckedConsumer<T> consumerExc(UncheckedConsumer<Object> fnc) {
		return (UncheckedConsumer<T>) fnc;
	}
}
