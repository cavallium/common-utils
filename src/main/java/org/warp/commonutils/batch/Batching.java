package org.warp.commonutils.batch;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Batching<T> {

	private final int pingRefreshTimeMillis;
	private volatile double singleItemTransferTimeMillis;
	private volatile double latencyMillis;
	private final AtomicBoolean enablePacking = new AtomicBoolean(false);
	private final ConcurrentLinkedQueue<ExecutorService> executors = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean closeRequested = new AtomicBoolean(false);
	private final ReentrantLock waitingAccesLock = new ReentrantLock();
	private final ConcurrentLinkedQueue<T> waitingPutItems = new ConcurrentLinkedQueue<>();
	private final AtomicDouble lostTimeMillis = new AtomicDouble(0d);
	private final AtomicDouble sentItems = new AtomicDouble(0);
	private final double startTimeMillis = ((double) System.nanoTime()) / 1000000d;

	public Batching(int pingRefreshTimeMillis) {
		this.pingRefreshTimeMillis = pingRefreshTimeMillis;
		refreshPing();

		if (enablePacking.get()) {
			ExecutorService executor = Executors.newFixedThreadPool(2);
			this.executors.offer(executor);
			executor.execute(this::pingRefreshExecutor);
			executor.execute(new BatchSender());
		}
	}

	private void pingRefreshExecutor() {
		boolean closeReq = false;
		while (!(closeReq = closeRequested.get())) {
			try {
				Thread.sleep(pingRefreshTimeMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			refreshPing();
		}
	}

	private void refreshPing() {
		double pingTime = ping();
		this.latencyMillis = 0.9 * pingTime;
		this.singleItemTransferTimeMillis = 0.1 * pingTime;
		this.enablePacking.compareAndSet(false, latencyMillis > 0.1d);
	}

	public void offer(T action) {
		if (enablePacking.get()) {
			sentItems.addAndGet(1d);
			waitingAccesLock.lock();
			try {
				waitingPutItems.offer(action);
			} finally {
				waitingAccesLock.unlock();
			}
		} else {
			executeDirect(action);
		}
	}

	public void offer(Collection<T> actions) {
		if (enablePacking.get()) {
			sentItems.addAndGet(actions.size());
			waitingAccesLock.lock();
			try {
				for (T action : actions) {
					waitingPutItems.offer(action);
				}
			} finally {
				waitingAccesLock.unlock();
			}
		} else {
			executeDirect(actions);
		}
	}

	public void offer(T... actions) {
		offer(List.of(actions));
	}

	protected abstract void executeBatch(Collection<T> actions);

	protected void executeBatch(T action) {
		executeBatch(List.of(action));
	}

	protected abstract void executeDirect(T action);

	protected abstract void executeDirect(Collection<T> action);

	protected abstract double ping();

	public abstract void close();


	private static final double getItemSendLongestTime(double lostTime, double latencyMillis, double waitingSize,
			double singleItemTransferTimeMillis) {
		return lostTime + latencyMillis + waitingSize * singleItemTransferTimeMillis;
	}

	private static final double getItemSendLongestTimeNext(double lostTime, double latencyMillis, double waitTime,
			double waitingSize, double singleItemTransferTimeMillis, double itemsPerMillisecondIdeal) {
		return lostTime + latencyMillis + waitTime + (waitingSize
				+ (waitTime * itemsPerMillisecondIdeal) * singleItemTransferTimeMillis);
	}

	private static final double getItemsPerSecond(double waitingSize, double itemSendLongestTime) {
		return waitingSize / notZero(itemSendLongestTime);
	}

	private static final double getAverageItemTime(double waitingSize, double itemSendLongestTime) {
		return itemSendLongestTime / notZero(waitingSize);
	}

	private static final double getNextItemsPerSecond(double waitingSize, double nextItemSendLongestTime, double waitTime,
			double itemsPerMillisecondIdeal) {
		return (waitingSize + (waitTime * itemsPerMillisecondIdeal)) / notZero(nextItemSendLongestTime);
	}

	private static final double getNextAverageItemTime(double waitingSize, double nextItemSendLongestTime,
			double waitTime, double itemsPerMillisecondIdeal) {
		return nextItemSendLongestTime / notZero((waitingSize + (waitTime * itemsPerMillisecondIdeal)));
	}

	private static final double notZero(double input) {
		if (input != 0) {
			return input;
		} else {
			return input + 0.000000000000000000001d;
		}
	}

	private class BatchSender implements Runnable {

		@Override
		public void run() {
			boolean closeReq;
			while ((!(closeReq = closeRequested.get())) || !waitingPutItems.isEmpty()) {
				double waitTimeMillis = latencyMillis;
				long waitTimeNanoMillis = (long) Math.floor(latencyMillis);
				int waitTimeNanos = (int) ((waitTimeMillis - ((double) waitTimeNanoMillis)) * 1000000d);
				try {
					if (!closeReq) {
						Thread.sleep(waitTimeNanoMillis, waitTimeNanos);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				waitingAccesLock.lock();
				try {
					if (!waitingPutItems.isEmpty()) {
						int waitingSize = waitingPutItems.size();
						double lostTime = lostTimeMillis.addAndGet(waitTimeMillis); // Get the lost time as the time
						// in the middle
						double idealItemsPerMillis =
								sentItems.get() / notZero(((double) System.nanoTime()) / 1000000d - startTimeMillis);
						double idealMillisPerItem = 1d / notZero(idealItemsPerMillis);
						double itemSendLongestTime = getItemSendLongestTime(lostTime, latencyMillis, waitingSize,
								singleItemTransferTimeMillis);
						double itemsPerSecond = getItemsPerSecond(waitingSize, itemSendLongestTime);
						double averageItemTime = getAverageItemTime(waitingSize, itemSendLongestTime);
						double nextItemSendLongestTime = getItemSendLongestTimeNext(lostTime, latencyMillis, waitTimeMillis,
								waitingSize, singleItemTransferTimeMillis, idealItemsPerMillis);
						double nextItemsPerSecond = getNextItemsPerSecond(waitingSize, nextItemSendLongestTime, waitTimeMillis,
								idealItemsPerMillis);
						double nextAverageItemTime = getNextAverageItemTime(waitingSize, itemSendLongestTime, waitTimeMillis,
								idealItemsPerMillis);
						boolean do1 = idealMillisPerItem > latencyMillis;
						boolean do2 = itemsPerSecond > nextItemsPerSecond;
						boolean do3 = averageItemTime - nextAverageItemTime < latencyMillis;
						boolean do4 = averageItemTime > 5;
						boolean doThisTurn = do1 | do2 | do3 | do4 || closeReq;

						if (doThisTurn) {
							lostTimeMillis.set(0);
							if (waitingSize > 1) {
								executeBatch(waitingPutItems);
							} else {
								T pair = waitingPutItems.poll();
								executeBatch(pair);
							}
							if ((System.nanoTime() % 100) < 1) {
								System.out.printf("LATENCY=%.2f; WAITED=%.2f; PACKET_SIZE=%.2f; AVG_ITEM_TIME=%.2f; "
												+ "NEXT_AVG_ITEM_TIME=%.2f; DO=%s,%s,%s\n", latencyMillis, lostTime, (double) waitingSize,
										averageItemTime, nextAverageItemTime, "" + do1, "" + do2, "" + do3);
								System.out.printf("idealMillisPerItem=%.2f; itemsPerSecond=%.2f; nextItemsPerSecond=%"
												+ ".2f; averageItemTime-nextAverageItemTime=%.2f\n", idealItemsPerMillis, itemsPerSecond,
										nextItemsPerSecond, averageItemTime - nextAverageItemTime);
							}
							waitingPutItems.clear();
						} else {
							if ((System.nanoTime() % 100) < 1) {
								System.out.println("SKIPPED TURN");
							}
						}
					}
				} finally {
					waitingAccesLock.unlock();
				}
			}
		}
	}
}
