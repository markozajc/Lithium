package com.github.markozajc.lithium.utilities.dialogs.waiter;

import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.processes.context.ProcessContext;
import com.github.markozajc.lithium.utilities.ThrowableConsumer;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TextInputDialog extends EventWaiterDialog<MessageReceivedEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(TextInputDialog.class);
	private final long userId;

	public TextInputDialog(ProcessContext context, MessageDialog messageDialog, User user,
			ThrowableConsumer<Message, Throwable> action) {
		this(context, messageDialog, user.getIdLong(), action);
	}

	public TextInputDialog(ProcessContext context, MessageDialog messageDialog, long userId,
			ThrowableConsumer<Message, Throwable> action) {
		super(context, MessageReceivedEvent.class, messageDialog, event -> action.accept(event.getMessage()));
		this.userId = userId;
	}

	@Override
	public CompletionStage<Message> display(MessageChannel channel) {
		Predicate<MessageReceivedEvent> isRight = event -> event.getAuthor().getIdLong() == this.userId
				&& event.getChannel().getIdLong() == channel.getIdLong();
		registerWaiter(this.createWaiter(isRight, channel, 0));

		this.getMessageDialog().display(channel).whenCompleteAsync((m, t) -> {
			if (m != null) {
				LOG.debug("Adding reactions");
				m.addReaction(Constants.ACCEPT_EMOJI).queue();
				m.addReaction(Constants.DENY_EMOJI).queue();
			}

			if (t != null)
				this.getContext().getLithium().getEventWaiter().handleThrowable(this.getContext(), t);
		});

		return null;
	}

}
