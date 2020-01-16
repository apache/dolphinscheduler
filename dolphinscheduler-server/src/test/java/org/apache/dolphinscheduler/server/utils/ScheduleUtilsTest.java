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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.assertEquals;

/**
 * Test ScheduleUtils
 */
public class ScheduleUtilsTest {

    /**
     * Test the getRecentTriggerTime method
     */
    @Test
    public void testGetRecentTriggerTime() {
        Date from = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date to = DateUtils.stringToDate("2020-01-31 01:00:00");
        // test date
        assertEquals(0, ScheduleUtils.getRecentTriggerTime("0 0 0 * * ? ", to, from).size());
        // test error cron
        assertEquals(0, ScheduleUtils.getRecentTriggerTime("0 0 0 * *", from, to).size());
        // test cron
        assertEquals(31, ScheduleUtils.getRecentTriggerTime("0 0 0 * * ? ", from, to).size());
    }
}