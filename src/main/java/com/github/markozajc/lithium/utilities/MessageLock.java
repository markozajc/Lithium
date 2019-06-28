package com.github.markozajc.lithium.utilities;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.markozajc.lithium.commands.exceptions.runtime.TimeoutException;

/**
 * An implementation of CountDownLatch able to transport values
 *
 * @author Marko Zajc
 *
 * @param <M>
 *            message (value) type
 */
public class MessageLock<M> {

	/**
	 * Indicates that the part that would otherwise send a message has encountered an
	 * error that requires it to resort to using
	 * {@link MessageLock#throwException(Exception)}.
	 *
	 * @author Marko Zajc
	 */
	public static class SenderException extends RuntimeException {

		SenderException(Throwable cause) {
			super(cause);
		}

	}

	private final Object lock;
	private boolean sent = false;

	private M message;
	private Exception exception;

	/**
	 * Creates a new instance if MessageLatch. After {@link #send(Object)} has been
	 * called, this object can not be reused.
	 */
	public MessageLock() {
		this.lock = new Object();
	}

	/**
	 * Sends the message.
	 *
	 * @param message
	 */
	public void send(M message) {
		this.message = message;

		synchronized (this.lock) {
			this.lock.notifyAll();
			this.sent = true;
		}
	}

	/**
	 * Throws a new exception, that is wrapped into a {@link SenderException} and passed
	 * onto the {@link #receive()} part.
	 *
	 * @param e
	 */
	public void throwException(Exception e) {
		this.exception = e;

		synchronized (this.lock) {
			this.lock.notifyAll();
			this.sent = true;
		}
	}

	/**
	 * Awaits {@link #send(Object)} to be called or timeout to expire.
	 *
	 * @param timeout
	 *            timeout
	 * @param unit
	 *            timeout time unit
	 * @return message provided in {@link #send(Object)}
	 * @throws ExecutionException
	 *             if the sender has encountered a problem and has resorted to
	 *             {@link #throwException(Exception)}
	 * @throws TimeoutException
	 *             if the time ran out
	 * @throws IllegalArgumentException
	 *             if timeout is less than 0
	 */
	public M receive(int timeout, TimeUnit unit) throws ExecutionException {
		if (timeout < 0)
			throw new IllegalArgumentException("Timeout can't be less than 0!");

		long timeoutMillis;
		long targetMillis;

		if (timeout == 0) {
			timeoutMillis = -1;
			targetMillis = -1;

		} else {
			timeoutMillis = unit.toMillis(timeout);
			targetMillis = timeoutMillis == 0 ? -1 : System.currentTimeMillis() + timeoutMillis - 50;

		}

		if (this.sent || this.message != null)
			return this.message;

		synchronized (this.lock) {
			try {
				if (targetMillis < 0) {
					while (!this.sent)
						this.lock.wait();
				} else {
					while (System.currentTimeMillis() < targetMillis && !this.sent)
						this.lock.wait(timeoutMillis);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		if (!this.sent)
			throw new TimeoutException();
		// If the message was actually sent before the timeout

		if (this.exception != null)
			throw new ExecutionException(this.exception);
		// If an exception has occurred

		return this.message;
	}

	/**
	 * Awaits {@link #send(Object)} to be called.
	 *
	 * @return message provided in <code>notify()</code>
	 * @throws ExecutionException
	 *             if the sender has encountered a problem and has resorted to
	 *             {@link #throwException(Exception)}
	 */
	public M receive() throws ExecutionException {
		return this.receive(0, TimeUnit.MILLISECONDS);
	}

}
