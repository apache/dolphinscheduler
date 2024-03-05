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

package org.apache.dolphinscheduler.server.master.dag;

import org.apache.dolphinscheduler.server.master.events.TaskOperationEvent;
import org.apache.dolphinscheduler.server.master.events.TaskOperationType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DAGEngine implements IDAGEngine {

    private final IWorkflowExecutionContext workflowExecutionContext;

    public DAGEngine(IWorkflowExecutionContext workflowExecutionContext) {
        this.workflowExecutionContext = workflowExecutionContext;
    }

    @Override
    public void triggerNextTasks(String parentTaskNodeName) {
        workflowExecutionContext.getWorkflowExecutionDAG()
                .getDirectPostNodeNames(parentTaskNodeName)
                .forEach(this::triggerTask);
    }

    @Override
    public void triggerTask(String taskName) {
        IWorkflowExecutionDAG workflowExecutionDAG = workflowExecutionContext.getWorkflowExecutionDAG();
        DAGNode dagNode = workflowExecutionDAG.getDAGNode(taskName);
        if (dagNode == null) {
            log.error("Cannot find the DAGNode for task: {}", taskName);
            return;
        }
        // Use condition check?
        if (!workflowExecutionDAG.isTaskAbleToBeTriggered(taskName)) {
            log.info("The task: {} is not able to be triggered", taskName);
            return;
        }

        if (dagNode.isSkip()) {
            log.info("The task: {} is skipped", taskName);
            triggerNextTasks(taskName);
            return;
        }

        TaskExecutionRunnable taskExecutionRunnable = workflowExecutionDAG.triggerTask(taskName);
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.RUN)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void failoverTask(Integer taskInstanceId) {
        TaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionContext.getWorkflowExecutionDAG().getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.FAILOVER)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void retryTask(Integer taskInstanceId) {
        TaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionContext.getWorkflowExecutionDAG().getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.RETRY)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void pauseAllTask() {
        workflowExecutionContext.getWorkflowExecutionDAG()
                .getActiveTaskExecutionRunnable()
                .stream()
                .map(taskExecutionRunnable -> taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getId())
                .forEach(this::pauseTask);
    }

    @Override
    public void pauseTask(Integer taskInstanceId) {
        TaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionContext.getWorkflowExecutionDAG().getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.PAUSE)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void killAllTask() {
        workflowExecutionContext.getWorkflowExecutionDAG()
                .getActiveTaskExecutionRunnable()
                .stream()
                .map(taskExecutionRunnable -> taskExecutionRunnable.getTaskExecutionContext().getTaskInstance().getId())
                .forEach(this::killTask);
    }

    @Override
    public void killTask(Integer taskInstanceId) {
        TaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionContext.getWorkflowExecutionDAG().getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }

        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.KILL)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

}
