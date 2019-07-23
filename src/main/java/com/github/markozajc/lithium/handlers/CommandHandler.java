package com.github.markozajc.lithium.handlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.processes.CommandProcess;
import com.github.markozajc.lithium.processes.context.CommandContext;

/**
 * A class used to handle {@link CommandProcess} execution. It does so using
 * {@link CommandListener}s which are invoked before the {@link Command} is executed
 * by a {@link CommandProcess} (to determine whether it's eligible for launch) and
 * after the {@link Command} finishes.
 *
 * @author Marko Zajc
 */
public class CommandHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CommandHandler.class);

	private final Set<CommandListener> commandListeners = Collections.synchronizedSet(new HashSet<CommandListener>());

	/**
	 * A listener with some actions for a command
	 *
	 * @author Marko Zajc
	 */
	@SuppressWarnings("unused")
	public static interface CommandListener {

		/**
		 * Called for each registered listener right after a {@link Command} is invoked. Used
		 * to determine if a {@link Command} is eligible for launch.
		 *
		 * @param context
		 *            The {@link CommandContext} with the {@link Command} in question.
		 * @return Whether the {@link Command} is eligible for launch
		 * @throws Throwable
		 *             in case an error occurs
		 */
		public default boolean canExecuteCommand(CommandContext context) throws Throwable { // NOSONAR
			return true;
		}

		/**
		 * Called for each registered listener when a command execution is finished.
		 *
		 * @param context
		 *            The {@link CommandContext} with the {@link Command} in question.
		 * @throws Throwable
		 *             in case an error occurs
		 */
		public default void onCommandFinished(CommandContext context) throws Throwable {} // NOSONAR

	}

	/**
	 * Lets the {@link CommandListener}s determine whether a {@link Command} is eligible
	 * for execution or not.
	 *
	 * @param context
	 *            The {@link CommandContext} to check.
	 * @return Whether or not the {@link Command} should be executed.
	 */
	public boolean canExecuteCommand(CommandContext context) {
		return this.commandListeners.stream().map(listener -> {
			try {
				return listener.canExecuteCommand(context);
			} catch (Throwable e) {
				LOG.error("Caught exception in a command listener", e);
				return false;
			}
		}).allMatch(r -> r);
	}

	/**
	 * Lets the {@link CommandListener}s know that a {@link Command} has finished.
	 *
	 * @param context
	 *            The {@link CommandContext} with the {@link Command} in question.
	 */
	public void runOnCommandFinished(CommandContext context) {
		this.commandListeners.forEach(listener -> {
			try {
				listener.onCommandFinished(context);
			} catch (Throwable e) {
				LOG.error("Caught exception in a command listener", e);
			}
		});
	}

	/**
	 * Registers a new {@link CommandListener}.
	 *
	 * @param listener
	 *            The {@link CommandListener} to register.
	 */
	public void registerListener(CommandListener listener) {
		this.commandListeners.add(listener);
	}

	/**
	 * Unregisters a CommandListener
	 *
	 * @param listener
	 *            The {@link CommandListener} to unregister.
	 */
	public void removeListener(CommandListener listener) {
		this.commandListeners.remove(listener);
	}
}
