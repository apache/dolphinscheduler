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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * to master/worker task transport
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskExecutionContext implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * task id
     */
    private int taskInstanceId;

    /**
     * task name
     */
    private String taskName;

    /**
     * task first submit time.
     */
    private long firstSubmitTime;

    /**
     * task start time
     */
    private long startTime;

    /**
     * task type
     */
    private String taskType;

    private String workflowInstanceHost;

    /**
     * host
     */
    private String host;

    /**
     * task execute path
     */
    private String executePath;

    /**
     * log path
     */
    private String logPath;

    /**
     * applicationId path
     */
    private String appInfoPath;

    /**
     * task json
     */
    private String taskJson;

    /**
     * processId
     */
    private int processId;

    /**
     * processCode
     */
    private Long processDefineCode;

    /**
     * processVersion
     */
    private int processDefineVersion;

    /**
     * appIds
     */
    private String appIds;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * process instance schedule time
     */
    private long scheduleTime;

    /**
     * process instance global parameters
     */
    private String globalParams;

    /**
     * execute user id
     */
    private int executorId;

    /**
     * command type if complement
     */
    private int cmdTypeIfComplement;

    /**
     * tenant code
     */
    private String tenantCode;

    /**
     * process define id
     */
    private int processDefineId;

    /**
     * project id
     */
    private int projectId;

    /**
     * project code
     */
    private long projectCode;

    /**
     * taskParams
     */
    private String taskParams;

    /**
     * environmentConfig
     */
    private String environmentConfig;

    /**
     * definedParams
     * // todo: we need to rename definedParams, prepareParamsMap, paramsMap, this is confusing
     */
    private Map<String, String> definedParams;

    /**
     * prepare params map
     */
    private Map<String, Property> prepareParamsMap;

    /**
     * task AppId
     */
    private String taskAppId;

    /**
     * task timeout strategy
     */
    private TaskTimeoutStrategy taskTimeoutStrategy;

    /**
     * task timeout
     */
    private int taskTimeout;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * current execution status
     */
    private TaskExecutionStatus currentExecutionStatus;

    private ResourceParametersHelper resourceParametersHelper;

    /**
     * endTime
     */
    private long endTime;

    /**
     * sql TaskExecutionContext
     */
    private SQLTaskExecutionContext sqlTaskExecutionContext;
    /**
     * k8s TaskExecutionContext
     */
    private K8sTaskExecutionContext k8sTaskExecutionContext;

    private ResourceContext resourceContext;

    /**
     * taskInstance varPool
     */
    private String varPool;

    /**
     * dry run flag
     */
    private int dryRun;

    private Map<String, Property> paramsMap;

    private DataQualityTaskExecutionContext dataQualityTaskExecutionContext;

    /**
     * cpu quota
     */
    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    /**
     * test flag
     */
    private int testFlag;

    private boolean logBufferEnable;

    private int dispatchFailTimes;

    public int increaseDispatchFailTimes() {
        return ++dispatchFailTimes;
    }
}
