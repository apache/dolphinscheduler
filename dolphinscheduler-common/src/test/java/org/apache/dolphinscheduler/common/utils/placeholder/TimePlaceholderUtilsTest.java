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

package org.apache.dolphinscheduler.common.utils.placeholder;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class TimePlaceholderUtilsTest {

    private Date date;

    @Before
    public void init() {
        date = DateUtils.parse("20170101010101", "yyyyMMddHHmmss");
    }

    @Test
    public void replacePlaceholdersT() {
        Assert.assertEquals("2017test12017:***2016-12-31,20170102,20170130,20161227,20161231", TimePlaceholderUtils
            .replacePlaceholders("$[yyyy]test1$[yyyy:***]$[yyyy-MM-dd-1],$[month_begin(yyyyMMdd, 1)],$[month_end(yyyyMMdd, -1)],$[week_begin(yyyyMMdd, 1)],$[week_end(yyyyMMdd, -1)]",
                date, true));

        Assert.assertEquals("1483200061,1483290061,1485709261,1482771661,1483113600,1483203661", TimePlaceholderUtils.replacePlaceholders("$[timestamp(yyyyMMdd00mmss)],"
                + "$[timestamp(month_begin(yyyyMMddHHmmss, 1))],"
                + "$[timestamp(month_end(yyyyMMddHHmmss, -1))],"
                + "$[timestamp(week_begin(yyyyMMddHHmmss, 1))],"
                + "$[timestamp(week_end(yyyyMMdd000000, -1))],"
                + "$[timestamp(yyyyMMddHHmmss)]",
            date, true));
    }

    @Test
    public void calcMinutesT() {
        Assert.assertEquals("Sun Jan 01 01:01:01 CST 2017=yyyy", TimePlaceholderUtils.calcMinutes("yyyy", date).toString());
        Assert.assertEquals("Sun Jan 08 01:01:01 CST 2017=yyyyMMdd", TimePlaceholderUtils.calcMinutes("yyyyMMdd+7*1", date).toString());
        Assert.assertEquals("Sun Dec 25 01:01:01 CST 2016=yyyyMMdd", TimePlaceholderUtils.calcMinutes("yyyyMMdd-7*1", date).toString());
        Assert.assertEquals("Mon Jan 02 01:01:01 CST 2017=yyyyMMdd", TimePlaceholderUtils.calcMinutes("yyyyMMdd+1", date).toString());
        Assert.assertEquals("Sat Dec 31 01:01:01 CST 2016=yyyyMMdd", TimePlaceholderUtils.calcMinutes("yyyyMMdd-1", date).toString());
        Assert.assertEquals("Sun Jan 01 02:01:01 CST 2017=yyyyMMddHH", TimePlaceholderUtils.calcMinutes("yyyyMMddHH+1/24", date).toString());
        Assert.assertEquals("Sun Jan 01 00:01:01 CST 2017=yyyyMMddHH", TimePlaceholderUtils.calcMinutes("yyyyMMddHH-1/24", date).toString());
    }

    @Test
    public void calcMonthsT() {
        Assert.assertEquals("Mon Jan 01 01:01:01 CST 2018=yyyyMMdd", TimePlaceholderUtils.calcMonths("add_months(yyyyMMdd,12*1)", date).toString());
        Assert.assertEquals("Fri Jan 01 01:01:01 CST 2016=yyyyMMdd", TimePlaceholderUtils.calcMonths("add_months(yyyyMMdd,-12*1)", date).toString());
    }

    @Test
    public void testGetPlaceHolderTime() {

        Assert.assertEquals("20170101", TimePlaceholderUtils.getPlaceHolderTime("yyyyMMdd", date));
    }

}