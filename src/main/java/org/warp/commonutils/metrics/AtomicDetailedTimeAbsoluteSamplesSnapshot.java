package org.warp.commonutils.metrics;

import java.util.Set;

public interface AtomicDetailedTimeAbsoluteSamplesSnapshot<T> extends AtomicTimeAbsoluteSamplesSnapshot {

	Set<T> getDetails();

	double getAveragePerSecond(T detail, long timeRange);

	long getCurrentCount(T detail);

	double getTotalAveragePerSecond(T detail);
}
