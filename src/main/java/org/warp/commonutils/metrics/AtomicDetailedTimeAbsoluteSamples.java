package org.warp.commonutils.metrics;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class AtomicDetailedTimeAbsoluteSamples<T> implements AtomicDetailedTimeAbsoluteSamplesSnapshot<T> {

	private final boolean isSnapshot;
	private final int sampleTime;
	private final int samplesCount;
	private Object2ObjectMap<T, AtomicTimeAbsoluteSamples> detailedAtomicTimeSamples = new Object2ObjectOpenHashMap<>();

	/**
	 * @param sampleTime   in milliseconds
	 * @param samplesCount
	 */
	public AtomicDetailedTimeAbsoluteSamples(int sampleTime, int samplesCount) {
		this.sampleTime = sampleTime;
		this.samplesCount = samplesCount;
		this.isSnapshot = false;
	}

	public AtomicDetailedTimeAbsoluteSamples(int sampleTime, int samplesCount, HashMap<T, AtomicTimeAbsoluteSamplesSnapshot> detailedAtomicTimeSamples, boolean isSnapshot) {
		this.sampleTime = sampleTime;
		this.samplesCount = samplesCount;
		this.detailedAtomicTimeSamples = new Object2ObjectOpenHashMap<>();
		detailedAtomicTimeSamples.forEach((detail, sample) -> this.detailedAtomicTimeSamples.put(detail, (AtomicTimeAbsoluteSamples) sample));
		this.isSnapshot = isSnapshot;
	}

	private synchronized void updateSamples() {

	}

	private synchronized AtomicTimeAbsoluteSamples getDetailed(T detail) {
		AtomicTimeAbsoluteSamples detailed = detailedAtomicTimeSamples.get(detail);
		if (detailed == null) {
			detailed = new AtomicTimeAbsoluteSamples(sampleTime, samplesCount);
			detailedAtomicTimeSamples.put(detail, detailed);
		}
		return detailed;
	}

	public synchronized void set(T detail, long count) {
		updateSamples();
		getDetailed(detail).set(count);
	}

	@Override
	public synchronized Set<T> getDetails() {
		return Collections.unmodifiableSet(new ObjectOpenHashSet<>(detailedAtomicTimeSamples.keySet()));
	}

	@Override
	public synchronized double getAveragePerSecond(T detail, long timeRange) {
		updateSamples();
		return getDetailed(detail).getAveragePerSecond(timeRange);
	}

	@Override
	public synchronized double getAveragePerSecond(long timeRange) {
		updateSamples();
		return detailedAtomicTimeSamples.values().stream().mapToDouble((detail) -> detail.getAveragePerSecond(timeRange)).sum();
	}

	@Override
	public synchronized long getCurrentCount(T detail) {
		updateSamples();
		return getDetailed(detail).getCurrentCount();
	}

	@Override
	public synchronized long getCurrentCount() {
		updateSamples();
		return detailedAtomicTimeSamples.values().stream().mapToLong(AtomicTimeAbsoluteSamples::getCurrentCount).sum();
	}

	@Override
	public synchronized double getTotalAveragePerSecond() {
		updateSamples();
		return detailedAtomicTimeSamples.values().stream().mapToDouble(AtomicTimeAbsoluteSamples::getTotalAveragePerSecond).sum();
	}

	@Override
	public synchronized double getTotalAveragePerSecond(T detail) {
		updateSamples();
		return getDetailed(detail).getTotalAveragePerSecond();
	}

	public synchronized AtomicTimeAbsoluteSamplesSnapshot snapshot(T detail) {
		return getDetailed(detail).snapshot();
	}

	public synchronized AtomicDetailedTimeAbsoluteSamples<T> snapshot() {
		if (isSnapshot) {
			return this;
		}
		var clonedDetailedAtomicTimeSamples = new HashMap<T, AtomicTimeAbsoluteSamplesSnapshot>(detailedAtomicTimeSamples);
		clonedDetailedAtomicTimeSamples.replaceAll((key, value) -> ((AtomicTimeAbsoluteSamples) value).snapshot());
		return new AtomicDetailedTimeAbsoluteSamples<>(sampleTime,
				samplesCount, clonedDetailedAtomicTimeSamples, true);
	}
}
