package ru.tehkode.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Time unit definitions for interval parsing.
 *
 * <p>Each constant represents a duration in seconds and one or more text labels that
 * {@link DateUtils} recognizes when parsing human-readable intervals.</p>
 */
public enum Interval {
    /** Unknown or unrecognized unit; maps to zero seconds. */
    UNKNOWN(0),
    /** One second ({@code "second"}, {@code "s"}). */
    SECOND(1, "second", "s"),
    /** One minute ({@code "minute"}, {@code "m"}). */
    MINUTE(60, "minute", "m"),
    /** One hour ({@code "hour"}, {@code "h"}). */
    HOUR(3600, "hour", "h"),
    /** One day ({@code "day"}, {@code "d"}). */
    DAY(86400, "day", "d"),
    /** One week ({@code "week"}, {@code "w"}). */
    WEEK(604800, "week", "w"),
    /** One month, approximated as 30 days ({@code "month"}). */
    MONTH(2592000, "month"),
    /** One year, approximated as 360 days ({@code "year"}). */
    YEAR(31104000, "year");

    private final int value;

    private final String[] labels;

    /**
     * Creates an interval unit with a duration and optional text labels.
     *
     * @param seconds duration of one unit in seconds
     * @param labels  recognized text labels for this unit (case-insensitive lookup)
     */
    private Interval(int seconds, String... labels) {
        // save into private final properties
        this.value = seconds;
        this.labels = labels;
    }

    /**
     * Returns the duration of one unit in seconds.
     *
     * @return seconds per unit
     */
    public int value() {
        return this.value;
    }

    /**
     * Returns the text labels recognized for this unit.
     *
     * @return array of label strings; never {@code null} but may be empty
     */
    public String[] labels() {
        return this.labels;
    }

    /**
     * Resolves an interval unit by its text label.
     *
     * @param label unit label to look up (for example {@code "h"} or {@code "hour"})
     * @return matching interval, or {@link #UNKNOWN} if the label is not recognized
     */
    public static Interval byLabel(String label) {
        if(intervalMap.containsKey(label)) {
            return intervalMap.get(label);
        } else {
            return UNKNOWN;
        }
    }

    private final static Map<String, Interval> intervalMap = new HashMap<>();

    static {
        for(Interval type : Interval.values()) {
            for(String label : type.labels()) {
                intervalMap.put(label, type);
            }
        }
    }

}
