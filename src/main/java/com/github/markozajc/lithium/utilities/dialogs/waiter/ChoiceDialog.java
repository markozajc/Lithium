package com.github.markozajc.lithium.utilities.dialogs.waiter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.processes.context.ProcessContext;
import com.github.markozajc.lithium.utilities.dialogs.Dialog;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ChoiceDialog extends EventWaiterDialog<MessageReceivedEvent> {

	private final Dialog invalidChoice;
	private final long userId;
	private final List<String> choices;

	public ChoiceDialog(CommandContext context, MessageDialog messageDialog, IntConsumer action, String... choices) {
		this(context, messageDialog, context.getUser(), action, Arrays.asList(choices));
	}

	public ChoiceDialog(ProcessContext context, MessageDialog messageDialog, User user, IntConsumer action,
			String... choices) {
		this(context, messageDialog, user, action, Arrays.asList(choices));
	}

	public ChoiceDialog(ProcessContext context, MessageDialog messageDialog, User user, IntConsumer action,
			List<String> choices) {
		this(context, messageDialog, user.getIdLong(), action, choices);
	}

	public ChoiceDialog(ProcessContext context, MessageDialog messageDialog, long userId, IntConsumer action,
			List<String> choices) {
		this(context, messageDialog, userId, action, choices,
				new EmbedDialog(EmbedDialog.generateEmbed("Invalid choice", "Please choose between "
						+ choices.stream().map(String::toUpperCase).collect(Collectors.joining("**, **", "**", "**")),
					Constants.NONE)));
	}

	public ChoiceDialog(ProcessContext context, MessageDialog messageDialog, long userId, IntConsumer action,
			List<String> choices, Dialog invalidChoice) {
		super(context, MessageReceivedEvent.class, messageDialog,
				event -> action.accept(choices.indexOf(event.getMessage().getContentDisplay().toLowerCase())));
		this.userId = userId;
		this.choices = choices;
		this.invalidChoice = invalidChoice;
	}

	@Override
	public CompletionStage<Message> display(MessageChannel channel) {
		Predicate<MessageReceivedEvent> isRight = event -> {
			if (event.getAuthor().getIdLong() == this.userId && event.getChannel().getIdLong() == channel.getIdLong()) {
				if (this.choices.indexOf(event.getMessage().getContentDisplay().toLowerCase()) > -1)
					return true;

				this.invalidChoice.display(channel);
			}

			return false;
		};
		registerWaiter(this.createWaiter(isRight, channel, this.userId));

		return this.getMessageDialog().display(channel);
	}

}
