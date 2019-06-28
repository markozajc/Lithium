package com.github.markozajc.lithium.processes.limiter;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.handlers.CommandHandler.CommandListener;
import com.github.markozajc.lithium.processes.LithiumProcess;
import com.github.markozajc.lithium.processes.ProcessManager;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;

public class CommandProcessLimiter implements CommandListener {

	private final int maxCommands;
	private final ProcessManager processes;
	private final PreparedDialog<CommandContext> limitHitDialog;

	public CommandProcessLimiter(int maxCommands, ProcessManager processes) {
		this(maxCommands, processes,
				new PreparedEmbedDialog<>(c -> EmbedDialog.generateEmbed("// LIMIT HIT //",
					"You currently have **" + maxCommands
							+ "** commands running, which is the upper limit. Please end a command before continuing.",
					Constants.RED, c.getUser())));

	}

	public CommandProcessLimiter(int maxCommands, ProcessManager processes,
			PreparedDialog<CommandContext> limitHitDialog) {
		this.maxCommands = maxCommands;
		this.processes = processes;
		this.limitHitDialog = limitHitDialog;
	}

	@Override
	public boolean canExecuteCommand(CommandContext context) throws Throwable {
		long running = this.processes.getProcesses()
				.stream()
				.map(LithiumProcess::getContext)
				.filter(c -> c instanceof CommandContext)
				.map(c -> ((CommandContext) c).getUser().getIdLong())
				.filter(id -> id == context.getUser().getIdLong())
				.count();

		if (running >= this.maxCommands) {
			this.limitHitDialog.generate(context).display(context.getChannel());
			return false;
		}

		return true;
	}

}
