package com.github.markozajc.lithium.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.Lithium;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.User;

public class BotUtils {

	private BotUtils() {}

	private static final Random GLOBAL_RANDOM = new Random();

	/**
	 * Checks if a {@link User} is the bot's owner.
	 *
	 * @param lithium
	 *            The {@link Lithium} instance.
	 * @param user
	 *            The {@link User} to evaluate.
	 * @return Whether the {@link User} is the bot's owner.
	 */
	public static boolean isOwner(Lithium lithium, User user) {
		return user.getIdLong() == lithium.getConfiguration().getOwnerId();
	}

	/**
	 * Builds a simple embedded message.
	 *
	 * @param title
	 *            The title of the embed.
	 * @param message
	 *            The message (description) of the embed.
	 * @param footer
	 *            The footer.
	 * @param color
	 *            The color of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed buildEmbed(String title, String message, String footer, Color color) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.appendDescription(message);
		builder.setFooter(footer, null);
		builder.setColor(color);

		return builder.build();
	}

	/**
	 * Builds a simple {@link MessageEmbed} without a title.
	 *
	 * @param message
	 *            Message (description) of the embed.
	 * @param color
	 *            {@link Color} of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed buildEmbed(String message, Color color) {
		return buildEmbed(null, message, null, color);
	}

	/**
	 * Builds a simple {@link MessageEmbed}.
	 *
	 * @param title
	 *            Title of the embed.
	 * @param message
	 *            Message (description) of the embed.
	 * @param color
	 *            {@link Color} of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed buildEmbed(String title, String message, Color color) {
		return buildEmbed(title, message, null, color);
	}

	/**
	 * Returns text with properly escaped markdown.
	 *
	 * @param text
	 *            Text with markdown.
	 * @return Text with escaped markdown.
	 */
	public static String escapeMarkdown(String text) {
		String result = text;
		for (String markdown : Constants.getMarkdown()) {
			result = result.replace(markdown, "\\" + markdown);
		}

		return result;
	}

	/**
	 * Returns text with properly unescaped markdown.
	 *
	 * @param text
	 *            Text with escaped markdown.
	 * @return Text with unescaped markdown.
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static String unescapeMarkdown(@Nonnull String text) {
		String result = text;
		for (String markdown : Constants.getMarkdown())
			result = result.replace("\\" + markdown, markdown);

		return result;
	}

	/**
	 * @return The global {@link Random} for this bot.
	 */
	public static Random getRandom() {
		return GLOBAL_RANDOM;
	}

	public static List<Message> embedToMessage(MessageEmbed embed) {
		StringBuilder sb = new StringBuilder();

		sb.append(embed.getTitle() != null ? ("\n**" + embed.getTitle() + "**").replace("\n", "\n\t") : "");
		// Appends embed's title (if present)

		sb.append(embed.getDescription() != null ? ("\n" + embed.getDescription()).replace("\n", "\n\t") : "");
		// Appends embed's description

		for (Field field : embed.getFields()) {
			sb.append("\n");
			sb.append(field.getName() != null ? ("\n**" + field.getName() + "**").replace("\n", "\n\t\t") : "");
			// Appends field's name (if present)

			sb.append(field.getValue() != null ? ("\n" + field.getValue()).replace("\n", "\n\t\t") : "");
			// Appends field's value (if present)
		}
		// Appends all embed's fields' data

		sb.append("\n");

		sb.append(embed.getFooter() != null ? ("\n_" + embed.getFooter().getText() + "_").replace("\n", "\n\t") : "");
		// Appends embed's footer

		MessageBuilder mb = new MessageBuilder(sb.toString());

		return Collections.unmodifiableList(new ArrayList<>(mb.buildAll()));
	}
}
