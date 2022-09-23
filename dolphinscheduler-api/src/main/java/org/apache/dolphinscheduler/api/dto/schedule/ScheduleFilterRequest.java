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

package org.apache.dolphinscheduler.api.dto.schedule;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * schedule query request
 */
@ApiModel("SCHEDULE-QUERY")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleFilterRequest extends PageQueryDto {

    @ApiModelProperty(example = "project-name")
    private String projectName;

    @ApiModelProperty(example = "process-definition-name")
    private String processDefinitionName;

    @ApiModelProperty(allowableValues = "ONLINE / OFFLINE", example = "OFFLINE", notes = "default OFFLINE if value not provide.")
    private String releaseState;

    public Schedule convert2Schedule() {
        Schedule schedule = new Schedule();
        if (this.projectName != null) {
            schedule.setProjectName(this.projectName);
        }
        if (this.processDefinitionName != null) {
            schedule.setProcessDefinitionName(this.processDefinitionName);
        }
        if (this.releaseState != null) {
            schedule.setReleaseState(ReleaseState.valueOf(this.releaseState));
        }
        return schedule;
    }
}
