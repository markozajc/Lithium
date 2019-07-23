package com.github.markozajc.lithium.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CommandListBuilder {

	private final Collection<Command> registered;

	/**
	 * Creates a list of executable commands
	 */
	public CommandListBuilder() {
		this.registered = new HashSet<>();
	}

	/**
	 * Registers a command.
	 *
	 * @param command
	 *            The {@link Command} to register.
	 * @return self, used for chaining
	 */
	public CommandListBuilder registerCommand(Command command) {
		this.registered.add(command);
		return this;
	}

	/**
	 * Registers a list of commands.
	 *
	 * @param commands
	 *            A {@link Collection} of {@link Command}s to register.
	 * @return self, used for chaining
	 */
	public CommandListBuilder registerCommands(Collection<Command> commands) {
		this.registered.addAll(commands);
		return this;
	}

	/**
	 * Unregisters a command.
	 *
	 * @param command
	 *            The {@link Command} to unregister.
	 * @return self, used for chaining
	 * @see CommandListBuilder#unregisterCommands(Collection)
	 */
	public CommandListBuilder unregisterCommand(Command command) {
		this.registered.remove(command);
		return this;
	}

	/**
	 * Unregisters a collection of commands.
	 *
	 * @param commands
	 *            A {@link Collection} of {@link Command}s to unregister.
	 * @return self, used for chaining
	 * @see CommandListBuilder#unregisterCommand(Command)
	 */
	public CommandListBuilder unregisterCommands(Collection<Command> commands) {
		this.registered.removeAll(commands);
		return this;
	}

	/**
	 * @return an unmodifiable collection of all currently registered commands.
	 */
	public Collection<Command> getRegistered() {
		return Collections.unmodifiableCollection(this.registered);
	}

	/**
	 * Creates a new CommandList for ease of access.
	 *
	 * @return Commands
	 */
	public CommandList build() {
		return new CommandList(this);
	}

}
