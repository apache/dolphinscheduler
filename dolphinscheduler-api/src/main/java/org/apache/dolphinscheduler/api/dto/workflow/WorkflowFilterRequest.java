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

package org.apache.dolphinscheduler.api.dto.workflow;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * workflow query response
 */
@Schema(name = "WORKFLOW-QUERY")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WorkflowFilterRequest extends PageQueryDto {

    @Schema(example = "project-name")
    private String projectName;

    @Schema(example = "workflow-name")
    private String workflowName;

    @Schema(example = "ONLINE / OFFLINE")
    private String releaseState;

    @Schema(example = "ONLINE / OFFLINE")
    private String scheduleReleaseState;

    public ProcessDefinition convert2ProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        if (this.workflowName != null) {
            processDefinition.setName(this.workflowName);
        }
        if (this.releaseState != null) {
            processDefinition.setReleaseState(ReleaseState.valueOf(this.releaseState));
        }
        return processDefinition;
    }
}
