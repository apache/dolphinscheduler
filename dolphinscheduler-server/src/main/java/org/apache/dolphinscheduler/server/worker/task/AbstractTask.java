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

package org.apache.dolphinscheduler.server.worker.task;

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
<<<<<<< HEAD
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.task.mr.MapreduceParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.python.PythonParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.common.task.ssh.SSHParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.TaskRecordDao;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.slf4j.Logger;
=======
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;

/**
 * executive task
 */
public abstract class AbstractTask {

    /**
     * varPool string
     */
    protected String varPool;

    /**
     * taskExecutionContext
     **/
    TaskExecutionContext taskExecutionContext;

    /**
     * log record
     */
    protected Logger logger;


    /**
     * SHELL process pid
     */
    protected int processId;

    /**
     * SHELL result string
     */
    protected String resultString;

    /**
     * other resource manager appId , for example : YARN etc
     */
    protected String appIds;


    /**
     * cancel
     */
    protected volatile boolean cancel = false;

    /**
     * exit code
     */
    protected volatile int exitStatusCode = -1;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    protected AbstractTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        this.taskExecutionContext = taskExecutionContext;
        this.logger = logger;
    }

    /**
     * init task
     *
     * @throws Exception exception
     */
    public void init() throws Exception {
    }

    /**
     * task handle
     *
     * @throws Exception exception
     */
    public abstract void handle() throws Exception;

    /**
     * result processing
     *
     * @throws Exception exception
     */
    public void after() throws Exception {
    }

    /**
     * cancel application
     *
     * @param status status
     * @throws Exception exception
     */
    public void cancelApplication(boolean status) throws Exception {
        this.cancel = status;
    }

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        if (logs.contains(FINALIZE_SESSION_MARKER.toString())) {
            logger.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        } else {
            // note: if the logs is a SynchronizedList and will be modified concurrently,
            // we should must use foreach to iterate the element, otherwise will throw a ConcurrentModifiedException(#issue 5528)
            StringJoiner joiner = new StringJoiner("\n\t");
            logs.forEach(joiner::add);
            logger.info(" -> {}", joiner);
        }
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }

    public String getVarPool() {
        return varPool;
    }

    /**
     * get exit status code
     *
     * @return exit status code
     */
    public int getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(int exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    public String getAppIds() {
        return appIds;
    }

    public void setAppIds(String appIds) {
        this.appIds = appIds;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    /**
     * get task parameters
     *
     * @return AbstractParameters
     */
    public abstract AbstractParameters getParameters();

<<<<<<< HEAD


    /**
     * result processing
     */
    public void after(){
        if (getExitStatusCode() == Constants.EXIT_CODE_SUCCESS){
            // task recor flat : if true , start up qianfan
            if (TaskRecordDao.getTaskRecordFlag()
                    && TaskType.typeIsNormalTask(taskExecutionContext.getTaskType())){
                AbstractParameters params = (AbstractParameters) JSONUtils.parseObject(taskExecutionContext.getTaskParams(), getCurTaskParamsClass());

                // replace placeholder
                Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                        taskExecutionContext.getDefinedParams(),
                        params.getLocalParametersMap(),
                        CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                        taskExecutionContext.getScheduleTime());
                if (paramsMap != null && !paramsMap.isEmpty()
                        && paramsMap.containsKey("v_proc_date")){
                    String vProcDate = paramsMap.get("v_proc_date").getValue();
                    if (!StringUtils.isEmpty(vProcDate)){
                        TaskRecordStatus taskRecordState = TaskRecordDao.getTaskRecordState(taskExecutionContext.getTaskName(), vProcDate);
                        logger.info("task record status : {}",taskRecordState);
                        if (taskRecordState == TaskRecordStatus.FAILURE){
                            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
                        }
                    }
                }
            }

        }else if (getExitStatusCode() == Constants.EXIT_CODE_KILL){
            setExitStatusCode(Constants.EXIT_CODE_KILL);
        }else {
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
        }
    }




    /**
     * get current task parameter class
     * @return Task Params Class
     */
    private Class getCurTaskParamsClass(){
        Class paramsClass = null;
        // get task type
        TaskType taskType = TaskType.valueOf(taskExecutionContext.getTaskType());
        switch (taskType){
            case SHELL:
                Boolean remote = false;
                if (JSONUtils.parseObject(taskExecutionContext.getTaskParams()).get("remote") != null) {
                    remote = JSONUtils.parseObject(taskExecutionContext.getTaskParams()).get("remote").asBoolean();
                }
                if (remote != null && remote) {
                    paramsClass = ShellParameters.class;
                } else {
                    paramsClass = SSHParameters.class;
                }
                break;
            case SQL:
                paramsClass = SqlParameters.class;
                break;
            case PROCEDURE:
                paramsClass = ProcedureParameters.class;
                break;
            case MR:
                paramsClass = MapreduceParameters.class;
                break;
            case SPARK:
                paramsClass = SparkParameters.class;
                break;
            case FLINK:
                paramsClass = FlinkParameters.class;
                break;
            case PYTHON:
                paramsClass = PythonParameters.class;
                break;
            case DATAX:
                paramsClass = DataxParameters.class;
                break;
            case SQOOP:
                paramsClass = SqoopParameters.class;
                break;
            case CONDITIONS:
                paramsClass = ConditionsParameters.class;
                break;
            default:
                logger.error("not support this task type: {}", taskType);
                throw new IllegalArgumentException("not support this task type");
        }
        return paramsClass;
=======
    private boolean typeIsNormalTask(String taskType) {
        return !(TaskType.SUB_PROCESS.getDesc().equalsIgnoreCase(taskType) || TaskType.DEPENDENT.getDesc().equalsIgnoreCase(taskType));
>>>>>>> cc9e5d5d34fcf2279b267cca7df37a9e80eeba07
    }

    /**
     * get exit status according to exitCode
     *
     * @return exit status
     */
    public ExecutionStatus getExitStatus() {
        ExecutionStatus status;
        switch (getExitStatusCode()) {
            case Constants.EXIT_CODE_SUCCESS:
                status = ExecutionStatus.SUCCESS;
                break;
            case Constants.EXIT_CODE_KILL:
                status = ExecutionStatus.KILL;
                break;
            default:
                status = ExecutionStatus.FAILURE;
                break;
        }
        return status;
    }

}