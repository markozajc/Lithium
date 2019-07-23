package com.github.markozajc.lithium.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message.MentionType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class FinderUtils {

	private FinderUtils() {}

	/**
	 * Searches all visible {@link User}s for matching/similar name and mention.
	 *
	 * @param jda
	 *            The {@link JDA} instance.
	 * @param query
	 *            The search query.
	 * @return The {@link List} of found {@link User}, sorted by similarity to the query,
	 *         with direct mentions first.
	 */
	public static List<User> findUsers(JDA jda, String query) {
		String text = query.toLowerCase().trim();

		List<User> found = new ArrayList<>();
		found.addAll(findUsersFromMentions(jda.getUserCache(), text));
		found.addAll(findUsersFromStream(jda.getUserCache().stream(), text));

		return Collections.unmodifiableList(found);
	}

	/**
	 * Searches all {@link Member}s in a {@link Guild} for matching/similar name and
	 * mention.
	 *
	 * @param query
	 *            The search query.
	 * @param guild
	 *            The {@link Guild} to search.
	 * @return list of found members, sorted by similarity to the query, with direct
	 *         mentions first (can be empty)
	 */
	public static List<Member> findMembers(String query, Guild guild) {
		String text = query.toLowerCase().trim();

		List<Member> found = new ArrayList<>();
		found.addAll(findMembersFromMentions(guild.getMemberCache(), text));
		found.addAll(findMembersFromStream(guild.getMemberCache().stream(), text));

		return Collections.unmodifiableList(found);
	}

	/**
	 * Searches all {@link Role}-s in a {@link Guild} for matching/similar name and
	 * mention.
	 *
	 * @param query
	 *            The search query.
	 * @param guild
	 *            The {@link Guild} to search.
	 * @return list of found roles, sorted by similarity to the query, with direct
	 *         mentions first (can be empty)
	 */
	public static List<Role> findRoles(String query, Guild guild) {
		String text = query.toLowerCase().trim();

		List<Role> found = new ArrayList<>();
		found.addAll(findRolesFromMentions(guild.getRoleCache(), text));
		found.addAll(findRolesFromStream(guild.getRoleCache().stream(), text));

		return Collections.unmodifiableList(found);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Private
	//////////////////////////////////////////////////////////////////////////////////////
	private static int compare(String name1, String name2, String query) {
		int compared = Integer.compare(getPriority(query, name1.toLowerCase().trim()),
			getPriority(query, name2.toLowerCase().trim()));

		if (compared == 0) {
			StringSimilarityService sss = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
			return Double.compare(sss.score(name1, query), sss.score(name2, query));
		}

		return compared;

	}

	private static int getPriority(String actual, String expected) {
		if (expected.equals(actual))
			return 4;

		if (expected.startsWith(actual))
			return 3;

		if (expected.endsWith(actual))
			return 2;

		if (expected.contains(actual))
			return 1;

		return 0;
	}

	private static List<User> findUsersFromStream(Stream<User> stream, String query) {
		List<User> bruteforce = stream.filter(u -> getPriority(query, u.getName().toLowerCase().trim()) > 0)
				.collect(Collectors.toList());

		bruteforce
				.sort((u1, u2) -> compare(u2.getName().toLowerCase().trim(), u1.getName().toLowerCase().trim(), query));

		return bruteforce;
	}

	private static List<Member> findMembersFromStream(Stream<Member> stream, String query) {
		List<Member> bruteforce = stream.filter(m -> getPriority(query, m.getEffectiveName().toLowerCase().trim()) > 0)
				.collect(Collectors.toList());

		bruteforce.sort((m1, m2) -> compare(m2.getEffectiveName().toLowerCase().trim(),
			m1.getEffectiveName().toLowerCase().trim(), query));

		return bruteforce;
	}

	private static List<Role> findRolesFromStream(Stream<Role> stream, String query) {
		List<Role> bruteforce = stream.filter(r -> getPriority(query, r.getName().toLowerCase().trim()) > 0)
				.collect(Collectors.toList());

		bruteforce
				.sort((r1, r2) -> compare(r2.getName().toLowerCase().trim(), r1.getName().toLowerCase().trim(), query));

		return bruteforce;
	}

	private static List<User> findUsersFromMentions(SnowflakeCacheView<User> index, String query) {
		List<User> matched = new ArrayList<>();

		Matcher matcher = MentionType.USER.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				User u = index.getElementById(matcher.group(1));
				if (u != null && !matched.contains(u))
					matched.add(u);
			} catch (NumberFormatException ignore) {
				// We can safely ignore the fact that the string wasn't a number
			}
		}

		return matched;
	}

	private static List<Member> findMembersFromMentions(MemberCacheView index, String query) {
		List<Member> matched = new ArrayList<>();

		Matcher matcher = MentionType.USER.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				Member u = index.getElementById(matcher.group(1));
				if (u != null && !matched.contains(u))
					matched.add(u);
			} catch (NumberFormatException ignore) {
				// We can safely ignore the fact that the string wasn't a number
			}
		}

		return matched;
	}

	private static List<Role> findRolesFromMentions(SnowflakeCacheView<Role> index, String query) {
		List<Role> matched = new ArrayList<>();

		Matcher matcher = MentionType.USER.getPattern().matcher(query);

		while (matcher.find()) {
			try {
				Role u = index.getElementById(matcher.group(1));
				if (u != null && !matched.contains(u))
					matched.add(u);
			} catch (NumberFormatException ignore) {
				// We can safely ignore the fact that the string wasn't a number
			}
		}

		return matched;
	}

}
