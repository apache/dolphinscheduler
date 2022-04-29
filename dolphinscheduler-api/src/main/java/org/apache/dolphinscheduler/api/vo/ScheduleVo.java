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

public class ScheduleVo {

    private int id;

    /**
     * process definition code
     */
    private long processDefinitionCode;

    /**
     * process definition name
     */
    private String processDefinitionName;

    /**
     * project name
     */
    private String projectName;

    /**
     * schedule description
     */
    private String definitionDescription;

    /**
     * schedule start time
     */
    private String startTime;

    /**
     * schedule end time
     */
    private String endTime;

    /**
     * timezoneId
     * <p>see {@link java.util.TimeZone#getTimeZone(String)}
     */
    private String timezoneId;

    /**
     * crontab expression
     */
    private String crontab;

    /**
     * failure strategy
     */
    private FailureStrategy failureStrategy;

    /**
     * warning type
     */
    private WarningType warningType;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * created user id
     */
    private int userId;

    /**
     * created user name
     */
    private String userName;

    /**
     * release state
     */
    private ReleaseState releaseState;

    /**
     * warning group id
     */
    private int warningGroupId;


    /**
     * process instance priority
     */
    private Priority processInstancePriority;

    /**
     *  worker group
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    public ScheduleVo(Schedule schedule) {
        this.setId(schedule.getId());
        this.setCrontab(schedule.getCrontab());
        this.setProjectName(schedule.getProjectName());
        this.setUserName(schedule.getUserName());
        this.setWorkerGroup(schedule.getWorkerGroup());
        this.setWarningType(schedule.getWarningType());
        this.setWarningGroupId(schedule.getWarningGroupId());
        this.setUserId(schedule.getUserId());
        this.setUpdateTime(schedule.getUpdateTime());
        this.setTimezoneId(schedule.getTimezoneId());
        this.setReleaseState(schedule.getReleaseState());
        this.setProcessInstancePriority(schedule.getProcessInstancePriority());
        this.setProcessDefinitionName(schedule.getProcessDefinitionName());
        this.setProcessDefinitionCode(schedule.getProcessDefinitionCode());
        this.setFailureStrategy(schedule.getFailureStrategy());
        this.setEnvironmentCode(schedule.getEnvironmentCode());
        this.setStartTime(DateUtils.dateToString(schedule.getStartTime(), ZoneId.systemDefault().getId()));
        this.setEndTime(DateUtils.dateToString(schedule.getEndTime(), ZoneId.systemDefault().getId()));
    }

    public int getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(int warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }

    public String getCrontab() {
        return crontab;
    }

    public void setCrontab(String crontab) {
        this.crontab = crontab;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ReleaseState getReleaseState() {
        return releaseState;
    }

    public void setReleaseState(ReleaseState releaseState) {
        this.releaseState = releaseState;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public Long getEnvironmentCode() {
        return this.environmentCode;
    }

    public void setEnvironmentCode(Long environmentCode) {
        this.environmentCode = environmentCode;
    }

    @Override
    public String toString() {
        return "Schedule{"
            + "id=" + id
            + ", processDefinitionCode=" + processDefinitionCode
            + ", processDefinitionName='" + processDefinitionName + '\''
            + ", projectName='" + projectName + '\''
            + ", description='" + definitionDescription + '\''
            + ", startTime=" + startTime
            + ", endTime=" + endTime
            + ", timezoneId='" + timezoneId + +'\''
            + ", crontab='" + crontab + '\''
            + ", failureStrategy=" + failureStrategy
            + ", warningType=" + warningType
            + ", createTime=" + createTime
            + ", updateTime=" + updateTime
            + ", userId=" + userId
            + ", userName='" + userName + '\''
            + ", releaseState=" + releaseState
            + ", warningGroupId=" + warningGroupId
            + ", processInstancePriority=" + processInstancePriority
            + ", workerGroup='" + workerGroup + '\''
            + ", environmentCode='" + environmentCode + '\''
            + '}';
    }

    public String getDefinitionDescription() {
        return definitionDescription;
    }

    public void setDefinitionDescription(String definitionDescription) {
        this.definitionDescription = definitionDescription;
    }
}
