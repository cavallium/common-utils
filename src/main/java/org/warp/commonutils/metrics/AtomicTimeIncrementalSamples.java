package org.warp.commonutils.metrics;

import java.util.Arrays;

public class AtomicTimeIncrementalSamples implements AtomicTimeIncrementalSamplesSnapshot {

	protected final boolean isSnapshot;
	protected long startTime;
	protected final long[] samples;
	protected final int sampleTime;
	protected long currentSampleStartTime;
	protected long totalEvents;

	/**
	 *
	 * @param sampleTime in milliseconds
	 * @param samplesCount
	 */
	public AtomicTimeIncrementalSamples(int sampleTime, int samplesCount) {
		this.samples = new long[samplesCount];
		this.sampleTime = sampleTime;
		startTime = -1;
		if (samplesCount < 1) throw new IndexOutOfBoundsException();
		if (sampleTime < 1) throw new IndexOutOfBoundsException();
		this.isSnapshot = false;
	}

	public AtomicTimeIncrementalSamples(long startTime, long[] samples, int sampleTime, long currentSampleStartTime, long totalEvents, boolean isSnapshot) {
		this.startTime = startTime;
		this.samples = samples;
		this.sampleTime = sampleTime;
		this.currentSampleStartTime = currentSampleStartTime;
		this.totalEvents = totalEvents;
		this.isSnapshot = isSnapshot;
	}

	protected synchronized void updateSamples() {
		checkStarted();

		if (isSnapshot) {
			return;
		}

		long currentTime = System.nanoTime() / 1000000L;
		long timeDiff = currentTime - currentSampleStartTime;
		long timeToShift = timeDiff - (timeDiff % sampleTime);
		int shiftCount = (int) (timeToShift / sampleTime);
		if (currentTime - (currentSampleStartTime + timeToShift) > sampleTime) {
			throw new IndexOutOfBoundsException("Time sample bigger than " + sampleTime + "! It's " + (currentTime - (currentSampleStartTime + timeToShift)));
		}
		if (shiftCount > 0) {
			shiftSamples(shiftCount);
			currentSampleStartTime += timeToShift;
		}
	}

	protected synchronized void checkStarted() {
		if (startTime == -1) {
			this.startTime = System.nanoTime() / 1000000L;
			this.currentSampleStartTime = startTime;
		}
	}

	protected synchronized void shiftSamples(int shiftCount) {
		checkStarted();
		if (samples.length - shiftCount > 0) {
			System.arraycopy(samples, 0, samples, shiftCount, samples.length - shiftCount);
			Arrays.fill(samples, 0, shiftCount, 0);
		} else {
			Arrays.fill(samples, 0);
		}
	}

	public synchronized void increment(long count) {
		updateSamples();
		samples[0]+=count;
		totalEvents+=count;
	}

	@Override
	public synchronized double getAveragePerSecond(long timeRange) {
		updateSamples();

		long currentTime = System.nanoTime() / 1000000L;
		double preciseTimeRange = timeRange;
		// Fix if the time range is bigger than the collected data since start
		if (currentTime - preciseTimeRange < startTime) {
			preciseTimeRange = currentTime - startTime;
		}

		double samplesCount = Math.min(Math.max(preciseTimeRange / sampleTime, 1d), samples.length);
		double roundedTimeRange = samplesCount * sampleTime;
		double value = 0;
		for (int i = samplesCount == 1 ? 0 : 1; i < samplesCount; i++) {
			double sampleValue;
			if (i == 0) {
				sampleValue = samples[i] * sampleTime / (double) (currentTime - currentSampleStartTime);
			} else {
				 sampleValue = samples[i];
			}
			value += sampleValue;
		}
		return (value / roundedTimeRange) * 1000d;
	}


	@Override
	public synchronized long getApproximateCount(long timeRange) {
		updateSamples();
		long samplesCount = Math.min(Math.max(timeRange / sampleTime, 1L), samples.length);
		long value = 0;
		for (int i = 0; i < samplesCount; i++) {
			value += samples[i];
		}
		return value;
	}

	@Override
	public synchronized long getTotalCount() {
		updateSamples();
		return totalEvents;
	}

	@Override
	public synchronized double getTotalAveragePerSecond() {
		updateSamples();
		if (currentSampleStartTime == startTime) {
			return 0;
		}
		return ((double) totalEvents) / (double) ((currentSampleStartTime - startTime) / 1000D);
	}

	public synchronized AtomicTimeIncrementalSamplesSnapshot snapshot() {
		if (isSnapshot) {
			return this;
		}
		return new AtomicTimeIncrementalSamples(startTime, Arrays.copyOf(this.samples, this.samples.length), sampleTime, currentSampleStartTime, totalEvents, true);
	}
}
