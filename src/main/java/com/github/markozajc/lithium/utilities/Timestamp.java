package com.github.markozajc.lithium.utilities;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Timestamp {

	private Timestamp() {}

	public static String formatTimestamp(long timestamp, String pattern) {
		return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime()
				.format(DateTimeFormatter.ofPattern(pattern));
	}

	public static String formatTimestamp(OffsetDateTime timestamp, String pattern) {
		return formatTimestamp(timestamp.toInstant().toEpochMilli(), pattern);
	}

}
