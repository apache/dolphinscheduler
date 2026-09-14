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

package org.apache.dolphinscheduler.server.master.integration.cases;

import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupQueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for workflow start task group scenarios.
 */
public class WorkflowStartTaskGroupTestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private TaskGroupDao taskGroupDao;

    @Autowired
    private TaskGroupQueueDao taskGroupQueueDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Test
    @DisplayName("Test start a workflow with two fake task(A) using task group")
    public void testStartWorkflow_with_successTaskUsingTaskGroup() {
        final String yaml = "/it/start/workflow_with_fake_tasks_using_task_group.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();

        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(2))
                .atLeast(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(2)
                            .allMatch(taskInstance -> TaskExecutionStatus.SUCCESS.equals(taskInstance.getState()) &&
                                    taskInstance.getTaskGroupId() == context.getTaskGroups().get(0).getId());

                    final TaskInstance taskA = taskInstances.stream()
                            .filter(t -> "A".equals(t.getName()))
                            .findFirst().get();
                    final TaskInstance taskB = taskInstances.stream()
                            .filter(t -> "B".equals(t.getName()))
                            .findFirst().get();
                    // TaskA's task group priority is smaller than B
                    Assertions.assertThat(taskA.getStartTime()).isAfter(taskB.getStartTime());
                    Assertions.assertThat(taskA.getEndTime()).isAfter(taskB.getEndTime());

                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test pause a workflow while tasks are waiting for a task group slot")
    public void testPauseWorkflow_withTasksWaitingForTaskGroupSlot() {
        final Integer workflowInstanceId = triggerWorkflowWithTasksWaitingForTaskGroupSlot();

        Assertions.assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess()).isTrue();

        assertWorkflowAndTasksFinished(workflowInstanceId, WorkflowExecutionStatus.PAUSE, TaskExecutionStatus.PAUSE);
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test stop a workflow while tasks are waiting for a task group slot")
    public void testStopWorkflow_withTasksWaitingForTaskGroupSlot() {
        final Integer workflowInstanceId = triggerWorkflowWithTasksWaitingForTaskGroupSlot();

        Assertions.assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId).isSuccess()).isTrue();

        assertWorkflowAndTasksFinished(workflowInstanceId, WorkflowExecutionStatus.STOP, TaskExecutionStatus.KILL);
        masterContainer.assertAllResourceReleased();
    }

    private Integer triggerWorkflowWithTasksWaitingForTaskGroupSlot() {
        final String yaml = "/it/start/workflow_with_fake_tasks_using_task_group.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();
        final TaskGroup taskGroup = context.getTaskGroups().get(0);
        occupyTaskGroupSlot(taskGroup);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions.assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                    Assertions.assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(2)
                            .allSatisfy(taskInstance -> {
                                Assertions.assertThat(taskInstance.getState())
                                        .isEqualTo(TaskExecutionStatus.SUBMITTED_SUCCESS);
                                Assertions.assertThat(taskInstance.getTaskGroupId()).isEqualTo(taskGroup.getId());
                                Assertions.assertThat(taskGroupQueueDao.queryByTaskInstanceId(taskInstance.getId()))
                                        .singleElement()
                                        .extracting(TaskGroupQueue::getStatus)
                                        .isEqualTo(TaskGroupQueueStatus.WAIT_QUEUE);
                            });
                });
        return workflowInstanceId;
    }

    private void assertWorkflowAndTasksFinished(final Integer workflowInstanceId,
                                                final WorkflowExecutionStatus workflowExecutionStatus,
                                                final TaskExecutionStatus taskExecutionStatus) {
        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions.assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(workflowExecutionStatus);
                    Assertions.assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(2)
                            .allSatisfy(taskInstance -> {
                                Assertions.assertThat(taskInstance.getState()).isEqualTo(taskExecutionStatus);
                                Assertions.assertThat(taskGroupQueueDao.queryByTaskInstanceId(taskInstance.getId()))
                                        .isEmpty();
                            });
                });
    }

    private void occupyTaskGroupSlot(final TaskGroup taskGroup) {
        final Date now = new Date();
        final TaskInstance slotHolder = TaskInstance.builder()
                .name("task-group-slot-holder")
                .taskType("LogicFakeTask")
                .workflowInstanceId(0)
                .workflowInstanceName("task-group-slot-holder")
                .taskCode(Long.MAX_VALUE)
                .taskDefinitionVersion(1)
                .state(TaskExecutionStatus.RUNNING_EXECUTION)
                .flag(Flag.YES)
                .submitTime(now)
                .firstSubmitTime(now)
                .startTime(now)
                .taskGroupId(taskGroup.getId())
                .build();
        Assertions.assertThat(taskInstanceDao.insert(slotHolder)).isEqualTo(1);

        final TaskGroupQueue slotHolderQueue = TaskGroupQueue.builder()
                .taskId(slotHolder.getId())
                .taskName(slotHolder.getName())
                .groupId(taskGroup.getId())
                .priority(0)
                .forceStart(Flag.NO.getCode())
                .inQueue(Flag.YES.getCode())
                .status(TaskGroupQueueStatus.ACQUIRE_SUCCESS)
                .createTime(now)
                .updateTime(now)
                .build();
        Assertions.assertThat(taskGroupQueueDao.insert(slotHolderQueue)).isEqualTo(1);

        taskGroup.setUseSize(taskGroup.getGroupSize());
        Assertions.assertThat(taskGroupDao.updateById(taskGroup)).isTrue();
    }

}
