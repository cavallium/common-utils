package org.warp.commonutils.functional;

import java.io.IOException;

public interface IOBooleanSupplier {

	boolean get() throws IOException;
}
