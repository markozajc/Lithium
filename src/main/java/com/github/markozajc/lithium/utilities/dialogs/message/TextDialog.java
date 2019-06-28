package com.github.markozajc.lithium.utilities.dialogs.message;

import net.dv8tion.jda.core.MessageBuilder;

public class TextDialog extends MessageDialog {

	public TextDialog(CharSequence message) {
		super(new MessageBuilder(message).build());
	}

}
