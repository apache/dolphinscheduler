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
package org.apache.dolphinscheduler.api.dto.gantt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Task
 */
public class Task {
    /**
     * task name
     */
    private String taskName;

    /**
     * task start date
     */
    private List<Long> startDate = new ArrayList<>();
    /**
     * task end date
     */
    private List<Long> endDate = new ArrayList<>();

    /**
     * task execution date
     */
    private Date executionDate;

    /**
     * task iso start
     */
    private Date isoStart;

    /**
     * task iso end
     */
    private Date isoEnd;

    /**
     * task status
     */
    private String status;

    /**
     * task duration
     */
    private String duration;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<Long> getStartDate() {
        return startDate;
    }

    public void setStartDate(List<Long> startDate) {
        this.startDate = startDate;
    }

    public List<Long> getEndDate() {
        return endDate;
    }

    public void setEndDate(List<Long> endDate) {
        this.endDate = endDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getIsoStart() {
        return isoStart;
    }

    public void setIsoStart(Date isoStart) {
        this.isoStart = isoStart;
    }

    public Date getIsoEnd() {
        return isoEnd;
    }

    public void setIsoEnd(Date isoEnd) {
        this.isoEnd = isoEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
