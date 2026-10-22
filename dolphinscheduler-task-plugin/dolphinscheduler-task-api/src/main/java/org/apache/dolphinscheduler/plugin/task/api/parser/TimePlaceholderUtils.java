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

package org.apache.dolphinscheduler.plugin.task.api.parser;

import static org.apache.commons.lang3.time.DateUtils.addWeeks;
import static org.apache.dolphinscheduler.common.utils.DateUtils.addDays;
import static org.apache.dolphinscheduler.common.utils.DateUtils.addMinutes;
import static org.apache.dolphinscheduler.common.utils.DateUtils.addMonths;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.ADD_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.ADD_MONTHS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.ADD_STRING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DIVISION_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DIVISION_STRING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.HYPHEN;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAST_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LEFT_BRACE_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LEFT_BRACE_STRING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MONTH_BEGIN;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MONTH_END;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MONTH_FIRST_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MONTH_LAST_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MULTIPLY_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MULTIPLY_STRING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.N;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.P;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_FORMAT_TIME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RIGHT_BRACE_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SUBTRACT_CHAR;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SUBTRACT_STRING;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.THIS_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TIMESTAMP;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.WEEK_BEGIN;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.WEEK_END;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.WEEK_FIRST_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.WEEK_LAST_DAY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.YEAR_WEEK;

import org.apache.dolphinscheduler.common.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimePlaceholderUtils {

    /**
     * calculate expression's value
     *
     * @param expression expression
     * @return expression's value
     */
    public static Integer calculate(String expression) {
        expression = StringUtils.trim(expression);
        expression = convert(expression);

        List<String> result = string2List(expression);
        result = convert2SuffixList(result);

        return calculate(result);
    }

    /**
     * Change the sign in the expression to P (positive) N (negative)
     *
     * @param expression
     * @return eg. "-3+-6*(+8)-(-5) -> S3+S6*(P8)-(S5)"
     */
    private static String convert(String expression) {
        char[] arr = expression.toCharArray();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == SUBTRACT_CHAR) {
                if (i == 0) {
                    arr[i] = N;
                } else {
                    char c = arr[i - 1];
                    if (c == ADD_CHAR || c == SUBTRACT_CHAR || c == MULTIPLY_CHAR || c == DIVISION_CHAR
                            || c == LEFT_BRACE_CHAR) {
                        arr[i] = N;
                    }
                }
            } else if (arr[i] == ADD_CHAR) {
                if (i == 0) {
                    arr[i] = P;
                } else {
                    char c = arr[i - 1];
                    if (c == ADD_CHAR || c == SUBTRACT_CHAR || c == MULTIPLY_CHAR || c == DIVISION_CHAR
                            || c == LEFT_BRACE_CHAR) {
                        arr[i] = P;
                    }
                }
            }
        }

        return new String(arr);
    }

    /**
     * to suffix expression
     *
     * @param srcList
     * @return
     */
    private static List<String> convert2SuffixList(List<String> srcList) {
        List<String> result = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (int i = 0; i < srcList.size(); i++) {
            if (Character.isDigit(srcList.get(i).charAt(0))) {
                result.add(srcList.get(i));
            } else {
                switch (srcList.get(i).charAt(0)) {
                    case LEFT_BRACE_CHAR:
                        stack.push(srcList.get(i));
                        break;
                    case RIGHT_BRACE_CHAR:
                        while (!LEFT_BRACE_STRING.equals(stack.peek())) {
                            result.add(stack.pop());
                        }
                        stack.pop();
                        break;
                    default:
                        while (!stack.isEmpty() && compare(stack.peek(), srcList.get(i))) {
                            result.add(stack.pop());
                        }
                        stack.push(srcList.get(i));
                        break;
                }
            }
        }

        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    /**
     * Calculate the suffix expression
     *
     * @param result
     * @return
     */
    private static Integer calculate(List<String> result) {
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < result.size(); i++) {
            if (Character.isDigit(result.get(i).charAt(0))) {
                stack.push(Integer.parseInt(result.get(i)));
            } else {
                Integer backInt = stack.pop();
                Integer frontInt = 0;
                char op = result.get(i).charAt(0);

                if (!(op == P || op == N)) {
                    frontInt = stack.pop();
                }

                Integer res = 0;
                switch (result.get(i).charAt(0)) {
                    case P:
                        res = frontInt + backInt;
                        break;
                    case N:
                        res = frontInt - backInt;
                        break;
                    case ADD_CHAR:
                        res = frontInt + backInt;
                        break;
                    case SUBTRACT_CHAR:
                        res = frontInt - backInt;
                        break;
                    case MULTIPLY_CHAR:
                        res = frontInt * backInt;
                        break;
                    case DIVISION_CHAR:
                        res = frontInt / backInt;
                        break;
                    default:
                        break;
                }
                stack.push(res);
            }
        }

        return stack.pop();
    }

    /**
     * string to list
     *
     * @param expression
     * @return list
     */
    private static List<String> string2List(String expression) {
        List<String> result = new ArrayList<>();
        String num = "";
        for (int i = 0; i < expression.length(); i++) {
            if (Character.isDigit(expression.charAt(i))) {
                num = num + expression.charAt(i);
            } else {
                if (!StringUtils.isEmpty(num)) {
                    result.add(num);
                }
                result.add(expression.charAt(i) + "");
                num = "";
            }
        }

        if (!num.isEmpty()) {
            result.add(num);
        }

        return result;
    }

    /**
     * compare loginUser level
     *
     * @param peek
     * @param cur
     * @return true or false
     */
    private static boolean compare(String peek, String cur) {
        if (MULTIPLY_STRING.equals(peek) && (DIVISION_STRING.equals(cur) || MULTIPLY_STRING.equals(cur)
                || ADD_STRING.equals(cur) || SUBTRACT_STRING.equals(cur))) {
            return true;
        } else if (DIVISION_STRING.equals(peek) && (DIVISION_STRING.equals(cur) || MULTIPLY_STRING.equals(cur)
                || ADD_STRING.equals(cur) || SUBTRACT_STRING.equals(cur))) {
            return true;
        } else if (ADD_STRING.equals(peek) && (ADD_STRING.equals(cur) || SUBTRACT_STRING.equals(cur))) {
            return true;
        } else {
            return SUBTRACT_STRING.equals(peek) && (ADD_STRING.equals(cur) || SUBTRACT_STRING.equals(cur));
        }

    }

    /**
     * Format time expression with given date, the date cannot be null.
     * <p> If the expression is not a time expression, return the original expression.
     *
     */
    public static String formatTimeExpression(final String timeExpression, final Date date,
                                              final boolean ignoreInvalidExpression) {
        // After N years: $[add_months(yyyyMMdd,12*N)], the first N months: $[add_months(yyyyMMdd,-N)], etc
        if (date == null) {
            throw new IllegalArgumentException("Cannot parse the expression: " + timeExpression + ", date is null");
        }
        if (StringUtils.isEmpty(timeExpression)) {
            if (ignoreInvalidExpression) {
                return timeExpression;
            }
            throw new IllegalArgumentException("Cannot format the date" + date + " with null timeExpression");
        }
        try {
            if (timeExpression.startsWith(TIMESTAMP)) {
                return calculateTimeStamp(timeExpression, date);
            }
            if (timeExpression.startsWith(YEAR_WEEK)) {
                return calculateYearWeek(timeExpression, date);
            }
            return calcTimeExpression(timeExpression, date);
        } catch (Exception e) {
            if (ignoreInvalidExpression) {
                return timeExpression;
            }
            throw new IllegalArgumentException("Unsupported placeholder expression: " + timeExpression, e);
        }
    }

    /**
     * get week of year
     * @param expression expression
     * @param date       date
     * @return week of year
     */
    private static String calculateYearWeek(final String expression, final Date date) {

        String dataFormat = expression.substring(YEAR_WEEK.length() + 1, expression.length() - 1);

        String targetDate = "";
        try {
            if (dataFormat.contains(COMMA)) {
                String param1 = dataFormat.split(COMMA)[0];
                String param2 = dataFormat.split(COMMA)[1];
                dataFormat = param1;

                Map.Entry<Date, String> baseEntry = calcFormatOffset(dataFormat, date);
                targetDate = transformYearWeek(baseEntry.getKey(), baseEntry.getValue(), calculate(param2));
            } else {
                Map.Entry<Date, String> baseEntry = calcFormatOffset(dataFormat, date);
                targetDate = transformYearWeek(baseEntry.getKey(), baseEntry.getValue(), 1);
            }
        } catch (Exception e) {
            throw new RuntimeException("expression not valid");
        }

        return targetDate;
    }

    /**
     * transform week of year
     * @param date date
     * @param format date_format,for example: yyyy-MM-dd / yyyyMMdd
     * @param weekDay day of week
     * @return date_string
     */
    private static String transformYearWeek(Date date, String format, int weekDay) {
        Calendar calendar = Calendar.getInstance();
        // Minimum number of days required for the first week of the year
        calendar.setMinimalDaysInFirstWeek(4);

        // By default ,Set Monday as the first day of the week
        switch (weekDay) {
            case 2:
                calendar.setFirstDayOfWeek(Calendar.TUESDAY);
                break;
            case 3:
                calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);
                break;
            case 4:
                calendar.setFirstDayOfWeek(Calendar.THURSDAY);
                break;
            case 5:
                calendar.setFirstDayOfWeek(Calendar.FRIDAY);
                break;
            case 6:
                calendar.setFirstDayOfWeek(Calendar.SATURDAY);
                break;
            case 7:
                calendar.setFirstDayOfWeek(Calendar.SUNDAY);
                break;
            default:
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                break;
        }
        calendar.setTimeInMillis(date.getTime());

        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        int year = calendar.get(Calendar.YEAR);

        String weekYearStr = "";
        if (weekOfYear < 10 && format.contains(HYPHEN)) {
            weekYearStr = String.format("%d%s0%d", year, HYPHEN, weekOfYear);
        } else if (weekOfYear >= 10 && format.contains(HYPHEN)) {
            weekYearStr = String.format("%d%s%d", year, HYPHEN, weekOfYear);
        } else if (weekOfYear < 10) {
            weekYearStr = String.format("%d0%d", year, weekOfYear);
        } else {
            weekYearStr = String.format("%d%d", year, weekOfYear);
        }

        return weekYearStr;
    }

    private static String calculateTimeStamp(final String expression, final Date date) {
        final String timeExpression = expression.substring(TIMESTAMP.length() + 1, expression.length() - 1);
        final String calculatedTimeExpression = calcTimeExpression(timeExpression, date);
        if (StringUtils.equals(expression, calculatedTimeExpression)) {
            return expression;
        }
        final Date timestamp = DateUtils.parse(calculatedTimeExpression, PARAMETER_FORMAT_TIME);
        if (timestamp == null) {
            throw new IllegalArgumentException("Cannot parse the expression: " + expression + " with date: " + date);
        }
        return String.valueOf(timestamp.getTime() / 1000);
    }

    /**
     * calculate time expresstion
     *
     * @param expression expresstion
     * @param date       date
     * @return map with date, date format
     */
    private static String calcTimeExpression(final String expression, final Date date) {
        final Map.Entry<Date, String> resultEntry;

        if (expression.startsWith(ADD_MONTHS)) {
            resultEntry = calcMonths(expression, date);
        } else if (expression.startsWith(MONTH_BEGIN)) {
            resultEntry = calcMonthBegin(expression, date);
        } else if (expression.startsWith(MONTH_END)) {
            resultEntry = calcMonthEnd(expression, date);
        } else if (expression.startsWith(WEEK_BEGIN)) {
            resultEntry = calcWeekStart(expression, date);
        } else if (expression.startsWith(WEEK_END)) {
            resultEntry = calcWeekEnd(expression, date);
        } else if (expression.startsWith(MONTH_FIRST_DAY)) {
            resultEntry = calcCustomDay(expression, MONTH_FIRST_DAY, date);
        } else if (expression.startsWith(MONTH_LAST_DAY)) {
            resultEntry = calcCustomDay(expression, MONTH_LAST_DAY, date);
        } else if (expression.startsWith(THIS_DAY)) {
            resultEntry = calcCustomDay(expression, THIS_DAY, date);
        } else if (expression.startsWith(LAST_DAY)) {
            resultEntry = calcCustomDay(expression, LAST_DAY, date);
        } else if (expression.startsWith(WEEK_FIRST_DAY)) {
            resultEntry = calcCustomDay(expression, WEEK_FIRST_DAY, date);
        } else if (expression.startsWith(WEEK_LAST_DAY)) {
            resultEntry = calcCustomDay(expression, WEEK_LAST_DAY, date);
        } else {
            resultEntry = calcMinutes(expression, date);
        }
        final Date finalDate = resultEntry.getKey();
        final String finalFormat = resultEntry.getValue();
        return DateUtils.format(finalDate, finalFormat);
    }

    /**
     * get first day of month
     *
     * @param expression expresstion
     * @param date       date
     * @return first day of month
     */
    public static Map.Entry<Date, String> calcMonthBegin(String expression, Date date) {
        String addMonthExpr = expression.substring(MONTH_BEGIN.length() + 1, expression.length() - 1);
        String[] params = addMonthExpr.split(COMMA);

        if (params.length == 2) {
            String dateFormat = params[0];
            String dayExpr = params[1];
            Map.Entry<Date, String> baseEntry = calcFormatOffset(dateFormat, date);
            Date baseDate = baseEntry.getKey();
            String cleanFormat = baseEntry.getValue();

            Integer day = calculate(dayExpr);
            Date targetDate = DateUtils.getFirstDayOfMonth(baseDate);
            targetDate = addDays(targetDate, day);

            return new AbstractMap.SimpleImmutableEntry<>(targetDate, cleanFormat);
        }

        throw new RuntimeException("expression not valid");
    }

    /**
     * get last day of month
     *
     * @param expression expresstion
     * @param date       date
     * @return last day of month
     */
    public static Map.Entry<Date, String> calcMonthEnd(String expression, Date date) {
        String addMonthExpr = expression.substring(MONTH_END.length() + 1, expression.length() - 1);
        String[] params = addMonthExpr.split(COMMA);

        if (params.length == 2) {
            String dateFormat = params[0];
            String dayExpr = params[1];
            Map.Entry<Date, String> baseEntry = calcFormatOffset(dateFormat, date);
            Date baseDate = baseEntry.getKey();
            String cleanFormat = baseEntry.getValue();

            Integer day = calculate(dayExpr);
            Date targetDate = DateUtils.getLastDayOfMonth(baseDate);
            targetDate = addDays(targetDate, day);

            return new AbstractMap.SimpleImmutableEntry<>(targetDate, cleanFormat);
        }

        throw new RuntimeException("expression not valid");
    }

    /**
     * calculate time expression
     * month first day month last day
     * @param expression expression
     * @param date       date
     * @return calculate time expression with date,format
     */
    public static Map.Entry<Date, String> calcCustomDay(String expression, String keyDate, Date date) {
        String dataFormat = "yyyy-MM-dd";
        Date targetDate = new Date();

        switch (keyDate) {
            case MONTH_FIRST_DAY:
                dataFormat = expression.substring(MONTH_FIRST_DAY.length() + 1, expression.length() - 1);
                String monthFirstOffset = null;
                if (dataFormat.contains(COMMA)) {
                    String[] parts = dataFormat.split(COMMA);
                    dataFormat = parts[0];
                    monthFirstOffset = parts[1];
                }
                Map.Entry<Date, String> monthFirstBaseEntry = calcFormatOffset(dataFormat, date);
                Date monthFirstBaseDate = monthFirstBaseEntry.getKey();
                dataFormat = monthFirstBaseEntry.getValue();

                if (monthFirstOffset != null) {
                    targetDate =
                            addMonths(DateUtils.getFirstDayOfMonth(monthFirstBaseDate), calculate(monthFirstOffset));
                } else {
                    targetDate = DateUtils.getFirstDayOfMonth(monthFirstBaseDate);
                }

                break;
            case MONTH_LAST_DAY:
                dataFormat = expression.substring(MONTH_LAST_DAY.length() + 1, expression.length() - 1);
                String monthLastOffset = null;
                if (dataFormat.contains(COMMA)) {
                    String[] parts = dataFormat.split(COMMA);
                    dataFormat = parts[0];
                    monthLastOffset = parts[1];
                }
                Map.Entry<Date, String> monthLastBaseEntry = calcFormatOffset(dataFormat, date);
                Date monthLastBaseDate = monthLastBaseEntry.getKey();
                dataFormat = monthLastBaseEntry.getValue();

                if (monthLastOffset != null) {
                    Date lastMonthDay = addMonths(monthLastBaseDate, calculate(monthLastOffset));
                    targetDate = DateUtils.getLastDayOfMonth(lastMonthDay);
                } else {
                    targetDate = DateUtils.getLastDayOfMonth(monthLastBaseDate);
                }
                break;
            case THIS_DAY:
                dataFormat = expression.substring(THIS_DAY.length() + 1, expression.length() - 1);
                Map.Entry<Date, String> thisDayBaseEntry = calcFormatOffset(dataFormat, date);
                targetDate = addDays(thisDayBaseEntry.getKey(), 0);
                dataFormat = thisDayBaseEntry.getValue();
                break;
            case LAST_DAY:
                dataFormat = expression.substring(LAST_DAY.length() + 1, expression.length() - 1);
                Map.Entry<Date, String> lastDayBaseEntry = calcFormatOffset(dataFormat, date);
                targetDate = addDays(lastDayBaseEntry.getKey(), -1);
                dataFormat = lastDayBaseEntry.getValue();
                break;
            case WEEK_FIRST_DAY:
                dataFormat = expression.substring(WEEK_FIRST_DAY.length() + 1, expression.length() - 1);
                String weekFirstOffset = null;
                if (dataFormat.contains(COMMA)) {
                    String[] parts = dataFormat.split(COMMA);
                    dataFormat = parts[0];
                    weekFirstOffset = parts[1];
                }
                Map.Entry<Date, String> weekFirstBaseEntry = calcFormatOffset(dataFormat, date);
                Date weekFirstBaseDate = weekFirstBaseEntry.getKey();
                dataFormat = weekFirstBaseEntry.getValue();

                if (weekFirstOffset != null) {
                    targetDate = addWeeks(DateUtils.getMonday(weekFirstBaseDate), calculate(weekFirstOffset));
                } else {
                    targetDate = addWeeks(DateUtils.getMonday(weekFirstBaseDate), 0);
                }
                break;
            case WEEK_LAST_DAY:
                dataFormat = expression.substring(WEEK_LAST_DAY.length() + 1, expression.length() - 1);
                String weekLastOffset = null;
                if (dataFormat.contains(COMMA)) {
                    String[] parts = dataFormat.split(COMMA);
                    dataFormat = parts[0];
                    weekLastOffset = parts[1];
                }
                Map.Entry<Date, String> weekLastBaseEntry = calcFormatOffset(dataFormat, date);
                Date weekLastBaseDate = weekLastBaseEntry.getKey();
                dataFormat = weekLastBaseEntry.getValue();

                if (weekLastOffset != null) {
                    targetDate = addWeeks(DateUtils.getSunday(weekLastBaseDate), calculate(weekLastOffset));
                } else {
                    targetDate = addWeeks(DateUtils.getSunday(weekLastBaseDate), 0);
                }
                break;
            default:
                break;
        }

        return new AbstractMap.SimpleImmutableEntry<>(targetDate, dataFormat);

    }

    /**
     * get first day of week
     *
     * @param expression expresstion
     * @param date       date
     * @return monday
     */
    public static Map.Entry<Date, String> calcWeekStart(String expression, Date date) {
        String addMonthExpr = expression.substring(WEEK_BEGIN.length() + 1, expression.length() - 1);
        String[] params = addMonthExpr.split(COMMA);

        if (params.length == 2) {
            String dateFormat = params[0];
            String dayExpr = params[1];
            Map.Entry<Date, String> baseEntry = calcFormatOffset(dateFormat, date);
            Date baseDate = baseEntry.getKey();
            String cleanFormat = baseEntry.getValue();

            Integer day = calculate(dayExpr);
            Date targetDate = DateUtils.getMonday(baseDate);
            targetDate = addDays(targetDate, day);
            return new AbstractMap.SimpleImmutableEntry<>(targetDate, cleanFormat);
        }

        throw new RuntimeException("expression not valid");
    }

    /**
     * get last day of week
     *
     * @param expression expresstion
     * @param date       date
     * @return last day of week
     */
    public static Map.Entry<Date, String> calcWeekEnd(String expression, Date date) {
        String addMonthExpr = expression.substring(WEEK_END.length() + 1, expression.length() - 1);
        String[] params = addMonthExpr.split(COMMA);

        if (params.length == 2) {
            String dateFormat = params[0];
            String dayExpr = params[1];
            Map.Entry<Date, String> baseEntry = calcFormatOffset(dateFormat, date);
            Date baseDate = baseEntry.getKey();
            String cleanFormat = baseEntry.getValue();

            Integer day = calculate(dayExpr);
            Date targetDate = DateUtils.getSunday(baseDate);
            targetDate = addDays(targetDate, day);

            return new AbstractMap.SimpleImmutableEntry<>(targetDate, cleanFormat);
        }

        throw new RuntimeException("Expression not valid");
    }

    /**
     * calc months expression
     *
     * @param expression expresstion
     * @param date       date
     * @return calc months
     */
    public static Map.Entry<Date, String> calcMonths(String expression, Date date) {
        String addMonthExpr = expression.substring(ADD_MONTHS.length() + 1, expression.length() - 1);
        String[] params = addMonthExpr.split(COMMA);

        if (params.length == 2) {
            String dateFormat = params[0];
            String monthExpr = params[1];
            Map.Entry<Date, String> baseEntry = calcFormatOffset(dateFormat, date);
            Date baseDate = baseEntry.getKey();
            String cleanFormat = baseEntry.getValue();

            Integer addMonth = calculate(monthExpr);
            Date targetDate = addMonths(baseDate, addMonth);

            return new AbstractMap.SimpleImmutableEntry<>(targetDate, cleanFormat);
        }

        throw new RuntimeException("expression not valid");
    }

    /**
     * Matches a timezone offset literal appended to the date format argument of a boundary function,
     * such as 0800 in month_first_day(yyyy-MM-dd+0800) or 08:00 in month_first_day(yyyy-MM-dd+08:00).
     * Timezone offsets range from UTC-12:00 to UTC+14:00, so only hours up to 14 with minutes up to
     * 59 are accepted. Standard placeholders are not affected by this pattern: their operands always
     * keep the legacy numeric day-offset semantics.
     */
    private static final Pattern TIMEZONE_PATTERN =
            Pattern.compile("^((?:0\\d|1[0-4])[0-5]\\d|(?:0\\d|1[0-4]):[0-5]\\d)$");

    /**
     * calculate time expression
     * <p> Keeps the legacy semantics of standard placeholders such as $[yyyyMMdd+1400]: the operand
     * after the last '+'/'-' sign is always evaluated as a numeric offset expression.
     *
     * @param expression expresstion
     * @param date       date
     * @return calculate time expression with date,format
     */
    public static Map.Entry<Date, String> calcMinutes(String expression, Date date) {
        if (expression.contains("+")) {
            int index = expression.lastIndexOf('+');

            if (Character.isDigit(expression.charAt(index + 1))) {
                String addMinuteExpr = expression.substring(index + 1);
                Date targetDate = addMinutes(date, calcMinutes(addMinuteExpr));
                String dateFormat = expression.substring(0, index);

                return new AbstractMap.SimpleImmutableEntry<>(targetDate, dateFormat);
            }
        } else if (expression.contains("-")) {
            int index = expression.lastIndexOf('-');

            if (Character.isDigit(expression.charAt(index + 1))) {
                String addMinuteExpr = expression.substring(index + 1);
                Date targetDate = addMinutes(date, 0 - calcMinutes(addMinuteExpr));
                String dateFormat = expression.substring(0, index);

                return new AbstractMap.SimpleImmutableEntry<>(targetDate, dateFormat);
            }

            // yyyy-MM-dd
            return new AbstractMap.SimpleImmutableEntry<>(date, expression);
        }

        // $[HHmmss]
        return new AbstractMap.SimpleImmutableEntry<>(date, expression);
    }

    /**
     * Resolves the date format argument of a boundary function, e.g. month_first_day(yyyy-MM-dd-1):
     * when the format carries a trailing offset expression the base date is shifted and the offset is
     * stripped from the returned format. Tails that are not arithmetic offsets - timezone literals
     * such as +0800 or +08:00, or quoted literal suffixes such as +08:00' (CST)' - stay in the format.
     *
     * @param format date format, possibly with a trailing offset
     * @param date   date
     * @return base date after applying the offset, and the date format without the offset
     */
    private static Map.Entry<Date, String> calcFormatOffset(String format, Date date) {
        int opIndex = findFormatOffsetIndex(format);
        if (opIndex == -1) {
            return new AbstractMap.SimpleImmutableEntry<>(date, format);
        }

        char op = format.charAt(opIndex);
        String addMinuteExpr = format.substring(opIndex + 1);
        String dateFormat = format.substring(0, opIndex);

        // arithmetic failures are propagated, formatTimeExpression decides how to handle them
        int minutes = calcMinutes(addMinuteExpr);
        Date targetDate = (op == '+') ? addMinutes(date, minutes) : addMinutes(date, -minutes);
        return new AbstractMap.SimpleImmutableEntry<>(targetDate, dateFormat);
    }

    /**
     * Finds the index of the arithmetic operator ('+' or '-') that separates the date format from a
     * complete trailing offset expression, ignoring operators inside single-quoted literals. The
     * boundary is the first sign whose entire remainder is an arithmetic expression: timezone
     * literals such as +08:00 or +0800 fail the arithmetic check and stay part of the format, while
     * unary signs inside the offset (e.g. the '-' in yyyy-MM-dd-7*-1) remain part of the operand.
     *
     * @param format date format, possibly with a trailing offset
     * @return index of operator, or -1 when the format has no trailing offset
     */
    private static int findFormatOffsetIndex(String format) {
        if (StringUtils.isEmpty(format)) {
            return -1;
        }

        boolean inQuote = false;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            if (c == '\'') {
                if (i + 1 < format.length() && format.charAt(i + 1) == '\'') {
                    // escaped quote ('') inside a literal
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (!inQuote && (c == '+' || c == '-') && i + 1 < format.length()
                    && Character.isDigit(format.charAt(i + 1))) {
                String operand = format.substring(i + 1).trim();
                // a tail that cannot be arithmetic (e.g. +08:00' (CST)'), a timezone offset literal
                // (e.g. +0800), or an arithmetic tail starting with a timezone literal (e.g. 0800-1)
                // belongs to the date format rather than to an offset
                if (isArithmeticExpression(operand) && !isTimezoneOffsetLiteral(operand)
                        && !startsWithTimezoneOffsetLiteral(operand)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * @param operand candidate offset expression
     * @return whether the operand consists only of arithmetic characters (digits, operators,
     *         parentheses and whitespace)
     */
    private static boolean isArithmeticExpression(String operand) {
        for (int i = 0; i < operand.length(); i++) {
            char c = operand.charAt(i);
            if (!Character.isDigit(c) && c != '+' && c != '-' && c != '*' && c != '/' && c != '(' && c != ')'
                    && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTimezoneOffsetLiteral(String operand) {
        if (StringUtils.isEmpty(operand)) {
            return false;
        }
        return TIMEZONE_PATTERN.matcher(operand).matches();
    }

    /**
     * @param operand candidate offset expression
     * @return whether the operand starts with a four-digit timezone offset literal (e.g. the 0800 in
     *         0800-1), so the sign belongs to the date format rather than to an arithmetic offset
     */
    private static boolean startsWithTimezoneOffsetLiteral(String operand) {
        return operand.length() > 4 && TIMEZONE_PATTERN.matcher(operand.substring(0, 4)).matches();
    }

    /**
     * calculate need minutes
     *
     * @param minuteExpression minute expression
     * @return calculate need minutes
     */
    public static Integer calcMinutes(String minuteExpression) {
        int index = minuteExpression.indexOf('/');

        String calcExpression;

        if (index == -1) {
            calcExpression = String.format("60*24*(%s)", minuteExpression);
        } else {

            calcExpression = String.format("60*24*(%s)%s", minuteExpression.substring(0, index),
                    minuteExpression.substring(index));
        }

        return calculate(calcExpression);
    }

}
