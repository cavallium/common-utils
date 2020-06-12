package org.warp.commonutils.locks;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.warp.commonutils.functional.IORunnable;
import org.warp.commonutils.functional.IOSupplier;

public class LockUtils {

	public static void lock(@Nullable Lock lock, @NotNull Runnable r) {
		if (lock != null) {
			lock.lock();
		}
		try {
			r.run();
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public static void lock(@Nullable LeftRightLock lock, boolean right, @NotNull Runnable r) {
		if (lock != null) {
			if (right) {
				lock.lockRight();
			} else {
				lock.lockLeft();
			}
		}
		try {
			r.run();
		} finally {
			if (lock != null) {
				if (right) {
					lock.releaseRight();
				} else {
					lock.releaseLeft();
				}
			}
		}
	}

	public static void lockIO(@Nullable Lock lock, @NotNull IORunnable r) throws IOException {
		if (lock != null) {
			lock.lock();
		}
		try {
			r.run();
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public static void lockIO(@Nullable LeftRightLock lock, boolean right, @NotNull IORunnable r) throws IOException {
		if (lock != null) {
			if (right) {
				lock.lockRight();
			} else {
				lock.lockLeft();
			}
		}
		try {
			r.run();
		} finally {
			if (lock != null) {
				if (right) {
					lock.releaseRight();
				} else {
					lock.releaseLeft();
				}
			}
		}
	}

	public static <T> T lock(@Nullable Lock lock, @NotNull Supplier<T> r) {
		if (lock != null) {
			lock.lock();
		}
		try {
			return r.get();
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public static <T> T lock(@Nullable LeftRightLock lock, boolean right, @NotNull Supplier<T> r) {
		if (lock != null) {
			if (right) {
				lock.lockRight();
			} else {
				lock.lockLeft();
			}
		}
		try {
			return r.get();
		} finally {
			if (lock != null) {
				if (right) {
					lock.releaseRight();
				} else {
					lock.releaseLeft();
				}
			}
		}
	}

	public static <T> T lockIO(@Nullable Lock lock, @NotNull IOSupplier<T> r) throws IOException {
		if (lock != null) {
			lock.lock();
		}
		try {
			return r.get();
		} finally {
			if (lock != null) {
				lock.unlock();
			}
		}
	}

	public static <T> T lockIO(@Nullable LeftRightLock lock, boolean right, @NotNull IOSupplier<T> r) throws IOException {
		if (lock != null) {
			if (right) {
				lock.lockRight();
			} else {
				lock.lockLeft();
			}
		}
		try {
			return r.get();
		} finally {
			if (lock != null) {
				if (right) {
					lock.releaseRight();
				} else {
					lock.releaseLeft();
				}
			}
		}
	}
}
