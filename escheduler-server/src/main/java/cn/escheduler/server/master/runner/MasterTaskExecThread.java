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
package cn.escheduler.server.master.runner;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.TaskTimeoutStrategy;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.task.TaskTimeoutParameter;
import cn.escheduler.common.thread.Stopper;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static cn.escheduler.common.Constants.SCHEDULER_TASKS_KILL;

/**
 *  master task exec thread
 */
public class MasterTaskExecThread extends MasterBaseTaskExecThread {

    private static final Logger logger = LoggerFactory.getLogger(MasterTaskExecThread.class);


    public MasterTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance){
        super(taskInstance, processInstance);
    }

    /**
     *  get task instance
     * @return
     */
    @Override
    public TaskInstance getTaskInstance(){
        return this.taskInstance;
    }

    private Boolean alreadyKilled = false;

    @Override
    public Boolean submitWaitComplete() {
        Boolean result = false;
        this.taskInstance = submit();
        if(!this.taskInstance.getState().typeIsFinished()) {
            result = waitTaskQuit();
        }
        taskInstance.setEndTime(new Date());
        processDao.updateTaskInstance(taskInstance);
        logger.info("task :{} id:{}, process id:{}, exec thread completed ",
                this.taskInstance.getName(),taskInstance.getId(), processInstance.getId() );
        return result;
    }


    public Boolean waitTaskQuit(){
        // query new state
        taskInstance = processDao.findTaskInstanceById(taskInstance.getId());
        Boolean result = true;
        // task time out
        Boolean checkTimeout = false;
        TaskTimeoutParameter taskTimeoutParameter = getTaskTimeoutParameter();
        if(taskTimeoutParameter.getEnable()){
            TaskTimeoutStrategy strategy = taskTimeoutParameter.getStrategy();
            if(strategy == TaskTimeoutStrategy.WARN || strategy == TaskTimeoutStrategy.WARNFAILED){
                checkTimeout = true;
            }
        }

        while (Stopper.isRunning()){
            try {
                if(this.processInstance == null){
                    logger.error("process instance not exists , master task exec thread exit");
                    return result;
                }
                // task instance add queue , waiting worker to kill
                if(this.cancel || this.processInstance.getState() == ExecutionStatus.READY_STOP){
                    cancelTaskInstance();
                }
                // task instance finished
                if (taskInstance.getState().typeIsFinished()){
                    break;
                }
                if(checkTimeout){
                    long remainTime = getRemaintime(taskTimeoutParameter.getInterval()*60);
                    if (remainTime < 0) {
                        logger.warn("task id: {} execution time out",taskInstance.getId());
                        // process define
                        ProcessDefinition processDefine = processDao.findProcessDefineById(processInstance.getProcessDefinitionId());
                        // send warn mail
                        alertDao.sendTaskTimeoutAlert(processInstance.getWarningGroupId(),processDefine.getReceivers(),processDefine.getReceiversCc(),taskInstance.getId(),taskInstance.getName());
                        checkTimeout = false;
                    }
                }
                // updateProcessInstance task instance
                taskInstance = processDao.findTaskInstanceById(taskInstance.getId());
                processInstance = processDao.findProcessInstanceById(processInstance.getId());
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                logger.error("exception: "+ e.getMessage(),e);
                logger.error("wait task quit failed, instance id:{}, task id:{}",
                        processInstance.getId(), taskInstance.getId());
            }
        }
        return  result;
    }


    /**
     *  task instance add queue , waiting worker to kill
     */
    private void cancelTaskInstance(){
        if(alreadyKilled || taskInstance.getHost() == null){
            return ;
        }
        alreadyKilled = true;
        String queueValue = String.format("%s-%d",
                taskInstance.getHost(), taskInstance.getId());
        taskQueue.sadd(SCHEDULER_TASKS_KILL, queueValue);

        logger.info("master add kill task :{} id:{} to kill queue",
                taskInstance.getName(), taskInstance.getId() );
    }

    /**
     * get task timeout parameter
     * @return
     */
    private TaskTimeoutParameter getTaskTimeoutParameter(){
        String taskJson = taskInstance.getTaskJson();
        TaskNode taskNode = JSONObject.parseObject(taskJson, TaskNode.class);
        return taskNode.getTaskTimeoutParameter();
    }


    /**
     * get remain time（s）
     *
     * @return
     */
    private long getRemaintime(long timeoutSeconds) {
        Date startTime = taskInstance.getStartTime();
        long usedTime = (System.currentTimeMillis() - startTime.getTime()) / 1000;
        long remainTime = timeoutSeconds - usedTime;
        return remainTime;
    }
}
