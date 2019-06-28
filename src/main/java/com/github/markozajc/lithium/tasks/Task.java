package com.github.markozajc.lithium.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.utilities.MessageLock;

/**
 * A representation of a task running in a {@link Thread} running independently of an
 * {@link Executor}.
 *
 * @author Marko Zajc
 */
public class Task {

	private static final String CHILDREN_INTERRUPTED = "Got interrupted while executing children.";
	private static final Logger LOG = LoggerFactory.getLogger(Task.class);

	private final String name;
	private final TaskAction taskAction;

	/**
	 * Creates a new {@link Task}.
	 *
	 * @param action
	 *            The {@link TaskAction} to take place.
	 * @param name
	 *            The name of this task.
	 */
	public Task(TaskAction action, String name) {
		this(action, name, new Task[0]);
	}

	/**
	 * Creates a new {@link Task} with children {@link Task}s.
	 *
	 * @param action
	 *            The {@link TaskAction} to take place.<gf
	 * @param name
	 *            The name of this task.
	 * @param children
	 *            The children {@link Task}s. Children {@link Task}s are executed
	 *            asynchronously after the parent {@link Task} completes. Children may
	 *            have children {@link Task}s too.
	 */
	public Task(TaskAction action, String name, Task... children) {
		this.name = name;
		Task[] childrenCopy = children.clone();

		this.taskAction = () -> {
			action.run();
			LOG.debug("Executed {}.", this.name);

			if (childrenCopy.length == 1) {
				executeChild(childrenCopy[0]);
			} else if (childrenCopy.length > 1) {
				executeChildren(childrenCopy);
			}
		};
	}

	/**
	 * Executes this {@link Task} in a new {@link Thread}.
	 *
	 * @return The {@link Future} of execution.
	 */
	public Future<Void> execute() {
		LOG.debug("Executing {}...", this.name);
		MessageLock<Void> lock = new MessageLock<>();
		Thread thread = new Thread(() -> {
			try {
				this.taskAction.run();
				lock.send(null);
			} catch (Exception e) {
				lock.throwException(e);
			}
		}, this.name);
		thread.setDaemon(true);
		thread.start();
		return new TaskFuture(thread, lock);
	}

	/**
	 * @return This task's name.
	 */
	public String getName() {
		return this.name;
	}

	private static void executeChild(Task child) throws ExecutionException {
		try {
			child.execute().get();
		} catch (InterruptedException e) {
			LOG.warn(CHILDREN_INTERRUPTED);
			Thread.currentThread().interrupt();
		}
	}

	private static void executeChildren(Task[] children) throws ExecutionException {
		List<Future<Void>> futures = new ArrayList<>(children.length);

		for (Task task : children)
			futures.add(task.execute());

		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				LOG.warn(CHILDREN_INTERRUPTED);
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * A {@link Future} implementation representing a future of a {@link Task}. Uses a
	 * {@link MessageLock} to determine {@link Exception}s and uses methods of
	 * {@link Thread} to determine completion.
	 *
	 * @author Marko Zajc
	 */
	public static class TaskFuture implements Future<Void> {

		private final Thread thread;
		private final MessageLock<Void> lock;

		TaskFuture(Thread thread, MessageLock<Void> lock) {
			this.thread = thread;
			this.lock = lock;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (isDone())
				return false;

			if (mayInterruptIfRunning) {
				this.thread.interrupt();
			} else {
				try {
					get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return true;
				} catch (ExecutionException e) {
					return false;
				}
			}

			return true;
		}

		@Override
		public boolean isCancelled() {
			return this.thread.isInterrupted();
		}

		@Override
		public boolean isDone() {
			return !this.thread.isAlive();
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			return this.lock.receive();
		}

		@Override
		public Void get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
			return this.lock.receive();
		}
	}

	/**
	 * A functional interface representing an action of a {@link Task}.
	 *
	 * @author Marko Zajc
	 */
	@FunctionalInterface
	public static interface TaskAction {

		/**
		 * Runs this {@link TaskAction}.
		 *
		 * @throws Exception
		 *             In case something goes wrong.
		 */
		void run() throws Exception; // NOSONAR

	}
}