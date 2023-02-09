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

package org.apache.dolphinscheduler.tools.demo;

import static org.apache.dolphinscheduler.common.enums.ConditionType.NONE;
import static org.apache.dolphinscheduler.common.enums.Flag.YES;
import static org.apache.dolphinscheduler.common.enums.Priority.MEDIUM;
import static org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum.PARALLEL;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessDefinitionDemo {

    @Value("${demo.tenant-code}")
    private String tenantCode;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private ProxyProcessDefinitionController proxyProcessDefinitionController;

    public void createProcessDefinitionDemo() throws Exception {
        // get user
        User loginUser = userMapper.selectById("1");
        Date now = new Date();

        // create demo tenantCode
        CreateDemoTenant createDemoTenant = new CreateDemoTenant();
        createDemoTenant.createTenantCode(tenantCode);

        // create and get demo projectCode
        Project project = projectMapper.queryByName("demo");
        if (project != null) {
            log.warn("Project {} already exists.", project.getName());
        }
        try {
            project = Project
                    .builder()
                    .name("demo")
                    .code(CodeGenerateUtils.getInstance().genCode())
                    .description("")
                    .userId(loginUser.getId())
                    .userName(loginUser.getUserName())
                    .createTime(now)
                    .updateTime(now)
                    .build();
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("create project error", e);
        }
        if (projectMapper.insert(project) > 0) {
            log.info("create project success");
        } else {
            throw new Exception("create project error");
        }
        Long projectCode = null;
        try {
            projectCode = project.getCode();
        } catch (NullPointerException e) {
            log.error("project code is null", e);
        }

        // generate access token
        String expireTime = DemoConstants.Expire_Time;
        String token = EncryptionUtils.getMd5(1 + expireTime + System.currentTimeMillis());
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(1);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());

        int insert = accessTokenMapper.insert(accessToken);

        if (insert > 0) {
            log.info("create access token success");
        } else {
            log.info("create access token error");
        }

        // creat process definition demo
        // shell demo
        ProxyResult shellResult = shellDemo(token, projectCode, tenantCode);
        log.info("create shell demo {}", shellResult.getMsg());

        // subprocess demo
        LinkedHashMap<String, Object> subProcess = (LinkedHashMap<String, Object>) shellResult.getData();
        String subProcessCode = String.valueOf(subProcess.get("code"));
        ProxyResult subProcessResult = subProcessDemo(token, projectCode, tenantCode, subProcessCode);
        log.info("create subprocess demo {}", subProcessResult.getMsg());

        // switch demo
        ProxyResult switchResult = swicthDemo(token, projectCode, tenantCode);
        log.info("create switch demo {}", switchResult.getMsg());

        // condition demo
        ProxyResult conditionResult = conditionDemo(token, projectCode, tenantCode);
        log.info("create condition demo {}", conditionResult.getMsg());

        // dependent demo
        LinkedHashMap<String, Object> switchProcess = (LinkedHashMap<String, Object>) switchResult.getData();
        String switchProcessCode = String.valueOf(switchProcess.get("code"));
        ProxyResult dependentResult =
                dependentProxyResultDemo(token, projectCode, tenantCode, subProcessCode, switchProcessCode);
        log.info("create dependent demo {}", dependentResult.getMsg());

        // parameter context demo
        ProxyResult parameterContextResult = parameterContextDemo(token, projectCode, tenantCode);
        log.info("create parameter context demo {}", parameterContextResult.getMsg());

        // clear log demo
        ProxyResult clearLogResult = clearLogDemo(token, projectCode, tenantCode);
        log.info("create clear log demo {}", clearLogResult.getMsg());

    }

    public ProxyResult clearLogDemo(String token, long projectCode, String tenantCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 1; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String absolutePath = System.getProperty("user.dir");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_clear_log");
        processDefinitionLog.setDescription("Clear the DS log files from 30 days ago");
        processDefinitionLog.setGlobalParams("[]");
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLog.setTaskType("SHELL");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("Clear log node");
        taskDefinitionLogFirst.setDescription("");
        taskDefinitionLogFirst.setTaskParams("{\"localParams\":[],\"rawScript\":\"cd cd " + absolutePath
                + "\\r\\nfind ./logs/ -mtime +30 -name \\\"*.log\\\" -exec rm -rf {} \\\\;\",\"resourceList\":[]}");

        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult dependentProxyResultDemo(String token, long projectCode, String tenantCode,
                                                String shellProcessCode, String switchProcessCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_dependent");
        processDefinitionLog.setDescription("Check the completion of daily tasks");
        processDefinitionLog.setGlobalParams("[]");
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogSecond = processTaskRelationLogs.get(1);
        processTaskRelationLogSecond.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogSecond.setPreTaskVersion(1);
        processTaskRelationLogSecond.setPostTaskCode(taskCodes.get(1));
        processTaskRelationLogSecond.setPostTaskVersion(1);

        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("Weekly report task");
        taskDefinitionLogFirst.setDescription(
                "The weekly report task requires the demo_shell and demo_switch tasks to be successfully executed every day of the last week");
        taskDefinitionLogFirst.setTaskParams(
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"projectCode\":"
                        + projectCode + ",\"definitionCode\":" + shellProcessCode
                        + ",\"depTaskCode\":0,\"cycle\":\"day\",\"dateValue\":\"last1Days\",\"state\":null},{\"projectCode\":"
                        + projectCode + ",\"definitionCode\":" + switchProcessCode
                        + ",\"depTaskCode\":0,\"cycle\":\"day\",\"dateValue\":\"last1Days\",\"state\":null}]}]}}");
        taskDefinitionLogFirst.setTaskType("DEPENDENT");

        TaskDefinitionLog taskDefinitionLogSecond = taskDefinitionLogs.get(1);
        taskDefinitionLogSecond.setCode(taskCodes.get(1));
        taskDefinitionLogSecond.setName("Weekly Report Task Result");
        taskDefinitionLogSecond.setDescription("Result report after the completion of the weekly report task");
        taskDefinitionLogSecond
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"end of report\\\"\",\"resourceList\":[]}");
        taskDefinitionLogSecond.setTaskType("SHELL");
        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult parameterContextDemo(String token, long projectCode, String tenantCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_parameter_context");
        processDefinitionLog.setDescription("Upstream and downstream task node parameter transfer");
        processDefinitionLog.setGlobalParams(DemoConstants.PARAMETER_CONTEXT_PARAMS);
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogSecond = processTaskRelationLogs.get(1);
        processTaskRelationLogSecond.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogSecond.setPreTaskVersion(1);
        processTaskRelationLogSecond.setPostTaskCode(taskCodes.get(1));
        processTaskRelationLogSecond.setPostTaskVersion(1);

        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLog.setTaskType("SHELL");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("Upstream task node");
        taskDefinitionLogFirst.setDescription("Create a local parameter and pass the assignment to the downstream");
        taskDefinitionLogFirst.setTaskParams(
                "{\"localParams\":[{\"prop\":\"value\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"0\"},{\"prop\":\"output\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"rawScript\":\"echo \\\"====Node start====\\\"\\r\\necho '${setValue(output=1)}'\\r\\n\\r\\necho ${output}\\r\\necho ${value}\\r\\n\\r\\necho \\\"====Node end====\\\"\",\"resourceList\":[]}");

        TaskDefinitionLog taskDefinitionLogSecond = taskDefinitionLogs.get(1);
        taskDefinitionLogSecond.setCode(taskCodes.get(1));
        taskDefinitionLogSecond.setName("Downstream task node");
        taskDefinitionLogSecond.setDescription("Test outputs the parameters passed by the upstream task");
        taskDefinitionLogSecond.setTaskParams(
                "{\"localParams\":[],\"rawScript\":\"echo \\\"====node start====\\\"\\r\\n\\r\\necho ${output}\\r\\n\\r\\necho ${value}\\r\\n\\r\\necho \\\"====Node end====\\\"\",\"resourceList\":[]}");
        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult conditionDemo(String token, long projectCode, String tenantCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_condition");
        processDefinitionLog.setDescription("Coin Toss");
        processDefinitionLog.setGlobalParams("[]");
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(1));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogSecond = processTaskRelationLogs.get(1);
        processTaskRelationLogSecond.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogSecond.setPreTaskVersion(1);
        processTaskRelationLogSecond.setPostTaskCode(taskCodes.get(2));
        processTaskRelationLogSecond.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogThird = processTaskRelationLogs.get(2);
        processTaskRelationLogThird.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogThird.setPreTaskVersion(1);
        processTaskRelationLogThird.setPostTaskCode(taskCodes.get(3));
        processTaskRelationLogThird.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogFourth = processTaskRelationLogs.get(3);
        processTaskRelationLogFourth.setPreTaskCode(taskCodes.get(1));
        processTaskRelationLogFourth.setPreTaskVersion(1);
        processTaskRelationLogFourth.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFourth.setPostTaskVersion(1);
        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("condition");
        taskDefinitionLogFirst.setDescription("head is the status of success, tail is the status of failure");
        taskDefinitionLogFirst.setTaskParams(
                "{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[]},\"conditionResult\":{\"successNode\":["
                        + taskCodeThird + "],\"failedNode\":[" + taskCodeFourth + "]}}");
        taskDefinitionLogFirst.setTaskType("CONDITIONS");

        TaskDefinitionLog taskDefinitionLogSecond = taskDefinitionLogs.get(1);
        taskDefinitionLogSecond.setCode(taskCodes.get(1));
        taskDefinitionLogSecond.setName("coin");
        taskDefinitionLogSecond.setDescription("Toss a coin");
        taskDefinitionLogSecond
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"Start\\\"\",\"resourceList\":[]}");
        taskDefinitionLogSecond.setTaskType("SHELL");

        TaskDefinitionLog taskDefinitionLogThird = taskDefinitionLogs.get(2);
        taskDefinitionLogThird.setCode(taskCodes.get(2));
        taskDefinitionLogThird.setName("head");
        taskDefinitionLogThird.setDescription("Choose to learn if the result is head");
        taskDefinitionLogThird.setTaskParams(
                "{\"localParams\":[],\"rawScript\":\"echo \\\"Start learning\\\"\",\"resourceList\":[]}");
        taskDefinitionLogThird.setTaskType("SHELL");

        TaskDefinitionLog taskDefinitionLogFourth = taskDefinitionLogs.get(3);
        taskDefinitionLogFourth.setCode(taskCodes.get(3));
        taskDefinitionLogFourth.setName("tail");
        taskDefinitionLogFourth.setDescription("Choose to play if the result is tail");
        taskDefinitionLogFourth
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"Start playing\\\"\",\"resourceList\":[]}");
        taskDefinitionLogFourth.setTaskType("SHELL");
        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult swicthDemo(String token, long projectCode, String tenantCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_switch");
        processDefinitionLog.setDescription("Determine which task to perform based on conditions");
        processDefinitionLog.setGlobalParams(DemoConstants.SWITCH_GLOBAL_PARAMS);
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogSecond = processTaskRelationLogs.get(1);
        processTaskRelationLogSecond.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogSecond.setPreTaskVersion(1);
        processTaskRelationLogSecond.setPostTaskCode(taskCodes.get(1));
        processTaskRelationLogSecond.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogThird = processTaskRelationLogs.get(2);
        processTaskRelationLogThird.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogThird.setPreTaskVersion(1);
        processTaskRelationLogThird.setPostTaskCode(taskCodes.get(2));
        processTaskRelationLogThird.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogFourth = processTaskRelationLogs.get(3);
        processTaskRelationLogFourth.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogFourth.setPreTaskVersion(1);
        processTaskRelationLogFourth.setPostTaskCode(taskCodes.get(3));
        processTaskRelationLogFourth.setPostTaskVersion(1);
        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("switch node");
        taskDefinitionLogFirst.setDescription(
                "The global parameter is to execute TaskA for A, and for B to execute TaskB, otherwise the default task is executed");
        taskDefinitionLogFirst.setTaskParams(
                "{\"localParams\":[],\"rawScript\":\"\",\"resourceList\":[],\"switchResult\":{\"dependTaskList\":[{\"condition\":\"${switchValue} == \\\"A\\\"\",\"nextNode\":"
                        + taskCodeThird + "},{\"condition\":\"${switchValue} == \\\"B\\\"\",\"nextNode\":"
                        + taskCodeFourth + "}],\"nextNode\":" + taskCodeSecond + "}}");
        taskDefinitionLogFirst.setTaskType("SWITCH");

        TaskDefinitionLog taskDefinitionLogSecond = taskDefinitionLogs.get(1);
        taskDefinitionLogSecond.setCode(taskCodes.get(1));
        taskDefinitionLogSecond.setName("default");
        taskDefinitionLogSecond.setDescription("executed default task");
        taskDefinitionLogSecond
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"default\\\"\",\"resourceList\":[]}");
        taskDefinitionLogSecond.setTaskType("SHELL");

        TaskDefinitionLog taskDefinitionLogThird = taskDefinitionLogs.get(2);
        taskDefinitionLogThird.setCode(taskCodes.get(2));
        taskDefinitionLogThird.setName("TaskA");
        taskDefinitionLogThird.setDescription("execute TaskA");
        taskDefinitionLogThird
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"TaskA\\\"\",\"resourceList\":[]}");
        taskDefinitionLogThird.setTaskType("SHELL");

        TaskDefinitionLog taskDefinitionLogFourth = taskDefinitionLogs.get(3);
        taskDefinitionLogFourth.setCode(taskCodes.get(3));
        taskDefinitionLogFourth.setName("TaskB");
        taskDefinitionLogFourth.setDescription("execute TaskB");
        taskDefinitionLogFourth
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"TaskB\\\"\",\"resourceList\":[]}");
        taskDefinitionLogFourth.setTaskType("SHELL");
        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult shellDemo(String token, long projectCode, String tenantCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_shell");
        processDefinitionLog.setDescription("Production, processing and sales of a series of processes");
        processDefinitionLog.setGlobalParams(DemoConstants.SHELL_GLOBAL_PARAMS);
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogSecond = processTaskRelationLogs.get(1);
        processTaskRelationLogSecond.setPreTaskCode(taskCodes.get(0));
        processTaskRelationLogSecond.setPreTaskVersion(1);
        processTaskRelationLogSecond.setPostTaskCode(taskCodes.get(1));
        processTaskRelationLogSecond.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLogThird = processTaskRelationLogs.get(2);
        processTaskRelationLogThird.setPreTaskCode(taskCodes.get(1));
        processTaskRelationLogThird.setPreTaskVersion(1);
        processTaskRelationLogThird.setPostTaskCode(taskCodes.get(2));
        processTaskRelationLogThird.setPostTaskVersion(1);
        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLog.setTaskType("SHELL");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("001");
        taskDefinitionLogFirst.setDescription("Make production order");
        taskDefinitionLogFirst
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"start\\\"\",\"resourceList\":[]}");

        TaskDefinitionLog taskDefinitionLogSecond = taskDefinitionLogs.get(1);
        taskDefinitionLogSecond.setCode(taskCodes.get(1));
        taskDefinitionLogSecond.setName("002");
        taskDefinitionLogSecond.setDescription("Get Information Processing");
        taskDefinitionLogSecond
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo ${resources}\",\"resourceList\":[]}");

        TaskDefinitionLog taskDefinitionLogThird = taskDefinitionLogs.get(2);
        taskDefinitionLogThird.setCode(taskCodes.get(2));
        taskDefinitionLogThird.setName("003");
        taskDefinitionLogThird.setDescription("Sell after completion");
        taskDefinitionLogThird
                .setTaskParams("{\"localParams\":[],\"rawScript\":\"echo \\\"end\\\"\",\"resourceList\":[]}");
        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }
    public ProxyResult subProcessDemo(String token, long projectCode, String tenantCode, String subProcessCode) {

        // get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 1; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("task code get error, ", e);
        }
        String taskCode = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");

        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog();
        processDefinitionLog.setName("demo_sub_process");
        processDefinitionLog.setDescription("Start the production line");
        processDefinitionLog.setGlobalParams("[]");
        processDefinitionLog.setLocations(null);
        processDefinitionLog.setTimeout(0);

        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
            processTaskRelationLog.setName("");
            processTaskRelationLog.setConditionType(NONE);
            processTaskRelationLog.setConditionParams("{}");
            processTaskRelationLogs.add(processTaskRelationLog);
        }
        ProcessTaskRelationLog processTaskRelationLogFirst = processTaskRelationLogs.get(0);
        processTaskRelationLogFirst.setPreTaskCode(0);
        processTaskRelationLogFirst.setPreTaskVersion(0);
        processTaskRelationLogFirst.setPostTaskCode(taskCodes.get(0));
        processTaskRelationLogFirst.setPostTaskVersion(1);

        String taskRelationJson = JSONUtils.toJsonString(processTaskRelationLogs);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
            taskDefinitionLog.setFlag(YES);
            taskDefinitionLog.setDelayTime(0);
            taskDefinitionLog.setEnvironmentCode(-1);
            taskDefinitionLog.setFailRetryInterval(1);
            taskDefinitionLog.setFailRetryTimes(0);
            taskDefinitionLog.setTaskPriority(MEDIUM);
            taskDefinitionLog.setTimeout(0);
            taskDefinitionLog.setTimeoutFlag(TimeoutFlag.CLOSE);
            taskDefinitionLog.setTimeoutNotifyStrategy(null);
            taskDefinitionLog.setWorkerGroup("default");
            taskDefinitionLog.setTaskType("SUB_PROCESS");
            taskDefinitionLogs.add(taskDefinitionLog);
        }
        TaskDefinitionLog taskDefinitionLogFirst = taskDefinitionLogs.get(0);
        taskDefinitionLogFirst.setCode(taskCodes.get(0));
        taskDefinitionLogFirst.setName("subprocess node");
        taskDefinitionLogFirst.setDescription("Enter the demo_shell subnode");
        taskDefinitionLogFirst.setTaskParams(
                "{\"localParams\":[],\"resourceList\":[],\"processDefinitionCode\":" + subProcessCode + "}");

        String taskDefinitionJson = JSONUtils.toJsonString(taskDefinitionLogs);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
                processDefinitionLog.getName(),
                processDefinitionLog.getDescription(),
                processDefinitionLog.getGlobalParams(),
                processDefinitionLog.getLocations(),
                processDefinitionLog.getTimeout(),
                tenantCode,
                taskRelationJson,
                taskDefinitionJson,
                PARALLEL);
        return ProxyResult;
    }

}
