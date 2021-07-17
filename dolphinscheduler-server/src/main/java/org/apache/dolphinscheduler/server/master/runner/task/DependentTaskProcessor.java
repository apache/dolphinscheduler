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

import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.utils.DependentExecute;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DependentTaskProcessor extends BaseTaskProcessor {

    @Autowired
    MasterConfig masterConfig;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dependentDate;

    ProcessInstance processInstance;
    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);


    boolean allDependentItemFinished;

    @Override
    public boolean submit(TaskInstance taskInstance, ProcessInstance processInstance, int masterTaskCommitRetryTimes, int masterTaskCommitInterval) {
        this.processInstance = processInstance;
        this.taskInstance = taskInstance;
        if (!processService.submitTask(taskInstance, masterTaskCommitRetryTimes, masterTaskCommitInterval)) {
            return false;
        }
        taskInstance.setLogPath(LogUtils.getTaskLogPath(processInstance.getProcessDefinitionCode(),
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
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    @Override
    public void run() {
        if (!allDependentItemFinished) {
            allDependentItemFinished = allDependentTaskFinish();
            return;
        }
        updateTaskState();
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
        DependResult result = DependentUtils.getDependResultForRelation(this.dependentParameters.getRelation(), dependResultList);
        logger.info("dependent task completed, dependent result:{}", result);
        return result;
    }

    /**
     *
     */
    private void updateTaskState() {
        ExecutionStatus status;
        DependResult result = getTaskDependResult();
        status = (result == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
    }

    @Override
    public String getType() {
        return TaskType.DEPENDENT.getDesc();
    }
}
