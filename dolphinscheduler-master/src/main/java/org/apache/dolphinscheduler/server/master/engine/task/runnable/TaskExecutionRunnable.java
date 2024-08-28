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

package org.apache.dolphinscheduler.server.master.engine.task.runnable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;

@Slf4j
public class TaskExecutionRunnable implements ITaskExecutionRunnable {

    private final ApplicationContext applicationContext;

    @Getter
    private final IWorkflowExecutionGraph workflowExecutionGraph;
    @Getter
    private final WorkflowEventBus workflowEventBus;
    @Getter
    private final WorkflowDefinition workflowDefinition;
    @Getter
    private final WorkflowInstance workflowInstance;
    @Getter
    private TaskInstance taskInstance;
    @Getter
    private final TaskDefinition taskDefinition;
    @Getter
    private TaskExecutionContext taskExecutionContext;

    public TaskExecutionRunnable(TaskExecutionRunnableBuilder taskExecutionRunnableBuilder) {
        this.applicationContext = taskExecutionRunnableBuilder.getApplicationContext();
        this.workflowExecutionGraph = checkNotNull(taskExecutionRunnableBuilder.getWorkflowExecutionGraph());
        this.workflowEventBus = checkNotNull(taskExecutionRunnableBuilder.getWorkflowEventBus());
        this.workflowDefinition = checkNotNull(taskExecutionRunnableBuilder.getWorkflowDefinition());
        this.workflowInstance = checkNotNull(taskExecutionRunnableBuilder.getWorkflowInstance());
        this.taskDefinition = checkNotNull(taskExecutionRunnableBuilder.getTaskDefinition());
        this.taskInstance = taskExecutionRunnableBuilder.getTaskInstance();
        if (taskInstance != null) {
            initializeTaskExecutionContext();
        }
    }

    @Override
    public String getName() {
        return taskDefinition.getName();
    }

    @Override
    public boolean isTaskInstanceInitialized() {
        return taskInstance != null;
    }

    @Override
    public void initializeTaskInstance() {
        checkState(taskInstance == null, "The task instance is not null, should not initialize again.");
        this.taskInstance = applicationContext.getBean(TaskInstanceFactories.class)
                .firstRunTaskInstanceFactory()
                .builder()
                .withTaskDefinition(taskDefinition)
                .withWorkflowInstance(workflowInstance)
                .build();
        initializeTaskExecutionContext();
    }

    @Override
    public boolean isTaskInstanceNeedRetry() {
        return taskInstance.getRetryTimes() < taskInstance.getMaxRetryTimes();
    }

    @Override
    public void initializeRetryTaskInstance() {
        checkState(taskInstance != null, "The task instance can't retry, should not initialize retry task instance.");
        this.taskInstance = applicationContext.getBean(TaskInstanceFactories.class)
                .retryTaskInstanceFactory()
                .builder()
                .withTaskInstance(taskInstance)
                .build();
        initializeTaskExecutionContext();
    }

    @Override
    public void initializeFailoverTaskInstance() {
        checkState(taskInstance != null,
                "The task instance can't failover, should not initialize failover task instance.");
        this.taskInstance = applicationContext.getBean(TaskInstanceFactories.class)
                .failoverTaskInstanceFactory()
                .builder()
                .withTaskInstance(taskInstance)
                .build();
        initializeTaskExecutionContext();
    }

    private void initializeTaskExecutionContext() {
        checkState(taskInstance != null, "The task instance is null, can't initialize TaskExecutionContext.");
        final TaskExecutionContextCreateRequest request = TaskExecutionContextCreateRequest.builder()
                .workflowDefinition(workflowDefinition)
                .workflowInstance(workflowInstance)
                .taskDefinition(taskDefinition)
                .taskInstance(taskInstance)
                .build();
        this.taskExecutionContext = applicationContext.getBean(TaskExecutionContextFactory.class)
                .createTaskExecutionContext(request);
    }

    @Override
    public int compareTo(ITaskExecutionRunnable other) {
        if (other == null) {
            return 1;
        }
        int workflowInstancePriorityCompareResult = workflowInstance.getProcessInstancePriority().getCode() -
                other.getWorkflowInstance().getProcessInstancePriority().getCode();
        if (workflowInstancePriorityCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
        }

        // smaller number, higher priority
        int taskInstancePriorityCompareResult = taskInstance.getTaskInstancePriority().getCode()
                - other.getTaskInstance().getTaskInstancePriority().getCode();
        if (taskInstancePriorityCompareResult != 0) {
            return taskInstancePriorityCompareResult;
        }

        // larger number, higher priority
        int taskGroupPriorityCompareResult =
                taskInstance.getTaskGroupPriority() - other.getTaskInstance().getTaskGroupPriority();
        if (taskGroupPriorityCompareResult != 0) {
            return -taskGroupPriorityCompareResult;
        }
        // earlier submit time, higher priority
        return taskInstance.getFirstSubmitTime().compareTo(other.getTaskInstance().getFirstSubmitTime());
    }

    @Override
    public String toString() {
        return "TaskExecutionRunnable{" + "name=" + getName() + '}';
    }
}
