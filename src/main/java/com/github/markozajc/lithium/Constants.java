package com.github.markozajc.lithium;

import java.awt.Color;
import java.util.function.Consumer;

import com.google.gson.Gson;

public class Constants {

	private Constants() {}

	// Colors
	public static final Color LITHIUM = new Color(73, 140, 255);
	public static final Color GREEN = new Color(0, 255, 0);
	public static final Color YELLOW = new Color(255, 255, 0);
	public static final Color RED = new Color(255, 0, 0);
	public static final Color GRAY = new Color(198, 198, 198);
	public static final Color NONE = new Color(79, 84, 92);

	// Status emojis
	public static final String ACCEPT_EMOJI = "\u2705";
	public static final String DENY_EMOJI = "\u274E";
	public static final String FAILURE_EMOJI = "\u274C";

	// Misc
	public static final String LITHIUM_VERSION = "0.1";
	public static final Gson GSON = new Gson();
	public static final Consumer<Throwable> EMPTY_FAIL_CONSUMER = e -> {};
	private static final String[] MARKDOWN = {
			"`", "*", "_", "~", "|"
	};

	public static String[] getMarkdown() {
		return MARKDOWN.clone();
	}

}
