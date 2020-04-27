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
package org.apache.dolphinscheduler.server.master.cache.impl;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.cache.TaskInstanceCacheManager;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  taskInstance state manager
 */
@Component
public class TaskInstanceCacheManagerImpl implements TaskInstanceCacheManager {

    /**
     * taskInstance caceh
     */
    private Map<Integer,TaskInstance> taskInstanceCache = new ConcurrentHashMap<>();

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;


    /**
     * get taskInstance by taskInstance id
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance
     */
    @Override
    public TaskInstance getByTaskInstanceId(Integer taskInstanceId) {
        TaskInstance taskInstance = taskInstanceCache.get(taskInstanceId);
        if (taskInstance == null){
            taskInstance = processService.findTaskInstanceById(taskInstanceId);
            taskInstanceCache.put(taskInstanceId,taskInstance);
        }
        return taskInstance;
    }

    /**
     * cache taskInstance
     *
     * @param taskExecutionContext taskExecutionContext
     */
    @Override
    public void cacheTaskInstance(TaskExecutionContext taskExecutionContext) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(taskExecutionContext.getTaskInstanceId());
        taskInstance.setName(taskExecutionContext.getTaskName());
        taskInstance.setStartTime(taskExecutionContext.getStartTime());
        taskInstance.setTaskType(taskInstance.getTaskType());
        taskInstance.setExecutePath(taskInstance.getExecutePath());
        taskInstance.setTaskJson(taskInstance.getTaskJson());
        taskInstanceCache.put(taskExecutionContext.getTaskInstanceId(), taskInstance);
    }

    /**
     * cache taskInstance
     *
     * @param taskAckCommand taskAckCommand
     */
    @Override
    public void cacheTaskInstance(TaskExecuteAckCommand taskAckCommand) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(ExecutionStatus.of(taskAckCommand.getStatus()));
        taskInstance.setStartTime(taskAckCommand.getStartTime());
        taskInstance.setHost(taskAckCommand.getHost());
        taskInstance.setExecutePath(taskAckCommand.getExecutePath());
        taskInstance.setLogPath(taskAckCommand.getLogPath());
        taskInstanceCache.put(taskAckCommand.getTaskInstanceId(), taskInstance);
    }

    /**
     * cache taskInstance
     *
     * @param taskExecuteResponseCommand taskExecuteResponseCommand
     */
    @Override
    public void cacheTaskInstance(TaskExecuteResponseCommand taskExecuteResponseCommand) {
        TaskInstance taskInstance = getByTaskInstanceId(taskExecuteResponseCommand.getTaskInstanceId());
        taskInstance.setState(ExecutionStatus.of(taskExecuteResponseCommand.getStatus()));
        taskInstance.setEndTime(taskExecuteResponseCommand.getEndTime());
    }

    /**
     * remove taskInstance by taskInstanceId
     * @param taskInstanceId taskInstanceId
     */
    @Override
    public void removeByTaskInstanceId(Integer taskInstanceId) {
        taskInstanceCache.remove(taskInstanceId);
    }
}
