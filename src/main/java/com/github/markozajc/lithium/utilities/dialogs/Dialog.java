package com.github.markozajc.lithium.utilities.dialogs;

import java.util.concurrent.CompletionStage;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public interface Dialog {

	public CompletionStage<Message> display(MessageChannel channel);

}
