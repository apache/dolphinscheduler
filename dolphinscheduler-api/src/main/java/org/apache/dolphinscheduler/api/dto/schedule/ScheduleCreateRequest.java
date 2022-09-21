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

import static org.apache.dolphinscheduler.common.utils.DateUtils.stringToDate;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.util.Date;

import lombok.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.ApiModelProperty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * schedule create request
 */
@Data
public class ScheduleCreateRequest {

    @ApiModelProperty(example = "1234567890123", required = true)
    private long processDefinitionCode;

    @ApiModelProperty(example = "schedule timezone", required = true)
    private String crontab;

    @ApiModelProperty(example = "2021-01-01 10:00:00", required = true)
    private String startTime;

    @ApiModelProperty(example = "2022-01-01 12:00:00", required = true)
    private String endTime;

    @ApiModelProperty(example = "Asia/Shanghai", required = true)
    private String timezoneId;

    @ApiModelProperty(allowableValues = "CONTINUE / END", example = "CONTINUE", notes = "default CONTINUE if value not provide.")
    private String failureStrategy;

    @ApiModelProperty(allowableValues = "ONLINE / OFFLINE", example = "OFFLINE", notes = "default OFFLINE if value not provide.")
    private String releaseState;

    @ApiModelProperty(allowableValues = "NONE / SUCCESS / FAILURE / ALL", example = "SUCCESS", notes = "default NONE if value not provide.")
    private String warningType;

    @ApiModelProperty(example = "2", notes = "default 0 if value not provide.")
    private int warningGroupId;

    @ApiModelProperty(allowableValues = "HIGHEST / HIGH / MEDIUM / LOW / LOWEST", example = "MEDIUM", notes = "default MEDIUM if value not provide.")
    private String processInstancePriority;

    @ApiModelProperty(example = "worker-group-name")
    private String workerGroup;

    @ApiModelProperty(example = "environment-code")
    private long environmentCode;

    public String getScheduleParam() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        ScheduleParam scheduleParam = new ScheduleParam(this.startTime, this.endTime, this.crontab, this.timezoneId);
        return gson.toJson(scheduleParam);
    }

    public Schedule convert2Schedule() {
        Schedule schedule = new Schedule();

        schedule.setProcessDefinitionCode(this.processDefinitionCode);
        schedule.setCrontab(this.crontab);
        schedule.setStartTime(stringToDate(this.startTime));
        schedule.setEndTime(stringToDate(this.endTime));
        schedule.setTimezoneId(this.timezoneId);
        schedule.setWarningGroupId(this.warningGroupId);
        schedule.setWorkerGroup(this.workerGroup);
        schedule.setEnvironmentCode(this.environmentCode);

        FailureStrategy newFailureStrategy =
                this.failureStrategy == null ? FailureStrategy.CONTINUE : FailureStrategy.valueOf(this.failureStrategy);
        schedule.setFailureStrategy(newFailureStrategy);

        ReleaseState newReleaseState =
                this.releaseState == null ? ReleaseState.OFFLINE : ReleaseState.valueOf(this.releaseState);
        schedule.setReleaseState(newReleaseState);

        WarningType newWarningType =
                this.warningType == null ? WarningType.NONE : WarningType.valueOf(this.warningType);
        schedule.setWarningType(newWarningType);

        Priority newPriority =
                this.processInstancePriority == null ? Priority.MEDIUM : Priority.valueOf(this.processInstancePriority);
        schedule.setProcessInstancePriority(newPriority);

        Date date = new Date();
        schedule.setCreateTime(date);
        schedule.setUpdateTime(date);
        return schedule;
    }
}
