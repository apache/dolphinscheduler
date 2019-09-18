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
package cn.escheduler.server.quartz;


import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.CommandType;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.Command;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.Schedule;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Date;

import static cn.escheduler.server.quartz.QuartzExecutors.buildJobGroupName;
import static cn.escheduler.server.quartz.QuartzExecutors.buildJobName;

/**
 * process schedule job
 * <p>
 *  {@link Job}
 * </p>
 */
public class ProcessScheduleJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ProcessScheduleJob.class);

    /**
     * {@link ProcessDao}
     */
    private static ProcessDao processDao;


    /**
     * init
     */
    public static void init(ProcessDao processDao) {
        ProcessScheduleJob.processDao = processDao;
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     *
     * <p>
     * The implementation may wish to set a
     * {@link JobExecutionContext#setResult(Object) result} object on the
     * {@link JobExecutionContext} before this method exits.  The result itself
     * is meaningless to Quartz, but may be informative to
     * <code>{@link JobListener}s</code> or
     * <code>{@link TriggerListener}s</code> that are watching the job's
     * execution.
     * </p>
     *
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        //TODO...
        Assert.notNull(processDao, "please call init() method first");

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        int projectId = dataMap.getInt(Constants.PROJECT_ID);
        int scheduleId = dataMap.getInt(Constants.SCHEDULE_ID);


        Date scheduledFireTime = context.getScheduledFireTime();


        Date fireTime = context.getFireTime();

        logger.info("scheduled fire time :{}, fire time :{}, process id :{}", scheduledFireTime, fireTime, scheduleId);

        // query schedule
        Schedule schedule = processDao.querySchedule(scheduleId);
        if (schedule == null) {
            logger.warn("process schedule does not exist in db，delete schedule job in quartz, projectId:{}, scheduleId:{}", projectId, scheduleId);
            deleteJob(projectId, scheduleId);
            return;
        }


        ProcessDefinition processDefinition = processDao.findProcessDefineById(schedule.getProcessDefinitionId());
        // release state : online/offline
        ReleaseState releaseState = processDefinition.getReleaseState();
        if (processDefinition == null || releaseState == ReleaseState.OFFLINE) {
            logger.warn("process definition does not exist in db or offline，need not to create command, projectId:{}, processId:{}", projectId, scheduleId);
            return;
        }

        Command command = new Command();
        command.setCommandType(CommandType.SCHEDULER);
        command.setExecutorId(schedule.getUserId());
        command.setFailureStrategy(schedule.getFailureStrategy());
        command.setProcessDefinitionId(schedule.getProcessDefinitionId());
        command.setScheduleTime(scheduledFireTime);
        command.setStartTime(fireTime);
        command.setWarningGroupId(schedule.getWarningGroupId());
        command.setWorkerGroupId(schedule.getWorkerGroupId());
        command.setWarningType(schedule.getWarningType());
        command.setProcessInstancePriority(schedule.getProcessInstancePriority());

        processDao.createCommand(command);
    }


    /**
     * delete job
     */
    private void deleteJob(int projectId, int scheduleId) {
        String jobName = buildJobName(scheduleId);
        String jobGroupName = buildJobGroupName(projectId);
        QuartzExecutors.getInstance().deleteJob(jobName, jobGroupName);
    }
}
