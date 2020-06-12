package org.warp.commonutils.metrics;

public interface AtomicTimeIncrementalSamplesSnapshot {

	double getAveragePerSecond(long timeRange);

	long getApproximateCount(long timeRange);

	long getTotalCount();

	double getTotalAveragePerSecond();
}
