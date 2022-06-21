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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRemoteChannel;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.lang.SystemUtils;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.netty.channel.Channel;

/**
 * worker request processor
 */
@Component
public class TaskExecuteProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteProcessor.class);

    /**
     * worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

    /**
     * task callback service
     */
    @Autowired
    private TaskCallbackService taskCallbackService;

    /**
     * alert client service
     */
    @Autowired
    private AlertClientService alertClientService;

    @Autowired
    private TaskPluginManager taskPluginManager;

    /**
     * task execute manager
     */
    @Autowired
    private WorkerManagerThread workerManager;

    @Counted(value = "ds.task.execution.count", description = "task execute total count")
    @Timed(value = "ds.task.execution.duration", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskExecuteRequestCommand taskRequestCommand = JSONUtils.parseObject(
                command.getBody(), TaskExecuteRequestCommand.class);

        if (taskRequestCommand == null) {
            logger.error("task execute request command is null");
            return;
        }
        logger.info("task execute request command : {}", taskRequestCommand);

        String contextJson = taskRequestCommand.getTaskExecutionContext();
        TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextJson, TaskExecutionContext.class);

        if (taskExecutionContext == null) {
            logger.error("task execution context is null");
            return;
        }
        TaskMetrics.incrTaskTypeExecuteCount(taskExecutionContext.getTaskType());

        // set cache, it will be used when kill task
        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);

        // todo custom logger

        taskExecutionContext.setHost(NetUtils.getAddr(workerConfig.getListenPort()));
        taskExecutionContext.setLogPath(LogUtils.getTaskLogPath(taskExecutionContext));

        if (Constants.DRY_RUN_FLAG_NO == taskExecutionContext.getDryRun()) {
            boolean osUserExistFlag ;
            //if Using distributed is true and Currently supported systems are linux,Should not let it automatically
            //create tenants,so TenantAutoCreate has no effect
            if (workerConfig.isTenantDistributedUser() && SystemUtils.IS_OS_LINUX){
                //use the id command to judge in linux
                osUserExistFlag = OSUtils.existTenantCodeInLinux(taskExecutionContext.getTenantCode());
            }else if (CommonUtils.isSudoEnable() && workerConfig.isTenantAutoCreate()){
                // if not exists this user, then create
                OSUtils.createUserIfAbsent(taskExecutionContext.getTenantCode());
                osUserExistFlag = OSUtils.getUserList().contains(taskExecutionContext.getTenantCode());
            }else {
                osUserExistFlag = OSUtils.getUserList().contains(taskExecutionContext.getTenantCode());
            }

            // check if the OS user exists
            if (!osUserExistFlag) {
                logger.error("tenantCode: {} does not exist, taskInstanceId: {}",
                        taskExecutionContext.getTenantCode(), taskExecutionContext.getTaskInstanceId());
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
                taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.FAILURE);
                taskExecutionContext.setEndTime(new Date());
                taskCallbackService.sendTaskExecuteResponseCommand(taskExecutionContext);
                return;
            }

            // local execute path
            String execLocalPath = getExecLocalPath(taskExecutionContext);
            logger.info("task instance local execute path : {}", execLocalPath);
            taskExecutionContext.setExecutePath(execLocalPath);

            try {
                FileUtils.createWorkDirIfAbsent(execLocalPath);
            } catch (Throwable ex) {
                logger.error("create execLocalPath fail, path: {}, taskInstanceId: {}", execLocalPath, taskExecutionContext.getTaskInstanceId());
                logger.error("create executeLocalPath fail", ex);
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
                taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.FAILURE);
                taskCallbackService.sendTaskExecuteResponseCommand(taskExecutionContext);
                return;
            }
        }

        taskCallbackService.addRemoteChannel(taskExecutionContext.getTaskInstanceId(), new NettyRemoteChannel(channel, command.getOpaque()));

        // delay task process
        long remainTime = DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(), taskExecutionContext.getDelayTime() * 60L);
        if (remainTime > 0) {
            logger.info("delay the execution of task instance {}, delay time: {} s", taskExecutionContext.getTaskInstanceId(), remainTime);
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.DELAY_EXECUTION);
            taskExecutionContext.setStartTime(null);
            taskCallbackService.sendTaskExecuteDelayCommand(taskExecutionContext);
        }

        // submit task to manager
        boolean offer = workerManager.offer(new TaskExecuteThread(taskExecutionContext, taskCallbackService, alertClientService, taskPluginManager));
        if (!offer) {
            logger.warn("submit task to wait queue error, queue is full, queue size is {}, taskInstanceId: {}",
                workerManager.getWaitSubmitQueueSize(), taskExecutionContext.getTaskInstanceId());
            taskCallbackService.sendRecallCommand(taskExecutionContext);
        }
    }

    /**
     * get execute local path
     *
     * @param taskExecutionContext taskExecutionContext
     * @return execute local path
     */
    private String getExecLocalPath(TaskExecutionContext taskExecutionContext) {
        return FileUtils.getProcessExecDir(taskExecutionContext.getProjectCode(),
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }
}
