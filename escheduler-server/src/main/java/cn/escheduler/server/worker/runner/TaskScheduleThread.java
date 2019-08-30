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
import cn.escheduler.common.enums.TaskRecordStatus;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.TaskTimeoutParameter;
import cn.escheduler.common.task.mr.MapreduceParameters;
import cn.escheduler.common.task.procedure.ProcedureParameters;
import cn.escheduler.common.task.python.PythonParameters;
import cn.escheduler.common.task.shell.ShellParameters;
import cn.escheduler.common.task.spark.SparkParameters;
import cn.escheduler.common.task.sql.SqlParameters;
import cn.escheduler.common.utils.*;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.TaskRecordDao;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.dao.model.Tenant;
import cn.escheduler.server.utils.LoggerUtils;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskManager;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
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
     *  process database access
     */
    private final ProcessDao processDao;

    /**
     *  abstract task
     */
    private AbstractTask task;

    public TaskScheduleThread(TaskInstance taskInstance, ProcessDao processDao){
        this.processDao = processDao;
        this.taskInstance = taskInstance;
    }

    @Override
    public void run() {

        // update task state is running according to task type
        updateTaskState(taskInstance.getTaskType());

        try {
            logger.info("script path : {}", taskInstance.getExecutePath());
            // task node
            TaskNode taskNode = JSONObject.parseObject(taskInstance.getTaskJson(), TaskNode.class);

            // copy hdfs/minio file to local
            copyHdfsToLocal(processDao,
                    taskInstance.getExecutePath(),
                    createProjectResFiles(taskNode),
                    logger);

            // get process instance according to tak instance
            ProcessInstance processInstance = taskInstance.getProcessInstance();
            // get process define according to tak instance
            ProcessDefinition processDefine = taskInstance.getProcessDefine();

            // get tenant info
            Tenant tenant = processDao.getTenantForProcess(processInstance.getTenantId(),
                                                    processDefine.getUserId());

            if(tenant == null){
                logger.error("cannot find the tenant, process definition id:{}, user id:{}",
                        processDefine.getId(),
                        processDefine.getUserId());
                task.setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            }else{

                // set task props
                TaskProps taskProps = new TaskProps(taskNode.getParams(),
                        taskInstance.getExecutePath(),
                        processInstance.getScheduleTime(),
                        taskInstance.getName(),
                        taskInstance.getTaskType(),
                        taskInstance.getId(),
                        CommonUtils.getSystemEnvPath(),
                        tenant.getTenantCode(),
                        tenant.getQueue(),
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
            }
        }catch (Exception e){
            logger.error("task scheduler failure", e);
            task.setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            kill();
        }

        logger.info("task instance id : {},task final status : {}",
                taskInstance.getId(),
                task.getExitStatus());
        // update task instance state
        processDao.changeTaskState(task.getExitStatus(),
                new Date(),
                taskInstance.getId());
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
     *  update task state according to task type
     * @param taskType
     */
    private void updateTaskState(String taskType) {
        // update task status is running
        if(taskType.equals(TaskType.SQL.name())  ||
                taskType.equals(TaskType.PROCEDURE.name())){
            processDao.changeTaskState(ExecutionStatus.RUNNING_EXEUTION,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    null,
                    getTaskLogPath(),
                    taskInstance.getId());
        }else{
            processDao.changeTaskState(ExecutionStatus.RUNNING_EXEUTION,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    taskInstance.getExecutePath(),
                    getTaskLogPath(),
                    taskInstance.getId());
        }
    }

    /**
     *  get task log path
     * @return
     */
    private String getTaskLogPath() {
        return System.getProperty("user.dir") + Constants.SINGLE_SLASH +
                "logs" +  Constants.SINGLE_SLASH +
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
                    // query the tenant code of the resource according to the name of the resource
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