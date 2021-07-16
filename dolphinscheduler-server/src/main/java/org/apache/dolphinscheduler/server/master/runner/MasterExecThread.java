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
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODE_NAMES;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.SEC_2_MINUTES_TIME_UNIT;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * master exec thread,split dag
 */
public class MasterExecThread implements Runnable {

    /**
     * logger of MasterExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterExecThread.class);
    /**
     * runing TaskNode
     */
    private final Map<Integer, ITaskProcessor> activeTaskNode = new ConcurrentHashMap<>();
    /**
     * task exec service
     */
    private final ExecutorService taskExecService;
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
    private NettyRemotingClient nettyRemotingClient;
    /**
     * submit post node
     *
     * @param parentNodeName parent node name
     */
    private Map<String, Object> propToValue = new ConcurrentHashMap<>();

    private ConcurrentLinkedDeque<StateEvent> stateEvents = new ConcurrentLinkedDeque<>();

    private List<Date> complementListDate = Lists.newLinkedList();

    private Table<Integer, Long, TaskInstance> taskInstanceHashMap = HashBasedTable.create();
    private ProcessDefinition processDefinition;
    private String key;


    /**
     * constructor of MasterExecThread
     *
     * @param processInstance processInstance
     * @param processService processService
     * @param nettyRemotingClient nettyRemotingClient
     */
    public MasterExecThread(ProcessInstance processInstance
            , ProcessService processService
            , NettyRemotingClient nettyRemotingClient
            , ProcessAlertManager processAlertManager
            , MasterConfig masterConfig) {
        this.processService = processService;

        this.processInstance = processInstance;
        this.masterConfig = masterConfig;
        int masterTaskExecNum = masterConfig.getMasterExecTaskNum();
        this.taskExecService = ThreadUtils.newDaemonFixedThreadExecutor("Master-Task-Exec-Thread",
                masterTaskExecNum);
        this.nettyRemotingClient = nettyRemotingClient;
        this.processAlertManager = processAlertManager;
    }

    @Override
    public void run() {
        try {
            startProcess();
            while(this.stateEvents.size() > 0){
                StateEvent stateEvent = this.stateEvents.peek();
                this.stateEventHandler(stateEvent);
                this.stateEvents.remove(stateEvent);
            }
        } catch (Exception e) {
            logger.error("handler error:",e);
        }

    }

    public String getKey(){
        if(StringUtils.isNotEmpty(key)
            || this.processDefinition == null){
            return key;
        }

        key = String.format("{}_{}_{}",
                this.processDefinition.getCode(),
                this.processDefinition.getVersion(),
                this.processInstance.getId());
        return key;
    }

    public boolean addStateEvent(StateEvent stateEvent) {
        if (stateEvents.contains(stateEvent)) {
            return false;
        }
        if (activeTaskNode.containsKey(stateEvent.getTaskInstanceId())
                || readyToSubmitTaskQueue.contains(stateEvent.getTaskInstanceId())
                ) {
            this.stateEvents.add(stateEvent);
            return true;
        }
        logger.info("state event would be abounded :{}", stateEvent.toString());
        return false;
    }

    public int eventSize(){
        return this.stateEvents.size();
    }

    public ProcessInstance getProcessInstance(){
        return this.processInstance;
    }

    private boolean stateEventHandler(StateEvent stateEvent) {
        logger.info("process event: {}", stateEvent.toString());

        if (!checkStateEvent(stateEvent)) {
            return false;
        }
        boolean result;
        if (stateEvent.getType() == "task") {
            result = taskStateChangeHandler(stateEvent);
        } else {
            result = processStateChangeHandler(stateEvent);
        }
        if(result){
            this.stateEvents.remove(stateEvent);
        }
        return result;
    }

    private boolean taskStateChangeHandler(StateEvent stateEvent) {
        logger.info("task event handler: {}", stateEvent.toString());
        if (stateEvent.getExecutionStatus().typeIsFinished()) {
            TaskInstance task = processService.findTaskInstanceById(stateEvent.getTaskInstanceId());
            if (task.taskCanRetry()) {
                addTaskToStandByList(task);
                return true;
            }
            ProcessInstance processInstance = processService.findProcessInstanceById(this.processInstance.getId());
            completeTaskList.put(task.getName(), task);
            activeTaskNode.remove(task.getId());
            if (task.getState().typeIsSuccess()) {
                processInstance.setVarPool(task.getVarPool());
                processService.saveProcessInstance(processInstance);
                submitPostNode(task.getName());
            } else if (task.getState().typeIsFailure()) {
                if (task.isConditionsTask()
                        || DagHelper.haveConditionsAfterNode(task.getName(), dag)) {
                    submitPostNode(task.getName());
                } else {
                    errorTaskList.put(task.getName(), task);
                    if (processInstance.getFailureStrategy() == FailureStrategy.END) {
                        killTheOtherTasks();
                    }
                }
            }
            this.updateProcessInstanceState();
        }else if(activeTaskNode.containsKey(stateEvent.getTaskInstanceId())){
            ITaskProcessor iTaskProcessor = activeTaskNode.get(stateEvent.getTaskInstanceId());
            iTaskProcessor.run();
            if(iTaskProcessor.taskState().typeIsFinished()){
                StateEvent taskFinished = new StateEvent();
                taskFinished.setType("task");
                taskFinished.setTaskInstanceId(stateEvent.getTaskInstanceId());
                taskFinished.setProcessInstanceId(this.processInstance.getId());
                this.taskStateChangeHandler(taskFinished);
            }
        }
        return true;
    }

    private boolean checkStateEvent(StateEvent stateEvent) {
        if (this.processInstance.getId() != stateEvent.getProcessInstanceId()) {
            logger.error("mismatch process instance id: {}, state event:{}",
                    this.processInstance.getId(),
                    stateEvent.toString());
            return false;
        }
        return true;
    }

    private boolean processStateChangeHandler(StateEvent stateEvent) {
        try {
            logger.info("process:{} state {} change to {}", processInstance.getId(), processInstance.getState(), stateEvent.getExecutionStatus());
            processInstance = processService.findProcessInstanceById(this.processInstance.getId());
            if ( processComplementData()) {
                return true;
            }
            if (stateEvent.getExecutionStatus().typeIsFinished()) {
                endProcess();
            }
            //TODO...
            //send event to dependent tasks/process
            return true;
        } catch (Exception e) {
            logger.error("process state change error:",e);
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
        } else if(processInstance.getState().typeIsFinished()){
            endProcess();
            int index = complementListDate.indexOf(scheduleDate);
            if (index >= complementListDate.size() - 1 || !processInstance.getState().typeIsSuccess()) {
                // complement data ends || no success
                return false;
            }
            scheduleDate = complementListDate.get(index+1);
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
        this.taskInstanceHashMap.clear();
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
        buildFlowDag();
        if (this.taskInstanceHashMap.size() == 0) {
            initTaskQueue();
            submitPostNode(null);
        }
    }


    /**
     * execute process
     *
     * @throws Exception exception
     */
    private void executeProcess() throws Exception {
        prepareProcess();
        runProcess();
        endProcess();
    }

    /**
     * execute complement process
     *
     * @throws Exception exception
     */
    private void executeComplementProcess() throws Exception {

        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());

        Date startDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
        Date endDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
        processService.saveProcessInstance(processInstance);

        // get schedules
        List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinition.getId());
        List<Date> listDate = Lists.newLinkedList();
        if (!CollectionUtils.isEmpty(schedules)) {
            for (Schedule schedule : schedules) {
                listDate.addAll(CronUtils.getSelfFireDateList(startDate, endDate, schedule.getCrontab()));
            }
        }
        // get first fire date
        Iterator<Date> iterator = null;
        Date scheduleDate;
        if (!CollectionUtils.isEmpty(listDate)) {
            iterator = listDate.iterator();
            scheduleDate = iterator.next();
            processInstance.setScheduleTime(scheduleDate);
            processService.updateProcessInstance(processInstance);
        } else {
            scheduleDate = processInstance.getScheduleTime();
            if (scheduleDate == null) {
                scheduleDate = startDate;
            }
        }

        while (Stopper.isRunning()) {
            logger.info("process {} start to complement {} data", processInstance.getId(), DateUtils.dateToString(scheduleDate));
            // prepare dag and other info
            prepareProcess();

            if (dag == null) {
                logger.error("process {} dag is null, please check out parameters",
                        processInstance.getId());
                processInstance.setState(ExecutionStatus.SUCCESS);
                processService.updateProcessInstance(processInstance);
                return;
            }

            // execute process ,waiting for end
            runProcess();

            endProcess();
            // process instance failure ，no more complements
            if (!processInstance.getState().typeIsSuccess()) {
                logger.info("process {} state {}, complement not completely!", processInstance.getId(), processInstance.getState());
                break;
            }
            //  current process instance success ,next execute
            if (null == iterator) {
                // loop by day
                scheduleDate = DateUtils.getSomeDay(scheduleDate, 1);
                if (scheduleDate.after(endDate)) {
                    // all success
                    logger.info("process {} complement completely!", processInstance.getId());
                    break;
                }
            } else {
                // loop by schedule date
                if (!iterator.hasNext()) {
                    // all success
                    logger.info("process {} complement completely!", processInstance.getId());
                    break;
                }
                scheduleDate = iterator.next();
            }
            // flow end
            // execute next process instance complement data
            processInstance.setScheduleTime(scheduleDate);
            if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
                cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
            }

            processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
            processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                    processDefinition.getGlobalParamMap(),
                    processDefinition.getGlobalParamList(),
                    CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));
            processInstance.setId(0);
            processInstance.setStartTime(new Date());
            processInstance.setEndTime(null);
            processService.saveProcessInstance(processInstance);
        }
    }

    /**
     * prepare process parameter
     *
     * @throws Exception exception
     */
    private void prepareProcess() throws Exception {

        // gen process dag
        buildFlowDag();

        // init task queue
        initTaskQueue();
        logger.info("prepare process :{} end", processInstance.getId());
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
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(processInstance.getId());
        ProjectUser projectUser = processService.queryProjectWithUserByProcessInstanceId(processInstance.getId());
        processAlertManager.sendAlertProcessInstance(processInstance, taskInstances, projectUser);
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
        recoverNodeIdList = getStartTaskInstanceList(processInstance.getCommandParam());
        List<TaskNode> taskNodeList =
                processService.genTaskNodeList(processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion(), new HashMap<>());
        forbiddenTaskList.clear();
        taskNodeList.forEach(taskNode -> {
            if (taskNode.isForbidden()) {
                forbiddenTaskList.put(taskNode.getName(), taskNode);
            }
        });
        // generate process to get DAG info
        List<String> recoveryNameList = getRecoveryNodeNameList();
        List<String> startNodeNameList = parseStartNodeName(processInstance.getCommandParam());
        ProcessDag processDag = generateFlowDag(taskNodeList,
                startNodeNameList, recoveryNameList, processInstance.getTaskDependType());
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
        activeTaskNode.clear();
        dependFailedTask.clear();
        completeTaskList.clear();
        errorTaskList.clear();
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
        for (TaskInstance task : taskInstanceList) {
            if (task.isTaskComplete()) {
                completeTaskList.put(task.getName(), task);
            }
            if (task.isConditionsTask() || DagHelper.haveConditionsAfterNode(task.getName(), dag)) {
                continue;
            }
            if (task.getState().typeIsFailure() && !task.taskCanRetry()) {
                errorTaskList.put(task.getName(), task);
            }
        }

        if (complementListDate.size() == 0 && needComplementProcess()) {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            Date startDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
            Date endDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
            if (startDate.after(endDate)) {
                Date tmp = startDate;
                startDate = endDate;
                endDate = tmp;
            }
            ProcessDefinition processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                    processInstance.getProcessDefinitionVersion());
            List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinition.getId());
            complementListDate.addAll(CronUtils.getSelfFireDateList(startDate, endDate, schedules));
            logger.info(" process definition id:{} complement data: {}",
                    processDefinition.getId(), complementListDate.toString());
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
            boolean submit = taskProcessor.submit(taskInstance, processInstance, masterConfig.getMasterTaskCommitRetryTimes(), masterConfig.getMasterTaskCommitInterval());
            if (submit) {
                this.taskInstanceHashMap.put(taskInstance.getId(), taskInstance.getTaskCode(), taskInstance);
                activeTaskNode.put(taskInstance.getId(), taskProcessor);
                taskProcessor.run();
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
            if (!processWorkerGroup.equals(DEFAULT_WORKER_GROUP) && taskWorkerGroup.equals(DEFAULT_WORKER_GROUP)) {
                taskInstance.setWorkerGroup(processWorkerGroup);
            } else {
                taskInstance.setWorkerGroup(taskWorkerGroup);
            }
            // delay execution time
            taskInstance.setDelayTime(taskNode.getDelayTime());
        }

        return taskInstance;
    }

    public void getPreVarPool(TaskInstance taskInstance,  Set<String> preTask) {
        Map<String,Property> allProperty = new HashMap<>();
        Map<String,TaskInstance> allTaskInstance = new HashMap<>();
        if (CollectionUtils.isNotEmpty(preTask)) {
            for (String preTaskName : preTask) {
                TaskInstance preTaskInstance = completeTaskList.get(preTaskName);
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
                    allTaskInstance.put(proName,preTaskInstance);
                } else {
                    allProperty.put(proName, otherPro);
                }
            } else {
                allProperty.put(proName, thisProperty);
                allTaskInstance.put(proName,preTaskInstance);
            }
        } else {
            allProperty.put(proName, thisProperty);
            allTaskInstance.put(proName,preTaskInstance);
        }
    }

    private void submitPostNode(String parentNodeName) {
        Set<String> submitTaskNodeList = DagHelper.parsePostNodes(parentNodeName, skipTaskNodeList, dag, completeTaskList);
        List<TaskInstance> taskInstances = new ArrayList<>();
        for (String taskNode : submitTaskNodeList) {
            TaskNode taskNodeObject = dag.getNode(taskNode);
            if(taskInstanceHashMap.containsColumn(taskNodeObject.getCode())){
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
            if (completeTaskList.containsKey(task.getName())) {
                logger.info("task {} has already run success", task.getName());
                continue;
            }
            if (task.getState().typeIsPause() || task.getState().typeIsCancel()) {
                logger.info("task {} stopped, the state is {}", task.getName(), task.getState());
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
    private DependResult isTaskDepsComplete(String taskName) {

        Collection<String> startNodes = dag.getBeginNode();
        // if vertex,returns true directly
        if (startNodes.contains(taskName)) {
            return DependResult.SUCCESS;
        }
        TaskNode taskNode = dag.getNode(taskName);
        List<String> depNameList = taskNode.getDepList();
        for (String depsNode : depNameList) {
            if (!dag.containsNode(depsNode)
                    || forbiddenTaskList.containsKey(depsNode)
                    || skipTaskNodeList.containsKey(depsNode)) {
                continue;
            }
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
            if (!dependTaskSuccess(depsNode, taskName)) {
                return DependResult.FAILED;
            }
        }
        logger.info("taskName: {} completeDependTaskList: {}", taskName, Arrays.toString(completeTaskList.keySet().toArray()));
        return DependResult.SUCCESS;
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
                return readyToSubmitTaskQueue.size() == 0 || activeTaskNode.size() == 0;
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
     * @param instance
     */
    private ExecutionStatus getProcessInstanceState(ProcessInstance instance) {
        ExecutionStatus state = instance.getState();

        if (activeTaskNode.size() > 0 || hasRetryTaskInStandBy()) {
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
     * whether standby task list have retry tasks
     */
    private boolean retryTaskExists() {

        boolean result = false;

        for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext(); ) {
            TaskInstance task = iter.next();
            if (task.getState().typeIsFailure()) {
                result = true;
                break;
            }
        }
        return result;
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
            stateEvent.setType("process");
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
        return isTaskDepsComplete(taskInstance.getName());
    }

    /**
     * add task to standby list
     *
     * @param taskInstance task instance
     */
    private void addTaskToStandByList(TaskInstance taskInstance) {
        logger.info("add task to stand by list: {}", taskInstance.getName());
        try {
            readyToSubmitTaskQueue.put(taskInstance);
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
        logger.info("remove task from stand by list: {}", taskInstance.getName());
        try {
            readyToSubmitTaskQueue.remove(taskInstance);
        } catch (Exception e) {
            logger.error("remove task instance from readyToSubmitTaskQueue error, taskName: {}", taskInstance.getName(), e);
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
     * submit and watch the tasks, until the work flow stop
     */
    private void runProcess() {
//        // submit start node
//        submitPostNode(null);
//        boolean sendTimeWarning = false;
//        while (!processInstance.isProcessInstanceStop() && Stopper.isRunning()) {
//
//            // send warning email if process time out.
//            if (!sendTimeWarning && checkProcessTimeOut(processInstance)) {
//                processAlertManager.sendProcessTimeoutAlert(processInstance,
//                        processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
//                        processInstance.getProcessDefinitionVersion()));
//                sendTimeWarning = true;
//            }
//            for (Map.Entry<MasterBaseTaskExecThread, Future<Boolean>> entry : activeTaskNode.entrySet()) {
//                Future<Boolean> future = entry.getValue();
//                TaskInstance task = entry.getKey().getTaskInstance();
//
//                if (!future.isDone()) {
//                    continue;
//                }
//
//                // node monitor thread complete
//                task = this.processService.findTaskInstanceById(task.getId());
//
//                if (task == null) {
//                    this.taskFailedSubmit = true;
//                    activeTaskNode.remove(entry.getKey());
//                    continue;
//                }
//
//                // node monitor thread complete
//                if (task.getState().typeIsFinished()) {
//                    activeTaskNode.remove(entry.getKey());
//                }
//
//                logger.info("task :{}, id:{} complete, state is {} ",
//                        task.getName(), task.getId(), task.getState());
//                // node success , post node submit
//                if (task.getState() == ExecutionStatus.SUCCESS) {
//                    processInstance = processService.findProcessInstanceById(processInstance.getId());
//                    processInstance.setVarPool(task.getVarPool());
//                    processService.updateProcessInstance(processInstance);
//                    completeTaskList.put(task.getName(), task);
//                    submitPostNode(task.getName());
//                    continue;
//                }
//                // node fails, retry first, and then execute the failure process
//                if (task.getState().typeIsFailure()) {
//                    if (task.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
//                        this.recoverToleranceFaultTaskList.add(task);
//                    }
//                    if (task.taskCanRetry()) {
//                        addTaskToStandByList(task);
//                    } else {
//                        completeTaskList.put(task.getName(), task);
//                        if (task.isConditionsTask()
//                                || DagHelper.haveConditionsAfterNode(task.getName(), dag)) {
//                            submitPostNode(task.getName());
//                        } else {
//                            errorTaskList.put(task.getName(), task);
//                            if (processInstance.getFailureStrategy() == FailureStrategy.END) {
//                                killTheOtherTasks();
//                            }
//                        }
//                    }
//                    continue;
//                }
//                // other status stop/pause
//                completeTaskList.put(task.getName(), task);
//            }
//            // send alert
//            if (CollectionUtils.isNotEmpty(this.recoverToleranceFaultTaskList)) {
//                processAlertManager.sendAlertWorkerToleranceFault(processInstance, recoverToleranceFaultTaskList);
//                this.recoverToleranceFaultTaskList.clear();
//            }
//            // updateProcessInstance completed task status
//            // failure priority is higher than pause
//            // if a task fails, other suspended tasks need to be reset kill
//            // check if there exists forced success nodes in errorTaskList
//            if (errorTaskList.size() > 0) {
//                for (Map.Entry<String, TaskInstance> entry : completeTaskList.entrySet()) {
//                    TaskInstance completeTask = entry.getValue();
//                    if (completeTask.getState() == ExecutionStatus.PAUSE) {
//                        completeTask.setState(ExecutionStatus.KILL);
//                        completeTaskList.put(entry.getKey(), completeTask);
//                        processService.updateTaskInstance(completeTask);
//                    }
//                }
//                for (Map.Entry<String, TaskInstance> entry : errorTaskList.entrySet()) {
//                    TaskInstance errorTask = entry.getValue();
//                    TaskInstance currentTask = processService.findTaskInstanceById(errorTask.getId());
//                    if (currentTask == null) {
//                        continue;
//                    }
//                    // for nodes that have been forced success
//                    if (errorTask.getState().typeIsFailure() && currentTask.getState().equals(ExecutionStatus.FORCED_SUCCESS)) {
//                        // update state in this thread and remove from errorTaskList
//                        errorTask.setState(currentTask.getState());
//                        logger.info("task: {} has been forced success, remove it from error task list", errorTask.getName());
//                        errorTaskList.remove(errorTask.getName());
//                        // submit post nodes
//                        submitPostNode(errorTask.getName());
//                    }
//                }
//            }
//            if (canSubmitTaskToQueue()) {
//                submitStandByTask();
//            }
//            try {
//                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage(), e);
//                Thread.currentThread().interrupt();
//            }
//            updateProcessInstanceState();
//        }
//
//        logger.info("process:{} end, state :{}", processInstance.getId(), processInstance.getState());
    }

    /**
     * whether check process time out
     *
     * @param processInstance task instance
     * @return true if time out of process instance > running time of process instance
     */
    private boolean checkProcessTimeOut(ProcessInstance processInstance) {
        if (processInstance.getTimeout() == 0) {
            return false;
        }

        Date now = new Date();
        long runningTime = DateUtils.diffMin(now, processInstance.getStartTime());

        return runningTime > processInstance.getTimeout();
    }

    /**
     * whether can submit task to queue
     *
     * @return boolean
     */
    private boolean canSubmitTaskToQueue() {
        return OSUtils.checkResource(masterConfig.getMasterMaxCpuloadAvg(), masterConfig.getMasterReservedMemory());
    }

    /**
     * close the on going tasks
     */
    private void killTheOtherTasks() {
        logger.info("kill called on process instance id: {}, num: {}", processInstance.getId(),
                activeTaskNode.size());
        for(int taskId : activeTaskNode.keySet()) {
            TaskInstance taskInstance = processService.findTaskInstanceById(taskId);
            if (taskInstance == null || taskInstance.getState().typeIsFinished()) {
                continue;
            }

            ITaskProcessor taskProcessor = activeTaskNode.get(taskId);
            taskProcessor.action(TaskAction.STOP);
        }

    }

    public boolean workFlowFinish() {
        return this.processInstance.getState().typeIsFinished();
    }

    /**
     * whether the retry interval is timed out
     *
     * @param taskInstance task instance
     * @return Boolean
     */
    private boolean retryTaskIntervalOverTime(TaskInstance taskInstance) {
        if (taskInstance.getState() != ExecutionStatus.FAILURE) {
            return true;
        }
        if (taskInstance.getId() == 0
                ||
                taskInstance.getMaxRetryTimes() == 0
                ||
                taskInstance.getRetryInterval() == 0) {
            return true;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, taskInstance.getEndTime());
        // task retry does not over time, return false
        return taskInstance.getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
    }

    /**
     * handling the list of tasks to be submitted
     */
    private void submitStandByTask() {
        try {
            int length = readyToSubmitTaskQueue.size();
            for (int i = 0; i < length; i++) {
                TaskInstance task = readyToSubmitTaskQueue.peek();
                if(task == null){
                    continue;
                }
                // stop tasks which is retrying if forced success happens
                if (task.taskCanRetry()) {
                    TaskInstance retryTask = processService.findTaskInstanceById(task.getId());
                    if (retryTask != null && retryTask.getState().equals(ExecutionStatus.FORCED_SUCCESS)) {
                        task.setState(retryTask.getState());
                        logger.info("task: {} has been forced success, put it into complete task list and stop retrying", task.getName());
                        removeTaskFromStandbyList(task);
                        completeTaskList.put(task.getName(), task);
                        submitPostNode(task.getName());
                        continue;
                    }
                }
                //init varPool only this task is the first time running
                if (task.isFirstRun()) {
                    //get pre task ,get all the task varPool to this task
                    Set<String> preTask = dag.getPreviousNodes(task.getName());
                    getPreVarPool(task, preTask);
                }
                DependResult dependResult = getDependResultForTask(task);
                if (DependResult.SUCCESS == dependResult) {
                    if (retryTaskIntervalOverTime(task)) {
                        TaskInstance taskInstance = submitTaskExec(task);
                        if(taskInstance == null){
                            this.taskFailedSubmit = true;
                        }else{
                            removeTaskFromStandbyList(task);
                        }
                    }
                } else if (DependResult.FAILED == dependResult) {
                    // if the dependency fails, the current node is not submitted and the state changes to failure.
                    dependFailedTask.put(task.getName(), task);
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
        if (paramMap.containsKey(CMD_PARAM_START_NODE_NAMES)) {
            startNodeNameList = Arrays.asList(paramMap.get(CMD_PARAM_START_NODE_NAMES).split(Constants.COMMA));
        }
        return startNodeNameList;
    }

    /**
     * generate start node name list from parsing command param;
     * if "StartNodeIdList" exists in command param, return StartNodeIdList
     *
     * @return recovery node name list
     */
    private List<String> getRecoveryNodeNameList() {
        List<String> recoveryNodeNameList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(recoverNodeIdList)) {
            for (TaskInstance task : recoverNodeIdList) {
                recoveryNodeNameList.add(task.getName());
            }
        }
        return recoveryNodeNameList;
    }

    /**
     * generate flow dag
     *
     * @param totalTaskNodeList total task node list
     * @param startNodeNameList start node name list
     * @param recoveryNodeNameList recovery node name list
     * @param depNodeType depend node type
     * @return ProcessDag           process dag
     * @throws Exception exception
     */
    public ProcessDag generateFlowDag(List<TaskNode> totalTaskNodeList,
                                      List<String> startNodeNameList,
                                      List<String> recoveryNodeNameList,
                                      TaskDependType depNodeType) throws Exception {
        return DagHelper.generateFlowDag(totalTaskNodeList, startNodeNameList, recoveryNodeNameList, depNodeType);
    }
}
