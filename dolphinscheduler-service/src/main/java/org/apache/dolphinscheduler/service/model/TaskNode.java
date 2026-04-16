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

package org.apache.dolphinscheduler.service.model;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
public class TaskNode {

    /**
     * task node id
     */
    private String id;

    /**
     * task node code
     */
    private long code;

    /**
     * task node version
     */
    private int version;

    /**
     * task node name
     */
    private String name;

    /**
     * task node description
     */
    private String desc;

    /**
     * task node type
     */
    private String type;

    /**
     * the run flag has two states, NORMAL or FORBIDDEN
     */
    private String runFlag;

    /**
     * the front field
     */
    private String loc;

    /**
     * maximum number of retries
     */
    private int maxRetryTimes;

    /**
     * Unit of retry interval: points
     */
    private int retryInterval;

    private int taskGroupId;
    /**
     * task group id
     */
    private int taskGroupPriority;

    /**
     * params information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String params;

    /**
     * inner dependency information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String preTasks;

    /**
     * users store additional information
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String extras;

    /**
     * node dependency list
     */
    private List<Long> depList;

    private Priority taskInstancePriority;

    private String workerGroup;

    private Long environmentCode;

    /**
     * task time out
     */
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
    private String timeout;

    /**
     * delay execution time.
     */
    private int delayTime;

    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    private TaskExecuteType taskExecuteType;

    public void setPreTasks(String preTasks) {
        this.preTasks = preTasks;
        this.depList = JSONUtils.toList(preTasks, Long.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskNode taskNode = (TaskNode) o;
        return Objects.equals(name, taskNode.name)
                && Objects.equals(desc, taskNode.desc)
                && Objects.equals(type, taskNode.type)
                && Objects.equals(params, taskNode.params)
                && Objects.equals(preTasks, taskNode.preTasks)
                && Objects.equals(extras, taskNode.extras)
                && Objects.equals(runFlag, taskNode.runFlag)
                && Objects.equals(workerGroup, taskNode.workerGroup)
                && Objects.equals(environmentCode, taskNode.environmentCode)
                && CollectionUtils.isEqualCollection(depList, taskNode.depList)
                && Objects.equals(taskExecuteType, taskNode.taskExecuteType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, type, params, preTasks, extras, depList, runFlag);
    }

    @Override
    public String toString() {
        return "TaskNode{"
                + "id='" + id + '\''
                + ", code=" + code
                + ", version=" + version
                + ", name='" + name + '\''
                + ", desc='" + desc + '\''
                + ", type='" + type + '\''
                + ", runFlag='" + runFlag + '\''
                + ", loc='" + loc + '\''
                + ", maxRetryTimes=" + maxRetryTimes
                + ", retryInterval=" + retryInterval
                + ", params='" + params + '\''
                + ", preTasks='" + preTasks + '\''
                + ", extras='" + extras + '\''
                + ", depList=" + depList
                + ", taskInstancePriority=" + taskInstancePriority
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentCode=" + environmentCode
                + ", timeout='" + timeout + '\''
                + ", delayTime=" + delayTime + '\''
                + ", taskExecuteType=" + taskExecuteType
                + '}';
    }

}
