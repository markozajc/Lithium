package com.github.markozajc.lithium.processes.context;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.commands.Command;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CommandContext extends ProcessContext {

	private final Command command;
	private final GuildMessageReceivedEvent event;

	/**
	 * Creates a new {@link CommandContext}.
	 *
	 * @param lithium
	 *            The {@link Lithium} instance.
	 * @param command
	 *            The {@link Command} to run.
	 * @param event
	 *            The {@link GuildMessageReceivedEvent} that was determined to have
	 *            requested the {@link Command} execution.
	 */
	public CommandContext(@Nonnull Lithium lithium, @Nonnull GuildMessageReceivedEvent event,
			@Nonnull Command command) {
		super(lithium, event.getJDA());
		this.command = command;
		this.event = event;
	}

	/**
	 * @return The {@link Command} of this {@link CommandContext}.
	 */
	@Nonnull
	public Command getCommand() {
		return this.command;
	}

	/**
	 * @return The {@link GuildMessageReceivedEvent} that was determined to have
	 *         requested the {@link Command} execution.
	 */
	@Nonnull
	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

	/**
	 * A shortcut to {@link GuildMessageReceivedEvent#getChannel()}.
	 *
	 * @return This {@link Command}'s {@link TextChannel}.
	 */
	@Nonnull
	public TextChannel getChannel() {
		return this.getEvent().getChannel();
	}

	/**
	 * A shortcut to {@link GuildMessageReceivedEvent#getAuthor()}.
	 *
	 * @return The {@link User} running this {@link Command}.
	 */
	@Nonnull
	public User getUser() {
		return this.getEvent().getAuthor();
	}

}
