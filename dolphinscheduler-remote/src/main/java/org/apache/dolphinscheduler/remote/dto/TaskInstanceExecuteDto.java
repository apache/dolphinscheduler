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

package org.apache.dolphinscheduler.remote.dto;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;
import java.util.Map;

import lombok.Data;

@Data
public class TaskInstanceExecuteDto {

    private int id;

    private String name;

    private String taskType;

    private int processInstanceId;

    private long taskCode;

    private int taskDefinitionVersion;

    private String processInstanceName;

    private int taskGroupPriority;

    private TaskExecutionStatus state;

    private Date firstSubmitTime;

    private Date submitTime;

    private Date startTime;

    private Date endTime;

    private String host;

    private String executePath;

    private String logPath;

    private int retryTimes;

    private Flag alertFlag;

    private int pid;

    private String appLink;

    private Flag flag;

    private String duration;

    private int maxRetryTimes;

    private int retryInterval;

    private Priority taskInstancePriority;

    private Priority processInstancePriority;

    private String workerGroup;

    private Long environmentCode;

    private String environmentConfig;

    private int executorId;

    private String varPool;

    private String executorName;

    private Map<String, String> resources;

    private int delayTime;

    private String taskParams;

    private int dryRun;

    private int taskGroupId;

    private Integer cpuQuota;

    private Integer memoryMax;

    private TaskExecuteType taskExecuteType;
}
