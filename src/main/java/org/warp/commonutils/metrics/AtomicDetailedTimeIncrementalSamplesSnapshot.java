package org.warp.commonutils.metrics;

import java.util.Set;

public interface AtomicDetailedTimeIncrementalSamplesSnapshot<T> extends AtomicTimeIncrementalSamplesSnapshot {

	Set<T> getDetails();

	double getAveragePerSecond(T detail, long timeRange);

	long getApproximateCount(T detail, long timeRange);

	long getTotalCount(T detail);

	double getTotalAverage(T detail);
}
