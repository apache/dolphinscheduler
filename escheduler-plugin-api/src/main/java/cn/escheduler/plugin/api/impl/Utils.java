/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.api.impl;

import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public final class Utils {
    // we cache a split version of the templates to speed up formatting
    private static final Map<String, String[]> TEMPLATES = new ConcurrentHashMap<>();
    private static final String TOKEN = "{}";
    private static final String PADDING = "000000000000000000000000000000000000";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final String ISO8601_UTC_MASK = "yyyy-MM-dd'T'HH:mm'Z'";

    private static Callable<String> sdcIdCallable;

    Utils() {
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param value an object reference
     * @param varName the variable name to use in an exception message if the check fails
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code value} is null
     */
    public static <T> T checkNotNull(@Nullable T value, Object varName) {
        if (value == null) {
            throw new NullPointerException(format("{} cannot be null", varName));
        }
        return value;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param msg the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, @Nullable Object msg) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(msg));
        }
    }

    /**
     * Ensures the truth of an expression involving the state of the calling instance, but not
     * involving any parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param msg the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void checkState(boolean expression, @Nullable Object msg) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(msg));
        }
    }

    static String[] prepareTemplate(String template) {
        List<String> list = new ArrayList<>();
        int pos = 0;
        int nextToken = template.indexOf(TOKEN, pos);
        while (nextToken > -1 && pos < template.length()) {
            list.add(template.substring(pos, nextToken));
            pos = nextToken + TOKEN.length();
            nextToken = template.indexOf(TOKEN, pos);
        }
        list.add(template.substring(pos));
        return list.toArray(new String[list.size()]);
    }

    // fast version of SLF4J MessageFormat.format(), uses {} tokens, no escaping is supported, no array content printing either.
    public static String format(String template, Object... args) {
        String[] templateArr = TEMPLATES.get(template);
        if (templateArr == null) {
            // we may have a race condition here but the end result is idempotent
            templateArr = prepareTemplate(template);
            TEMPLATES.put(template, templateArr);
        }
        StringBuilder sb = new StringBuilder(template.length() * 2);
        for (int i = 0; i < templateArr.length; i++) {
            sb.append(templateArr[i]);
            if (args != null && i < templateArr.length - 1) {
                sb.append((i < args.length) ? args[i] : TOKEN);
            }
        }
        return sb.toString();
    }

    //format with lazy-eval
    public static Object formatL(final String template, final Object... args) {
        return new Object() {
            @Override
            public String toString() {
                return format(template, args);
            }
        };
    }

    public static String intToPaddedString(int value, int pad) {
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        int padding = pad - sb.length();
        if (padding > 0) {
            sb.insert(0, PADDING.subSequence(0, padding));
        }
        return sb.toString();
    }

    private static DateFormat getISO8601DateFormat() {
        DateFormat dateFormat = new SimpleDateFormat(ISO8601_UTC_MASK);
        // Stricter parsing to prevent dates such as 2011-12-50T01:00Z (December 50th) from matching
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(UTC);
        return dateFormat;
    }

    public static Date parse(String str) throws ParseException {
        return getISO8601DateFormat().parse(str);
    }

    public static ZonedDateTime parseZoned(String str) {
        return ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static String format(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Given an integer, return a string that is in an approximate, but human
     * readable format.
     * It uses the bases 'KiB', 'MiB', and 'GiB' for 1024, 1024**2, and 1024**3.
     * @param number the number to format
     * @return a human readable form of the integer
     */
    public static String humanReadableInt(long number) {
        DecimalFormat oneDecimal = new DecimalFormat("0.0");
        long absNumber = Math.abs(number);
        double result;
        String prefix = number < 0 ? "-" : "";
        String suffix;
        if (absNumber < 1000) {
            // since no division has occurred, don't format with a decimal point
            return number + " bytes";
        } else if (absNumber < 1000.0 * 1000.0) {
            result = number / 1000.0;
            suffix = " KB";
        } else if (absNumber < 1000.0 * 1000.0 * 1000.0) {
            result = number / (1000.0 * 1000.0);
            suffix = " MB";
        } else {
            result = number / (1000.0 * 1000.0 * 1000.0);
            suffix = " GB";
        }
        return prefix + oneDecimal.format(result) + suffix;
    }

    public static void setSdcIdCallable(Callable<String> callable) {
        sdcIdCallable = callable;
    }

    public static String getSdcId() {
        Utils.checkState(sdcIdCallable != null, "sdcIdCallable has not been set");
        try {
            return sdcIdCallable.call();
        } catch (Exception ex) {
            throw new RuntimeException(Utils.format("SDC ID Callable threw an unexpected exception: {}", ex.toString(), ex));
        }
    }

}
