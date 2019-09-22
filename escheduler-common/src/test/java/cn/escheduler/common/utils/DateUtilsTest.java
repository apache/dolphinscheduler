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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtilsTest {

    @Rule public final ExpectedException thrown = ExpectedException.none();

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
    public void testDateToString() {
        final Date date = new Date(1_560_265_977_787L);

        Assert.assertEquals("2019-06-11 16:12:57", DateUtils.dateToString(date));
    }

    @Test
    public void testDifferMs() {
        final Date date1 = new Date(1_515_585_600_000L);
        final Date date2 = new Date(1_515_585_600_000L);
        final Date date3 = new Date(1_515_585_599_999L);

        Assert.assertEquals(0L, DateUtils.differMs(date1, date2));
        Assert.assertEquals(1L, DateUtils.differMs(date1, date3));
    }

    @Test
    public void testDiffHours() {
        final Date date1 = new Date(1_515_585_600_000L);
        final Date date2 = new Date(1_515_585_600_000L);
        final Date date3 = new Date(1_515_585_599_999L);

        Assert.assertEquals(0L, DateUtils.diffHours(date1, date2));
        Assert.assertEquals(1L, DateUtils.diffHours(date1, date3));
    }

    @Test
    public void testGetSomeDay() {
        final Date date = new Date(1_560_265_977_787L);

        Assert.assertEquals("Fri Jun 14 16:12:57 BST 2019",
                DateUtils.getSomeDay(date, 3).toString());
    }

    @Test
    public void testCompare() {
        final Date date1 = new Date(1_560_265_977_787L);
        final Date date2 = new Date(1_515_585_600_000L);

        Assert.assertTrue(DateUtils.compare(date1, date2));

        Assert.assertFalse(DateUtils.compare(date2, date1));
    }

    @Test
    public void testFormat() {
        final Date date = new Date(1_560_265_977_787L);
        final String format = "yyyy-MM-dd HH:mm:ss";

        Assert.assertEquals("2019-06-11 16:12:57", DateUtils.format(date, format));
    }

    @Test
    public void testGetEndOfDay() {
        final Date endOfDay = DateUtils.getEndOfDay(new Date(1_560_265_977_787L));

        Assert.assertEquals("Tue Jun 11 23:59:59 BST 2019", endOfDay.toString());
    }

    @Test
    public void testGetEndOfHour() {
        final Date endOfHour = DateUtils.getEndOfHour(new Date(1_560_265_977_787L));

        Assert.assertEquals("Tue Jun 11 16:59:59 BST 2019", endOfHour.toString());
    }

    @Test
    public void testGetFirstDayOfMonth() {
        final Date firstDayOfMonth = DateUtils.getFirstDayOfMonth(new Date(1_560_265_977_787L));

        Assert.assertEquals("Sat Jun 01 16:12:57 BST 2019", firstDayOfMonth.toString());
    }

    @Test
    public void testGetMonday() {
        final Date monday = DateUtils.getMonday(new Date(1_560_265_977_787L));

        Assert.assertEquals("Mon Jun 10 16:12:57 BST 2019", monday.toString());
    }

    @Test
    public void testGetScheduleDate() {
        Assert.assertEquals("Tue Jun 11 16:12:57 BST 2019",
                DateUtils.getScheduleDate("2019-06-11 16:12:57").toString());
    }

    @Test
    public void testGetSomeHourOfDay() {
        final Date someHourOfDay =
                DateUtils.getSomeHourOfDay(new Date(1_560_265_977_787L), 4);

        Assert.assertEquals("Tue Jun 11 12:00:00 BST 2019", someHourOfDay.toString());
    }

    @Test
    public void testGetLastDayOfMonth() {
        final Date lastDayOfMonth =
                DateUtils.getLastDayOfMonth(new Date(1_560_265_977_787L));

        Assert.assertEquals("Sun Jun 30 16:12:57 BST 2019", lastDayOfMonth.toString());
    }

    @Test
    public void testGetStartOfDay() {
        final Date startOfDay = DateUtils.getStartOfDay(new Date(1_560_265_977_787L));

        Assert.assertEquals("Tue Jun 11 00:00:00 BST 2019", startOfDay.toString());
    }

    @Test
    public void testGetStartOfHour() {
        final Date startOfHour = DateUtils.getStartOfHour(new Date(1_560_265_977_787L));

        Assert.assertEquals("Tue Jun 11 16:00:00 BST 2019", startOfHour.toString());
    }

    @Test
    public void testGetSunday() {
        final Date sunday = DateUtils.getSunday(new Date(1_560_265_977_787L));

        Assert.assertEquals("Sun Jun 16 16:12:57 BST 2019", sunday.toString());
    }

    @Test
    public void testParse() {
        final String date = "2019-06-11 16:12:57";
        final String format = "yyyy-MM-dd HH:mm:ss";
        final Date actual = DateUtils.parse(date, format);

        Assert.assertEquals("Tue Jun 11 16:12:57 BST 2019", actual.toString());

        Assert.assertNull(DateUtils.parse("2", format));
    }

    @Test
    public void testStringToDate() {
        final Date date = DateUtils.stringToDate("2019-06-11 16:12:57");

        Assert.assertEquals("Tue Jun 11 16:12:57 BST 2019", date.toString());
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
}