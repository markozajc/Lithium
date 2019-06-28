package com.github.markozajc.lithium.commands.exceptions.runtime;

import com.github.markozajc.lithium.commands.exceptions.CommandException;

public class NumberOverflowException extends CommandException {

	private static final long serialVersionUID = 1L;

	public NumberOverflowException() {
		super(false);
	}

}
