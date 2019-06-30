package com.github.markozajc.lithium.utilities.dialogs;

import java.util.function.Predicate;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class Dialogs {

	private Dialogs() {}

	public static Predicate<Void> getDefaultCleanupPredicate(MessageChannel channel, long userId) {
		return v -> {
			MessageChannel foundChannel;

			User foundUser = channel.getJDA().getUserById(userId);
			if (foundUser == null)
				return true;
			// User no longer exists

			switch (channel.getType()) {
				case GROUP:
					if (channel.getJDA().getAccountType().equals(AccountType.CLIENT)) {
						foundChannel = channel.getJDA().asClient().getGroupById(channel.getIdLong());
					} else {
						foundChannel = null;
					}
					break;
				case PRIVATE:
					foundChannel = channel.getJDA().getPrivateChannelById(channel.getIdLong());
					break;
				case TEXT:
					foundChannel = channel.getJDA().getTextChannelById(channel.getIdLong());
					break;
				default:
					foundChannel = null;
					break;
			}

			if (foundChannel == null)
				return true;
			// Channel no longer exists

			if (foundChannel instanceof TextChannel) {
				if (!((TextChannel) foundChannel).getGuild().isMember(foundUser))
					return true;
				// User is not in guild

				if (!((TextChannel) foundChannel).canTalk(((TextChannel) foundChannel).getGuild().getMember(foundUser)))
					return true;
				// User can't talk anymore

				if(!((TextChannel) foundChannel).canTalk())
					return true;
				// Bot can't talk anymore
			}

			return false;
		};

	}

}
