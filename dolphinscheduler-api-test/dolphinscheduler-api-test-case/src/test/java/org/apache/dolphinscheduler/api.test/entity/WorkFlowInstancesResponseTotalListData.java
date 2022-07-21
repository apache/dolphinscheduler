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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkFlowInstancesResponseTotalListData {

    private Boolean blocked;

    private String cmdTypeIfComplement;

    private String commandParam;

    private String commandStartTime;

    private String commandType;

    private String complementData;

    private String dagData;

    private String dependenceScheduleTimes;

    private String dryRun;

    private String duration;

    private String endTime;

    private String environmentCode;

    private String executorId;

    private String executorName;

    private String failureStrategy;

    private String globalParams;

    private String historyCmd;

    private String host;

    private String id;

    private String isSubProcess;

    private String labels;

    private String locations;

    private String maxTryTimes;

    private String name;

    private String nextProcessInstanceId;

    private String processDefinition;

    private String processDefinitionCode;

    private String processDefinitionVersion;

    private String processInstancePriority;

    private String processInstanceStop;

    private String queue;

    private String recovery;

    private String restartTime;

    private String runTimes;

    private String scheduleTime;

    private String startTime;

    private String state;

    private String taskDependType;

    private String tenantCode;

    private String tenantId;

    private String timeout;

    private String varPool;

    private String warningGroupId;

    private String warningType;

    private String workerGroup;


}
