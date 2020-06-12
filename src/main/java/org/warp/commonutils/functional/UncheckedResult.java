package org.warp.commonutils.functional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UncheckedResult {

	@Nullable
	private final Exception e;

	public UncheckedResult(@NotNull Exception e) {
		this.e = e;
	}

	public UncheckedResult() {
		this.e = null;
	}

	public <T extends Exception> UncheckedResult throwException(@NotNull Class<T> exceptionClass) throws T {
		if (e != null) {
			if (exceptionClass.isInstance(e)) {
				throw (T) e;
			}
		}
		return this;
	}

	public void done() {
		if (e != null) {
			throw new RuntimeException(e);
		}
	}
}
