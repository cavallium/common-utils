package org.warp.commonutils.range;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.warp.commonutils.error.IndexOutOfBoundsException;

public class Ranges {

	private final ObjectArrayList<Range> ranges;

	public Ranges(long start, long end) {
		if (start > end) {
			throw new IndexOutOfBoundsException(start, 0, end);
		}
		this.ranges = new ObjectArrayList<>();
		ranges.add(new Range(start, end));
	}

	public void addRange(Range range) {
		addRange(range.start, range.end);
	}

	public void addRange(long start, long end) {
		if (start > end) {
			throw new IndexOutOfBoundsException(start, 0, end);
		}
		long rangeStart = start;
		long rangeEnd = end;
		var it = ranges.iterator();
		while (it.hasNext()) {
			Range range = it.next();
			if (range.start <= end && range.end >= start) {
				boolean remove = false;
				if (range.start < rangeStart && range.end >= rangeStart) {
					rangeStart = range.start;
					remove = true;
				}
				if (range.end > rangeEnd && range.start <= rangeEnd) {
					rangeEnd = range.end;
					remove = true;
				}
				if (remove) {
					it.remove();
				}
			}
		}
		addRangeIfNotZero(new Range(rangeStart, rangeEnd));
	}

	public void deleteRange(final long start, final long end) {
		if (start > end) {
			throw new IndexOutOfBoundsException(start);
		}
		List<Range> rangesToAdd = new ArrayList<>(ranges.size());
		var it = ranges.iterator();
		while (it.hasNext()) {
			Range range = it.next();
			if (range.start <= end && range.end >= start) {
				if (range.start >= start && range.end <= end) {
					// delete the range
					it.remove();
				} else if (range.start <= start && range.end >= end) {
					// cut the hole
					it.remove();
					rangesToAdd.add(new Range(range.start, start));
					rangesToAdd.add(new Range(end, range.end));
				} else if (range.start <= start && range.end <= end && range.end > start) {
					// shrink the right border
					it.remove();
					rangesToAdd.add(new Range(range.start, start));
				} else if (range.start >= start && range.end >= end && range.start < end) {
					// shrink the left border
					it.remove();
					rangesToAdd.add(new Range(end, range.end));
				}
			}
		}
		for (Range rangeToAdd : rangesToAdd) {
			addRangeIfNotZero(rangeToAdd);
		}
	}

	/**
	 * This methods does not check overlapping ranges! It's used only internally to skip empty ranges
	 *
	 * @param range
	 */
	private void addRangeIfNotZero(Range range) {
		if (range.start != range.end) {
			ranges.add(range);
		}
	}

	public ObjectSortedSet<UnmodifiableRange> getRanges() {
		ObjectSortedSet<UnmodifiableRange> a = new ObjectRBTreeSet<>(Comparator.comparingLong(UnmodifiableRange::getStart));
		ranges.forEach((range) -> a.add(range.unmodifiableClone()));
		return ObjectSortedSets.unmodifiable(a);
	}
}
