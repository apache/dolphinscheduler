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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.logging.log4j.core.tools.CustomLoggerGenerator;

import java.util.Date;


/**
 *
 */
public class SubTaskProcessor extends BaseTaskProcessor{



    ProcessInstance processInstance;

    ProcessInstance subProcessInstance = null;
    TaskDefinition taskDefinition;

    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    @Override
    public boolean submit(TaskInstance taskInstance, ProcessInstance processInstance, int masterTaskCommitRetryTimes, int masterTaskCommitInterval) {
        this.processInstance = processInstance;
        this.taskInstance = taskInstance;
        taskDefinition = processService.findTaskDefinition(
                taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion()
        );
        if (!processService.submitTask(this.taskInstance, masterTaskCommitRetryTimes, masterTaskCommitInterval)) {
            return false;
        }
        return true;
    }

    @Override
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    @Override
    public void run() {

        try{
            if(setSubWorkFlow()){
                updateTaskState();
            }
        }catch (Exception e){
            logger.error("work flow {} sub task {} exceptions",
                    this.processInstance.getId(),
                    this.taskInstance.getId(),
                    e);

        }
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy =
                taskDefinition.getTimeoutNotifyStrategy();
        if(TaskTimeoutStrategy.FAILED != taskTimeoutStrategy
                && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy){
            return true;
        }
        logger.info("sub process task {} timeout, strategy {} ",
                taskInstance.getId(), taskTimeoutStrategy.getDescp());
        killTask();
        return true;
    }

    private void updateTaskState() {
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        logger.info("work flow {} task {}, sub work flow: {} state: {}",
                this.processInstance.getId(),
                this.taskInstance.getId(),
                subProcessInstance.getId(),
                subProcessInstance.getState().getDescp());
        if(subProcessInstance != null && subProcessInstance.getState().typeIsFinished()){
            taskInstance.setState(subProcessInstance.getState());
            taskInstance.setEndTime(new Date());
            processService.saveTaskInstance(taskInstance);
        }
    }

    @Override
    protected boolean pauseTask() {
        pauseSubWorkFlow();
        return true;
    }

    private boolean pauseSubWorkFlow() {
        ProcessInstance subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if(subProcessInstance == null || taskInstance.getState().typeIsFinished()){
            return false;
        }
        subProcessInstance.setState(ExecutionStatus.READY_PAUSE);
        processService.updateProcessInstance(subProcessInstance);
        //TODO...
        // send event to sub process master
        return true;
    }

    private boolean setSubWorkFlow() {
        logger.info("set work flow {} task {} running",
                this.processInstance.getId(),
                this.taskInstance.getId());
        if(this.subProcessInstance != null){
            return true;
        }
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if(subProcessInstance == null || taskInstance.getState().typeIsFinished()){
            return false;
        }

        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
        logger.info("set sub work flow {} task {} state: {}",
                processInstance.getId(),
                taskInstance.getId(),
                taskInstance.getState());
        return true;

    }

    @Override
    protected boolean killTask() {
        ProcessInstance subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if(subProcessInstance == null || taskInstance.getState().typeIsFinished()){
            return false;
        }
        subProcessInstance.setState(ExecutionStatus.READY_STOP);
        processService.updateProcessInstance(subProcessInstance);
        return true;
    }

    @Override
    public String getType() {
        return TaskType.SUB_PROCESS.getDesc();
    }
}
