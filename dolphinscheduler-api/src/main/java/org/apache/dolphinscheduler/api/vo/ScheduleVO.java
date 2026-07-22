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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Schedule;

import java.time.ZoneId;
import java.util.Date;

import lombok.Data;

@Data
public class ScheduleVO {

    private int id;

    private long workflowDefinitionCode;

    private String workflowDefinitionName;

    private String projectName;

    private String definitionDescription;

    private String startTime;

    private String endTime;

    /**
     * timezoneId
     * <p>see {@link java.util.TimeZone#getTimeZone(String)}
     */
    private String timezoneId;

    private String crontab;

    private FailureStrategy failureStrategy;

    private WarningType warningType;

    private Date createTime;

    private Date updateTime;

    private int userId;

    private String userName;

    private ReleaseState releaseState;

    private int warningGroupId;

    private Priority workflowInstancePriority;

    private String workerGroup;

    private String tenantCode;

    private Long environmentCode;

    private String environmentName;

    public ScheduleVO(Schedule schedule) {
        this.setId(schedule.getId());
        this.setCrontab(schedule.getCrontab());
        this.setProjectName(schedule.getProjectName());
        this.setUserName(schedule.getUserName());
        this.setWorkerGroup(schedule.getWorkerGroup());
        this.setWarningType(schedule.getWarningType());
        this.setWarningGroupId(schedule.getWarningGroupId());
        this.setUserId(schedule.getUserId());
        this.setCreateTime(schedule.getCreateTime());
        this.setUpdateTime(schedule.getUpdateTime());
        this.setTimezoneId(schedule.getTimezoneId());
        this.setReleaseState(schedule.getReleaseState());
        this.setWorkflowInstancePriority(schedule.getWorkflowInstancePriority());
        this.setWorkflowDefinitionName(schedule.getWorkflowDefinitionName());
        this.setWorkflowDefinitionCode(schedule.getWorkflowDefinitionCode());
        this.setFailureStrategy(schedule.getFailureStrategy());
        this.setEnvironmentCode(schedule.getEnvironmentCode());
        this.setStartTime(DateUtils.dateToString(schedule.getStartTime(), ZoneId.systemDefault().getId()));
        this.setEndTime(DateUtils.dateToString(schedule.getEndTime(), ZoneId.systemDefault().getId()));
        this.setEnvironmentName(schedule.getEnvironmentName());
        this.setTenantCode(schedule.getTenantCode());
    }
}
