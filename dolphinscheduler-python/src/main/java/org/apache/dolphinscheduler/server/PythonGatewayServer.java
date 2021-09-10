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

package org.apache.dolphinscheduler.server;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import py4j.GatewayServer;

@ComponentScan(value = "org.apache.dolphinscheduler", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "org.apache.dolphinscheduler.server.master.*",
        "org.apache.dolphinscheduler.server.worker.*",
        "org.apache.dolphinscheduler.server.monitor.*",
        "org.apache.dolphinscheduler.server.log.*"
    })
})
public class PythonGatewayServer extends SpringBootServletInitializer {
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    // TODO replace this user to build in admin user if we make sure build in one could not be change
    private final User dummyAdminUser = new User() {
        {
            setId(Integer.MAX_VALUE);
            setUserName("dummyUser");
            setUserType(UserType.ADMIN_USER);
        }
    };

    private final Queue queuePythonGateway = new Queue() {
        {
            setId(Integer.MAX_VALUE);
            setQueueName("queuePythonGateway");
        }
    };

    public String ping() {
        return "PONG";
    }

    // TODO Should we import package in python client side? utils package can but service can not, why
    // Core api
    public Map<String, Object> genTaskCodeList(Integer genNum) {
        return taskDefinitionService.genTaskCodeList(genNum);
    }

    public Map<String, Long> getCodeAndVersion(String projectName, String taskName) throws SnowFlakeUtils.SnowFlakeException {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Long> result = new HashMap<>();
        // project do not exists, mean task not exists too, so we should directly return init value
        if (project == null) {
            result.put("code", SnowFlakeUtils.getInstance().nextId());
            result.put("version", 0L);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), taskName);
        if (taskDefinition == null) {
            result.put("code", SnowFlakeUtils.getInstance().nextId());
            result.put("version", 0L);
        } else {
            result.put("code", taskDefinition.getCode());
            result.put("version", (long) taskDefinition.getVersion());
        }
        return result;
    }

    /**
     * create or update process definition.
     * If process definition do not exists in Project=`projectCode` would create a new one
     * If process definition already exists in Project=`projectCode` would update it
     * All requests
     * <p>
     * //     * @param loginUser          login user
     *
     * @param name               process definition name
     * @param description        description
     * @param globalParams       global params
     * @param locations          locations for nodes
     * @param timeout            timeout
     * @param tenantCode         tenantCode
     * @param taskRelationJson   relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @return create result code
     */
    public Long createOrUpdateProcessDefinition(String userName,
                                                String projectName,
                                                String name,
                                                String description,
                                                String globalParams,
                                                String locations,
                                                int timeout,
                                                String tenantCode,
                                                String taskRelationJson,
                                                String taskDefinitionJson) {
        User user = usersService.queryUser(userName);
        Project project = (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
        long projectCode = project.getCode();
        Map<String, Object> verifyProcessDefinitionExists = processDefinitionService.verifyProcessDefinitionName(user, projectCode, name);

        if (verifyProcessDefinitionExists.get(Constants.STATUS) != Status.SUCCESS) {
            // update process definition
            ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, name);
            long processDefinitionCode = processDefinition.getCode();
            // make sure process definition offline which could edit
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
            Map<String, Object> result = processDefinitionService.updateProcessDefinition(user, projectCode, name, processDefinitionCode, description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson);
            return processDefinitionCode;
        } else {
            // create process definition
            Map<String, Object> result = processDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson);
            ProcessDefinition processDefinition = (ProcessDefinition) result.get(Constants.DATA_LIST);
            return processDefinition.getCode();
        }
    }

    public void execProcessInstance(String userName,
                                    String projectName,
                                    String processDefinitionName,
                                    String cronTime,
                                    String workerGroup,
                                    Integer timeout
    ) {
        User user = usersService.queryUser(userName);
        Project project = projectMapper.queryByName(projectName);
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(project.getCode(), processDefinitionName);

        // temp default value
        FailureStrategy failureStrategy = FailureStrategy.CONTINUE;
        TaskDependType taskDependType = TaskDependType.TASK_POST;
        WarningType warningType = WarningType.NONE;
        RunMode runMode = RunMode.RUN_MODE_SERIAL;
        Priority priority = Priority.MEDIUM;
        int warningGroupId = 0;
        Long environmentCode = -1L;
        Map<String, String> startParams = null;
        Integer expectedParallelismNumber = null;
        String startNodeList = null;

        // make sure process definition online
        processDefinitionService.releaseProcessDefinition(user, project.getCode(), processDefinition.getCode(), ReleaseState.ONLINE);

        executorService.execProcessInstance(user,
            project.getCode(),
            processDefinition.getCode(),
            cronTime,
            null,
            failureStrategy,
            startNodeList,
            taskDependType,
            warningType,
            warningGroupId,
            runMode,
            priority,
            workerGroup,
            environmentCode,
            timeout,
            startParams,
            expectedParallelismNumber,
            0
        );
    }

    // side object
    public Map<String, Object> createProject(String userName, String name, String desc) {
        User user = usersService.queryUser(userName);
        return projectService.createProject(user, name, desc);
    }

    public Map<String, Object> createQueue(String name, String queueName) {
        Result<Object> verifyQueueExists = queueService.verifyQueue(name, queueName);
        if (verifyQueueExists.getCode() == 0) {
            return queueService.createQueue(dummyAdminUser, name, queueName);
        } else {
            Map<String, Object> result = new HashMap<>();
            // TODO function putMsg do not work here
            result.put(Constants.STATUS, Status.SUCCESS);
            result.put(Constants.MSG, Status.SUCCESS.getMsg());
            return result;
        }
    }

    public Map<String, Object> createTenant(String tenantCode, String desc, String queueName) throws Exception {
        if (tenantService.checkTenantExists(tenantCode)) {
            Map<String, Object> result = new HashMap<>();
            // TODO function putMsg do not work here
            result.put(Constants.STATUS, Status.SUCCESS);
            result.put(Constants.MSG, Status.SUCCESS.getMsg());
            return result;
        } else {
            Result<Object> verifyQueueExists = queueService.verifyQueue(queueName, queueName);
            if (verifyQueueExists.getCode() == 0) {
                // TODO why create do not return id?
                queueService.createQueue(dummyAdminUser, queueName, queueName);
            }
            Map<String, Object> result = queueService.queryQueueName(queueName);
            List<Queue> queueList = (List<Queue>) result.get(Constants.DATA_LIST);
            Queue queue = queueList.get(0);
            return tenantService.createTenant(dummyAdminUser, tenantCode, queue.getId(), desc);
        }
    }

    public void createUser(String userName,
                           String userPassword,
                           String email,
                           String phone,
                           String tenantCode,
                           String queue,
                           int state) {
        User user = usersService.queryUser(userName);
        if (Objects.isNull(user)) {
            Map<String, Object> tenantResult = tenantService.queryByTenantCode(tenantCode);
            Tenant tenant = (Tenant) tenantResult.get(Constants.DATA_LIST);
            usersService.createUser(userName, userPassword, email, tenant.getId(), phone, queue, state);
        }
    }

    @PostConstruct
    public void run() {
        GatewayServer server = new GatewayServer(this);
        GatewayServer.turnLoggingOn();
        // Start server to accept python client RPC
        server.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(PythonGatewayServer.class, args);
    }
}
