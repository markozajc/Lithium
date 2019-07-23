package com.github.markozajc.lithium.processes;

import java.util.concurrent.Callable;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.processes.context.ProcessContext;

/**
 * A representation of a process. {@link LithiumProcess}es are managed by a
 * {@link ProcessManager}. Each {@link LithiumProcess} has a {@link ProcessContext}
 * which can be used to determine various things
 *
 * @author Marko Zajc
 * @param <T>
 *            The return type of this process.
 */
public abstract class LithiumProcess<T> implements Callable<T> {

	private final ProcessContext context;

	/**
	 * Creates a new {@link LithiumProcess}.
	 *
	 * @param context
	 *            The {@link ProcessContext} of this {@link LithiumProcess}.
	 */
	public LithiumProcess(ProcessContext context) {
		this.context = context;
	}

	/**
	 * @return The {@link Lithium} instance of this {@link LithiumProcess}.
	 */
	public ProcessContext getContext() {
		return this.context;
	}

}
