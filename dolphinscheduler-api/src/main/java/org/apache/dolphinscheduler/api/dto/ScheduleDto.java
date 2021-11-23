/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.WarningType;

/**
 * schedule dto
 **/
public class ScheduleDto {

    private ScheduleParam schedule;
    private FailureStrategy failureStrategy;
    private WarningType warningType;
    private Priority processInstancePriority;
    private Integer warningGroupId;
    private String workerGroup;
    private Long environmentCode;

    public ScheduleDto() {
    }

    public ScheduleParam getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleParam schedule) {
        this.schedule = schedule;
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

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public Integer getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(Integer warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public Long getEnvironmentCode() {
        return environmentCode;
    }

    public void setEnvironmentCode(Long environmentCode) {
        this.environmentCode = environmentCode;
    }

    @Override
    public String toString() {
        return "ScheduleDto{" +
                "schedule=" + schedule +
                ", failureStrategy=" + failureStrategy +
                ", warningType=" + warningType +
                ", processInstancePriority=" + processInstancePriority +
                ", warningGroupId=" + warningGroupId +
                ", workerGroup='" + workerGroup + '\'' +
                ", environmentCode=" + environmentCode +
                '}';
    }
}
