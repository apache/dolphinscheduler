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

package org.apache.dolphinscheduler.server.master.cluster;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.server.master.runner.WorkerGroupTaskDispatchManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class ThreadCreatingAndDestroyingWorkerGroupListenerTest {

    @Mock
    private WorkerGroupTaskDispatchManager workerGroupTaskDispatchManager;

    @InjectMocks
    private ThreadCreatingAndDestroyingWorkerGroupListener threadCreatingAndDestroyingWorkerGroupListener;

    private WorkerGroup workerGroup1;
    private WorkerGroup workerGroup2;

    @BeforeEach
    public void setUp() {

        workerGroup1 = new WorkerGroup();
        workerGroup1.setName("workerGroup1");

        workerGroup2 = new WorkerGroup();
        workerGroup2.setName("workerGroup2");
    }

    @Test
    public void testOnWorkerGroupAdd() {
        List<WorkerGroup> workerGroups = Arrays.asList(workerGroup1, workerGroup2);

        threadCreatingAndDestroyingWorkerGroupListener.onWorkerGroupAdd(workerGroups);

        verify(workerGroupTaskDispatchManager, times(1)).addWorkerGroup(workerGroup1.getName());
        verify(workerGroupTaskDispatchManager, times(1)).addWorkerGroup(workerGroup2.getName());
    }

    @Test
    public void testOnWorkerGroupDelete() throws Exception {
        List<WorkerGroup> workerGroups = Arrays.asList(workerGroup1, workerGroup2);

        threadCreatingAndDestroyingWorkerGroupListener.onWorkerGroupDelete(workerGroups);

        verify(workerGroupTaskDispatchManager, times(1)).stopWorkerGroup(workerGroup1.getName());
        verify(workerGroupTaskDispatchManager, times(1)).stopWorkerGroup(workerGroup2.getName());
    }

    @Test
    public void testOnWorkerGroupDeleteWithException() throws Exception {
        List<WorkerGroup> workerGroups = Collections.singletonList(workerGroup1);

        doThrow(new RuntimeException("Error stopping worker group")).when(workerGroupTaskDispatchManager)
                .stopWorkerGroup(anyString());

        threadCreatingAndDestroyingWorkerGroupListener.onWorkerGroupDelete(workerGroups);

        verify(workerGroupTaskDispatchManager, times(1)).stopWorkerGroup(workerGroup1.getName());
    }
}
