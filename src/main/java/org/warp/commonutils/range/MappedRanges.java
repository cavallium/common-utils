package org.warp.commonutils.range;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Comparator;
import java.util.function.Function;

public class MappedRanges<T> {

	private final Object2ObjectMap<Range, T> ranges;

	public MappedRanges(int start, int end, T value) {
		if (start > end) {
			throw new IndexOutOfBoundsException();
		}
		this.ranges = new Object2ObjectOpenHashMap<>();
		ranges.put(new Range(start, end), value);
	}

	public void deleteRange(final int start, final int end, Function<T, T> replaceWhenSplitting, Function<T, T> cloneWhenSplitting) {
		if (start > end) {
			throw new IndexOutOfBoundsException();
		}
		Object2ObjectOpenHashMap<Range, T> rangesToAdd = new Object2ObjectOpenHashMap<>();
		ObjectOpenHashSet<Range> rangesToDelete = new ObjectOpenHashSet<>();
		ranges.forEach((range, value) -> {
			if (range.start <= end && range.end >= start) {
				if (range.start >= start && range.end <= end) {
					// delete the range
					rangesToDelete.add(range);
				} else if (range.start <= start && range.end >= end) {
					// cut the hole
					rangesToDelete.add(range);
					rangesToAdd.put(new Range(range.start, start), value);
					rangesToAdd.put(new Range(end, range.end), cloneWhenSplitting.apply(value));
				} else if (range.start <= start && range.end <= end && range.end > start) {
					// shrink the right border
					rangesToDelete.add(range);
					rangesToAdd.put(new Range(range.start, start), value);
				} else if (range.start >= start && range.end >= end && range.start < end) {
					// shrink the left border
					rangesToDelete.add(range);
					rangesToAdd.put(new Range(end, range.end), value);
				}
			}
		});
		for (Range range : rangesToDelete) {
			ranges.remove(range);
		}
		rangesToAdd.forEach((range, value) -> {
			if (canAddRange(range)) {
				ranges.put(range, replaceWhenSplitting.apply(value));
			}
		});
	}

	public void transformRange(int start, int end, Function<T, T> replaceWhenOverlapping, Function<T, T> cloneWhenSplitting) {
		if (start > end) {
			throw new IndexOutOfBoundsException();
		}
		Object2ObjectOpenHashMap<Range, T> rangesToTransform = new Object2ObjectOpenHashMap<>();
		Object2ObjectOpenHashMap<Range, T> rangesToAdd = new Object2ObjectOpenHashMap<>();
		ObjectOpenHashSet<Range> rangesToRemove = new ObjectOpenHashSet<>();
		ranges.forEach((range, value) -> {
			if (range.start <= end && range.end >= start) {
				if (range.start >= start && range.end <= end) {
					// transform the range
					rangesToTransform.put(range, replaceWhenOverlapping.apply(value));
				} else if (range.start <= start && range.end >= end) {
					// transform the middle
					rangesToRemove.add(range);
					rangesToAdd.put(new Range(range.start, start), value);
					rangesToTransform.put(new Range(start, end), replaceWhenOverlapping.apply(cloneWhenSplitting.apply(value)));
					rangesToAdd.put(new Range(end, range.end), cloneWhenSplitting.apply(value));
				} else if (range.start <= start && range.end <= end && range.end > start) {
					// transform the right
					rangesToRemove.add(range);
					rangesToAdd.put(new Range(range.start, start), value);
					rangesToTransform.put(new Range(start, range.end), replaceWhenOverlapping.apply(cloneWhenSplitting.apply(value)));
				} else if (range.start >= start && range.end >= end && range.start < end) {
					// transform the left
					rangesToRemove.add(range);
					rangesToTransform.put(new Range(range.start, end), replaceWhenOverlapping.apply(cloneWhenSplitting.apply(value)));
					rangesToAdd.put(new Range(end, range.end), value);
				} else {
					// do not transform
				}
			}
		});

		rangesToRemove.forEach((range) -> {
			ranges.remove(range);
		});
		rangesToAdd.forEach((range, value) -> {
			if (canAddRange(range)) {
				ranges.put(range, value);
			}
		});
		rangesToTransform.forEach((range, value) -> {
			ranges.put(range, value);
		});
	}

	private boolean canAddRange(UnmodifiableRange range) {
		return range.getStart() != range.getEnd();
	}

	private boolean canAddRange(Range range) {
		return range.getStart() != range.getEnd();
	}

	public Object2ObjectMap<UnmodifiableRange, T> getRanges() {
		Object2ObjectSortedMap<UnmodifiableRange, T> a = new Object2ObjectRBTreeMap<>(Comparator.comparingLong(UnmodifiableRange::getStart));
		ranges.forEach((range, value) -> a.put(range.unmodifiableClone(), value));
		return Object2ObjectMaps.unmodifiable(a);
	}
}
