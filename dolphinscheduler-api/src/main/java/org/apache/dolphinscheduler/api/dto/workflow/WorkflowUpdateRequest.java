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

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

/**
 * workflow update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WorkflowUpdateRequest {

    @ApiModelProperty(example = "workflow's name")
    private String name;

    @ApiModelProperty(example = "workflow's description")
    private String description;

    @ApiModelProperty(allowableValues = "ONLINE / OFFLINE", example = "OFFLINE")
    private String releaseState;

    @ApiModelProperty(example = "[{\"prop\":\"key\",\"value\":\"value\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]")
    private String globalParams;

    @ApiModelProperty(example = "2")
    private int warningGroupId;

    @ApiModelProperty(example = "60")
    private int timeout;

    @ApiModelProperty(example = "tenantCode1")
    private String tenantCode;

    @ApiModelProperty(allowableValues = "PARALLEL / SERIAL_WAIT / SERIAL_DISCARD / SERIAL_PRIORITY", example = "PARALLEL", notes = "default PARALLEL if not provide.")
    private String executionType;

    @ApiModelProperty(example = "[{\\\"taskCode\\\":7009653961024,\\\"x\\\":312,\\\"y\\\":196}]")
    private String location;

    /**
     * Merge workflowUpdateRequest information into exists processDefinition object
     *
     * @param processDefinition exists processDefinition object
     * @return process definition
     */
    public ProcessDefinition mergeIntoProcessDefinition(ProcessDefinition processDefinition) {
        ProcessDefinition processDefinitionDeepCopy =
                JSONUtils.parseObject(JSONUtils.toJsonString(processDefinition), ProcessDefinition.class);
        assert processDefinitionDeepCopy != null;
        if (this.name != null) {
            processDefinitionDeepCopy.setName(this.name);
        }
        if (this.description != null) {
            processDefinitionDeepCopy.setDescription(this.description);
        }
        if (this.releaseState != null) {
            processDefinitionDeepCopy.setReleaseState(ReleaseState.valueOf(this.releaseState));
        }
        if (this.globalParams != null) {
            processDefinitionDeepCopy.setGlobalParams(this.globalParams);
        }
        if (this.warningGroupId != 0) {
            processDefinitionDeepCopy.setWarningGroupId(this.warningGroupId);
        }
        if (this.timeout != 0) {
            processDefinitionDeepCopy.setTimeout(this.timeout);
        }
        if (this.tenantCode != null) {
            processDefinitionDeepCopy.setTenantCode(this.tenantCode);
        }
        if (this.executionType != null) {
            processDefinitionDeepCopy.setExecutionType(ProcessExecutionTypeEnum.valueOf(this.executionType));
        }
        if (this.location != null) {
            processDefinitionDeepCopy.setLocations(this.location);
        }

        int version = processDefinitionDeepCopy.getVersion() + 1;
        processDefinitionDeepCopy.setVersion(version);
        processDefinitionDeepCopy.setUpdateTime(new Date());
        return processDefinitionDeepCopy;
    }
}
