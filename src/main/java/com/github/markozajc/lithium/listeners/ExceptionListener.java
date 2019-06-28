package com.github.markozajc.lithium.listeners;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.utilities.BotUtils;

import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ExceptionListener extends ListenerAdapter {

	private final Lithium lithium;

	public ExceptionListener(Lithium lithium) {
		this.lithium = lithium;
	}

	@Override
	public void onException(ExceptionEvent event) {
		this.lithium.getConfiguration().getOwner(event.getJDA()).openPrivateChannel().queue(dm -> {
			if (event.getCause() instanceof OutOfMemoryError) {
				dm.sendMessage(BotUtils.buildEmbed("Out of memory",
					"Looks like " + this.lithium.getConfiguration().getName() + " ran out of memory!", Constants.RED))
						.queue();

			} else {
				dm.sendMessage(
					BotUtils.buildEmbed("Something has failed within " + this.lithium.getConfiguration().getName(),
						"```" + ExceptionUtils.getStackTrace(event.getCause()) + "```", Constants.RED)).queue();
			}
		});
	}
}
