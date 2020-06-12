package org.warp.commonutils.error;

public class IndexOutOfBoundsException extends RuntimeException {

	public IndexOutOfBoundsException() {
	}

	public IndexOutOfBoundsException(String s) {
		super(s);
	}

	public IndexOutOfBoundsException(long index) {
		super("Index out of range: " + index);
	}

	public IndexOutOfBoundsException(long index, long min, long max) {
		super("Index " + index + " out of range (from " + min + " to " + max + ")");
	}
}
