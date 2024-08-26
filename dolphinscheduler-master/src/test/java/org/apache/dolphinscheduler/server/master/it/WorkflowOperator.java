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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.extract.master.IWorkflowInstanceController;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstancePauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstancePauseResponse;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.scheduler.api.SchedulerApi;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowOperator {

    @Autowired
    private CommandDao commandDao;

    @Autowired
    private IWorkflowInstanceController workflowInstanceController;

    @Autowired
    private SchedulerApi schedulerApi;

    public void backfillWorkflow(final WorkflowBackfillDTO workflowBackfillDTO) {
        final ProcessDefinition workflowDefinition = workflowBackfillDTO.getWorkflow();
        final Command command = Command.builder()
                .commandType(CommandType.COMPLEMENT_DATA)
                .processDefinitionCode(workflowDefinition.getCode())
                .processDefinitionVersion(workflowDefinition.getVersion())
                .executorId(workflowDefinition.getUserId())
                .scheduleTime(DateUtils.stringToDate(
                        workflowBackfillDTO.getBackfillWorkflowCommandParam().getBackfillTimeList().get(0)))
                .commandParam(JSONUtils.toJsonString(workflowBackfillDTO.getBackfillWorkflowCommandParam()))
                .startTime(new Date())
                .updateTime(new Date())
                .build();
        commandDao.insert(command);
    }

    public void schedulingWorkflow(final WorkflowSchedulingDTO workflowSchedulingDTO) {
        final Project project = workflowSchedulingDTO.getProject();
        final Schedule schedule = workflowSchedulingDTO.getSchedule();
        schedulerApi.insertOrUpdateScheduleTask(project.getId(), schedule);
    }

    public void triggerWorkflow(final WorkflowTriggerDTO workflowTriggerDTO) {
        final ProcessDefinition workflowDefinition = workflowTriggerDTO.getWorkflowDefinition();
        final RunWorkflowCommandParam runWorkflowCommandParam = workflowTriggerDTO.getRunWorkflowCommandParam();
        final Command command = Command.builder()
                .commandType(CommandType.START_PROCESS)
                .processDefinitionCode(workflowDefinition.getCode())
                .processDefinitionVersion(workflowDefinition.getVersion())
                .executorId(workflowDefinition.getUserId())
                .commandParam(JSONUtils.toJsonString(runWorkflowCommandParam))
                .startTime(new Date())
                .updateTime(new Date())
                .build();
        commandDao.insert(command);
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
