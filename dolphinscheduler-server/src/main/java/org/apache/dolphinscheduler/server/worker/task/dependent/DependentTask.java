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
package org.apache.dolphinscheduler.server.worker.task.dependent;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.slf4j.Logger;

import java.util.*;

import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;

/**
 * Dependent Task
 */
public class DependentTask extends AbstractTask {

    /**
     * dependent task list
     */
    private List<DependentExecute> dependentTaskList = new ArrayList<>();

    /**
     * depend item result map
     * save the result to log file
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();

    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     * dependent date
     */
    private Date dependentDate;

    /**
     * process dao
     */
    private ProcessDao processDao;

    /**
     * constructor
     * @param props     props
     * @param logger    logger
     */
    public DependentTask(TaskProps props, Logger logger) {
        super(props, logger);
    }

    @Override
    public void init(){
        logger.info("dependent task initialize");

        this.dependentParameters = JSONUtils.parseObject(this.taskProps.getDependence(),
                DependentParameters.class);

        for(DependentTaskModel taskModel : dependentParameters.getDependTaskList()){
            this.dependentTaskList.add(new DependentExecute(
                            taskModel.getDependItemList(), taskModel.getRelation()));
        }

        this.processDao = SpringApplicationContext.getBean(ProcessDao.class);

        if(taskProps.getScheduleTime() != null){
            this.dependentDate = taskProps.getScheduleTime();
        }else{
            this.dependentDate = taskProps.getTaskStartTime();
        }

    }

    @Override
    public void handle(){
        // set the name of the current thread
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        try{
            TaskInstance taskInstance = null;
            while(Stopper.isRunning()){
                taskInstance = processDao.findTaskInstanceById(this.taskProps.getTaskInstId());

                if(taskInstance == null){
                    exitStatusCode = -1;
                    break;
                }

                if(taskInstance.getState() == ExecutionStatus.KILL){
                    this.cancel = true;
                }

                if(this.cancel || allDependentTaskFinish()){
                    break;
                }

                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            }

            if(cancel){
                exitStatusCode = Constants.EXIT_CODE_KILL;
            }else{
                DependResult result = getTaskDependResult();
                exitStatusCode = (result == DependResult.SUCCESS) ?
                        Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            exitStatusCode = -1;
        }
    }

    /**
     * get dependent result
     * @return DependResult
     */
    private DependResult getTaskDependResult(){
        List<DependResult> dependResultList = new ArrayList<>();
        for(DependentExecute dependentExecute : dependentTaskList){
            DependResult dependResult = dependentExecute.getModelDependResult(dependentDate);
            dependResultList.add(dependResult);
        }
        DependResult result = DependentUtils.getDependResultForRelation(
                this.dependentParameters.getRelation(), dependResultList
        );
        return result;
    }

    /**
     * judge all dependent tasks finish
     * @return whether all dependent tasks finish
     */
    private boolean allDependentTaskFinish(){
        boolean finish = true;
        for(DependentExecute dependentExecute : dependentTaskList){
            for(Map.Entry<String, DependResult> entry: dependentExecute.getDependResultMap().entrySet()) {
                if(!dependResultMap.containsKey(entry.getKey())){
                    dependResultMap.put(entry.getKey(), entry.getValue());
                    //save depend result to log
                    logger.info("dependent item complete {} {},{}",
                            DEPENDENT_SPLIT, entry.getKey(), entry.getValue().toString());
                }
            }
            if(!dependentExecute.finish(dependentDate)){
                finish = false;
            }
        }
        return finish;
    }


    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        this.cancel = true;
    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }
}
