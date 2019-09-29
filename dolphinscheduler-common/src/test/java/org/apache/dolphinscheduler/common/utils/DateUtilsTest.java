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
}