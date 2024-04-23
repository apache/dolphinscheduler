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

import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.scheduler.api.SchedulerException;
import org.apache.dolphinscheduler.scheduler.quartz.exception.QuartzSchedulerExceptionEnum;

import lombok.extern.slf4j.Slf4j;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import com.google.common.collect.Sets;

@Slf4j
public class QuartzScheduler implements SchedulerApi {

    private final Scheduler scheduler;

    public QuartzScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void start() throws SchedulerException {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new SchedulerException(QuartzSchedulerExceptionEnum.QUARTZ_SCHEDULER_START_ERROR, e);
        }
    }

    @Override
    public void insertOrUpdateScheduleTask(int projectId, Schedule schedule) throws SchedulerException {
        try {
            CronTrigger cornTrigger = QuartzCornTriggerBuilder.newBuilder()
                    .withProjectId(projectId)
                    .withSchedule(schedule)
                    .build();
            JobDetail jobDetail = QuartzJobDetailBuilder.newBuilder()
                    .withProjectId(projectId)
                    .withSchedule(schedule.getId())
                    .build();
            scheduler.scheduleJob(jobDetail, Sets.newHashSet(cornTrigger), true);
            log.info("Success scheduleJob: {} with trigger: {} at quartz", jobDetail, cornTrigger);
        } catch (Exception e) {
            log.error("Failed to add scheduler task, projectId: {}, scheduler: {}", projectId, schedule, e);
            throw new SchedulerException(QuartzSchedulerExceptionEnum.QUARTZ_UPSERT_JOB_ERROR, e);
        }
    }

    @Override
    public void deleteScheduleTask(int projectId, int scheduleId) throws SchedulerException {
        JobKey jobKey = QuartzJobKey.of(projectId, scheduleId).toJobKey();
        try {
            if (scheduler.checkExists(jobKey)) {
                log.info("Try to delete scheduler task, projectId: {}, schedulerId: {}", projectId, scheduleId);
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception e) {
            log.error("Failed to delete scheduler task, projectId: {}, schedulerId: {}", projectId, scheduleId, e);
            throw new SchedulerException(QuartzSchedulerExceptionEnum.QUARTZ_DELETE_JOB_ERROR, e);
        }
    }

    @Override
    public void close() {
        // nothing to do
        try {
            scheduler.shutdown();
        } catch (Exception e) {
            throw new SchedulerException(QuartzSchedulerExceptionEnum.QUARTZ_SCHEDULER_SHOWDOWN_ERROR, e);
        }
    }
}
