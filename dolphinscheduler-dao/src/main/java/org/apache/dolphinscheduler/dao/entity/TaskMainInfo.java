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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.ReleaseState;

import java.util.Date;
import java.util.Map;

/**
 * task main info
 */
public class TaskMainInfo {

    /**
     * task name
     */
    private String taskName;

    /**
     * task code
     */
    private long taskCode;

    /**
     * task version
     */
    private int taskVersion;

    /**
     * task type
     */
    private String taskType;

    /**
     * create time
     */
    private Date taskCreateTime;

    /**
     * update time
     */
    private Date taskUpdateTime;

    /**
     * processDefinitionCode
     */
    private long processDefinitionCode;

    /**
     * processDefinitionVersion
     */
    private int processDefinitionVersion;

    /**
     * processDefinitionName
     */
    private String processDefinitionName;

    /**
     * processReleaseState
     */
    private ReleaseState processReleaseState;

    /**
     * upstreamTaskMap(k:code,v:name)
     */
    private Map<Long, String> upstreamTaskMap;

    /**
     * upstreamTaskCode
     */
    private long upstreamTaskCode;

    /**
     * upstreamTaskName
     */
    private String upstreamTaskName;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public long getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(long taskCode) {
        this.taskCode = taskCode;
    }

    public int getTaskVersion() {
        return taskVersion;
    }

    public void setTaskVersion(int taskVersion) {
        this.taskVersion = taskVersion;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Date getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Date taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Date getTaskUpdateTime() {
        return taskUpdateTime;
    }

    public void setTaskUpdateTime(Date taskUpdateTime) {
        this.taskUpdateTime = taskUpdateTime;
    }

    public long getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(long processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public int getProcessDefinitionVersion() {
        return processDefinitionVersion;
    }

    public void setProcessDefinitionVersion(int processDefinitionVersion) {
        this.processDefinitionVersion = processDefinitionVersion;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public ReleaseState getProcessReleaseState() {
        return processReleaseState;
    }

    public void setProcessReleaseState(ReleaseState processReleaseState) {
        this.processReleaseState = processReleaseState;
    }

    public Map<Long, String> getUpstreamTaskMap() {
        return upstreamTaskMap;
    }

    public void setUpstreamTaskMap(Map<Long, String> upstreamTaskMap) {
        this.upstreamTaskMap = upstreamTaskMap;
    }

    public long getUpstreamTaskCode() {
        return upstreamTaskCode;
    }

    public void setUpstreamTaskCode(long upstreamTaskCode) {
        this.upstreamTaskCode = upstreamTaskCode;
    }

    public String getUpstreamTaskName() {
        return upstreamTaskName;
    }

    public void setUpstreamTaskName(String upstreamTaskName) {
        this.upstreamTaskName = upstreamTaskName;
    }
}
