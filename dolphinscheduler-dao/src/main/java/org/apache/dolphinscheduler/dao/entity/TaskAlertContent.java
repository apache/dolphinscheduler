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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaskAlertContent implements Serializable {

    @JsonProperty("taskInstanceId")
    private int taskInstanceId;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("taskType")
    private String taskType;
    @JsonProperty("processDefinitionId")
    private int processDefinitionId;
    @JsonProperty("processDefinitionName")
    private String processDefinitionName;
    @JsonProperty("processInstanceId")
    private int processInstanceId;
    @JsonProperty("processInstanceName")
    private String processInstanceName;
    @JsonProperty("state")
    private TaskExecutionStatus state;
    @JsonProperty("startTime")
    private Date startTime;
    @JsonProperty("endTime")
    private Date endTime;
    @JsonProperty("host")
    private String host;
    @JsonProperty("logPath")
    private String logPath;

}
