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

package org.apache.dolphinscheduler.api.dto.taskInstance;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task instance request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskInstanceQueryRequest extends PageQueryDto {

    @Schema(name = "processInstanceId", example = "PROCESS_INSTANCE_ID", defaultValue = "0")
    Integer processInstanceId;

    @Schema(name = "processInstanceName", example = "PROCESS-INSTANCE-NAME")
    String processInstanceName;

    @Schema(name = "processDefinitionName", example = "PROCESS-DEFINITION-NAME")
    String processDefinitionName;

    @Schema(name = "searchVal", example = "SEARCH-VAL")
    String searchVal;

    @Schema(name = "taskName", example = "TASK-NAME")
    String taskName;

    @Schema(name = "executorName", example = "EXECUTOR-NAME")
    String executorName;

    @Schema(name = "stateType", example = "STATE-TYPE")
    TaskExecutionStatus stateType;

    @Schema(name = "host", example = "HOST")
    String host;

    @Schema(name = "startDate", example = "START-TIME")
    String startTime;

    @Schema(name = "endDate", example = "END-DATE")
    String endTime;

    @Schema(name = "taskExecuteType", example = "EXECUTE-TYPE", defaultValue = "BATCH")
    TaskExecuteType taskExecuteType;
}
