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
import org.apache.dolphinscheduler.common.queue.ITaskQueue;
import org.apache.dolphinscheduler.common.queue.TaskQueueFactory;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.utils.BeanContext;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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
     * process dao
     */
    protected ProcessDao processDao;

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
     * load configuration file
     */
    private static Configuration conf;

    static {
        try {
            conf = new PropertiesConfiguration(Constants.MASTER_PROPERTIES_PATH);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * constructor of MasterBaseTaskExecThread
     * @param taskInstance      task instance
     * @param processInstance   process instance
     */
    public MasterBaseTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance){
        this.processDao = BeanContext.getBean(ProcessDao.class);
        this.alertDao = BeanContext.getBean(AlertDao.class);
        this.processInstance = processInstance;
        this.taskQueue = TaskQueueFactory.getTaskQueueInstance();
        this.cancel = false;
        this.taskInstance = taskInstance;
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

    /**
     * submit master base task exec thread
     * @return TaskInstance
     */
    protected TaskInstance submit(){
        Integer commitRetryTimes = conf.getInt(Constants.MASTER_COMMIT_RETRY_TIMES,
                Constants.defaultMasterCommitRetryTimes);
        Integer commitRetryInterval = conf.getInt(Constants.MASTER_COMMIT_RETRY_INTERVAL,
                Constants.defaultMasterCommitRetryInterval);

        int retryTimes = 1;
        boolean taskDBFlag = false;
        boolean taskQueueFlag = false;
        while (true){
            try {
                TaskInstance task = null;
                if(!taskDBFlag){
                    // submit task to db
                    task = processDao.submitTask(taskInstance, processInstance);
                    if(task != null && task.getId() != 0){
                        taskDBFlag = true;
                    }
                }
                if(taskDBFlag && !taskQueueFlag){
                    // submit task to queue
                    taskQueueFlag = processDao.submitTaskToQueue(task);
                }
                if(taskDBFlag && taskQueueFlag){
                    return task;
                }
                if(!taskDBFlag){
                    logger.error("task commit to mysql failed , task has already retry {} times, please check the database", retryTimes);
                }else if(!taskQueueFlag){
                    logger.error("task commit to queue failed , task has already retry {} times, please check the database", retryTimes);

                }
                Thread.sleep(commitRetryInterval);
            } catch (Exception e) {
                logger.error("task commit to mysql and queue failed : " + e.getMessage(),e);
            }
            retryTimes += 1;
        }
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
