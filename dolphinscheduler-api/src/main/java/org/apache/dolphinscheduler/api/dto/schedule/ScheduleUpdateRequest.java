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

import static org.apache.dolphinscheduler.common.Constants.YYYY_MM_DD_HH_MM_SS;
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
import io.swagger.annotations.ApiModelProperty;

/**
 * schedule update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleUpdateRequest {

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
