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

package org.apache.dolphinscheduler.api.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_WORKFLOW_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowExecuteResponse;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.executor.workflow.ExecutorClient;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowRequestTransformer;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowRequestTransformer;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ExecutorServiceImpl extends BaseServiceImpl implements ExecutorService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Lazy()
    @Autowired
    private ProcessService processService;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private CommandService commandService;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private TriggerWorkflowRequestTransformer triggerWorkflowRequestTransformer;

    @Autowired
    private TriggerWorkflowDTOValidator triggerWorkflowDTOValidator;

    @Autowired
    private BackfillWorkflowRequestTransformer backfillWorkflowRequestTransformer;

    @Autowired
    private BackfillWorkflowDTOValidator backfillWorkflowDTOValidator;

    @Autowired
    private ExecutorClient executorClient;

    @Override
    @Transactional
    public Integer triggerWorkflowDefinition(final WorkflowTriggerRequest triggerRequest) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(
                triggerRequest.getLoginUser(),
                triggerRequest.getProjectCode(),
                ApiFuncIdentificationConstant.RERUN);
        final TriggerWorkflowDTO triggerWorkflowDTO = triggerWorkflowRequestTransformer.transform(triggerRequest);
        // verify the workflow definition belongs to the URL's project
        if (triggerWorkflowDTO.getWorkflowDefinition() == null
                || triggerWorkflowDTO.getWorkflowDefinition().getProjectCode() != triggerRequest.getProjectCode()) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST,
                    String.valueOf(triggerRequest.getWorkflowDefinitionCode()));
        }
        // todo: use validator chain
        triggerWorkflowDTOValidator.validate(triggerWorkflowDTO);
        return executorClient.triggerWorkflowDefinition().execute(triggerWorkflowDTO);
    }

    @Override
    @Transactional
    public List<Integer> backfillWorkflowDefinition(final WorkflowBackFillRequest workflowBackFillRequest) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(
                workflowBackFillRequest.getLoginUser(),
                workflowBackFillRequest.getProjectCode(),
                ApiFuncIdentificationConstant.RERUN);
        final BackfillWorkflowDTO backfillWorkflowDTO =
                backfillWorkflowRequestTransformer.transform(workflowBackFillRequest);
        // verify the workflow definition belongs to the URL's project
        if (backfillWorkflowDTO.getWorkflowDefinition() == null
                || backfillWorkflowDTO.getWorkflowDefinition().getProjectCode() != workflowBackFillRequest
                        .getProjectCode()) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST,
                    String.valueOf(workflowBackFillRequest.getWorkflowDefinitionCode()));
        }
        backfillWorkflowDTOValidator.validate(backfillWorkflowDTO);
        return executorClient.backfillWorkflowDefinition().execute(backfillWorkflowDTO);
    }

    /**
     * check whether the workflow definition can be executed
     *
     * @param projectCode       project code
     * @param workflowDefinition workflow definition
     */
    @Override
    public void checkWorkflowDefinitionValid(long projectCode, WorkflowDefinition workflowDefinition,
                                             long workflowDefinitionCode, Integer version) {
        // check workflow definition exists
        if (projectCode != workflowDefinition.getProjectCode()) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_EXIST, workflowDefinition.getCode());
        }
        // check workflow definition online
        if (workflowDefinition.getReleaseState() != ReleaseState.ONLINE) {
            throw new ServiceException(Status.WORKFLOW_DEFINITION_NOT_RELEASE, workflowDefinition.getCode(),
                    workflowDefinition.getVersion());
        }
        // check sub workflow definition online
        if (!checkSubWorkflowDefinitionValid(workflowDefinition)) {
            throw new ServiceException(Status.SUB_WORKFLOW_DEFINITION_NOT_RELEASE);
        }
    }

    /**
     * check whether the current workflow has sub workflows and validate all sub workflows
     *
     * @param workflowDefinition
     * @return check result
     */
    @Override
    public boolean checkSubWorkflowDefinitionValid(WorkflowDefinition workflowDefinition) {
        // query all sub workflows under the current workflow
        List<WorkflowTaskRelation> workflowTaskRelations =
                workflowTaskRelationMapper.queryDownstreamByWorkflowDefinitionCode(workflowDefinition.getCode());
        if (workflowTaskRelations.isEmpty()) {
            return true;
        }
        Set<Long> relationCodes =
                workflowTaskRelations.stream().map(WorkflowTaskRelation::getPostTaskCode).collect(Collectors.toSet());
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryByCodeList(relationCodes);

        // find out the workflow definition code
        Set<Long> workflowDefinitionCodeSet = new HashSet<>();
        taskDefinitions.stream()
                .filter(task -> TaskTypeUtils.isSubWorkflowTask(task.getTaskType())).forEach(
                        taskDefinition -> workflowDefinitionCodeSet.add(Long.valueOf(
                                JSONUtils.getNodeString(taskDefinition.getTaskParams(),
                                        CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE))));
        if (workflowDefinitionCodeSet.isEmpty()) {
            return true;
        }

        // check sub releaseState
        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionMapper.queryByCodes(workflowDefinitionCodeSet);
        return workflowDefinitions.stream()
                .filter(definition -> definition.getReleaseState().equals(ReleaseState.OFFLINE))
                .collect(Collectors.toSet())
                .isEmpty();
    }

    @Override
    public void controlWorkflowInstance(User loginUser, Integer workflowInstanceId, ExecuteType executeType) {
        checkNotNull(workflowInstanceId, "workflowInstanceId cannot be null");
        checkNotNull(executeType, "executeType cannot be null");

        WorkflowInstance workflowInstance = workflowInstanceDao
                .queryOptionalById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(Status.WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));

        // check user access for project
        projectService.checkProjectAndAuthThrowException(
                loginUser,
                workflowInstance.getProjectCode(),
                ApiFuncIdentificationConstant.map.get(executeType));

        switch (executeType) {
            case REPEAT_RUNNING:
                executorClient
                        .repeatRunningWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case START_FAILURE_TASK_PROCESS:
                executorClient.recoverFailureTaskInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case RECOVER_SUSPENDED_PROCESS:
                executorClient.recoverSuspendedWorkflowInstanceOperation()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case PAUSE:
                executorClient.pauseWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case STOP:
                executorClient.stopWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            default:
                throw new ServiceException("Unsupported executeType: " + executeType);
        }
    }

    /**
     * do action to execute task in workflow instance
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param workflowInstanceId workflow instance id
     * @param startNodeList     start node list
     * @param taskDependType    task depend type
     * @return execute result code
     */
    @Override
    public WorkflowExecuteResponse executeTask(User loginUser,
                                               long projectCode,
                                               Integer workflowInstanceId,
                                               String startNodeList,
                                               TaskDependType taskDependType) {

        WorkflowExecuteResponse response = new WorkflowExecuteResponse();

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
                ApiFuncIdentificationConstant.map.get(ExecuteType.EXECUTE_TASK));

        WorkflowInstance workflowInstance = processService.findWorkflowInstanceDetailById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(Status.WORKFLOW_INSTANCE_NOT_EXIST, workflowInstanceId));

        if (!workflowInstance.getState().isFinalState()) {
            log.error("Can not execute task for workflow instance which is not finished, workflowInstanceId:{}.",
                    workflowInstanceId);
            putMsg(response, Status.WORKFLOW_INSTANCE_IS_NOT_FINISHED);
            return response;
        }

        WorkflowDefinition workflowDefinition =
                processService.findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        this.checkWorkflowDefinitionValid(projectCode, workflowDefinition, workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion());

        // get the startParams user specified at the first starting while repeat running is needed

        long startNodeListLong;
        try {
            startNodeListLong = Long.parseLong(startNodeList);
        } catch (NumberFormatException e) {
            log.error("startNodeList is not a number");
            putMsg(response, Status.REQUEST_PARAMS_NOT_VALID_ERROR, startNodeList);
            return response;
        }

        if (taskDefinitionLogMapper.queryMaxVersionForDefinition(startNodeListLong) == null) {
            putMsg(response, Status.EXECUTE_NOT_DEFINE_TASK);
            return response;
        }

        // To add startParams only when repeat running is needed
        Map<String, Object> cmdParam = new HashMap<>();
        cmdParam.put(CMD_PARAM_RECOVER_WORKFLOW_ID_STRING, workflowInstanceId);
        // Add StartNodeList
        cmdParam.put(CMD_PARAM_START_NODES, startNodeList);

        Command command = new Command();
        command.setCommandType(CommandType.EXECUTE_TASK);
        command.setWorkflowDefinitionCode(workflowDefinition.getCode());
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(loginUser.getId());
        command.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
        command.setWorkflowInstanceId(workflowInstanceId);

        // Add taskDependType
        command.setTaskDependType(taskDependType);

        if (!commandService.verifyIsNeedCreateCommand(command)) {
            log.warn(
                    "workflow instance is executing the command, workflowDefinitionCode:{}, workflowDefinitionVersion:{}, workflowInstanceId:{}.",
                    workflowDefinition.getCode(), workflowDefinition.getVersion(), workflowInstanceId);
            putMsg(response, Status.WORKFLOW_INSTANCE_EXECUTING_COMMAND,
                    String.valueOf(workflowDefinition.getCode()));
            return response;
        }

        log.info("Creating command, commandInfo:{}.", command);
        int create = commandService.createCommand(command);

        if (create > 0) {
            log.info("Create {} command complete, workflowDefinitionCode:{}, workflowDefinitionVersion:{}.",
                    command.getCommandType().getDescp(), command.getWorkflowDefinitionCode(),
                    workflowDefinition.getVersion());
            putMsg(response, Status.SUCCESS);
        } else {
            log.error(
                    "Execute workflow instance failed because create {} command error, workflowDefinitionCode:{}, workflowDefinitionVersion:{}， workflowInstanceId:{}.",
                    command.getCommandType().getDescp(), command.getWorkflowDefinitionCode(),
                    workflowDefinition.getVersion(),
                    workflowInstanceId);
            putMsg(response, Status.EXECUTE_WORKFLOW_INSTANCE_ERROR);
        }

        return response;
    }

    @Override
    public void forceStartTaskInstance(User loginUser, int queueId) {
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.selectById(queueId);
        // check workflow instance exist
        workflowInstanceDao.queryOptionalById(taskGroupQueue.getWorkflowInstanceId())
                .orElseThrow(
                        () -> new ServiceException(Status.WORKFLOW_INSTANCE_NOT_EXIST,
                                taskGroupQueue.getWorkflowInstanceId()));

        if (taskGroupQueue.getInQueue() == Flag.NO.getCode()) {
            throw new ServiceException(Status.TASK_GROUP_QUEUE_ALREADY_START);
        }
        taskGroupQueue.setForceStart(Flag.YES.getCode());
        taskGroupQueue.setUpdateTime(new Date());
        taskGroupQueueMapper.updateById(taskGroupQueue);
    }

    @Override
    public void execStreamTaskInstance(User loginUser,
                                       long projectCode,
                                       long taskDefinitionCode,
                                       int taskDefinitionVersion,
                                       int warningGroupId,
                                       String workerGroup,
                                       String tenantCode,
                                       Long environmentCode,
                                       Map<String, String> startParams,
                                       int dryRun) {
        throw new ServiceException("Not supported");
    }
}
