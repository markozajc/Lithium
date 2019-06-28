package com.github.markozajc.lithium.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.commands.exceptions.startup.MemberInsufficientPermissionsException;
import com.github.markozajc.lithium.commands.utils.Commands;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.BotUtils;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

public abstract class Command {

	private static final int DEFAULT_RATELIMIT = 0;

	/**
	 * Checks if a member has sufficient permissions to execute a command. If they don't,
	 * this will throw a {@link MemberInsufficientPermissionsException}.
	 *
	 * @param member
	 * @throws MemberInsufficientPermissionsException
	 */
	public final void checkPermissions(Member member) {
		Permission[] permissions = this.getPermissions();

		if (member.hasPermission(permissions))
			return;

		throw new MemberInsufficientPermissionsException(
				Arrays.asList(permissions).stream().filter(p -> !member.hasPermission(p)).collect(Collectors.toList()));

	}

	public abstract void execute(CommandContext context, Parameters params) throws Throwable; // NOSONAR

	@Nullable
	public String getAdditionalData() {
		return null;
	}

	@Nonnull
	public String[] getAliases() {
		return new String[0];
	}

	@Nonnull
	public abstract CommandCategory getCategory();

	@Nonnegative
	public final int getId() {
		return getName().hashCode();
	}

	@Nonnull
	public abstract String getInfo();

	@Nonnegative
	public int getMinParameters() {
		return getParameters().length;
	}

	@Nonnull
	public abstract String getName();

	@Nonnull
	public String[] getParameters() {
		return new String[0];
	}

	@Nonnull
	public Permission[] getPermissions() {
		return new Permission[0];
	}

	public int getRatelimit() {
		return DEFAULT_RATELIMIT;
	}

	@Nonnull
	public String getRatelimitId() {
		return getName();
	}

	@Nonnull
	public String getUnescapedUsage(Lithium lithium) {
		return BotUtils.unescapeMarkdown(getUsage(lithium));
	}

	@Nonnull
	public String getUsage(Lithium lithium) {
		return Commands.buildUsage(this, lithium.getConfiguration().getDefaultPrefix());
	}

	public boolean pausesThread() {
		return false;
	}

	@SuppressWarnings("unused")
	public void startupCheck(CommandContext context, Parameters params) throws Throwable { // NOSONAR
		checkPermissions(context.getEvent().getMember());
	}
}
