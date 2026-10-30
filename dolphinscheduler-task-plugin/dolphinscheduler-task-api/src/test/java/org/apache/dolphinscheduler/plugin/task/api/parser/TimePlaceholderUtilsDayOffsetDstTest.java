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

import org.apache.dolphinscheduler.common.thread.ThreadLocalContext;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Day offsets must follow calendar days, not a fixed 24-hour duration, so that DST transitions
 * do not shift the resulting date.
 */
public class TimePlaceholderUtilsDayOffsetDstTest {

    private TimeZone originalTimeZone;

    private String originalContextTimezone;

    @BeforeEach
    public void setLosAngelesTimeZone() {
        originalTimeZone = TimeZone.getDefault();
        originalContextTimezone = ThreadLocalContext.getTimezone();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    @AfterEach
    public void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
        if (originalContextTimezone == null) {
            ThreadLocalContext.removeTimezone();
        } else {
            ThreadLocalContext.setTimezone(originalContextTimezone);
        }
    }

    private static Date losAngeles(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar.getTime();
    }

    @Test
    public void dayOffsetAcrossSpringForwardTransition() {
        // 2026-03-08 02:00 -> 03:00 in America/Los_Angeles: that day has 23 hours
        Date base = losAngeles(2026, Calendar.MARCH, 9, 0, 10);
        assertEquals("2026-03-08", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd-1", base, false));
        assertEquals("2026-03-10", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd+1", base, false));
        assertEquals("2026-03-07", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd-2", base, false));
    }

    @Test
    public void dayOffsetAcrossFallBackTransition() {
        // 2026-11-01 02:00 -> 01:00: that day has 25 hours
        Date base = losAngeles(2026, Calendar.NOVEMBER, 2, 0, 10);
        assertEquals("2026-11-01", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd-1", base, false));
        assertEquals("2026-11-03", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd+1", base, false));
    }

    @Test
    public void arithmeticOperandsStillWork() {
        // guards the regression that made the previous attempt at this issue fail review:
        // the operand must stay an arithmetic expression, not a bare integer
        Date base = losAngeles(2026, Calendar.JUNE, 9, 0, 10);
        assertEquals("2026-06-23", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd+7*2", base, false));
        assertEquals("2026-05-26", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd-7*2", base, false));
        assertEquals("2026-06-02", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd+7*(-1)", base, false));
    }

    @Test
    public void dayOffsetOnRegularDay() {
        Date base = losAngeles(2026, Calendar.JUNE, 9, 0, 10);
        assertEquals("2026-06-08", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd-1", base, false));
    }

    @Test
    public void divisionOperandKeepsLegacyMinuteSemantics() {
        // an operand containing '/' is a duration in minutes, not a number of days:
        // -1000/24/60 is 1000 minutes, i.e. the same value the legacy calcMinutes() produced
        Date base = losAngeles(2026, Calendar.MARCH, 9, 0, 10);
        assertEquals("2026-03-08 07:30", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd HH:mm-1000/24/60",
                base, false));
    }

    @Test
    public void plainOperandIsWholeDaysNotMinutes() {
        // a bare operand is a number of days, so -2 crosses the DST day without an hour drift
        Date base = losAngeles(2026, Calendar.MARCH, 9, 0, 10);
        assertEquals("2026-03-07 00:10", TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd HH:mm-2", base, false));
    }

    @Test
    public void boundaryFunctionDayOffsetAcrossSpringForward() {
        Date base = losAngeles(2026, Calendar.MARCH, 9, 0, 10);
        assertEquals("2026-03-08",
                TimePlaceholderUtils.formatTimeExpression("this_day(yyyy-MM-dd-1)", base, false));
    }

    @Test
    public void dayOffsetUsesRequestTimezoneNotJvmDefault() {
        // The JVM runs in UTC while the request timezone is America/Los_Angeles (a supported and
        // common deployment). Base time 2026-03-09 00:10 PDT is 07:10 UTC; subtracting one calendar
        // day must stay on 00:10 local. Shifting in the JVM zone instead would yield 2026-03-07 23:10.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ThreadLocalContext.setTimezone("America/Los_Angeles");

        Calendar pacific = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        pacific.clear();
        pacific.set(2026, Calendar.MARCH, 9, 0, 10, 0);
        Date base = pacific.getTime();

        assertEquals("2026-03-08 00:10",
                TimePlaceholderUtils.formatTimeExpression("yyyy-MM-dd HH:mm-1", base, false));
    }
}
