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

package org.apache.dolphinscheduler.server.builder;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;

/**
 *  TaskExecutionContext builder
 */
public class TaskExecutionContextBuilder {

    public static TaskExecutionContextBuilder get(){
        return new TaskExecutionContextBuilder();
    }

    private TaskExecutionContext taskExecutionContext =  new TaskExecutionContext();

    /**
     * build taskInstance related info
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskInstanceRelatedInfo(TaskInstance taskInstance){
        taskExecutionContext.setTaskInstanceId(taskInstance.getId());
        taskExecutionContext.setTaskName(taskInstance.getName());
        taskExecutionContext.setStartTime(taskInstance.getStartTime());
        taskExecutionContext.setTaskType(taskInstance.getTaskType());
        taskExecutionContext.setLogPath(taskInstance.getLogPath());
        taskExecutionContext.setExecutePath(taskInstance.getExecutePath());
        taskExecutionContext.setTaskJson(taskInstance.getTaskJson());
        taskExecutionContext.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        return this;
    }


    /**
     * build processInstance related info
     *
     * @param processInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessInstanceRelatedInfo(ProcessInstance processInstance){
        taskExecutionContext.setProcessInstanceId(processInstance.getId());
        taskExecutionContext.setScheduleTime(processInstance.getScheduleTime());
        taskExecutionContext.setGlobalParams(processInstance.getGlobalParams());
        taskExecutionContext.setExecutorId(processInstance.getExecutorId());
        taskExecutionContext.setCmdTypeIfComplement(processInstance.getCmdTypeIfComplement().getCode());
        taskExecutionContext.setTenantCode(processInstance.getTenantCode());
        taskExecutionContext.setQueue(processInstance.getQueue());
        return this;
    }

    /**
     * build processDefinition related info
     *
     * @param processDefinition processDefinition
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessDefinitionRelatedInfo(ProcessDefinition processDefinition){
        taskExecutionContext.setProcessDefineId(processDefinition.getId());
        taskExecutionContext.setProjectId(processDefinition.getProjectId());
        return this;
    }

    /**
     * create
     *
     * @return taskExecutionContext
     */
    public TaskExecutionContext create(){
        return taskExecutionContext;
    }

}
