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

package org.apache.dolphinscheduler.api.python;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.api.configuration.PythonGatewayConfiguration;
import org.apache.dolphinscheduler.api.dto.EnvironmentDto;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import py4j.GatewayServer;

@Component
public class PythonGateway {
    private static final Logger logger = LoggerFactory.getLogger(PythonGateway.class);

    private static final FailureStrategy DEFAULT_FAILURE_STRATEGY = FailureStrategy.CONTINUE;
    private static final Priority DEFAULT_PRIORITY = Priority.MEDIUM;
    private static final Long DEFAULT_ENVIRONMENT_CODE = -1L;

    private static final TaskDependType DEFAULT_TASK_DEPEND_TYPE = TaskDependType.TASK_POST;
    private static final RunMode DEFAULT_RUN_MODE = RunMode.RUN_MODE_SERIAL;
    private static final int DEFAULT_DRY_RUN = 0;
    private static final int DEFAULT_TEST_FLAG = 0;
    private static final ComplementDependentMode COMPLEMENT_DEPENDENT_MODE = ComplementDependentMode.OFF_MODE;
    // We use admin user's user_id to skip some permission issue from python gateway service
    private static final int ADMIN_USER_ID = 1;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ResourcesService resourceService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private PythonGatewayConfiguration pythonGatewayConfiguration;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    // TODO replace this user to build in admin user if we make sure build in one could not be change
    private final User dummyAdminUser = new User() {
        {
            setId(ADMIN_USER_ID);
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

    public Map<String, Long> getCodeAndVersion(String projectName, String processDefinitionName, String taskName) throws CodeGenerateUtils.CodeGenerateException {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Long> result = new HashMap<>();
        // project do not exists, mean task not exists too, so we should directly return init value
        if (project == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
            return result;
        }

        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(project.getCode(), processDefinitionName);
        // In the case project exists, but current process definition still not created, we should also return the init version of it
        if (processDefinition == null) {
            result.put("code", CodeGenerateUtils.getInstance().genCode());
            result.put("version", 0L);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(), taskName);
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
     * @param userName user name who create or update process definition
     * @param projectName project name which process definition belongs to
     * @param name process definition name
     * @param description description
     * @param globalParams global params
     * @param schedule schedule for process definition, will not set schedule if null,
     * and if would always fresh exists schedule if not null
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param timeout timeout for process definition working, if running time longer than timeout,
     * task will mark as fail
     * @param workerGroup run task in which worker group
     * @param tenantCode tenantCode
     * @param taskRelationJson relation json for nodes
     * @param taskDefinitionJson taskDefinitionJson
     * @param otherParamsJson otherParamsJson handle other params
     * @return create result code
     */
    public Long createOrUpdateProcessDefinition(String userName,
                                                String projectName,
                                                String name,
                                                String description,
                                                String globalParams,
                                                String schedule,
                                                String warningType,
                                                int warningGroupId,
                                                int timeout,
                                                String workerGroup,
                                                String tenantCode,
                                                int releaseState,
                                                String taskRelationJson,
                                                String taskDefinitionJson,
                                                String otherParamsJson,
                                                ProcessExecutionTypeEnum executionType) {
        User user = usersService.queryUser(userName);
        Project project = projectMapper.queryByName(projectName);
        long projectCode = project.getCode();

        ProcessDefinition processDefinition = getProcessDefinition(user, projectCode, name);
        long processDefinitionCode;
        // create or update process definition
        if (processDefinition != null) {
            processDefinitionCode = processDefinition.getCode();
            // make sure process definition offline which could edit
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
            Map<String, Object> result = processDefinitionService.updateProcessDefinition(user, projectCode, name, processDefinitionCode, description, globalParams,
                    null, timeout, tenantCode, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
        } else {
            Map<String, Object> result = processDefinitionService.createProcessDefinition(user, projectCode, name, description, globalParams,
                null, timeout, tenantCode, taskRelationJson, taskDefinitionJson, otherParamsJson, executionType);
            processDefinition = (ProcessDefinition) result.get(Constants.DATA_LIST);
            processDefinitionCode = processDefinition.getCode();
        }

        // Fresh process definition schedule
        if (schedule != null) {
            createOrUpdateSchedule(user, projectCode, processDefinitionCode, schedule, workerGroup, warningType, warningGroupId);
        }
        processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.getEnum(releaseState));
        return processDefinitionCode;
    }

    /**
     * get process definition
     *
     * @param user user who create or update schedule
     * @param projectCode project which process definition belongs to
     * @param processDefinitionName process definition name
     */
    private ProcessDefinition getProcessDefinition(User user, long projectCode, String processDefinitionName) {
        Map<String, Object> verifyProcessDefinitionExists = processDefinitionService.verifyProcessDefinitionName(user, projectCode, processDefinitionName, 0);
        Status verifyStatus = (Status) verifyProcessDefinitionExists.get(Constants.STATUS);

        ProcessDefinition processDefinition = null;
        if (verifyStatus == Status.PROCESS_DEFINITION_NAME_EXIST) {
            processDefinition = processDefinitionMapper.queryByDefineName(projectCode, processDefinitionName);
        } else if (verifyStatus != Status.SUCCESS) {
            String msg = "Verify process definition exists status is invalid, neither SUCCESS or PROCESS_DEFINITION_NAME_EXIST.";
            logger.error(msg);
            throw new RuntimeException(msg);
        }

        return processDefinition;
    }

    /**
     * create or update process definition schedule.
     * It would always use latest schedule define in workflow-as-code, and set schedule online when
     * it's not null
     *
     * @param user user who create or update schedule
     * @param projectCode project which process definition belongs to
     * @param processDefinitionCode process definition code
     * @param schedule schedule expression
     * @param workerGroup work group
     * @param warningType warning type
     * @param warningGroupId warning group id
     */
    private void createOrUpdateSchedule(User user,
                                        long projectCode,
                                        long processDefinitionCode,
                                        String schedule,
                                        String workerGroup,
                                        String warningType,
                                        int warningGroupId) {
        Schedule scheduleObj = scheduleMapper.queryByProcessDefinitionCode(processDefinitionCode);
        // create or update schedule
        int scheduleId;
        if (scheduleObj == null) {
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.ONLINE);
            Map<String, Object> result = schedulerService.insertSchedule(user, projectCode, processDefinitionCode, schedule, WarningType.valueOf(warningType),
                    warningGroupId, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
            scheduleId = (int) result.get("scheduleId");
        } else {
            scheduleId = scheduleObj.getId();
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinitionCode, ReleaseState.OFFLINE);
            schedulerService.updateSchedule(user, projectCode, scheduleId, schedule, WarningType.valueOf(warningType),
                    warningGroupId, DEFAULT_FAILURE_STRATEGY, DEFAULT_PRIORITY, workerGroup, DEFAULT_ENVIRONMENT_CODE);
        }
        schedulerService.setScheduleState(user, projectCode, scheduleId, ReleaseState.ONLINE);
    }

    public void execProcessInstance(String userName,
                                    String projectName,
                                    String processDefinitionName,
                                    String cronTime,
                                    String workerGroup,
                                    String warningType,
                                    Integer warningGroupId,
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
                WarningType.valueOf(warningType),
                warningGroupId,
                DEFAULT_RUN_MODE,
                DEFAULT_PRIORITY,
                workerGroup,
                DEFAULT_ENVIRONMENT_CODE,
                timeout,
                null,
                null,
                DEFAULT_DRY_RUN,
                DEFAULT_TEST_FLAG,
                COMPLEMENT_DEPENDENT_MODE
        );
    }

    // side object
    /*
      Grant project's permission to user. Use when project's created user not current but
      Python API use it to change process definition.
     */
    private Integer grantProjectToUser(Project project, User user) {
        Date now = new Date();
        ProjectUser projectUser = new ProjectUser();
        projectUser.setUserId(user.getId());
        projectUser.setProjectId(project.getId());
        projectUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
        projectUser.setCreateTime(now);
        projectUser.setUpdateTime(now);
        return projectUserMapper.insert(projectUser);
    }

    /*
      Grant or create project. Create a new project if project do not exists, and grant the project
      permission to user if project exists but without permission to this user.
     */
    public void createOrGrantProject(String userName, String name, String desc) {
        User user = usersService.queryUser(userName);

        Project project;
        project = projectMapper.queryByName(name);
        if (project == null) {
            projectService.createProject(user, name, desc);
        } else if (project.getUserId() != user.getId()) {
            ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());
            if (projectUser == null) {
                grantProjectToUser(project, user);
            }
        }
    }

    public Project queryProjectByName(String userName, String projectName) {
        User user = usersService.queryUser(userName);
        return (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
    }

    public void updateProject(String userName, Long projectCode, String projectName, String desc) {
        User user = usersService.queryUser(userName);
        projectService.update(user, projectCode, projectName, desc, userName);
    }

    public void deleteProject(String userName, Long projectCode) {
        User user = usersService.queryUser(userName);
        projectService.deleteProject(user, projectCode);
    }

    public Tenant createTenant(String tenantCode, String desc, String queueName) {
        return tenantService.createTenantIfNotExists(tenantCode, desc, queueName, queueName);
    }

    public Tenant queryTenantByCode(String tenantCode) {
        return (Tenant) tenantService.queryByTenantCode(tenantCode).get(Constants.DATA_LIST);
    }

    public void updateTenant(String userName, int id, String tenantCode, int queueId, String desc) throws Exception {
        User user = usersService.queryUser(userName);
        tenantService.updateTenant(user, id, tenantCode, queueId, desc);
    }

    public void deleteTenantById(String userName, Integer tenantId) throws Exception {
        User user = usersService.queryUser(userName);
        tenantService.deleteTenantById(user, tenantId);
    }

    public User createUser(String userName,
                           String userPassword,
                           String email,
                           String phone,
                           String tenantCode,
                           String queue,
                           int state) throws IOException {
        return usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queue, state);
    }

    public User queryUser(int id) {
        User user = usersService.queryUser(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    public User updateUser(String userName, String userPassword, String email, String phone, String tenantCode, String queue, int state) throws Exception {
        return usersService.createUserIfNotExists(userName, userPassword, email, phone, tenantCode, queue, state);
    }

    public User deleteUser(String userName, int id) throws Exception {
        User user = usersService.queryUser(userName);
        usersService.deleteUserById(user, id);
        return usersService.queryUser(userName);
    }

    /**
     * Get datasource by given datasource name. It return map contain datasource id, type, name.
     * Useful in Python API create sql task which need datasource information.
     *
     * @param datasourceName user who create or update schedule
     */
    public Map<String, Object> getDatasourceInfo(String datasourceName) {
        Map<String, Object> result = new HashMap<>();
        List<DataSource> dataSourceList = dataSourceMapper.queryDataSourceByName(datasourceName);
        if (dataSourceList == null || dataSourceList.isEmpty()) {
            String msg = String.format("Can not find any datasource by name %s", datasourceName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        } else if (dataSourceList.size() > 1) {
            String msg = String.format("Get more than one datasource by name %s", datasourceName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        } else {
            DataSource dataSource = dataSourceList.get(0);
            result.put("id", dataSource.getId());
            result.put("type", dataSource.getType().name());
            result.put("name", dataSource.getName());
        }
        return result;
    }

    /**
     * Get processDefinition by given processDefinitionName name. It return map contain processDefinition id, name, code.
     * Useful in Python API create subProcess task which need processDefinition information.
     *
     * @param userName user who create or update schedule
     * @param projectName project name which process definition belongs to
     * @param processDefinitionName process definition name
     */
    public Map<String, Object> getProcessDefinitionInfo(String userName, String projectName, String processDefinitionName) {
        Map<String, Object> result = new HashMap<>();

        User user = usersService.queryUser(userName);
        Project project = (Project) projectService.queryByName(user, projectName).get(Constants.DATA_LIST);
        long projectCode = project.getCode();
        ProcessDefinition processDefinition = getProcessDefinition(user, projectCode, processDefinitionName);
        // get process definition info
        if (processDefinition != null) {
            // make sure process definition online
            processDefinitionService.releaseProcessDefinition(user, projectCode, processDefinition.getCode(), ReleaseState.ONLINE);
            result.put("id", processDefinition.getId());
            result.put("name", processDefinition.getName());
            result.put("code", processDefinition.getCode());
        } else {
            String msg = String.format("Can not find valid process definition by name %s", processDefinitionName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return result;
    }

    /**
     * Get project, process definition, task code.
     * Useful in Python API create dependent task which need processDefinition information.
     *
     * @param projectName project name which process definition belongs to
     * @param processDefinitionName process definition name
     * @param taskName task name
     */
    public Map<String, Object> getDependentInfo(String projectName, String processDefinitionName, String taskName) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByName(projectName);
        if (project == null) {
            String msg = String.format("Can not find valid project by name %s", projectName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        long projectCode = project.getCode();
        result.put("projectCode", projectCode);

        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineName(projectCode, processDefinitionName);
        if (processDefinition == null) {
            String msg = String.format("Can not find valid process definition by name %s", processDefinitionName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        result.put("processDefinitionCode", processDefinition.getCode());

        if (taskName != null) {
            TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(projectCode, processDefinition.getCode(), taskName);
            result.put("taskDefinitionCode", taskDefinition.getCode());
        }
        return result;
    }

    /**
     * Get resource by given program type and full name. It return map contain resource id, name.
     * Useful in Python API create flink or spark task which need processDefinition information.
     *
     * @param programType program type one of SCALA, JAVA and PYTHON
     * @param fullName full name of the resource
     */
    public Map<String, Object> getResourcesFileInfo(String programType, String fullName) {
        Map<String, Object> result = new HashMap<>();

        Result<Object> resources = resourceService.queryResourceByProgramType(dummyAdminUser, ResourceType.FILE, ProgramType.valueOf(programType));
        List<ResourceComponent> resourcesComponent = (List<ResourceComponent>) resources.getData();
        List<ResourceComponent> namedResources = resourcesComponent.stream().filter(s -> fullName.equals(s.getFullName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(namedResources)) {
            String msg = String.format("Can not find valid resource by program type %s and name %s", programType, fullName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        result.put("id", namedResources.get(0).getId());
        result.put("name", namedResources.get(0).getName());
        return result;
    }

    /**
     * Get environment info by given environment name. It return environment code.
     * Useful in Python API create task which need environment information.
     *
     * @param environmentName name of the environment
     */
    public Long getEnvironmentInfo(String environmentName) {
        Map<String, Object> result = environmentService.queryEnvironmentByName(environmentName);

        if (result.get("data") == null) {
            String msg = String.format("Can not find valid environment by name %s", environmentName);
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        EnvironmentDto environmentDto = EnvironmentDto.class.cast(result.get("data"));
        return environmentDto.getCode();
    }


    /**
     * Get resource by given resource type and full name. It return map contain resource id, name.
     * Useful in Python API create task which need processDefinition information.
     *
     * @param userName user who query resource
     * @param fullName full name of the resource
     */
    public Resource queryResourcesFileInfo(String userName, String fullName) {
        return resourceService.queryResourcesFileInfo(userName, fullName);
    }

    public String getGatewayVersion() {
        return PythonGateway.class.getPackage().getImplementationVersion();
    }

    /**
     * create or update resource.
     * If the folder is not already created, it will be
     *
     * @param userName user who create or update resource
     * @param fullName The fullname of resource.Includes path and suffix.
     * @param description description of resource
     * @param resourceContent content of resource
     * @return id of resource
     */
    public Integer createOrUpdateResource(
            String userName, String fullName, String description, String resourceContent) {
        return resourceService.createOrUpdateResource(userName, fullName, description, resourceContent);
    }

    @PostConstruct
    public void init() {
        if (pythonGatewayConfiguration.getEnabled()) {
            this.start();
        }
    }

    private void start() {
        GatewayServer server;
        try {
            InetAddress gatewayHost = InetAddress.getByName(pythonGatewayConfiguration.getGatewayServerAddress());
            InetAddress pythonHost = InetAddress.getByName(pythonGatewayConfiguration.getPythonAddress());
            server = new GatewayServer(
                this,
                pythonGatewayConfiguration.getGatewayServerPort(),
                pythonGatewayConfiguration.getPythonPort(),
                gatewayHost,
                pythonHost,
                pythonGatewayConfiguration.getConnectTimeout(),
                pythonGatewayConfiguration.getReadTimeout(),
                null
            );
            GatewayServer.turnLoggingOn();
            logger.info("PythonGatewayService started on: " + gatewayHost.toString());
            server.start();
        } catch (UnknownHostException e) {
            logger.error("exception occurred while constructing PythonGatewayService().", e);
        }
    }
}
