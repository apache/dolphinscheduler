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

package org.apache.dolphinscheduler.scheduler.quartz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.quartz.SimpleTrigger;

class QuartzSimpleTriggerBuilderTest {

    @Test
    void buildIntervalTrigger() {
        Schedule schedule = new Schedule();
        schedule.setId(2);
        schedule.setCrontab("{\"minute\":5,\"repeat\":3}");
        schedule.setTimezoneId("UTC");
        schedule.setStartTime(new Date(System.currentTimeMillis() + 60_000));
        schedule.setEndTime(new Date(System.currentTimeMillis() + 3_600_000));

        SimpleTrigger trigger = QuartzSimpleTriggerBuilder.newBuilder()
                .withProjectId(1)
                .withSchedule(schedule)
                .build();

        assertEquals(300_000L, trigger.getRepeatInterval());
        assertEquals(3, trigger.getRepeatCount());
    }
}
