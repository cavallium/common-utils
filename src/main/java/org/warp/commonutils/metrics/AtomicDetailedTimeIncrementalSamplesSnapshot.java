package org.warp.commonutils.metrics;

public interface AtomicDetailedTimeIncrementalSamplesSnapshot<T> extends AtomicTimeIncrementalSamplesSnapshot {

	double getAveragePerSecond(T detail, long timeRange);

	long getApproximateCount(T detail, long timeRange);

	long getTotalCount(T detail);

	double getTotalAverage(T detail);
}
