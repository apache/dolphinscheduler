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
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.TaskTimeoutParameter;
import cn.escheduler.common.utils.CommonUtils;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.common.utils.TaskParametersUtils;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.LoggerUtils;
import cn.escheduler.server.worker.log.TaskLogger;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskManager;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


/**
 *  task scheduler thread
 */
public class TaskScheduleThread implements Callable<Boolean> {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskScheduleThread.class);

    private static final String TASK_PREFIX = "TASK";

    /**
     *  task instance
     */
    private TaskInstance taskInstance;

    /**
     *  process database access
     */
    private final ProcessDao processDao;

    /**
     *  execute task info
     */
    private AbstractTask task;

    public TaskScheduleThread(TaskInstance taskInstance, ProcessDao processDao){
        this.processDao = processDao;
        this.taskInstance = taskInstance;
    }

    @Override
    public Boolean call() throws Exception {

        // get task type
        String taskType = taskInstance.getTaskType();
        // set task state
        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);

        // update task state
        if(taskType.equals(TaskType.SQL.name())  || taskType.equals(TaskType.PROCEDURE.name())){
            processDao.changeTaskState(taskInstance.getState(),
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    null,
                    System.getProperty("user.dir") + "/logs/" +
                            taskInstance.getProcessDefinitionId() +"/" +
                            taskInstance.getProcessInstanceId() +"/" +
                            taskInstance.getId() + ".log",
                    taskInstance.getId());
        }else{
            processDao.changeTaskState(taskInstance.getState(),
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    taskInstance.getExecutePath(),
                    System.getProperty("user.dir") + "/logs/" +
                            taskInstance.getProcessDefinitionId() +"/" +
                            taskInstance.getProcessInstanceId() +"/" +
                            taskInstance.getId() + ".log",
                    taskInstance.getId());
        }

        ExecutionStatus status = ExecutionStatus.SUCCESS;

        try {


            // custom param str
            String customParamStr = taskInstance.getProcessInstance().getGlobalParams();


            Map<String,String> allParamMap = new HashMap<>();


            if (customParamStr != null) {
                List<Property> customParamMap = JSONObject.parseArray(customParamStr, Property.class);

                Map<String,String> userDefinedParamMap = customParamMap.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));

                allParamMap.putAll(userDefinedParamMap);
            }

            logger.info("script path : {}",taskInstance.getExecutePath());

            TaskProps taskProps = new TaskProps();

            taskProps.setTaskDir(taskInstance.getExecutePath());

            String taskJson = taskInstance.getTaskJson();


            TaskNode taskNode = JSONObject.parseObject(taskJson, TaskNode.class);

            List<String> projectRes = createProjectResFiles(taskNode);

            // copy hdfs file to local
            copyHdfsToLocal(processDao,
                    taskInstance.getExecutePath(),
                    projectRes,
                    logger);

            // set task params
            taskProps.setTaskParams(taskNode.getParams());
            // set tenant code , execute task linux user
            taskProps.setTenantCode(taskInstance.getProcessInstance().getTenantCode());

            ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskInstance.getId());
            taskProps.setScheduleTime(processInstance.getScheduleTime());
            taskProps.setNodeName(taskInstance.getName());
            taskProps.setTaskInstId(taskInstance.getId());
            taskProps.setEnvFile(CommonUtils.getSystemEnvPath());
            // set queue
            taskProps.setQueue(taskInstance.getProcessInstance().getQueue());
            taskProps.setTaskStartTime(taskInstance.getStartTime());
            taskProps.setDefinedParams(allParamMap);

            // set task timeout
            setTaskTimeout(taskProps, taskNode);

            taskProps.setDependence(taskInstance.getDependency());

            taskProps.setTaskAppId(String.format("%s_%s_%s",
                    taskInstance.getProcessDefine().getId(),
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId()));

            // custom logger
            TaskLogger taskLogger = new TaskLogger(LoggerUtils.buildTaskId(TASK_PREFIX,
                    taskInstance.getProcessDefine().getId(),
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId()));

            task = TaskManager.newTask(taskInstance.getTaskType(), taskProps, taskLogger);

            // job init
            task.init();

            // job handle
            task.handle();


            logger.info("task : {} exit status code : {}",taskProps.getTaskAppId(),task.getExitStatusCode());

            if (task.getExitStatusCode() == Constants.EXIT_CODE_SUCCESS){
                status = ExecutionStatus.SUCCESS;
            }else if (task.getExitStatusCode() == Constants.EXIT_CODE_KILL){
                status = ExecutionStatus.KILL;
            }else {
                status = ExecutionStatus.FAILURE;
            }
        }catch (Exception e){
            logger.error("task escheduler failure : " + e.getMessage(),e);
            status = ExecutionStatus.FAILURE ;
            logger.error(String.format("task process exception, process id : %s , task : %s",
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getName()),e);
            kill();
        }
        // update task instance state
        processDao.changeTaskState(status,
                new Date(),
                taskInstance.getId());
        return task.getExitStatusCode() > Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * set task time out
     * @param taskProps
     * @param taskNode
     */
    private void setTaskTimeout(TaskProps taskProps, TaskNode taskNode) {
        taskProps.setTaskTimeout(Integer.MAX_VALUE);
        TaskTimeoutParameter taskTimeoutParameter = taskNode.getTaskTimeoutParameter();
        if (taskTimeoutParameter.getEnable()){
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
     * copy hdfs file to local
     *
     * @param processDao
     * @param execLocalPath
     * @param projectRes
     * @param logger
     */
    private void copyHdfsToLocal(ProcessDao processDao, String execLocalPath, List<String> projectRes, Logger logger) throws IOException {
        for (String res : projectRes) {
            File resFile = new File(execLocalPath, res);
            if (!resFile.exists()) {
                try {
                    /**
                     * query the tenant code of the resource according to the name of the resource
                     */
                    String tentnCode = processDao.queryTenantCodeByResName(res);
                    String resHdfsPath = HadoopUtils.getHdfsFilename(tentnCode,res);

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
}