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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.Date;
import java.util.TimeZone;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The integration test for scheduling a workflow from workflow definition.
 */
public class WorkflowSchedulingTestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Test
    @DisplayName("Test scheduling a workflow with one fake task(A) success")
    public void testSchedulingWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/scheduling/workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final Schedule schedule = Schedule.builder()
                .workflowDefinitionCode(workflow.getCode())
                .startTime(new Date())
                .endTime(DateUtils.addDays(new Date(), 1))
                .timezoneId(TimeZone.getDefault().getID())
                .crontab("0/5 * * * * ?")
                .failureStrategy(FailureStrategy.CONTINUE)
                .warningType(WarningType.NONE)
                .createTime(new Date())
                .updateTime(new Date())
                .userId(1)
                .releaseState(ReleaseState.ONLINE)
                .workflowInstancePriority(Priority.MEDIUM)
                .build();

        scheduleMapper.insert(schedule);

        WorkflowOperator.WorkflowSchedulingDTO workflowSchedulingDTO = WorkflowOperator.WorkflowSchedulingDTO.builder()
                .project(context.getProject())
                .workflow(context.getWorkflows().get(0))
                .schedule(schedule)
                .build();

        workflowOperator.schedulingWorkflow(workflowSchedulingDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .areAtLeast(3,
                                    new Condition<>(instance -> instance.getState() == WorkflowExecutionStatus.SUCCESS,
                                            "Workflow instance should be success"));
                });

        workflowOperator.unSchedulingWorkflow(workflowSchedulingDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryTaskInstance(workflow))
                            .areAtLeast(3,
                                    new Condition<>(
                                            taskInstance -> taskInstance.getState() == TaskExecutionStatus.SUCCESS,
                                            "Task instance should be A"));
                });

        masterContainer.assertAllResourceReleased();
    }

}
