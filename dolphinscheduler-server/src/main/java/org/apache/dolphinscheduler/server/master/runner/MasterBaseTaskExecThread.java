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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.log.TaskLogDiscriminator;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.dolphinscheduler.common.Constants.*;

import java.util.concurrent.Callable;


/**
 * master task exec base class
 */
public class MasterBaseTaskExecThread implements Callable<Boolean> {

    /**
     * logger of MasterBaseTaskExecThread
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());


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
     * whether need cancel
     */
    protected boolean cancel;

    /**
     * master config
     */
    protected MasterConfig masterConfig;

    /**
     * taskUpdateQueue
     */
    private TaskPriorityQueue taskUpdateQueue;
    /**
     * constructor of MasterBaseTaskExecThread
     * @param taskInstance      task instance
     */
    public MasterBaseTaskExecThread(TaskInstance taskInstance){
        this.processService = SpringApplicationContext.getBean(ProcessService.class);
        this.alertDao = SpringApplicationContext.getBean(AlertDao.class);
        this.cancel = false;
        this.taskInstance = taskInstance;
        this.masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        this.taskUpdateQueue = SpringApplicationContext.getBean(TaskPriorityQueueImpl.class);
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
        Integer commitRetryTimes = masterConfig.getMasterTaskCommitRetryTimes();
        Integer commitRetryInterval = masterConfig.getMasterTaskCommitInterval();

        int retryTimes = 1;
        boolean submitDB = false;
        boolean submitTask = false;
        TaskInstance task = null;
        while (retryTimes <= commitRetryTimes){
            try {
                if(!submitDB){
                    // submit task to db
                    task = processService.submitTask(taskInstance);
                    if(task != null && task.getId() != 0){
                        submitDB = true;
                    }
                }
                if(submitDB && !submitTask){
                    // dispatch task
                    submitTask = dispatchTask(task);
                }
                if(submitDB && submitTask){
                    return task;
                }
                if(!submitDB){
                    logger.error("task commit to db failed , taskId {} has already retry {} times, please check the database", taskInstance.getId(), retryTimes);
                }else if(!submitTask){
                    logger.error("task commit  failed , taskId {} has already retry {} times, please check", taskInstance.getId(), retryTimes);
                }
                Thread.sleep(commitRetryInterval);
            } catch (Exception e) {
                logger.error("task commit to mysql and dispatcht task failed",e);
            }
            retryTimes += 1;
        }
        return task;
    }



    /**
     * dispatcht task
     * @param taskInstance taskInstance
     * @return whether submit task success
     */
    public Boolean dispatchTask(TaskInstance taskInstance) {

        try{
            if(taskInstance.isConditionsTask()
                    || taskInstance.isDependTask()
                    || taskInstance.isSubProcess()){
                return true;
            }
            if(taskInstance.getState().typeIsFinished()){
                logger.info(String.format("submit task , but task [%s] state [%s] is already  finished. ", taskInstance.getName(), taskInstance.getState().toString()));
                return true;
            }
            // task cannot submit when running
            if(taskInstance.getState() == ExecutionStatus.RUNNING_EXEUTION){
                logger.info(String.format("submit to task, but task [%s] state already be running. ", taskInstance.getName()));
                return true;
            }
            logger.info("task ready to submit: {}", taskInstance);

            /**
             *  taskPriorityInfo
             */
            String taskPriorityInfo = buildTaskPriorityInfo(processInstance.getProcessInstancePriority().getCode(),
                    processInstance.getId(),
                    taskInstance.getProcessInstancePriority().getCode(),
                    taskInstance.getId(),
                    org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP);
            taskUpdateQueue.put(taskPriorityInfo);
            logger.info(String.format("master submit success, task : %s", taskInstance.getName()) );
            return true;
        }catch (Exception e){
            logger.error("submit task  Exception: ", e);
            logger.error("task error : %s", JSONUtils.toJson(taskInstance));
            return false;
        }
    }


    /**
     *  buildTaskPriorityInfo
     *
     * @param processInstancePriority processInstancePriority
     * @param processInstanceId processInstanceId
     * @param taskInstancePriority taskInstancePriority
     * @param taskInstanceId taskInstanceId
     * @param workerGroup workerGroup
     * @return TaskPriorityInfo
     */
    private String buildTaskPriorityInfo(int processInstancePriority,
                                         int processInstanceId,
                                         int taskInstancePriority,
                                         int taskInstanceId,
                                         String workerGroup){
        return processInstancePriority +
                UNDERLINE +
                processInstanceId +
                UNDERLINE +
                taskInstancePriority +
                UNDERLINE +
                taskInstanceId +
                UNDERLINE +
                workerGroup;
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
        this.processInstance = processService.findProcessInstanceById(taskInstance.getProcessInstanceId());
        return submitWaitComplete();
    }

    /**
     * get task log path
     * @return log path
     */
    public String getTaskLogPath(TaskInstance task) {
        String logPath;
        try{
            String baseLog = ((TaskLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                    .getLogger("ROOT")
                    .getAppender("TASKLOGFILE"))
                    .getDiscriminator()).getLogBase();
            if (baseLog.startsWith(Constants.SINGLE_SLASH)){
                logPath =  baseLog + Constants.SINGLE_SLASH +
                        task.getProcessDefinitionId() + Constants.SINGLE_SLASH  +
                        task.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                        task.getId() + ".log";
            }else{
                logPath = System.getProperty("user.dir") + Constants.SINGLE_SLASH +
                        baseLog +  Constants.SINGLE_SLASH +
                        task.getProcessDefinitionId() + Constants.SINGLE_SLASH  +
                        task.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                        task.getId() + ".log";
            }
        }catch (Exception e){
            logger.error("logger", e);
            logPath = "";
        }
        return logPath;
    }


}
