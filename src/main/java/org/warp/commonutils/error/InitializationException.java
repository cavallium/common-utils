package org.warp.commonutils.error;

import java.io.IOException;

public class InitializationException extends IOException {
	public InitializationException() {
		super();
	}

	public InitializationException(String text) {
		super(text);
	}

	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitializationException(Throwable cause) {
		super(cause);
	}
}
