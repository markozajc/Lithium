package com.github.markozajc.lithium.commands.ratelimits;

import java.util.HashMap;

public class Ratelimits {

	private long waitMilis;
	private HashMap<Object, Long> registered;

	/**
	 * Creates a new ratelimit checker
	 * 
	 * @param seconds
	 *            ratelimit time in seconds
	 */
	public Ratelimits(int seconds) {
		this.registered = new HashMap<>();
		this.waitMilis = seconds * 1000L;
	}

	/**
	 * Creates a new ratelimit checker
	 */
	public Ratelimits() {
		this.registered = new HashMap<>();
		this.waitMilis = 0;
	}

	/**
	 * Registers a new ratelimit waiter
	 * 
	 * @param o
	 *            identifier
	 */
	public void register(Object o) {
		this.registered.put(o, System.currentTimeMillis());
	}

	/**
	 * Checks the remaining ratelimit time of an identifier
	 * 
	 * @param o
	 *            identifier
	 * @return remaining time in milliseconds or -1 if ratelimit has ended or this
	 *         identifier hasn't been registered
	 */
	public long check(Object o) {
		if (!this.registered.containsKey(o)) {
			return -1;
		}

		long remaining = (this.registered.get(o) + this.waitMilis) - System.currentTimeMillis();

		return remaining > 0 ? remaining : -1;
	}

	/**
	 * Retrieves ratelimit waiting time in seconds
	 * 
	 * @return ratelimit waiting time in seconds
	 */
	public long getWaitingSeconds() {
		return this.waitMilis / 1000;
	}

	/**
	 * Sets a new ratelimit waiting time in seconds
	 * 
	 * @param waitingSeconds
	 *            new ratelimit waiting time in seconds
	 * @return self, for chain methods.
	 */
	public Ratelimits setWaitingSeconds(long waitingSeconds) {
		this.waitMilis = waitingSeconds * 1000;

		return this;
	}

}
