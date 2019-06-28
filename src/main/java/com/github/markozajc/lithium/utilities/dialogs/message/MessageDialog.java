package com.github.markozajc.lithium.utilities.dialogs.message;

import java.util.concurrent.CompletionStage;

import com.github.markozajc.lithium.utilities.dialogs.Dialog;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class MessageDialog implements Dialog {

	private final Message message;

	public MessageDialog(Message message) {
		this.message = message;
	}

	public final Message getMessage() {
		return this.message;
	}

	@Override
	public CompletionStage<Message> display(MessageChannel channel) {
		return channel.sendMessage(getMessage()).submit();
	}

}
