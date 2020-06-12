package org.warp.commonutils.functional.org.warp.commonutils.locks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.warp.commonutils.locks.LeftRightLock;

public class LeftRightLockTest {

	int logLineSequenceNumber = 0;
	private LeftRightLock sut = new LeftRightLock();

	@Timeout(2000)
	@Test()
	public void acquiringLeftLockExcludeAcquiringRightLock() throws Exception {
		sut.lockLeft();


		Future<Boolean> task = Executors.newSingleThreadExecutor().submit(() -> sut.tryLockRight());
		assertFalse(task.get(), "I shouldn't be able to acquire the RIGHT lock!");
	}

	@Timeout(2000)
	@Test()
	public void acquiringRightLockExcludeAcquiringLeftLock() throws Exception {
		sut.lockRight();
		Future<Boolean> task = Executors.newSingleThreadExecutor().submit(() -> sut.tryLockLeft());
		assertFalse(task.get(), "I shouldn't be able to acquire the LEFT lock!");
	}

	@Timeout(2000)
	@Test()
	public void theLockShouldBeReentrant() throws Exception {
		sut.lockLeft();
		assertTrue(sut.tryLockLeft());
	}

	@Timeout(2000)
	@Test()
	public void multipleThreadShouldBeAbleToAcquireTheSameLock_Right() throws Exception {
		sut.lockRight();
		Future<Boolean> task = Executors.newSingleThreadExecutor().submit(() -> sut.tryLockRight());
		assertTrue(task.get());
	}

	@Timeout(2000)
	@Test()
	public void multipleThreadShouldBeAbleToAcquireTheSameLock_left() throws Exception {
		sut.lockLeft();
		Future<Boolean> task = Executors.newSingleThreadExecutor().submit(() -> sut.tryLockLeft());
		assertTrue(task.get());
	}

	@Timeout(2000)
	@Test()
	public void shouldKeepCountOfAllTheThreadsHoldingTheSide() throws Exception {

		CountDownLatch latchA = new CountDownLatch(1);
		CountDownLatch latchB = new CountDownLatch(1);


		Thread threadA = spawnThreadToAcquireLeftLock(latchA, sut);
		Thread threadB = spawnThreadToAcquireLeftLock(latchB, sut);

		System.out.println("Both threads have acquired the left lock.");

		try {
			latchA.countDown();
			threadA.join();
			boolean acqStatus = sut.tryLockRight();
			System.out.println("The right lock was " + (acqStatus ? "" : "not") + " acquired");
			assertFalse(acqStatus, "There is still a thread holding the left lock. This shouldn't succeed.");
		} finally {
			latchB.countDown();
			threadB.join();
		}

	}

	@Timeout(2000)
	@Test()
	public void shouldBlockThreadsTryingToAcquireLeftIfRightIsHeld() throws Exception {
		sut.lockLeft();

		CountDownLatch taskStartedLatch = new CountDownLatch(1);

		final Future<Boolean> task = Executors.newSingleThreadExecutor().submit(() -> {
			taskStartedLatch.countDown();
			sut.lockRight();
			return false;
		});

		taskStartedLatch.await();
		Thread.sleep(100);

		assertFalse(task.isDone());
	}

	@Test
	public void shouldBeFreeAfterRelease() throws Exception {
		sut.lockLeft();
		sut.releaseLeft();
		assertTrue(sut.tryLockRight());
	}

	@Test
	public void shouldBeFreeAfterMultipleThreadsReleaseIt() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);

		final Thread thread1 = spawnThreadToAcquireLeftLock(latch, sut);
		final Thread thread2 = spawnThreadToAcquireLeftLock(latch, sut);

		latch.countDown();

		thread1.join();
		thread2.join();

		assertTrue(sut.tryLockRight());

	}

	@Timeout(2000)
	@Test()
	public void lockShouldBeReleasedIfNoThreadIsHoldingIt() throws Exception {
		CountDownLatch releaseLeftLatch = new CountDownLatch(1);
		CountDownLatch rightLockTaskIsRunning = new CountDownLatch(1);

		Thread leftLockThread1 = spawnThreadToAcquireLeftLock(releaseLeftLatch, sut);
		Thread leftLockThread2 = spawnThreadToAcquireLeftLock(releaseLeftLatch, sut);

		Future<Boolean> acquireRightLockTask = Executors.newSingleThreadExecutor().submit(() -> {
			if (sut.tryLockRight())
				throw new AssertionError("The left lock should be still held, I shouldn't be able to acquire right a this point.");
			printSynchronously("Going to be blocked on right lock");
			rightLockTaskIsRunning.countDown();
			sut.lockRight();
			printSynchronously("Lock acquired!");
			return true;
		});

		rightLockTaskIsRunning.await();

		releaseLeftLatch.countDown();
		leftLockThread1.join();
		leftLockThread2.join();

		assertTrue(acquireRightLockTask.get());
	}

	private synchronized void printSynchronously(String str) {

		System.out.println(logLineSequenceNumber++ + ")" + str);
		System.out.flush();
	}

	private Thread spawnThreadToAcquireLeftLock(CountDownLatch releaseLockLatch, LeftRightLock lock) throws InterruptedException {
		CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
		final Thread thread = spawnThreadToAcquireLeftLock(releaseLockLatch, lockAcquiredLatch, lock);
		lockAcquiredLatch.await();
		return thread;
	}

	private Thread spawnThreadToAcquireLeftLock(CountDownLatch releaseLockLatch, CountDownLatch lockAcquiredLatch, LeftRightLock lock) {
		final Thread thread = new Thread(() -> {
			lock.lockLeft();
			printSynchronously("Thread " + Thread.currentThread() + " Acquired left lock");
			try {
				lockAcquiredLatch.countDown();
				releaseLockLatch.await();
			} catch (InterruptedException ignore) {
			} finally {
				lock.releaseLeft();
			}

			printSynchronously("Thread " + Thread.currentThread() + " RELEASED left lock");
		});
		thread.start();
		return thread;
	}
}