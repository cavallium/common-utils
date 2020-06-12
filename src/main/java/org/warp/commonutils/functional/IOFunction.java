package org.warp.commonutils.functional;

import java.io.IOException;

public interface IOFunction<T, U> {

	U run(T data) throws IOException;
}
