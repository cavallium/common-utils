package org.warp.commonutils.metrics;

public interface AtomicTimeAbsoluteSamplesSnapshot {

	double getAveragePerSecond(long timeRange);

	long getCurrentCount();

	double getTotalAveragePerSecond();
}
