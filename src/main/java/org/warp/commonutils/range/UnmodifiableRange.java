package org.warp.commonutils.range;

import java.util.Objects;
import java.util.StringJoiner;
import org.warp.commonutils.error.IndexOutOfBoundsException;

public class UnmodifiableRange {

	private final long start;
	private final long end;

	public UnmodifiableRange(long start, long end) {
		if (start > end) {
			throw new IndexOutOfBoundsException(start, 0, end);
		}
		this.start = start;
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		UnmodifiableRange that = (UnmodifiableRange) o;
		return start == that.start && end == that.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", UnmodifiableRange.class.getSimpleName() + "[", "]").add("start=" + start)
				.add("end=" + end).toString();
	}

	public Range toRange() {
		return new Range(start, end);
	}
}
