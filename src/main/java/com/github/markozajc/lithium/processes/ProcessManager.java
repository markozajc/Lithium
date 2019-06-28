package com.github.markozajc.lithium.processes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.markozajc.lithium.tasks.Task;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A manager for {@link LithiumProcess}. Can be used to submit {@link LithiumProcess}
 * to the internal {@link ExecutorService} (using
 * {@link #submitProcess(LithiumProcess)}) or to run them as unbound processes (using
 * {@link #runUnboundProcess(LithiumProcess)}). A {@link ProcessManager} also keeps
 * tabs on the currently running processes, a {@link Set} which can easily be fetched
 * using {@link #getProcesses()}.
 *
 * @author Marko Zajc
 */
public class ProcessManager {

	private static final String UNBOUND_NAME = "lithium-unbound-process";
	private final ExecutorService executor;
	private final Set<LithiumProcess<?>> processes = Collections.synchronizedSet(new HashSet<>());

	public ProcessManager(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Submits a {@link LithiumProcess} to the {@link ExecutorService}.
	 *
	 * @param process
	 *            The {@link LithiumProcess} to submit.
	 * @return The {@link Future} of this process.
	 */
	@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE") // Exceptions are caught in the submit block and passed
																// to the CompletableFuture
	public <T> CompletableFuture<T> submitProcess(LithiumProcess<T> process) {
		this.processes.add(process);
		CompletableFuture<T> cf = new CompletableFuture<>();
		this.executor.submit(() -> {
			try {
				cf.complete(process.call());
			} catch (Exception e) {
				cf.completeExceptionally(e);
			}
			this.processes.remove(process);
		});
		return cf;
	}

	/**
	 * Runs an "unbound" {@link LithiumProcess}. Unbound processes run independently of
	 * the {@link ExecutorService}. Unbound processes use {@link Task}s in the
	 * background. All created {@link Task}s will be named
	 * {@code lithium-unbound-process}
	 *
	 * @param process
	 *            The {@link LithiumProcess} to run.
	 * @return The {@link Future} of this process.
	 */
	public <T> CompletableFuture<T> runUnboundProcess(LithiumProcess<T> process) {
		this.processes.add(process);
		CompletableFuture<T> cf = new CompletableFuture<>();
		new Task(() -> {
			this.processes.add(process);
			cf.complete(process.call());
			this.processes.remove(process);
		}, UNBOUND_NAME).execute();
		return cf;
	}

	/**
	 * A {@link Set} of currently running {@link LithiumProcess}es. Note that this does
	 * not include processes that are queued in this {@link ProcessManager}'s
	 * {@link ExecutorService}.
	 *
	 * @return A {@link Set} of running {@link LithiumProcess}es.
	 */
	public Set<LithiumProcess<?>> getProcesses() { // NOSONAR
		return Collections.unmodifiableSet(this.processes);
	}

	public ExecutorService getExecutorService() {
		return this.executor;
	}

}
