package com.github.markozajc.lithium.utilities.dialogs.waiter;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.processes.context.ProcessContext;
import com.github.markozajc.lithium.utilities.dialogs.Dialogs;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

public class BooleanDialog extends EventWaiterDialog<GenericMessageReactionEvent> {

	private final long userId;

	public BooleanDialog(CommandContext context, MessageDialog messageDialog, Consumer<Boolean> action) {
		this(context, messageDialog, context.getUser(), action);
	}

	public BooleanDialog(ProcessContext context, MessageDialog messageDialog, User user, Consumer<Boolean> action) {
		this(context, messageDialog, user.getIdLong(), action);
	}

	public BooleanDialog(ProcessContext context, MessageDialog messageDialog, long userId, Consumer<Boolean> action) {
		super(context, GenericMessageReactionEvent.class, messageDialog, event -> action
				.accept(event.getReaction().getReactionEmote().getName().equals(Constants.ACCEPT_EMOJI)));
		this.userId = userId;
	}

	@Override
	public CompletionStage<Message> display(MessageChannel channel) {

		return this.getMessageDialog().display(channel).whenCompleteAsync((m, t) -> {
			if (m != null) {
				m.addReaction(Constants.ACCEPT_EMOJI).queue();
				m.addReaction(Constants.DENY_EMOJI).queue();

				Predicate<GenericMessageReactionEvent> isRight = event -> event.getUser().getIdLong() == this.userId
						&& event.getMessageIdLong() == m.getIdLong()
						&& (event.getReaction().getReactionEmote().getName().equals(Constants.ACCEPT_EMOJI)
								|| event.getReaction().getReactionEmote().getName().equals(Constants.DENY_EMOJI));
				Predicate<Void> canCleanup = Dialogs.getDefaultCleanupPredicate(channel, this.userId)
						.and(v -> m.getTextChannel().getMessageById(m.getId()) != null);

				registerWaiter(this.createWaiter(isRight, canCleanup));
			}

			if (t != null)
				this.getContext().getLithium().getEventWaiter().handleThrowable(this.getContext(), t);
		});
	}

}
