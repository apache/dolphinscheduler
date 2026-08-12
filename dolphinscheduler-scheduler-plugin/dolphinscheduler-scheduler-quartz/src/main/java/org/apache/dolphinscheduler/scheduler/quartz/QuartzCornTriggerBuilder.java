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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * QuartzCornTriggerBuilder used to build a {@link CronTrigger} instance.
 */
public class QuartzCornTriggerBuilder implements QuartzTriggerBuilder {

    private Integer projectId;

    private Schedule schedule;

    public static QuartzCornTriggerBuilder newBuilder() {
        return new QuartzCornTriggerBuilder();
    }

    public QuartzCornTriggerBuilder withProjectId(Integer projectId) {
        this.projectId = projectId;
        return this;
    }

    public QuartzCornTriggerBuilder withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    @Override
    public CronTrigger build() {

        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }
        if (schedule == null) {
            throw new IllegalArgumentException("schedule cannot be null");
        }

        /**
         * transform from server default timezone to schedule timezone
         * e.g. server default timezone is `UTC`
         * user set a schedule with startTime `2022-04-28 10:00:00`, timezone is `Asia/Shanghai`,
         * api skip to transform it and save into databases directly, startTime `2022-04-28 10:00:00`, timezone is `UTC`, which actually added 8 hours,
         * so when add job to quartz, it should recover by transform timezone
         */
        Date startDate = DateUtils.transformTimezoneDate(schedule.getStartTime(), schedule.getTimezoneId());
        Date endDate = DateUtils.transformTimezoneDate(schedule.getEndTime(), schedule.getTimezoneId());
        /**
         * If the start time is less than the current time, the start time is set to the current time.
         * We do this change to avoid misfires all triggers when update the scheduler.
         */
        Date now = new Date();
        if (startDate.before(now)) {
            startDate = now;
        }
        JobKey jobKey = QuartzJobKey.of(projectId, schedule.getId()).toJobKey();

        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(startDate)
                .endAt(endDate)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(schedule.getCrontab())
                                .withMisfireHandlingInstructionIgnoreMisfires()
                                .inTimeZone(DateUtils.getTimezone(schedule.getTimezoneId())))
                .build();
    }

}
