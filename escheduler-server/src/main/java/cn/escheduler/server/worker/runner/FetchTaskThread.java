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
import cn.escheduler.dao.model.WorkerGroup;
import cn.escheduler.server.zk.ZKWorkerClient;
import com.cronutils.utils.StringUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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

    /**
     * Check if the task runs on this worker
     * @param taskInstance
     * @param host
     * @return
     */
    private boolean checkWorkerGroup(TaskInstance taskInstance, String host){

        int taskWorkerGroupId = taskInstance.getWorkerGroupId();
        ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskInstance.getId());
        if(processInstance == null){
            logger.error("cannot find the task:{} process instance", taskInstance.getId());
            return false;
        }
        int processWorkerGroupId = processInstance.getWorkerGroupId();

        taskWorkerGroupId = (taskWorkerGroupId <= 0 ? processWorkerGroupId : taskWorkerGroupId);

        if(taskWorkerGroupId <= 0){
            return true;
        }
        WorkerGroup workerGroup = processDao.queryWorkerGroupById(taskWorkerGroupId);
        if(workerGroup == null ){
            logger.info("task {} cannot find the worker group, use all worker instead.", taskInstance.getId());
            return true;
        }
        String ips = workerGroup.getIpList();
        if(ips == null){
            logger.error("task:{} worker group:{} parameters(ip_list) is null, this task would be running on all workers",
                    taskInstance.getId(), workerGroup.getId());
        }
        String[] ipArray = ips.split(",");
        List<String> ipList =  Arrays.asList(ipArray);
        return ipList.contains(host);
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
                        String taskQueueStr = taskQueue.poll(Constants.SCHEDULER_TASKS_QUEUE, false);

                        if (!StringUtils.isEmpty(taskQueueStr )) {

                            String[] taskStringArray = taskQueueStr.split(Constants.UNDERLINE);
                            String taskInstIdStr = taskStringArray[taskStringArray.length - 1];
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

                            if (taskInstance == null ) {
                                logger.error("task instance is null. task id : {} ", taskId);
                                continue;
                            }
                            if(!checkWorkerGroup(taskInstance, OSUtils.getHost())){
                                continue;
                            }
                            taskQueue.removeNode(Constants.SCHEDULER_TASKS_QUEUE, taskQueueStr);
                            logger.info("remove task:{} from queue", taskQueueStr);

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
                                    processInstance.getTenantCode(), logger);

                            logger.info("task : {} ready to submit to task scheduler thread",taskId);
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