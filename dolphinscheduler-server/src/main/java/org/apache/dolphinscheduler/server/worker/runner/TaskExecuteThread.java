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


import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;
import org.apache.dolphinscheduler.remote.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskManager;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 *  task scheduler thread
 */
public class TaskExecuteThread implements Runnable {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskExecuteThread.class);

    /**
     *  task instance
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     *  process service
     */
    private final ProcessService processService;

    /**
     *  abstract task
     */
    private AbstractTask task;

    /**
     *  task callback service
     */
    private TaskCallbackService taskCallbackService;

    /**
     *  constructor
     * @param taskExecutionContext taskExecutionContext
     * @param processService processService
     * @param taskCallbackService taskCallbackService
     */
    public TaskExecuteThread(TaskExecutionContext taskExecutionContext, ProcessService processService, TaskCallbackService taskCallbackService){
        this.processService = processService;
        this.taskExecutionContext = taskExecutionContext;
        this.taskCallbackService = taskCallbackService;
    }

    @Override
    public void run() {

        ExecuteTaskResponseCommand responseCommand = new ExecuteTaskResponseCommand(taskExecutionContext.getTaskInstanceId());
        try {
            logger.info("script path : {}", taskExecutionContext.getExecutePath());
            // task node
            TaskNode taskNode = JSONObject.parseObject(taskExecutionContext.getTaskJson(), TaskNode.class);

            // get resource files
            List<String> resourceFiles = createProjectResFiles(taskNode);
            // copy hdfs/minio file to local
            downloadResource(
                    taskExecutionContext.getExecutePath(),
                    resourceFiles,
                    logger);

            // set task props
            TaskProps taskProps = new TaskProps(taskNode.getParams(),
                    taskExecutionContext.getScheduleTime(),
                    taskExecutionContext.getTaskName(),
                    taskExecutionContext.getTaskType(),
                    taskExecutionContext.getTaskInstanceId(),
                    CommonUtils.getSystemEnvPath(),
                    taskExecutionContext.getTenantCode(),
                    taskExecutionContext.getQueue(),
                    taskExecutionContext.getStartTime(),
                    getGlobalParamsMap(),
                    null,
                    CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                    OSUtils.getHost(),
                    taskExecutionContext.getLogPath(),
                    taskExecutionContext.getExecutePath());
            // set task timeout
            setTaskTimeout(taskProps, taskNode);

            taskProps.setTaskAppId(String.format("%s_%s_%s",
                    taskExecutionContext.getProcessDefineId(),
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId()));

            // custom logger
            Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskExecutionContext.getProcessDefineId(),
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId()));

            task = TaskManager.newTask(taskExecutionContext.getTaskType(),
                    taskProps,
                    taskLogger);

            // task init
            task.init();

            // task handle
            task.handle();

            // task result process
            task.after();

            responseCommand.setStatus(task.getExitStatus().getCode());
            responseCommand.setEndTime(new Date());
            logger.info("task instance id : {},task final status : {}", taskExecutionContext.getTaskInstanceId(), task.getExitStatus());
        }catch (Exception e){
            logger.error("task scheduler failure", e);
            kill();
            responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
            responseCommand.setEndTime(new Date());
        } finally {
            taskCallbackService.sendResult(taskExecutionContext.getTaskInstanceId(), responseCommand);
        }
    }

    /**
     * get global paras map
     * @return
     */
    private Map<String, String> getGlobalParamsMap() {
        Map<String,String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = taskExecutionContext.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONObject.parseArray(globalParamsStr, Property.class);
            globalParamsMap.putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        return globalParamsMap;
    }

    /**
     * set task timeout
     * @param taskProps
     * @param taskNode
     */
    private void setTaskTimeout(TaskProps taskProps, TaskNode taskNode) {
        // the default timeout is the maximum value of the integer
        taskProps.setTaskTimeout(Integer.MAX_VALUE);
        TaskTimeoutParameter taskTimeoutParameter = taskNode.getTaskTimeoutParameter();
        if (taskTimeoutParameter.getEnable()){
            // get timeout strategy
            taskProps.setTaskTimeoutStrategy(taskTimeoutParameter.getStrategy());
            switch (taskTimeoutParameter.getStrategy()){
                case WARN:
                    break;
                case FAILED:
                    if (Integer.MAX_VALUE > taskTimeoutParameter.getInterval() * 60) {
                        taskProps.setTaskTimeout(taskTimeoutParameter.getInterval() * 60);
                    }
                    break;
                case WARNFAILED:
                    if (Integer.MAX_VALUE > taskTimeoutParameter.getInterval() * 60) {
                        taskProps.setTaskTimeout(taskTimeoutParameter.getInterval() * 60);
                    }
                    break;
                default:
                    logger.error("not support task timeout strategy: {}", taskTimeoutParameter.getStrategy());
                    throw new IllegalArgumentException("not support task timeout strategy");

            }
        }
    }


    /**
     *  kill task
     */
    public void kill(){
        if (task != null){
            try {
                task.cancelApplication(true);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }


    /**
     *  create project resource files
     */
    private List<String> createProjectResFiles(TaskNode taskNode) throws Exception{

        Set<String> projectFiles = new HashSet<>();
        AbstractParameters baseParam = TaskParametersUtils.getParameters(taskNode.getType(), taskNode.getParams());

        if (baseParam != null) {
            List<String> projectResourceFiles = baseParam.getResourceFilesList();
            if (projectResourceFiles != null) {
                projectFiles.addAll(projectResourceFiles);
            }
        }

        return new ArrayList<>(projectFiles);
    }

    /**
     * download resource file
     *
     * @param execLocalPath
     * @param projectRes
     * @param logger
     */
    private void downloadResource(String execLocalPath, List<String> projectRes, Logger logger) throws Exception {
        checkDownloadPermission(projectRes);
        for (String res : projectRes) {
            File resFile = new File(execLocalPath, res);
            if (!resFile.exists()) {
                try {
                    // query the tenant code of the resource according to the name of the resource
                    String tentnCode = processService.queryTenantCodeByResName(res);
                    String resHdfsPath = HadoopUtils.getHdfsFilename(tentnCode, res);

                    logger.info("get resource file from hdfs :{}", resHdfsPath);
                    HadoopUtils.getInstance().copyHdfsToLocal(resHdfsPath, execLocalPath + File.separator + res, false, true);
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                    throw new RuntimeException(e.getMessage());
                }
            } else {
                logger.info("file : {} exists ", resFile.getName());
            }
        }
    }

    /**
     * check download resource permission
     * @param projectRes resource name list
     * @throws Exception exception
     */
    private void checkDownloadPermission(List<String> projectRes) throws Exception {
        int executorId = taskExecutionContext.getExecutorId();
        String[] resNames = projectRes.toArray(new String[projectRes.size()]);
        PermissionCheck<String> permissionCheck = new PermissionCheck<>(AuthorizationType.RESOURCE_FILE, processService,resNames,executorId,logger);
        permissionCheck.checkPermission();
    }
}