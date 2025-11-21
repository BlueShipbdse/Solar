package com.blueship.solar.util;

import com.blueship.solar.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Converts ticks to time in Minecraft's Time format.
 * 24000 Ticks -> 1 MC Day
 */
public class TimeUtil {
    public static final float TICKS_PER_HOUR = Constants.DAYLIGHT_CYCLE / 24F;
    public static final float TICKS_PER_MINUTE = TICKS_PER_HOUR / 60F;
    public static final float TICKS_PER_SECOND = TICKS_PER_MINUTE / 60F;


    /**
     * Converts days to ticks.
     *
     * @param days the amount of days to convert to ticks
     * @return the amount of ticks converted from days
     */
    public static long fromDays(long days) {
        return days * Constants.DAYLIGHT_CYCLE;
    }

    /**
     * Converts hours to ticks.
     * Result is rounded down.
     *
     * @param hours the amount of hours to convert to ticks
     * @return the amount of ticks converted from hours
     */
    public static long fromHours(long hours) {
        return (long) (hours * TICKS_PER_HOUR);
    }

    /**
     * Converts minutes to ticks.
     *
     * @param minutes the amount of minutes to convert to ticks
     * @return the amount of ticks converted from minutes
     */
    public static long fromMinutes(long minutes) {
        return (long) (minutes * TICKS_PER_MINUTE);
    }

    /**
     * Converts seconds to ticks.
     *
     * @param seconds the amount of seconds to convert to ticks
     * @return the amount of ticks converted from seconds
     */
    public static long fromSeconds(long seconds) {
        return (long) (seconds * TICKS_PER_SECOND);
    }

    /**
     * Gets the amount of days from an amount of ticks.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of days
     */
    public static long toDays(long ticks) {
        return ticks / Constants.DAYLIGHT_CYCLE;
    }

    /**
     * Gets the amount of days in that year of tick.
     *
     * @param ticks the amount of ticks to calculate with
     * @return the amount of days
     */
    public static long toDaysPart(long ticks) {
        return toDays(ticks) % 360;
    }


    /**
     * Gets the amount of hours from an amount of ticks.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of hours
     */
    public static long toHours(long ticks) {
        return (long) (ticks / TICKS_PER_HOUR);
    }

    /**
     * Gets the amount of hours in that day of tick.
     * <br>
     * Bounded between 0 and 24.
     * <br>
     * Effectively shows the current amount
     * of hours in that day.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of hours passed in the day
     */
    public static long toHoursPart(long ticks) {
        return toHours(ticks) % 24;
    }

    /**
     * Gets the amount of minutes from an amount of ticks.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of minutes
     */
    public static long toMinutes(long ticks) {
        return Math.round(ticks % TICKS_PER_HOUR / TICKS_PER_MINUTE);
    }

    /**
     * Gets the amount of minutes in that hour of tick.
     * <br>
     * Bounded between 0 and 60.
     * <br>
     * Effectively shows the current amount of minutes in
     * that hour.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of minutes passed in the hour
     */
    public static long toMinutesPart(long ticks) {
        return toMinutes(ticks) % 60;
    }

    /**
     * Gets the amount of seconds from an amount of ticks.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of seconds
     */
    public static long toSeconds(long ticks) {
        return (long) (ticks % TICKS_PER_HOUR % TICKS_PER_MINUTE / TICKS_PER_SECOND);
    }

    /**
     * Gets the amount of seconds in that minute of tick.
     * <br>
     * Bounded between 0 and 60.
     * <br>
     * Effectively shows the current amount of seconds in
     * that minute.
     * @param ticks the amount of ticks to calculate with
     * @return the amount of seconds passed in the minute
     */
    public static long toSecondsPart(long ticks) {
        return (toSeconds(ticks) % 60);
    }

    private static final int HOUR_OFFSET = 6;

    public static @NotNull String getTimeInHHMM(long ticks) {
        long hours = HOUR_OFFSET + toHoursPart(ticks);
        long minutes = toMinutesPart(ticks);
        boolean isAM = (hours / 12) % 2 == 0;
        hours %= 12;
        if (hours == 0) {
            hours = 12;
        }
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + (isAM ? " AM" : " PM");
    }

    public static @NotNull String getTimeInHHMMSS(long ticks) {
        long hours = HOUR_OFFSET + toHoursPart(ticks);
        long minutes = toMinutesPart(ticks);
        long seconds = toSecondsPart(ticks);
        boolean isAM = (hours / 12) % 2 == 0;
        hours %= 12;
        if (hours == 0) {
            hours = 12;
        }
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + (isAM ? " AM" : " PM");
    }
}
