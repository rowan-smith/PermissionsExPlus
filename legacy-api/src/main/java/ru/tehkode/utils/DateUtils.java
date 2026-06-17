package ru.tehkode.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for parsing human-readable time intervals into seconds.
 *
 * <p>Supports plain integer seconds (for example {@code "3600"}) and compound expressions with unit
 * suffixes (for example {@code "1h 30m"}).</p>
 */
public class DateUtils {
	/** Pattern matching numeric intervals with optional unit labels. */
	protected final static Pattern INTERVAL_PATTERN = Pattern.compile("((?:\\d+)|(?:\\d+\\.\\d+))\\s*(second|minute|hour|day|week|month|year|s|m|h|d|w)", Pattern.CASE_INSENSITIVE);


	/**
	 * Parses a time interval string into total seconds.
	 *
	 * <p>If {@code arg} is a plain integer, it is interpreted as seconds directly. Otherwise each
	 * {@code number + unit} token in the string is summed (for example {@code "1h 30m"} → 5400).</p>
	 *
	 * @param arg interval expression or plain second count; must not be {@code null}
	 * @return total interval in seconds
	 * @throws NumberFormatException if a numeric token cannot be parsed
	 */
	public static int parseInterval(String arg) {
		if (arg.matches("^\\d+$")) {
			return Integer.parseInt(arg);
		}

		Matcher match = INTERVAL_PATTERN.matcher(arg);

		int interval = 0;

		while (match.find()) {
			interval += Math.round(Float.parseFloat(match.group(1)) * getSecondsIn(match.group(2)));
		}

		return interval;
	}

	/**
	 * Returns the number of seconds in one unit of the given interval type label.
	 *
	 * @param type unit label (for example {@code "hour"}, {@code "h"}); case-insensitive
	 * @return seconds per unit, or {@code 0} if the label is unknown
	 */
	public static int getSecondsIn(String type) {
		type = type.toLowerCase();

		return Interval.byLabel(type).value();
    }
}
