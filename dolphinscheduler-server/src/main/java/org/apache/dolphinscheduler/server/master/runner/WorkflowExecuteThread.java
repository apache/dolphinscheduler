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
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.remote.command.HostUpdateCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * master exec thread,split dag
 */
public class WorkflowExecuteThread implements Runnable {

    /**
     * logger of WorkflowExecuteThread
     */
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteThread.class);
    /**
     * runing TaskNode
     */
    private final Map<Integer, ITaskProcessor> activeTaskProcessorMaps = new ConcurrentHashMap<>();

    /**
     * handle task event
     */
    private TaskResponseService taskResponseService;

    /**
     * process instance
     */
    private ProcessInstance processInstance;
    /**
     * submit failure nodes
     */
    private boolean taskFailedSubmit = false;

    /**
     * recover node id list
     */
    private List<TaskInstance> recoverNodeIdList = new ArrayList<>();

    /**
     * error task list
     */
    private Map<String, TaskInstance> errorTaskList = new ConcurrentHashMap<>();

    /**
     * complete task list
     */
    private Map<String, TaskInstance> completeTaskList = new ConcurrentHashMap<>();

    /**
     * ready to submit task queue
     */
    private PeerTaskInstancePriorityQueue readyToSubmitTaskQueue = new PeerTaskInstancePriorityQueue();

    /**
     * depend failed task map
     */
    private Map<String, TaskInstance> dependFailedTask = new ConcurrentHashMap<>();

    /**
     * forbidden task map
     */
    private Map<String, TaskNode> forbiddenTaskList = new ConcurrentHashMap<>();

    /**
     * skip task map
     */
    private Map<String, TaskNode> skipTaskNodeList = new ConcurrentHashMap<>();

    /**
     * recover tolerance fault task list
     */
    private List<TaskInstance> recoverToleranceFaultTaskList = new ArrayList<>();

    /**
     * alert manager
     */
    private ProcessAlertManager processAlertManager;

    /**
     * the object of DAG
     */
    private DAG<String, TaskNode, TaskNodeRelation> dag;

    /**
     * process service
     */
    private ProcessService processService;

    /**
     * master config
     */
    private MasterConfig masterConfig;

    /**
     *
     */
    private NettyExecutorManager nettyExecutorManager;

    private ConcurrentLinkedQueue<StateEvent> stateEvents = new ConcurrentLinkedQueue<>();

    private List<Date> complementListDate = Lists.newLinkedList();

    private Table<Integer, Long, TaskInstance> taskInstanceHashMap = HashBasedTable.create();
    private ProcessDefinition processDefinition;
    private String key;

    private ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList;

    /**
     * task retry check list
     */
    private ConcurrentHashMap<Integer, TaskInstance> taskRetryCheckList;

    /**
     * start flag, true: start nodes submit completely
     */
    private boolean isStart = false;

    /**
     * constructor of WorkflowExecuteThread
     *
     * @param processInstance processInstance
     * @param processService processService
     * @param nettyExecutorManager nettyExecutorManager
     */
    public WorkflowExecuteThread(ProcessInstance processInstance
            , TaskResponseService taskResponseService
            , ProcessService processService
            , NettyExecutorManager nettyExecutorManager
            , ProcessAlertManager processAlertManager
            , MasterConfig masterConfig
            , ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList
            , ConcurrentHashMap<Integer, TaskInstance> taskRetryCheckList) {
        this.processService = processService;
        this.taskResponseService = taskResponseService;
        this.processInstance = processInstance;
        this.masterConfig = masterConfig;
        this.nettyExecutorManager = nettyExecutorManager;
        this.processAlertManager = processAlertManager;
        this.taskTimeoutCheckList = taskTimeoutCheckList;
        this.taskRetryCheckList = taskRetryCheckList;
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

        if (!checkStateEvent(stateEvent)) {
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

        if (!taskInstanceHashMap.containsRow(stateEvent.getTaskInstanceId())) {
            return true;
        }

        // get lastest task instance into
        TaskInstance taskInstance = processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
        TaskDefinition taskDefinition = processService.findTaskDefinition(taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
        taskInstance.setTaskDefine(taskDefinition);

        if (TimeoutFlag.CLOSE == taskInstance.getTaskDefine().getTimeoutFlag()) {
            return true;
        }
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED == taskTimeoutStrategy) {
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(stateEvent.getTaskInstanceId());
            taskProcessor.action(TaskAction.TIMEOUT);
        } else {
            processAlertManager.sendTaskTimeoutAlert(processInstance, taskInstance, taskInstance.getTaskDefine());
        }
        return true;
    }

    private boolean processTimeout() {
        this.processAlertManager.sendProcessTimeoutAlert(this.processInstance, this.processDefinition);
        return true;
    }

    private boolean taskStateChangeHandler(StateEvent stateEvent) {
        TaskInstance task = processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
        if (task.getState().typeIsFinished()) {
            taskFinished(task);
        } else if (activeTaskProcessorMaps.containsKey(stateEvent.getTaskInstanceId())) {
            ITaskProcessor iTaskProcessor = activeTaskProcessorMaps.get(stateEvent.getTaskInstanceId());
            iTaskProcessor.action(TaskAction.RUN);

            if (iTaskProcessor.taskState().typeIsFinished()) {
                task = processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
                taskFinished(task);
            }
        } else {
            logger.error("state handler error: {}", stateEvent.toString());
        }
        return true;
    }

    private void taskFinished(TaskInstance task) {
        logger.info("work flow {} task {} state:{} ",
                processInstance.getId(),
                task.getId(),
                task.getState());
        if (task.taskCanRetry() && processInstance.getState() != ExecutionStatus.READY_STOP) {
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
                this.addRetryCheck(task);
            } else {
                submitStandByTask();
                taskTimeoutCheckList.remove(task.getId());
                taskRetryCheckList.remove(task.getId());
            }
            return;
        }
        ProcessInstance processInstance = processService.findProcessInstanceById(this.processInstance.getId());
        completeTaskList.put(Long.toString(task.getTaskCode()), task);
        activeTaskProcessorMaps.remove(task.getId());
        taskTimeoutCheckList.remove(task.getId());
        taskRetryCheckList.remove(task.getId());
        if (task.getState().typeIsSuccess()) {
            processInstance.setVarPool(task.getVarPool());
            processService.saveProcessInstance(processInstance);
            submitPostNode(Long.toString(task.getTaskCode()));
        } else if (task.getState().typeIsFailure()) {
            if (task.isConditionsTask()
                    || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()), dag)) {
                submitPostNode(Long.toString(task.getTaskCode()));
            } else {
                errorTaskList.put(Long.toString(task.getTaskCode()), task);
                if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                    killAllTasks();
                }
            }
        }
        this.updateProcessInstanceState();
    }

    private boolean checkStateEvent(StateEvent stateEvent) {
        if (this.processInstance.getId() != stateEvent.getProcessInstanceId()) {
            logger.error("mismatch process instance id: {}, state event:{}, task instance id:{}",
                    this.processInstance.getId(),
                    stateEvent.toString(),
                    stateEvent.getTaskInstanceId());
            return false;
        }
        return true;
    }

    private boolean processStateChangeHandler(StateEvent stateEvent) {
        try {
            logger.info("process:{} state {} change to {}", processInstance.getId(), processInstance.getState(), stateEvent.getExecutionStatus());
            processInstance = processService.findProcessInstanceById(this.processInstance.getId());

            if (stateEvent.getExecutionStatus() == ExecutionStatus.STOP) {
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

        }

        //the next process complement
        int create = this.createComplementDataCommand(scheduleDate);
        if (create > 0) {
            logger.info("create complement data command successfully. process id: {}", processInstance.getId());
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
        cmdParam.replace(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.format(scheduleDate, "yyyy-MM-dd HH:mm:ss"));
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

    private void startProcess() throws Exception {
        if (this.taskInstanceHashMap.size() == 0) {
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
        processInstance.setEndTime(new Date());
        processService.updateProcessInstance(processInstance);
        if (processInstance.getState().typeIsWaitingThread()) {
            processService.createRecoveryWaitingThreadCommand(null, processInstance);
        }
        if (processAlertManager.isNeedToSendWarning(processInstance)) {
            List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(processInstance.getId());
            ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
            processAlertManager.sendAlertProcessInstance(processInstance, taskInstances, projectUser);
        }
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
        processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion());
        recoverNodeIdList = getStartTaskInstanceList(processInstance.getCommandParam());
        List<ProcessTaskRelation> processTaskRelationList = processService.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskDefinitionLog> taskDefinitionLogList = processService.getTaskDefineLogListByRelation(processTaskRelationList);
        List<TaskNode> taskNodeList = processService.transformTask(processTaskRelationList, taskDefinitionLogList);
        forbiddenTaskList.clear();

        taskNodeList.forEach(taskNode -> {
            if (taskNode.isForbidden()) {
                forbiddenTaskList.put(Long.toString(taskNode.getCode()), taskNode);
            }
        });

        // generate process to get DAG info
        List<String> recoveryNodeCodeList = getRecoveryNodeCodeList();
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
        dependFailedTask.clear();
        completeTaskList.clear();
        errorTaskList.clear();
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
        for (TaskInstance task : taskInstanceList) {
            if (task.isTaskComplete()) {
                completeTaskList.put(Long.toString(task.getTaskCode()), task);
            }
            if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(Long.toString(task.getTaskCode()), dag)) {
                continue;
            }
            if (task.getState().typeIsFailure() && !task.taskCanRetry()) {
                errorTaskList.put(Long.toString(task.getTaskCode()), task);
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
            taskProcessor.init(taskInstance, processInstance);
            boolean submit = taskProcessor.action(TaskAction.SUBMIT);
            if (submit) {
                this.taskInstanceHashMap.put(taskInstance.getId(), taskInstance.getTaskCode(), taskInstance);
                activeTaskProcessorMaps.put(taskInstance.getId(), taskProcessor);
                taskProcessor.action(TaskAction.RUN);
                addTimeoutCheck(taskInstance);
                addRetryCheck(taskInstance);
                TaskDefinition taskDefinition = processService.findTaskDefinition(
                        taskInstance.getTaskCode(),
                        taskInstance.getTaskDefinitionVersion());
                taskInstance.setTaskDefine(taskDefinition);
                if (taskProcessor.taskState().typeIsFinished()) {
                    StateEvent stateEvent = new StateEvent();
                    stateEvent.setProcessInstanceId(this.processInstance.getId());
                    stateEvent.setTaskInstanceId(taskInstance.getId());
                    stateEvent.setExecutionStatus(taskProcessor.taskState());
                    stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                    this.stateEvents.add(stateEvent);
                }
                return taskInstance;
            } else {
                logger.error("process id:{} name:{} submit standby task id:{} name:{} failed!",
                        processInstance.getId(), processInstance.getName(),
                        taskInstance.getId(), taskInstance.getName());
                return null;
            }
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
        if (taskInstance.getTaskDefine() == null) {
            TaskDefinition taskDefinition = processService.findTaskDefinition(
                    taskInstance.getTaskCode(),
                    taskInstance.getTaskDefinitionVersion()
            );
            taskInstance.setTaskDefine(taskDefinition);
        }
        if (TimeoutFlag.OPEN == taskInstance.getTaskDefine().getTimeoutFlag()) {
            this.taskTimeoutCheckList.put(taskInstance.getId(), taskInstance);
        }
        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            this.taskTimeoutCheckList.put(taskInstance.getId(), taskInstance);
        }
    }

    private void addRetryCheck(TaskInstance taskInstance) {
        if (taskRetryCheckList.containsKey(taskInstance.getId())) {
            return;
        }
        if (taskInstance.getTaskDefine() == null) {
            TaskDefinition taskDefinition = processService.findTaskDefinition(
                    taskInstance.getTaskCode(),
                    taskInstance.getTaskDefinitionVersion()
            );
            taskInstance.setTaskDefine(taskDefinition);
        }
        if (taskInstance.taskCanRetry()) {
            this.taskRetryCheckList.put(taskInstance.getId(), taskInstance);
        }

        if (taskInstance.isDependTask() || taskInstance.isSubProcess()) {
            this.taskRetryCheckList.put(taskInstance.getId(), taskInstance);
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
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(this.processInstance.getId());
        for (TaskInstance taskInstance : taskInstanceList) {
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
                TaskInstance preTaskInstance = completeTaskList.get(preTaskCode);
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

    private void submitPostNode(String parentNodeCode) {
        Set<String> submitTaskNodeList = DagHelper.parsePostNodes(parentNodeCode, skipTaskNodeList, dag, completeTaskList);
        List<TaskInstance> taskInstances = new ArrayList<>();
        for (String taskNode : submitTaskNodeList) {
            TaskNode taskNodeObject = dag.getNode(taskNode);
            if (taskInstanceHashMap.containsColumn(taskNodeObject.getCode())) {
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

            if (completeTaskList.containsKey(Long.toString(task.getTaskCode()))) {
                logger.info("task {} has already run success, task id:{}", task.getName(), task.getId());
                continue;
            }
            if (task.getState().typeIsPause() || task.getState().typeIsCancel()) {
                logger.info("task {} stopped, the state is {}, task id:{}", task.getName(), task.getState(), task.getId());
            } else {
                addTaskToStandByList(task);
            }
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
            if (dag.containsNode(depsNode) && !skipTaskNodeList.containsKey(depsNode)) {
                // dependencies must be fully completed
                if (!completeTaskList.containsKey(depsNode)) {
                    return DependResult.WAITING;
                }
                ExecutionStatus depTaskState = completeTaskList.get(depsNode).getState();
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
        }
        logger.info("taskCode: {} completeDependTaskList: {}", taskCode, Arrays.toString(completeTaskList.keySet().toArray()));
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
            if (forbiddenTaskList.containsKey(depsNode)) {
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
            List<String> nextTaskList = DagHelper.parseConditionTask(dependNodeName, skipTaskNodeList, dag, completeTaskList);
            if (!nextTaskList.contains(nextNodeName)) {
                return false;
            }
        } else {
            ExecutionStatus depTaskState = completeTaskList.get(dependNodeName).getState();
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
        for (Map.Entry<String, TaskInstance> entry : completeTaskList.entrySet()) {
            if (entry.getValue().getState() == state) {
                resultList.add(entry.getValue());
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
        if (this.errorTaskList.size() > 0) {
            return true;
        }
        return this.dependFailedTask.size() > 0;
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
        ProcessInstance instance = processService.findProcessInstanceById(processInstance.getId());
        ExecutionStatus state = getProcessInstanceState(instance);
        if (processInstance.getState() != state) {
            logger.info(
                    "work flow process instance [id: {}, name:{}], state change from {} to {}, cmd type: {}",
                    processInstance.getId(), processInstance.getName(),
                    processInstance.getState(), state,
                    processInstance.getCommandType());

            instance.setState(state);
            processService.updateProcessInstance(instance);
            processInstance = instance;
            StateEvent stateEvent = new StateEvent();
            stateEvent.setExecutionStatus(processInstance.getState());
            stateEvent.setProcessInstanceId(this.processInstance.getId());
            stateEvent.setType(StateEventType.PROCESS_STATE_CHANGE);
            this.processStateChangeHandler(stateEvent);
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
            // need to check if the tasks with same task code is active
            boolean active = false;
            Map<Integer, TaskInstance> taskInstanceMap = taskInstanceHashMap.column(taskInstance.getTaskCode());
            if (taskInstanceMap != null && taskInstanceMap.size() > 0) {
                for (Entry<Integer, TaskInstance> entry : taskInstanceMap.entrySet()) {
                    Integer taskInstanceId = entry.getKey();
                    if (activeTaskProcessorMaps.containsKey(taskInstanceId)) {
                        TaskInstance latestTaskInstance = processService.findTaskInstanceById(taskInstanceId);
                        if (latestTaskInstance != null && !latestTaskInstance.getState().typeIsFailure()) {
                            active = true;
                            break;
                        }
                    }
                }
            }
            if (active) {
                logger.warn("task was found in active task list, task code:{}", taskInstance.getTaskCode());
                return;
            }
            logger.info("add task to stand by list, task name:{}, task id:{}, task code:{}",
                    taskInstance.getName(), taskInstance.getId(), taskInstance.getTaskCode());
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

    private void addProcessStopEvent(ProcessInstance processInstance) {
        StateEvent stateEvent = new StateEvent();
        stateEvent.setType(StateEventType.PROCESS_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.STOP);
        this.addStateEvent(stateEvent);
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

        for (int taskId : activeTaskProcessorMaps.keySet()) {
            TaskInstance taskInstance = processService.findTaskInstanceById(taskId);
            if (taskInstance == null || taskInstance.getState().typeIsFinished()) {
                continue;
            }
            ITaskProcessor taskProcessor = activeTaskProcessorMaps.get(taskId);
            taskProcessor.action(TaskAction.STOP);
            if (taskProcessor != null && taskProcessor.taskState().typeIsFinished()) {
                TaskResponseEvent taskResponseEvent = TaskResponseEvent.newActionStop(
                        taskProcessor.taskState(),
                        taskInstance.getId(),
                        this.processInstance.getId());
                taskResponseService.addResponse(taskResponseEvent);
            }
        }

        if (taskRetryCheckList.size() > 0) {
            this.taskRetryCheckList.clear();
            this.addProcessStopEvent(processInstance);
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
                        logger.info("task name: {} has been forced success, put it into complete task list and stop retrying, task id:{}", task.getName(), task.getId());
                        removeTaskFromStandbyList(task);
                        completeTaskList.put(Long.toString(task.getTaskCode()), task);
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
                    dependFailedTask.put(Long.toString(task.getTaskCode()), task);
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
    private List<String> getRecoveryNodeCodeList() {
        List<String> recoveryNodeCodeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(recoverNodeIdList)) {
            for (TaskInstance task : recoverNodeIdList) {
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

    public Map<Integer, ITaskProcessor> getActiveTaskProcessorMaps() {
        return activeTaskProcessorMaps;
    }
}
