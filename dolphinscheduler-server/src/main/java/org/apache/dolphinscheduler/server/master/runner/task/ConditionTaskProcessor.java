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
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

/**
 * condition task processor
 */
public class ConditionTaskProcessor extends BaseTaskProcessor {

    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    ProcessInstance processInstance;

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

    private TaskDefinition taskDefinition;

    @Override
    public boolean submit(TaskInstance task, ProcessInstance processInstance, int masterTaskCommitRetryTimes, int masterTaskCommitInterval) {
        this.processInstance = processInstance;
        this.taskInstance = processService.submitTask(task, masterTaskCommitRetryTimes, masterTaskCommitInterval);

        if (this.taskInstance == null) {
            return false;
        }
        taskDefinition = processService.findTaskDefinition(
                taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion()
        );

        logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
        Thread.currentThread().setName(threadLoggerInfoName);
        initTaskParameters();
        logger.info("dependent task start");
        endTask();
        return true;
    }

    @Override
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    @Override
    public void run() {
        if (conditionResult.equals(DependResult.WAITING)) {
            setConditionResult();
        } else {
            endTask();
        }
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(ExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy =
                taskDefinition.getTimeoutNotifyStrategy();
        if (taskTimeoutStrategy == TaskTimeoutStrategy.WARN) {
            return true;
        }
        logger.info("condition task {} timeout, strategy {} ",
                taskInstance.getId(), taskTimeoutStrategy.getDescp());
        conditionResult = DependResult.FAILED;
        endTask();
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
    public String getType() {
        return TaskType.CONDITIONS.getDesc();
    }

    private void initTaskParameters() {
        taskInstance.setLogPath(LogUtils.getTaskLogPath(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        this.taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        this.processService.saveTaskInstance(taskInstance);
        this.dependentParameters = taskInstance.getDependency();
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
                itemDependResult.add(getDependResultForItem(item));
            }
            DependResult modelResult = DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            modelResultList.add(modelResult);
        }
        conditionResult = DependentUtils.getDependResultForRelation(dependentParameters.getRelation(), modelResultList);
        logger.info("the conditions task depend result : {}", conditionResult);
    }

    /**
     * depend result for depend item
     */
    private DependResult getDependResultForItem(DependentItem item) {

        DependResult dependResult = DependResult.SUCCESS;
        if (!completeTaskList.containsKey(item.getDepTasks())) {
            logger.info("depend item: {} have not completed yet.", item.getDepTasks());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        ExecutionStatus executionStatus = completeTaskList.get(item.getDepTasks());
        if (executionStatus != item.getStatus()) {
            logger.info("depend item : {} expect status: {}, actual status: {}", item.getDepTasks(), item.getStatus(), executionStatus);
            dependResult = DependResult.FAILED;
        }
        logger.info("dependent item complete {} {},{}",
                Constants.DEPENDENT_SPLIT, item.getDepTasks(), dependResult);
        return dependResult;
    }

    /**
     *
     */
    private void endTask() {
        ExecutionStatus status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }
}
