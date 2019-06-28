package com.github.markozajc.lithium.utilities.dialogs.message.prepared;

import java.util.function.Function;

import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;

import net.dv8tion.jda.core.entities.MessageEmbed;

public class PreparedEmbedDialog<C> implements PreparedDialog<C> {

	private final Function<C, MessageEmbed> embed;

	public PreparedEmbedDialog(Function<C, MessageEmbed> embed) {
		this.embed = embed;
	}

	@Override
	public EmbedDialog generate(C context) {
		return new EmbedDialog(this.embed.apply(context));
	}

}
