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

import org.apache.dolphinscheduler.common.enums.ScheduleMissedFirePolicy;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

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
        assertEquals(Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY, trigger.getMisfireInstruction());
    }

    @Test
    void buildUnlimitedIntervalTriggerWithMisfirePolicies() {
        for (ScheduleMissedFirePolicy policy : ScheduleMissedFirePolicy.values()) {
            Schedule schedule = new Schedule();
            schedule.setId(2);
            schedule.setCrontab("{\"second\":10,\"repeat\":-1}");
            schedule.setTimezoneId("UTC");
            schedule.setStartTime(new Date(System.currentTimeMillis() + 60_000));
            schedule.setEndTime(new Date(System.currentTimeMillis() + 3_600_000));
            schedule.setMissedFirePolicy(policy);

            SimpleTrigger trigger = QuartzSimpleTriggerBuilder.newBuilder()
                    .withProjectId(1)
                    .withSchedule(schedule)
                    .build();

            assertEquals(SimpleTrigger.REPEAT_INDEFINITELY, trigger.getRepeatCount());
            int expectedInstruction;
            if (policy == ScheduleMissedFirePolicy.SKIP_MISSED) {
                expectedInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT;
            } else if (policy == ScheduleMissedFirePolicy.FIRE_ONCE_NOW) {
                expectedInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;
            } else {
                expectedInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
            }
            assertEquals(expectedInstruction, trigger.getMisfireInstruction());
        }
    }
}
