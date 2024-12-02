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

package org.apache.dolphinscheduler.server.master.engine.executor;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.LogicTaskPluginFactoryBuilder;
import org.apache.dolphinscheduler.task.executor.AbstractTaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;
import org.apache.dolphinscheduler.task.executor.TaskExecutorStateMappings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicTaskExecutor extends AbstractTaskExecutor {

    private ILogicTask<? extends AbstractParameters> logicTask;

    private final LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder;

    public LogicTaskExecutor(final LogicTaskExecutorBuilder logicTaskExecutorBuilder) {
        super(logicTaskExecutorBuilder.getTaskExecutionContext(), logicTaskExecutorBuilder.getTaskExecutorEventBus());
        this.logicTaskPluginFactoryBuilder = logicTaskExecutorBuilder.getLogicTaskPluginFactoryBuilder();
    }

    @Override
    protected TaskExecutorState doTrackTaskPluginStatus() {
        return TaskExecutorStateMappings.mapState(logicTask.getTaskExecutionState());
    }

    @SneakyThrows
    @Override
    protected void initializeTaskPlugin() {
        logicTask = logicTaskPluginFactoryBuilder
                .createILogicTaskPluginFactory(taskExecutionContext.getTaskType())
                .createLogicTask(this);
        log.info("Initialized task plugin instance: {} successfully", taskExecutionContext.getTaskType());
    }

    @SneakyThrows
    @Override
    protected void doTriggerTaskPlugin() {
        logicTask.start();
    }

    @SneakyThrows
    @Override
    public void pause() {
        // todo: judge the status, whether the task is running, we should support to pause the task which is not running
        // if the status is initialized, then we can directly change to paused
        if (logicTask != null) {
            logicTask.pause();
        }
    }

    @SneakyThrows
    @Override
    public void kill() {
        if (logicTask != null) {
            logicTask.kill();
        }
    }

    @Override
    public String toString() {
        return "LogicTaskExecutor{" +
                "id=" + taskExecutionContext.getTaskInstanceId() +
                ", name=" + taskExecutionContext.getTaskName() +
                ", stat=" + taskExecutorState.get() +
                '}';
    }
}
