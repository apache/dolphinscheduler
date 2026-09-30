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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.model.TaskInstanceSummaryDto;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lightweight response VO for task instance list / paging queries.
 *
 * <p>Unlike {@link TaskInstance}, this VO intentionally omits heavy columns
 * that are only needed for task execution or detail views. This allows the
 * corresponding DAO queries to use the optimized {@code listSql} projection
 * instead of the full {@code baseSql}.
 *
 * <p><b>Incompatible API change (documented):</b> The following properties that
 * were previously present in task instance list API responses are no longer
 * returned:
 *
 * <p><b>Heavy DB-backed fields removed from the SQL projection:</b>
 * <ul>
 *   <li>{@code taskParams}</li>
 *   <li>{@code varPool}</li>
 *   <li>{@code logPath}</li>
 * </ul>
 *
 * <p><b>Transient (non-DB) fields removed from the entity that were always
 * {@code null} in list responses:</b>
 * <ul>
 *   <li>{@code processDefinitionName}</li>
 *   <li>{@code taskGroupPriority}</li>
 *   <li>{@code workflowInstance}</li>
 *   <li>{@code workflowDefinition}</li>
 *   <li>{@code taskDefine}</li>
 *   <li>{@code workflowInstancePriority}</li>
 * </ul>
 *
 * <p>Consumers that require {@code taskParams}, {@code varPool}, or
 * {@code logPath} should use the task instance detail query path (e.g.
 * {@code queryById}) instead, which continues to return the full
 * {@link TaskInstance}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TASK_INSTANCE_QUERY_RESPONSE")
public class TaskInstanceSummaryVO {

    @Schema(description = "task instance id")
    private Integer id;

    @Schema(description = "task instance name")
    private String name;

    @Schema(description = "task type")
    private String taskType;

    @Schema(description = "workflow instance id")
    private int workflowInstanceId;

    @Schema(description = "workflow instance name")
    private String workflowInstanceName;

    @Schema(description = "project code")
    private Long projectCode;

    @Schema(description = "task code")
    private long taskCode;

    @Schema(description = "task definition version")
    private int taskDefinitionVersion;

    @Schema(description = "task execution status")
    private TaskExecutionStatus state;

    @Schema(description = "first submit time")
    private Date firstSubmitTime;

    @Schema(description = "submit time")
    private Date submitTime;

    @Schema(description = "start time")
    private Date startTime;

    @Schema(description = "end time")
    private Date endTime;

    @Schema(description = "host")
    private String host;

    @Schema(description = "execute path")
    private String executePath;

    @Schema(description = "alert flag")
    private Flag alertFlag;

    @Schema(description = "retry times")
    private int retryTimes;

    @Schema(description = "pid")
    private int pid;

    @Schema(description = "app link")
    private String appLink;

    @Schema(description = "flag")
    private Flag flag;

    @Schema(description = "max retry times")
    private int maxRetryTimes;

    @Schema(description = "retry interval")
    private int retryInterval;

    @Schema(description = "task instance priority")
    private Priority taskInstancePriority;

    @Schema(description = "worker group")
    private String workerGroup;

    @Schema(description = "environment code")
    private Long environmentCode;

    @Schema(description = "executor id")
    private int executorId;

    @Schema(description = "executor name")
    private String executorName;

    @Schema(description = "delay time")
    private int delayTime;

    @Schema(description = "dry run")
    private int dryRun;

    @Schema(description = "task group id")
    private int taskGroupId;

    @Schema(description = "cpu quota")
    private Integer cpuQuota;

    @Schema(description = "memory max")
    private Integer memoryMax;

    @Schema(description = "task execute type")
    private TaskExecuteType taskExecuteType;

    @Schema(description = "duration string, e.g. 1h 2m 3s")
    private String duration;

    /**
     * Create a {@link TaskInstanceSummaryVO} from a {@link TaskInstanceSummaryDto} DAO DTO.
     */
    public static TaskInstanceSummaryVO fromSummaryDto(TaskInstanceSummaryDto dto) {
        return new TaskInstanceSummaryVO(
                dto.getId(),
                dto.getName(),
                dto.getTaskType(),
                dto.getWorkflowInstanceId(),
                dto.getWorkflowInstanceName(),
                dto.getProjectCode(),
                dto.getTaskCode(),
                dto.getTaskDefinitionVersion(),
                dto.getState(),
                dto.getFirstSubmitTime(),
                dto.getSubmitTime(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getHost(),
                dto.getExecutePath(),
                dto.getAlertFlag(),
                dto.getRetryTimes(),
                dto.getPid(),
                dto.getAppLink(),
                dto.getFlag(),
                dto.getMaxRetryTimes(),
                dto.getRetryInterval(),
                dto.getTaskInstancePriority(),
                dto.getWorkerGroup(),
                dto.getEnvironmentCode(),
                dto.getExecutorId(),
                dto.getExecutorName(),
                dto.getDelayTime(),
                dto.getDryRun(),
                dto.getTaskGroupId(),
                dto.getCpuQuota(),
                dto.getMemoryMax(),
                dto.getTaskExecuteType(),
                dto.getDuration());
    }
}
