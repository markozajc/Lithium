package com.github.markozajc.lithium.utilities.dialogs.message.prepared;

import java.util.function.Function;

import com.github.markozajc.lithium.utilities.dialogs.message.TextDialog;

public class PreparedTextDialog<C> implements PreparedDialog<C> {

	private final Function<C, CharSequence> message;

	public PreparedTextDialog(Function<C, CharSequence> message) {
		this.message = message;
	}

	@Override
	public TextDialog generate(C context) {
		return new TextDialog(this.message.apply(context));
	}

}
