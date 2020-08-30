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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtilsTest {
    @Test
    public void format2Readable() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String start = "2015-12-21 18:00:36";
        Date startDate = sdf.parse(start);

        String end = "2015-12-23 03:23:44";
        Date endDate = sdf.parse(end);

        String readableDate = DateUtils.format2Readable(endDate.getTime() - startDate.getTime());

        Assert.assertEquals("01 09:23:08", readableDate);
    }


    @Test
    public void testWeek(){

        Date curr = DateUtils.stringToDate("2019-02-01 00:00:00");
        Date monday1 = DateUtils.stringToDate("2019-01-28 00:00:00");
        Date sunday1 = DateUtils.stringToDate("2019-02-03 00:00:00");
        Date monday = DateUtils.getMonday(curr);
        Date sunday = DateUtils.getSunday(monday);

        Assert.assertEquals(monday, monday1);
        Assert.assertEquals(sunday, sunday1);

    }

    @Test
    public void diffHours(){
        Date d1 = DateUtils.stringToDate("2019-01-28 00:00:00");
        Date d2 = DateUtils.stringToDate("2019-01-28 20:00:00");
        Assert.assertEquals(DateUtils.diffHours(d1, d2), 20);
        Date d3 = DateUtils.stringToDate("2019-01-28 20:00:00");
        Assert.assertEquals(DateUtils.diffHours(d3, d2), 0);
        Assert.assertEquals(DateUtils.diffHours(d2, d1), 20);
        Date d4 = null;
        Assert.assertEquals(DateUtils.diffHours(d2, d4), 0);
    }

    @Test
    public void dateToString() {
        Date d1 = DateUtils.stringToDate("2019-01-28");
        Assert.assertNull(d1);
        d1 = DateUtils.stringToDate("2019-01-28 00:00:00");
        Assert.assertEquals(DateUtils.dateToString(d1), "2019-01-28 00:00:00");
    }

    @Test
    public void getSomeDay() {
        Date d1 = DateUtils.stringToDate("2019-01-31 00:00:00");
        Date curr = DateUtils.getSomeDay(d1, 1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-02-01 00:00:00");
        Assert.assertEquals(DateUtils.dateToString(DateUtils.getSomeDay(d1, -31)), "2018-12-31 00:00:00");
    }

    @Test
    public void getFirstDayOfMonth() {
        Date d1 = DateUtils.stringToDate("2019-01-31 00:00:00");
        Date curr = DateUtils.getFirstDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-01 00:00:00");

        d1 = DateUtils.stringToDate("2019-01-31 01:59:00");
        curr = DateUtils.getFirstDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-01 01:59:00");
    }

    @Test
    public void getSomeHourOfDay() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:59:59");
        Date curr = DateUtils.getSomeHourOfDay(d1, -1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 10:00:00");
        curr = DateUtils.getSomeHourOfDay(d1, 0);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 11:00:00");
        curr = DateUtils.getSomeHourOfDay(d1, 2);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 13:00:00");
        curr = DateUtils.getSomeHourOfDay(d1, 24);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-02-01 11:00:00");
    }

    @Test
    public void getLastDayOfMonth() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:59:59");
        Date curr = DateUtils.getLastDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 11:59:59");
        d1 = DateUtils.stringToDate("2019-01-02 11:59:59");
        curr = DateUtils.getLastDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 11:59:59");

        d1 = DateUtils.stringToDate("2019-02-02 11:59:59");
        curr = DateUtils.getLastDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-02-28 11:59:59");

        d1 = DateUtils.stringToDate("2020-02-02 11:59:59");
        curr = DateUtils.getLastDayOfMonth(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2020-02-29 11:59:59");
    }

    @Test
    public void getStartOfDay() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:59:59");
        Date curr = DateUtils.getStartOfDay(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 00:00:00");
    }

    @Test
    public void getEndOfDay() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:00:59");
        Date curr = DateUtils.getEndOfDay(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 23:59:59");
    }

    @Test
    public void getStartOfHour() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:00:59");
        Date curr = DateUtils.getStartOfHour(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 11:00:00");
    }

    @Test
    public void getEndOfHour() {
        Date d1 = DateUtils.stringToDate("2019-01-31 11:00:59");
        Date curr = DateUtils.getEndOfHour(d1);
        Assert.assertEquals(DateUtils.dateToString(curr), "2019-01-31 11:59:59");
    }
}
