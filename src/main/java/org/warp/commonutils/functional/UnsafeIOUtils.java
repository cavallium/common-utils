package org.warp.commonutils.functional;

import java.io.IOError;
import java.io.IOException;
import org.warp.commonutils.functional.IOBooleanSupplier;
import org.warp.commonutils.functional.IOIntegerSupplier;
import org.warp.commonutils.functional.IOLongSupplier;
import org.warp.commonutils.functional.IORunnable;
import org.warp.commonutils.functional.IOSupplier;

public final class UnsafeIOUtils {

	public static <T> T unsafe(IOSupplier<T> expression) {
		try {
			return expression.get();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	public static int unsafe(IOIntegerSupplier expression) {
		try {
			return expression.get();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	public static boolean unsafe(IOBooleanSupplier expression) {
		try {
			return expression.get();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	public static long unsafe(IOLongSupplier expression) {
		try {
			return expression.get();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}

	public static void unsafe(IORunnable expression) {
		try {
			expression.run();
		} catch (IOException e) {
			throw new IOError(e);
		}
	}
}
