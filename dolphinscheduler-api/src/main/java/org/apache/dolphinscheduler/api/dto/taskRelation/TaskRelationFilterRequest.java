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

package org.apache.dolphinscheduler.api.dto.taskRelation;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task relation query request
 */
@Schema(name = "TASK-RELATION-QUERY")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskRelationFilterRequest extends PageQueryDto {

    @Schema(example = "1234567890123")
    private long workflowCode;

    @Schema(example = "1234567890123")
    private long preTaskCode;

    @Schema(example = "1234567890123")
    private long postTaskCode;

    public TaskRelationFilterRequest(long workflowCode, long preTaskCode, long postTaskCode) {
        this.workflowCode = workflowCode;
        this.preTaskCode = preTaskCode;
        this.postTaskCode = postTaskCode;
    }

    public TaskRelationFilterRequest(long preTaskCode, long postTaskCode) {
        this.preTaskCode = preTaskCode;
        this.postTaskCode = postTaskCode;
    }

    public ProcessTaskRelation convert2TaskDefinition() {
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        if (this.workflowCode != 0L) {
            processTaskRelation.setProcessDefinitionCode(this.workflowCode);
        }
        if (this.preTaskCode != 0L) {
            processTaskRelation.setPreTaskCode(this.preTaskCode);
        }
        if (this.postTaskCode != 0L) {
            processTaskRelation.setPostTaskCode(this.postTaskCode);
        }
        return processTaskRelation;
    }
}
