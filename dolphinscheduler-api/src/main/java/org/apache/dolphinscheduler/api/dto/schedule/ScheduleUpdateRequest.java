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

import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;
import static org.apache.dolphinscheduler.common.utils.DateUtils.format;
import static org.apache.dolphinscheduler.common.utils.DateUtils.stringToDate;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * schedule update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleUpdateRequest {

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

    @Schema(example = "environment-code")
    private long environmentCode;

    public String updateScheduleParam(Schedule schedule) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Schedule scheduleUpdate = this.mergeIntoSchedule(schedule);

        String startTimeUpdate = scheduleUpdate.getStartTime() == null ? null
                : format(scheduleUpdate.getStartTime(), YYYY_MM_DD_HH_MM_SS, schedule.getTimezoneId());
        String endTimeUpdate = scheduleUpdate.getEndTime() == null ? null
                : format(scheduleUpdate.getEndTime(), YYYY_MM_DD_HH_MM_SS, schedule.getTimezoneId());
        ScheduleParam scheduleParam = new ScheduleParam(startTimeUpdate, endTimeUpdate, scheduleUpdate.getCrontab(),
                scheduleUpdate.getTimezoneId());

        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(scheduleParam);
    }

    public Schedule mergeIntoSchedule(Schedule schedule) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Schedule scheduleDeepCopy = (Schedule) BeanUtils.cloneBean(schedule);;
        assert scheduleDeepCopy != null;
        if (this.crontab != null) {
            scheduleDeepCopy.setCrontab(this.crontab);
        }
        if (this.startTime != null) {
            scheduleDeepCopy.setStartTime(stringToDate(this.startTime));
        }
        if (this.endTime != null) {
            scheduleDeepCopy.setEndTime(stringToDate(this.endTime));
        }
        if (this.timezoneId != null) {
            scheduleDeepCopy.setTimezoneId(this.timezoneId);
        }
        if (this.failureStrategy != null) {
            scheduleDeepCopy.setFailureStrategy(FailureStrategy.valueOf(this.failureStrategy));
        }
        if (this.releaseState != null) {
            scheduleDeepCopy.setReleaseState(ReleaseState.valueOf(this.releaseState));
        }
        if (this.warningType != null) {
            scheduleDeepCopy.setWarningType(WarningType.valueOf(this.warningType));
        }
        if (this.warningGroupId != 0) {
            scheduleDeepCopy.setWarningGroupId(this.warningGroupId);
        }
        if (this.processInstancePriority != null) {
            scheduleDeepCopy.setProcessInstancePriority(Priority.valueOf(this.processInstancePriority));
        }
        if (this.workerGroup != null) {
            scheduleDeepCopy.setWorkerGroup(this.workerGroup);
        }
        if (this.environmentCode != 0L) {
            scheduleDeepCopy.setEnvironmentCode(this.environmentCode);
        }

        scheduleDeepCopy.setUpdateTime(new Date());
        return scheduleDeepCopy;
    }
}
