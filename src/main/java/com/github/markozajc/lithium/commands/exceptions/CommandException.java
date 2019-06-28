package com.github.markozajc.lithium.commands.exceptions;

import java.awt.Color;

import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.utilities.BotUtils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * An exceptions used to indicate that a {@link Command} has failed and whether it
 * should avoid ratelimit registration
 *
 * @author Marko Zajc
 */
public class CommandException extends RuntimeException {

	private final transient Message msg;
	private final boolean registerRatelimit;

	public CommandException(String title, String message, Color color, boolean registerRatelimit) {
		this.msg = new MessageBuilder(BotUtils.buildEmbed(title, message, color)).build();
		this.registerRatelimit = registerRatelimit;
	}

	public CommandException(String message, Color color, boolean registerRatelimit) {
		this(null, message, color, registerRatelimit);
	}

	public CommandException(String message, boolean registerRatelimit) {
		this.msg = new MessageBuilder(message).build();
		this.registerRatelimit = registerRatelimit;
	}

	public CommandException(boolean registerRatelimit) {
		this.msg = null;
		this.registerRatelimit = registerRatelimit;
	}

	/**
	 * If a ratelimit should be registered upon throwing this exception.
	 *
	 * @return whether ratelimit should be registered
	 */
	public boolean doesRegisterRatelimit() {
		return this.registerRatelimit;
	}

	/**
	 * Sends the given message to a MessageChannel. If a message wasn't specified, this
	 * won't do anything.
	 *
	 * @param channel
	 */
	public void sendMessage(MessageChannel channel) {
		if (this.msg != null)
			channel.sendMessage(this.msg).queue();
	}
}
