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

package org.apache.dolphinscheduler.service.quartz.impl;

import static org.apache.dolphinscheduler.common.Constants.PROJECT_ID;
import static org.apache.dolphinscheduler.common.Constants.QUARTZ_JOB_GROUP_PREFIX;
import static org.apache.dolphinscheduler.common.Constants.QUARTZ_JOB_PREFIX;
import static org.apache.dolphinscheduler.common.Constants.SCHEDULE;
import static org.apache.dolphinscheduler.common.Constants.SCHEDULE_ID;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.quartz.QuartzExecutor;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuartzExecutorImpl implements QuartzExecutor {
    private static final Logger logger = LoggerFactory.getLogger(QuartzExecutorImpl.class);

    @Autowired
    private Scheduler scheduler;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * add task trigger , if this task already exists, return this task with updated trigger
     *
     * @param clazz job class name
     * @param projectId projectId
     * @param schedule schedule
     */
    @Override
    public void addJob(Class<? extends Job> clazz, int projectId, final Schedule schedule) {
        String jobName = this.buildJobName(schedule.getId());
        String jobGroupName = this.buildJobGroupName(projectId);

        Map<String, Object> jobDataMap = this.buildDataMap(projectId, schedule);
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

        lock.writeLock().lock();
        try {

            JobKey jobKey = new JobKey(jobName, jobGroupName);
            JobDetail jobDetail;
            //add a task (if this task already exists, return this task directly)
            if (scheduler.checkExists(jobKey)) {

                jobDetail = scheduler.getJobDetail(jobKey);
                jobDetail.getJobDataMap().putAll(jobDataMap);
            } else {
                jobDetail = newJob(clazz).withIdentity(jobKey).build();

                jobDetail.getJobDataMap().putAll(jobDataMap);

                scheduler.addJob(jobDetail, false, true);

                logger.info("Add job, job name: {}, group name: {}",
                        jobName, jobGroupName);
            }

            TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
            /*
             * Instructs the Scheduler that upon a mis-fire
             * situation, the CronTrigger wants to have it's
             * next-fire-time updated to the next time in the schedule after the
             * current time (taking into account any associated Calendar),
             * but it does not want to be fired now.
             */
            CronTrigger cronTrigger = newTrigger()
                    .withIdentity(triggerKey)
                    .startAt(startDate)
                    .endAt(endDate)
                    .withSchedule(
                            cronSchedule(cronExpression)
                                    .withMisfireHandlingInstructionDoNothing()
                                    .inTimeZone(DateUtils.getTimezone(timezoneId))
                    )
                    .forJob(jobDetail).build();

            if (scheduler.checkExists(triggerKey)) {
                // updateProcessInstance scheduler trigger when scheduler cycle changes
                CronTrigger oldCronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                String oldCronExpression = oldCronTrigger.getCronExpression();

                if (!StringUtils.equalsIgnoreCase(cronExpression, oldCronExpression)) {
                    // reschedule job trigger
                    scheduler.rescheduleJob(triggerKey, cronTrigger);
                    logger.info("reschedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                            jobName, jobGroupName, cronExpression, startDate, endDate);
                }
            } else {
                scheduler.scheduleJob(cronTrigger);
                logger.info("schedule job trigger, triggerName: {}, triggerGroupName: {}, cronExpression: {}, startDate: {}, endDate: {}",
                        jobName, jobGroupName, cronExpression, startDate, endDate);
            }

        } catch (Exception e) {
            throw new ServiceException("add job failed", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String buildJobName(int processId) {
        return QUARTZ_JOB_PREFIX + UNDERLINE + processId;
    }

    @Override
    public String buildJobGroupName(int projectId) {
        return QUARTZ_JOB_GROUP_PREFIX + UNDERLINE + projectId;
    }

    @Override
    public Map<String, Object> buildDataMap(int projectId, Schedule schedule) {
        Map<String, Object> dataMap = new HashMap<>(8);
        dataMap.put(PROJECT_ID, projectId);
        dataMap.put(SCHEDULE_ID, schedule.getId());
        dataMap.put(SCHEDULE, JSONUtils.toJsonString(schedule));

        return dataMap;
    }

}
