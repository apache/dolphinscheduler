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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.Constants.COMMA;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.YYYY_MM_DD_HH_MM_SS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;
import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
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
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.BlockingParameters;
import org.apache.dolphinscheduler.remote.command.HostUpdateCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.corn.CronUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Workflow execute task, used to execute a workflow instance.
 */
public class WorkflowExecuteRunnable implements Runnable {

    /**
     * logger of WorkflowExecuteThread
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteRunnable.class);

    /**
     * master config
     */
    private final MasterConfig masterConfig;

    /**
     * process service
     */
    private final ProcessService processService;

    /**
     * alert manager
     */
    private final ProcessAlertManager processAlertManager;

    /**
     * netty executor manager
     */
    private final NettyExecutorManager nettyExecutorManager;

    /**
     * process instance
     */
    private ProcessInstance processInstance;

    /**
     * process definition
     */
    private ProcessDefinition processDefinition;

    /**
     * the object of DAG
     */
    private DAG<String, TaskNode, TaskNodeRelation> dag;

    /**
     * key of workflow
     */
    private String key;

    /**
     * start flag, true: start nodes submit completely
     */
    private boolean isStart = false;

    /**
     * submit failure nodes
     */
    private boolean taskFailedSubmit = false;

    /**
     * task instance hash map, taskId as key
     */
    private final Map<Integer, TaskInstance> taskInstanceMap = new ConcurrentHashMap<>();

    /**
     * running taskProcessor, taskCode as key, taskProcessor as value
     * only on taskProcessor per taskCode
     */
    private final Map<Long, ITaskProcessor> activeTaskProcessorMaps = new ConcurrentHashMap<>();

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
     * complete task map, taskCode as key, taskInstanceId as value
     * in a DAG, only one taskInstance per taskCode is valid
     */
    private final Map<Long, Integer> completeTaskMap = new ConcurrentHashMap<>();

    /**
     * depend failed task map, taskCode as key, taskId as value
     */
    private final Map<Long, Integer> dependFailedTaskMap = new ConcurrentHashMap<>();

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
     * ready to submit task queue
     */
    private final PeerTaskInstancePriorityQueue readyToSubmitTaskQueue = new PeerTaskInstancePriorityQueue();

    /**
     * wait to retry taskInstance map, taskCode as key, taskInstance as value
     * before retry, the taskInstance id is 0
     */
    private final Map<Long, TaskInstance> waitToRetryTaskInstanceMap = new ConcurrentHashMap<>();

    /**
     * state wheel execute thread
     */
    private final StateWheelExecuteThread stateWheelExecuteThread;

    /**
     * @param processInstance         processInstance
     * @param processService          processService
     * @param nettyExecutorManager    nettyExecutorManager
     * @param processAlertManager     processAlertManager
     * @param masterConfig            masterConfig
     * @param stateWheelExecuteThread stateWheelExecuteThread
     */
    public WorkflowExecuteRunnable(ProcessInstance processInstance
            , ProcessService processService
            , NettyExecutorManager nettyExecutorManager
            , ProcessAlertManager processAlertManager
            , MasterConfig masterConfig
            , StateWheelExecuteThread stateWheelExecuteThread) {
        this.processService = processService;
        this.processInstance = processInstance;
        this.masterConfig = masterConfig;
        this.nettyExecutorManager = nettyExecutorManager;
        this.processAlertManager = processAlertManager;
        this.stateWheelExecuteThread = stateWheelExecuteThread;
        TaskMetrics.registerTaskRunning(readyToSubmitTaskQueue::size);
    }

    /**
     * the process start nodes are submitted completely.
     */
    public boolean isStart() {
        return this.isStart;
    }

    /**
     * handle event
     */
    public void handleEvents() {
        if (!isStart) {
            return;
        }
        while (!this.stateEvents.isEmpty()) {
            try {
                StateEvent stateEvent = this.stateEvents.peek();
                if (stateEventHandler(stateEvent)) {
                    this.stateEvents.remove(stateEvent);
                }
            } catch (Exception e) {
                logger.error("state handle error:", e);
            }
        }
    }

    public String getKey() {
        if (StringUtils.isNotEmpty(key)
                || this.processDefinition == null) {
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
            logger.info("state event would be abounded :{}", stateEvent);
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

    private boolean stateEventHandler(StateEvent stateEvent) {
        logger.info("process event: {}", stateEvent);

        if (!checkProcessInstance(stateEvent)) {
            return false;
        }

        boolean result = false;
        switch (stateEvent.getType()) {
            case PROCESS_STATE_CHANGE:
                measureProcessState(stateEvent);
                result = processStateChangeHandler(stateEvent);
                break;
            case TASK_STATE_CHANGE:
                measureTaskState(stateEvent);
                result = taskStateChangeHandler(stateEvent);
                break;
            case PROCESS_TIMEOUT:
                ProcessInstanceMetrics.incProcessInstanceTimeout();
                result = processTimeout();
                break;
            case TASK_TIMEOUT:
                TaskMetrics.incTaskTimeout();
                result = taskTimeout(stateEvent);
                break;
            case WAIT_TASK_GROUP:
                result = checkForceStartAndWakeUp(stateEvent);
                break;
            case TASK_RETRY:
                TaskMetrics.incTaskRetry();
                result = taskRetryEventHandler(stateEvent);
                break;
            case PROCESS_BLOCKED:
                result = processBlockHandler(stateEvent);
                break;
            default:
                break;
        }

        if (result) {
            this.stateEvents.remove(stateEvent);
        }
        return result;
    }

    private boolean checkForceStartAndWakeUp(StateEvent stateEvent) {
        TaskGroupQueue taskGroupQueue = this.processService.loadTaskGroupQueue(stateEvent.getTaskInstanceId());
        if (taskGroupQueue.getForceStart() == Flag.YES.getCode()) {
            TaskInstance taskInstance = this.processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskInstance.getTaskCode());
            taskProcessor.action(TaskAction.DISPATCH);
            this.processService.updateTaskGroupQueueStatus(taskGroupQueue.getTaskId(), TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode());
            return true;
        }
        if (taskGroupQueue.getInQueue() == Flag.YES.getCode()) {
            boolean acquireTaskGroup = processService.acquireTaskGroupAgain(taskGroupQueue);
            if (acquireTaskGroup) {
                TaskInstance taskInstance = this.processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
                ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskInstance.getTaskCode());
                taskProcessor.action(TaskAction.DISPATCH);
                return true;
            }
        }
        return false;
    }

    private boolean taskTimeout(StateEvent stateEvent) {
        if (!checkTaskInstanceByStateEvent(stateEvent)) {
            return true;
        }

        TaskInstance taskInstance = taskInstanceMap.get(stateEvent.getTaskInstanceId());
        if (TimeoutFlag.CLOSE == taskInstance.getTaskDefine().getTimeoutFlag()) {
            return true;
        }
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED == taskTimeoutStrategy || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy) {
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskInstance.getTaskCode());
            taskProcessor.action(TaskAction.TIMEOUT);
        }
        if (TaskTimeoutStrategy.WARN == taskTimeoutStrategy || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy) {
            ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
            processAlertManager.sendTaskTimeoutAlert(processInstance, taskInstance, projectUser);
        }
        return true;
    }

    private boolean processTimeout() {
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        this.processAlertManager.sendProcessTimeoutAlert(this.processInstance, projectUser);
        return true;
    }

    private boolean taskStateChangeHandler(StateEvent stateEvent) {
        if (!checkTaskInstanceByStateEvent(stateEvent)) {
            return true;
        }

        Optional<TaskInstance> taskInstanceOptional = getTaskInstance(stateEvent.getTaskInstanceId());
        TaskInstance task = taskInstanceOptional.orElseThrow(
                () -> new RuntimeException("Cannot find task instance by task instance id: " + stateEvent.getTaskInstanceId()));

        if (task.getState() == null) {
            logger.error("task state is null, state handler error: {}", stateEvent);
            return true;
        }

        if (task.getState().typeIsFinished()) {
            if (completeTaskMap.containsKey(task.getTaskCode()) && completeTaskMap.get(task.getTaskCode()) == task.getId()) {
                return true;
            }
            taskFinished(task);
            if (task.getTaskGroupId() > 0) {
                releaseTaskGroup(task);
            }
            return true;
        }
        if (activeTaskProcessorMaps.containsKey(task.getTaskCode())) {
            ITaskProcessor iTaskProcessor = activeTaskProcessorMaps.get(task.getTaskCode());
            iTaskProcessor.action(TaskAction.RUN);

            if (iTaskProcessor.taskInstance().getState().typeIsFinished()) {
                if (iTaskProcessor.taskInstance().getState() != task.getState()) {
                    task.setState(iTaskProcessor.taskInstance().getState());
                }
                taskFinished(task);
            }
            return true;
        }
        logger.error("state handler error: {}", stateEvent);

        return true;
    }

    private void taskFinished(TaskInstance taskInstance) {
        logger.info("work flow {} task id:{} code:{} state:{} ",
                processInstance.getId(),
                taskInstance.getId(),
                taskInstance.getTaskCode(),
                taskInstance.getState());

        activeTaskProcessorMaps.remove(taskInstance.getTaskCode());
        stateWheelExecuteThread.removeTask4TimeoutCheck(processInstance, taskInstance);
        stateWheelExecuteThread.removeTask4RetryCheck(processInstance, taskInstance);
        stateWheelExecuteThread.removeTask4StateCheck(processInstance, taskInstance);

        if (taskInstance.getState().typeIsSuccess()) {
            completeTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
            processInstance.setVarPool(taskInstance.getVarPool());
            processService.saveProcessInstance(processInstance);
            if (!processInstance.isBlocked()) {
                submitPostNode(Long.toString(taskInstance.getTaskCode()));
            }
        } else if (taskInstance.taskCanRetry() && processInstance.getState() != ExecutionStatus.READY_STOP) {
            // retry task
            retryTaskInstance(taskInstance);
        } else if (taskInstance.getState().typeIsFailure()) {
            completeTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
            // There are child nodes and the failure policy is: CONTINUE
            if (DagHelper.haveAllNodeAfterNode(Long.toString(taskInstance.getTaskCode()), dag)
                    && processInstance.getFailureStrategy() == FailureStrategy.CONTINUE) {
                submitPostNode(Long.toString(taskInstance.getTaskCode()));
            } else {
                errorTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
                if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                    killAllTasks();
                }
            }
        } else if (taskInstance.getState().typeIsFinished()) {
            completeTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
        }

        this.updateProcessInstanceState();
    }

    /**
     * release task group
     *
     * @param taskInstance
     */
    private void releaseTaskGroup(TaskInstance taskInstance) {
        if (taskInstance.getTaskGroupId() > 0) {
            TaskInstance nextTaskInstance = this.processService.releaseTaskGroup(taskInstance);
            if (nextTaskInstance != null) {
                if (nextTaskInstance.getProcessInstanceId() == taskInstance.getProcessInstanceId()) {
                    StateEvent nextEvent = new StateEvent();
                    nextEvent.setProcessInstanceId(this.processInstance.getId());
                    nextEvent.setTaskInstanceId(nextTaskInstance.getId());
                    nextEvent.setType(StateEventType.WAIT_TASK_GROUP);
                    this.stateEvents.add(nextEvent);
                } else {
                    ProcessInstance processInstance = this.processService.findProcessInstanceById(nextTaskInstance.getProcessInstanceId());
                    this.processService.sendStartTask2Master(processInstance, nextTaskInstance.getId(),
                            org.apache.dolphinscheduler.remote.command.CommandType.TASK_WAKEUP_EVENT_REQUEST);
                }
            }
        }
    }

    /**
     * crate new task instance to retry, different objects from the original
     *
     * @param taskInstance
     */
    private void retryTaskInstance(TaskInstance taskInstance) {
        if (!taskInstance.taskCanRetry()) {
            return;
        }
        TaskInstance newTaskInstance = cloneRetryTaskInstance(taskInstance);
        if (newTaskInstance == null) {
            logger.error("retry fail, new taskInstancce is null, task code:{}, task id:{}", taskInstance.getTaskCode(), taskInstance.getId());
            return;
        }
        waitToRetryTaskInstanceMap.put(newTaskInstance.getTaskCode(), newTaskInstance);
        if (!taskInstance.retryTaskIntervalOverTime()) {
            logger.info("failure task will be submitted: process id: {}, task instance code: {} state:{} retry times:{} / {}, interval:{}",
                    processInstance.getId(),
                    newTaskInstance.getTaskCode(),
                    newTaskInstance.getState(),
                    newTaskInstance.getRetryTimes(),
                    newTaskInstance.getMaxRetryTimes(),
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
     * handle task retry event
     *
     * @param stateEvent
     * @return
     */
    private boolean taskRetryEventHandler(StateEvent stateEvent) {
        TaskInstance taskInstance = waitToRetryTaskInstanceMap.get(stateEvent.getTaskCode());
        addTaskToStandByList(taskInstance);
        submitStandByTask();
        waitToRetryTaskInstanceMap.remove(stateEvent.getTaskCode());
        return true;
    }

    /**
     * update process instance
     */
    public void refreshProcessInstance(int processInstanceId) {
        logger.info("process instance update: {}", processInstanceId);
        processInstance = processService.findProcessInstanceById(processInstanceId);
        processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        processInstance.setProcessDefinition(processDefinition);
    }

    /**
     * update task instance
     */
    public void refreshTaskInstance(int taskInstanceId) {
        logger.info("task instance update: {} ", taskInstanceId);
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
        if (taskInstance == null) {
            logger.error("can not find task instance, id:{}", taskInstanceId);
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
    public boolean checkProcessInstance(StateEvent stateEvent) {
        if (this.processInstance.getId() != stateEvent.getProcessInstanceId()) {
            logger.error("mismatch process instance id: {}, state event:{}",
                    this.processInstance.getId(),
                    stateEvent);
            return false;
        }
        return true;
    }

    /**
     * check if task instance exist by state event
     */
    public boolean checkTaskInstanceByStateEvent(StateEvent stateEvent) {
        if (stateEvent.getTaskInstanceId() == 0) {
            logger.error("task instance id null, state event:{}", stateEvent);
            return false;
        }

        if (!taskInstanceMap.containsKey(stateEvent.getTaskInstanceId())) {
            logger.error("mismatch task instance id, event:{}", stateEvent);
            return false;
        }
        return true;
    }

    /**
     * check if task instance exist by task code
     */
    public boolean checkTaskInstanceByCode(long taskCode) {
        if (taskInstanceMap == null || taskInstanceMap.size() == 0) {
            return false;
        }
        for (TaskInstance taskInstance : taskInstanceMap.values()) {
            if (taskInstance.getTaskCode() == taskCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if task instance exist by id
     */
    public boolean checkTaskInstanceById(int taskInstanceId) {
        if (taskInstanceMap == null || taskInstanceMap.size() == 0) {
            return false;
        }
        return taskInstanceMap.containsKey(taskInstanceId);
    }

    /**
     * get task instance from memory
     */
    public Optional<TaskInstance> getTaskInstance(int taskInstanceId) {
        if (taskInstanceMap.containsKey(taskInstanceId)) {
            return Optional.ofNullable(taskInstanceMap.get(taskInstanceId));
        }
        return Optional.empty();
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
        if (activeTaskProcessorMaps.containsKey(taskCode)) {
            return Optional.ofNullable(activeTaskProcessorMaps.get(taskCode).taskInstance());
        }
        return Optional.empty();
    }

    public Optional<TaskInstance> getRetryTaskInstanceByTaskCode(long taskCode) {
        if (waitToRetryTaskInstanceMap.containsKey(taskCode)) {
            return Optional.ofNullable(waitToRetryTaskInstanceMap.get(taskCode));
        }
        return Optional.empty();
    }

    private boolean processStateChangeHandler(StateEvent stateEvent) {
        try {
            logger.info("process:{} state {} change to {}", processInstance.getId(), processInstance.getState(), stateEvent.getExecutionStatus());

            if (stateEvent.getExecutionStatus() == ExecutionStatus.STOP) {
                // serial wait execution type needs to wake up the waiting process
                if (processDefinition.getExecutionType().typeIsSerialWait()){
                    endProcess();
                    return true;
                }
                this.updateProcessInstanceState(stateEvent);
                return true;
            }

            if (processComplementData()) {
                return true;
            }
            if (stateEvent.getExecutionStatus().typeIsFinished()) {
                endProcess();
            }
            if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                killAllTasks();
            }
            return true;
        } catch (Exception e) {
            logger.error("process state change error:", e);
        }
        return true;
    }

    private boolean processBlockHandler(StateEvent stateEvent) {
        try {
            Optional<TaskInstance> taskInstanceOptional = getTaskInstance(stateEvent.getTaskInstanceId());
            TaskInstance task = taskInstanceOptional.orElseThrow(
                    () -> new RuntimeException("Cannot find taskInstance by taskInstanceId:" + stateEvent.getTaskInstanceId()));
            if (!checkTaskInstanceByStateEvent(stateEvent)) {
                logger.error("task {} is not a blocking task", task.getTaskCode());
                return false;
            }

            BlockingParameters parameters = JSONUtils.parseObject(task.getTaskParams(), BlockingParameters.class);
            if (parameters.isAlertWhenBlocking()) {
                ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
                processAlertManager.sendProcessBlockingAlert(processInstance, projectUser);
                logger.info("processInstance {} block alert send successful!", processInstance.getId());
            }
        } catch (Exception e) {
            logger.error("sending blocking message error:", e);
        }
        return true;
    }

    private boolean processComplementData() throws Exception {
        if (!needComplementProcess()) {
            return false;
        }

        if (processInstance.getState() == ExecutionStatus.READY_STOP) {
            return false;
        }

        Date scheduleDate = processInstance.getScheduleTime();
        if (scheduleDate == null) {
            scheduleDate = complementListDate.get(0);
        } else if (processInstance.getState().typeIsFinished()) {
            endProcess();
            if (complementListDate.isEmpty()) {
                logger.info("process complement end. process id:{}", processInstance.getId());
                return true;
            }
            int index = complementListDate.indexOf(scheduleDate);
            if (index >= complementListDate.size() - 1 || !processInstance.getState().typeIsSuccess()) {
                logger.info("process complement end. process id:{}", processInstance.getId());
                // complement data ends || no success
                return true;
            }
            logger.info("process complement continue. process id:{}, schedule time:{} complementListDate:{}",
                    processInstance.getId(),
                    processInstance.getScheduleTime(),
                    complementListDate);
            scheduleDate = complementListDate.get(index + 1);
        }
        //the next process complement
        int create = this.createComplementDataCommand(scheduleDate);
        if (create > 0) {
            logger.info("create complement data command successfully.");
        }
        return true;
    }

    private int createComplementDataCommand(Date scheduleDate) {
        Command command = new Command();
        command.setScheduleTime(scheduleDate);
        command.setCommandType(CommandType.COMPLEMENT_DATA);
        command.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
        }
        if(cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)){
            cmdParam.replace(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, cmdParam.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).substring(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).indexOf(COMMA)+1));
        }
        if(cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)){
            cmdParam.replace(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.format(scheduleDate, YYYY_MM_DD_HH_MM_SS, null));
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
        return processService.createCommand(command);
    }

    private boolean needComplementProcess() {
        if (processInstance.isComplementData()
                && Flag.NO == processInstance.getIsSubProcess()) {
            return true;
        }
        return false;
    }

    /**
     * ProcessInstance start entrypoint.
     */
    @Override
    public void run() {
        if (this.taskInstanceMap.size() > 0 || isStart) {
            logger.warn("The workflow has already been started");
            return;
        }
        try {
            buildFlowDag();
            initTaskQueue();
            submitPostNode(null);
            isStart = true;
        } catch (Exception e) {
            logger.error("start process error, process instance id:{}", processInstance.getId(), e);
        }
    }

    /**
     * process end handle
     */
    public void endProcess() {
        this.stateEvents.clear();
        if (processDefinition.getExecutionType().typeIsSerialWait()) {
            checkSerialProcess(processDefinition);
        }
        if (processInstance.getState().typeIsWaitingThread()) {
            processService.createRecoveryWaitingThreadCommand(null, processInstance);
        }
        if (processAlertManager.isNeedToSendWarning(processInstance)) {
            ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
            processAlertManager.sendAlertProcessInstance(processInstance, getValidTaskList(), projectUser);
        }
        if (checkTaskQueue()) {
            //release task group
            processService.releaseAllTaskGroup(processInstance.getId());
        }
    }

    public void checkSerialProcess(ProcessDefinition processDefinition) {
        int nextInstanceId = processInstance.getNextProcessInstanceId();
        if (nextInstanceId == 0) {
            ProcessInstance nextProcessInstance = this.processService.loadNextProcess4Serial(processInstance.getProcessDefinition().getCode(), ExecutionStatus.SERIAL_WAIT.getCode(), processInstance.getId());
            if (nextProcessInstance == null) {
                return;
            }
            nextInstanceId = nextProcessInstance.getId();
        }
        ProcessInstance nextProcessInstance = this.processService.findProcessInstanceById(nextInstanceId);
        if (nextProcessInstance.getState().typeIsFinished() || nextProcessInstance.getState().typeIsRunning()) {
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
        processService.createCommand(command);
    }

    /**
     * generate process dag
     *
     * @throws Exception exception
     */
    private void buildFlowDag() throws Exception {
        if (this.dag != null) {
            return;
        }
        processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        processInstance.setProcessDefinition(processDefinition);

        List<TaskInstance> recoverNodeList = getStartTaskInstanceList(processInstance.getCommandParam());

        List<ProcessTaskRelation> processTaskRelations = processService.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskDefinitionLog> taskDefinitionLogs = processService.getTaskDefineLogListByRelation(processTaskRelations);
        List<TaskNode> taskNodeList = processService.transformTask(processTaskRelations, taskDefinitionLogs);
        forbiddenTaskMap.clear();

        taskNodeList.forEach(taskNode -> {
            if (taskNode.isForbidden()) {
                forbiddenTaskMap.put(taskNode.getCode(), taskNode);
            }
        });

        // generate process to get DAG info
        List<String> recoveryNodeCodeList = getRecoveryNodeCodeList(recoverNodeList);
        List<String> startNodeNameList = parseStartNodeName(processInstance.getCommandParam());
        ProcessDag processDag = generateFlowDag(taskNodeList,
                startNodeNameList, recoveryNodeCodeList, processInstance.getTaskDependType());
        if (processDag == null) {
            logger.error("processDag is null");
            return;
        }
        // generate process dag
        dag = DagHelper.buildDagGraph(processDag);
    }

    /**
     * init task queue
     */
    private void initTaskQueue() {

        taskFailedSubmit = false;
        activeTaskProcessorMaps.clear();
        dependFailedTaskMap.clear();
        completeTaskMap.clear();
        errorTaskMap.clear();

        if (!isNewProcessInstance()) {
            List<TaskInstance> validTaskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
            for (TaskInstance task : validTaskInstanceList) {
                if (validTaskMap.containsKey(task.getTaskCode())) {
                    int oldTaskInstanceId = validTaskMap.get(task.getTaskCode());
                    TaskInstance oldTaskInstance = taskInstanceMap.get(oldTaskInstanceId);
                    if (!oldTaskInstance.getState().typeIsFinished() && task.getState().typeIsFinished()) {
                        task.setFlag(Flag.NO);
                        processService.updateTaskInstance(task);
                        continue;
                    }
                    logger.warn("have same taskCode taskInstance when init task queue, taskCode:{}", task.getTaskCode());
                }

                validTaskMap.put(task.getTaskCode(), task.getId());
                taskInstanceMap.put(task.getId(), task);

                if (task.isTaskComplete()) {
                    completeTaskMap.put(task.getTaskCode(), task.getId());
                    continue;
                }
                if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()), dag)) {
                    continue;
                }
                if (task.taskCanRetry()) {
                    if (task.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
                        // tolerantTaskInstance add to standby list directly
                        TaskInstance tolerantTaskInstance = cloneTolerantTaskInstance(task);
                        addTaskToStandByList(tolerantTaskInstance);
                    } else {
                        retryTaskInstance(task);
                    }
                    continue;
                }
                if (task.getState().typeIsFailure()) {
                    errorTaskMap.put(task.getTaskCode(), task.getId());
                }
            }
        }

        if (processInstance.isComplementData() && complementListDate.isEmpty()) {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            if (cmdParam != null) {
                // reset global params while there are start parameters
                setGlobalParamIfCommanded(processDefinition, cmdParam);

                Date start = null;
                Date end = null;
                if(cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE) && cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_END_DATE)){
                    start = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
                    end = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
                }
                List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(processInstance.getProcessDefinitionCode());
                if (complementListDate.isEmpty() && needComplementProcess()) {
                    if(start != null && end != null){
                        complementListDate = CronUtils.getSelfFireDateList(start, end, schedules);
                    }
                    if(cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)){
                        complementListDate = CronUtils.getSelfScheduleDateList(cmdParam);
                    }
                    logger.info(" process definition code:{} complement data: {}",
                            processInstance.getProcessDefinitionCode(), complementListDate);

                    if (!complementListDate.isEmpty() && Flag.NO == processInstance.getIsSubProcess()) {
                        processInstance.setScheduleTime(complementListDate.get(0));
                        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                                processDefinition.getGlobalParamMap(),
                                processDefinition.getGlobalParamList(),
                                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime(), cmdParam.get(Constants.SCHEDULE_TIMEZONE)));
                        processService.updateProcessInstance(processInstance);
                    }
                }
            }
        }
    }

    /**
     * submit task to execute
     *
     * @param taskInstance task instance
     * @return TaskInstance
     */
    private Optional<TaskInstance> submitTaskExec(TaskInstance taskInstance) {
        try {
            // package task instance before submit
            processService.packageTaskInstance(taskInstance, processInstance);

            ITaskProcessor taskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType());
            taskProcessor.init(taskInstance, processInstance);

            if (taskInstance.getState() == ExecutionStatus.RUNNING_EXECUTION
                    && taskProcessor.getType().equalsIgnoreCase(Constants.COMMON_TASK_TYPE)) {
                notifyProcessHostUpdate(taskInstance);
            }

            boolean submit = taskProcessor.action(TaskAction.SUBMIT);
            if (!submit) {
                logger.error("process id:{} name:{} submit standby task id:{} name:{} failed!",
                        processInstance.getId(), processInstance.getName(),
                        taskInstance.getId(), taskInstance.getName());
                return Optional.empty();
            }

            // in a dag, only one taskInstance is valid per taskCode, so need to set the old taskInstance invalid
            if (validTaskMap.containsKey(taskInstance.getTaskCode())) {
                int oldTaskInstanceId = validTaskMap.get(taskInstance.getTaskCode());
                if (taskInstance.getId() != oldTaskInstanceId) {
                    TaskInstance oldTaskInstance = taskInstanceMap.get(oldTaskInstanceId);
                    oldTaskInstance.setFlag(Flag.NO);
                    processService.updateTaskInstance(oldTaskInstance);
                    validTaskMap.remove(taskInstance.getTaskCode());
                    activeTaskProcessorMaps.remove(taskInstance.getTaskCode());
                }
            }

            validTaskMap.put(taskInstance.getTaskCode(), taskInstance.getId());
            taskInstanceMap.put(taskInstance.getId(), taskInstance);
            activeTaskProcessorMaps.put(taskInstance.getTaskCode(), taskProcessor);
            taskProcessor.action(TaskAction.RUN);

            stateWheelExecuteThread.addTask4TimeoutCheck(processInstance, taskInstance);
            stateWheelExecuteThread.addTask4StateCheck(processInstance, taskInstance);

            if (taskProcessor.taskInstance().getState().typeIsFinished()) {
                if (processInstance.isBlocked()) {
                    StateEvent processBlockEvent = new StateEvent();
                    processBlockEvent.setProcessInstanceId(this.processInstance.getId());
                    processBlockEvent.setTaskInstanceId(taskInstance.getId());
                    processBlockEvent.setExecutionStatus(taskProcessor.taskInstance().getState());
                    processBlockEvent.setType(StateEventType.PROCESS_BLOCKED);
                    this.stateEvents.add(processBlockEvent);
                }
                StateEvent taskStateChangeEvent = new StateEvent();
                taskStateChangeEvent.setProcessInstanceId(this.processInstance.getId());
                taskStateChangeEvent.setTaskInstanceId(taskInstance.getId());
                taskStateChangeEvent.setExecutionStatus(taskProcessor.taskInstance().getState());
                taskStateChangeEvent.setType(StateEventType.TASK_STATE_CHANGE);
                this.stateEvents.add(taskStateChangeEvent);
            }
            return Optional.of(taskInstance);
        } catch (Exception e) {
            logger.error("submit standby task error", e);
            return Optional.empty();
        }
    }

    private void notifyProcessHostUpdate(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return;
        }

        try {
            HostUpdateCommand hostUpdateCommand = new HostUpdateCommand();
            hostUpdateCommand.setProcessHost(NetUtils.getAddr(masterConfig.getListenPort()));
            hostUpdateCommand.setTaskInstanceId(taskInstance.getId());
            Host host = new Host(taskInstance.getHost());
            nettyExecutorManager.doExecute(host, hostUpdateCommand.convert2Command());
        } catch (Exception e) {
            logger.error("notify process host update", e);
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
     * encapsulation task
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
     * @return
     */
    public TaskInstance cloneRetryTaskInstance(TaskInstance taskInstance) {
        TaskNode taskNode = dag.getNode(Long.toString(taskInstance.getTaskCode()));
        if (taskNode == null) {
            logger.error("taskNode is null, code:{}", taskInstance.getTaskCode());
            return null;
        }
        TaskInstance newTaskInstance = newTaskInstance(processInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(processInstance);
        newTaskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1);
        // todo relative funtion: TaskInstance.retryTaskIntervalOverTime
        newTaskInstance.setState(taskInstance.getState());
        newTaskInstance.setEndTime(taskInstance.getEndTime());
        return newTaskInstance;
    }

    /**
     * clone a new taskInstance for tolerant and reset some logic fields
     *
     * @return
     */
    public TaskInstance cloneTolerantTaskInstance(TaskInstance taskInstance) {
        TaskNode taskNode = dag.getNode(Long.toString(taskInstance.getTaskCode()));
        if (taskNode == null) {
            logger.error("taskNode is null, code:{}", taskInstance.getTaskCode());
            return null;
        }
        TaskInstance newTaskInstance = newTaskInstance(processInstance, taskNode);
        newTaskInstance.setTaskDefine(taskInstance.getTaskDefine());
        newTaskInstance.setProcessDefine(taskInstance.getProcessDefine());
        newTaskInstance.setProcessInstance(processInstance);
        newTaskInstance.setRetryTimes(taskInstance.getRetryTimes());
        newTaskInstance.setState(taskInstance.getState());
        return newTaskInstance;
    }

    /**
     * new a taskInstance
     *
     * @param processInstance
     * @param taskNode
     * @return
     */
    public TaskInstance newTaskInstance(ProcessInstance processInstance, TaskNode taskNode) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskCode(taskNode.getCode());
        taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
        // task name
        taskInstance.setName(taskNode.getName());
        // task instance state
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        // process instance id
        taskInstance.setProcessInstanceId(processInstance.getId());
        // task instance type
        taskInstance.setTaskType(taskNode.getType().toUpperCase());
        // task instance whether alert
        taskInstance.setAlertFlag(Flag.NO);

        // task instance start time
        taskInstance.setStartTime(null);

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

        //set task param
        taskInstance.setTaskParams(taskNode.getTaskParams());

        //set task group and priority
        taskInstance.setTaskGroupId(taskNode.getTaskGroupId());
        taskInstance.setTaskGroupPriority(taskNode.getTaskGroupPriority());

        //set task cpu quota and max memory
        taskInstance.setCpuQuota(taskNode.getCpuQuota());
        taskInstance.setMemoryMax(taskNode.getMemoryMax());

        // task instance priority
        if (taskNode.getTaskInstancePriority() == null) {
            taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        } else {
            taskInstance.setTaskInstancePriority(taskNode.getTaskInstancePriority());
        }

        String processWorkerGroup = processInstance.getWorkerGroup();
        processWorkerGroup = StringUtils.isBlank(processWorkerGroup) ? DEFAULT_WORKER_GROUP : processWorkerGroup;
        String taskWorkerGroup = StringUtils.isBlank(taskNode.getWorkerGroup()) ? processWorkerGroup : taskNode.getWorkerGroup();

        Long processEnvironmentCode = Objects.isNull(processInstance.getEnvironmentCode()) ? -1 : processInstance.getEnvironmentCode();
        Long taskEnvironmentCode = Objects.isNull(taskNode.getEnvironmentCode()) ? processEnvironmentCode : taskNode.getEnvironmentCode();

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
        return taskInstance;
    }

    public void getPreVarPool(TaskInstance taskInstance, Set<String> preTask) {
        Map<String, Property> allProperty = new HashMap<>();
        Map<String, TaskInstance> allTaskInstance = new HashMap<>();
        if (CollectionUtils.isNotEmpty(preTask)) {
            for (String preTaskCode : preTask) {
                Integer taskId = completeTaskMap.get(Long.parseLong(preTaskCode));
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

    private void setVarPoolValue(Map<String, Property> allProperty, Map<String, TaskInstance> allTaskInstance, TaskInstance preTaskInstance, Property thisProperty) {
        //for this taskInstance all the param in this part is IN.
        thisProperty.setDirect(Direct.IN);
        //get the pre taskInstance Property's name
        String proName = thisProperty.getProp();
        //if the Previous nodes have the Property of same name
        if (allProperty.containsKey(proName)) {
            //comparison the value of two Property
            Property otherPro = allProperty.get(proName);
            //if this property'value of loop is empty,use the other,whether the other's value is empty or not
            if (StringUtils.isEmpty(thisProperty.getValue())) {
                allProperty.put(proName, otherPro);
                //if  property'value of loop is not empty,and the other's value is not empty too, use the earlier value
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
        for (Integer taskInstanceId : completeTaskMap.values()) {
            TaskInstance taskInstance = taskInstanceMap.get(taskInstanceId);
            completeTaskInstanceMap.put(Long.toString(taskInstance.getTaskCode()), taskInstance);
        }
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

    private void submitPostNode(String parentNodeCode) {
        Set<String> submitTaskNodeList = DagHelper.parsePostNodes(parentNodeCode, skipTaskNodeMap, dag, getCompleteTaskInstanceMap());
        List<TaskInstance> taskInstances = new ArrayList<>();
        for (String taskNode : submitTaskNodeList) {
            TaskNode taskNodeObject = dag.getNode(taskNode);
            Optional<TaskInstance> existTaskInstanceOptional = getTaskInstance(taskNodeObject.getCode());
            if (existTaskInstanceOptional.isPresent()) {
                taskInstances.add(existTaskInstanceOptional.get());
                continue;
            }
            TaskInstance task = createTaskInstance(processInstance, taskNodeObject);
            taskInstances.add(task);
        }
        //the end node of the branch of the dag
        if (StringUtils.isNotEmpty(parentNodeCode) && dag.getEndNode().contains(parentNodeCode)) {
            TaskInstance endTaskInstance = taskInstanceMap.get(completeTaskMap.get(NumberUtils.toLong(parentNodeCode)));
            String taskInstanceVarPool = endTaskInstance.getVarPool();
            if (StringUtils.isNotEmpty(taskInstanceVarPool)) {
                Set<Property> taskProperties = new HashSet<>(JSONUtils.toList(taskInstanceVarPool, Property.class));
                String processInstanceVarPool = processInstance.getVarPool();
                if (StringUtils.isNotEmpty(processInstanceVarPool)) {
                    Set<Property> properties = new HashSet<>(JSONUtils.toList(processInstanceVarPool, Property.class));
                    properties.addAll(taskProperties);
                    processInstance.setVarPool(JSONUtils.toJsonString(properties));
                } else {
                    processInstance.setVarPool(JSONUtils.toJsonString(taskProperties));
                }
            }
        }

        // if previous node success , post node submit
        for (TaskInstance task : taskInstances) {

            if (readyToSubmitTaskQueue.contains(task)) {
                logger.warn("Task is already at submit queue, taskInstanceId: {}", task.getId());
                continue;
            }

            if (task.getId() > 0 && completeTaskMap.containsKey(task.getTaskCode())) {
                logger.info("task {} has already run success", task.getName());
                continue;
            }
            if (task.getState().typeIsPause() || task.getState().typeIsCancel()) {
                logger.info("task {} stopped, the state is {}", task.getName(), task.getState());
                continue;
            }

            addTaskToStandByList(task);
        }
        submitStandByTask();
        updateProcessInstanceState();
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
                Long despNodeTaskCode = Long.parseLong(depsNode);
                if (!completeTaskMap.containsKey(despNodeTaskCode)) {
                    return DependResult.WAITING;
                }
                Integer depsTaskId = completeTaskMap.get(despNodeTaskCode);
                ExecutionStatus depTaskState = taskInstanceMap.get(depsTaskId).getState();
                if (depTaskState.typeIsPause() || depTaskState.typeIsCancel()) {
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
        logger.info("taskCode: {} completeDependTaskList: {}", taskCode, Arrays.toString(completeTaskMap.keySet().toArray()));
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
        List<String> depCodeList = taskNode.getDepList();
        for (String depsNode : depCodeList) {
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
    private boolean dependTaskSuccess(String dependNodeName, String nextNodeName) {
        if (dag.getNode(dependNodeName).isConditionsTask()) {
            //condition task need check the branch to run
            List<String> nextTaskList = DagHelper.parseConditionTask(dependNodeName, skipTaskNodeMap, dag, getCompleteTaskInstanceMap());
            if (!nextTaskList.contains(nextNodeName)) {
                return false;
            }
        } else {
            long taskCode = Long.parseLong(dependNodeName);
            Integer taskInstanceId = completeTaskMap.get(taskCode);
            ExecutionStatus depTaskState = taskInstanceMap.get(taskInstanceId).getState();
            if (depTaskState.typeIsFailure()) {
                return false;
            }
        }
        return true;
    }

    /**
     * query task instance by complete state
     *
     * @param state state
     * @return task instance list
     */
    private List<TaskInstance> getCompleteTaskByState(ExecutionStatus state) {
        List<TaskInstance> resultList = new ArrayList<>();
        for (Integer taskInstanceId : completeTaskMap.values()) {
            TaskInstance taskInstance = taskInstanceMap.get(taskInstanceId);
            if (taskInstance != null && taskInstance.getState() == state) {
                resultList.add(taskInstance);
            }
        }
        return resultList;
    }

    /**
     * where there are ongoing tasks
     *
     * @param state state
     * @return ExecutionStatus
     */
    private ExecutionStatus runningState(ExecutionStatus state) {
        if (state == ExecutionStatus.READY_STOP
                || state == ExecutionStatus.READY_PAUSE
                || state == ExecutionStatus.WAITING_THREAD
                || state == ExecutionStatus.READY_BLOCK
                || state == ExecutionStatus.DELAY_EXECUTION) {
            // if the running task is not completed, the state remains unchanged
            return state;
        } else {
            return ExecutionStatus.RUNNING_EXECUTION;
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
        return this.dependFailedTaskMap.size() > 0;
    }

    /**
     * process instance failure
     *
     * @return Boolean whether process instance failed
     */
    private boolean processFailed() {
        if (hasFailedTask()) {
            if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                return true;
            }
            if (processInstance.getFailureStrategy() == FailureStrategy.CONTINUE) {
                return readyToSubmitTaskQueue.size() == 0
                        && activeTaskProcessorMaps.size() == 0
                        && waitToRetryTaskInstanceMap.size() == 0;
            }
        }
        return false;
    }

    /**
     * whether task for waiting thread
     *
     * @return Boolean whether has waiting thread task
     */
    private boolean hasWaitingThreadTask() {
        List<TaskInstance> waitingList = getCompleteTaskByState(ExecutionStatus.WAITING_THREAD);
        return CollectionUtils.isNotEmpty(waitingList);
    }

    /**
     * prepare for pause
     * 1，failed retry task in the preparation queue , returns to failure directly
     * 2，exists pause task，complement not completed, pending submission of tasks, return to suspension
     * 3，success
     *
     * @return ExecutionStatus
     */
    private ExecutionStatus processReadyPause() {
        if (hasRetryTaskInStandBy()) {
            return ExecutionStatus.FAILURE;
        }

        List<TaskInstance> pauseList = getCompleteTaskByState(ExecutionStatus.PAUSE);
        if (CollectionUtils.isNotEmpty(pauseList)
                || processInstance.isBlocked()
                || !isComplementEnd()
                || readyToSubmitTaskQueue.size() > 0) {
            return ExecutionStatus.PAUSE;
        } else {
            return ExecutionStatus.SUCCESS;
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
    private ExecutionStatus processReadyBlock() {
        if (activeTaskProcessorMaps.size() > 0) {
            for (ITaskProcessor taskProcessor : activeTaskProcessorMaps.values()) {
                if (!TASK_TYPE_BLOCKING.equals(taskProcessor.getType())) {
                    taskProcessor.action(TaskAction.PAUSE);
                }
            }
        }
        if (readyToSubmitTaskQueue.size() > 0) {
            for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext(); ) {
                iter.next().setState(ExecutionStatus.KILL);
            }
        }
        return ExecutionStatus.BLOCK;
    }

    /**
     * generate the latest process instance status by the tasks state
     *
     * @return process instance execution status
     */
    private ExecutionStatus getProcessInstanceState(ProcessInstance instance) {
        ExecutionStatus state = instance.getState();

        if (activeTaskProcessorMaps.size() > 0 || hasRetryTaskInStandBy()) {
            // active task and retry task exists
            return runningState(state);
        }

        // block
        if (state == ExecutionStatus.READY_BLOCK) {
            return processReadyBlock();
        }

        // waiting thread
        if (hasWaitingThreadTask()) {
            return ExecutionStatus.WAITING_THREAD;
        }

        // pause
        if (state == ExecutionStatus.READY_PAUSE) {
            return processReadyPause();
        }

        // stop
        if (state == ExecutionStatus.READY_STOP) {
            List<TaskInstance> stopList = getCompleteTaskByState(ExecutionStatus.STOP);
            List<TaskInstance> killList = getCompleteTaskByState(ExecutionStatus.KILL);
            List<TaskInstance> failList = getCompleteTaskByState(ExecutionStatus.FAILURE);
            if (CollectionUtils.isNotEmpty(stopList)
                    || CollectionUtils.isNotEmpty(killList)
                    || CollectionUtils.isNotEmpty(failList)
                    || !isComplementEnd()) {
                return ExecutionStatus.STOP;
            } else {
                return ExecutionStatus.SUCCESS;
            }
        }

        // process failure
        if (processFailed()) {
            return ExecutionStatus.FAILURE;
        }

        // success
        if (state == ExecutionStatus.RUNNING_EXECUTION) {
            List<TaskInstance> killTasks = getCompleteTaskByState(ExecutionStatus.KILL);
            if (readyToSubmitTaskQueue.size() > 0 || waitToRetryTaskInstanceMap.size() > 0) {
                //tasks currently pending submission, no retries, indicating that depend is waiting to complete
                return ExecutionStatus.RUNNING_EXECUTION;
            } else if (CollectionUtils.isNotEmpty(killTasks)) {
                // tasks maybe killed manually
                return ExecutionStatus.FAILURE;
            } else {
                //  if the waiting queue is empty and the status is in progress, then success
                return ExecutionStatus.SUCCESS;
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

        try {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            Date endTime = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
            return processInstance.getScheduleTime().equals(endTime);
        } catch (Exception e) {
            logger.error("complement end failed ", e);
            return false;
        }
    }

    /**
     * updateProcessInstance process instance state
     * after each batch of tasks is executed, the status of the process instance is updated
     */
    private void updateProcessInstanceState() {
        ExecutionStatus state = getProcessInstanceState(processInstance);
        if (processInstance.getState() != state) {
            logger.info(
                    "work flow process instance [id: {}, name:{}], state change from {} to {}, cmd type: {}",
                    processInstance.getId(), processInstance.getName(),
                    processInstance.getState(), state,
                    processInstance.getCommandType());

            processInstance.setState(state);
            if (state.typeIsFinished()) {
                processInstance.setEndTime(new Date());
            }
            processService.updateProcessInstance(processInstance);

            StateEvent stateEvent = new StateEvent();
            stateEvent.setExecutionStatus(processInstance.getState());
            stateEvent.setProcessInstanceId(this.processInstance.getId());
            stateEvent.setType(StateEventType.PROCESS_STATE_CHANGE);
            // this.processStateChangeHandler(stateEvent);
            // replace with `stateEvents`, make sure `WorkflowExecuteThread` can be deleted to avoid memory leaks
            this.stateEvents.add(stateEvent);
        }
    }

    /**
     * stateEvent's execution status as process instance state
     */
    private void updateProcessInstanceState(StateEvent stateEvent) {
        ExecutionStatus state = stateEvent.getExecutionStatus();
        if (processInstance.getState() != state) {
            logger.info(
                    "work flow process instance [id: {}, name:{}], state change from {} to {}, cmd type: {}",
                    processInstance.getId(), processInstance.getName(),
                    processInstance.getState(), state,
                    processInstance.getCommandType());

            processInstance.setState(state);
            if (state.typeIsFinished()) {
                processInstance.setEndTime(new Date());
            }
            processService.updateProcessInstance(processInstance);
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
    private void addTaskToStandByList(TaskInstance taskInstance) {
        try {
            if (readyToSubmitTaskQueue.contains(taskInstance)) {
                logger.warn("task was found in ready submit queue, task code:{}", taskInstance.getTaskCode());
                return;
            }
            logger.info("add task to stand by list, task name:{}, task id:{}, task code:{}",
                    taskInstance.getName(), taskInstance.getId(), taskInstance.getTaskCode());
            TaskMetrics.incTaskSubmit();
            readyToSubmitTaskQueue.put(taskInstance);
        } catch (Exception e) {
            logger.error("add task instance to readyToSubmitTaskQueue, taskName:{}, task id:{}", taskInstance.getName(), taskInstance.getId(), e);
        }
    }

    /**
     * remove task from stand by list
     *
     * @param taskInstance task instance
     */
    private void removeTaskFromStandbyList(TaskInstance taskInstance) {
        try {
            readyToSubmitTaskQueue.remove(taskInstance);
        } catch (Exception e) {
            logger.error("remove task instance from readyToSubmitTaskQueue error, task id:{}, Name: {}",
                    taskInstance.getId(),
                    taskInstance.getName(), e);
        }
    }

    /**
     * has retry task in standby
     *
     * @return Boolean whether has retry task in standby
     */
    private boolean hasRetryTaskInStandBy() {
        for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext(); ) {
            if (iter.next().getState().typeIsFailure()) {
                return true;
            }
        }
        return false;
    }

    /**
     * close the on going tasks
     */
    private void killAllTasks() {
        logger.info("kill called on process instance id: {}, num: {}", processInstance.getId(),
                activeTaskProcessorMaps.size());

        if (readyToSubmitTaskQueue.size() > 0) {
            readyToSubmitTaskQueue.clear();
        }

        for (long taskCode : activeTaskProcessorMaps.keySet()) {
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskCode);
            Integer taskInstanceId = validTaskMap.get(taskCode);
            if (taskInstanceId == null || taskInstanceId.equals(0)) {
                continue;
            }
            TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
            if (taskInstance == null || taskInstance.getState().typeIsFinished()) {
                continue;
            }
            taskProcessor.action(TaskAction.STOP);
            if (taskProcessor.taskInstance().getState().typeIsFinished()) {
                StateEvent stateEvent = new StateEvent();
                stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                stateEvent.setProcessInstanceId(this.processInstance.getId());
                stateEvent.setTaskInstanceId(taskInstance.getId());
                stateEvent.setExecutionStatus(taskProcessor.taskInstance().getState());
                this.addStateEvent(stateEvent);
            }
        }
    }

    public boolean workFlowFinish() {
        return this.processInstance.getState().typeIsFinished();
    }

    /**
     * handling the list of tasks to be submitted
     */
    private void submitStandByTask() {
        try {
            int length = readyToSubmitTaskQueue.size();
            for (int i = 0; i < length; i++) {
                TaskInstance task = readyToSubmitTaskQueue.peek();
                if (task == null) {
                    continue;
                }
                // stop tasks which is retrying if forced success happens
                if (task.taskCanRetry()) {
                    TaskInstance retryTask = processService.findTaskInstanceById(task.getId());
                    if (retryTask != null && retryTask.getState().equals(ExecutionStatus.FORCED_SUCCESS)) {
                        task.setState(retryTask.getState());
                        logger.info("task: {} has been forced success, put it into complete task list and stop retrying", task.getName());
                        removeTaskFromStandbyList(task);
                        completeTaskMap.put(task.getTaskCode(), task.getId());
                        taskInstanceMap.put(task.getId(), task);
                        submitPostNode(Long.toString(task.getTaskCode()));
                        continue;
                    }
                }
                //init varPool only this task is the first time running
                if (task.isFirstRun()) {
                    //get pre task ,get all the task varPool to this task
                    Set<String> preTask = dag.getPreviousNodes(Long.toString(task.getTaskCode()));
                    getPreVarPool(task, preTask);
                }
                DependResult dependResult = getDependResultForTask(task);
                if (DependResult.SUCCESS == dependResult) {
                    Optional<TaskInstance> taskInstanceOptional = submitTaskExec(task);
                    if (!taskInstanceOptional.isPresent()) {
                        this.taskFailedSubmit = true;
                        // Remove and add to complete map and error map
                        removeTaskFromStandbyList(task);
                        completeTaskMap.put(task.getTaskCode(), task.getId());
                        errorTaskMap.put(task.getTaskCode(), task.getId());
                        logger.error("Task submitted failed, processInstanceId: {}, taskInstanceId: {}", task.getProcessInstanceId(), task.getId());
                    } else {
                        removeTaskFromStandbyList(task);
                    }
                } else if (DependResult.FAILED == dependResult) {
                    // if the dependency fails, the current node is not submitted and the state changes to failure.
                    dependFailedTaskMap.put(task.getTaskCode(), task.getId());
                    removeTaskFromStandbyList(task);
                    logger.info("Task dependent result is failed, taskInstanceId:{} depend result : {}", task.getId(), dependResult);
                } else if (DependResult.NON_EXEC == dependResult) {
                    // for some reasons(depend task pause/stop) this task would not be submit
                    removeTaskFromStandbyList(task);
                    logger.info("Remove task due to depend result not executed, taskInstanceId:{} depend result : {}", task.getId(), dependResult);
                }
            }
        } catch (Exception e) {
            logger.error("submit standby task error", e);
        }
    }

    /**
     * get recovery task instance list
     *
     * @param taskIdArray task id array
     * @return recovery task instance list
     */
    private List<TaskInstance> getRecoverTaskInstanceList(String[] taskIdArray) {
        if (taskIdArray == null || taskIdArray.length == 0) {
            return new ArrayList<>();
        }
        List<Integer> taskIdList = new ArrayList<>(taskIdArray.length);
        for (String taskId : taskIdArray) {
            try {
                Integer id = Integer.valueOf(taskId);
                taskIdList.add(id);
            } catch (Exception e) {
                logger.error("get recovery task instance failed ", e);
            }
        }
        return processService.findTaskInstanceByIdList(taskIdList);
    }

    /**
     * get start task instance list
     *
     * @param cmdParam command param
     * @return task instance list
     */
    private List<TaskInstance> getStartTaskInstanceList(String cmdParam) {

        List<TaskInstance> instanceList = new ArrayList<>();
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);

        if (paramMap != null && paramMap.containsKey(CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            String[] idList = paramMap.get(CMD_PARAM_RECOVERY_START_NODE_STRING).split(Constants.COMMA);
            instanceList = getRecoverTaskInstanceList(idList);
        }
        return instanceList;
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
        if (ExecutionStatus.RUNNING_EXECUTION == processInstance.getState() && processInstance.getRunTimes() == 1) {
            return true;
        } else if (processInstance.getRecovery().equals(Flag.YES)) {
            // host is empty use old task instance
            return false;
        } else {
            return false;
        }
    }

    public void resubmit(long taskCode) throws Exception {
        ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskCode);
        if (taskProcessor != null) {
            taskProcessor.action(TaskAction.RESUBMIT);
            logger.debug("RESUBMIT: task code:{}", taskCode);
        } else {
            throw new Exception("resubmit error, taskProcessor is null, task code: " + taskCode);
        }
    }
    private void setGlobalParamIfCommanded(ProcessDefinition processDefinition, Map<String, String> cmdParam) {
        // get start params from command param
        Map<String, String> startParamMap = new HashMap<>();
        if (cmdParam.containsKey(Constants.CMD_PARAM_START_PARAMS)) {
            String startParamJson = cmdParam.get(Constants.CMD_PARAM_START_PARAMS);
            startParamMap = JSONUtils.toMap(startParamJson);
        }
        Map<String, String> fatherParamMap = new HashMap<>();
        if (cmdParam.containsKey(Constants.CMD_PARAM_FATHER_PARAMS)) {
            String fatherParamJson = cmdParam.get(Constants.CMD_PARAM_FATHER_PARAMS);
            fatherParamMap = JSONUtils.toMap(fatherParamJson);
        }
        startParamMap.putAll(fatherParamMap);
        // set start param into global params
        Map<String, String> globalMap = processDefinition.getGlobalParamMap();
        List<Property> globalParamList = processDefinition.getGlobalParamList();
        if (startParamMap.size() > 0 && globalMap != null) {
            //start param to overwrite global param
            for (Map.Entry<String, String> param : globalMap.entrySet()) {
                String val = startParamMap.get(param.getKey());
                if (val != null) {
                    param.setValue(val);
                }
            }
            //start param to create new global param if global not exist
            for (Map.Entry<String, String> startParam : startParamMap.entrySet()) {
                if (!globalMap.containsKey(startParam.getKey())) {
                    globalMap.put(startParam.getKey(), startParam.getValue());
                    globalParamList.add(new Property(startParam.getKey(), IN, VARCHAR, startParam.getValue()));
                }
            }
        }
    }

    private void measureProcessState(StateEvent processStateEvent) {
        if (processStateEvent.getExecutionStatus().typeIsFinished()) {
            ProcessInstanceMetrics.incProcessInstanceFinish();
        }
        switch (processStateEvent.getExecutionStatus()) {
            case STOP:
                ProcessInstanceMetrics.incProcessInstanceStop();
                break;
            case SUCCESS:
                ProcessInstanceMetrics.incProcessInstanceSuccess();
                break;
            case FAILURE:
                ProcessInstanceMetrics.incProcessInstanceFailure();
                break;
            default:
                break;
        }
    }

    private void measureTaskState(StateEvent taskStateEvent) {
        if (taskStateEvent == null || taskStateEvent.getExecutionStatus() == null) {
            // the event is broken
            logger.warn("The task event is broken..., taskEvent: {}", taskStateEvent);
            return;
        }
        if (taskStateEvent.getExecutionStatus().typeIsFinished()) {
            TaskMetrics.incTaskFinish();
        }
        switch (taskStateEvent.getExecutionStatus()) {
            case STOP:
                TaskMetrics.incTaskStop();
                break;
            case SUCCESS:
                TaskMetrics.incTaskSuccess();
                break;
            case FAILURE:
                TaskMetrics.incTaskFailure();
                break;
            default:
                break;
        }
    }
}