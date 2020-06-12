package org.warp.commonutils.metrics;

import java.util.Arrays;
import java.util.HashMap;

public class AtomicDetailedTimeIncrementalSamples<T> extends AtomicTimeIncrementalSamples implements
		AtomicDetailedTimeIncrementalSamplesSnapshot<T> {

	private HashMap<T, AtomicTimeIncrementalSamples> detailedAtomicTimeSamples = new HashMap<>();

	/**
	 * @param sampleTime   in milliseconds
	 * @param samplesCount
	 */
	public AtomicDetailedTimeIncrementalSamples(int sampleTime, int samplesCount) {
		super(sampleTime, samplesCount);
	}

	public AtomicDetailedTimeIncrementalSamples(long startTime, long[] samples, int sampleTime, long currentSampleStartTime, long totalEvents,
			HashMap<T, AtomicTimeIncrementalSamplesSnapshot> detailedAtomicTimeSamples) {
		super(startTime, samples, sampleTime, currentSampleStartTime, totalEvents);
		this.detailedAtomicTimeSamples = new HashMap<>();
		detailedAtomicTimeSamples.forEach((detail, sample) -> detailedAtomicTimeSamples.put(detail, (AtomicTimeIncrementalSamples) sample));
	}

	private synchronized AtomicTimeIncrementalSamples getDetailed(T detail) {
		AtomicTimeIncrementalSamples detailed = detailedAtomicTimeSamples.get(detail);
		if (detailed == null) {
			detailed = new AtomicTimeIncrementalSamples(sampleTime, samples.length);
			detailedAtomicTimeSamples.put(detail, detailed);
		}
		return detailed;
	}

	public synchronized void increment(T detail, long count) {
		updateSamples();
		getDetailed(detail).increment(count);
		increment(count);
	}

	@Override
	public synchronized double getAveragePerSecond(T detail, long timeRange) {
		updateSamples();
		return getDetailed(detail).getAveragePerSecond(timeRange);
	}

	@Override
	public synchronized long getApproximateCount(T detail, long timeRange) {
		updateSamples();
		return getDetailed(detail).getApproximateCount(timeRange);
	}

	@Override
	public synchronized long getTotalCount(T detail) {
		updateSamples();
		return getDetailed(detail).getTotalCount();
	}

	@Override
	public synchronized double getTotalAverage(T detail) {
		updateSamples();
		return getDetailed(detail).getTotalAveragePerSecond();
	}

	public synchronized AtomicTimeIncrementalSamplesSnapshot snapshot(T detail) {
		return getDetailed(detail).snapshot();
	}

	@Override
	protected synchronized void shiftSamples(int shiftCount) {
		//detailedAtomicTimeSamples.values().forEach(AtomicTimeSamples::shiftSamples);
		super.shiftSamples(shiftCount);
	}

	public synchronized AtomicDetailedTimeIncrementalSamples<T> snapshot() {
		var clonedDetailedAtomicTimeSamples = new HashMap<T, AtomicTimeIncrementalSamplesSnapshot>(detailedAtomicTimeSamples);
		clonedDetailedAtomicTimeSamples.replaceAll((key, value) -> ((AtomicTimeIncrementalSamples) value).snapshot());
		return new AtomicDetailedTimeIncrementalSamples<>(startTime, Arrays.copyOf(this.samples, this.samples.length), sampleTime,
				currentSampleStartTime, totalEvents, clonedDetailedAtomicTimeSamples);
	}
}
