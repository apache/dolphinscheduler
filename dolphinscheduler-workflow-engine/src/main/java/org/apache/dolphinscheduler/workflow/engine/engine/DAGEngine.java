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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.workflow.engine.dag.DAGNode;
import org.apache.dolphinscheduler.workflow.engine.event.TaskOperationEvent;
import org.apache.dolphinscheduler.workflow.engine.event.TaskOperationType;
import org.apache.dolphinscheduler.workflow.engine.workflow.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.workflow.engine.workflow.ITaskExecutionRunnableFactory;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionDAG;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DAGEngine implements IDAGEngine {

    private final IWorkflowExecutionContext workflowExecutionContext;

    private final ITaskExecutionRunnableFactory taskExecutionRunnableFactory;

    public DAGEngine(IWorkflowExecutionContext workflowExecutionContext,
                     ITaskExecutionRunnableFactory taskExecutionRunnableFactory) {
        this.workflowExecutionContext = workflowExecutionContext;
        this.taskExecutionRunnableFactory = taskExecutionRunnableFactory;
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

        // todo: Use condition check?
        // How to make sure the
        if (!workflowExecutionDAG.isTaskAbleToBeTriggered(taskName)) {
            log.info("The task: {} is not able to be triggered", taskName);
            return;
        }

        if (dagNode.isSkip()) {
            log.info("The task: {} is skipped", taskName);
            triggerNextTasks(taskName);
            return;
        }

        ITaskExecutionRunnable taskExecutionRunnable =
                taskExecutionRunnableFactory.createTaskExecutionRunnable(taskName, workflowExecutionContext);
        workflowExecutionDAG.storeTaskExecutionRunnable(taskExecutionRunnable);
        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.RUN)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void failoverTask(Integer taskInstanceId) {
        IWorkflowExecutionDAG workflowExecutionDAG = workflowExecutionContext.getWorkflowExecutionDAG();
        ITaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionDAG.getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }
        ITaskExecutionRunnable failoverTaskExecutionRunnable = taskExecutionRunnableFactory
                .createFailoverTaskExecutionRunnable(taskExecutionRunnable, workflowExecutionContext);
        workflowExecutionDAG.storeTaskExecutionRunnable(failoverTaskExecutionRunnable);

        TaskOperationEvent taskOperationEvent = TaskOperationEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .taskOperationType(TaskOperationType.FAILOVER)
                .build();
        workflowExecutionContext.getEventRepository().storeEventToTail(taskOperationEvent);
    }

    @Override
    public void retryTask(Integer taskInstanceId) {
        IWorkflowExecutionDAG workflowExecutionDAG = workflowExecutionContext.getWorkflowExecutionDAG();
        ITaskExecutionRunnable taskExecutionRunnable =
                workflowExecutionDAG.getTaskExecutionRunnableById(taskInstanceId);
        if (taskExecutionRunnable == null) {
            log.error("Cannot find the ITaskExecutionRunnable for taskInstance: {}", taskInstanceId);
            return;
        }
        ITaskExecutionRunnable retryTaskExecutionRunnable = taskExecutionRunnableFactory
                .createRetryTaskExecutionRunnable(taskExecutionRunnable, workflowExecutionContext);
        workflowExecutionDAG.storeTaskExecutionRunnable(retryTaskExecutionRunnable);

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
        ITaskExecutionRunnable taskExecutionRunnable =
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
        ITaskExecutionRunnable taskExecutionRunnable =
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
