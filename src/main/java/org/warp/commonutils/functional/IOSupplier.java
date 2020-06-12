package org.warp.commonutils.functional;

import java.io.IOException;

public interface IOSupplier<T> {

	T get() throws IOException;
}
