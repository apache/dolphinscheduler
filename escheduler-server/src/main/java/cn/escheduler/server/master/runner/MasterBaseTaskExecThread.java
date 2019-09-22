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
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.queue.TaskQueueFactory;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.dao.utils.BeanContext;
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

    private static final Logger logger = LoggerFactory.getLogger(MasterBaseTaskExecThread.class);

    /**
     *  process dao
     */
    protected ProcessDao processDao;

    /**
     *  alert database access
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
     *  task queue
     */
    protected ITaskQueue taskQueue;
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

    public MasterBaseTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance){
        this.processDao = BeanContext.getBean(ProcessDao.class);
        this.alertDao = BeanContext.getBean(AlertDao.class);
        this.processInstance = processInstance;
        this.taskQueue = TaskQueueFactory.getTaskQueueInstance();
        this.cancel = false;
        this.taskInstance = taskInstance;
    }

    public TaskInstance getTaskInstance(){
        return this.taskInstance;
    }

    public void kill(){
        this.cancel = true;
    }

    protected TaskInstance submit(){
        Integer commitRetryTimes = conf.getInt(Constants.MASTER_COMMIT_RETRY_TIMES,
                Constants.defaultMasterCommitRetryTimes);
        Integer commitRetryInterval = conf.getInt(Constants.MASTER_COMMIT_RETRY_INTERVAL,
                Constants.defaultMasterCommitRetryInterval);

        int retryTimes = 1;

        while (retryTimes <= commitRetryTimes){
            try {
                TaskInstance task = processDao.submitTask(taskInstance, processInstance);
                if(task != null){
                    return task;
                }
                logger.error("task commit to mysql and queue failed , task has already retry {} times, please check the database", commitRetryTimes);
                Thread.sleep(commitRetryInterval);
            } catch (Exception e) {
                logger.error("task commit to mysql and queue failed : " + e.getMessage(),e);
            }
            retryTimes += 1;
        }
        return null;
    }

    protected Boolean submitWaitComplete(){
        return true;
    }

    @Override
    public Boolean call() throws Exception {
        return submitWaitComplete();
    }

}
