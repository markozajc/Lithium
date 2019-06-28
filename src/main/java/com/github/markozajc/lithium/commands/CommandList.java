package com.github.markozajc.lithium.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class CommandList implements Iterable<Command> {

	private final List<Command> commands;

	/**
	 * You may not use this constructor alone, rather use CommandsBuilder to construct a
	 * list of commands
	 *
	 * @param builder
	 *            builder to get registered commands from
	 */
	public CommandList(CommandListBuilder builder) {
		List<Command> newCommands = new ArrayList<>(builder.getRegistered());

		Collections.sort(newCommands, (Command c1, Command c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

		this.commands = Collections.unmodifiableList(newCommands);
	}

	/**
	 * Checks if command with given name exists
	 *
	 * @param name
	 *            command's name to search for
	 * @return true if the command exists, false if it doesn't
	 */
	public boolean contains(String name) {
		return get(name) == null;
	}

	/**
	 * @param name
	 *            name of command to search for
	 * @return the command with given alias or name, if found, null if a command with
	 *         such alias/name wasn't found
	 */
	public Command get(String name) {
		return this.commands.stream()
				.filter(cmd -> cmd.getName().equalsIgnoreCase(name.toLowerCase()))
				.findAny()
				.orElse(this.commands.stream().filter(cmd -> {

					String[] aliases = cmd.getAliases();
					for (String alias : aliases)
						if (alias.equalsIgnoreCase(name))
							return true;

					return false;
				}).findAny().orElse(null));
	}

	/**
	 * @param id
	 * @return the command with given id, if found, null if a command with such ID wasn't
	 *         found
	 */
	@Nullable
	public Command getById(int id) {
		return this.commands.stream().filter(c -> c.getId() == id).findAny().orElse(null);
	}

	/**
	 * Retrieves list of all registered commands
	 *
	 * @return An unmodifiable containg all registered commands
	 */
	public List<Command> getRegisteredCommands() {
		return Collections.unmodifiableList(this.commands);
	}

	/**
	 * Retrieves all commands from a category
	 *
	 * @param category
	 *            category to retrieve commands from
	 * @return list of all commands from that category
	 */
	public List<Command> getAll(CommandCategory category) {
		return this.commands.stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
	}

	@Override
	public Iterator<Command> iterator() {
		return this.commands.iterator();
	}

}