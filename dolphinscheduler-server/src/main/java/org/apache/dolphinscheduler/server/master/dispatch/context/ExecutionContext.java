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


import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;

/**
 *  execution context
 */
public class ExecutionContext {

    /**
     * host
     */
    private Host host;

    /**
     *  command
     */
    private final Command command;

    /**
     *  executor type : worker or client
     */
    private final ExecutorType executorType;

    /**
     *  worker group
     */
    private String workerGroup;


    public ExecutionContext(Command command, ExecutorType executorType) {
        this(command, executorType, DEFAULT_WORKER_GROUP);
    }

    public ExecutionContext(Command command, ExecutorType executorType, String workerGroup) {
        this.command = command;
        this.executorType = executorType;
        this.workerGroup = workerGroup;
    }

    public Command getCommand() {
        return command;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }


    public String getWorkerGroup(){
        return this.workerGroup;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
}
