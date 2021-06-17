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

import static org.apache.dolphinscheduler.common.Constants.CACHE_REFRESH_TIME_MILLIS;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.cache.TaskInstanceCacheManager;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  taskInstance state manager
 */
@Component
public class TaskInstanceCacheManagerImpl implements TaskInstanceCacheManager {

    /**
     * taskInstance cache
     */
    private Map<Integer,TaskInstance> taskInstanceCache = new ConcurrentHashMap<>();

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * taskInstance cache refresh timer
     */
    private Timer refreshTaskInstanceTimer = null;

    @PostConstruct
    public void init() {
        //issue#5539 add thread to fetch task state from database in a fixed rate
        this.refreshTaskInstanceTimer = new Timer(true);
        refreshTaskInstanceTimer.scheduleAtFixedRate(
                new RefreshTaskInstanceTimerTask(), CACHE_REFRESH_TIME_MILLIS, CACHE_REFRESH_TIME_MILLIS
        );
    }

    @PreDestroy
    public void close() {
        this.refreshTaskInstanceTimer.cancel();
    }

    /**
     * get taskInstance by taskInstance id
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance
     */
    @Override
    public TaskInstance getByTaskInstanceId(Integer taskInstanceId) {
        return taskInstanceCache.computeIfAbsent(taskInstanceId, k -> processService.findTaskInstanceById(taskInstanceId));
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
        taskInstance.setTaskType(taskExecutionContext.getTaskType());
        taskInstance.setExecutePath(taskExecutionContext.getExecutePath());
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
        taskInstance.setId(taskAckCommand.getTaskInstanceId());
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
        taskInstanceCache.put(taskExecuteResponseCommand.getTaskInstanceId(), taskInstance);
    }

    /**
     * remove taskInstance by taskInstanceId
     * @param taskInstanceId taskInstanceId
     */
    @Override
    public void removeByTaskInstanceId(Integer taskInstanceId) {
        taskInstanceCache.remove(taskInstanceId);
    }

    class RefreshTaskInstanceTimerTask extends TimerTask {
        @Override
        public void run() {
            for (Entry<Integer, TaskInstance> taskInstanceEntry : taskInstanceCache.entrySet()) {
                TaskInstance taskInstance = processService.findTaskInstanceById(taskInstanceEntry.getKey());
                if (null != taskInstance && taskInstance.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
                    taskInstanceCache.computeIfPresent(taskInstanceEntry.getKey(), (k, v) -> taskInstance);
                }
            }

        }
    }
}
