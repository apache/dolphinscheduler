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
package org.apache.dolphinscheduler.server.quartz;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * single Quartz executors instance
 */
public class QuartzExecutors {

  /**
   * logger of QuartzExecutors
   */
  private static final Logger logger = LoggerFactory.getLogger(QuartzExecutors.class);

  /**
   * read write lock
   */
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * A Scheduler maintains a registry of org.quartz.JobDetail and Trigger.
   */
  private static Scheduler scheduler;

  /**
   * instance of QuartzExecutors
   */
  private static volatile QuartzExecutors INSTANCE = null;

  private QuartzExecutors() {}

  /**
   * thread safe and performance promote
   * @return instance of Quartz Executors
   */
  public static QuartzExecutors getInstance() {
    if (INSTANCE == null) {
      synchronized (QuartzExecutors.class) {
        // when more than two threads run into the first null check same time, to avoid instanced more than one time, it needs to be checked again.
        if (INSTANCE == null) {
          INSTANCE = new QuartzExecutors();
          //finish QuartzExecutors init
          INSTANCE.init();
        }
      }
    }
    return INSTANCE;
  }


  /**
   * init
   *
   * Returns a client-usable handle to a Scheduler.
   */
  private void init() {
    try {
      SchedulerFactory schedulerFactory = new StdSchedulerFactory(Constants.QUARTZ_PROPERTIES_PATH);
      scheduler = schedulerFactory.getScheduler();

    } catch (SchedulerException e) {
      logger.error(e.getMessage(),e);
      System.exit(1);
    }

  }

  /**
   * Whether the scheduler has been started.
   *
   * @throws SchedulerException scheduler exception
   */
  public void start() throws SchedulerException {
    if (!scheduler.isStarted()){
      scheduler.start();
      logger.info("Quartz service started" );
    }
  }

  /**
   * stop all scheduled tasks
   *
   * Halts the Scheduler's firing of Triggers,
   * and cleans up all resources associated with the Scheduler.
   *
   * The scheduler cannot be re-started.
   * @throws SchedulerException scheduler exception
   */
  public void shutdown() throws SchedulerException {
    if (!scheduler.isShutdown()) {
        // don't wait for the task to complete
        scheduler.shutdown();
        logger.info("Quartz service stopped, and halt all tasks");
    }
  }


  /**
   * add task trigger , if this task already exists, return this task with updated trigger
   *
   * @param clazz             job class name
   * @param jobName           job name
   * @param jobGroupName      job group name
   * @param startDate         job start date
   * @param endDate           job end date
   * @param cronExpression    cron expression
   * @param jobDataMap        job parameters data map
   */
  public void addJob(Class<? extends Job> clazz,String jobName,String jobGroupName,Date startDate, Date endDate,
                                 String cronExpression,
                                 Map<String, Object> jobDataMap) {
    lock.writeLock().lock();
    try {

      JobKey jobKey = new JobKey(jobName, jobGroupName);
      JobDetail jobDetail;
      //add a task (if this task already exists, return this task directly)
      if (scheduler.checkExists(jobKey)) {

        jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDataMap != null) {
          jobDetail.getJobDataMap().putAll(jobDataMap);
        }
      } else {
        jobDetail = newJob(clazz).withIdentity(jobKey).build();

        if (jobDataMap != null) {
          jobDetail.getJobDataMap().putAll(jobDataMap);
        }

        scheduler.addJob(jobDetail, false, true);

        logger.info("Add job, job name: {}, group name: {}",
                jobName, jobGroupName);
      }

      TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
      /**
       * Instructs the Scheduler that upon a mis-fire
       * situation, the CronTrigger wants to have it's
       * next-fire-time updated to the next time in the schedule after the
       * current time (taking into account any associated Calendar),
       * but it does not want to be fired now.
       */
      CronTrigger cronTrigger = newTrigger().withIdentity(triggerKey).startAt(startDate).endAt(endDate)
              .withSchedule(cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing())
              .forJob(jobDetail).build();

      if (scheduler.checkExists(triggerKey)) {
          // updateProcessInstance scheduler trigger when scheduler cycle changes
          CronTrigger oldCronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
          String oldCronExpression = oldCronTrigger.getCronExpression();

          if (!StringUtils.equalsIgnoreCase(cronExpression,oldCronExpression)) {
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
      logger.error("add job failed", e);
      throw new RuntimeException("add job failed", e);
    } finally {
      lock.writeLock().unlock();
    }
  }


  /**
   * delete job
   *
   * @param jobName      job name
   * @param jobGroupName job group name
   * @return true if the Job was found and deleted.
   */
  public boolean deleteJob(String jobName, String jobGroupName) {
    lock.writeLock().lock();
    try {
      JobKey jobKey = new JobKey(jobName,jobGroupName);
      if(scheduler.checkExists(jobKey)){
        logger.info("try to delete job, job name: {}, job group name: {},", jobName, jobGroupName);
        return scheduler.deleteJob(jobKey);
      }else {
        return true;
      }

    } catch (SchedulerException e) {
      logger.error(String.format("delete job : %s failed",jobName), e);
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  /**
   * delete all jobs in job group
   *
   * @param jobGroupName job group name
   *
   * @return true if all of the Jobs were found and deleted, false if
   *      one or more were not deleted.
   */
  public boolean deleteAllJobs(String jobGroupName) {
    lock.writeLock().lock();
    try {
      logger.info("try to delete all jobs in job group: {}", jobGroupName);
      List<JobKey> jobKeys = new ArrayList<>();
      jobKeys.addAll(scheduler.getJobKeys(GroupMatcher.groupEndsWith(jobGroupName)));

      return scheduler.deleteJobs(jobKeys);
    } catch (SchedulerException e) {
      logger.error(String.format("delete all jobs in job group: %s failed",jobGroupName), e);
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }

  /**
   * build job name
   * @param processId process id
   * @return job name
   */
  public static String buildJobName(int processId) {
    StringBuilder sb = new StringBuilder(30);
    sb.append(Constants.QUARTZ_JOB_PRIFIX).append(Constants.UNDERLINE).append(processId);
    return sb.toString();
  }

  /**
   * build job group name
   * @param projectId project id
   * @return job group name
   */
  public static String buildJobGroupName(int projectId) {
    StringBuilder sb = new StringBuilder(30);
    sb.append(Constants.QUARTZ_JOB_GROUP_PRIFIX).append(Constants.UNDERLINE).append(projectId);
    return sb.toString();
  }

  /**
   * add params to map
   *
   * @param projectId   project id
   * @param scheduleId  schedule id
   * @param schedule    schedule
   * @return data map
   */
  public static Map<String, Object> buildDataMap(int projectId, int scheduleId, Schedule schedule) {
    Map<String, Object> dataMap = new HashMap<>(3);
    dataMap.put(Constants.PROJECT_ID, projectId);
    dataMap.put(Constants.SCHEDULE_ID, scheduleId);
    dataMap.put(Constants.SCHEDULE, JSONUtils.toJson(schedule));

    return dataMap;
  }

}
