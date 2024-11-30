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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.TaskEngine;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicTaskEngineDelegator implements AutoCloseable {

    private final TaskEngine taskEngine;

    private final LogicTaskExecutorFactory logicTaskExecutorFactory;

    private final LogicTaskExecutorLifecycleEventReporter logicTaskExecutorEventReporter;

    public LogicTaskEngineDelegator(final LogicTaskEngineFactory logicTaskEngineFactory,
                                    final LogicTaskExecutorFactory logicTaskExecutorFactory,
                                    final LogicTaskExecutorLifecycleEventReporter logicTaskExecutorEventReporter) {
        this.logicTaskExecutorFactory = logicTaskExecutorFactory;
        this.taskEngine = logicTaskEngineFactory.createTaskEngine();
        this.logicTaskExecutorEventReporter = logicTaskExecutorEventReporter;
    }

    public void start() {
        taskEngine.start();
        logicTaskExecutorEventReporter.start();
        log.info("LogicTaskEngineDelegator started");
    }

    public void dispatchLogicTask(final TaskExecutionContext taskExecutionContext) {
        final ITaskExecutor taskExecutor = logicTaskExecutorFactory.createTaskExecutor(taskExecutionContext);
        taskEngine.submitTask(taskExecutor);
    }

    public void killLogicTask(final int taskInstanceId) {
        taskEngine.killTask(taskInstanceId);
    }

    public void pauseLogicTask(final int taskInstanceId) {
        taskEngine.pauseTask(taskInstanceId);
    }

    public void ackLogicTaskExecutionEvent(final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        logicTaskExecutorEventReporter.receiveTaskExecutorLifecycleEventACK(taskExecutorLifecycleEventAck);
    }

    @Override
    public void close() {
        try (
                final TaskEngine ignore1 = taskEngine;
                final LogicTaskExecutorLifecycleEventReporter ignore2 = logicTaskExecutorEventReporter) {
            log.info("LogicTaskEngineDelegator closed");
        }
    }
}
