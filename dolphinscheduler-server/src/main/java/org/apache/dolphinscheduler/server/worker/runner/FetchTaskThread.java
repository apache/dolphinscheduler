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
package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.queue.ITaskQueue;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.common.zk.AbstractZKClient;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.zk.ZKWorkerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *  fetch task thread
 */
public class FetchTaskThread implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(FetchTaskThread.class);
    /**
     *  set worker concurrent tasks
     */
    private final int taskNum;

    /**
     *  zkWorkerClient
     */
    private final ZKWorkerClient zkWorkerClient;

    /**
     * task queue impl
     */
    protected ITaskQueue taskQueue;

    /**
     *  process database access
     */
    private final ProcessDao processDao;

    /**
     *  worker thread pool executor
     */
    private final ExecutorService workerExecService;

    /**
     *  worker exec nums
     */
    private int workerExecNums;

    /**
     *  task instance
     */
    private TaskInstance taskInstance;

    /**
     * task instance id
     */
    Integer taskInstId;

    /**
     * worker config
     */
    private WorkerConfig workerConfig;

    public FetchTaskThread(ZKWorkerClient zkWorkerClient,
                           ProcessDao processDao,
                           ITaskQueue taskQueue){
        this.zkWorkerClient = zkWorkerClient;
        this.processDao = processDao;
        this.taskQueue = taskQueue;
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.taskNum = workerConfig.getWorkerFetchTaskNum();
        this.workerExecNums = workerConfig.getWorkerExecThreads();
        // worker thread pool executor
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Fetch-Task-Thread", workerExecNums);
        this.taskInstance = null;
    }

    /**
     * Check if the task runs on this worker
     * @param taskInstance
     * @param host
     * @return
     */
    private boolean checkWorkerGroup(TaskInstance taskInstance, String host){

        int taskWorkerGroupId = processDao.getTaskWorkerGroupId(taskInstance);

        if(taskWorkerGroupId <= 0){
            return true;
        }
        WorkerGroup workerGroup = processDao.queryWorkerGroupById(taskWorkerGroupId);
        if(workerGroup == null ){
            logger.info("task {} cannot find the worker group, use all worker instead.", taskInstance.getId());
            return true;
        }
        String ips = workerGroup.getIpList();
        if(StringUtils.isBlank(ips)){
            logger.error("task:{} worker group:{} parameters(ip_list) is null, this task would be running on all workers",
                    taskInstance.getId(), workerGroup.getId());
        }
        String[] ipArray = ips.split(Constants.COMMA);
        List<String> ipList =  Arrays.asList(ipArray);
        return ipList.contains(host);
    }




    @Override
    public void run() {
        logger.info("worker start fetch tasks...");
        while (Stopper.isRunning()){
            InterProcessMutex mutex = null;
            try {
                ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) workerExecService;
                //check memory and cpu usage and threads
                boolean runCheckFlag = OSUtils.checkResource(workerConfig.getWorkerMaxCpuloadAvg(), workerConfig.getWorkerReservedMemory()) && checkThreadCount(poolExecutor);

                if(!runCheckFlag) {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }

                //whether have tasks, if no tasks , no need lock  //get all tasks
                List<String> tasksQueueList = taskQueue.getAllTasks(Constants.DOLPHINSCHEDULER_TASKS_QUEUE);
                if (CollectionUtils.isEmpty(tasksQueueList)){
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                    continue;
                }
                // creating distributed locks, lock path /dolphinscheduler/lock/worker
                mutex = zkWorkerClient.acquireZkLock(zkWorkerClient.getZkClient(),
                        zkWorkerClient.getWorkerLockPath());


                // task instance id str
                List<String> taskQueueStrArr = taskQueue.poll(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, taskNum);

                for(String taskQueueStr : taskQueueStrArr){
                    if (StringUtils.isEmpty(taskQueueStr)) {
                        continue;
                    }

                    if (!checkThreadCount(poolExecutor)) {
                        break;
                    }

                    // get task instance id
                    taskInstId = getTaskInstanceId(taskQueueStr);

                    // mainly to wait for the master insert task to succeed
                    waitForTaskInstance();

                    taskInstance = processDao.getTaskInstanceDetailByTaskId(taskInstId);

                    // verify task instance is null
                    if (verifyTaskInstanceIsNull(taskInstance)) {
                        logger.warn("remove task queue : {} due to taskInstance is null", taskQueueStr);
                        removeNodeFromTaskQueue(taskQueueStr);
                        continue;
                    }

                    Tenant tenant = processDao.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(),
                            taskInstance.getProcessDefine().getUserId());

                    // verify tenant is null
                    if (verifyTenantIsNull(tenant)) {
                        logger.warn("remove task queue : {} due to tenant is null", taskQueueStr);
                        removeNodeFromTaskQueue(taskQueueStr);
                        continue;
                    }

                    // set queue for process instance, user-specified queue takes precedence over tenant queue
                    String userQueue = processDao.queryUserQueueByProcessInstanceId(taskInstance.getProcessInstanceId());
                    taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
                    taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());

                    logger.info("worker fetch taskId : {} from queue ", taskInstId);


                    if(!checkWorkerGroup(taskInstance, OSUtils.getHost())){
                        continue;
                    }

                    // local execute path
                    String execLocalPath = getExecLocalPath();

                    logger.info("task instance  local execute path : {} ", execLocalPath);

                    // init task
                    taskInstance.init(OSUtils.getHost(),
                            new Date(),
                            execLocalPath);

                    // check and create users
                    FileUtils.createWorkDirAndUserIfAbsent(execLocalPath,
                            tenant.getTenantCode());

                    logger.info("task : {} ready to submit to task scheduler thread",taskInstId);
                    // submit task
                    workerExecService.submit(new TaskScheduleThread(taskInstance, processDao));

                    // remove node from zk
                    removeNodeFromTaskQueue(taskQueueStr);
                }

            }catch (Exception e){
                logger.error("fetch task thread failure" ,e);
            }finally {
                AbstractZKClient.releaseMutex(mutex);
            }
        }
    }

    /**
     * remove node from task queue
     *
     * @param taskQueueStr task queue
     */
    private void removeNodeFromTaskQueue(String taskQueueStr){
        taskQueue.removeNode(Constants.DOLPHINSCHEDULER_TASKS_QUEUE, taskQueueStr);
    }

    /**
     * verify task instance is null
     * @param taskInstance
     * @return true if task instance is null
     */
    private boolean verifyTaskInstanceIsNull(TaskInstance taskInstance) {
        if (taskInstance == null ) {
            logger.error("task instance is null. task id : {} ", taskInstId);
            return true;
        }
        return false;
    }

    /**
     * verify tenant is null
     *
     * @param tenant tenant
     * @return true if tenant is null
     */
    private boolean verifyTenantIsNull(Tenant tenant) {
        if(tenant == null){
            logger.error("tenant not exists,process define id : {},process instance id : {},task instance id : {}",
                    taskInstance.getProcessDefine().getId(),
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId());
            return true;
        }
        return false;
    }

    /**
     * get execute local path
     *
     * @return execute local path
     */
    private String getExecLocalPath(){
        return FileUtils.getProcessExecDir(taskInstance.getProcessDefine().getProjectId(),
                taskInstance.getProcessDefine().getId(),
                taskInstance.getProcessInstance().getId(),
                taskInstance.getId());
    }

    /**
     * check thread count
     *
     * @param poolExecutor pool executor
     * @return true if active count < worker exec nums
     */
    private boolean checkThreadCount(ThreadPoolExecutor poolExecutor) {
        int activeCount = poolExecutor.getActiveCount();
        if (activeCount >= workerExecNums) {
            logger.info("thread insufficient , activeCount : {} , " +
                            "workerExecNums : {}, will sleep : {} millis for thread resource",
                    activeCount,
                    workerExecNums,
                    Constants.SLEEP_TIME_MILLIS);
            return false;
        }
        return true;
    }

    /**
     * wait for task instance exists, because of db action would be delayed.
     *
     * @throws Exception exception
     */
    private void waitForTaskInstance()throws Exception{
        int retryTimes = 30;
        while (taskInstance == null && retryTimes > 0) {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            taskInstance = processDao.findTaskInstanceById(taskInstId);
            retryTimes--;
        }
    }

    /**
     * get task instance id
     *
     * @param taskQueueStr task queue
     * @return task instance id
     */
    private int getTaskInstanceId(String taskQueueStr){
        return Integer.parseInt(taskQueueStr.split(Constants.UNDERLINE)[3]);
    }
}
