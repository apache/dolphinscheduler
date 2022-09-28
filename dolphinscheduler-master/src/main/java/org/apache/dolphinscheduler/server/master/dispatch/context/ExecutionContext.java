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

package org.apache.dolphinscheduler.server.master.dispatch.context;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;

import lombok.Data;

/**
 *  execution context
 */
@Data
public class ExecutionContext {

    /**
     * host
     */
    private Host host;

    /**
     * command
     */
    private final Command command;

    private final TaskInstance taskInstance;

    /**
     * executor type : worker or client
     */
    private final ExecutorType executorType;

    /**
     * worker group
     */
    private final String workerGroup;

    public ExecutionContext(Command command, ExecutorType executorType, TaskInstance taskInstance) {
        this(command, executorType, DEFAULT_WORKER_GROUP, taskInstance);
    }

    public ExecutionContext(Command command, ExecutorType executorType, String workerGroup, TaskInstance taskInstance) {
        this.command = command;
        this.executorType = executorType;
        this.workerGroup = workerGroup;
        this.taskInstance = taskInstance;
    }
}
