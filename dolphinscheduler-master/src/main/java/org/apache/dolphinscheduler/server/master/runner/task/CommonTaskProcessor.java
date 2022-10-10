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

package org.apache.dolphinscheduler.server.master.runner.task;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

import com.google.auto.service.AutoService;

/**
 * common task processor
 */
@AutoService(ITaskProcessor.class)
public class CommonTaskProcessor extends BaseTaskProcessor {

    private TaskPriorityQueue<TaskPriority> taskUpdateQueue;

    private NettyExecutorManager nettyExecutorManager = SpringApplicationContext.getBean(NettyExecutorManager.class);

    @Override
    protected boolean submitTask() {
        if (this.taskInstance.getTestFlag() == Constants.TEST_FLAG_YES) {
            convertExeEnvironmentOnlineToTest();
        }

        this.taskInstance =
                processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        return this.taskInstance != null;
    }

    @Override
    protected boolean resubmitTask() {
        if (this.taskInstance == null) {
            return false;
        }
        setTaskExecutionLogger();
        return dispatchTask();
    }

    @Override
    public boolean runTask() {
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        return true;
    }

    /**
     * common task cannot be paused
     */
    @Override
    protected boolean pauseTask() {
        return true;
    }

    @Override
    public String getType() {
        return Constants.COMMON_TASK_TYPE;
    }

    @Override
    public boolean dispatchTask() {
        try {
            if (taskUpdateQueue == null) {
                this.initQueue();
            }
            if (taskInstance.getState().isFinished()) {
                logger.info("Task {} has already finished, no need to submit to task queue, taskState: {}",
                        taskInstance.getName(), taskInstance.getState());
                return true;
            }
            // task cannot be submitted because its execution state is RUNNING or DELAY.
            if (taskInstance.getState() == TaskExecutionStatus.RUNNING_EXECUTION
                    || taskInstance.getState() == TaskExecutionStatus.DELAY_EXECUTION) {
                logger.info("Task {} is already running or delayed, no need to submit to task queue, taskState: {}",
                        taskInstance.getName(), taskInstance.getState());
                return true;
            }
            logger.info("Task {} is ready to dispatch to worker", taskInstance.getName());

            TaskPriority taskPriority = new TaskPriority(processInstance.getProcessInstancePriority().getCode(),
                    processInstance.getId(), taskInstance.getProcessInstancePriority().getCode(),
                    taskInstance.getId(), taskInstance.getTaskGroupPriority(),
                    org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP);

            TaskExecutionContext taskExecutionContext = getTaskExecutionContext(taskInstance);
            if (taskExecutionContext == null) {
                logger.error("Get taskExecutionContext fail, task: {}", taskInstance);
                return false;
            }

            taskPriority.setTaskExecutionContext(taskExecutionContext);

            taskUpdateQueue.put(taskPriority);
            logger.info("Task {} is submitted to priority queue success by master", taskInstance.getName());
            return true;
        } catch (Exception e) {
            logger.error("Task {} is submitted to priority queue error", taskInstance.getName(), e);
            return false;
        }
    }

    public void initQueue() {
        this.taskUpdateQueue = SpringApplicationContext.getBean(TaskPriorityQueueImpl.class);
    }

    @Override
    public boolean killTask() {

        try {
            taskInstance = processService.findTaskInstanceById(taskInstance.getId());
            if (taskInstance == null) {
                return true;
            }
            if (taskInstance.getState().isFinished()) {
                return true;
            }
            // we don't wait the kill response
            taskInstance.setState(TaskExecutionStatus.KILL);
            taskInstance.setEndTime(new Date());
            processService.updateTaskInstance(taskInstance);
            if (StringUtils.isNotEmpty(taskInstance.getHost())) {
                killRemoteTask();
            }
        } catch (Exception e) {
            logger.error("master kill task error, taskInstance id: {}", taskInstance.getId(), e);
            return false;
        }

        logger.info("master success kill taskInstance name: {} taskInstance id: {}",
                taskInstance.getName(), taskInstance.getId());
        return true;
    }

    private void killRemoteTask() throws ExecuteException {
        TaskKillRequestCommand killCommand = new TaskKillRequestCommand();
        killCommand.setTaskInstanceId(taskInstance.getId());

        ExecutionContext executionContext =
                new ExecutionContext(killCommand.convert2Command(), ExecutorType.WORKER, taskInstance);

        Host host = Host.of(taskInstance.getHost());
        executionContext.setHost(host);

        nettyExecutorManager.executeDirectly(executionContext);
    }

    protected void convertExeEnvironmentOnlineToTest() {
        //SQL taskType
        if (TaskConstants.TASK_TYPE_SQL.equals(taskInstance.getTaskType())) {
            //replace test data source
            Map<String, Object> taskDefinitionParams = JSONUtils.parseObject(taskInstance.getTaskDefine().getTaskParams(), new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> taskInstanceParams = JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<Map<String, Object>>() {
            });
            Integer onlineDataSourceId = (Integer) taskDefinitionParams.get(Constants.DATASOUCE);
            Integer testDataSourceId = processService.queryTestDataSourceId(onlineDataSourceId);
            taskDefinitionParams.put(Constants.DATASOUCE, testDataSourceId);
            taskInstanceParams.put(Constants.DATASOUCE, testDataSourceId);
            taskInstance.getTaskDefine().setTaskParams(JSONUtils.toJsonString(taskDefinitionParams));
            taskInstance.setTaskParams(JSONUtils.toJsonString(taskInstanceParams));
            if (null == testDataSourceId) {
                logger.warn("task name :{}, test data source replacement failed", taskInstance.getName());
            } else {
                logger.info("task name :{}, test data source replacement succeeded", taskInstance.getName());
            }
        }
    }
}
