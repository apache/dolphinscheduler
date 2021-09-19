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
package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * blocking task processor
 */
public class BlockingTaskProcessor extends BaseTaskProcessor{
    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    private ProcessInstance processInstance;

    /**
     * condition result
     */
    private DependResult conditionResult = DependResult.WAITING;

    /**
     * complete task map
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);
    MasterConfig masterConfig = SpringApplicationContext.getBean(MasterConfig.class);


    private boolean isBlocked;

    /**
     * blocking condition result
     */
    private String blockingConditionResult;

    @Override
    public boolean submit(TaskInstance task, ProcessInstance processInstance, int masterTaskCommitRetryTimes, int masterTaskCommitInterval) {
        this.processInstance = processInstance;
        this.taskInstance = processService.submitTask(task, masterTaskCommitRetryTimes, masterTaskCommitInterval);

        if (this.taskInstance == null) {
            return false;
        }

        logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
        Thread.currentThread().setName(threadLoggerInfoName);
        initTaskParameters();
        logger.info("blocking task start");
        return true;
    }

    @Override
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(ExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean killTask() {
        this.taskInstance.setState(ExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        return false;
    }

    @Override
    public void run() {
        if (conditionResult.equals(DependResult.WAITING)) {
            setConditionResult();
        }
        endTask();
    }

    /**
     * init task running parameters
     */
    private void initTaskParameters(){
        this.taskInstance.setLogPath(LogUtils.getTaskLogPath(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        this.taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        this.taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        this.taskInstance.setStartTime(new Date());
        this.processService.saveTaskInstance(taskInstance);
        this.dependentParameters = taskInstance.getDependency();
        this.blockingConditionResult = taskInstance.getBlockingCondition();
    }

    /**
     *
     * @param item the dependent item containing depTasks and status
     * @return depend result for depend item. SUCCESS or FAILED
     */
    private DependResult getDependentResultForItem(DependentItem item){
        DependResult dependResult = DependResult.SUCCESS;
        if(!completeTaskList.containsKey(item.getDepTasks())){
            logger.info("depend item: {} have not completed yet.", item.getDepTasks());
            return dependResult;
        }
        // the actual status of task
        ExecutionStatus executionStatus = completeTaskList.get(item.getDepTasks());
        if(executionStatus != item.getStatus()){
            logger.info("depend item: {} expect status: {}, actual status: {}",item.getDepTasks(),
                    item.getStatus(),executionStatus);
            dependResult = DependResult.FAILED;
        }
        logger.info("dependent item complete {} {}, {}",
                Constants.DEPENDENT_SPLIT,item.getStatus(),executionStatus);
        return dependResult;
    }

    private void setConditionResult() {

        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(taskInstance.getProcessInstanceId());
        for (TaskInstance task : taskInstances) {
            completeTaskList.putIfAbsent(task.getName(), task.getState());
        }

        List<DependResult> modelResultList = new ArrayList<>();
        for (DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()) {
            List<DependResult> itemDependResult = new ArrayList<>();
            for (DependentItem item : dependentTaskModel.getDependItemList()) {
                itemDependResult.add(getDependentResultForItem(item));
            }
            DependResult modelResult = DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            modelResultList.add(modelResult);
        }
        conditionResult = DependentUtils.getDependResultForRelation(dependentParameters.getRelation(), modelResultList);
        logger.info("the conditions task depend result : {}", conditionResult);
    }

    /**
     * set task execution status and then update db
     * for blocking task, determine blocking logic result in this function
     */
    private void endTask() {
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        DependResult expected = "BlockingOnSuccess".equals(this.blockingConditionResult) ? DependResult.SUCCESS : DependResult.FAILED;
        logger.info("blocking result: expected-->{}, actual-->{}",expected,conditionResult);
        isBlocked = expected == conditionResult ? true : false;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    @Override
    public Object taskExtraInfo() {
        return isBlocked;
    }
}
