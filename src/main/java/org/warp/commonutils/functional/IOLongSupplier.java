package org.warp.commonutils.functional;

import java.io.IOException;

public interface IOLongSupplier {

	long get() throws IOException;
}
