package org.warp.commonutils.concurrency.future;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.warp.commonutils.functional.IOSupplier;

public class FutureLockUtils {

	public static <T> CompletableFuture<T> readLock(@Nullable StampedLock lock, @NotNull Supplier<CompletableFuture<T>> r) {
		long lockValue;
		if (lock != null) {
			lockValue = lock.readLock();
		} else {
			lockValue = 0;
		}
		return r.get().whenComplete((x, y) -> {
			if (lock != null) {
				lock.unlockRead(lockValue);
			}
		});
	}

	public static <T> CompletableFuture<T> writeLock(@Nullable StampedLock lock, @NotNull Supplier<CompletableFuture<T>> r) {
		long lockValue;
		if (lock != null) {
			lockValue = lock.writeLock();
		} else {
			lockValue = 0;
		}
		return r.get().whenComplete((x, y) -> {
			if (lock != null) {
				lock.unlockWrite(lockValue);
			}
		});
	}

	public static <T> CompletableFuture<T> readLockIO(@Nullable StampedLock lock, @NotNull IOSupplier<CompletableFuture<T>> r) throws IOException {
		long lockValue;
		if (lock != null) {
			lockValue = lock.readLock();
		} else {
			lockValue = 0;
		}
		return r.get().whenComplete((x, y) -> {
			if (lock != null) {
				lock.unlockRead(lockValue);
			}
		});
	}

	public static <T> CompletableFuture<T> writeLockIO(@Nullable StampedLock lock, @NotNull IOSupplier<CompletableFuture<T>> r) throws IOException {
		long lockValue;
		if (lock != null) {
			lockValue = lock.writeLock();
		} else {
			lockValue = 0;
		}
		return r.get().whenComplete((x, y) -> {
			if (lock != null) {
				lock.unlockWrite(lockValue);
			}
		});
	}
}
