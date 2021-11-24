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
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.remote.command.HostUpdateCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * master exec thread,split dag
 */
public class WorkflowExecuteThread implements Runnable {

    /**
     * logger of WorkflowExecuteThread
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteThread.class);

    /**
     * master config
     */
    private MasterConfig masterConfig;

    /**
     * process service
     */
    private ProcessService processService;

    /**
     * alert manager
     */
    private ProcessAlertManager processAlertManager;

    /**
     * netty executor manager
     */
    private NettyExecutorManager nettyExecutorManager;

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
    private Map<Integer, TaskInstance> taskInstanceMap = new ConcurrentHashMap<>();

    /**
     * running TaskNode, taskId as key
     */
    private final Map<Integer, ITaskProcessor> activeTaskProcessorMaps = new ConcurrentHashMap<>();

    /**
     * valid task map, taskCode as key, taskId as value
     */
    private Map<String, Integer> validTaskMap = new ConcurrentHashMap<>();

    /**
     * error task map, taskCode as key, taskId as value
     */
    private Map<String, Integer> errorTaskMap = new ConcurrentHashMap<>();

    /**
     * complete task map, taskCode as key, taskId as value
     */
    private Map<String, Integer> completeTaskMap = new ConcurrentHashMap<>();

    /**
     * depend failed task map, taskCode as key, taskId as value
     */
    private Map<String, Integer> dependFailedTaskMap = new ConcurrentHashMap<>();

    /**
     * forbidden task map, code as key
     */
    private Map<String, TaskNode> forbiddenTaskMap = new ConcurrentHashMap<>();

    /**
     * skip task map, code as key
     */
    private Map<String, TaskNode> skipTaskNodeMap = new ConcurrentHashMap<>();

    /**
     * complement date list
     */
    private List<Date> complementListDate = Lists.newLinkedList();

    /**
     * task timeout check list
     */
    private ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList;

    /**
     * state event queue
     */
    private ConcurrentLinkedQueue<StateEvent> stateEvents = new ConcurrentLinkedQueue<>();

    /**
     * ready to submit task queue
     */
    private PeerTaskInstancePriorityQueue readyToSubmitTaskQueue = new PeerTaskInstancePriorityQueue();

    /**
     * constructor of WorkflowExecuteThread
     *
     * @param processInstance processInstance
     * @param processService processService
     * @param nettyExecutorManager nettyExecutorManager
     */
    public WorkflowExecuteThread(ProcessInstance processInstance
            , ProcessService processService
            , NettyExecutorManager nettyExecutorManager
            , ProcessAlertManager processAlertManager
            , MasterConfig masterConfig
            , ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList) {
        this.processService = processService;

        this.processInstance = processInstance;
        this.masterConfig = masterConfig;
        this.nettyExecutorManager = nettyExecutorManager;
        this.processAlertManager = processAlertManager;
        this.taskTimeoutCheckList = taskTimeoutCheckList;
    }

    @Override
    public void run() {
        try {
            if (!this.isStart()) {
                startProcess();
            } else {
                handleEvents();
            }
        } catch (Exception e) {
            logger.error("handler error:", e);
        }
    }

    /**
     * the process start nodes are submitted completely.
     */
    public boolean isStart() {
        return this.isStart;
    }

    private void handleEvents() {
        while (this.stateEvents.size() > 0) {

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
            logger.info("state event would be abounded :{}", stateEvent.toString());
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
        logger.info("process event: {}", stateEvent.toString());

        if (!checkProcessInstance(stateEvent)) {
            return false;
        }

        boolean result = false;
        switch (stateEvent.getType()) {
            case PROCESS_STATE_CHANGE:
                result = processStateChangeHandler(stateEvent);
                break;
            case TASK_STATE_CHANGE:
                result = taskStateChangeHandler(stateEvent);
                break;
            case PROCESS_TIMEOUT:
                result = processTimeout();
                break;
            case TASK_TIMEOUT:
                result = taskTimeout(stateEvent);
                break;
            default:
                break;
        }

        if (result) {
            this.stateEvents.remove(stateEvent);
        }
        return result;
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
        if (TaskTimeoutStrategy.FAILED == taskTimeoutStrategy) {
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(stateEvent.getTaskInstanceId());
            taskProcessor.action(TaskAction.TIMEOUT);
            return false;
        } else {
            processAlertManager.sendTaskTimeoutAlert(processInstance, taskInstance, taskInstance.getTaskDefine());
            return true;
        }
    }

    private boolean processTimeout() {
        this.processAlertManager.sendProcessTimeoutAlert(this.processInstance, this.processDefinition);
        return true;
    }

    private boolean taskStateChangeHandler(StateEvent stateEvent) {
        if (!checkTaskInstanceByStateEvent(stateEvent)) {
            return true;
        }

        TaskInstance task = getTaskInstance(stateEvent.getTaskInstanceId());
        if (task.getState() == null) {
            logger.error("task state is null, state handler error: {}", stateEvent);
            return true;
        }

        if (task.getState().typeIsFinished()) {
            taskFinished(task);
        } else if (activeTaskProcessorMaps.containsKey(stateEvent.getTaskInstanceId())) {
            ITaskProcessor iTaskProcessor = activeTaskProcessorMaps.get(stateEvent.getTaskInstanceId());
            iTaskProcessor.run();

            if (iTaskProcessor.taskState().typeIsFinished()) {
                task = processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
                taskFinished(task);
            }
        } else {
            logger.error("state handler error: {}", stateEvent);
        }
        return true;
    }

    private void taskFinished(TaskInstance task) {
        logger.info("work flow {} task {} state:{} ",
                processInstance.getId(),
                task.getId(),
                task.getState());
        if (task.taskCanRetry()) {
            addTaskToStandByList(task);
            if (!task.retryTaskIntervalOverTime()) {
                logger.info("failure task will be submitted: process id: {}, task instance id: {} state:{} retry times:{} / {}, interval:{}",
                        processInstance.getId(),
                        task.getId(),
                        task.getState(),
                        task.getRetryTimes(),
                        task.getMaxRetryTimes(),
                        task.getRetryInterval());
                this.addTimeoutCheck(task);
            } else {
                submitStandByTask();
            }
            return;
        }

        completeTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
        activeTaskProcessorMaps.remove(task.getId());
        taskTimeoutCheckList.remove(task.getId());

        if (task.getState().typeIsSuccess()) {
            processInstance.setVarPool(task.getVarPool());
            processService.saveProcessInstance(processInstance);
            submitPostNode(Long.toString(task.getTaskCode()));
        } else if (task.getState().typeIsFailure()) {
            if (task.isConditionsTask()
                    || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()), dag)) {
                submitPostNode(Long.toString(task.getTaskCode()));
            } else {
                errorTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
                if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                    killAllTasks();
                }
            }
        }
        this.updateProcessInstanceState();
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

        validTaskMap.remove(Long.toString(taskInstance.getTaskCode()));
        if (Flag.YES == taskInstance.getFlag()) {
            validTaskMap.put(Long.toString(taskInstance.getTaskCode()), taskInstance.getId());
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
    public TaskInstance getTaskInstance(int taskInstanceId) {
        if (taskInstanceMap.containsKey(taskInstanceId)) {
            return taskInstanceMap.get(taskInstanceId);
        }
        return null;
    }

    private boolean processStateChangeHandler(StateEvent stateEvent) {
        try {
            logger.info("process:{} state {} change to {}", processInstance.getId(), processInstance.getState(), stateEvent.getExecutionStatus());
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

    private boolean processComplementData() throws Exception {
        if (!needComplementProcess()) {
            return false;
        }

        Date scheduleDate = processInstance.getScheduleTime();
        if (scheduleDate == null) {
            scheduleDate = complementListDate.get(0);
        } else if (processInstance.getState().typeIsFinished()) {
            endProcess();
            if (complementListDate.size() <= 0) {
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
                    complementListDate.toString());
            scheduleDate = complementListDate.get(index + 1);
            //the next process complement
            processInstance.setId(0);
        }
        processInstance.setScheduleTime(scheduleDate);
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
            processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
        }

        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(null);
        processService.saveProcessInstance(processInstance);
        this.taskInstanceMap.clear();
        startProcess();
        return true;
    }

    private boolean needComplementProcess() {
        if (processInstance.isComplementData()
                && Flag.NO == processInstance.getIsSubProcess()) {
            return true;
        }
        return false;
    }

    private void startProcess() throws Exception {
        if (this.taskInstanceMap.size() == 0) {
            isStart = false;
            buildFlowDag();
            initTaskQueue();
            submitPostNode(null);
            isStart = true;
        }
    }

    /**
     * process end handle
     */
    private void endProcess() {
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
    }

    public void checkSerialProcess(ProcessDefinition processDefinition) {
        int nextInstanceId = processInstance.getNextProcessInstanceId();
        if (nextInstanceId == 0) {
            ProcessInstance nextProcessInstance = this.processService.loadNextProcess4Serial(processInstance.getProcessDefinition().getCode(), ExecutionStatus.SERIAL_WAIT.getCode());
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
        command.setProcessDefinitionCode(processDefinition.getCode());
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
        List<TaskNode> taskNodeList =
                processService.transformTask(processService.findRelationByCode(processDefinition.getProjectCode(), processDefinition.getCode()), Lists.newArrayList());
        forbiddenTaskMap.clear();

        taskNodeList.forEach(taskNode -> {
            if (taskNode.isForbidden()) {
                forbiddenTaskMap.put(Long.toString(taskNode.getCode()), taskNode);
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

        List<TaskInstance> validTaskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
        for (TaskInstance task : validTaskInstanceList) {
            validTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
            taskInstanceMap.put(task.getId(), task);

            if (task.isTaskComplete()) {
                completeTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
            }
            if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()), dag)) {
                continue;
            }
            if (task.getState().typeIsFailure() && !task.taskCanRetry()) {
                errorTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
            }
        }

        if (processInstance.isComplementData() && complementListDate.size() == 0) {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            if (cmdParam != null && cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)) {
                Date start = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
                Date end = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
                List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(processInstance.getProcessDefinitionCode());
                if (complementListDate.size() == 0 && needComplementProcess()) {
                    complementListDate = CronUtils.getSelfFireDateList(start, end, schedules);
                    logger.info(" process definition code:{} complement data: {}",
                            processInstance.getProcessDefinitionCode(), complementListDate.toString());

                    if (complementListDate.size() > 0 && Flag.NO == processInstance.getIsSubProcess()) {
                        processInstance.setScheduleTime(complementListDate.get(0));
                        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                                processDefinition.getGlobalParamMap(),
                                processDefinition.getGlobalParamList(),
                                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));
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
    private TaskInstance submitTaskExec(TaskInstance taskInstance) {
        try {
            ITaskProcessor taskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType());
            if (taskInstance.getState() == ExecutionStatus.RUNNING_EXECUTION
                    && taskProcessor.getType().equalsIgnoreCase(Constants.COMMON_TASK_TYPE)) {
                notifyProcessHostUpdate(taskInstance);
            }

            // package task instance before submit
            processService.packageTaskInstance(taskInstance, processInstance);

            boolean submit = taskProcessor.submit(taskInstance, processInstance, masterConfig.getTaskCommitRetryTimes(), masterConfig.getTaskCommitInterval());
            if (!submit) {
                logger.error("process id:{} name:{} submit standby task id:{} name:{} failed!",
                        processInstance.getId(), processInstance.getName(),
                        taskInstance.getId(), taskInstance.getName());
                return null;
            }
            taskInstanceMap.put(taskInstance.getId(), taskInstance);
            activeTaskProcessorMaps.put(taskInstance.getId(), taskProcessor);
            taskProcessor.run();

            addTimeoutCheck(taskInstance);

            if (taskProcessor.taskState().typeIsFinished()) {
                StateEvent stateEvent = new StateEvent();
                stateEvent.setProcessInstanceId(this.processInstance.getId());
                stateEvent.setTaskInstanceId(taskInstance.getId());
                stateEvent.setExecutionStatus(taskProcessor.taskState());
                stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                this.stateEvents.add(stateEvent);
            }
            return taskInstance;
        } catch (Exception e) {
            logger.error("submit standby task error", e);
            return null;
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

    private void addTimeoutCheck(TaskInstance taskInstance) {
        if (taskTimeoutCheckList.containsKey(taskInstance.getId())) {
            return;
        }
        TaskDefinition taskDefinition = taskInstance.getTaskDefine();
        if (taskDefinition == null) {
            logger.error("taskDefinition is null, taskId:{}", taskInstance.getId());
            return;
        }

        if (TimeoutFlag.OPEN == taskDefinition.getTimeoutFlag() || taskInstance.taskCanRetry()) {
            this.taskTimeoutCheckList.put(taskInstance.getId(), taskInstance);
        } else {
            if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
                this.taskTimeoutCheckList.put(taskInstance.getId(), taskInstance);
            }
        }
    }

    /**
     * find task instance in db.
     * in case submit more than one same name task in the same time.
     *
     * @param taskCode task code
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
     * @param taskNode taskNode
     * @return TaskInstance
     */
    private TaskInstance createTaskInstance(ProcessInstance processInstance, TaskNode taskNode) {
        TaskInstance taskInstance = findTaskIfExists(taskNode.getCode(), taskNode.getVersion());
        if (taskInstance == null) {
            taskInstance = new TaskInstance();
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
        }

        return taskInstance;
    }

    public void getPreVarPool(TaskInstance taskInstance, Set<String> preTask) {
        Map<String, Property> allProperty = new HashMap<>();
        Map<String, TaskInstance> allTaskInstance = new HashMap<>();
        if (CollectionUtils.isNotEmpty(preTask)) {
            for (String preTaskCode : preTask) {
                Integer taskId = completeTaskMap.get(preTaskCode);
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
            if (checkTaskInstanceByCode(taskNodeObject.getCode())) {
                continue;
            }
            TaskInstance task = createTaskInstance(processInstance, taskNodeObject);
            taskInstances.add(task);
        }

        // if previous node success , post node submit
        for (TaskInstance task : taskInstances) {

            if (readyToSubmitTaskQueue.contains(task)) {
                continue;
            }

            if (completeTaskMap.containsKey(Long.toString(task.getTaskCode()))) {
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
        List<String> depCodeList = taskNode.getDepList();
        for (String depsNode : depCodeList) {
            if (!dag.containsNode(depsNode)
                    || forbiddenTaskMap.containsKey(depsNode)
                    || skipTaskNodeMap.containsKey(depsNode)) {
                continue;
            }
            // dependencies must be fully completed
            if (!completeTaskMap.containsKey(depsNode)) {
                return DependResult.WAITING;
            }
            Integer depsTaskId = completeTaskMap.get(depsNode);
            ExecutionStatus depTaskState = taskInstanceMap.get(depsTaskId).getState();
            if (depTaskState.typeIsPause() || depTaskState.typeIsCancel()) {
                return DependResult.NON_EXEC;
            }
            // ignore task state if current task is condition
            if (taskNode.isConditionsTask()) {
                continue;
            }
            if (!dependTaskSuccess(depsNode, taskCode)) {
                return DependResult.FAILED;
            }
        }
        logger.info("taskCode: {} completeDependTaskList: {}", taskCode, Arrays.toString(completeTaskMap.keySet().toArray()));
        return DependResult.SUCCESS;
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
            Integer taskInstanceId = completeTaskMap.get(dependNodeName);
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
                return readyToSubmitTaskQueue.size() == 0 && activeTaskProcessorMaps.size() == 0;
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
                || !isComplementEnd()
                || readyToSubmitTaskQueue.size() > 0) {
            return ExecutionStatus.PAUSE;
        } else {
            return ExecutionStatus.SUCCESS;
        }
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
        // process failure
        if (processFailed()) {
            return ExecutionStatus.FAILURE;
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
            if (CollectionUtils.isNotEmpty(stopList)
                    || CollectionUtils.isNotEmpty(killList)
                    || !isComplementEnd()) {
                return ExecutionStatus.STOP;
            } else {
                return ExecutionStatus.SUCCESS;
            }
        }

        // success
        if (state == ExecutionStatus.RUNNING_EXECUTION) {
            List<TaskInstance> killTasks = getCompleteTaskByState(ExecutionStatus.KILL);
            if (readyToSubmitTaskQueue.size() > 0) {
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
            this.processStateChangeHandler(stateEvent);
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
        logger.info("add task to stand by list: {}", taskInstance.getName());
        try {
            if (!readyToSubmitTaskQueue.contains(taskInstance)) {
                readyToSubmitTaskQueue.put(taskInstance);
            }
        } catch (Exception e) {
            logger.error("add task instance to readyToSubmitTaskQueue error, taskName: {}", taskInstance.getName(), e);
        }
    }

    /**
     * remove task from stand by list
     *
     * @param taskInstance task instance
     */
    private void removeTaskFromStandbyList(TaskInstance taskInstance) {
        logger.info("remove task from stand by list, id: {} name:{}",
                taskInstance.getId(),
                taskInstance.getName());
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
        for (int taskId : activeTaskProcessorMaps.keySet()) {
            TaskInstance taskInstance = processService.findTaskInstanceById(taskId);
            if (taskInstance == null || taskInstance.getState().typeIsFinished()) {
                continue;
            }
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskId);
            taskProcessor.action(TaskAction.STOP);
            if (taskProcessor.taskState().typeIsFinished()) {
                StateEvent stateEvent = new StateEvent();
                stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                stateEvent.setProcessInstanceId(this.processInstance.getId());
                stateEvent.setTaskInstanceId(taskInstance.getId());
                stateEvent.setExecutionStatus(taskProcessor.taskState());
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
                        completeTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
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
                    if (task.retryTaskIntervalOverTime()) {
                        int originalId = task.getId();
                        TaskInstance taskInstance = submitTaskExec(task);
                        if (taskInstance == null) {
                            this.taskFailedSubmit = true;
                        } else {
                            removeTaskFromStandbyList(task);
                            if (taskInstance.getId() != originalId) {
                                activeTaskProcessorMaps.remove(originalId);
                            }
                        }
                    }
                } else if (DependResult.FAILED == dependResult) {
                    // if the dependency fails, the current node is not submitted and the state changes to failure.
                    dependFailedTaskMap.put(Long.toString(task.getTaskCode()), task.getId());
                    removeTaskFromStandbyList(task);
                    logger.info("task {},id:{} depend result : {}", task.getName(), task.getId(), dependResult);
                } else if (DependResult.NON_EXEC == dependResult) {
                    // for some reasons(depend task pause/stop) this task would not be submit
                    removeTaskFromStandbyList(task);
                    logger.info("remove task {},id:{} , because depend result : {}", task.getName(), task.getId(), dependResult);
                }
            }
        } catch (Exception e) {
            logger.error("submit standby task error", e);
        }
    }

    /**
     * get recovery task instance
     *
     * @param taskId task id
     * @return recovery task instance
     */
    private TaskInstance getRecoveryTaskInstance(String taskId) {
        if (!StringUtils.isNotEmpty(taskId)) {
            return null;
        }
        try {
            Integer intId = Integer.valueOf(taskId);
            TaskInstance task = processService.findTaskInstanceById(intId);
            if (task == null) {
                logger.error("start node id cannot be found: {}", taskId);
            } else {
                return task;
            }
        } catch (Exception e) {
            logger.error("get recovery task instance failed ", e);
        }
        return null;
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
            for (String nodeId : idList) {
                TaskInstance task = getRecoveryTaskInstance(nodeId);
                if (task != null) {
                    instanceList.add(task);
                }
            }
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
     * @param totalTaskNodeList total task node list
     * @param startNodeNameList start node name list
     * @param recoveryNodeCodeList recovery node code list
     * @param depNodeType depend node type
     * @return ProcessDag           process dag
     * @throws Exception exception
     */
    public ProcessDag generateFlowDag(List<TaskNode> totalTaskNodeList,
                                      List<String> startNodeNameList,
                                      List<String> recoveryNodeCodeList,
                                      TaskDependType depNodeType) throws Exception {
        return DagHelper.generateFlowDag(totalTaskNodeList, startNodeNameList, recoveryNodeCodeList, depNodeType);
    }
}
