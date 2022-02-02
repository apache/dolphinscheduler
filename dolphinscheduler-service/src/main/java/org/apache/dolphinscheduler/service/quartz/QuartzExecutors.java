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

package org.apache.dolphinscheduler.service.quartz;

import static org.apache.dolphinscheduler.common.Constants.PROJECT_ID;
import static org.apache.dolphinscheduler.common.Constants.QUARTZ_JOB_GROUP_PRIFIX;
import static org.apache.dolphinscheduler.common.Constants.QUARTZ_JOB_PRIFIX;
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

public class QuartzExecutors {
    private static final Logger logger = LoggerFactory.getLogger(QuartzExecutors.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final class Holder {
        private static final QuartzExecutors instance = new QuartzExecutors();
    }

    private QuartzExecutors() {
    }

    public static QuartzExecutors getInstance() {
        return Holder.instance;
    }

    /**
     * add task trigger , if this task already exists, return this task with updated trigger
     *
     * @param clazz job class name
     * @param projectId projectId
     * @param schedule schedule
     */
    public void addJob(Scheduler scheduler, Class<? extends Job> clazz, int projectId, final Schedule schedule) {
        String jobName = QuartzExecutors.buildJobName(schedule.getId());
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);
        Date startDate = schedule.getStartTime();
        Date endDate = schedule.getEndTime();
        Map<String, Object> jobDataMap = QuartzExecutors.buildDataMap(projectId, schedule);
        String cronExpression = schedule.getCrontab();
        String timezoneId = schedule.getTimezoneId();

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
                    .startAt(DateUtils.getTimezoneDate(startDate, timezoneId))
                    .endAt(DateUtils.getTimezoneDate(endDate, timezoneId))
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

    public static String buildJobName(int processId) {
        return QUARTZ_JOB_PRIFIX + UNDERLINE + processId;
    }

    public static String buildJobGroupName(int projectId) {
        return QUARTZ_JOB_GROUP_PRIFIX + UNDERLINE + projectId;
    }

    public static Map<String, Object> buildDataMap(int projectId, Schedule schedule) {
        Map<String, Object> dataMap = new HashMap<>(8);
        dataMap.put(PROJECT_ID, projectId);
        dataMap.put(SCHEDULE_ID, schedule.getId());
        dataMap.put(SCHEDULE, JSONUtils.toJsonString(schedule));

        return dataMap;
    }

}
