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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TimePlaceholderUtilsTest {

    private Date date = DateUtils.parse("2022-08-26 00:00:00", "yyyy-MM-dd HH:mm:ss");

    private Map<String, String> timeParams = BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, date, null);

    @Test
    public void timePlaceHolderForThisDay() {
        String thisDay = "$[this_day(yyyy-MM-dd)]";
        String thisDate = "$[this_day(yyyyMMdd)]";

        String thisDayTime = ParameterUtils.convertParameterPlaceholders(thisDay, timeParams);
        assertEquals(thisDayTime, "2022-08-26");

        String thisDateTime = ParameterUtils.convertParameterPlaceholders(thisDate, timeParams);
        assertEquals(thisDateTime, "20220826");
    }

    @Test
    public void timePlaceHolderForLastDay() {
        String lastDay = "$[last_day(yyyy-MM-dd)]";
        String lastDate = "$[last_day(yyyyMMdd)]";
        String lastDayTime = ParameterUtils.convertParameterPlaceholders(lastDay, timeParams);
        assertEquals(lastDayTime, "2022-08-25");

        String lastDateTime = ParameterUtils.convertParameterPlaceholders(lastDate, timeParams);
        assertEquals(lastDateTime, "20220825");
    }

    @Test
    public void timePlaceHolderForYearWeekDay() {
        // Start the week on Monday
        String yearWeekDate = "$[year_week(yyyy-MM-dd)]";
        String yearWeekDay = "$[year_week(yyyyMMdd)]";

        String yearWeekDateTime = ParameterUtils.convertParameterPlaceholders(yearWeekDate, timeParams);
        assertEquals(yearWeekDateTime, "2022-34");

        String yearWeekDayTime = ParameterUtils.convertParameterPlaceholders(yearWeekDay, timeParams);
        assertEquals(yearWeekDayTime, "202234");

        // Start the week on Friday
        String yearWeekDateAny = "$[year_week(yyyy-MM-dd,5)]";
        String yearWeekDayAny = "$[year_week(yyyyMMdd,5)]";

        String yearWeekDateAnyTime = ParameterUtils.convertParameterPlaceholders(yearWeekDateAny, timeParams);
        assertEquals(yearWeekDateAnyTime, "2022-35");

        String yearWeekDayAnyTime = ParameterUtils.convertParameterPlaceholders(yearWeekDayAny, timeParams);
        assertEquals(yearWeekDayAnyTime, "202235");
    }

    @Test
    public void timePlaceHolderForMonthFirstDay() {
        String monthFirstDate = "$[month_first_day(yyyy-MM-dd,-1)]";
        String monthFirstDay = "$[month_first_day(yyyyMMdd,-1)]";

        String monthFirstDateTime = ParameterUtils.convertParameterPlaceholders(monthFirstDate, timeParams);
        assertEquals(monthFirstDateTime, "2022-07-01");

        String monthFirstDayTime = ParameterUtils.convertParameterPlaceholders(monthFirstDay, timeParams);
        assertEquals(monthFirstDayTime, "20220701");
    }

    @Test
    public void timePlaceHolderForMonthLastDay() {
        String monthLastDate = "$[month_last_day(yyyy-MM-dd,-1)]";
        String monthLastDay = "$[month_last_day(yyyyMMdd,-1)]";

        String monthLastDateTime = ParameterUtils.convertParameterPlaceholders(monthLastDate, timeParams);
        assertEquals(monthLastDateTime, "2022-07-31");

        String monthLastDayTime = ParameterUtils.convertParameterPlaceholders(monthLastDay, timeParams);
        assertEquals(monthLastDayTime, "20220731");
    }

    @Test
    public void timePlaceHolderForWeekFirstDay() {
        String weekFirstDate = "$[week_first_day(yyyy-MM-dd,0)]";
        String weekFirstDay = "$[week_first_day(yyyyMMdd,0)]";

        String weekFirstDateTime = ParameterUtils.convertParameterPlaceholders(weekFirstDate, timeParams);
        assertEquals(weekFirstDateTime, "2022-08-22");

        String weekFirstDayTime = ParameterUtils.convertParameterPlaceholders(weekFirstDay, timeParams);
        assertEquals(weekFirstDayTime, "20220822");
    }

    @Test
    public void timePlaceHolderForWeekLastDay() {
        String weekLastDate = "$[week_last_day(yyyy-MM-dd,0)]";
        String weekLastDay = "$[week_last_day(yyyyMMdd,0)]";

        String weekLastDateTime = ParameterUtils.convertParameterPlaceholders(weekLastDate, timeParams);
        assertEquals(weekLastDateTime, "2022-08-28");

        String weekLastDayTime = ParameterUtils.convertParameterPlaceholders(weekLastDay, timeParams);
        assertEquals(weekLastDayTime, "20220828");
    }

    @Test
    public void timePlaceHolderForMonthFirstDayWithDateOffset() {
        // Business date: 2022-08-26 (Friday)
        // Yesterday is 2022-08-25, which is in August 2022. First day of yesterday's month is 2022-08-01
        String monthFirstDate = "$[month_first_day(yyyy-MM-dd-1)]";
        String monthFirstDay = "$[month_first_day(yyyyMMdd-1)]";
        assertEquals("2022-08-01", ParameterUtils.convertParameterPlaceholders(monthFirstDate, timeParams));
        assertEquals("20220801", ParameterUtils.convertParameterPlaceholders(monthFirstDay, timeParams));

        String monthFirstDateWithMonthOffset = "$[month_first_day(yyyy-MM-dd-1,0)]";
        assertEquals("2022-08-01",
                ParameterUtils.convertParameterPlaceholders(monthFirstDateWithMonthOffset, timeParams));

        String monthFirstDateLastMonth = "$[month_first_day(yyyy-MM-dd-1,-1)]";
        assertEquals("2022-07-01", ParameterUtils.convertParameterPlaceholders(monthFirstDateLastMonth, timeParams));

        // Test across month boundary: on 2022-09-01, yesterday is 2022-08-31
        Date septFirst = DateUtils.parse("2022-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> septParams =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, septFirst, null);
        assertEquals("2022-08-01",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd-1)]", septParams));
        assertEquals("2022-08-01",
                ParameterUtils.convertParameterPlaceholders("$[month_begin(yyyy-MM-dd-1,0)]", septParams));
        assertEquals("2022-08-31",
                ParameterUtils.convertParameterPlaceholders("$[month_last_day(yyyy-MM-dd-1)]", septParams));
        assertEquals("2022-08-31",
                ParameterUtils.convertParameterPlaceholders("$[month_end(yyyy-MM-dd-1,0)]", septParams));

        // On 2022-09-02, yesterday is 2022-09-01
        Date septSecond = DateUtils.parse("2022-09-02 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> septSecondParams =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, septSecond, null);
        assertEquals("2022-09-01",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd-1)]", septSecondParams));
        assertEquals("2022-09-01",
                ParameterUtils.convertParameterPlaceholders("$[month_begin(yyyy-MM-dd-1,0)]", septSecondParams));
        assertEquals("2022-09-30",
                ParameterUtils.convertParameterPlaceholders("$[month_last_day(yyyy-MM-dd-1)]", septSecondParams));
        assertEquals("2022-09-30",
                ParameterUtils.convertParameterPlaceholders("$[month_end(yyyy-MM-dd-1,0)]", septSecondParams));

        // Week tests with date offset:
        // On 2022-08-29 (Monday), yesterday is 2022-08-28 (Sunday of previous week)
        Date aug29 = DateUtils.parse("2022-08-29 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> aug29Params =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, aug29, null);
        assertEquals("2022-08-22",
                ParameterUtils.convertParameterPlaceholders("$[week_first_day(yyyy-MM-dd-1)]", aug29Params));
        assertEquals("2022-08-28",
                ParameterUtils.convertParameterPlaceholders("$[week_last_day(yyyy-MM-dd-1)]", aug29Params));
        assertEquals("2022-08-22",
                ParameterUtils.convertParameterPlaceholders("$[week_begin(yyyy-MM-dd-1,0)]", aug29Params));
        assertEquals("2022-08-28",
                ParameterUtils.convertParameterPlaceholders("$[week_end(yyyy-MM-dd-1,0)]", aug29Params));

        // add_months with date offset:
        assertEquals("2022-09-25",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyy-MM-dd-1,1)]", timeParams));

        // Preserve date-format literals and quoted literals:
        assertEquals("2022-08-01+0800",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd+0800)]", timeParams));
        assertEquals("2022-08-01+0800",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd'+0800')]", timeParams));
        assertEquals("2022-08-01+08:00",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd+08:00)]", timeParams));
        assertEquals("2022-08-01+08:00",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd'+08:00')]", timeParams));
        assertEquals("2022-08-01-0500",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd-0500)]", timeParams));
        assertEquals("2022-08-01-0500",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd'-0500')]", timeParams));
        assertEquals("2022-08-01T00:00:00",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd'T'HH:mm:ss)]", timeParams));

        assertEquals("2022-08-01+0800",
                ParameterUtils.convertParameterPlaceholders("$[month_begin(yyyy-MM-dd'+0800',0)]", timeParams));
        assertEquals("2022-08-22+0800",
                ParameterUtils.convertParameterPlaceholders("$[week_first_day(yyyy-MM-dd'+0800')]", timeParams));
        assertEquals("2022-09-26+0800",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyy-MM-dd'+0800',1)]", timeParams));

        // Standard placeholders keep the legacy numeric offset semantics: timezone-shaped operands
        // are evaluated as day offsets, and quoted literals that fail arithmetic parsing or date
        // formatting leave the placeholder unresolved
        assertEquals("2024-11-03",
                ParameterUtils.convertParameterPlaceholders("$[yyyy-MM-dd+0800]", timeParams));
        assertEquals("$[yyyy-MM-dd'+0800']",
                ParameterUtils.convertParameterPlaceholders("$[yyyy-MM-dd'+0800']", timeParams));
        assertEquals("2021-04-13",
                ParameterUtils.convertParameterPlaceholders("$[yyyy-MM-dd-0500]", timeParams));
        assertEquals("$[yyyy-MM-dd'-0500']",
                ParameterUtils.convertParameterPlaceholders("$[yyyy-MM-dd'-0500']", timeParams));
    }

    @Test
    void formatTimeExpressionWithInvalidExpression() {
        assertEquals("$[week_last_day(yyyy-MM-dd,0) - 1]",
                TimePlaceholderUtils.formatTimeExpression("$[week_last_day(yyyy-MM-dd,0) - 1]", new Date(), true));
    }

    @Test
    public void timePlaceHolderWithFourDigitDayOffset() {
        // Standard placeholders keep the legacy numeric day-offset semantics for any operand,
        // including timezone-shaped four-digit values such as +1400
        assertEquals("20120828", ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd-3650]", timeParams));
        assertEquals("20320823", ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+3650]", timeParams));
        assertEquals("20260626", ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+1400]", timeParams));
    }

    @Test
    public void timePlaceHolderWithUnaryMinusInOffsetExpression() {
        // The boundary sign only starts the offset expression: unary signs and parenthesized
        // arithmetic inside the operand stay part of the arithmetic expression
        assertEquals("20220819", ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+7*-1]", timeParams));
        assertEquals("20220819", ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+7*(-1)]", timeParams));
    }

    @Test
    public void timePlaceHolderWithArithmeticFailure() {
        // ignore-invalid mode preserves the original placeholder
        assertEquals("$[yyyyMMdd+1/0]",
                ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+1/0]", timeParams));
        assertEquals("yyyyMMdd+1/0",
                TimePlaceholderUtils.formatTimeExpression("yyyyMMdd+1/0", date, true));
        // strict mode rejects the expression
        assertThrows(IllegalArgumentException.class,
                () -> TimePlaceholderUtils.formatTimeExpression("yyyyMMdd+1/0", date, false));
    }

    @Test
    public void timePlaceHolderWithTrailingFormatLiteralSuffix() {
        // Boundary-function format arguments keep tails that are not arithmetic offsets, such as
        // a timezone suffix followed by a quoted zone name
        assertEquals("2022-08-26T00:00:00+08:00 (CST)",
                ParameterUtils.convertParameterPlaceholders(
                        "$[this_day(yyyy-MM-dd'T'HH:mm:ss+08:00' (CST)')]", timeParams));
        assertEquals("2022-08-01T00:00:00+08:00 (CST)",
                ParameterUtils.convertParameterPlaceholders(
                        "$[month_first_day(yyyy-MM-dd'T'HH:mm:ss+08:00' (CST)')]", timeParams));
    }

    @Test
    public void timePlaceHolderWithBoundaryOffsetAfterTimezoneLiteral() {
        // The offset boundary is the complete trailing arithmetic operand: a timezone literal
        // followed by an offset keeps both
        Date septFirst = DateUtils.parse("2022-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> septFirstParams =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, septFirst, null);
        // colon timezone + negative offset
        assertEquals("2022-08-01T00:00:00+08:00",
                ParameterUtils.convertParameterPlaceholders(
                        "$[month_first_day(yyyy-MM-dd'T'HH:mm:ss+08:00-1)]", septFirstParams));
        // colon timezone + positive offset
        assertEquals("2022-09-01T00:00:00+08:00",
                ParameterUtils.convertParameterPlaceholders(
                        "$[month_first_day(yyyy-MM-dd'T'HH:mm:ss+08:00+1)]", septFirstParams));
        // compact timezone + offset: the 0800 prefix is a timezone literal, not the start of the
        // arithmetic operand
        assertEquals("20220801+0800",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyyMMdd+0800-1)]",
                        septFirstParams));
        // quoted zone-name suffix + offset
        assertEquals("2022-08-01+08:00 (CST)",
                ParameterUtils.convertParameterPlaceholders(
                        "$[month_first_day(yyyy-MM-dd+08:00' (CST)'-1)]", septFirstParams));
    }

    @Test
    public void timePlaceHolderWithUnaryMinusInBoundaryOffset() {
        // Unary signs and parenthesized arithmetic inside a boundary-function offset stay part
        // of the operand
        Date septFirst = DateUtils.parse("2022-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> septFirstParams =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, septFirst, null);
        assertEquals("2022-09-01",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd-7*-1)]",
                        septFirstParams));
        assertEquals("20220801",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyyMMdd+7*-1)]", timeParams));
        assertEquals("20220821",
                ParameterUtils.convertParameterPlaceholders("$[week_last_day(yyyyMMdd+7*(-1))]", timeParams));
    }

    @Test
    public void timePlaceHolderWithBoundaryArithmeticFailure() {
        // Arithmetic failures in boundary-function offsets propagate to formatTimeExpression:
        // ignore-invalid mode preserves the placeholder, strict mode rejects the expression
        assertEquals("$[month_first_day(yyyy-MM-dd+1/0)]",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyy-MM-dd+1/0)]", timeParams));
        assertThrows(IllegalArgumentException.class, () -> TimePlaceholderUtils
                .formatTimeExpression("month_first_day(yyyy-MM-dd+1/0)", date, false));
    }

    @Test
    public void timePlaceHolderWithTzShapedArithmeticOperand() {
        // Operands that merely start with a timezone-shaped number are complete arithmetic
        // expressions: division, multiplication and longer numbers are evaluated as offsets,
        // not treated as timezone literals followed by another offset
        Date septFirst = DateUtils.parse("2022-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String, String> septFirstParams =
                BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, septFirst, null);
        // division operand: 1000/24/60 is 1000 minutes
        assertEquals("20220801",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyyMMdd-1000/24/60)]",
                        septFirstParams));
        // multiplication operand: 2*3 is 6 days
        assertEquals("20220820",
                ParameterUtils.convertParameterPlaceholders("$[this_day(yyyyMMdd-2*3)]", timeParams));
        // five-digit numeric operand
        assertEquals("19950401",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyyMMdd-10000)]",
                        septFirstParams));
        // arithmetic failures with a timezone-shaped prefix still reach formatTimeExpression
        assertEquals("$[month_first_day(yyyyMMdd+1000/0)]",
                ParameterUtils.convertParameterPlaceholders("$[month_first_day(yyyyMMdd+1000/0)]",
                        septFirstParams));
        assertThrows(IllegalArgumentException.class, () -> TimePlaceholderUtils
                .formatTimeExpression("month_first_day(yyyyMMdd+1000/0)", date, false));
    }
}
