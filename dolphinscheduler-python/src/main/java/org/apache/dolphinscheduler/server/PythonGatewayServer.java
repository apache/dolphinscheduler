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
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        "org.apache.dolphinscheduler.server.log.*",
        "org.apache.dolphinscheduler.alert.*"
    })
})
public class PythonGatewayServer extends SpringBootServletInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonGatewayServer.class);
    
    private static final WarningType DEFAULT_WARNING_TYPE = WarningType.NONE;
    private static final int DEFAULT_WARNING_GROUP_ID = 0;
    private static final FailureStrategy DEFAULT_FAILURE_STRATEGY = FailureStrategy.CONTINUE;
    private static final Priority DEFAULT_PRIORITY = Priority.MEDIUM;
    private static final Long DEFAULT_ENVIRONMENT_CODE = -1L;

    private static final TaskDependType DEFAULT_TASK_DEPEND_TYPE = TaskDependType.TASK_POST;
    private static final RunMode DEFAULT_RUN_MODE = RunMode.RUN_MODE_SERIAL;
    private static final int DEFAULT_DRY_RUN = 0;

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

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ScheduleMapper scheduleMapper;

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

    public Map<String, Long> getCodeAndVersion(String projectName, String taskName) throws CodeGenerateUtils.CodeGenerateException {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Long> result = new HashMap<>();
        // project do not exists, mean task not exists too, so we should directly return init value
        if (project == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), taskName);
        if (taskDefinition == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
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
     *
     * @param userName           user name who create or update process definition
     * @param projectName        project name which process definition belongs to
     * @param name               process definition name
     * @param description        description
     * @param globalParams       global params
     * @param schedule           schedule for process definition, will not set schedule if null,
     *                           and if would always fresh exists schedule if not null
     * @param locations          locations json object about all tasks
     * @param timeout            timeout for process definition working, if running time longer than timeout,
     *                           task will mark as fail
     * @param workerGroup        run task in which worker group
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
                                                String schedule,
                                                String locations,
                                                int timeout,
                                                String workerGroup,
                                                String tenantCode,
                                                String taskRelationJson,
                                                String taskDefinitionJson,
                                                ProcessExecutionTypeEnum executionType) {
        User user = usersService.queryUser(userName);
        Project project = (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
        long projectCode = project.getCode();
        Map<String, Object> verifyProcessDefinitionExists = processDefinitionService.verifyProcessDefinitionName(user, projectCode, name);
        Status verifyStatus = (Status) verifyProcessDefinitionExists.get(Constants.STATUS);

        long processDefinitionCode;
        // create or update process definition
        if (verifyStatus == Status.PROCESS_DEFINITION_NAME_EXIST) {
            ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, name);
            processDefinitionCode = processDefinition.getCode();
            // make sure process definition offline which could edit
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
            Map<String, Object> result = processDefinitionService.updateProcessDefinition(user, projectCode, name, processDefinitionCode, description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson,executionType);
        } else if (verifyStatus == Status.SUCCESS) {
            Map<String, Object> result = processDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams,
                locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson,executionType);
            ProcessDefinition processDefinition = (ProcessDefinition) result.get(Constants.DATA_LIST);
            processDefinitionCode = processDefinition.getCode();
        } else {
            String msg = "Verify process definition exists status is invalid, neither SUCCESS or PROCESS_DEFINITION_NAME_EXIST.";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        
        // Fresh process definition schedule 
        if (schedule != null) {
            createOrUpdateSchedule(user, projectCode, processDefinitionCode, schedule, workerGroup);
        }
        processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.ONLINE);
        return processDefinitionCode;
    }

    /**
     * create or update process definition schedule.
     * It would always use latest schedule define in workflow-as-code, and set schedule online when
     * it's not null
     *
     * @param user                  user who create or update schedule
     * @param projectCode           project which process definition belongs to
     * @param processDefinitionCode process definition code
     * @param schedule              schedule expression
     * @param workerGroup           work group
     */
    private void createOrUpdateSchedule(User user,
                                        long projectCode,
                                        long processDefinitionCode,
                                        String schedule,
                                        String workerGroup) {
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionCode(processDefinitionCode);
        // create or update schedule
        int scheduleId;
        if (schedules.isEmpty()) {
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.ONLINE);
            Map<String, Object> result = schedulerService.insertSchedule(user, projectCode, processDefinitionCode, schedule, DEFAULT_WARNING_TYPE,
                DEFAULT_WARNING_GROUP_ID, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
            scheduleId = (int) result.get("scheduleId");
        } else {
            scheduleId = schedules.get(0).getId();
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
            schedulerService.updateSchedule(user, projectCode, scheduleId, schedule, DEFAULT_WARNING_TYPE,
                DEFAULT_WARNING_GROUP_ID, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
        }
        schedulerService.setScheduleState(user, projectCode, scheduleId, ReleaseState.ONLINE);
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

        // make sure process definition online
        processDefinitionService.releaseProcessDefinition(user, project.getCode(), processDefinition.getCode(), ReleaseState.ONLINE);

        executorService.execProcessInstance(user,
            project.getCode(),
            processDefinition.getCode(),
            cronTime,
            null,
            DEFAULT_FAILURE_STRATEGY,
            null,
            DEFAULT_TASK_DEPEND_TYPE,
            DEFAULT_WARNING_TYPE,
            DEFAULT_WARNING_GROUP_ID,
            DEFAULT_RUN_MODE,
            DEFAULT_PRIORITY,
            workerGroup,
            DEFAULT_ENVIRONMENT_CODE,
            timeout,
            null,
            null,
            DEFAULT_DRY_RUN
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
