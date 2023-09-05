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

import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.dispatch.host.HostManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.execute.TaskExecuteRunnable;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkerTaskDispatcherTest {

    @Test
    public void getTaskInstanceDispatchHost() throws WorkerGroupNotFoundException {
        TaskEventService taskEventService = Mockito.mock(TaskEventService.class);
        MasterConfig masterConfig = Mockito.mock(MasterConfig.class);
        HostManager hostManager = Mockito.mock(HostManager.class);
        Mockito.when(hostManager.select(Mockito.any())).thenReturn(Optional.of(Host.of("localhost:1234")));
        WorkerTaskDispatcher workerTaskDispatcher =
                new WorkerTaskDispatcher(taskEventService, masterConfig, hostManager);

        TaskExecuteRunnable taskExecuteRunnable = Mockito.mock(TaskExecuteRunnable.class);
        Mockito.when(taskExecuteRunnable.getTaskExecutionContext()).thenReturn(new TaskExecutionContext());
        Optional<Host> taskInstanceDispatchHost = workerTaskDispatcher.getTaskInstanceDispatchHost(taskExecuteRunnable);
        Assertions.assertEquals("localhost:1234", taskInstanceDispatchHost.get().getAddress());
    }
}
