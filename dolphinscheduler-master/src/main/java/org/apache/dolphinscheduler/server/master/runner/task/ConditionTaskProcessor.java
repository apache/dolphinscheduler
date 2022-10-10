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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_CONDITIONS;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.service.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.auto.service.AutoService;

/**
 * condition task processor
 */
@AutoService(ITaskProcessor.class)
public class ConditionTaskProcessor extends BaseTaskProcessor {

    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     * condition result
     */
    private DependResult conditionResult = DependResult.WAITING;

    /**
     * complete task map
     */
    private Map<Long, TaskExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    @Override
    public boolean submitTask() {
        this.taskInstance =
                processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);
        if (this.taskInstance == null) {
            return false;
        }
        this.setTaskExecutionLogger();
        logger.info("condition task submit success");
        return true;
    }

    @Override
    public boolean runTask() {
        initTaskParameters();
        logger.info("condition task start");
        if (conditionResult.equals(DependResult.WAITING)) {
            setConditionResult();
            endTask();
        } else {
            endTask();
        }
        logger.info("condition task finished");
        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(TaskExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
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
        this.taskInstance.setState(TaskExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    public String getType() {
        return TASK_TYPE_CONDITIONS;
    }

    private void initTaskParameters() {
        taskInstance.setLogPath(
                LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(), processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion(),
                        taskInstance.getProcessInstanceId(),
                        taskInstance.getId()));
        this.taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        this.processService.saveTaskInstance(taskInstance);
        this.dependentParameters = taskInstance.getDependency();
    }

    private void setConditionResult() {

        List<TaskInstance> taskInstances = processService
                .findValidTaskListByProcessId(taskInstance.getProcessInstanceId(), processInstance.getTestFlag());
        for (TaskInstance task : taskInstances) {
            completeTaskList.putIfAbsent(task.getTaskCode(), task.getState());
        }

        List<DependResult> modelResultList = new ArrayList<>();
        for (DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()) {
            List<DependResult> itemDependResult = new ArrayList<>();
            for (DependentItem item : dependentTaskModel.getDependItemList()) {
                itemDependResult.add(getDependResultForItem(item));
            }
            DependResult modelResult =
                    DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
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
        if (!completeTaskList.containsKey(item.getDepTaskCode())) {
            logger.info("depend item: {} have not completed yet.", item.getDepTaskCode());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        TaskExecutionStatus executionStatus = completeTaskList.get(item.getDepTaskCode());
        if (executionStatus != item.getStatus()) {
            logger.info("depend item : {} expect status: {}, actual status: {}", item.getDepTaskCode(),
                    item.getStatus(), executionStatus);
            dependResult = DependResult.FAILED;
        }
        logger.info("dependent item complete, dependentTaskCode: {}, dependResult: {}", item.getDepTaskCode(),
                dependResult);
        return dependResult;
    }

    /**
     *
     */
    private void endTask() {
        TaskExecutionStatus status =
                (conditionResult == DependResult.SUCCESS) ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }
}
