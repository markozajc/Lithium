package com.github.markozajc.lithium.commands.utils;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.exceptions.startup.UsageException;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Commands {

	private Commands() {}

	/**
	 * Creates an usage string for a command using a prefix. Example of an usage
	 * string:<br>
	 * {@code*command <param1> <param2>}<br>
	 * where {@code *} is the prefix, {@code command} is the command name and
	 * {@code param1} and {@code param2} are the parameters
	 * 
	 * @param command
	 * @param prefix
	 * @return usage for that command
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static String buildUsage(Command command, String prefix) {
		StringBuilder usage = new StringBuilder();

		usage.append(prefix);
		usage.append(command.getName());

		for (String param : command.getParameters())
			usage.append(" <" + param + ">");

		return usage.toString();
	}

	/**
	 * Generates Parameters for a command from a raw message.
	 * 
	 * @param command
	 *            command to generate parameters for
	 * @param event
	 *            the event to generate parameters for
	 * @return parameters
	 * @throws UsageException
	 *             if {@link Command#getMinParameters()} is more than quantity of actual
	 *             parameters in rawMessage
	 */
	public static Parameters generateParameters(Command command, GuildMessageReceivedEvent event) {
		Parameters params = new Parameters(command.getParameters().length, event.getMessage().getContentRaw());
		if (!params.check(command.getMinParameters())) {
			throw new UsageException();
		}

		return params;
	}

	/**
	 * @param vars
	 *            variables to use
	 * @return array of variables
	 */
	@SafeVarargs
	@Nonnull
	public static <T> T[] toArray(@Nonnull T... vars) {
		return vars;
	}
}
