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
package cn.escheduler.server.worker.runner;

import cn.escheduler.common.Constants;
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.thread.Stopper;
import cn.escheduler.common.thread.ThreadUtils;
import cn.escheduler.common.utils.FileUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.zk.ZKWorkerClient;
import com.cronutils.utils.StringUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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

    private Configuration conf;


    public FetchTaskThread(int taskNum, ZKWorkerClient zkWorkerClient,
                           ProcessDao processDao, Configuration conf,
                           ITaskQueue taskQueue){
        this.taskNum = taskNum;
        this.zkWorkerClient = zkWorkerClient;
        this.processDao = processDao;
        this.workerExecNums = conf.getInt(Constants.WORKER_EXEC_THREADS,
                Constants.defaultWorkerExecThreadNum);
        // worker thread pool executor
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Fetch-Task-Thread",workerExecNums);
        this.conf = conf;
        this.taskQueue = taskQueue;
    }


    @Override
    public void run() {

        while (Stopper.isRunning()){

            InterProcessMutex mutex = null;
            try {
                if(OSUtils.checkResource(this.conf, false)) {

                    // creating distributed locks, lock path /escheduler/lock/worker
                    String zNodeLockPath = zkWorkerClient.getWorkerLockPath();
                    mutex = new InterProcessMutex(zkWorkerClient.getZkClient(), zNodeLockPath);
                    mutex.acquire();

                    ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) workerExecService;

                    for (int i = 0; i < taskNum; i++) {

                        int activeCount = poolExecutor.getActiveCount();
                        if (activeCount >= workerExecNums) {
                            logger.info("thread insufficient , activeCount : {} , workerExecNums : {}",activeCount,workerExecNums);
                            continue;
                        }

                        // task instance id str
                        String taskInstIdStr = taskQueue.poll(Constants.SCHEDULER_TASKS_QUEUE);

                        if (!StringUtils.isEmpty(taskInstIdStr)) {
                            Date now = new Date();

                            Integer taskId = Integer.parseInt(taskInstIdStr);

                            // find task instance by task id
                            TaskInstance taskInstance = processDao.findTaskInstanceById(taskId);

                            logger.info("worker fetch taskId : {} from queue ", taskId);

                            int retryTimes = 30;
                            // mainly to wait for the master insert task to succeed
                            while (taskInstance == null && retryTimes > 0) {
                                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                                taskInstance = processDao.findTaskInstanceById(taskId);
                                retryTimes--;
                            }

                            if (taskInstance == null) {
                                logger.error("task instance is null. task id : {} ", taskId);
                                continue;
                            }

                            // set execute task worker host
                            taskInstance.setHost(OSUtils.getHost());
                            taskInstance.setStartTime(now);


                            // get process instance
                            ProcessInstance processInstance = processDao.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());

                            // get process define
                            ProcessDefinition processDefine = processDao.findProcessDefineById(taskInstance.getProcessDefinitionId());


                            taskInstance.setProcessInstance(processInstance);
                            taskInstance.setProcessDefine(processDefine);


                            // get local execute path
                            String execLocalPath = FileUtils.getProcessExecDir(processDefine.getProjectId(),
                                    processDefine.getId(),
                                    processInstance.getId(),
                                    taskInstance.getId());
                            logger.info("task instance  local execute path : {} ", execLocalPath);


                            // set task execute path
                            taskInstance.setExecutePath(execLocalPath);

                            // check and create Linux users
                            FileUtils.createWorkDirAndUserIfAbsent(execLocalPath,
                                    processDefine.getUserName(), logger);


                            // submit task
                            workerExecService.submit(new TaskScheduleThread(taskInstance, processDao));
                        }
                    }
                }

                Thread.sleep(Constants.SLEEP_TIME_MILLIS);

            }catch (Exception e){
                logger.error("fetch task thread exception : " + e.getMessage(),e);
            }
            finally {
                if (mutex != null){
                    try {
                        mutex.release();
                    } catch (Exception e) {
                        if(e.getMessage().equals("instance must be started before calling this method")){
                            logger.warn("fetch task lock release");
                        }else{
                            logger.error("fetch task lock release failed : " + e.getMessage(),e);
                        }
                    }
                }
            }
        }
    }
}