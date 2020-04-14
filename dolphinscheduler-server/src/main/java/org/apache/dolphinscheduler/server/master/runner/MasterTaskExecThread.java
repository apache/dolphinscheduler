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

import org.slf4j.Logger;


import com.alibaba.fastjson.JSON;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.cache.TaskInstanceCacheManager;
import org.apache.dolphinscheduler.server.master.cache.impl.TaskInstanceCacheManagerImpl;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.LoggerFactory;

import java.util.Date;


/**
 * master task exec thread
 */
public class MasterTaskExecThread extends MasterBaseTaskExecThread {

    /**
     * logger of MasterTaskExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterTaskExecThread.class);


    /**
     * taskInstance state manager
     */
    private TaskInstanceCacheManager taskInstanceCacheManager;


    private NettyExecutorManager nettyExecutorManager;

    /**
     * constructor of MasterTaskExecThread
     * @param taskInstance      task instance
     */
    public MasterTaskExecThread(TaskInstance taskInstance){
        super(taskInstance);
        this.taskInstanceCacheManager = SpringApplicationContext.getBean(TaskInstanceCacheManagerImpl.class);
        this.nettyExecutorManager = SpringApplicationContext.getBean(NettyExecutorManager.class);
    }

    /**
     * get task instance
     * @return TaskInstance
     */
    @Override
    public TaskInstance getTaskInstance(){
        return this.taskInstance;
    }

    /**
     * whether already Killed,default false
     */
    private boolean alreadyKilled = false;

    /**
     * submit task instance and wait complete
     *
     * @return true is task quit is true
     */
    @Override
    public Boolean submitWaitComplete() {
        Boolean result = false;
        this.taskInstance = submit();
        if(this.taskInstance == null){
            logger.error("submit task instance to mysql and queue failed , please check and fix it");
            return result;
        }
        if(!this.taskInstance.getState().typeIsFinished()) {
            result = waitTaskQuit();
        }
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
        logger.info("task :{} id:{}, process id:{}, exec thread completed ",
                this.taskInstance.getName(),taskInstance.getId(), processInstance.getId() );
        return result;
    }

    /**
     * polling db
     *
     * wait task quit
     * @return true if task quit success
     */
    public Boolean waitTaskQuit(){
        // query new state
        taskInstance = processService.findTaskInstanceById(taskInstance.getId());
        logger.info("wait task: process id: {}, task id:{}, task name:{} complete",
                this.taskInstance.getProcessInstanceId(), this.taskInstance.getId(), this.taskInstance.getName());
        // task time out
        boolean checkTimeout = false;
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
                    return true;
                }
                // task instance add queue , waiting worker to kill
                if(this.cancel || this.processInstance.getState() == ExecutionStatus.READY_STOP){
                    cancelTaskInstance();
                }
                // task instance finished
                if (taskInstance.getState().typeIsFinished()){
                    // if task is final result , then remove taskInstance from cache
                    taskInstanceCacheManager.removeByTaskInstanceId(taskInstance.getId());
                    break;
                }
                if(checkTimeout){
                    long remainTime = getRemaintime(taskTimeoutParameter.getInterval() * 60L);
                    if (remainTime < 0) {
                        logger.warn("task id: {} execution time out",taskInstance.getId());
                        // process define
                        ProcessDefinition processDefine = processService.findProcessDefineById(processInstance.getProcessDefinitionId());
                        // send warn mail
                        alertDao.sendTaskTimeoutAlert(processInstance.getWarningGroupId(),processDefine.getReceivers(),
                                processDefine.getReceiversCc(), processInstance.getId(), processInstance.getName(),
                                taskInstance.getId(),taskInstance.getName());
                        checkTimeout = false;
                    }
                }
                // updateProcessInstance task instance
                taskInstance = processService.findTaskInstanceById(taskInstance.getId());
                processInstance = processService.findProcessInstanceById(processInstance.getId());
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                logger.error("exception",e);
                if (processInstance != null) {
                    logger.error("wait task quit failed, instance id:{}, task id:{}",
                            processInstance.getId(), taskInstance.getId());
                }
            }
        }
        return true;
    }


    /**
     *  task instance add queue , waiting worker to kill
     */
    private void cancelTaskInstance() throws Exception{
        if(alreadyKilled){
            return ;
        }
        alreadyKilled = true;

        TaskKillRequestCommand killCommand = new TaskKillRequestCommand();
        killCommand.setTaskInstanceId(taskInstance.getId());

        ExecutionContext executionContext = new ExecutionContext(killCommand.convert2Command(), ExecutorType.WORKER);

        Host host = Host.of(taskInstance.getHost());
        executionContext.setHost(host);

        nettyExecutorManager.executeDirectly(executionContext);

        logger.info("master add kill task :{} id:{} to kill queue",
                taskInstance.getName(), taskInstance.getId() );
    }

    /**
     * get task timeout parameter
     * @return TaskTimeoutParameter
     */
    private TaskTimeoutParameter getTaskTimeoutParameter(){
        String taskJson = taskInstance.getTaskJson();
        TaskNode taskNode = JSON.parseObject(taskJson, TaskNode.class);
        return taskNode.getTaskTimeoutParameter();
    }


    /**
     * get remain time?s?
     *
     * @return remain time
     */
    private long getRemaintime(long timeoutSeconds) {
        Date startTime = taskInstance.getStartTime();
        long usedTime = (System.currentTimeMillis() - startTime.getTime()) / 1000;
        return timeoutSeconds - usedTime;
    }
}
