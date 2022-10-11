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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;
import org.apache.dolphinscheduler.spi.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DependentUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(DependentUtilsTest.class);

    @Test
    public void getDependResultForRelation() {
        // failed
        DependentRelation dependentRelation = DependentRelation.AND;
        List<DependResult> dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.FAILED);
        dependResultList.add(DependResult.SUCCESS);
        DependResult result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.FAILED);

        // waiting
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.WAITING);
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.WAITING);

        // success
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.SUCCESS);
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.SUCCESS);

        // one success
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.SUCCESS);

        // one failed
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.FAILED);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.FAILED);

        // or success
        dependentRelation = DependentRelation.OR;
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.FAILED);
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.SUCCESS);

        // waiting
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.WAITING);
        dependResultList.add(DependResult.FAILED);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.WAITING);

        // success
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.SUCCESS);
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.SUCCESS);

        // one success
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.SUCCESS);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.SUCCESS);

        // one failed
        dependResultList = new ArrayList<>();
        dependResultList.add(DependResult.FAILED);
        result = DependentUtils.getDependResultForRelation(dependentRelation, dependResultList);
        Assertions.assertEquals(result, DependResult.FAILED);
    }

    @Test
    public void getDateIntervalList() {

        Date curDay = DateUtils.stringToDate("2019-02-05 00:00:00");

        DateInterval diCur = new DateInterval(DateUtils.getStartOfDay(curDay),
                DateUtils.getEndOfDay(curDay));

        Date day1 = DateUtils.stringToDate("2019-02-04 00:00:00");
        DateInterval di1 = new DateInterval(DateUtils.getStartOfDay(day1),
                DateUtils.getEndOfDay(day1));
        Date day2 = DateUtils.stringToDate("2019-02-03 00:00:00");
        DateInterval di2 = new DateInterval(DateUtils.getStartOfDay(day2),
                DateUtils.getEndOfDay(day2));
        String dateValue = "last1Days";
        List<DateInterval> dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);
        Assertions.assertEquals(dateIntervals.get(0), di1);

        dateValue = "last2Days";
        dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);
        for (DateInterval dateInterval : dateIntervals) {
            logger.info(dateInterval.getStartTime().toString() + " == " + dateInterval.getEndTime().toString());
        }

        Assertions.assertEquals(dateIntervals.get(1), di1);
        Assertions.assertEquals(dateIntervals.get(0), di2);

        dateValue = "today";
        dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);
        Assertions.assertEquals(dateIntervals.get(0), diCur);

        dateValue = "thisWeek";
        Date firstWeekDay = DateUtils.getMonday(curDay);
        dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);

        DateInterval weekHead =
                new DateInterval(DateUtils.getStartOfDay(firstWeekDay), DateUtils.getEndOfDay(firstWeekDay));
        DateInterval weekThis = new DateInterval(DateUtils.getStartOfDay(curDay), DateUtils.getEndOfDay(curDay));

        Assertions.assertEquals(dateIntervals.get(0), weekHead);
        Assertions.assertEquals(dateIntervals.get(dateIntervals.size() - 1), weekThis);

        dateValue = "thisMonth";
        Date firstMonthDay = DateUtils.getFirstDayOfMonth(curDay);
        dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);

        DateInterval monthHead =
                new DateInterval(DateUtils.getStartOfDay(firstMonthDay), DateUtils.getEndOfDay(firstMonthDay));
        DateInterval monthThis = new DateInterval(DateUtils.getStartOfDay(curDay), DateUtils.getEndOfDay(curDay));

        Assertions.assertEquals(dateIntervals.get(0), monthHead);
        Assertions.assertEquals(dateIntervals.get(dateIntervals.size() - 1), monthThis);

        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-04 10:00:00"), "last1Hour");
        DateInterval expect = new DateInterval(DateUtils.stringToDate("2019-02-04 09:00:00"),
                DateUtils.getEndOfHour(DateUtils.stringToDate("2019-02-04 09:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));

        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-04 10:00:00"), "last2Hours");
        expect = new DateInterval(DateUtils.stringToDate("2019-02-04 08:00:00"),
                DateUtils.getEndOfHour(DateUtils.stringToDate("2019-02-04 08:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));

        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-04 10:00:00"), "last3Hours");
        expect = new DateInterval(DateUtils.stringToDate("2019-02-04 07:00:00"),
                DateUtils.getEndOfHour(DateUtils.stringToDate("2019-02-04 07:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));

        dateValue = "last3Days";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-02-07 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-07 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));

        dateValue = "last7Days";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-02-03 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-03 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));

        dateValue = "lastWeek";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-28 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-28 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        expect = new DateInterval(DateUtils.stringToDate("2019-02-03 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-03 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(6));
        Assertions.assertEquals(7, dateIntervals.size());

        dateValue = "lastMonday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-28 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-28 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastTuesday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-29 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-29 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastWednesday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-30 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-30 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastThursday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-31 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-31 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastFriday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-02-01 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-01 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastSaturday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-02-02 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-02 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastSunday";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-02-03 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-02-03 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastMonth";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-01 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-01 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        expect = new DateInterval(DateUtils.stringToDate("2019-01-31 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-31 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(30));
        Assertions.assertEquals(31, dateIntervals.size());

        dateValue = "lastMonthBegin";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-01 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-01 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());

        dateValue = "lastMonthEnd";
        dateIntervals = DependentUtils.getDateIntervalList(DateUtils.stringToDate("2019-02-10 07:00:00"), dateValue);
        expect = new DateInterval(DateUtils.stringToDate("2019-01-31 00:00:00"),
                DateUtils.getEndOfDay(DateUtils.stringToDate("2019-01-31 00:00:00")));
        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());
    }

    @Test
    public void testWeek() {

        Date curDay = DateUtils.stringToDate("2019-02-05 00:00:00");
        Date day1 = DateUtils.stringToDate("2019-01-28 00:00:00");
        DateInterval di1 = new DateInterval(DateUtils.getStartOfDay(day1),
                DateUtils.getEndOfDay(day1));

        Date day2 = DateUtils.stringToDate("2019-01-29 00:00:00");
        DateInterval di2 = new DateInterval(DateUtils.getStartOfDay(day2),
                DateUtils.getEndOfDay(day2));
        Date day3 = DateUtils.stringToDate("2019-01-30 00:00:00");
        DateInterval di3 = new DateInterval(DateUtils.getStartOfDay(day3),
                DateUtils.getEndOfDay(day3));
        Date day4 = DateUtils.stringToDate("2019-01-31 00:00:00");
        DateInterval di4 = new DateInterval(DateUtils.getStartOfDay(day4),
                DateUtils.getEndOfDay(day4));
        Date day5 = DateUtils.stringToDate("2019-02-01 00:00:00");
        DateInterval di5 = new DateInterval(DateUtils.getStartOfDay(day5),
                DateUtils.getEndOfDay(day5));
        Date day6 = DateUtils.stringToDate("2019-02-02 00:00:00");
        DateInterval di6 = new DateInterval(DateUtils.getStartOfDay(day6),
                DateUtils.getEndOfDay(day6));
        Date day7 = DateUtils.stringToDate("2019-02-03 00:00:00");
        DateInterval di7 = new DateInterval(DateUtils.getStartOfDay(day7),
                DateUtils.getEndOfDay(day7));
        List<DateInterval> dateIntervals = DependentDateUtils.getLastWeekInterval(curDay);
        Assertions.assertEquals(dateIntervals.size(), 7);
        Assertions.assertEquals(dateIntervals.get(0), di1);
        Assertions.assertEquals(dateIntervals.get(1), di2);
        Assertions.assertEquals(dateIntervals.get(2), di3);
        Assertions.assertEquals(dateIntervals.get(3), di4);

        List<DateInterval> monday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 1);
        Assertions.assertEquals(monday.get(0), di1);
        List<DateInterval> tuesday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 2);
        Assertions.assertEquals(tuesday.get(0), di2);
        List<DateInterval> wednesday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 3);
        Assertions.assertEquals(wednesday.get(0), di3);
        List<DateInterval> thursday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 4);
        Assertions.assertEquals(thursday.get(0), di4);
        List<DateInterval> friday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 5);
        Assertions.assertEquals(friday.get(0), di5);
        List<DateInterval> saturday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 6);
        Assertions.assertEquals(saturday.get(0), di6);
        List<DateInterval> sunday = DependentDateUtils.getLastWeekOneDayInterval(curDay, 7);
        Assertions.assertEquals(sunday.get(0), di7);
    }

    @Test
    public void testHour() {

        Date curDay = DateUtils.stringToDate("2019-02-05 12:10:00");
        Date day1 = DateUtils.stringToDate("2019-02-05 11:00:00");
        DateInterval di1 = new DateInterval(DateUtils.getStartOfHour(day1),
                DateUtils.getEndOfHour(day1));
        Date day2 = DateUtils.stringToDate("2019-02-05 10:00:00");
        DateInterval di2 = new DateInterval(DateUtils.getStartOfHour(day2),
                DateUtils.getEndOfHour(day2));
        Date day3 = DateUtils.stringToDate("2019-02-05 09:00:00");
        DateInterval di3 = new DateInterval(DateUtils.getStartOfHour(day3),
                DateUtils.getEndOfHour(day3));

        List<DateInterval> dateIntervals = DependentDateUtils.getLastHoursInterval(curDay, 1);
        Assertions.assertEquals(dateIntervals.get(0), di1);
        dateIntervals = DependentDateUtils.getLastHoursInterval(curDay, 2);
        Assertions.assertEquals(dateIntervals.get(1), di1);
        Assertions.assertEquals(dateIntervals.get(0), di2);
        dateIntervals = DependentDateUtils.getLastHoursInterval(curDay, 3);
        Assertions.assertEquals(dateIntervals.get(2), di1);
        Assertions.assertEquals(dateIntervals.get(1), di2);
        Assertions.assertEquals(dateIntervals.get(0), di3);

    }

    @Test
    public void testGetCurretHour() {
        String dateValue = "currentHour";

        Date curDay = DateUtils.stringToDate("2020-05-15 12:10:00");

        List<DateInterval> dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);

        DateInterval expect = new DateInterval(DateUtils.getStartOfHour(DateUtils.stringToDate("2020-05-15 12:00:00")),
                DateUtils.getEndOfHour(DateUtils.stringToDate("2020-05-15 12:59:59")));

        Assertions.assertEquals(expect, dateIntervals.get(0));
        Assertions.assertEquals(1, dateIntervals.size());
    }

    @Test
    public void testGetLast24Hour() {
        Date curDay = DateUtils.stringToDate("2020-05-15 12:10:00");
        String dateValue = "last24Hours";

        List<DateInterval> dateIntervals = DependentUtils.getDateIntervalList(curDay, dateValue);

        List<DateInterval> expect = Lists.newArrayList();
        for (int a = 1; a < 24; a++) {
            String i = a + "";
            if (a < 10) {
                i = "0" + i;
            }
            DateInterval dateInterval =
                    new DateInterval(DateUtils.getStartOfHour(DateUtils.stringToDate("2020-05-14 " + i + ":00:00")),
                            DateUtils.getEndOfHour(DateUtils.stringToDate("2020-05-14 " + i + ":59:59")));
            expect.add(dateInterval);
        }
        DateInterval dateInterval =
                new DateInterval(DateUtils.getStartOfHour(DateUtils.stringToDate("2020-05-15 00:00:00")),
                        DateUtils.getEndOfHour(DateUtils.stringToDate("2020-05-15 00:59:59")));
        expect.add(dateInterval);

        Assertions.assertEquals(24, dateIntervals.size());

        for (int i = 0; i < expect.size(); i++) {
            Assertions.assertEquals(expect.get(i), dateIntervals.get(i));
        }
    }

    @Test
    public void testMonth() {
        Date curDay = DateUtils.stringToDate("2019-02-05 00:00:00");
        Date day1 = DateUtils.stringToDate("2019-01-01 00:00:00");
        DateInterval di1 = new DateInterval(DateUtils.getStartOfDay(day1),
                DateUtils.getEndOfDay(day1));

        Date day2 = DateUtils.stringToDate("2019-01-31 00:00:00");
        DateInterval di2 = new DateInterval(DateUtils.getStartOfDay(day2),
                DateUtils.getEndOfDay(day2));

        List<DateInterval> dateIntervals = DependentDateUtils.getLastMonthInterval(curDay);

        Assertions.assertEquals(dateIntervals.size(), 31);
        Assertions.assertEquals(dateIntervals.get(0), di1);
        Assertions.assertEquals(dateIntervals.get(30), di2);
    }

}
