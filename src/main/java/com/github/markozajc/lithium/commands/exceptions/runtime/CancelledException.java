package com.github.markozajc.lithium.commands.exceptions.runtime;

import com.github.markozajc.lithium.commands.exceptions.CommandException;

public class CancelledException extends CommandException {

	public CancelledException() {
		super(false);
	}
}
