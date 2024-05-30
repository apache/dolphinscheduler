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

package org.apache.dolphinscheduler.server.master.runner;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskCacheUtils;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostResponse;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.event.StateEventHandleError;
import org.apache.dolphinscheduler.server.master.event.StateEventHandleException;
import org.apache.dolphinscheduler.server.master.event.StateEventHandler;
import org.apache.dolphinscheduler.server.master.event.StateEventHandlerManager;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowStateEvent;
import org.apache.dolphinscheduler.server.master.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.server.master.runner.taskgroup.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.utils.TaskUtils;
import org.apache.dolphinscheduler.server.master.utils.WorkflowInstanceUtils;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.StandByTaskInstancePriorityQueue;
import org.apache.dolphinscheduler.service.utils.DagHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Workflow execute task, used to execute a workflow instance.
 */
@Slf4j
public class WorkflowExecuteRunnable implements IWorkflowExecuteRunnable {

    private final ProcessService processService;

    private final CommandService commandService;

    private final ProcessInstanceDao processInstanceDao;

    private final TaskInstanceDao taskInstanceDao;

    private final ProcessAlertManager processAlertManager;

    private final IWorkflowExecuteContext workflowExecuteContext;

    private WorkflowRunnableStatus workflowRunnableStatus = WorkflowRunnableStatus.CREATED;

    /**
     * submit failure nodes
     */
    private boolean taskFailedSubmit = false;

    /**
     * task instance hash map, taskId as key
     */
    private final Map<Integer, TaskInstance> taskInstanceMap = new ConcurrentHashMap<>();

    /**
     * task instance hash map, taskCode as key
     */
    private final Map<Long, TaskInstance> taskCodeInstanceMap = new ConcurrentHashMap<>();

    /**
     * TaskCode as Key, TaskExecuteRunnable as Value
     */
    private final Map<Long, DefaultTaskExecuteRunnable> taskExecuteRunnableMap = new ConcurrentHashMap<>();

    /**
     * valid task map, taskCode as key, taskId as value
     * in a DAG, only one taskInstance per taskCode is valid
     */
    private final Map<Long, Integer> validTaskMap = new ConcurrentHashMap<>();

    /**
     * error task map, taskCode as key, taskInstanceId as value
     * in a DAG, only one taskInstance per taskCode is valid
     */
    private final Map<Long, Integer> errorTaskMap = new ConcurrentHashMap<>();

    /**
     * complete task set
     * in a DAG, only one taskInstance per taskCode is valid
     */
    private final Set<Long> completeTaskSet = Sets.newConcurrentHashSet();

    /**
     * depend failed task set
     */
    private final Set<Long> dependFailedTaskSet = Sets.newConcurrentHashSet();

    /**
     * todo: remove this field
     * skip task map, code as key
     */
    private final Map<Long, TaskNode> skipTaskNodeMap = new ConcurrentHashMap<>();

    /**
     * complement date list
     */
    private List<Date> complementListDate = Lists.newLinkedList();

    /**
     * state event queue
     */
    private final ConcurrentLinkedQueue<StateEvent> stateEvents = new ConcurrentLinkedQueue<>();

    /**
     * The StandBy task list, will be executed, need to know, the taskInstance in this queue may doesn't have id.
     */
    private final StandByTaskInstancePriorityQueue standByTaskInstancePriorityQueue =
            new StandByTaskInstancePriorityQueue();

    /**
     * wait to retry taskInstance map, taskCode as key, taskInstance as value
     * before retry, the taskInstance id is 0
     */
    private final Map<Long, TaskInstance> waitToRetryTaskInstanceMap = new ConcurrentHashMap<>();

    private final StateWheelExecuteThread stateWheelExecuteThread;

    private final CuringParamsService curingParamsService;

    private final DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory;

    private final MasterConfig masterConfig;

    private final ListenerEventAlertManager listenerEventAlertManager;

    private final TaskGroupCoordinator taskGroupCoordinator;

    public WorkflowExecuteRunnable(
                                   @NonNull IWorkflowExecuteContext workflowExecuteContext,
                                   @NonNull CommandService commandService,
                                   @NonNull ProcessService processService,
                                   @NonNull ProcessInstanceDao processInstanceDao,
                                   @NonNull ProcessAlertManager processAlertManager,
                                   @NonNull MasterConfig masterConfig,
                                   @NonNull StateWheelExecuteThread stateWheelExecuteThread,
                                   @NonNull CuringParamsService curingParamsService,
                                   @NonNull TaskInstanceDao taskInstanceDao,
                                   @NonNull DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory,
                                   @NonNull ListenerEventAlertManager listenerEventAlertManager,
                                   @NonNull TaskGroupCoordinator taskGroupCoordinator) {
        this.processService = processService;
        this.commandService = commandService;
        this.processInstanceDao = processInstanceDao;
        this.workflowExecuteContext = workflowExecuteContext;
        this.masterConfig = masterConfig;
        this.processAlertManager = processAlertManager;
        this.stateWheelExecuteThread = stateWheelExecuteThread;
        this.curingParamsService = curingParamsService;
        this.taskInstanceDao = taskInstanceDao;
        this.defaultTaskExecuteRunnableFactory = defaultTaskExecuteRunnableFactory;
        this.listenerEventAlertManager = listenerEventAlertManager;
        this.taskGroupCoordinator = taskGroupCoordinator;
        TaskMetrics.registerTaskPrepared(standByTaskInstancePriorityQueue::size);
    }

    /**
     * the process start nodes are submitted completely.
     */
    public boolean isStart() {
        return WorkflowRunnableStatus.STARTED == workflowRunnableStatus;
    }

    /**
     * handle event
     */
    public void handleEvents() {
        if (!isStart()) {
            log.info(
                    "The workflow instance is not started, will not handle its state event, current state event size: {}",
                    stateEvents);
            return;
        }
        int loopTimes = stateEvents.size() * 2;
        for (int i = 0; i < loopTimes; i++) {
            final StateEvent stateEvent = this.stateEvents.peek();
            if (stateEvent == null) {
                return;
            }
            try {
                LogUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(),
                        stateEvent.getTaskInstanceId());
                // if state handle success then will remove this state, otherwise will retry this state next time.
                // The state should always handle success except database error.
                checkProcessInstance(stateEvent);

                StateEventHandler stateEventHandler =
                        StateEventHandlerManager.getStateEventHandler(stateEvent.getType())
                                .orElseThrow(() -> new StateEventHandleError(
                                        "Cannot find handler for the given state event"));
                log.info("Begin to handle state event, {}", stateEvent);
                if (stateEventHandler.handleStateEvent(this, stateEvent)) {
                    this.stateEvents.remove(stateEvent);
                }
            } catch (StateEventHandleError stateEventHandleError) {
                log.error("State event handle error, will remove this event: {}", stateEvent,
                        stateEventHandleError);
                this.stateEvents.remove(stateEvent);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (StateEventHandleException stateEventHandleException) {
                log.error("State event handle error, will retry this event: {}",
                        stateEvent,
                        stateEventHandleException);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                // we catch the exception here, since if the state event handle failed, the state event will still
                // keep
                // in the stateEvents queue.
                log.error("State event handle error, get a unknown exception, will retry this event: {}",
                        stateEvent,
                        e);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } finally {
                LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
    }

    public IWorkflowExecuteContext getWorkflowExecuteContext() {
        return workflowExecuteContext;
    }

    public boolean addStateEvent(StateEvent stateEvent) {
        if (workflowExecuteContext.getWorkflowInstance().getId() != stateEvent.getProcessInstanceId()) {
            log.info("state event would be abounded :{}", stateEvent);
            return false;
        }
        this.stateEvents.add(stateEvent);
        return true;
    }

    public int eventSize() {
        return this.stateEvents.size();
    }

    public void processStart() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        this.listenerEventAlertManager.publishProcessStartListenerEvent(workflowInstance, projectUser);
    }

    public void taskStart(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        this.listenerEventAlertManager.publishTaskStartListenerEvent(workflowInstance, taskInstance, projectUser);
    }

    public void processTimeout() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        this.processAlertManager.sendProcessTimeoutAlert(workflowInstance, projectUser);
    }

    public void taskTimeout(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        processAlertManager.sendTaskTimeoutAlert(workflowInstance, taskInstance, projectUser);
    }

    public void taskFinished(TaskInstance taskInstance) throws StateEventHandleException {
        log.info("TaskInstance finished task code:{} state:{}", taskInstance.getTaskCode(), taskInstance.getState());
        try {
            ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
            // Do we need to remove?
            taskExecuteRunnableMap.remove(taskInstance.getTaskCode());
            stateWheelExecuteThread.removeTask4TimeoutCheck(workflowInstance, taskInstance);
            stateWheelExecuteThread.removeTask4RetryCheck(workflowInstance, taskInstance);
            if (taskInstance.getTaskGroupId() > 0) {
                releaseTaskGroupIfNeeded(taskInstance);
                log.info("Release task Group slot: {}  for taskInstance: {} ", taskInstance.getTaskGroupId(),
                        taskInstance.getId());
            }

            if (taskInstance.getState().isSuccess()) {
                completeTaskSet.add(taskInstance.getTaskCode());
                workflowInstance.setVarPool(VarPoolUtils.mergeVarPoolJsonString(
                        Lists.newArrayList(workflowInstance.getVarPool(), taskInstance.getVarPool())));
                processInstanceDao.upsertProcessInstance(workflowInstance);
                ProjectUser projectUser =
                        processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
                listenerEventAlertManager.publishTaskEndListenerEvent(workflowInstance, taskInstance, projectUser);
                // save the cacheKey only if the task is defined as cache task and the task is success
                if (taskInstance.getIsCache().equals(Flag.YES)) {
                    saveCacheTaskInstance(taskInstance);
                }
                if (!workflowInstance.isBlocked()) {
                    submitPostNode(taskInstance.getTaskCode());
                }
            } else if (taskInstance.taskCanRetry() && !workflowInstance.getState().isReadyStop()) {
                // retry task
                log.info("Retry taskInstance taskInstance state: {}", taskInstance.getState());
                retryTaskInstance(taskInstance);
            } else if (taskInstance.getState().isFailure()) {
                completeTaskSet.add(taskInstance.getTaskCode());
                ProjectUser projectUser =
                        processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
                listenerEventAlertManager.publishTaskFailListenerEvent(workflowInstance, taskInstance, projectUser);
                // There are child nodes and the failure policy is: CONTINUE
                if (workflowInstance.getFailureStrategy() == FailureStrategy.CONTINUE && DagHelper.haveAllNodeAfterNode(
                        taskInstance.getTaskCode(),
                        workflowExecuteContext.getWorkflowGraph().getDag())) {
                    submitPostNode(taskInstance.getTaskCode());
                } else {
                    errorTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
                    if (workflowInstance.getFailureStrategy() == FailureStrategy.END) {
                        killAllTasks();
                    }
                }
            } else if (taskInstance.getState().isFinished()) {
                // todo: when the task instance type is pause, then it should not in completeTaskSet
                completeTaskSet.add(taskInstance.getTaskCode());
            }
            log.info("TaskInstance finished will try to update the workflow instance state, task code:{} state:{}",
                    taskInstance.getTaskCode(),
                    taskInstance.getState());
            this.updateProcessInstanceState();
            // log the taskInstance in detail after task is finished
            log.info(WorkflowInstanceUtils.logTaskInstanceInDetail(taskInstance));
            sendTaskLogOnMasterToRemoteIfNeeded(taskInstance);
        } catch (Exception ex) {
            log.error("Task finish failed, get a exception, will remove this taskInstance from completeTaskSet", ex);
            // remove the task from complete map, so that we can finish in the next time.
            completeTaskSet.remove(taskInstance.getTaskCode());
            throw ex;
        }
    }

    private void releaseTaskGroupIfNeeded(TaskInstance taskInstance) {
        // todo: use Integer
        if (taskInstance.getTaskGroupId() <= 0) {
            log.info("The current TaskInstance: {} doesn't use taskGroup, no need to release taskGroup",
                    taskInstance.getName());
            return;
        }
        taskGroupCoordinator.releaseTaskGroupSlot(taskInstance);
        log.info("Success release task Group slot: {}  for taskInstance: {} ", taskInstance.getTaskGroupId(),
                taskInstance.getName());
    }

    /**
     * crate new task instance to retry, different objects from the original
     */
    private void retryTaskInstance(TaskInstance taskInstance) throws StateEventHandleException {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (!taskInstance.taskCanRetry()) {
            return;
        }
        TaskInstance newTaskInstance = cloneRetryTaskInstance(taskInstance);
        if (newTaskInstance == null) {
            log.error("Retry task fail because new taskInstance is null, task code:{}, task id:{}",
                    taskInstance.getTaskCode(),
                    taskInstance.getId());
            return;
        }
        waitToRetryTaskInstanceMap.put(newTaskInstance.getTaskCode(), newTaskInstance);
        if (!taskInstance.retryTaskIntervalOverTime()) {
            log.info(
                    "Failure task will be submitted, process id: {}, task instance code: {}, state: {}, retry times: {} / {}, interval: {}",
                    workflowInstance.getId(), newTaskInstance.getTaskCode(),
                    newTaskInstance.getState(), newTaskInstance.getRetryTimes(), newTaskInstance.getMaxRetryTimes(),
                    newTaskInstance.getRetryInterval());
            stateWheelExecuteThread.addTask4TimeoutCheck(workflowInstance, newTaskInstance);
            stateWheelExecuteThread.addTask4RetryCheck(workflowInstance, newTaskInstance);
        } else {
            addTaskToStandByList(newTaskInstance);
            submitStandByTask();
            waitToRetryTaskInstanceMap.remove(newTaskInstance.getTaskCode());
        }
    }

    // todo: remove this method, it's not a good practice to expose method to reload the workflow instance from db.
    // all the update method should use RPC
    public void refreshProcessInstance(int processInstanceId) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProcessDefinition workflowDefinition = workflowExecuteContext.getWorkflowDefinition();

        log.info("process instance update: {}", processInstanceId);
        ProcessInstance newProcessInstance = processService.findProcessInstanceById(processInstanceId);
        // just update the processInstance field(this is soft copy)
        BeanUtils.copyProperties(newProcessInstance, workflowInstance);

        ProcessDefinition newWorkflowDefinition = processService.findProcessDefinition(
                workflowInstance.getProcessDefinitionCode(), workflowInstance.getProcessDefinitionVersion());
        workflowInstance.setProcessDefinition(workflowDefinition);

        // just update the processInstance field(this is soft copy)
        BeanUtils.copyProperties(newWorkflowDefinition, workflowDefinition);
    }

    /**
     * update task instance
     */
    public void refreshTaskInstance(int taskInstanceId) {
        log.info("task instance update: {} ", taskInstanceId);
        TaskInstance taskInstance = taskInstanceDao.queryById(taskInstanceId);
        if (taskInstance == null) {
            log.error("can not find task instance, id:{}", taskInstanceId);
            return;
        }
        processService.packageTaskInstance(taskInstance, workflowExecuteContext.getWorkflowInstance());
        taskInstanceMap.put(taskInstance.getId(), taskInstance);
        taskCodeInstanceMap.put(taskInstance.getTaskCode(), taskInstance);

        validTaskMap.remove(taskInstance.getTaskCode());
        if (Flag.YES == taskInstance.getFlag()) {
            validTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
        }
    }

    /**
     * check process instance by state event
     */
    public void checkProcessInstance(StateEvent stateEvent) throws StateEventHandleError {
        if (workflowExecuteContext.getWorkflowInstance().getId() != stateEvent.getProcessInstanceId()) {
            throw new StateEventHandleError("The event doesn't contains process instance id");
        }
    }

    /**
     * check if task instance exist by state event
     */
    public void checkTaskInstanceByStateEvent(TaskStateEvent stateEvent) throws StateEventHandleError {
        if (stateEvent.getTaskInstanceId() == null || stateEvent.getTaskInstanceId() == 0) {
            throw new StateEventHandleError("The taskInstanceId is 0");
        }

        if (!taskInstanceMap.containsKey(stateEvent.getTaskInstanceId())) {
            throw new StateEventHandleError("Cannot find the taskInstance from taskInstanceMap");
        }
    }

    /**
     * get task instance from memory
     */
    public Optional<TaskInstance> getTaskInstance(int taskInstanceId) {
        return Optional.ofNullable(taskInstanceMap.get(taskInstanceId));
    }

    public Optional<TaskInstance> getTaskInstance(long taskCode) {
        return Optional.ofNullable(taskCodeInstanceMap.get(taskCode));
    }

    public Optional<TaskInstance> getActiveTaskInstanceByTaskCode(long taskCode) {
        Integer taskInstanceId = validTaskMap.get(taskCode);
        if (taskInstanceId != null) {
            return Optional.ofNullable(taskInstanceMap.get(taskInstanceId));
        }
        return Optional.empty();
    }

    public Optional<TaskInstance> getRetryTaskInstanceByTaskCode(long taskCode) {
        if (waitToRetryTaskInstanceMap.containsKey(taskCode)) {
            return Optional.ofNullable(waitToRetryTaskInstanceMap.get(taskCode));
        }
        return Optional.empty();
    }

    public void processBlock() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        processAlertManager.sendProcessBlockingAlert(workflowInstance, projectUser);
        log.info("processInstance {} block alert send successful!", workflowInstance.getId());
    }

    public boolean processComplementData() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (!needComplementProcess()) {
            return false;
        }

        // when the serial complement is executed, the next complement instance is created,
        // and this method does not need to be executed when the parallel complement is used.
        if (workflowInstance.getState().isReadyStop() || !workflowInstance.getState().isFinished()) {
            return false;
        }

        Date scheduleDate = workflowInstance.getScheduleTime();
        if (scheduleDate == null) {
            if (CollectionUtils.isEmpty(complementListDate)) {
                log.info("complementListDate is empty, process complement end. process id:{}",
                        workflowInstance.getId());

                return true;
            }
            scheduleDate = complementListDate.get(0);
        } else if (workflowInstance.getState().isFinished()) {
            endProcess();
            if (complementListDate.isEmpty()) {
                log.info("process complement end. process id:{}", workflowInstance.getId());
                return true;
            }
            int index = complementListDate.indexOf(scheduleDate);
            if (index >= complementListDate.size() - 1 || !workflowInstance.getState().isSuccess()) {
                log.info("process complement end. process id:{}", workflowInstance.getId());
                // complement data ends || no success
                return true;
            }
            log.info("process complement continue. process id:{}, schedule time:{} complementListDate:{}",
                    workflowInstance.getId(), workflowInstance.getScheduleTime(), complementListDate);
            scheduleDate = complementListDate.get(index + 1);
        }
        // the next process complement
        int create = this.createComplementDataCommand(scheduleDate);
        if (create > 0) {
            log.info("create complement data command successfully.");
        }
        return true;
    }

    private int createComplementDataCommand(Date scheduleDate) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();

        Command command = new Command();
        command.setScheduleTime(scheduleDate);
        command.setCommandType(CommandType.COMPLEMENT_DATA);
        command.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        Map<String, String> cmdParam = JSONUtils.toMap(workflowInstance.getCommandParam());
        if (cmdParam.containsKey(CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            cmdParam.remove(CMD_PARAM_RECOVERY_START_NODE_STRING);
        }

        if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            cmdParam.replace(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST,
                    cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)
                            .substring(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).indexOf(COMMA) + 1));
        }

        if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE)) {
            cmdParam.replace(CMD_PARAM_COMPLEMENT_DATA_START_DATE,
                    DateUtils.format(scheduleDate, YYYY_MM_DD_HH_MM_SS, null));
        }
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setTaskDependType(workflowInstance.getTaskDependType());
        command.setFailureStrategy(workflowInstance.getFailureStrategy());
        command.setWarningType(workflowInstance.getWarningType());
        command.setWarningGroupId(workflowInstance.getWarningGroupId());
        command.setStartTime(new Date());
        command.setExecutorId(workflowInstance.getExecutorId());
        command.setUpdateTime(new Date());
        command.setProcessInstancePriority(workflowInstance.getProcessInstancePriority());
        command.setWorkerGroup(workflowInstance.getWorkerGroup());
        command.setEnvironmentCode(workflowInstance.getEnvironmentCode());
        command.setDryRun(workflowInstance.getDryRun());
        command.setProcessInstanceId(0);
        command.setProcessDefinitionVersion(workflowInstance.getProcessDefinitionVersion());
        command.setTestFlag(workflowInstance.getTestFlag());
        command.setTenantCode(workflowInstance.getTenantCode());
        int create = commandService.createCommand(command);
        processService.saveCommandTrigger(command.getId(), workflowInstance.getId());
        return create;
    }

    private boolean needComplementProcess() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        return workflowInstance.isComplementData() && Flag.NO == workflowInstance.getIsSubProcess();
    }

    /**
     * ProcessInstance start entrypoint.
     */
    @Override
    public WorkflowStartStatus startWorkflow() {

        try {
            ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
            LogUtils.setWorkflowInstanceIdMDC(workflowInstance.getId());
            if (isStart()) {
                // This case should not been happened
                log.warn("The workflow has already been started, current state: {}", workflowRunnableStatus);
                return WorkflowStartStatus.DUPLICATED_SUBMITTED;
            }
            if (workflowRunnableStatus == WorkflowRunnableStatus.CREATED) {
                initTaskQueue();
                workflowRunnableStatus = WorkflowRunnableStatus.INITIALIZE_QUEUE;
                log.info("workflowStatue changed to :{}", workflowRunnableStatus);
            }
            if (workflowRunnableStatus == WorkflowRunnableStatus.INITIALIZE_QUEUE) {
                processStart();
                submitPostNode(null);
                workflowRunnableStatus = WorkflowRunnableStatus.STARTED;
                log.info("workflowStatue changed to :{}", workflowRunnableStatus);
            }
            return WorkflowStartStatus.SUCCESS;
        } catch (Exception e) {
            log.error("Start workflow error", e);
            return WorkflowStartStatus.FAILED;
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    /**
     * process end handle
     */
    public void endProcess() {
        this.stateEvents.clear();
        ProcessDefinition workflowDefinition = workflowExecuteContext.getWorkflowDefinition();
        if (workflowDefinition.getExecutionType().typeIsSerialWait() || workflowDefinition.getExecutionType()
                .typeIsSerialPriority()) {
            checkSerialProcess(workflowDefinition);
        }
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(workflowInstance.getId());
        processAlertManager.sendAlertProcessInstance(workflowInstance, getValidTaskList(), projectUser);
        if (workflowInstance.getState().isSuccess()) {
            processAlertManager.closeAlert(workflowInstance);
            listenerEventAlertManager.publishProcessEndListenerEvent(workflowInstance, projectUser);
        } else {
            listenerEventAlertManager.publishProcessFailListenerEvent(workflowInstance, projectUser);
        }
        taskInstanceMap.forEach((id, taskInstance) -> {
            if (taskInstance != null && taskInstance.getTaskGroupId() > 0) {
                releaseTaskGroupIfNeeded(taskInstance);
            }
        });
        // Log the workflowInstance in detail
        log.info(WorkflowInstanceUtils.logWorkflowInstanceInDetails(workflowInstance));
    }

    public void checkSerialProcess(ProcessDefinition processDefinition) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();

        int nextInstanceId = workflowInstance.getNextProcessInstanceId();
        if (nextInstanceId == 0) {
            ProcessInstance nextProcessInstance =
                    this.processService.loadNextProcess4Serial(workflowInstance.getProcessDefinition().getCode(),
                            WorkflowExecutionStatus.SERIAL_WAIT.getCode(), workflowInstance.getId());
            if (nextProcessInstance == null) {
                return;
            }
            ProcessInstance nextReadyStopProcessInstance =
                    this.processService.loadNextProcess4Serial(workflowInstance.getProcessDefinition().getCode(),
                            WorkflowExecutionStatus.READY_STOP.getCode(), workflowInstance.getId());
            if (processDefinition.getExecutionType().typeIsSerialPriority() && nextReadyStopProcessInstance != null) {
                return;
            }
            nextInstanceId = nextProcessInstance.getId();
        }
        ProcessInstance nextProcessInstance = this.processService.findProcessInstanceById(nextInstanceId);
        if (nextProcessInstance.getState().isFinished() || nextProcessInstance.getState().isRunning()) {
            return;
        }
        Map<String, Object> cmdParam = new HashMap<>();
        // write the parameters of the nextProcessInstance to command
        if (StringUtils.isNotEmpty(nextProcessInstance.getCommandParam())) {
            Map<String, String> commandStartParamsMap = JSONUtils.toMap(nextProcessInstance.getCommandParam());
            if (MapUtils.isNotEmpty(commandStartParamsMap)) {
                Map<String, String> paramsMap = JSONUtils.toMap(commandStartParamsMap.get(CMD_PARAM_START_PARAMS));
                if (MapUtils.isNotEmpty(paramsMap)) {
                    cmdParam.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(paramsMap));
                }
            }
        }
        cmdParam.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, nextInstanceId);
        Command command = new Command();
        command.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command.setProcessInstanceId(nextProcessInstance.getId());
        command.setProcessDefinitionCode(processDefinition.getCode());
        command.setProcessDefinitionVersion(processDefinition.getVersion());
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        commandService.createCommand(command);
    }

    private void initTaskQueue() throws StateEventHandleException, CronParseException {

        taskFailedSubmit = false;
        // do we need to clear?
        taskExecuteRunnableMap.clear();
        dependFailedTaskSet.clear();
        completeTaskSet.clear();
        errorTaskMap.clear();

        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        ProcessDefinition workflowDefinition = workflowExecuteContext.getWorkflowDefinition();

        if (!isNewProcessInstance()) {
            log.info("The workflowInstance is not a newly running instance, runtimes: {}, recover flag: {}",
                    workflowInstance.getRunTimes(),
                    workflowInstance.getRecovery());
            List<TaskInstance> validTaskInstanceList =
                    taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                            workflowInstance.getTestFlag());
            for (TaskInstance task : validTaskInstanceList) {
                try {
                    LogUtils.setWorkflowAndTaskInstanceIDMDC(task.getProcessInstanceId(), task.getId());
                    log.info(
                            "Check the taskInstance from a exist workflowInstance, existTaskInstanceCode: {}, taskInstanceStatus: {}",
                            task.getTaskCode(),
                            task.getState());
                    if (validTaskMap.containsKey(task.getTaskCode())) {
                        log.warn(
                                "Have same taskCode taskInstance when init task queue, need to check taskExecutionStatus, taskCode:{}",
                                task.getTaskCode());
                        int oldTaskInstanceId = validTaskMap.get(task.getTaskCode());
                        TaskInstance oldTaskInstance = taskInstanceMap.get(oldTaskInstanceId);
                        if (!oldTaskInstance.getState().isFinished() && task.getState().isFinished()) {
                            task.setFlag(Flag.NO);
                            taskInstanceDao.updateById(task);
                            continue;
                        }
                    }

                    processService.packageTaskInstance(task, workflowInstance);
                    validTaskMap.put(task.getTaskCode(), task.getId());
                    taskInstanceMap.put(task.getId(), task);
                    taskCodeInstanceMap.put(task.getTaskCode(), task);

                    if (task.isTaskComplete()) {
                        log.info("TaskInstance is already complete.");
                        completeTaskSet.add(task.getTaskCode());
                        continue;
                    }
                    if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(task.getTaskCode(),
                            workflowExecuteContext.getWorkflowGraph().getDag())) {
                        continue;
                    }
                    if (task.taskCanRetry()) {
                        if (task.getState().isNeedFaultTolerance()) {
                            log.info("TaskInstance needs fault tolerance, will be added to standby list.");
                            task.setFlag(Flag.NO);
                            taskInstanceDao.updateById(task);

                            // tolerantTaskInstance add to standby list directly
                            TaskInstance tolerantTaskInstance = cloneTolerantTaskInstance(task);
                            addTaskToStandByList(tolerantTaskInstance);
                        } else {
                            log.info("Retry taskInstance, taskState: {}", task.getState());
                            retryTaskInstance(task);
                        }
                        continue;
                    }
                    if (task.getState().isFailure()) {
                        errorTaskMap.put(task.getTaskCode(), task.getId());
                    }
                } finally {
                    LogUtils.removeWorkflowAndTaskInstanceIdMDC();
                }
            }
            clearDataIfExecuteTask();
        } else {
            log.info("The current workflowInstance is a newly running workflowInstance");
        }

        if (workflowInstance.isComplementData() && complementListDate.isEmpty()) {
            Map<String, String> cmdParam = JSONUtils.toMap(workflowInstance.getCommandParam());
            if (cmdParam != null) {
                // reset global params while there are start parameters
                processService.setGlobalParamIfCommanded(workflowDefinition, cmdParam);

                Date start = null;
                Date end = null;
                if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE)
                        && cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_END_DATE)) {
                    start = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE));
                    end = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
                }
                if (complementListDate.isEmpty() && needComplementProcess()) {
                    if (start != null && end != null) {
                        List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                                workflowInstance.getProcessDefinitionCode());
                        complementListDate = CronUtils.getSelfFireDateList(start, end, schedules);
                    }
                    if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
                        complementListDate = CronUtils.getSelfScheduleDateList(cmdParam);
                    }
                    log.info(" process definition code:{} complement data: {}",
                            workflowInstance.getProcessDefinitionCode(), complementListDate);

                    if (!complementListDate.isEmpty() && Flag.NO == workflowInstance.getIsSubProcess()) {
                        workflowInstance.setScheduleTime(complementListDate.get(0));
                        String globalParams = curingParamsService.curingGlobalParams(workflowInstance.getId(),
                                workflowDefinition.getGlobalParamMap(),
                                workflowDefinition.getGlobalParamList(),
                                CommandType.COMPLEMENT_DATA,
                                workflowInstance.getScheduleTime(),
                                cmdParam.get(Constants.SCHEDULE_TIMEZONE));
                        workflowInstance.setGlobalParams(globalParams);
                        processInstanceDao.updateById(workflowInstance);
                    }
                }
            }
        }
        log.info("Initialize task queue, dependFailedTaskSet: {}, completeTaskSet: {}, errorTaskMap: {}",
                dependFailedTaskSet,
                completeTaskSet,
                errorTaskMap);
    }

    private boolean executeTask(TaskInstance taskInstance) {
        try {
            ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
            // package task instance before submit, inject the process instance to task instance
            // todo: we need to use task execute context rather than packege a lot of pojo into task instance
            // 1. submit the task instance to db
            processService.packageTaskInstance(taskInstance, workflowInstance);
            // todo: remove this method
            if (!processService.submitTask(workflowInstance, taskInstance)) {
                log.error("Submit standby task: {} failed", taskInstance.getName());
                return true;
            }
            // 2. create task execute runnable
            // in a dag, only one taskInstance is valid per taskCode, so need to set the old taskInstance invalid
            try {
                LogUtils.setTaskInstanceIdMDC(taskInstance.getId());
                DefaultTaskExecuteRunnable taskExecuteRunnable =
                        defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(taskInstance);
                if (validTaskMap.containsKey(taskInstance.getTaskCode())) {
                    int oldTaskInstanceId = validTaskMap.get(taskInstance.getTaskCode());
                    if (taskInstance.getId() != oldTaskInstanceId) {
                        TaskInstance oldTaskInstance = taskInstanceMap.get(oldTaskInstanceId);
                        oldTaskInstance.setFlag(Flag.NO);
                        taskInstanceDao.updateById(oldTaskInstance);
                        validTaskMap.remove(taskInstance.getTaskCode());
                        taskExecuteRunnableMap.remove(taskInstance.getTaskCode());
                    }
                }

                validTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
                taskInstanceMap.put(taskInstance.getId(), taskInstance);
                taskCodeInstanceMap.put(taskInstance.getTaskCode(), taskInstance);
                taskExecuteRunnableMap.put(taskInstance.getTaskCode(), taskExecuteRunnable);
                // 3. acquire the task group.
                // if we use task group, then need to acquire the task group resource
                // if there is no resource the current task instance will not be dispatched
                // it will be wakeup when other tasks release the resource.
                int taskGroupId = taskInstance.getTaskGroupId();
                if (taskGroupId > 0) {
                    taskGroupCoordinator.acquireTaskGroupSlot(taskInstance);
                    log.info("The TaskInstance: {} use taskGroup: {} to manage the resource, will wait to notify it",
                            taskInstance,
                            taskGroupId);
                    return true;
                }
                // 4. submit to dispatch queue
                tryToDispatchTaskInstance(taskInstance, taskExecuteRunnable);

                stateWheelExecuteThread.addTask4TimeoutCheck(workflowInstance, taskInstance);
                return true;
            } finally {
                LogUtils.removeTaskInstanceIdMDC();
            }
        } catch (Exception e) {
            log.error("Submit standby task {} error", taskInstance.getName(), e);
            return false;
        }
    }

    /**
     * Sometimes (such as pause), if the task instance status has already been finished,
     * there is no need to dispatch it
     */
    @VisibleForTesting
    void tryToDispatchTaskInstance(TaskInstance taskInstance, TaskExecuteRunnable taskExecuteRunnable) {
        if (!taskInstance.getState().isFinished()) {
            taskExecuteRunnable.dispatch();
        } else {
            if (workflowExecuteContext.getWorkflowInstance().isBlocked()) {
                TaskStateEvent processBlockEvent = TaskStateEvent.builder()
                        .processInstanceId(workflowExecuteContext.getWorkflowInstance().getId())
                        .taskInstanceId(taskInstance.getId())
                        .status(taskInstance.getState())
                        .type(StateEventType.PROCESS_BLOCKED)
                        .build();
                this.stateEvents.add(processBlockEvent);
            }

            TaskStateEvent taskStateChangeEvent = TaskStateEvent.builder()
                    .processInstanceId(workflowExecuteContext.getWorkflowInstance().getId())
                    .taskInstanceId(taskInstance.getId())
                    .status(taskInstance.getState())
                    .type(StateEventType.TASK_STATE_CHANGE)
                    .build();
            this.stateEvents.add(taskStateChangeEvent);
        }
    }

    /**
     * find task instance in db.
     * in case submit more than one same name task in the same time.
     *
     * @param taskCode    task code
     * @param taskVersion task version
     * @return TaskInstance
     */
    private TaskInstance findTaskIfExists(Long taskCode, int taskVersion) {
        List<TaskInstance> validTaskInstanceList = getValidTaskList();
        for (TaskInstance taskInstance : validTaskInstanceList) {
            if (taskInstance.getTaskCode() == taskCode && taskInstance.getTaskDefinitionVersion() == taskVersion) {
                return taskInstance;
            }
        }
        return null;
    }

    /**
     * encapsulation task, this method will only create a new task instance, the return task instance will not contain id.
     *
     * @param processInstance process instance
     * @param taskNode        taskNode
     * @return TaskInstance
     */
    private TaskInstance createTaskInstance(ProcessInstance processInstance, TaskNode taskNode) {
        TaskInstance taskInstance = findTaskIfExists(taskNode.getCode(), taskNode.getVersion());
        if (taskInstance != null) {
            return taskInstance;
        }

        return newTaskInstance(processInstance, taskNode);
    }

    /**
     * clone a new taskInstance for retry and reset some logic fields
     *
     * @return taskInstance
     */
    public TaskInstance cloneRetryTaskInstance(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        TaskNode taskNode =
                workflowExecuteContext.getWorkflowGraph().getTaskNodeByCode(taskInstance.getTaskCode());
        TaskInstance newTaskInstance = newTaskInstance(workflowInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(workflowInstance);
        newTaskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1);
        // todo relative function: TaskInstance.retryTaskIntervalOverTime
        newTaskInstance.setState(taskInstance.getState());
        newTaskInstance.setEndTime(taskInstance.getEndTime());
        newTaskInstance.setVarPool(taskInstance.getVarPool());

        if (taskInstance.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE) {
            newTaskInstance.setAppLink(taskInstance.getAppLink());
        }

        return newTaskInstance;
    }

    /**
     * clone a new taskInstance for tolerant and reset some logic fields
     *
     * @return taskInstance
     */
    public TaskInstance cloneTolerantTaskInstance(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        TaskNode taskNode =
                workflowExecuteContext.getWorkflowGraph().getTaskNodeByCode(taskInstance.getTaskCode());
        TaskInstance newTaskInstance = newTaskInstance(workflowInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(workflowInstance);
        newTaskInstance.setRetryTimes(taskInstance.getRetryTimes());
        newTaskInstance.setState(taskInstance.getState());
        newTaskInstance.setAppLink(taskInstance.getAppLink());
        newTaskInstance.setVarPool(taskInstance.getVarPool());
        return newTaskInstance;
    }

    /**
     * new a taskInstance
     *
     * @param processInstance process instance
     * @param taskNode        task node
     * @return task instance
     */
    public TaskInstance newTaskInstance(ProcessInstance processInstance, TaskNode taskNode) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskCode(taskNode.getCode());
        taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
        // task name
        taskInstance.setName(taskNode.getName());
        // task instance state
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        // process instance id
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setProcessInstanceName(processInstance.getName());
        taskInstance.setProjectCode(processInstance.getProjectCode());
        // task instance type
        taskInstance.setTaskType(taskNode.getType().toUpperCase());
        // task instance whether alert
        taskInstance.setAlertFlag(Flag.NO);

        // task instance start time
        taskInstance.setStartTime(null);

        // task test flag
        taskInstance.setTestFlag(processInstance.getTestFlag());

        // task instance flag
        taskInstance.setFlag(Flag.YES);

        // task dry run flag
        taskInstance.setDryRun(processInstance.getDryRun());

        // task instance retry times
        taskInstance.setRetryTimes(0);

        // max task instance retry times
        taskInstance.setMaxRetryTimes(taskNode.getMaxRetryTimes());

        // retry task instance interval
        taskInstance.setRetryInterval(taskNode.getRetryInterval());

        // set task param
        taskInstance.setTaskParams(taskNode.getTaskParams());

        // set task group and priority
        taskInstance.setTaskGroupId(taskNode.getTaskGroupId());
        taskInstance.setTaskGroupPriority(taskNode.getTaskGroupPriority());

        // set task cpu quota and max memory
        taskInstance.setCpuQuota(taskNode.getCpuQuota());
        taskInstance.setMemoryMax(taskNode.getMemoryMax());

        taskInstance.setIsCache(taskNode.getIsCache() == Flag.YES.getCode() ? Flag.YES : Flag.NO);

        // task instance priority
        if (taskNode.getTaskInstancePriority() == null) {
            taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        } else {
            taskInstance.setTaskInstancePriority(taskNode.getTaskInstancePriority());
        }

        String processWorkerGroup = processInstance.getWorkerGroup();
        processWorkerGroup = StringUtils.isBlank(processWorkerGroup) ? DEFAULT_WORKER_GROUP : processWorkerGroup;
        String taskWorkerGroup =
                StringUtils.isBlank(taskNode.getWorkerGroup()) ? processWorkerGroup : taskNode.getWorkerGroup();

        Long processEnvironmentCode =
                Objects.isNull(processInstance.getEnvironmentCode()) ? -1 : processInstance.getEnvironmentCode();
        Long taskEnvironmentCode =
                Objects.isNull(taskNode.getEnvironmentCode()) ? processEnvironmentCode : taskNode.getEnvironmentCode();

        if (!processWorkerGroup.equals(DEFAULT_WORKER_GROUP) && taskWorkerGroup.equals(DEFAULT_WORKER_GROUP)) {
            taskInstance.setWorkerGroup(processWorkerGroup);
            taskInstance.setEnvironmentCode(processEnvironmentCode);
        } else {
            taskInstance.setWorkerGroup(taskWorkerGroup);
            taskInstance.setEnvironmentCode(taskEnvironmentCode);
        }

        if (!taskInstance.getEnvironmentCode().equals(-1L)) {
            Environment environment = processService.findEnvironmentByCode(taskInstance.getEnvironmentCode());
            if (Objects.nonNull(environment) && StringUtils.isNotEmpty(environment.getConfig())) {
                taskInstance.setEnvironmentConfig(environment.getConfig());
            }
        }
        // delay execution time
        taskInstance.setDelayTime(taskNode.getDelayTime());
        taskInstance.setTaskExecuteType(taskNode.getTaskExecuteType());
        return taskInstance;
    }

    void initializeTaskInstanceVarPool(TaskInstance taskInstance) {
        // get pre task ,get all the task varPool to this task
        // Do not use dag.getPreviousNodes because of the dag may be miss the upstream node
        String preTasks =
                workflowExecuteContext.getWorkflowGraph().getTaskNodeByCode(taskInstance.getTaskCode()).getPreTasks();
        Set<Long> preTaskList = new HashSet<>(JSONUtils.toList(preTasks, Long.class));
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();

        if (CollectionUtils.isEmpty(preTaskList)) {
            taskInstance.setVarPool(workflowInstance.getVarPool());
            return;
        }
        List<String> preTaskInstanceVarPools = preTaskList
                .stream()
                .map(taskCode -> getTaskInstance(taskCode).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TaskInstance::getEndTime))
                .map(TaskInstance::getVarPool)
                .collect(Collectors.toList());
        taskInstance.setVarPool(VarPoolUtils.mergeVarPoolJsonString(preTaskInstanceVarPools));
    }

    public Collection<TaskInstance> getAllTaskInstances() {
        return taskInstanceMap.values();
    }

    /**
     * get complete task instance map, taskCode as key
     */
    private Map<Long, TaskInstance> getCompleteTaskInstanceMap() {
        Map<Long, TaskInstance> completeTaskInstanceMap = new HashMap<>();

        completeTaskSet.forEach(taskCode -> {
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskCode);
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance taskInstance = existTaskInstanceOptional.get();
                completeTaskInstanceMap.put(taskCode, taskInstance);
            } else {
                // This case will happen when we submit to db failed, then the taskInstanceId is 0
                log.warn("Cannot find the taskInstance from taskInstanceMap, taskConde: {}",
                        taskCode);
            }
        });

        return completeTaskInstanceMap;
    }

    /**
     * get valid task list
     */
    private List<TaskInstance> getValidTaskList() {
        List<TaskInstance> validTaskInstanceList = new ArrayList<>();
        for (Integer taskInstanceId : validTaskMap.values()) {
            validTaskInstanceList.add(taskInstanceMap.get(taskInstanceId));
        }
        return validTaskInstanceList;
    }

    private void submitPostNode(Long parentNodeCode) throws StateEventHandleException {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        DAG<Long, TaskNode, TaskNodeRelation> dag = workflowExecuteContext.getWorkflowGraph().getDag();

        Set<Long> submitTaskNodeList =
                DagHelper.parsePostNodes(parentNodeCode, skipTaskNodeMap, dag, getCompleteTaskInstanceMap());
        List<TaskInstance> taskInstances = new ArrayList<>();
        for (Long taskNode : submitTaskNodeList) {
            TaskNode taskNodeObject = dag.getNode(taskNode);
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskNodeObject.getCode());
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance existTaskInstance = existTaskInstanceOptional.get();
                TaskExecutionStatus state = existTaskInstance.getState();
                if (state == TaskExecutionStatus.RUNNING_EXECUTION
                        || state == TaskExecutionStatus.DISPATCH
                        || state == TaskExecutionStatus.SUBMITTED_SUCCESS
                        || state == TaskExecutionStatus.DELAY_EXECUTION) {
                    // try to take over task instance
                    if (state == TaskExecutionStatus.SUBMITTED_SUCCESS || state == TaskExecutionStatus.DELAY_EXECUTION
                            || state == TaskExecutionStatus.DISPATCH) {
                        // The taskInstance is not in running, directly takeover it
                    } else if (tryToTakeOverTaskInstance(existTaskInstance)) {
                        log.info("Success take over task {}", existTaskInstance.getName());
                        continue;
                    } else {
                        // set the task instance state to fault tolerance
                        existTaskInstance.setFlag(Flag.NO);
                        existTaskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
                        releaseTaskGroupIfNeeded(existTaskInstance);

                        validTaskMap.remove(existTaskInstance.getTaskCode());
                        taskInstanceDao.updateById(existTaskInstance);
                        existTaskInstance = cloneTolerantTaskInstance(existTaskInstance);
                        log.info("task {} cannot be take over will generate a tolerant task instance",
                                existTaskInstance.getName());
                    }
                }
                taskInstances.add(existTaskInstance);
            } else {
                taskInstances.add(createTaskInstance(workflowInstance, taskNodeObject));
            }
        }
        // the end node of the branch of the dag
        if (parentNodeCode != null && dag.getEndNode().contains(parentNodeCode)) {
            getTaskInstance(parentNodeCode)
                    .ifPresent(endTaskInstance -> workflowInstance.setVarPool(VarPoolUtils.mergeVarPoolJsonString(
                            Lists.newArrayList(workflowInstance.getVarPool(), endTaskInstance.getVarPool()))));

        }

        // if previous node success , post node submit
        for (TaskInstance task : taskInstances) {

            if (standByTaskInstancePriorityQueue.contains(task)) {
                log.warn("Task is already at submit queue, taskInstanceName: {}", task.getName());
                continue;
            }

            if (task.getId() != null && completeTaskSet.contains(task.getTaskCode())) {
                log.info("Task has already run success, taskName: {}", task.getName());
                continue;
            }
            if (task.getState().isKill()) {
                log.info("Task is be stopped, the state is {}, taskInstanceId: {}", task.getState(), task.getId());
                continue;
            }

            addTaskToStandByList(task);
        }
        submitStandByTask();
        updateProcessInstanceState();
    }

    private boolean tryToTakeOverTaskInstance(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (TaskUtils.isMasterTask(taskInstance.getTaskType())) {
            return false;
        }
        try {
            ITaskInstanceOperator iTaskInstanceOperator =
                    SingletonJdkDynamicRpcClientProxyFactory
                            .getProxyClient(taskInstance.getHost(), ITaskInstanceOperator.class);
            UpdateWorkflowHostResponse response = iTaskInstanceOperator.updateWorkflowInstanceHost(
                    new UpdateWorkflowHostRequest(taskInstance.getId(), masterConfig.getMasterAddress()));
            if (!response.isSuccess()) {
                log.error(
                        "Takeover TaskInstance failed, receive a failed response: {} from worker: {}, will try to create a new TaskInstance",
                        response, taskInstance.getHost());
                return false;
            }

            // todo: create the takeover task execute runnable.
            taskExecuteRunnableMap.put(taskInstance.getTaskCode(),
                    defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(taskInstance));

            taskInstanceMap.put(taskInstance.getId(), taskInstance);
            taskCodeInstanceMap.put(taskInstance.getTaskCode(), taskInstance);
            stateWheelExecuteThread.addTask4TimeoutCheck(workflowInstance, taskInstance);
            stateWheelExecuteThread.addTask4RetryCheck(workflowInstance, taskInstance);
            return true;
        } catch (Exception e) {
            log.error(
                    "Takeover TaskInstance failed, the worker {} might not be alive, will try to create a new TaskInstance",
                    taskInstance.getHost(), e);
            return false;
        }
    }

    /**
     * determine whether the dependencies of the task node are complete
     *
     * @return DependResult
     */
    private DependResult isTaskDepsComplete(Long taskCode) {
        DAG<Long, TaskNode, TaskNodeRelation> dag = workflowExecuteContext.getWorkflowGraph().getDag();

        Collection<Long> startNodes = dag.getBeginNode();
        // if vertex,returns true directly
        if (startNodes.contains(taskCode)) {
            return DependResult.SUCCESS;
        }
        TaskNode taskNode = dag.getNode(taskCode);
        List<Long> indirectDepCodeList = new ArrayList<>();
        setIndirectDepList(taskCode, indirectDepCodeList);
        for (Long depsNode : indirectDepCodeList) {
            if (dag.containsNode(depsNode) && !skipTaskNodeMap.containsKey(depsNode)) {
                // dependencies must be fully completed
                if (!completeTaskSet.contains(depsNode)) {
                    return DependResult.WAITING;
                }

                Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(depsNode);
                if (!existTaskInstanceOptional.isPresent()) {
                    return DependResult.NON_EXEC;
                }

                TaskExecutionStatus depTaskState =
                        taskInstanceMap.get(existTaskInstanceOptional.get().getId()).getState();
                if (depTaskState.isKill()) {
                    return DependResult.NON_EXEC;
                }
                // ignore task state if current task is block
                if (taskNode.isBlockingTask()) {
                    continue;
                }

                // always return success if current task is condition
                if (taskNode.isConditionsTask()) {
                    continue;
                }

                if (!dependTaskSuccess(depsNode, taskCode)) {
                    return DependResult.FAILED;
                }
            }
        }
        log.info("The dependTasks of task all success, currentTaskCode: {}, dependTaskCodes: {}",
                taskCode, Arrays.toString(indirectDepCodeList.toArray()));
        return DependResult.SUCCESS;
    }

    /**
     * This function is specially used to handle the dependency situation where the parent node is a prohibited node.
     * When the parent node is a forbidden node, the dependency relationship should continue to be traced
     *
     * @param taskCode            taskCode
     * @param indirectDepCodeList All indirectly dependent nodes
     */
    private void setIndirectDepList(Long taskCode, List<Long> indirectDepCodeList) {
        IWorkflowGraph workflowGraph = workflowExecuteContext.getWorkflowGraph();
        DAG<Long, TaskNode, TaskNodeRelation> dag = workflowGraph.getDag();
        TaskNode taskNode = dag.getNode(taskCode);
        // If workflow start with startNode or recoveryNode, taskNode may be null
        if (taskNode == null) {
            return;
        }

        for (Long depsNode : taskNode.getDepList()) {
            if (workflowGraph.isForbiddenTask(depsNode)) {
                setIndirectDepList(depsNode, indirectDepCodeList);
            } else {
                indirectDepCodeList.add(depsNode);
            }
        }
    }

    /**
     * depend node is completed, but here need check the condition task branch is the next node
     */
    private boolean dependTaskSuccess(Long dependNodeCode, Long nextNodeCode) {
        DAG<Long, TaskNode, TaskNodeRelation> dag = workflowExecuteContext.getWorkflowGraph().getDag();
        TaskNode dependentNode = dag.getNode(dependNodeCode);
        if (dependentNode.isConditionsTask()) {
            // condition task need check the branch to run
            List<Long> nextTaskList =
                    DagHelper.parseConditionTask(dependNodeCode, skipTaskNodeMap, dag, getCompleteTaskInstanceMap());
            if (!nextTaskList.contains(nextNodeCode)) {
                log.info(
                        "DependTask is a condition task, and its next condition branch does not hava current task, " +
                                "dependTaskCode: {}, currentTaskCode: {}",
                        dependNodeCode, nextNodeCode);
                return false;
            }
            return true;
        }
        if (dependentNode.isSwitchTask()) {
            TaskInstance dependentTaskInstance = taskInstanceMap.get(validTaskMap.get(dependentNode.getCode()));
            SwitchParameters switchParameters = dependentTaskInstance.getSwitchDependency();
            return switchParameters.getDependTaskList().get(switchParameters.getResultConditionLocation()).getNextNode()
                    .contains(nextNodeCode);
        }
        Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(dependNodeCode);
        if (!existTaskInstanceOptional.isPresent()) {
            return false;
        }

        TaskExecutionStatus depTaskState = existTaskInstanceOptional.get().getState();
        return !depTaskState.isFailure();
    }

    /**
     * query task instance by complete state
     *
     * @param state state
     * @return task instance list
     */
    private List<TaskInstance> getCompleteTaskByState(TaskExecutionStatus state) {
        List<TaskInstance> resultList = new ArrayList<>();

        completeTaskSet.forEach(taskCode -> {
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskCode);
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance taskInstance = existTaskInstanceOptional.get();
                if (taskInstance.getState() == state) {
                    resultList.add(taskInstance);
                }
            }
        });

        return resultList;
    }

    /**
     * where there are ongoing tasks
     *
     * @param state state
     * @return ExecutionStatus
     */
    private WorkflowExecutionStatus runningState(WorkflowExecutionStatus state) {
        if (state == WorkflowExecutionStatus.READY_STOP || state == WorkflowExecutionStatus.READY_PAUSE
                || state == WorkflowExecutionStatus.READY_BLOCK ||
                state == WorkflowExecutionStatus.DELAY_EXECUTION) {
            // if the running task is not completed, the state remains unchanged
            return state;
        } else {
            return WorkflowExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * exists failure task,contains submit failure、dependency failure,execute failure(retry after)
     *
     * @return Boolean whether has failed task
     */
    private boolean hasFailedTask() {

        if (this.taskFailedSubmit) {
            return true;
        }
        if (this.errorTaskMap.size() > 0) {
            return true;
        }
        return this.dependFailedTaskSet.size() > 0;
    }

    /**
     * process instance failure
     *
     * @return Boolean whether process instance failed
     */
    private boolean processFailed() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (hasFailedTask()) {
            log.info("The current process has failed task, the current process failed");
            if (workflowInstance.getFailureStrategy() == FailureStrategy.END) {
                return true;
            }
            if (workflowInstance.getFailureStrategy() == FailureStrategy.CONTINUE) {
                return standByTaskInstancePriorityQueue.size() == 0 && taskExecuteRunnableMap.size() == 0
                        && waitToRetryTaskInstanceMap.size() == 0;
            }
        }
        return false;
    }

    /**
     * prepare for pause
     * 1，failed retry task in the preparation queue , returns to failure directly
     * 2，exists pause task，complement not completed, pending submission of tasks, return to suspension
     * 3，success
     *
     * @return ExecutionStatus
     */
    private WorkflowExecutionStatus processReadyPause() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (hasRetryTaskInStandBy()) {
            return WorkflowExecutionStatus.FAILURE;
        }

        List<TaskInstance> pauseList = getCompleteTaskByState(TaskExecutionStatus.PAUSE);
        if (CollectionUtils.isNotEmpty(pauseList) || workflowInstance.isBlocked() || !isComplementEnd()
                || standByTaskInstancePriorityQueue.size() > 0) {
            return WorkflowExecutionStatus.PAUSE;
        } else {
            return WorkflowExecutionStatus.SUCCESS;
        }
    }

    /**
     * prepare for block
     * if process has tasks still running, pause them
     * if readyToSubmitTaskQueue is not empty, kill them
     * else return block status directly
     *
     * @return ExecutionStatus
     */
    private WorkflowExecutionStatus processReadyBlock() {
        if (taskExecuteRunnableMap.size() > 0) {
            for (DefaultTaskExecuteRunnable taskExecuteRunnable : taskExecuteRunnableMap.values()) {
                if (!TASK_TYPE_BLOCKING.equals(taskExecuteRunnable.getTaskInstance().getTaskType())) {
                    taskExecuteRunnable.pause();
                }
            }
        }
        if (standByTaskInstancePriorityQueue.size() > 0) {
            for (Iterator<TaskInstance> iter = standByTaskInstancePriorityQueue.iterator(); iter.hasNext();) {
                iter.next().setState(TaskExecutionStatus.PAUSE);
            }
        }
        return WorkflowExecutionStatus.BLOCK;
    }

    /**
     * generate the latest process instance status by the tasks state
     *
     * @return process instance execution status
     */
    private WorkflowExecutionStatus getProcessInstanceState(ProcessInstance instance) {
        WorkflowExecutionStatus state = instance.getState();

        if (taskExecuteRunnableMap.size() > 0 || hasRetryTaskInStandBy()) {
            // active task and retry task exists
            WorkflowExecutionStatus executionStatus = runningState(state);
            log.info("The workflowInstance has task running, the workflowInstance status is {}", executionStatus);
            return executionStatus;
        }

        // block
        if (state == WorkflowExecutionStatus.READY_BLOCK) {
            WorkflowExecutionStatus executionStatus = processReadyBlock();
            log.info("The workflowInstance is ready to block, the workflowInstance status is {}", executionStatus);
            return executionStatus;
        }

        // pause
        if (state == WorkflowExecutionStatus.READY_PAUSE) {
            WorkflowExecutionStatus executionStatus = processReadyPause();
            log.info("The workflowInstance is ready to pause, the workflow status is {}", executionStatus);
            return executionStatus;
        }

        // stop
        if (state == WorkflowExecutionStatus.READY_STOP) {
            List<TaskInstance> killList = getCompleteTaskByState(TaskExecutionStatus.KILL);
            List<TaskInstance> failList = getCompleteTaskByState(TaskExecutionStatus.FAILURE);
            List<TaskInstance> stopList = getCompleteTaskByState(TaskExecutionStatus.STOP);
            WorkflowExecutionStatus executionStatus;
            if (CollectionUtils.isNotEmpty(stopList) || CollectionUtils.isNotEmpty(killList)
                    || CollectionUtils.isNotEmpty(failList) || !isComplementEnd()) {
                executionStatus = WorkflowExecutionStatus.STOP;
            } else {
                executionStatus = WorkflowExecutionStatus.SUCCESS;
            }
            log.info("The workflowInstance is ready to stop, the workflow status is {}", executionStatus);
            return executionStatus;
        }

        // process failure
        if (processFailed()) {
            log.info("The workflowInstance is failed, the workflow status is {}", WorkflowExecutionStatus.FAILURE);
            return WorkflowExecutionStatus.FAILURE;
        }

        // success
        if (state == WorkflowExecutionStatus.RUNNING_EXECUTION) {
            List<TaskInstance> killTasks = getCompleteTaskByState(TaskExecutionStatus.KILL);
            if (standByTaskInstancePriorityQueue.size() > 0 || waitToRetryTaskInstanceMap.size() > 0) {
                // tasks currently pending submission, no retries, indicating that depend is waiting to complete
                return WorkflowExecutionStatus.RUNNING_EXECUTION;
            } else if (CollectionUtils.isNotEmpty(killTasks)) {
                // tasks maybe killed manually
                return WorkflowExecutionStatus.FAILURE;
            } else {
                // if the waiting queue is empty and the status is in progress, then success
                return WorkflowExecutionStatus.SUCCESS;
            }
        }

        return state;
    }

    /**
     * whether complement end
     *
     * @return Boolean whether is complement end
     */
    private boolean isComplementEnd() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (!workflowInstance.isComplementData()) {
            return true;
        }

        Map<String, String> cmdParam = JSONUtils.toMap(workflowInstance.getCommandParam());
        Date endTime = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
        return workflowInstance.getScheduleTime().equals(endTime);
    }

    /**
     * updateProcessInstance process instance state
     * after each batch of tasks is executed, the status of the process instance is updated
     */
    private void updateProcessInstanceState() throws StateEventHandleException {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        WorkflowExecutionStatus state = getProcessInstanceState(workflowInstance);
        if (workflowInstance.getState() != state) {
            log.info("Update workflowInstance states, origin state: {}, target state: {}",
                    workflowInstance.getState(),
                    state);
            updateWorkflowInstanceStatesToDB(state);

            WorkflowStateEvent stateEvent = WorkflowStateEvent.builder()
                    .processInstanceId(workflowInstance.getId())
                    .status(workflowInstance.getState())
                    .type(StateEventType.PROCESS_STATE_CHANGE)
                    .build();
            // replace with `stateEvents`, make sure `WorkflowExecuteThread` can be deleted to avoid memory leaks
            this.stateEvents.add(stateEvent);
        } else {
            log.info("There is no need to update the workflow instance state, origin state: {}, target state: {}",
                    workflowInstance.getState(),
                    state);
        }
    }

    /**
     * stateEvent's execution status as process instance state
     */
    public void updateProcessInstanceState(WorkflowStateEvent stateEvent) throws StateEventHandleException {
        WorkflowExecutionStatus state = stateEvent.getStatus();
        updateWorkflowInstanceStatesToDB(state);
    }

    private void updateWorkflowInstanceStatesToDB(WorkflowExecutionStatus newStates) throws StateEventHandleException {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        WorkflowExecutionStatus originStates = workflowInstance.getState();
        if (originStates != newStates) {
            log.info("Begin to update workflow instance state , state will change from {} to {}",
                    originStates,
                    newStates);

            workflowInstance.setStateWithDesc(newStates, "update by workflow executor");
            if (newStates.isFinished()) {
                workflowInstance.setEndTime(new Date());
            }
            try {
                processInstanceDao.performTransactionalUpsert(workflowInstance);
            } catch (Exception ex) {
                // recover the status
                workflowInstance.setStateWithDesc(originStates, "recover state by DB error");
                workflowInstance.setEndTime(null);
                throw new StateEventHandleException("Update process instance status to DB error", ex);
            }
        }
    }

    /**
     * get task dependency result
     *
     * @param taskInstance task instance
     * @return DependResult
     */
    private DependResult getDependResultForTask(TaskInstance taskInstance) {
        return isTaskDepsComplete(taskInstance.getTaskCode());
    }

    /**
     * add task to standby list
     *
     * @param taskInstance task instance
     */
    public void addTaskToStandByList(TaskInstance taskInstance) {
        if (standByTaskInstancePriorityQueue.contains(taskInstance)) {
            log.warn("Task already exists in ready submit queue, no need to add again, task code:{}",
                    taskInstance.getTaskCode());
            return;
        }
        log.info("Add task to stand by list, task name:{}, task id:{}, task code:{}",
                taskInstance.getName(),
                taskInstance.getId(),
                taskInstance.getTaskCode());
        TaskMetrics.incTaskInstanceByState("submit");
        standByTaskInstancePriorityQueue.put(taskInstance);
    }

    /**
     * remove task from stand by list
     *
     * @param taskInstance task instance
     */
    private boolean removeTaskFromStandbyList(TaskInstance taskInstance) {
        return standByTaskInstancePriorityQueue.remove(taskInstance);
    }

    /**
     * has retry task in standby
     *
     * @return Boolean whether has retry task in standby
     */
    private boolean hasRetryTaskInStandBy() {
        for (Iterator<TaskInstance> iter = standByTaskInstancePriorityQueue.iterator(); iter.hasNext();) {
            if (iter.next().getState().isFailure()) {
                return true;
            }
        }
        return false;
    }

    /**
     * close the on going tasks
     */
    public void killAllTasks() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        log.info("kill called on process instance id: {}, num: {}",
                workflowInstance.getId(),
                taskExecuteRunnableMap.size());

        if (standByTaskInstancePriorityQueue.size() > 0) {
            standByTaskInstancePriorityQueue.clear();
        }

        for (long taskCode : taskExecuteRunnableMap.keySet()) {
            Integer taskInstanceId = validTaskMap.get(taskCode);
            if (taskInstanceId == null || taskInstanceId.equals(0)) {
                continue;
            }
            try {
                LogUtils.setWorkflowAndTaskInstanceIDMDC(workflowInstance.getId(), taskInstanceId);
                TaskInstance taskInstance = taskInstanceDao.queryById(taskInstanceId);
                if (taskInstance == null || taskInstance.getState().isFinished()) {
                    continue;
                }
                DefaultTaskExecuteRunnable defaultTaskExecuteRunnable = taskExecuteRunnableMap.get(taskCode);
                defaultTaskExecuteRunnable.kill();
                if (defaultTaskExecuteRunnable.getTaskInstance().getState().isFinished()) {
                    TaskStateEvent taskStateEvent = TaskStateEvent.builder()
                            .processInstanceId(workflowInstance.getId())
                            .taskInstanceId(taskInstance.getId())
                            .status(defaultTaskExecuteRunnable.getTaskInstance().getState())
                            .type(StateEventType.TASK_STATE_CHANGE)
                            .build();
                    this.addStateEvent(taskStateEvent);
                }
            } finally {
                LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
    }

    public boolean workFlowFinish() {
        return workflowExecuteContext.getWorkflowInstance().getState().isFinished();
    }

    /**
     * handling the list of tasks to be submitted
     */
    public void submitStandByTask() throws StateEventHandleException {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        TaskInstance task;
        while ((task = standByTaskInstancePriorityQueue.peek()) != null) {
            // stop tasks which is retrying if forced success happens
            if (task.getId() != null && task.taskCanRetry()) {
                TaskInstance retryTask = taskInstanceDao.queryById(task.getId());
                if (retryTask != null && retryTask.getState().isForceSuccess()) {
                    task.setState(retryTask.getState());
                    log.info(
                            "Task {} has been forced success, put it into complete task list and stop retrying, taskInstanceId: {}",
                            task.getName(), task.getId());
                    removeTaskFromStandbyList(task);
                    completeTaskSet.add(task.getTaskCode());
                    taskInstanceMap.put(task.getId(), task);
                    taskCodeInstanceMap.put(task.getTaskCode(), task);
                    submitPostNode(task.getTaskCode());
                    continue;
                }
            }
            if (task.isFirstRun()) {
                initializeTaskInstanceVarPool(task);
            }
            DependResult dependResult = getDependResultForTask(task);
            if (DependResult.SUCCESS == dependResult) {
                log.info("The dependResult of task {} is success, so ready to submit to execute", task.getName());
                if (!executeTask(task)) {
                    this.taskFailedSubmit = true;
                    // Remove and add to complete map and error map
                    if (!removeTaskFromStandbyList(task)) {
                        log.error(
                                "Task submit failed, remove from standby list failed, workflowInstanceId: {}, taskCode: {}",
                                workflowInstance.getId(),
                                task.getTaskCode());
                    }
                    completeTaskSet.add(task.getTaskCode());
                    taskInstanceMap.put(task.getId(), task);
                    taskCodeInstanceMap.put(task.getTaskCode(), task);
                    errorTaskMap.put(task.getTaskCode(), task.getId());

                    taskExecuteRunnableMap.remove(task.getTaskCode());

                    log.error("Task submitted failed, workflowInstanceId: {}, taskInstanceId: {}, taskCode: {}",
                            task.getProcessInstanceId(),
                            task.getId(),
                            task.getTaskCode());
                } else {
                    removeTaskFromStandbyList(task);
                }
            } else if (DependResult.FAILED == dependResult) {
                // if the dependency fails, the current node is not submitted and the state changes to failure.
                dependFailedTaskSet.add(task.getTaskCode());
                removeTaskFromStandbyList(task);
                log.info("Task dependent result is failed, taskInstanceName: {} depend result : {}", task.getName(),
                        dependResult);
            } else if (DependResult.NON_EXEC == dependResult) {
                // for some reasons(depend task pause/stop) this task would not be submit
                removeTaskFromStandbyList(task);
                log.info("Remove task due to depend result not executed, taskInstanceName:{} depend result : {}",
                        task.getName(), dependResult);
            }
        }
    }

    /**
     * Get start task instance list from recover
     *
     * @param cmdParam command param
     * @return task instance list
     */
    protected List<TaskInstance> getRecoverTaskInstanceList(String cmdParam) {
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);

        // todo: Can we use a better way to set the recover taskInstanceId list? rather then use the cmdParam
        if (paramMap != null && paramMap.containsKey(CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            List<Integer> startTaskInstanceIds = Arrays.stream(paramMap.get(CMD_PARAM_RECOVERY_START_NODE_STRING)
                    .split(COMMA))
                    .filter(StringUtils::isNotEmpty)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(startTaskInstanceIds)) {
                return taskInstanceDao.queryByIds(startTaskInstanceIds);
            }
        }
        return Collections.emptyList();
    }

    /**
     * parse "StartNodeNameList" from cmd param
     *
     * @param cmdParam command param
     * @return start node name list
     */
    private List<String> parseStartNodeName(String cmdParam) {
        List<String> startNodeNameList = new ArrayList<>();
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        if (paramMap == null) {
            return startNodeNameList;
        }
        if (paramMap.containsKey(CMD_PARAM_START_NODES)) {
            startNodeNameList = Arrays.asList(paramMap.get(CMD_PARAM_START_NODES).split(Constants.COMMA));
        }
        return startNodeNameList;
    }

    /**
     * generate start node code list from parsing command param;
     * if "StartNodeIdList" exists in command param, return StartNodeIdList
     *
     * @return recovery node code list
     */
    private List<String> getRecoveryNodeCodeList(List<TaskInstance> recoverNodeList) {
        List<String> recoveryNodeCodeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(recoverNodeList)) {
            for (TaskInstance task : recoverNodeList) {
                recoveryNodeCodeList.add(Long.toString(task.getTaskCode()));
            }
        }
        return recoveryNodeCodeList;
    }

    private boolean isNewProcessInstance() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        if (Flag.YES.equals(workflowInstance.getRecovery())) {
            log.info("This workInstance will be recover by this execution");
            return false;
        }

        if (WorkflowExecutionStatus.RUNNING_EXECUTION == workflowInstance.getState()
                && workflowInstance.getRunTimes() == 1) {
            return true;
        }
        log.info(
                "The workflowInstance has been executed before, this execution is to reRun, processInstance status: {}, runTimes: {}",
                workflowInstance.getState(),
                workflowInstance.getRunTimes());
        return false;
    }

    public Set<Long> getCompleteTaskCodes() {
        return completeTaskSet;
    }

    public Map<Long, DefaultTaskExecuteRunnable> getTaskExecuteRunnableMap() {
        return taskExecuteRunnableMap;
    }

    public Optional<DefaultTaskExecuteRunnable> getTaskExecuteRunnableById(Integer taskInstanceId) {
        if (taskInstanceId == null) {
            throw new IllegalArgumentException("taskInstanceId can't be null");
        }
        TaskInstance taskInstance = taskInstanceMap.get(taskInstanceId);
        if (taskInstance == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(taskExecuteRunnableMap.get(taskInstance.getTaskCode()));
    }

    public Map<Long, TaskInstance> getWaitToRetryTaskInstanceMap() {
        return waitToRetryTaskInstanceMap;
    }

    /**
     * clear related data if command of process instance is EXECUTE_TASK
     * 1. find all task code from sub dag (only contains related task)
     * 2. set the flag of tasks to Flag.NO
     * 3. clear varPool data from re-execute task instance in process instance
     * 4. remove related task instance from taskInstanceMap, completeTaskSet, validTaskMap, errorTaskMap
     *
     * @return task instance
     */
    protected void clearDataIfExecuteTask() {
        ProcessInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        // only clear data if command is EXECUTE_TASK
        if (!workflowInstance.getCommandType().equals(CommandType.EXECUTE_TASK)) {
            return;
        }

        // Records the key of varPool data to be removed
        DAG<Long, TaskNode, TaskNodeRelation> dag = workflowExecuteContext.getWorkflowGraph().getDag();
        Set<Long> allNodesList = dag.getAllNodesList();

        List<TaskInstance> removeTaskInstances = new ArrayList<>();

        for (Long taskCode : allNodesList) {
            TaskInstance taskInstance;
            if (validTaskMap.containsKey(taskCode)) {
                taskInstance = taskInstanceMap.get(validTaskMap.get(taskCode));
            } else {
                taskInstance = taskInstanceDao.queryByWorkflowInstanceIdAndTaskCode(workflowInstance.getId(), taskCode);
            }
            if (taskInstance == null) {
                continue;
            }
            removeTaskInstances.add(taskInstance);
        }

        for (TaskInstance taskInstance : removeTaskInstances) {
            taskInstance.setFlag(Flag.NO);
            taskInstanceDao.updateById(taskInstance);
        }

        workflowInstance.setVarPool(
                VarPoolUtils.subtractVarPoolJson(workflowInstance.getVarPool(),
                        removeTaskInstances.stream().map(TaskInstance::getVarPool).collect(Collectors.toList())));
        processInstanceDao.updateById(workflowInstance);

        // remove task instance from taskInstanceMap,taskCodeInstanceMap , completeTaskSet, validTaskMap, errorTaskMap
        completeTaskSet.removeIf(dag::containsNode);
        taskCodeInstanceMap.entrySet().removeIf(entity -> dag.containsNode(entity.getValue().getTaskCode()));
        taskInstanceMap.entrySet().removeIf(entry -> dag.containsNode(entry.getValue().getTaskCode()));
        validTaskMap.entrySet().removeIf(entry -> dag.containsNode(entry.getKey()));
        errorTaskMap.entrySet().removeIf(entry -> dag.containsNode(entry.getKey()));
    }

    private void saveCacheTaskInstance(TaskInstance taskInstance) {
        Pair<Integer, String> taskIdAndCacheKey = TaskCacheUtils.revertCacheKey(taskInstance.getCacheKey());
        Integer taskId = taskIdAndCacheKey.getLeft();
        if (taskId.equals(taskInstance.getId())) {
            taskInstance.setCacheKey(taskIdAndCacheKey.getRight());
            try {
                taskInstanceDao.updateById(taskInstance);
            } catch (Exception e) {
                log.error("update task instance cache key failed", e);
            }
        }
    }

    private enum WorkflowRunnableStatus {
        CREATED, INITIALIZE_QUEUE, STARTED,
        ;

    }

    private void sendTaskLogOnMasterToRemoteIfNeeded(TaskInstance taskInstance) {
        if (RemoteLogUtils.isRemoteLoggingEnable() && TaskUtils.isMasterTask(taskInstance.getTaskType())) {
            RemoteLogUtils.sendRemoteLog(taskInstance.getLogPath());
            log.info("Master sends task log {} to remote storage asynchronously.", taskInstance.getLogPath());
        }
    }

}
