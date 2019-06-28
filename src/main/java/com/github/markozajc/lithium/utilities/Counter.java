package com.github.markozajc.lithium.utilities;

/**
 * A class used as a counter in lambda clauses.
 *
 * @author Marko Zajc
 */
public class Counter {

	private int count;

	/**
	 * Creates a new simple counter with initial count of 0
	 */
	public Counter() {
		this(0);
	}

	/**
	 * Creates a new simple counter with initial count of {@code initialCount}.
	 *
	 * @param initialCount
	 *            initial count
	 */
	public Counter(int initialCount) {
		this.count = initialCount;
	}

	/**
	 * Counts up for one
	 */
	public void count() {
		this.count(1);
	}

	/**
	 * Counts up for a specified amount
	 *
	 * @param amount
	 *            the amount to increase the counter by
	 */
	public void count(int amount) {
		this.count += amount;
	}

	/**
	 * Resets the counter to 0
	 */
	public void reset() {
		this.count = 0;
	}

	/**
	 * Gets the count
	 *
	 * @return count
	 */
	public int getCount() {
		return this.count;
	}

}
