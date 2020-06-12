package org.warp.commonutils.metrics;

public interface AtomicDetailedTimeAbsoluteSamplesSnapshot<T> extends AtomicTimeAbsoluteSamplesSnapshot {

	double getAveragePerSecond(T detail, long timeRange);

	long getCurrentCount(T detail);

	double getTotalAveragePerSecond(T detail);
}
