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


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.log.TaskLogDiscriminator;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;
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
public class TaskScheduleThread implements Runnable {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskScheduleThread.class);

    /**
     *  task instance
     */
    private TaskInstance taskInstance;

    /**
     *  process service
     */
    private final ProcessService processService;

    /**
     *  abstract task
     */
    private AbstractTask task;

    /**
     *  task instance callback service
     */
    private TaskCallbackService taskInstanceCallbackService;

    /**
     * constructor
     *
     * @param taskInstance  task instance
     * @param processService    process dao
     */
    public TaskScheduleThread(TaskInstance taskInstance, ProcessService processService, TaskCallbackService taskInstanceCallbackService){
        this.processService = processService;
        this.taskInstance = taskInstance;
        this.taskInstanceCallbackService = taskInstanceCallbackService;
    }

    @Override
    public void run() {

        ExecuteTaskResponseCommand responseCommand = new ExecuteTaskResponseCommand(taskInstance.getId());

        try {
            // tell master that task is in executing
            ExecuteTaskAckCommand ackCommand = buildAckCommand(taskInstance.getTaskType());
            taskInstanceCallbackService.sendAck(taskInstance.getId(), ackCommand);

            logger.info("script path : {}", taskInstance.getExecutePath());
            // task node
            TaskNode taskNode = JSONObject.parseObject(taskInstance.getTaskJson(), TaskNode.class);

            // get resource files
            List<String> resourceFiles = createProjectResFiles(taskNode);
            // copy hdfs/minio file to local
            downloadResource(
                    taskInstance.getExecutePath(),
                    resourceFiles,
                    logger);


            // get process instance according to tak instance
            ProcessInstance processInstance = taskInstance.getProcessInstance();

            // set task props
            TaskProps taskProps = new TaskProps(taskNode.getParams(),
                    taskInstance.getExecutePath(),
                    processInstance.getScheduleTime(),
                    taskInstance.getName(),
                    taskInstance.getTaskType(),
                    taskInstance.getId(),
                    CommonUtils.getSystemEnvPath(),
                    processInstance.getTenantCode(),
                    processInstance.getQueue(),
                    taskInstance.getStartTime(),
                    getGlobalParamsMap(),
                    taskInstance.getDependency(),
                    processInstance.getCmdTypeIfComplement());
            // set task timeout
            setTaskTimeout(taskProps, taskNode);

            taskProps.setTaskAppId(String.format("%s_%s_%s",
                    taskInstance.getProcessDefine().getId(),
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId()));

            // custom logger
            Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskInstance.getProcessDefine().getId(),
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId()));

            task = TaskManager.newTask(taskInstance.getTaskType(),
                    taskProps,
                    taskLogger);

            // task init
            task.init();

            // task handle
            task.handle();

            // task result process
            task.after();

            //
            responseCommand.setStatus(task.getExitStatus().getCode());
            responseCommand.setEndTime(new Date());
            logger.info("task instance id : {},task final status : {}", taskInstance.getId(), task.getExitStatus());
        }catch (Exception e){
            logger.error("task scheduler failure", e);
            kill();
            responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
            responseCommand.setEndTime(new Date());
        } finally {
            taskInstanceCallbackService.sendResult(taskInstance.getId(), responseCommand);
        }
    }

    /**
     * get global paras map
     * @return
     */
    private Map<String, String> getGlobalParamsMap() {
        Map<String,String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = taskInstance.getProcessInstance().getGlobalParams();

        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONObject.parseArray(globalParamsStr, Property.class);
            globalParamsMap.putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        return globalParamsMap;
    }
    /**
     *  build ack command
     * @param taskType
     */
    private ExecuteTaskAckCommand buildAckCommand(String taskType) {
        ExecuteTaskAckCommand ackCommand = new ExecuteTaskAckCommand();
        ackCommand.setStatus(ExecutionStatus.RUNNING_EXEUTION.getCode());
        ackCommand.setLogPath(getTaskLogPath());
        ackCommand.setHost("localhost");
        ackCommand.setStartTime(new Date());
        if(taskType.equals(TaskType.SQL.name()) || taskType.equals(TaskType.PROCEDURE.name())){
            ackCommand.setExecutePath(null);
        }else{
            ackCommand.setExecutePath(taskInstance.getExecutePath());
        }
        return ackCommand;
    }

    /**
     * get task log path
     * @return log path
     */
    private String getTaskLogPath() {
        String baseLog = ((TaskLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger("ROOT")
                .getAppender("TASKLOGFILE"))
                .getDiscriminator()).getLogBase();
        if (baseLog.startsWith(Constants.SINGLE_SLASH)){
            return baseLog + Constants.SINGLE_SLASH +
                    taskInstance.getProcessDefinitionId() + Constants.SINGLE_SLASH  +
                    taskInstance.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                    taskInstance.getId() + ".log";
        }
        return System.getProperty("user.dir") + Constants.SINGLE_SLASH +
                baseLog +  Constants.SINGLE_SLASH +
                taskInstance.getProcessDefinitionId() + Constants.SINGLE_SLASH  +
                taskInstance.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                taskInstance.getId() + ".log";
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
        int userId = taskInstance.getProcessInstance().getExecutorId();
        String[] resNames = projectRes.toArray(new String[projectRes.size()]);
        PermissionCheck<String> permissionCheck = new PermissionCheck<>(AuthorizationType.RESOURCE_FILE, processService,resNames,userId,logger);
        permissionCheck.checkPermission();
    }

    /**
     *  update task state according to task type
     * @param taskType
     */
    private void updateTaskState(String taskType) {
        // update task status is running
        if(taskType.equals(TaskType.SQL.name())  ||
                taskType.equals(TaskType.PROCEDURE.name())){
            processService.changeTaskState(ExecutionStatus.RUNNING_EXEUTION,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    null,
                    getTaskLogPath(),
                    taskInstance.getId());
        }else{
            processService.changeTaskState(ExecutionStatus.RUNNING_EXEUTION,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    taskInstance.getExecutePath(),
                    getTaskLogPath(),
                    taskInstance.getId());
        }
    }
}