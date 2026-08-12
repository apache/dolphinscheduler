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

import org.apache.dolphinscheduler.common.enums.ScheduleMissedFirePolicy;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.IntervalSchedule;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Date;

import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * Builds a Quartz {@link SimpleTrigger} from a fixed-interval schedule expression.
 */
public class QuartzSimpleTriggerBuilder {

    private Integer projectId;

    private Schedule schedule;

    public static QuartzSimpleTriggerBuilder newBuilder() {
        return new QuartzSimpleTriggerBuilder();
    }

    public QuartzSimpleTriggerBuilder withProjectId(Integer projectId) {
        this.projectId = projectId;
        return this;
    }

    public QuartzSimpleTriggerBuilder withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public SimpleTrigger build() {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("schedule cannot be null");
        }

        IntervalSchedule intervalSchedule = IntervalSchedule.parse(schedule.getCrontab());
        JobKey jobKey = QuartzJobKey.of(projectId, schedule.getId()).toJobKey();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
        Date startTime = DateUtils.transformTimezoneDate(schedule.getStartTime(), schedule.getTimezoneId());
        Date endTime = DateUtils.transformTimezoneDate(schedule.getEndTime(), schedule.getTimezoneId());
        Date now = new Date();
        if (startTime.before(now)) {
            startTime = now;
        }

        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(intervalSchedule.getIntervalMilliseconds());
        if (intervalSchedule.getRepeatCount() < 0) {
            scheduleBuilder.repeatForever();
        } else {
            scheduleBuilder.withRepeatCount(intervalSchedule.getRepeatCount());
        }

        ScheduleMissedFirePolicy missedFirePolicy = schedule.getMissedFirePolicy();
        if (missedFirePolicy == ScheduleMissedFirePolicy.SKIP_MISSED) {
            scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
        } else if (missedFirePolicy == ScheduleMissedFirePolicy.FIRE_ONCE_NOW) {
            scheduleBuilder.withMisfireHandlingInstructionFireNow();
        } else {
            scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        }

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(startTime)
                .endAt(endTime)
                .withSchedule(scheduleBuilder)
                .build();
    }
}
