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

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;

import java.util.Date;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task relation create request
 */
@Data
public class TaskRelationCreateRequest {

    @Schema(example = "12345678")
    private long projectCode;

    @Schema(example = "87654321", required = true)
    private long workflowCode;

    @Schema(example = "12345", required = true)
    private long preTaskCode;

    @Schema(example = "54321", required = true)
    private long postTaskCode;

    public ProcessTaskRelation convert2ProcessTaskRelation() {
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();

        processTaskRelation.setProjectCode(this.projectCode);
        processTaskRelation.setProcessDefinitionCode(this.workflowCode);
        processTaskRelation.setPreTaskCode(this.preTaskCode);
        processTaskRelation.setPostTaskCode(this.postTaskCode);

        Date date = new Date();
        processTaskRelation.setCreateTime(date);
        processTaskRelation.setUpdateTime(date);
        return processTaskRelation;
    }
}
