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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.utils.AlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * master exec thread,split dag
 */
public class MasterExecThread implements Runnable {
    /**
     * logger of MasterExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterExecThread.class);

    /**
     * process instance
     */
    private ProcessInstance processInstance;

    /**
     *  runing TaskNode
     */
    private final Map<MasterBaseTaskExecThread,Future<Boolean>> activeTaskNode = new ConcurrentHashMap<>();

    /**
     * task exec service
     */
    private final ExecutorService taskExecService;

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
    private Map<String,TaskInstance> errorTaskList = new ConcurrentHashMap<>();

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
    private AlertManager alertManager = new AlertManager();

    /**
     * the object of DAG
     */
    private DAG<String,TaskNode,TaskNodeRelation> dag;

    /**
     *  process service
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
     * constructor of MasterExecThread
     * @param processInstance processInstance
     * @param processService processService
     * @param nettyRemotingClient nettyRemotingClient
     */
    public MasterExecThread(ProcessInstance processInstance, ProcessService processService, NettyRemotingClient nettyRemotingClient){
        this.processService = processService;

        this.processInstance = processInstance;
        this.masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        int masterTaskExecNum = masterConfig.getMasterExecTaskNum();
        this.taskExecService = ThreadUtils.newDaemonFixedThreadExecutor("Master-Task-Exec-Thread",
                masterTaskExecNum);
        this.nettyRemotingClient = nettyRemotingClient;
    }

    @Override
    public void run() {

        // process instance is null
        if (processInstance == null){
            logger.info("process instance is not exists");
            return;
        }
        // check to see if it's done
        if (processInstance.getState().typeIsFinished()){
            logger.info("process instance is done : {}",processInstance.getId());
            return;
        }
        try {
            if (processInstance.isComplementData() &&  Flag.NO == processInstance.getIsSubProcess()){
                // sub process complement data
                executeComplementProcess();
            }else{
                // execute flow
                executeProcess();
            }
        }catch (Exception e){
            logger.error("master exec thread exception", e);
            logger.error("process execute failed, process id:{}", processInstance.getId());
            processInstance.setState(ExecutionStatus.FAILURE);
            processInstance.setEndTime(new Date());
            processService.updateProcessInstance(processInstance);
        }finally {
            taskExecService.shutdown();
        }
    }

    /**
     * execute process
     * @throws Exception exception
     */
    private void executeProcess() throws Exception {
        prepareProcess();
        runProcess();
        endProcess();
    }

    /**
     * execute complement process
     * @throws Exception exception
     */
    private void executeComplementProcess() throws Exception {

        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());

        Date startDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
        Date endDate = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
        processService.saveProcessInstance(processInstance);

        // get schedules
        int processDefinitionId = processInstance.getProcessDefinitionId();
        List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId);
        List<Date> listDate = Lists.newLinkedList();
        if(!CollectionUtils.isEmpty(schedules)){
            for (Schedule schedule : schedules) {
                listDate.addAll(CronUtils.getSelfFireDateList(startDate, endDate, schedule.getCrontab()));
            }
        }
        // get first fire date
        Iterator<Date> iterator = null;
        Date scheduleDate = null;
        if(!CollectionUtils.isEmpty(listDate)) {
            iterator = listDate.iterator();
            scheduleDate = iterator.next();
            processInstance.setScheduleTime(scheduleDate);
            processService.updateProcessInstance(processInstance);
        }else{
            scheduleDate = processInstance.getScheduleTime();
            if(scheduleDate == null){
                scheduleDate = startDate;
            }
        }

        while(Stopper.isRunning()){

            logger.info("process {} start to complement {} data",
                    processInstance.getId(), DateUtils.dateToString(scheduleDate));
            // prepare dag and other info
            prepareProcess();

            if(dag == null){
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
            if(!processInstance.getState().typeIsSuccess()){
                logger.info("process {} state {}, complement not completely!",
                        processInstance.getId(), processInstance.getState());
                break;
            }
            //  current process instance success ,next execute
            if(null == iterator){
                // loop by day
                scheduleDate = DateUtils.getSomeDay(scheduleDate, 1);
                if(scheduleDate.after(endDate)){
                    // all success
                    logger.info("process {} complement completely!", processInstance.getId());
                    break;
                }
            }else{
                // loop by schedule date
                if(!iterator.hasNext()){
                    // all success
                    logger.info("process {} complement completely!", processInstance.getId());
                    break;
                }
                scheduleDate = iterator.next();
            }
            // flow end
            // execute next process instance complement data
            processInstance.setScheduleTime(scheduleDate);
            if(cmdParam.containsKey(Constants.CMDPARAM_RECOVERY_START_NODE_STRING)){
                cmdParam.remove(Constants.CMDPARAM_RECOVERY_START_NODE_STRING);
                processInstance.setCommandParam(JSONUtils.toJson(cmdParam));
            }

            processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
            processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                    processInstance.getProcessDefinition().getGlobalParamMap(),
                    processInstance.getProcessDefinition().getGlobalParamList(),
                    CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));
            processInstance.setId(0);
            processInstance.setStartTime(new Date());
            processInstance.setEndTime(null);
            processService.saveProcessInstance(processInstance);
        }
    }


    /**
     * prepare process parameter
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
        processInstance.setEndTime(new Date());
        processService.updateProcessInstance(processInstance);
        if(processInstance.getState().typeIsWaitingThread()){
            processService.createRecoveryWaitingThreadCommand(null, processInstance);
        }
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(processInstance.getId());
        alertManager.sendAlertProcessInstance(processInstance, taskInstances);
    }


    /**
     *  generate process dag
     * @throws Exception exception
     */
    private void buildFlowDag() throws Exception {
        recoverNodeIdList = getStartTaskInstanceList(processInstance.getCommandParam());

        forbiddenTaskList = DagHelper.getForbiddenTaskNodeMaps(processInstance.getProcessInstanceJson());
        // generate process to get DAG info
        List<String> recoveryNameList = getRecoveryNodeNameList();
        List<String> startNodeNameList = parseStartNodeName(processInstance.getCommandParam());
        ProcessDag processDag = generateFlowDag(processInstance.getProcessInstanceJson(),
                startNodeNameList, recoveryNameList, processInstance.getTaskDependType());
        if(processDag == null){
            logger.error("processDag is null");
            return;
        }
        // generate process dag
        dag = DagHelper.buildDagGraph(processDag);
    }

    /**
     * init task queue
     */
    private void initTaskQueue(){

        taskFailedSubmit = false;
        activeTaskNode.clear();
        dependFailedTask.clear();
        completeTaskList.clear();
        errorTaskList.clear();
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
        for(TaskInstance task : taskInstanceList){
            if(task.isTaskComplete()){
                completeTaskList.put(task.getName(), task);
            }
            if(task.isConditionsTask() || DagHelper.haveConditionsAfterNode(task.getName(), dag)){
                continue;
            }
            if(task.getState().typeIsFailure() && !task.taskCanRetry()){
                errorTaskList.put(task.getName(), task);
            }
        }
    }

    /**
     * submit task to execute
     * @param taskInstance task instance
     * @return TaskInstance
     */
    private TaskInstance submitTaskExec(TaskInstance taskInstance) {
        MasterBaseTaskExecThread abstractExecThread = null;
        if(taskInstance.isSubProcess()){
            abstractExecThread = new SubProcessTaskExecThread(taskInstance);
        }else if(taskInstance.isDependTask()){
            abstractExecThread = new DependentTaskExecThread(taskInstance);
        }else if(taskInstance.isConditionsTask()){
            abstractExecThread = new ConditionsTaskExecThread(taskInstance);
        }else {
            abstractExecThread = new MasterTaskExecThread(taskInstance);
        }
        Future<Boolean> future = taskExecService.submit(abstractExecThread);
        activeTaskNode.putIfAbsent(abstractExecThread, future);
        return abstractExecThread.getTaskInstance();
    }

    /**
     * find task instance in db.
     * in case submit more than one same name task in the same time.
     * @param taskName task name
     * @return TaskInstance
     */
    private TaskInstance findTaskIfExists(String taskName){
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(this.processInstance.getId());
        for(TaskInstance taskInstance : taskInstanceList){
            if(taskInstance.getName().equals(taskName)){
                return taskInstance;
            }
        }
        return null;
    }

    /**
     * encapsulation task
     * @param processInstance   process instance
     * @param nodeName          node name
     * @return TaskInstance
     */
    private TaskInstance createTaskInstance(ProcessInstance processInstance, String nodeName,
                                            TaskNode taskNode) {

        TaskInstance taskInstance = findTaskIfExists(nodeName);
        if(taskInstance == null){
            taskInstance = new TaskInstance();
            // task name
            taskInstance.setName(nodeName);
            // process instance define id
            taskInstance.setProcessDefinitionId(processInstance.getProcessDefinitionId());
            // task instance state
            taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
            // process instance id
            taskInstance.setProcessInstanceId(processInstance.getId());
            // task instance node json
            taskInstance.setTaskJson(JSON.toJSONString(taskNode));
            // task instance type
            taskInstance.setTaskType(taskNode.getType());
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

            // task instance priority
            if(taskNode.getTaskInstancePriority() == null){
                taskInstance.setTaskInstancePriority(Priority.MEDIUM);
            }else{
                taskInstance.setTaskInstancePriority(taskNode.getTaskInstancePriority());
            }

            String processWorkerGroup = processInstance.getWorkerGroup();
            processWorkerGroup = StringUtils.isBlank(processWorkerGroup) ? DEFAULT_WORKER_GROUP : processWorkerGroup;
            String taskWorkerGroup = StringUtils.isBlank(taskNode.getWorkerGroup()) ? processWorkerGroup : taskNode.getWorkerGroup();
            if (!processWorkerGroup.equals(DEFAULT_WORKER_GROUP) && taskWorkerGroup.equals(DEFAULT_WORKER_GROUP)) {
                taskInstance.setWorkerGroup(processWorkerGroup);
            }else {
                taskInstance.setWorkerGroup(taskWorkerGroup);
            }

        }
        return taskInstance;
    }

    /**
     * submit post node
     * @param parentNodeName parent node name
     */
    private void submitPostNode(String parentNodeName){
        Set<String> submitTaskNodeList = DagHelper.parsePostNodes(parentNodeName, skipTaskNodeList, dag, completeTaskList);
        List<TaskInstance> taskInstances = new ArrayList<>();
        for(String taskNode : submitTaskNodeList){
            taskInstances.add(createTaskInstance(processInstance, taskNode,
                    dag.getNode(taskNode)));
        }
        // if previous node success , post node submit
        for(TaskInstance task : taskInstances){
            if(readyToSubmitTaskQueue.contains(task)){
                continue;
            }
            if(completeTaskList.containsKey(task.getName())){
                logger.info("task {} has already run success", task.getName());
                continue;
            }
            if(task.getState().typeIsPause() || task.getState().typeIsCancel()){
                logger.info("task {} stopped, the state is {}", task.getName(), task.getState());
            }else{
                addTaskToStandByList(task);
            }
        }
    }

    /**
     * determine whether the dependencies of the task node are complete
     * @return DependResult
     */
    private DependResult isTaskDepsComplete(String taskName) {

        Collection<String> startNodes = dag.getBeginNode();
        // if vertex,returns true directly
        if(startNodes.contains(taskName)){
            return DependResult.SUCCESS;
        }
        TaskNode taskNode = dag.getNode(taskName);
        List<String> depNameList = taskNode.getDepList();
        for(String depsNode : depNameList ){
            if(!dag.containsNode(depsNode)
                    || skipTaskNodeList.containsKey(depsNode)
                    || forbiddenTaskList.containsKey(depsNode)){
                continue;
            }
            // all the dependencies must be completed
            if(!completeTaskList.containsKey(depsNode)){
                return DependResult.WAITING;
            }
            ExecutionStatus depTaskState = completeTaskList.get(depsNode).getState();
            if(depTaskState.typeIsPause() || depTaskState.typeIsCancel()){
                return DependResult.WAITING;
            }
            // ignore task state if current task is condition
            if(taskNode.isConditionsTask()){
                continue;
            }
            if(!dependTaskSuccess(depsNode, taskName)){
                return DependResult.FAILED;
            }
        }
        logger.info("taskName: {} completeDependTaskList: {}", taskName, Arrays.toString(completeTaskList.keySet().toArray()));
        return DependResult.SUCCESS;
    }

    /**
     * depend node is completed, but here need check the condition task branch is the next node
     * @param dependNodeName
     * @param nextNodeName
     * @return
     */
    private boolean dependTaskSuccess(String dependNodeName, String nextNodeName){
        if(dag.getNode(dependNodeName).isConditionsTask()){
            //condition task need check the branch to run
            List<String> nextTaskList = DagHelper.parseConditionTask(dependNodeName, skipTaskNodeList, dag, completeTaskList);
            if(!nextTaskList.contains(nextNodeName)){
                return false;
            }
        }else {
            ExecutionStatus depTaskState = completeTaskList.get(dependNodeName).getState();
            if(depTaskState.typeIsFailure()){
                return false;
            }
        }
        return true;
    }

    /**
     * query task instance by complete state
     * @param state state
     * @return task instance list
     */
    private List<TaskInstance> getCompleteTaskByState(ExecutionStatus state){
        List<TaskInstance> resultList = new ArrayList<>();
        for (Map.Entry<String, TaskInstance> entry: completeTaskList.entrySet()) {
            if(entry.getValue().getState() == state){
                resultList.add(entry.getValue());
            }
        }
        return resultList;
    }

    /**
     *  where there are ongoing tasks
     * @param state state
     * @return ExecutionStatus
     */
    private ExecutionStatus runningState(ExecutionStatus state){
        if(state == ExecutionStatus.READY_STOP ||
                state == ExecutionStatus.READY_PAUSE ||
                state == ExecutionStatus.WAITTING_THREAD){
            // if the running task is not completed, the state remains unchanged
            return state;
        }else{
            return ExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * exists failure task,contains submit failure、dependency failure,execute failure(retry after)
     *
     * @return Boolean whether has failed task
     */
    private boolean hasFailedTask(){

        if(this.taskFailedSubmit){
            return true;
        }
        if(this.errorTaskList.size() > 0){
            return true;
        }
        return this.dependFailedTask.size() > 0;
    }

    /**
     * process instance failure
     *
     * @return Boolean whether process instance failed
     */
    private boolean processFailed(){
        if(hasFailedTask()) {
            if(processInstance.getFailureStrategy() == FailureStrategy.END){
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
     * @return Boolean whether has waiting thread task
     */
    private boolean hasWaitingThreadTask(){
        List<TaskInstance> waitingList = getCompleteTaskByState(ExecutionStatus.WAITTING_THREAD);
        return CollectionUtils.isNotEmpty(waitingList);
    }

    /**
     * prepare for pause
     * 1，failed retry task in the preparation queue , returns to failure directly
     * 2，exists pause task，complement not completed, pending submission of tasks, return to suspension
     * 3，success
     * @return ExecutionStatus
     */
    private ExecutionStatus processReadyPause(){
        if(hasRetryTaskInStandBy()){
            return ExecutionStatus.FAILURE;
        }

        List<TaskInstance> pauseList = getCompleteTaskByState(ExecutionStatus.PAUSE);
        if(CollectionUtils.isNotEmpty(pauseList)
                || !isComplementEnd()
                || readyToSubmitTaskQueue.size() > 0){
            return ExecutionStatus.PAUSE;
        }else{
            return ExecutionStatus.SUCCESS;
        }
    }


    /**
     * generate the latest process instance status by the tasks state
     * @return process instance execution status
     */
    private ExecutionStatus getProcessInstanceState(){
        ProcessInstance instance = processService.findProcessInstanceById(processInstance.getId());
        ExecutionStatus state = instance.getState();

        if(activeTaskNode.size() > 0 || retryTaskExists()){
            // active task and retry task exists
            return runningState(state);
        }
        // process failure
        if(processFailed()){
            return ExecutionStatus.FAILURE;
        }

        // waiting thread
        if(hasWaitingThreadTask()){
            return ExecutionStatus.WAITTING_THREAD;
        }

        // pause
        if(state == ExecutionStatus.READY_PAUSE){
            return processReadyPause();
        }

        // stop
        if(state == ExecutionStatus.READY_STOP){
            List<TaskInstance> stopList = getCompleteTaskByState(ExecutionStatus.STOP);
            List<TaskInstance> killList = getCompleteTaskByState(ExecutionStatus.KILL);
            if(CollectionUtils.isNotEmpty(stopList)
                    || CollectionUtils.isNotEmpty(killList)
                    || !isComplementEnd()){
                return ExecutionStatus.STOP;
            }else{
                return ExecutionStatus.SUCCESS;
            }
        }

        // success
        if(state == ExecutionStatus.RUNNING_EXECUTION){
            List<TaskInstance> killTasks = getCompleteTaskByState(ExecutionStatus.KILL);
            if(readyToSubmitTaskQueue.size() > 0){
                //tasks currently pending submission, no retries, indicating that depend is waiting to complete
                return ExecutionStatus.RUNNING_EXECUTION;
            }else if(CollectionUtils.isNotEmpty(killTasks)){
                // tasks maybe killed manually
                return ExecutionStatus.FAILURE;
            }else{
                //  if the waiting queue is empty and the status is in progress, then success
                return ExecutionStatus.SUCCESS;
            }
        }

        return state;
    }

    /**
     * whether standby task list have retry tasks
     * @return
     */
    private boolean retryTaskExists() {

        boolean result = false;

        for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext();) {
            TaskInstance task =  iter.next();
            if(task.getState().typeIsFailure()){
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * whether complement end
     * @return Boolean whether is complement end
     */
    private boolean isComplementEnd() {
        if(!processInstance.isComplementData()){
            return true;
        }

        try {
            Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            Date endTime = DateUtils.getScheduleDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
            return processInstance.getScheduleTime().equals(endTime);
        } catch (Exception e) {
            logger.error("complement end failed ",e);
            return false;
        }
    }

    /**
     * updateProcessInstance process instance state
     * after each batch of tasks is executed, the status of the process instance is updated
     */
    private void updateProcessInstanceState() {
        ExecutionStatus state = getProcessInstanceState();
        if(processInstance.getState() != state){
            logger.info(
                    "work flow process instance [id: {}, name:{}], state change from {} to {}, cmd type: {}",
                    processInstance.getId(), processInstance.getName(),
                    processInstance.getState(), state,
                    processInstance.getCommandType());

            ProcessInstance instance = processService.findProcessInstanceById(processInstance.getId());
            instance.setState(state);
            instance.setProcessDefinition(processInstance.getProcessDefinition());
            processService.updateProcessInstance(instance);
            processInstance = instance;
        }
    }

    /**
     * get task dependency result
     * @param taskInstance task instance
     * @return DependResult
     */
    private DependResult getDependResultForTask(TaskInstance taskInstance){
        return isTaskDepsComplete(taskInstance.getName());
    }

    /**
     * add task to standby list
     * @param taskInstance task instance
     */
    private void addTaskToStandByList(TaskInstance taskInstance){
        logger.info("add task to stand by list: {}", taskInstance.getName());
        try {
            readyToSubmitTaskQueue.put(taskInstance);
        } catch (Exception e) {
            logger.error("add task instance to readyToSubmitTaskQueue error");
        }
    }

    /**
     * remove task from stand by list
     * @param taskInstance task instance
     */
    private void removeTaskFromStandbyList(TaskInstance taskInstance){
        logger.info("remove task from stand by list: {}", taskInstance.getName());
        try {
            readyToSubmitTaskQueue.remove(taskInstance);
        } catch (Exception e) {
            logger.error("remove task instance from readyToSubmitTaskQueue error");
        }
    }

    /**
     * has retry task in standby
     * @return Boolean whether has retry task in standby
     */
    private boolean hasRetryTaskInStandBy(){
        for (Iterator<TaskInstance> iter = readyToSubmitTaskQueue.iterator(); iter.hasNext();) {
            if(iter.next().getState().typeIsFailure()){
                return true;
            }

        }
        return false;
    }

    /**
     * submit and watch the tasks, until the work flow stop
     */
    private void runProcess(){
        // submit start node
        submitPostNode(null);
        boolean sendTimeWarning = false;
        while(!processInstance.isProcessInstanceStop() && Stopper.isRunning()){

            // send warning email if process time out.
            if(!sendTimeWarning && checkProcessTimeOut(processInstance) ){
                alertManager.sendProcessTimeoutAlert(processInstance,
                        processService.findProcessDefineById(processInstance.getProcessDefinitionId()));
                sendTimeWarning = true;
            }
            for(Map.Entry<MasterBaseTaskExecThread,Future<Boolean>> entry: activeTaskNode.entrySet()) {
                Future<Boolean> future = entry.getValue();
                TaskInstance task  = entry.getKey().getTaskInstance();

                if(!future.isDone()){
                    continue;
                }

                // node monitor thread complete
                task = this.processService.findTaskInstanceById(task.getId());

                if(task == null){
                    this.taskFailedSubmit = true;
                    activeTaskNode.remove(entry.getKey());
                    continue;
                }

                // node monitor thread complete
                if(task.getState().typeIsFinished()){
                    activeTaskNode.remove(entry.getKey());
                }

                logger.info("task :{}, id:{} complete, state is {} ",
                        task.getName(), task.getId(), task.getState());
                // node success , post node submit
                if(task.getState() == ExecutionStatus.SUCCESS){
                    completeTaskList.put(task.getName(), task);
                    submitPostNode(task.getName());
                    continue;
                }
                // node fails, retry first, and then execute the failure process
                if(task.getState().typeIsFailure()){
                    if(task.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE){
                        this.recoverToleranceFaultTaskList.add(task);
                    }
                    if(task.taskCanRetry()){
                        addTaskToStandByList(task);
                    }else{
                        completeTaskList.put(task.getName(), task);
                        if( task.isConditionsTask()
                            || DagHelper.haveConditionsAfterNode(task.getName(), dag)) {
                            submitPostNode(task.getName());
                        }else{
                            errorTaskList.put(task.getName(), task);
                            if(processInstance.getFailureStrategy() == FailureStrategy.END){
                                killTheOtherTasks();
                            }
                        }
                    }
                    continue;
                }
                // other status stop/pause
                completeTaskList.put(task.getName(), task);
            }
            // send alert
            if(CollectionUtils.isNotEmpty(this.recoverToleranceFaultTaskList)){
                alertManager.sendAlertWorkerToleranceFault(processInstance, recoverToleranceFaultTaskList);
                this.recoverToleranceFaultTaskList.clear();
            }
            // updateProcessInstance completed task status
            // failure priority is higher than pause
            // if a task fails, other suspended tasks need to be reset kill
            if(errorTaskList.size() > 0){
                for(Map.Entry<String, TaskInstance> entry: completeTaskList.entrySet()) {
                    TaskInstance completeTask = entry.getValue();
                    if(completeTask.getState()== ExecutionStatus.PAUSE){
                        completeTask.setState(ExecutionStatus.KILL);
                        completeTaskList.put(entry.getKey(), completeTask);
                        processService.updateTaskInstance(completeTask);
                    }
                }
            }
            if(canSubmitTaskToQueue()){
                submitStandByTask();
            }
            try {
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
            updateProcessInstanceState();
        }

        logger.info("process:{} end, state :{}", processInstance.getId(), processInstance.getState());
    }

    /**
     * whether check process time out
     * @param processInstance task instance
     * @return true if time out of process instance > running time of process instance
     */
    private boolean checkProcessTimeOut(ProcessInstance processInstance) {
        if(processInstance.getTimeout() == 0 ){
            return false;
        }

        Date now = new Date();
        long runningTime =  DateUtils.diffMin(now, processInstance.getStartTime());

        return runningTime > processInstance.getTimeout();
    }

    /**
     * whether can submit task to queue
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
        for (Map.Entry<MasterBaseTaskExecThread, Future<Boolean>> entry : activeTaskNode.entrySet()) {
            MasterBaseTaskExecThread taskExecThread = entry.getKey();
            Future<Boolean> future = entry.getValue();

            TaskInstance taskInstance = taskExecThread.getTaskInstance();
            taskInstance = processService.findTaskInstanceById(taskInstance.getId());
            if(taskInstance != null && taskInstance.getState().typeIsFinished()){
                continue;
            }

            if (!future.isDone()) {
                // record kill info
                logger.info("kill process instance, id: {}, task: {}", processInstance.getId(), taskExecThread.getTaskInstance().getId());

                //  kill node
                taskExecThread.kill();
            }
        }
    }

    /**
     * whether the retry interval is timed out
     * @param taskInstance task instance
     * @return Boolean
     */
    private boolean retryTaskIntervalOverTime(TaskInstance taskInstance){
        if(taskInstance.getState() != ExecutionStatus.FAILURE){
            return true;
        }
        if(taskInstance.getId() == 0 ||
                taskInstance.getMaxRetryTimes() ==0 ||
                taskInstance.getRetryInterval() == 0 ){
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
    private void submitStandByTask(){

        try {
            int length = readyToSubmitTaskQueue.size();
            for (int i=0;i<length;i++) {
                TaskInstance task = readyToSubmitTaskQueue.peek();
                DependResult dependResult = getDependResultForTask(task);
                if(DependResult.SUCCESS == dependResult){
                    if(retryTaskIntervalOverTime(task)){
                        submitTaskExec(task);
                        removeTaskFromStandbyList(task);
                    }
                }else if(DependResult.FAILED == dependResult){
                    // if the dependency fails, the current node is not submitted and the state changes to failure.
                    dependFailedTask.put(task.getName(), task);
                    removeTaskFromStandbyList(task);
                    logger.info("task {},id:{} depend result : {}",task.getName(), task.getId(), dependResult);
                }
            }
        } catch (Exception e) {
            logger.error("submit standby task error",e);
        }
    }

    /**
     * get recovery task instance
     * @param taskId task id
     * @return recovery task instance
     */
    private TaskInstance getRecoveryTaskInstance(String taskId){
        if(!StringUtils.isNotEmpty(taskId)){
            return null;
        }
        try {
            Integer intId = Integer.valueOf(taskId);
            TaskInstance task = processService.findTaskInstanceById(intId);
            if(task == null){
                logger.error("start node id cannot be found: {}",  taskId);
            }else {
                return task;
            }
        }catch (Exception e){
            logger.error("get recovery task instance failed ",e);
        }
        return null;
    }

    /**
     * get start task instance list
     * @param cmdParam command param
     * @return task instance list
     */
    private List<TaskInstance> getStartTaskInstanceList(String cmdParam){

        List<TaskInstance> instanceList = new ArrayList<>();
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);

        if(paramMap != null && paramMap.containsKey(CMDPARAM_RECOVERY_START_NODE_STRING)){
            String[] idList = paramMap.get(CMDPARAM_RECOVERY_START_NODE_STRING).split(Constants.COMMA);
            for(String nodeId : idList){
                TaskInstance task = getRecoveryTaskInstance(nodeId);
                if(task != null){
                    instanceList.add(task);
                }
            }
        }
        return instanceList;
    }

    /**
     * parse "StartNodeNameList" from cmd param
     * @param cmdParam command param
     * @return start node name list
     */
    private List<String> parseStartNodeName(String cmdParam){
        List<String> startNodeNameList = new ArrayList<>();
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        if(paramMap == null){
            return startNodeNameList;
        }
        if(paramMap.containsKey(CMDPARAM_START_NODE_NAMES)){
            startNodeNameList = Arrays.asList(paramMap.get(CMDPARAM_START_NODE_NAMES).split(Constants.COMMA));
        }
        return startNodeNameList;
    }

    /**
     * generate start node name list from parsing command param;
     * if "StartNodeIdList" exists in command param, return StartNodeIdList
     * @return recovery node name list
     */
    private List<String> getRecoveryNodeNameList(){
        List<String> recoveryNodeNameList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(recoverNodeIdList)) {
            for (TaskInstance task : recoverNodeIdList) {
                recoveryNodeNameList.add(task.getName());
            }
        }
        return recoveryNodeNameList;
    }

    /**
     * generate flow dag
     * @param processDefinitionJson process definition json
     * @param startNodeNameList     start node name list
     * @param recoveryNodeNameList  recovery node name list
     * @param depNodeType           depend node type
     * @return ProcessDag           process dag
     * @throws Exception            exception
     */
    public ProcessDag generateFlowDag(String processDefinitionJson,
                                      List<String> startNodeNameList,
                                      List<String> recoveryNodeNameList,
                                      TaskDependType depNodeType)throws Exception{
        return DagHelper.generateFlowDag(processDefinitionJson, startNodeNameList, recoveryNodeNameList, depNodeType);
    }
}
