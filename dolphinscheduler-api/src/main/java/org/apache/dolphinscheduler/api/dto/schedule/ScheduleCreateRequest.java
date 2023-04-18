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
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * schedule create request
 */
@Data
public class ScheduleCreateRequest {

    @Schema(example = "1234567890123", required = true)
    private long processDefinitionCode;

    @Schema(example = "schedule timezone", required = true)
    private String crontab;

    @Schema(example = "2021-01-01 10:00:00", required = true)
    private String startTime;

    @Schema(example = "2022-01-01 12:00:00", required = true)
    private String endTime;

    @Schema(example = "Asia/Shanghai", required = true)
    private String timezoneId;

    @Schema(allowableValues = "CONTINUE / END", example = "CONTINUE", description = "default CONTINUE if value not provide.")
    private String failureStrategy;

    @Schema(allowableValues = "ONLINE / OFFLINE", example = "OFFLINE", description = "default OFFLINE if value not provide.")
    private String releaseState;

    @Schema(allowableValues = "NONE / SUCCESS / FAILURE / ALL", example = "SUCCESS", description = "default NONE if value not provide.")
    private String warningType;

    @Schema(example = "2", description = "default 0 if value not provide.")
    private int warningGroupId;

    @Schema(allowableValues = "HIGHEST / HIGH / MEDIUM / LOW / LOWEST", example = "MEDIUM", description = "default MEDIUM if value not provide.")
    private String processInstancePriority;

    @Schema(example = "worker-group-name")
    private String workerGroup;

    @Schema(example = "tenant-code")
    private String tenantCode;

    @Schema(example = "environment-code")
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
        schedule.setTenantCode(this.tenantCode);
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
