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
package org.apache.dolphinscheduler.server.worker.task.conditions;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConditionsTask extends AbstractTask {


    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     * process dao
     */
    private ProcessService processService;

    /**
     * taskInstance
     */
    private TaskInstance taskInstance;

    /**
     *
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();


    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext taskExecutionContext
     *
     * @param logger logger
     */
    public ConditionsTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() throws Exception {
        logger.info("conditions task initialize");

        this.processService = SpringApplicationContext.getBean(ProcessService.class);

        this.dependentParameters = JSONUtils.parseObject(taskExecutionContext.
                getDependenceTaskExecutionContext()
                .getDependence(),
                DependentParameters.class);

        this.taskInstance = processService.findTaskInstanceById(taskExecutionContext.getTaskInstanceId());

        if(taskInstance == null){
            throw new Exception("cannot find the task instance!");
        }

        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(taskInstance.getProcessInstanceId());
        for(TaskInstance task : taskInstanceList){
            this.completeTaskList.putIfAbsent(task.getName(), task.getState());
        }
    }

    @Override
    public void handle() throws Exception {

        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT,
                taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        List<DependResult> modelResultList = new ArrayList<>();
        for(DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()){

            List<DependResult> itemDependResult = new ArrayList<>();
            for(DependentItem item : dependentTaskModel.getDependItemList()){
                itemDependResult.add(getDependResultForItem(item));
            }
            DependResult modelResult = DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            modelResultList.add(modelResult);
        }
        DependResult result = DependentUtils.getDependResultForRelation(
                dependentParameters.getRelation(), modelResultList
        );
        logger.info("the conditions task depend result : {}", result);
        exitStatusCode = (result == DependResult.SUCCESS) ?
                Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    private DependResult getDependResultForItem(DependentItem item){

        DependResult dependResult = DependResult.SUCCESS;
        if(!completeTaskList.containsKey(item.getDepTasks())){
            logger.info("depend item: {} have not completed yet.", item.getDepTasks());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        ExecutionStatus executionStatus = completeTaskList.get(item.getDepTasks());
        if(executionStatus != item.getStatus()){
            logger.info("depend item : {} expect status: {}, actual status: {}" ,item.getDepTasks(), item.getStatus().toString(), executionStatus.toString());
            dependResult = DependResult.FAILED;
        }
        logger.info("depend item: {}, depend result: {}",
                item.getDepTasks(), dependResult);
        return dependResult;
    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }
}