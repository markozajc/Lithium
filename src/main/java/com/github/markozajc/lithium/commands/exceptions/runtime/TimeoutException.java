package com.github.markozajc.lithium.commands.exceptions.runtime;

import com.github.markozajc.lithium.commands.exceptions.CommandException;

public class TimeoutException extends CommandException {

	public TimeoutException() {
		super(false);
	}

}
