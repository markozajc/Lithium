package com.github.markozajc.lithium.utilities.dialogs.message;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class EmbedDialog extends MessageDialog {

	public EmbedDialog(MessageEmbed embed) {
		super(new MessageBuilder().setEmbed(embed).build());
	}

	public static EmbedBuilder setFooterUser(@Nonnull EmbedBuilder builder, @Nonnull User footer) {
		return builder.setFooter(getUserNameDiscriminator(footer), footer.getEffectiveAvatarUrl());
	}

	public static EmbedBuilder setAuthorUser(@Nonnull EmbedBuilder builder, @Nonnull User author) {
		return builder.setFooter(getUserNameDiscriminator(author), author.getEffectiveAvatarUrl());
	}

	/**
	 * Generates a {@link MessageEmbed}. Works using an {@link EmbedBuilder}, except that
	 * it takes all parameters as arguments rather than use the builder (chaining)
	 * pattern.
	 *
	 * @param description
	 *            The description (text) of the embed. <b>Can not be {@code null} when
	 *            sent to a channel!</b>
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed generateEmbed(@Nonnull String description) {
		return generateEmbed(null, description, null, null, null);
	}

	/**
	 * Generates a {@link MessageEmbed}. Works using an {@link EmbedBuilder}, except that
	 * it takes all parameters as arguments rather than use the builder (chaining)
	 * pattern.
	 *
	 * @param description
	 *            The description (text) of the embed. <b>Can not be {@code null} when
	 *            sent to a channel!</b>
	 * @param color
	 *            The color of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed generateEmbed(@Nonnull String description, @Nullable Color color) {
		return generateEmbed(null, description, color, null, null);
	}

	/**
	 * Generates a {@link MessageEmbed}. Works using an {@link EmbedBuilder}, except that
	 * it takes all parameters as arguments rather than use the builder (chaining)
	 * pattern.
	 *
	 * @param title
	 *            The title of the embed.
	 * @param description
	 *            The description (text) of the embed. <b>Can not be {@code null} when
	 *            sent to a channel!</b>
	 * @param color
	 *            The color of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed generateEmbed(@Nullable String title, @Nonnull String description, @Nullable Color color) {
		return generateEmbed(title, description, color, null);
	}

	/**
	 * Generates a {@link MessageEmbed}. Works using an {@link EmbedBuilder}, except that
	 * it takes all parameters as arguments rather than use the builder (chaining)
	 * pattern.
	 *
	 * @param title
	 *            The title of the embed.
	 * @param description
	 *            The description (text) of the embed. <b>Can not be {@code null} when
	 *            sent to a channel!</b>
	 * @param color
	 *            The color of the embed.
	 * @param footer
	 *            The footer user of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	public static MessageEmbed generateEmbed(@Nullable String title, @Nonnull String description, @Nullable Color color, @Nullable User footer) {
		return generateEmbed(title, description, color, footer, null);
	}

	/**
	 * Generates a {@link MessageEmbed}. Works using an {@link EmbedBuilder}, except that
	 * it takes all parameters as arguments rather than use the builder (chaining)
	 * pattern.
	 *
	 * @param title
	 *            The title of the embed.
	 * @param description
	 *            The description (text) of the embed. <b>Can not be {@code null} when
	 *            sent to a channel!</b>
	 * @param color
	 *            The color of the embed.
	 * @param footer
	 *            The footer user of the embed.
	 * @param author
	 *            The author of the embed.
	 * @return The generated {@link MessageEmbed}.
	 */
	@SuppressWarnings("null")
	public static MessageEmbed generateEmbed(@Nullable String title, @Nonnull String description, @Nullable Color color, @Nullable User footer, @Nullable User author) {
		EmbedBuilder b = new EmbedBuilder().setTitle(title).setDescription(description).setColor(color);
		if (author != null)
			setAuthorUser(b, author);
		if (footer != null)
			setFooterUser(b, footer);
		return b.build();
	}

	private static String getUserNameDiscriminator(@Nonnull User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}

}
