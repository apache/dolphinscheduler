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
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class SubTaskProcessor extends BaseTaskProcessor{



    ProcessInstance processInstance;

    ProcessInstance subProcessInstance = null;
    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);



    @Override
    public boolean submit(TaskInstance taskInstance, ProcessInstance processInstance, int masterTaskCommitRetryTimes, int masterTaskCommitInterval) {
        this.processInstance = processInstance;
        this.taskInstance = taskInstance;
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
        setSubWorkFlow();
        updateTaskState();
    }

    private void updateTaskState() {
        if(null == subProcessInstance){
            return ;
        }
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if(subProcessInstance.getState().typeIsFinished()){
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
        //TODO...
        // send event to sub process master
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
