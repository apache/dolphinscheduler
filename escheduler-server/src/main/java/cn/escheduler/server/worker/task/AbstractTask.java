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
package cn.escheduler.server.worker.task;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.TaskRecordStatus;
import cn.escheduler.common.enums.TaskType;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.flink.FlinkParameters;
import cn.escheduler.common.task.mr.MapreduceParameters;
import cn.escheduler.common.task.procedure.ProcedureParameters;
import cn.escheduler.common.task.python.PythonParameters;
import cn.escheduler.common.task.shell.ShellParameters;
import cn.escheduler.common.task.spark.SparkParameters;
import cn.escheduler.common.task.sql.SqlParameters;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.dao.TaskRecordDao;
import cn.escheduler.server.utils.ParamUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 *  executive task
 */
public abstract class AbstractTask {

    /**
     * task props
     **/
    protected TaskProps taskProps;

    /**
     *  log record
     */
    protected Logger logger;


    /**
     *  cancel
     */
    protected volatile boolean cancel = false;

    /**
     *  exit code
     */
    protected volatile int exitStatusCode = -1;

    /**
     * @param taskProps
     * @param logger
     */
    protected AbstractTask(TaskProps taskProps, Logger logger) {
        this.taskProps = taskProps;
        this.logger = logger;
    }

    /**
     * init task
     */
    public void init() throws Exception {
    }

    /**
     * task handle
     */
    public abstract void handle() throws Exception;



    public void cancelApplication(boolean status) throws Exception {
        this.cancel = status;
    }

    /**
     *  log process
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        logger.info(" -> {}", String.join("\n\t", logs));
    }


    /**
     *  exit code
     */
    public int getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(int exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    /**
     * get task parameters
     */
    public abstract AbstractParameters getParameters();


    /**
     * result processing
     */
    public void after(){
        if (getExitStatusCode() == Constants.EXIT_CODE_SUCCESS){
            // task recor flat : if true , start up qianfan
            if (TaskRecordDao.getTaskRecordFlag()
                    && TaskType.typeIsNormalTask(taskProps.getTaskType())){
                AbstractParameters params = (AbstractParameters) JSONUtils.parseObject(taskProps.getTaskParams(), getCurTaskParamsClass());

                // replace placeholder
                Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                        taskProps.getDefinedParams(),
                        params.getLocalParametersMap(),
                        taskProps.getCmdTypeIfComplement(),
                        taskProps.getScheduleTime());
                if (paramsMap != null && !paramsMap.isEmpty()
                        && paramsMap.containsKey("v_proc_date")){
                    String vProcDate = paramsMap.get("v_proc_date").getValue();
                    if (!StringUtils.isEmpty(vProcDate)){
                        TaskRecordStatus taskRecordState = TaskRecordDao.getTaskRecordState(taskProps.getNodeName(), vProcDate);
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
     * @return
     */
    private Class getCurTaskParamsClass(){
        Class paramsClass = null;
        // get task type
        TaskType taskType = TaskType.valueOf(taskProps.getTaskType());
        switch (taskType){
            case SHELL:
                paramsClass = ShellParameters.class;
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
            case PYTHON:
                paramsClass = PythonParameters.class;
                break;
            default:
                logger.error("not support this task type: {}", taskType);
                throw new IllegalArgumentException("not support this task type");
        }
        return paramsClass;
    }

    /**
     *  get exit status according to exitCode
     * @return
     */
    public ExecutionStatus getExitStatus(){
        ExecutionStatus status;
        switch (getExitStatusCode()){
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