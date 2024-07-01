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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
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
    void getPlaceHolderTime() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class,
                () -> TimePlaceholderUtils.getPlaceHolderTime("$[week_last_day(yyyy-MM-dd,0) - 1]", new Date()));
        assertEquals("Unsupported placeholder expression: $[week_last_day(yyyy-MM-dd,0) - 1]",
                illegalArgumentException.getMessage());
    }
}
