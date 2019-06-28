package com.github.markozajc.lithium.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.CommandProcess;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

	private final Lithium lithium;
	private final PreparedDialog<GuildMessageReceivedEvent> mentionDialog;

	public MessageListener(Lithium lithium) {
		this(lithium,
				new PreparedEmbedDialog<>(e -> EmbedDialog.generateEmbed(
					"Thanks for choosing " + lithium.getConfiguration().getName(),
					"To get started, type in `" + lithium.getConfiguration().getDefaultPrefix() + "help` or `@"
							+ e.getGuild().getSelfMember().getEffectiveName() + " help` to receive a list of commands. "
							+ "If you're still unsure about what which command does, you can type in `"
							+ lithium.getConfiguration().getDefaultPrefix() + "help <command's name>`"
							+ "to get information about a command!",
					Constants.LITHIUM, e.getAuthor())));
	}

	public MessageListener(Lithium lithium, PreparedDialog<GuildMessageReceivedEvent> mentionDialog) {
		this.lithium = lithium;
		this.mentionDialog = mentionDialog;
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		// Checks if sender is a bot

		if (event.getMessage().getContentRaw().equals(event.getJDA().getSelfUser().getAsMention())) {
			this.mentionDialog.generate(event).display(event.getChannel());
			return;
		}
		// Displays 'mention dialog' if the bot is mentioned

		String selfMention = event.getGuild().getSelfMember().getAsMention();
		if (event.getMessage().getContentRaw().startsWith(this.lithium.getConfiguration().getDefaultPrefix())
				|| event.getMessage().getContentRaw().startsWith(selfMention)) {
			int substring = 0;
			if (event.getMessage().getContentRaw().startsWith(selfMention)) {
				substring = selfMention.length();

			} else if (event.getMessage()
					.getContentRaw()
					.startsWith(this.lithium.getConfiguration().getDefaultPrefix())) {
				substring = this.lithium.getConfiguration().getDefaultPrefix().length();
			}

			String commandName;
			String[] parameters = Parameters.formatParams(
				event.getMessage().getContentRaw().toLowerCase().substring(substring).trim(), 0, false, false);
			if (parameters.length < 1)
				return;
			commandName = parameters[0];

			LOG.debug("Requested execution of {}.", commandName);

			Command command = this.lithium.getCommands().get(commandName);

			if (command == null) {
				LOG.debug("Command {} does not exist.", commandName);
				return;
			}

			LOG.debug("Sumbitting command {} to the ProcessManager.", command.getName());
			this.lithium.getProcessManager()
					.submitProcess(new CommandProcess(new CommandContext(this.lithium, event, command)));
			// Launches the command handler
		}
	}

}
