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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.utils.BeanContext;
import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskRequestCommand;
import org.apache.dolphinscheduler.remote.command.TaskInfo;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Address;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
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
    public void sendToWorker(TaskInstance taskInstance){
        final Address address = new Address("127.0.0.1", 12346);

        /**
         *  set taskInstance relation
         */
        TaskInstance destTaskInstance = setTaskInstanceRelation(taskInstance);

        ExecuteTaskRequestCommand taskRequestCommand = new ExecuteTaskRequestCommand(
                FastJsonSerializer.serializeToString(convertToTaskInfo(destTaskInstance)));
        try {
            Command responseCommand = nettyRemotingClient.sendSync(address,
                    taskRequestCommand.convert2Command(), Integer.MAX_VALUE);

            ExecuteTaskAckCommand taskAckCommand = FastJsonSerializer.deserialize(
                    responseCommand.getBody(), ExecuteTaskAckCommand.class);

            logger.info("taskAckCommand : {}",taskAckCommand);

            processService.changeTaskState(ExecutionStatus.of(taskAckCommand.getStatus()),
                    taskAckCommand.getStartTime(),
                    taskAckCommand.getHost(),
                    taskAckCommand.getExecutePath(),
                    taskAckCommand.getLogPath(),
                    taskInstance.getId());

        } catch (InterruptedException | RemotingException ex) {
            logger.error(String.format("send command to : %s error", address), ex);
        }
    }


    /**
     *  set task instance relation
     *
     * @param taskInstance taskInstance
     */
    private TaskInstance setTaskInstanceRelation(TaskInstance taskInstance){
        taskInstance = processService.getTaskInstanceDetailByTaskId(taskInstance.getId());

        int userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);
        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            processService.changeTaskState(ExecutionStatus.FAILURE, taskInstance.getStartTime(), taskInstance.getHost(), null, null, taskInstance.getId());
            return null;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstanceId(taskInstance.getProcessInstanceId());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());

        return taskInstance;
    }


    /**
     *  whehter tenant is null
     * @param tenant tenant
     * @param taskInstance taskInstance
     * @return result
     */
    private boolean verifyTenantIsNull(Tenant tenant, TaskInstance taskInstance) {
        if(tenant == null){
            logger.error("tenant not exists,process instance id : {},task instance id : {}",
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId());
            return true;
        }
        return false;
    }


    /**
     * taskInstance convert to taskInfo
     *
     * @param taskInstance taskInstance
     * @return taskInfo
     */
    private TaskInfo convertToTaskInfo(TaskInstance taskInstance){
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskInstance.getId());
        taskInfo.setTaskName(taskInstance.getName());
        taskInfo.setStartTime(taskInstance.getStartTime());
        taskInfo.setTaskType(taskInstance.getTaskType());
        taskInfo.setExecutePath(getExecLocalPath(taskInstance));
        taskInfo.setTaskJson(taskInstance.getTaskJson());
        taskInfo.setProcessInstanceId(taskInstance.getProcessInstance().getId());
        taskInfo.setScheduleTime(taskInstance.getProcessInstance().getScheduleTime());
        taskInfo.setGlobalParams(taskInstance.getProcessInstance().getGlobalParams());
        taskInfo.setExecutorId(taskInstance.getProcessInstance().getExecutorId());
        taskInfo.setCmdTypeIfComplement(taskInstance.getProcessInstance().getCmdTypeIfComplement().getCode());
        taskInfo.setTenantCode(taskInstance.getProcessInstance().getTenantCode());
        taskInfo.setQueue(taskInstance.getProcessInstance().getQueue());
        taskInfo.setProcessDefineId(taskInstance.getProcessDefine().getId());
        taskInfo.setProjectId(taskInstance.getProcessDefine().getProjectId());

        return taskInfo;
    }


    /**
     * get execute local path
     *
     * @return execute local path
     */
    private String getExecLocalPath(TaskInstance taskInstance){
        return FileUtils.getProcessExecDir(taskInstance.getProcessDefine().getProjectId(),
                taskInstance.getProcessDefine().getId(),
                taskInstance.getProcessInstance().getId(),
                taskInstance.getId());
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
                    sendToWorker(task);
                    submitQueue = true;
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
