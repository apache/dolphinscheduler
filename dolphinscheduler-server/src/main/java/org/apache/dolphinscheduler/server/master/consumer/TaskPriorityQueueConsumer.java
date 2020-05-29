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

package org.apache.dolphinscheduler.server.master.consumer;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetMysqlParameter;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.*;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

/**
 * TaskUpdateQueue consumer
 */
@Component
public class TaskPriorityQueueConsumer extends Thread{

    /**
     * logger of TaskUpdateQueueConsumer
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskPriorityQueueConsumer.class);

    /**
     * taskUpdateQueue
     */
    @Autowired
    private TaskPriorityQueue taskPriorityQueue;

    /**
     * processService
     */
    @Autowired
    private ProcessService processService;

    /**
     * executor dispatcher
     */
    @Autowired
    private ExecutorDispatcher dispatcher;


    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    @PostConstruct
    public void init(){
        super.setName("TaskUpdateQueueConsumerThread");
        super.start();
    }

    @Override
    public void run() {
        List<String> failedDispatchTasks = new ArrayList<>();
        while (Stopper.isRunning()){
            try {
                int fetchTaskNum = masterConfig.getMasterDispatchTaskNumber();
                failedDispatchTasks.clear();
                for(int i = 0; i < fetchTaskNum; i++){
                    if(taskPriorityQueue.size() <= 0){
                        Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                        continue;
                    }
                    // if not task , blocking here
                    String taskPriorityInfo = taskPriorityQueue.take();
                    TaskPriority taskPriority = TaskPriority.of(taskPriorityInfo);
                    boolean dispatchResult = dispatch(taskPriority.getTaskId());
                    if(!dispatchResult){
                        failedDispatchTasks.add(taskPriorityInfo);
                    }
                }
                for(String dispatchFailedTask : failedDispatchTasks){
                    taskPriorityQueue.put(dispatchFailedTask);
                }
            }catch (Exception e){
                logger.error("dispatcher task error",e);
            }
        }
    }


    /**
     * dispatch task
     *
     * @param taskInstanceId taskInstanceId
     * @return result
     */
    private boolean dispatch(int taskInstanceId){
        boolean result = false;
        try {
            TaskExecutionContext context = getTaskExecutionContext(taskInstanceId);
            ExecutionContext executionContext = new ExecutionContext(context.toCommand(), ExecutorType.WORKER, context.getWorkerGroup());

            if (taskInstanceIsFinalState(taskInstanceId)){
                // when task finish, ignore this task, there is no need to dispatch anymore
                return true;
            }else{
                result = dispatcher.dispatch(executionContext);
            }
        } catch (ExecuteException e) {
            logger.error("dispatch error",e);
        }
        return result;
    }


    /**
     * taskInstance is final state
     * success，failure，kill，stop，pause，threadwaiting is final state
     * @param taskInstanceId taskInstanceId
     * @return taskInstance is final state
     */
    public Boolean taskInstanceIsFinalState(int taskInstanceId){
        TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceId);
        return taskInstance.getState().typeIsFinished();
    }

    /**
     * get TaskExecutionContext
     * @param taskInstanceId taskInstanceId
     * @return TaskExecutionContext
     */
    protected TaskExecutionContext getTaskExecutionContext(int taskInstanceId){
        TaskInstance taskInstance = processService.getTaskInstanceDetailByTaskId(taskInstanceId);

        // task type
        TaskType taskType = TaskType.valueOf(taskInstance.getTaskType());

        // task node
        TaskNode taskNode = JSONObject.parseObject(taskInstance.getTaskJson(), TaskNode.class);

        Integer userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);

        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            processService.changeTaskState(ExecutionStatus.FAILURE,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    null,
                    null,
                    taskInstance.getId());
            return null;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstanceId(taskInstance.getProcessInstanceId());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());
        taskInstance.setExecutePath(getExecLocalPath(taskInstance));
        taskInstance.setResources(getResourceFullNames(taskNode));


        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        ProcedureTaskExecutionContext procedureTaskExecutionContext = new ProcedureTaskExecutionContext();
        SqoopTaskExecutionContext sqoopTaskExecutionContext = new SqoopTaskExecutionContext();


        // SQL task
        if (taskType == TaskType.SQL){
            setSQLTaskRelation(sqlTaskExecutionContext, taskNode);

        }

        // DATAX task
        if (taskType == TaskType.DATAX){
            setDataxTaskRelation(dataxTaskExecutionContext, taskNode);
        }


        // procedure task
        if (taskType == TaskType.PROCEDURE){
            setProcedureTaskRelation(procedureTaskExecutionContext, taskNode);
        }

        if (taskType == TaskType.SQOOP){
            setSqoopTaskRelation(sqoopTaskExecutionContext,taskNode);
        }


        return TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildSQLTaskRelatedInfo(sqlTaskExecutionContext)
                .buildDataxTaskRelatedInfo(dataxTaskExecutionContext)
                .buildProcedureTaskRelatedInfo(procedureTaskExecutionContext)
                .buildSqoopTaskRelatedInfo(sqoopTaskExecutionContext)
                .create();
    }

    /**
     * set procedure task relation
     * @param procedureTaskExecutionContext procedureTaskExecutionContext
     * @param taskNode taskNode
     */
    private void setProcedureTaskRelation(ProcedureTaskExecutionContext procedureTaskExecutionContext, TaskNode taskNode) {
        ProcedureParameters procedureParameters = JSONObject.parseObject(taskNode.getParams(), ProcedureParameters.class);
        int datasourceId = procedureParameters.getDatasource();
        DataSource datasource = processService.findDataSourceById(datasourceId);
        procedureTaskExecutionContext.setConnectionParams(datasource.getConnectionParams());
    }

    /**
     * set datax task relation
     * @param dataxTaskExecutionContext dataxTaskExecutionContext
     * @param taskNode taskNode
     */
    private void setDataxTaskRelation(DataxTaskExecutionContext dataxTaskExecutionContext, TaskNode taskNode) {
        DataxParameters dataxParameters = JSONObject.parseObject(taskNode.getParams(), DataxParameters.class);

        DataSource dataSource = processService.findDataSourceById(dataxParameters.getDataSource());
        DataSource dataTarget = processService.findDataSourceById(dataxParameters.getDataTarget());


        if (dataSource != null){
            dataxTaskExecutionContext.setDataSourceId(dataxParameters.getDataSource());
            dataxTaskExecutionContext.setSourcetype(dataSource.getType().getCode());
            dataxTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
        }

        if (dataTarget != null){
            dataxTaskExecutionContext.setDataTargetId(dataxParameters.getDataTarget());
            dataxTaskExecutionContext.setTargetType(dataTarget.getType().getCode());
            dataxTaskExecutionContext.setTargetConnectionParams(dataTarget.getConnectionParams());
        }
    }


    /**
     * set datax task relation
     * @param sqoopTaskExecutionContext sqoopTaskExecutionContext
     * @param taskNode taskNode
     */
    private void setSqoopTaskRelation(SqoopTaskExecutionContext sqoopTaskExecutionContext, TaskNode taskNode) {
        SqoopParameters sqoopParameters = JSONObject.parseObject(taskNode.getParams(), SqoopParameters.class);

        SourceMysqlParameter sourceMysqlParameter = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceMysqlParameter.class);
        TargetMysqlParameter targetMysqlParameter = JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetMysqlParameter.class);

        DataSource dataSource = processService.findDataSourceById(sourceMysqlParameter.getSrcDatasource());
        DataSource dataTarget = processService.findDataSourceById(targetMysqlParameter.getTargetDatasource());

        if (dataSource != null){
            sqoopTaskExecutionContext.setDataSourceId(dataSource.getId());
            sqoopTaskExecutionContext.setSourcetype(dataSource.getType().getCode());
            sqoopTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
        }

        if (dataTarget != null){
            sqoopTaskExecutionContext.setDataTargetId(dataTarget.getId());
            sqoopTaskExecutionContext.setTargetType(dataTarget.getType().getCode());
            sqoopTaskExecutionContext.setTargetConnectionParams(dataTarget.getConnectionParams());
        }
    }

    /**
     * set SQL task relation
     * @param sqlTaskExecutionContext sqlTaskExecutionContext
     * @param taskNode taskNode
     */
    private void setSQLTaskRelation(SQLTaskExecutionContext sqlTaskExecutionContext, TaskNode taskNode) {
        SqlParameters sqlParameters = JSONObject.parseObject(taskNode.getParams(), SqlParameters.class);
        int datasourceId = sqlParameters.getDatasource();
        DataSource datasource = processService.findDataSourceById(datasourceId);
        sqlTaskExecutionContext.setConnectionParams(datasource.getConnectionParams());

        // whether udf type
        boolean udfTypeFlag = EnumUtils.isValidEnum(UdfType.class, sqlParameters.getType())
                && StringUtils.isNotEmpty(sqlParameters.getUdfs());

        if (udfTypeFlag){
            String[] udfFunIds = sqlParameters.getUdfs().split(",");
            int[] udfFunIdsArray = new int[udfFunIds.length];
            for(int i = 0 ; i < udfFunIds.length;i++){
                udfFunIdsArray[i]=Integer.parseInt(udfFunIds[i]);
            }

            List<UdfFunc> udfFuncList = processService.queryUdfFunListByids(udfFunIdsArray);
            sqlTaskExecutionContext.setUdfFuncList(udfFuncList);
        }
    }

    /**
     * get execute local path
     *
     * @return execute local path
     */
    private String getExecLocalPath(TaskInstance taskInstance){
        return FileUtils.getProcessExecDir(taskInstance.getProcessDefine().getProjectId(),
                taskInstance.getProcessDefine().getId(),
                taskInstance.getProcessInstance().getId(),
                taskInstance.getId());
    }


    /**
     *  whehter tenant is null
     * @param tenant tenant
     * @param taskInstance taskInstance
     * @return result
     */
    private boolean verifyTenantIsNull(Tenant tenant, TaskInstance taskInstance) {
        if(tenant == null){
            logger.error("tenant not exists,process instance id : {},task instance id : {}",
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId());
            return true;
        }
        return false;
    }

    /**
     * get resource full name list
     */
    private List<String> getResourceFullNames(TaskNode taskNode) {
        List<String> resourceFullNameList = new ArrayList<>();
        AbstractParameters baseParam = TaskParametersUtils.getParameters(taskNode.getType(), taskNode.getParams());

        if (baseParam != null) {
            List<ResourceInfo> projectResourceFiles = baseParam.getResourceFilesList();
            if (projectResourceFiles != null) {

                // filter the resources that the resource id equals 0
                Set<ResourceInfo> oldVersionResources = projectResourceFiles.stream().filter(t -> t.getId() == 0).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(oldVersionResources)) {
                    resourceFullNameList.addAll(oldVersionResources.stream().map(resource -> resource.getRes()).collect(Collectors.toSet()));
                }

                // get the resource id in order to get the resource names in batch
                Stream<Integer> resourceIdStream = projectResourceFiles.stream().map(resourceInfo -> resourceInfo.getId());
                Set<Integer> resourceIdsSet = resourceIdStream.collect(Collectors.toSet());

                if (CollectionUtils.isNotEmpty(resourceIdsSet)) {
                    Integer[] resourceIds = resourceIdsSet.toArray(new Integer[resourceIdsSet.size()]);

                    List<Resource> resources = processService.listResourceByIds(resourceIds);
                    resourceFullNameList.addAll(resources.stream()
                            .map(resourceInfo -> resourceInfo.getFullName())
                            .collect(Collectors.toList()));
                }
            }
        }

        return resourceFullNameList;
    }
}
