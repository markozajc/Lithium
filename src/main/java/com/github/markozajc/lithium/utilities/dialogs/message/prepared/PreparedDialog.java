package com.github.markozajc.lithium.utilities.dialogs.message.prepared;

import javax.annotation.CheckReturnValue;

import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;

public interface PreparedDialog<C> {

	@CheckReturnValue
	public MessageDialog generate(C context);

}
