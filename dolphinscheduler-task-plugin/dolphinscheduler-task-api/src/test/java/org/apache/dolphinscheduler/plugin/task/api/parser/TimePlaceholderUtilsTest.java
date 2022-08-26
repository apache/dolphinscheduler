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

import org.apache.dolphinscheduler.spi.enums.CommandType;
import org.apache.dolphinscheduler.spi.utils.DateUtils;

import java.util.Date;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class TimePlaceholderUtilsTest {

    @Test
    public void placeHolderTimeTest() {
        Date date = DateUtils.parse("2022-08-26 00:00:00", "yyyy-MM-dd HH:mm:ss");

        Map<String, String> timeParams = BusinessTimeUtils.getBusinessTime(CommandType.COMPLEMENT_DATA, date);

        // this_day test
        String thisDay = "$[this_day(yyyy-MM-dd)]";
        String thisDate = "$[this_day(yyyyMMdd)]";

        String thisDayTime = ParameterUtils.convertParameterPlaceholders(thisDay, timeParams);
        Assert.assertEquals(thisDayTime,"2022-08-26");

        String thisDateTime = ParameterUtils.convertParameterPlaceholders(thisDate, timeParams);
        Assert.assertEquals(thisDateTime,"20220826");

        // last_day test
        String lastDay = "$[last_day(yyyy-MM-dd)]";
        String lastDate = "$[last_day(yyyyMMdd)]";
        String lastDayTime = ParameterUtils.convertParameterPlaceholders(lastDay, timeParams);
        Assert.assertEquals(lastDayTime,"2022-08-25");

        String lastDateTime = ParameterUtils.convertParameterPlaceholders(lastDate, timeParams);
        Assert.assertEquals(lastDateTime,"20220825");

        // year_Week default test
        String yearWeekDate = "$[year_week(yyyy-MM-dd)]";
        String yearWeekDay = "$[year_week(yyyyMMdd)]";

        String yearWeekDateTime = ParameterUtils.convertParameterPlaceholders(yearWeekDate, timeParams);
        Assert.assertEquals(yearWeekDateTime,"2022-34");

        String yearWeekDayTime = ParameterUtils.convertParameterPlaceholders(yearWeekDay, timeParams);
        Assert.assertEquals(yearWeekDayTime,"202234");

        // year_Week custom test
        String yearWeekDateAny = "$[year_week(yyyy-MM-dd,5)]";
        String yearWeekDayAny = "$[year_week(yyyyMMdd,5)]";

        String yearWeekDateAnyTime = ParameterUtils.convertParameterPlaceholders(yearWeekDateAny, timeParams);
        Assert.assertEquals(yearWeekDateAnyTime,"2022-35");

        String yearWeekDayAnyTime = ParameterUtils.convertParameterPlaceholders(yearWeekDayAny, timeParams);
        Assert.assertEquals(yearWeekDayAnyTime,"202235");

        // month_first_day test
        String monthFirstDate = "$[month_first_day(yyyy-MM-dd,-1)]";
        String monthFirstDay = "$[month_first_day(yyyyMMdd,-1)]";

        String monthFirstDateTime = ParameterUtils.convertParameterPlaceholders(monthFirstDate, timeParams);
        Assert.assertEquals(monthFirstDateTime,"2022-07-01");

        String monthFirstDayTime = ParameterUtils.convertParameterPlaceholders(monthFirstDay, timeParams);
        Assert.assertEquals(monthFirstDayTime,"20220701");

        // month_last_day test
        String monthLastDate = "$[month_last_day(yyyy-MM-dd,-1)]";
        String monthLastDay = "$[month_last_day(yyyyMMdd,-1)]";

        String monthLastDateTime = ParameterUtils.convertParameterPlaceholders(monthLastDate, timeParams);
        Assert.assertEquals(monthLastDateTime,"2022-07-31");

        String monthLastDayTime = ParameterUtils.convertParameterPlaceholders(monthLastDay, timeParams);
        Assert.assertEquals(monthLastDayTime,"20220731");

        // week_first_day test
        String weekFirstDate = "$[week_first_day(yyyy-MM-dd,0)]";
        String weekFirstDay = "$[week_first_day(yyyyMMdd,0)]";

        String weekFirstDateTime = ParameterUtils.convertParameterPlaceholders(weekFirstDate, timeParams);
        Assert.assertEquals(weekFirstDateTime,"2022-08-22");

        String weekFirstDayTime = ParameterUtils.convertParameterPlaceholders(weekFirstDay, timeParams);
        Assert.assertEquals(weekFirstDayTime,"20220822");

        // week_last_day test
        String weekLastDate = "$[week_last_day(yyyy-MM-dd,0)]";
        String weekLastDay = "$[week_last_day(yyyyMMdd,0)]";

        String weekLastDateTime = ParameterUtils.convertParameterPlaceholders(weekLastDate, timeParams);
        Assert.assertEquals(weekLastDateTime,"2022-08-28");

        String weekLastDayTime = ParameterUtils.convertParameterPlaceholders(weekLastDay, timeParams);
        Assert.assertEquals(weekLastDayTime,"20220828");

    }
}
