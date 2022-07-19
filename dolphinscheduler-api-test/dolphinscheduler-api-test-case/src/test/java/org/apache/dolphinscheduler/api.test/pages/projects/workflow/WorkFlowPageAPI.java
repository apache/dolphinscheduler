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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow;

import org.apache.dolphinscheduler.api.test.core.common.RequestMethod;
import org.apache.dolphinscheduler.api.test.pages.Route;
import org.apache.dolphinscheduler.api.test.pages.projects.project.ProjectPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.ScheduleTimeEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskDefinitionEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskLocationsEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskParamsEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskRelationEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.TenantPageAPI;
import org.apache.dolphinscheduler.api.test.pages.security.tenant.entity.TenantResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;
import org.apache.dolphinscheduler.api.test.utils.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.api.test.utils.enums.ConditionType;
import org.apache.dolphinscheduler.api.test.utils.enums.ExecCommandType;
import org.apache.dolphinscheduler.api.test.utils.enums.FailureStrategy;
import org.apache.dolphinscheduler.api.test.utils.enums.Flag;
import org.apache.dolphinscheduler.api.test.utils.enums.Priority;
import org.apache.dolphinscheduler.api.test.utils.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.api.test.utils.enums.ReleaseState;
import org.apache.dolphinscheduler.api.test.utils.enums.RunMode;
import org.apache.dolphinscheduler.api.test.utils.enums.TaskDependType;
import org.apache.dolphinscheduler.api.test.utils.enums.TimeoutFlag;
import org.apache.dolphinscheduler.api.test.utils.enums.WarningType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devskiller.jfairy.Fairy;

import io.restassured.specification.RequestSpecification;

public class WorkFlowPageAPI implements IWorkFlowPageAPI {
    private final Fairy fairy = Fairy.create();
    private final RequestSpecification reqSpec;
    private final String sessionId;

    private TenantPageAPI tenantPageAPI = null;

    private ProjectPageAPI projectPageAPI = null;

    public WorkFlowPageAPI(RequestSpecification reqSpec, String sessionId) {
        tenantPageAPI = new TenantPageAPI(reqSpec, sessionId);
        projectPageAPI = new ProjectPageAPI(reqSpec, sessionId);
        this.reqSpec = reqSpec;
        this.sessionId = sessionId;
    }

    @Override
    public RestResponse<Result> createWorkFlowDefinition(WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity, String projectCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntity.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    public RestResponse<Result> createWorkFlowDefinitionByTenant(String taskDefinitionName,
                                                                 String rawScript,
                                                                 String taskType,
                                                                 String tenant) {
        String projectCode = getProjectCode();
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntityInstance = getWorkFlowDefinitionRequestEntityInstance(taskDefinitionName, rawScript, taskType, projectCode, tenant);
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntityInstance.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    public RestResponse<Result> createWorkFlowDefinitionByTenantAndProjectName(String taskDefinitionName,
                                                                               String rawScript,
                                                                               String taskType,
                                                                               String tenant,
                                                                               String projectName) {
        String projectCode = getProjectCode(projectName);
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntityInstance = getWorkFlowDefinitionRequestEntityInstance(taskDefinitionName, rawScript, taskType, projectCode, tenant);
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntityInstance.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    public RestResponse<Result> createWorkFlowDefinition(String taskDefinitionName,
                                                         String rawScript,
                                                         String taskType,
                                                         String projectCode) {
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntityInstance = getWorkFlowDefinitionRequestEntityInstance(taskDefinitionName, rawScript, taskType, projectCode);
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntityInstance.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    public RestResponse<Result> createWorkFlowDefinition(String taskDefinitionName,
                                                         String rawScript,
                                                         String taskType,
                                                         String projectCode,
                                                         String tenant) {
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntityInstance = getWorkFlowDefinitionRequestEntityInstance(taskDefinitionName, rawScript, taskType, projectCode, tenant);
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowDefinitionRequestEntityInstance.toMap(), Route.workFlowDefinition(projectCode), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> releaseWorkFlowDefinition(WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity, String projectCode, String workFlowDefinitionCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowReleaseRequestEntity.toMap(), Route.workFlowDefinition(projectCode, workFlowDefinitionCode), RequestMethod.POST));
    }

    public RestResponse<Result> releaseWorkFlowDefinition(WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity, WorkFlowDefinitionResponseEntity workFlowDefinitionResponseEntity) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowReleaseRequestEntity.toMap(), Route.workFlowDefinition(
                String.valueOf(workFlowDefinitionResponseEntity.getProjectCode()),
                String.valueOf(workFlowDefinitionResponseEntity.getCode())),
            RequestMethod.POST));
    }

    public RestResponse<Result> releaseWorkFlowDefinition(String workFlowName, ReleaseState releaseState, String projectCode, String workFlowDefinitionCode) {
        WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity = getWorkFlowReleaseRequestEntityInstance(workFlowName, releaseState);
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowReleaseRequestEntity.toMap(), Route.workFlowDefinition(projectCode, workFlowDefinitionCode), RequestMethod.POST));
    }

    @Override
    public RestResponse<Result> runWorkFlowDefinition(WorkFlowRunRequestEntity workFlowRunRequestEntity, String projectCode) {
        return toResponse(restRequestByRequestMap(getRequestNewInstance().spec(reqSpec), sessionId,
            workFlowRunRequestEntity.toMap(), Route.workFlowRun(projectCode), RequestMethod.POST));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String name, String tenantCode, ProcessExecutionTypeEnum executionType,
                                                                                      String description, int timeout,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity
    ) {
        ArrayList<TaskDefinitionEntity> taskDefinitionEntities = new ArrayList<>();
        taskDefinitionEntities.add(taskDefinitionEntity);
        ArrayList<TaskRelationEntity> taskRelationEntities = new ArrayList<>();
        taskRelationEntities.add(taskRelationEntity);
        ArrayList<TaskLocationsEntity> taskLocationsEntities = new ArrayList<>();
        taskLocationsEntities.add(taskLocationsEntity);
        return getWorkFlowDefinitionRequestEntityInstance(name, tenantCode, executionType, description, timeout,
            entityToJson(taskDefinitionEntities),
            entityToJson(taskRelationEntities),
            entityToJson(taskLocationsEntities));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String name, String tenantCode, ProcessExecutionTypeEnum executionType,
                                                                                      String description, int timeout,
                                                                                      ArrayList<TaskDefinitionEntity> taskDefinitionEntities,
                                                                                      ArrayList<TaskRelationEntity> taskRelationEntities,
                                                                                      ArrayList<TaskLocationsEntity> taskLocationsEntities) {
        return getWorkFlowDefinitionRequestEntityInstance(name, tenantCode, executionType, description, timeout, entityToJson(taskDefinitionEntities), entityToJson(taskLocationsEntities),
            entityToJson(taskRelationEntities));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String description, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(),
            executionType, description, timeout, taskDefinitionEntity, taskRelationEntity, taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String description, int timeout,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(),
            ProcessExecutionTypeEnum.PARALLEL, description, timeout, taskDefinitionEntity,
            taskRelationEntity, taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String description,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(),
            ProcessExecutionTypeEnum.PARALLEL, description, 0, taskDefinitionEntity,
            taskRelationEntity, taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(),
            ProcessExecutionTypeEnum.PARALLEL, "", 0, taskDefinitionEntity, taskRelationEntity, taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String description, String tenantCode, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), tenantCode, executionType, description, timeout, taskDefinitionEntity, taskRelationEntity,
            taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String name, String description, String tenantCode, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      TaskDefinitionEntity taskDefinitionEntity,
                                                                                      TaskRelationEntity taskRelationEntity,
                                                                                      TaskLocationsEntity taskLocationsEntity) {
        return getWorkFlowDefinitionRequestEntityInstance(name, tenantCode, executionType, description, timeout, taskDefinitionEntity, taskRelationEntity,
            taskLocationsEntity);
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String name, String description, String tenantCode, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode) {
        return getWorkFlowDefinitionRequestEntityInstance(name, tenantCode, executionType, description, timeout,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String description, String tenantCode, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), tenantCode, executionType, description, timeout,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String tenantCode, int timeout, ProcessExecutionTypeEnum executionType,
                                                                                      String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), tenantCode, executionType, "", timeout,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(ProcessExecutionTypeEnum executionType,
                                                                                      String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(), executionType, "", 0,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), createTenant().getTenantCode(), ProcessExecutionTypeEnum.PARALLEL, "", 0,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }

    public WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String taskDefinitionName,
                                                                                      String rawScript,
                                                                                      String taskType,
                                                                                      String projectCode,
                                                                                      String tenant) {
        return getWorkFlowDefinitionRequestEntityInstance(fairy.person().getFirstName(), tenant, ProcessExecutionTypeEnum.PARALLEL, "", 0,
            getTaskDefinitionEntityInstance(taskDefinitionName, rawScript, taskType, projectCode),
            getTaskRelationEntityInstance(projectCode),
            getTaskLocationsEntityInstance(projectCode));
    }


    public TaskParamsEntity getTaskParamsEntityInstance(List<String> localParams, String rawScript, List<String> resourceList) {
        TaskParamsEntity taskParamsEntity = new TaskParamsEntity();
        taskParamsEntity.setLocalParams(localParams);
        taskParamsEntity.setRawScript(rawScript);
        taskParamsEntity.setResourceList(resourceList);
        return taskParamsEntity;
    }

    public TaskParamsEntity getTaskParamsEntityInstance(String rawScript) {
        TaskParamsEntity taskParamsEntity = new TaskParamsEntity();
        taskParamsEntity.setLocalParams(new ArrayList<>());
        taskParamsEntity.setRawScript(rawScript);
        taskParamsEntity.setResourceList(new ArrayList<>());
        return taskParamsEntity;
    }

    public TaskRelationEntity getTaskRelationEntityInstance(String name, int preTaskCode, String postTaskCode, int postTaskVersion, ConditionType conditionType, Map<String, String> conditionParams) {
        TaskRelationEntity taskRelationEntity = new TaskRelationEntity();
        taskRelationEntity.setName(name);
        taskRelationEntity.setPreTaskCode(preTaskCode);
        taskRelationEntity.setPostTaskVersion(postTaskVersion);
        taskRelationEntity.setPostTaskCode(postTaskCode);
        taskRelationEntity.setPostTaskVersion(postTaskVersion);
        taskRelationEntity.setConditionType(conditionType);
        taskRelationEntity.setConditionParams(conditionParams);
        return taskRelationEntity;
    }

    public TaskRelationEntity getTaskRelationEntityInstance(String projectCode) {
        TaskRelationEntity taskRelationEntity = new TaskRelationEntity();
        taskRelationEntity.setName("");
        taskRelationEntity.setPreTaskCode(0);
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setPostTaskCode(projectCode);
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setConditionType(ConditionType.NONE);
        taskRelationEntity.setConditionParams(new HashMap<>());
        return taskRelationEntity;
    }

    public TaskRelationEntity getTaskRelationEntityInstance() {
        TaskRelationEntity taskRelationEntity = new TaskRelationEntity();
        taskRelationEntity.setName("");
        taskRelationEntity.setPreTaskCode(0);
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setPostTaskCode(getProjectCode());
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setConditionType(ConditionType.NONE);
        taskRelationEntity.setConditionParams(new HashMap<>());
        return taskRelationEntity;
    }

    public TaskLocationsEntity getTaskLocationsEntityInstance(String projectCode, String x, String y) {
        TaskLocationsEntity taskLocationsEntity = new TaskLocationsEntity();
        taskLocationsEntity.setTaskCode(projectCode);
        taskLocationsEntity.setX(x);
        taskLocationsEntity.setY(y);
        return taskLocationsEntity;
    }

    public TaskLocationsEntity getTaskLocationsEntityInstance(String projectCode) {
        TaskLocationsEntity taskLocationsEntity = new TaskLocationsEntity();
        taskLocationsEntity.setTaskCode(projectCode);
        taskLocationsEntity.setX("66");
        taskLocationsEntity.setY("59");
        return taskLocationsEntity;
    }

    public TaskLocationsEntity getTaskLocationsEntityInstance() {
        TaskLocationsEntity taskLocationsEntity = new TaskLocationsEntity();
        taskLocationsEntity.setTaskCode(getProjectCode());
        taskLocationsEntity.setX("66");
        taskLocationsEntity.setY("59");
        return taskLocationsEntity;
    }

    public TaskDefinitionEntity getTaskDefinitionEntityInstance(String name, String description, String projectCode, String delayTime, int environmentCode,
                                                                String failRetryInterval, String failRetryTimes, Flag flag, TaskParamsEntity taskParams,
                                                                Priority taskPriority, String taskType, int timeout, TimeoutFlag timeoutFlag,
                                                                String timeoutNotifyStrategy, String workerGroup, int cpuQuota, int memoryMax) {
        TaskDefinitionEntity taskDefinitionEntity = new TaskDefinitionEntity();
        taskDefinitionEntity.setName(name);
        taskDefinitionEntity.setDescription(description);
        taskDefinitionEntity.setCode(projectCode);
        taskDefinitionEntity.setDelayTime(delayTime);
        taskDefinitionEntity.setEnvironmentCode(environmentCode);
        taskDefinitionEntity.setFailRetryInterval(failRetryInterval);
        taskDefinitionEntity.setFailRetryTimes(failRetryTimes);
        taskDefinitionEntity.setFlag(flag);
        taskDefinitionEntity.setTaskParams(taskParams);
        taskDefinitionEntity.setTaskPriority(taskPriority);
        taskDefinitionEntity.setTaskType(taskType);
        taskDefinitionEntity.setTimeout(timeout);
        taskDefinitionEntity.setTimeoutFlag(timeoutFlag);
        taskDefinitionEntity.setTimeoutNotifyStrategy(timeoutNotifyStrategy);
        taskDefinitionEntity.setWorkerGroup(workerGroup);
        taskDefinitionEntity.setCpuQuota(cpuQuota);
        taskDefinitionEntity.setMemoryMax(memoryMax);
        return taskDefinitionEntity;
    }

    public TaskDefinitionEntity getTaskDefinitionEntityInstance(String name,
                                                                String rawScript,
                                                                String taskType
    ) {

        return getTaskDefinitionEntityInstance(name, "", getProjectCode(), "0", -1, "1", "0",
            Flag.YES, getTaskParamsEntityInstance(rawScript), Priority.MEDIUM,
            taskType, 0, TimeoutFlag.CLOSE, "", "default", -1, -1);
    }

    public TaskDefinitionEntity getTaskDefinitionEntityInstance(String name,
                                                                String rawScript,
                                                                String taskType,
                                                                String projectCode
    ) {
        return getTaskDefinitionEntityInstance(name, "", projectCode, "0", -1, "1", "0",
            Flag.YES, getTaskParamsEntityInstance(rawScript), Priority.MEDIUM,
            taskType, 0, TimeoutFlag.CLOSE, "", "default", -1, -1);
    }

    public TaskDefinitionEntity getTaskDefinitionEntityInstance(String name,
                                                                TaskParamsEntity taskParams,
                                                                String taskType
    ) {
        return getTaskDefinitionEntityInstance(name, "", getProjectCode(), "0", -1, "1", "0",
            Flag.YES, taskParams, Priority.MEDIUM,
            taskType, 0, TimeoutFlag.CLOSE, "", "default", -1, -1);
    }

    public WorkFlowReleaseRequestEntity getWorkFlowReleaseRequestEntityInstance(String workFlowName, ReleaseState releaseState) {
        WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity = new WorkFlowReleaseRequestEntity();
        workFlowReleaseRequestEntity.setName(workFlowName);
        workFlowReleaseRequestEntity.setReleaseState(releaseState);
        return workFlowReleaseRequestEntity;
    }

    public WorkFlowReleaseRequestEntity getWorkFlowReleaseOnlineRequestEntityInstance(String workFlowName, ReleaseState releaseState) {
        return getWorkFlowReleaseRequestEntityInstance(workFlowName, ReleaseState.ONLINE);
    }

    public WorkFlowReleaseRequestEntity getWorkFlowReleaseOfflineRequestEntityInstance(String workFlowName, ReleaseState releaseState) {
        return getWorkFlowReleaseRequestEntityInstance(workFlowName, ReleaseState.OFFLINE);
    }

    public WorkFlowRunRequestEntity getWorkFlowRunRequestEntityInstance(String workFlowCode,
                                                                        FailureStrategy failureStrategy,
                                                                        WarningType warningType,
                                                                        String warningGroupId,
                                                                        ExecCommandType execType,
                                                                        String startNodeList,
                                                                        TaskDependType taskDependType,
                                                                        ComplementDependentMode complementDependentMode,
                                                                        RunMode runMode,
                                                                        Priority processInstancePriority,
                                                                        String workerGroup,
                                                                        String environmentCode,
                                                                        String expectedParallelismNumber,
                                                                        int dryRun,
                                                                        ScheduleTimeEntity scheduleTimeEntity) {
        WorkFlowRunRequestEntity workFlowRunRequestEntity = new WorkFlowRunRequestEntity();
        workFlowRunRequestEntity.setProcessDefinitionCode(workFlowCode);
        workFlowRunRequestEntity.setFailureStrategy(failureStrategy);
        workFlowRunRequestEntity.setWarningType(warningType);
        workFlowRunRequestEntity.setWarningGroupId(warningGroupId);
        workFlowRunRequestEntity.setExecType(execType);
        workFlowRunRequestEntity.setStartNodeList(startNodeList);
        workFlowRunRequestEntity.setTaskDependType(taskDependType);
        workFlowRunRequestEntity.setComplementDependentMode(complementDependentMode);
        workFlowRunRequestEntity.setRunMode(runMode);
        workFlowRunRequestEntity.setProcessInstancePriority(processInstancePriority);
        workFlowRunRequestEntity.setWorkerGroup(workerGroup);
        workFlowRunRequestEntity.setEnvironmentCode(environmentCode);
        workFlowRunRequestEntity.setExpectedParallelismNumber(expectedParallelismNumber);
        workFlowRunRequestEntity.setDryRun(dryRun);
        workFlowRunRequestEntity.setScheduleTime(scheduleTimeEntity.toString());
        return workFlowRunRequestEntity;
    }

    public WorkFlowRunRequestEntity getWorkFlowRunRequestEntityInstance(String workFlowCode,
                                                                        ScheduleTimeEntity scheduleTimeEntity) {

        return getWorkFlowRunRequestEntityInstance(workFlowCode,
            FailureStrategy.CONTINUE, WarningType.NONE, "",
            ExecCommandType.START_PROCESS, "", TaskDependType.TASK_POST,
            ComplementDependentMode.OFF_MODE,
            RunMode.RUN_MODE_SERIAL,
            Priority.MEDIUM,
            "default",
            "",
            "",
            0,
            scheduleTimeEntity);
    }

    public WorkFlowRunRequestEntity getWorkFlowRunRequestEntityInstance(String workFlowCode,
                                                                        String complementStartDate,
                                                                        String complementEndDate) {

        return getWorkFlowRunRequestEntityInstance(workFlowCode,
            FailureStrategy.CONTINUE, WarningType.NONE, "",
            ExecCommandType.START_PROCESS, "", TaskDependType.TASK_POST,
            ComplementDependentMode.OFF_MODE,
            RunMode.RUN_MODE_SERIAL,
            Priority.MEDIUM,
            "default",
            "",
            "",
            0,
            getScheduleTimeEntityInstance(complementStartDate, complementEndDate));
    }

    public WorkFlowRunRequestEntity getWorkFlowRunRequestEntityInstance(String workFlowCode,
                                                                        Date complementStartDate,
                                                                        Date complementEndDate) {

        return getWorkFlowRunRequestEntityInstance(workFlowCode,
            FailureStrategy.CONTINUE, WarningType.NONE, "",
            ExecCommandType.START_PROCESS, "", TaskDependType.TASK_POST,
            ComplementDependentMode.OFF_MODE,
            RunMode.RUN_MODE_SERIAL,
            Priority.MEDIUM,
            "default",
            "",
            "",
            0,
            getScheduleTimeEntityInstance(complementStartDate, complementEndDate));
    }

    private WorkFlowDefinitionRequestEntity getWorkFlowDefinitionRequestEntityInstance(String name, String tenantCode, ProcessExecutionTypeEnum executionType,
                                                                                       String description, int timeout,
                                                                                       String taskDefinitionJson, String taskRelationJson, String locations) {
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity = new WorkFlowDefinitionRequestEntity();
        workFlowDefinitionRequestEntity.setName(name);
        workFlowDefinitionRequestEntity.setTenantCode(tenantCode);
        workFlowDefinitionRequestEntity.setExecutionType(executionType);
        workFlowDefinitionRequestEntity.setDescription(description);
        workFlowDefinitionRequestEntity.setTimeout(timeout);
        workFlowDefinitionRequestEntity.setTaskDefinitionJson(taskDefinitionJson);
        workFlowDefinitionRequestEntity.setTaskRelationJson(taskRelationJson);
        workFlowDefinitionRequestEntity.setLocations(locations);
        return workFlowDefinitionRequestEntity;
    }

    private ScheduleTimeEntity getScheduleTimeEntityInstance(String complementStartDate,
                                                             String complementEndDate) {
        ScheduleTimeEntity scheduleTimeEntity = new ScheduleTimeEntity();
        scheduleTimeEntity.setComplementStartDate(complementStartDate);
        scheduleTimeEntity.setComplementEndDate(complementEndDate);
        return scheduleTimeEntity;
    }

    private ScheduleTimeEntity getScheduleTimeEntityInstance(Date complementStartDate,
                                                             Date complementEndDate) {
        ScheduleTimeEntity scheduleTimeEntity = new ScheduleTimeEntity();
        scheduleTimeEntity.setComplementStartDate(complementStartDate);
        scheduleTimeEntity.setComplementEndDate(complementEndDate);
        return scheduleTimeEntity;
    }

    private TenantResponseEntity createTenant() {
        return tenantPageAPI.createTenant();
    }

    private ProjectResponseEntity projectResponseEntity() {
        return projectPageAPI.createProject();
    }

    private ProjectResponseEntity projectResponseEntity(String projectName) {
        return projectPageAPI.createProject(projectName);
    }

    private String getProjectCode(String projectName) {
        return projectResponseEntity(projectName).getCode();
    }

    private String getProjectCode() {
        return projectResponseEntity().getCode();
    }

}
