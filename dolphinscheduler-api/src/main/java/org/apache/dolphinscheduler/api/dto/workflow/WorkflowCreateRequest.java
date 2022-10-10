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

import static org.apache.dolphinscheduler.common.Constants.VERSION_FIRST;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.Date;

import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * workflow create request
 */
@Data
public class WorkflowCreateRequest {

    @ApiModelProperty(example = "workflow name", required = true)
    private String name;

    @ApiModelProperty(example = "workflow's description")
    private String description;

    @ApiModelProperty(example = "12345", required = true)
    private long projectCode;

    @ApiModelProperty(allowableValues = "ONLINE / OFFLINE", example = "OFFLINE", notes = "default OFFLINE if not provide.")
    private String releaseState;

    @ApiModelProperty(example = "[{\"prop\":\"key\",\"value\":\"value\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]")
    private String globalParams;

    @ApiModelProperty(example = "2")
    private int warningGroupId;

    @ApiModelProperty(example = "60")
    private int timeout;

    @ApiModelProperty(example = "tenant1", required = true)
    private String tenantCode;

    @ApiModelProperty(allowableValues = "PARALLEL / SERIAL_WAIT / SERIAL_DISCARD / SERIAL_PRIORITY", example = "PARALLEL", notes = "default PARALLEL if not provide.")
    private String executionType;

    public ProcessDefinition convert2ProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();

        processDefinition.setName(this.name);
        processDefinition.setDescription(this.description);
        processDefinition.setProjectCode(this.projectCode);
        processDefinition.setGlobalParams(this.globalParams);
        processDefinition.setWarningGroupId(this.warningGroupId);
        processDefinition.setTimeout(this.timeout);
        processDefinition.setTenantCode(this.tenantCode);

        ReleaseState pdReleaseState =
                this.releaseState == null ? ReleaseState.OFFLINE : ReleaseState.valueOf(this.releaseState);
        processDefinition.setReleaseState(pdReleaseState);
        ProcessExecutionTypeEnum processExecutionTypeEnum =
                this.executionType == null ? ProcessExecutionTypeEnum.PARALLEL
                        : ProcessExecutionTypeEnum.valueOf(this.executionType);
        processDefinition.setExecutionType(processExecutionTypeEnum);

        processDefinition.setVersion(VERSION_FIRST);
        Date date = new Date();
        processDefinition.setCreateTime(date);
        processDefinition.setUpdateTime(date);
        return processDefinition;
    }
}
