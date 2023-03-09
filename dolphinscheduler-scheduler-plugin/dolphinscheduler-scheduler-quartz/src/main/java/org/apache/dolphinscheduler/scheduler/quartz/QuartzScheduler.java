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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;
import org.apache.dolphinscheduler.scheduler.api.SchedulerException;
import org.apache.dolphinscheduler.scheduler.quartz.utils.QuartzTaskUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.extern.slf4j.Slf4j;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

@Slf4j
public class QuartzScheduler implements SchedulerApi {

    @Autowired
    private Scheduler scheduler;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void start() throws SchedulerException {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new SchedulerException("Failed to start quartz scheduler ", e);
        }
    }

    @Override
    public void insertOrUpdateScheduleTask(int projectId, Schedule schedule) throws SchedulerException {
        JobKey jobKey = QuartzTaskUtils.getJobKey(schedule.getId(), projectId);
        Map<String, Object> jobDataMap = QuartzTaskUtils.buildDataMap(projectId, schedule);
        String cronExpression = schedule.getCrontab();
        String timezoneId = schedule.getTimezoneId();

        /**
         * transform from server default timezone to schedule timezone
         * e.g. server default timezone is `UTC`
         * user set a schedule with startTime `2022-04-28 10:00:00`, timezone is `Asia/Shanghai`,
         * api skip to transform it and save into databases directly, startTime `2022-04-28 10:00:00`, timezone is `UTC`, which actually added 8 hours,
         * so when add job to quartz, it should recover by transform timezone
         */
        Date startDate = DateUtils.transformTimezoneDate(schedule.getStartTime(), timezoneId);
        Date endDate = DateUtils.transformTimezoneDate(schedule.getEndTime(), timezoneId);
        /**
         * If the start time is less than the current time, the start time is set to the current time.
         * We do this change to avoid misfires all triggers when update the scheduler.
         */
        Date now = new Date();
        if (startDate.before(now)) {
            startDate = now;
        }

        lock.writeLock().lock();
        try {

            JobDetail jobDetail;
            // add a task (if this task already exists, return this task directly)
            if (scheduler.checkExists(jobKey)) {

                jobDetail = scheduler.getJobDetail(jobKey);
                jobDetail.getJobDataMap().putAll(jobDataMap);
            } else {
                jobDetail = newJob(ProcessScheduleTask.class).withIdentity(jobKey).build();

                jobDetail.getJobDataMap().putAll(jobDataMap);

                scheduler.addJob(jobDetail, false, true);

                log.info("Add job, job name: {}, group name: {}", jobKey.getName(), jobKey.getGroup());
            }

            TriggerKey triggerKey = new TriggerKey(jobKey.getName(), jobKey.getGroup());
            /*
             * Instructs the Scheduler that upon a mis-fire situation, the CronTrigger wants to have it's next-fire-time
             * updated to the next time in the schedule after the current time (taking into account any associated
             * Calendar), but it does not want to be fired now.
             */
            CronTrigger cronTrigger = newTrigger()
                    .withIdentity(triggerKey)
                    .startAt(startDate)
                    .endAt(endDate)
                    .withSchedule(
                            cronSchedule(cronExpression)
                                    .withMisfireHandlingInstructionIgnoreMisfires()
                                    .inTimeZone(DateUtils.getTimezone(timezoneId)))
                    .forJob(jobDetail).build();

            if (scheduler.checkExists(triggerKey)) {
                // updateProcessInstance scheduler trigger when scheduler cycle changes
                CronTrigger oldCronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                String oldCronExpression = oldCronTrigger.getCronExpression();

                if (!Strings.nullToEmpty(cronExpression).equalsIgnoreCase(Strings.nullToEmpty(oldCronExpression))) {
                    // reschedule job trigger
                    scheduler.rescheduleJob(triggerKey, cronTrigger);
                    log.info(
                            "reschedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                            triggerKey.getName(), triggerKey.getGroup(), cronExpression, startDate, endDate);
                }
            } else {
                scheduler.scheduleJob(cronTrigger);
                log.info(
                        "schedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                        triggerKey.getName(), triggerKey.getGroup(), cronExpression, startDate, endDate);
            }

        } catch (Exception e) {
            log.error("Failed to add scheduler task, projectId: {}, scheduler: {}", projectId, schedule, e);
            throw new SchedulerException("Add schedule job failed", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteScheduleTask(int projectId, int scheduleId) throws SchedulerException {
        JobKey jobKey = QuartzTaskUtils.getJobKey(scheduleId, projectId);
        try {
            if (scheduler.checkExists(jobKey)) {
                log.info("Try to delete scheduler task, projectId: {}, schedulerId: {}", projectId, scheduleId);
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception e) {
            log.error("Failed to delete scheduler task, projectId: {}, schedulerId: {}", projectId, scheduleId, e);
            throw new SchedulerException("Failed to delete scheduler task");
        }
    }

    @Override
    public void close() {
        // nothing to do
        try {
            scheduler.shutdown();
        } catch (org.quartz.SchedulerException e) {
            throw new SchedulerException("Failed to shutdown scheduler", e);
        }
    }
}
