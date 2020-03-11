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
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.EnumUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskPriority;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutorDispatcher;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.ExecuteException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskUpdateQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * TaskUpdateQueue consumer
 */
@Component
public class TaskUpdateQueueConsumer extends Thread{

    /**
     * logger of TaskUpdateQueueConsumer
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskUpdateQueueConsumer.class);

    /**
     * taskUpdateQueue
     */
    @Autowired
    private TaskUpdateQueue taskUpdateQueue;

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

    @PostConstruct
    public void init(){
        super.setName("TaskUpdateQueueConsumerThread");
        super.start();
    }

    @Override
    public void run() {
        while (Stopper.isRunning()){
            try {
                if (taskUpdateQueue.size() == 0){
                    continue;
                }
                String taskPriorityInfo = taskUpdateQueue.take();
                TaskPriority taskPriority = TaskPriority.of(taskPriorityInfo);
                dispatch(taskPriority.getTaskId());
            }catch (Exception e){
                logger.error("dispatcher task error",e);
            }
        }
    }


    /**
     * TODO dispatch task
     *
     * @param taskInstanceId taskInstanceId
     * @return result
     */
    private Boolean dispatch(int taskInstanceId){
        TaskExecutionContext context = getTaskExecutionContext(taskInstanceId);
        ExecutionContext executionContext = new ExecutionContext(context.toCommand(), ExecutorType.WORKER, context.getWorkerGroup());
        try {
            return dispatcher.dispatch(executionContext);
        } catch (ExecuteException e) {
            logger.error("execute exception", e);
            return false;
        }

    }

    /**
     * get TaskExecutionContext
     * @param taskInstanceId taskInstanceId
     * @return TaskExecutionContext
     */
    protected TaskExecutionContext getTaskExecutionContext(int taskInstanceId){
        TaskInstance taskInstance = processService.getTaskInstanceDetailByTaskId(taskInstanceId);

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

        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();

        TaskType taskType = TaskType.valueOf(taskInstance.getTaskType());
        if (taskType == TaskType.SQL){
            TaskNode taskNode = JSONObject.parseObject(taskInstance.getTaskJson(), TaskNode.class);
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

        if (taskType == TaskType.DATAX){

        }


        return TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildSQLTaskRelatedInfo(sqlTaskExecutionContext)
                .buildDataxTaskRelatedInfo(dataxTaskExecutionContext)
                .create();
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
}
