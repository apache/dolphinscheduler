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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.scheduler.quartz.utils.QuartzTaskUtils;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

@Slf4j
public class ProcessScheduleTask extends QuartzJobBean {

    @Autowired
    private ProcessService processService;

    @Autowired
    private CommandService commandService;

    @Counted(value = "ds.master.quartz.job.executed")
    @Timed(value = "ds.master.quartz.job.execution.time", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        int projectId = dataMap.getInt(QuartzTaskUtils.PROJECT_ID);
        int scheduleId = dataMap.getInt(QuartzTaskUtils.SCHEDULE_ID);

        Date scheduledFireTime = context.getScheduledFireTime();

        Date fireTime = context.getFireTime();

        log.info("scheduled fire time :{}, fire time :{}, scheduleId :{}", scheduledFireTime, fireTime, scheduleId);

        // query schedule
        Schedule schedule = processService.querySchedule(scheduleId);
        if (schedule == null || ReleaseState.OFFLINE == schedule.getReleaseState()) {
            log.warn(
                    "process schedule does not exist in db or process schedule offline，delete schedule job in quartz, projectId:{}, scheduleId:{}",
                    projectId, scheduleId);
            deleteJob(context, projectId, scheduleId);
            return;
        }

        ProcessDefinition processDefinition =
                processService.findProcessDefinitionByCode(schedule.getProcessDefinitionCode());
        // release state : online/offline
        ReleaseState releaseState = processDefinition.getReleaseState();
        if (releaseState == ReleaseState.OFFLINE) {
            log.warn(
                    "process definition does not exist in db or offline，need not to create command, projectId:{}, processDefinitionId:{}",
                    projectId, processDefinition.getId());
            return;
        }

        Command command = new Command();
        command.setCommandType(CommandType.SCHEDULER);
        command.setExecutorId(schedule.getUserId());
        command.setFailureStrategy(schedule.getFailureStrategy());
        command.setProcessDefinitionCode(schedule.getProcessDefinitionCode());
        command.setScheduleTime(scheduledFireTime);
        command.setStartTime(fireTime);
        command.setWarningGroupId(schedule.getWarningGroupId());
        String workerGroup = StringUtils.isEmpty(schedule.getWorkerGroup()) ? Constants.DEFAULT_WORKER_GROUP
                : schedule.getWorkerGroup();
        command.setWorkerGroup(workerGroup);
        command.setTenantCode(schedule.getTenantCode());
        command.setEnvironmentCode(schedule.getEnvironmentCode());
        command.setWarningType(schedule.getWarningType());
        command.setProcessInstancePriority(schedule.getProcessInstancePriority());
        command.setProcessDefinitionVersion(processDefinition.getVersion());

        commandService.createCommand(command);
    }

    private void deleteJob(JobExecutionContext context, int projectId, int scheduleId) {
        final Scheduler scheduler = context.getScheduler();
        JobKey jobKey = QuartzTaskUtils.getJobKey(scheduleId, projectId);
        try {
            if (scheduler.checkExists(jobKey)) {
                log.info("Try to delete job: {}, projectId: {}, schedulerId", projectId, scheduleId);
                scheduler.deleteJob(jobKey);
            }
        } catch (Exception e) {
            log.error("Failed to delete job: {}", jobKey);
        }
    }
}
