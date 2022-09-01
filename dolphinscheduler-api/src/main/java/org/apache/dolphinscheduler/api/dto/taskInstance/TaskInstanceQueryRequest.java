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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * task instance request
 */
@ApiModel("TASK-INSTANCE-QUERY")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskInstanceQueryRequest extends PageQueryDto {

    @ApiModelProperty(name = "processInstanceId", example = "PROCESS_INSTANCE_ID", value = "0")
    Integer processInstanceId;

    @ApiModelProperty(name = "processInstanceName", example = "PROCESS-INSTANCE-NAME")
    String processInstanceName;

    @ApiModelProperty(name = "processDefinitionName", example = "PROCESS-DEFINITION-NAME")
    String processDefinitionName;

    @ApiModelProperty(name = "searchVal", example = "SEARCH-VAL")
    String searchVal;

    @ApiModelProperty(name = "taskName", example = "TASK-NAME")
    String taskName;

    @ApiModelProperty(name = "executorName", example = "EXECUTOR-NAME")
    String executorName;

    @ApiModelProperty(name = "stateType", example = "STATE-TYPE")
    TaskExecutionStatus stateType;

    @ApiModelProperty(name = "host", example = "HOST")
    String host;

    @ApiModelProperty(name = "startDate", example = "START-TIME")
    String startTime;

    @ApiModelProperty(name = "endDate", example = "END-DATE")
    String endTime;

    @ApiModelProperty(name = "taskExecuteType", example = "EXECUTE-TYPE", value = "BATCH")
    TaskExecuteType taskExecuteType;
}
