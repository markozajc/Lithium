package com.github.markozajc.lithium.commands.ratelimits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.markozajc.lithium.commands.Command;

public class RatelimitsManager {

	private RatelimitsManager() {}
	
	// Ratelimits
	private static final Map<String, Ratelimits> RATELIMITS = new ConcurrentHashMap<>();

	/**
	 * Creates / retrieves ratelimits for a command. If ratelimits do not exist already,
	 * they will be created and configured with waiting time of 0 seconds.
	 * 
	 * @param command
	 *            command to retrieve ratelimits for
	 * @return never-null ratelimits for that identifier
	 */
	public static Ratelimits getRatelimits(final Command command) {
		if (!RATELIMITS.containsKey(command.getRatelimitId())) {
			RATELIMITS.put(command.getRatelimitId(), new Ratelimits());
		}

		return RATELIMITS.get(command.getRatelimitId());
	}

}
