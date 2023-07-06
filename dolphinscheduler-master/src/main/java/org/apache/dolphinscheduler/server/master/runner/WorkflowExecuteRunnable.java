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
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_FATHER_PARAMS;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYY_MM_DD_HH_MM_SS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskCacheUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.task.TaskWakeupRequest;
import org.apache.dolphinscheduler.remote.command.task.WorkflowHostChangeRequest;
import org.apache.dolphinscheduler.remote.command.task.WorkflowHostChangeResponse;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.event.StateEventHandleError;
import org.apache.dolphinscheduler.server.master.event.StateEventHandleException;
import org.apache.dolphinscheduler.server.master.event.StateEventHandleFailure;
import org.apache.dolphinscheduler.server.master.event.StateEventHandler;
import org.apache.dolphinscheduler.server.master.event.StateEventHandlerManager;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowStateEvent;
import org.apache.dolphinscheduler.server.master.exception.TaskExecuteRunnableCreateException;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcClient;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.server.master.utils.TaskUtils;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessDag;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;
import org.apache.dolphinscheduler.service.utils.DagHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Workflow execute task, used to execute a workflow instance.
 */
@Slf4j
public class WorkflowExecuteRunnable implements Callable<WorkflowSubmitStatus> {

    private final ProcessService processService;

    private final CommandService commandService;

    private ProcessInstanceDao processInstanceDao;

    private TaskInstanceDao taskInstanceDao;

    private TaskDefinitionLogDao taskDefinitionLogDao;

    private final ProcessAlertManager processAlertManager;

    private final MasterRpcClient masterRpcClient;

    private final ProcessInstance processInstance;

    private ProcessDefinition processDefinition;

    private DAG<String, TaskNode, TaskNodeRelation> dag;

    /**
     * full task node map, key is task node id, value is task node
     * # TODO: This field can be removed later if the dag is complete
     */
    private Map<Long, TaskNode> taskNodesMap;

    /**
     * unique key of workflow
     */
    private String key;

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
     * forbidden task map, code as key
     */
    private final Map<Long, TaskNode> forbiddenTaskMap = new ConcurrentHashMap<>();

    /**
     * skip task map, code as key
     */
    private final Map<String, TaskNode> skipTaskNodeMap = new ConcurrentHashMap<>();

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
    private final PeerTaskInstancePriorityQueue readyToSubmitTaskQueue = new PeerTaskInstancePriorityQueue();

    /**
     * wait to retry taskInstance map, taskCode as key, taskInstance as value
     * before retry, the taskInstance id is 0
     */
    private final Map<Long, TaskInstance> waitToRetryTaskInstanceMap = new ConcurrentHashMap<>();

    private final StateWheelExecuteThread stateWheelExecuteThread;

    private final CuringParamsService curingParamsService;

    private final String masterAddress;

    private final DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory;

    /**
     * @param processInstance         processInstance
     * @param processService          processService
     * @param processInstanceDao      processInstanceDao
     * @param masterRpcClient         masterRpcClient
     * @param processAlertManager     processAlertManager
     * @param masterConfig            masterConfig
     * @param stateWheelExecuteThread stateWheelExecuteThread
     */
    public WorkflowExecuteRunnable(
                                   @NonNull ProcessInstance processInstance,
                                   @NonNull CommandService commandService,
                                   @NonNull ProcessService processService,
                                   @NonNull ProcessInstanceDao processInstanceDao,
                                   @NonNull MasterRpcClient masterRpcClient,
                                   @NonNull ProcessAlertManager processAlertManager,
                                   @NonNull MasterConfig masterConfig,
                                   @NonNull StateWheelExecuteThread stateWheelExecuteThread,
                                   @NonNull CuringParamsService curingParamsService,
                                   @NonNull TaskInstanceDao taskInstanceDao,
                                   @NonNull TaskDefinitionLogDao taskDefinitionLogDao,
                                   @NonNull DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory) {
        this.processService = processService;
        this.commandService = commandService;
        this.processInstanceDao = processInstanceDao;
        this.processInstance = processInstance;
        this.masterRpcClient = masterRpcClient;
        this.processAlertManager = processAlertManager;
        this.stateWheelExecuteThread = stateWheelExecuteThread;
        this.curingParamsService = curingParamsService;
        this.taskInstanceDao = taskInstanceDao;
        this.taskDefinitionLogDao = taskDefinitionLogDao;
        this.masterAddress = NetUtils.getAddr(masterConfig.getListenPort());
        this.defaultTaskExecuteRunnableFactory = defaultTaskExecuteRunnableFactory;
        this.processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        this.processInstance.setProcessDefinition(processDefinition);
        TaskMetrics.registerTaskPrepared(readyToSubmitTaskQueue::size);
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
            } catch (StateEventHandleFailure stateEventHandleFailure) {
                log.error("State event handle failed, will move event to the tail: {}",
                        stateEvent,
                        stateEventHandleFailure);
                this.stateEvents.remove(stateEvent);
                this.stateEvents.offer(stateEvent);
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

    public String getKey() {
        if (StringUtils.isNotEmpty(key) || this.processDefinition == null) {
            return key;
        }

        key = String.format("%d_%d_%d",
                this.processDefinition.getCode(),
                this.processDefinition.getVersion(),
                this.processInstance.getId());
        return key;
    }

    public boolean addStateEvent(StateEvent stateEvent) {
        if (processInstance.getId() != stateEvent.getProcessInstanceId()) {
            log.info("state event would be abounded :{}", stateEvent);
            return false;
        }
        this.stateEvents.add(stateEvent);
        return true;
    }

    public int eventSize() {
        return this.stateEvents.size();
    }

    public ProcessInstance getProcessInstance() {
        return this.processInstance;
    }

    public boolean checkForceStartAndWakeUp(StateEvent stateEvent) {
        TaskGroupQueue taskGroupQueue = processService.loadTaskGroupQueue(stateEvent.getTaskInstanceId());
        if (taskGroupQueue.getForceStart() == Flag.YES.getCode()) {
            log.info("Begin to force start taskGroupQueue: {}", taskGroupQueue.getId());
            TaskInstance taskInstance = taskInstanceDao.queryById(stateEvent.getTaskInstanceId());

            DefaultTaskExecuteRunnable defaultTaskExecuteRunnable =
                    taskExecuteRunnableMap.get(taskInstance.getTaskCode());
            if (defaultTaskExecuteRunnable != null) {
                defaultTaskExecuteRunnable.dispatch();
                this.processService.updateTaskGroupQueueStatus(taskGroupQueue.getTaskId(),
                        TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode());
                log.info("Success force start task: {}, taskGroup: {}", taskGroupQueue.getTaskName(),
                        taskGroupQueue.getGroupId());
            } else {
                log.warn("Cannot find the TaskExecuteRunnable: {}", taskGroupQueue.getTaskName());
            }
            return true;
        }
        if (taskGroupQueue.getInQueue() == Flag.YES.getCode()) {
            log.info("Begin to wake up taskGroupQueue: {}", taskGroupQueue.getId());
            boolean acquireTaskGroup = processService.robTaskGroupResource(taskGroupQueue);
            if (acquireTaskGroup) {
                TaskInstance taskInstance = taskInstanceDao.queryById(stateEvent.getTaskInstanceId());
                taskExecuteRunnableMap.get(taskInstance.getTaskCode()).dispatch();
                log.info("Success wake up taskGroupQueue: {}", taskGroupQueue.getId());
                return true;
            }
            log.warn("Failed to wake up taskGroupQueue, taskGroupQueueId: {}", taskGroupQueue.getId());
            return false;
        } else {
            log.info(
                    "Failed to wake up the taskGroupQueue: {}, since the taskGroupQueue is not in queue, will no need to wake up.",
                    taskGroupQueue);
            return true;
        }
    }

    public void processTimeout() {
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        this.processAlertManager.sendProcessTimeoutAlert(this.processInstance, projectUser);
    }

    public void taskTimeout(TaskInstance taskInstance) {
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        processAlertManager.sendTaskTimeoutAlert(processInstance, taskInstance, projectUser);
    }

    public void taskFinished(TaskInstance taskInstance) throws StateEventHandleException {
        log.info("TaskInstance finished task code:{} state:{}", taskInstance.getTaskCode(), taskInstance.getState());
        try {

            // Do we need to remove?
            taskExecuteRunnableMap.remove(taskInstance.getTaskCode());
            stateWheelExecuteThread.removeTask4TimeoutCheck(processInstance, taskInstance);
            stateWheelExecuteThread.removeTask4RetryCheck(processInstance, taskInstance);

            if (taskInstance.getState().isSuccess()) {
                completeTaskSet.add(taskInstance.getTaskCode());
                mergeTaskInstanceVarPool(taskInstance);
                processInstanceDao.upsertProcessInstance(processInstance);
                // save the cacheKey only if the task is defined as cache task and the task is success
                if (taskInstance.getIsCache().equals(Flag.YES)) {
                    saveCacheTaskInstance(taskInstance);
                }
                if (!processInstance.isBlocked()) {
                    submitPostNode(Long.toString(taskInstance.getTaskCode()));
                }
            } else if (taskInstance.taskCanRetry() && !processInstance.getState().isReadyStop()) {
                // retry task
                log.info("Retry taskInstance taskInstance state: {}", taskInstance.getState());
                retryTaskInstance(taskInstance);
            } else if (taskInstance.getState().isFailure()) {
                completeTaskSet.add(taskInstance.getTaskCode());
                // There are child nodes and the failure policy is: CONTINUE
                if (processInstance.getFailureStrategy() == FailureStrategy.CONTINUE && DagHelper.haveAllNodeAfterNode(
                        Long.toString(taskInstance.getTaskCode()),
                        dag)) {
                    submitPostNode(Long.toString(taskInstance.getTaskCode()));
                } else {
                    errorTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
                    if (processInstance.getFailureStrategy() == FailureStrategy.END) {
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

            sendTaskLogOnMasterToRemoteIfNeeded(taskInstance.getLogPath(), taskInstance.getHost());
        } catch (Exception ex) {
            log.error("Task finish failed, get a exception, will remove this taskInstance from completeTaskSet", ex);
            // remove the task from complete map, so that we can finish in the next time.
            completeTaskSet.remove(taskInstance.getTaskCode());
            throw ex;
        }
    }

    /**
     * release task group
     *
     */
    public void releaseTaskGroup(TaskInstance taskInstance) throws RemotingException, InterruptedException {
        // todo: use Integer
        if (taskInstance.getTaskGroupId() <= 0) {
            log.info("The current TaskInstance: {} doesn't use taskGroup, no need to release taskGroup",
                    taskInstance.getName());
        }
        TaskInstance nextTaskInstance = processService.releaseTaskGroup(taskInstance);
        if (nextTaskInstance == null) {
            log.info(
                    "The current TaskInstance: {} is the last taskInstance in the taskGroup, no need to wakeup next taskInstance",
                    taskInstance.getName());
            return;
        }
        if (nextTaskInstance.getProcessInstanceId() == taskInstance.getProcessInstanceId()) {
            TaskStateEvent nextEvent = TaskStateEvent.builder()
                    .processInstanceId(processInstance.getId())
                    .taskInstanceId(nextTaskInstance.getId())
                    .type(StateEventType.WAKE_UP_TASK_GROUP)
                    .build();
            stateEvents.add(nextEvent);
        } else {
            ProcessInstance processInstance =
                    processService.findProcessInstanceById(nextTaskInstance.getProcessInstanceId());
            masterRpcClient.sendSyncCommand(
                    Host.of(processInstance.getHost()),
                    new TaskWakeupRequest(processInstance.getId(), nextTaskInstance.getId()).convert2Command());
        }
        log.info("Success send wakeup message to next taskInstance: {}", nextTaskInstance.getId());
    }

    /**
     * crate new task instance to retry, different objects from the original
     *
     */
    private void retryTaskInstance(TaskInstance taskInstance) throws StateEventHandleException {
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
                    processInstance.getId(), newTaskInstance.getTaskCode(),
                    newTaskInstance.getState(), newTaskInstance.getRetryTimes(), newTaskInstance.getMaxRetryTimes(),
                    newTaskInstance.getRetryInterval());
            stateWheelExecuteThread.addTask4TimeoutCheck(processInstance, newTaskInstance);
            stateWheelExecuteThread.addTask4RetryCheck(processInstance, newTaskInstance);
        } else {
            addTaskToStandByList(newTaskInstance);
            submitStandByTask();
            waitToRetryTaskInstanceMap.remove(newTaskInstance.getTaskCode());
        }
    }

    /**
     * update process instance
     */
    public void refreshProcessInstance(int processInstanceId) {
        log.info("process instance update: {}", processInstanceId);
        ProcessInstance newProcessInstance = processService.findProcessInstanceById(processInstanceId);
        // just update the processInstance field(this is soft copy)
        BeanUtils.copyProperties(newProcessInstance, processInstance);

        processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        processInstance.setProcessDefinition(processDefinition);
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
        processService.packageTaskInstance(taskInstance, processInstance);
        taskInstanceMap.put(taskInstance.getId(), taskInstance);

        validTaskMap.remove(taskInstance.getTaskCode());
        if (Flag.YES == taskInstance.getFlag()) {
            validTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
        }
    }

    /**
     * check process instance by state event
     */
    public void checkProcessInstance(StateEvent stateEvent) throws StateEventHandleError {
        if (this.processInstance.getId() != stateEvent.getProcessInstanceId()) {
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
     * check if task instance exist by id
     */
    public boolean checkTaskInstanceById(int taskInstanceId) {
        if (taskInstanceMap.isEmpty()) {
            return false;
        }
        return taskInstanceMap.containsKey(taskInstanceId);
    }

    /**
     * get task instance from memory
     */
    public Optional<TaskInstance> getTaskInstance(int taskInstanceId) {
        return Optional.ofNullable(taskInstanceMap.get(taskInstanceId));
    }

    public Optional<TaskInstance> getTaskInstance(long taskCode) {
        if (taskInstanceMap.isEmpty()) {
            return Optional.empty();
        }
        for (TaskInstance taskInstance : taskInstanceMap.values()) {
            if (taskInstance.getTaskCode() == taskCode) {
                return Optional.of(taskInstance);
            }
        }
        return Optional.empty();
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
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        processAlertManager.sendProcessBlockingAlert(processInstance, projectUser);
        log.info("processInstance {} block alert send successful!", processInstance.getId());
    }

    public boolean processComplementData() {
        if (!needComplementProcess()) {
            return false;
        }

        // when the serial complement is executed, the next complement instance is created,
        // and this method does not need to be executed when the parallel complement is used.
        if (processInstance.getState().isReadyStop() || !processInstance.getState().isFinished()) {
            return false;
        }

        Date scheduleDate = processInstance.getScheduleTime();
        if (scheduleDate == null) {
            if (CollectionUtils.isEmpty(complementListDate)) {
                log.info("complementListDate is empty, process complement end. process id:{}", processInstance.getId());

                return true;
            }
            scheduleDate = complementListDate.get(0);
        } else if (processInstance.getState().isFinished()) {
            endProcess();
            if (complementListDate.isEmpty()) {
                log.info("process complement end. process id:{}", processInstance.getId());
                return true;
            }
            int index = complementListDate.indexOf(scheduleDate);
            if (index >= complementListDate.size() - 1 || !processInstance.getState().isSuccess()) {
                log.info("process complement end. process id:{}", processInstance.getId());
                // complement data ends || no success
                return true;
            }
            log.info("process complement continue. process id:{}, schedule time:{} complementListDate:{}",
                    processInstance.getId(), processInstance.getScheduleTime(), complementListDate);
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
        Command command = new Command();
        command.setScheduleTime(scheduleDate);
        command.setCommandType(CommandType.COMPLEMENT_DATA);
        command.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
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
        command.setTaskDependType(processInstance.getTaskDependType());
        command.setFailureStrategy(processInstance.getFailureStrategy());
        command.setWarningType(processInstance.getWarningType());
        command.setWarningGroupId(processInstance.getWarningGroupId());
        command.setStartTime(new Date());
        command.setExecutorId(processInstance.getExecutorId());
        command.setUpdateTime(new Date());
        command.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        command.setWorkerGroup(processInstance.getWorkerGroup());
        command.setEnvironmentCode(processInstance.getEnvironmentCode());
        command.setDryRun(processInstance.getDryRun());
        command.setProcessInstanceId(0);
        command.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
        command.setTestFlag(processInstance.getTestFlag());
        int create = commandService.createCommand(command);
        processService.saveCommandTrigger(command.getId(), processInstance.getId());
        return create;
    }

    private boolean needComplementProcess() {
        return processInstance.isComplementData() && Flag.NO == processInstance.getIsSubProcess();
    }

    /**
     * ProcessInstance start entrypoint.
     */
    @Override
    public WorkflowSubmitStatus call() {

        try {
            LogUtils.setWorkflowInstanceIdMDC(processInstance.getId());
            if (isStart()) {
                // This case should not been happened
                log.warn("The workflow has already been started, current state: {}", workflowRunnableStatus);
                return WorkflowSubmitStatus.DUPLICATED_SUBMITTED;
            }
            if (workflowRunnableStatus == WorkflowRunnableStatus.CREATED) {
                buildFlowDag();
                workflowRunnableStatus = WorkflowRunnableStatus.INITIALIZE_DAG;
                log.info("workflowStatue changed to :{}", workflowRunnableStatus);
            }
            if (workflowRunnableStatus == WorkflowRunnableStatus.INITIALIZE_DAG) {
                initTaskQueue();
                workflowRunnableStatus = WorkflowRunnableStatus.INITIALIZE_QUEUE;
                log.info("workflowStatue changed to :{}", workflowRunnableStatus);
            }
            if (workflowRunnableStatus == WorkflowRunnableStatus.INITIALIZE_QUEUE) {
                submitPostNode(null);
                workflowRunnableStatus = WorkflowRunnableStatus.STARTED;
                log.info("workflowStatue changed to :{}", workflowRunnableStatus);
            }
            return WorkflowSubmitStatus.SUCCESS;
        } catch (Exception e) {
            log.error("Start workflow error", e);
            return WorkflowSubmitStatus.FAILED;
        } finally {
            LogUtils.removeWorkflowInstanceIdMDC();
        }
    }

    /**
     * process end handle
     */
    public void endProcess() {
        this.stateEvents.clear();
        if (processDefinition.getExecutionType().typeIsSerialWait() || processDefinition.getExecutionType()
                .typeIsSerialPriority()) {
            checkSerialProcess(processDefinition);
        }
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        processAlertManager.sendAlertProcessInstance(processInstance, getValidTaskList(), projectUser);
        if (processInstance.getState().isSuccess()) {
            processAlertManager.closeAlert(processInstance);
        }
        if (checkTaskQueue()) {
            // release task group
            processService.releaseAllTaskGroup(processInstance.getId());
        }
    }

    public void checkSerialProcess(ProcessDefinition processDefinition) {
        int nextInstanceId = processInstance.getNextProcessInstanceId();
        if (nextInstanceId == 0) {
            ProcessInstance nextProcessInstance =
                    this.processService.loadNextProcess4Serial(processInstance.getProcessDefinition().getCode(),
                            WorkflowExecutionStatus.SERIAL_WAIT.getCode(), processInstance.getId());
            if (nextProcessInstance == null) {
                return;
            }
            ProcessInstance nextReadyStopProcessInstance =
                    this.processService.loadNextProcess4Serial(processInstance.getProcessDefinition().getCode(),
                            WorkflowExecutionStatus.READY_STOP.getCode(), processInstance.getId());
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
        cmdParam.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, nextInstanceId);
        Command command = new Command();
        command.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command.setProcessInstanceId(nextProcessInstance.getId());
        command.setProcessDefinitionCode(processDefinition.getCode());
        command.setProcessDefinitionVersion(processDefinition.getVersion());
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        commandService.createCommand(command);
    }

    // todo: move the initialize code to constructor
    private void buildFlowDag() throws Exception {

        List<TaskInstance> recoverNodeList = getRecoverTaskInstanceList(processInstance.getCommandParam());

        List<ProcessTaskRelation> processTaskRelations =
                processService.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskDefinitionLog> taskDefinitionLogs =
                taskDefinitionLogDao.queryTaskDefineLogList(processTaskRelations);
        List<TaskNode> taskNodeList = processService.transformTask(processTaskRelations, taskDefinitionLogs);
        forbiddenTaskMap.clear();

        taskNodeList.forEach(taskNode -> {
            if (taskNode.isForbidden()) {
                forbiddenTaskMap.put(taskNode.getCode(), taskNode);
            }
        });

        taskNodesMap = taskNodeList.stream().collect(Collectors.toMap(TaskNode::getCode, taskNode -> taskNode));

        // generate process to get DAG info
        List<String> recoveryNodeCodeList = getRecoveryNodeCodeList(recoverNodeList);
        List<String> startNodeNameList = parseStartNodeName(processInstance.getCommandParam());
        ProcessDag processDag = generateFlowDag(taskNodeList, startNodeNameList, recoveryNodeCodeList,
                processInstance.getTaskDependType());
        if (processDag == null) {
            log.error("ProcessDag is null");
            return;
        }
        // generate process dag
        dag = DagHelper.buildDagGraph(processDag);
        log.info("Build dag success, dag: {}", dag);
    }

    /**
     * init task queue
     */
    private void initTaskQueue() throws StateEventHandleException, CronParseException {

        taskFailedSubmit = false;
        // do we need to clear?
        taskExecuteRunnableMap.clear();
        dependFailedTaskSet.clear();
        completeTaskSet.clear();
        errorTaskMap.clear();

        if (!isNewProcessInstance()) {
            log.info("The workflowInstance is not a newly running instance, runtimes: {}, recover flag: {}",
                    processInstance.getRunTimes(),
                    processInstance.getRecovery());
            List<TaskInstance> validTaskInstanceList =
                    taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                            processInstance.getTestFlag());
            for (TaskInstance task : validTaskInstanceList) {
                try (
                        final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                                LogUtils.setWorkflowAndTaskInstanceIDMDC(task.getProcessInstanceId(), task.getId());) {
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

                    validTaskMap.put(task.getTaskCode(), task.getId());
                    taskInstanceMap.put(task.getId(), task);

                    if (task.isTaskComplete()) {
                        log.info("TaskInstance is already complete.");
                        completeTaskSet.add(task.getTaskCode());
                        continue;
                    }
                    if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()),
                            dag)) {
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
                }
            }
            clearDataIfExecuteTask();
        } else {
            log.info("The current workflowInstance is a newly running workflowInstance");
        }

        if (processInstance.isComplementData() && complementListDate.isEmpty()) {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            if (cmdParam != null) {
                // reset global params while there are start parameters
                setGlobalParamIfCommanded(processDefinition, cmdParam);

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
                                processInstance.getProcessDefinitionCode());
                        complementListDate = CronUtils.getSelfFireDateList(start, end, schedules);
                    }
                    if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
                        complementListDate = CronUtils.getSelfScheduleDateList(cmdParam);
                    }
                    log.info(" process definition code:{} complement data: {}",
                            processInstance.getProcessDefinitionCode(), complementListDate);

                    if (!complementListDate.isEmpty() && Flag.NO == processInstance.getIsSubProcess()) {
                        processInstance.setScheduleTime(complementListDate.get(0));
                        String globalParams = curingParamsService.curingGlobalParams(processInstance.getId(),
                                processDefinition.getGlobalParamMap(),
                                processDefinition.getGlobalParamList(),
                                CommandType.COMPLEMENT_DATA,
                                processInstance.getScheduleTime(),
                                cmdParam.get(Constants.SCHEDULE_TIMEZONE));
                        processInstance.setGlobalParams(globalParams);
                        processInstanceDao.updateById(processInstance);
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
            // package task instance before submit, inject the process instance to task instance
            // todo: we need to use task execute context rather than packege a lot of pojo into task instance
            // 1. submit the task instance to db
            processService.packageTaskInstance(taskInstance, processInstance);
            // todo: remove this method
            if (!processService.submitTask(processInstance, taskInstance)) {
                log.error("Submit standby task: {} failed", taskInstance.getName());
                return true;
            }
            // 2. create task execute runnable
            // in a dag, only one taskInstance is valid per taskCode, so need to set the old taskInstance invalid
            try (
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setTaskInstanceIdMDC(taskInstance.getId())) {
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
                taskExecuteRunnableMap.put(taskInstance.getTaskCode(), taskExecuteRunnable);
                // 3. acquire the task group.
                // if we use task group, then need to acquire the task group resource
                // if there is no resource the current task instance will not be dispatched
                // it will be wakeup when other tasks release the resource.
                int taskGroupId = taskInstance.getTaskGroupId();
                if (taskGroupId > 0) {
                    boolean acquireTaskGroup = processService.acquireTaskGroup(taskInstance.getId(),
                            taskInstance.getName(),
                            taskGroupId,
                            taskInstance.getProcessInstanceId(),
                            taskInstance.getTaskGroupPriority());
                    if (!acquireTaskGroup) {
                        log.info(
                                "Submitted task will not be dispatch right now because the first time to try to acquire"
                                        +
                                        " task group failed, taskInstanceName: {}, taskGroupId: {}",
                                taskInstance.getName(), taskGroupId);
                        return true;
                    }
                }
                // 4. submit to dispatch queue
                taskExecuteRunnable.dispatch();

                stateWheelExecuteThread.addTask4TimeoutCheck(processInstance, taskInstance);
                return true;
            }
        } catch (Exception e) {
            log.error("Submit standby task {} error", taskInstance.getName(), e);
            return false;
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
        TaskNode taskNode = dag.getNode(Long.toString(taskInstance.getTaskCode()));
        if (taskNode == null) {
            log.error("Clone retry taskInstance error because taskNode is null, taskCode:{}",
                    taskInstance.getTaskCode());
            return null;
        }
        TaskInstance newTaskInstance = newTaskInstance(processInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(processInstance);
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
        TaskNode taskNode = dag.getNode(Long.toString(taskInstance.getTaskCode()));
        if (taskNode == null) {
            log.error("Clone tolerant taskInstance error because taskNode is null, taskCode:{}",
                    taskInstance.getTaskCode());
            return null;
        }
        TaskInstance newTaskInstance = newTaskInstance(processInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(processInstance);
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
     * @param taskNode task node
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

    public void getPreVarPool(TaskInstance taskInstance, Set<String> preTask) {
        Map<String, Property> allProperty = new HashMap<>();
        Map<String, TaskInstance> allTaskInstance = new HashMap<>();
        if (CollectionUtils.isNotEmpty(preTask)) {
            for (String preTaskCode : preTask) {
                Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(Long.parseLong(preTaskCode));
                if (!existTaskInstanceOptional.isPresent()) {
                    continue;
                }

                Integer taskId = existTaskInstanceOptional.get().getId();
                if (taskId == null) {
                    continue;
                }
                TaskInstance preTaskInstance = taskInstanceMap.get(taskId);
                if (preTaskInstance == null) {
                    continue;
                }
                String preVarPool = preTaskInstance.getVarPool();
                if (StringUtils.isNotEmpty(preVarPool)) {
                    List<Property> properties = JSONUtils.toList(preVarPool, Property.class);
                    for (Property info : properties) {
                        setVarPoolValue(allProperty, allTaskInstance, preTaskInstance, info);
                    }
                }
            }
            if (allProperty.size() > 0) {
                taskInstance.setVarPool(JSONUtils.toJsonString(allProperty.values()));
            }
        } else {
            if (StringUtils.isNotEmpty(processInstance.getVarPool())) {
                taskInstance.setVarPool(processInstance.getVarPool());
            }
        }
    }

    public Collection<TaskInstance> getAllTaskInstances() {
        return taskInstanceMap.values();
    }

    private void setVarPoolValue(Map<String, Property> allProperty, Map<String, TaskInstance> allTaskInstance,
                                 TaskInstance preTaskInstance, Property thisProperty) {
        // for this taskInstance all the param in this part is IN.
        thisProperty.setDirect(Direct.IN);
        // get the pre taskInstance Property's name
        String proName = thisProperty.getProp();
        // if the Previous nodes have the Property of same name
        if (allProperty.containsKey(proName)) {
            // comparison the value of two Property
            Property otherPro = allProperty.get(proName);
            // if this property'value of loop is empty,use the other,whether the other's value is empty or not
            if (StringUtils.isEmpty(thisProperty.getValue())) {
                allProperty.put(proName, otherPro);
                // if property'value of loop is not empty,and the other's value is not empty too, use the earlier value
            } else if (StringUtils.isNotEmpty(otherPro.getValue())) {
                TaskInstance otherTask = allTaskInstance.get(proName);
                if (otherTask.getEndTime().getTime() > preTaskInstance.getEndTime().getTime()) {
                    allProperty.put(proName, thisProperty);
                    allTaskInstance.put(proName, preTaskInstance);
                } else {
                    allProperty.put(proName, otherPro);
                }
            } else {
                allProperty.put(proName, thisProperty);
                allTaskInstance.put(proName, preTaskInstance);
            }
        } else {
            allProperty.put(proName, thisProperty);
            allTaskInstance.put(proName, preTaskInstance);
        }
    }

    /**
     * get complete task instance map, taskCode as key
     */
    private Map<String, TaskInstance> getCompleteTaskInstanceMap() {
        Map<String, TaskInstance> completeTaskInstanceMap = new HashMap<>();

        completeTaskSet.forEach(taskCode -> {
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskCode);
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance taskInstance = taskInstanceMap.get(existTaskInstanceOptional.get().getId());
                if (taskInstance == null) {
                    // This case will happen when we submit to db failed, then the taskInstanceId is 0
                    log.warn("Cannot find the taskInstance from taskInstanceMap, taskConde: {}",
                            taskCode);
                } else {
                    completeTaskInstanceMap.put(Long.toString(taskCode), taskInstance);
                }
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

    private void submitPostNode(String parentNodeCode) throws StateEventHandleException {
        Set<String> submitTaskNodeList =
                DagHelper.parsePostNodes(parentNodeCode, skipTaskNodeMap, dag, getCompleteTaskInstanceMap());
        List<TaskInstance> taskInstances = new ArrayList<>();
        for (String taskNode : submitTaskNodeList) {
            TaskNode taskNodeObject = dag.getNode(taskNode);
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskNodeObject.getCode());
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance existTaskInstance = existTaskInstanceOptional.get();
                if (existTaskInstance.getState() == TaskExecutionStatus.RUNNING_EXECUTION
                        || existTaskInstance.getState() == TaskExecutionStatus.DISPATCH) {
                    // try to take over task instance
                    if (tryToTakeOverTaskInstance(existTaskInstance)) {
                        log.info("Success take over task {}", existTaskInstance.getName());
                        continue;
                    } else {
                        // set the task instance state to fault tolerance
                        existTaskInstance.setFlag(Flag.NO);
                        existTaskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
                        validTaskMap.remove(existTaskInstance.getTaskCode());
                        taskInstanceDao.updateById(existTaskInstance);
                        existTaskInstance = cloneTolerantTaskInstance(existTaskInstance);
                        log.info("task {} cannot be take over will generate a tolerant task instance",
                                existTaskInstance.getName());
                    }
                }
                taskInstances.add(existTaskInstance);
            } else {
                taskInstances.add(createTaskInstance(processInstance, taskNodeObject));
            }
        }
        // the end node of the branch of the dag
        if (StringUtils.isNotEmpty(parentNodeCode) && dag.getEndNode().contains(parentNodeCode)) {
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(NumberUtils.toLong(parentNodeCode));
            if (existTaskInstanceOptional.isPresent()) {
                TaskInstance endTaskInstance = taskInstanceMap.get(existTaskInstanceOptional.get().getId());
                String taskInstanceVarPool = endTaskInstance.getVarPool();
                if (StringUtils.isNotEmpty(taskInstanceVarPool)) {
                    Set<Property> taskProperties = new HashSet<>(JSONUtils.toList(taskInstanceVarPool, Property.class));
                    String processInstanceVarPool = processInstance.getVarPool();
                    if (StringUtils.isNotEmpty(processInstanceVarPool)) {
                        Set<Property> properties =
                                new HashSet<>(JSONUtils.toList(processInstanceVarPool, Property.class));
                        properties.addAll(taskProperties);
                        processInstance.setVarPool(JSONUtils.toJsonString(properties));
                    } else {
                        processInstance.setVarPool(JSONUtils.toJsonString(taskProperties));
                    }
                }
            }
        }

        // if previous node success , post node submit
        for (TaskInstance task : taskInstances) {

            if (readyToSubmitTaskQueue.contains(task)) {
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
        if (TaskUtils.isMasterTask(taskInstance.getTaskType())) {
            return false;
        }
        try {
            Message message =
                    masterRpcClient.sendSyncCommand(Host.of(taskInstance.getHost()),
                            new WorkflowHostChangeRequest(taskInstance.getId(), masterAddress).convert2Command());
            if (message == null) {
                log.error(
                        "Takeover task instance failed, the worker {} might not be alive, will try to create a new task instance",
                        taskInstance.getHost());
                return false;
            }
            WorkflowHostChangeResponse workflowHostChangeResponse =
                    JSONUtils.parseObject(message.getBody(), WorkflowHostChangeResponse.class);
            if (workflowHostChangeResponse == null || !workflowHostChangeResponse.isSuccess()) {
                log.error(
                        "Takeover task instance failed, receive a failed response from worker: {}, will try to create a new task instance",
                        taskInstance.getHost());
                return false;
            }

            // todo: create the takeover task execute runnable.
            taskExecuteRunnableMap.put(taskInstance.getTaskCode(),
                    defaultTaskExecuteRunnableFactory.createTaskExecuteRunnable(taskInstance));

            taskInstanceMap.put(taskInstance.getId(), taskInstance);
            stateWheelExecuteThread.addTask4TimeoutCheck(processInstance, taskInstance);
            stateWheelExecuteThread.addTask4RetryCheck(processInstance, taskInstance);
            return true;
        } catch (RemotingException | InterruptedException | TaskExecuteRunnableCreateException e) {
            log.error(
                    "Takeover task instance failed, the worker {} might not be alive, will try to create a new task instance",
                    taskInstance.getHost(), e);
            return false;
        }
    }

    /**
     * determine whether the dependencies of the task node are complete
     *
     * @return DependResult
     */
    private DependResult isTaskDepsComplete(String taskCode) {

        Collection<String> startNodes = dag.getBeginNode();
        // if vertex,returns true directly
        if (startNodes.contains(taskCode)) {
            return DependResult.SUCCESS;
        }
        TaskNode taskNode = dag.getNode(taskCode);
        List<String> indirectDepCodeList = new ArrayList<>();
        setIndirectDepList(taskCode, indirectDepCodeList);
        for (String depsNode : indirectDepCodeList) {
            if (dag.containsNode(depsNode) && !skipTaskNodeMap.containsKey(depsNode)) {
                // dependencies must be fully completed
                long despNodeTaskCode = Long.parseLong(depsNode);
                if (!completeTaskSet.contains(despNodeTaskCode)) {
                    return DependResult.WAITING;
                }

                Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(despNodeTaskCode);
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
                taskCode, Arrays.toString(completeTaskSet.toArray()));
        return DependResult.SUCCESS;
    }

    /**
     * This function is specially used to handle the dependency situation where the parent node is a prohibited node.
     * When the parent node is a forbidden node, the dependency relationship should continue to be traced
     *
     * @param taskCode            taskCode
     * @param indirectDepCodeList All indirectly dependent nodes
     */
    private void setIndirectDepList(String taskCode, List<String> indirectDepCodeList) {
        TaskNode taskNode = dag.getNode(taskCode);
        // If workflow start with startNode or recoveryNode, taskNode may be null
        if (taskNode == null) {
            return;
        }
        for (String depsNode : taskNode.getDepList()) {
            if (forbiddenTaskMap.containsKey(Long.parseLong(depsNode))) {
                setIndirectDepList(depsNode, indirectDepCodeList);
            } else {
                indirectDepCodeList.add(depsNode);
            }
        }
    }

    /**
     * depend node is completed, but here need check the condition task branch is the next node
     */
    private boolean dependTaskSuccess(String dependNodeCode, String nextNodeCode) {
        TaskNode dependentNode = dag.getNode(dependNodeCode);
        if (dependentNode.isConditionsTask()) {
            // condition task need check the branch to run
            List<String> nextTaskList =
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
        long taskCode = Long.parseLong(dependNodeCode);
        Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskCode);
        if (!existTaskInstanceOptional.isPresent()) {
            return false;
        }

        TaskExecutionStatus depTaskState = taskInstanceMap.get(existTaskInstanceOptional.get().getId()).getState();
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
                TaskInstance taskInstance = taskInstanceMap.get(existTaskInstanceOptional.get().getId());
                if (taskInstance != null && taskInstance.getState() == state) {
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
        if (hasFailedTask()) {
            log.info("The current process has failed task, the current process failed");
            if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                return true;
            }
            if (processInstance.getFailureStrategy() == FailureStrategy.CONTINUE) {
                return readyToSubmitTaskQueue.size() == 0 && taskExecuteRunnableMap.size() == 0
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
        if (hasRetryTaskInStandBy()) {
            return WorkflowExecutionStatus.FAILURE;
        }

        List<TaskInstance> pauseList = getCompleteTaskByState(TaskExecutionStatus.PAUSE);
        if (CollectionUtils.isNotEmpty(pauseList) || processInstance.isBlocked() || !isComplementEnd()
                || readyToSubmitTaskQueue.size() > 0) {
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
        if (readyToSubmitTaskQueue.size() > 0) {
            for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext();) {
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
            if (readyToSubmitTaskQueue.size() > 0 || waitToRetryTaskInstanceMap.size() > 0) {
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
        if (!processInstance.isComplementData()) {
            return true;
        }

        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        Date endTime = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
        return processInstance.getScheduleTime().equals(endTime);
    }

    /**
     * updateProcessInstance process instance state
     * after each batch of tasks is executed, the status of the process instance is updated
     */
    private void updateProcessInstanceState() throws StateEventHandleException {
        WorkflowExecutionStatus state = getProcessInstanceState(processInstance);
        if (processInstance.getState() != state) {
            log.info("Update workflowInstance states, origin state: {}, target state: {}",
                    processInstance.getState(),
                    state);
            updateWorkflowInstanceStatesToDB(state);

            WorkflowStateEvent stateEvent = WorkflowStateEvent.builder()
                    .processInstanceId(processInstance.getId())
                    .status(processInstance.getState())
                    .type(StateEventType.PROCESS_STATE_CHANGE)
                    .build();
            // replace with `stateEvents`, make sure `WorkflowExecuteThread` can be deleted to avoid memory leaks
            this.stateEvents.add(stateEvent);
        } else {
            log.info("There is no need to update the workflow instance state, origin state: {}, target state: {}",
                    processInstance.getState(),
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
        WorkflowExecutionStatus originStates = processInstance.getState();
        if (originStates != newStates) {
            log.info("Begin to update workflow instance state , state will change from {} to {}",
                    originStates,
                    newStates);

            processInstance.setStateWithDesc(newStates, "update by workflow executor");
            if (newStates.isFinished()) {
                processInstance.setEndTime(new Date());
            }
            try {
                processInstanceDao.updateById(processInstance);
            } catch (Exception ex) {
                // recover the status
                processInstance.setStateWithDesc(originStates, "recover state by DB error");
                processInstance.setEndTime(null);
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
        return isTaskDepsComplete(Long.toString(taskInstance.getTaskCode()));
    }

    /**
     * add task to standby list
     *
     * @param taskInstance task instance
     */
    public void addTaskToStandByList(TaskInstance taskInstance) {
        if (readyToSubmitTaskQueue.contains(taskInstance)) {
            log.warn("Task already exists in ready submit queue, no need to add again, task code:{}",
                    taskInstance.getTaskCode());
            return;
        }
        log.info("Add task to stand by list, task name:{}, task id:{}, task code:{}",
                taskInstance.getName(),
                taskInstance.getId(),
                taskInstance.getTaskCode());
        TaskMetrics.incTaskInstanceByState("submit");
        readyToSubmitTaskQueue.put(taskInstance);
    }

    /**
     * remove task from stand by list
     *
     * @param taskInstance task instance
     */
    private boolean removeTaskFromStandbyList(TaskInstance taskInstance) {
        return readyToSubmitTaskQueue.remove(taskInstance);
    }

    /**
     * has retry task in standby
     *
     * @return Boolean whether has retry task in standby
     */
    private boolean hasRetryTaskInStandBy() {
        for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext();) {
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
        log.info("kill called on process instance id: {}, num: {}",
                processInstance.getId(),
                taskExecuteRunnableMap.size());

        if (readyToSubmitTaskQueue.size() > 0) {
            readyToSubmitTaskQueue.clear();
        }

        for (long taskCode : taskExecuteRunnableMap.keySet()) {
            Integer taskInstanceId = validTaskMap.get(taskCode);
            if (taskInstanceId == null || taskInstanceId.equals(0)) {
                continue;
            }
            try (
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowAndTaskInstanceIDMDC(processInstance.getId(), taskInstanceId)) {
                TaskInstance taskInstance = taskInstanceDao.queryById(taskInstanceId);
                if (taskInstance == null || taskInstance.getState().isFinished()) {
                    continue;
                }
                DefaultTaskExecuteRunnable defaultTaskExecuteRunnable = taskExecuteRunnableMap.get(taskCode);
                defaultTaskExecuteRunnable.kill();
                if (defaultTaskExecuteRunnable.getTaskInstance().getState().isFinished()) {
                    TaskStateEvent taskStateEvent = TaskStateEvent.builder()
                            .processInstanceId(processInstance.getId())
                            .taskInstanceId(taskInstance.getId())
                            .status(defaultTaskExecuteRunnable.getTaskInstance().getState())
                            .type(StateEventType.TASK_STATE_CHANGE)
                            .build();
                    this.addStateEvent(taskStateEvent);
                }
            }
        }
    }

    public boolean workFlowFinish() {
        return this.processInstance.getState().isFinished();
    }

    /**
     * handling the list of tasks to be submitted
     */
    public void submitStandByTask() throws StateEventHandleException {
        TaskInstance task;
        while ((task = readyToSubmitTaskQueue.peek()) != null) {
            // stop tasks which is retrying if forced success happens
            if (task.taskCanRetry()) {
                TaskInstance retryTask = taskInstanceDao.queryById(task.getId());
                if (retryTask != null && retryTask.getState().isForceSuccess()) {
                    task.setState(retryTask.getState());
                    log.info(
                            "Task {} has been forced success, put it into complete task list and stop retrying, taskInstanceId: {}",
                            task.getName(), task.getId());
                    removeTaskFromStandbyList(task);
                    completeTaskSet.add(task.getTaskCode());
                    taskInstanceMap.put(task.getId(), task);
                    submitPostNode(Long.toString(task.getTaskCode()));
                    continue;
                }
            }
            // init varPool only this task is the first time running
            if (task.isFirstRun()) {
                // get pre task ,get all the task varPool to this task
                // Do not use dag.getPreviousNodes because of the dag may be miss the upstream node
                String preTasks = taskNodesMap.get(task.getTaskCode()).getPreTasks();
                Set<String> preTaskList = new HashSet<>(JSONUtils.toList(preTasks, String.class));
                getPreVarPool(task, preTaskList);
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
                                processInstance.getId(),
                                task.getTaskCode());
                    }
                    completeTaskSet.add(task.getTaskCode());
                    taskInstanceMap.put(task.getId(), task);
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

    /**
     * generate flow dag
     *
     * @param totalTaskNodeList    total task node list
     * @param startNodeNameList    start node name list
     * @param recoveryNodeCodeList recovery node code list
     * @param depNodeType          depend node type
     * @return ProcessDag           process dag
     * @throws Exception exception
     */
    public ProcessDag generateFlowDag(List<TaskNode> totalTaskNodeList,
                                      List<String> startNodeNameList,
                                      List<String> recoveryNodeCodeList,
                                      TaskDependType depNodeType) throws Exception {
        return DagHelper.generateFlowDag(totalTaskNodeList, startNodeNameList, recoveryNodeCodeList, depNodeType);
    }

    /**
     * check task queue
     */
    private boolean checkTaskQueue() {
        AtomicBoolean result = new AtomicBoolean(false);
        taskInstanceMap.forEach((id, taskInstance) -> {
            if (taskInstance != null && taskInstance.getTaskGroupId() > 0) {
                result.set(true);
            }
        });
        return result.get();
    }

    /**
     * is new process instance
     */
    private boolean isNewProcessInstance() {
        if (Flag.YES.equals(processInstance.getRecovery())) {
            log.info("This workInstance will be recover by this execution");
            return false;
        }

        if (WorkflowExecutionStatus.RUNNING_EXECUTION == processInstance.getState()
                && processInstance.getRunTimes() == 1) {
            return true;
        }
        log.info(
                "The workflowInstance has been executed before, this execution is to reRun, processInstance status: {}, runTimes: {}",
                processInstance.getState(),
                processInstance.getRunTimes());
        return false;
    }

    public Set<Long> getCompleteTaskCodes() {
        return completeTaskSet;
    }

    public Map<Long, DefaultTaskExecuteRunnable> getTaskExecuteRunnableMap() {
        return taskExecuteRunnableMap;
    }

    public Map<Long, TaskInstance> getWaitToRetryTaskInstanceMap() {
        return waitToRetryTaskInstanceMap;
    }

    private void setGlobalParamIfCommanded(ProcessDefinition processDefinition, Map<String, String> cmdParam) {
        // get start params from command param
        Map<String, String> startParamMap = new HashMap<>();
        if (cmdParam.containsKey(CMD_PARAM_START_PARAMS)) {
            String startParamJson = cmdParam.get(CMD_PARAM_START_PARAMS);
            startParamMap = JSONUtils.toMap(startParamJson);
        }
        Map<String, String> fatherParamMap = new HashMap<>();
        if (cmdParam.containsKey(CMD_PARAM_FATHER_PARAMS)) {
            String fatherParamJson = cmdParam.get(CMD_PARAM_FATHER_PARAMS);
            fatherParamMap = JSONUtils.toMap(fatherParamJson);
        }
        startParamMap.putAll(fatherParamMap);
        // set start param into global params
        Map<String, String> globalMap = processDefinition.getGlobalParamMap();
        List<Property> globalParamList = processDefinition.getGlobalParamList();
        if (startParamMap.size() > 0 && globalMap != null) {
            // start param to overwrite global param
            for (Map.Entry<String, String> param : globalMap.entrySet()) {
                String val = startParamMap.get(param.getKey());
                if (val != null) {
                    param.setValue(val);
                }
            }
            // start param to create new global param if global not exist
            for (Map.Entry<String, String> startParam : startParamMap.entrySet()) {
                if (!globalMap.containsKey(startParam.getKey())) {
                    globalMap.put(startParam.getKey(), startParam.getValue());
                    globalParamList.add(new Property(startParam.getKey(), IN, VARCHAR, startParam.getValue()));
                }
            }
        }
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
        // only clear data if command is EXECUTE_TASK
        if (!processInstance.getCommandType().equals(CommandType.EXECUTE_TASK)) {
            return;
        }

        // Records the key of varPool data to be removed
        Set<String> taskCodesString = dag.getAllNodesList();

        List<TaskInstance> removeTaskInstances = new ArrayList<>();

        for (String taskCodeString : taskCodesString) {
            long taskCode = Long.parseLong(taskCodeString);
            TaskInstance taskInstance;
            if (validTaskMap.containsKey(taskCode)) {
                taskInstance = taskInstanceMap.get(validTaskMap.get(taskCode));
            } else {
                taskInstance = taskInstanceDao.queryByWorkflowInstanceIdAndTaskCode(processInstance.getId(), taskCode);
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

        Set<String> removeSet = new HashSet<>();
        for (TaskInstance taskInstance : removeTaskInstances) {
            String taskVarPool = taskInstance.getVarPool();
            if (StringUtils.isNotEmpty(taskVarPool)) {
                List<Property> properties = JSONUtils.toList(taskVarPool, Property.class);
                List<String> keys = properties.stream()
                        .filter(property -> property.getDirect().equals(Direct.OUT))
                        .map(property -> String.format("%s_%s", property.getProp(), property.getType()))
                        .collect(Collectors.toList());
                removeSet.addAll(keys);
            }
        }

        // remove varPool data and update process instance
        // TODO: we can remove this snippet if : we get varPool from pre taskInstance instead of process instance when
        // task can not get pre task from incomplete dag
        List<Property> processProperties = JSONUtils.toList(processInstance.getVarPool(), Property.class);
        processProperties = processProperties.stream()
                .filter(property -> !(property.getDirect().equals(Direct.IN)
                        && removeSet.contains(String.format("%s_%s", property.getProp(), property.getType()))))
                .collect(Collectors.toList());

        processInstance.setVarPool(JSONUtils.toJsonString(processProperties));
        processInstanceDao.updateById(processInstance);

        // remove task instance from taskInstanceMap, completeTaskSet, validTaskMap, errorTaskMap
        // completeTaskSet remove dependency taskInstanceMap, so the sort can't change
        completeTaskSet.removeIf(taskCode -> {
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskCode);
            return existTaskInstanceOptional
                    .filter(taskInstance -> dag.containsNode(Long.toString(taskInstance.getTaskCode()))).isPresent();
        });
        taskInstanceMap.entrySet().removeIf(entry -> dag.containsNode(Long.toString(entry.getValue().getTaskCode())));
        validTaskMap.entrySet().removeIf(entry -> dag.containsNode(Long.toString(entry.getKey())));
        errorTaskMap.entrySet().removeIf(entry -> dag.containsNode(Long.toString(entry.getKey())));
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
        CREATED, INITIALIZE_DAG, INITIALIZE_QUEUE, STARTED,
        ;

    }

    private void sendTaskLogOnMasterToRemoteIfNeeded(String logPath, String host) {
        if (RemoteLogUtils.isRemoteLoggingEnable() && isExecutedOnMaster(host)) {
            RemoteLogUtils.sendRemoteLog(logPath);
            log.info("Master sends task log {} to remote storage asynchronously.", logPath);
        }
    }

    private boolean isExecutedOnMaster(String host) {
        return host.endsWith(masterAddress.split(Constants.COLON)[1]);
    }

    private void mergeTaskInstanceVarPool(TaskInstance taskInstance) {
        String taskVarPoolJson = taskInstance.getVarPool();
        if (StringUtils.isEmpty(taskVarPoolJson)) {
            return;
        }
        String processVarPoolJson = processInstance.getVarPool();
        if (StringUtils.isEmpty(processVarPoolJson)) {
            processInstance.setVarPool(taskVarPoolJson);
            return;
        }
        List<Property> processVarPool = new ArrayList<>(JSONUtils.toList(processVarPoolJson, Property.class));
        List<Property> taskVarPool = JSONUtils.toList(taskVarPoolJson, Property.class);
        Set<String> newProcessVarPoolKeys = taskVarPool.stream().map(Property::getProp).collect(Collectors.toSet());
        processVarPool = processVarPool.stream().filter(property -> !newProcessVarPoolKeys.contains(property.getProp()))
                .collect(Collectors.toList());

        processVarPool.addAll(taskVarPool);

        processInstance.setVarPool(JSONUtils.toJsonString(processVarPool));
    }
}
