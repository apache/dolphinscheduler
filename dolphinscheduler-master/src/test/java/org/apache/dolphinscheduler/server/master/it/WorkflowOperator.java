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

package org.apache.dolphinscheduler.server.master.it;

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerResponse;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowOperator {

    @Autowired
    private IWorkflowControlClient workflowInstanceController;

    @Autowired
    private SchedulerApi schedulerApi;

    public Integer manualTriggerWorkflow(final WorkflowTriggerDTO workflowTriggerDTO) {
        final WorkflowManualTriggerRequest workflowManualTriggerRequest = WorkflowManualTriggerRequest.builder()
                .userId(workflowTriggerDTO.workflowDefinition.getUserId())
                .workflowDefinitionCode(workflowTriggerDTO.workflowDefinition.getCode())
                .workflowDefinitionVersion(workflowTriggerDTO.workflowDefinition.getVersion())
                .startNodes(workflowTriggerDTO.getRunWorkflowCommandParam().getStartNodes())
                .startParamList(workflowTriggerDTO.getRunWorkflowCommandParam().getCommandParams())
                .build();

        final WorkflowManualTriggerResponse manualTriggerWorkflowResponse =
                workflowInstanceController.manualTriggerWorkflow(workflowManualTriggerRequest);
        Assertions.assertThat(manualTriggerWorkflowResponse.isSuccess()).isTrue();

        return manualTriggerWorkflowResponse.getWorkflowInstanceId();
    }

    public void backfillWorkflow(final WorkflowBackfillDTO workflowBackfillDTO) {
        final ProcessDefinition workflowDefinition = workflowBackfillDTO.getWorkflow();

        final WorkflowBackfillTriggerRequest backfillTriggerRequest = WorkflowBackfillTriggerRequest.builder()
                .userId(workflowDefinition.getUserId())
                .workflowCode(workflowDefinition.getCode())
                .workflowVersion(workflowDefinition.getVersion())
                .startNodes(workflowBackfillDTO.getBackfillWorkflowCommandParam().getStartNodes())
                .startParamList(workflowBackfillDTO.getBackfillWorkflowCommandParam().getCommandParams())
                .backfillTimeList(workflowBackfillDTO.getBackfillWorkflowCommandParam().getBackfillTimeList())
                .build();
        final WorkflowBackfillTriggerResponse backfillTriggerResponse =
                workflowInstanceController.backfillTriggerWorkflow(backfillTriggerRequest);

        Assertions.assertThat(backfillTriggerResponse.isSuccess()).isTrue();
    }

    public void schedulingWorkflow(final WorkflowSchedulingDTO workflowSchedulingDTO) {
        final Project project = workflowSchedulingDTO.getProject();
        final Schedule schedule = workflowSchedulingDTO.getSchedule();
        schedulerApi.insertOrUpdateScheduleTask(project.getId(), schedule);
    }

    public WorkflowInstancePauseResponse pauseWorkflowInstance(Integer workflowInstanceId) {
        final WorkflowInstancePauseRequest workflowInstancePauseRequest =
                new WorkflowInstancePauseRequest(workflowInstanceId);
        return workflowInstanceController.pauseWorkflowInstance(workflowInstancePauseRequest);
    }

    public WorkflowInstanceStopResponse stopWorkflowInstance(Integer workflowInstanceId) {
        final WorkflowInstanceStopRequest workflowInstanceStopRequest =
                new WorkflowInstanceStopRequest(workflowInstanceId);
        return workflowInstanceController.stopWorkflowInstance(workflowInstanceStopRequest);
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class WorkflowTriggerDTO {

        private final ProcessDefinition workflowDefinition;

        private final RunWorkflowCommandParam runWorkflowCommandParam;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class WorkflowSchedulingDTO {

        private ProcessDefinition workflow;
        private Project project;
        private Schedule schedule;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class WorkflowBackfillDTO {

        private ProcessDefinition workflow;
        private BackfillWorkflowCommandParam backfillWorkflowCommandParam;
    }

}
