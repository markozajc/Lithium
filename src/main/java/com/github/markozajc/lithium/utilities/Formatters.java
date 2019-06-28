package com.github.markozajc.lithium.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for formatting text into various forms.
 *
 * @author Marko Zajc
 */
public class Formatters {

	private Formatters() {}

	/**
	 * Gets text as camel. For example, this will turn 'hello world' into 'helloWorld',
	 * 'FOO BAR' into 'fooBar', etc.
	 *
	 * @param text
	 *            The text to format.
	 * @return The given text formatted as camel case.
	 */
	public static String getAsCamel(String text) {
		String[] spaces = text.split(" ");

		StringBuilder sb = new StringBuilder(spaces[0].toLowerCase());
		for (int i = 0; i < spaces.length; i++) {
			if (i > 0) {
				sb.append(getFirstUpper(spaces[1].toLowerCase()));
			}
		}

		return sb.toString();
	}

	/**
	 * Turns text's first letter into upper case. For example, this will turn
	 * 'helloWorld' into 'HelloWorld'.
	 *
	 * @param text
	 *            The text to format.
	 * @return The given text with first letter in upper case.
	 */
	public static String getFirstUpper(String text) {
		String[] splitted = text.split("", 2);
		return splitted[0].toUpperCase() + splitted[1];
	}

	/**
	 * This will word-wrap text to match preferred number of characters.
	 *
	 * @param text
	 *            The text to wrap.
	 * @param chars
	 *            The maximum amount of characters in a line.
	 * @return A sorted {@link List} of lines of text.
	 */
	public static List<String> wrap(String text, int chars) {
		List<String> lines = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		String[] splitted = text.split(" ");
		for (String part : splitted) {
			sb.append(part + " ");

			if (sb.length() > chars) {
				lines.add(sb.toString());
				sb = new StringBuilder();
			}
		}

		if (sb.length() != 0)
			lines.add(sb.toString());

		return lines;
	}
}