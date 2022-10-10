/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.spi.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * date utils
 */
public class DateUtils {

    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;

    /**
     * a default datetime formatter for the timestamp
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    private DateUtils() {
        throw new UnsupportedOperationException("Construct DateUtils");
    }

    /**
     * @param timeMillis timeMillis like System.currentTimeMillis()
     * @return string formatted as yyyy-MM-dd HH:mm:ss
     */
    public static String formatTimeStamp(long timeMillis) {
        return formatTimeStamp(timeMillis, DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * @param timeMillis timeMillis like System.currentTimeMillis()
     * @param dateTimeFormatter expect formatter, like yyyy-MM-dd HH:mm:ss
     * @return formatted string
     */
    public static String formatTimeStamp(long timeMillis, DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(dateTimeFormatter);
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis),
                ZoneId.systemDefault()));
    }

    /**
     * date to local datetime
     *
     * @param date date
     * @return local datetime
     */
    private static LocalDateTime date2LocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * local datetime to date
     *
     * @param localDateTime local datetime
     * @return date
     */
    private static Date localDateTime2Date(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * get the formatted date string
     *
     * @param date date
     * @param format e.g. yyyy-MM-dd HH:mm:ss
     * @return date string
     */
    public static String format(Date date, String format) {
        return format(date2LocalDateTime(date), format);
    }

    /**
     * get the formatted date string
     *
     * @param localDateTime local data time
     * @param format yyyy-MM-dd HH:mm:ss
     * @return date string
     */
    public static String format(LocalDateTime localDateTime, String format) {
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * convert string to date and time
     *
     * @param date date
     * @param format format
     * @return date
     */
    public static Date parse(String date, String format) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            return localDateTime2Date(ldt);
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    /**
     * convert date str to yyyy-MM-dd HH:mm:ss format
     *
     * @param str date string
     * @return yyyy-MM-dd HH:mm:ss format
     */
    public static Date stringToDate(String str) {
        return parse(str, Constants.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * get seconds between two dates
     *
     * @param d1 date1
     * @param d2 date2
     * @return differ seconds
     */
    public static long differSec(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return 0;
        }
        return (long) Math.ceil(differMs(d1, d2) / 1000.0);
    }

    /**
     * get ms between two dates
     *
     * @param d1 date1
     * @param d2 date2
     * @return differ ms
     */
    public static long differMs(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime());
    }

    /**
     * get the date of the specified date in the days before and after
     *
     * @param date date
     * @param day  day
     * @return the date of the specified date in the days before and after
     */
    public static Date getSomeDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * get the hour of day.
     *
     * @param date date
     * @return hour of day
     */
    public static int getHourIndex(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * compare two dates
     *
     * @param future future date
     * @param old old date
     * @return true if future time greater than old time
     */
    public static boolean compare(Date future, Date old) {
        return future.getTime() > old.getTime();
    }

    /**
     * convert schedule string to date
     *
     * @param schedule schedule
     * @return convert schedule string to date
     */
    public static Date getScheduleDate(String schedule) {
        return stringToDate(schedule);
    }

    /**
     * get monday
     * <p>
     * note: Set the first day of the week to Monday, the default is Sunday
     *
     * @param date date
     * @return get monday
     */
    public static Date getMonday(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return cal.getTime();
    }

    /**
     * get sunday
     * <p>
     * note: Set the first day of the week to Monday, the default is Sunday
     *
     * @param date date
     * @return get sunday
     */
    public static Date getSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return cal.getTime();
    }

    /**
     * get first day of month
     *
     * @param date date
     * @return first day of month
     */
    public static Date getFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    /**
     * get some hour of day
     *
     * @param date       date
     * @param offsetHour hours
     * @return some hour of day
     */
    public static Date getSomeHourOfDay(Date date, int offsetHour) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + offsetHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * get last day of month
     *
     * @param date date
     * @return get last day of month
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);

        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    /**
     * return YYYY-MM-DD 00:00:00
     *
     * @param inputDay date
     * @return start day
     */
    public static Date getStartOfDay(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * return YYYY-MM-DD 23:59:59
     *
     * @param inputDay day
     * @return end of day
     */
    public static Date getEndOfDay(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * return YYYY-MM-DD 00:00:00
     *
     * @param inputDay day
     * @return start of hour
     */
    public static Date getStartOfHour(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * return YYYY-MM-DD 23:59:59
     *
     * @param inputDay day
     * @return end of hour
     */
    public static Date getEndOfHour(Date inputDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDay);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * get current date
     *
     * @return current date
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date addMonths(Date date, int amount) {
        return add(date, 2, amount);
    }

    public static Date addDays(Date date, int amount) {
        return add(date, 5, amount);
    }

    public static Date addMinutes(Date date, int amount) {
        return add(date, 12, amount);
    }

    /**
     * get date
     *
     * @param date date
     * @param calendarField calendarField
     * @param amount amount
     * @return date
     */
    public static Date add(final Date date, final int calendarField, final int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * starting from the current time, get how many seconds are left before the target time.
     * targetTime = baseTime + intervalSeconds
     *
     * @param baseTime base time
     * @param intervalSeconds a period of time
     * @return the number of seconds
     */
    public static long getRemainTime(Date baseTime, long intervalSeconds) {
        if (baseTime == null) {
            return 0;
        }
        long usedTime = (System.currentTimeMillis() - baseTime.getTime()) / 1000;
        return intervalSeconds - usedTime;
    }

    /**
     * get timezone by timezoneId
     */
    public static TimeZone getTimezone(String timezoneId) {
        if (StringUtils.isEmpty(timezoneId)) {
            return null;
        }
        return TimeZone.getTimeZone(timezoneId);
    }

    public static String getTimestampString() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * transform timeStamp to local date
     *
     * @param timeStamp time stamp (milliseconds)
     * @return local date
     */
    public static @Nullable
    Date timeStampToDate(long timeStamp) {
        return timeStamp <= 0L ? null : new Date(timeStamp);
    }
}
