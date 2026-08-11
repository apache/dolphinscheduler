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

package org.apache.dolphinscheduler.api.dto.workflowInstance;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lightweight response DTO for workflow instance list / top-N / trigger queries.
 *
 * <p>Unlike {@link WorkflowInstance}, this DTO intentionally omits heavy columns
 * that are only needed for detail views or internal processing. This allows the
 * corresponding DAO queries to use the optimized {@code listSql} projection
 * instead of the full {@code baseSql}.
 *
 * <p><b>Incompatible API change (documented):</b> The following properties that
 * were previously present in list/topN/trigger API responses are no longer
 * returned:
 *
 * <p><b>Heavy DB-backed fields removed from the SQL projection:</b>
 * <ul>
 *   <li>{@code commandParam}</li>
 *   <li>{@code globalParams}</li>
 *   <li>{@code historyCmd}</li>
 *   <li>{@code varPool}</li>
 *   <li>{@code stateHistory}</li>
 * </ul>
 *
 * <p><b>Transient (non-DB) fields removed from the entity that were always
 * {@code null} in list responses:</b>
 * <ul>
 *   <li>{@code stateDescList}</li>
 *   <li>{@code workflowDefinition} (deprecated)</li>
 *   <li>{@code dagData}</li>
 *   <li>{@code queue}</li>
 *   <li>{@code locations}</li>
 *   <li>{@code dependenceScheduleTimes}</li>
 * </ul>
 *
 * <p><b>Derived getter properties removed:</b>
 * <ul>
 *   <li>{@code cmdTypeIfComplement} — previously returned
 *       {@link CommandType#COMPLEMENT_DATA} for complement-data executions;
 *       consumers should inspect {@code commandType} on the detail endpoint
 *       instead</li>
 *   <li>{@code complementData} — previously returned {@code true} for
 *       complement-data executions; no longer available in list responses</li>
 * </ul>
 *
 * <p>Consumers that require any of these fields should call the detail endpoint
 * {@code GET /projects/{projectCode}/workflow-instances/{id}} instead, which
 * continues to return the full {@link WorkflowInstance}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WORKFLOW_INSTANCE_QUERY_RESPONSE")
public class WorkflowInstanceQueryDTO {

    @Schema(description = "workflow instance id")
    private Integer id;

    @Schema(description = "workflow definition code")
    private Long workflowDefinitionCode;

    @Schema(description = "workflow definition version")
    private int workflowDefinitionVersion;

    @Schema(description = "project code")
    private Long projectCode;

    @Schema(description = "workflow execution status")
    private WorkflowExecutionStatus state;

    @Schema(description = "recovery flag")
    private Flag recovery;

    @Schema(description = "start time")
    private Date startTime;

    @Schema(description = "end time")
    private Date endTime;

    @Schema(description = "run times")
    private int runTimes;

    @Schema(description = "workflow instance name")
    private String name;

    @Schema(description = "host")
    private String host;

    @Schema(description = "command type")
    private CommandType commandType;

    @Schema(description = "task depend type")
    private TaskDependType taskDependType;

    @Schema(description = "max try times")
    private int maxTryTimes;

    @Schema(description = "failure strategy")
    private FailureStrategy failureStrategy;

    @Schema(description = "warning type")
    private WarningType warningType;

    @Schema(description = "warning group id")
    private Integer warningGroupId;

    @Schema(description = "schedule time")
    private Date scheduleTime;

    @Schema(description = "command start time")
    private Date commandStartTime;

    @Schema(description = "is sub workflow")
    private Flag isSubWorkflow;

    @Schema(description = "executor id")
    private int executorId;

    @Schema(description = "executor name")
    private String executorName;

    @Schema(description = "workflow instance priority")
    private Priority workflowInstancePriority;

    @Schema(description = "worker group")
    private String workerGroup;

    @Schema(description = "environment code")
    private Long environmentCode;

    @Schema(description = "timeout")
    private int timeout;

    @Schema(description = "tenant code")
    private String tenantCode;

    @Schema(description = "dry run")
    private int dryRun;

    @Schema(description = "next workflow instance id")
    private int nextWorkflowInstanceId;

    @Schema(description = "restart time")
    private Date restartTime;

    @Schema(description = "duration string, e.g. 1h 2m 3s")
    private String duration;

    /**
     * Create a {@link WorkflowInstanceQueryDTO} from a {@link WorkflowInstance} entity.
     */
    public static WorkflowInstanceQueryDTO fromEntity(WorkflowInstance instance) {
        return new WorkflowInstanceQueryDTO(
                instance.getId(),
                instance.getWorkflowDefinitionCode(),
                instance.getWorkflowDefinitionVersion(),
                instance.getProjectCode(),
                instance.getState(),
                instance.getRecovery(),
                instance.getStartTime(),
                instance.getEndTime(),
                instance.getRunTimes(),
                instance.getName(),
                instance.getHost(),
                instance.getCommandType(),
                instance.getTaskDependType(),
                instance.getMaxTryTimes(),
                instance.getFailureStrategy(),
                instance.getWarningType(),
                instance.getWarningGroupId(),
                instance.getScheduleTime(),
                instance.getCommandStartTime(),
                instance.getIsSubWorkflow(),
                instance.getExecutorId(),
                instance.getExecutorName(),
                instance.getWorkflowInstancePriority(),
                instance.getWorkerGroup(),
                instance.getEnvironmentCode(),
                instance.getTimeout(),
                instance.getTenantCode(),
                instance.getDryRun(),
                instance.getNextWorkflowInstanceId(),
                instance.getRestartTime(),
                instance.getDuration());
    }
}
