package org.warp.commonutils.functional;

import java.io.IOException;

public interface IOConsumer<T> {

	void consume(T value) throws IOException;
}
