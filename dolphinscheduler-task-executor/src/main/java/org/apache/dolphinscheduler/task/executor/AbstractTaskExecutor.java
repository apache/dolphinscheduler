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

package org.apache.dolphinscheduler.task.executor;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorRuntimeException;

import java.util.concurrent.atomic.AtomicReference;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTaskExecutor implements ITaskExecutor {

    private static final long DEFAULT_TRACK_INTERVAL = 10_000;

    private long latestStateTrackTime;

    private boolean startFlag;

    protected final AtomicReference<TaskExecutorState> taskExecutorState = new AtomicReference<>();

    protected final TaskExecutionContext taskExecutionContext;

    protected final TaskExecutorEventBus taskExecutorEventBus;

    public AbstractTaskExecutor(final TaskExecutionContext taskExecutionContext,
                                final TaskExecutorEventBus taskExecutorEventBus) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskExecutorEventBus = taskExecutorEventBus;
        this.latestStateTrackTime = 0;
        this.startFlag = false;
        transitTaskExecutorState(TaskExecutorState.INITIALIZED);
    }

    @SneakyThrows
    @Override
    public void start() {
        if (startFlag) {
            throw new TaskExecutorRuntimeException(this + " has already started");
        }
        startFlag = true;
        TaskInstanceLogHeader.printInitializeTaskContextHeader();
        initializeTaskContext();

        publishTaskRunningEvent();

        TaskInstanceLogHeader.printLoadTaskInstancePluginHeader();
        initializeTaskPlugin();

        TaskInstanceLogHeader.printExecuteTaskHeader();

        if (taskExecutionContext.getDryRun() == Flag.YES.getCode()) {
            log.info("The task: {} is dryRun model, skip the trigger stage.", taskExecutionContext.getTaskName());
            transitTaskExecutorState(TaskExecutorState.SUCCEEDED);
            return;
        }

        doTriggerTaskPlugin();
    }

    @Override
    public boolean isStarted() {
        return startFlag;
    }

    @Override
    public TaskExecutorState trackTaskExecutorState() {
        final TaskExecutorState state = taskExecutorState.get();
        if (state.isFinalState()) {
            return state;
        }
        latestStateTrackTime = System.currentTimeMillis();
        transitTaskExecutorState(doTrackTaskPluginStatus());
        return taskExecutorState.get();
    }

    @Override
    public long getRemainingTrackDelay() {
        if (latestStateTrackTime == 0) {
            return 0;
        }
        return latestStateTrackTime + DEFAULT_TRACK_INTERVAL - System.currentTimeMillis();
    }

    @Override
    public TaskExecutorEventBus getTaskExecutorEventBus() {
        return taskExecutorEventBus;
    }

    @Override
    public TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    protected void transitTaskExecutorState(final TaskExecutorState taskExecutorState) {
        this.taskExecutorState.set(taskExecutorState);
    }

    protected abstract TaskExecutorState doTrackTaskPluginStatus();

    protected abstract void initializeTaskPlugin();

    protected abstract void doTriggerTaskPlugin();

    protected void initializeTaskContext() {
        log.info("Begin to initialize taskContext");
        taskExecutionContext.setStartTime(System.currentTimeMillis());
        log.info("End initialize taskContext {}", JSONUtils.toPrettyJsonString(taskExecutionContext));
    }

    private void publishTaskRunningEvent() {
        transitTaskExecutorState(TaskExecutorState.RUNNING);
        taskExecutorEventBus.publish(TaskExecutorStartedLifecycleEvent.of(this));
    }

}
