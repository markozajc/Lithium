package com.github.markozajc.lithium.handlers;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.exceptions.CommandException;
import com.github.markozajc.lithium.commands.exceptions.runtime.CancelledException;
import com.github.markozajc.lithium.commands.exceptions.runtime.NumberOverflowException;
import com.github.markozajc.lithium.commands.exceptions.runtime.TimeoutException;
import com.github.markozajc.lithium.commands.exceptions.startup.MemberInsufficientPermissionsException;
import com.github.markozajc.lithium.commands.exceptions.startup.MissingParametersException;
import com.github.markozajc.lithium.commands.exceptions.startup.UsageException;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.processes.context.ProcessContext;
import com.github.markozajc.lithium.utilities.BotUtils;
import com.github.markozajc.lithium.utilities.Formatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class ExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

	public void handleThrowable(ProcessContext context, Throwable throwable) {
		Throwable throwableUnpacked = unpackThrowable(throwable);
		LOG.debug("Handling {} as {}.", throwable.getClass().getSimpleName(),
			throwableUnpacked.getClass().getSimpleName());

		String errorCode = Integer.toHexString(throwableUnpacked.toString().hashCode());
		if (context instanceof CommandContext) {
			LOG.debug("Process is a CommandProcess, handling normally.");

			if (!HandleThrowable.handleThrowable((CommandContext) context, throwableUnpacked)) {
				LOG.debug("Couldn't handle, sending the generic error message.");
				if (((CommandContext) context).getChannel().canTalk())
					((CommandContext) context).getChannel()
							.sendMessage(BotUtils.buildEmbed("// FAILURE //",
								context.getLithium().getConfiguration().getName()
										+ " ran into an unknown error (error code: `0x" + errorCode + "`).",
								Constants.RED))
							.queue();
				reportThrowable(context, throwableUnpacked, errorCode);
			}

			if (((CommandContext) context).getCommand().getRatelimit() != 0 && shouldRatelimit(throwableUnpacked)) {}
			// TODO ratelimit

		} else {
			LOG.debug("Process is a not CommandProcess, just reporting to the owner.");
			reportThrowable(context, throwableUnpacked, errorCode);
		}

	}

	private static class HandleThrowable {

		private HandleThrowable() {}

		static boolean handleThrowable(CommandContext context, Throwable t) {
			if (t instanceof Error) {
				return HandleError.handleError(context, (Error) t);
			}

			if (t instanceof Exception) {
				return HandlePlain.handleException(context, (Exception) t);
			}

			return true;
		}

		private static class HandleError {

			static boolean handleError(CommandContext context, Error e) {
				// Finds the root exception type
				if (e instanceof VirtualMachineError) {
					return HandleVirtualMachineError.handleVirtualMachineError(context, (VirtualMachineError) e);
				}

				return false;
			}

			private static class HandleVirtualMachineError {

				static boolean handleVirtualMachineError(CommandContext context, VirtualMachineError vme) {
					// Finds the root exception type
					if (vme instanceof OutOfMemoryError) {
						handleOutOfMemoryError(context);
						return true;
					}

					return false;
				}

				private static void handleOutOfMemoryError(CommandContext context) {
					if (context.getChannel().canTalk())
						context.getChannel()
								.sendMessage(BotUtils.buildEmbed("// RAM LOW //",
									context.getLithium().getConfiguration().getName()
											+ " was unable to launch this command "
											+ "because there is a possibility that it would halt its vital processes. "
											+ "Please wait some time or try launching something else!",
									Constants.LITHIUM))
								.queue();
					// TODO perform cleanups
				}

			}

		}

		@SuppressFBWarnings("NM_CLASS_NOT_EXCEPTION")
		private static class HandlePlain {

			private HandlePlain() {}

			static boolean handleException(CommandContext context, Exception e) {
				// Finds the root exception type
				if (e instanceof InterruptedException) {
					handleInterruptedException(context);
					return true;
				}

				if (e instanceof RuntimeException) {
					return HandleRuntime.handleRuntimeException(context, (RuntimeException) e);
				}

				return false;

			}

			private static void handleInterruptedException(CommandContext context) {
				// Reports if command failed because the thread was interrupted

				if (context.getChannel().canTalk()) {
					context.getChannel()
							.sendMessage(BotUtils.buildEmbed(null,
								Formatters.getFirstUpper(context.getCommand().getName()) + " has been suspended.",
								Constants.GRAY))
							.queue();
				}
			}

			private static class HandleRuntime {

				static boolean handleRuntimeException(CommandContext context, RuntimeException re) {
					// Finds the root exception type
					if (re instanceof CommandException) {
						return HandleCommand.handleCommandException(context, (CommandException) re);
					}

					if (re instanceof ErrorResponseException) {
						handleErrorResponseException(context, (ErrorResponseException) re);
						return true;
					}

					if (re instanceof IllegalArgumentException) {
						return HandleIllegalArgument.handleIllegalArgumentException(context,
							(IllegalArgumentException) re);
					}

					if (re instanceof PermissionException) {
						return HandlePermission.handlePermissionException(context, (PermissionException) re);
					}

					return false;

				}

				private static class HandleCommand {

					static boolean handleCommandException(CommandContext context, CommandException ce) {
						// Finds the root exception type
						if (ce instanceof CancelledException) {
							// No need to handle
							return true;
						}

						if (ce instanceof MemberInsufficientPermissionsException) {
							handleMemberInsufficientPermissionsException(context,
								(MemberInsufficientPermissionsException) ce);
							return true;
						}

						if (ce instanceof NumberOverflowException) {
							handleNumberOverflowException(context);
							return true;
						}

						if (ce instanceof TimeoutException) {
							handleTimeoutException(context);
							return true;
						}

						if (ce instanceof UsageException) {
							return HandleUsage.handleUsageException(context, (UsageException) ce);
						}

						// Reports if command execution failed due to a known reason

						ce.sendMessage(context.getChannel());
						return true;
					}

					private static void handleMemberInsufficientPermissionsException(CommandContext context, MemberInsufficientPermissionsException mipe) {
						// Reports if the member executing the command does not have enough permissions to
						// execute that command / part of the command

						if (context.getChannel().canTalk()) {
							List<String> missing = mipe.getMissingPermissions()
									.stream()
									.map(Permission::getName)
									.collect(Collectors.toList());

							context.getChannel()
									.sendMessage(BotUtils.buildEmbed("// ACCESS DENIED //",
										"You need " + (missing.size() == 1 ? "this permission" : "these permissions")
												+ " in order to be able to execute this command:"
												+ missing.stream().collect(Collectors.joining("\n,", "\n", ".")),
										Constants.YELLOW))
									.queue();
						}
					}

					private static void handleNumberOverflowException(CommandContext context) {
						// Reports a number overflow

						if (context.getChannel().canTalk())
							context.getChannel()
									.sendMessage(BotUtils.buildEmbed("// INPUT TOO BIG //",
										"Looks like " + context.getLithium().getConfiguration().getName()
												+ " couldn't understand some number because it was too big!",
										Constants.YELLOW))
									.queue();
					}

					private static void handleTimeoutException(CommandContext context) {
						// Reports an EventWaiter timeout

						if (context.getChannel().canTalk())
							context.getChannel()
									.sendMessage(BotUtils.buildEmbed("Response time has run out.", Constants.GRAY))
									.queue();
					}

					private static class HandleUsage {

						static boolean handleUsageException(CommandContext context, UsageException ue) {
							// Finds the root exception type
							if (ue instanceof MissingParametersException) {
								handleMissingParametersException(context);
								return true;
							}

							// Reports if command execution failed due to incorrect usage

							if (context.getChannel().canTalk())
								context.getChannel()
										.sendMessage(BotUtils.buildEmbed("// USAGE INCORRECT //", "Correct usage: `"
												+ context.getCommand().getUnescapedUsage(context.getLithium()) + "`.",
											Constants.YELLOW))
										.queue();

							return true;
						}

						private static void handleMissingParametersException(CommandContext context) {
							// Reports if command execution failed due to missing parameters

							if (context.getChannel().canTalk())
								context.getChannel()
										.sendMessage(BotUtils.buildEmbed("// USAGE INCORRECT //",
											"You've provided too little parameters!\nCorrect usage: `"
													+ context.getCommand().getUnescapedUsage(context.getLithium())
													+ "`.",
											Constants.YELLOW))
										.queue();
						}

					}
				}

				private static void handleErrorResponseException(CommandContext context, ErrorResponseException ere) {
					// Reports an API error response

					if (context.getChannel().canTalk())
						context.getChannel()
								.sendMessage(BotUtils.buildEmbed("// DISCORD BROKE //",
									"Looks like Discord didn't like that for some reason.\nError: " + ere.getMeaning()
											+ ".",
									Constants.RED))
								.queue();
				}

				private static class HandleIllegalArgument {

					static boolean handleIllegalArgumentException(CommandContext context, IllegalArgumentException iae) {
						// Finds the root exception type
						if (iae instanceof NumberFormatException) {
							handleNumberFormatException(context);
							return true;
						}

						return false;
					}

					private static void handleNumberFormatException(CommandContext context) {
						// Reports if command execution failed due to hierarchy problems

						if (context.getChannel().canTalk())
							context.getChannel()
									.sendMessage(BotUtils.buildEmbed("// NOT A NUMBER //",
										"Looks like you have provided text in a place where a number would fit the best.",
										Constants.YELLOW))
									.queue();
					}
				}

				private static class HandlePermission {

					static boolean handlePermissionException(CommandContext context, PermissionException pe) {
						// Finds the root exception type
						if (pe instanceof HierarchyException) {
							handleHierarchyException(context);
							return true;
						}

						if (pe instanceof InsufficientPermissionException) {
							handleInsufficientPermissionException(context, (InsufficientPermissionException) pe);
							return true;
						}

						return false;
					}

					private static void handleHierarchyException(CommandContext context) {
						// Reports if command execution failed due to hierarchy problems

						if (context.getChannel().canTalk())
							context.getChannel()
									.sendMessage(BotUtils.buildEmbed("// HIERARCHY ERROR //",
										"Looks like you tried to perform an audit action on a user that is in a role higher than "
												+ context.getLithium().getConfiguration().getName() + ", "
												+ "which you can't. Please move "
												+ context.getLithium().getConfiguration().getName()
												+ "'s role up or demote that user!",
										Constants.YELLOW))
									.queue();
					}

					private static void handleInsufficientPermissionException(CommandContext context, InsufficientPermissionException ipe) {
						// Reports if command execution failed due to missing permissions

						if (context.getChannel().canTalk()) {
							if (ipe.getPermission().equals(Permission.MESSAGE_EMBED_LINKS)) {
								context.getChannel()
										.sendMessage("**// EMBED REQUIRED //**\nYou must first grant "
												+ context.getLithium().getConfiguration().getName() + " permission _"
												+ ipe.getPermission().getName()
												+ "_ in order to be able to execute this command!")
										.queue();

							} else {
								context.getChannel()
										.sendMessage(BotUtils.buildEmbed("// ACCESS DENIED //",
											"You must first grant " + context.getLithium().getConfiguration().getName()
													+ " permission _" + ipe.getPermission().getName()
													+ "_ in order to be able to execute this command!",
											Constants.YELLOW))
										.queue();
							}
						}
					}

				}
			}
		}
	}

	private static final Throwable unpackThrowable(Throwable t) {
		Throwable tt = ExceptionUtils.getRootCause(t);
		if (tt == null)
			return t;

		return tt;
	}

	private static boolean shouldRatelimit(Throwable t) {
		if (t instanceof CommandException) {
			CommandException ce = (CommandException) t;
			return ce.doesRegisterRatelimit();
		}

		return true;
	}

	@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE") // All queues have a predetermined length that will
																// never be exceeded (thus #offer will never return
																// false and can be safely ignored)
	private static void reportThrowable(ProcessContext context, Throwable t, String errorCode) {
		LOG.debug("Reporting a throwable to bot's owner.");

		String trace = ExceptionUtils.getStackTrace(t);
		Queue<Message> messages;
		if (t instanceof StackOverflowError) {
			messages = new ArrayDeque<>(1);
			messages.offer(new MessageBuilder(BotUtils.buildEmbed("A StackOverflowError has occured",
				"Error code: `" + errorCode + "`\n```(trace logged to console)```", Constants.RED)).build());
			LOG.error("Encountered an unhandled StackOverflowError", t);

		} else if (trace.length() <= MessageEmbed.TEXT_MAX_LENGTH) {
			messages = new ArrayDeque<>(1);
			messages.offer(new MessageBuilder(BotUtils.buildEmbed("An exception has occured",
				"Error code: `" + errorCode + "`\n```" + trace + "```", Constants.RED)).build());
		} else {
			messages = new MessageBuilder(
					"**An exception has occurred```**\n" + trace + "```\n**Error code:** " + errorCode)
							.buildAll(SplitPolicy.NEWLINE);
		}

		context.getLithium()
				.getConfiguration()
				.getOwner(context.getJDA())
				.openPrivateChannel()
				.queue(dm -> messages.stream().map(dm::sendMessage).forEach(MessageAction::queue));
	}

}
