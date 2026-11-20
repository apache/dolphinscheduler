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

package org.apache.dolphinscheduler.server.master.engine.task.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.base.exception.RemoteTimeoutException;
import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.cluster.ClusterManager;
import org.apache.dolphinscheduler.server.master.cluster.WorkerClusters;
import org.apache.dolphinscheduler.server.master.cluster.loadbalancer.IWorkerLoadBalancer;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchResultUnknownException;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class PhysicalTaskExecutorClientDelegatorTest {

    private static final String WORKER_A = "worker-a:1234";
    private static final String WORKER_B = "worker-b:1234";

    private final PhysicalTaskExecutorClientDelegator delegator = new PhysicalTaskExecutorClientDelegator();
    private final IWorkerLoadBalancer loadBalancer = mock(IWorkerLoadBalancer.class);
    private final WorkerClusters workerClusters = mock(WorkerClusters.class);
    private final IPhysicalTaskExecutorOperator workerA = mock(IPhysicalTaskExecutorOperator.class);
    private final IPhysicalTaskExecutorOperator workerB = mock(IPhysicalTaskExecutorOperator.class);
    private final ITaskExecution execution = mock(ITaskExecution.class);
    private final TaskExecutionContext context = new TaskExecutionContext();
    private final TaskInstance instance = new TaskInstance();
    private MockedStatic<Clients> clients;

    @BeforeEach
    void setUp() {
        final ClusterManager clusterManager = mock(ClusterManager.class);
        when(clusterManager.getWorkerClusters()).thenReturn(workerClusters);
        when(workerClusters.containsWorkerGroup("default")).thenReturn(true);
        when(loadBalancer.select("default")).thenReturn(Optional.of(WORKER_A), Optional.of(WORKER_B));
        ReflectionTestUtils.setField(delegator, "clusterManager", clusterManager);
        ReflectionTestUtils.setField(delegator, "workerLoadBalancer", loadBalancer);
        context.setTaskInstanceId(7);
        context.setTaskName("test-task");
        context.setWorkerGroup("default");
        instance.setId(7);
        instance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        when(execution.getTaskExecutionContext()).thenReturn(context);
        when(execution.getTaskInstance()).thenReturn(instance);

        final Clients.JdkDynamicRpcClientProxyBuilder<IPhysicalTaskExecutorOperator> builder =
                mock(Clients.JdkDynamicRpcClientProxyBuilder.class);
        when(builder.withHost(anyString()))
                .thenAnswer(invocation -> WORKER_A.equals(invocation.getArgument(0)) ? workerA : workerB);
        clients = mockStatic(Clients.class);
        clients.when(() -> Clients.withService(IPhysicalTaskExecutorOperator.class)).thenReturn(builder);
        when(workerB.dispatchTask(any())).thenReturn(TaskExecutorDispatchResponse.success());
    }

    @AfterEach
    void tearDown() {
        clients.close();
    }

    @Test
    void shouldRetryTimedOutDispatchOnOriginalWorker() throws TaskDispatchException {
        when(workerA.dispatchTask(any()))
                .thenThrow(new RemoteTimeoutException(WORKER_A, 1000))
                .thenReturn(TaskExecutorDispatchResponse.success());

        assertThrows(TaskDispatchException.class, () -> delegator.dispatch(execution));
        delegator.dispatch(execution);

        verify(loadBalancer, times(1)).select("default");
        verify(workerA, times(2)).dispatchTask(any());
        verify(workerB, never()).dispatchTask(any());
        assertEquals(WORKER_A, instance.getHost());
    }

    @Test
    void shouldReselectWorkerAfterDefiniteRejection() throws TaskDispatchException {
        when(workerA.dispatchTask(any())).thenReturn(TaskExecutorDispatchResponse.failed("no slot available"));

        assertThrows(TaskDispatchException.class, () -> delegator.dispatch(execution));
        delegator.dispatch(execution);

        verify(loadBalancer, times(2)).select("default");
        verify(workerB).dispatchTask(any());
        assertEquals(WORKER_B, instance.getHost());
    }

    @Test
    void shouldKeepOriginalWorkerWhenRejectionFollowsTimeout() throws TaskDispatchException {
        when(workerA.dispatchTask(any()))
                .thenThrow(new RemoteTimeoutException(WORKER_A, 1000))
                .thenReturn(TaskExecutorDispatchResponse.failed("no slot available"))
                .thenReturn(TaskExecutorDispatchResponse.success());

        assertThrows(TaskDispatchResultUnknownException.class, () -> delegator.dispatch(execution));
        assertThrows(TaskDispatchResultUnknownException.class, () -> delegator.dispatch(execution));
        delegator.dispatch(execution);

        verify(loadBalancer, times(1)).select("default");
        verify(workerA, times(3)).dispatchTask(any());
        verify(workerB, never()).dispatchTask(any());
    }

    @Test
    void shouldRetryOriginalWorkerEvenIfItsGroupDisappears() throws TaskDispatchException {
        when(workerA.dispatchTask(any()))
                .thenThrow(new RemoteTimeoutException(WORKER_A, 1000))
                .thenReturn(TaskExecutorDispatchResponse.success());

        assertThrows(TaskDispatchException.class, () -> delegator.dispatch(execution));
        when(workerClusters.containsWorkerGroup("default")).thenReturn(false);
        delegator.dispatch(execution);

        verify(workerA, times(2)).dispatchTask(any());
        verify(loadBalancer, times(1)).select("default");
    }
}
