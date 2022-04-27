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

import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.DependentUtils;
import org.apache.dolphinscheduler.server.utils.DependentExecute;
import org.apache.dolphinscheduler.server.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.service.AutoService;

/**
 * dependent task processor
 */
@AutoService(ITaskProcessor.class)
public class DependentTaskProcessor extends BaseTaskProcessor {

    private DependentParameters dependentParameters;

    /**
     * dependent task list
     */
    private List<DependentExecute> dependentTaskList = new ArrayList<>();

    /**
     * depend item result map
     * save the result to log file
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    /**
     * dependent date
     */
    private Date dependentDate;

    DependResult result;

    boolean allDependentItemFinished;

    @Override
    public boolean submitTask() {
        this.taskInstance = processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }
        this.setTaskExecutionLogger();
        taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(),
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
        initDependParameters();
        return true;
    }

    @Override
    public boolean runTask() {
        if (!allDependentItemFinished) {
            allDependentItemFinished = allDependentTaskFinish();
        }
        if (allDependentItemFinished) {
            getTaskDependResult();
            endTask();
        }
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED != taskTimeoutStrategy
                && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy) {
            return true;
        }
        logger.info("dependent task {} timeout, strategy {} ",
                taskInstance.getId(), taskTimeoutStrategy.getDescp());
        result = DependResult.FAILED;
        endTask();
        return true;
    }

    /**
     * init dependent parameters
     */
    private void initDependParameters() {
        this.dependentParameters = taskInstance.getDependency();
        for (DependentTaskModel taskModel : dependentParameters.getDependTaskList()) {
            this.dependentTaskList.add(new DependentExecute(taskModel.getDependItemList(), taskModel.getRelation()));
        }
        if (processInstance.getScheduleTime() != null) {
            this.dependentDate = this.processInstance.getScheduleTime();
        } else {
            this.dependentDate = new Date();
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
    protected boolean killTask() {
        this.taskInstance.setState(ExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    /**
     * judge all dependent tasks finish
     *
     * @return whether all dependent tasks finish
     */
    private boolean allDependentTaskFinish() {
        boolean finish = true;
        for (DependentExecute dependentExecute : dependentTaskList) {
            for (Map.Entry<String, DependResult> entry : dependentExecute.getDependResultMap().entrySet()) {
                if (!dependResultMap.containsKey(entry.getKey())) {
                    dependResultMap.put(entry.getKey(), entry.getValue());
                    //save depend result to log
                    logger.info("dependent item complete {} {},{}", DEPENDENT_SPLIT, entry.getKey(), entry.getValue());
                }
            }
            if (!dependentExecute.finish(dependentDate)) {
                finish = false;
            }
        }
        return finish;
    }

    /**
     * get dependent result
     *
     * @return DependResult
     */
    private DependResult getTaskDependResult() {
        List<DependResult> dependResultList = new ArrayList<>();
        for (DependentExecute dependentExecute : dependentTaskList) {
            DependResult dependResult = dependentExecute.getModelDependResult(dependentDate);
            dependResultList.add(dependResult);
        }
        result = DependentUtils.getDependResultForRelation(this.dependentParameters.getRelation(), dependResultList);
        logger.info("dependent task completed, dependent result:{}", result);
        return result;
    }

    /**
     *
     */
    private void endTask() {
        ExecutionStatus status;
        status = (result == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
    }

    @Override
    public String getType() {
        return TASK_TYPE_DEPENDENT;
    }
}
