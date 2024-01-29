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

package org.apache.dolphinscheduler.api.dto.project;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsStateRequest {

    @Schema(name = "isAll", example = "true")
    boolean isAll;

    @Schema(name = "projectName", example = "PROJECT-NAME")
    String projectName;

    @Schema(name = "projectCode", example = "1234567890")
    Long projectCode;

    @Schema(name = "workflowName", example = "WORKFLOW-NAME")
    String workflowName;

    @Schema(name = "workflowCode", example = "1234567890")
    Long workflowCode;

    @Schema(name = "taskName", example = "TASK-NAME")
    String taskName;

    @Schema(name = "taskCode", example = "1234567890")
    Long taskCode;

    @Schema(name = "startDate", example = "2022-01-01 10:01:02")
    Date startTime;

    @Schema(name = "endDate", example = "2022-01-02 10:01:02")
    Date endTime;

}
