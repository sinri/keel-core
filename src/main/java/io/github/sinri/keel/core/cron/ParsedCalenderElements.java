package io.github.sinri.keel.core.cron;

import javax.annotation.Nonnull;
import java.util.Calendar;

/**
 * @since 3.2.4
 * @since 4.0.0 moved out
 */
public class ParsedCalenderElements {
    public final int minute;
    public final int hour;
    public final int day;
    public final int month;
    public final int weekday;

    // debug use
    public final int second;

    /**
     * @since 4.0.0
     */
    public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday, int second) {
        this.minute = minute;
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.weekday = weekday;
        this.second = second;
    }

    /**
     * @since 4.0.0
     */
    public ParsedCalenderElements(int minute, int hour, int day, int month, int weekday) {
        this(minute, hour, day, month, weekday, 0);
    }

    public ParsedCalenderElements(@Nonnull Calendar currentCalendar) {
        minute = currentCalendar.get(Calendar.MINUTE);
        hour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        day = currentCalendar.get(Calendar.DAY_OF_MONTH);
        month = 1 + currentCalendar.get(Calendar.MONTH);// make JAN 1, ...
        weekday = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1; // make sunday 0, ...
        second = currentCalendar.get(Calendar.SECOND);
    }

    @Override
    public String toString() {
        return "(" + second + ") " + minute + " " + hour + " " + day + " " + month + " " + weekday;
    }
}
