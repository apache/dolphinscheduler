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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 *  subflow task exec thread
 */
public class SubProcessTaskExecThread extends MasterBaseTaskExecThread {

    /**
     * logger of SubProcessTaskExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(SubProcessTaskExecThread.class);

    /**
     * sub process instance
     */
    private ProcessInstance subProcessInstance;

    /**
     * sub process task exec thread
     * @param taskInstance      task instance
     * @param processInstance   process instance
     */
    public SubProcessTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance){
        super(taskInstance, processInstance);
    }

    @Override
    public Boolean submitWaitComplete() {

        Boolean result = false;
        try{
            // submit task instance
            this.taskInstance = submit();

            if(taskInstance == null){
                logger.error("sub work flow submit task instance to mysql and queue failed , please check and fix it");
                return result;
            }
            setTaskInstanceState();
            waitTaskQuit();
            subProcessInstance = processDao.findSubProcessInstance(processInstance.getId(), taskInstance.getId());

            // at the end of the subflow , the task state is changed to the subflow state
            if(subProcessInstance != null){
                if(subProcessInstance.getState() == ExecutionStatus.STOP){
                    this.taskInstance.setState(ExecutionStatus.KILL);
                }else{
                    this.taskInstance.setState(subProcessInstance.getState());
                }
            }
            taskInstance.setEndTime(new Date());
            processDao.updateTaskInstance(taskInstance);
            logger.info("subflow task :{} id:{}, process id:{}, exec thread completed ",
                    this.taskInstance.getName(),taskInstance.getId(), processInstance.getId() );
            result = true;

        }catch (Exception e){
            logger.error("exception: "+ e.getMessage(),e);
            logger.error("wait task quit failed, instance id:{}, task id:{}",
                    processInstance.getId(), taskInstance.getId());
        }
        return result;
    }


    /**
     *  set task instance state
     * @return
     */
    private Boolean setTaskInstanceState(){
        subProcessInstance = processDao.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if(subProcessInstance == null || taskInstance.getState().typeIsFinished()){
            return false;
        }

        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        taskInstance.setStartTime(new Date());
        processDao.updateTaskInstance(taskInstance);
        return true;
    }

    /**
     *  updateProcessInstance parent state
     */
    private void updateParentProcessState(){
        ProcessInstance parentProcessInstance = processDao.findProcessInstanceById(this.processInstance.getId());

        if(parentProcessInstance == null){
            logger.error("parent work flow instance is null ,  please check it! work flow id {}", processInstance.getId());
            return;
        }
        this.processInstance.setState(parentProcessInstance.getState());
    }

    /**
     * wait task quit
     * @throws InterruptedException
     */
    private void waitTaskQuit() throws InterruptedException {

        logger.info("wait sub work flow: {} complete", this.taskInstance.getName());

        if (taskInstance.getState().typeIsFinished()) {
            logger.info("sub work flow task {} already complete. task state:{}, parent work flow instance state:{}",
                    this.taskInstance.getName(),
                    this.taskInstance.getState().toString(),
                    this.processInstance.getState().toString());
            return;
        }
        while (Stopper.isRunning()) {
            // waiting for subflow process instance establishment
            if (subProcessInstance == null) {

                Thread.sleep(Constants.SLEEP_TIME_MILLIS);

                if(!setTaskInstanceState()){
                    continue;
                }
            }
            subProcessInstance = processDao.findProcessInstanceById(subProcessInstance.getId());
            updateParentProcessState();
            if (subProcessInstance.getState().typeIsFinished()){
                break;
            }

            if(this.processInstance.getState() == ExecutionStatus.READY_PAUSE){
                // parent process "ready to pause" , child process "pause"
                pauseSubProcess();
            }else if(this.cancel || this.processInstance.getState() == ExecutionStatus.READY_STOP){
                // parent Process "Ready to Cancel" , subflow "Cancel"
                stopSubProcess();
            }
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
        }
    }

    /**
     * stop sub process
     */
    private void stopSubProcess() {
        if(subProcessInstance.getState() == ExecutionStatus.STOP ||
                subProcessInstance.getState() == ExecutionStatus.READY_STOP){
            return;
        }
        subProcessInstance.setState(ExecutionStatus.READY_STOP);
        processDao.updateProcessInstance(subProcessInstance);
    }

    /**
     * pause sub process
     */
    private void pauseSubProcess() {
        if(subProcessInstance.getState() == ExecutionStatus.PAUSE ||
                subProcessInstance.getState() == ExecutionStatus.READY_PAUSE){
            return;
        }
        subProcessInstance.setState(ExecutionStatus.READY_PAUSE);
        processDao.updateProcessInstance(subProcessInstance);
    }
}
