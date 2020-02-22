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
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskRequestCommand;
import org.apache.dolphinscheduler.remote.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        String contextJson = taskRequestCommand.getTaskExecutionContext();

        TaskExecutionContext taskExecutionContext = JSONObject.parseObject(contextJson, TaskExecutionContext.class);

        // local execute path
        String execLocalPath = getExecLocalPath(taskExecutionContext);
        logger.info("task instance  local execute path : {} ", execLocalPath);

        try {
            FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, taskExecutionContext.getTenantCode());
        } catch (Exception ex){
            logger.error(String.format("create execLocalPath : %s", execLocalPath), ex);
        }
        taskCallbackService.addCallbackChannel(taskExecutionContext.getTaskInstanceId(),
                new CallbackChannel(channel, command.getOpaque()));

        // submit task
        workerExecService.submit(new TaskScheduleThread(taskExecutionContext,
                processService, taskCallbackService));
    }


    /**
     * get execute local path
     * @param taskExecutionContext taskExecutionContext
     * @return execute local path
     */
    private String getExecLocalPath(TaskExecutionContext taskExecutionContext){
        return FileUtils.getProcessExecDir(taskExecutionContext.getProjectId(),
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }
}
