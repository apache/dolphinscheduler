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

package org.apache.dolphinscheduler.server.worker.processor;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskRequestCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ExecutorService;

/**
 *  worker request processor
 */
public class WorkerRequestProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(WorkerRequestProcessor.class);

    /**
     * process service
     */
    private final ProcessService processService;

    /**
     *  thread executor service
     */
    private final ExecutorService workerExecService;

    /**
     *  worker config
     */
    private final WorkerConfig workerConfig;

    /**
     *  task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public WorkerRequestProcessor(ProcessService processService){
        this.processService = processService;
        this.taskCallbackService = new TaskCallbackService();
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getWorkerExecThreads());
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.EXECUTE_TASK_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));
        logger.info("received command : {}", command);
        ExecuteTaskRequestCommand taskRequestCommand = FastJsonSerializer.deserialize(
                command.getBody(), ExecuteTaskRequestCommand.class);

        String taskInstanceJson = taskRequestCommand.getTaskInstanceJson();

        TaskInstance taskInstance = JSONObject.parseObject(taskInstanceJson, TaskInstance.class);

        taskInstance = processService.getTaskInstanceDetailByTaskId(taskInstance.getId());


        //TODO this logic need add to master
        int userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);
        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            processService.changeTaskState(ExecutionStatus.FAILURE, taskInstance.getStartTime(), taskInstance.getHost(), null, null, taskInstance.getId());
            return;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstanceId(taskInstance.getProcessInstanceId());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());
        //TODO end

        // local execute path
        String execLocalPath = getExecLocalPath(taskInstance);
        logger.info("task instance  local execute path : {} ", execLocalPath);
        // init task
        taskInstance.init(OSUtils.getHost(), new Date(), execLocalPath);
        try {
            FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, tenant.getTenantCode());
        } catch (Exception ex){
            logger.error(String.format("create execLocalPath : %s", execLocalPath), ex);
        }

        taskCallbackService.addCallbackChannel(taskInstance.getId(),
                new CallbackChannel(channel, command.getOpaque()));

        // submit task
        workerExecService.submit(new TaskScheduleThread(taskInstance,
                processService, taskCallbackService));

        ExecuteTaskResponseCommand executeTaskResponseCommand = new ExecuteTaskResponseCommand(taskInstance.getId());
        channel.writeAndFlush(executeTaskResponseCommand.convert2Command(command.getOpaque()));
    }

    private boolean verifyTenantIsNull(Tenant tenant, TaskInstance taskInstance) {
        if(tenant == null){
            logger.error("tenant not exists,process instance id : {},task instance id : {}",
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId());
            return true;
        }
        return false;
    }

    private String getExecLocalPath(TaskInstance taskInstance){
        return FileUtils.getProcessExecDir(taskInstance.getProcessDefine().getProjectId(),
                taskInstance.getProcessDefine().getId(),
                taskInstance.getProcessInstance().getId(),
                taskInstance.getId());
    }
}
