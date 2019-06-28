package com.github.markozajc.lithium.processes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.commands.ratelimits.RatelimitsManager;
import com.github.markozajc.lithium.commands.utils.Commands;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.processes.context.ProcessContext;

public class CommandProcess extends LithiumProcess<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(CommandProcess.class);

	public CommandProcess(CommandContext context) {
		super(context);
	}

	@Override
	public Void call() {
		try {
			LOG.debug("Querying command handler for command {}.", this.getContext().getCommand().getId());
			if (this.getContext().getLithium().getHandlers().getCommand().canExecuteCommand(this.getContext())) {
				LOG.debug("Command {} eligible, preparing execution.", this.getContext().getCommand().getId());

				Parameters params = Commands.generateParameters(this.getContext().getCommand(),
					this.getContext().getEvent());
				// Generates parameters

				this.getContext().getCommand().startupCheck(this.getContext(), params);
				// Checks if the command can be launched

				LOG.debug("Executing command {}.", this.getContext().getCommand().getId());
				this.getContext().getCommand().execute(this.getContext(), params);
				// Executes the command

				if (this.getContext().getCommand().getRatelimit() != 0)
					RatelimitsManager.getRatelimits(this.getContext().getCommand())
							.register(this.getContext().getEvent().getAuthor().getId());
				// Registers the ratelimit if the command has finished

				LOG.debug("Running termination listeners for command {}.", this.getContext().getCommand().getId());
				this.getContext().getLithium().getHandlers().getCommand().runOnCommandFinished(this.getContext());
			} else {
				LOG.debug("Command {} not eligible.", this.getContext().getCommand().getId());
			}

		} catch (Throwable t) {
			LOG.debug("Caught exception on command {}, handling.", this.getContext().getCommand().getId());
			this.getContext().getLithium().getHandlers().getException().handleThrowable(this.getContext(), t);
			// Handles the exception on exception
		}

		return null;
	}

	@Override
	public CommandContext getContext() {
		ProcessContext context = super.getContext();
		if (context instanceof CommandContext)
			return (CommandContext) context;

		throw new IllegalStateException(
				"CommandProcess's context is not an instance of CommandContext. How on earth did this occur? (context is of type "
						+ context.getClass().getCanonicalName() + ")");
	}

}
