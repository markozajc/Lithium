package com.github.markozajc.lithium.processes.context;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.processes.LithiumProcess;

import net.dv8tion.jda.core.JDA;

public class ProcessContext {

	@Nonnull
	private final Lithium lithium;
	@Nonnull
	private final JDA jda;

	/**
	 * Creates a new {@link ProcessContext}.
	 *
	 * @param lithium
	 *            The {@link Lithium} instance of this {@link LithiumProcess}.
	 * @param jda
	 *            The {@link JDA} instance of this {@link LithiumProcess}.
	 */
	public ProcessContext(@Nonnull Lithium lithium, @Nonnull JDA jda) {
		this.lithium = lithium;
		this.jda = jda;
	}

	/**
	 * @return The {@link Lithium} instance of this {@link LithiumProcess}.
	 */
	@Nonnull
	public Lithium getLithium() {
		return this.lithium;
	}

	/**
	 * @return The {@link JDA} instance of this {@link LithiumProcess}.
	 */
	@Nonnull
	public JDA getJDA() {
		return this.jda;
	}

}
