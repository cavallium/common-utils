package org.warp.commonutils.range;

import java.util.Objects;
import java.util.StringJoiner;
import org.warp.commonutils.error.IndexOutOfBoundsException;

public class Range {

	public long start;
	public long end;

	public Range(long start, long end) {
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
		Range range = (Range) o;
		return start == range.start && end == range.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Range.class.getSimpleName() + "[", "]").add("start=" + start).add("end=" + end)
				.toString();
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public Range clone() {
		return new Range(start, end);
	}

	public UnmodifiableRange unmodifiableClone() {
		return new UnmodifiableRange(start, end);
	}
}
