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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.server.master.engine.command.CommandEngine;
import org.apache.dolphinscheduler.server.master.runner.MasterTaskExecutorBootstrap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEngine implements AutoCloseable {

    @Autowired
    private TaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    private WorkflowEventBusCoordinator workflowEventBusCoordinator;

    @Autowired
    private MasterTaskExecutorBootstrap masterTaskExecutorBootstrap;

    @Autowired
    private CommandEngine commandEngine;

    public void start() {

        taskGroupCoordinator.start();

        masterTaskExecutorBootstrap.start();

        workflowEventBusCoordinator.start();

        commandEngine.start();

        log.info("WorkflowEngine started");
    }

    @Override
    public void close() throws Exception {
        try (
                final CommandEngine commandEngine1 = commandEngine;
                final WorkflowEventBusCoordinator workflowEventBusCoordinator1 = workflowEventBusCoordinator;
                final MasterTaskExecutorBootstrap masterTaskExecutorBootstrap1 = masterTaskExecutorBootstrap;
                final TaskGroupCoordinator taskGroupCoordinator1 = taskGroupCoordinator) {
            // closed the resource
        }
    }
}
