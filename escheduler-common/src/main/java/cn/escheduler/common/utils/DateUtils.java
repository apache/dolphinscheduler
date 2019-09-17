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
package cn.escheduler.common.utils;

import cn.escheduler.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * date utils
 */
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * <code>java.util.Date</code> to <code>java.time.LocalDateTime</code>
     * use default zone
     * @param date
     * @return
     */
    private static LocalDateTime date2LocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * <code>java.time.LocalDateTime</code> to <code>java.util.Date</code>
     * use default zone
     * @param localDateTime
     * @return
     */
    private static Date localDateTime2Date(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * @return get the formatted date string for the current time
     */
    public static String getCurrentTime() {
        return getCurrentTime(Constants.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * @param format
     * @return get the date string in the specified format of the current time
     */
    public static String getCurrentTime(String format) {
//        return new SimpleDateFormat(format).format(new Date());
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * @param date
     * @param format e.g. yyyy-MM-dd HH:mm:ss
     * @return get the formatted date string
     */
    public static String format(Date date, String format) {
//        return new SimpleDateFormat(format).format(date);
        return format(date2LocalDateTime(date), format);
    }

    /**
     * @param localDateTime
     * @param format        e.g. yyyy-MM-dd HH:mm:ss
     * @return get the formatted date string
     */
    public static String format(LocalDateTime localDateTime, String format) {
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * @param date
     * @return convert time to yyyy-MM-dd HH:mm:ss format
     */
    public static String dateToString(Date date) {
        return format(date, Constants.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * @param instant
     * @return convert time to yyyy-MM-dd HH:mm:ss format with default time zone
     */
    public static String instantToString(Instant instant) {
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(Constants.YYYY_MM_DD_HH_MM_SS));
    }


    /**
     * @param date
     * @return convert string to date and time
     */
    public static Date parse(String date, String format) {
        try {
            //     return new SimpleDateFormat(format).parse(date);
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            return localDateTime2Date(ldt);
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    /**
     * @param date
     * @param format
     * @return convert string to instant
     */
    public static Instant parseInstant(String date,String format) {
        try {
            //     return new SimpleDateFormat(format).parse(date);
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            logger.error("error while parse date:" + date, e);
        }
        return null;
    }

    /**
     * convert date str to yyyy-MM-dd HH:mm:ss format
     *
     * @param str
     * @return
     */
    public static Date stringToDate(String str) {
        return parse(str, Constants.YYYY_MM_DD_HH_MM_SS);
    }

    public static Instant stringToInstant(String str) {
        return parseInstant(str, Constants.YYYY_MM_DD_HH_MM_SS);
    }
    /**
     * get seconds between two dates
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long differSec(Date d1, Date d2) {
        return (long) Math.ceil(differMs(d1, d2) / 1000.0);
    }

    /**
     * get ms between two dates
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long differMs(Date d1, Date d2) {
        return Math.abs(d1.getTime() - d2.getTime());
    }


    /**
     * get hours between two dates
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long diffHours(Date d1, Date d2) {
        return (long) Math.ceil(diffMin(d1, d2) / 60.0);
    }

    /**
     * get minutes between two dates
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long diffMin(Date d1, Date d2) {
        return (long) Math.ceil(differSec(d1, d2) / 60.0);
    }


    /**
     * get the date of the specified date in the days before and after
     *
     * @param date
     * @param day
     * @return
     */
    public static Date getSomeDay(Date date, int day) {
        ZonedDateTime zonedDateTime =
                date.toInstant().atZone(ZoneId.systemDefault()).plusDays(day);
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * compare two dates
     *
     * @param future
     * @param old
     * @return
     */
    public static boolean compare(Date future, Date old) {
        return future.getTime() > old.getTime();
    }

    /**
     * convert schedule string to date
     *
     * @param schedule
     * @return
     */
    public static Date getScheduleDate(String schedule) {
        return stringToDate(schedule);
    }

    /**
     * format time to readable
     *
     * @param ms
     * @return
     */
    public static String format2Readable(long ms) {

        long days = ms / (1000 * 60 * 60 * 24);
        long hours = (ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;

        return String.format("%02d %02d:%02d:%02d", days, hours, minutes, seconds);

    }

    /**
     * get monday
     */
    public static Date getMonday(Date date) {
        Instant instant = date.toInstant().atZone(ZoneId.systemDefault()).with(DayOfWeek.MONDAY).toInstant();
        return Date.from(instant);
    }

    /**
     * get sunday
     */
    public static Date getSunday(Date date) {
        Instant instant = date.toInstant().atZone(ZoneId.systemDefault()).with(DayOfWeek.SUNDAY).toInstant();
        return Date.from(instant);
    }

    /**
     * get first day of month
     */
    public static Date getFirstDayOfMonth(Date date) {
        Instant instant = date.toInstant().atZone(ZoneId.systemDefault()).withDayOfMonth(1).toInstant();
        return Date.from(instant);
    }

    /**
     * get several hours ago
     */

    public static Date getSomeHourOfDay(Date date, int hours) {
        Instant instant =
                date.toInstant().atZone(ZoneId.systemDefault())
                        .minusHours(hours)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .toInstant();

        return Date.from(instant);
    }

    /**
     * get last day of month
     */
    public static Date getLastDayOfMonth(Date date) {
        Instant instant =
                date.toInstant().atZone(ZoneId.systemDefault())
                        .withDayOfMonth(1)
                        .plusMonths(1)
                        .minusDays(1)
                        .toInstant();

        return Date.from(instant);
    }

    /**
     * return YYYY-MM-DD 00:00:00
     *
     * @param inputDay
     * @return
     */
    public static Date getStartOfDay(Date inputDay) {
        Instant instant =
                inputDay.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant();

        return Date.from(instant);
    }

    /**
     * return YYYY-MM-DD 23:59:59
     *
     * @param inputDay
     * @return
     */
    public static Date getEndOfDay(Date inputDay) {
        Instant instant =
                inputDay.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .plusDays(1)
                        .minusSeconds(1)
                        .toInstant();

        return Date.from(instant);
    }

    /**
     * return YYYY-MM-DD 00:00:00
     *
     * @param inputDay
     * @return
     */
    public static Date getStartOfHour(Date inputDay) {
        Instant instant =
                inputDay.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0).toInstant();

        return Date.from(instant);
    }

    /**
     * return YYYY-MM-DD 23:59:59
     *
     * @param inputDay
     * @return
     */
    public static Date getEndOfHour(Date inputDay) {
        Instant instant =
                inputDay.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .plusHours(1)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .minusSeconds(1).toInstant();

        return Date.from(instant);
    }


}
