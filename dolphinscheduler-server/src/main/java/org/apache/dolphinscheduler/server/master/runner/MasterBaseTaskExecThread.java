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

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.BeanContext;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskRequestCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.ITaskQueue;
import org.apache.dolphinscheduler.service.queue.TaskQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * master task exec base class
 */
public class MasterBaseTaskExecThread implements Callable<Boolean> {

    /**
     * logger of MasterBaseTaskExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterBaseTaskExecThread.class);

    /**
     * process service
     */
    protected ProcessService processService;

    /**
     * alert database access
     */
    protected AlertDao alertDao;

    /**
     * process instance
     */
    protected ProcessInstance processInstance;

    /**
     * task instance
     */
    protected TaskInstance taskInstance;

    /**
     * task queue
     */
    protected ITaskQueue taskQueue;

    /**
     * whether need cancel
     */
    protected boolean cancel;

    /**
     * master config
     */
    private MasterConfig masterConfig;


    /**
     *  netty remoting client
     */
    private static final NettyRemotingClient nettyRemotingClient = new NettyRemotingClient(new NettyClientConfig());

    /**
     * constructor of MasterBaseTaskExecThread
     * @param taskInstance      task instance
     * @param processInstance   process instance
     */
    public MasterBaseTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance){
        this.processService = BeanContext.getBean(ProcessService.class);
        this.alertDao = BeanContext.getBean(AlertDao.class);
        this.processInstance = processInstance;
        this.taskQueue = TaskQueueFactory.getTaskQueueInstance();
        this.cancel = false;
        this.taskInstance = taskInstance;
        this.masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
    }

    /**
     * get task instance
     * @return TaskInstance
     */
    public TaskInstance getTaskInstance(){
        return this.taskInstance;
    }

    /**
     * kill master base task exec thread
     */
    public void kill(){
        this.cancel = true;
    }


    // TODO send task to worker
    public void sendToWorker(String taskInstanceJson){
        final Address address = new Address("192.168.220.247", 12346);
        ExecuteTaskRequestCommand command = new ExecuteTaskRequestCommand(taskInstanceJson);
        try {
            Command response = nettyRemotingClient.sendSync(address, command.convert2Command(), Integer.MAX_VALUE);
            logger.info("response result : {}",response);
        } catch (InterruptedException | RemotingException ex) {
            logger.error(String.format("send command to : %s error", address), ex);
        }
    }

    /**
     * submit master base task exec thread
     * @return TaskInstance
     */
    protected TaskInstance submit(){
        Integer commitRetryTimes = masterConfig.getMasterTaskCommitRetryTimes();
        Integer commitRetryInterval = masterConfig.getMasterTaskCommitInterval();

        int retryTimes = 1;
        boolean submitDB = false;
        boolean submitQueue = false;
        TaskInstance task = null;
        while (retryTimes <= commitRetryTimes){
            try {
                if(!submitDB){
                    // submit task to db
                    task = processService.submitTask(taskInstance, processInstance);
                    if(task != null && task.getId() != 0){
                        submitDB = true;
                    }
                }
                if(submitDB && !submitQueue){
                    // submit task to queue
                    sendToWorker(JSONObject.toJSONString(task));
                }
                if(submitDB && submitQueue){
                    return task;
                }
                if(!submitDB){
                    logger.error("task commit to db failed , taskId {} has already retry {} times, please check the database", taskInstance.getId(), retryTimes);
                }else if(!submitQueue){
                    logger.error("task commit to queue failed , taskId {} has already retry {} times, please check the queue", taskInstance.getId(), retryTimes);
                }
                Thread.sleep(commitRetryInterval);
            } catch (Exception e) {
                logger.error("task commit to mysql and queue failed",e);
            }
            retryTimes += 1;
        }
        return task;
    }

    /**
     * submit wait complete
     * @return true
     */
    protected Boolean submitWaitComplete(){
        return true;
    }

    /**
     * call
     * @return boolean
     * @throws Exception exception
     */
    @Override
    public Boolean call() throws Exception {
        return submitWaitComplete();
    }

}
