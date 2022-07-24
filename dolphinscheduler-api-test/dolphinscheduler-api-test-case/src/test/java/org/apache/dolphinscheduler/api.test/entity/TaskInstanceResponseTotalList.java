/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskInstanceResponseTotalList {

    private String alertFlag;

    private String appLink;

    private boolean blockingTask;

    private boolean conditionsTask;

    private Integer delayTime;

    private boolean dependTask;

    private String dependency;

    private String dependentResult;

    private Integer dryRun;

    private String duration;

    private Date endTime;

    private String environmentCode;

    private String environmentConfig;

    private String executePath;

    private String executorId;

    private String executorName;

    private boolean firstRun;

    private Date firstSubmitTime;

    private String flag;

    private String host;

    private Integer id;

    private String logPath;

    private Integer maxRetryTimes;

    private String name;

    private Integer pid;

    private String processDefine;

    private String processInstance;

    private Integer processInstanceId;

    private String processInstanceName;

    private String processInstancePriority;

    private String resources;

    private Integer retryInterval;

    private Integer retryTimes;

    private Date startTime;

    private String state;

    private boolean subProcess;

    private Date submitTime;

    private String switchDependency;

    private boolean switchTask;

    private String taskCode;

    private boolean taskComplete;

    private String taskDefine;

    private Integer taskDefinitionVersion;

    private Integer taskGroupId;

    private Integer taskGroupPriority;

    private String taskInstancePriority;

    private String taskParams;

    private String taskType;

    private String varPool;

    private String workerGroup;

}
