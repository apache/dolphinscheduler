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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
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
        this.taskInstance = processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }

        int taskGroupId = taskInstance.getTaskGroupId();
        if (taskGroupId > 0) {
            boolean acquireTaskGroup = processService.acquireTaskGroup(taskInstance.getId(),
                    taskInstance.getName(),
                    taskGroupId,
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getTaskGroupPriority());
            if (!acquireTaskGroup) {
                logger.info("submit task name :{}, but the first time to try to acquire task group failed", taskInstance.getName());
                return true;
            }
        }
        dispatchTask();
        return true;
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
            if (taskInstance.getState().typeIsFinished()) {
                logger.info("submit task , but task [{}] state [{}] is already  finished. ", taskInstance.getName(), taskInstance.getState());
                return true;
            }
            // task cannot be submitted because its execution state is RUNNING or DELAY.
            if (taskInstance.getState() == ExecutionStatus.RUNNING_EXECUTION
                    || taskInstance.getState() == ExecutionStatus.DELAY_EXECUTION) {
                logger.info("submit task, but the status of the task {} is already running or delayed.", taskInstance.getName());
                return true;
            }
            logger.info("task ready to submit: taskInstanceId: {}", taskInstance.getId());

            TaskPriority taskPriority = new TaskPriority(processInstance.getProcessInstancePriority().getCode(),
                    processInstance.getId(), taskInstance.getProcessInstancePriority().getCode(),
                    taskInstance.getId(), taskInstance.getTaskGroupPriority(), org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP);

            TaskExecutionContext taskExecutionContext = getTaskExecutionContext(taskInstance);
            if (taskExecutionContext == null) {
                logger.error("task get taskExecutionContext fail: {}", taskInstance);
                return false;
            }

            taskPriority.setTaskExecutionContext(taskExecutionContext);

            taskUpdateQueue.put(taskPriority);
            logger.info("Master submit task to priority queue success, taskInstanceId : {}", taskInstance.getId());
            return true;
        } catch (Exception e) {
            logger.error("submit task error", e);
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
            if (taskInstance.getState().typeIsFinished()) {
                return true;
            }
            if (StringUtils.isBlank(taskInstance.getHost())) {
                taskInstance.setState(ExecutionStatus.KILL);
                taskInstance.setEndTime(new Date());
                processService.updateTaskInstance(taskInstance);
                return true;
            }

            TaskKillRequestCommand killCommand = new TaskKillRequestCommand();
            killCommand.setTaskInstanceId(taskInstance.getId());

            ExecutionContext executionContext = new ExecutionContext(killCommand.convert2Command(), ExecutorType.WORKER);

            Host host = Host.of(taskInstance.getHost());
            executionContext.setHost(host);

            nettyExecutorManager.executeDirectly(executionContext);
        } catch (ExecuteException e) {
            logger.error("kill task error:", e);
            return false;
        }

        logger.info("master kill taskInstance name :{} taskInstance id:{}",
                taskInstance.getName(), taskInstance.getId());
        return true;
    }
}
