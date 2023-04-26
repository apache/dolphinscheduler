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

package org.apache.dolphinscheduler.server.master.runner.dispatcher;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.dispatch.host.HostManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcClient;
import org.apache.dolphinscheduler.server.master.runner.BaseTaskDispatcher;
import org.apache.dolphinscheduler.server.master.runner.execute.TaskExecuteRunnable;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkerTaskDispatcher extends BaseTaskDispatcher {

    private final HostManager hostManager;

    public WorkerTaskDispatcher(TaskEventService taskEventService,
                                MasterConfig masterConfig,
                                MasterRpcClient masterRpcClient,
                                HostManager hostManager) {
        super(taskEventService, masterConfig, masterRpcClient);
        this.hostManager = checkNotNull(hostManager);
    }

    @Override
    protected Optional<Host> getTaskInstanceDispatchHost(TaskExecuteRunnable taskExecuteRunnable) throws WorkerGroupNotFoundException {
        String workerGroup = taskExecuteRunnable.getTaskExecutionContext().getWorkerGroup();
        return hostManager.select(workerGroup);

    }
}
