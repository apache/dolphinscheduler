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

package org.apache.dolphinscheduler.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;

@Builder
@Getter
public class CreateWorkflowDefinitionRequest {

    @ApiModelProperty(name = "name", example = "WORKFLOW_DEFINITION_NAME", value = "WORKFLOW_DEFINITION_NAME", required = true)
    String name;

    @ApiModelProperty(name = "description", example = "WORKFLOW_DEFINITION_DESC", value = "WORKFLOW_DEFINITION_DESC", required = false)
    String description;

    @ApiModelProperty(name = "globalParams", value = "", required = false)
    String globalParams;

    @ApiModelProperty(name = "locations", example = "WORKFLOW_DEFINITION_LOCATIONS", value = "WORKFLOW_DEFINITION_LOCATIONS", required = false)
    String locations;

    @ApiModelProperty(name = "timeout", value = "", required = false)
    int timeout;

    @ApiModelProperty(name = "tenantCode", required = true)
    String tenantCode;

    @ApiModelProperty(name = "taskRelationJson", required = true)
    String taskRelationJson;

    @ApiModelProperty(name = "taskDefinitionJson", required = true)
    String taskDefinitionJson;

    @ApiModelProperty(name = "otherParamsJson", required = false)
    String otherParamsJson;

    @ApiModelProperty(name = "executionType", example = "PARALLEL", value = "PARALLEL", required = true)
    ProcessExecutionTypeEnum executionType;

}
